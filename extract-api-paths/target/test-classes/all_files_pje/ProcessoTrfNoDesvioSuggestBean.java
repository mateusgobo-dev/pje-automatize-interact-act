package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.home.SessaoHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;

/*
 * PJE-JT: Ricardo Scholz : PJE-785 - 2011-11-02 Alteracoes feitas pela JT.
 * 
 * Regra de negócio discutida com Marcelo Pinto e Guilherme Bispo. Visando
 * diminuir os riscos de efeitos colaterais, uma vez que a classe 
 * ProcessoTrfSuggestBean é usada por diversos outros suggests, esta classe foi 
 * replicada a partir da classe ProcessoTrfSuggestBean, removendo do EJBQL as 
 * restrições abaixo relacionadas, uma vez que não mapeiam corretamente as 
 * regras de negócio da criação de nó de desvio da JT. Restrições excluídas:
 * 
 *     sb.append("and o.orgaoJulgador.instancia = '2' ");
 *     sb.append("and o.processo in (select c.processo from 
 *     		ProcessoEvento c where c.evento.eventoSuperior.idEvento = "+
 *         	ParametroUtil.instance().getEventoConclusao().getIdEvento()+") ");
 *     sb.append("and (o.selecionadoPauta = true or o.selecionadoJulgamento = true) ");
 * 
 * Substituição das ELs "#{sessaoHome.instance}" e "#{orgaoJulgadorAtual}"
 * pelas chamadas a "sessaoInstance().getIdSessao()" e 
 * "Authenticator.getOrgaoJulgadorAtual().getIdOrgaoJulgador()",
 * respectivamente, uma vez que as ELs estavam causando erro na execução.
 * 
 * Foi realizado refactoring na classe SolicitacaoNoDesvioHome, que passou a
 * usar esta classe em substituição à classe ProcessoTrfSuggestBean.
 */
@Name("processoTrfNoDesvioSuggest")
@BypassInterceptors
public class ProcessoTrfNoDesvioSuggestBean extends AbstractSuggestBean<ProcessoTrf> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoTrf o ");
		sb.append("where lower(o.processo.numeroProcesso) like lower(concat ('%', :");
		sb.append(INPUT_PARAMETER);
		sb.append(", '%')) ");

		sb.append("and o not in (select b.processoTrf from SessaoPautaProcessoTrf b where b.sessao.idSessao = "
				+ sessaoInstance().getIdSessao()
				+ " and b.sessao.dataFechamentoSessao is null and b.dataExclusaoProcessoTrf is null) ");

		if (Authenticator.getOrgaoJulgadorAtual() == null) {
			if (Identity.instance().hasRole("Servidor") && Authenticator.isPapelPermissaoSecretarioSessao()) {
				sb.append("and o.orgaoJulgador in ");
				sb.append("    (select a.orgaoJulgadorColegiadoOrgaoJulgador.orgaoJulgador ");
				sb.append("     from SessaoComposicaoOrdem a where a.sessao.idSessao = "
						+ sessaoInstance().getIdSessao() + ") ");
			}
		} else {
			sb.append("and (o.orgaoJulgador in ");
			sb.append("     (select p.orgaoJulgadorColegiadoOrgaoJulgador.orgaoJulgador from SessaoComposicaoOrdem p ");
			sb.append("       where p.orgaoJulgadorColegiadoRevisor.orgaoJulgador.idOrgaoJulgador ="
					+ Authenticator.getOrgaoJulgadorAtual().getIdOrgaoJulgador());
			sb.append("		  and p.sessao.idSessao = " + sessaoInstance().getIdSessao() + ") ");
			sb.append("     or o.orgaoJulgador.idOrgaoJulgador = "
					+ Authenticator.getOrgaoJulgadorAtual().getIdOrgaoJulgador() + ")");
		}

		sb.append("order by o.numeroOrgaoJustica asc, o.numeroOrigem asc, o.numeroSequencia asc");

		return sb.toString();
	}

	private Sessao sessaoInstance() {
		return SessaoHome.instance().getInstance();
	}

	public static ProcessoTrfNoDesvioSuggestBean instance() {
		return ComponentUtil.getComponent("processoTrfNoDesvioSuggest");
	}
}