package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.CentralMandado;

public abstract class AbstractCentralMandadoHome<T> extends AbstractHome<CentralMandado> {

	private static final long serialVersionUID = 1L;

	public void setCentralMandadoIdCentralMandado(Integer id) {
		setId(id);
	}

	public Integer getCentralMandadoIdCentralMandado() {
		return (Integer) getId();
	}

	@Override
	public String remove(CentralMandado obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("centralMandadoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		return action;
	}

}