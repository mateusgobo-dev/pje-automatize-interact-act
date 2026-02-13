package br.com.infox.pje.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.service.LogService;
import br.com.infox.pje.manager.LogIntegracaoManager;
import br.com.infox.pje.processor.strategy.ComunicacaoProcessualDTOLogIntegracaoRequestStrategy;
import br.com.infox.pje.processor.strategy.LogIntegracaoRequestStrategy;
import br.com.infox.pje.processor.strategy.StringLogIntegracaoRequestStrategy;
import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.AtualizacaoDataCienciaDTO;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.ComunicacaoProcessualDTO;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.RegistroCienciaDTO;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.RepresentantesDTO;
import br.jus.pje.nucleo.entidades.LogIntegracao;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Job responsável pelo reenvio das requisições ao Domicílio Eletrônico que deram erro.
 * 
 * @author Adriano Pamplona
 */
@Name(DomicilioEletronicoProcessor.NAME)
@AutoCreate
@SuppressWarnings("rawtypes")
public class DomicilioEletronicoProcessor {

	public static final String NAME = "domicilioEletronicoProcessor";

	@In
	private LogService logService;
	
	@Logger
	private Log log;
	
	private Map<Class, LogIntegracaoRequestStrategy> mapStrategy = new HashMap<>();

	/**
	 * @return Instância da classe.
	 */
	public static DomicilioEletronicoProcessor instance() {
		return (DomicilioEletronicoProcessor) Component.getInstance(NAME);
	}

	/**
	 * Método invocado pela trigger sempre que o intervalo da cron estipulado
	 * for concluído, ele também irá processar os eventos que devem ser
	 * incluídos nas estatísticas.
	 * 
	 * @param cron
	 * @return QuartzTriggerHandle
	 */
	@Asynchronous
	@Transactional
	public QuartzTriggerHandle execute(@IntervalCron String cron) {
		try {
			reenviarRequisicoes();
		} catch (Exception exception) {
			logService.enviarLogPorEmail(log, exception, this.getClass(), NAME);
		}
		return null;
	}
	
	
	/**
	 * Reenvia as requisições com erro 5xx e 4xx .
	 */
	public void reenviarRequisicoes() {
		if (DomicilioEletronicoService.instance().isOnline()) {
			LogIntegracaoManager manager = LogIntegracaoManager.instance();
			List<LogIntegracao> logs = manager.consultarLogDomicilioEletronico();
			for (LogIntegracao logIntegracao : logs) {
				try {
					Util.beginAndJoinTransaction();
					LogIntegracaoRequestStrategy strategy = getDomicilioEletronicoStrategy(logIntegracao);
					strategy.execute(logIntegracao);
					EntityUtil.flush();
					Util.commitAndOpenJoinTransaction();
				}catch (Exception e) {
					e.printStackTrace();
					log.error(e);
					Util.rollbackAndOpenJoinTransaction();
				}
			}
		}
	}
	
	/**
	 * @param log LogIntegracao
	 * @return DomicilioEletronicoStrategy
	 */
	protected LogIntegracaoRequestStrategy getDomicilioEletronicoStrategy(LogIntegracao log) {
		LogIntegracaoRequestStrategy strategy = getMapStrategy().get(getClass(log));
		if (strategy == null) {
			strategy = StringLogIntegracaoRequestStrategy.instance();
		}
		return strategy;
	}
	
	/**
	 * @return Map de class/DomicilioEletronicoStrategy.
	 */
	protected Map<Class, LogIntegracaoRequestStrategy> getMapStrategy() {
		if (mapStrategy == null || mapStrategy.isEmpty()) {
			mapStrategy = new HashMap<>();
			mapStrategy.put(null, StringLogIntegracaoRequestStrategy.instance());
			mapStrategy.put(String.class, new StringLogIntegracaoRequestStrategy());
			mapStrategy.put(ComunicacaoProcessualDTO.class, ComunicacaoProcessualDTOLogIntegracaoRequestStrategy.instance());
			mapStrategy.put(AtualizacaoDataCienciaDTO.class, StringLogIntegracaoRequestStrategy.instance());
			mapStrategy.put(RegistroCienciaDTO.class, StringLogIntegracaoRequestStrategy.instance());
			mapStrategy.put(RepresentantesDTO.class, StringLogIntegracaoRequestStrategy.instance());
		}
		return mapStrategy;
	}
	
	/**
	 * @param log LogIntegracao
	 * @return Class do requestPayloadClass.
	 */
	protected Class getClass(LogIntegracao log) {
		Class result = null;
		String requestPayloadClass = log.getRequestPayloadClass();

		if(StringUtil.isEmpty(requestPayloadClass)) {
			return null;
		}
		
		try {
			result = ClassUtils.getClass(requestPayloadClass);
		} catch (ClassNotFoundException e) {
			this.log.error("A classe '{0}' definida na tabela 'tb_integracao_log' não existe.", requestPayloadClass);
		}
		
		return result;
	}
	
	/**
	 * Classe estática com as constantes dos atributos/métodos da classe.
	 *
	 */
	public static final class ATTR {
		
		/**
		 * Contrutor
		 * 
		 */
		private ATTR() {
			// Construtor.
		}
		
		public static final String EXECUTE = "execute";
	}
}