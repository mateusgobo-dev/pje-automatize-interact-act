package br.jus.jt.estatistica.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.jt.entidades.estatistica.Quadro;

@Name(QuadroList.NAME)
@BypassInterceptors
@Scope(ScopeType.EVENT)
public class QuadroList extends EntityList<Quadro> {

	public static final String NAME = "quadroList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from Quadro o ";
	private static final String DEFAULT_ORDER = "ordem";

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

	@Override
	protected void addSearchFields() {
		// TODO Auto-generated method stub
		
	}

}
