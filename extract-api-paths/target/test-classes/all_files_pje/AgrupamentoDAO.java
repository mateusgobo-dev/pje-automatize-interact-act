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

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Agrupamento;

/**
 * Componente de acesso a dados da entidade {@link Agrupamento}.
 * 
 * @author cristof
 *
 */
@Name("agrupamentoDAO")
public class AgrupamentoDAO extends BaseDAO<Agrupamento> {

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(Agrupamento agr) {
		return agr.getIdAgrupamento();
	}
	
	/**
	 * Recupera o agrupamento de tipos de movimentação processual ativo que tem o nome dado.
	 * 
	 * @param codigo o nome identificador do agrupamento
	 * @return o agrupamento ativo que tem o nome dado ou null, se inexistente
	 */
	public Agrupamento findByNome(String nome){
		String query = "SELECT a FROM Agrupamento AS a WHERE a.ativo = true AND a.agrupamento = :agrupamento ";
		Query q = entityManager.createQuery(query);
		q.setParameter("agrupamento", nome);
		try {
			return (Agrupamento) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

}
