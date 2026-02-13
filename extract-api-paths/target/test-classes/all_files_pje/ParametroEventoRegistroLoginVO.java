package br.jus.cnj.pje.entidades.vo;

import java.io.Serializable;

public class ParametroEventoRegistroLoginVO implements Serializable {
	private static final long serialVersionUID = 1647197857671935250L;
	private Integer idUsuario;
	private boolean deveBloquearSenha;
	private boolean inicializaFalhasAutenticacao;
	private String ip;
	private boolean isLogouComCertificado;
	private boolean temCertificado;

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public boolean isDeveBloquearSenha() {
		return deveBloquearSenha;
	}

	public void setDeveBloquearSenha(boolean deveBloquearSenha) {
		this.deveBloquearSenha = deveBloquearSenha;
	}

	public boolean isInicializaFalhasAutenticacao() {
		return inicializaFalhasAutenticacao;
	}

	public void setInicializaFalhasAutenticacao(boolean inicializaFalhasAutenticacao) {
		this.inicializaFalhasAutenticacao = inicializaFalhasAutenticacao;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public boolean isLogouComCertificado() {
		return isLogouComCertificado;
	}

	public void setLogouComCertificado(boolean isLogouComCertificado) {
		this.isLogouComCertificado = isLogouComCertificado;
	}

	public boolean isTemCertificado() {
		return temCertificado;
	}

	public void setTemCertificado(boolean temCertificado) {
		this.temCertificado = temCertificado;
	}
}
