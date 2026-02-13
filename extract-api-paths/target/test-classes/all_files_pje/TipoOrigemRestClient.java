package br.jus.cnj.pje.webservice.client.criminal;

import java.util.List;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.pje.nucleo.dto.TipoOrigemDTO;

@Name(TipoOrigemRestClient.NAME)
public class TipoOrigemRestClient extends BaseRestClient<TipoOrigemDTO>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tipoOrigemRestClient";

	@Override
	public String getServicePath() {
		return "/criminal/tiposOrigem";
	}

	
	public List<TipoOrigemDTO> recuperarTiposOrigem() {
		this.webTarget = this.client.target(getGatewayPath()).path("/criminal/tiposOrigem");
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();

		}
		
		List<TipoOrigemDTO> response = invocationBuilder.get(new GenericType<List<TipoOrigemDTO>> () {});
		
		return response;	
	}
	
	public Integer recuperarTotalTiposOrigem() {
		this.webTarget = this.client.target(getGatewayPath()).path("/criminal/tiposOrigem/count");
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();

		}
		
		Integer response = invocationBuilder.get(new GenericType<Integer> () {});
		
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
