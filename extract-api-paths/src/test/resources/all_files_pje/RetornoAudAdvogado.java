package br.jus.csjt.pje.business.service;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

import br.jus.pje.jt.entidades.AudAdvogados;

@XmlType
public class RetornoAudAdvogado extends RetornoAud {
	// Lista contendo os dados solicitados de acordo com o serviço.
	private List<AudAdvogados> listaAdvogado;

	public void setListaAdvogado(List<AudAdvogados> listaAdvogado) {
		this.listaAdvogado = listaAdvogado;
	}

	public List<AudAdvogados> getListaAdvogado() {
		return listaAdvogado;
	}
}
