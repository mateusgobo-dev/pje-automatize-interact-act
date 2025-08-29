package br.jus.cnj.pje.webservice.client.criminal;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.pje.nucleo.dto.ProcessoProcedimentoOrigemDTO;

@Name(ProcessoProcedimentoOrigemRestClient.NAME)
public class ProcessoProcedimentoOrigemRestClient extends BaseRestClient<ProcessoProcedimentoOrigemDTO> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "processoProcedimentoOrigemRestClient";

	@Override
	public String getServicePath() {
		return "/criminal/processosProcedimentosOrigem";
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
