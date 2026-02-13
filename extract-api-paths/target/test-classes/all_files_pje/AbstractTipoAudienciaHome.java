package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoAudiencia;

public abstract class AbstractTipoAudienciaHome<T> extends AbstractHome<TipoAudiencia> {

	private static final long serialVersionUID = 1L;

	public void setTipoAudienciaIdTipoAudiencia(Integer id) {
		setId(id);
	}

	public Integer getTipoAudienciaIdTipoAudiencia() {
		return (Integer) getId();
	}

	@Override
	protected TipoAudiencia createInstance() {
		TipoAudiencia tipoAudiencia = new TipoAudiencia();
		return tipoAudiencia;
	}

	@Override
	public String remove(TipoAudiencia obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		refreshGrid("tipoAudienciaGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		return action;
	}

}