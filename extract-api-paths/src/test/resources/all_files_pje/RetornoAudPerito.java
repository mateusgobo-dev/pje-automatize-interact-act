package br.jus.csjt.pje.business.service;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

import br.jus.pje.jt.entidades.AudPeritos;

@XmlType
public class RetornoAudPerito extends RetornoAud {
	// Lista contendo os dados solicitados de acordo com o serviço.
	private List<AudPeritos> listaPerito;

	public void setListaPerito(List<AudPeritos> listaPerito) {
		this.listaPerito = listaPerito;
	}

	public List<AudPeritos> getListaPerito() {
		return listaPerito;
	}
}
