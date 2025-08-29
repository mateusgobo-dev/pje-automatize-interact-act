package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoParte;

public abstract class AbstractTipoParteHome<T> extends AbstractHome<TipoParte> {

	private static final long serialVersionUID = 1L;

	public void setTipoParteIdTipoParte(Integer id) {
		setId(id);
	}

	public Integer getTipoParteIdTipoParte() {
		return (Integer) getId();
	}

	@Override
	protected TipoParte createInstance() {
		TipoParte tipoParte = new TipoParte();
		return tipoParte;
	}

	@Override
	public String remove(TipoParte obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("tipoParteGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		return action;
	}
}