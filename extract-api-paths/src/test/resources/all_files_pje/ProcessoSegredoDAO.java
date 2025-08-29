/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoSegredo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de acesso a dados da entidade {@link ProcessoSegredo}.
 * 
 * @author cristof
 *
 */
@Name("processoSegredoDAO")
public class ProcessoSegredoDAO extends BaseDAO<ProcessoSegredo> {

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(ProcessoSegredo seg) {
		return seg.getIdProcessoSegredo();
	}
	
	/**
	 * Recupera a lista de solicitações de segredo ou sigilo realizadas para um dado processo judicial.
	 * 
	 * @param processo o processo a respeito do qual se pretende fazer a pesquisa.
	 * @return a lista de solicitações, que pode ser vazia
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoSegredo> getSolicitacoesSegredo(ProcessoTrf processo){
		String query = "SELECT ps FROM ProcessoSegredo AS ps " +
				"	WHERE ps.processoTrf = :processo";
		Query q = entityManager.createQuery(query);
		q.setParameter("processo", processo);
		return q.getResultList();
	}
	
	/**
	 * Recupera a lista e solicitações de segredo ou sigilo realizadas para um dado processo judicial e
	 * que ainda não foram rejeitadas ou acatadas.
	 * 
	 * @param processo o processo a respeito do qual se pretende fazer a pesquisa
	 * @return a lista de solicitações não apreciadas
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoSegredo> getSolicitacoesPendentes(ProcessoTrf processo){
		String query = "SELECT ps FROM ProcessoSegredo AS ps " +
				"	WHERE ps.processoTrf = :processo " +
				"		AND ps.status IS NULL ";
		Query q = entityManager.createQuery(query);
		q.setParameter("processo", processo);
		return q.getResultList();
	}
	
	public void removerProcessoSegredoPendente(ProcessoTrf processoTrf){ 
		String query = "DELETE FROM ProcessoSegredo AS ps " + 
					   "  WHERE ps.processoTrf = :processoTrf AND ps.status IS NULL "; 
		Query q = entityManager.createQuery(query); 
		q.setParameter("processoTrf", processoTrf); 
		q.executeUpdate(); 
	} 
   
  	public void removerProcessoSegredoPendenteUsuarioLogado(ProcessoTrf processoTrf) {
  		StringBuilder sb = new StringBuilder("DELETE FROM ProcessoSegredo AS ps ")
  		        .append(" WHERE ps.processoTrf.idProcessoTrf = :processoTrf")
  		        .append(" AND ps.status IS NULL AND ps.usuarioLogin.idUsuario = :usuarioLogin");

  		Query query = entityManager.createQuery(sb.toString());
  		query.setParameter("processoTrf", processoTrf.getIdProcessoTrf());
  		query.setParameter("usuarioLogin", Authenticator.getIdUsuarioLogado());
    	query.executeUpdate();
  	}

  	/**
  	 * metodo responsavel por recuperar todos os @ProcessoSegredo cadastrados pela pessoa passada em parametro.
  	 * @param _pessoa
  	 * @return
  	 */
	public List<ProcessoSegredo> recuperaSegredoProcessosCadastrados(Pessoa _pessoa) {
		List<ProcessoSegredo> resultado = null;
		Search search = new Search(ProcessoSegredo.class);
		try {
			search.addCriteria(Criteria.equals("usuarioLogin.idUsuario", _pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		resultado = list(search);
		return resultado;
	}
}
