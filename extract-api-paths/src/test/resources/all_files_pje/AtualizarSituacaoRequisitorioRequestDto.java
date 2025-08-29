package br.jus.cnj.pje.webservice.client;

import java.io.Serializable;

public class AtualizarSituacaoRequisitorioRequestDto  implements Serializable {

	private static final long serialVersionUID = -1317877505059264262L;

	private String codSituacao;
	private String motivoSituacao;
	private String autorSituacao;
	private String localizacao;
	private String papel;

	public String getCodSituacao() {
		return codSituacao;
	}

	public void setCodSituacao(String codSituacao) {
		this.codSituacao = codSituacao;
	}

	public String getMotivoSituacao() {
		return motivoSituacao;
	}

	public void setMotivoSituacao(String motivoSituacao) {
		this.motivoSituacao = motivoSituacao;
	}

	public String getAutorSituacao() {
		return autorSituacao;
	}

	public void setAutorSituacao(String autorSituacao) {
		this.autorSituacao = autorSituacao;
	}

	public String getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(String localizacao) {
		this.localizacao = localizacao;
	}

	public String getPapel() {
		return papel;
	}

	public void setPapel(String papel) {
		this.papel = papel;
	}
	
}
