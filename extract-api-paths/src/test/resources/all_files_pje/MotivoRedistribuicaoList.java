/* $Id: CargoList.java 13417 2010-10-28 13:49:03Z allan $ */

package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.MotivoRedistribuicao;

@Name(MotivoRedistribuicaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class MotivoRedistribuicaoList extends EntityList<MotivoRedistribuicao> {

	public static final String NAME = "motivoRedistribuicaoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from MotivoRedistribuicao o";
	private static final String DEFAULT_ORDER = "idMotivoRedistribuicao";

	@Override
	protected void addSearchFields() {
		addSearchField("motivoRedistribuicao", SearchCriteria.contendo);
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
