package br.com.infox.ibpm.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Agrupamento;

public abstract class AbstractAgrupamentoHome<T> extends AbstractHome<Agrupamento> {

	private static final long serialVersionUID = 1L;

	public void setAgrupamentoIdAgrupamento(Integer id) {
		setId(id);
	}

	public Integer getAgrupamentoIdAgrupamento() {
		return (Integer) getId();
	}

	@Override
	public String remove(Agrupamento obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("agrupamentoGrid");
		return ret;
	}

}
