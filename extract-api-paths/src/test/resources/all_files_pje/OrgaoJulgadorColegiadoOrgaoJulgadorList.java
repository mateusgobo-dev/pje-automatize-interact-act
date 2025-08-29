package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;

@Name(OrgaoJulgadorColegiadoOrgaoJulgadorList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class OrgaoJulgadorColegiadoOrgaoJulgadorList extends EntityList<OrgaoJulgadorColegiadoOrgaoJulgador> {

	public static final String NAME = "orgaoJulgadorColegiadoOrgaoJulgadorList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from OrgaoJulgadorColegiadoOrgaoJulgador o ";
	private static final String DEFAULT_ORDER = "o.ordem, o.idOrgaoJulgadorColegiadoOrgaoJulgador";

	private static final String R1 = "o.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoHome.definedInstance}";
	private static final String R2 = "o.orgaoJulgadorColegiado = #{sessaoHome.instance.orgaoJulgadorColegiado}";

	@Override
	protected void addSearchFields() {
		addSearchField("orgaoJulgadorColegiado", SearchCriteria.igual, R1);
		addSearchField("orgaoJulgadorColegiadoSessao", SearchCriteria.igual, R2);
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