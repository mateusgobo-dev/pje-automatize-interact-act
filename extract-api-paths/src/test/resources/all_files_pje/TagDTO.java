package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.jus.pje.nucleo.entidades.Tag;
import br.jus.pje.nucleo.entidades.TagMin;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TagDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private Integer id;
	private String nomeTag;
	private String nomeTagCompleto;
	private TagDTO pai;
	private Integer idPai;
	private Boolean possuiFilhos;
	private Integer qtdeProcessos;
	
	private Boolean favorita;
	
	public TagDTO(TagMin tagMin) {
		this.id = tagMin.getId();
		this.nomeTag = tagMin.getNomeTag();
		this.nomeTagCompleto = tagMin.getNomeTagCompleto();
		if ( tagMin.getIdTagPai()!=null ) {
			this.pai = new TagDTO(tagMin.getIdTagPai());
		}
	}
	
	public TagDTO(Tag tag) {
		this.id = tag.getId();
		this.nomeTag = tag.getNomeTag();
		this.nomeTagCompleto = tag.getNomeTagCompleto();
		
		if ( tag.getPai()!=null ) {
			this.pai = new TagDTO(tag.getPai());
		}
	}
	
	public TagDTO(Integer id, String nomeTag, String nomeTagCompleto, Integer idPai) {
		super();
		this.id = id;
		this.nomeTag = nomeTag;
		this.nomeTagCompleto = nomeTagCompleto;
		this.idPai = idPai;
	}

	public TagDTO(Integer id) {
		this.id = id;
	}
	
	public TagDTO() {
		super();
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getNomeTag() {
		return nomeTag;
	}
	
	public void setNomeTag(String nomeTag) {
		this.nomeTag = nomeTag;
	}
	
	public String getNomeTagCompleto() {
		return nomeTagCompleto;
	}
	
	public void setNomeTagCompleto(String nomeTagCompleto) {
		this.nomeTagCompleto = nomeTagCompleto;
	}
	
	public TagDTO getPai() {
		return pai;
	}
	
	public void setPai(TagDTO pai) {
		this.pai = pai;
	}
	
	public Integer getIdPai() {
		return idPai;
	}
	
	public void setIdPai(Integer idPai) {
		this.idPai = idPai;
	}
	
	public Boolean getFavorita() {
		return favorita;
	}
	
	public void setFavorita(Boolean favorita) {
		this.favorita = favorita;
	}

	public Boolean getPossuiFilhos() {
		return possuiFilhos;
	}

	public void setPossuiFilhos(Boolean possuiFilhos) {
		this.possuiFilhos = possuiFilhos;
	}

	public Integer getQtdeProcessos() {
		return qtdeProcessos;
	}

	public void setQtdeProcessos(Integer qtdeProcessos) {
		this.qtdeProcessos = qtdeProcessos;
	}

}
