package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoSessao;

public abstract class AbstractTipoSessaoHome<T> extends AbstractHome<TipoSessao> {

	private static final long serialVersionUID = 1L;

	public void setTipoSessaoIdTipoSessao(Integer id) {
		setId(id);
	}

	public Integer getTipoSessaoIdTipoSessao() {
		return (Integer) getId();
	}

	@Override
	protected TipoSessao createInstance() {
		TipoSessao tipoSessao = new TipoSessao();
		return tipoSessao;
	}

	@Override
	public String remove(TipoSessao obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		return ret;
	}

	@Override
	public String persist() {
		getInstance().setAtivo(true);
		String action = super.persist();
		return action;
	}

}