/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.identidade.Papel;

/**
 * @author cristof
 * 
 */
@Name("papelDAO")
public class PapelDAO extends BaseDAO<Papel>{

	public Papel findByName(String name){
		String queryStr = "SELECT p FROM Papel AS p WHERE p.nome = :nome";
		Query q = this.entityManager.createQuery(queryStr);
		q.setParameter("nome", name);
		Papel p = null;
		try{
			p = (Papel) q.getSingleResult();
		} catch (NoResultException e){
			return null;
		} catch (NonUniqueResultException e){
			throw new IllegalStateException("Há mais de um papel com o nome [" + name + "].");
		}
		return p;
	}

	public Papel findByCodeName(String codeName){
		String queryStr = "SELECT p FROM Papel AS p WHERE p.identificador = :identificador";
		Query q = this.entityManager.createQuery(queryStr);
		q.setParameter("identificador", codeName);
		Papel p = null;
		try{
			p = (Papel) q.getSingleResult();
		} catch (NoResultException e){
			return null;
		} catch (NonUniqueResultException e){
			throw new IllegalStateException("Há mais de um papel com o identificador[" + codeName + "].");
		}
		return p;
	}

	@Override
	public Integer getId(Papel e){
		return e.getIdPapel();
	}
	
	@SuppressWarnings("unchecked")
	public List<Papel> findAllChildrenByPapel(Papel p){
		List<Integer> idList = p.getListIdsPapeisInferiores();
		List<Papel> papelList = null;
		if(idList != null && !idList.isEmpty()) {
			StringBuilder queryStr = new StringBuilder("SELECT p FROM Papel as p WHERE p.idPapel in (:idsPapeis) ORDER BY p.nome");
			Query q = this.getEntityManager().createQuery(queryStr.toString());
			q.setParameter("idsPapeis", idList);
			papelList = (List<Papel>) q.getResultList();
		}
		
		return papelList;
	}
	
	@SuppressWarnings("unchecked")
	public List<Papel> findAllHerdeirosByPapel(Papel papel) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT p FROM Papel p WHERE p.idsPapeisInferiores like :idPapel");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idPapel", "%:" + String.valueOf(papel.getIdPapel()) + ":%");
		
		return q.getResultList();
	}

}
