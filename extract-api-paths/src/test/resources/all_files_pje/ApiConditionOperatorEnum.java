package br.jus.pje.api.filter;

public enum ApiConditionOperatorEnum {

	EQ("eq"),
	LT("lt"),
	GT("gt"),
	LE("le"),
	GE("ge"),
	IN("in"),
	STARTS_WITH("starts-with"),
	ENDS_WITH("ends-with"),
	CONTAINS("contains"),
	NOT("not");
	
	private String label;
	
	ApiConditionOperatorEnum(String label){
		this.label = label;
	}
	
	public String getLabel() {
		return this.label;
	}	
}
