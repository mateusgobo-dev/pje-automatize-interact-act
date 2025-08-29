package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Escolaridade;

public abstract class AbstractEscolaridadeHome<T> extends AbstractHome<Escolaridade> {

	private static final long serialVersionUID = 1L;

	public void setEscolaridadeIdEscolaridade(Integer id) {
		setId(id);
	}

	public Integer getEscolaridadeIdEscolaridade() {
		return (Integer) getId();
	}

	@Override
	protected Escolaridade createInstance() {
		Escolaridade escolaridade = new Escolaridade();
		return escolaridade;
	}

	@Override
	public String remove(Escolaridade obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		refreshGrid("escolaridadeGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		// newInstance();
		return action;
	}

}