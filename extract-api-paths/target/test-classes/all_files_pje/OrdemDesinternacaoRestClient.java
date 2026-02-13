package br.jus.cnj.pje.webservice.client.bnmp;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.PecasPessoaLightDTO;
import br.jus.cnj.pje.webservice.client.bnmp.dto.PecaDTO;

@Name(OrdemDesinternacaoRestClient.NAME)
public class OrdemDesinternacaoRestClient extends PecaBnmpRestClient<PecaDTO>{

	
	
	public OrdemDesinternacaoRestClient() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static final String NAME = "ordemDesinternacaoRestClient";


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getServicePath() {
		return "bnmpservice/api/ordem-desinternacaos";
	}

	@Override
	public String getWebPath() {
		return "/#/cadastro-ordem-desinternacao/";
	}
	
	@Override
	public String getWebSearchPath() {
		return "/#/ordem-desinternacao/";
	}

}
