package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.AtuacaoAdvogado;

public abstract class AbstractAtuacaoAdvogadoHome<T> extends AbstractHome<AtuacaoAdvogado> {

	private static final long serialVersionUID = 1L;

	public void setAtuacaoAdvogadoIdAtuacaoAdvogado(Integer id) {
		setId(id);
	}

	public Integer getAtuacaoAdvogadoIdAtuacaoAdvogado() {
		return (Integer) getId();
	}

	@Override
	protected AtuacaoAdvogado createInstance() {
		AtuacaoAdvogado atuacaoAdvogado = new AtuacaoAdvogado();
		return atuacaoAdvogado;
	}

	@Override
	public String remove(AtuacaoAdvogado obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("atuacaoAdvogadoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		return action;
	}

}