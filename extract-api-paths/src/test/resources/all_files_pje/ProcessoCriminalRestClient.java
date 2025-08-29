package br.jus.cnj.pje.webservice.client.criminal;

import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.criminal.error.PjeErrorDetail;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.cnj.pje.nucleo.PjeRestClientException;
import br.jus.pje.nucleo.dto.InformacaoCriminalDTO;
import br.jus.pje.nucleo.dto.ProcessoCriminalDTO;
import br.jus.pje.nucleo.dto.ProcessoProcedimentoOrigemDTO;

import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

@Name(ProcessoCriminalRestClient.NAME)
public class ProcessoCriminalRestClient extends BaseRestClient<ProcessoCriminalDTO> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "processoCriminalRestClient";

	@Override
	public String getServicePath() {
		return "/criminal/processos";
	}

	@Override
	public String getSearchPath() {
		return "pesquisar";
	}
	
	public ProcessoCriminalDTO getResourceByProcesso(String numeroProcesso) throws PJeException {
		webTarget = client.target(getGatewayPath()).path(getServicePath() + "/numero/" + numeroProcesso);
		Invocation.Builder invocationBuilder = webTarget.request("application/json;charset=UTF-8");
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();

		}
		ClientResponse resp = null;
		ProcessoCriminalDTO response = null;

		try{
			resp = (ClientResponse)  invocationBuilder.get();

			if(resp.getStatus() == HttpStatus.SC_OK){
				if (resp.hasEntity()) {
					response = resp.readEntity(ProcessoCriminalDTO.class);
				}
			}else{
				if (resp.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
					FacesMessages.instance().clear();
					FacesMessages.instance().add(Severity.INFO, "Registros criminais não foram recuperados pelo sistema.");
				}
				PjeErrorDetail erro = resp.readEntity(PjeErrorDetail.class);
				throw new PJeException(erro.toString());
			}		
		}catch (Exception e) {
			throw new PJeException(e.getMessage());
		} finally {
			if (resp != null) resp.close();
		}
		
		return response;
	}

	public ProcessoCriminalDTO save(ProcessoCriminalDTO processoCriminalDTO) throws PJeException {
		webTarget = client.target(getGatewayPath()).path(getServicePath());
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();

		}
		
		ClientResponse resp = null;
		ProcessoCriminalDTO response = null;

		try{
			resp = (ClientResponse)  invocationBuilder.post(Entity.entity(processoCriminalDTO, MediaType.APPLICATION_JSON));
		}catch (Exception e) {
			throw new PJeException(e.getMessage());
		}
		
		if(resp.getStatus() == HttpStatus.SC_OK){
			response = resp.readEntity(ProcessoCriminalDTO.class);			
		}else{
			PjeErrorDetail erro = resp.readEntity(PjeErrorDetail.class);		
			throw new PjeRestClientException(erro);
		}		
		return response;
	}
	
	public List<Integer> inserirInformacoesCriminaisAoProcessoCriminal(ProcessoCriminalDTO processoCriminalDTO, List<InformacaoCriminalDTO> informacoesCriminais) throws PJeException{
		webTarget = client.target(getGatewayPath()).path(getServicePath() + "/" + processoCriminalDTO.getId() + "/informacoes-criminais");

		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();

		}
		
		ClientResponse resp = null;
		List<Integer> response = null;

		try{
			resp = (ClientResponse)  invocationBuilder.post(Entity.entity(informacoesCriminais, MediaType.APPLICATION_JSON));
		}catch (Exception e) {
			throw new PJeException(e.getMessage());
		}
		
		if(resp.getStatus() == HttpStatus.SC_OK){
			response = resp.readEntity(new GenericType<List<Integer>>() {});			
		}else{
			PjeErrorDetail erro = resp.readEntity(PjeErrorDetail.class);		
			throw new PJeException(erro.toString());
		}		
		return response;		
	}
	
	/**
	 * Executa um WebService Rest e retorna seu resultado.
	 * 
	 * @param url String da URL
	 * @param method Mtodo do protocolo Http. (Classe HttpMethod)
	 * @return Resultado da execuo do WebService Rest.
	 * @throws PJeException
	 */
	protected <T> T executarRest(String url, String method, Object tipoResultado) throws PJeException {
		webTarget = client.target(getGatewayPath()).path(url);

		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();
		}
		
		ClientResponse resp = null;
		T response = null;

		try{
			resp = (ClientResponse) invocationBuilder.build(method).invoke();
		}catch (Exception e) {
			throw new PJeException(e.getMessage());
		}
		
		if(resp.getStatus() == HttpStatus.SC_OK){		
			
			if(!resp.hasEntity()) {
				throw new PJeException("Processo não encontrado na base criminal.");
			}
			if (tipoResultado instanceof GenericType) {
				//System.out.println("Response: " + resp.getStatus() + " - " + resp.readEntity(String.class));
				response = (T) resp.readEntity((GenericType) tipoResultado);			
			} else {
				response = (T) resp.readEntity((Class) tipoResultado);
			}
		} else if (resp.getStatus() == HttpStatus.SC_NOT_FOUND) {
			throw new PJeException("Recurso não encontrado. Status: "+ resp.getStatus());
		} else{
			PjeErrorDetail erro = resp.readEntity(PjeErrorDetail.class);		
			throw new PJeException(erro.toString());
		}	
		
		return response;	
	}


	public List<String> consultaNumeroProcessoProcedimentoOrigem(ProcessoProcedimentoOrigemDTO processoProcedimento) throws PJeException {
		webTarget = client.target(getGatewayPath()).path(getServicePath() + "/pesquisar/procedimentoOrigem/");
		
		ClientResponse resp = null;
		List<String> response;

		try{
			resp = (ClientResponse)  getInvocationDefaults().post(Entity.entity(processoProcedimento, MediaType.APPLICATION_JSON));
		}catch (Exception e) {
			throw new PJeException(e.getMessage());
		}
		
		if(resp.getStatus() == HttpStatus.SC_OK){
			response = resp.readEntity(new GenericType<List<String>>() {
			});			
		}else{
			PjeErrorDetail erro = resp.readEntity(PjeErrorDetail.class);		
			throw new PJeException(erro.toString());
		}		
		
		return response;
	}

	@Override
	public String getServiceUsername() {
		return ConfiguracaoIntegracaoCloud.PJE2_CRIMINAL_USERNAME;
	}

	@Override
	public String getServicePassword() {
		return ConfiguracaoIntegracaoCloud.PJE2_CRIMINAL_PASSWORD;
	}

	@Override
	public boolean isBasicAuth() {
		return true;
	}
	
	public List<InformacaoCriminalDTO> recuperarInformacaoCriminal(String numeroProcesso) throws PJeException{
		String url = "/criminal/eventosCriminais/processos/"+ numeroProcesso;
		List<InformacaoCriminalDTO> resultado = executarRest(url, HttpMethod.GET, new GenericType<List<InformacaoCriminalDTO>>(){});
		return resultado;
	}
}