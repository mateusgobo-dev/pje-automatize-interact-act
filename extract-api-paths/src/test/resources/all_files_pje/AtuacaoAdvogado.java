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
@Table(name = AtuacaoAdvogado.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_atuacao_adv", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_atuacao_advogado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AtuacaoAdvogado implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<AtuacaoAdvogado,Integer> {

	public static final String TABLE_NAME = "tb_atuacao_advogado";
	private static final long serialVersionUID = 1L;

	private int idAtuacaoAdvogado;
	private String atuacaoAdvogado;
	private Boolean ativo;

	public AtuacaoAdvogado() {
	}

	@Id
	@GeneratedValue(generator = "gen_atuacao_adv")
	@Column(name = "id_atuacao_advogado", unique = true, nullable = false)
	public int getIdAtuacaoAdvogado() {
		return this.idAtuacaoAdvogado;
	}

	public void setIdAtuacaoAdvogado(int idAtuacaoAdvogado) {
		this.idAtuacaoAdvogado = idAtuacaoAdvogado;
	}

	@Column(name = "ds_atuacao_advogado", nullable = false, length = 100, unique = true)
	@NotNull
	@Length(max = 100)
	public String getAtuacaoAdvogado() {
		return this.atuacaoAdvogado;
	}

	public void setAtuacaoAdvogado(String atuacaoAdvogado) {
		this.atuacaoAdvogado = atuacaoAdvogado;
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
	public String toString() {
		return atuacaoAdvogado;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AtuacaoAdvogado)) {
			return false;
		}
		AtuacaoAdvogado other = (AtuacaoAdvogado) obj;
		if (getIdAtuacaoAdvogado() != other.getIdAtuacaoAdvogado()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdAtuacaoAdvogado();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AtuacaoAdvogado> getEntityClass() {
		return AtuacaoAdvogado.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAtuacaoAdvogado());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
