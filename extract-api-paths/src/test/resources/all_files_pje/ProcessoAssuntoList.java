package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;

@Name(ProcessoAssuntoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoAssuntoList extends EntityList<ProcessoAssunto> {

	private static final long serialVersionUID = 1884997099710801870L;

	public static final String NAME = "processoAssuntoList";

	private static final String DEFAULT_EJBQL = "select o from ProcessoAssunto o ";
	private static final String DEFAULT_ORDER = "o.assuntoTrf.assuntoTrf ";

	private static final String R1 = "o.processoTrf.idProcessoTrf = #{processoTrfHome.instance.idProcessoTrf} ";

	@Override
	protected void addSearchFields() {
		addSearchField("processo", SearchCriteria.igual, R1);
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