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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.SexoEnum;

@Entity
@Table(name = PessoaExpedienteFisica.TABLE_NAME)
//@PrimaryKeyJoinColumn(name = "id_pessoa_exp_fisica")
public class PessoaExpedienteFisica extends PessoaExpediente implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_pessoa_exp_fisica";
	private static final long serialVersionUID = 1L;

	private Escolaridade escolaridade;
	private EstadoCivil estadoCivil;
	private Profissao profissao;
	private Etnia etnia;
	// private Estado estado;
	private Municipio municipioNascimento;
	private SexoEnum sexo;
	private Date dataNascimento;
	private String nomeGenitor;
	private String nomeGenitora;
	private Boolean incapaz = Boolean.FALSE;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_escolaridade")
	public Escolaridade getEscolaridade() {
		return escolaridade;
	}

	public void setEscolaridade(Escolaridade escolaridade) {
		this.escolaridade = escolaridade;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estado_civil")
	public EstadoCivil getEstadoCivil() {
		return estadoCivil;
	}

	public void setEstadoCivil(EstadoCivil estadoCivil) {
		this.estadoCivil = estadoCivil;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_profissao")
	public Profissao getProfissao() {
		return profissao;
	}

	public void setProfissao(Profissao profissao) {
		this.profissao = profissao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_etnia")
	public Etnia getEtnia() {
		return etnia;
	}

	public void setEtnia(Etnia etnia) {
		this.etnia = etnia;
	}

	// public Estado getEstado() {
	// return estado;
	// }
	// public void setEstado(Estado estado) {
	// this.estado = estado;
	// }

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_municipio")
	public Municipio getMunicipioNascimento() {
		return municipioNascimento;
	}

	public void setMunicipioNascimento(Municipio municipioNascimento) {
		this.municipioNascimento = municipioNascimento;
	}

	@Column(name = "in_sexo")
	@Enumerated(EnumType.STRING)
	public SexoEnum getSexo() {
		return sexo;
	}

	public void setSexo(SexoEnum sexo) {
		this.sexo = sexo;
	}

	@Column(name = "dt_nascimento")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	@Column(name = "nm_genitor", length = 150)
	@Length(max = 150)
	public String getNomeGenitor() {
		return nomeGenitor;
	}

	public void setNomeGenitor(String nomeGenitor) {
		this.nomeGenitor = nomeGenitor;
	}

	@Column(name = "nm_genitora", length = 150)
	@Length(max = 150)
	public String getNomeGenitora() {
		return nomeGenitora;
	}

	public void setNomeGenitora(String nomeGenitora) {
		this.nomeGenitora = nomeGenitora;
	}

	@Column(name = "in_incapaz")
	public Boolean getIncapaz() {
		return incapaz;
	}

	public void setIncapaz(Boolean incapaz) {
		this.incapaz = incapaz;
	}

	@Transient
	@Override
	public Class<? extends PessoaExpediente> getEntityClass() {
		return PessoaExpedienteFisica.class;
	}
}