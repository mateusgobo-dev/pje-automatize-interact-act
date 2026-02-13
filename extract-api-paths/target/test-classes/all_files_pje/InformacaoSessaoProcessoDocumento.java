package br.jus.cnj.pje.webservice;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

public class InformacaoSessaoProcessoDocumento implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4526616063308233270L;
	
	private Integer idSessaoProcessoDocumento;
	private Integer idTipoProcessoDocumento;
	private String conteudo;
	
	@XmlElement(name="idSessaoProcessoDocumento")
	public Integer getIdSessaoProcessoDocumento() {
		return idSessaoProcessoDocumento;
	}
	
	public void setIdSessaoProcessoDocumento(Integer idSessaoProcessoDocumento) {
		this.idSessaoProcessoDocumento = idSessaoProcessoDocumento;
	}	

	@XmlElement(name="idTipoProcessoDocumento")
	public Integer getIdTipoProcessoDocumento() {
		return idTipoProcessoDocumento;
	}

	public void setIdTipoProcessoDocumento(Integer idTipoProcessoDocumento) {
		this.idTipoProcessoDocumento = idTipoProcessoDocumento;
	}

	@XmlElement(name="conteudo")
	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}
}
