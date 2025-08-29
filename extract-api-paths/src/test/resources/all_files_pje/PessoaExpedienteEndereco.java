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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;


@Entity
@Table(name = "tb_pessoa_exp_endereco")
@org.hibernate.annotations.GenericGenerator(name = "gen_pessoa_exp_endereco", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pessoa_exp_endereco"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PessoaExpedienteEndereco implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaExpedienteEndereco,Integer> {

	private static final long serialVersionUID = 1L;

	private int idPessoaExpedienteEndereco;
	private Cep cep;
	private PessoaExpediente pessoaExpediente;
	private String nomeLogradouro;
	private String numeroEndereco;
	private String complemento;
	private String nomeBairro;
	private String nomeCidade;
	private Boolean correspondencia;
	private Date dataAlteracao;

	@Id
	@GeneratedValue(generator = "gen_pessoa_exp_endereco")
	@Column(name = "id_pessoa_exp_endereco")
	public int getIdPessoaExpedienteEndereco() {
		return this.idPessoaExpedienteEndereco;
	}

	public void setIdPessoaExpedienteEndereco(int idPessoaExpedienteEndereco) {
		this.idPessoaExpedienteEndereco = idPessoaExpedienteEndereco;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cep", nullable = false)
	@NotNull
	public Cep getCep() {
		return this.cep;
	}

	public void setCep(Cep cep) {
		this.cep = cep;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_exp", nullable = false)
	@NotNull
	public PessoaExpediente getPessoaExpediente() {
		return this.pessoaExpediente;
	}

	public void setPessoaExpediente(PessoaExpediente pessoaExpediente) {
		this.pessoaExpediente = pessoaExpediente;
	}

	@Column(name = "nm_logradouro", length = 200)
	@Length(max = 200)
	public String getNomeLogradouro() {
		return this.nomeLogradouro;
	}

	public void setNomeLogradouro(String nomeLogradouro) {
		this.nomeLogradouro = nomeLogradouro;
	}

	@Column(name = "nr_endereco", length = 15)
	@Length(max = 15)
	public String getNumeroEndereco() {
		return this.numeroEndereco;
	}

	public void setNumeroEndereco(String numeroEndereco) {
		this.numeroEndereco = numeroEndereco;
	}

	@Column(name = "ds_complemento", length = 100)
	@Length(max = 100)
	public String getComplemento() {
		return this.complemento;
	}

	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

	@Column(name = "nm_bairro", length = 100)
	@Length(max = 100)
	public String getNomeBairro() {
		return this.nomeBairro;
	}

	public void setNomeBairro(String nomeBairro) {
		this.nomeBairro = nomeBairro;
	}

	@Column(name = "nm_cidade", length = 100)
	@Length(max = 100)
	public String getNomeCidade() {
		return nomeCidade;
	}

	public void setNomeCidade(String nomeCidade) {
		this.nomeCidade = nomeCidade;
	}

	@Column(name = "in_correspondencia")
	public Boolean getCorrespondencia() {
		return this.correspondencia;
	}

	public void setCorrespondencia(Boolean correspondencia) {
		this.correspondencia = correspondencia;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_alteracao_endereco")
	public Date getDataAlteracao() {
		return this.dataAlteracao;
	}

	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	@Override
	public String toString() {
		if (nomeLogradouro != null)
			return nomeLogradouro + ", " + numeroEndereco + ", " + getNomeCidade();
		else
			return "";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PessoaExpedienteEndereco)) {
			return false;
		}
		PessoaExpedienteEndereco other = (PessoaExpedienteEndereco) obj;
		if (getIdPessoaExpedienteEndereco() != other.getIdPessoaExpedienteEndereco()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPessoaExpedienteEndereco();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaExpedienteEndereco> getEntityClass() {
		return PessoaExpedienteEndereco.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPessoaExpedienteEndereco());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
