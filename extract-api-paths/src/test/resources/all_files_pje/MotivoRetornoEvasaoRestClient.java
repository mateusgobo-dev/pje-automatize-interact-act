package br.jus.cnj.pje.webservice.client;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.pje.nucleo.dto.MotivoRetornoEvasaoDTO;

@Name(MotivoRetornoEvasaoRestClient.NAME)
public class MotivoRetornoEvasaoRestClient extends BaseRestClient<MotivoRetornoEvasaoDTO>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "motivoRetornoEvasaoRestClient";

	@Override
	public String getServicePath() {
		return "/criminal/api/v2/motivos-retorno-evasao";
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
