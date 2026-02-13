package br.com.infox.cliente.home;

import java.util.List;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoLote;

public abstract class AbstractProcessoLoteHome<T> extends AbstractHome<ProcessoLote> {

	private static final long serialVersionUID = 1L;

	public void setProcessoLoteIdProcessoLote(Integer id) {
		setId(id);
	}

	public Integer getProcessoLoteIdProcessoLote() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoLote createInstance() {
		ProcessoLote processoLote = new ProcessoLote();
		LoteHome loteHome = LoteHome.instance();
		if (loteHome != null) {
			processoLote.setLote(loteHome.getDefinedInstance());
		}
		ProcessoTrfHome processoTrfHome = (ProcessoTrfHome) Component.getInstance("processoTrfHome", false);
		if (processoTrfHome != null) {
			processoLote.setProcessoTrf(processoTrfHome.getDefinedInstance());
		}
		return processoLote;
	}

	@Override
	public String remove() {
		ProcessoTrfHome processoTrf = (ProcessoTrfHome) Component.getInstance("processoTrfHome", false);
		if (processoTrf != null) {
			processoTrf.getInstance().getProcessoLoteList().remove(instance);
		}
		LoteHome lote = (LoteHome) Component.getInstance("loteHome", false);
		if (lote != null) {
			List<ProcessoLote> processoLoteList = lote.getInstance().getProcessoLoteList();
			processoLoteList.remove(instance);
		}
		return super.remove();
	}

	// @Override
	// protected ProcessoLote createInstance() {
	// ProcessoLote processoLote = new ProcessoLote();
	// return processoLote;
	// }

	@Override
	public String remove(ProcessoLote obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("processoLoteGrid");
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