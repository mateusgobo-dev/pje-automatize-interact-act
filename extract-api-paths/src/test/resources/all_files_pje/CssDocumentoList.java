package br.com.infox.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.editor.CssDocumento;

@Name(CssDocumentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class CssDocumentoList extends EntityList<CssDocumento>{

	public static final String NAME = "cssDocumentoList";
	
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_EJBQL = "select o from CssDocumento o";
	private static final String DEFAULT_ORDER = "nome";

	@Override
	protected void addSearchFields() {
		addSearchField("nome", SearchCriteria.contendo);
		addSearchField("ativo", SearchCriteria.igual);
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
