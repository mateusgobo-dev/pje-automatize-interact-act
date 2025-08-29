package br.jus.csjt.pje.business.service;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

import br.jus.pje.jt.entidades.AudTipoVerba;

@XmlType
public class RetornoAudTipoVerba extends RetornoAud {
	
	private List<AudTipoVerba> listaTipoVerba;

	public void setListaTipoVerba(List<AudTipoVerba> listaTipoVerba) {
		this.listaTipoVerba = listaTipoVerba;
	}

	public List<AudTipoVerba> getListaTipoVerba() {
		return listaTipoVerba;
	}
}
