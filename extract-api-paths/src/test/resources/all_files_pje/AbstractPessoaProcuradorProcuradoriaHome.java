package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PessoaProcuradorProcuradoria;

public abstract class AbstractPessoaProcuradorProcuradoriaHome<T> extends AbstractHome<PessoaProcuradorProcuradoria> {

	private static final long serialVersionUID = 1L;

	public void setPessoaProcuradorProcuradoriaIdPessoaProcuradorProcuradoria(Integer id) {
		setId(id);
	}

	public Integer getPessoaProcuradorProcuradoriaIdPessoaProcuradorProcuradoria() {
		return (Integer) getId();
	}

	@Override
	protected PessoaProcuradorProcuradoria createInstance() {
		PessoaProcuradorProcuradoria pessoaProcuradorProcuradoria = new PessoaProcuradorProcuradoria();
		return pessoaProcuradorProcuradoria;
	}

	@Override
	public String remove(PessoaProcuradorProcuradoria obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("pessoaProcuradorProcuradoriaGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		newInstance();
		refreshGrid("pessoaProcuradorProcuradoriaGrid");
		return action;
	}

}