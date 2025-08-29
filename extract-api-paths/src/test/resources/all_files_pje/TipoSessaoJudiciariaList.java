package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.TipoSessao;

@Name(TipoSessaoJudiciariaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class TipoSessaoJudiciariaList extends EntityList<TipoSessao> {

	public static final String NAME = "tipoSessaoJudiciariaList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from SecaoJudiciaria o";
	private static final String DEFAULT_ORDER = "descSecaoJudiciaria";

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