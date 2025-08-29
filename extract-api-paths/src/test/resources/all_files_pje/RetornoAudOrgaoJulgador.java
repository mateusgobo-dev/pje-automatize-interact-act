package br.jus.csjt.pje.business.service;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class RetornoAudOrgaoJulgador extends RetornoAud {
	
	private List<AudOrgaoJulgador> listaOrgaoJulgador;

	public List<AudOrgaoJulgador> getListaOrgaoJulgador() {
		return listaOrgaoJulgador;
	}

	public void setListaOrgaoJulgador(List<AudOrgaoJulgador> listaOrgaoJulgador) {
		this.listaOrgaoJulgador = listaOrgaoJulgador;
	}
	
}
