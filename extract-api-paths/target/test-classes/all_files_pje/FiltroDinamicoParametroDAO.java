/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.FiltroDinamicoParametro;

/**
 * @author Everton nogueira pereira
 *
 */
@Name(FiltroDinamicoParametroDAO.NAME)
public class FiltroDinamicoParametroDAO extends BaseDAO<FiltroDinamicoParametro>{
	public static final String NAME = "filtroDinamicoParametroDAO";

	@SuppressWarnings("unchecked")
	public List<FiltroDinamicoParametro> obtemTodosParametros() {
		StringBuilder sb = new StringBuilder();
		sb.append("select p from FiltroDinamicoParametro p ");
		sb.append("order by p.parametro "); 
		Query q = getEntityManager().createQuery(sb.toString());
		return q.getResultList();
	}

	public FiltroDinamicoParametro obtemEntidadeByParametro(String parametro) {
		StringBuilder sb = new StringBuilder();
		sb.append("select p from FiltroDinamicoParametro p ");
		sb.append("where p.parametro = :param ");
		sb.append("order by p.parametro ");  
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("param", parametro);
		try{
			return (FiltroDinamicoParametro) q.getSingleResult();
		}catch(NoResultException e){
			return null;
		}catch(NonUniqueResultException e){
			return null;
		}
	}

	@Override
	public Object getId(FiltroDinamicoParametro e) {
		return e.getIdParametro();
	}
}
