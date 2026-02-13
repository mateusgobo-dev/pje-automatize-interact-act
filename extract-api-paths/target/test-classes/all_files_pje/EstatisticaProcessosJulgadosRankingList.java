package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(EstatisticaProcessosJulgadosRankingList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosJulgadosRankingList extends EntityList<Object[]> {

	public static final String NAME = "estatisticaProcessosJulgadosRankingList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_EJBQL = "select o.competencia, o.orgaoJulgador, o.jurisdicao, count(distinct o.numeroProcesso) as qtdProcesso "
			+ "from EstatisticaEventoProcesso o where o.codEvento = #{parametroUtil.getEventoJulgamentoProcessual().codEvento} ";

	private static final String GROUP_BY = "o.competencia, o.orgaoJulgador, o.jurisdicao";
	private static final String DEFAULT_ORDER = "o.competencia, 4 desc";

	private static final String R1 = " to_char(o.dataInclusao,'yyyy-MM') >= #{estatisticaRankingTipoVaraAction.dataInicioFormatada} ";
	private static final String R2 = " to_char(o.dataInclusao,'yyyy-MM') <= #{estatisticaRankingTipoVaraAction.dataFimFormatada} ";
	private static final String R3 = "o.competencia like #{estatisticaRankingTipoVaraAction.competencia}";
	private static final String R4 = "o.classeJudicial != #{estatisticaRankingTipoVaraAction.incluiEmbargosDeclaracao ? null : 'EMBARGOS DE DECLARAÇÃO'}";
	private static final String R5 = "o.codEstado = #{parametroUtil.primeiroGrau ? parametroUtil.secao : estatisticaRankingTipoVaraAction.secaoJudiciaria.cdSecaoJudiciaria}";

	@Override
	protected void addSearchFields() {
		addSearchField("dataInclusaoInicio", SearchCriteria.igual, R1);
		addSearchField("dataInclusaoFim", SearchCriteria.igual, R2);
		addSearchField("competencia", SearchCriteria.igual, R3);
		addSearchField("classeJudicial", SearchCriteria.igual, R4);
		addSearchField("codEstado", SearchCriteria.igual, R5);
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