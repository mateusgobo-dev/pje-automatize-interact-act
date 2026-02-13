package br.com.infox.cliente.home;

import java.util.Date;
import java.util.List;

import org.hibernate.AssertionFailure;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.jus.pje.nucleo.entidades.Lote;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoLote;
import br.jus.pje.nucleo.entidades.ProcessoLoteLog;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name("processoLoteHome")
@BypassInterceptors
public class ProcessoLoteHome extends AbstractProcessoLoteHome<ProcessoLote> {

	private static final long serialVersionUID = 1L;

	private String numeroProcesso;

	public void addProcessoLote(ProcessoTrf obj, String gridId) {
		if (getInstance() != null) {
			getInstance().setProcessoTrf(obj);
			getInstance().setDtInclusaoProcesso(new Date());

			Context session = Contexts.getSessionContext();
			Pessoa pessoa = (Pessoa) session.get("usuarioLogado");
			getInstance().setPessoaInclusao(pessoa);

			ProcessoLote processoLote = getInstance();

			persist();

			LoteHome.instance().getInstance().getProcessoLoteList().add(processoLote);

			refreshGrid("processoLoteGrid");
			refreshGrid("loteProcessoTrfGrid");
		}
	}

	private ProcessoLoteLogHome getProcessoLoteLogHome() {
		return getComponent("processoLoteLogHome");
	}

	public void removeProcessoLote(ProcessoLote obj, String gridId) {
		if (getInstance() != null) {

			Context session = Contexts.getSessionContext();
			Pessoa pessoa = (Pessoa) session.get("usuarioLogado");

			ProcessoLoteLog loteLog = ProcessoLoteLogHome.instance().getInstance();
			loteLog.setProcessoTrf(obj.getProcessoTrf());
			loteLog.setPessoaInclusao(obj.getPessoaInclusao());
			loteLog.setPessoaExclusao(pessoa);
			loteLog.setLote(obj.getLote());
			loteLog.setDtInclusaoProcesso(obj.getDtInclusaoProcesso());
			loteLog.setDtExclusaoProcesso(new Date());

			Lote lote = LoteHome.instance().getInstance();

			List<ProcessoLote> processoLoteList = lote.getProcessoLoteList();
			processoLoteList.remove(obj);

			getEntityManager().remove(obj);
			getEntityManager().persist(loteLog);

			try {
				getEntityManager().flush();
				FacesMessages.instance().add(Severity.INFO, "Processo excluido do lote");
			} catch (AssertionFailure e) {
				System.out.println(e.getMessage());
			}

			getProcessoLoteLogHome().newInstance();
			newInstance();
			refreshGrid("processoLoteGrid");
			refreshGrid("loteProcessoTrfGrid");
		}
	}

	public String getPartesProcesso(ProcessoLote pl) {
		StringBuilder sb = new StringBuilder();
		for (ProcessoParte pp : pl.getProcessoTrf().getProcessoParteList()) {
			if (pp.getInParticipacao() == ProcessoParteParticipacaoEnum.A && pp.getPessoa().getNome() != null) {
				sb.append(pp.getPessoa().getNome());
				sb.append(" e outros... X ");
				break;
			}
		}
		for (ProcessoParte pp : pl.getProcessoTrf().getProcessoParteList()) {
			if (pp.getInParticipacao() == ProcessoParteParticipacaoEnum.P && pp.getPessoa().getNome() != null) {
				sb.append(pp.getPessoa().getNome());
				sb.append(" e outros... X ");
				break;
			}
		}
		return sb.toString();
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

}
