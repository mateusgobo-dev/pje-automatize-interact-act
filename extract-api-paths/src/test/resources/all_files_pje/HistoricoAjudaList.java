package br.com.infox.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ajuda.HistoricoAjuda;

@Name(HistoricoAjudaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class HistoricoAjudaList extends EntityList<HistoricoAjuda> {

	public static final String NAME = "historicoAjudaList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from HistoricoAjuda o";
	private static final String DEFAULT_ORDER = "dataRegistro desc";

	private static final String R1 = "pagina.url = #{ajudaHome.viewId}";

	@Override
	protected void addSearchFields() {
		addSearchField("pagina.url", SearchCriteria.igual, R1);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		// TODO Auto-generated method stub
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