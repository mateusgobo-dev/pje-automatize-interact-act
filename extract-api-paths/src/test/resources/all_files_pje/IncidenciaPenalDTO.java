package br.jus.pje.nucleo.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class IncidenciaPenalDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private String id;
	private DispositivoDTO dispositivo;
	private Date dtFato;
	private Date dtPrescricao;

	public IncidenciaPenalDTO() {
		super();
	}
	
	public IncidenciaPenalDTO(String id, DispositivoDTO dispositivo, Date dtFato, Date dtPrescricao) {
		super();
		if(id == null){
			this.id = UUID.randomUUID().toString();
		} else {
			this.id = id;			
		}
		this.dispositivo = dispositivo;
		this.dtFato = dtFato;
		this.dtPrescricao = dtPrescricao;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public DispositivoDTO getDispositivo() {
		return dispositivo;
	}

	public void setDispositivo(DispositivoDTO dispositivo) {
		this.dispositivo = dispositivo;
	}

	public Date getDtFato() {
		return dtFato;
	}

	public void setDtFato(Date dtFato) {
		this.dtFato = dtFato;
	}

	public Date getDtPrescricao() {
		return dtPrescricao;
	}

	public void setDtPrescricao(Date dtPrescricao) {
		this.dtPrescricao = dtPrescricao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dispositivo == null) ? 0 : dispositivo.hashCode());
		result = prime * result + ((dtFato == null) ? 0 : dtFato.hashCode());
		result = prime * result + ((dtPrescricao == null) ? 0 : dtPrescricao.hashCode());
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
		IncidenciaPenalDTO other = (IncidenciaPenalDTO) obj;
		if (dispositivo == null) {
			if (other.dispositivo != null)
				return false;
		} else if (!dispositivo.equals(other.dispositivo))
			return false;
		if (dtFato == null) {
			if (other.dtFato != null)
				return false;
		} else if (!dtFato.equals(other.dtFato))
			return false;
		if (dtPrescricao == null) {
			if (other.dtPrescricao != null)
				return false;
		} else if (!dtPrescricao.equals(other.dtPrescricao))
			return false;
		return true;
	}
	
	

}
