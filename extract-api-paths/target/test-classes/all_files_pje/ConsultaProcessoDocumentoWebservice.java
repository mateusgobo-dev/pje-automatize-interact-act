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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "vs_proc_documento_webservice")
public class ConsultaProcessoDocumentoWebservice implements Serializable {

	private static final long serialVersionUID = 1L;

	private int idProcessoDocumento;
	private int idProcesso;
	private String processoDocumento;
	private Date dataInclusao;
	private Date dataExclusao;
	private String motivoExclusao;
	private Boolean ativo;
	private String observacaoProcedimento;
	private String numeroDocumento;
	private Boolean documentoSigiloso;
	private String nomeUsuarioInclusao;
	private String numeroCpfUsuarioInclusao;
	private String nomeUsuarioExclusao;
	private String numeroCpfUsuarioExclusao;
	private Integer idUsuarioAlteracao;
	private String nomeUsuarioAlteracao;
	private String numeroCpfUsuarioAlteracao;
	private String papel;
	private String localizacao;
	private String extensao;
	private String modeloDocumento;
	private String nomeArquivo;
	private Date dataInclusaoBin;
	@XmlInlineBinaryData
	private byte[] bytesProcessoDocumento;
	private Integer tamanho;
	private String signature;
	private String certChain;
	private String nomeUsuarioProcessoDocumentoBin;
	private String numeroCpfProcessoDocumentoBin;
	private String nomeUsuarioUltimoAssinar;
	private String numeroCpfUsuarioUltimoAssinar;
	private String md5Documento;
	private String tipoProcessoDocumento;
	private Integer idProcessoDocumentoBin;
	private List<ConsultaProcessoBinAssinaturaWebservice> processoBinAssinaturaWebservices;

	@Id
	@Column(name = "id_processo_documento", insertable = false, updatable = false)
	public int getIdProcessoDocumento() {
		return idProcessoDocumento;
	}

	public void setIdProcessoDocumento(int idProcessoDocumento) {
		this.idProcessoDocumento = idProcessoDocumento;
	}

	@Column(name = "id_processo", insertable = false, updatable = false)
	public int getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(int idProcesso) {
		this.idProcesso = idProcesso;
	}

	@Column(name = "ds_processo_documento", insertable = false, updatable = false)
	public String getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(String processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inclusao", insertable = false, updatable = false)
	public Date getDataInclusao() {
		return dataInclusao;
	}

	public void setDataInclusao(Date dataInclusao) {
		this.dataInclusao = dataInclusao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_exclusao", insertable = false, updatable = false)
	public Date getDataExclusao() {
		return dataExclusao;
	}

	public void setDataExclusao(Date dataExclusao) {
		this.dataExclusao = dataExclusao;
	}

	@Column(name = "ds_motivo_exclusao", insertable = false, updatable = false)
	public String getMotivoExclusao() {
		return motivoExclusao;
	}

	public void setMotivoExclusao(String motivoExclusao) {
		this.motivoExclusao = motivoExclusao;
	}

	@Column(name = "in_ativo", insertable = false, updatable = false)
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "ds_observacao_procedimento", insertable = false, updatable = false)
	public String getObservacaoProcedimento() {
		return observacaoProcedimento;
	}

	public void setObservacaoProcedimento(String observacaoProcedimento) {
		this.observacaoProcedimento = observacaoProcedimento;
	}

	@Column(name = "nr_documento", insertable = false, updatable = false)
	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	@Column(name = "in_documento_sigiloso", insertable = false, updatable = false)
	public Boolean getDocumentoSigiloso() {
		return documentoSigiloso;
	}

	public void setDocumentoSigiloso(Boolean documentoSigiloso) {
		this.documentoSigiloso = documentoSigiloso;
	}

	@Column(name = "ds_nome_usuario_inclusao", insertable = false, updatable = false)
	public String getNomeUsuarioInclusao() {
		return nomeUsuarioInclusao;
	}

	public void setNomeUsuarioInclusao(String nomeUsuarioInclusao) {
		this.nomeUsuarioInclusao = nomeUsuarioInclusao;
	}

	@Column(name = "nr_cpf_usuario_inclusao", insertable = false, updatable = false)
	public String getNumeroCpfUsuarioInclusao() {
		return numeroCpfUsuarioInclusao;
	}

	public void setNumeroCpfUsuarioInclusao(String numeroCpfUsuarioInclusao) {
		this.numeroCpfUsuarioInclusao = numeroCpfUsuarioInclusao;
	}

	@Column(name = "ds_nome_usuario_exclusao", insertable = false, updatable = false)
	public String getNomeUsuarioExclusao() {
		return nomeUsuarioExclusao;
	}

	public void setNomeUsuarioExclusao(String nomeUsuarioExclusao) {
		this.nomeUsuarioExclusao = nomeUsuarioExclusao;
	}

	@Column(name = "nr_cpf_usuario_exclusao", insertable = false, updatable = false)
	public String getNumeroCpfUsuarioExclusao() {
		return numeroCpfUsuarioExclusao;
	}

	public void setNumeroCpfUsuarioExclusao(String numeroCpfUsuarioExclusao) {
		this.numeroCpfUsuarioExclusao = numeroCpfUsuarioExclusao;
	}

	@Column(name = "id_usuario_alteracao", insertable = false, updatable = false)
	public Integer getIdUsuarioAlteracao() {
		return idUsuarioAlteracao;
	}

	public void setIdUsuarioAlteracao(Integer idUsuarioAlteracao) {
		this.idUsuarioAlteracao = idUsuarioAlteracao;
	}

	@Column(name = "ds_nome_usuario_alteracao", insertable = false, updatable = false)
	public String getNomeUsuarioAlteracao() {
		return nomeUsuarioAlteracao;
	}

	public void setNomeUsuarioAlteracao(String nomeUsuarioAlteracao) {
		this.nomeUsuarioAlteracao = nomeUsuarioAlteracao;
	}

	@Column(name = "nr_cpf_usuario_alteracao", insertable = false, updatable = false)
	public String getNumeroCpfUsuarioAlteracao() {
		return numeroCpfUsuarioAlteracao;
	}

	public void setNumeroCpfUsuarioAlteracao(String numeroCpfUsuarioAlteracao) {
		this.numeroCpfUsuarioAlteracao = numeroCpfUsuarioAlteracao;
	}

	@Column(name = "ds_papel", insertable = false, updatable = false)
	public String getPapel() {
		return papel;
	}

	public void setPapel(String papel) {
		this.papel = papel;
	}

	@Column(name = "ds_localizacao", insertable = false, updatable = false)
	public String getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(String localizacao) {
		this.localizacao = localizacao;
	}

	@Column(name = "ds_extensao", insertable = false, updatable = false)
	public String getExtensao() {
		return extensao;
	}

	public void setExtensao(String extensao) {
		this.extensao = extensao;
	}

	@Column(name = "ds_modelo_documento", insertable = false, updatable = false)
	public String getModeloDocumento() {
		return modeloDocumento;
	}

	public void setModeloDocumento(String modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	@Column(name = "nm_arquivo", insertable = false, updatable = false)
	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inclusao_bin", insertable = false, updatable = false)
	public Date getDataInclusaoBin() {
		return dataInclusaoBin;
	}

	public void setDataInclusaoBin(Date dataInclusaoBin) {
		this.dataInclusaoBin = dataInclusaoBin;
	}

	@Column(name = "ob_processo_documento", insertable = false, updatable = false)
	public byte[] getBytesProcessoDocumento() {
		return bytesProcessoDocumento;
	}

	public void setBytesProcessoDocumento(byte[] bytesProcessoDocumento) {
		this.bytesProcessoDocumento = bytesProcessoDocumento;
	}

	@Column(name = "nr_tamanho", insertable = false, updatable = false)
	public Integer getTamanho() {
		return tamanho;
	}

	public void setTamanho(Integer tamanho) {
		this.tamanho = tamanho;
	}

	@Column(name = "ds_signature", insertable = false, updatable = false)
	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	@Column(name = "ds_cert_chain", insertable = false, updatable = false)
	public String getCertChain() {
		return certChain;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	@Column(name = "nm_usuario_proc_documento_bin", insertable = false, updatable = false)
	public String getNomeUsuarioProcessoDocumentoBin() {
		return nomeUsuarioProcessoDocumentoBin;
	}

	public void setNomeUsuarioProcessoDocumentoBin(String nomeUsuarioProcessoDocumentoBin) {
		this.nomeUsuarioProcessoDocumentoBin = nomeUsuarioProcessoDocumentoBin;
	}

	@Column(name = "nr_cpf_processo_documento_bin", insertable = false, updatable = false)
	public String getNumeroCpfProcessoDocumentoBin() {
		return numeroCpfProcessoDocumentoBin;
	}

	public void setNumeroCpfProcessoDocumentoBin(String numeroCpfProcessoDocumentoBin) {
		this.numeroCpfProcessoDocumentoBin = numeroCpfProcessoDocumentoBin;
	}

	@Column(name = "nm_usuario_ultimo_assinar", insertable = false, updatable = false)
	public String getNomeUsuarioUltimoAssinar() {
		return nomeUsuarioUltimoAssinar;
	}

	public void setNomeUsuarioUltimoAssinar(String nomeUsuarioUltimoAssinar) {
		this.nomeUsuarioUltimoAssinar = nomeUsuarioUltimoAssinar;
	}

	@Column(name = "nr_cpf_usuario_ultimo_assinar", insertable = false, updatable = false)
	public String getNumeroCpfUsuarioUltimoAssinar() {
		return numeroCpfUsuarioUltimoAssinar;
	}

	public void setNumeroCpfUsuarioUltimoAssinar(String numeroCpfUsuarioUltimoAssinar) {
		this.numeroCpfUsuarioUltimoAssinar = numeroCpfUsuarioUltimoAssinar;
	}

	@OneToMany(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_documento_bin", insertable = false, updatable = false)
	public List<ConsultaProcessoBinAssinaturaWebservice> getProcessoBinAssinaturaWebservices() {
		return processoBinAssinaturaWebservices;
	}

	public void setProcessoBinAssinaturaWebservices(
			List<ConsultaProcessoBinAssinaturaWebservice> processoBinAssinaturaWebservices) {
		this.processoBinAssinaturaWebservices = processoBinAssinaturaWebservices;
	}

	@Column(name = "ds_md5_documento", insertable = false, updatable = false)
	public String getMd5Documento() {
		return md5Documento;
	}

	public void setMd5Documento(String md5Documento) {
		this.md5Documento = md5Documento;
	}

	@Column(name = "ds_tipo_processo_documento", insertable = false, updatable = false)
	public String getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(String tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	@Column(name = "id_processo_documento_bin", insertable = false, updatable = false)
	public Integer getIdProcessoDocumentoBin() {
		return idProcessoDocumentoBin;
	}

	public void setIdProcessoDocumentoBin(Integer idProcessoDocumentoBin) {
		this.idProcessoDocumentoBin = idProcessoDocumentoBin;
	}

}
