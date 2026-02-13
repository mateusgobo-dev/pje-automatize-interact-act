package br.com.infox.bpm.taskPage.FGPJE;

import java.io.Serializable;

import org.jboss.seam.annotations.In;
import br.com.infox.bpm.action.TaskAction;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.pje.service.IntimacaoPartesService;
import br.jus.pje.nucleo.entidades.Caixa;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Tarefa;

public class DarCienciaPartesTaskAction extends TaskAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "darCienciaPartesTaskAction";

	private String avisoIntimacao;

	@In
	protected GenericManager genericManager;
	@In
	private IntimacaoPartesService intimacaoPartesService;

	/**
	 * Altera a tarefa do processo para Dar Ciencia as Partes e insere o
	 * processo na caixa de Intimações Automáticas Pendentes.
	 * 
	 * @param processo
	 *            a ser colocado na caixa.
	 */
	protected void endDarCienciaAsPartes(Processo processo) {
		ParametroUtil parametroUtil = ParametroUtil.instance();
		Tarefa tarefaCiencia = null;
		Caixa caixaIntimacao = null;
		if (parametroUtil.isPrimeiroGrau()) {
			tarefaCiencia = parametroUtil.getTarefaDarCienciaPartes();
			caixaIntimacao = parametroUtil.getCaixaIntimacaoAutoPend();
		}
		start();
		updateTransitions();
		end(tarefaCiencia.getTarefa());
		processo.setCaixa(caixaIntimacao);
		genericManager.update(processo);
	}

	protected void intimarPartesAutomaticamente(ProcessoDocumento processoDocumento) {
		Processo processo = ProcessoHome.instance().getInstance();
		ProcessoTrf processoTrf = genericManager.find(ProcessoTrf.class, processo.getIdProcesso());
		if (intimacaoPartesService.intimarPartesAutomaticamente(processoTrf, processoDocumento)) {
			start();
			updateTransitions();
			super.end(ParametroUtil.instance().getTarefaControlePrazo().getTarefa());
			setAvisoIntimacao("Expediente Via Sistema enviado com sucesso. Processo remetido para ''Controle de Prazo''.");
		} else {
			endDarCienciaAsPartes(processoTrf.getProcesso());
			setAvisoIntimacao("Processo remetido para ''Dar Ciências às Partes'', pois contem partes que não podem ser intimadas automaticamente.");
		}
	}

	public String getAvisoIntimacao() {
		return avisoIntimacao;
	}

	public void setAvisoIntimacao(String avisoIntimacao) {
		this.avisoIntimacao = avisoIntimacao;
	}

}
