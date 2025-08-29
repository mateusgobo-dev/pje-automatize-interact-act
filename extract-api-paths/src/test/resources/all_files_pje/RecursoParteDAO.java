package br.com.jt.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.itx.util.EntityUtil;
import br.jus.pje.jt.entidades.RecursoParte;
import br.jus.pje.nucleo.entidades.ProcessoParte;


@Name(RecursoParteDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class RecursoParteDAO extends GenericDAO implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String NAME = "recursoParteDAO";

  	public void removeBy(Integer idRecurso, Integer idProcessoParte) {
  		StringBuffer sb = new StringBuffer();
		sb.append("delete from RecursoParte o ");
		sb.append(" where o.assistenteAdmissibilidadeRecurso.idAssistenteAdmissibilidadeRecurso = :idAssistenteAdmissibilidadeRecurso ");
		sb.append("   and o.processoParte.idProcessoParte = :idProcessoParte ");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("idAssistenteAdmissibilidadeRecurso", idRecurso);
		q.setParameter("idProcessoParte", idProcessoParte);
		q.executeUpdate();
  	}
  	
  	@SuppressWarnings("unchecked")
	public List<ProcessoParte> getPartesBy(Integer idRecurso){
  		StringBuffer sb = new StringBuffer();
		sb.append("select o.processoParte from RecursoParte o ");
		sb.append(" where o.assistenteAdmissibilidadeRecurso.idAssistenteAdmissibilidadeRecurso = :idAssistenteAdmissibilidadeRecurso");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("idAssistenteAdmissibilidadeRecurso", idRecurso);
		
		List<ProcessoParte> list = (List<ProcessoParte>) q.getResultList();
		return list;
  	}
  	
  	@SuppressWarnings("unchecked")
  	public List<RecursoParte> getRecursoPartesByRecurso(Integer idRecurso){
  		StringBuffer sb = new StringBuffer();
  		sb.append("select o from RecursoParte o ");
  		sb.append(" where o.assistenteAdmissibilidadeRecurso.idAssistenteAdmissibilidadeRecurso = :idAssistenteAdmissibilidadeRecurso ");
  		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
  		q.setParameter("idAssistenteAdmissibilidadeRecurso", idRecurso);
  		List<RecursoParte> list = (List<RecursoParte>) q.getResultList();
  		return list;
  	}
  	
  	public boolean existeRecursoParte(Integer idRecurso, Integer idParte){
  		StringBuffer sb = new StringBuffer();
  		sb.append("select count(o) from RecursoParte o ");
  		sb.append("where o.assistenteAdmissibilidadeRecurso.idAssistenteAdmissibilidadeRecurso = :idAssistenteAdmissibilidadeRecurso ");
  		sb.append("and o.processoParte.idProcessoParte = :idProcessoParte ");
  		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
  		q.setParameter("idAssistenteAdmissibilidadeRecurso", idRecurso);
  		q.setParameter("idProcessoParte", idParte);
  		Long result = (Long)q.getSingleResult();
  		return result > 0;
  	}
}
