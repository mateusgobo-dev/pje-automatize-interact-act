/* $Id: CargoList.java 13417 2010-10-28 13:49:03Z allan $ */

package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.MotivoEventoRedistribuicao;

@Name(MotivoEventoRedistribuicaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class MotivoEventoRedistribuicaoList extends EntityList<MotivoEventoRedistribuicao> {

	public static final String NAME = "motivoEventoRedistribuicaoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from MotivoEventoRedistribuicao o";
	private static final String DEFAULT_ORDER = "idMotivoEventoRedistribuicao";

	private static final String R1 = "o.motivoRedistribuicao = #{motivoRedistribuicaoHome.instance}";

	@Override
	protected void addSearchFields() {
		addSearchField("idMotivoEventoRedistribuicao", SearchCriteria.contendo, R1);
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
