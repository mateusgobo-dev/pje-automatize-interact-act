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

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Estado;

/**
 * Componente de acesso aos dados da entidade {@link Estado}.
 */
@Name("estadoDAO")
public class EstadoDAO extends BaseDAO<Estado> {

	@Override
	public Integer getId(Estado e) {
		return e.getIdEstado();
	}
	
	/**
	 * Recupera a lista de estados ativos na instalação ordenada pelo nome.
	 * 
	 * @return a lista de estados ativos
	 */
	@SuppressWarnings("unchecked")
	public List<Estado> estadoItems() {
		String query = "SELECT e FROM Estado AS e " +
				"	WHERE e.ativo IS true " +
				"	ORDER BY e.estado";
		Query q = entityManager.createQuery(query);
		return q.getResultList();
	}
	
	/**
	 * Recupera a lista de estados que têm a sigla dada.
	 * 
	 * @param sigla a sigla do estado
	 * @return a lista de estados que têm a sigla informada.
	 * @see #findBySigla
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public List<Estado> findByUf(String sigla){
		String query = "SELECT e FROM Estado AS e " +
				"	WHERE e.codEstado = :sigla";
		Query q = entityManager.createQuery(query);
		q.setParameter("sigla", sigla);
		return q.getResultList();		
	}
	
	/**
	 * Recupera o estado ativo que tem por sigla a informada.
	 * 
	 * @param sigla a sigla do estado ativo que se pretende recuperar
	 * @return o estado ativo que tem a sigla dada, ou null se não existir
	 */
	public Estado findBySigla(String sigla){
		String query = "SELECT e FROM Estado AS e " +
				"	WHERE e.ativo = true " +
				"		AND LOWER(e.codEstado) = :sigla";
		Query q = entityManager.createQuery(query);
		q.setParameter("sigla", sigla.toLowerCase());
		try{
			return (Estado) q.getSingleResult();
		}catch (NoResultException e){
			return null;
		}
	}
	
	/**
	 * Recupera os estados das jurisdicoes ativas.
	 * 
	 * @return Lista de estados
	 */
	@SuppressWarnings("unchecked")
	public List<Estado> recuperarPorJurisdicaoAtiva() {
		Query query = this.entityManager.createQuery(
				"SELECT DISTINCT e FROM Jurisdicao o JOIN o.estado e WHERE o.ativo = true ORDER BY e.codEstado");

		return query.getResultList();
	}
	
	/**
	 * Recupera os estados das jurisdições que possuem competência cadastrada.
	 * 
	 * @return Lista de estados
	 */
	@SuppressWarnings("unchecked")
	public List<Estado> recuperarPorJurisdicaoCompetenciaAtiva() {
		Query query = this.entityManager.createQuery(
				"SELECT DISTINCT q FROM CompetenciaAreaDireito o JOIN o.jurisdicao p JOIN p.estado q ORDER BY q.codEstado");

		return query.getResultList();
	}

}
