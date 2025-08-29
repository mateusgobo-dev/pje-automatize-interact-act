package br.jus.cnj.pje.webservice.client.bnmp;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.webservice.client.bnmp.dto.PecaDTO;


@Name(CertidaoArquivamentoGuiaRestClient.NAME)
public class CertidaoArquivamentoGuiaRestClient extends PecaBnmpRestClient<PecaDTO> {

	public CertidaoArquivamentoGuiaRestClient() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static final String NAME = "certidaoArquivamentoGuiaRestClient";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getServicePath() {
		return "bnmpservice/api/certidoes-arquivamento-guia";
	}

	@Override
	public String getWebPath() {
		return "/#/cadastro-certidao-arquivamento-guia/";
	}
	
	
	public String getWebSearchPath() {
		return "/#/certidao-arquivamento-guia/";
	}

}
