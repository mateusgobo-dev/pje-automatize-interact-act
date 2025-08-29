package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoProcedimentoOrigem;

public abstract class AbstractTipoProcedimentoOrigemHome<T> extends AbstractHome<TipoProcedimentoOrigem> {

	private static final long serialVersionUID = 1L;

	public void setTipoProcedimentoOrigemId(Integer id) {
		setId(id);
	}

	public Integer getTipoProcedimentoOrigemId() {
		return (Integer) getId();
	}

	@Override
	public void setTab(String tab) {
		super.setTab(tab);
		refreshGrid("tipoProcedimentoOrigemGrid");
	}

	@Override
	public void setGoBackTab(String goBackTab) {
		super.setGoBackTab(goBackTab);
		refreshGrid("tipoProcedimentoOrigemGrid");
	}
}
