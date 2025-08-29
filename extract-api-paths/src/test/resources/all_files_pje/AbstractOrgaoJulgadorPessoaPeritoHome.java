package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorPessoaPerito;

public abstract class AbstractOrgaoJulgadorPessoaPeritoHome<T> extends AbstractHome<OrgaoJulgadorPessoaPerito> {

	private static final long serialVersionUID = 1L;

	public void setOrgaoJulgadorPessoaPeritoIdOrgaoJulgadorPessoaPerito(Integer id) {
		setId(id);
	}

	public Integer getOrgaoJulgadorPessoaPeritoIdOrgaoJulgadorPessoaPerito() {
		return (Integer) getId();
	}

}