package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Etnia;

public abstract class AbstractEtniaHome<T> extends AbstractHome<Etnia> {

	private static final long serialVersionUID = 1L;

	public void setEtniaIdEtnia(Integer id) {
		setId(id);
	}

	public Integer getEtniaIdEtnia() {
		return (Integer) getId();
	}

	@Override
	protected Etnia createInstance() {
		Etnia etnia = new Etnia();
		return etnia;
	}

	@Override
	public String remove(Etnia obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		refreshGrid("etniaGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		return action;
	}

}