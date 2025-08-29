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

import br.jus.pje.nucleo.entidades.lancadormovimento.Dominio;

/**
 * Componente de acesso a dados da entidade {@link Dominio}.
 * 
 * @author cristof
 *
 */
@Name("dominioDAO")
public class DominioDAO extends BaseDAO<Dominio> {

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Long getId(Dominio e) {
		return e.getIdDominio();
	}

	/**
	 * Recupera um domínio por seu código identificador.
	 * 
	 * @param codigo o código identificador do domínio
	 * @return o domínio que tem o código, ou null se inexistir um domínio com o código indicado.
	 */
	public Dominio findByCodigo(String codigo) {
		String query = "SELECT d FROM Dominio AS d WHERE d.ativo = true AND d.codigo = :codigo";
		Query q = entityManager.createQuery(query);
		q.setParameter("codigo", codigo);
		try{
			return (Dominio) q.getSingleResult();
		}catch (NoResultException e){
			return null;
		}
	}
	
}
