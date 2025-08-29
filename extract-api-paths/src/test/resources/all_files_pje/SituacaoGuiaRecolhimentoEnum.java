package br.jus.pje.nucleo.enums;

public enum SituacaoGuiaRecolhimentoEnum {

	NG("Guia de recolhimento não gerada", "fa fa-money-check-alt fa-lg text-danger"),
	NV("Guia de recolhimento não validada", "fa fa-money-check-alt fa-lg text-danger"),
	GG("Guia de recolhimento gerada", "fa fa-usd fa-lg text-info"),
	GA("Guia de recolhimento se encontra aberta", "fa fa-hand-holding-usd fa-lg text-info"),
	GP("Todas as guias geradas foram pagas", "fa fa-usd fa-lg text-success"),
	MI("Processo marcado com isenção de guia", "fa fa-tags fa-lg text-info");

	private String label;

	private String icone;

	SituacaoGuiaRecolhimentoEnum(String label, String icone) {
		this.label = label;
		this.icone = icone;
	}

	public String getLabel() {
		return this.label;
	}

	public String getIcone() {
		return icone;
	}

	public void setIcone(String icone) {
		this.icone = icone;
	}
}