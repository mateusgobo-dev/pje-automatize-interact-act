package br.jus.pje.nucleo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MedidaSocioeducativaDTO extends PJeServiceApiDTO {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String cdMedidaSocioeducativa;
	private String dsMedidaSocioeducativa;
	private Boolean ativo = Boolean.TRUE;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getCdMedidaSocioeducativa() {
		return cdMedidaSocioeducativa;
	}
	public void setCdMedidaSocioeducativa(String cdMedidaSocioeducativa) {
		this.cdMedidaSocioeducativa = cdMedidaSocioeducativa;
	}
	public String getDsMedidaSocioeducativa() {
		return dsMedidaSocioeducativa;
	}
	public void setDsMedidaSocioeducativa(String dsMedidaSocioeducativa) {
		this.dsMedidaSocioeducativa = dsMedidaSocioeducativa;
	}
	public Boolean getAtivo() {
		return ativo;
	}
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ativo == null) ? 0 : ativo.hashCode());
		result = prime * result + ((cdMedidaSocioeducativa == null) ? 0 : cdMedidaSocioeducativa.hashCode());
		result = prime * result + ((dsMedidaSocioeducativa == null) ? 0 : dsMedidaSocioeducativa.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MedidaSocioeducativaDTO other = (MedidaSocioeducativaDTO) obj;
		if(!isAtivo(other)) {
			return false;
		}
		if(!isCdMedidaSocioeducativa(other)) {
			return false;
		}		
		if (dsMedidaSocioeducativa == null) {
			if (other.dsMedidaSocioeducativa != null)
				return false;
		} else if (!dsMedidaSocioeducativa.equals(other.dsMedidaSocioeducativa))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
		
	private boolean isAtivo(MedidaSocioeducativaDTO other) {
		if (ativo == null) {
			if (other.ativo != null)
				return false;
		} 
		else if (!ativo.equals(other.ativo))
			return false;
		
		return true;
	}
	private boolean isCdMedidaSocioeducativa(MedidaSocioeducativaDTO other) {
		if (cdMedidaSocioeducativa == null) {
			if (other.cdMedidaSocioeducativa != null)
				return false;
		} else if (!cdMedidaSocioeducativa.equals(other.cdMedidaSocioeducativa))
			return false;
		
		return true;		
	}
	
}
