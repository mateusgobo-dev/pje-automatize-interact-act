package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(EstatisticaProcessosDistribuidosArquivadosList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosDistribuidosArquivadosList extends EntityList<Object[]> {

	public static final String NAME = "estatisticaProcessosDistribuidosArquivadosList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_EJBQL = "select o.codEstado, o.orgaoJulgador, o.jurisdicao, count(distinct o.numeroProcesso)as procDistribuidos, "
			+ "(select count(distinct ep.numeroProcesso) from EstatisticaEventoProcesso ep "
			+ "where ep.codEstado = o.codEstado and ep.orgaoJulgador = o.orgaoJulgador "
			+ " and (ep.codEvento = #{parametroUtil.getEventoArquivamento().codEvento} or "
			+ "	   ep.codEvento = #{parametroUtil.getEventoArquivamentoProvisorio().codEvento} or "
			+ "      ep.codEvento = #{parametroUtil.getEventoArquivamentoDefinitivoProcessual().codEvento})"
			+ " and to_char(ep.dataInclusao,'yyyy-MM') >= #{estatisticaProcessosDistribuidosArquivadosAction.dataInicioFormatada}"
			+ " and to_char(ep.dataInclusao,'yyyy-MM') <= #{estatisticaProcessosDistribuidosArquivadosAction.dataFimFormatada}) as procArquivados "
			+ "from EstatisticaEventoProcesso o where o.codEvento = #{parametroUtil.getEventoProcessualDistribuicao().codEvento} ";
	private static final String DEFAULT_ORDER = "o.codEstado, o.orgaoJulgador";
	private static final String GROUP_BY = "o.codEstado, o.orgaoJulgador, o.jurisdicao";

	private static final String R1 = " to_char(o.dataInclusao,'yyyy-MM') >= #{estatisticaProcessosDistribuidosArquivadosAction.dataInicioFormatada} ";
	private static final String R2 = " to_char(o.dataInclusao,'yyyy-MM') <= #{estatisticaProcessosDistribuidosArquivadosAction.dataFimFormatada} ";
	private static final String R3 = "o.codEstado = #{parametroUtil.primeiroGrau ? parametroUtil.secao : estatisticaProcessosDistribuidosArquivadosAction.secaoJudiciaria.cdSecaoJudiciaria} ";

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