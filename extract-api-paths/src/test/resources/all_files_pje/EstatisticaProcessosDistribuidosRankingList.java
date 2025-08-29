package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(EstatisticaProcessosDistribuidosRankingList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosDistribuidosRankingList extends EntityList<Object[]> {

	public static final String NAME = "estatisticaProcessosDistribuidosRankingList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_EJBQL = "select o.codEstado, o.orgaoJulgador, o.jurisdicao, count(distinct o.numeroProcesso) "
			+ "as qtdProcesso "
			+ "from EstatisticaEventoProcesso o where o.codEvento = #{parametroUtil.eventoProcessualDistribuicao.codEvento} ";

	private static final String DEFAULT_ORDER = "o.codEstado, count(o.numeroProcesso) desc";
	private static final String GROUP_BY = "o.codEstado, o.orgaoJulgador, o.jurisdicao";

	private static final String R1 = "o.codEstado like #{parametroUtil.primeiroGrau ? parametroUtil.secao : estatisticaRankingSecaoAction.secaoJudiciaria.cdSecaoJudiciaria} ";
	private static final String R2 = " to_char(o.dataInclusao,'yyyy-MM') >= #{estatisticaRankingSecaoAction.dataInicioFormatada} ";
	private static final String R3 = " to_char(o.dataInclusao,'yyyy-MM') <= #{estatisticaRankingSecaoAction.dataFimFormatada} ";

	@Override
	protected void addSearchFields() {
		addSearchField("codEstado", SearchCriteria.igual, R1);
		addSearchField("dataInclusaoInicio", SearchCriteria.igual, R2);
		addSearchField("dataInclusaoFim", SearchCriteria.igual, R3);
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