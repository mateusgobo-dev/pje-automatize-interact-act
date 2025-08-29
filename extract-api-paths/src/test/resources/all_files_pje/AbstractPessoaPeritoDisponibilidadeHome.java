package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PessoaPeritoDisponibilidade;

public abstract class AbstractPessoaPeritoDisponibilidadeHome<T> extends AbstractHome<PessoaPeritoDisponibilidade> {

	private static final long serialVersionUID = 1L;

	public void setPessoaPeritoDisponibilidadeIdPessoaPeritoDisponibilidade(Integer id) {
		setId(id);
	}

	public Integer getPessoaPeritoDisponibilidadeIdPessoaPeritoDisponibilidade() {
		return (Integer) getId();
	}

	@Override
	protected PessoaPeritoDisponibilidade createInstance() {
		PessoaPeritoDisponibilidade pessoaPeritoDisponibilidade = new PessoaPeritoDisponibilidade();
		return pessoaPeritoDisponibilidade;
	}

	@Override
	public String remove(PessoaPeritoDisponibilidade obj) {
		obj.setAtivo(false);
		return update(obj);
	}

	public String persist(PessoaPeritoDisponibilidade obj) {
		setInstance(obj);
		String ret = super.persist();
		newInstance();
		refreshGrid("pessoaPeritoDisponibilidadeGrid");
		refreshGrid("pessoaPeritoDisponibilidadePeritoGrid");
		return ret;
	}

	public String update(PessoaPeritoDisponibilidade obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		refreshGrid("pessoaPeritoDisponibilidadeGrid");
		refreshGrid("pessoaPeritoDisponibilidadePeritoGrid");
		return ret;
	}

}