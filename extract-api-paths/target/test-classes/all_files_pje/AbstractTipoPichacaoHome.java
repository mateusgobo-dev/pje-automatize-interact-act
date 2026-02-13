package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoPichacao;

public abstract class AbstractTipoPichacaoHome<T> extends AbstractHome<TipoPichacao> {

	private static final long serialVersionUID = 1L;

	public void setTipoPichacaoIdTipoPichacao(Integer id) {
		setId(id);
	}

	public Integer getTipoPichacaoIdTipoPichacao() {
		return (Integer) getId();
	}

	@Override
	protected TipoPichacao createInstance() {
		TipoPichacao tipoPichacao = new TipoPichacao();
		return tipoPichacao;
	}

	@Override
	public String remove(TipoPichacao obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("tipoPichacaoGrid");
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
	// @Override
	// public String persist() {
	// String action = super.persist();
	// newInstance();
	// return action;
	// }
}