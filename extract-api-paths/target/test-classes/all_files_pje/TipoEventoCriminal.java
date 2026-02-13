package br.jus.pje.nucleo.beans.criminal;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TipoEventoCriminal implements Serializable{
	private static final long serialVersionUID = 5121713171560504947L;
	private Integer id;
	private TipoEventoCriminalEnum codTipoIc;
	private String descricao;
	private Boolean ativo;
	private Boolean exigeTipificacaoDelito;
	private TipoProcessoEnum tipoProcesso;

	public TipoEventoCriminal(TipoEventoCriminalEnum codTipoIc) {
		super();
		this.codTipoIc = codTipoIc;
	}

	public TipoEventoCriminal() {

	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public TipoEventoCriminalEnum getCodTipoIc() {
		return codTipoIc;
	}

	public void setCodTipoIc(TipoEventoCriminalEnum codTipoIc) {
		this.codTipoIc = codTipoIc;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public Boolean getExigeTipificacaoDelito() {
		return exigeTipificacaoDelito;
	}

	public void setExigeTipificacaoDelito(Boolean exigeTipificacaoDelito) {
		this.exigeTipificacaoDelito = exigeTipificacaoDelito;
	}

	public TipoProcessoEnum getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcessoEnum tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

}
