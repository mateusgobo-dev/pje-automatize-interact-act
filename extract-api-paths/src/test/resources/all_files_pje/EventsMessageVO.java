package org.jboss.seam.core;

import java.io.Serializable;

public class EventsMessageVO implements Serializable {
	private static final long serialVersionUID = -973152986694308506L;
	private String type;
	private Object[] parameters;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public Object[] getParameters() {
		return parameters;
	}
	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}
}
