package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoLido;

public abstract class AbstractProcessoDocumentoLidoHome<T> extends AbstractHome<ProcessoDocumentoLido> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void setProcessoDocumentoLidoIdProcessoDocumentoLido(Integer id) {
		setId(id);
	}

	public Integer getProcessoDocumentoLidoIdProcessoDocumentoLido() {
		return (Integer) getId();
	}
}