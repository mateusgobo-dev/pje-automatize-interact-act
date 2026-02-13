package br.jus.pje.nucleo.enums;

public enum TipoDecisaoPublicacaoEnum {

	/**
	 * Tipo de decisao colegiada
	 */
	COLEGIADA("Colegiada"), 

	/**
	 * Tipo de decisao monocratica
	 */
	MONOCRATICA("Monocrática");
	
	private String label;

	TipoDecisaoPublicacaoEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}
	
	public boolean isMonocratica(){
		return MONOCRATICA.equals(this);
	}
	
	public boolean isColegiada(){
		return COLEGIADA.equals(this);
	}
}