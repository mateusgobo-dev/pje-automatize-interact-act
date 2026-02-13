package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.jt.entidades.AudVerbaImportacao;

@Name(AudAcordoVerbaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AudAcordoVerbaList extends EntityList<AudVerbaImportacao> {

	private static final long serialVersionUID = 1401056105531721717L;
	public static final String NAME = "audAcordoVerbaList";
	private static final String DEFAULT_ORDER = "o.idVerbaImportacao";
	private static final String R1 = "o.audImportacao = #{audAcordoVerbaList.entity.audImportacao} ";

	@Override
	protected void addSearchFields() {
		addSearchField("audImportacao", SearchCriteria.igual, R1);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {

		StringBuilder sql = new StringBuilder();
		sql.append("select o from AudVerbaImportacao o WHERE 1=1 ");

		return sql.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

}