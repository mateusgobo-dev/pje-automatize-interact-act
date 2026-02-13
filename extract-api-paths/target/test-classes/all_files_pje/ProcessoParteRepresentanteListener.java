package br.jus.cnj.pje.entidades.listeners;

import org.jboss.seam.core.Events;

import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventVerbEnum;
import br.jus.cnj.pje.servicos.MensagemAMQPService;
import br.jus.cnj.pje.visao.beans.ObjectObserverBean;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public class ProcessoParteRepresentanteListener {

	public void postInsert(ProcessoParteRepresentante processoParteRepresentante) {
		Events.instance().raiseAsynchronousEvent(MensagemAMQPService.MESSAGE_TO_RABBIT, new ObjectObserverBean(CloudEventVerbEnum.POST, processoParteRepresentante));
		DomicilioEletronicoService.instance().alterarRepresentantesAsync(getProcessoTrf(processoParteRepresentante));
	}

	public void postUpdate(ProcessoParteRepresentante processoParteRepresentante) {
		Events.instance().raiseAsynchronousEvent(MensagemAMQPService.MESSAGE_TO_RABBIT, new ObjectObserverBean(CloudEventVerbEnum.PUT, processoParteRepresentante));
		DomicilioEletronicoService.instance().alterarRepresentantesAsync(getProcessoTrf(processoParteRepresentante));
	}

	public void postRemove(ProcessoParteRepresentante processoParteRepresentante) {
		DomicilioEletronicoService.instance().alterarRepresentantesAsync(getProcessoTrf(processoParteRepresentante));
	}
	
	private ProcessoTrf getProcessoTrf(ProcessoParteRepresentante representante) {
		ProcessoTrf resultado = null;
		if (representante != null && 
				representante.getProcessoParte() != null) {
			ProcessoParte processoParte = representante.getProcessoParte();
			resultado = processoParte.getProcessoTrf();
		}
		return resultado;
	}
}