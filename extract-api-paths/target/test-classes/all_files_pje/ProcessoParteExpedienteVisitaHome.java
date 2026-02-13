package br.com.infox.cliente.home;

import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteVisita;

@Name("processoParteExpedienteVisitaHome")
@BypassInterceptors
public class ProcessoParteExpedienteVisitaHome extends
		AbstractProcessoParteExpedienteVisitaHome<ProcessoParteExpedienteVisita> {

	private static final long serialVersionUID = 1L;

	@Override
	public void newInstance() {
		super.newInstance();
	}

	@Override
	public String persist() {
		String ret = null;
		try {
			ret = super.persist();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	@Override
	public String remove(ProcessoParteExpedienteVisita obj) {
		return super.remove(obj);
	}

	@Override
	public String update() {
		String ret = null;
		try {
			getEntityManager().merge(getInstance());
			getEntityManager().flush();
			ret = getUpdatedMessage().getValue().toString();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro alterado com sucesso");
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof ConstraintViolationException) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado!");
			}
		}
		return ret;
	}

	public static ProcessoParteExpedienteVisitaHome instance() {
		return ComponentUtil.getComponent("processoParteExpedienteVisitaHome");
	}

}
