package br.com.infox.cliente.home;

import static org.jboss.seam.faces.FacesMessages.instance;

import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.StatusMessage;

import br.jus.pje.nucleo.entidades.TipoRedistribuicao;

@Name("tipoRedistribuicaoHome")
@BypassInterceptors
public class TipoRedistribuicaoHome extends AbstractTipoRedistribuicaoHome<TipoRedistribuicao> {

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
	public String update() {
		String ret = null;

		try {
			getEntityManager().merge(getInstance());
			getEntityManager().flush();
			ret = getUpdatedMessage().getValue().toString();
			instance().add(StatusMessage.Severity.INFO, "Registro alterado com sucesso");
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof ConstraintViolationException) {
				instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado!");
			}
		}
		return ret;
	}

}