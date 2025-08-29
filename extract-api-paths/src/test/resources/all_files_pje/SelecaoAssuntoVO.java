package br.jus.cnj.pje.entidades.vo;

import br.jus.pje.nucleo.entidades.AssuntoTrf;

public class SelecaoAssuntoVO {

	private Boolean selecionado;
	private AssuntoTrf assuntoTrf;
	private Boolean assuntoPrincipal;
	
	public Boolean getSelecionado() {
		return selecionado;
	}
	
	public void setSelecionado(Boolean selecionado) {
		this.selecionado = selecionado;
	}
	
	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}
	
	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public Boolean getAssuntoPrincipal() {
		return assuntoPrincipal;
	}

	public void setAssuntoPrincipal(Boolean assuntoPrincipal) {
		this.assuntoPrincipal = assuntoPrincipal;
	}
}
