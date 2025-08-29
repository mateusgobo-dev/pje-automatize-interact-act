package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import br.jus.pje.nucleo.entidades.CriterioFiltro;
import br.jus.pje.nucleo.enums.TipoCriterioEnum;

public class CriterioFiltroDTO {

	private Integer id;
	private TipoCriterioEnum tipoCriterio;
	//private String tipoCriterioLabel;
	private String textoCriterio;
	private String valorCriterio;
	private Integer idFiltro;
	
	public CriterioFiltroDTO() {
		super();
	}
	
	public CriterioFiltroDTO(Integer id, TipoCriterioEnum tipoCriterio, String valorCriterio, String textoCriterio, Integer idFiltro) {
		super();
		this.id = id;
		this.tipoCriterio = tipoCriterio;
		this.valorCriterio = valorCriterio;
		this.textoCriterio = textoCriterio;
		this.idFiltro = idFiltro;
	}
	
	public CriterioFiltroDTO(CriterioFiltro criterioFiltro) {
		super();
		this.id = criterioFiltro.getId();
		this.tipoCriterio = criterioFiltro.getTipoCriterio();
		this.valorCriterio = criterioFiltro.getValorCriterio();
		this.textoCriterio = criterioFiltro.getTextoCriterio();
		this.idFiltro = criterioFiltro.getFiltro().getId();
	}	

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public TipoCriterioEnum getTipoCriterio() {
		return tipoCriterio;
	}
	
	public void setTipoCriterio(TipoCriterioEnum tipoCriterio) {
		this.tipoCriterio = tipoCriterio;
	}
	
	public String getValorCriterio() {
		return valorCriterio;
	}
	
	public void setValorCriterio(String valorCriterio) {
		this.valorCriterio = valorCriterio;
	}
	
	public Integer getIdFiltro() {
		return idFiltro;
	}
	
	public void setIdFiltro(Integer idFiltro) {
		this.idFiltro = idFiltro;
	}
	
	public String getTipoCriterioLabel() {
		return this.tipoCriterio.getLabel();
	}

	public String getTextoCriterio() {
		return textoCriterio;
	}

	public void setTextoCriterio(String textoCriterio) {
		this.textoCriterio = textoCriterio;
	}

}
