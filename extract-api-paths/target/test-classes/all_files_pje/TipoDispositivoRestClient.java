package br.jus.cnj.pje.webservice.client;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.pje.nucleo.dto.TipoDispositivoDTO;

@Name(TipoDispositivoRestClient.NAME)
public class TipoDispositivoRestClient extends BaseRestClient<TipoDispositivoDTO>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tipoDispositivoRestClient";

	@Override
	public String getServicePath() {
		return "/criminal/tiposDispositivos";
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
