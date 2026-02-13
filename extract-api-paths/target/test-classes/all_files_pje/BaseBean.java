package br.jus.pje.nucleo.beans.criminal;

import java.io.Serializable;
import java.util.UUID;

public abstract class BaseBean implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected String id;
	
	public abstract String getId();
	
	public abstract void setId(String id);
	
	public BaseBean(String id) {
		super();
		if(id == null) {
			this.id = UUID.randomUUID().toString();
		} else {
			this.id = id;
		}
	}
	
	public BaseBean() {
		super();
		this.id = UUID.randomUUID().toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		BaseBean other = (BaseBean) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}
