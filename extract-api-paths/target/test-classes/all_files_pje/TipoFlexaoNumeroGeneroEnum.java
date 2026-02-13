package br.jus.cnj.pje.util;


public enum TipoFlexaoNumeroGeneroEnum{
	SM("Singular Masculino"),
	SF("Singular Feminino"),
	PM("Plural Masculino"),
	PF("Plural Feminino");
	
	private String label;

	TipoFlexaoNumeroGeneroEnum(String label) {
		this.label = label;
	}
	public String getLabel() {
		return this.label;
	}
	
	public static TipoFlexaoNumeroGeneroEnum getByCaracteristica(boolean singular, boolean masculino){
		if (singular){
			if (masculino){
				return SM;
			}
			else{
				return SF;
			}
		}
		else{
			if (masculino){
				return PM;
			}
			else{
				return PF;
			}
		}
	}
}
