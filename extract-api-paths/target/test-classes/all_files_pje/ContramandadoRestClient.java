package br.jus.cnj.pje.webservice.client.bnmp;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.webservice.client.bnmp.dto.PecaDTO;

@Name(ContramandadoRestClient.NAME)
public class ContramandadoRestClient extends PecaBnmpRestClient<PecaDTO> {

	

	public ContramandadoRestClient() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String NAME = "contramandadoRestClient";
	
	@Override
	public String getSearchPath() {
		return "v2/pessoa";
	}
	
	@Override
	public String getServicePath() {
		return "bnmpservice/api/contramandados";
	}

	@Override
	public String getWebPath() {
		return "/#/cadastro-contramandado/";
	}
	
	@Override
	public String getWebSearchPath() {
		return "/#/contramandado/"; 
	}
	
	@Override
	public String getAcaoCadastro() {
		return "Novo";
	}

}
