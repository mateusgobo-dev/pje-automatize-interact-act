package br.jus.csjt.pje.business.service;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

import br.jus.pje.jt.entidades.AudPauta;

@XmlType
public class RetornoAudPauta extends RetornoAud {
	// Lista contendo os dados solicitados de acordo com o serviço.
	private List<AudPauta> listaPauta;

	public void setListaPauta(List<AudPauta> listaPauta) {
		this.listaPauta = listaPauta;
	}

	public List<AudPauta> getListaPauta() {
		return listaPauta;
	}
}
