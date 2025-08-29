package br.com.infox.cliente.home;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoPrioridadeProcesso;

public abstract class AbstractProcessoPrioridadeProcessoHome<T> extends AbstractHome<ProcessoPrioridadeProcesso> {

	private static final long serialVersionUID = 1L;

	public void setProcessoPrioridadeProcessoIdProcessoPrioridadeProcesso(Integer id) {
		setId(id);
	}

	public Integer getProcessoPrioridadeProcessoIdProcessoPrioridadeProcesso() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoPrioridadeProcesso createInstance() {
		ProcessoPrioridadeProcesso processoPrioridadeProcesso = new ProcessoPrioridadeProcesso();
		ProcessoTrfHome processoTrfHome = (ProcessoTrfHome) Component.getInstance("processoTrfHome", false);
		if (processoTrfHome != null) {
			processoPrioridadeProcesso.setProcessoTrf(processoTrfHome.getDefinedInstance());
		}
		PrioridadeProcessoHome prioridadeProcessoHome = (PrioridadeProcessoHome) Component.getInstance(
				"prioridadeProcessoHome", false);
		if (prioridadeProcessoHome != null) {
			processoPrioridadeProcesso.setPrioridadeProcesso(prioridadeProcessoHome.getDefinedInstance());
		}
		return processoPrioridadeProcesso;
	}

	@Override
	public String remove() {
		ProcessoTrfHome processoTrf = (ProcessoTrfHome) Component.getInstance("processoTrfHome", false);
		if (processoTrf != null) {
			processoTrf.getInstance().getProcessoPrioridadeProcessoList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(ProcessoPrioridadeProcesso obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("processoPrioridadeProcessoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		newInstance();
		return action;
	}
}