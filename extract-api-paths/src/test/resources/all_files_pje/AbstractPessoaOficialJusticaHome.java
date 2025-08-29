package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PessoaOficialJustica;

public abstract class AbstractPessoaOficialJusticaHome<T> extends AbstractHome<PessoaOficialJustica> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void setPessoaOficialJusticaIdPessoaOficialJustica(Integer id) {
		setId(id);
	}

	public Integer getPessoaOficialJusticaIdPessoaOficialJustica() {
		return (Integer) getId();
	}

	@Override
	public String remove(PessoaOficialJustica obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("pessoaOficialJusticaGrid");
		return ret;
	}

}