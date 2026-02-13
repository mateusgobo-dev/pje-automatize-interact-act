package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteVisita;

public abstract class AbstractProcessoParteExpedienteVisitaHome<T> extends AbstractHome<ProcessoParteExpedienteVisita> {

	private static final long serialVersionUID = 1L;

	public void setProcessoParteExpedienteVisitaIdProcessoParteExpedienteVisita(Integer id) {
		setId(id);
	}

	public Integer getProcessoParteExpedienteVisitaIdProcessoParteExpedienteVisita() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoParteExpedienteVisita createInstance() {
		ProcessoParteExpedienteVisita processoParteExpedienteVisita = new ProcessoParteExpedienteVisita();
		return processoParteExpedienteVisita;
	}

	@Override
	public String remove(ProcessoParteExpedienteVisita obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		refreshGrid("processoParteExpedienteVisitaGrid");
		return ret;
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