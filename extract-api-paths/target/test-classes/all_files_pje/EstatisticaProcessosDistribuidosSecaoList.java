package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.util.ParametroUtil;

@Name(EstatisticaProcessosDistribuidosSecaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosDistribuidosSecaoList extends EntityList<String> {
	public static final String NAME = "estatisticaProcessosDistribuidosSecaoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.codEstado, o.orgaoJulgador";
	private static final String GROUP_BY = "o.codEstado, o.orgaoJulgador";

	private static final String R1 = " to_char(o.dataInclusao,'yyyy-MM-dd') >= #{estatisticaProcessosDistribuidosSecaoAction.dataInicioFormatada} ";
	private static final String R2 = " to_char(o.dataInclusao,'yyyy-MM-dd') <= #{estatisticaProcessosDistribuidosSecaoAction.dataFimFormatada} ";
	private static final String R3 = "o.codEstado like #{estatisticaProcessosDistribuidosSecaoAction.secaoJudiciaria.cdSecaoJudiciaria} ";

	@Override
	protected void addSearchFields() {
		addSearchField("dataInclusaoInicio", SearchCriteria.igual, R1);
		addSearchField("dataInclusaoFim", SearchCriteria.igual, R2);
		addSearchField("secaoJudiciaria", SearchCriteria.igual, R3);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.codEstado from EstatisticaEventoProcesso o ");
		sb.append("where o.codEvento = #{parametroUtil.getEventoProcessualDistribuicao().codEvento} ");
		sb.append("and cast(o.dataInclusao as date) <= #{estatisticaProcessosDistribuidosSecaoAction.dataFim} ");
		sb.append("and cast(o.dataInclusao as date) >= #{estatisticaProcessosDistribuidosSecaoAction.dataInicio} ");
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			sb.append("and o.codEstado = #{parametroUtil.secao} ");
		}
		return sb.toString();
	}

	@Override
	public String getGroupBy() {
		return GROUP_BY;
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
	public String getEntity() {
		if (entity == null) {
			entity = (String) Contexts.getConversationContext().get(getEntityComponentName());
			if (entity == null) {
				entity = new String();
			}
		}
		return entity;
	}

	@Override
	public void newInstance() {
		entity = new String();
	}

}