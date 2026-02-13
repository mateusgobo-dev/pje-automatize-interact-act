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
@Table(name = "vs_proc_bin_assinat_webservice")
public class ConsultaProcessoBinAssinaturaWebservice implements Serializable {

	private static final long serialVersionUID = 1L;

	private int idProcessoDocumentoBinAssinatura;
	private int idProcessoDocumentoBin;
	private String assinatura;
	private Date dataAssinatura;
	private String certChain;
	private String nomePessoaAssinatura;
	private String numeroCpfPessoaAssinatura;

	@Id
	@Column(name = "id_processo_doc_bin_pessoa", insertable = false, updatable = false)
	public int getIdProcessoDocumentoBinAssinatura() {
		return idProcessoDocumentoBinAssinatura;
	}

	public void setIdProcessoDocumentoBinAssinatura(int idProcessoDocumentoBinAssinatura) {
		this.idProcessoDocumentoBinAssinatura = idProcessoDocumentoBinAssinatura;
	}

	@Column(name = "id_processo_documento_bin", insertable = false, updatable = false)
	public int getIdProcessoDocumentoBin() {
		return idProcessoDocumentoBin;
	}

	public void setIdProcessoDocumentoBin(int idProcessoDocumentoBin) {
		this.idProcessoDocumentoBin = idProcessoDocumentoBin;
	}

	@Column(name = "ds_assinatura", insertable = false, updatable = false)
	public String getAssinatura() {
		return assinatura;
	}

	public void setAssinatura(String assinatura) {
		this.assinatura = assinatura;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_assinatura", insertable = false, updatable = false)
	public Date getDataAssinatura() {
		return dataAssinatura;
	}

	public void setDataAssinatura(Date dataAssinatura) {
		this.dataAssinatura = dataAssinatura;
	}

	@Column(name = "nm_pessoa_assinatura", insertable = false, updatable = false)
	public String getNomePessoaAssinatura() {
		return nomePessoaAssinatura;
	}

	public void setNomePessoaAssinatura(String nomePessoaAssinatura) {
		this.nomePessoaAssinatura = nomePessoaAssinatura;
	}

	@Column(name = "nr_cpf_pessoa_assinatura", insertable = false, updatable = false)
	public String getNumeroCpfPessoaAssinatura() {
		return numeroCpfPessoaAssinatura;
	}

	public void setNumeroCpfPessoaAssinatura(String numeroCpfPessoaAssinatura) {
		this.numeroCpfPessoaAssinatura = numeroCpfPessoaAssinatura;
	}

	@Column(name = "ds_cert_chain", insertable = false, updatable = false)
	public String getCertChain() {
		return certChain;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

}
