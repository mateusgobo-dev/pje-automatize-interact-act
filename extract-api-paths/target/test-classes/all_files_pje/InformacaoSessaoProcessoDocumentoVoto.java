package br.jus.cnj.pje.webservice;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class InformacaoSessaoProcessoDocumentoVoto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4526616063308233270L;
	
	private Integer idSessaoProcessoDocumentoVoto;
	private String conteudoVoto;
	private String orgaoJulgadorVoto;
	private Integer idOrgaoJulgador;
	private List<InformacaoSessaoProcessoDocumento> listaInformacaoSessaoProcessoDocumento;
	
	@XmlElement(name="idVoto")
	public Integer getIdSessaoProcessoDocumentoVoto() {
		return idSessaoProcessoDocumentoVoto;
	}
	
	public void setIdSessaoProcessoDocumentoVoto(Integer idSessaoProcessoDocumentoVoto) {
		this.idSessaoProcessoDocumentoVoto = idSessaoProcessoDocumentoVoto;
	}
	
	@XmlElement(name="conteudoVoto")
	public String getConteudoVoto() {
		return conteudoVoto;
	}
	
	public void setConteudoVoto(String conteudoVoto) {
		this.conteudoVoto = conteudoVoto;
	}

	@XmlElement(name="orgaoJulgadorVoto")
	public String getOrgaoJulgadorVoto() {
		return orgaoJulgadorVoto;
	}

	public void setOrgaoJulgadorVoto(String orgaoJulgadorVoto) {
		this.orgaoJulgadorVoto = orgaoJulgadorVoto;
	}
	
	@XmlElement(name="idOrgaoJulgador")
	public Integer getIdOrgaoJulgador() {
		return idOrgaoJulgador;
	}

	public void setIdOrgaoJulgador(Integer idOrgaoJulgador) {
		this.idOrgaoJulgador = idOrgaoJulgador;
	}

	public List<InformacaoSessaoProcessoDocumento> getListaInformacaoSessaoProcessoDocumento() {
		return listaInformacaoSessaoProcessoDocumento;
	}

	@XmlElement(name="outrosDocumentos")
	public void setListaInformacaoSessaoProcessoDocumento(
			List<InformacaoSessaoProcessoDocumento> listaInformacaoSessaoProcessoDocumento) {
		this.listaInformacaoSessaoProcessoDocumento = listaInformacaoSessaoProcessoDocumento;
	}
	
}
