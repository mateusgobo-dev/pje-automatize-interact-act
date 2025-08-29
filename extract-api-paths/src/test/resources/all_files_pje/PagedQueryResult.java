package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PagedQueryResult<E> implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long count;
	
	private List<E> entities = new ArrayList<E>();

	public PagedQueryResult(Long count, List<E> entities) {
		this.count = count;
		this.entities = entities;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public List<E> getEntities() {
		return entities;
	}

	public void setEntities(List<E> entities) {
		this.entities = entities;
	}
}