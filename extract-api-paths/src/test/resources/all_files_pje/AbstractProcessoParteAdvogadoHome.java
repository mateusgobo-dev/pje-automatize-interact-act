package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoParteAdvogado;

public abstract class AbstractProcessoParteAdvogadoHome<T> extends AbstractHome<ProcessoParteAdvogado> {

	private static final long serialVersionUID = 1L;

	public void setProcessoParteAdvogadoIdProcessoParteAdvogado(Integer id) {
		setId(id);
	}

	public Integer getProcessoParteAdvogadoIdProcessoParteAdvogado() {
		return (Integer) getId();
	}

}