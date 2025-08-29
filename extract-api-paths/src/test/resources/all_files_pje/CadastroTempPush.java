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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tb_cadastro_temp_push")
public class CadastroTempPush implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private Integer idCadastroTempPush;
	private String nrDocumento;
	private String dsEmail;
	private TipoDocumentoIdentificacao tipoDocumentoIdentificacao;
	private Date dtInclusao;
	private Date dtExpiracao;
	private String cdHash;
	private Boolean confirmado;

	@Id
	@GeneratedValue
	@Column(name = "id_cadastro_temp_push")
	public Integer getIdCadastroTempPush() {
		return idCadastroTempPush;
	}

	public void setIdCadastroTempPush(Integer idCadastroTempPush) {
		this.idCadastroTempPush = idCadastroTempPush;
	}

	@Column(name = "nr_documento")
	public String getNrDocumento() {
		return nrDocumento;
	}

	public void setNrDocumento(String nrDocumento) {
		this.nrDocumento = nrDocumento;
	}

	@Column(name = "ds_email")
	public String getDsEmail() {
		return dsEmail;
	}

	public void setDsEmail(String dsEmail) {
		this.dsEmail = dsEmail;
	}

	@ManyToOne
	@JoinColumn(name = "cd_tipo_doc_identificacao")
	public TipoDocumentoIdentificacao getTipoDocumentoIdentificacao() {
		return tipoDocumentoIdentificacao;
	}

	public void setTipoDocumentoIdentificacao(
			TipoDocumentoIdentificacao tipoDocumentoIdentificacao) {
		this.tipoDocumentoIdentificacao = tipoDocumentoIdentificacao;
	}

	@Column(name = "dt_inclusao")
	public Date getDtInclusao() {
		return dtInclusao;
	}

	public void setDtInclusao(Date dtInclusao) {
		this.dtInclusao = dtInclusao;
	}

	@Column(name = "dt_expiracao")
	public Date getDtExpiracao() {
		return dtExpiracao;
	}

	public void setDtExpiracao(Date dtExpiracao) {
		this.dtExpiracao = dtExpiracao;
	}

	@Column(name = "cd_hash")
	public String getCdHash() {
		return cdHash;
	}

	public void setCdHash(String cdHash) {
		this.cdHash = cdHash;
	}

	@Column(name = "in_confirmado")
	public Boolean getConfirmado() {
		return confirmado;
	}

	public void setConfirmado(Boolean confirmado) {
		this.confirmado = confirmado;
	}

}
