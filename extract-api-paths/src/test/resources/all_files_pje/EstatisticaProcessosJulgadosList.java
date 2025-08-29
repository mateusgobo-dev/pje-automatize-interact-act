package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(EstatisticaProcessosJulgadosList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosJulgadosList extends EntityList<Map<String, Object>> {

	public static final String NAME = "estatisticaProcessosJulgadosList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_EJBQL = "select new map(o.codEstado as codEstado)"
			+ "from EstatisticaEventoProcesso o where o.codEvento = #{parametroUtil.getEventoJulgamentoProcessual().getCodEvento()}";
	private static final String DEFAULT_GROUP_BY = "o.codEstado";
	private static final String DEFAULT_ORDER = "o.codEstado";

	private static final String R1 = " to_char(o.dataInclusao,'yyyy-MM') >= #{estatisticaProcessosJulgadosAction.dataInicioFormatada} ";
	private static final String R2 = " to_char(o.dataInclusao,'yyyy-MM') <= #{estatisticaProcessosJulgadosAction.dataFimFormatada} ";
	private static final String R3 = "o.classeJudicial != #{estatisticaProcessosJulgadosAction.incluiEmbargosDeclaracao ? null : 'EMBARGOS DE DECLARAÇÃO'}";
	private static final String R4 = "o.codEstado = #{ParametroUtil.instance().isPrimeiroGrau() ? parametroUtil.secao : estatisticaProcessosJulgadosAction.secaoJudiciaria.cdSecaoJudiciaria} ";

	@Override
	protected void addSearchFields() {
		addSearchField("dataInclusaoInicio", SearchCriteria.igual, R1);
		addSearchField("dataInclusaoFim", SearchCriteria.igual, R2);
		addSearchField("classeJudicial", SearchCriteria.diferente, R3);
		addSearchField("codEstado", SearchCriteria.igual, R4);
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
	protected String getEntityName() {
		return NAME;
	}

	@Override
	public String getGroupBy() {
		return DEFAULT_GROUP_BY;
	}

	@Override
	public void newInstance() {
		entity = new HashMap<String, Object>();
	}
}