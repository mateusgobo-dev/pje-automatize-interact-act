package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(EstatisticaProcessosDistribuidosJulgadosList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosDistribuidosJulgadosList extends EntityList<Object[]> {

	public static final String NAME = "estatisticaProcessosDistribuidosJulgadosList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o.codEstado, o.orgaoJulgador, o.jurisdicao, "
			+ "(select count(distinct eep.numeroProcesso) from EstatisticaEventoProcesso eep where eep.codEstado = o.codEstado "
			+ "and eep.orgaoJulgador = o.orgaoJulgador "
			+ "and to_char(eep.dataInclusao, 'yyyy-MM') >= #{estatisticaProcessosDistribuidosJulgadosAction.dataInicioFormatada} "
			+ "and to_char(eep.dataInclusao, 'yyyy-MM') <= #{estatisticaProcessosDistribuidosJulgadosAction.dataFimFormatada} "
			+ "and eep.codEvento = #{parametroUtil.eventoProcessualDistribuicao.codEvento}) as procDistribuidos, "
			+ "(select count(distinct ep.numeroProcesso) from EstatisticaEventoProcesso ep where ep.codEstado = o.codEstado "
			+ "and ep.orgaoJulgador = o.orgaoJulgador "
			+ "and to_char(ep.dataInclusao, 'yyyy-MM') >= #{estatisticaProcessosDistribuidosJulgadosAction.dataInicioFormatada} "
			+ "and to_char(ep.dataInclusao, 'yyyy-MM') <= #{estatisticaProcessosDistribuidosJulgadosAction.dataFimFormatada} "
			+ "and ep.codEvento = #{parametroUtil.eventoJulgamentoProcessual.codEvento}) as procJulgados "
			+ "from EstatisticaEventoProcesso o where 1=1 ";

	private static final String DEFAULT_ORDER = "o.codEstado, count(distinct o.numeroProcesso) desc";
	private static final String GROUP_BY = "o.codEstado, o.orgaoJulgador, o.jurisdicao";

	private static final String R1 = " to_char(o.dataInclusao, 'yyyy-MM') >= #{estatisticaProcessosDistribuidosJulgadosAction.dataInicioFormatada} ";
	private static final String R2 = " to_char(o.dataInclusao, 'yyyy-MM') <= #{estatisticaProcessosDistribuidosJulgadosAction.dataFimFormatada} ";
	private static final String R3 = " o.codEstado = #{estatisticaProcessosDistribuidosJulgadosAction.secaoJudiciaria.cdSecaoJudiciaria} ";

	@Override
	protected void addSearchFields() {
		addSearchField("dataInclusaoInicio", SearchCriteria.igual, R1);
		addSearchField("dataInclusaoFim", SearchCriteria.igual, R2);
		addSearchField("codEstado", SearchCriteria.igual, R3);
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