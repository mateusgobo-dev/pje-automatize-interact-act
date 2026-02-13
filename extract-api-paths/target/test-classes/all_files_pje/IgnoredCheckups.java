package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tb_ignored_checkups")
/**
 * Entidade que representa os ids de checkups e erros ignorados, 
 * ou seja, que serão ignorados e no sero apresentados como erros.
 * 
 * @see CheckupError#getID()
 * @see CheckupWorker#getID()
 */
public class IgnoredCheckups implements Serializable{

	private static final long serialVersionUID = 1L;

	private String ignoredHash;

	public IgnoredCheckups() {}
	
	public IgnoredCheckups(String hash) {
		this.ignoredHash = hash;
	}

	@Id
	@Column(name = "ignored_hash", nullable = false, updatable = false, unique = true)
	public String getIgnoredHash(){
		return ignoredHash;
	}

	public void setIgnoredHash(String ignoredHash) {
		this.ignoredHash = ignoredHash;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((ignoredHash == null) ? 0 : ignoredHash.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IgnoredCheckups))
			return false;
		IgnoredCheckups other = (IgnoredCheckups) obj;
		if (getIgnoredHash() == null) {
			if (other.getIgnoredHash() != null)
				return false;
		} else if (!getIgnoredHash().equals(other.getIgnoredHash()))
			return false;
		return true;
	}

}
