package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.jus.pje.nucleo.entidades.ProcessoPrioridadeProcesso;

@Name("processoPrioridadeProcessoHome")
@BypassInterceptors
public class ProcessoPrioridadeProcessoHome extends AbstractProcessoPrioridadeProcessoHome<ProcessoPrioridadeProcesso> {

	private static final long serialVersionUID = 1L;

	@Override
	public String remove(ProcessoPrioridadeProcesso processoPrioridadeProcesso) {
		String ret = super.remove(processoPrioridadeProcesso);
		refreshGrid("processoPrioridadeProcessoGrid");
		return ret;
	}

	@Override
	public void newInstance() {
		super.newInstance();
		ProcessoTrfHome.instance().getInstance();
		getInstance().setProcessoTrf(ProcessoTrfHome.instance().getInstance());
	}

	public String gravarPrioridade() {
		getInstance().setProcessoTrf(ProcessoTrfHome.instance().getInstance());
		String persist = persist();
		refreshGrid("prioridadeProcessoIncidenteGrid");
		return persist;
	}

	@Override
	public String persist() {
		if (this.instance.getPrioridadeProcesso() == null) {
			FacesMessages.instance().add(Severity.ERROR, "Selecione uma opção válida");
			return null;
		} else {
			String msg = super.persist();
			refreshGrid("processoPrioridadeProcessoGrid");
			Contexts.removeFromAllContexts("prioridadeProcessoItems");
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Prioridade incluída com sucesso");
			return msg;
		}
	}

	@Override
	public String update() {
		refreshGrid("processoPrioridadeProcessoGrid");
		return super.update();
	}
	
	public void criaProcessoPrioridadeProcessoEntidade() {
		super.persist();
	}
}