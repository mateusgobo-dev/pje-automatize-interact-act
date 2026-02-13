package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;

public abstract class AbstractPessoaMagistradoHome<T> extends AbstractHome<PessoaMagistrado> {

	private static final long serialVersionUID = 1L;

	public void setPessoaMagistradoIdPessoaMagistrado(Integer id) {
		setId(id);
	}

	public Integer getPessoaMagistradoIdPessoaMagistrado() {
		return (Integer) getId();
	}

	@Override
	public String remove(PessoaMagistrado obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("pessoaMagistradoGrid");
		return ret;
	}

}