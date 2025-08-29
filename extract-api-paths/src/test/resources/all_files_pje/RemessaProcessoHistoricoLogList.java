package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.RemessaProcessoHistoricoLog;

@Name(RemessaProcessoHistoricoLogList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class RemessaProcessoHistoricoLogList extends EntityList<RemessaProcessoHistoricoLog> {

	public static final String NAME = "remessaProcessoHistoricoLogList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from RemessaProcessoHistoricoLog o ";
	private static final String DEFAULT_ORDER = "o.dataCadastro desc";

	@Override
	protected void addSearchFields() {
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
		map.put("dataCadastro", DEFAULT_ORDER);
		return map;
	}

}