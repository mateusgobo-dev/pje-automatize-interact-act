package br.jus.cnj.pje.nucleo.service;

import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Contexts;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.webservice.client.keycloak.KeycloakServiceClient;

/**
 * Camada de serviço responsável pelas funcionalidades gerais da instalação.
 * 
 * @author Adriano Pamplona
 */
@Name(TribunalService.NAME)
@Transactional
public class TribunalService {

	public static final String TRIBUNAL_TOKEN = "KEYCLOAK_TRIBUNAL_TOKEN";
	
	public static final String NAME = "tribunalService";

	/**
	 * @return Instância da classe.
	 */
	public static TribunalService instance() {
		return ComponentUtil.getComponent(TribunalService.class);
	}

	/**
	 * Efetua o login no Tribunal do tipo 'client-credentials'.
	 * 
	 * @return Token.
	 */
	public String login() {
		String token = (String) Contexts.getSessionContext().get(TRIBUNAL_TOKEN);
		if(StringUtils.isBlank(token)){
			KeycloakServiceClient keycloak = KeycloakServiceClient.instance();
			String clientId = ConfiguracaoIntegracaoCloud.getSSOClientId();
			String secret = ConfiguracaoIntegracaoCloud.getSSOClientSecret();
			
			token = keycloak.login(clientId, secret);
			Contexts.getSessionContext().set(TRIBUNAL_TOKEN, token);
		}
		
		return token;
	}
}
