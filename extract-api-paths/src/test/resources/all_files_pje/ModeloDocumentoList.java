package br.com.infox.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ModeloDocumento;

@Name(ModeloDocumentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ModeloDocumentoList extends EntityList<ModeloDocumento> {

	public static final String NAME = "modeloDocumentoList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from ModeloDocumento o";
	private static final String DEFAULT_ORDER = "tituloModeloDocumento";

	@Override
	protected void addSearchFields() {
		addSearchField("ativo", SearchCriteria.igual);
		addSearchField("tipoModeloDocumento", SearchCriteria.igual);
		addSearchField("tituloModeloDocumento", SearchCriteria.contendo);
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