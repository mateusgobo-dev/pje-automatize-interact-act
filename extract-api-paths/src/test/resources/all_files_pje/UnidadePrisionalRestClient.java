package br.jus.cnj.pje.webservice.client.criminal;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.pje.nucleo.dto.UnidadePrisionalDTO;

@Name(UnidadePrisionalRestClient.NAME)
public class UnidadePrisionalRestClient extends BaseRestClient<UnidadePrisionalDTO>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "unidadePrisionalRestClient";

	@Override
	public String getServicePath() {
		return "/criminal/unidadesPrisionais";
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
