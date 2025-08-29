package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgadorLog;

@Name(OrgaoJulgadorColegiadoOrgaoJulgadorLogList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class OrgaoJulgadorColegiadoOrgaoJulgadorLogList extends EntityList<OrgaoJulgadorColegiadoOrgaoJulgadorLog> {

	public static final String NAME = "orgaoJulgadorColegiadoOrgaoJulgadorLogList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from OrgaoJulgadorColegiadoOrgaoJulgadorLog o where o.orgaoJulgadorColegiado in(select oj.orgaoJulgadorColegiado.orgaoJulgadorColegiado from "
			+ "OrgaoJulgadorColegiado oj where oj = #{orgaoJulgadorColegiadoHome.instance})";
	private static final String DEFAULT_ORDER = "dataInicial";

	@Override
	protected void addSearchFields() {
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
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}
}