package br.com.infox.editor.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumento;

@Name(EstruturaTipoDocumentoVinculadoList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class EstruturaTipoDocumentoVinculadoList extends EntityList<EstruturaDocumento> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "estruturaTipoDocumentoVinculadoList";
	
	private static final String DEFAULT_EJBQL = "select o from EstruturaTipoDocumento o ";
	private static final String DEFAULT_ORDER = "o.tipoProcessoDocumento.tipoProcessoDocumento";

	@Override
	protected void addSearchFields() {
		addSearchField("estruturaDocumento", SearchCriteria.igual, "o.estruturaDocumento = #{estruturaDocumentoHome.instance}");
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
