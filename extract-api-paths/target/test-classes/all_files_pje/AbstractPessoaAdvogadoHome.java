package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;

public abstract class AbstractPessoaAdvogadoHome<T> extends AbstractHome<PessoaAdvogado> {

	private static final long serialVersionUID = 1L;

	public void setPessoaAdvogadoIdPessoaAdvogado(Integer id) {
		setId(id);
	}

	public Integer getPessoaAdvogadoIdPessoaAdvogado() {
		return (Integer) getId();
	}

	@Override
	protected PessoaAdvogado createInstance() {
		PessoaAdvogado pessoaAdvogado = new PessoaAdvogado();
		return pessoaAdvogado;
	}

	@Override
	public String remove(PessoaAdvogado obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("pessoaAdvogadoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (action != null)
			newInstance();
		return action;
	}
}