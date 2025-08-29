

package br.jus.cnj.pje.business.dao;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.util.DateUtil;


@Name(SessaoJudicialDAO.NAME)
public class SessaoJudicialDAO extends BaseDAO<Sessao> {

	public static final String NAME = "sessaoJudicialDAO";

	@Override
	public Object getId(Sessao e) {
		return e.getIdSessao();
	}
	
	@SuppressWarnings("unchecked")
	public List<Sessao> findByAno(Integer ano, Boolean somenteContinuas, Boolean sessoesFuturas){
		Date dataLimite = Calendar.getInstance().getTime();
		if (sessoesFuturas) {
			dataLimite = DateUtil.getEndOfDay(dataLimite);
		}
				
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ano", ano.toString());
		
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT sessao FROM Sessao sessao ");
		hql.append("WHERE sessao.dataExclusao IS NULL ");
		hql.append("AND TO_CHAR(sessao.dataSessao, 'YYYY') = :ano ");
		if (sessoesFuturas == Boolean.FALSE) {
			hql.append("AND( ");
			hql.append("	sessao.dataSessao < :dataLimite ");
			hql.append("	OR sessao.dataSessao = :dataLimite AND (TO_CHAR(sessao.horarioInicio, 'HH24:MI:SS') <= :horarioAtual OR sessao.horarioInicio is null) ");
			hql.append(") ");
			params.put("dataLimite", dataLimite);
			params.put("horarioAtual", DateUtil.dateToString(dataLimite, "HH:mm:ss"));
			
		}
		if(somenteContinuas != null && somenteContinuas){
			hql.append("AND sessao.continua = :somenteContinuas ");
			params.put("somenteContinuas", somenteContinuas);
		}
		hql.append("ORDER BY sessao.dataSessao DESC");
		
		Query query = getEntityManager().createQuery(hql.toString());
		for(Entry<String, Object> param : params.entrySet()){
			query.setParameter(param.getKey(), param.getValue());
		}
		try{
			return (List<Sessao>) query.getResultList();
		}
		catch(NoResultException e){
			return Collections.EMPTY_LIST;
		}
	}
}