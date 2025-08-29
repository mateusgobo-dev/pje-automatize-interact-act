package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoVisibilidadeSegredo;

public abstract class AbstractProcessoDocumentoVisibilidadeSegredoHome<T> extends
		AbstractHome<ProcessoDocumentoVisibilidadeSegredo> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void setProcessoDocumentoVisibilidadeSegredoIdProcessoDocumentoVisibilidadeSegredo(Integer id) {
		setId(id);
	}

	public Integer getProcessoDocumentoVisibilidadeSegredoIdProcessoDocumentoVisibilidadeSegredo() {
		return (Integer) getId();
	}

	public String persist(ProcessoDocumentoVisibilidadeSegredo obj) {
		setInstance(obj);
		String ret = super.persist();
		newInstance();
		return ret;
	}

	@Override
	public String remove(ProcessoDocumentoVisibilidadeSegredo obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		return ret;
	}

}