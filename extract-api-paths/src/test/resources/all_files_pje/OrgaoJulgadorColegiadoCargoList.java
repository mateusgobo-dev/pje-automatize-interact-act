package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoCargo;

@Name(OrgaoJulgadorColegiadoCargoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class OrgaoJulgadorColegiadoCargoList extends EntityList<OrgaoJulgadorColegiadoCargo> {

	public static final String NAME = "orgaoJulgadorColegiadoCargoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from OrgaoJulgadorColegiadoCargo o "
			+ "where o.orgaoJulgadorColegiado= #{orgaoJulgadorColegiadoCargoHome.instance.orgaoJulgadorColegiado}";
	private static final String DEFAULT_ORDER = "cargo";

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