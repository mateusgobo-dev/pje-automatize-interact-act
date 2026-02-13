 package br.jus.cnj.pje.webservice.client.bnmp;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.webservice.client.bnmp.dto.PecaDTO;

@Name(AlvaraSolturaRestClient.NAME)
public class AlvaraSolturaRestClient extends PecaBnmpRestClient<PecaDTO> {

	public AlvaraSolturaRestClient() {
		super();
	}

	public static final String NAME ="alvaraSolturaRestClient";
	
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
		return "bnmpservice/api/alvara-solturas";
	}

	@Override
	public String getWebPath() {
		return "/#/alvara-soltura/";
	}
	
	
	public String getWebSearchPath() {
		return getWebPath();
	}
	
}
