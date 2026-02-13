package br.jus.pje.api.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApiCriteria implements Serializable{

	private static final long serialVersionUID = 1L;

	private String field;
	private List<ApiCondition> conditions = new ArrayList<>();
	
	public ApiCriteria(String field, List<ApiCondition> conditions) {
		super();
		this.field = field;
		this.conditions = conditions;
	}

	public ApiCriteria() {
		super();
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public List<ApiCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<ApiCondition> conditions) {
		this.conditions = conditions;
	}
	
	public void addCondition(ApiCondition condition) {
		this.conditions.add(condition);
	}
	
}
