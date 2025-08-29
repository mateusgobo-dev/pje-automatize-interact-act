package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.util.ComponentUtil;

@Name("tipoParteTrfHome")
@BypassInterceptors
public class TipoParteTrfHome extends AbstractPessoaMagistradoHome<TipoParteTrfHome> {

	private static final long serialVersionUID = 1L;

	public static TipoParteTrfHome instance() {
		return ComponentUtil.getComponent("tipoParteTrfHome");
	}
}