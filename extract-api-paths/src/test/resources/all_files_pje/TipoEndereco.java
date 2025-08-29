/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_tipo_endereco")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_endereco", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_endereco"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoEndereco implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoEndereco,Integer> {

	private static final long serialVersionUID = 1L;

	private int idTipoEndereco;
	private String tipoEndereco;
	private Boolean ativo;

	public TipoEndereco() {

	}

	@Id
	@GeneratedValue(generator = "gen_tipo_endereco")
	@Column(name = "id_tipo_endereco", unique = true, nullable = false)
	public int getIdTipoEndereco() {
		return this.idTipoEndereco;
	}

	public void setIdTipoEndereco(int idTipoEndereco) {
		this.idTipoEndereco = idTipoEndereco;
	}

	@Column(name = "ds_tipo_endereco", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getTipoEndereco() {
		return this.tipoEndereco;
	}

	public void setTipoEndereco(String tipoEndereco) {
		this.tipoEndereco = tipoEndereco;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoEndereco)) {
			return false;
		}
		TipoEndereco other = (TipoEndereco) obj;
		if (getIdTipoEndereco() != other.getIdTipoEndereco()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoEndereco();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoEndereco> getEntityClass() {
		return TipoEndereco.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoEndereco());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
