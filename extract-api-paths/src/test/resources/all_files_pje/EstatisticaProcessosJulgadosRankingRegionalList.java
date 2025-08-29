package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(EstatisticaProcessosJulgadosRankingRegionalList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class EstatisticaProcessosJulgadosRankingRegionalList extends EntityList<Object[]> {

	public static final String NAME = "estatisticaProcessosJulgadosRankingRegionalList";
	private static final long serialVersionUID = 1L;

	private static final String GROUP_BY = "o.orgaoJulgador, o.jurisdicao";
	private static final String DEFAULT_ORDER = "count(o.numeroProcesso) desc";

	private static final String R1 = " cast(o.dataInclusao as date) >= #{estatisticaProcessosJulgadosRankingRegionalAction.dataInicio} ";
	private static final String R2 = " cast(o.dataInclusao as date) <= #{estatisticaProcessosJulgadosRankingRegionalAction.dataFim} ";
	private static final String R3 = "o.competencia like #{estatisticaProcessosJulgadosRankingRegionalAction.competencia}";
	private static final String R4 = "o.classeJudicial != #{estatisticaProcessosJulgadosRankingRegionalList.incluiEmbargosDeclaracao ? null : 'EMBARGOS DE DECLARAÇÃO'}";

	private boolean incluiEmbargosDeclaracao;

	@Override
	protected void addSearchFields() {
		addSearchField("dataInclusaoInicio", SearchCriteria.igual, R1);
		addSearchField("dataInclusaoFim", SearchCriteria.igual, R2);
		addSearchField("competencia", SearchCriteria.igual, R3);
		addSearchField("classeJudicial", SearchCriteria.igual, R4);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.orgaoJulgador, o.jurisdicao, count(distinct o.numeroProcesso)  ");
		sb.append("as qtdProcesso ");
		sb.append("from EstatisticaEventoProcesso o where o.codEvento = #{parametroUtil.getEventoJulgamento().codEvento} ");
		return sb.toString();
	}

	@Override
	public List<Object[]> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
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

	public boolean getIncluiEmbargosDeclaracao() {
		return incluiEmbargosDeclaracao;
	}

	public void setIncluiEmbargosDeclaracao(boolean incluiEmbargosDeclaracao) {
		this.incluiEmbargosDeclaracao = incluiEmbargosDeclaracao;
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