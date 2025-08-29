package br.jus.pje.nucleo.beans.criminal;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FugaBean extends BaseBean{
	
	private static final long serialVersionUID = 1L;

	private String dsFuga;
	private Date dtFuga;
	private UnidadePrisionalBean unidadePrisional;
	
	public FugaBean() {
		super();
	}

	public FugaBean(String id, String dsFuga, Date dtFuga, UnidadePrisionalBean unidadePrisional) {
		super(id);
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

	public UnidadePrisionalBean getUnidadePrisional() {
		return unidadePrisional;
	}

	public void setUnidadePrisional(UnidadePrisionalBean unidadePrisional) {
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
		FugaBean other = (FugaBean) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
