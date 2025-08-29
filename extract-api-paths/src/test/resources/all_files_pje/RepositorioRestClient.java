package br.jus.cnj.pje.webservice.client;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.PjeRestClientException;
import br.jus.cnj.pje.webservice.client.keycloak.SsoTokenRestClient;

@Name(RepositorioRestClient.NAME)
@Scope(ScopeType.EVENT)
public class RepositorioRestClient {
	private Logger log = Logger.getLogger(RepositorioRestClient.class);
	public static final String NAME = "repositorioRestClient";
	private static final String URL = ConfiguracaoIntegracaoCloud.getRepositorioUrl();
	
	@In(create=true)
	private SsoTokenRestClient ssoTokenRestClient;
	
	private String getToken() throws PjeRestClientException {
		return ssoTokenRestClient.getTokenSso();
	}
	
	public byte[] getFile(String context, String hash) {
		String url = URL + context + "?hash=" + hash;
		String fileUrl = getFileUrl(url);
		return getFile(fileUrl);		
	}

	private String getFileUrl(String serviceUrl) {
		String fileUrl = null;
		try {
			CloseableHttpClient client = HttpClients.custom().disableRedirectHandling().build();
			HttpGet request = new HttpGet(serviceUrl);
			String token = getToken();
			if (StringUtils.isNotEmpty(token)) {
				request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
			}
			HttpResponse response = client.execute(request);
			if(response.getStatusLine().getStatusCode() == 302) {
				fileUrl = response.getLastHeader("Location").getValue();
			}
		}
		catch(Exception e) {
			throw new PJeRuntimeException(e);
		}
		return fileUrl;
	}
	
	private byte[] getFile(String fileUrl) {
		byte[] file = null;
		CloseableHttpClient client = HttpClients.createDefault();
		try {
			HttpGet request = new HttpGet(fileUrl);
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			if(response.getStatusLine().getStatusCode() == 200) {
				file = IOUtils.toByteArray(entity.getContent());
			}
		}
		catch(Exception e) {
			throw new PJeRuntimeException(e);
		}
		finally {
			try {
				client.close();
			} catch (IOException e) {
				log.error("Não foi possível fechar a conexão HTTP:" + e.getLocalizedMessage());
			}
		}
		return file;
	}
}
