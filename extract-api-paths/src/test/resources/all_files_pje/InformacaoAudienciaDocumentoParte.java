package br.jus.cnj.pje.webservice;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InformacaoAudienciaDocumentoParte {
	private String tipoDocumento;
	private String numeroDocumento;
	
	public String getTipoDocumento() {
		return tipoDocumento;
	}
	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}
	public String getNumeroDocumento() {
		return numeroDocumento;
	}
	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}
	
	
}
