package br.jus.cnj.pje.webservice.client.bnmp;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.seam.annotations.Name;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.jus.cnj.pje.criminal.error.PjeErrorDetail;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.PessoaDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.PessoaListDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.PessoaResponseDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.filter.PessoaFilter;
import br.jus.pje.nucleo.dto.EntityPageDTO;

@Name(PessoaRestClient.NAME)
public class PessoaRestClient extends BnmpRestClient<PessoaResponseDTO> {

	public static final String NAME = "pessoaRestClient";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getServicePath() {
		return "bnmpservice/api/pessoas";
	}

	@Override
	public String getSearchPath() {
		return "duplicidades";
	}
	
	public String getResourceByRJI() {
		return "rji";
	}
	
	public String getStatusPessoaPath() {
		return "status-pessoas";
	}
	
	@SuppressWarnings("unchecked")
	public EntityPageDTO<PessoaListDTO> searchDuplicidades(Integer page, Integer size, PessoaFilter resourceExample) throws ClientErrorException{
		EntityPageDTO<PessoaListDTO> ret = null;
		
		if(page != null && size != null){
			
			this.webTarget = this.client.target(getGatewayPath())
										.path(this.getServicePath()).path(this.getSearchPath())
										.queryParam("page", page)
										.queryParam("size", size);
			Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
			
			if(this.isBasicAuth()){
				invocationBuilder = this.getInvocationDefaults();
			}
			try {
				EntityPageDTO<PessoaListDTO> response  = null;
				
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

						List<PessoaListDTO> lista = mapper.readValue(mapper.writeValueAsString(response.getContent()), mapper.getTypeFactory().constructCollectionType(ArrayList.class, PessoaListDTO.class));
						response.setContent(lista);
					ret = response;
				}
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

	public PessoaResponseDTO createResource(PessoaDTO resourceToCreate) throws ClientErrorException, PJeException{
		this.webTarget = this.client.target(getGatewayPath()).path(this.getServicePath());
		PessoaResponseDTO response = null;
		MultipartFormDataOutput mpfdo = new MultipartFormDataOutput();
        mpfdo.addFormData("pessoa",resourceToCreate , MediaType.APPLICATION_JSON_TYPE);
        Entity.entity(mpfdo, MediaType.MULTIPART_FORM_DATA_TYPE);
		Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.MULTIPART_FORM_DATA).header("Accept", MediaType.MULTIPART_FORM_DATA_TYPE);
		
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();
		}
		ClientResponse resp = null;
		try{
			resp = (ClientResponse) invocationBuilder.post(Entity.entity(mpfdo, MediaType.MULTIPART_FORM_DATA_TYPE));
		}catch (Exception e) {
			e.printStackTrace();
			throw new PJeException(e);
		}
		if(resp.getStatus() == HttpStatus.SC_CREATED){
			response  = resp.readEntity(PessoaResponseDTO.class);			
		}else{
			PjeErrorDetail erro = resp.readEntity(PjeErrorDetail.class);		
			throw new PJeException(erro.toString());
		}
		return response;
	}
}
