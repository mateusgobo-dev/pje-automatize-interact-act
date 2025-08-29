package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.pje.bean.EstatisticaJFAudienciaProcessoClasses;

@Name(EstatisticaAudienciaProcessoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaAudienciaProcessoList extends EntityList<EstatisticaJFAudienciaProcessoClasses> {

	public static final String NAME = "estatisticaAudienciaProcessoList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_ORDER = "o.idClasseJudicial";
	private static final String GROUP_BY = "o.classeJudicial";

	private static final String R1 = "o.orgaoJulgador = #{estatisticaAudienciaAction.getOrgaoJulgador()}";
	private static final String R2 = "o.secaoJudiciaria = #{estatisticaAudienciaAction.getSecaoJudiciaria().getCdSecaoJudiciaria()}";
	private static final String R3 = "o.pessoaMagistrado = #{estatisticaAudienciaAction.juiz} ";

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct new br.com.infox.pje.bean.EstatisticaJFAudienciaProcessoClasses");
		sb.append("(o.classeJudicial as classe) from EstatisticaProcessoJusticaFederal o ");
		return sb.toString();
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
	public List<EstatisticaJFAudienciaProcessoClasses> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	@Override
	protected void addSearchFields() {
		addSearchField("orgaoJulgador", SearchCriteria.igual, R1);
		addSearchField("secaoJudiciaria", SearchCriteria.igual, R2);
		addSearchField("juiz", SearchCriteria.igual, R3);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		// TODO Auto-generated method stub
		return null;
	}
}