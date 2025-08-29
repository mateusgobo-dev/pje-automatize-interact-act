package br.com.infox.pattern.strategy.ProcessoDistribuido;

import br.com.infox.pje.bean.EstatisticaProcessoDistribuidoAnaliticoAssuntoBean;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public interface ProcessoDistribuidoAnaliticoAssuntoStrategy {
	public EstatisticaProcessoDistribuidoAnaliticoAssuntoBean adicionaProcesso(ProcessoTrf ptrf);
}
