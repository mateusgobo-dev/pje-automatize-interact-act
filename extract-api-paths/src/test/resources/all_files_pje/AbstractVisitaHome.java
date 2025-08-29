package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Visita;

public abstract class AbstractVisitaHome<T> extends AbstractHome<Visita> {

	private static final long serialVersionUID = 1L;

	public void setVisitaIdVisita(Integer id) {
		setId(id);
	}

	public Integer getVisitaIdVisita() {
		return (Integer) getId();
	}

	@Override
	protected Visita createInstance() {
		Visita visita = new Visita();
		return visita;
	}

	@Override
	public String remove(Visita obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("visitaGrid");
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