package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import br.jus.pje.nucleo.entidades.Jurisdicao;

public class JurisdicaoDTO {
	
	private int idJurisdicao;
	private String jurisdicao;
	
	public JurisdicaoDTO() {
		super();
	}
	
	public JurisdicaoDTO(int idJurisdicao, String jurisdicao) {
		super();
		this.idJurisdicao = idJurisdicao;
		this.jurisdicao = jurisdicao;
	}

	public JurisdicaoDTO(Jurisdicao jurisdicao){
		super();
		this.idJurisdicao = jurisdicao.getIdJurisdicao();
		this.jurisdicao = jurisdicao.getJurisdicao();
	}
	
	public int getIdJurisdicao() {
		return idJurisdicao;
	}
	public void setIdJurisdicao(int idJurisdicao) {
		this.idJurisdicao = idJurisdicao;
	}
	public String getJurisdicao() {
		return jurisdicao;
	}
	public void setJurisdicao(String jurisdicao) {
		this.jurisdicao = jurisdicao;
	}
	
	
}
