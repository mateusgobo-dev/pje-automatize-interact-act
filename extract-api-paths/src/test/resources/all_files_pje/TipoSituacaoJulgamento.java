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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.ContextoEnum;

@Entity
@Table(name = "tb_tp_situacao_julgamento", uniqueConstraints = { @UniqueConstraint(columnNames = { "ds_tipo_situacao_julgamento" }) })
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_situacao_julgamento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_situacao_julgamento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoSituacaoJulgamento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoSituacaoJulgamento,Integer> {

	private static final long serialVersionUID = 1L;

	private int idTipoSituacaoJulgamento;
	private String tipoSituacaoJulgamento;
	private ContextoEnum contexto = ContextoEnum.JU;
	private Boolean ativo;

	public TipoSituacaoJulgamento() {
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_situacao_julgamento")
	@Column(name = "id_tipo_situacao_julgamento", unique = true, nullable = false)
	public int getIdTipoSituacaoJulgamento() {
		return this.idTipoSituacaoJulgamento;
	}

	public void setIdTipoSituacaoJulgamento(int idTipoSituacaoJulgamento) {
		this.idTipoSituacaoJulgamento = idTipoSituacaoJulgamento;
	}

	@Column(name = "ds_tipo_situacao_julgamento", unique = true, nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getTipoSituacaoJulgamento() {
		return this.tipoSituacaoJulgamento;
	}

	public void setTipoSituacaoJulgamento(String tipoSituacaoJulgamento) {
		this.tipoSituacaoJulgamento = tipoSituacaoJulgamento;
	}

	@Column(name = "tp_contexto", length = 2)
	@Enumerated(EnumType.STRING)
	public ContextoEnum getContexto() {
		return this.contexto;
	}

	public void setContexto(ContextoEnum contexto) {
		this.contexto = contexto;
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
		return tipoSituacaoJulgamento;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoSituacaoJulgamento();
		result = prime * result + ((getTipoSituacaoJulgamento() == null) ? 0 : tipoSituacaoJulgamento.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TipoSituacaoJulgamento))
			return false;
		TipoSituacaoJulgamento other = (TipoSituacaoJulgamento) obj;
		if (getIdTipoSituacaoJulgamento() != other.getIdTipoSituacaoJulgamento())
			return false;
		if (getTipoSituacaoJulgamento() == null) {
			if (other.getTipoSituacaoJulgamento() != null)
				return false;
		} else if (!tipoSituacaoJulgamento.equals(other.getTipoSituacaoJulgamento()))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoSituacaoJulgamento> getEntityClass() {
		return TipoSituacaoJulgamento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoSituacaoJulgamento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
