package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;

public abstract class AbstractUsuarioLocalizacaoMagistradoServidorHome<T> extends
		AbstractHome<UsuarioLocalizacaoMagistradoServidor> {

	private static final long serialVersionUID = 1L;

	public void setUsuarioLocalizacaoMagistradoServidorIdUsuarioLocalizacaoMagistradoServidor(Integer id) {
		setId(id);
	}

	public Integer getUsuarioLocalizacaoMagistradoServidorIdUsuarioLocalizacaoMagistradoServidor() {
		return (Integer) getId();
	}

	@Override
	protected UsuarioLocalizacaoMagistradoServidor createInstance() {
		UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor = new UsuarioLocalizacaoMagistradoServidor();
		return usuarioLocalizacaoMagistradoServidor;
	}

	@Override
	public String remove(UsuarioLocalizacaoMagistradoServidor obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		return action;
	}
}