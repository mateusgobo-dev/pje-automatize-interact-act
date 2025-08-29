package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.Processo;

@Name(ProcessoBloqueadoFluxoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoBloqueadoFluxoList extends EntityList<Processo> {

	public static final String NAME = "processoBloqueadoFluxoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from Processo o where o.actorId != null";
	private static final String DEFAULT_ORDER = "idProcesso";

	@Override
	protected void addSearchFields() {
		addSearchField("numeroProcesso", SearchCriteria.contendo);
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
