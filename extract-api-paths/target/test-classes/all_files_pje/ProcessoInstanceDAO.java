/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.ProcessoInstance;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Componente de acesso a dados da entidade {@link ProcessoInstance}.
 * 
 * @author cristof
 *
 */
@Name("processoInstanceDAO")
public class ProcessoInstanceDAO extends BaseDAO<ProcessoInstance> {
	
	@Override
	public Long getId(ProcessoInstance pi) {
		return pi.getIdProcessoInstance();
	}
	
	/**
	 * Recupera a lista de instâncias de fluxo ativas para um dado processo judicial.
	 * 
	 * @param pj o processo judicial cujas instâncias de fluxo ativas se pretende recuperar
	 * @param first inteiro indicativo do primeiro resultado que se pretende recuperar (ou nulo, para o inicial)
	 * @param maxResults inteiro indicativo do número máximo de resultados que se pretende recuperar (ou nulo, para todos)
	 * @return a lista de instâncias de fluxo ativas para o processo judicial dado
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoInstance> recuperaAtivas(ProcessoTrf pj, Integer first, Integer maxResults){
		String query = "SELECT p FROM ProcessoInstance AS p WHERE p.idProcesso = :idProcesso AND p.ativo = true ";
		Query q = entityManager.createQuery(query);
		q.setParameter("idProcesso",pj.getIdProcessoTrf());
		if(first != null && first.intValue() > 0){
			q.setFirstResult(first);
		}
		if(maxResults != null && maxResults.intValue() > 0){
			q.setMaxResults(maxResults);
		}
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Integer> getIdsProcessoLocalizacao(List<Integer> idsLocalizacoes, Integer first, Integer maxResults) {
		String query = "SELECT DISTINCT(p.idProcesso) FROM ProcessoInstance AS p WHERE p.ativo = true AND p.idLocalizacao IN (:idsLocalizacoes) ";
		Query q = entityManager.createQuery(query);
		q.setParameter("idsLocalizacoes",idsLocalizacoes);
		if(first != null && first.intValue() > 0){
			q.setFirstResult(first);
		}
		if(maxResults != null && maxResults.intValue() > 0){
			q.setMaxResults(maxResults);
		}
		return q.getResultList();
	}
	
	public boolean existeProcessoInstancePorLocalizacaoPessoa(Integer idProcessoTrf, List<Integer> idsLocalizacoesFisicas, Integer idOrgaoJulgadorColegiado, boolean isServidorExclusivoOJC) {
		StringBuilder hql = new StringBuilder("SELECT o FROM ProcessoInstance o ")
				.append(" WHERE o.idProcesso = :idProcessoTrf ")
				.append(" AND o.ativo = true ");
		if(!isServidorExclusivoOJC) {
			hql.append(" AND o.idLocalizacao IN (:idsLocalizacoesFisicas) ");
		}

		if(idOrgaoJulgadorColegiado != null && idOrgaoJulgadorColegiado > 0) {
			hql.append(" AND o.orgaoJulgadorColegiado = :idOrgaoJulgadorColegiado ");
		}
		Query q = entityManager.createQuery(hql.toString());
		q.setParameter("idProcessoTrf",idProcessoTrf);
		if(!isServidorExclusivoOJC) {
			q.setParameter("idsLocalizacoesFisicas",idsLocalizacoesFisicas);
		}
		if(idOrgaoJulgadorColegiado != null && idOrgaoJulgadorColegiado > 0) {
			q.setParameter("idOrgaoJulgadorColegiado",idOrgaoJulgadorColegiado);
		}
		
		return CollectionUtilsPje.isNotEmpty(q.getResultList());
	}
}
