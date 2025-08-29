package br.jus.pje.nucleo.beans.criminal;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import br.jus.pje.nucleo.dto.DispositivoDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IncidenciaPenalBean extends BaseBean{

	private static final long serialVersionUID = 1L;

	private DispositivoDTO dispositivo;
	private Date dtFato;
	private Date dtPrescricao;

	public IncidenciaPenalBean() {
		super();
	}
	
	public IncidenciaPenalBean(String id, DispositivoDTO dispositivo, Date dtFato, Date dtPrescricao) {
		super(id);
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
		IncidenciaPenalBean other = (IncidenciaPenalBean) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
