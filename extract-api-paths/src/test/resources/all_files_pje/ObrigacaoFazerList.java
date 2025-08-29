package br.jus.csjt.pje.view.action.component.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.jt.entidades.ObrigacaoFazer;

@Name(ObrigacaoFazerList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ObrigacaoFazerList extends EntityList<ObrigacaoFazer> {

	public static final String NAME = "obrigacaoFazerList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from ObrigacaoFazer o where o.processoTrf = #{processoTrfHome.instance}";
	private static final String DEFAULT_ORDER = "idObrigacaoFazer";

	@Override
	protected void addSearchFields() {

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