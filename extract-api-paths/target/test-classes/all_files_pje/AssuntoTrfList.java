package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.AssuntoTrf;

@Name(AssuntoTrfList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AssuntoTrfList extends EntityList<AssuntoTrf>{

	public static final String NAME = "assuntoTrfList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from AssuntoTrf o";
	private static final String DEFAULT_ORDER = "assuntoCompleto";

	private static final String R1 = "o.assuntoCompleto like concat("
		+ "#{assuntoTrfList.entity.assuntoTrfSuperior.assuntoCompleto}, '%')";

	@Override
	protected void addSearchFields(){
		addSearchField("assuntoTrf", SearchCriteria.contendo);
		addSearchField("assuntoTrfSuperior", SearchCriteria.contendo, R1);
		addSearchField("codAssuntoTrf", SearchCriteria.contendo);
		addSearchField("ativo", SearchCriteria.igual);
	}

	@Override
	protected String getDefaultEjbql(){
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder(){
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("assuntoTrfSuperior", "assuntoTrfSuperior.assuntoTrf");
		return map;
	}
}
