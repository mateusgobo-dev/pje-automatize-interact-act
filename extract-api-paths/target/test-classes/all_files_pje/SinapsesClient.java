package br.jus.cnj.pje.webservice.client;

import java.io.Serializable;
import java.util.Base64;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.jus.cnj.pje.criminal.error.PjeErrorDetail;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeRestException;
import br.jus.pje.nucleo.dto.sinapses.Mensagem;
import br.jus.pje.nucleo.dto.sinapses.MovimentacaoSugeridaResponse;

@Name(SinapsesClient.NAME)
@Scope(ScopeType.EVENT)
public class SinapsesClient implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "sinapsesClient";
	
	private Client client;
	
	private WebTarget webTarget;
	
	private static final String EXECUTAR_SERVICO_PATH = "rest/modelo/executarServico";
	
	@Create
	public void init(){
		this.client = ClientBuilder.newClient();
	}
	
	public MovimentacaoSugeridaResponse recuperarMovimetacoesSugeridas(Mensagem mensagem, String path) throws PJeRestException{
		return this.recuperarMovimetacoesSugeridas(this.client.target(sinapsesServiceURL()).path(EXECUTAR_SERVICO_PATH).path(path), mensagem);
	}
	
	public MovimentacaoSugeridaResponse recuperarMovimetacoesSugeridas(Mensagem mensagem, String unidadeDominio, String modelo, String versao) throws PJeRestException{
		return this.recuperarMovimetacoesSugeridas(this.client.target(sinapsesServiceURL()).path(EXECUTAR_SERVICO_PATH).path(unidadeDominio).path(modelo).path(versao), mensagem);
	}
	
	private MovimentacaoSugeridaResponse recuperarMovimetacoesSugeridas(WebTarget target, Mensagem mensagem) throws PJeRestException{
		this.webTarget = target;
		
		Invocation.Builder invocationBuilder = this.getInvocationDefaults();
		
		ClientResponse resp;
		
		MovimentacaoSugeridaResponse ret;
		
		try{
			ObjectMapper mapper = new ObjectMapper();
			System.out.println(mapper.writeValueAsString(mensagem));
			resp = (ClientResponse) invocationBuilder.post(Entity.entity(mensagem, MediaType.APPLICATION_JSON));
		}catch (Exception e) {
			throw new PJeRestException(e);
		}

		if(resp.getStatus() == HttpStatus.SC_OK){
				ret = resp.readEntity(MovimentacaoSugeridaResponse.class);
		}else{
			PjeErrorDetail pjeErrorDetail = new PjeErrorDetail();
			try {
				pjeErrorDetail = resp.readEntity(PjeErrorDetail.class);	
			}catch (Exception e) {
				pjeErrorDetail.setStatus(resp.getStatus());
				pjeErrorDetail.setMessage(e.getMessage());
			}
			throw new PJeRestException(pjeErrorDetail.getStatus().toString(), pjeErrorDetail.toString());
		}
		
		return ret;
	}
	
	public String sinapsesServiceURL() {
		return ConfiguracaoIntegracaoCloud.getSinapsesUrl();
	}
	
	public String sinapsesServiceUsername() {
		return ConfiguracaoIntegracaoCloud.getSinapsesUsername();
	}
	
	public String sinapsesServicePassword() {
		return ConfiguracaoIntegracaoCloud.getSinapsesPassword();
	}
	
	private Invocation.Builder getInvocationDefaults() {
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json");
		return invocationBuilder
					.header("Content-Type", "application/json")
					.header("Authorization", "Basic " + this.getBasicAuthCredentials());
	}
	
	private String getBasicAuthCredentials(){
		
		String auth = "";
		
		auth = Base64.getEncoder().encodeToString((this.sinapsesServiceUsername() + ":" + this.sinapsesServicePassword()).getBytes());
		
		return auth;
	}
}
