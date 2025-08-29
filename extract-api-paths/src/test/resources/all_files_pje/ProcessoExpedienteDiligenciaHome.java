package br.com.infox.cliente.home;

import org.hibernate.AssertionFailure;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.jus.pje.nucleo.entidades.ProcessoExpedienteDiligencia;
import br.jus.pje.nucleo.entidades.TipoDiligencia;

@Name("processoExpedienteDiligenciaHome")
@BypassInterceptors
public class ProcessoExpedienteDiligenciaHome extends
		AbstractProcessoExpedienteDiligenciaHome<ProcessoExpedienteDiligencia> {

	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(ProcessoExpedienteDiligenciaHome.class);

	public void addProcessoExpedienteDiligencia(TipoDiligencia obj, String gridId) {
		ProcessoExpedienteDiligencia ped = new ProcessoExpedienteDiligencia();
		ped.setTipoDiligencia(obj);
		ped.setProcessoExpediente(ProcessoExpedienteHome.instance().getInstance());
		getEntityManager().persist(ped);
		getEntityManager().flush();
		ProcessoExpedienteHome.instance().getInstance().getProcessoExpedienteDiligenciaList().add(ped);
		refreshGrid("processoExpedienteDiligenciaGrid");
		refreshGrid("tipoDiligenciaGrid");
	}

	public void removeProcessoExpedienteDiligencia(ProcessoExpedienteDiligencia obj, String gridId) {
		if (getInstance() != null) {
			if (ProcessoExpedienteHome.instance().getInstance().getProcessoExpedienteDiligenciaList().contains(obj)) {
				ProcessoExpedienteHome.instance().getInstance().getProcessoExpedienteDiligenciaList().remove(obj);
			}

			getEntityManager().remove(obj);

			try {
				getEntityManager().flush();
				FacesMessages.instance().add(Severity.INFO, "Registro inativado com sucesso.");
			} catch (AssertionFailure e) {
				log.error(e.getMessage());
			}

			newInstance();
			refreshGrid("processoExpedienteDiligenciaGrid");
			refreshGrid("tipoDiligenciaGrid");
		}
	}

}