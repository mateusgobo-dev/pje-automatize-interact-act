package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(MapaDeProdutividadeDaSecaoJudiciariaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class MapaDeProdutividadeDaSecaoJudiciariaList extends EntityList<Object[]> {

	public static final String NAME = "mapaDeProdutividadeDaSecaoJudiciariaList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o.codEstado as estado, o.competencia as competencia, "
			+ "count(distinct o.numeroProcesso) as qtdSentenasProferidas, extract(month from o.dataInclusao) as mes "
			+ "from EstatisticaEventoProcesso o where o.competencia != null "
			+ "and o.codEvento = #{parametroUtil.getEventoJulgamento().codEvento}";
	private static final String DEFAULT_ORDER = "o.competencia, o.codEstado";
	private static final String GROUP_BY = "o.competencia, o.codEstado, extract(month from o.dataInclusao) ";

	private static final String R1 = " cast(o.dataInclusao as date) >= #{mapaDeProdutividadeDaSecaoJudiciariaAction.dataInicio} ";
	private static final String R2 = " cast(o.dataInclusao as date) <= #{mapaDeProdutividadeDaSecaoJudiciariaAction.dataFim} ";
	private static final String R3 = " competencia = #{mapaDeProdutividadeDaSecaoJudiciariaAction.competencia} ";

	@Override
	protected void addSearchFields() {
		addSearchField("dataInclusaoInicio", SearchCriteria.igual, R1);
		addSearchField("dataInclusaoFim", SearchCriteria.igual, R2);
		addSearchField("competencia", SearchCriteria.igual, R3);
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
	public Object[] getEntity() {
		if (entity == null) {
			entity = (Object[]) Contexts.getConversationContext().get(getEntityComponentName());
			if (entity == null) {
				entity = new Object[0];
			}
		}
		return entity;
	}

}