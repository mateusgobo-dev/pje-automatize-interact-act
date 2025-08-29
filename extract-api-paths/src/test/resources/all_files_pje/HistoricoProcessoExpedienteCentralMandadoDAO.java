package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.CentralMandado;
import br.jus.pje.nucleo.entidades.HistoricoProcessoExpedienteCentralMandado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.ProcessoExpedienteCentralMandadoStatusEnum;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

@Name(HistoricoProcessoExpedienteCentralMandadoDAO.NAME)
public class HistoricoProcessoExpedienteCentralMandadoDAO extends BaseDAO<HistoricoProcessoExpedienteCentralMandado> {

	public static final String NAME = "historicoProcessoExpedienteCentralMandadoDAO";
	
	@Override
	public Object getId(HistoricoProcessoExpedienteCentralMandado e) {
		return e.getIdHistoricoProcessoExpedienteCentralMandado();
	}

	@Override
	protected void loadOrderBy(StringBuilder sb, Map<String, Order> orders) {
		boolean ordered = false;
		for(Entry<String, Order> e: orders.entrySet()){
			if(!ordered){
				sb.append(" ORDER BY ");
				ordered = true;
			}else{
				sb.append(",");
			}
			String order = e.getKey();
			if(!order.startsWith("o.")) {
				order = "o." + order; 
			}
			sb.append(order);
			if(e.getValue() == Order.DESC){
				sb.append(" DESC");
			}
		}
	}

	@SuppressWarnings("unchecked")
	public List<HistoricoProcessoExpedienteCentralMandado> obterPorProcessoExpedienteCentralMandado(ProcessoExpediente processoExpediente){
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append(" SELECT hpe ");
		sbQuery.append(" FROM HistoricoProcessoExpedienteCentralMandado hpe ");
		sbQuery.append(" JOIN hpe.processoExpedienteCentralMandado pe ");
		sbQuery.append(" WHERE pe.processoExpediente = :processoExpediente ");
		
		Query query = getEntityManager().createQuery(sbQuery.toString());
		query.setParameter("processoExpediente", processoExpediente);
		
		return (List<HistoricoProcessoExpedienteCentralMandado>) query.getResultList();
	}
}
