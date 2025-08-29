package br.jus.pje.nucleo.dto;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.TipoParte;

public class TipoParteDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Integer idTipoParte;
	private String tipoParte;
	private Boolean ativo;
	private Boolean tipoPrincipal;
	
	public TipoParteDTO() {
		super();
	}
	
	public TipoParteDTO(Integer idTipoParte, String tipoParte, Boolean ativo, Boolean tipoPrincipal) {
		super();
		this.idTipoParte = idTipoParte;
		this.tipoParte = tipoParte;
		this.ativo = ativo;
		this.tipoPrincipal = tipoPrincipal;
	}

	public TipoParteDTO(TipoParte tipoParte) {		
		this.idTipoParte = tipoParte.getIdTipoParte();
		this.tipoParte = tipoParte.getTipoParte();
		this.ativo = tipoParte.getAtivo();
		this.tipoPrincipal = tipoParte.getTipoPrincipal();
	}
	
	public Integer getIdTipoParte() {
		return idTipoParte;
	}
	
	public void setIdTipoParte(Integer idTipoParte) {
		this.idTipoParte = idTipoParte;
	}
	
	public String getTipoParte() {
		return tipoParte;
	}
	
	public void setTipoParte(String tipoParte) {
		this.tipoParte = tipoParte;
	}
	
	public Boolean getAtivo() {
		return ativo;
	}
	
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	public Boolean getTipoPrincipal() {
		return tipoPrincipal;
	}
	
	public void setTipoPrincipal(Boolean tipoPrincipal) {
		this.tipoPrincipal = tipoPrincipal;
	}
}
