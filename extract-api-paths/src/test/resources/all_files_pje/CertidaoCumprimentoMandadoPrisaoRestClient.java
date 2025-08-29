package br.jus.cnj.pje.webservice.client.bnmp;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.webservice.client.bnmp.dto.PecaDTO;

@Name(CertidaoCumprimentoMandadoPrisaoRestClient.NAME)
public class CertidaoCumprimentoMandadoPrisaoRestClient extends PecaBnmpRestClient<PecaDTO> {
	
	public CertidaoCumprimentoMandadoPrisaoRestClient() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static final String NAME = "certidaoCumprimentoMandadoPrisaoRestClient";

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
		return "bnmpservice/api/certidao-cumprimento-mandado-prisaos";
	}

	@Override
	public String getWebPath() {
		return "/#/cadastro-certidao-cumprimento-mandado-prisao/";
	}
	
	@Override
	public String getWebSearchPath() {
		return "/#/certidao-cumprimento-mandado-prisao/";
	}

}
