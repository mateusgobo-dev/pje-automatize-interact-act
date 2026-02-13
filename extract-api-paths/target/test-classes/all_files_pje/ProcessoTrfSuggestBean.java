package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.home.SessaoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;

@Name("processoTrfSuggest")
@Scope(ScopeType.CONVERSATION)
public class ProcessoTrfSuggestBean extends AbstractSuggestBean<ProcessoTrf> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		OrgaoJulgador orgao = Authenticator.getOrgaoJulgadorAtual();

		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoTrf o ");
		sb.append("where lower(o.processo.numeroProcesso) like lower(concat ('%', :");
		sb.append(INPUT_PARAMETER);
		sb.append(", '%')) ");

		if (ParametroUtil.instance().isPrimeiroGrau()) {
			if (orgao != null) {
				sb.append(" and o.orgaoJulgador.idOrgaoJulgador = " + orgao.getIdOrgaoJulgador());
			}
		} else {
			if (ParametroUtil.instance().getEventoConclusao() != null) {
				sb.append("and o.processo in (select c.processo from ProcessoEvento c where c.evento.eventoSuperior.idEvento = "
						+ ParametroUtil.instance().getEventoConclusao().getIdEvento() + " or c.evento.idEvento = "+ParametroUtil.instance().getEventoConclusao().getIdEvento()+") ");
			}

			if (orgao == null) {
				if (Authenticator.isPapelPermissaoSecretarioSessao() && sessaoInstance().getDataAberturaSessao() != null) {
					sb.append("and o.orgaoJulgador in ");
					sb.append("    (select a.orgaoJulgador ");
					sb.append("     from SessaoComposicaoOrdem a where a.sessao.idSessao = "
							+ sessaoInstance().getIdSessao() + ") ");
				}
			} else {
				sb.append("and (o.orgaoJulgador in ");
				sb.append("     (select p.orgaoJulgador "
						+ "      from SessaoComposicaoOrdem p " + "      inner join p.sessao s "
						+ "      where p.orgaoJulgadorRevisor.idOrgaoJulgador = " + Authenticator.getOrgaoJulgadorAtual().getIdOrgaoJulgador() 
						+ "      and s.idSessao = " + sessaoInstance().getIdSessao() + ") "
						+ "      or o.orgaoJulgador.idOrgaoJulgador = " + Authenticator.getOrgaoJulgadorAtual().getIdOrgaoJulgador() + ")");
			}
			sb.append(" and o not in ("
					+ "					select b.processoTrf from SessaoPautaProcessoTrf b "
					+ "					where	b.julgamentoFinalizado is false and b.sessao.dataRealizacaoSessao is null and b.sessao.dataFechamentoSessao is null "
					+ "							and b.dataExclusaoProcessoTrf is null"
					+ "				 ) ");

			sb.append(" and (o.selecionadoPauta = true or o.selecionadoJulgamento = true) ");
		}

		sb.append("order by 1");
		return sb.toString();
	}
	
	private Sessao sessaoInstance() {
		return SessaoHome.instance().getInstance();
	}

	public static ProcessoTrfSuggestBean instance() {
		return ComponentUtil.getComponent("processoTrfSuggest");
	}

}
