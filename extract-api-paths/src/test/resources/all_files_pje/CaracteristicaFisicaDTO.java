package br.jus.pje.nucleo.dto;

import java.io.Serializable;

public class CaracteristicaFisicaDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private String id;
	private String tipoCaracteristica;
	private String dsCaracteristica;

	public CaracteristicaFisicaDTO() {
		super();
	}

	public CaracteristicaFisicaDTO(String id, String tipoCaracteristica, String dsCaracteristica) {
		super();
		this.id = id;
		this.tipoCaracteristica = tipoCaracteristica;
		this.dsCaracteristica = dsCaracteristica;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getTipoCaracteristica() {
		return tipoCaracteristica;
	}

	public void setTipoCaracteristica(String tipoCaracteristica) {
		this.tipoCaracteristica = tipoCaracteristica;
	}

	public String getDsCaracteristica() {
		return dsCaracteristica;
	}

	public void setDsCaracteristica(String dsCaracteristica) {
		this.dsCaracteristica = dsCaracteristica;
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
		CaracteristicaFisicaDTO other = (CaracteristicaFisicaDTO) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
