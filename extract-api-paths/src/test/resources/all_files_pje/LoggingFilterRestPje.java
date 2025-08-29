package br.jus.cnj.pje.webservice.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

@Name(LoggingFilterRestPje.LOGGING_FILTER_REST_PJE)
@Scope(ScopeType.APPLICATION)
public class LoggingFilterRestPje implements ClientRequestFilter, ClientResponseFilter {
	static final String LOGGING_FILTER_REST_PJE = "loggingFilterRestPje";
	private static final Logger logger = LoggerFactory.getLogger(LoggingFilterRestPje.class);

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		if(logger.isDebugEnabled()){
			logger.debug("-->DADOS INTEGRACAO - REQUISICAO API<--");
			Object entity = null;
			String metodoHttpChamado = null;
			String enderecoChamado = null;
			String entityJson = null;
			if (requestContext != null) {
	
				metodoHttpChamado = requestContext.getMethod();
				enderecoChamado = requestContext.getUri().toString();
				if (requestContext.hasEntity()) {
					entity = requestContext.getEntity();
					ObjectMapper mapper = new ObjectMapper();
					entityJson = mapper.writeValueAsString(entity);
				}
			}
			logger.debug("-----Endereco requisio: " + enderecoChamado);
			logger.debug("-----Metodo Http requisicao: " + metodoHttpChamado);
			logger.debug("-----Entidade enviada: " + entity);
			logger.debug("-----Entidade enviada (JSON FORMAT): " + entityJson);
			if (requestContext != null) {
				MultivaluedMap<String, Object> headersMap = requestContext.getHeaders();
				Set<String> keysHeader = headersMap.keySet();
				logger.debug("-----HEADERS");
				for (String key : keysHeader) {
					logger.debug("-------chave: " + key + ", valor: " + headersMap.get(key));
				}
				logger.debug("-----FIM HEADERS");
			}
	
			logger.debug("-->FIM DADOS INTEGRACAO - REQUISICAO API<--");
		}
	}

	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
		if(logger.isDebugEnabled()){
			logger.debug("-->DADOS INTEGRACAO - RESPOSTA REQUISICAO API<--");
			Object entity = null;
			String metodoHttpChamado = null;
			String enderecoChamado = null;
			String entityJson = null;
	
			String codigoHttpRetorno = null;
			String entidadeRetorno = null;
	
			if (requestContext != null) {
				metodoHttpChamado = requestContext.getMethod();
				enderecoChamado = requestContext.getUri().toString();
				if (requestContext.hasEntity()) {
					entity = requestContext.getEntity();
					ObjectMapper mapper = new ObjectMapper();
					entityJson = mapper.writeValueAsString(entity);
				}
			}
	
			if (responseContext != null) {
				codigoHttpRetorno = responseContext.getStatus() + "";
				if (responseContext.hasEntity()) {
	
					ByteArrayOutputStream result = getEntityStream(responseContext);
					entidadeRetorno = result.toString("UTF-8");
					responseContext.setEntityStream(new ByteArrayInputStream(result.toByteArray()));
				}
			}
			logger.debug("---REQUEST---");
			logger.debug("-----Endereco requisicao: " + enderecoChamado);
			logger.debug("-----Metodo Http requisicao: " + metodoHttpChamado);
			logger.debug("-----Entidade enviada: " + entity);
			logger.debug("-----Entidade enviada (JSON FORMAT): " + entityJson);
			logger.debug("---RESPONSE---");
			logger.debug("-----Codigo Http: " + codigoHttpRetorno);
			logger.debug("-----Corpo: " + entidadeRetorno);
			logger.debug("-->FIM DADOS INTEGRACAO - RESPOSTA REQUISIO API<--");
		}
	}

	private ByteArrayOutputStream getEntityStream(ClientResponseContext responseContext) throws IOException {
		InputStream stream = responseContext.getEntityStream();
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = stream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		stream.close();
		return result;
	}
}
