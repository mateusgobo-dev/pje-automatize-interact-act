package br.jus.csjt.pje.persistence.dao;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.jt.entidades.AtividadeEconomica;

@Name(AtividadeEconomicaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AtividadeEconomicaList extends EntityList<AtividadeEconomica> {

	public static final String NAME = "atividadeEconomicaList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from AtividadeEconomica o";
	private static final String DEFAULT_ORDER = "nomeAtividadeEconomica";

	@Override
	protected void addSearchFields() {
		addSearchField("nomeAtividadeEconomica", SearchCriteria.contendo);
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
