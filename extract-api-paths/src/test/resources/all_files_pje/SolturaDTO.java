package br.jus.pje.nucleo.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class SolturaDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private String id;
	private Date dtSoltura;
	private MotivoSolturaDTO motivoSoltura;
	private UnidadePrisionalDTO unidadePrisional;

	public SolturaDTO() {
		super();
	}

	public SolturaDTO(String id, Date dtSoltura, MotivoSolturaDTO motivoSoltura, UnidadePrisionalDTO unidadePrisional) {
		super();
		if(id == null){
			this.id = UUID.randomUUID().toString();
		} else {
			this.id = id;			
		}
		this.dtSoltura = dtSoltura;
		this.motivoSoltura = motivoSoltura;
		this.unidadePrisional = unidadePrisional;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public Date getDtSoltura() {
		return dtSoltura;
	}

	public void setDtSoltura(Date dtSoltura) {
		this.dtSoltura = dtSoltura;
	}

	public MotivoSolturaDTO getMotivoSoltura() {
		return motivoSoltura;
	}

	public void setMotivoSoltura(MotivoSolturaDTO motivoSoltura) {
		this.motivoSoltura = motivoSoltura;
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
		SolturaDTO other = (SolturaDTO) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
