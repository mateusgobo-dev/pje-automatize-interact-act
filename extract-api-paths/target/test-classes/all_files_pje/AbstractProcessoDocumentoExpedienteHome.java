package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;

public abstract class AbstractProcessoDocumentoExpedienteHome<T> extends AbstractHome<ProcessoDocumentoExpediente> {

	private static final long serialVersionUID = 1L;

	public void setProcessoDocumentoExpedienteIdProcessoDocumentoExpediente(Integer id) {
		setId(id);
	}

	public Integer getProcessoDocumentoExpedienteIdProcessoDocumentoExpediente() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoDocumentoExpediente createInstance() {
		ProcessoDocumentoExpediente processoDocumentoExpediente = new ProcessoDocumentoExpediente();
		ProcessoDocumentoHome processoDocumentoHome = ProcessoDocumentoHome.instance();
		if (processoDocumentoHome != null) {
			processoDocumentoExpediente.setProcessoDocumento(processoDocumentoHome.getDefinedInstance());
		}
		ProcessoExpedienteHome processoExpedienteHome = ProcessoExpedienteHome.instance();
		if (processoExpedienteHome != null) {
			processoDocumentoExpediente.setProcessoExpediente(processoExpedienteHome.getDefinedInstance());
		}
		return processoDocumentoExpediente;
	}

	@Override
	public String remove() {
		ProcessoExpedienteHome processoExpediente = ProcessoExpedienteHome.instance();
		if (processoExpediente != null) {
			processoExpediente.getInstance().getProcessoDocumentoExpedienteList().remove(instance);
		}
		// ProcessoDocumentoHome processoDocumento =
		// ProcessoDocumentoHome.instance();
		// if (processoDocumento != null) {
		// List<ProcessoDocumentoExpediente> processoDocumentoExpedienteList =
		// processoDocumento.getInstance().getProcessoDocumentoExpedienteList();
		// processoDocumentoExpedienteList.remove(instance);
		// }
		return super.remove();
	}

	@Override
	public String remove(ProcessoDocumentoExpediente obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("processoDocumentoExpedienteGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		// if (action != null) {
		// newInstance();
		// }
		newInstance();
		return action;
	}

}