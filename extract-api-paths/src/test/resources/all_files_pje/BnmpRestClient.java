package br.jus.cnj.pje.webservice.client.bnmp;

import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.client.Invocation;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.webservice.client.BaseRestClient;

public abstract class BnmpRestClient<E> extends BaseRestClient<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public String getServiceUsername() {
		return null;
	}

	@Override
	public String getServicePassword() {
		return null;
	}
	
	protected BnmpRestClient() {
		super();
	}

	@Override
	public Invocation.Builder getInvocationDefaults() {
		return getInvocationDefaults(null);
	}
	
	public Invocation.Builder getInvocationDefaults(Map<String, Object> queryParams) {
        if (queryParams != null) {
            for (Entry<String, Object> entry : queryParams.entrySet()) {
                webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
            }
        }
        
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		return invocationBuilder.header("Authorization", this.getBearerAuthCredentials());
	}

	protected String getBearerAuthCredentials() {
		return "Bearer "+ ConfiguracaoIntegracaoCloud.getBnmpApiToken();
	}

	
	@Override
	public boolean isBasicAuth() {
		return false;

	}
	
	public boolean isBearerAuth() {
		return true;
	}
	
	@Override
	public String getGatewayPath() {
		return ConfiguracaoIntegracaoCloud.getBnmpApiUrl();
	}

	public String getWebUrl() {
		return ConfiguracaoIntegracaoCloud.getBnmpWebUrl();
	}
}
