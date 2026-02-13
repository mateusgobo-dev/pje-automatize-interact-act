package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoCertidao;

public abstract class AbstractTipoCertidaoHome<T> extends AbstractHome<TipoCertidao> {

	private static final long serialVersionUID = 1L;

	public void setTipoCertidaoIdTipoCertidao(Integer id) {
		setId(id);
	}

	public Integer getTipoCertidaoIdTipoCertidao() {
		return (Integer) getId();
	}
}