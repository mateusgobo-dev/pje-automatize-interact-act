package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;

public abstract class AbstractPrioridadeProcessoHome<T> extends AbstractHome<PrioridadeProcesso> {

	private static final long serialVersionUID = 1L;

	public void setPrioridadeProcessoIdPrioridadeProcesso(Integer id) {
		setId(id);
	}

	public Integer getPrioridadeProcessoIdPrioridadeProcesso() {
		return (Integer) getId();
	}

	@Override
	protected PrioridadeProcesso createInstance() {
		PrioridadeProcesso prioridadeProcesso = new PrioridadeProcesso();
		return prioridadeProcesso;
	}

	@Override
	public String remove(PrioridadeProcesso obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("prioridadeProcessoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		// newInstance();
		return action;
	}

}