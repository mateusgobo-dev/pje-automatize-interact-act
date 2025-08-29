package br.jus.cnj.pje.webservice.client.keycloak;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade.Request;
import org.keycloak.representations.adapters.config.AdapterConfig;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;

public class SSOConfigResolver implements KeycloakConfigResolver {

	private KeycloakDeployment keycloakDeployment;

	@Override
	public KeycloakDeployment resolve(Request facade) {
		if (this.keycloakDeployment == null) {
			keycloakDeployment = build();
		}
		return this.keycloakDeployment;
	}

	private KeycloakDeployment build() {
		AdapterConfig config = new AdapterConfig();
		config.setRealm(ConfiguracaoIntegracaoCloud.getSSORealm());
		config.setAuthServerUrl(ConfiguracaoIntegracaoCloud.getSSOAuthServerUrl());
		config.setSslRequired(ConfiguracaoIntegracaoCloud.getSSOSslRequired());
		config.setResource(ConfiguracaoIntegracaoCloud.getSSOClientId());
		config.setConfidentialPort(ConfiguracaoIntegracaoCloud.getSSOConfidentialPort());
		config.setPublicClient(this.getPublicClient());
		config.setUseResourceRoleMappings(this.getUseResourceRoleMappings());
		config.setEnableBasicAuth(this.getEnableBasicAuth());
		config.setCredentials(this.getCredentials());
		return KeycloakDeploymentBuilder.build(config);
	}

	private Boolean getPublicClient() {
		return false;
	}

	private Map<String, Object> getCredentials() {
		Map<String, Object> credentials = new HashMap<String, Object>();
		credentials.put("secret", ConfiguracaoIntegracaoCloud.getSSOClientSecret());

		return credentials;
	}

	private Boolean getUseResourceRoleMappings() {
		return true;
	}

	private Boolean getEnableBasicAuth() {
		return false;
	}

}
