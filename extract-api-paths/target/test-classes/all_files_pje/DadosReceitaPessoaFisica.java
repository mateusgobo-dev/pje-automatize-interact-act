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
package br.jus.pje.ws.externo.srfb.entidades;

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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;

@Entity
@Table(name = "tb_dado_receita_pess_fsica")
@org.hibernate.annotations.GenericGenerator(name = "gen_dado_receita_pess_fisica", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_dado_receita_pess_fisica"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DadosReceitaPessoaFisica implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<DadosReceitaPessoaFisica,Integer>, DadosReceitaPessoa {

	private static final long serialVersionUID = 1L;

	private int idDadosReceitaPessoaFisica;
	private String numCPF;
	private String nome;
	private Date dataNascimento;
	private String sexo;
	private String nomeMae;
	private String numTituloEleitor;
	private String tipoLogradouro;
	private String logradouro;
	private String numLogradouro;
	private String complemento;
	private String bairro;
	private String municipio;
	private String siglaUF;
	private String numCEP;
	private String situacaoCadastral;
	private Date dataAtualizacao;
	private PessoaFisica pessoaFisica;

	@Id
	@GeneratedValue(generator = "gen_dado_receita_pess_fisica")
	@Column(name = "id_dados_receita_pessoa_fisica", unique = true, nullable = false)
	public int getIdDadosReceitaPessoaFisica() {
		return idDadosReceitaPessoaFisica;
	}

	public void setIdDadosReceitaPessoaFisica(int idDadosReceitaPessoaFisica) {
		this.idDadosReceitaPessoaFisica = idDadosReceitaPessoaFisica;
	}

	@Column(name = "nr_cpf", nullable = false, length = 15)
	@NotNull
	@Length(max = 15)
	public String getNumCPF() {
		return numCPF;
	}

	public void setNumCPF(String numCPF) {
		this.numCPF = numCPF;
	}

	@Column(name = "nm_pessoa", nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "dt_nascimento")
	@Temporal(TemporalType.DATE)
	public Date getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	@Column(name = "tp_sexo", length = 1)
	@Length(max = 1)
	public String getSexo() {
		return sexo;
	}

	public void setSexo(String sexo) {
		this.sexo = sexo;
	}

	@Column(name = "nm_mae", length = 150)
	@Length(max = 150)
	public String getNomeMae() {
		return nomeMae;
	}

	public void setNomeMae(String nomeMae) {
		this.nomeMae = nomeMae;
	}

	@Column(name = "nr_titulo_eleitor", length = 15)
	@Length(max = 15)
	public String getNumTituloEleitor() {
		return numTituloEleitor;
	}

	public void setNumTituloEleitor(String numTituloEleitor) {
		this.numTituloEleitor = numTituloEleitor;
	}

	@Column(name = "tp_logradouro")
	@Length(max = 15)
	public String getTipoLogradouro() {
		return tipoLogradouro;
	}

	public void setTipoLogradouro(String tipoLogradouro) {
		this.tipoLogradouro = tipoLogradouro;
	}

	@Column(name = "ds_logradouro", length = 100)
	@Length(max = 100)
	public String getLogradouro() {
		return logradouro;
	}

	public void setLogradouro(String logradouro) {
		this.logradouro = logradouro;
	}

	@Column(name = "nr_logradouro", length = 15)
	@Length(max = 15)
	public String getNumLogradouro() {
		return numLogradouro;
	}

	public void setNumLogradouro(String numLogradouro) {
		this.numLogradouro = numLogradouro;
	}

	@Column(name = "ds_complemento", length = 50)
	@Length(max = 50)
	public String getComplemento() {
		return complemento;
	}

	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

	@Column(name = "ds_bairro", length = 50)
	@Length(max = 50)
	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	@Column(name = "nm_municipio", length = 100)
	@Length(max = 100)
	public String getMunicipio() {
		return municipio;
	}

	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}

	@Column(name = "sg_uf", length = 2)
	@Length(max = 2)
	public String getSiglaUF() {
		return siglaUF;
	}

	public void setSiglaUF(String siglaUF) {
		this.siglaUF = siglaUF;
	}

	@Column(name = "nr_cep", length = 9)
	@Length(max = 9)
	public String getNumCEP() {
		return numCEP;
	}

	public void setNumCEP(String numCEP) {
		this.numCEP = numCEP;
	}

	@Column(name = "tp_situacao_cadastral", length = 1)
	@Length(max = 1)
	public String getSituacaoCadastral() {
		return situacaoCadastral;
	}

	public void setSituacaoCadastral(String situacaoCadastral) {
		this.situacaoCadastral = situacaoCadastral;
	}

	@Column(name = "dt_atualizacao")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_fisica")
	public PessoaFisica getPessoaFisica() {
		return pessoaFisica;
	}

	public void setPessoaFisica(PessoaFisica pessoaFisica) {
		this.pessoaFisica = pessoaFisica;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaFisica(PessoaFisica)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaFisica(PessoaFisicaEspecializada pessoa){
		setPessoaFisica(pessoa != null ? pessoa.getPessoa() : (PessoaFisica) null);
	}

	@Transient
	public String getSexoString() {
		return "1".equals(sexo) ? "Masculino" : "Feminino";
	}

	@Transient
	public String getDescricaoSituacaoCadastral() {
		return br.jus.pje.ws.externo.srfb.util.SituacaoCadastroPessoaFisicaReceita
				.getDescricaoSituacao(this.situacaoCadastral);
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DadosReceitaPessoaFisica> getEntityClass() {
		return DadosReceitaPessoaFisica.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdDadosReceitaPessoaFisica());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
