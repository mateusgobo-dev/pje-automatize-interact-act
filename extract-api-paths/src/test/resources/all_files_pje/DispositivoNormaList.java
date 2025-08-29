package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.DispositivoNorma;

@Name(DispositivoNormaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class DispositivoNormaList extends EntityList<DispositivoNorma> {

	public static final String NAME = "dispositivoNormaList";

	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_EJBQL = "select o from DispositivoNorma  o where o.normaPenal.idNormaPenal = #{normaPenalHome.instance.idNormaPenal}";
	private static final String DEFAULT_ORDER = "o.numeroOrdem";

	
	
	@Override
	protected void addSearchFields() {
		addSearchField("dsIdentificador", SearchCriteria.contendo);
		addSearchField("tipoDispositivoNorma", SearchCriteria.igual);
		addSearchField("ativo", SearchCriteria.igual);
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
	
	@Override
	public List<DispositivoNorma> list(int maxResult) {
		List<DispositivoNorma> returnValue = super.list(maxResult);

		return returnValue;
	}
}