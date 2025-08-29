package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PessoaServidor;

public abstract class AbstractPessoaServidorHome<T> extends AbstractHome<PessoaServidor> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void setPessoaServidorIdPessoaServidor(Integer id) {
		setId(id);
	}

	public Integer getPessoaServidorIdPessoaServidor() {
		return (Integer) getId();
	}

	@Override
	public String remove(PessoaServidor obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("pessoaServidorGrid");
		return ret;
	}

}