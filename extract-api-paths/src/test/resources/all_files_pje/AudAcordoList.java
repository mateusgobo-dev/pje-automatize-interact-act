package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.jt.entidades.AudParcelaImportacao;

@Name(AudAcordoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AudAcordoList extends EntityList<AudParcelaImportacao> {

	private static final long serialVersionUID = 1401056105531721717L;
	public static final String NAME = "audAcordoList";
	private static final String DEFAULT_ORDER = "o.idAudParcelaImportacao";
	private static final String R1 = "o.audImportacao = #{audAcordoList.entity.audImportacao} ";

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
		sql.append("select o from AudParcelaImportacao o WHERE 1=1 ");

		return sql.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

}