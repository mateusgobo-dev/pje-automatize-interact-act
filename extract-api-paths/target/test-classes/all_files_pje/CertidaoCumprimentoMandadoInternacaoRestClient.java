package br.jus.cnj.pje.webservice.client.bnmp;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.webservice.client.bnmp.dto.PecaDTO;

@Name(CertidaoCumprimentoMandadoInternacaoRestClient.NAME)
public class CertidaoCumprimentoMandadoInternacaoRestClient extends PecaBnmpRestClient<PecaDTO> {

	
	public CertidaoCumprimentoMandadoInternacaoRestClient() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static final String NAME = "certidaoCumprimentoMandadoInternacaoRestClient";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getServicePath() {
		return "bnmpservice/api/certidao-cumprimento-mandado-internacaos";
	}

	@Override
	public String getWebPath() {
		return "/#/cadastro-certidao-cumprimento-mandado-internacao/";
	}
	
	@Override
	public String getWebSearchPath() {
		return "/#/certidao-cumprimento-mandado-internacao/";
	}

}
