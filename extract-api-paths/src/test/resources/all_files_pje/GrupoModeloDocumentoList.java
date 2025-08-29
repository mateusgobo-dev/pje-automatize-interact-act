package br.com.infox.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.GrupoModeloDocumento;

@Name(GrupoModeloDocumentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class GrupoModeloDocumentoList extends EntityList<GrupoModeloDocumento> {

	public static final String NAME = "grupoModeloDocumentoList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from GrupoModeloDocumento o";
	private static final String DEFAULT_ORDER = "grupoModeloDocumento";

	@Override
	protected void addSearchFields() {
		addSearchField("grupoModeloDocumento", SearchCriteria.contendo);
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