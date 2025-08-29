package br.com.infox.cliente.home;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.com.itx.component.grid.GridQuery;
import br.jus.pje.nucleo.entidades.ProcessoPericia;

public abstract class AbstractProcessoPericiaHome<T> extends AbstractHome<ProcessoPericia> {

	private static final long serialVersionUID = 1L;

	public void setProcessoPericiaIdProcessoPericia(Integer id) {
		setId(id);
	}

	public Integer getProcessoPericiaIdProcessoPericia() {
		return (Integer) getId();
	}

	@Override
	public String remove(ProcessoPericia obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		GridQuery grid = (GridQuery) Component.getInstance("processoPericiaGrid");
		grid.refresh();
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		return action;
	}

}