package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.lancadormovimento.Dominio;

@Name(DominioList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class DominioList extends EntityList<Dominio>{

	public static final String NAME = "dominioList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from Dominio o";
	private static final String DEFAULT_ORDER = "nomeDominio";

	private static final String R1 = "o.codigo like concat(" +
		"#{dominioList.entity.codigo}, '%')";

	private static final String R2 = "o.nomeDominio like concat(" +
		"#{dominioList.entity.nomeDominio}, '%')";

	public DominioList(){
		super();
		setEjbql(DEFAULT_EJBQL);
		setOrder(DEFAULT_ORDER);
		setMaxResults(DEFAULT_MAX_RESULT);
	}

	protected void addSearchFields(){
		addSearchField("ativo", SearchCriteria.igual);
		addSearchField("codigo", SearchCriteria.igual, R1);
		addSearchField("nomeDominio", SearchCriteria.igual, R2);
	}

	protected Map<String, String> getCustomColumnsOrder(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("nomeDominio", "nomeDominio");
		return map;
	}

	protected String getDefaultEjbql(){
		return DEFAULT_EJBQL;
	}

	protected String getDefaultOrder(){
		return DEFAULT_ORDER;
	}

	public boolean showGrid(){
		return false;
	}

}