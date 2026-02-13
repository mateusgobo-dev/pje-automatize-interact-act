package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoCompetencia;

public abstract class AbstractOrgaoJulgadorColegiadoCompetenciaHome<T> extends
		AbstractHome<OrgaoJulgadorColegiadoCompetencia> {

	private static final long serialVersionUID = 1L;

	public void setOrgaoJulgadorColegiadoCompetenciaIdOrgaoJulgadorColegiadoCompetencia(Integer id) {
		setId(id);
	}

	public Integer getOrgaoJulgadorColegiadoCompetenciaIdOrgaoJulgadorColegiadoCompetencia() {
		return (Integer) getId();
	}

}