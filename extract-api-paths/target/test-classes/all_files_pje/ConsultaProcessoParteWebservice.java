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
package br.jus.pje.nucleo.entidades.ws.consulta;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "vs_processo_parte_webservice")
public class ConsultaProcessoParteWebservice implements Serializable {

	private static final long serialVersionUID = 1L;

	private int idProcessoParte;
	private int idProcessoTrf;
	private String tipoPessoa;
	private String participacao;
	private boolean segredo;
	private String tipoParte;
	private String email;
	private String nome;
	private String etnia;
	private String escolaridade;
	private String estadoCivil;
	private String nomeTipoPessoa;
	private String profissao;
	private String numeroCpf;
	private Character sexo;
	private Date dataNascimento;
	private String nomeGenitora;
	private String nomeGenitor;
	private String municipioPessoa;
	private String numeroTituloEleitor;
	private String numeroRegistroGeral;
	private String orgaoExpedidorRg;
	private Date dataExpedicaoRg;
	private Date dataObito;
	private String numeroDddCelular;
	private String numeroCelular;
	private String numeroDddTelefoneResidencial;
	private String numeroTelefoneResidencial;
	private String numeroDddTelefoneComercial;
	private String numeroTelefoneComercial;
	private String incapaz;
	private String numeroCnpj;
	private String nomeFantasia;
	private Date dataAbertura;
	private Date dataFimAtividade;
	private String numeroCpfResponsavel;
	private String nomeResponsavel;
	private String numeroResgistroJuntaComercial;
	private String ramoAtividade;
	private String codigoEstado;
	private Boolean orgaoPublico;
	private Boolean atraiCompetencia;

	@Id
	@Column(name = "id_processo_parte", insertable = false, updatable = false)
	public int getIdProcessoParte() {
		return idProcessoParte;
	}

	public void setIdProcessoParte(int idProcessoParte) {
		this.idProcessoParte = idProcessoParte;
	}

	@Column(name = "id_processo_trf", insertable = false, updatable = false)
	public int getIdProcessoTrf() {
		return idProcessoTrf;
	}

	public void setIdProcessoTrf(int idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}

	@Column(name = "in_tipo_pessoa", insertable = false, updatable = false)
	public String getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(String tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	@Column(name = "in_participacao", insertable = false, updatable = false)
	public String getParticipacao() {
		return participacao;
	}

	public void setParticipacao(String participacao) {
		this.participacao = participacao;
	}

	@Column(name = "in_segredo", insertable = false, updatable = false)
	public boolean getSegredo() {
		return segredo;
	}

	public void setSegredo(boolean segredo) {
		this.segredo = segredo;
	}

	@Column(name = "ds_tipo_parte", insertable = false, updatable = false)
	public String getTipoParte() {
		return tipoParte;
	}

	public void setTipoParte(String tipoParte) {
		this.tipoParte = tipoParte;
	}

	@Column(name = "ds_email", insertable = false, updatable = false)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "ds_nome", insertable = false, updatable = false)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "ds_etnia", insertable = false, updatable = false)
	public String getEtnia() {
		return etnia;
	}

	public void setEtnia(String etnia) {
		this.etnia = etnia;
	}

	@Column(name = "ds_escolaridade", insertable = false, updatable = false)
	public String getEscolaridade() {
		return escolaridade;
	}

	public void setEscolaridade(String escolaridade) {
		this.escolaridade = escolaridade;
	}

	@Column(name = "ds_estado_civil", insertable = false, updatable = false)
	public String getEstadoCivil() {
		return estadoCivil;
	}

	public void setEstadoCivil(String estadoCivil) {
		this.estadoCivil = estadoCivil;
	}

	@Column(name = "ds_tipo_pessoa", insertable = false, updatable = false)
	public String getNomeTipoPessoa() {
		return nomeTipoPessoa;
	}

	public void setNomeTipoPessoa(String nomeTipoPessoa) {
		this.nomeTipoPessoa = nomeTipoPessoa;
	}

	@Column(name = "ds_profissao", insertable = false, updatable = false)
	public String getProfissao() {
		return profissao;
	}

	public void setProfissao(String profissao) {
		this.profissao = profissao;
	}

	@Column(name = "nr_cpf", insertable = false, updatable = false)
	public String getNumeroCpf() {
		return numeroCpf;
	}

	public void setNumeroCpf(String numeroCpf) {
		this.numeroCpf = numeroCpf;
	}

	@Column(name = "in_sexo", insertable = false, updatable = false)
	public Character getSexo() {
		return sexo;
	}

	public void setSexo(Character sexo) {
		this.sexo = sexo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_nascimento", insertable = false, updatable = false)
	public Date getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	@Column(name = "nm_genitora", insertable = false, updatable = false)
	public String getNomeGenitora() {
		return nomeGenitora;
	}

	public void setNomeGenitora(String nomeGenitora) {
		this.nomeGenitora = nomeGenitora;
	}

	@Column(name = "nm_genitor", insertable = false, updatable = false)
	public String getNomeGenitor() {
		return nomeGenitor;
	}

	public void setNomeGenitor(String nomeGenitor) {
		this.nomeGenitor = nomeGenitor;
	}

	@Column(name = "nr_titulo_eleitor", insertable = false, updatable = false)
	public String getNumeroTituloEleitor() {
		return numeroTituloEleitor;
	}

	public void setNumeroTituloEleitor(String numeroTituloEleitor) {
		this.numeroTituloEleitor = numeroTituloEleitor;
	}

	@Column(name = "nr_registro_geral", insertable = false, updatable = false)
	public String getNumeroRegistroGeral() {
		return numeroRegistroGeral;
	}

	public void setNumeroRegistroGeral(String numeroRegistroGeral) {
		this.numeroRegistroGeral = numeroRegistroGeral;
	}

	@Column(name = "nm_orgao_expedidor_rg", insertable = false, updatable = false)
	public String getOrgaoExpedidorRg() {
		return orgaoExpedidorRg;
	}

	public void setOrgaoExpedidorRg(String orgaoExpedidorRg) {
		this.orgaoExpedidorRg = orgaoExpedidorRg;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_expedicao_rg", insertable = false, updatable = false)
	public Date getDataExpedicaoRg() {
		return dataExpedicaoRg;
	}

	public void setDataExpedicaoRg(Date dataExpedicaoRg) {
		this.dataExpedicaoRg = dataExpedicaoRg;
	}

	@Column(name = "dt_obito", insertable = false, updatable = false)
	public Date getDataObito() {
		return dataObito;
	}

	public void setDataObito(Date dataObito) {
		this.dataObito = dataObito;
	}

	@Column(name = "nr_ddd_celular", insertable = false, updatable = false)
	public String getNumeroDddCelular() {
		return numeroDddCelular;
	}

	public void setNumeroDddCelular(String numeroDddCelular) {
		this.numeroDddCelular = numeroDddCelular;
	}

	@Column(name = "nr_celular", insertable = false, updatable = false)
	public String getNumeroCelular() {
		return numeroCelular;
	}

	public void setNumeroCelular(String numeroCelular) {
		this.numeroCelular = numeroCelular;
	}

	@Column(name = "nr_ddd_tel_residencial", insertable = false, updatable = false)
	public String getNumeroDddTelefoneResidencial() {
		return numeroDddTelefoneResidencial;
	}

	public void setNumeroDddTelefoneResidencial(String numeroDddTelefoneResidencial) {
		this.numeroDddTelefoneResidencial = numeroDddTelefoneResidencial;
	}

	@Column(name = "nr_tel_residencial", insertable = false, updatable = false)
	public String getNumeroTelefoneResidencial() {
		return numeroTelefoneResidencial;
	}

	public void setNumeroTelefoneResidencial(String numeroTelefoneResidencial) {
		this.numeroTelefoneResidencial = numeroTelefoneResidencial;
	}

	@Column(name = "nr_ddd_tel_comercial", insertable = false, updatable = false)
	public String getNumeroDddTelefoneComercial() {
		return numeroDddTelefoneComercial;
	}

	public void setNumeroDddTelefoneComercial(String numeroDddTelefoneComercial) {
		this.numeroDddTelefoneComercial = numeroDddTelefoneComercial;
	}

	@Column(name = "nr_tel_comercial", insertable = false, updatable = false)
	public String getNumeroTelefoneComercial() {
		return numeroTelefoneComercial;
	}

	public void setNumeroTelefoneComercial(String numeroTelefoneComercial) {
		this.numeroTelefoneComercial = numeroTelefoneComercial;
	}

	@Column(name = "in_incapaz", insertable = false, updatable = false)
	public String getIncapaz() {
		return incapaz;
	}

	public void setIncapaz(String incapaz) {
		this.incapaz = incapaz;
	}

	@Column(name = "nr_cnpj", insertable = false, updatable = false)
	public String getNumeroCnpj() {
		return numeroCnpj;
	}

	public void setNumeroCnpj(String numeroCnpj) {
		this.numeroCnpj = numeroCnpj;
	}

	@Column(name = "nm_fantasia", insertable = false, updatable = false)
	public String getNomeFantasia() {
		return nomeFantasia;
	}

	public void setNomeFantasia(String nomeFantasia) {
		this.nomeFantasia = nomeFantasia;
	}

	@Column(name = "dt_abertura", insertable = false, updatable = false)
	public Date getDataAbertura() {
		return dataAbertura;
	}

	public void setDataAbertura(Date dataAbertura) {
		this.dataAbertura = dataAbertura;
	}

	@Column(name = "dt_fim_atividade", insertable = false, updatable = false)
	public Date getDataFimAtividade() {
		return dataFimAtividade;
	}

	public void setDataFimAtividade(Date dataFimAtividade) {
		this.dataFimAtividade = dataFimAtividade;
	}

	@Column(name = "nr_cpf_responsavel", insertable = false, updatable = false)
	public String getNumeroCpfResponsavel() {
		return numeroCpfResponsavel;
	}

	public void setNumeroCpfResponsavel(String numeroCpfResponsavel) {
		this.numeroCpfResponsavel = numeroCpfResponsavel;
	}

	@Column(name = "nr_registro_junta_comercial", insertable = false, updatable = false)
	public String getNomeResponsavel() {
		return nomeResponsavel;
	}

	public void setNomeResponsavel(String nomeResponsavel) {
		this.nomeResponsavel = nomeResponsavel;
	}

	@Column(name = "nr_registro_junta_comercial", insertable = false, updatable = false)
	public String getNumeroResgistroJuntaComercial() {
		return numeroResgistroJuntaComercial;
	}

	public void setNumeroResgistroJuntaComercial(String numeroResgistroJuntaComercial) {
		this.numeroResgistroJuntaComercial = numeroResgistroJuntaComercial;
	}

	@Column(name = "ds_ramo_atividade", insertable = false, updatable = false)
	public String getRamoAtividade() {
		return ramoAtividade;
	}

	public void setRamoAtividade(String ramoAtividade) {
		this.ramoAtividade = ramoAtividade;
	}

	@Column(name = "cd_estado_pj", insertable = false, updatable = false)
	public String getCodigoEstado() {
		return codigoEstado;
	}

	public void setCodigoEstado(String codigoEstado) {
		this.codigoEstado = codigoEstado;
	}

	@Column(name = "in_atrai_competencia", insertable = false, updatable = false)
	public Boolean getAtraiCompetencia() {
		return atraiCompetencia;
	}

	public void setAtraiCompetencia(Boolean atraiCompetencia) {
		this.atraiCompetencia = atraiCompetencia;
	}

	@Column(name = "ds_municipio", insertable = false, updatable = false)
	public String getMunicipioPessoa() {
		return municipioPessoa;
	}

	public void setMunicipioPessoa(String municipioPessoa) {
		this.municipioPessoa = municipioPessoa;
	}

	@Column(name = "in_orgao_publico", insertable = false, updatable = false)
	public Boolean getOrgaoPublico() {
		return orgaoPublico;
	}

	public void setOrgaoPublico(Boolean orgaoPublico) {
		this.orgaoPublico = orgaoPublico;
	}

}
