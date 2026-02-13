package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;

@Name(VerificarPrevencaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class VerificarPrevencaoList extends EntityList<ProcessoTrfConexao> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "verificarPrevencaoList";

	private static final String DEFAULT_ORDER = "idProcessoTrfConexao";

	@Override
	protected void addSearchFields() {
		addSearchField("tipoConexao", SearchCriteria.igual);
		addSearchField("processoTrf.numeroProcesso", SearchCriteria.contendo);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {

		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoTrfConexao o ");
		sb.append("where o.processoTrf.processo.idProcesso= #{processoHome.instance.idProcesso}");
		return sb.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

}