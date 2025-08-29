package br.jus.csjt.pje.business.service;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

import br.jus.pje.jt.entidades.AudAutor;

@XmlType
public class RetornoAudAutor extends RetornoAud {
	// Lista contendo os dados solicitados de acordo com o serviço.
	private List<AudAutor> listaAutor;

	public void setListaAutor(List<AudAutor> listaAutor) {
		this.listaAutor = listaAutor;
	}

	public List<AudAutor> getListaAutor() {
		return listaAutor;
	}
}
