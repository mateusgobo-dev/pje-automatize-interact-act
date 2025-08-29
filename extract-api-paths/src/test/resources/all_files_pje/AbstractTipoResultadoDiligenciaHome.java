package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoResultadoDiligencia;

public abstract class AbstractTipoResultadoDiligenciaHome<T> extends AbstractHome<TipoResultadoDiligencia> {

	private static final long serialVersionUID = 1L;

	public void setTipoResultadoDiligenciaIdTipoResultadoDiligencia(Integer id) {
		setId(id);
	}

	public Integer getTipoResultadoDiligenciaIdTipoResultadoDiligencia() {
		return (Integer) getId();
	}

	@Override
	protected TipoResultadoDiligencia createInstance() {
		TipoResultadoDiligencia tipoResultadoDiligencia = new TipoResultadoDiligencia();
		return tipoResultadoDiligencia;
	}

	@Override
	public String remove(TipoResultadoDiligencia obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		refreshGrid("tipoResultadoDiligenciaGrid");
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