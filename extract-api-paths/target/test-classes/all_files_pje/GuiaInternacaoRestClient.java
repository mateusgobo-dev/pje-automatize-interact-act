package br.jus.cnj.pje.webservice.client.bnmp;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.webservice.client.bnmp.dto.PecaDTO;

@Name(GuiaInternacaoRestClient.NAME)
public class GuiaInternacaoRestClient extends PecaBnmpRestClient<PecaDTO> {

	
	public GuiaInternacaoRestClient() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static final String NAME = "guiaInternacaoRestClient";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getServicePath() {
		return "bnmpservice/api/guia-internacaos";
	}

	@Override
	public String getWebPath() {
		return "/#/cadastro-guia-internacao/";
	}

	@Override
	public String getWebSearchPath() {
		return "/#/guia-internacao/";
	}
}
