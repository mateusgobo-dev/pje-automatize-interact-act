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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;


@Entity
@Table(name = DocumentoPessoa.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_documento_pessoa", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_documento_pessoa"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DocumentoPessoa implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<DocumentoPessoa,Integer> {

	public static final String TABLE_NAME = "tb_documento_pessoa";
	private static final long serialVersionUID = 1L;

	private int idDocumentoPessoa;
	private Pessoa pessoa;
	private TipoProcessoDocumento tipoProcessoDocumento;
	private String documentoHtml;
	private byte[] documentoBin;
	private Pessoa usuarioCadastro;
	private Date dataInclusao;
	private String assinatura;
	private Boolean ativo;
	private String nomeArquivo;
	private String signature;
	private String certChain;

	public DocumentoPessoa() {
	}

	@Id
	@GeneratedValue(generator = "gen_documento_pessoa")
	@Column(name = "id_documento_pessoa", unique = true, nullable = false)
	public int getIdDocumentoPessoa() {
		return this.idDocumentoPessoa;
	}

	public void setIdDocumentoPessoa(int idDocumentoPessoa) {
		this.idDocumentoPessoa = idDocumentoPessoa;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa", nullable = false)
	@NotNull
	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_processo_documento", nullable = false)
	@NotNull
	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_documento_html")
	public String getDocumentoHtml() {
		return documentoHtml;
	}

	public void setDocumentoHtml(String documentoHtml) {
		this.documentoHtml = documentoHtml;
	}

	@Column(name = "ds_documento_bin")
	@Basic(fetch = FetchType.LAZY)
	public byte[] getDocumentoBin() {
		return documentoBin;
	}

	public void setDocumentoBin(byte[] documentoBin) {
		this.documentoBin = documentoBin;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_cadastro", nullable = false)
	@NotNull
	public Pessoa getUsuarioCadastro() {
		return usuarioCadastro;
	}

	public void setUsuarioCadastro(Pessoa usuarioCadastro) {
		this.usuarioCadastro = usuarioCadastro;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inclusao")
	public Date getDataInclusao() {
		return dataInclusao;
	}

	public void setDataInclusao(Date dataInclusao) {
		this.dataInclusao = dataInclusao;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_assinatura_documento")
	public String getAssinatura() {
		return assinatura;
	}

	public void setAssinatura(String assinatura) {
		this.assinatura = assinatura;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_signature")
	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_cert_chain")
	public String getCertChain() {
		return certChain;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	@Column(name = "nm_arquivo", length = 300)
	@Length(max = 300)
	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DocumentoPessoa)) {
			return false;
		}
		DocumentoPessoa other = (DocumentoPessoa) obj;
		if (getIdDocumentoPessoa() != other.getIdDocumentoPessoa()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdDocumentoPessoa();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DocumentoPessoa> getEntityClass() {
		return DocumentoPessoa.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdDocumentoPessoa());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
