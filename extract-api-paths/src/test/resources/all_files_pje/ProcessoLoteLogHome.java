package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ProcessoLoteLog;

@Name("processoLoteLogHome")
@BypassInterceptors
public class ProcessoLoteLogHome extends AbstractProcessoLoteLogHome<ProcessoLoteLog> {

	private static final long serialVersionUID = 1L;

	public static ProcessoLoteLogHome instance() {
		return ComponentUtil.getComponent("processoLoteLogHome");
	}

}