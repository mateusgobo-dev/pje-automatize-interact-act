package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TransitoEmJulgado;

public abstract class AbstractTransitoEmJulgadoHome<T> extends AbstractHome<TransitoEmJulgado> {
	private static final long serialVersionUID = 1L;

	public void setTransitoEmJulgadoId(Integer id) {
		setId(id);
	}

	public Integer getTransitoEmJulgadoId() {
		return (Integer) super.getId();
	}
}
