package br.jus.cnj.pje.webservice.client.keycloak;

import java.io.Serializable;
import java.util.Base64;
import java.util.Date;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.json.JSONObject;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PjeRestClientException;
import br.jus.cnj.pje.webservice.client.GenericRestClient;
import br.jus.pje.nucleo.util.StringUtil;

@Name(SsoTokenRestClient.NAME)
public class SsoTokenRestClient extends GenericRestClient implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final LogProvider log = Logging.getLogProvider(SsoTokenRestClient.class);

	private static final String TOKEN_NAME = "sso.token";
	
	public static final String NAME = "ssoTokenRestClient";
	
	private JSONObject getJsonToken(String token) {
		Base64.Decoder decoder = Base64.getUrlDecoder();
		String[] chunks = token.split("\\."); 
		String payload = new String(decoder.decode(chunks[1]));
		return new JSONObject(payload);
	}
	
	private Date getDataExpiracaoToken(String token) {
		JSONObject tokenPayload = getJsonToken(token);
		Long timeStamp = tokenPayload.getLong("exp");
		return new Date(timeStamp * 1000);
	}
	
	private Boolean isTokenExpirado(String token) {
		final Date expiration = getDataExpiracaoToken(token);
		return expiration.before(new Date());
	}
	
	public String getTokenSso() throws PjeRestClientException {
		String token = (String) Contexts.getApplicationContext().get(TOKEN_NAME);
		if(token == null || isTokenExpirado(token)) {
			token = gerarTokenSso();
			Contexts.getApplicationContext().set(TOKEN_NAME, token);
		}
		return token;
	}
	
	public String gerarTokenSso() throws PjeRestClientException {
		String retorno = null;
		String mensagem = null;
	
		this.webTarget = this.client.target(getServicePath());
		
		String request = "grant_type=client_credentials"
				+ "&client_id=" + ConfiguracaoIntegracaoCloud.getSSOClientId()
				+ "&client_secret=" + ConfiguracaoIntegracaoCloud.getSSOClientSecret();
		
		Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
		
		Response response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_FORM_URLENCODED));
		int requisicaoStatus = response.getStatus();
		String json = response.readEntity(String.class);
		JSONObject jsonObject = new JSONObject(json);
		
		response.close();
		
		if (requisicaoStatus == HttpStatus.SC_OK) {
			retorno = jsonObject.getString("access_token");
			
			log.info(StringUtil.normalize("Token gerado com sucesso."));
		}		
		else {			
			mensagem = "Falha ao tentar gerar token. Status: " + requisicaoStatus;
		
			log.error(StringUtil.normalize(mensagem));
			throw new PjeRestClientException(mensagem);
		}
	
		return retorno;
	}

	@Override
	public String getServicePath() {
		return ConfiguracaoIntegracaoCloud.getSSOAuthServerUrl() + "/realms/" + ConfiguracaoIntegracaoCloud.getSSORealm() + "/protocol/openid-connect/token";
	}
}
