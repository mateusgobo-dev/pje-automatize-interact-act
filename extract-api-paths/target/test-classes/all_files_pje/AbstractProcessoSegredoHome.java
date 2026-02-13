package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoSegredo;

public abstract class AbstractProcessoSegredoHome<T> extends AbstractHome<ProcessoSegredo> {

	private static final long serialVersionUID = 1L;

	public void setProcessoSegredoIdProcessoSegredo(Integer id) {
		setId(id);
	}

	public Integer getProcessoSegredoIdProcessoSegredo() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoSegredo createInstance() {
		ProcessoSegredo processoSegredo = new ProcessoSegredo();
		return processoSegredo;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (action != null) {
			newInstance();
		}
		return action;
	}
}