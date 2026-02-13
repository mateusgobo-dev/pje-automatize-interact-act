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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;


@Entity
@Table(name = PessoaAssistenteAdvogadoLocal.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "id_pessoa_assit_adv_local")
public class PessoaAssistenteAdvogadoLocal extends UsuarioLocalizacao implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_pessoa_assist_adv_local";
	private static final long serialVersionUID = 1L;

	private Date dataPosse;
	private boolean assinadoDigitalmente = false;
	private boolean gestor = false;

	public PessoaAssistenteAdvogadoLocal() {
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_posse")
	@Past
	public Date getDataPosse() {
		return this.dataPosse;
	}

	public void setDataPosse(Date dataPosse) {
		this.dataPosse = dataPosse;
	}

	@Column(name = "in_assina_digitalmente", nullable = false)
	@NotNull
	public boolean getAssinadoDigitalmente() {
		return assinadoDigitalmente;
	}

	public void setAssinadoDigitalmente(boolean assinadoDigitalmente) {
		this.assinadoDigitalmente = assinadoDigitalmente;
	}

	@Column(name = "in_gestor")
	@NotNull
	public boolean getGestor() {
		return gestor;
	}

	public void setGestor(boolean gestor) {
		this.gestor = gestor;
	}

	@Transient
	@Override
	public Class<? extends UsuarioLocalizacao> getEntityClass() {
		return PessoaAssistenteAdvogadoLocal.class;
	}
	
}