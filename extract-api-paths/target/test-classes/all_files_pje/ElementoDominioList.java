package br.com.infox.pje.list;

import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.lancadormovimento.ElementoDominio;

@Name(ElementoDominioList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ElementoDominioList extends EntityList<ElementoDominio>{

	public static final String NAME = "elementoDominioList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from ElementoDominio o";
	private static final String DEFAULT_ORDER = "valor";

	private static final String R1 = "o.dominio = #{dominioHome.dominio}";

	protected void addSearchFields(){
		addSearchField("dominio", SearchCriteria.igual, R1);
	}

	protected Map<String, String> getCustomColumnsOrder(){
		return null;
	}

	protected String getDefaultEjbql(){
		return DEFAULT_EJBQL;
	}

	protected String getDefaultOrder(){
		return DEFAULT_ORDER;
	}

}