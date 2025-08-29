package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;
import java.util.List;

public class AplicarFiltroDTO implements Serializable{

	private static final long serialVersionUID = 1L;
	private Integer idEtiqueta;
	private List<Integer> idsFiltros;
	
	public Integer getIdEtiqueta() {
		return idEtiqueta;
	}
	public void setIdEtiqueta(Integer idEtiqueta) {
		this.idEtiqueta = idEtiqueta;
	}
	public List<Integer> getIdsFiltros() {
		return idsFiltros;
	}
	public void setIdsFiltros(List<Integer> idsFiltros) {
		this.idsFiltros = idsFiltros;
	}
	
	
}
