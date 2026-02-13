package br.jus.cnj.pje.webservice.client.criminal;

import javax.ws.rs.client.Invocation;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.pje.nucleo.dto.TipoOrigemDTO;

@Name(UsuarioAutenticadoRestClient.NAME)
public class UsuarioAutenticadoRestClient extends BaseRestClient<TipoOrigemDTO>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "usuarioAutenticadoRestClient";

	@Override
	public String getServicePath() {
		return "/criminal";
	}

	public String recuperarUsuarioAutenticado() {
		this.webTarget = this.client.target(getGatewayPath()).path(getServicePath() + "/api/v2/usuario-autenticado/");
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();
		}
		
		String response = invocationBuilder.get(String.class);
		
		return response;	
	}

	@Override
	public String getSearchPath() {
		return "pesquisar";
	}

	@Override
	public String getServiceUsername() {
		return ConfiguracaoIntegracaoCloud.PJE2_CRIMINAL_USERNAME;
	}

	@Override
	public String getServicePassword() {
		return ConfiguracaoIntegracaoCloud.PJE2_CRIMINAL_PASSWORD;
	}

	@Override
	public boolean isBasicAuth() {
		return true;
	}
}
