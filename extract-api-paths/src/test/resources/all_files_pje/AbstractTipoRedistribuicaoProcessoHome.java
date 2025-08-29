package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoRedistribuicao;

public abstract class AbstractTipoRedistribuicaoProcessoHome<T> extends AbstractHome<TipoRedistribuicao> {

	private static final long serialVersionUID = 1L;

	public void setRedistribuirProcessoIdRedistribuirProcesso(Integer id) {
		setId(id);
	}

	public Integer getRedistribuirProcessoIdRedistribuirProcesso() {
		return (Integer) getId();
	}

	@Override
	public String remove(TipoRedistribuicao obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		// refreshGrid("");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (action != null)
			newInstance();
		return action;
	}
}