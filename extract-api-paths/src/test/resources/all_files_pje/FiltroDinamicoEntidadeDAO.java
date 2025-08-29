/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.FiltroDinamicoEntidade;

/**
 * @author Everton nogueira pereira
 *
 */
@Name(FiltroDinamicoEntidadeDAO.NAME)
public class FiltroDinamicoEntidadeDAO extends BaseDAO<FiltroDinamicoEntidade>{
	public static final String NAME = "filtroDinamicoEntidadeDAO";

	@SuppressWarnings("unchecked")
	public List<FiltroDinamicoEntidade> obtemTodasEntidades() {
		StringBuilder sb = new StringBuilder();
		sb.append("select e from FiltroDinamicoEntidade e ");
		sb.append("order by e.entidade "); 
		Query q = getEntityManager().createQuery(sb.toString());
		return q.getResultList();
	}

	@Override
	public Object getId(FiltroDinamicoEntidade e) {
		return e.getIdEntidade();
	}
}
