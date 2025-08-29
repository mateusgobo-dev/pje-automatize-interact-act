package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.CondicaoSuspensao;

@Name(CondicaoSuspensaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class CondicaoSuspensaoList extends EntityList<CondicaoSuspensao> {

	public static final String NAME = "condicaoSuspensaoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from CondicaoSuspensao o "
			+ "where o.tipoSuspensao.id = #{tipoSuspensaoHome.instance.id}";
	private static final String DEFAULT_ORDER = "id";

	@Override
	protected void addSearchFields() {
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