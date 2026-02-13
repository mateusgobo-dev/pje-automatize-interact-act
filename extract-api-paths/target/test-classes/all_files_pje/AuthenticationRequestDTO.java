package br.jus.cnj.pje.webservice.client.bnmp.dto;

public class AuthenticationRequestDTO {
    private String clientId;
    private String username;
    private String password;
    private String codigoOrgao;

    public AuthenticationRequestDTO() {
    }
    
    

    /**
     * @param clientId
     * @param username
     * @param password
     * @param codigoOrgao
     */
    public AuthenticationRequestDTO(String clientId, String username, String password, String codigoOrgao) {
		super();
		this.clientId = clientId;
		this.username = username;
		this.password = password;
		this.codigoOrgao = codigoOrgao;
	}



	public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCodigoOrgao() {
        return this.codigoOrgao;
    }

    public void setCodigoOrgao(String codigoOrgao) {
        this.codigoOrgao = codigoOrgao;
    }
}
