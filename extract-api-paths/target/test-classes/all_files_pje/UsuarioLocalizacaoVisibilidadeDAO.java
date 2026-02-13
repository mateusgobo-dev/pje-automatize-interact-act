/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *  
 */
package br.jus.cnj.pje.business.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.time.DateUtils;
import org.jboss.seam.annotations.Name;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.SubstituicaoMagistrado;
import br.jus.cnj.pje.servicos.DateService;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoVisibilidade;
import br.jus.pje.nucleo.util.DateUtil;

/**
 * Componente de acesso a dados da entidade {@link UsuarioLocalizacaoVisibilidade}.
 * 
 * @author cristof
 *
 */
@Name("usuarioLocalizacaoVisibilidadeDAO")
public class UsuarioLocalizacaoVisibilidadeDAO extends BaseDAO<UsuarioLocalizacaoVisibilidade> {

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(UsuarioLocalizacaoVisibilidade loc) {
		return loc.getIdUsuarioLocalizacaoVisibilidade();
	}

	/**
	 * Indica se existe alguma visibilidade de atuação associada à localização interna dada. 
	 * A visibilidade deve estar ativa no momento da verificação.
	 *  
	 * @param loc a localização a respeito da qual se pretende identificar a existência de visibilidade
	 * @return true, se houver pelo menos uma visibilidade ativa associada. 
	 */
	public boolean temVisibilidade(UsuarioLocalizacaoMagistradoServidor loc) {
		Date hoje = DateUtils.truncate(DateService.instance().getDataHoraAtual(), Calendar.DATE);
		
		String query = "SELECT COUNT(loc.idUsuarioLocalizacaoVisibilidade) FROM UsuarioLocalizacaoVisibilidade AS loc " +
				"	WHERE loc.usuarioLocalizacaoMagistradoServidor.idUsuarioLocalizacaoMagistradoServidor = :loc " +
				"		AND loc.dtInicio <= :data" +
				"		AND (loc.dtFinal IS NULL OR loc.dtFinal >= :data)";
				
		Query q = entityManager.createQuery(query);
		q.setParameter("loc", loc.getIdUsuarioLocalizacaoMagistradoServidor());
		q.setParameter("data", hoje);
		q.setMaxResults(1);
		Number cont = (Number) q.getSingleResult();
		return cont.longValue() > 0;
	}
	
	/**
	 * Indica se existe alguma visibilidade vinculada à localização interna dada associada com
	 * pelo menos um cargo judicial.
	 * 
	 * @param loc a localização respeito da qual se pretende identificar a existência de visibilidade
	 * @return true, se houver pelo menos uma visibilidade ativa e vinculada a um cargo
	 */
	public boolean temOrgaoVisivel(UsuarioLocalizacaoMagistradoServidor loc){
		Date hoje = DateUtils.truncate(DateService.instance().getDataHoraAtual(), Calendar.DATE);
		
		String query = "SELECT COUNT(loc.idUsuarioLocalizacaoVisibilidade) FROM UsuarioLocalizacaoVisibilidade AS loc " +
				"	WHERE loc.usuarioLocalizacaoMagistradoServidor.idUsuarioLocalizacaoMagistradoServidor = :loc " +
				"		AND loc.dtInicio <= :data" +
				"		AND (loc.dtFinal IS NULL OR loc.dtFinal >= :data) " +
				"		AND loc.orgaoJulgadorCargo IS NOT NULL ";
		Query q = entityManager.createQuery(query);
		q.setParameter("loc", loc.getIdUsuarioLocalizacaoMagistradoServidor());
		q.setParameter("data", hoje);
		q.setMaxResults(1);
		Number cont = (Number) q.getSingleResult();
		return cont.longValue() > 0;
	}

	/**
 	 * Retorna todas as visibilidades de uma dada localização/lotação. 
 	 *  
 	 * @param localizacao localização em questão
 	 * @return lista de visibilidades 
 	 */
 	public List<UsuarioLocalizacaoVisibilidade> obterVisibilidades(UsuarioLocalizacaoMagistradoServidor localizacao, 
 			boolean apenasAtivos) {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("select o from UsuarioLocalizacaoVisibilidade o ");
 		sb.append("where o.usuarioLocalizacaoMagistradoServidor = :localizacao ");

 		if(apenasAtivos) {
 			sb.append("and o.dtInicio <= :dataAtualInicio 												");
 			sb.append("and (o.dtFinal is null or o.dtFinal >= :dataAtualFinal )  						");
 		}
		
 		Query q = entityManager.createQuery(sb.toString());
 		q.setParameter("localizacao", localizacao);
 		if(apenasAtivos) {
 			q.setParameter("dataAtualInicio", DateUtil.getBeginningOfToday());
 			q.setParameter("dataAtualFinal", DateUtil.getEndOfToday());
 		}
 
 		@SuppressWarnings("unchecked")
 		List<UsuarioLocalizacaoVisibilidade> resultList = q.getResultList();		
 		return resultList;
 	}	
 	
 	/**
	 * Remove todas as visibilidades geradas pra substituições que já
	 * se encerraram.
	 */
	public void removerVisibilidadesSubstituicoesEncerradas() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("DELETE FROM UsuarioLocalizacaoVisibilidade ");
		sb.append("WHERE dtFinal IS NOT NULL AND dtFinal < current_date ");
		sb.append("AND substituicaoMagistrado IS NOT NULL ");
		
		Query q = getEntityManager().createQuery(sb.toString());
        q.executeUpdate();
	}

	/**
	 * Remove todas as visibilidades geradas para uma dada substituição
	 * @param substituicaoMagistrado substituição cujas visibilidades serão removidas
	 */
	public void removerVisibilidadesSubstituicao(SubstituicaoMagistrado substituicaoMagistrado) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("DELETE FROM UsuarioLocalizacaoVisibilidade ");
		sb.append("WHERE substituicaoMagistrado = :substituicaoMagistrado ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("substituicaoMagistrado", substituicaoMagistrado);
        q.executeUpdate();
	}
	
	/**
	 * Dada uma visibilidade, verifica se existe alguma outra visibilidade
	 * com periodo conflitante abrangendo o mesmo cargo.
	 * @param visibilidade a visibilidade a ser analisada
	 * @return true caso existam visibilidades conflitantes.
	 */
	public boolean existeVisibilidadeConflitante(
			UsuarioLocalizacaoVisibilidade visibilidade) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("select o from UsuarioLocalizacaoVisibilidade o 				     		   								");
		sb.append("where o.usuarioLocalizacaoMagistradoServidor = :localizacao 	     		   								");
		sb.append("and ( 																								    ");
		sb.append("			(o.dtInicio >= :novaDataInicial and o.dtInicio <= :novaDataFinal )   							");
		sb.append("			or																								");
		sb.append("			(:novaDataInicial >= o.dtInicio and (:novaDataInicial <= o.dtFinal or o.dtFinal is null) )   	");
		sb.append("    ) 																								    ");
		sb.append("and (o.orgaoJulgadorCargo is null or o.orgaoJulgadorCargo = :orgaoJulgadorCargo) 						");
		sb.append("and o.idUsuarioLocalizacaoVisibilidade <> :idUsuarioLocalizacaoVisibilidade 								");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("localizacao", visibilidade.getUsuarioLocalizacaoMagistradoServidor());
		q.setParameter("novaDataInicial", visibilidade.getDtInicio());
		q.setParameter("novaDataFinal", visibilidade.getDtFinal());
		q.setParameter("orgaoJulgadorCargo", visibilidade.getOrgaoJulgadorCargo());
		q.setParameter("idUsuarioLocalizacaoVisibilidade", visibilidade.getIdUsuarioLocalizacaoVisibilidade());
		
		@SuppressWarnings("unchecked")
		List<UsuarioLocalizacaoVisibilidade> resultList = q.getResultList();		
		return resultList.size() > 0;
	}

	/**
	 * Verifica se a lotaçao dada <code>lotacao</code> possui visibilidade ativa para o cargo judicial dado <code>cargoJudicial</code>
	 * @param lotacao
	 * @param cargoJudicial
	 * @return true caso a lotação dada possua visibilidade ativa para o cargo dado.
	 */
	public Boolean possuiVisibilidadeAtiva(UsuarioLocalizacaoMagistradoServidor lotacao,
			OrgaoJulgadorCargo cargoJudicial) {

		StringBuilder sb = new StringBuilder();
		
		sb.append("select o from UsuarioLocalizacaoVisibilidade o 				     		   	");
		sb.append("where o.usuarioLocalizacaoMagistradoServidor = :lotacao 	     				");
		sb.append("and o.dtInicio <= :dataAtualInicio 												");
		sb.append("and (o.dtFinal is null or o.dtFinal >= :dataAtualFinal )  						");
		sb.append("and (o.orgaoJulgadorCargo is null or o.orgaoJulgadorCargo = :cargoJudicial) 	");
		
		Query q = getEntityManager().createQuery(sb.toString());
		

		q.setParameter("lotacao", lotacao);
		q.setParameter("dataAtualInicio", DateUtil.getBeginningOfToday());
		q.setParameter("dataAtualFinal", DateUtil.getEndOfToday());
		q.setParameter("cargoJudicial", cargoJudicial);
		
		@SuppressWarnings("unchecked")
		List<UsuarioLocalizacaoVisibilidade> resultList = q.getResultList();		
		return resultList.size() > 0;
	}
}
