package br.com.infox.core.certificado;

import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Classe que contém dados da assinatura digital e do signatário de um documento
 * @author Leonardo Inácio
 *
 */
public class DadosAssinatura {
	public String nome;

	public String commonName;

	public String cadastroMF;

	public String assinatura;

	public Date dataAssinatura;

	public String certChain;

	public String issuer;

	public X509Certificate certificate;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getCadastroMF() {
		return cadastroMF;
	}

	public void setCadastroMF(String cadastroMF) {
		this.cadastroMF = cadastroMF;
	}

	public Date getDataAssinatura() {
		return dataAssinatura;
	}

	public void setDataAssinatura(Date dataAssinatura) {
		this.dataAssinatura = dataAssinatura;
	}

	public X509Certificate getCertificate() {
		return certificate;
	}

	public void setCertificate(X509Certificate certificate) {
		this.certificate = certificate;
	}

	public String getAssinatura() {
		return assinatura;
	}

	public void setAssinatura(String assinatura) {
		this.assinatura = assinatura;
	}

	public String getCertChain() {
		return certChain;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
}