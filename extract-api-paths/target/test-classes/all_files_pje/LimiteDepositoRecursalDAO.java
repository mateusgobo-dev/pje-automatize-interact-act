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
import br.jus.pje.jt.entidades.LimiteDepositoRecursal;

@Name(LimiteDepositoRecursalDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class LimiteDepositoRecursalDAO extends GenericDAO implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "limiteDepositoRecursalDAO";
	

	@SuppressWarnings("unchecked")
	public List<LimiteDepositoRecursal> getLimiteDepositoRecursalEntre(Date dataInicio, Date dataFim, Integer idLimiteExcludente){
		StringBuffer sb = new StringBuffer();
		sb.append("select o from LimiteDepositoRecursal o ");
		sb.append("where (cast(:dataInicio as date) between cast(o.dataInicioVigencia as date) and cast(o.dataFimVigencia as date) ");
		if(dataFim != null){
			sb.append("   or cast(:dataFim as date) between cast(o.dataInicioVigencia as date) and cast(o.dataFimVigencia as date) ");
		}
		sb.append(") ");
		if(dataFim != null){
			sb.append("or (cast(:dataInicio as date) <= cast(o.dataInicioVigencia as date) and cast(:dataFim as date) >= cast(o.dataFimVigencia as date))");
		}
		if(idLimiteExcludente != null){
			sb.append("and o.idLimiteDepositoRecursal != :idLimiteExcludente");
		}
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("dataInicio", dataInicio);
		if(dataFim != null){
			q.setParameter("dataFim", dataFim);
		}
		if(idLimiteExcludente != null){
			q.setParameter("idLimiteExcludente", idLimiteExcludente);
		}
		List<LimiteDepositoRecursal> resultList = (List<LimiteDepositoRecursal>) q.getResultList();
		return resultList;
	}
	
	public List<LimiteDepositoRecursal> getLimiteDepositoRecursalEntre(Date dataInicio, Date dataFim){
		return getLimiteDepositoRecursalEntre(dataInicio, dataFim, null);
	}
	
	public LimiteDepositoRecursal getLimiteDepositoRecursalEm(Date data){
		List<LimiteDepositoRecursal> result = getLimiteDepositoRecursalEntre(data, null);
		if(result != null && !result.isEmpty()){
			return getLimiteDepositoRecursalEntre(data, null).get(0);
		}else{
			return null;
		}
	}
	
	public LimiteDepositoRecursal getLimiteDepositoRecursalEmVigencia(){
		StringBuffer sb = new StringBuffer();
		sb.append("select o from LimiteDepositoRecursal o ");
		sb.append("where o.dataFimVigencia is null ");
		Query q = getEntityManager().createQuery(sb.toString());
		LimiteDepositoRecursal result = EntityUtil.getSingleResult(q);
		return result;
	}
	
	public Long getQtdLimitesFechadosApos(Date data){
		return (Long) getEntityManager()
						.createQuery("select count(o) from LimiteDepositoRecursal o where :dataInicio < o.dataFimVigencia")
						.setParameter("dataInicio", data)
						.getSingleResult();
	}
}
