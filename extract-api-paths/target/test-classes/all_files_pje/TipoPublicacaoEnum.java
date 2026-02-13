package br.jus.pje.nucleo.enums;

public enum TipoPublicacaoEnum {

	/**
	 * Tipo de publicação mural  
	 */
	MURAL("Em Mural"), 

	/**
	 * Tipo de publicaçãoo sessão 
	 */
	SESSAO("Em Sessão");
	
	private String label;

	TipoPublicacaoEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}
	
	public boolean isMural(){
		return MURAL.equals(this);
	}
	
	public boolean isSessao(){
		return SESSAO.equals(this);
	}
}