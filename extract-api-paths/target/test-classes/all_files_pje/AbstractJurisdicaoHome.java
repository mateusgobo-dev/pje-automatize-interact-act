package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Jurisdicao;

public abstract class AbstractJurisdicaoHome<T> extends AbstractHome<Jurisdicao> {

	private static final long serialVersionUID = 1L;

	public void setJurisdicaoIdJurisdicao(Integer id) {
		setId(id);
	}

	public Integer getJurisdicaoIdJurisdicao() {
		return (Integer) getId();
	}

	@Override
	protected Jurisdicao createInstance() {
		Jurisdicao jurisdicao = new Jurisdicao();
		return jurisdicao;
	}

	@Override
	public String persist() {
		String action = super.persist();
		return action;
	}

}