package br.jus.cnj.pje.indexer;

import java.io.Serializable;

@SuppressWarnings("rawtypes")
public class IndexVO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Object id;
	private Class clazz;
	
	public IndexVO() { }
	
	public IndexVO(Object id, Class clazz) {
		this.id = id;
		this.clazz = clazz;
	}	
	
	public Object getId() {
		return id;
	}
	
	public void setId(Object id) {
		this.id = id;
	}
	
	public Class getClazz() {
		return clazz;
	}
	
	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}
}
