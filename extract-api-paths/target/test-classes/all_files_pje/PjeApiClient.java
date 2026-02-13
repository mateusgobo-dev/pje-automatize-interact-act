package br.jus.cnj.pje.webservice.client;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.util.ComponentUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.com.infox.utils.Constantes;
import br.jus.cnj.pje.criminal.error.PjeErrorDetail;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeRestException;
import br.jus.cnj.pje.pjecommons.model.services.PjeResponse;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.cnj.pje.webservice.PjeEurekaRegister;

@Name(PjeApiClient.NAME)
@Scope(ScopeType.EVENT)
public class PjeApiClient implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "pjeApiClient";
	
	protected Client client;
	
	protected WebTarget webTarget;
	
	public static final String HEADER_PJE_LEGACY_APP = "X-pje-legacy-app";
	
	public static final String HEADER_PJE_LEGACY_CONTEXT_PATH = "X-pje-legacy-context-path";
	
	private Map<String, String> customHeaders = new HashMap<>();;
	public static PjeApiClient instance() {
		return ComponentUtil.getComponent(PjeApiClient.class);
	}
	
	@Create
	public void init() {
		this.client = ClientBuilder.newClient();
	}
	
	/**
	 * Consulta a api do PJe através do gateway e retorna o atributo result de {@link PjeResponse} como {@link String}
	 * @param apiPath path para a api a ser consultada
	 * @return atributo result de {@link PjeResponse} como {@link String}
	 * @throws PJeRestException
	 */
	@SuppressWarnings("unchecked")
	public String getStringValue(String apiPath) throws PJeRestException {
		PjeResponse<String> pjeResponse = new PjeResponse<String>();
		
		try {
			pjeResponse = (PjeResponse<String>) this.get(apiPath, String.class, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pjeResponse.getResult();
	}
	
	/**
	 * Consulta a api do PJe através do gateway e retorna o atributo result de {@link PjeResponse} como {@link Boolean}
	 * @param apiPath path para a api a ser consultada
	 * @return atributo result de {@link PjeResponse} como {@link Boolean}
	 * @throws PJeRestException
	 */
	@SuppressWarnings("unchecked")
	public Boolean getBooleanValue(String apiPath) throws PJeRestException {
		PjeResponse<Boolean> pjeResponse = (PjeResponse<Boolean>) this.get(apiPath, Boolean.class, true);
		return pjeResponse.getResult();
	}
	
	/**
	 * Consulta a api do PJe através do gateway e retorna o atributo result de {@link PjeResponse} como {@link Integer}
	 * @param apiPath path para a api a ser consultada
	 * @return atributo result de {@link PjeResponse} como {@link Integer}
	 * @throws PJeRestException
	 */
	@SuppressWarnings("unchecked")
	public Integer getIntegerValue(String apiPath) throws PJeRestException {
		PjeResponse<Integer> pjeResponse = (PjeResponse<Integer>) this.get(apiPath, Integer.class, true);
		return pjeResponse.getResult();		
	}	
	
	/**
	 * Consulta a api do PJe através do gateway e retorna o response como {@link String}
	 * @param apiPath path para a api a ser consultada
	 * @param incluirJsessionid define se deverá incluir cookie Jsessionid na consulta
	 * @return response como {@link String}
	 * @throws PJeRestException
	 */
	public String getStringValueSimple(String apiPath, boolean incluirJsessionid) throws PJeRestException {
		
		String ret = null;
		
		try {
			ret = (String) this.get(apiPath, String.class, false, incluirJsessionid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}

	public String getStringValueSimple(String apiPath) throws PJeRestException {
		return  getStringValueSimple(apiPath, true);
	}
	
	/**
	 * Consulta a api do PJe através do gateway e retorna o response como {@link Boolean}
	 * @param apiPath path para a api a ser consultada
	 * @return response como {@link Boolean}
	 * @throws PJeRestException
	 */
	public Boolean getBooleanValueSimple(String apiPath) throws PJeRestException {
		return (Boolean) this.get(apiPath, Boolean.class, false);
	}

	/**
	 * Consulta a api do PJe através do gateway e retorna o response como {@link Integer}
	 * @param apiPath path para a api a ser consultada
	 * @return response como {@link Integer}
	 * @throws PJeRestException
	 */
	public Integer getIntegerValueSimple(String apiPath) throws PJeRestException {
		return (Integer) this.get(apiPath, Integer.class, false);
	}
	
	public PjeResponse<?> get(String apiPath, Class<?> resultClazz) throws PJeRestException {
		return (PjeResponse<?>) this.get(apiPath, resultClazz, true);
	}
	
	private Object get(String apiPath, Class<?> resultClazz, Boolean isPjeResponse, boolean incluirJsessionid) throws PJeRestException{
		this.webTarget = this.client.target(getGatewayPath() +  apiPath);
		
		Invocation.Builder invocationBuilder = this.getInvocationDefaults(incluirJsessionid);

		ClientResponse resp;
		
		try{
			resp = (ClientResponse) invocationBuilder.get();
		}catch (Exception e) {
			throw new PJeRestException(e);
		}

		Object ret = this.handleResponse(resp, isPjeResponse, resultClazz);
		
		return ret;
	}

	private Object get(String apiPath, Class<?> resultClazz, Boolean isPjeResponse) throws PJeRestException{
		return get(apiPath, resultClazz, isPjeResponse, true);
	}
	
	public PjeResponse<?> delete(String apiPath, Class<?> clazz) throws PJeRestException {
		return (PjeResponse<?>) this.delete(apiPath, clazz, true);
	}
	
	private Object delete(String apiPath, Class<?> resultClazz, Boolean isPjeResponse) throws PJeRestException{
		this.webTarget = this.client.target(getGatewayPath()).path(apiPath);
		
		Invocation.Builder invocationBuilder = this.getInvocationDefaults();

		ClientResponse resp;
		
		try{
			resp = (ClientResponse) invocationBuilder.delete();
		}catch (Exception e) {
			throw new PJeRestException(e);
		}

		Object ret = this.handleResponse(resp, isPjeResponse, resultClazz);
		
		return ret;
	}	
	
	public PjeResponse<?> post(String apiPath, Class<?> resultClazz, Object body) throws PJeRestException {
		return (PjeResponse<?>) this.post(apiPath, resultClazz, true, body);
	}
	
	public Object post(String apiPath, Class<?> resultClazz, Boolean isPjeResponse, Object body) throws PJeRestException{
		this.webTarget = this.client.target(getGatewayPath()).path(apiPath);
		
		Invocation.Builder invocationBuilder = this.getInvocationDefaults();
		
		ClientResponse resp;
		
		try{
			resp = (ClientResponse) invocationBuilder.post(Entity.entity(body, MediaType.APPLICATION_JSON));
		}catch (Exception e) {
			throw new PJeRestException(e);
		}

		Object ret = this.handleResponse(resp, isPjeResponse, resultClazz);
		
		return ret;
	}
	
	public PjeResponse<?> put(String apiPath, Class<?> resultClazz, Object body) throws PJeRestException {
		return (PjeResponse<?>) this.put(apiPath, resultClazz, true, body);
	}	
	
	private Object put(String apiPath, Class<?> resultClazz, Boolean isPjeResponse, Object body) throws PJeRestException{
		this.webTarget = this.client.target(getGatewayPath()).path(apiPath);
		
		Invocation.Builder invocationBuilder = this.getInvocationDefaults();

		ClientResponse resp;
		
		try{
			resp = (ClientResponse) invocationBuilder.put(Entity.entity(body, MediaType.APPLICATION_JSON));
		}catch (Exception e) {
			throw new PJeRestException(e);
		}

		Object ret = this.handleResponse(resp, isPjeResponse, resultClazz);
		
		return ret;
	}	
	
	private String getGatewayPath(){
		return PjeEurekaRegister.instance().getUrlGatewayService(false);
	}	
	
	private Invocation.Builder getInvocationDefaults(boolean incluirJsessionid) {
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		invocationBuilder
				.header(HEADER_PJE_LEGACY_APP, ConfiguracaoIntegracaoCloud.getAppName())
				.header(HEADER_PJE_LEGACY_CONTEXT_PATH, PjeUtil.instance().getContextPath())
				.cookie(ConfiguracaoIntegracaoCloud.getAppName().toUpperCase() + "-StickySessionRule", PjeEurekaRegister.instance().getInstanceId());

		if (incluirJsessionid) {
			invocationBuilder.cookie("JSESSIONID", PjeUtil.instance().getJsessionid());
		}

		for (Map.Entry<String, String> element : this.customHeaders.entrySet()) {
			invocationBuilder.header(element.getKey(), element.getValue());
		}
		
		return invocationBuilder;
	}

	private Invocation.Builder getInvocationDefaults() {
		return getInvocationDefaults(true);
	}
	
	private <T> GenericType<PjeResponse<T>> getPjeResponseType(final Class<T> clazz) {
	    ParameterizedType genericType = new ParameterizedType() {
	        public Type[] getActualTypeArguments() {
	            return new Type[]{clazz};
	        }

	        public Type getRawType() {
	            return PjeResponse.class;
	        }

	        public Type getOwnerType() {
	            return PjeResponse.class;
	        }
	    };
	    return new GenericType<PjeResponse<T>>(genericType) { };
	}
	
	private Object handleResponse(ClientResponse resp, Boolean isPjeResponse, Class<?> resultClazz) throws PJeRestException {
		
		PjeResponse<?> pjeResponse = null;
		
		Object ret = null;
		
		if(resp.getStatus() == HttpStatus.SC_OK){
			if(isPjeResponse) {
				pjeResponse = resp.readEntity(this.getPjeResponseType(resultClazz));
				ret = pjeResponse;
			} else {
				ret = resp.readEntity(resultClazz);
			}
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
	
	public JsonObject converterParaJsonObject(String json) {
		JsonObject resultado = new JsonObject();
		
		if (StringUtils.isNotBlank(json)) {
			String pre = !StringUtils.startsWith(json, "{") ? "{" : "";
			String pos = !StringUtils.endsWith(json, "}") ? "}" : "";
			
			json = pre + json + pos; 
			JsonParser parser = new JsonParser();
			try {
				resultado = parser.parse(json).getAsJsonObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return resultado;
	}
	
	public String encodeToBase64(String stringToEncode) {
		return Base64.getUrlEncoder().encodeToString(stringToEncode.getBytes());
	}
	
	public PjeApiClient addCustomHeader(String headerName, String headerValue) {
		this.customHeaders.put(headerName, headerValue);
		
		return this;
	}
	
	public PjeApiClient addBasicAuthHeader(String username, String password) {
		return this.addCustomHeader(Constantes.AUTHORIZATION_TOKEN, this.getBasicAuthCredentials(username, password));
	} 

	
	private String getBasicAuthCredentials(String username, String password){
		
		String auth = "";
		
		auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
		
		return "Basic " + auth;
	}	

	public Map<String, String> getCustomHeaders() {
		return customHeaders;
	}
	
}
