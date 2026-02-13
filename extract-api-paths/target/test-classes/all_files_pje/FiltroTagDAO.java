package br.jus.cnj.pje.business.dao;

import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.FiltroTag;

@Name(FiltroTagDAO.NAME)
public class FiltroTagDAO extends BaseDAO<FiltroTag>{
	
	public static final String NAME = "filtroTagDAO";
	
	@Override
	public Object getId(FiltroTag e) {
		return e.hashCode(); 
	}
	
	public FiltroTag findFiltroTagByIdTagAndIdFiltro(Integer idFiltro, Integer idTag){
		StringBuilder sb = new StringBuilder("");
		FiltroTag filtroTag = null;
		
		sb.append("SELECT o FROM FiltroTag o ");
		sb.append("WHERE o.idTag = :idTag AND o.idFiltro = :idFiltro");
		
		Query q = this.entityManager.createQuery(sb.toString());
		
		q.setParameter("idTag", idTag);
		q.setParameter("idFiltro", idFiltro);
		
		try {
			filtroTag = (FiltroTag) q.getSingleResult();
		} catch (NonUniqueResultException e) {
			e.printStackTrace();
		}
		
		return filtroTag;
	}
	
}
