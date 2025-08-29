package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;

@Name(EnderecoWsdlList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EnderecoWsdlList extends EntityList<EnderecoWsdl> {

	public static final String NAME = "enderecoWsdlList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_EJBQL = "select o from EnderecoWsdl o ";
	private static final String DEFAULT_ORDER = "idEnderecoWsdl";

	@Override
	protected void addSearchFields() {
		addSearchField("descricao", SearchCriteria.contendo);
		addSearchField("wsdlIntercomunicacao", SearchCriteria.contendo);
		addSearchField("wsdlConsulta", SearchCriteria.contendo);
		addSearchField("instancia", SearchCriteria.igual);
	}

	@Override
	public void newInstance() {
		super.newInstance();
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