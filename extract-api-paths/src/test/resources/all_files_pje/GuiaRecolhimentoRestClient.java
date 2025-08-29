package br.jus.cnj.pje.webservice.client.bnmp;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.webservice.client.bnmp.dto.PecaDTO;

@Name(GuiaRecolhimentoRestClient.NAME)
public class GuiaRecolhimentoRestClient extends PecaBnmpRestClient<PecaDTO>{

	
	public GuiaRecolhimentoRestClient() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static final String NAME = "guiaRecolhimentoRestClient";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getServicePath() {
		return "bnmpservice/api/guia-recolhimentos";
	}

	@Override
	public String getWebPath() {
		return "/#/cadastro-guia-recolhimento/";
	}
	
	@Override
	public String getWebSearchPath() {
		return "/#/guia-recolhimento/";
	}

}
