package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.TipoSessao;

@Name(TipoSessaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class TipoSessaoList extends EntityList<TipoSessao> {

	public static final String NAME = "tipoSessaoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from TipoSessao o";
	private static final String DEFAULT_ORDER = "idTipoSessao";

	@Override
	protected void addSearchFields() {
		addSearchField("tipoSessao", SearchCriteria.contendo);
		addSearchField("ativo", SearchCriteria.igual);
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