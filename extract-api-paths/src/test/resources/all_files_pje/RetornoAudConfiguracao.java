package br.jus.csjt.pje.business.service;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class RetornoAudConfiguracao extends RetornoAud {
	
	private List<AudConfiguracao> audConfiguracao;

	public List<AudConfiguracao> getAudConfiguracao() {
		return audConfiguracao;
	}

	public void setAudConfiguracao(List<AudConfiguracao> audConfiguracao) {
		this.audConfiguracao = audConfiguracao;
	}
}
