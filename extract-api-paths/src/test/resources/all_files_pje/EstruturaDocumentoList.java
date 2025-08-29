package br.com.infox.editor.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumento;

@Name(EstruturaDocumentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class EstruturaDocumentoList extends EntityList<EstruturaDocumento> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "estruturaDocumentoList";
	
	private static final String DEFAULT_EJBQL = "select o from EstruturaDocumento o ";
	private static final String DEFAULT_ORDER = "o.estruturaDocumento";
	
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
		addSearchField("estruturaDocumento", SearchCriteria.contendo);
		addSearchField("ativo", SearchCriteria.igual);
	}

}