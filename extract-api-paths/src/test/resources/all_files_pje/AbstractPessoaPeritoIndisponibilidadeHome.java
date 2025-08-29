package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PessoaPeritoIndisponibilidade;

public abstract class AbstractPessoaPeritoIndisponibilidadeHome<T> extends AbstractHome<PessoaPeritoIndisponibilidade> {

	private static final long serialVersionUID = 1L;

	public void setPessoaPeritoIndisponibilidadeIdPessoaPeritoIndisponibilidade(Integer id) {
		setId(id);
	}

	public Integer getPessoaPeritoIndisponibilidadeIdPessoaPeritoIndisponibilidade() {
		return (Integer) getId();
	}

	@Override
	protected PessoaPeritoIndisponibilidade createInstance() {
		PessoaPeritoIndisponibilidade pessoaPeritoIndisponibilidade = new PessoaPeritoIndisponibilidade();
		return pessoaPeritoIndisponibilidade;
	}

	@Override
	public String remove(PessoaPeritoIndisponibilidade obj) {
		obj.setAtivo(false);
		return update(obj);
	}

	public String persist(PessoaPeritoIndisponibilidade obj) {
		setInstance(obj);
		String ret = super.persist();
		newInstance();
		return ret;
	}

	public String update(PessoaPeritoIndisponibilidade obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		return ret;
	}

}