package br.jus.cnj.pje.entidades.listeners;

import org.apache.commons.lang3.ObjectUtils;
import org.jboss.seam.core.Events;

import br.jus.cnj.pje.nucleo.service.AutomacaoTagService;
import br.jus.pje.nucleo.entidades.ProcessoPrioridadeProcesso;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

public class ProcessoPrioridadeProcessoListener {

	public void postInsert(ProcessoPrioridadeProcesso prioridade){
		processarTags(prioridade);
	}
	
	public void postRemove(ProcessoPrioridadeProcesso prioridade){
		processarTags(prioridade);
	}
	
	private void processarTags(ProcessoPrioridadeProcesso prioridade) {
		if(ObjectUtils.notEqual(prioridade.getProcessoTrf(), null) && ProcessoStatusEnum.D.equals(prioridade.getProcessoTrf().getProcessoStatus())){
			Events.instance().raiseAsynchronousEvent(AutomacaoTagService.EVENTO_AUTOMACAO_TAG, prioridade.getProcessoTrf().getIdProcessoTrf());
		}
	}
}