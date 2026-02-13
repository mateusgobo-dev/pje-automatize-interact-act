package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Diligencia;

public abstract class AbstractDiligenciaHome<T> extends AbstractHome<Diligencia> {

	private static final long serialVersionUID = 1L;

	public void setDiligenciaIdDiligencia(Integer id) {
		setId(id);
	}

	public Integer getDiligenciaIdDiligencia() {
		return (Integer) getId();
	}

	@Override
	protected Diligencia createInstance() {
		Diligencia diligencia = new Diligencia();
		return diligencia;
	}

	@Override
	public String remove(Diligencia obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		refreshGrid("diligenciaGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		return action;
	}
}