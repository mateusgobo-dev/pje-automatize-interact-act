package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;

public abstract class AbstractProcessoExpedienteHome<T> extends AbstractHome<ProcessoExpediente> {

	private static final long serialVersionUID = 1L;

	public void setProcessoExpedienteIdProcessoExpediente(Integer id) {
		setId(id);
	}

	public Integer getProcessoExpedienteIdProcessoExpediente() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoExpediente createInstance() {
		ProcessoExpediente processoExpediente = new ProcessoExpediente();
		return processoExpediente;
	}

	@Override
	public String remove(ProcessoExpediente obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		refreshGrid("processoExpedienteGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		return action;
	}
}
