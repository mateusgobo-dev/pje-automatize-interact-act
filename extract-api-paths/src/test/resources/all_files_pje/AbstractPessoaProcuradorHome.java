package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PessoaProcurador;

public abstract class AbstractPessoaProcuradorHome<T> extends AbstractHome<PessoaProcurador> {

	private static final long serialVersionUID = 1L;

	public void setPessoaProcuradorIdPessoaProcurador(Integer id) {
		setId(id);
	}

	public Integer getPessoaProcuradorIdPessoaProcurador() {
		return (Integer) getId();
	}

	@Override
	public String remove(PessoaProcurador obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("pessoaProcuradorGrid");
		return ret;
	}
}