package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import br.jus.pje.nucleo.entidades.Filtro;

public class FiltroDTO {
	
	private Integer id;
	private String nomeFiltro;
	private Integer idLocalizacao;
	private Integer idTagHerdado;
	
	public FiltroDTO() {
		super();
	}
	
	public FiltroDTO(Integer id, String nomeFiltro, Integer idLocalizacao, Integer idTagHerdado) {
		super();
		this.id = id;
		this.nomeFiltro = nomeFiltro;
		this.idLocalizacao = idLocalizacao;
		this.idTagHerdado = idTagHerdado;
	}
	
	public FiltroDTO(Filtro filtro){
		super();
		this.id = filtro.getId();
		this.nomeFiltro = filtro.getNomeFiltro();
		this.idLocalizacao = filtro.getIdLocalizacao();
	}

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getNomeFiltro() {
		return nomeFiltro;
	}
	public void setNomeFiltro(String nomeFiltro) {
		this.nomeFiltro = nomeFiltro;
	}
	public Integer getIdLocalizacao() {
		return idLocalizacao;
	}
	public void setIdLocalizacao(Integer idLocalizacao) {
		this.idLocalizacao = idLocalizacao;
	}

	public Integer getIdTagHerdado() {
		return idTagHerdado;
	}

	public void setIdTagHerdado(Integer idTagHerdado) {
		this.idTagHerdado = idTagHerdado;
	}
}
