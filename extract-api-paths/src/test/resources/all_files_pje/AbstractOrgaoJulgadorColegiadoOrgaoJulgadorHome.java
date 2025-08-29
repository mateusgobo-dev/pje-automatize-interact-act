package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;

public abstract class AbstractOrgaoJulgadorColegiadoOrgaoJulgadorHome<T> extends
		AbstractHome<OrgaoJulgadorColegiadoOrgaoJulgador> {

	private static final long serialVersionUID = 1L;

	public void setOrgaoJulgadorColegiadoOrgaoJulgadorIdOrgaoJulgadorColegiadoOrgaoJulgador(Integer id) {
		setId(id);
	}

	public Integer getOrgaoJulgadorColegiadoOrgaoJulgadorIdOrgaoJulgadorColegiadoOrgaoJulgador() {
		return (Integer) getId();
	}

}