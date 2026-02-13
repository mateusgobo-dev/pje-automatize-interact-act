package br.jus.cnj.pje.webservice.client.criminal;

import java.util.List;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.pje.nucleo.dto.OrgaoProcedimentoOriginarioDTO;

@Name(OrgaoProcedimentoOriginarioRestClient.NAME)
public class OrgaoProcedimentoOriginarioRestClient extends BaseRestClient<OrgaoProcedimentoOriginarioDTO> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "orgaoProcedimentoOriginarioRestClient";

	@Override
	public String getServicePath() {
		return "/criminal/orgaosProcedimentoOriginario";
	}

	@Override
	public String getSearchPath() {
		return "pesquisar";
	}
	
	public List<OrgaoProcedimentoOriginarioDTO> findByTipoOrigemAndUf(Integer idTipoOrigem, String uf) {
		this.webTarget = this.client.target(getGatewayPath()).path(getServicePath() + "/" + idTipoOrigem + "/" + uf);
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		if (this.isBasicAuth()) {
			invocationBuilder = this.getInvocationDefaults();

		}
		List<OrgaoProcedimentoOriginarioDTO> response = invocationBuilder
				.get(new GenericType<List<OrgaoProcedimentoOriginarioDTO>>() {
				});
		return response;
	}

	public OrgaoProcedimentoOriginarioDTO findByCodigoNacional(Integer idOrgaoProcedimentoOriginario) {
		this.webTarget = this.client.target(getGatewayPath()).path(getServicePath() + "/" + idOrgaoProcedimentoOriginario );
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		if (this.isBasicAuth()) {
			invocationBuilder = this.getInvocationDefaults();

		}
		OrgaoProcedimentoOriginarioDTO response = invocationBuilder
				.get(new GenericType<OrgaoProcedimentoOriginarioDTO>() {
				});
		return response;
	}
	
	public List<OrgaoProcedimentoOriginarioDTO> recuperaListagem() throws PJeException {		
		this.webTarget = this.client.target(getGatewayPath()).path(getServicePath());		
		try {
			return getInvocationDefaults().get(new GenericType<List<OrgaoProcedimentoOriginarioDTO>>(){});
		}catch (Exception e) {
			throw new PJeException(e);
		}
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
