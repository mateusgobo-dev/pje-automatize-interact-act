package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.AreaOficial;

@Name(AreaOficialHome.NAME)
@BypassInterceptors
public class AreaOficialHome extends AbstractHome<AreaOficial> {

	public static final String NAME = "areaOficialHome";
	private static final long serialVersionUID = -1405266269656465042L;

	public static AreaOficialHome instance() {
		return ComponentUtil.getComponent(AreaOficialHome.NAME);
	}
}
