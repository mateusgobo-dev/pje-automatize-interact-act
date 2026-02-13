package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.RelacaoPessoal;

public abstract class AbstractRelacaoPessoalHome<T> extends AbstractHome<RelacaoPessoal> {

	private static final long serialVersionUID = 1L;

	public void setRelacaoPessoalIdRelacaoPessoal(Integer id) {
		setId(id);
	}

	public Integer getRelacaoPessoalIdRelacaoPessoal() {
		return (Integer) getId();
	}

	@Override
	public String remove(RelacaoPessoal obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		refreshGrid("relacaoPessoalGrid");
		return ret;
	}
}
