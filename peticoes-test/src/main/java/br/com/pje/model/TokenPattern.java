package br.com.pje.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class TokenPattern implements Serializable {
    private static final long serialVersionUID = 1L;

    private String access_token;
    private Long expires_in;
    private Long refresh_expires_in;
    private String token_type;
    private String scope;

    @JsonProperty("not-before-policy")
    private Integer notBeforePolicy;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public Long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(Long expires_in) {
        this.expires_in = expires_in;
    }

    public Long getRefresh_expires_in() {
        return refresh_expires_in;
    }

    public void setRefresh_expires_in(Long refresh_expires_in) {
        this.refresh_expires_in = refresh_expires_in;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Integer getNotBeforePolicy() {
        return notBeforePolicy;
    }

    public void setNotBeforePolicy(Integer notBeforePolicy) {
        this.notBeforePolicy = notBeforePolicy;
    }

    @Override
    public String toString() {
        return "TokenPattern{" +
                "access_token='" + access_token + '\'' +
                ", expires_in=" + expires_in +
                ", refresh_expires_in=" + refresh_expires_in +
                ", token_type='" + token_type + '\'' +
                ", scope='" + scope + '\'' +
                ", notBeforePolicy=" + notBeforePolicy +
                '}';
    }
}
