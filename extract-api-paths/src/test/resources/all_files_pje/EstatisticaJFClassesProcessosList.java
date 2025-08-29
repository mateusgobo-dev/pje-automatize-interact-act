package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(EstatisticaJFClassesProcessosList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaJFClassesProcessosList extends EntityList<Map<String, Object>> {

	public static final String NAME = "estatisticaJFClassesProcessosList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_ORDER = "o.classeJudicial.classeJudicial asc";
	private static final String GROUP_BY = "o.classeJudicial, o.classeJudicial.classeJudicial";

	private static final String R1 = "o.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList})";
	private static final String R2 = "o.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.cargoJuiz} ";
	private static final String R3 = "o.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.competencia} ";

	@Override
	protected void addSearchFields() {
		addSearchField("classeJudicial", SearchCriteria.igual, R1);
		addSearchField("cargo", SearchCriteria.igual, R2);
		addSearchField("competencia", SearchCriteria.igual, R3);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select new Map(o.classeJudicial as classe, o.classeJudicial.classeJudicial as nome) from EstatisticaProcessoJusticaFederal o ");
		sb.append("where o.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
		sb.append("and o.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append("     and to_char(o.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("     and to_char(o.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		return sb.toString();
	}

	@Override
	protected String getEntityName() {
		return NAME;
	}

	@Override
	public List<Map<String, Object>> getResultList() {
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

}