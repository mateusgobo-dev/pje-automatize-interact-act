package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Peticao;

public abstract class AbstractPeticaoHome<T> extends AbstractHome<Peticao> {

	private static final long serialVersionUID = 1L;

	public void setPeticaoIdPeticao(Integer id) {
		setId(id);
	}

	public Integer getPeticaoIdPeticao() {
		return (Integer) getId();
	}

	@Override
	protected Peticao createInstance() {
		Peticao peticao = new Peticao();
		return peticao;
	}

	@Override
	public String remove(Peticao obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("peticaoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		// newInstance();
		return action;
	}

}