package br.jus.pje.nucleo.dto;

import java.io.Serializable;

public class ParametroDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private Integer idParametro;
	private String nomeVariavel;
	private String descricaoVariavel;
	private String valorVariavel;
	private Boolean ativo;
	
	public ParametroDTO(Integer idParametro, String nomeVariavel, String descricaoVariavel, String valorVariavel, Boolean ativo) {
		super();
		this.idParametro = idParametro;
		this.nomeVariavel = nomeVariavel;
		this.descricaoVariavel = descricaoVariavel;
		this.valorVariavel = valorVariavel;
		this.ativo = ativo;
	}

	public ParametroDTO() {
		super();
	}

	public Integer getIdParametro() {
		return idParametro;
	}

	public void setIdParametro(Integer idParametro) {
		this.idParametro = idParametro;
	}

	public String getNomeVariavel() {
		return nomeVariavel;
	}

	public void setNomeVariavel(String nomeVariavel) {
		this.nomeVariavel = nomeVariavel;
	}

	public String getDescricaoVariavel() {
		return descricaoVariavel;
	}

	public void setDescricaoVariavel(String descricaoVariavel) {
		this.descricaoVariavel = descricaoVariavel;
	}

	public String getValorVariavel() {
		return valorVariavel;
	}

	public void setValorVariavel(String valorVariavel) {
		this.valorVariavel = valorVariavel;
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
		int result = 1;
		result = prime * result + ((ativo == null) ? 0 : ativo.hashCode());
		result = prime * result + ((descricaoVariavel == null) ? 0 : descricaoVariavel.hashCode());
		result = prime * result + ((idParametro == null) ? 0 : idParametro.hashCode());
		result = prime * result + ((nomeVariavel == null) ? 0 : nomeVariavel.hashCode());
		result = prime * result + ((valorVariavel == null) ? 0 : valorVariavel.hashCode());
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
		ParametroDTO other = (ParametroDTO) obj;
		if (ativo == null) {
			if (other.ativo != null)
				return false;
		} else if (!ativo.equals(other.ativo))
			return false;
		if (descricaoVariavel == null) {
			if (other.descricaoVariavel != null)
				return false;
		} else if (!descricaoVariavel.equals(other.descricaoVariavel))
			return false;
		if (idParametro == null) {
			if (other.idParametro != null)
				return false;
		} else if (!idParametro.equals(other.idParametro))
			return false;
		if (nomeVariavel == null) {
			if (other.nomeVariavel != null)
				return false;
		} else if (!nomeVariavel.equals(other.nomeVariavel))
			return false;
		if (valorVariavel == null) {
			if (other.valorVariavel != null)
				return false;
		} else if (!valorVariavel.equals(other.valorVariavel))
			return false;
		return true;
	}

}
