package br.jus.cnj.pje.webservice.client.bnmp.dto;

import java.io.Serializable;

public class AuthenticationResponseDTO implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -9050791908422053994L;
	private String token_jwt;
    private String token_csrf;
    private String password;

    public String getJWT() {
        return token_jwt;
    }

    public String getCSRF() {
        return token_csrf;
    }

    public void setToken_jwt(String token_jwt) {
        this.token_jwt = token_jwt;
    }

    public void setToken_csrf(String token_csrf) {
        this.token_csrf = token_csrf;
    }

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
