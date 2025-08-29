package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PessoaFisica;

public abstract class AbstractPessoaFisicaHome<T> extends AbstractHome<PessoaFisica> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void setPessoaFisicaIdPessoaFisica(Integer id) {
		setId(id);
	}

	public Integer getPessoaFisicaIdPessoaFisica() {
		return (Integer) getId();
	}

	@Override
	public String remove(PessoaFisica obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("pessoaFisicaGrid");
		return ret;
	}

}