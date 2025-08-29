package br.jus.cnj.pje.vo;

import br.jus.pje.nucleo.util.StringUtil;

public class ArquivoAssinadoHash {

	/**
	 * Define o id do arquivo assinado
	 */
	private String id;
		
	/**
	 * Define o cod ini do arquivo assinado
	 */
	private String codIni;
	
	/**
	 * Define o hash do arquivo assinado
	 */
	private String hash;
	
	/**
	 * Define a assinatura do arquivo assinado
	 */
	private String assinatura;
		
	/**
	 * Define a cadeia de certificado do signatario
	 */
	private String cadeiaCertificado;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getCodIni() {
		return codIni;
	}
	
	public void setCodIni(String codIni) {
		this.codIni = codIni;
	}
	
	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
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

	public Integer getIdEmInteger() {
		return !StringUtil.isNullOrEmpty(getId()) ? Integer.valueOf(getId()) : null;
	}	
	
	public String toString() {
		StringBuilder sb = new StringBuilder(300)
				.append('#').append(getId()).append(' ').append(getCodIni()).append(" - ").append(getHash());
		return sb.toString();
	}
}