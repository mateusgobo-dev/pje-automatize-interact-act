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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_usu_local_visibilidade")
@org.hibernate.annotations.GenericGenerator(name = "gen_usu_localiz_visibilidade", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_usu_localiz_visibilidade"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class UsuarioLocalizacaoVisibilidade implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<UsuarioLocalizacaoVisibilidade,Integer> {

	private static final long serialVersionUID = 1L;

	private int idUsuarioLocalizacaoVisibilidade;
	private UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor;
	private OrgaoJulgadorCargo orgaoJulgadorCargo;
	private SubstituicaoMagistrado substituicaoMagistrado;
	private Date dtInicio;
	private Date dtFinal;
	
	public UsuarioLocalizacaoVisibilidade() {
	}

	@Id
	@GeneratedValue(generator = "gen_usu_localiz_visibilidade")
	@Column(name = "id_usu_localzacao_visibilidade", unique = true, nullable = false, updatable = false)
	public int getIdUsuarioLocalizacaoVisibilidade() {
		return idUsuarioLocalizacaoVisibilidade;
	}

	public void setIdUsuarioLocalizacaoVisibilidade(int idUsuarioLocalizacaoVisibilidade) {
		this.idUsuarioLocalizacaoVisibilidade = idUsuarioLocalizacaoVisibilidade;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usu_local_mgstrado_servidor", nullable = false)
	@NotNull
	public UsuarioLocalizacaoMagistradoServidor getUsuarioLocalizacaoMagistradoServidor() {
		return usuarioLocalizacaoMagistradoServidor;
	}

	public void setUsuarioLocalizacaoMagistradoServidor(
			UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor) {
		this.usuarioLocalizacaoMagistradoServidor = usuarioLocalizacaoMagistradoServidor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_org_julg_cargo_visibilidade")
	public OrgaoJulgadorCargo getOrgaoJulgadorCargo() {
		return orgaoJulgadorCargo;
	}

	public void setOrgaoJulgadorCargo(OrgaoJulgadorCargo orgaoJulgadorCargo) {
		this.orgaoJulgadorCargo = orgaoJulgadorCargo;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_substituicao_magistrado")
	public SubstituicaoMagistrado getSubstituicaoMagistrado() {
		return substituicaoMagistrado;
	}
	
	public void setSubstituicaoMagistrado(SubstituicaoMagistrado substituicaoMagistrado) {
		this.substituicaoMagistrado = substituicaoMagistrado;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicio")
	@NotNull
	public Date getDtInicio() {
		return dtInicio;
	}

	public void setDtInicio(Date dtInicio) {
		this.dtInicio = dtInicio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_final")
	public Date getDtFinal() {
		return dtFinal;
	}

	public void setDtFinal(Date dtFinal) {
		this.dtFinal = dtFinal;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof UsuarioLocalizacaoVisibilidade)) {
			return false;
		}
		UsuarioLocalizacaoVisibilidade other = (UsuarioLocalizacaoVisibilidade) obj;
		if (getIdUsuarioLocalizacaoVisibilidade() != other.getIdUsuarioLocalizacaoVisibilidade()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdUsuarioLocalizacaoVisibilidade();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends UsuarioLocalizacaoVisibilidade> getEntityClass() {
		return UsuarioLocalizacaoVisibilidade.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdUsuarioLocalizacaoVisibilidade());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
