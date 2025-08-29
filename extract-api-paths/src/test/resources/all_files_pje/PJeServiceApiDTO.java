package br.jus.pje.nucleo.dto;

import java.io.Serializable;

public abstract class PJeServiceApiDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract Integer getId();

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PJeServiceApiDTO other = (PJeServiceApiDTO) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		if (getId() != null) {
			return getId().hashCode();
		} else {
			return super.hashCode();
		}
	}
}
