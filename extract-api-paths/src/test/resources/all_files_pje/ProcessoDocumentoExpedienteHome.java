package br.com.infox.cliente.home;

import java.util.Date;

import org.hibernate.AssertionFailure;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;

@Name("processoDocumentoExpedienteHome")
@BypassInterceptors
public class ProcessoDocumentoExpedienteHome extends
		AbstractProcessoDocumentoExpedienteHome<ProcessoDocumentoExpediente> {

	private static final long serialVersionUID = 1L;
	private Boolean impresso = Boolean.FALSE;
	
	public void addProcessoDocumentoExpediente(ProcessoDocumento obj, String gridId) {
		ProcessoDocumentoExpediente pde = new ProcessoDocumentoExpediente();
		pde.setProcessoExpediente(ProcessoExpedienteHome.instance().getInstance());
		pde.setProcessoDocumento(obj);
		pde.setAnexo(true);
		
		getEntityManager().persist(pde);
		getEntityManager().flush();
		
		refreshGrid("processoDocumentoExpedienteGrid");
		refreshGrid("processoDocumentoProcessoExpedienteGrid");
	}

	public void removeProcessoDocumentoExpediente(ProcessoDocumentoExpediente obj, String gridId) {
			getEntityManager().remove(obj);

			try {
				getEntityManager().flush();
				FacesMessages.instance().add(Severity.INFO, "Registro inativado com sucesso.");
			} catch (AssertionFailure e) {
				System.out.println(e.getMessage());
			}

			newInstance();
			refreshGrid("processoDocumentoExpedienteGrid");
			refreshGrid("processoDocumentoProcessoExpedienteGrid");
	}

	public void inserirNaoAnexo(ProcessoExpediente obj, ProcessoDocumento obj2) {
		getInstance().setProcessoDocumento(obj2);
		getInstance().setProcessoExpediente(obj);
		getInstance().setAnexo(Boolean.FALSE);
		// obj.getProcessoDocumentoExpedienteList().add(getInstance());
		persist();
	}

	public static ProcessoDocumentoExpedienteHome instance() {
		return ComponentUtil.getComponent("processoDocumentoExpedienteHome");
	}

	public void marcarDesmarcarImpresso(ProcessoDocumentoExpediente obj) {
		Date data = null;
		setInstance(obj);
		if (!obj.getImpresso()) {
			data = new Date();
		}
		getInstance().setDtImpressao(data);
		persist();
		// getEntityManager().flush();
		refreshGrid("processoExpedienteSetorMaloteGrid");
	}

	public void setImpresso(Boolean impresso) {
		this.impresso = impresso;
	}

	public Boolean getImpresso() {
		return impresso;
	}
	
}