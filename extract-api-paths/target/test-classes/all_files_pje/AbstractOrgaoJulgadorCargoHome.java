package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;

public abstract class AbstractOrgaoJulgadorCargoHome<T> extends AbstractHome<OrgaoJulgadorCargo> {

	private static final long serialVersionUID = 1L;

	public void setOrgaoJulgadorCargoIdOrgaoJulgadorCargo(Integer id) {
		setId(id);
	}

	public Integer getOrgaoJulgadorCargoIdOrgaoJulgadorCargo() {
		return (Integer) getId();
	}

}