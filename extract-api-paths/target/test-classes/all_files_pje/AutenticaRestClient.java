package br.jus.cnj.pje.webservice.client.bnmp;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.cnj.pje.webservice.client.bnmp.dto.AuthenticationRequestDTO;
import br.jus.cnj.pje.webservice.client.bnmp.dto.AuthenticationResponseDTO;

@Name(AutenticaRestClient.NAME)
@Scope(ScopeType.EVENT)
public class AutenticaRestClient extends BaseRestClient<AuthenticationResponseDTO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String NAME = "autenticaRestClient";

	@Override
	public String getServicePath() {
		return "api/public/authenticate";
	}

	@Override
	public String getSearchPath() {
		return null;
	}

	public AuthenticationResponseDTO login(String username, String password, String codigoOrgao) throws ClientErrorException, PJeException {
		AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO(
				ConfiguracaoIntegracaoCloud.getBnmpClientId(), username, password, codigoOrgao);
		
		this.webTarget = this.client.target(getGatewayPath()).path(this.getServicePath());
		AuthenticationResponseDTO response = null; 

		Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
		
		ClientResponse resp = null;
		try{
			resp = (ClientResponse) invocationBuilder.post(
				Entity.entity(authenticationRequestDTO, MediaType.APPLICATION_JSON));
		}catch (Exception e) {
			e.printStackTrace();
			throw new PJeException(e);
		}
		
		if(resp.getStatus() == HttpStatus.SC_OK){
			response  = resp.readEntity(AuthenticationResponseDTO.class);			
		}else{
			throw new ClientErrorException(resp.getStatus());
		}
		return response;
		 
	}

	@Override
	public String getServiceUsername() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServicePassword() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBasicAuth() {
		return false;
	}
	
	@Override
	public String getGatewayPath() {
		return ConfiguracaoIntegracaoCloud.getBnmpApiUrl();
	}

	public AuthenticationResponseDTO login(String password) throws ClientErrorException, PJeException {
		return  login(Authenticator.getUsuarioLogado().getLogin(),password,Authenticator.getOrgaoJulgadorAtual().getNumeroVara()+"");
	}

}
