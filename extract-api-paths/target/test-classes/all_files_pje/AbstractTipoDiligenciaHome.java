package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoDiligencia;

public abstract class AbstractTipoDiligenciaHome<T> extends AbstractHome<TipoDiligencia> {

	private static final long serialVersionUID = 1L;

	public void setTipoDiligenciaIdTipoDiligencia(Integer id) {
		setId(id);
	}

	public Integer getTipoDiligenciaIdTipoDiligencia() {
		return (Integer) getId();
	}

	@Override
	protected TipoDiligencia createInstance() {
		TipoDiligencia tipoDiligencia = new TipoDiligencia();
		return tipoDiligencia;
	}

	@Override
	public String remove(TipoDiligencia obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		refreshGrid("tipoDiligenciaGrid");
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