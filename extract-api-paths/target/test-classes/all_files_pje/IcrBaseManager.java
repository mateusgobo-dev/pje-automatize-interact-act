package br.com.infox.cliente.home.icrrefactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.Component;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante;
import br.jus.pje.nucleo.util.DateUtil;

public abstract class IcrBaseManager<T extends InformacaoCriminalRelevante>{

	protected EntityManager getEntityManager(){
		return (EntityManager) Component.getInstance("entityManager");
	}

	public abstract Boolean possuiDataPublicacao();

	public abstract Date getDtPublicacao(T entity);

	public abstract void setDtPublicacao(T entity, Date data);

	protected void prePersist(T entity) throws IcrValidationException{
		if (entity.getProcessoParte() == null){
			throw new IcrValidationException(
					"informacaoCriminalRelevante.parteNaoInformada");
		}
		if (entity.getProcessoEventoList() == null
			|| entity.getProcessoEventoList().isEmpty()){
			throw new IcrValidationException(
					"informacaoCriminalRelevante.movimentacaoNaoInformada");
		}
		validaDataDaSentenca(entity.getData());
		if (possuiDataPublicacao()
			&& DateUtil.isDataMenor(getDtPublicacao(entity),
					entity.getData())){
			throw new IcrValidationException(getMensagemDataPublicacaoInvalida());
		}
		if (possuiDataPublicacao() 
				&& DateUtil.isDataMaior(getDtPublicacao(entity), DateUtil.getDataAtual())){
				throw new IcrValidationException(
						"informacaoCriminalRelevante.dataPublicacaoMaiorDataAtual");
			}
	}

	protected String getMensagemDataPublicacaoInvalida() {
		return "informacaoCriminalRelevante.dataPublicacaoInvalida";
	}

	protected void validaDataDaSentenca(Date data)
			throws IcrValidationException{
		if (DateUtil.isDataMaior(data, DateUtil.getDataAtual())){
			throw new IcrValidationException(
					"informacaoCriminalRelevante.dataInvalida");
		}
	}

	protected void preInactive(T entity) throws IcrValidationException{
	}
	
	/**
	 * Verifica as regras de validação da icr
	 * 
	 * @param entity
	 * @throws IcrValidationException caso falhe alguma regra de validação
	 */
	public void validate(T entity) throws IcrValidationException{
		prePersist(entity);
	}

	public void inactive(T entity) throws IcrValidationException{
		preInactive(entity);
		entity.setAtivo(false);
		getEntityManager().persist(entity);
		getEntityManager().flush();
	}

	public void persist(T entity) throws IcrValidationException{
		prePersist(entity);
		doPersist(entity);
		getEntityManager().flush();
	}

	protected void doPersist(T entity) throws IcrValidationException{
		ensureUniqueness(entity);
		getEntityManager().persist(entity);
		entity.getProcessoParte().getInformacaoCriminalRelevanteList()
				.add(entity);
	}

	public boolean exists(T entity){
		try{
			ensureUniqueness(entity);
		} catch (IcrValidationException e){
			return true;
		}
		return false;
	}

	public void persistAll(List<T> entityList) throws IcrValidationException{
		if (entityList != null){
			for (T entity : entityList){
				prePersist(entity);
				doPersist(entity);
			}
			getEntityManager().flush();
		}
	}

	/**
	 * Recarrega a entidade cancelando todas as alterações realizadas
	 * 
	 * @param entity
	 */
	public void refresh(T entity){
		getEntityManager().refresh(entity);
	}

	/**
	 * Lógica de unicidade
	 * 
	 * @param entity
	 * @throws IcrValidationException
	 */
	protected void ensureUniqueness(T entity) throws IcrValidationException{
		StringBuilder sb = new StringBuilder();
		if (entity.getId() == null){ // PERSIST
			sb.append("SELECT o FROM InformacaoCriminalRelevante o where o.ativo = true");
		}
		else if (entity.getId() != null){ // UPDATE
			sb.append("SELECT o FROM InformacaoCriminalRelevante o where o.ativo = true and o.id <> "
				+ entity.getId());
		}
		sb.append(" and o.tipo.codigo = :codigo");
		sb.append(" and o.processoParte.idProcessoParte =  :idProcessoParte");
		sb.append(" and o.data = :dataIcr ");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("codigo", entity.getTipo().getCodigo());
		query.setParameter("idProcessoParte", entity.getProcessoParte()
				.getIdProcessoParte());
		query.setParameter("dataIcr", entity.getData());
		if (!query.getResultList().isEmpty()){
			throw new IcrValidationException(
					"Registro informado já cadastrado no sistema.");
		}
	}

	@SuppressWarnings({"unchecked", "deprecation"})
	public List<ProcessoEvento> getMovimentacoes( ProcessoTrf processoTrf,
			Date dataInicio, Date dataFim, Evento movimentacaoSelecionada)
			throws IcrValidationException{
		StringBuilder sql = new StringBuilder(1000);
		sql.append(" select distinct pe ");
		sql.append(" from 	ProcessoEvento pe ");
		sql.append(" 		inner join pe.evento ev ");
		sql.append(" 		inner join pe.processoDocumento pd ");
		sql.append(" 		inner join pd.tipoProcessoDocumento dpd ");
		sql.append(" where ");
		if (dataInicio != null && dataFim != null){
			if (dataFim.before(dataInicio)){
				throw new IcrValidationException(
						"Período inválido. Data inicial menor do que a data final.");
			}
			else{
				sql.append(" ( ");
				sql.append(" 		pe.dataAtualizacao >= '" + dataInicio + "' ");
				sql.append(" 		and ");
				sql.append(" 		pe.dataAtualizacao <= '"
					+ dataFim.toString().replace("00:00:00", "23:59:59")
					+ "' ");
				sql.append(" ) ");
				sql.append(" 		and ");
			}
		}
		if (movimentacaoSelecionada != null){ 
				//&& !movimentacaoSelecionada.getMovimento().equals("")){
			//sql.append(" 		lower(pe.evento.evento) like lower('%"
			sql.append(" 		lower(pe.evento.caminhoCompleto) like lower('%"
				+ movimentacaoSelecionada.toString() + "%') ");
			sql.append(" 		and ");
		}
		sql.append(" 		pe.processo.idProcesso = "
			+ processoTrf.getProcesso().getIdProcesso());
		sql.append("order by pe.dataAtualizacao DESC");
		Query q = getEntityManager().createQuery(sql.toString());
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<TipoInformacaoCriminalRelevante> getTipoInformacaoCriminalRelevanteList(){
		StringBuilder sql = new StringBuilder();
		sql.append("select o from TipoInformacaoCriminalRelevante o where o.inAtivo = true");
		Query q = getEntityManager().createQuery(sql.toString());
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	/**
	 * Para cada reu na lista de reus, persiste uma nova icr baseada nos dados do parametro icrBase
	 * Utilizado na INCLUSÃO de icrs, quando é possivel selecionar mais de um reu e no final ocore inclusões em lote.
	 * @param entity
	 * @param reus
	 * @return
	 */
	public List<T> persistAll(T icrBase, List<ProcessoParte> reus)
			throws IcrValidationException{
		List<T> icrCopyList = new ArrayList<T>(0);

		if (reus != null){
			for (ProcessoParte reu : reus){
				Map<Object, Object> deParaMap = new HashMap<Object, Object>();
				icrBase.setProcessoParte(reu);
				icrCopyList.add((T) clone(icrBase, deParaMap));
			}
			persistAll(icrCopyList);
		}

		return icrCopyList;
	}

	@SuppressWarnings("unchecked")
	/**
	 * Retorna o clone da entidade source mantendo a mesmas referências para associações managed e 
	 * criando outras instancias para novas associações (transient) 
	 * @param source
	 * @param deParaMap
	 * @return
	 * @throws Exception
	 */
	private Object clone(Object source, Map<Object, Object> deParaMap){
		Object target = null;
		if (source != null){
			if (deParaMap.containsKey(source)){
				return deParaMap.get(source);
			}

			try{
				target = source.getClass().newInstance();
				PropertyUtils.copyProperties(target, source);
				deParaMap.put(source, target);
				for (Method method : source.getClass().getMethods()){
					// Verificar as associações do tipo ManyToOne ou de Coleções
					for (Annotation annotation : method.getAnnotations()){
						if (ManyToOne.class.isAssignableFrom(annotation
								.getClass())){
							Method getterMethod = method;
							Method copySetterMethod = target.getClass()
									.getMethod(
											method.getName().replace("get",
													"set"),
											getterMethod.getReturnType());
							Object value = getterMethod.invoke(source);

							if (deParaMap.containsKey(value)){
								copySetterMethod.invoke(target,
										deParaMap.get(value));
								continue;
							}

							// se entidade for "MANAGED" então manter o valor
							// para as cópias
							if (getEntityManager().contains(value)){
								copySetterMethod.invoke(target, value);
								deParaMap.put(value, value);
							}

							// senão criar novo clone da entidade
							else{
								copySetterMethod.invoke(target,
										clone(value, deParaMap));
							}
						}
						// se method retornar uma collection, copiar cada item
						else if (Collection.class.isAssignableFrom(method
								.getReturnType())
							&& (OneToMany.class.isAssignableFrom(annotation
									.getClass()) || ManyToMany.class
									.isAssignableFrom(annotation.getClass()))){
							Collection<Object> collection = (Collection<Object>) method
									.invoke(source);
							Method copyGetterMethod = target.getClass()
									.getMethod(method.getName());
							Method copySetterMethod = target.getClass()
									.getMethod(
											method.getName().replace("get",
													"set"),
											copyGetterMethod.getReturnType());
							copySetterMethod.invoke(target, collection
									.getClass().newInstance());
							for (Object item : collection){
								if (deParaMap.containsKey(item)){
									((Collection<Object>) copyGetterMethod
											.invoke(target)).add(deParaMap
											.get(item));
									continue;
								}
								// se entidade for "MANAGED" então manter o
								// valor para as cópias
								if (getEntityManager().contains(item)){
									((Collection<Object>) copyGetterMethod
											.invoke(target)).add(item);
									deParaMap.put(item, item);
								}
								// senão criar novo clone da entidade
								else{
									((Collection<Object>) copyGetterMethod
											.invoke(target)).add(clone(item,
											deParaMap));
								}
							}
						}
					}
				}
			} catch (Exception e){
				throw new RuntimeException(e);
			}

		}

		return target;
	}
}
