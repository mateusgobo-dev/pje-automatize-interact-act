package br.com.jt.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.jt.entidades.PautaSessao;

@Name(VotacaoAntecipadaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class VotacaoAntecipadaList extends FiltrosPautaVotacaoAntecipadaList<PautaSessao> {

	public static final String NAME = "votacaoAntecipadaList";
	
	private static final long serialVersionUID = 1L;
	
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("processo", "processoTrf.processo.numeroProcesso");
		map.put("orgaoJulgador", "processoTrf.orgaoJulgador.orgaoJulgador");
		map.put("classeJudicial", "processoTrf.classeJudicial.classeJudicial");
		return map;
	}

	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder(FiltrosPautaVotacaoAntecipadaList.DEFAULT_EJBQL);
		sb.append("where o.sessao = #{pautaJulgamentoAction.sessao} ");
		sb.append("and o.processoTrf.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual} ");
		if(Authenticator.getOrgaoJulgadorAtual() != null){
			sb.append("and o.processoTrf.orgaoJulgador != #{orgaoJulgadorAtual} ");
		}
		return sb.toString();
	}

}