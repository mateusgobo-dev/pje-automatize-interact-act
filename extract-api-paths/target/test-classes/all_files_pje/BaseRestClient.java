package br.jus.cnj.pje.webservice.client;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.utils.Constantes;
import br.jus.cnj.pje.criminal.error.PjeErrorDetail;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.PJeRestException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.cnj.pje.webservice.PjeEurekaRegister;
import br.jus.pje.nucleo.dto.EntityPageDTO;

@Scope(ScopeType.EVENT)
public abstract class BaseRestClient<E> implements Serializable{

	private static final long serialVersionUID = 1L;

	public abstract String getServicePath();
	
	public abstract String getSearchPath();
	
	public abstract String getServiceUsername();
	
	public abstract String getServicePassword();
	
	public abstract boolean isBasicAuth();
	
	protected Client client;
	
	protected WebTarget webTarget;
	
	private Class<E> entityClass;
	
	private static final String COUNT_PATH = "count";
	
	public static final String HEADER_PJE_LEGACY_APP = "X-pje-legacy-app";
	
	public static final String HEADER_PJE_LEGACY_CONTEXT_PATH = "X-pje-legacy-context-path";
	
	@Create
	public void init(){
		this.client = ClientBuilder.newClient();
		this.client.register(new AuthenticationRestFilter());
		this.entityClass = getEntityClass();
	}
	
	public String getGatewayPath(){
		return PjeEurekaRegister.instance().getUrlGatewayService(false);
	}
	
	public List<E> getResources() throws ClientErrorException, PJeRestException{
		this.webTarget = this.client.target(getGatewayPath()).path(this.getServicePath());
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();
		}

		List<E> list = null;
		ClientResponse resp;
		
		try{
			resp = (ClientResponse) invocationBuilder.get();
		}catch (Exception e) {
			throw new PJeRestException(e);
		}

		if(resp.getStatus() == HttpStatus.SC_OK){
			list = resp.readEntity(this.getListType(entityClass));			
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
		
		if(list == null){
			list = new ArrayList<E>(0);
		}
		
		return list;
	}
	
	public E getResourceById(Integer id) throws ClientErrorException, PJeException{
		
		this.webTarget = this.client.target(getGatewayPath()).path(this.getServicePath() + "/" + id);
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		E ret;
		
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();
		}
				
		ClientResponse resp = (ClientResponse) invocationBuilder.get();
		
		if(resp.getStatus() == HttpStatus.SC_OK){
		 ret  = resp.readEntity(entityClass);			
		}else{
			PjeErrorDetail erro = resp.readEntity(PjeErrorDetail.class);		
			throw new PJeException(erro.toString());
		}
		
		return ret;
	}
	
	public E createResource(E resourceToCreate) throws ClientErrorException, PJeException{
		this.webTarget = this.client.target(getGatewayPath()).path(this.getServicePath());
		E response = null; 

		Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
		
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();
		}
		ClientResponse resp = null;
		try{
			resp = (ClientResponse) invocationBuilder.post(
				Entity.entity(resourceToCreate, MediaType.APPLICATION_JSON));
		}catch (Exception e) {
			throw new PJeException(e);
		}
		
		if(resp.getStatus() == HttpStatus.SC_OK){
			response  = resp.readEntity(entityClass);			
		}else{
			PjeErrorDetail erro = resp.readEntity(PjeErrorDetail.class);		
			throw new PJeException(erro.toString());
		}
		return response;
	}
	
	public E updateResource(E resourceToUpdate) throws ClientErrorException, PJeException{
		this.webTarget = this.client.target(getGatewayPath()).path(this.getServicePath());
		E response = null; 
		
		Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
		
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();
		}
		
		ClientResponse resp = (ClientResponse) invocationBuilder.put(
				Entity.entity(resourceToUpdate, MediaType.APPLICATION_JSON));
		
		if(resp.getStatus() == HttpStatus.SC_OK){
			response = resp.readEntity(this.entityClass);			
		}else{
			PjeErrorDetail erro =	resp.readEntity(PjeErrorDetail.class);		
			throw new PJeException(erro.getMessage());
		}
		return response;
	}	
	
	public Boolean deleteResource(Integer id) throws ClientErrorException {
		webTarget = this.client.target(getGatewayPath()).path(this.getServicePath() + "/" + id);
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		if (this.isBasicAuth()) {
			invocationBuilder = this.getInvocationDefaults();
		}
		ClientResponse resp = (ClientResponse) invocationBuilder.delete();
		if (resp.getStatus() == HttpStatus.SC_OK) {
			return true;
		} else {
			PjeErrorDetail erro = resp.readEntity(PjeErrorDetail.class);
			throw new PJeRuntimeException(erro.getMessage());
		}
	}
	
	public Boolean inactivateResource(Integer id) throws ClientErrorException {
		webTarget = this.client.target(getGatewayPath()).path(this.getServicePath() + "/" + id);
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		if (this.isBasicAuth()) {
			invocationBuilder = this.getInvocationDefaults();
		}
		ClientResponse resp = (ClientResponse) invocationBuilder.delete();
		if (resp.getStatus() == HttpStatus.SC_OK) {
			return true;
		} else {
			PjeErrorDetail erro = resp.readEntity(PjeErrorDetail.class);
			throw new PJeRuntimeException(erro.getMessage());
		}
	}
	
	public EntityPageDTO<E> searchResources(Integer page, Integer size) throws ClientErrorException{
		return this.searchResources(page, size, null);
	}	
	
	@SuppressWarnings("unchecked")
	public EntityPageDTO<E> searchResources(Integer page, Integer size, E resourceExample) throws ClientErrorException{
		EntityPageDTO<E> ret = null;
		
		if(page != null && size != null){
			
			this.webTarget = this.client.target(getGatewayPath())
										.path(this.getServicePath() + "/" + this.getSearchPath())
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
	
	public Long countResources() throws ClientErrorException{

		this.webTarget = this.client.target(getGatewayPath()).path(this.getServicePath() + '/' + COUNT_PATH);
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();
		}

		Long count = null; 
		
		ClientResponse resp = (ClientResponse) invocationBuilder.get();
		
		if(resp.getStatus() == HttpStatus.SC_OK){
			count = resp.readEntity(Long.class);			
		}else{
			PjeErrorDetail erro =	resp.readEntity(PjeErrorDetail.class);		
			throw new PJeRuntimeException(erro.getMessage());
		}
		return count;
	}	
	
	public Long countResources(E resourceExample) throws ClientErrorException{

		this.webTarget = this.client.target(getGatewayPath()).path(this.getServicePath() + '/' + COUNT_PATH);
	
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		if(this.isBasicAuth()){
			invocationBuilder = this.getInvocationDefaults();
		}		

		Long count = null; 
		
		ClientResponse resp = (ClientResponse) invocationBuilder.post(Entity.entity(resourceExample == null ? Entity.json(null) : resourceExample, MediaType.APPLICATION_JSON));
		
		if(resp.getStatus() == HttpStatus.SC_OK){
			count = resp.readEntity(Long.class);			
		}else{
			PjeErrorDetail erro =	resp.readEntity(PjeErrorDetail.class);		
			throw new PJeRuntimeException(erro.toString());
		}
		return count;
	}
	

	private <T> GenericType<List<T>> getListType(final Class<T> clazz) {
	    ParameterizedType genericType = new ParameterizedType() {
	        public Type[] getActualTypeArguments() {
	            return new Type[]{clazz};
	        }

	        public Type getRawType() {
	            return List.class;
	        }

	        public Type getOwnerType() {
	            return List.class;
	        }
	    };
	    return new GenericType<List<T>>(genericType) { };
	}
	
	@SuppressWarnings("unchecked")
	public Class<E> getEntityClass(){
		if (entityClass == null){
			Type type = getClass().getGenericSuperclass();
			if (type instanceof ParameterizedType){
				ParameterizedType paramType = (ParameterizedType) type;
				if (paramType.getActualTypeArguments().length == 2){
					if (paramType.getActualTypeArguments()[1] instanceof TypeVariable){
						throw new IllegalArgumentException("Could not guess entity class by reflection");
					}
					else{
						entityClass = (Class<E>) paramType.getActualTypeArguments()[0];
					}
				}
				else{
					entityClass = (Class<E>) paramType.getActualTypeArguments()[0];
				}
			}
			else{
				throw new IllegalArgumentException("Could not guess entity class by reflection");
			}
		}
		return entityClass;
	}
	
	protected String getSessionId(){
		return PjeUtil.instance().getJsessionid();
	}
	
	protected String getBasicAuthCredentials(){
		
		String auth = "";
		
		auth = Base64.getEncoder().encodeToString((Authenticator.getUsuarioLogado().getLogin() + ":" + this.getServicePassword()).getBytes());
		
		return auth;
	}
	
	public Invocation.Builder getInvocationDefaults() {
		RefreshableKeycloakSecurityContext ksc = (RefreshableKeycloakSecurityContext) Contexts.getSessionContext().get(Constantes.SSO_CONTEXT_NAME);
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		if(ksc != null) {
			invocationBuilder = invocationBuilder.header("Authorization", "Bearer " + ksc.getTokenString());
		} else {
			invocationBuilder = invocationBuilder.header("Authorization", "Basic " + this.getBasicAuthCredentials());
		}
		
		return invocationBuilder
					.header(HEADER_PJE_LEGACY_APP, ConfiguracaoIntegracaoCloud.getAppName())
					.header(HEADER_PJE_LEGACY_CONTEXT_PATH, PjeUtil.instance().getContextPath())
					.cookie("JSESSIONID", this.getSessionId())
					.cookie(ConfiguracaoIntegracaoCloud.getAppName().toUpperCase() + "-StickySessionRule", PjeEurekaRegister.instance().getInstanceId());
	} 

}


