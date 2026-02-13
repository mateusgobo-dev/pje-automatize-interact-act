package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoCompetencia;

@Name(OrgaoJulgadorColegiadoCompetenciaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class OrgaoJulgadorColegiadoCompetenciaList extends EntityList<OrgaoJulgadorColegiadoCompetencia> {

	public static final String NAME = "orgaoJulgadorColegiadoCompetenciaList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from OrgaoJulgadorColegiadoCompetencia o where o.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoHome.instance}";
	private static final String DEFAULT_ORDER = "idOrgaoJulgadorColegiadoCompetencia";

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