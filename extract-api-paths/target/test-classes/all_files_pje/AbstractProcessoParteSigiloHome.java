package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoParteSigilo;

public abstract class AbstractProcessoParteSigiloHome<T> extends AbstractHome<ProcessoParteSigilo> {

	private static final long serialVersionUID = 1L;

	public void setProcessoParteSigiloIdProcessoParteSigilo(Integer id) {
		setId(id);
	}

	public Integer getProcessoParteSigiloIdProcessoParteSigilo() {
		return (Integer) getId();
	}
}