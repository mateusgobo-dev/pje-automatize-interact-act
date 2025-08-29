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
package br.jus.pje.jt.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.jt.enums.ClassificacaoTipoSituacaoPautaEnum;

@Entity
@Table(name="tb_tipo_situacao_pauta")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_situacao_pauta", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_situacao_pauta"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoSituacaoPauta implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoSituacaoPauta,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idTipoSituacaoPauta;
	private String codigoTipoSituacaoPauta;
	private String tipoSituacaoPauta;
	private Boolean ativo;
	private ClassificacaoTipoSituacaoPautaEnum classificacao;

	@Id
	@GeneratedValue(generator = "gen_tipo_situacao_pauta")
	@Column(name = "id_tipo_situacao_pauta", unique = true, nullable = false)
	public Integer getIdTipoSituacaoPauta() {
		return idTipoSituacaoPauta;
	}

	public void setIdTipoSituacaoPauta(Integer idTipoSituacaoPauta) {
		this.idTipoSituacaoPauta = idTipoSituacaoPauta;
	}

	@Column(name="cd_tipo_situacao_pauta", unique=true, nullable=false, length=2)
	@NotNull
	@Length(max=2)
	public String getCodigoTipoSituacaoPauta() {
		return codigoTipoSituacaoPauta;
	}

	public void setCodigoTipoSituacaoPauta(String codigoTipoSituacaoPauta) {
		this.codigoTipoSituacaoPauta = codigoTipoSituacaoPauta;
	}

	@Column(name="ds_tipo_situacao_pauta", unique=true, nullable=false, length=150)
	@NotNull
	@Length(max=150)
	public String getTipoSituacaoPauta() {
		return tipoSituacaoPauta;
	}

	public void setTipoSituacaoPauta(String tipoSituacaoPauta) {
		this.tipoSituacaoPauta = tipoSituacaoPauta;
	}

	@Column(name="in_ativo", nullable=false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "in_classificacao", nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	public ClassificacaoTipoSituacaoPautaEnum getClassificacao() {
		return classificacao;
	}

	public void setClassificacao(ClassificacaoTipoSituacaoPautaEnum classificacao) {
		this.classificacao = classificacao;
	}
	
	@Override
	public String toString() {
		return tipoSituacaoPauta;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoSituacaoPauta();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TipoSituacaoPauta)) {
			return false;
		}
		if(getIdTipoSituacaoPauta() == null){
			return false;
		}
		TipoSituacaoPauta other = (TipoSituacaoPauta) obj;
		if (!idTipoSituacaoPauta.equals(other.getIdTipoSituacaoPauta()))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoSituacaoPauta> getEntityClass() {
		return TipoSituacaoPauta.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdTipoSituacaoPauta();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
