/**
 *  pje-web
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

import br.jus.pje.nucleo.entidades.Pais;

/**
 * Componente de acesso a dados da entidade {@link Pais}.
 * 
 * @author cristof
 *
 */
@Name("paisDAO")
public class PaisDAO extends BaseDAO<Pais> {

	@Override
	public Integer getId(Pais e) {
		return e.getId();
	}
	
	public Pais findByCodigo(String codigo){
		String query = "SELECT p FROM Pais AS p WHERE p.codigo = :codigo";
		Query q = entityManager.createQuery(query);
		q.setParameter("codigo", codigo);
		try{
			return (Pais) q.getSingleResult();
		} catch (NoResultException e){
			return null;
		}
	}

}
