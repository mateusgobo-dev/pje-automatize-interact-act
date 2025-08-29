package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PessoaJuridica;

public abstract class AbstractPessoaJuridicaHome<T> extends AbstractHome<PessoaJuridica> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void setPessoaJuridicaIdPessoaJuridica(Integer id) {
		setId(id);
	}

	public Integer getPessoaJuridicaIdPessoaJuridica() {
		return (Integer) getId();
	}

	@Override
	public String remove(PessoaJuridica obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("pessoaJuridicaGrid");
		return ret;
	}

}