package br.jus.cnj.pje.webservice.client.criminal;

import java.util.List;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.pje.nucleo.dto.TipoProcedimentoOrigemDTO;

@Name(TipoProcedimentoOrigemRestClient.NAME)
public class TipoProcedimentoOrigemRestClient extends BaseRestClient<TipoProcedimentoOrigemDTO>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tipoProcedimentoOrigemRestClient";

	@Override
	public String getServicePath() {
		return "/criminal/tiposProcedimentoOrigem";
	}

	@Override
	public String getSearchPath() {
		return "pesquisar";
	}
	

	public List<TipoProcedimentoOrigemDTO> findByTipoOrigem(Integer idTipoOrigem) {
		
		this.webTarget = this.client.target(getGatewayPath()).path("/criminal/tiposOrigem" + "/" + idTipoOrigem + "/tiposProcedimentoOrigem");
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();

		}
		
		List<TipoProcedimentoOrigemDTO> response = invocationBuilder.get(new GenericType<List<TipoProcedimentoOrigemDTO>> () {});
		
		return response;	
	}
	
	public List<TipoProcedimentoOrigemDTO> recuperarTiposProcedimentoOrigem() {
		this.webTarget = this.client.target(getGatewayPath()).path("/criminal/tiposProcedimentoOrigem");
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();

		}
		
		List<TipoProcedimentoOrigemDTO> response = invocationBuilder.get(new GenericType<List<TipoProcedimentoOrigemDTO>> () {});
		
		return response;	
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
