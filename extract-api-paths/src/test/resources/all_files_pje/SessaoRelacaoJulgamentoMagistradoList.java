package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.Sessao;

@Name(SessaoRelacaoJulgamentoMagistradoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoRelacaoJulgamentoMagistradoList extends EntityList<Sessao> {

	public static final String NAME = "sessaoRelacaoJulgamentoMagistradoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from Sessao o";

	private static final String DEFAULT_ORDER = "idSessao";

	private static final String R1 = "(cast(o.dataSessao as date) = #{agendaSessao.currentDate}) or (o.dataFimSessao is not null and  :el2 between o.dataSessao and o.dataFimSessao))";
	private static final String R2 = "o.orgaoJulgadorColegiado = #{authenticator.getOrgaoJulgadorColegiadoAtual()}";

	@Override
	protected void addSearchFields() {
		addSearchField("dataSessao", SearchCriteria.igual, R1);
		addSearchField("dataExclusao", SearchCriteria.igual, R2);
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