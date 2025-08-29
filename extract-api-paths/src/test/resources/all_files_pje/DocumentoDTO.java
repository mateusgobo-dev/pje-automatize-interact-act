package br.jus.pje.nucleo.dto.portal;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

public class DocumentoDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	
	private Long idDocumentoPai;
	
	@NotNull
	private String urlConteudo;

	@NotNull
	private TipoDocumentoDTO tipoDocumento;
	
    @NotNull
    @JsonFormat(pattern="dd/MM/yyyy HH:mm:ss")
    private Date dataHora;

    @Size(max = 255)
    private String descricao;

    @Size(max = 255)
    private String mimeType;

    @NotNull
    private Boolean principal;

    @NotNull
    private Integer nivelSigilo;

    @NotNull
    private Integer tamanho;

    @Size(max = 40)
    private String hash;
    
    private String cadeiaCertificado;
    
    private String assinatura;
    
    private Integer ordem;

	@Override
	public String toString() {
		return "DocumentoDTO [urlConteudo=" + urlConteudo + ", tipoDocumento=" + tipoDocumento + ", descricao="
				+ descricao + ", mimeType=" + mimeType + "]";
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getIdDocumentoPai() {
		return idDocumentoPai;
	}

	public void setIdDocumentoPai(Long idDocumentoPai) {
		this.idDocumentoPai = idDocumentoPai;
	}

	public String getUrlConteudo() {
		return urlConteudo;
	}

	public void setUrlConteudo(String urlConteudo) {
		this.urlConteudo = urlConteudo;
	}

	public TipoDocumentoDTO getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumentoDTO tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public Date getDataHora() {
		return dataHora;
	}

	public void setDataHora(Date dataHora) {
		this.dataHora = dataHora;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}

	public Integer getNivelSigilo() {
		return nivelSigilo;
	}

	public void setNivelSigilo(Integer nivelSigilo) {
		this.nivelSigilo = nivelSigilo;
	}

	public Integer getTamanho() {
		return tamanho;
	}

	public void setTamanho(Integer tamanho) {
		this.tamanho = tamanho;
	}

	public String getAssinatura() {
		return assinatura;
	}

	public void setAssinatura(String assinatura) {
		this.assinatura = assinatura;
	}

	public String getCadeiaCertificado() {
		return cadeiaCertificado;
	}

	public void setCadeiaCertificado(String cadeiaCertificado) {
		this.cadeiaCertificado = cadeiaCertificado;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Integer getOrdem() {
		return ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}
	
	
}
