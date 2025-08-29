package br.jus.cnj.pje.webservice;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class InformacaoVotosResposta implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7044959423391988062L;
	
	private Integer idProcesso;
	private Integer idSessao;
	private InformacaoSessaoProcessoDocumentoVoto voto = new InformacaoSessaoProcessoDocumentoVoto();
	
	@XmlElement(name="idProcesso")
	public Integer getIdProcesso() {
		return idProcesso;
	}
	
	public void setIdProcesso(Integer idProcesso) {
		this.idProcesso = idProcesso;
	}
	
	@XmlElement(name="idSessao")
	public Integer getIdSessao() {
		return idSessao;
	}
	
	public void setIdSessao(Integer idSessao) {
		this.idSessao = idSessao;
	}

	@XmlElement(name="voto")
	public InformacaoSessaoProcessoDocumentoVoto getVoto() {
		return voto;
	}

	public void setVoto(InformacaoSessaoProcessoDocumentoVoto voto) {
		this.voto = voto;
	}
}