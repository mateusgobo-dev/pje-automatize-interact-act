package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.PesoQtdEndereco;

@Name(PesoQtdEnderecoList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class PesoQtdEnderecoList extends EntityList<PesoQtdEndereco> {

	private static final long serialVersionUID = -5740991141577759508L;
	public static final String NAME = "pesoQtdEnderecoList";

	private static final String DEFAULT_EJBQL = "select o from PesoQtdEndereco o";
	private static final String DEFAULT_ORDER = "o.nrEndereco";

	@Override
	protected void addSearchFields() {
		addSearchField("ativo", SearchCriteria.igual);
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
