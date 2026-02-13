package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

public class TipoSessaoDTO {

	private Integer idTipoSessao;
	private String tipoSessao;
	private Boolean ativo;
	
	public TipoSessaoDTO() {
		super();
	}
	
	public TipoSessaoDTO(Integer idTipoSessao, String tipoSessao, Boolean ativo) {
		super();
		this.idTipoSessao = idTipoSessao;
		this.tipoSessao = tipoSessao;
		this.ativo = ativo;
	}
	
	public Integer getIdTipoSessao() {
		return idTipoSessao;
	}
	
	public void setIdTipoSessao(Integer idTipoSessao) {
		this.idTipoSessao = idTipoSessao;
	}
	
	public String getTipoSessao() {
		return tipoSessao;
	}
	
	public void setTipoSessao(String tipoSessao) {
		this.tipoSessao = tipoSessao;
	}
	
	public Boolean getAtivo() {
		return ativo;
	}
	
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}	
}
