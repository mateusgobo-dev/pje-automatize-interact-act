package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoEvento;

@Name(MovimentacoesProcessoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class MovimentacoesProcessoList extends EntityList<ProcessoEvento> {

	public static final String NAME = "movimentacoesProcessoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from ProcessoEvento o where "
			+ "o.ativo = true and exists (select ep from Evento ep where "
			+ "ep = o.evento and (ep.segredoJustica = false or ep.segredoJustica is null))";
	private static final String DEFAULT_ORDER = "o.dataAtualizacao desc";

	private static final String R1 = "o.processo = #{processoTrfHome.instance.processo} ";

	@Override
	protected void addSearchFields() {
		addSearchField("processo", SearchCriteria.igual, R1);
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
