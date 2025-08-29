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
package br.jus.pje.ws.externo.cna.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.IEntidade;
import br.jus.pje.nucleo.util.StringUtil;

@Entity
@Table(name = "tb_dado_oab_pess_advogado")
@org.hibernate.annotations.GenericGenerator(name = "gen_dados_oab_pess_advogado", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_dados_oab_pess_advogado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DadosAdvogadoOAB implements IEntidade<DadosAdvogadoOAB, Integer> {

	private static final long serialVersionUID = 1L;
	private int idDadosAdvogadoOAB;
	private String numSeguranca;
	private String uf;
	private String organizacao;
	private String nome;
	private String nomePai;
	private String nomeMae;
	private String numInscricao;
	private String numCPF;
	private String tipoInscricao;
	private String codigoSituacao;
	private String situacaoInscricao;
	private String logadouro;
	private String bairro;
	private String cidade;
	private String cep;
	private String email;
	private String ddd;
	private String telefone;
	private Date dataCadastro;
	private Boolean oabSelecionado;
	private String letra;
	
	@Id
	@GeneratedValue(generator = "gen_dados_oab_pess_advogado")
	@Column(name = "id_dados_advogado_oab", unique = true, nullable = false)
	public int getIdDadosAdvogadoOAB() {
		return idDadosAdvogadoOAB;
	}

	public void setIdDadosAdvogadoOAB(int idDadosAdvogadoOAB) {
		this.idDadosAdvogadoOAB = idDadosAdvogadoOAB;
	}

	@Column(name = "nr_seguranca", nullable = false, length = 8)
	@NotNull
	@Length(max = 8)
	public String getNumSeguranca() {
		return numSeguranca;
	}

	public void setNumSeguranca(String numSeguranca) {
		this.numSeguranca = numSeguranca;
	}

	@Column(name = "cd_uf", nullable = false, length = 2)
	@NotNull
	@Length(max = 2)
	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

	@Column(name = "nm_organizacao", length = 150)
	@Length(max = 150)
	public String getOrganizacao() {
		return organizacao;
	}

	public void setOrganizacao(String organizacao) {
		this.organizacao = StringUtil.normalize(organizacao);
	}

	@Column(name = "nm_nome", length = 150)
	@Length(max = 150)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = StringUtil.normalize(nome);
	}

	@Column(name = "nm_pai", length = 150)
	@Length(max = 150)
	public String getNomePai() {
		return nomePai;
	}

	public void setNomePai(String nomePai) {
		this.nomePai = StringUtil.normalize(nomePai);
	}

	@Column(name = "nm_mae", length = 150)
	@Length(max = 150)
	public String getNomeMae() {
		return nomeMae;
	}

	public void setNomeMae(String nomeMae) {
		this.nomeMae = StringUtil.normalize(nomeMae);
	}

	@Column(name = "nr_inscricao", length = 15)
	@Length(max = 15)
	public String getNumInscricao() {
		return numInscricao;
	}

	public void setNumInscricao(String numInscricao) {
		this.numInscricao = numInscricao;
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

	@Column(name = "ds_tipo_inscricao", length = 30)
	@Length(max = 30)
	public String getTipoInscricao() {
		return tipoInscricao;
	}

	public void setTipoInscricao(String tipoInscricao) {
		this.tipoInscricao = tipoInscricao;
	}

	@Column(name = "ds_situacao", length = 30)
	@Length(max = 30)
	public String getSituacaoInscricao() {
		return situacaoInscricao;
	}

	public void setSituacaoInscricao(String situacaoInscricao) {
		this.situacaoInscricao = situacaoInscricao;
	}

	@Column(name = "nm_logradouro", length = 150)
	@Length(max = 150)
	public String getLogadouro() {
		return logadouro;
	}

	public void setLogadouro(String logadouro) {
		this.logadouro = StringUtil.normalize(logadouro);
	}

	@Column(name = "nm_bairro", length = 150)
	@Length(max = 150)
	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = StringUtil.normalize(bairro);
	}

	@Column(name = "nm_municipio", length = 150)
	@Length(max = 150)
	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = StringUtil.normalize(cidade);
	}

	@Column(name = "nr_cep", length = 9)
	@Length(max = 9)
	public String getCep() {
		return cep;
	}

	public void setCep(String cep) {
		this.cep = cep;
	}

	@Column(name = "ds_email", length = 200)
	@Length(max = 200)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "nr_ddd", length = 5)
	@Length(max = 5)
	public String getDdd() {
		return ddd;
	}

	public void setDdd(String ddd) {
		this.ddd = ddd;
	}

	@Column(name = "nr_telefone", length = 200)
	@Length(max = 200)
	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	@Column(name = "dt_cadastro")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDataCadastro() {
		return dataCadastro;
	}

	@Override
	public String toString() {
		return nome + " (" + numInscricao + "-" + uf + ")";
	}

	@Column(name = "cd_situacao", length = 1)
	@Length(max = 1)
	public String getCodigoSituacao() {
		return codigoSituacao;
	}

	public void setCodigoSituacao(String codigoSituacao) {
		this.codigoSituacao = codigoSituacao;
	}

	@Transient
	public Boolean getOabSelecionado() {
		return oabSelecionado;
	}

	public void setOabSelecionado(Boolean oabSelecionado) {
		this.oabSelecionado = oabSelecionado;
	}

	@Transient
	public String getLetra() {
		return letra;
	}

	public void setLetra(String letra) {
		this.letra = letra;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DadosAdvogadoOAB> getEntityClass() {
		return DadosAdvogadoOAB.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdDadosAdvogadoOAB());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
