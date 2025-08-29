package br.jus.cnj.pje.webservice.client.bnmp;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.webservice.client.bnmp.dto.PecaDTO;

@Name(MandadoInternacaoRestClient.NAME)
public class MandadoInternacaoRestClient extends PecaBnmpRestClient<PecaDTO>{

	

	public MandadoInternacaoRestClient() {
		super();
		// TODO Auto-generated constructor stub
	}


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String NAME = "mandadoInternacaoRestClient";
	
	@Override
	public String getSearchPath() {
		return "v2/pessoa";
	}
	
	@Override
	public String getServicePath() {
		return "bnmpservice/api/mandado-internacaos";
	}

	@Override
	public String getWebPath() {
		return "/#/mandado-internacao/";
	}
	
	@Override
	public String getWebSearchPath() {
		return getWebPath();
	}

}
