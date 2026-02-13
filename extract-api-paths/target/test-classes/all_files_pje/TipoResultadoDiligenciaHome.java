package br.com.infox.cliente.home;

import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.TipoResultadoDiligencia;

@Name("tipoResultadoDiligenciaHome")
@BypassInterceptors
public class TipoResultadoDiligenciaHome extends AbstractTipoResultadoDiligenciaHome<TipoResultadoDiligencia> {

	private static final long serialVersionUID = 1L;

	@Override
	public String persist() {
		String ret = null;
		try {
			getEntityManager().merge(instance);
			EntityUtil.flush();
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Registro inserido com Sucesso.");
		} catch (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado!");
		}
		return ret;
	}

	@Override
	public String remove(TipoResultadoDiligencia obj) {
		obj.setAtivo(Boolean.FALSE);
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

	public static TipoResultadoDiligenciaHome instance() {
		return ComponentUtil.getComponent("tipoResultadoDiligenciaHome");
	}
}
