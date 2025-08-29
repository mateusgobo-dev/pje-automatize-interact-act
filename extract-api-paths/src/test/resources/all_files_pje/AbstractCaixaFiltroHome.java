package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.CaixaFiltro;

public abstract class AbstractCaixaFiltroHome<T> extends AbstractHome<CaixaFiltro> {

	private static final long serialVersionUID = 1L;

	@Override
	protected CaixaFiltro createInstance() {
		CaixaFiltro caixaFiltro = new CaixaFiltro();
		return caixaFiltro;
	}

}