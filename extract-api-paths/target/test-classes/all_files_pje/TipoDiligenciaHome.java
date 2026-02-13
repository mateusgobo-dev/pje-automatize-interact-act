package br.com.infox.cliente.home;

import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.TipoDiligencia;

@Name("tipoDiligenciaHome")
@BypassInterceptors
public class TipoDiligenciaHome extends AbstractTipoDiligenciaHome<TipoDiligencia> {

	private static final long serialVersionUID = 1L;

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
	public String remove(TipoDiligencia obj) {
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

	public static TipoDiligenciaHome instance() {
		return ComponentUtil.getComponent("tipoDiligenciaHome");
	}
}