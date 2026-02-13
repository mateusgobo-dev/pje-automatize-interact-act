/**
 * HttpUtil.java
 *
 * Data: 28/08/2023
 */
package br.com.infox.cliente.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;
import org.json.JSONObject;

import br.com.itx.exception.AplicationException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;

/**
 * Classe utilitária para manipular HTTP.
 * 
 * @author Adriano Pamplona
 */
public class HttpUtil {

	@Logger
	private static Log logger;
	
	private static final String BEARER_TOKEN_FORMAT = "Bearer %s";
	public static final Integer DEFAULT_HTTP_CLIENT_TIMEOUT_IN_MILLIS = 30000;

	/**
	 * Construtor.
	 */
	private HttpUtil() {
		// Construtor.
	}

	/**
	 * @param status Código do status.
	 * @return True se o status for 2xx.
	 */
	public static boolean isStatus2xx(HttpResponse response) {
		return (response != null && isStatus2xx(response.getStatusLine().getStatusCode()));
	}

	/**
	 * @param status Código do status.
	 * @return True se o status for 2xx.
	 */
	public static boolean isStatus2xx(Integer status) {
		return (status >= 200 && status <= 299);
	}

	/**
	 * @param status Código do status.
	 * @return True se o status for 3xx.
	 */
	public static boolean isStatus3xx(Integer status) {
		return (status >= 300 && status <= 399);
	}

	/**
	 * @param status Código do status.
	 * @return True se o status for 4xx.
	 */
	public static boolean isStatus4xx(Integer status) {
		return (status >= 400 && status <= 499);
	}

	/**
	 * @param status Código do status.
	 * @return True se o status for 5xx.
	 */
	public static boolean isStatus5xx(Integer status) {
		return (status >= 500 && status <= 599);
	}

	public static <T> T getEntityFromGET(String url, String token, Class<T> pojoEntity) {
		T pojoResponse = null;
		
		if (StringUtils.isNotEmpty(url)) {
			HttpGet request = new HttpGet(url);
			if (StringUtils.isNotEmpty(token)) {
				request.addHeader(HttpHeaders.AUTHORIZATION, formatBearerToken(token));
			}
			try (CloseableHttpResponse response = createHttpClient().execute(request)) {
				HttpEntity entity = response.getEntity();
				if (isStatus2xx(response) && entity != null) {
					pojoResponse = JSONUtil.converterStringParaObjeto(entity, pojoEntity);
				}
			} catch (Exception e) {
				throw new AplicationException(e);
			}
		}
		
		return pojoResponse;
	}

	public static File getFileFromGET(String url, String token, String nomeArquivo) {
		File pojoResponse = null;
		
		if (StringUtils.isNotEmpty(url)) {
			HttpGet request = new HttpGet(url);
			// TJRJ: Retirado o token pois estava causando status 400 - bad request
			//if (StringUtils.isNotEmpty(token)) {
			//	request.addHeader(HttpHeaders.AUTHORIZATION, formatBearerToken(token));
			//}
			try (CloseableHttpResponse response = createHttpClient().execute(request)) {
				HttpEntity entity = response.getEntity();
				if (isStatus2xx(response) && entity != null) {
					pojoResponse = File.createTempFile(nomeArquivo, ".tmp");
					FileUtils.writeByteArrayToFile(pojoResponse, IOUtils.toByteArray(entity.getContent()));
				}
			} catch (Exception e) {
				throw new AplicationException(e);
			}
		}

		return pojoResponse;
	}

	public static JSONObject getJSONContent(String url, String token) {
		HttpResponse response = null;
		JSONObject jsonContent = null;
		CloseableHttpClient client = HttpClients.createDefault();
		try {
			HttpGet request = new HttpGet(url);
			if (StringUtils.isNotEmpty(token)) {
				request.addHeader(HttpHeaders.AUTHORIZATION, formatBearerToken(token));
			}
			response = client.execute(request);
			if(isStatus2xx(response.getStatusLine().getStatusCode())) {
				String content = EntityUtils.toString(response.getEntity(),"UTF-8");
				jsonContent = new JSONObject(content);
			}
		}
		catch(Exception e) {
			throw new PJeRuntimeException(e);
		}
		finally {
			try {
				client.close();
			} catch (IOException e) {
				logger.error("Não foi possível fechar a conexão HTTP:" + e.getLocalizedMessage());
			}
		}
		return jsonContent;
	}

	public static void postJSONContent(String url, String token, String jsonContent) {
		CloseableHttpClient client = HttpClients.createDefault();
		try {
			HttpPost post = new HttpPost(url);
			post.setHeader("Content-Type", "application/json");
			StringEntity json = new StringEntity(jsonContent, StandardCharsets.UTF_8);
			post.setEntity(json);
			if (StringUtils.isNotEmpty(token)) {
				post.addHeader(HttpHeaders.AUTHORIZATION, formatBearerToken(token));
			}
			client.execute(post);
		}
		catch(Exception e) {
			throw new PJeRuntimeException(e);
		}
		finally {
			try {
				client.close();
			} catch (IOException e) {
				logger.error("Não foi possível fechar a conexão HTTP:" + e.getLocalizedMessage());
			}
		}
	}

	public static CloseableHttpClient createHttpClient() {
		int timeout = DEFAULT_HTTP_CLIENT_TIMEOUT_IN_MILLIS;
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout)
				.setConnectionRequestTimeout(timeout)
				.setSocketTimeout(timeout)
				.build();
		return HttpClients.custom().setDefaultRequestConfig(config).build();
	}

	public static String formatBearerToken(String token) {
		return String.format(BEARER_TOKEN_FORMAT, token);
	}
}
