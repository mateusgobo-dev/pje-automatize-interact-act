package br.jus.pje.nucleo.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class FugaDTO implements Serializable{
	
	
	private static final long serialVersionUID = 1L;

	private String id;
	private String dsFuga;
	private Date dtFuga;
	private UnidadePrisionalDTO unidadePrisional;
	
	public FugaDTO() {
		super();
	}

	public FugaDTO(String id, String dsFuga, Date dtFuga, UnidadePrisionalDTO unidadePrisional) {
		super();
		if(id == null){
			this.id = UUID.randomUUID().toString();
		} else {
			this.id = id;			
		}
		this.dsFuga = dsFuga;
		this.dtFuga = dtFuga;
		this.unidadePrisional = unidadePrisional;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getDsFuga() {
		return dsFuga;
	}

	public void setDsFuga(String dsFuga) {
		this.dsFuga = dsFuga;
	}

	public Date getDtFuga() {
		return dtFuga;
	}

	public void setDtFuga(Date dtFuga) {
		this.dtFuga = dtFuga;
	}

	public UnidadePrisionalDTO getUnidadePrisional() {
		return unidadePrisional;
	}

	public void setUnidadePrisional(UnidadePrisionalDTO unidadePrisional) {
		this.unidadePrisional = unidadePrisional;
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
		FugaDTO other = (FugaDTO) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
