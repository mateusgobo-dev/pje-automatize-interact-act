package br.jus.pje.api.filter;

import java.io.Serializable;

public class ApiCondition implements Serializable{

	private static final long serialVersionUID = 1L;

	private ApiConditionOperatorEnum operator;
	private String value;
	
	public ApiCondition() {
		super();
	}

	public ApiCondition(String value, ApiConditionOperatorEnum operator) {
		super();
		this.value = value;
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ApiConditionOperatorEnum getOperator() {
		return operator;
	}

	public void setOperator(ApiConditionOperatorEnum operator) {
		this.operator = operator;
	}
	
}
