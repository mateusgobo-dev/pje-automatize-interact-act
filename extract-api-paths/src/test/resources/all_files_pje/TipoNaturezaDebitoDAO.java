package br.jus.cnj.pje.business.dao;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.TipoNaturezaDebito;

/**
 * DAO da entidade TipoNaturezaDebito.
 * 
 * @author Adriano Pamplona
 */
@Name("tipoNaturezaDebitoDAO")
public class TipoNaturezaDebitoDAO extends BaseDAO<TipoNaturezaDebito>{

	@Override
	public Object getId(TipoNaturezaDebito e) {
		return e.getId();
	}	
	
	/**
	 * @param codigo
	 * @return TipoNaturezaDebito do código informado.
	 */
	public TipoNaturezaDebito findByCodigo(String codigo) {
		String hql = "SELECT o FROM TipoNaturezaDebito AS o WHERE o.codigo = :codigo ";
		Query query = entityManager.createQuery(hql);
		query.setParameter("codigo", codigo);

		return getSingleResult(query);
	}
}
