package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(ProcessosDistribuidosRankingRegionalList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessosDistribuidosRankingRegionalList extends EntityList<Map<String, Object>> {

	public static final String NAME = "processosDistribuidosRankingRegionalList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select new map(o.codEstado as codEstado) "
			+ "from EstatisticaEventoProcesso o where o.codEvento = #{parametroUtil.getEventoProcessualDistribuicao().getCodEvento()}"
			+ " and o.codEstado like #{parametroUtil.secao} ";
	private static final String DEFAULT_ORDER = "o.codEstado";
	private static final String GROUP_BY = "o.codEstado";

	private static final String R1 = " to_char(o.dataInclusao,'MM/yyyy') >= #{estatisticaProcessosDistribuidosRankingRegionalAction.dataInicio} ";
	private static final String R2 = " to_char(o.dataInclusao,'MM/yyyy') <= #{estatisticaProcessosDistribuidosRankingRegionalAction.dataFim} ";

	@Override
	protected void addSearchFields() {
		addSearchField("dataInclusaoInicio", SearchCriteria.igual, R1);
		addSearchField("dataInclusaoFim", SearchCriteria.igual, R2);
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

	@Override
	public String getGroupBy() {
		return GROUP_BY;
	}

	@Override
	protected String getEntityName() {
		return NAME;
	}

	@Override
	public void newInstance() {
		entity = new HashMap<String, Object>();
	}
}