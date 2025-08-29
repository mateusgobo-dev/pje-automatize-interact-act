package br.jus.cnj.pje.webservice.controller.status.dto;

import java.io.Serializable;

public class InfoDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private String version;
	
	private String tribunal;
	
	private String instancia;
	
	private String tipoJustica;

	public InfoDTO() {
		super();
	}

	public InfoDTO(String version, String tribunal, String instancia, String tipoJustica) {
		super();
		this.version = version;
		this.tribunal = tribunal;
		this.instancia = instancia;
		this.tipoJustica = tipoJustica;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTribunal() {
		return tribunal;
	}

	public void setTribunal(String tribunal) {
		this.tribunal = tribunal;
	}

	public String getInstancia() {
		return instancia;
	}

	public void setInstancia(String instancia) {
		this.instancia = instancia;
	}
	
	public String getTipoJustica() {
		return tipoJustica;
	}
	
	public void setTipoJustica(String tipoJustica) {
		this.tipoJustica = tipoJustica;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((instancia == null) ? 0 : instancia.hashCode());
		result = prime * result + ((tipoJustica == null) ? 0 : tipoJustica.hashCode());
		result = prime * result + ((tribunal == null) ? 0 : tribunal.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		InfoDTO other = (InfoDTO) obj;
		if (instancia == null) {
			if (other.instancia != null) {
				return false;
			}
		} else if (!instancia.equals(other.instancia)) {
			return false;
		}
		if (tipoJustica == null) {
			if (other.tipoJustica != null) {
				return false;
			}
		} else if (!tipoJustica.equals(other.tipoJustica)) {
			return false;
		}
		if (tribunal == null) {
			if (other.tribunal != null) {
				return false;
			}
		} else if (!tribunal.equals(other.tribunal)) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		return true;
	}
	
}
