package br.com.infox.editor.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.editor.XslDocumento;

@Name(XslDocumentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class XslDocumentoList extends EntityList<XslDocumento>{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "xslDocumentoList";
	
	private static final String DEFAULT_EJBQL = "select o from XslDocumento o ";
	private static final String DEFAULT_ORDER = "o.nome";
	
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected void addSearchFields() {
		addSearchField("nome", SearchCriteria.contendo);
		addSearchField("ativo", SearchCriteria.igual);
	}
}
