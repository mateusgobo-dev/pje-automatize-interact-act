package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoLoteLog;

public abstract class AbstractProcessoLoteLogHome<T> extends AbstractHome<ProcessoLoteLog> {

	private static final long serialVersionUID = 1L;

	public void setProcessoLoteLogIdProcessoLoteLog(Integer id) {
		setId(id);
	}

	public Integer getProcessoLoteLogIdProcessoLoteLog() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoLoteLog createInstance() {
		ProcessoLoteLog processoLoteLog = new ProcessoLoteLog();
		return processoLoteLog;
	}

	@Override
	public String persist() {
		String action = super.persist();
		newInstance();
		return action;
	}

}