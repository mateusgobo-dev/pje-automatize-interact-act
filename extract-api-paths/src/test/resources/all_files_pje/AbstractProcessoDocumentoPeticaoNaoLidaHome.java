package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoPeticaoNaoLida;

public abstract class AbstractProcessoDocumentoPeticaoNaoLidaHome<T> extends
		AbstractHome<ProcessoDocumentoPeticaoNaoLida> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void setProcessoDocumentoPeticaoNaoLidaIdProcessoDocumentoPeticaoNaoLida(Integer id) {
		setId(id);
	}

	public Integer getProcessoDocumentoPeticaoNaoLidaIdProcessoDocumentoPeticaoNaoLida() {
		return (Integer) getId();
	}
}