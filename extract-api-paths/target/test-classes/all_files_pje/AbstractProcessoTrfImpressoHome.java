package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoTrfImpresso;

public abstract class AbstractProcessoTrfImpressoHome<T> extends AbstractHome<ProcessoTrfImpresso> {

	private static final long serialVersionUID = 1L;

	public void setProcessoTrfImpressoIdProcessoTrfImpresso(Integer id) {
		setId(id);
	}

	public Integer getProcessoTrfImpressoIdProcessoTrfImpresso() {
		return (Integer) getId();
	}
}