package br.jus.cnj.pje.webservice.client;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.pje.nucleo.dto.NormaDTO;

@Name(NormaRestClient.NAME)
public class NormaRestClient extends BaseRestClient<NormaDTO> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "normaRestClient";

	@Override
	public String getServicePath() {
		return "/criminal/normas";
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
