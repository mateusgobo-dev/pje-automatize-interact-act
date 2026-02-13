package br.jus.cnj.pje.servicos;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.service.LogService;
import br.jus.cnj.pje.nucleo.manager.AMQPEventManager;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Job responsável pelo reenvio das mensagens ao serviço de mensageria.
 *
 * @author Adriano Pamplona
 */
@Name(AMQPService.NAME)
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class AMQPService {
	
	public static final String NAME = "amqpService";
	
	@In
	private LogService logService;

	@Logger
	private Log log;
	
	@In
	private AMQPEventManager amqpEventManager;
	
	private transient volatile Future<List<Long>> reenviandoMensagens;
	
	@Asynchronous
	public QuartzTriggerHandle execute(@IntervalCron String cron) {
		try {
			reenviarMensagensComErro();
		} catch (Exception exception) {
			logService.enviarLogPorEmail(log, exception, this.getClass(), "reenviarMensagensComErro");

		}

		return null;
	}

	@Transactional
	public void reenviarMensagensComErro() {
		if (reenviandoMensagens != null && !reenviandoMensagens.isDone()) {
			log.info("Ainda está reenviando as mensagens...");
			return;
		}
		reenviandoMensagens = null;

		log.info("Iniciar envio das mensagens.");
		Collection<Long> idEventos = amqpEventManager.consultarIdsMensagensPendentes();
		if (idEventos.size() > 0) {
			if (idEventos.size() > 10000) {
				log.info("Reenviar {0} mensagens. A operação pode demorar.", idEventos.size());
			}
			reenviandoMensagens = amqpEventManager.enviarSetMensagemAssincrona(idEventos);
		}
		log.info("Fim da solicitação de reenvio das {0} mensagens.", idEventos.size());
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
