package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.Sessao;

@Name(SessaoRelacaoJulgamentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoRelacaoJulgamentoList extends EntityList<Sessao> {

	public static final String NAME = "sessaoRelacaoJulgamentoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from Sessao o " + "where o.dataFechamentoSessao = null";

	private static final String DEFAULT_ORDER = "idSessao";

	private static final String R1 = "cast(o.dataSessao as date) = #{agendaSessao.currentDate}";

	@Override
	protected void addSearchFields() {
		addSearchField("dataSessao", SearchCriteria.igual, R1);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("sala", "o.orgaoJulgadorColegiadoSalaHorario.sala");
		map.put("tipoSessao", "o.tipoSessao");
		map.put("ativo", "o.ativo");
		return map;
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