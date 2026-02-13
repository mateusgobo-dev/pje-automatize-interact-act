/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "tb_locks")
public class Lock implements Serializable{

	private static final long serialVersionUID = 1L;

	private String codigo;
	private long version;

	@Id
	@Column(name = "cd_lock", nullable = false, updatable = false)
	public String getCodigo(){
		return codigo;
	}

	public void setCodigo(String codigo){
		this.codigo = codigo;
	}

	@Version
	@Column(name = "version")
	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getCodigo() == null) ? 0 : getCodigo().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof Lock)
			return false;
		Lock other = (Lock) obj;
		if (getCodigo() == null){
			if (other.getCodigo() != null)
				return false;
		}
		else if (!getCodigo().equals(other.getCodigo()))
			return false;
		return true;
	}

}
