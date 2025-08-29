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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name = "vs_processo_trf_webservice")
public class ConsultaProcessoWebservice implements Serializable {

	private static final long serialVersionUID = 1L;

	private int idProcessoTrf;
	private String numeroProcesso;
	private Date dataInicio;
	private String nomeUsuarioCadastro;
	private String numeroCpfUsuarioCadastro;
	private String inInicial;
	private String codStatusProcesso;
	private Double valorCausa;
	private Date dataAutuacao;
	private Boolean segredoJustica;
	private String segredoJusticaObservacao;
	private Boolean justicaGratuita;
	private String codClasseJudicial;
	private Boolean tutelaLiminar;
	private Boolean apreciadoTutelaLiminar;
	private String apreciadoSegredo;
	private Date dataDistribuicao;
	private Integer sequencia;
	private Integer digitoVerificador;
	private Integer ano;
	private Integer identificador;
	private Integer origem;
	private String descricaoEnderecoWsdl;

	private List<ConsultaAssuntoTrf> assuntoTrfList;
	private List<ConsultaProcessoDocumentoWebservice> documentoWebservicesList;
	private List<ConsultaProcessoParteWebservice> partesList;
	private List<String> prioridade = new ArrayList<String>(0);
	private List<ConsultaProcessoConexoWebservice> processosConexosList;
	private List<ConsultaProcessoEventoWebservice> processosEventosList;
	private List<ConsultaProcessoExpedienteWebservice> processosExpedientesList;

	@Id
	@Column(name = "id_processo", insertable = false, updatable = false)
	public int getIdProcessoTrf() {
		return idProcessoTrf;
	}

	public void setIdProcessoTrf(int idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}

	@Column(name = "nr_processo", insertable = false, updatable = false)
	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicio", insertable = false, updatable = false)
	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	@Column(name = "nm_usuario_cadastro", insertable = false, updatable = false)
	public String getNomeUsuarioCadastro() {
		return nomeUsuarioCadastro;
	}

	public void setNomeUsuarioCadastro(String nomeUsuarioCadastro) {
		this.nomeUsuarioCadastro = nomeUsuarioCadastro;
	}

	@Column(name = "nr_cpf_usuario_cadastro", insertable = false, updatable = false)
	public String getNumeroCpfUsuarioCadastro() {
		return numeroCpfUsuarioCadastro;
	}

	public void setNumeroCpfUsuarioCadastro(String numeroCpfUsuarioCadastro) {
		this.numeroCpfUsuarioCadastro = numeroCpfUsuarioCadastro;
	}

	@Column(name = "in_inicial", insertable = false, updatable = false)
	public String getInInicial() {
		return inInicial;
	}

	public void setInInicial(String inInicial) {
		this.inInicial = inInicial;
	}

	@Column(name = "cd_processo_status", insertable = false, updatable = false)
	public String getCodStatusProcesso() {
		return codStatusProcesso;
	}

	public void setCodStatusProcesso(String codStatusProcesso) {
		this.codStatusProcesso = codStatusProcesso;
	}

	@Column(name = "vl_causa", insertable = false, updatable = false)
	public Double getValorCausa() {
		return valorCausa;
	}

	public void setValorCausa(Double valorCausa) {
		this.valorCausa = valorCausa;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_autuacao", insertable = false, updatable = false)
	public Date getDataAutuacao() {
		return dataAutuacao;
	}

	public void setDataAutuacao(Date dataAutuacao) {
		this.dataAutuacao = dataAutuacao;
	}

	@Column(name = "in_segredo_justica", insertable = false, updatable = false)
	public Boolean getSegredoJustica() {
		return segredoJustica;
	}

	public void setSegredoJustica(Boolean segredoJustica) {
		this.segredoJustica = segredoJustica;
	}

	@Column(name = "ds_observacao_segredo", insertable = false, updatable = false)
	public String getSegredoJusticaObservacao() {
		return segredoJusticaObservacao;
	}

	public void setSegredoJusticaObservacao(String segredoJusticaObservacao) {
		this.segredoJusticaObservacao = segredoJusticaObservacao;
	}

	@Column(name = "in_justica_gratuita", insertable = false, updatable = false)
	public Boolean getJusticaGratuita() {
		return justicaGratuita;
	}

	public void setJusticaGratuita(Boolean justicaGratuita) {
		this.justicaGratuita = justicaGratuita;
	}

	@Column(name = "in_tutela_liminar")
	public Boolean getTutelaLiminar() {
		return this.tutelaLiminar;
	}

	public void setTutelaLiminar(Boolean tutelaLiminar) {
		this.tutelaLiminar = tutelaLiminar;
	}

	@Column(name = "in_apreciado_tutela_liminar")
	public Boolean getApreciadoTutelaLiminar() {
		return apreciadoTutelaLiminar;
	}

	public void setApreciadoTutelaLiminar(Boolean apreciadoTutelaLiminar) {
		this.apreciadoTutelaLiminar = apreciadoTutelaLiminar;
	}

	@Column(name = "cd_classe_judicial", insertable = false, updatable = false)
	public String getCodClasseJudicial() {
		return codClasseJudicial;
	}

	public void setCodClasseJudicial(String codClasseJudicial) {
		this.codClasseJudicial = codClasseJudicial;
	}

	@Column(name = "in_apreciado_segredo", insertable = false, updatable = false)
	public String getApreciadoSegredo() {
		return apreciadoSegredo;
	}

	public void setApreciadoSegredo(String apreciadoSegredo) {
		this.apreciadoSegredo = apreciadoSegredo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_distribuicao", insertable = false, updatable = false)
	public Date getDataDistribuicao() {
		return dataDistribuicao;
	}

	public void setDataDistribuicao(Date dataDistribuicao) {
		this.dataDistribuicao = dataDistribuicao;
	}

	@Column(name = "nr_sequencia", insertable = false, updatable = false)
	public Integer getSequencia() {
		return sequencia;
	}

	public void setSequencia(Integer sequencia) {
		this.sequencia = sequencia;
	}

	@Column(name = "nr_digito_verificador", insertable = false, updatable = false)
	public Integer getDigitoVerificador() {
		return digitoVerificador;
	}

	public void setDigitoVerificador(Integer digitoVerificador) {
		this.digitoVerificador = digitoVerificador;
	}

	@Column(name = "nr_ano", insertable = false, updatable = false)
	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	@Column(name = "nr_identificacao_orgao_justica", insertable = false, updatable = false)
	public Integer getIdentificador() {
		return identificador;
	}

	public void setIdentificador(Integer identificador) {
		this.identificador = identificador;
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "tb_processo_assunto", joinColumns = { @JoinColumn(name = "id_processo_trf", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_assunto_trf", nullable = false, updatable = false) })
	public List<ConsultaAssuntoTrf> getAssuntoTrfList() {
		return this.assuntoTrfList;
	}

	public void setAssuntoTrfList(List<ConsultaAssuntoTrf> assuntoTrfList) {
		this.assuntoTrfList = assuntoTrfList;
	}

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "tb_processo_documento", joinColumns = { @JoinColumn(name = "id_processo", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_processo_documento", nullable = false, updatable = false) })
	public List<ConsultaProcessoDocumentoWebservice> getDocumentoWebservicesList() {
		return documentoWebservicesList;
	}

	public void setDocumentoWebservicesList(List<ConsultaProcessoDocumentoWebservice> documentoWebservicesList) {
		this.documentoWebservicesList = documentoWebservicesList;
	}

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "tb_processo_parte", joinColumns = { @JoinColumn(name = "id_processo_trf", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_processo_parte", nullable = false, updatable = false) })
	public List<ConsultaProcessoParteWebservice> getPartesList() {
		return partesList;
	}

	public void setPartesList(List<ConsultaProcessoParteWebservice> partesList) {
		this.partesList = partesList;
	}

	@Column(name = "ds_descricao", insertable = false, updatable = false)
	public String getDescricaoEnderecoWsdl() {
		return descricaoEnderecoWsdl;
	}

	public void setDescricaoEnderecoWsdl(String descricaoEnderecoWsdl) {
		this.descricaoEnderecoWsdl = descricaoEnderecoWsdl;
	}

	@Transient
	public List<String> getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(List<String> prioridade) {
		this.prioridade = prioridade;
	}

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "tb_processo_trf_conexao", joinColumns = { @JoinColumn(name = "id_processo_trf", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_processo_trf_conexao", nullable = false, updatable = false) })
	public List<ConsultaProcessoConexoWebservice> getProcessosConexosList() {
		return processosConexosList;
	}

	public void setProcessosConexosList(List<ConsultaProcessoConexoWebservice> processosConexosList) {
		this.processosConexosList = processosConexosList;
	}

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "tb_processo_evento", joinColumns = { @JoinColumn(name = "id_processo", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_processo_evento", nullable = false, updatable = false) })
	public List<ConsultaProcessoEventoWebservice> getProcessosEventosList() {
		return processosEventosList;
	}

	public void setProcessosEventosList(List<ConsultaProcessoEventoWebservice> processosEventosList) {
		this.processosEventosList = processosEventosList;
	}

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "tb_processo_expediente", joinColumns = { @JoinColumn(name = "id_processo_trf", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_processo_expediente", nullable = false, updatable = false) })
	public List<ConsultaProcessoExpedienteWebservice> getProcessosExpedientesList() {
		return processosExpedientesList;
	}

	public void setProcessosExpedientesList(List<ConsultaProcessoExpedienteWebservice> processosExpedientesList) {
		this.processosExpedientesList = processosExpedientesList;
	}

	@Column(name = "nr_origem_processo", insertable = false, updatable = false)
	public Integer getOrigem() {
		return origem;
	}

	public void setOrigem(Integer origem) {
		this.origem = origem;
	}

	@Override
	public String toString() {
		return numeroProcesso;
	}

}
