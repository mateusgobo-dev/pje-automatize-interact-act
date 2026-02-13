package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.JurisdicaoMunicipio;

@Name(JurisdicaoMunicipioList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class JurisdicaoMunicipioList extends EntityList<JurisdicaoMunicipio> {

	public static final String NAME = "jurisdicaoMunicipioList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from JurisdicaoMunicipio o ";
	private static final String DEFAULT_ORDER = "o.municipio.municipio";

	private static final String R1 = "o.jurisdicao = #{jurisdicaoHome.instance}";

	@Override
	protected void addSearchFields() {
		addSearchField("municipio", SearchCriteria.igual, R1);
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
		Map<String, String> map = new HashMap<String, String>();
		map.put("estado", "o.municipio.estado");
		return map;
	}

}