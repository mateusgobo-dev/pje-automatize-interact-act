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
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.validator.constraints.Length;


@Entity
@Table(name = PessoaExpedienteJuridica.TABLE_NAME)
//@PrimaryKeyJoinColumn(name = "id_pessoa_exp_juridica")
public class PessoaExpedienteJuridica extends PessoaExpediente implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_pessoa_exp_juridica";
	private static final long serialVersionUID = 1L;

	private Estado estado;
	// private String nomeFantasia;
	private String numeroCpfResponsavel;
	private String nomeResponsavel;
	private Date dataAbertura;
	private Date dataFimAtividade;
	private Boolean orgaoPublico = Boolean.FALSE;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estado")
	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	// @Column(name = "nm_fantasia", length = 50)
	// @Length(max = 50)
	// public String getNomeFantasia() {
	// return this.nomeFantasia;
	// }
	// public void setNomeFantasia(String nomeFantasia) {
	// this.nomeFantasia = nomeFantasia;
	// }

	@Column(name = "nr_cpf_responsavel", length = 15)
	@Length(max = 15)
	public String getNumeroCpfResponsavel() {
		return numeroCpfResponsavel;
	}

	public void setNumeroCpfResponsavel(String numeroCpfResponsavel) {
		this.numeroCpfResponsavel = numeroCpfResponsavel;
	}

	@Column(name = "nm_responsavel", length = 150)
	@Length(max = 150)
	public String getNomeResponsavel() {
		return this.nomeResponsavel;
	}

	public void setNomeResponsavel(String nomeResponsavel) {
		this.nomeResponsavel = nomeResponsavel;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_abertura")
	public Date getDataAbertura() {
		return this.dataAbertura;
	}

	public void setDataAbertura(Date dataAbertura) {
		this.dataAbertura = dataAbertura;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fim_atividade")
	public Date getDataFimAtividade() {
		return this.dataFimAtividade;
	}

	public void setDataFimAtividade(Date dataFimAtividade) {
		this.dataFimAtividade = dataFimAtividade;
	}

	@Column(name = "in_orgao_publico")
	public Boolean getOrgaoPublico() {
		return orgaoPublico;
	}

	public void setOrgaoPublico(Boolean orgaoPublico) {
		this.orgaoPublico = orgaoPublico;
	}

	@Transient
	@Override
	public Class<? extends PessoaExpediente> getEntityClass() {
		return PessoaExpedienteJuridica.class;
	}
}