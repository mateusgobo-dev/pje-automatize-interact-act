package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaEntidade;

public abstract class AbstractPessoaProcuradoriaEntidadeHome<T> extends AbstractHome<PessoaProcuradoriaEntidade> {

	private static final long serialVersionUID = 1L;

	public void setPessoaProcuradoriaEntidadeIdPessoaProcuradoriaEntidade(Integer id) {
		setId(id);
	}

	public Integer getPessoaProcuradoriaEntidadeIdPessoaProcuradoriaEntidade() {
		return (Integer) getId();
	}

	@Override
	protected PessoaProcuradoriaEntidade createInstance() {
		PessoaProcuradoriaEntidade pessoaProcuradoriaEntidade = new PessoaProcuradoriaEntidade();
		return pessoaProcuradoriaEntidade;
	}

	@Override
	public String remove(PessoaProcuradoriaEntidade obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("pessoaProcuradoriaEntidadeGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		newInstance();
		refreshGrid("pessoaProcuradoriaEntidadeGrid");
		return action;
	}

}