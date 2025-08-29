package br.com.infox.cliente.home;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.BaseCalculoIr;

@Name(BaseCalculoIrHome.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class BaseCalculoIrHome extends AbstractHome<BaseCalculoIr> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "baseCalculoIrHome";

	public static BaseCalculoIrHome instance() {
		return (BaseCalculoIrHome) Component.getInstance(NAME);
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (instance.getVlBaseCalculoIrMaximo() != null
				&& (instance.getVlBaseCalculoIrMaximo() < instance.getVlBaseCalculoIrMinimo())) {
			FacesMessages.instance().add(Severity.ERROR,
					"O valor máximo da base de cálculo, não pode ser menor que o valor mínimo");
			return false;
		}
		return super.beforePersistOrUpdate();
	}

}