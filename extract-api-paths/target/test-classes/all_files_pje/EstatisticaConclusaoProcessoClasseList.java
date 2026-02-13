package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@Name(EstatisticaConclusaoProcessoClasseList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaConclusaoProcessoClasseList extends
		AbstractEstatisticaConclusaoProcessoList<Map<String, Object>> {

	public static final String NAME = "estatisticaConclusaoProcessoClasseList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_ORDER = "o.classeJudicial";

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select new Map(o.classeJudicial as classe, o.classeJudicial.classeJudicial as nome) from EstatisticaProcessoJusticaFederal o ");
		sb.append("where o.secaoJudiciaria = #{estatisticaConclusaoAction.getSecao().getCdSecaoJudiciaria()} ");
		sb.append("   and o.orgaoJulgador = #{estatisticaConclusaoAction.getOrgaoJulgador()} ");
		sb.append("   and to_char(o.dtEvento,'yyyy-MM-dd') = #{estatisticaConclusaoAction.dataInicio} ");
		return sb.toString();
	}

	@Override
	protected String getEntityName() {
		return NAME;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	public List<Map<String, Object>> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}
}