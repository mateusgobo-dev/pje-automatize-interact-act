package br.jus.cnj.pje.status;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class Health implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private Status status;
	private Map<String, Object> details;
	
	public Health() {
		super();
	}

	public Health(Status status, Map<String, Object> details) {
		super();
		this.status = status;
		this.details = details;
	}
	
	@JsonUnwrapped
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	@JsonUnwrapped
	public Map<String, Object> getDetails() {
		return details;
	}

	public void setDetails(Map<String, Object> details) {
		this.details = details;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((details == null) ? 0 : details.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Health other = (Health) obj;
		if (details == null) {
			if (other.details != null)
				return false;
		} else if (!details.equals(other.details))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		return true;
	}

}
