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
@Table(name = "tb_tipo_redistribuicao")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_redistribuicao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_redistribuicao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoRedistribuicao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoRedistribuicao,Integer> {

	private static final long serialVersionUID = 1L;

	private int idTipoRedistribuicao;
	private String tipoRedistribuicao;
	private Boolean ativo;

	public TipoRedistribuicao() {
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_redistribuicao")
	@Column(name = "id_tipo_redistribuicao", unique = true, nullable = false)
	public int getIdTipoRedistribuicao() {
		return idTipoRedistribuicao;
	}

	public void setIdTipoRedistribuicao(int idTipoRedistribuicao) {
		this.idTipoRedistribuicao = idTipoRedistribuicao;
	}

	@Column(name = "ds_tipo_redistribuicao", nullable = false, length = 150, unique = true)
	@NotNull
	@Length(max = 150)
	public String getTipoRedistribuicao() {
		return tipoRedistribuicao;
	}

	public void setTipoRedistribuicao(String tipoRedistribuicao) {
		this.tipoRedistribuicao = tipoRedistribuicao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return tipoRedistribuicao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoRedistribuicao)) {
			return false;
		}
		TipoRedistribuicao other = (TipoRedistribuicao) obj;
		if (getIdTipoRedistribuicao() != other.getIdTipoRedistribuicao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoRedistribuicao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoRedistribuicao> getEntityClass() {
		return TipoRedistribuicao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoRedistribuicao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
