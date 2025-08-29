package br.jus.csjt.pje.business.service;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

import br.jus.pje.jt.entidades.AudEspecie;

@XmlType
public class RetornoAudEspecie extends RetornoAud {
	// Lista contendo os dados solicitados de acordo com o serviço.
	private List<AudEspecie> listaEspecie;

	public void setListaEspecie(List<AudEspecie> listaEspecie) {
		this.listaEspecie = listaEspecie;
	}

	public List<AudEspecie> getListaEspecie() {
		return listaEspecie;
	}
}
