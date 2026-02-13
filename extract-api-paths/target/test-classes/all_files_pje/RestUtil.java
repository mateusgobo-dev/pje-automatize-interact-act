package br.jus.cnj.pje.webservice.util;

import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.persistence.EntityManager;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.infox.cliente.util.HttpUtil;
import br.com.infox.cliente.util.JSONUtil;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.manager.ProcessoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.pje.nucleo.entidades.LogIntegracao;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.HttpResponseInaptosJobDomEletronicoProcessorEnum;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;

@Name("restUtil")
@AutoCreate
public class RestUtil {
	
	@Logger
	private static Log log;
	
	public static final String RAISE_PERSIST_LOG_EVENT = "RAISE_PERSIST_LOG_EVENT";

	private static final String RAISE_UPDATE_LOG_EVENT = "RAISE_UPDATE_LOG_EVENT";

	private static final String RAISE_REMOVE_LOG_EVENT = "RAISE_REMOVE_LOG_EVENT";
	
	public Response reportarErro(Exception e) {
		Response response;
		log.error("Erro no serviço de informações de sessão", e);
		response = Response.serverError()
				.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		return response;
	}
	
    public static Response sucesso(JSONObject json){
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(json.toString()).build();
	}

	public static Response sucesso(JSONArray json){
		return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(json.toString()).build();
	}
    
    public static Response sucesso(){
        return Response.ok().build();
	}
    
    public static Response criadoComSucesso(){
        return Response.status(Response.Status.CREATED).build();
	}
	
    public static Response erroRequisicao(){
        return Response.status(Response.Status.BAD_REQUEST).build();
	}
    
    public static Response erro(Status status){
        return Response.status(status).build();
	}
	
	public static Response sucesso(String texto){
        return Response.status(Response.Status.OK).type(MediaType.TEXT_PLAIN).entity(texto).build();
	}
	
	public static Response erroRequisicao(String texto){
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(texto).build();
	}
	
	/**
	 * Executa uma URL rest e retorna a resposta caso o resultado seja HTTP 200.
	 * 
	 * @param url URL da api rest.
	 * @param parametro Objeto que ser enviado no body.
	 * @param tipoRetorno Tipo do objeto de retorno.
	 * @return restassures.response.Response
	 */
	public static <T> T get(String url, Object parametro, Class<T> tipoRetorno) {
		return request(url, parametro, tipoRetorno, null, Method.GET);
	}
	
	/**
	 * Executa uma URL rest e retorna a resposta caso o resultado seja HTTP 200.
	 * 
	 * @param url URL da api rest.
	 * @param parametro Objeto que ser enviado no body.
	 * @param tipoRetorno Tipo do objeto de retorno.
	 * @param token Token que ser adicionado no header.
	 * @return restassures.response.Response
	 */
	public static <T> T get(String url, Object parametro, Class<T> tipoRetorno, String token) {
		return request(url, parametro, tipoRetorno, token, Method.GET);
	}
	
	/**
	 * Executa uma URL rest e retorna a resposta caso o resultado seja HTTP 200.
	 * 
	 * @param url URL da api rest.
	 * @param parametro Objeto que ser enviado no body.
	 * @param tipoRetorno Tipo do objeto de retorno.
	 * @param token Token que ser adicionado no header.
	 * @return restassures.response.Response
	 */
	public static <T> T getAsync(String url, Object parametro, Class<T> tipoRetorno, String token) {
		return requestAsync(url, parametro, tipoRetorno, token, Method.GET, null);
	}
	
	/**
	 * Executa uma URL rest e retorna a resposta caso o resultado seja HTTP 200.
	 * 
	 * @param url URL da api rest.
	 * @param parametro Objeto que ser enviado no body.
	 * @param tipoRetorno Tipo do objeto de retorno.
	 * @return restassures.response.Response
	 */
	public static <T> T post(String url, Object parametro, Class<T> tipoRetorno) {
		return request(url, parametro, tipoRetorno, null, Method.POST);
	}
	
	/**
	 * Executa uma URL rest e retorna a resposta caso o resultado seja HTTP 200.
	 * 
	 * @param url URL da api rest.
	 * @param parametro Objeto que ser enviado no body.
	 * @param tipoRetorno Tipo do objeto de retorno.
	 * @param token Token que ser adicionado no header.
	 * @return restassures.response.Response
	 */
	public static <T> T post(String url, Object parametro, Class<T> tipoRetorno, String token) {
		return request(url, parametro, tipoRetorno, token, Method.POST);
	}
	
	/**
	 * Executa uma URL rest e retorna a resposta caso o resultado seja HTTP 200.
	 * 
	 * @param url URL da api rest.
	 * @param parametro Objeto que ser enviado no body.
	 * @param tipoRetorno Tipo do objeto de retorno.
	 * @param token Token que ser adicionado no header.
	 * @return restassures.response.Response
	 */
	public static <T> T postAsync(String url, Object parametro, Class<T> tipoRetorno, String token) {
		return requestAsync(url, parametro, tipoRetorno, token, Method.POST, null);
	}
	
	/**
	 * Executa uma URL rest e retorna a resposta caso o resultado seja HTTP 200.
	 * 
	 * @param url URL da api rest.
	 * @param parametro Objeto que ser enviado no body.
	 * @param tipoRetorno Tipo do objeto de retorno.
	 * @return restassures.response.Response
	 */
	public static <T> T patch(String url, Object parametro, Class<T> tipoRetorno) {
		return request(url, parametro, tipoRetorno, null, Method.PATCH);
	}
	
	/**
	 * Executa uma URL rest e retorna a resposta caso o resultado seja HTTP 200.
	 * 
	 * @param url URL da api rest.
	 * @param parametro Objeto que ser enviado no body.
	 * @param tipoRetorno Tipo do objeto de retorno.
	 * @param token Token que ser adicionado no header.
	 * @return restassures.response.Response
	 */
	public static <T> T patch(String url, Object parametro, Class<T> tipoRetorno, String token) {
		return request(url, parametro, tipoRetorno, token, Method.PATCH);
	}
	
	/**
	 * Executa uma URL rest e retorna a resposta caso o resultado seja HTTP 200.
	 * 
	 * @param url URL da api rest.
	 * @param parametro Objeto que ser enviado no body.
	 * @param tipoRetorno Tipo do objeto de retorno.
	 * @param token Token que ser adicionado no header.
	 * @return restassures.response.Response
	 */
	public static <T> T patchAsync(String url, Object parametro, Class<T> tipoRetorno, String token) {
		return requestAsync(url, parametro, tipoRetorno, token, Method.PATCH, null);
	}

	/**
	 * Executa uma URL rest e retorna a resposta caso o resultado seja HTTP 200.
	 * 
	 * @param url URL da api rest.
	 * @param parametro Objeto que ser enviado no body.
	 * @param tipoRetorno Tipo do objeto de retorno.
	 * @return restassures.response.Response
	 */
	public static <T> T put(String url, Object parametro, Class<T> tipoRetorno) {
		return request(url, parametro, tipoRetorno, null, Method.PUT);
	}
	
	/**
	 * Executa uma URL rest e retorna a resposta caso o resultado seja HTTP 200.
	 * 
	 * @param url URL da api rest.
	 * @param parametro Objeto que ser enviado no body.
	 * @param tipoRetorno Tipo do objeto de retorno.
	 * @param token Token que ser adicionado no header.
	 * @return restassures.response.Response
	 */
	public static <T> T put(String url, Object parametro, Class<T> tipoRetorno, String token) {
		return request(url, parametro, tipoRetorno, token, Method.PUT);
	}
	
	/**
	 * Executa uma URL rest e retorna a resposta caso o resultado seja HTTP 200.
	 * 
	 * @param url URL da api rest.
	 * @param parametro Objeto que ser enviado no body.
	 * @param tipoRetorno Tipo do objeto de retorno.
	 * @param token Token que ser adicionado no header.
	 * @return restassures.response.Response
	 */
	public static <T> T putAsync(String url, Object parametro, Class<T> tipoRetorno, String token) {
		return requestAsync(url, parametro, tipoRetorno, token, Method.PUT, null);
	}
	
	/**
	 * Executa uma URL rest e retorna a resposta caso o resultado seja HTTP 200.
	 * 
	 * @param url URL da api rest.
	 * @param parametro Objeto que ser enviado no body.
	 * @param tipoRetorno Tipo do objeto de retorno.
	 * @param token Token que ser adicionado no header.
	 * @param method Mtodo do protocolo HTTP.
	 * @return restassures.response.Response
	 */
	public static <T> T request(String url, Object parametro, Class<T> tipoRetorno, String token, Method method) {
		return request(url, parametro, tipoRetorno, token, method, newCallbackException());
	}
	
	/**
	 * Executa uma URL rest e retorna a resposta caso o resultado seja HTTP 200.
	 * 
	 * @param url URL da api rest.
	 * @param parametro Objeto que ser enviado no body.
	 * @param tipoRetorno Tipo do objeto de retorno.
	 * @param token Token que ser adicionado no header.
	 * @param method Método do protocolo HTTP.
	 * @param callbackResponse Callback de tratamento do response.
	 * @return restassures.response.Response
	 */
	public static <T> T request(String url, Object parametro, Class<T> tipoRetorno, String token, Method method, Consumer<io.restassured.response.Response> callbackException) {
		try {
			String json = JSONUtil.converterObjetoParaString(parametro);

			method = ObjectUtils.firstNonNull(method, Method.POST);

			RequestSpecification request = RestAssured.given()
	                                                  .contentType(ContentType.JSON)
	                                                  .accept(ContentType.JSON)
	                                                  .body(json);

			if (StringUtils.isNotBlank(token)) {
	            request.auth()
	                   .preemptive()
	                   .oauth2(token);
			}

			request.when().log().all();

			io.restassured.response.Response response = request.request(method, url);

			response.then().log().all(true);

			if (HttpUtil.isStatus2xx(response.getStatusCode())) {
				if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
					return createJsonWhenNonContent(response, tipoRetorno, parametro);
				} else {
					return createJsonFromResponse(response, tipoRetorno, parametro);
				}
			} else {
				Consumer<io.restassured.response.Response> callback = ObjectUtils.firstNonNull(callbackException, newCallbackException());
				callback.accept(response);

//				logNotSuccess(response, tipoRetorno, parametro);
			}
		} catch (Exception e) {
			String mensagem = "Nao foi possivel chamar o servico '%s'. Erro: %s";
			throw new PJeRuntimeException(String.format(mensagem, url, e.getLocalizedMessage()), e);
		}

		return null;
	}

	/**
     * Loga detalhes de uma resposta não bem-sucedida.
     *
     * @param response O objeto de resposta contendo os detalhes da resposta HTTP.
     * @param tipoRetorno A classe do tipo do objeto esperado como retorno.
     * @param <T> O tipo do objeto de retorno.
     */
	private static <T> void logNotSuccess(io.restassured.response.Response response, Class<T> tipoRetorno, Object parametro) {
		try {
			T tJson = createJsonFromResponse(response, tipoRetorno, parametro);

			if (tJson instanceof JSONObject) {
				JSONObject contentJson = (JSONObject) tJson;
				log.error("[NOT_SUCCESS_REQUEST_RESTUTIL]: \n %s", contentJson.toString(4));
			}
		} catch (Exception e) {
			throw new RuntimeException("Não foi possível formatar a string JSON", e);
		}
	}

	/**
     * Cria um objeto do tipo T a partir do corpo da resposta JSON.
     *
     * @param response O objeto de resposta contendo os detalhes da resposta HTTP.
     * @param tipoRetorno A classe do tipo do objeto esperado como retorno.
     * @param <T> O tipo do objeto de retorno.
     * @return O objeto do tipo T criado a partir do corpo da resposta JSON.
     */
	private static <T> T createJsonFromResponse(io.restassured.response.Response response, Class<T> tipoRetorno, Object parametro) {
		String responseBody = response.getBody().asString();

		if (StringUtils.isNotBlank(responseBody)) {
			return JSONUtil.converterStringParaObjeto(responseBody, tipoRetorno);
		} else {
			return createJsonWhenNonContent(response, tipoRetorno, parametro);
		}
	}

	 /**
     * Cria um objeto JSON com campos padrão quando a resposta não possui conteúdo (204).
     *
     * @param response O objeto de resposta contendo os detalhes da resposta HTTP.
     * @param tipoRetorno O tipo de classe do objeto esperado como retorno.
     * @param <T> O tipo do objeto de retorno.
     * @return O objeto do tipo T criado a partir do objeto JSON.
     */
	private static <T> T createJsonWhenNonContent(io.restassured.response.Response response, Class<T> tipoRetorno, Object parametro) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("statusCode", response.getStatusCode());
		jsonObject.put("description", HttpStatus.getStatusText(response.getStatusCode()));

		if (parametro instanceof LogIntegracao) {
			LogIntegracao logIntegracao = (LogIntegracao) parametro;

			try {
				JSONObject logIntegracaoJson = new JSONObject(new ObjectMapper().writeValueAsString(logIntegracao));
				jsonObject.put("logIntegracao", logIntegracaoJson);
			} catch (JSONException | JsonProcessingException e) {
				log.error("[RESTUTIL] Erro durante a criação do Json: ", e.getLocalizedMessage());
			}
		}

		return JSONUtil.converterStringParaObjeto(jsonObject.toString(), tipoRetorno);
	}

	/**
	 * Executa uma URL rest e retorna a resposta caso o resultado seja HTTP 200.
	 * Os erros serão armazenados na tabela tb_log_integracao para que um Job o leia e faça o reenvio.
	 * 
	 * @param url URL da api rest.
	 * @param parametro Objeto que ser enviado no body.
	 * @param tipoRetorno Tipo do objeto de retorno.
	 * @param token Token que ser adicionado no header.
	 * @param method Método do protocolo HTTP.
	 * @param callbackResponse Callback de tratamento do response.
	 * @return restassures.response.Response
	 */
	public static <T> T requestAsync(String url, Object parametro, Class<T> tipoRetorno, String token, Method method, Consumer<io.restassured.response.Response> callbackException) {
		return request(
				url, 
				parametro, 
				tipoRetorno, 
				token, 
				method, 
				ObjectUtils.firstNonNull(callbackException, newCallbackExceptionLog(url, method, token, parametro, null)));
	}

	/**
	 * @return Callback de tratamento de erro.
	 */
	public static Consumer<io.restassured.response.Response> newCallbackException() {
		return (io.restassured.response.Response response) -> {
			if (!HttpUtil.isStatus2xx(response.getStatusCode())) {
				throw new AplicationException(response.getBody().asString());
			}
		};
	}

    /**
     * @param url
     * @param method
     * @param token
     * @param parametro
     * @param predicateIsException
     * @return Callback de tratamento de erro.
     */
	public static Consumer<io.restassured.response.Response> newCallbackExceptionLog(
            String url, 
            Method method, 
            String token, 
            Object parametro, 
            Predicate<io.restassured.response.Response> predicateIsException) {

		return response -> {
			int statusCode = response.getStatusCode();
			String responseBody = response.getBody().asString();

			if (parametro instanceof LogIntegracao) {
				LogIntegracao logIntegracao = (LogIntegracao) parametro;
				if (HttpResponseInaptosJobDomEletronicoProcessorEnum.existeHpttpResponse(statusCode, responseBody)) {
					Events.instance().raiseAsynchronousEvent(RAISE_REMOVE_LOG_EVENT, logIntegracao);
				} else if (!HttpUtil.isStatus2xx(statusCode)) {
					Events.instance().raiseAsynchronousEvent(RAISE_UPDATE_LOG_EVENT, token, statusCode, responseBody, logIntegracao);
				}
			} else {
		        Predicate<io.restassured.response.Response> exceptionPredicate = 
		        		(predicateIsException != null) ? 
		        				predicateIsException : 
		        					responsePredicate -> !HttpUtil.isStatus2xx(responsePredicate.getStatusCode());
		        					
				if (exceptionPredicate.test(response)) {
					Events.instance().raiseTransactionSuccessEvent(RAISE_PERSIST_LOG_EVENT, url, method, token, parametro, response);
				}
			}
		};
	}

	/**
	 * Inclui o LogIntegracao com os dados da requisição que deu erro.
	 * 
	 * @param url
	 * @param method
	 * @param token
	 * @param parametro
	 * @param response
	 */
	@Observer(RAISE_PERSIST_LOG_EVENT)
	@Asynchronous
	@Transactional
	public void persistLogIntegracao(String url, Method method, String token, Object parametro, io.restassured.response.Response response) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		log.info(logsLogIntegracao(parametro, null, stackTraceElements));
		try {
			String mensagem = response.getBody().asString();
			LogIntegracao logIntegracao = new LogIntegracao();
			logIntegracao.setData(new Date());
			logIntegracao.setRequestUrl(url);
			logIntegracao.setRequestMethod(method.toString());
			logIntegracao.setRequestPayload(JSONUtil.converterObjetoParaString(parametro));
			logIntegracao.setRequestPayloadClass(ClassUtils.getName(parametro));
			logIntegracao.setRequestToken(token);
			logIntegracao.setResponseStatus(response.getStatusCode());
			logIntegracao.setResponsePayload(mensagem);
			logIntegracao.setDataUltimaAlteracao(null);

			if (logIntegracao.getNumeroComunicacao() != null) {
				ProcessoParteExpedienteManager processoParteExpedienteManager = ProcessoParteExpedienteManager.instance();
				ProcessoParteExpediente processoParteExpediente = processoParteExpedienteManager.findById(logIntegracao.getNumeroComunicacao());
				logIntegracao.setProcessoParteExpediente(processoParteExpediente);
				logIntegracao.setProcessoTrf(processoParteExpediente.getProcessoJudicial());
				logIntegracao.setTipoProcessoDocumento(processoParteExpediente.getProcessoExpediente().getTipoProcessoDocumento());
				logIntegracao.setNumeroProcesso(processoParteExpediente.getProcessoJudicial().getNumeroProcesso());
			} else {
				ProcessoManager processoManager = ComponentUtil.getComponent(ProcessoManager.class);
				Processo processo = processoManager.findByNumeroProcesso(logIntegracao.getNumeroProcesso());
				ProcessoTrfManager processoTrfManager = ComponentUtil.getComponent(ProcessoTrfManager.class);
				ProcessoTrf processoTrf = processoTrfManager.getProcessoTrfByProcesso(processo);
				logIntegracao.setProcessoTrf(processoTrf);
				logIntegracao.setNumeroProcesso(processoTrf.getNumeroProcesso());
				logIntegracao.setProcessoParteExpediente(null);
				logIntegracao.setTipoProcessoDocumento(null);
			}

			EntityManager em = EntityUtil.getEntityManager();
			em.persist(logIntegracao);
			em.flush();
		} catch (Exception e) {
			log.info(logsLogIntegracao(parametro, e.getLocalizedMessage(), stackTraceElements));
		}
	}

	@Observer(RAISE_UPDATE_LOG_EVENT)
	@Transactional
	public void updateLogIntegracao(String token, int novoCodigoStatus, String novaMensagem, LogIntegracao logIntegracao) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		log.info(logsLogIntegracao(logIntegracao, null, stackTraceElements));
		try {
			EntityManager em = EntityUtil.getEntityManager();
			logIntegracao = em.find(LogIntegracao.class, logIntegracao.getId());
			logIntegracao.setDataUltimaAlteracao(new Date());
			logIntegracao.setRequestToken(token);
			logIntegracao.setResponseStatus(novoCodigoStatus);
			logIntegracao.setResponsePayload(novaMensagem);
			em.merge(logIntegracao);
			em.flush();
		} catch (Exception e) {
			log.error(logsLogIntegracao(logIntegracao, e.getLocalizedMessage(), stackTraceElements));
		}
	}

	@Observer(RAISE_REMOVE_LOG_EVENT)
	@Transactional
	public void removeLogIntegracao(LogIntegracao logIntegracao) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		log.info(logsLogIntegracao(logIntegracao, null, stackTraceElements));
		try {
			EntityManager em = EntityUtil.getEntityManager();
			logIntegracao = em.find(LogIntegracao.class, logIntegracao.getId());
			em.remove(logIntegracao);
			em.flush();
		} catch (Exception e) {
			log.error(logsLogIntegracao(logIntegracao, e.getLocalizedMessage(), stackTraceElements));
		}
	}

	public static String logsLogIntegracao(Object parametro, String e, StackTraceElement[] stackTraceElements) {
		LogIntegracao logIntegracao = null;

		if (parametro instanceof LogIntegracao) {
			logIntegracao = (LogIntegracao) parametro;
		}

	    String stringRequestPayload = logIntegracao != null ? logIntegracao.getRequestPayload() : JSONUtil.converterObjetoParaString(parametro);
		JSONObject jsonRequestPayload = JSONUtil.converterStringParaObjeto(stringRequestPayload, JSONObject.class);

		Integer idProcessoParteExpediente = jsonRequestPayload.optInt("idProcessoParteExpediente", 0);
		String numeroProcesso = jsonRequestPayload.optString("numeroProcesso");

		String idExibicao = idProcessoParteExpediente > 0 ? String.valueOf(idProcessoParteExpediente) : numeroProcesso;
		String textoExibicao = idProcessoParteExpediente > 0 ? "idProcessoParteExpediente" : "numeroProcesso";

		String methodName = stackTraceElements[1].getMethodName();
		String className = stackTraceElements[1].getClassName();

		String idLogIntegracao = (logIntegracao != null && logIntegracao.getId() != null && logIntegracao.getId() > 0)
				? String.format("para o id [%s] e", logIntegracao.getId())
				: "";

		if (e != null && !e.isEmpty()) {
			return String.format("[REST_UTIL] %s.%s %s %s [%s]. Erro: %s", className, methodName, idLogIntegracao,
					textoExibicao, idExibicao, e);
		} else {
			return String.format("[REST_UTIL] %s.%s %s %s [%s].", className, methodName, idLogIntegracao, textoExibicao,
					idExibicao);
		}
	}

}
