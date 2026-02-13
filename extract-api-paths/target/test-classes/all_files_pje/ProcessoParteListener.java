package br.jus.cnj.pje.entidades.listeners;

import org.jboss.seam.core.Events;

import br.jus.cnj.pje.nucleo.service.AutomacaoTagService;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

public class ProcessoParteListener {

	public void postInsert(ProcessoParte processoParte){
		processarTags(processoParte);
	}
	
	public void postUpdate(ProcessoParte processoParte){
		processarTags(processoParte);
	}
	
	private void processarTags(ProcessoParte processoParte) {
		if(ProcessoStatusEnum.D.equals(processoParte.getProcessoTrf().getProcessoStatus())){
			Events.instance().raiseAsynchronousEvent(AutomacaoTagService.EVENTO_AUTOMACAO_TAG, processoParte.getProcessoTrf().getIdProcessoTrf());
		}
	}
}