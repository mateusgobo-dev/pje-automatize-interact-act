package br.jus.cnj.pje.webservice;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InformacaoAudienciaResposta {

	private List<InformacaoAudiencia> audiencias;
	
	public List<InformacaoAudiencia> getAudiencias() {
		return audiencias;
	}
	
	public void setAudiencias(List<InformacaoAudiencia> audiencias) {
		this.audiencias = audiencias;
	}		
}
