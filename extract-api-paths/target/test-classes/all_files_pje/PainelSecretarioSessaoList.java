package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Sessao;

@Name(PainelSecretarioSessaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PainelSecretarioSessaoList extends EntityList<Sessao> {

	public static final String NAME = "painelSecretarioSessaoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from Sessao o where o.dataExclusao is null";

	private static final String DEFAULT_ORDER = "idSessao";

	private static final String R1 = "(cast(o.dataSessao as date) = #{agendaSessao.currentDate}) or (o.dataFimSessao is not null and  :el2 between o.dataSessao and o.dataFimSessao))";
	
	private static final String R2 = "o.orgaoJulgadorColegiado = #{authenticator.getOrgaoJulgadorColegiadoAtual()}";

	@Override
	protected void addSearchFields() {
		addSearchField("dataSessao", SearchCriteria.igual, R1);
		addSearchField("procurador", SearchCriteria.igual, R2);
		if (Authenticator.getOrgaoJulgadorColegiadoAtual() != null && Authenticator.getOrgaoJulgadorAtual() != null) {
			addSearchField("dataFechamentoSessao", SearchCriteria.igual, r3());
		}
	}

	public static String r3() {
		StringBuilder sb = new StringBuilder();
		if (Authenticator.getOrgaoJulgadorColegiadoAtual() != null && Authenticator.getOrgaoJulgadorAtual() != null) {
			sb.append("o in (select sco.sessao from SessaoComposicaoOrdem sco ");
			sb.append("		 where sco.orgaoJulgador = #{authenticator.getOrgaoJulgadorAtual()}) ");
		}
		return sb.toString();
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("sala", "o.orgaoJulgadorColegiadoSalaHorario.sala");
		map.put("tipoSessao", "o.tipoSessao");
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