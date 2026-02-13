package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;

public class PrioridadeProcessoDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private Integer idPrioridadeProcesso;
	private String nomePrioridadeProcesso;
	
	public PrioridadeProcessoDTO() {
		super();
	}
	
	public PrioridadeProcessoDTO(Integer idPrioridadeProcesso, String nomePrioridadeProcesso) {
		super();
		this.idPrioridadeProcesso = idPrioridadeProcesso;
		this.nomePrioridadeProcesso = nomePrioridadeProcesso;
	}
	
	public Integer getIdPrioridadeProcesso() {
		return idPrioridadeProcesso;
	}
	
	public void setIdPrioridadeProcesso(Integer idPrioridadeProcesso) {
		this.idPrioridadeProcesso = idPrioridadeProcesso;
	}
	
	public String getNomePrioridadeProcesso() {
		return nomePrioridadeProcesso;
	}
	
	public void setNomePrioridadeProcesso(String nomePrioridadeProcesso) {
		this.nomePrioridadeProcesso = nomePrioridadeProcesso;
	}
	
}
