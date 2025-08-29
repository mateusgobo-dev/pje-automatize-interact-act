/* $Id: AbstractPessoaPeritoHome.java 10746 2010-08-12 23:23:46Z jplacerda $ */

package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PessoaPerito;

public abstract class AbstractPessoaPeritoHome<T> extends AbstractHome<PessoaPerito> {

	private static final long serialVersionUID = 1L;

	public void setPessoaPeritoIdPessoaPerito(Integer id) {
		setId(id);
	}

	public Integer getPessoaPeritoIdPessoaPerito() {
		return (Integer) getId();
	}

	@Override
	public String remove(PessoaPerito obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("pessoaPeritoGrid");
		return ret;
	}

	@Override
	public String inactive(PessoaPerito p) {
		p.setAtivo(Boolean.FALSE);
		String resultado = super.update();
		newInstance();
		refreshGrid("pessoaPeritoGrid");
		return resultado;
	}

}