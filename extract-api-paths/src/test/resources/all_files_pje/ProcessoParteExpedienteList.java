package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;

@Name(ProcessoParteExpedienteList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoParteExpedienteList extends EntityList<ProcessoParteExpediente> {

	public static final String NAME = "processoParteExpedienteList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from ProcessoParteExpediente o";
	private static final String DEFAULT_ORDER = "o.idProcessoParteExpediente";

	private static final String R1 = "o.processoJudicial.idProcessoTrf = #{processoTrfHome.instance.idProcessoTrf}";

	@Override
	protected void addSearchFields() {
		addSearchField("processoTrf", SearchCriteria.igual, R1);
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