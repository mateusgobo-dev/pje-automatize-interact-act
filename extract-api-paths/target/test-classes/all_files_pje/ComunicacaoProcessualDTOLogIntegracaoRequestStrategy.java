package br.com.infox.pje.processor.strategy;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.json.JSONObject;

import br.com.infox.cliente.util.JSONUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.ComunicacaoProcessualDTO;
import br.jus.pje.nucleo.entidades.LogIntegracao;
import io.restassured.http.Method;

/**
 * Interface de persistência do LogIntegracao para requisições com payload ComunicacaoProcessualDTO.
 * 
 * @author Adriano Pamplona
 */
@Name("comunicacaoProcessualDTOLogIntegracaoRequestStrategy")
public class ComunicacaoProcessualDTOLogIntegracaoRequestStrategy implements LogIntegracaoRequestStrategy {

	private static final String RAISE_REMOVE_LOG_EVENT = "RAISE_REMOVE_LOG_EVENT";

	@Logger
	private Log log;
	
	/**
     * @return Instância da classe.
     */
    public static ComunicacaoProcessualDTOLogIntegracaoRequestStrategy instance() {
        return ComponentUtil.getComponent(ComunicacaoProcessualDTOLogIntegracaoRequestStrategy.class);
    }
    
	@Override
	public <T> T execute(LogIntegracao logIntegracao) {
		ComunicacaoProcessualDTO dto = JSONUtil.converterStringParaObjeto(logIntegracao.getRequestPayload(), ComunicacaoProcessualDTO.class);

		DomicilioEletronicoService domicilioEletronicoService = DomicilioEletronicoService.instance();

		boolean isHabilitada = domicilioEletronicoService.isPessoaHabilitada(dto.getDocumentoDestinatario());

		if (!isHabilitada) {
			domicilioEletronicoService.atualizaFlagEnviadoDomicilio(dto.getNumeroComunicacao(), Boolean.FALSE);
			Events.instance().raiseAsynchronousEvent(RAISE_REMOVE_LOG_EVENT, logIntegracao);
			return null;
		}

		StringLogIntegracaoRequestStrategy strategy = StringLogIntegracaoRequestStrategy.instance();

		JSONObject responseSuccess = strategy.execute(logIntegracao);

		if (responseSuccess != null) {
			domicilioEletronicoService.atualizaFlagEnviadoDomicilio(dto.getNumeroComunicacao(), Boolean.TRUE);
			return null;
		}

		if (logIntegracao.getRequestMethod() == Method.POST.toString()) {
			domicilioEletronicoService.atualizaFlagEnviadoDomicilio(dto.getNumeroComunicacao(), Boolean.FALSE);
		}

		return null;
	}
}
