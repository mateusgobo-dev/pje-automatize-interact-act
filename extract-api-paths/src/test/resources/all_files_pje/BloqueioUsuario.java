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

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "tb_bloqueio_usuario")
@org.hibernate.annotations.GenericGenerator(name = "gen_bloqueio_usuario", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_bloqueio_usuario"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class BloqueioUsuario implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<BloqueioUsuario,Integer> {

	private static final long serialVersionUID = 1L;

	private int idBloqueioUsuario;
	private Usuario usuario;
	private Date dataBloqueio;
	private Date dataPrevisaoDesbloqueio;
	private String motivoBloqueio;
	private Date dataDesbloqueio;

	public BloqueioUsuario() {
	}

	@Id
	@GeneratedValue(generator = "gen_bloqueio_usuario")
	@Column(name = "id_bloqueio_usuario", unique = true, nullable = false)
	public int getIdBloqueioUsuario() {
		return this.idBloqueioUsuario;
	}

	public void setIdBloqueioUsuario(int idBloqueioUsuario) {
		this.idBloqueioUsuario = idBloqueioUsuario;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario", nullable = false)
	@NotNull
	public Usuario getUsuario() {
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_bloqueio", nullable = false)
	@NotNull
	public Date getDataBloqueio() {
		return this.dataBloqueio;
	}

	public void setDataBloqueio(Date dataBloqueio) {
		this.dataBloqueio = dataBloqueio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_previsao_desbloqueio", nullable = false)
	@NotNull
	public Date getDataPrevisaoDesbloqueio() {
		return this.dataPrevisaoDesbloqueio;
	}

	public void setDataPrevisaoDesbloqueio(Date dataPrevisaoDesbloqueio) {
		this.dataPrevisaoDesbloqueio = dataPrevisaoDesbloqueio;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_motivo_bloqueio", nullable = false)
	@NotNull
	public String getMotivoBloqueio() {
		return this.motivoBloqueio;
	}

	public void setMotivoBloqueio(String motivoBloqueio) {
		this.motivoBloqueio = motivoBloqueio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_desbloqueio")
	public Date getDataDesbloqueio() {
		return this.dataDesbloqueio;
	}

	public void setDataDesbloqueio(Date dataDesbloqueio) {
		this.dataDesbloqueio = dataDesbloqueio;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof BloqueioUsuario)) {
			return false;
		}
		BloqueioUsuario other = (BloqueioUsuario) obj;
		if (getIdBloqueioUsuario() != other.getIdBloqueioUsuario()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdBloqueioUsuario();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends BloqueioUsuario> getEntityClass() {
		return BloqueioUsuario.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdBloqueioUsuario());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
