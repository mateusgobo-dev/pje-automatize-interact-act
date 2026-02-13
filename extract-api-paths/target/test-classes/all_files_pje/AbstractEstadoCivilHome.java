package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.EstadoCivil;

public abstract class AbstractEstadoCivilHome<T> extends AbstractHome<EstadoCivil> {

	private static final long serialVersionUID = 1L;

	public void setEstadoCivilIdEstadoCivil(Integer id) {
		setId(id);
	}

	public Integer getEstadoCivilIdEstadoCivil() {
		return (Integer) getId();
	}

	@Override
	protected EstadoCivil createInstance() {
		EstadoCivil estadoCivil = new EstadoCivil();
		return estadoCivil;
	}

	@Override
	public String remove(EstadoCivil obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		refreshGrid("estadoCivilGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		return action;
	}

}