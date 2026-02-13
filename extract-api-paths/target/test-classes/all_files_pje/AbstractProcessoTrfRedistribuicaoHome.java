package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoTrfRedistribuicao;

public abstract class AbstractProcessoTrfRedistribuicaoHome<T> extends AbstractHome<ProcessoTrfRedistribuicao> {

	private static final long serialVersionUID = 1L;

	public void setProcessoTrfRedistribuicaoIdProcessoTrfRedistribuicao(Integer id) {
		setId(id);
	}

	public Integer getProcessoTrfRedistribuicaoIdProcessoTrfRedistribuicao() {
		return (Integer) getId();
	}
}