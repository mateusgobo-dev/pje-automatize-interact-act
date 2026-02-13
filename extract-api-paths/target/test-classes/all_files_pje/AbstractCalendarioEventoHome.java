package br.com.infox.cliente.home;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.com.itx.component.grid.GridQuery;
import br.jus.pje.nucleo.entidades.CalendarioEvento;

public abstract class AbstractCalendarioEventoHome<T> extends AbstractHome<CalendarioEvento> {

	private static final long serialVersionUID = 1L;

	public void setCalendarioEventoIdCalendarioEvento(Integer id) {
		setId(id);
	}

	public Integer getCalendarioEventoIdCalendarioEvento() {
		return (Integer) getId();
	}

	@Override
	public void newInstance() {
		super.newInstance();
	}

	@Override
	protected CalendarioEvento createInstance() {
		CalendarioEvento calendarioEvento = new CalendarioEvento();
		return calendarioEvento;
	}

	@Override
	public String remove(CalendarioEvento obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		GridQuery grid = (GridQuery) Component.getInstance("calendarioEventoGrid");
		grid.refresh();
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
