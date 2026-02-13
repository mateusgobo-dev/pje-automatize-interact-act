package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.QuadroAvisoPapel;

@Name(QuadroAvisoPapelList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class QuadroAvisoPapelList extends EntityList<QuadroAvisoPapel> {

	public static final String NAME = "quadroAvisoPapelList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from QuadroAvisoPapel o ";
	private static final String DEFAULT_ORDER = "o.papel.nome";

	private static final String R1 = "o.quadroAviso =  #{quadroAvisoHome.instance}";

	@Override
	protected void addSearchFields() {
		addSearchField("idQuadroAvisoPapel", SearchCriteria.igual, R1);
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

}
