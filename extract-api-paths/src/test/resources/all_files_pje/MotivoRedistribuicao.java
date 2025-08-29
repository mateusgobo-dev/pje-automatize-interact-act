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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = MotivoRedistribuicao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_motivo_redistribuicao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_motivo_redistribuicao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class MotivoRedistribuicao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<MotivoRedistribuicao,Integer> {

	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "tb_motivo_redistribuicao";

	private Integer idMotivoRedistribuicao;
	private String motivoRedistribuicao;
	private Boolean ativo;

	@Id
	@GeneratedValue(generator = "gen_motivo_redistribuicao")
	@Column(name = "id_motivo_redistribuicao", unique = true, nullable = false)
	public Integer getIdMotivoRedistribuicao() {
		return idMotivoRedistribuicao;
	}

	public void setIdMotivoRedistribuicao(Integer idMotivoRedistribuicao) {
		this.idMotivoRedistribuicao = idMotivoRedistribuicao;
	}

	@Column(name = "ds_motivo_redistribuicao", length = 150, nullable = false, unique = true)
	@Length(max = 150)
	@NotNull
	public String getMotivoRedistribuicao() {
		return motivoRedistribuicao;
	}

	public void setMotivoRedistribuicao(String motivoRedistribuicao) {
		this.motivoRedistribuicao = motivoRedistribuicao;
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
		return motivoRedistribuicao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MotivoRedistribuicao)) {
			return false;
		}
		if(getIdMotivoRedistribuicao() == null){
			return false;
		}
		MotivoRedistribuicao other = (MotivoRedistribuicao) obj;
		if (!idMotivoRedistribuicao.equals(other.getIdMotivoRedistribuicao())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdMotivoRedistribuicao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends MotivoRedistribuicao> getEntityClass() {
		return MotivoRedistribuicao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdMotivoRedistribuicao();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
