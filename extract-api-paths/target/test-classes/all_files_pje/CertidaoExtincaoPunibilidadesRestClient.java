package br.jus.cnj.pje.webservice.client.bnmp;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.webservice.client.bnmp.dto.PecaDTO;

@Name(CertidaoExtincaoPunibilidadesRestClient.NAME)
public class CertidaoExtincaoPunibilidadesRestClient extends PecaBnmpRestClient<PecaDTO> {
	
	public CertidaoExtincaoPunibilidadesRestClient() {
		super();

	}

	public static final String NAME = "certidaoExtincaoPunibilidadesRestClient";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getServicePath() {
		return "bnmpservice/api/certidao-extincao-punibilidades";
	}

	@Override
	public String getWebPath() {
		return "/#/cadastro-certidao-extincao-punibilidade/";
	}
	
	@Override
	public String getWebSearchPath() {
		return "/#/certidao-extincao-punibilidade/";
	}

}
