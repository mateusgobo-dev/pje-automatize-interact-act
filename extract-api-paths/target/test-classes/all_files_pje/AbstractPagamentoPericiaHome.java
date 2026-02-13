package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PagamentoPericia;

public abstract class AbstractPagamentoPericiaHome<T> extends AbstractHome<PagamentoPericia> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void setPagamentoPericiaIdPagamentoPericia(Integer id) {
		setId(id);
	}

	public Integer getPagamentoPericiaIdPagamentoPericia() {
		return (Integer) getId();
	}

}