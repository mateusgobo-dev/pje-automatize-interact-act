package br.jus.cnj.pje.entidades.vo;

import java.util.Date;


public class DocumentoAssinaturaVO implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private Integer idProcessoDocumento;
	private Date dataInclusao;
	private String md5;
	private Boolean binario;
		
	public DocumentoAssinaturaVO(){
		
	}

	public Integer getIdProcessoDocumento() {
		return idProcessoDocumento;
	}

	public void setIdProcessoDocumento(Integer idProcessoDocumento) {
		this.idProcessoDocumento = idProcessoDocumento;
	}

	public Date getDataInclusao() {
		return dataInclusao;
	}

	public void setDataInclusao(Date dataInclusao) {
		this.dataInclusao = dataInclusao;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public Boolean getBinario() {
		return binario;
	}

	public void setBinario(Boolean binario) {
		this.binario = binario;
	}
}