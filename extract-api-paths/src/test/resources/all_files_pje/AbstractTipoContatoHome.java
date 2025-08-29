package br.com.infox.cliente.home;

import java.util.List;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.MeioContato;
import br.jus.pje.nucleo.entidades.TipoContato;

public abstract class AbstractTipoContatoHome<T> extends AbstractHome<TipoContato> {

	private static final long serialVersionUID = 1L;

	public void setTipoContatoIdTipoContato(Integer id) {
		setId(id);
	}

	public Integer getTipoContatoIdTipoContato() {
		return (Integer) getId();
	}

	@Override
	protected TipoContato createInstance() {
		TipoContato tipoContato = new TipoContato();
		return tipoContato;
	}

	@Override
	public String remove(TipoContato obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		refreshGrid("tipoContatoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		// newInstance();
		return action;
	}

	public List<MeioContato> getMeioContatoList() {
		return getInstance() == null ? null : getInstance().getMeioContatoList();
	}

}