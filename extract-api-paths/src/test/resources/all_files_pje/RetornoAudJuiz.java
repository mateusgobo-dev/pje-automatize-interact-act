package br.jus.csjt.pje.business.service;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

import br.jus.pje.jt.entidades.AudJuizes;

@XmlType
public class RetornoAudJuiz extends RetornoAud {
	// Lista contendo os dados solicitados de acordo com o serviço.
	private List<AudJuizes> listaJuiz;

	public void setListaJuiz(List<AudJuizes> listaJuiz) {
		this.listaJuiz = listaJuiz;
	}

	public List<AudJuizes> getListaJuiz() {
		return listaJuiz;
	}
}
