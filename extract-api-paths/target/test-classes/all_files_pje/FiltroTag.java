package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tb_filtro_tag")
public class FiltroTag implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id_filtro")
	private Integer idFiltro;
	
	@Id
	@Column(name = "id_tag")
	private Integer idTag;
	
	@Column(name = "id_tag_herdado")
	private Integer idTagHerdado;
	
	public FiltroTag() {
		super();
	}

	public FiltroTag(Integer idFiltro, Integer idTag) {
		super();
		this.idFiltro = idFiltro;
		this.idTag = idTag;
	}
	
	public Integer getIdFiltro() {
		return idFiltro;
	}
	
	public void setIdFiltro(Integer idFiltro) {
		this.idFiltro = idFiltro;
	}
	
	public Integer getIdTag() {
		return idTag;
	}
	
	public void setIdTag(Integer idTag) {
		this.idTag = idTag;
	}

	public Integer getIdTagHerdado() {
		return idTagHerdado;
	}

	public void setIdTagHerdado(Integer idTagHerdado) {
		this.idTagHerdado = idTagHerdado;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idFiltro == null) ? 0 : idFiltro.hashCode());
		result = prime * result + ((idTag == null) ? 0 : idTag.hashCode());
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
		FiltroTag other = (FiltroTag) obj;
		if (idFiltro == null) {
			if (other.idFiltro != null)
				return false;
		} else if (!idFiltro.equals(other.idFiltro))
			return false;
		if (idTag == null) {
			if (other.idTag != null)
				return false;
		} else if (!idTag.equals(other.idTag))
			return false;
		return true;
	}
	
}
