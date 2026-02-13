package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoVisibilidadeSegredo;

public abstract class AbstractProcessoVisibilidadeSegredoHome<T> extends AbstractHome<ProcessoVisibilidadeSegredo> {

	/**
	 * AbstractProcessoVisibilidadeSegredoHome by Wilson
	 */
	private static final long serialVersionUID = 1L;

	public void setProcessoVisibilidadeSegredoIdProcessoVisibilidadeSegredo(Integer id) {
		setId(id);
	}

	public Integer getProcessoVisibilidadeSegredoIdProcessoVisibilidadeSegredo() {
		return (Integer) getId();
	}

	public String persist(ProcessoVisibilidadeSegredo obj) {
		setInstance(obj);
		String ret = super.persist();
		newInstance();
		return ret;
	}

	@Override
	public String remove(ProcessoVisibilidadeSegredo obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		return ret;
	}

}