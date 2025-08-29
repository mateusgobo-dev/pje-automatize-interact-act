package br.jus.cnj.pje.webservice;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

public class InformacaoSessaoProcessoVotante implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3125110417095224091L;
	
	private Integer idOrgaoJulgador;
	private String nomeOrgaoJulgador;
	private Boolean votoPossuiConteudo;
	private Boolean acompanhaRelator;
	
	@XmlElement(name="idOrgaoJulgador")
	public Integer getIdOrgaoJulgador() {
		return idOrgaoJulgador;
	}
	
	public void setIdOrgaoJulgador(Integer idOrgaoJulgador) {
		this.idOrgaoJulgador = idOrgaoJulgador;
	}
	
	@XmlElement(name="nomeOrgaoJulgador")
	public String getNomeOrgaoJulgador() {
		return nomeOrgaoJulgador;
	}

	public void setNomeOrgaoJulgador(String nomeOrgaoJulgador) {
		this.nomeOrgaoJulgador = nomeOrgaoJulgador;
	}

	@XmlElement(name="votoPossuiConteudo")
	public Boolean getVotoPossuiConteudo() {
		return votoPossuiConteudo;
	}
	
	public void setVotoPossuiConteudo(Boolean votoPossuiConteudo) {
		this.votoPossuiConteudo = votoPossuiConteudo;
	}

	@XmlElement(name="acompanhaRelator")
	public Boolean getAcompanhaRelator() {
		return acompanhaRelator;
	}

	public void setAcompanhaRelator(Boolean acompanhaRelator) {
		this.acompanhaRelator = acompanhaRelator;
	}
	
}
