package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;

public abstract class AbstractProcessoExpedienteCentralMandadoHome<T> extends
		AbstractHome<ProcessoExpedienteCentralMandado> {

	private static final long serialVersionUID = 1L;

	public void setProcessoExpedienteCentralMandadoIdProcessoExpedienteCentralMandado(Integer id) {
		setId(id);
	}

	public Integer getProcessoExpedienteCentralMandadoIdProcessoExpedienteCentralMandado() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoExpedienteCentralMandado createInstance() {
		ProcessoExpedienteCentralMandado processoExpedienteCentralMandado = new ProcessoExpedienteCentralMandado();

		CentralMandadoHome centralMandadoHome = CentralMandadoHome.instance();
		if (centralMandadoHome != null) {
			processoExpedienteCentralMandado.setCentralMandado(centralMandadoHome.getDefinedInstance());
		}

		ProcessoExpedienteHome processoExpedienteHome = ProcessoExpedienteHome.instance();
		if (processoExpedienteHome != null) {
			processoExpedienteCentralMandado.setProcessoExpediente(processoExpedienteHome.getDefinedInstance());
		}
		return processoExpedienteCentralMandado;
	}

	@Override
	public String remove() {
		// ProcessoExpedienteHome processoExpediente =
		// ProcessoExpedienteHome.instance();
		// if (processoExpediente != null) {
		// processoExpediente.getInstance().getProcessoExpedienteCentralMandadoList().remove(instance);
		// }
		//
		// CentralMandadoHome centralMandado = CentralMandadoHome.instance();
		// if (centralMandado != null) {
		// centralMandado.getInstance().getProcessoExpedienteCentralMandadoList().remove(instance);
		// }
		return super.remove();
	}

	@Override
	public String remove(ProcessoExpedienteCentralMandado obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("processoExpedienteCentralMandadoGrid");
		return ret;
	}

	public String persist(ProcessoExpedienteCentralMandado obj) {
		setInstance(obj);
		String ret = super.persist();
		newInstance();
		refreshGrid("processoExpedienteCentralMandadoGrid");
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