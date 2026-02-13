package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.pje.bean.EstatisticaProcessosPrazoVencidoBean;

@Name(EstatisticaJusticaFederalProcessosPrazosVencidosList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaJusticaFederalProcessosPrazosVencidosList extends
		EntityList<EstatisticaProcessosPrazoVencidoBean> {

	public static final String NAME = "estatisticaJusticaFederalProcessosPrazosVencidosList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select new br.com.infox.pje.bean.EstatisticaProcessosPrazoVencidoBean("
			+ "o.processoJudicial as processo, "
			+ " o.processoJudicial.classeJudicial.classeJudicial as classe,  "
			+ "        o.dtPrazoLegal as dataExpiracao,  "
			+ "        extract(day from(current_date() - o.dtPrazoLegal)) as diasVencido, "
			+ " 	   (select e.evento.evento from ProcessoEvento e  "
			+ " 	   where e.processo.idProcesso = o.processoJudicial.processo.idProcesso "
			+ " 	   and e.idProcessoEvento =  "
			+ " 	   (select max(e2.idProcessoEvento)  "
			+ " 	     from ProcessoEvento e2  "
			+ " 	   where e2.processo.idProcesso = o.processoJudicial.processo.idProcesso)) as fase)"
			+ " from ProcessoParteExpediente o "
			+ " where o.dtPrazoLegal is not null "
			+ " and o.dtPrazoLegal < current_date() "
			+ " and o.prazoLegal > 0 and o.dtPrazoLegal ="
			+ " (select max(o2.dtPrazoLegal) "
			+ " from ProcessoParteExpediente o2 "
			+ " where o2.dtPrazoLegal is not null "
			+ " and o2.dtPrazoLegal < current_date() "
			+ " and o2.prazoLegal > 0 " + " and o.processoJudicial = o2.processoJudicial) ";

	private static final String DEFAULT_ORDER = "3 desc";
	private static final String R1 = " o.processoJudicial.orgaoJulgador = #{estatisticaJusticaFederalProcessosPrazosVencidosAction.orgaoJulgador}";
	private static final String R2 = " o.processoJudicial.classeJudicial in (#{estatisticaJusticaFederalProcessosPrazosVencidosAction.classeJudicialList})";
	private static final String R3 = " (select e.evento from ProcessoEvento e  "
			+ " where e.processo.idProcesso = o.processoJudicial.idProcessoTrf  "
			+ "	and e.idProcessoEvento = (select max(e2.idProcessoEvento)  " + " from ProcessoEvento e2   "
			+ " where e2.processo.idProcesso = o.processoJudicial.idProcessoTrf)) in "
			+ "(#{estatisticaJusticaFederalProcessosPrazosVencidosAction.eventoList})";

	@Override
	protected void addSearchFields() {
		addSearchField("orgaoJulgador", SearchCriteria.igual, R1);
		addSearchField("classeJudicial", SearchCriteria.igual, R2);
		addSearchField("evento", SearchCriteria.igual, R3);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

}