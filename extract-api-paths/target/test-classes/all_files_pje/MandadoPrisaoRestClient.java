package br.jus.cnj.pje.webservice.client.bnmp;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.webservice.client.bnmp.dto.PecaDTO;

@Name(MandadoPrisaoRestClient.NAME)
public class MandadoPrisaoRestClient extends PecaBnmpRestClient<PecaDTO>{

	
	
	public MandadoPrisaoRestClient() {
		super();
	}

	public static final String NAME = "mandadoPrisaoRestClient";


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public String getSearchPath() {
		return "v2/pessoa";
	}

	@Override
	public String getServicePath() {
		return "bnmpservice/api/mandado-prisaos";
	}

	@Override
	public String getWebPath() {
		return "/#/mandado-prisao/";
	}
	
	@Override
	public String getWebSearchPath() {
		return getWebPath();
	}
}
