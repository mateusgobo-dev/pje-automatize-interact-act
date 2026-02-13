package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ItemsLog;


@Name(ItemLogList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ItemLogList extends EntityList<ItemsLog> {

	private static final long serialVersionUID = 1401056105531721717L;
	public static final String NAME = "itemLogList";
	private static final String DEFAULT_ORDER = "o.idItem";
	private static final String R1 = "o.processoTrfLog.idProcessoTrfLog = #{processoTrfLogDistribuicaoHome.id} ";

	@Override
	protected void addSearchFields() {
		addSearchField("processoTrfLog", SearchCriteria.igual, R1);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {

		StringBuilder sql = new StringBuilder();
		sql.append("select o from ItemsLog o WHERE 1=1 ");

		return sql.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

}