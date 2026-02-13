package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Procuradoria;

public abstract class AbstractProcuradoriaHome<T> extends AbstractHome<Procuradoria> {

	private static final long serialVersionUID = 1L;

	public void setProcuradoriaIdProcuradoria(Integer id) {
		setId(id);
	}

	public Integer getProcuradoriaIdProcuradoria() {
		return (Integer) getId();
	}

}