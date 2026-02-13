package br.jus.pje.api.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApiFilter implements Serializable{

	private static final long serialVersionUID = 1L;

	private List<ApiCriteria> criterias = new ArrayList<>();;

	public ApiFilter() {
		super();
	}

	public ApiFilter(List<ApiCriteria> criterias) {
		super();
		this.criterias = criterias;
	}

	public List<ApiCriteria> getCriterias() {
		return criterias;
	}

	public void setCriterias(List<ApiCriteria> criterias) {
		this.criterias = criterias;
	}
	
	public void addCriteria(ApiCriteria criteria) {
		this.criterias.add(criteria);
	}
	
}
