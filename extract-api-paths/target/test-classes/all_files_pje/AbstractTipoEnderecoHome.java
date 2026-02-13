package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoEndereco;

public abstract class AbstractTipoEnderecoHome<T> extends AbstractHome<TipoEndereco> {

	private static final long serialVersionUID = 1L;

	public void setTipoEnderecoIdTipoEndereco(Integer id) {
		setId(id);
	}

	public Integer getTipoEnderecoIdTipoEndereco() {
		return (Integer) getId();
	}

	@Override
	protected TipoEndereco createInstance() {
		TipoEndereco tipoEndereco = new TipoEndereco();
		return tipoEndereco;
	}

	@Override
	public String remove(TipoEndereco obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("tipoEnderecoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		// newInstance();
		return action;
	}
}