package br.jus.pje.nucleo.enums;

/**
 * @author everton
 */
public enum TipoParametroFiltroDinamico {
	STR("String"),
	INT("Inteiro"),
	DAT("Data"),
	ENT("Entidade");
	
	private TipoParametroFiltroDinamico(String label){
		this.label = label;
	}
	
	private String label;
	
	public String getLabel(){
		return this.label;
	}
}
