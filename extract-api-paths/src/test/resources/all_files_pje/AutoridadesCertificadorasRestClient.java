package br.jus.cnj.pje.webservice.client;

import java.util.Date;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.springframework.http.HttpStatus;

import br.jus.cnj.pje.pjecommons.model.services.PjeResponse;
import br.jus.cnj.pje.pjecommons.model.services.autoridadescertificadoras.Binario;
import br.jus.cnj.pje.pjecommons.model.services.autoridadescertificadoras.MetaDadosConjuntoACs;
import br.jus.pje.nucleo.util.DateUtil;

@Name(AutoridadesCertificadorasRestClient.NAME)
public class AutoridadesCertificadorasRestClient extends BaseRestClient<MetaDadosConjuntoACs>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "autoridadesCertificadorasRestClient";
	
	private String codeOK = HttpStatus.OK.toString().concat(" OK");

	@Logger
	private Log logger;

	@Override
	public String getServicePath() {
		return "/autoridades-certificadoras/api/v1";
	}

	/**
	 * Força a sincronização da lista de autoridades-certificadoras do serviço com o ICPBR
	 * 
	 * @return
	 */
	public boolean atualizarACs() {
		this.webTarget = this.client.target(getGatewayPath()).path(this.getServicePath() + "/acs-icpbr:atualizar");
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();
		}
		
		PjeResponse<String> response = (PjeResponse<String>) invocationBuilder.post(null, new GenericType<PjeResponse<String>> () {});
		return codeOK.equals(response.getCode());
	}
	
	/**
	 * Busca os meta-dados da atualização vigente no dia indicado
	 * 
	 * @param date
	 * @return
	 */
	public MetaDadosConjuntoACs recuperarAtualizacao(Date date) {
		String dataYYYMMDD = DateUtil.dateToString(date, "yyyy-MM-dd");
		this.webTarget = this.client.target(getGatewayPath()).path(this.getServicePath() + "/meta-dados/atualizacao/" + dataYYYMMDD);
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();
		}
		
		MetaDadosConjuntoACs metaDados = null;
		PjeResponse<MetaDadosConjuntoACs> response = (PjeResponse<MetaDadosConjuntoACs>) invocationBuilder.get(new GenericType<PjeResponse<MetaDadosConjuntoACs>> () {});
		if(codeOK.equals(response.getCode())) {
			metaDados = response.getResult();
		}
		return metaDados;
	}

	public MetaDadosConjuntoACs recuperarUltimaAtualizacao() {
		return this.recuperarAtualizacao(new Date());
	}
	
	public Binario recuperarBinarioIntermediarias(String hash) {
		return this.recuperarBinario(this.getServicePath() + "/binarios/intermediarias/", hash);
	}

	public Binario recuperarBinarioConfiaveis(String hash) {
		return this.recuperarBinario(this.getServicePath() + "/binarios/confiaveis/", hash);
	}

	public Binario recuperarBinarioACcompactado(String hash) {
		return this.recuperarBinario(this.getServicePath() + "/binarios/accompactado/", hash);
	}
	
	private Binario recuperarBinario(String servicePath, String hash) {
		this.webTarget = this.client.target(getGatewayPath()).path(servicePath + hash);
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();
		}

		Binario binario = null;
		PjeResponse<Binario> response = (PjeResponse<Binario>) invocationBuilder.get(new GenericType<PjeResponse<Binario>> () {});
		if(codeOK.equals(response.getCode())) {
			binario = response.getResult();
		}
		return binario;
	}

	@Override
	public String getServiceUsername() {
		return null;
	}

	@Override
	public String getServicePassword() {
		return null;
	}

	@Override
	public boolean isBasicAuth() {
		return false;
	}

	@Override
	public String getSearchPath() {
		return "meta-dados";
	}
}
