package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoVoto;

@Name(TipoVotoHome.NAME)
@BypassInterceptors
public class TipoVotoHome extends AbstractHome<TipoVoto> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "tipoVotoHome";

}