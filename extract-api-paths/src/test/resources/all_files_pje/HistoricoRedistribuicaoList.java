package br.com.infox.pje.list;

import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoTrfRedistribuicao;

@Name(HistoricoRedistribuicaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class HistoricoRedistribuicaoList extends EntityList<ProcessoTrfRedistribuicao> {

	public static final String NAME = "historicoRedistribuicaoList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.dataRedistribuicao";
	
	private static final String DEFAULT_EJBQL = "select o from ProcessoTrfRedistribuicao o";
	
	private static final String R1 = "o.processoTrf.idProcessoTrf = #{processoTrfHome.instance.idProcessoTrf eq 0 ? listProcessoCompletoBetaAction.processoSelecionado.idProcessoTrf : processoTrfHome.instance.idProcessoTrf}";

	@Override
	protected void addSearchFields() {
		addSearchField("processoTrf", SearchCriteria.igual, R1);
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
