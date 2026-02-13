package br.jus.cnj.pje.webservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class InformacaoSessaoProcessoPlacar implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7862028559998134249L;
	
	private String tipoVoto;
	private Integer quantidade;
	private List<InformacaoSessaoProcessoVotante> votantes = new ArrayList<InformacaoSessaoProcessoVotante>(0);
	
	@XmlElement(name="tipoVoto")
	public String getTipoVoto() {
		return tipoVoto;
	}
	
	public void setTipoVoto(String tipoVoto) {
		this.tipoVoto = tipoVoto;
	}
	
	@XmlElement(name="quantidade")
	public Integer getQuantidade() {
		return quantidade;
	}
	
	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	@XmlElement(name="votantes")
	public List<InformacaoSessaoProcessoVotante> getVotantes() {
		return votantes;
	}

	public void setVotantes(List<InformacaoSessaoProcessoVotante> votantes) {
		this.votantes = votantes;
	}
	
}
