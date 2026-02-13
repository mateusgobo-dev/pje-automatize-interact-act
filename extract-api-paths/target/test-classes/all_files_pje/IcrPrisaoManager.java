package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import br.jus.pje.nucleo.entidades.EstabelecimentoPrisional;
import br.jus.pje.nucleo.entidades.IcrPrisao;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.TipoPrisaoEnum;
import br.jus.pje.nucleo.enums.TipoSolturaEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name("icrPRIManager")
public class IcrPrisaoManager extends InformacaoCriminalRelevanteManager<IcrPrisao>{

	private List<IcrPrisao> prisoesEncerrar = new ArrayList<IcrPrisao>(0);

	public List<IcrPrisao> getPrisoesEncerrar(){
		return prisoesEncerrar;
	}

	public void setPrisoesEncerrar(List<IcrPrisao> prisoesEncerrar){
		this.prisoesEncerrar = prisoesEncerrar;
	}

	@Override
	protected void prePersist(IcrPrisao entity) throws IcrValidationException{
		super.prePersist(entity);
		/*
		 * So permite cadastrar como tipo 'Temporária', se a ultima soltura for do tipo 'Final do Prazo da Temporária'
		 */
		if (entity.getUltimaSoltura() != null){
			if (entity.getUltimaSoltura().getInTipoSoltura().equals(TipoSolturaEnum.FPT)){
				if (!entity.getInTipoPrisao().equals(TipoPrisaoEnum.TMP)){
					throw new IcrValidationException("icrPrisao.tipoImcompativelComTipoSoltura");
				}
			}
		}
		if (entity.getProcessoParte() == null){
			throw new IcrValidationException("Selecione o Réu do Processo");
		}
		for (IcrPrisao prisaoEncerrar : getPrisoesEncerrar()){
			if (prisaoEncerrar.getProcessoParte().equals(entity.getProcessoParte())){
				validarEncerramentoPrisao(prisaoEncerrar);
				entity.setPrisaoEncerrada(prisaoEncerrar);
				getEntityManager().persist(prisaoEncerrar);
			}
		}
	}

	@Override
	protected void preInactive(IcrPrisao entity) throws IcrValidationException{
		if (entity.possuiTransferenciaFugaSoltura()){
			throw new IcrValidationException("A prisão não pode ser excluída. "
				+ "Existem transferências, fugas e/ou solturas vinculadas a ela");
		}
		if (entity.getDtEncerramentoPrisao() != null && existePrisaoVinculada(entity)){
			throw new IcrValidationException("A prisão não pode ser excluída. "
				+ "Existe outra prisão vinculada a ela");
		}
		if (entity.getPrisaoEncerrada() != null){
			entity.getPrisaoEncerrada().setDtEncerramentoPrisao(null);
			entity.getPrisaoEncerrada().setDsMotivoEncerramento(null);
			entity.getPrisaoEncerrada().setMotivoEncerramentoPrisao(null);
			getEntityManager().merge(entity.getPrisaoEncerrada());
		}
	}

	private boolean existePrisaoVinculada(IcrPrisao entity){
		StringBuilder sb = new StringBuilder();
		sb.append(" select icrPri from IcrPrisao icrPri");
		sb.append(" where icrPri.ativo = true ");
		sb.append(" and icrPri.prisaoEncerrada = :prisao ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("prisao", entity);
		return !q.getResultList().isEmpty();

	}

	@Override
	protected void ensureUniqueness(IcrPrisao entity) throws IcrValidationException{
		// super.ensureUniqueness(entity);
		if (existemPrisoesNoPeriodo(entity)){
			throw new IcrValidationException("Existem prisões cadastradas para o mesmo período");
		}
	}

	public void validarEncerramentoPrisao(IcrPrisao entity) throws IcrValidationException{
		if (entity.getDtEncerramentoPrisao() == null){
			throw new IcrValidationException("A data de encerramento é de preenchimento obrigatório");
		}
		if (DateUtil.isDataMaior(entity.getData(), entity.getDtEncerramentoPrisao())){
			throw new IcrValidationException("A data de encerramento não pode ser inferior a data da prisão");
		}
	}

	private boolean existemPrisoesNoPeriodo(IcrPrisao entity){
		List<IcrPrisao> prisoes = new ArrayList<IcrPrisao>(0);
		ProcessoParte reuSelecionado = entity.getProcessoParte();
		if (reuSelecionado.getIcrPrisoesAtivas() != null){
			for (IcrPrisao aux : reuSelecionado.getIcrPrisoesAtivas()){
				if (entity.getId() != null){
					if (!entity.getId().equals(aux.getId())){
						if (aux.getDtEncerramentoPrisao() != null){
							if ((!DateUtil.isDataIgual(aux.getData(), aux.getDtEncerramentoPrisao()))
								&& (DateUtil.isDataIgual(entity.getData(), aux.getData()) || (DateUtil.isDataMaior(entity.getData(), aux.getData()) &&
								DateUtil.isDataMenor(entity.getData(), aux.getDtEncerramentoPrisao())))){
								prisoes.add(aux);
							}
						}
					}
				}
				else{
					if (aux.getDtEncerramentoPrisao() != null){
						if (!DateUtil.isDataIgual(aux.getData(), aux.getDtEncerramentoPrisao())
							&& DateUtil.isDataIgual(entity.getData(), aux.getData())){
							prisoes.add(aux);
						}
						if (DateUtil.isDataMaior(entity.getData(), aux.getData()) &&
							DateUtil.isDataMenor(entity.getData(), aux.getDtEncerramentoPrisao())){
							prisoes.add(aux);
						}
					}
				}
			}
		}
		return !prisoes.isEmpty();
	}

	@SuppressWarnings("unchecked")
	public List<IcrPrisao> recuperarPrisoesEmAberto(ProcessoTrf processoTrf){
		StringBuilder sb = new StringBuilder();
		sb.append(" select 	");
		sb.append(" 	 icrPri ");
		sb.append(" from 	");
		sb.append(" 	 IcrPrisao icrPri 					");
		sb.append(" 	 inner join icrPri.processoParte pp ");
		sb.append(" 	 inner join pp.tipoParte tp 		");
		sb.append(" 	 inner join pp.pessoa pf 			");
		sb.append(" where ");
		sb.append(" 	 pp.processoTrf = :processoTrf  ");
		sb.append(" and   ");
		sb.append(" 	 icrPri.ativo = true ");
		sb.append(" and   ");
		sb.append(" 	 pp.inParticipacao = 'P' ");
		sb.append(" and   ");
		sb.append(" 	 icrPri.dtEncerramentoPrisao IS NULL ");
		sb.append(" order by 	 ");
		sb.append(" 	 pf.nome ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processoTrf", processoTrf);
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<String> recuperarEstadosEstabelecimentosPrisionais(){
		Query query = getEntityManager().createQuery(
				" select distinct e.uf " + " from EstabelecimentoPrisional e" + " order by e.uf");
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<String> recuperarCidadesEstabelecimentosPrisionais(String uf){
		if (uf != null){
			Query query = getEntityManager().createQuery(
					" select distinct o.dsCidade " + " from EstabelecimentoPrisional o" + " where o.uf = :uf "
						+ " order by o.dsCidade ");
			query.setParameter("uf", uf);
			return query.getResultList();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<EstabelecimentoPrisional> recuperarEstabelecimentosPrisionais(String cidade){
		if (cidade != null){
			Query query = getEntityManager().createQuery(
					" select o " + " from EstabelecimentoPrisional o " + " where o.dsCidade = :dsCidade "
						+ " order by o.dsCidade ");
			query.setParameter("dsCidade", cidade);
			return query.getResultList();
		}
		return null;
	}

	@Override
	public Date getDtPublicacao(IcrPrisao entity){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean possuiDataPublicacao(){
		// TODO Auto-generated method stub
		return false;
	}
}
