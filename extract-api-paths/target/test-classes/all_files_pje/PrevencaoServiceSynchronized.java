package br.jus.cnj.pje.servicos;

import java.util.Collections;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.log.Log;
import org.jboss.seam.transaction.Transaction;

import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.enums.PrevencaoEnum;
import br.jus.pje.nucleo.enums.TipoConexaoEnum;

abstract class PrevencaoServiceSynchronized {

	public static synchronized void iniciarFluxoPrevencao(ProcessoTrfConexao processoTrfConexao, Log logger, ParametroService parametroService, FluxoManager fluxoManager) throws Exception {
		UserTransaction transaction = null;
		try {
			transaction = Transaction.instance();
			transaction.begin();
			Thread thread = Thread.currentThread();
			logger.info("Inicio thread: " + thread.getId());
			

			// Verifica se o objeto de conexao está instanciado, bem como os processos conexos
			if (processoTrfConexao == null || processoTrfConexao.getProcessoTrf() == null || processoTrfConexao.getProcessoTrfConexo() == null) {
				return;
			}

			// Verifica se a conexão é do tipo prevenção e se a análise está pendente
			if (!processoTrfConexao.getTipoConexao().equals(TipoConexaoEnum.PR) || !processoTrfConexao.getPrevencao().equals(PrevencaoEnum.PE)) {
				return;
			}

			String codigoFluxoPrevencao = parametroService.valueOf(Parametros.CODIGO_FLUXO_PREVENCAO);

			// Verifica se o código do fluxo de prevenção foi configurado
			if (codigoFluxoPrevencao == null || codigoFluxoPrevencao.trim().equals("")) {
				return;
			}

			// Instancia o fluxo de prevenção
			Fluxo fluxoPrevencao = fluxoManager.findByCodigo(codigoFluxoPrevencao);
			// Se o fluxo foi instanciado...
			if (fluxoPrevencao != null) {
				// Verifica se já existe fluxo de prevenção iniciado para o processo
				// boolean existeFluxoPrevencaoAtivo =
				// fluxoManager.existeFluxoComVariavel(processoTrfConexao.getProcessoTrf(),
				// Variaveis.VARIAVEL_FLUXO_PREVENCAO);
				Integer idProcesso = Integer.valueOf(processoTrfConexao.getProcessoTrf().getIdProcessoTrf());
				Integer idProcessoPrevento = Integer.valueOf(processoTrfConexao.getProcessoTrfConexo().getIdProcessoTrf());

				logger.info("Inserindo prevencao: " + idProcesso + ", " + idProcessoPrevento);

				boolean existeFluxoPrevencaoAtivo = fluxoManager.existeProcessoNoFluxo(idProcesso, fluxoPrevencao.getFluxo());
				logger.info("Existe no fluxo: " + idProcesso + " => " + existeFluxoPrevencaoAtivo);

				// Se não há fluxo de prevenção instanciado para o processo, inicia um novo fluxo
				if (!existeFluxoPrevencaoAtivo) {
					Map<String, Object> parametros = Collections.singletonMap(Variaveis.VARIAVEL_FLUXO_PREVENCAO, (Object) idProcesso);
					logger.info("Adicionando conexao para " + idProcesso);
					fluxoManager.iniciarFluxoProcesso(processoTrfConexao.getProcessoTrf().getProcesso(), fluxoPrevencao, parametros);
					logger.info("Processo Instance: " + idProcesso + ", " + BusinessProcess.instance().getProcessId());
				}
			}

			logger.info("Fim thread: " + thread.getId());
			transaction.commit();
		}
		catch(Exception e) {
			if(transaction != null) {
				transaction.rollback();
			}
			// Agenda novamente a chamada do evento???
			// Events.instance().raiseAsynchronousEvent(Eventos.CONEXAO_PROCESSUAL_CRIADA, processoTrfConexao);
			throw e;
		}
	}

}
