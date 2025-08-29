package br.com.infox.cliente.home;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;

public abstract class AbstractProcessoAssuntoHome<T> extends AbstractHome<ProcessoAssunto> {

	private static final long serialVersionUID = 1L;

	public void setProcessoAssuntoIdProcessoAssunto(Integer id) {
		setId(id);
	}

	public Integer getProcessoAssuntoIdProcessoAssunto() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoAssunto createInstance() {
		ProcessoAssunto processoAssunto = new ProcessoAssunto();
		ProcessoTrfHome processoTrfHome = (ProcessoTrfHome) Component.getInstance("processoTrfHome", false);
		if (processoTrfHome != null) {
			processoAssunto.setProcessoTrf(processoTrfHome.getDefinedInstance());
		}
		AssuntoTrfHome assuntoTrfHome = (AssuntoTrfHome) Component.getInstance("assuntoTrfHome", false);
		if (assuntoTrfHome != null) {
			processoAssunto.setAssuntoTrf(assuntoTrfHome.getDefinedInstance());
		}
		return processoAssunto;
	}

	@Override
	public String remove() {
		ProcessoTrfHome processoTrf = (ProcessoTrfHome) Component.getInstance("processoTrfHome", false);
		if (processoTrf != null) {
			processoTrf.getInstance().getProcessoAssuntoList().remove(instance);
		}
		AssuntoTrfHome assuntoTrf = (AssuntoTrfHome) Component.getInstance("assuntoTrfHome", false);
		if (assuntoTrf != null) {
			assuntoTrf.getInstance().getProcessoAssuntoList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(ProcessoAssunto obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("processoAssuntoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		newInstance();
		return action;
	}

}