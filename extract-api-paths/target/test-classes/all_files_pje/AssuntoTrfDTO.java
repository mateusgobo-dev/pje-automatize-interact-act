package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import br.jus.pje.nucleo.entidades.ProcessoAssunto;

public class AssuntoTrfDTO {

	private int idAssuntoTrf;
	private String codAssuntoTrf;
	private String assuntoTrf;
	
	public AssuntoTrfDTO() {
		super();
	}
	
	public AssuntoTrfDTO(int idAssuntoTrf, String codAssuntoTrf, String assuntoTrf) {
		super();
		this.idAssuntoTrf = idAssuntoTrf;
		this.codAssuntoTrf = codAssuntoTrf;
		this.assuntoTrf = assuntoTrf;
	}

	public AssuntoTrfDTO(ProcessoAssunto processoAssunto) {
		this.assuntoTrf = processoAssunto.getAssuntoTrf().getAssuntoTrf();
		this.codAssuntoTrf = processoAssunto.getAssuntoTrf().getCodAssuntoTrf();
	}
	
	public int getIdAssuntoTrf() {
		return idAssuntoTrf;
	}
	
	public void setIdAssuntoTrf(int idAssuntoTrf) {
		this.idAssuntoTrf = idAssuntoTrf;
	}
	
	public String getCodAssuntoTrf() {
		return codAssuntoTrf;
	}
	
	public void setCodAssuntoTrf(String codAssuntoTrf) {
		this.codAssuntoTrf = codAssuntoTrf;
	}
	
	public String getAssuntoTrf() {
		return assuntoTrf;
	}
	
	public void setAssuntoTrf(String assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}
	
}
