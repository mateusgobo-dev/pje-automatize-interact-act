package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;

public abstract class AbstractProcessoParteExpedienteHome<T> extends AbstractHome<ProcessoParteExpediente> {

	private static final long serialVersionUID = 1L;

	public void setProcessoParteExpedienteIdProcessoParteExpediente(Integer id) {
		setId(id);
	}

	public Integer getProcessoParteExpedienteIdProcessoParteExpediente() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoParteExpediente createInstance() {
		ProcessoParteExpediente processoParteExpediente = new ProcessoParteExpediente();
		ProcessoParteHome processoParteHome = ProcessoParteHome.instance();
		if (processoParteHome != null && processoParteHome.getDefinedInstance() != null) {
			processoParteExpediente.setPessoaParte(processoParteHome.getDefinedInstance().getPessoa());
		}
		ProcessoExpedienteHome processoExpedienteHome = ProcessoExpedienteHome.instance();
		if (processoExpedienteHome != null) {
			processoParteExpediente.setProcessoExpediente(processoExpedienteHome.getDefinedInstance());
		}
		return processoParteExpediente;
	}

	@Override
	public String remove() {
		ProcessoExpedienteHome processoExpediente = ProcessoExpedienteHome.instance();
		if (processoExpediente != null) {
			processoExpediente.getInstance().getProcessoParteExpedienteList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(ProcessoParteExpediente obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		return ret;
	}

	public String persist(ProcessoParteExpediente obj, boolean limparEntidade) {
		setInstance(obj);
		String ret = super.persist();
		if (limparEntidade) {
			newInstance();
		}
		return ret;
	}

	public String persist(ProcessoParteExpediente obj) {
		return persist(obj, true);
	}

	@Override
	public String persist() {
		String action = super.persist();
		newInstance();
		return action;
	}

}