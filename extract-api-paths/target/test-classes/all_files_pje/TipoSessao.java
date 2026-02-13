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

// Generated 30/10/2008 07:40:27 by Hibernate Tools 3.2.0.CR1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@javax.persistence.Cacheable(true)
@Cache(region = "FechamentoPautaSessaoJulgamento", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = TipoSessao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_sessao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_sessao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler", "session", "flushMode", "persistenceContext"})
public class TipoSessao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoSessao,Integer> {

	public static final String TABLE_NAME = "tb_tipo_sessao";
	private static final long serialVersionUID = 1L;

	private int idTipoSessao;
	private String tipoSessao;
	private Boolean ativo;

	public TipoSessao() {
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_sessao")
	@Column(name = "id_tipo_sessao", unique = true, nullable = false)
	public int getIdTipoSessao() {
		return this.idTipoSessao;
	}

	public void setIdTipoSessao(int idTipoSessao) {
		this.idTipoSessao = idTipoSessao;
	}

	@Column(name = "ds_tipo_sessao", unique = true, nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getTipoSessao() {
		return this.tipoSessao;
	}

	public void setTipoSessao(String tipoSessao) {
		this.tipoSessao = tipoSessao;
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
		return tipoSessao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoSessao)) {
			return false;
		}
		TipoSessao other = (TipoSessao) obj;
		if (getIdTipoSessao() != other.getIdTipoSessao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoSessao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoSessao> getEntityClass() {
		return TipoSessao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoSessao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}