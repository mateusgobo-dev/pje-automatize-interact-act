package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Pessoa;

public abstract class AbstractPessoaHome<T> extends AbstractHome<Pessoa> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void setPessoaIdPessoa(Integer id) {
		setId(id);
	}

	public Integer getPessoaIdPessoa() {
		return (Integer) getId();
	}

	@Override
	public String remove(Pessoa obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("pessoaGrid");
		return ret;
	}

}