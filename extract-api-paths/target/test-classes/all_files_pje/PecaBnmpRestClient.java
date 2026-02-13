package br.jus.cnj.pje.webservice.client.bnmp;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.criminal.error.PjeErrorDetail;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.webservice.client.bnmp.dto.AuthenticationResponseDTO;
import br.jus.cnj.pje.webservice.client.bnmp.dto.PecaDTO;
import br.jus.pje.nucleo.dto.EntityPageDTO;


public abstract class PecaBnmpRestClient<E> extends BnmpRestClient<E> {
	
	public static final String NAME = "pecaBnmpRestClient";
	
	private static final long serialVersionUID = 1L;
	
	public abstract String getWebPath();
	
	public abstract String getWebSearchPath();

	@Override
	public String getSearchPath() {
		return "pessoa";
	}
	
	public PecaBnmpRestClient() {
		super();
	}
	
	public String getAssinaServidorPath() {
		return "assinar-servidor";
	}

	public String getAssinaMagistradoPath() {
		return "assinar-magistrado";
	}

	private PecaDTO assina(String idPeca, String assinaPecaPath) throws ClientErrorException, PJeException {
			this.webTarget = this.client.target(getGatewayPath()).path(this.getServicePath()).path(assinaPecaPath).path(idPeca);
			PecaDTO response = null; 

			Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
			
			if(this.isBasicAuth()){
				invocationBuilder = this.getInvocationDefaults();
				ZoneId fusoHorarioDeSaoPaulo = ZoneId.of("America/Sao_Paulo");
				ZonedDateTime timeStamp = ZonedDateTime.now(fusoHorarioDeSaoPaulo);
				invocationBuilder
				.header("login", Authenticator.getUsuarioLogado().getLogin())
				.header("senha", ((AuthenticationResponseDTO)Authenticator.getUsuarioBNMPLogado()).getPassword())
				.header("dataZoneLocal",timeStamp.format(DateTimeFormatter.RFC_1123_DATE_TIME));
			}
			ClientResponse resp = null;
			try{
				resp = (ClientResponse) invocationBuilder.get();
			}catch (Exception e) {
				throw new PJeException(e);
			}
			
			if(resp.getStatus() == HttpStatus.SC_OK){
				response  = resp.readEntity(PecaDTO.class);			
			}else{
				PjeErrorDetail erro = resp.readEntity(PjeErrorDetail.class);		
				throw new PJeException(erro.toString());
			}
			return response;
	}

	public PecaDTO assinaMagistrado(String idPeca) throws ClientErrorException, PJeException {
		return assina(idPeca, getAssinaMagistradoPath());
	}
	
	public PecaDTO assinaServidor(String idPeca) throws ClientErrorException, PJeException {
		return assina(idPeca, getAssinaServidorPath());
	}

	public String getJSONResourceById(Number id) throws ClientErrorException, PJeException{
		
		this.webTarget = this.client.target(getGatewayPath()).path(this.getServicePath()).path(id+"");
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		String ret;
		
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();
		}
				
		ClientResponse resp = (ClientResponse) invocationBuilder.get();
		
		if(resp.getStatus() == HttpStatus.SC_OK){
		 ret  = resp.readEntity(String.class);		
		}else{
			PjeErrorDetail erro = resp.readEntity(PjeErrorDetail.class);		
			throw new PJeException(erro.toString());
		}
		return ret;
	}
	
	public EntityPageDTO<E> pesquisaByPessoaRJIByTipoPecaAndByStatusPeca(Integer page, Integer size, E resourceExample) throws ClientErrorException{
		EntityPageDTO<E> ret = null;
		
		if(page != null && size != null){
			
			this.webTarget = this.client.target(getGatewayPath())
										.path("bnmpservice/api/pecas/light-filter")
										.queryParam("page", page)
										.queryParam("size", size);
			Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
			
			if(this.isBasicAuth()){
				invocationBuilder = this.getInvocationDefaults();
			}
			try {
				EntityPageDTO<E> response  = null;
				
				ClientResponse resp = (ClientResponse) invocationBuilder.post(Entity.entity(resourceExample == null ? Entity.json(null) : resourceExample, MediaType.APPLICATION_JSON));
				
				if(resp.getStatus() == HttpStatus.SC_OK){
					response = resp.readEntity(EntityPageDTO.class);			
				}else{
					PjeErrorDetail erro =	resp.readEntity(PjeErrorDetail.class);		
					throw new PJeRuntimeException(erro.getMessage());
				}
				
				if(response != null){
					ObjectMapper mapper = new ObjectMapper();
							   mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
										false);
						List<E> lista = mapper.readValue(mapper.writeValueAsString(response.getContent()), mapper.getTypeFactory().constructCollectionType(ArrayList.class, this.getEntityClass()));
						response.setContent(lista);
					ret = response;
				}
			} catch (PJeRuntimeException e) {
				throw e;
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	/**
	 * retorna acao de acordo com o tipo de pea
	 * aes: 	Cadastrar ( Mandado Priso, Mandado Internao, Certido de Cumprimento (Priso e Internao), Alvar de Soltura
	 * 			Novo ( Contramandado)
	 * 			Editar (Todas as peas)
	 * 			Visualizar (Todas as peas)
	 * @return
	 */
	public String getAcaoCadastro() {
		return "cadastrar";
	}

}
