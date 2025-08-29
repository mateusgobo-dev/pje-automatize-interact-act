package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCompetencia;

@Name(OrgaoJulgadorCompetenciaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class OrgaoJulgadorCompetenciaList extends EntityList<OrgaoJulgadorCompetencia> {

	public static final String NAME = "orgaoJulgadorCompetenciaList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from OrgaoJulgadorCompetencia o "
			+ "where o.orgaoJulgador = #{orgaoJulgadorHome.instance}";
	private static final String DEFAULT_ORDER = "competencia";

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