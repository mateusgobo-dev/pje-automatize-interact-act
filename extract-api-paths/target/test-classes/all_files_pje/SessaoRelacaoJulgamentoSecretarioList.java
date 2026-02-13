package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.Sessao;

@Name(SessaoRelacaoJulgamentoSecretarioList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoRelacaoJulgamentoSecretarioList extends EntityList<Sessao> {

	public static final String NAME = "sessaoRelacaoJulgamentoSecretarioList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from Sessao o ";

	private static final String DEFAULT_ORDER = "idSessao";

	private static final String R1 = "cast(o.dataSessao as date) = #{agendaSessao.currentDate}";

	@Override
	protected void addSearchFields() {
		addSearchField("dataSessao", SearchCriteria.igual, R1);
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