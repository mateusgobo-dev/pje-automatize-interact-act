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

import br.jus.pje.nucleo.entidades.PessoaJuridica;

@Entity
@Table(name = "tb_dado_receita_pess_jurid")
@org.hibernate.annotations.GenericGenerator(name = "gen_dado_rcita_pess_juridica", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_dado_rcita_pess_juridica"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DadosReceitaPessoaJuridica implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<DadosReceitaPessoaJuridica,Integer>, DadosReceitaPessoa {

	private static final long serialVersionUID = 1L;

	private int idDadosReceitaPessoaJuridica;
	private PessoaJuridica pessoaJuridica;
	private String numCNPJ;
	private String razaoSocial;
	private Date dataRegistro;
	private String status;
	private Date dataAtualizacao;
	private Boolean ativo = Boolean.TRUE;
	private String tipoMatrizFilial;
	private String nomeFantasia;
	private String numTelefone1;
	private String numDdd1;
	private String numTelefone2;
	private String numDdd2;
	private String numFax;
	private String numDddFax;
	private String correioEletronico;
	private String inSocio;
	private String codigoCnaeFiscal;
	private String descricaoCnaeFiscal;
	private String codigoNaturezaJuridica;
	private String descricaoNaturezaJuridica;
	private Date dataSituacaoCnpj;
	private String statusCadastralPessoaJuridica;
	private String numNire;
	private String numCpfResponsavel;
	private String nomeResponsavel;
	private String tipoLogradouroResponsavel;
	private String descricaoLogradouroResponsavel;
	private String numLogradouroResponsavel;
	private String descricaoComplementoResponsavel;
	private String descricaoBairroResponsavel;
	private String numCepResponsavel;
	private String codigoMunicipioResponsavel;
	private String descricaoMunicipioResponsavel;
	private String codigoUfResponsavel;
	private String numTelefoneResponsavel;
	private String numDddTelefoneResponsavel;
	private String correioEletronicoResponsavel;
	private String codigoQualificacaoResponsavel;
	private String descricaoQualificacaoResponsavel;
	private String codigoSituacaoAtualizacao;
	private String tipoLogradouro;
	private String descricaoLogradouro;
	private String numLogradouro;
	private String descricaoComplemento;
	private String descricaoBairro;
	private String codigoBairro;
	private String codigoMunicipio;
	private String descricaoMunicipio;
	private String siglaUf;
	private String numCep;

	@Id
	@GeneratedValue(generator = "gen_dado_rcita_pess_juridica")
	@Column(name = "id_dado_receita_pess_juridica", unique = true, nullable = false)
	public int getIdDadosReceitaPessoaJuridica() {
		return idDadosReceitaPessoaJuridica;
	}

	public void setIdDadosReceitaPessoaJuridica(int idDadosReceitaPessoaJuridica) {
		this.idDadosReceitaPessoaJuridica = idDadosReceitaPessoaJuridica;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_juridica")
	public PessoaJuridica getPessoaJuridica() {
		return pessoaJuridica;
	}

	public void setPessoaJuridica(PessoaJuridica pessoaJuridica) {
		this.pessoaJuridica = pessoaJuridica;
	}

	@Column(name = "nr_cnpj", nullable = false, length = 18)
	@NotNull
	@Length(max = 18)
	public String getNumCNPJ() {
		return numCNPJ;
	}

	public void setNumCNPJ(String numCNPJ) {
		this.numCNPJ = numCNPJ;
	}

	@Column(name = "ds_razao_social", length = 200)
	@Length(max = 200)
	public String getRazaoSocial() {
		return razaoSocial;
	}

	public void setRazaoSocial(String razaoSocial) {
		this.razaoSocial = razaoSocial;
	}

	@Column(name = "dt_registro")
	@Temporal(TemporalType.DATE)
	public Date getDataRegistro() {
		return dataRegistro;
	}

	public void setDataRegistro(Date dataRegistro) {
		this.dataRegistro = dataRegistro;
	}

	@Column(name = "in_status", length = 1)
	@Length(max = 1)
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Column(name = "dt_atualizacao")
	@Temporal(TemporalType.DATE)
	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	@Column(name = "in_ativo")
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "tp_matriz_filial", length = 1)
	@Length(max = 1)
	public String getTipoMatrizFilial() {
		return tipoMatrizFilial;
	}

	public void setTipoMatrizFilial(String tipoMatrizFilial) {
		this.tipoMatrizFilial = tipoMatrizFilial;
	}

	@Column(name = "nm_fantasia", length = 200)
	@Length(max = 200)
	public String getNomeFantasia() {
		return nomeFantasia;
	}

	public void setNomeFantasia(String nomeFantasia) {
		this.nomeFantasia = nomeFantasia;
	}

	@Column(name = "nr_telefone1", length = 15)
	@Length(max = 15)
	public String getNumTelefone1() {
		return numTelefone1;
	}

	public void setNumTelefone1(String numTelefone1) {
		this.numTelefone1 = numTelefone1;
	}

	@Column(name = "nr_ddd1", length = 5)
	@Length(max = 5)
	public String getNumDdd1() {
		return numDdd1;
	}

	public void setNumDdd1(String numDdd1) {
		this.numDdd1 = numDdd1;
	}

	@Column(name = "nr_telefone2", length = 15)
	@Length(max = 15)
	public String getNumTelefone2() {
		return numTelefone2;
	}

	public void setNumTelefone2(String numTelefone2) {
		this.numTelefone2 = numTelefone2;
	}

	@Column(name = "nr_ddd2", length = 5)
	@Length(max = 5)
	public String getNumDdd2() {
		return numDdd2;
	}

	public void setNumDdd2(String numDdd2) {
		this.numDdd2 = numDdd2;
	}

	@Column(name = "nr_fax", length = 15)
	@Length(max = 15)
	public String getNumFax() {
		return numFax;
	}

	public void setNumFax(String numFax) {
		this.numFax = numFax;
	}

	@Column(name = "nr_ddd_fax", length = 5)
	@Length(max = 5)
	public String getNumDddFax() {
		return numDddFax;
	}

	public void setNumDddFax(String numDddFax) {
		this.numDddFax = numDddFax;
	}

	@Column(name = "ds_correio_eletronico", length = 100)
	@Length(max = 100)
	public String getCorreioEletronico() {
		return correioEletronico;
	}

	public void setCorreioEletronico(String correioEletronico) {
		this.correioEletronico = correioEletronico;
	}

	@Column(name = "in_socio", length = 1)
	@Length(max = 1)
	public String getInSocio() {
		return inSocio;
	}

	public void setInSocio(String inSocio) {
		this.inSocio = inSocio;
	}

	@Column(name = "cd_cnae_fiscal", length = 15)
	@Length(max = 15)
	public String getCodigoCnaeFiscal() {
		return codigoCnaeFiscal;
	}

	public void setCodigoCnaeFiscal(String codigoCnaeFiscal) {
		this.codigoCnaeFiscal = codigoCnaeFiscal;
	}

	@Column(name = "ds_cnae_fiscal", length = 200)
	@Length(max = 200)
	public String getDescricaoCnaeFiscal() {
		return descricaoCnaeFiscal;
	}

	public void setDescricaoCnaeFiscal(String descricaoCnaeFiscal) {
		this.descricaoCnaeFiscal = descricaoCnaeFiscal;
	}

	@Column(name = "cd_natureza_juridica", length = 15)
	@Length(max = 15)
	public String getCodigoNaturezaJuridica() {
		return codigoNaturezaJuridica;
	}

	public void setCodigoNaturezaJuridica(String codigoNaturezaJuridica) {
		this.codigoNaturezaJuridica = codigoNaturezaJuridica;
	}

	@Column(name = "ds_natureza_juridica", length = 200)
	@Length(max = 200)
	public String getDescricaoNaturezaJuridica() {
		return descricaoNaturezaJuridica;
	}

	public void setDescricaoNaturezaJuridica(String descricaoNaturezaJuridica) {
		this.descricaoNaturezaJuridica = descricaoNaturezaJuridica;
	}

	@Column(name = "dt_situacao_cnpj")
	@Temporal(TemporalType.DATE)
	public Date getDataSituacaoCnpj() {
		return dataSituacaoCnpj;
	}

	public void setDataSituacaoCnpj(Date dataSituacaoCnpj) {
		this.dataSituacaoCnpj = dataSituacaoCnpj;
	}

	@Column(name = "st_cadastral_pessoa_juridica", length = 15)
	@Length(max = 15)
	public String getStatusCadastralPessoaJuridica() {
		return statusCadastralPessoaJuridica;
	}

	public void setStatusCadastralPessoaJuridica(String statusCadastralPessoaJuridica) {
		this.statusCadastralPessoaJuridica = statusCadastralPessoaJuridica;
	}

	@Transient
	public String getDescricaoSituacaoCadastral() {
		return br.jus.pje.ws.externo.srfb.util.SituacaoCadastroPessoaJuridicaReceita
				.getDescricaoSituacao(this.statusCadastralPessoaJuridica);
	}

	@Column(name = "nr_nire", length = 100)
	@Length(max = 100)
	public String getNumNire() {
		return numNire;
	}

	public void setNumNire(String numNire) {
		this.numNire = numNire;
	}

	@Column(name = "nr_cpf_responsavel", length = 15)
	@Length(max = 15)
	public String getNumCpfResponsavel() {
		return numCpfResponsavel;
	}

	public void setNumCpfResponsavel(String numCpfResponsavel) {
		this.numCpfResponsavel = numCpfResponsavel;
	}

	@Column(name = "nm_responsavel", length = 200)
	@Length(max = 200)
	public String getNomeResponsavel() {
		return nomeResponsavel;
	}

	public void setNomeResponsavel(String nomeResponsavel) {
		this.nomeResponsavel = nomeResponsavel;
	}

	@Column(name = "tp_logradouro_responsavel", length = 15)
	@Length(max = 15)
	public String getTipoLogradouroResponsavel() {
		return tipoLogradouroResponsavel;
	}

	public void setTipoLogradouroResponsavel(String tipoLogradouroResponsavel) {
		this.tipoLogradouroResponsavel = tipoLogradouroResponsavel;
	}

	@Column(name = "ds_logradouro_responsavel", length = 200)
	@Length(max = 200)
	public String getDescricaoLogradouroResponsavel() {
		return descricaoLogradouroResponsavel;
	}

	public void setDescricaoLogradouroResponsavel(String descricaoLogradouroResponsavel) {
		this.descricaoLogradouroResponsavel = descricaoLogradouroResponsavel;
	}

	@Column(name = "nr_logradouro_responsavel", length = 15)
	@Length(max = 15)
	public String getNumLogradouroResponsavel() {
		return numLogradouroResponsavel;
	}

	public void setNumLogradouroResponsavel(String numLogradouroResponsavel) {
		this.numLogradouroResponsavel = numLogradouroResponsavel;
	}

	@Column(name = "ds_complemento_responsavel", length = 200)
	@Length(max = 200)
	public String getDescricaoComplementoResponsavel() {
		return descricaoComplementoResponsavel;
	}

	public void setDescricaoComplementoResponsavel(String descricaoComplementoResponsavel) {
		this.descricaoComplementoResponsavel = descricaoComplementoResponsavel;
	}

	@Column(name = "ds_bairro_responsavel", length = 150)
	@Length(max = 150)
	public String getDescricaoBairroResponsavel() {
		return descricaoBairroResponsavel;
	}

	public void setDescricaoBairroResponsavel(String descricaoBairroResponsavel) {
		this.descricaoBairroResponsavel = descricaoBairroResponsavel;
	}

	@Column(name = "nr_cep_responsavel", length = 15)
	@Length(max = 15)
	public String getNumCepResponsavel() {
		return numCepResponsavel;
	}

	public void setNumCepResponsavel(String numCepResponsavel) {
		this.numCepResponsavel = numCepResponsavel;
	}

	@Column(name = "cd_municipio_responsavel", length = 15)
	@Length(max = 15)
	public String getCodigoMunicipioResponsavel() {
		return codigoMunicipioResponsavel;
	}

	public void setCodigoMunicipioResponsavel(String codigoMunicipioResponsavel) {
		this.codigoMunicipioResponsavel = codigoMunicipioResponsavel;
	}

	@Column(name = "ds_municipio_responsavel", length = 150)
	@Length(max = 150)
	public String getDescricaoMunicipioResponsavel() {
		return descricaoMunicipioResponsavel;
	}

	public void setDescricaoMunicipioResponsavel(String descricaoMunicipioResponsavel) {
		this.descricaoMunicipioResponsavel = descricaoMunicipioResponsavel;
	}

	@Column(name = "cd_uf_responsavel", length = 2)
	@Length(max = 2)
	public String getCodigoUfResponsavel() {
		return codigoUfResponsavel;
	}

	public void setCodigoUfResponsavel(String codigoUfResponsavel) {
		this.codigoUfResponsavel = codigoUfResponsavel;
	}

	@Column(name = "nr_ddd_telefone_responsavel", length = 12)
	@Length(max = 12)
	public String getNumDddTelefoneResponsavel() {
		return numDddTelefoneResponsavel;
	}

	public void setNumDddTelefoneResponsavel(String numDddTelefoneResponsavel) {
		this.numDddTelefoneResponsavel = numDddTelefoneResponsavel;
	}

	@Column(name = "nr_telefone_responsavel", length = 15)
	@Length(max = 15)
	public String getNumTelefoneResponsavel() {
		return numTelefoneResponsavel;
	}

	public void setNumTelefoneResponsavel(String numTelefoneResponsavel) {
		this.numTelefoneResponsavel = numTelefoneResponsavel;
	}

	@Column(name = "ds_correio_eltrnco_responsavel", length = 200)
	@Length(max = 100)
	public String getCorreioEletronicoResponsavel() {
		return correioEletronicoResponsavel;
	}

	public void setCorreioEletronicoResponsavel(String correioEletronicoResponsavel) {
		this.correioEletronicoResponsavel = correioEletronicoResponsavel;
	}

	@Column(name = "cd_qualificacao_responsavel", length = 15)
	@Length(max = 15)
	public String getCodigoQualificacaoResponsavel() {
		return codigoQualificacaoResponsavel;
	}

	public void setCodigoQualificacaoResponsavel(String codigoQualificacaoResponsavel) {
		this.codigoQualificacaoResponsavel = codigoQualificacaoResponsavel;
	}

	@Column(name = "ds_qualificacao_responsavel", length = 200)
	@Length(max = 200)
	public String getDescricaoQualificacaoResponsavel() {
		return descricaoQualificacaoResponsavel;
	}

	public void setDescricaoQualificacaoResponsavel(String descricaoQualificacaoResponsavel) {
		this.descricaoQualificacaoResponsavel = descricaoQualificacaoResponsavel;
	}

	@Column(name = "cd_situacao_atualizacao", length = 15)
	@Length(max = 15)
	public String getCodigoSituacaoAtualizacao() {
		return codigoSituacaoAtualizacao;
	}

	public void setCodigoSituacaoAtualizacao(String codigoSituacaoAtualizacao) {
		this.codigoSituacaoAtualizacao = codigoSituacaoAtualizacao;
	}

	@Column(name = "tp_logradouro", length = 30)
	@Length(max = 30)
	public String getTipoLogradouro() {
		return tipoLogradouro;
	}

	public void setTipoLogradouro(String tipoLogradouro) {
		this.tipoLogradouro = tipoLogradouro;
	}

	@Column(name = "ds_logradouro", length = 200)
	@Length(max = 200)
	public String getDescricaoLogradouro() {
		return descricaoLogradouro;
	}

	public void setDescricaoLogradouro(String descricaoLogradouro) {
		this.descricaoLogradouro = descricaoLogradouro;
	}

	@Column(name = "nr_logradouro", length = 15)
	@Length(max = 15)
	public String getNumLogradouro() {
		return numLogradouro;
	}

	public void setNumLogradouro(String numLogradouro) {
		this.numLogradouro = numLogradouro;
	}

	@Column(name = "ds_complemento", length = 100)
	@Length(max = 100)
	public String getDescricaoComplemento() {
		if (descricaoComplemento != null && descricaoComplemento.length()>100) { 
			descricaoComplemento = descricaoComplemento.substring(0,100);
		}
		return descricaoComplemento;
	}

	public void setDescricaoComplemento(String descricaoComplemento) {
		if (descricaoComplemento != null && descricaoComplemento.length()>100) { 
			descricaoComplemento = descricaoComplemento.substring(0,100);
		}
		this.descricaoComplemento = descricaoComplemento;
	}

	@Column(name = "ds_bairro", length = 150)
	@Length(max = 150)
	public String getDescricaoBairro() {
		return descricaoBairro;
	}

	public void setDescricaoBairro(String descricaoBairro) {
		this.descricaoBairro = descricaoBairro;
	}

	@Column(name = "cd_bairro", length = 15)
	@Length(max = 15)
	public String getCodigoBairro() {
		return codigoBairro;
	}

	public void setCodigoBairro(String codigoBairro) {
		this.codigoBairro = codigoBairro;
	}

	@Column(name = "cd_municipio", length = 15)
	@Length(max = 15)
	public String getCodigoMunicipio() {
		return codigoMunicipio;
	}

	public void setCodigoMunicipio(String codigoMunicipio) {
		this.codigoMunicipio = codigoMunicipio;
	}

	@Column(name = "ds_municipio", length = 150)
	@Length(max = 150)
	public String getDescricaoMunicipio() {
		return descricaoMunicipio;
	}

	public void setDescricaoMunicipio(String descricaoMunicipio) {
		this.descricaoMunicipio = descricaoMunicipio;
	}

	@Column(name = "cd_uf", length = 2)
	@Length(max = 2)
	public String getSiglaUf() {
		return siglaUf;
	}

	public void setSiglaUf(String siglaUf) {
		this.siglaUf = siglaUf;
	}

	@Column(name = "nr_cep", length = 15)
	@Length(max = 15)
	public String getNumCep() {
		return numCep;
	}

	public void setNumCep(String numCep) {
		this.numCep = numCep;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DadosReceitaPessoaJuridica> getEntityClass() {
		return DadosReceitaPessoaJuridica.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdDadosReceitaPessoaJuridica());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
