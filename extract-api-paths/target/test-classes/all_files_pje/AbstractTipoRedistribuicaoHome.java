package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoRedistribuicao;

public abstract class AbstractTipoRedistribuicaoHome<T> extends AbstractHome<TipoRedistribuicao> {

	private static final long serialVersionUID = 1L;

	public void setTipoRedistribuicaoIdTipoRedistribuicao(Integer id) {
		setId(id);
	}

	public Integer getTipoRedistribuicaoIdTipoRedistribuicao() {
		return (Integer) getId();
	}
}