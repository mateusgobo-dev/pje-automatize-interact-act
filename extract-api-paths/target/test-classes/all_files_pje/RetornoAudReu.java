package br.jus.csjt.pje.business.service;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

import br.jus.pje.jt.entidades.AudReu;

@XmlType
public class RetornoAudReu extends RetornoAud {
	// Lista contendo os dados solicitados de acordo com o serviço.
	private List<AudReu> listaReu;

	public void setListaReu(List<AudReu> listaReu) {
		this.listaReu = listaReu;
	}

	public List<AudReu> getListaReu() {
		return listaReu;
	}
}
