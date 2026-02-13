package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoEvento;

public abstract class AbstractProcessoEventoHome<T> extends AbstractHome<ProcessoEvento> {

	private static final long serialVersionUID = 1L;

	public void setProcessoEventoIdProcessoEvento(Integer id) {
		setId(id);
	}

	public Integer getProcessoEventoIdProcessoEvento() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoEvento createInstance() {
		ProcessoEvento processoEvento = new ProcessoEvento();
		return processoEvento;
	}

	@Override
	public String remove(ProcessoEvento obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("processoEventoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		newInstance();
		return action;
	}
}