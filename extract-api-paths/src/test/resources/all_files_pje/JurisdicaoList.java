package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.Jurisdicao;

@Name(JurisdicaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class JurisdicaoList extends EntityList<Jurisdicao> {

	public static final String NAME = "jurisdicaoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from Jurisdicao o ";
	private static final String DEFAULT_ORDER = "o.jurisdicao";

	@Override
	protected void addSearchFields() {
		addSearchField("jurisdicao", SearchCriteria.contendo);
		addSearchField("ativo", SearchCriteria.igual);
		addSearchField("estado", SearchCriteria.igual);
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