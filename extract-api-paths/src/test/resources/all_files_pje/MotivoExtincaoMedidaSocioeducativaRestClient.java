package br.jus.cnj.pje.webservice.client;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.pje.nucleo.dto.MotivoExtincaoMedidaSocioeducativaDTO;

@Name(MotivoExtincaoMedidaSocioeducativaRestClient.NAME)
public class MotivoExtincaoMedidaSocioeducativaRestClient extends BaseRestClient<MotivoExtincaoMedidaSocioeducativaDTO>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "motivoExtincaoMedidaSocioeducativaRestClient";
	
	String username = ConfiguracaoIntegracaoCloud.PJE2_CRIMINAL_USERNAME;
	String password = ConfiguracaoIntegracaoCloud.PJE2_CRIMINAL_PASSWORD;
	boolean basicAuth = true;
	String pesquisar = "pesquisar";

	@Override
	public String getServicePath() {
		return "/criminal/api/v2/motivos-extincao-medida-socioeducativa";
	}

	@Override
	public String getSearchPath() {
		return pesquisar;
	}

	@Override
	public String getServiceUsername() {
		return username;
	}

	@Override
	public String getServicePassword() {
		return password;
	}

	@Override
	public boolean isBasicAuth() {
		return basicAuth;
	}	
	
	
}
