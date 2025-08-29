package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;

public abstract class AbstractProcessoParteRepresentanteHome<T> extends AbstractHome<ProcessoParteRepresentante> {

	private static final long serialVersionUID = 1L;

	public void setProcessoParteRepresentanteIdProcessoParteRepresentante(Integer id) {
		setId(id);
	}

	public Integer getProcessoParteRepresentanteIdProcessoParteRepresentante() {
		return (Integer) getId();
	}

}