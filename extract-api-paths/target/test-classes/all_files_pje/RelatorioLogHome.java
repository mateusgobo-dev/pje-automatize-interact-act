package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.RelatorioLog;

@Name(RelatorioLogHome.NAME)
@BypassInterceptors
public class RelatorioLogHome extends AbstractEtniaHome<RelatorioLog> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "relatorioLogHome";

}