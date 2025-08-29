package br.jus.pje.nucleo.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MotivoExtincaoMedidaSocioeducativaDTO extends PJeServiceApiDTO {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String dsMotivoExtincao;
	private Boolean ativo = Boolean.TRUE;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Boolean getAtivo() {
		return ativo;
	}
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	public String getDsMotivoExtincao() {
		return dsMotivoExtincao;
	}
	public void setDsMotivoExtincao(String dsMotivoExtincao) {
		this.dsMotivoExtincao = dsMotivoExtincao;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(ativo, dsMotivoExtincao, id);
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
		MotivoExtincaoMedidaSocioeducativaDTO other = (MotivoExtincaoMedidaSocioeducativaDTO) obj;
		return Objects.equals(ativo, other.ativo) && Objects.equals(dsMotivoExtincao, other.dsMotivoExtincao)
				&& Objects.equals(id, other.id);
	}
	
}
