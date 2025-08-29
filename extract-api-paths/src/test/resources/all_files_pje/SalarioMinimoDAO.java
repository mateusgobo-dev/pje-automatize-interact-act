package br.com.jt.pje.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.itx.util.EntityUtil;
import br.jus.pje.jt.entidades.SalarioMinimo;

@Name(SalarioMinimoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class SalarioMinimoDAO extends GenericDAO implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "salarioMinimoDAO";
	
	@SuppressWarnings("unchecked")
	public List<SalarioMinimo> getSalarioMinimoEntre(Date dataInicio, Date dataFim, Integer idSalarioExcludente){
		StringBuffer sb = new StringBuffer();
		sb.append("select o from SalarioMinimo o ");
		sb.append("where (cast(:dataInicio as date) between cast(o.dataInicioVigencia as date) and cast(o.dataFimVigencia as date) ");
		if(dataFim != null){
			sb.append("   or cast(:dataFim as date) between cast(o.dataInicioVigencia as date) and cast(o.dataFimVigencia as date) ");
		}
		sb.append(" ) ");
		if(dataFim != null){
			sb.append("or (cast(:dataInicio as date) <= cast(o.dataInicioVigencia as date) and cast(:dataFim as date) >= cast(o.dataFimVigencia as date))");
		}
		if(idSalarioExcludente != null){
			sb.append("and o.idSalarioMinimo != :idSalarioExcludente");
		}
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("dataInicio", dataInicio);
		if(dataFim != null){
			q.setParameter("dataFim", dataFim);
		}
		if(idSalarioExcludente != null){
			q.setParameter("idSalarioExcludente", idSalarioExcludente);
		}
		List<SalarioMinimo> resultList = (List<SalarioMinimo>) q.getResultList();
		return resultList;
	}
	
	public List<SalarioMinimo> getSalarioMinimoEntre(Date dataInicio, Date dataFim){
		return getSalarioMinimoEntre(dataInicio, dataFim, null);
	}
	
	public SalarioMinimo getSalarioMinimoEm(Date data){
		List<SalarioMinimo> result = getSalarioMinimoEntre(data, null); 
		return (result != null && !result.isEmpty()) ? result.get(0) : null;
	}
	
	public SalarioMinimo getSalarioMinimoEmVigencia(){
		StringBuffer sb = new StringBuffer();
		sb.append("select o from SalarioMinimo o ");
		sb.append("where o.dataFimVigencia is null ");
		Query q = getEntityManager().createQuery(sb.toString());
		SalarioMinimo result = EntityUtil.getSingleResult(q);
		return result;
	}
	
	public Long getQtdSalariosFechadosApos(Date data){
		return (Long) getEntityManager()
						.createQuery("select count(o) from SalarioMinimo o where :dataInicio < o.dataFimVigencia")
						.setParameter("dataInicio", data)
						.getSingleResult();
	}
}
