package br.jus.csjt.pje.business.service;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

import br.jus.pje.jt.entidades.AudOrgaoMunicipio;

@XmlType
public class RetornoAudOrgaoMunicipio extends RetornoAud {
	// Lista contendo os dados solicitados de acordo com o serviço.
	private List<AudOrgaoMunicipio> listaOrgaoMunicipio;

	public void setlistaOrgaoMunicipio(List<AudOrgaoMunicipio> listaOrgaoMunicipio) {
		this.listaOrgaoMunicipio = listaOrgaoMunicipio;
	}

	public List<AudOrgaoMunicipio> getlistaOrgaoMunicipio() {
		return listaOrgaoMunicipio;
	}
}
