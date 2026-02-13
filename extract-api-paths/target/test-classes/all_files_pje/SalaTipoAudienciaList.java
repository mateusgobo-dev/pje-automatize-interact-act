/* $Id: SalaHorarioList.java 17521 2011-01-25 18:34:25Z laercio $ */

package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.TipoAudiencia;

@Name(SalaTipoAudienciaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SalaTipoAudienciaList extends EntityList<TipoAudiencia> {

	public static final String NAME = "salaTipoAudienciaList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o.tipoAudienciaList from Sala o";
	private static final String DEFAULT_ORDER = "1";

	private static final String R1 = "o = #{salaHome.definedInstance}";

	@Override
	protected void addSearchFields() {
		addSearchField("sala", SearchCriteria.igual, R1);
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
