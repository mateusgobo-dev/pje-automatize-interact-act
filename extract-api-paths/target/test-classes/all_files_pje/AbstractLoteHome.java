package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Lote;

public abstract class AbstractLoteHome<T> extends AbstractHome<Lote> {

	private static final long serialVersionUID = 1L;

	public void setLoteIdLote(Integer id) {
		setId(id);
	}

	public Integer getLoteIdLote() {
		return (Integer) getId();
	}

	@Override
	protected Lote createInstance() {
		Lote lote = new Lote();
		return lote;
	}

	@Override
	public String remove(Lote obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("LoteGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		return action;
	}
}