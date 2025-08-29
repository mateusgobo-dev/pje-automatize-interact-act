package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.Area;

@Name(AreaList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class AreaList extends EntityList<Area> {

	public static final String NAME = "areaList";
	private static final long serialVersionUID = -452754229147682857L;
	private static final String DEFAULT_EJBQL = "select o from Area o";
	private static final String DEFAULT_ORDER = "o.dsArea";

	@Override
	protected void addSearchFields() {
		addSearchField("dsArea", SearchCriteria.contendo);
		addSearchField("ativo", SearchCriteria.igual);
		addSearchField("centralMandado.idCentralMandado", SearchCriteria.igual);
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

	public static String getName() {
		return NAME;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
