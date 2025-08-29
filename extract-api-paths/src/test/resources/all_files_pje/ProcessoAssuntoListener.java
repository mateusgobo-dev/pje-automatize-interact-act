package br.jus.cnj.pje.entidades.listeners;

import org.jboss.seam.core.Events;

import br.jus.cnj.pje.nucleo.service.AutomacaoTagService;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

public class ProcessoAssuntoListener {

	public void postInsert(ProcessoAssunto processoAssunto){
		processarTags(processoAssunto);
	}
	
	public void postRemove(ProcessoAssunto processoAssunto){
		processarTags(processoAssunto);
	}
	
	private void processarTags(ProcessoAssunto processoAssunto) {
		if(ProcessoStatusEnum.D.equals(processoAssunto.getProcessoTrf().getProcessoStatus())){
			Events.instance().raiseAsynchronousEvent(AutomacaoTagService.EVENTO_AUTOMACAO_TAG, processoAssunto.getProcessoTrf().getIdProcessoTrf());
		}
	}
}