package br.jus.pje.nucleo.entidades.acesso;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenSso implements Serializable {
	private static final long serialVersionUID = -8584634845593050735L;

	/**
	 * Token de acesso ao SSO.
	 */
	@JsonProperty("access_token")
	private String accessToken;

	/**
	 * Define quanto tempo (em segundos) o Access Token irá expirar.
	 */
	@JsonProperty("expires_in")
	private Long expiresIn;

	/**
	 * Data e hora em que o Token irá expirar, com 30 segundos subtraídos como
	 * cautela.
	 */
	private LocalDateTime expirationDateTime;

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public Long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(Long expiresIn) {
		this.expiresIn = expiresIn;
		this.expirationDateTime = LocalDateTime.now().plusSeconds(expiresIn - 30); // Subtraindo 30 segundos por cautela
	}

	public LocalDateTime getExpirationDateTime() {
		return expirationDateTime;
	}

	public void setExpirationDateTime(LocalDateTime expirationDateTime) {
		this.expirationDateTime = expirationDateTime;
	}

}
