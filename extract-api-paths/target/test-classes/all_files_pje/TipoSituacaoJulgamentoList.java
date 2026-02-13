package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.TipoSituacaoJulgamento;

/**
 * TipoSituacaoJulgamento
 */

@Name(TipoSituacaoJulgamentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class TipoSituacaoJulgamentoList extends EntityList<TipoSituacaoJulgamento> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tipoSituacaoJulgamentoList";

	private static final String DEFAULT_EJBQL = "select o from TipoSituacaoJulgamento o";
	private static final String DEFAULT_ORDER = "tipoSituacaoJulgamento";

	@Override
	protected void addSearchFields() {
		addSearchField("tipoSituacaoJulgamento", SearchCriteria.contendo);
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
