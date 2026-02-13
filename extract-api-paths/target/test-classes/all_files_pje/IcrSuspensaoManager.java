package br.com.infox.cliente.home.icrrefactory;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import br.jus.pje.nucleo.entidades.AcompanhamentoCondicaoSuspensao;
import br.jus.pje.nucleo.entidades.CondicaoSuspensao;
import br.jus.pje.nucleo.entidades.CondicaoSuspensaoAssociada;
import br.jus.pje.nucleo.entidades.IcrSuspensao;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoSuspensao;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name("icrSUSManager")
public class IcrSuspensaoManager extends
		InformacaoCriminalRelevanteManager<IcrSuspensao>{

	@Override
	protected void prePersist(IcrSuspensao entity)
			throws IcrValidationException{
		super.prePersist(entity);
		validar(entity);
		for (CondicaoSuspensaoAssociada condicao : entity.getCondicaoSuspensaoAssociadaList()){
			validar(condicao);
			condicao.setIcrSuspensao(entity);
			for (AcompanhamentoCondicaoSuspensao acompanhamento : condicao.getAcompanhamentoCondicaoSuspensaoList()){
				validar(acompanhamento);
				acompanhamento.setCondicaoSuspensaoAssociada(condicao);
			}
		}
	}

	public void validar(IcrSuspensao entity)
			throws IcrValidationException{
		if (entity == null)
			return;
		if (entity.getId() != null && !entity.getAtivo())
			return;
		if (entity.getId() != null && verificaEncerramento(entity)){
			throw new IcrValidationException(
					"icrSuspensao.erroSuspensaoEncerrada");
		}
		if (entity.getId() != null && verificaSuspensao(entity)){
			throw new IcrValidationException(
					"icrSuspensao.erroSuspensaoSuspensa");
		}
		if (entity.getDataPrevistaTermino() != null
			&& DateUtil.isDataMaior(entity.getData(), entity.getDataPrevistaTermino())){
			throw new IcrValidationException(
					"icrSuspensao.data_suspensao_maior_data_prevista_termino");
		}	
		boolean hasListaCondicoesAtivas = false;
		for (CondicaoSuspensaoAssociada condicao : entity.getCondicaoSuspensaoAssociadaList()){
			if(condicao.getAtivo()){
				hasListaCondicoesAtivas = true;
				break;
			}
		}	
		if (!hasListaCondicoesAtivas) {
			throw new IcrValidationException("icrSuspensao.condicao_nao_adicionada");
		}
	}

	public void validar(CondicaoSuspensaoAssociada entity)
			throws IcrValidationException{
		if (entity == null)
			return;
		if (entity.getId() != null && !entity.getAtivo())
			return;
		if (entity.getCondicaoSuspensao() == null){
			throw new IcrValidationException(
					"icrSuspensao.condicao_nao_selecionada");
		}
		if (entity.getCondicaoSuspensao().getIsCampoTextoLivre() && (entity.getTextoLivre() == null || entity.getTextoLivre().equals(""))){
			throw new IcrValidationException(
					"icrSuspensao.condicao_texto_livre_vazio");
		}
		if (entity.getId() == null && entity.getAtivo()){
			if (verificaDuplicidade(entity)){
				throw new IcrValidationException(
						"icrSuspensao.condicao_duplicada");
			}
		}
	}

	public void validar(AcompanhamentoCondicaoSuspensao entity)
			throws IcrValidationException{
		if (entity == null)
			return;
		if (entity.getId() != null && !entity.getAtivo())
			return;
		if (entity.getCondicaoSuspensaoAssociada().getId() == null){
			throw new IcrValidationException(
					"icrSuspensao.condicao_nao_selecionada");
		}
		if (entity.getDataPrevistaCumprimento() == null){
			throw new IcrValidationException(
					"icrSuspensao.data_prevista_nao_informada");
		}
		if (entity.getDataCumprimento() != null 
				&& DateUtil.isDataMaior(entity.getDataPrevistaCumprimento(), entity.getDataCumprimento())){
			throw new IcrValidationException(
					"icrSuspensao.data_prevista_maior_que_data_cumprimento");
		}
		// if (entity.getId() == null && entity.getAtivo()){
		// if (verificaDuplicidade(entity)){
		// throw new IcrValidationException(
		// "icrSuspensao.acompanhamento_duplicado");
		// }
		// }
	}

	private boolean verificaDuplicidade(CondicaoSuspensaoAssociada entity){
		StringBuilder query = new StringBuilder("select o from CondicaoSuspensaoAssociada o where o.ativo=true and o.icrSuspensao.ativo=true  ");
		if (entity.getCondicaoSuspensao().getCampoTextoLivre() && entity.getTextoLivre() != null && !entity.getTextoLivre().equals("")){
			query.append(" and lower(o.textoLivre) = lower('");
			query.append(entity.getTextoLivre());
			query.append("')");
		}
		if (entity.getIcrSuspensao() != null && entity.getIcrSuspensao().getId() != null){
			query.append("  and o.icrSuspensao.id = :idIcr");
		}
		if (entity.getCondicaoSuspensao() != null && entity.getCondicaoSuspensao().getId() != null){
			query.append(" and o.condicaoSuspensao.ativo=true and o.condicaoSuspensao.id = :idCondicao");
		}
		if (!InformacaoCriminalRelevanteHome.getHomeInstance().getReusSelecionados().isEmpty()){
			query.append(" and  o.icrSuspensao.processoParte.idProcessoParte in(");
			for (ProcessoParte pp : InformacaoCriminalRelevanteHome.getHomeInstance().getReusSelecionados()){
				query.append(pp.getIdProcessoParte());
			}
			query.append(")");
		}
		Query eMQuery = getEntityManager()
				.createQuery(query.toString());
		if (entity.getIcrSuspensao() != null && entity.getIcrSuspensao().getId() != null){
			eMQuery.setParameter("idIcr", entity.getIcrSuspensao().getId());
		}
		if (entity.getCondicaoSuspensao() != null && entity.getCondicaoSuspensao().getId() != null){
			eMQuery.setParameter("idCondicao", entity.getCondicaoSuspensao().getId());
		}
		return !eMQuery
				.getResultList().isEmpty();
	}

	private boolean verificaDuplicidade(AcompanhamentoCondicaoSuspensao entity){
		return !getEntityManager()
				.createQuery(
						"select o from AcompanhamentoCondicaoSuspensao o where o.ativo=true and o.condicaoSuspensaoAssociada = :condicao")
				.setParameter("condicao", entity.getCondicaoSuspensaoAssociada()).getResultList().isEmpty();
	}

	@Override
	protected void preInactive(IcrSuspensao entity)
			throws IcrValidationException{
		super.preInactive(entity);
		if (verificaEncerramento(entity)){
			throw new IcrValidationException(
					"icrSuspensao.erroSuspensaoEncerrada");
		}
		if (verificaSuspensao(entity)){
			throw new IcrValidationException(
					"icrSuspensao.erroSuspensaoSuspensa");
		}
		for (CondicaoSuspensaoAssociada csa : entity.getCondicaoSuspensaoAssociadaList()){
			csa.setAtivo(false);
			for (AcompanhamentoCondicaoSuspensao acs : csa.getAcompanhamentoCondicaoSuspensaoList()){
				acs.setAtivo(false);
			}
		}
	}

	private boolean verificaEncerramento(IcrSuspensao entity){
		return !getEntityManager()
				.createQuery(
						"select o from IcrEncerrarSuspensaoProcesso o where o.ativo=true and o.icrAfetada = :icr")
				.setParameter("icr", entity).getResultList().isEmpty();
	}

	private boolean verificaSuspensao(IcrSuspensao entity){
		return !getEntityManager()
				.createQuery(
						"select o from IcrSuspenderSuspensao o where o.ativo=true and o.icrAfetada = :icr")
				.setParameter("icr", entity).getResultList().isEmpty();
	}

	@SuppressWarnings("unchecked")
	public List<TipoSuspensao> getTipoSuspensaoList(IcrSuspensao entity){
		String queryString = "from TipoSuspensao t where t.ativo = true";
		if (entity.getId() != null){
			queryString += " or t.id in(select o.tipoSuspensao.id from IcrSuspensao o "
				+ "where o.ativo = true and o.id = :idIcr)";
		}
		Query query = getEntityManager().createQuery(queryString);
		if (entity.getId() != null){
			query.setParameter("idIcr", entity.getId());
		}
		return query.getResultList();
	}

	// @SuppressWarnings("unchecked")
	// public List<CondicaoSuspensao> getCondicaoSuspensaoList(
	// IcrSuspensao icrSuspensao) {
	// Query query = null;
	// CondicaoSuspensao c = icrSuspensao.getCondicaoSuspensao();
	//
	// if (c != null && c.getAtivo() == false) {
	// query = getEntityManager()
	// .createQuery(
	// "from CondicaoSuspensao c where (c.ativo = true or c.id = ?) and c.tipoSuspensao.id = ?");
	// query.setParameter(1, c.getId());
	// query.setParameter(2, icrSuspensao.getTipoSuspensao().getId());
	//
	// } else {
	// query = getEntityManager()
	// .createQuery(
	// "from CondicaoSuspensao c where c.ativo = true and c.tipoSuspensao.id = ?");
	// query.setParameter(1, icrSuspensao.getTipoSuspensao().getId());
	// }
	// return query.getResultList();
	// }

	@SuppressWarnings("unchecked")
	public List<CondicaoSuspensao> getCondicaoSuspensaoList(IcrSuspensao icrSuspensao){
		String queryString = "from CondicaoSuspensao c where (c.ativo = true and c.tipoSuspensao = :tipo) " +
				"or c.id IN (select csa.condicaoSuspensao.id from CondicaoSuspensaoAssociada csa where csa.icrSuspensao.id = :id_icr)";
		Query query = getEntityManager().createQuery(queryString);
		query.setParameter("tipo", icrSuspensao.getTipoSuspensao());
		query.setParameter("id_icr", icrSuspensao.getId());
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<CondicaoSuspensaoAssociada> getCondicaoSuspensaoAssociadaList(
			IcrSuspensao icrSuspensao){
		if (icrSuspensao.getId() != null){
			Query query = getEntityManager()
					.createQuery(
							"from CondicaoSuspensaoAssociada c where c.ativo = true and c.icrSuspensao.id = ?");
			query.setParameter(1, icrSuspensao.getId());
			return query.getResultList();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void ensureUniqueness(IcrSuspensao entity)
			throws IcrValidationException{
		super.ensureUniqueness(entity);
		String queryString = " from IcrSuspensao s where s.ativo = true ";
		if (entity.getId() != null){
			queryString += " and s.id != :id_icr";
		}
		Query query = getEntityManager().createQuery(queryString);
		if (entity.getId() != null){
			query.setParameter("id_icr", entity.getId());
		}
		Date dataInicioInstance = entity.getData();
		Date dataTerminoInstance = entity.getDataPrevistaTermino() == null ? getDataDistante() : entity.getDataPrevistaTermino();
		for (IcrSuspensao icrSuspensaoItem : (List<IcrSuspensao>) query
				.getResultList()){
			Date dataInicioItem = icrSuspensaoItem.getData();
			Date dataTerminoItem = icrSuspensaoItem.getDataPrevistaTermino() == null ? getDataDistante() : icrSuspensaoItem.getDataPrevistaTermino();
			if (DateUtil.isDataEntre(dataInicioInstance, dataInicioItem, dataTerminoItem)
				|| DateUtil.isDataEntre(dataTerminoInstance, dataInicioItem, dataTerminoItem)
				|| DateUtil.isDataEntre(dataInicioItem, dataInicioInstance, dataTerminoInstance)
				|| DateUtil.isDataEntre(dataTerminoItem, dataInicioInstance, dataTerminoInstance)){
				uniquenessException();
			}
		}
	}

	private Date getDataDistante(){
		// data substituta para os casos em que a data de termino == null
		Calendar calendar = Calendar.getInstance();
		calendar.set(7000, 11, 1, 0, 0, 0);
		return calendar.getTime();
	}

	private IcrValidationException uniquenessException(){
		return new IcrValidationException(
				"icrSuspensao.suspensao_duplicada");
	}

	public List<ProcessoParte> getListaReus(ProcessoTrf processoTrf){
		List<ProcessoParte> reus = processoTrf.getListaPartePrincipalPassivo();
		Query query = getEntityManager()
				.createQuery(
						"select o.processoParte from IcrSuspensao o "
							+ " where o.processoParte.processoTrf.idProcessoTrf = :idProcessoTrf" 
							+ " and o.ativo = true" 
							+ " or o.tipo.codigo != :codIcr");
		
		query.setParameter("idProcessoTrf", processoTrf.getIdProcessoTrf());
		query.setParameter("codIcr", TipoIcrEnum.SUS.toString());
		reus.removeAll(query.getResultList());
		return reus;
	}

	public void gravarAcompanhamentoCondicaoSuspensao(
			List<AcompanhamentoCondicaoSuspensao> acompanhamentos)
			throws IcrValidationException{

		if (acompanhamentos != null){
			for (AcompanhamentoCondicaoSuspensao ac : acompanhamentos){
				ac.setAtivo(true);
				getEntityManager().persist(ac);
			}
			getEntityManager().flush();
		}
	}
}
