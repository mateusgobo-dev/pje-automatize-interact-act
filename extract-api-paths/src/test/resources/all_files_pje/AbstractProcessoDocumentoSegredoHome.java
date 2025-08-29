package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoSegredo;

public abstract class AbstractProcessoDocumentoSegredoHome<T> extends AbstractHome<ProcessoDocumentoSegredo> {

	private static final long serialVersionUID = 1L;

	public void setProcessoDocumentoSegredoIdProcessoDocumentoSegredo(Integer id) {
		setId(id);
	}

	public Integer getProcessoDocumentoSegredoIdProcessoDocumentoSegredo() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoDocumentoSegredo createInstance() {
		ProcessoDocumentoSegredo processoDocumentoSegredo = new ProcessoDocumentoSegredo();
		return processoDocumentoSegredo;
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