package br.com.infox.pje.list;

import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.MotivoIsencaoGuia;

@Name(MotivoIsencaoGuiaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class MotivoIsencaoGuiaList extends EntityList<MotivoIsencaoGuia> {
	public static final String NAME = "motivoIsencaoGuiaList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_EJBQL = "select o from MotivoIsencaoGuia o ";
	private static final String DEFAULT_ORDER = "o.dsMotivoIsencao";

	@Override
	protected void addSearchFields() {
		addSearchField("dsMotivoIsencao", SearchCriteria.contendo);
		addSearchField("ativo", SearchCriteria.igual);
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