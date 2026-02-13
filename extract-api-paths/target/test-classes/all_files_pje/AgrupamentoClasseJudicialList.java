/* $Id: AgrupamentoList.java 869 2010-09-28 14:06:42Z luizruiz $ */

package br.com.infox.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.AgrupamentoClasseJudicial;

@Name(AgrupamentoClasseJudicialList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AgrupamentoClasseJudicialList extends EntityList<AgrupamentoClasseJudicial> {

	public static final String NAME = "agrupamentoClasseJudicialList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from AgrupamentoClasseJudicial o";
	private static final String DEFAULT_ORDER = "agrupamento";

	@Override
	protected void addSearchFields() {
		addSearchField("agrupamento", SearchCriteria.contendo);
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

}