package br.com.infox.pattern.strategy.ProcessoDistribuido;

import br.com.infox.pje.bean.EstatisticaProcessoDistribuidoAnaliticoAssuntoBean;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public class ProcessoDistribuidoAnaliticoAssuntoSituacao {
	ProcessoDistribuidoAnaliticoAssuntoStrategy strategy;
	public ProcessoDistribuidoAnaliticoAssuntoSituacao(ProcessoDistribuidoAnaliticoAssuntoStrategy strategy) {
		this.strategy = strategy;
	}
	
	public EstatisticaProcessoDistribuidoAnaliticoAssuntoBean eventoProcessoDistribuido(ProcessoTrf ptrf){
		return strategy.adicionaProcesso(ptrf);
	}
}
