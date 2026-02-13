package br.com.infox.pje.processor.strategy;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.json.JSONObject;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.service.TribunalService;
import br.jus.cnj.pje.webservice.util.RestUtil;
import br.jus.pje.nucleo.entidades.LogIntegracao;
import io.restassured.http.Method;

/**
 * Interface de persistência do LogIntegracao para requisições com payload String.
 * 
 * @author Adriano Pamplona
 */
@Name("stringLogIntegracaoRequestStrategy")
public class StringLogIntegracaoRequestStrategy implements LogIntegracaoRequestStrategy {

	private static final String RAISE_REMOVE_LOG_EVENT = "RAISE_REMOVE_LOG_EVENT";

	@Logger
	private Log log;
	
	/**
     * @return Instância da classe.
     */
    public static StringLogIntegracaoRequestStrategy instance() {
        return ComponentUtil.getComponent(StringLogIntegracaoRequestStrategy.class);
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public <T> T execute(LogIntegracao logIntegracao) {
		String token = TribunalService.instance().login();
		Method method = Method.valueOf(logIntegracao.getRequestMethod());
		
		T responseSuccess = (T) RestUtil.request(
				logIntegracao.getRequestUrl(), 
				logIntegracao.getRequestPayload(), 
				JSONObject.class, 
				token, 
				method, 
				RestUtil.newCallbackExceptionLog(null, null, token, logIntegracao, null));

		// DISPARA EVENTO ASSÍNCRONO PARA REMOVER O REGISTRO DA TABELA
		// TB_LOG_INTEGRACAO, CASO A RESPOSTA FOI BEM-SUCEDIDA
		if (responseSuccess != null) {
			JSONObject json = (JSONObject) responseSuccess;
			log.info("NO_CONTENT:", json.toString());

			Events.instance().raiseAsynchronousEvent(RAISE_REMOVE_LOG_EVENT, logIntegracao);
		}

		return responseSuccess;
	}
}
