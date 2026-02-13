package br.com.infox.cliente.home;

import java.util.List;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteDiligencia;

public abstract class AbstractProcessoExpedienteDiligenciaHome<T> extends AbstractHome<ProcessoExpedienteDiligencia> {

	private static final long serialVersionUID = 1L;

	public void setProcessoExpedienteDiligenciaIdProcessoExpedienteDiligencia(Integer id) {
		setId(id);
	}

	public Integer getProcessoExpedienteDiligenciaIdProcessoExpedienteDiligencia() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoExpedienteDiligencia createInstance() {
		ProcessoExpedienteDiligencia processoExpedienteDiligencia = new ProcessoExpedienteDiligencia();
		TipoDiligenciaHome tipoDiligenciaHome = TipoDiligenciaHome.instance();
		if (tipoDiligenciaHome != null) {
			processoExpedienteDiligencia.setTipoDiligencia(tipoDiligenciaHome.getDefinedInstance());
		}
		ProcessoExpedienteHome processoExpedienteHome = ProcessoExpedienteHome.instance();
		if (processoExpedienteHome != null) {
			processoExpedienteDiligencia.setProcessoExpediente(processoExpedienteHome.getDefinedInstance());
		}
		return processoExpedienteDiligencia;
	}

	@Override
	public String remove() {
		ProcessoExpedienteHome processoExpediente = ProcessoExpedienteHome.instance();
		if (processoExpediente != null) {
			processoExpediente.getInstance().getProcessoExpedienteDiligenciaList().remove(instance);
		}
		TipoDiligenciaHome tipoDiligencia = TipoDiligenciaHome.instance();
		if (tipoDiligencia != null) {
			List<ProcessoExpedienteDiligencia> processoExpedienteDiligenciaList = tipoDiligencia.getInstance()
					.getProcessoExpedienteDiligenciaList();
			processoExpedienteDiligenciaList.remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(ProcessoExpedienteDiligencia obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("processoExpedienteDiligenciaGrid");
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