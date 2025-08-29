package br.jus.pje.nucleo.dto.portal;

import java.io.Serializable;

import javax.validation.constraints.NotNull;


public class MensagemDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
    private String protocolo;
	
	@NotNull
	private String url;
	
	@NotNull
	private String callback;

	@NotNull
	private  String notificacaoId;

	public MensagemDTO() {
	}
	
	public MensagemDTO(@NotNull String protocolo, @NotNull String url, @NotNull String callback, @NotNull String notificacaoId) {
		super();
		this.protocolo = protocolo;
		this.url = url;
		this.callback = callback;
		this.notificacaoId = notificacaoId;
	}

	@Override
	public String toString() {
		return "MensagemDTO{" +
				"protocolo='" + protocolo + '\'' +
				", url='" + url + '\'' +
				", callback='" + callback + '\'' +
				", notificacaoId='" + notificacaoId + '\'' +
				'}';
	}

	public String getProtocolo() {
		return protocolo;
	}

	public void setProtocolo(String protocolo) {
		this.protocolo = protocolo;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	public @NotNull String getNotificacaoId() {
		return notificacaoId;
	}

	public void setNotificacaoId(@NotNull String notificacaoId) {
		this.notificacaoId = notificacaoId;
	}

}
