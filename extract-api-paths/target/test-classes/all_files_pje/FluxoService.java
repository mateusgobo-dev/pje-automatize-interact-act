/**
 * 
 */
package br.jus.cnj.pje.nucleo.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.infox.cliente.util.ProjetoUtil;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.bpm.Actor;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.transaction.SeSynchronizations;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.def.TaskController;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.jbpm.actions.JbpmEventsHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.component.Util;

import br.com.itx.util.ComponentUtil;

import br.com.itx.exception.AplicationException;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TransicaoDTO;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.AtividadesLoteEnum;

@Name("fluxoService")
public class FluxoService {

	@In
	private FluxoManager fluxoManager;
	
	@In
	private UsuarioService usuarioService;
	@In
	private ProcessoManager processoManager;
	@In
	private ProcessoJudicialManager processoJudicialManager;

	private static final LogProvider log = Logging.getLogProvider(FluxoService.class);
	
	@In(create = true, required = false)
	private JbpmEventsHandler jbpmEventsHandler;

	
	@In
	private TramitacaoProcessualImpl tramitacaoProcessualService;
	
	public Fluxo findByCodigo(String codigo){
		return fluxoManager.findByCodigo(codigo);
	}

	public void iniciarHomesProcessos(ProcessoTrf processoTrf){
		ProcessoTrfHome.instance().setInstance(null);
		ProcessoTrfHome.instance().setId(processoTrf.getIdProcessoTrf());
		ProcessoHome.instance().setInstance(null);
		ProcessoHome.instance().setId(processoTrf.getProcesso().getIdProcesso());
	}

	public void iniciarBusinessProcess(Long idTaskInstance){
		TaskInstanceHome.instance().setTaskId(idTaskInstance, false);
		TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTaskInstance);
		if (taskInstance.getStart() == null){
			JbpmUtil.restaurarVariaveis(taskInstance);
			if (Contexts.isSessionContextActive()) {
				BusinessProcess.instance().startTask();
			} else {
				TaskInstance task = org.jboss.seam.bpm.TaskInstance.instance();
				try {
					task.start(usuarioService.getUsuarioSistema().getLogin());
				} catch (PJeBusinessException e) {
					task.start();
				}
				Events.instance().raiseEvent("org.jboss.seam.startTask." + task.getTask().getName());
			}
		}
	}
	
	public String obterModuloAngular(Long idTarefa) {
		return this.recuperarVariavel(idTarefa, "ngModule");
	}
	
    public Boolean podeMovimentarEmLote(Long idTarefa) {
        List<String> atividades = this.recuperarAtividadesLote(idTarefa);
        return atividades != null && atividades.contains(AtividadesLoteEnum.M.toString());
    }	
    
    public Boolean podeMinutarEmLote(Long idTarefa) {
    	List<String> atividades = this.recuperarAtividadesLote(idTarefa);
        return atividades != null && atividades.contains(AtividadesLoteEnum.E.toString());
	}
    
    public Boolean podeIntimarEmLote(Long idTarefa) {
    	List<String> atividades = this.recuperarAtividadesLote(idTarefa);
        return atividades != null && atividades.contains(AtividadesLoteEnum.I.toString());
	}
    
    public Boolean podeDesignarAudienciaEmLote(Long idTarefa) {
    	List<String> atividades = this.recuperarAtividadesLote(idTarefa);
        return atividades != null && atividades.contains(AtividadesLoteEnum.DA.toString());
	}
    
    public Boolean podeDesignarPericiaEmLote(Long idTarefa) {
    	List<String> atividades = this.recuperarAtividadesLote(idTarefa);
        return atividades != null && atividades.contains(AtividadesLoteEnum.DP.toString());
	}
    
    public Boolean podeRenajudEmLote(Long idTarefa) {
    	List<String> atividades = this.recuperarAtividadesLote(idTarefa);
        return atividades != null && atividades.contains(AtividadesLoteEnum.RE.toString());
	}
	
	private List<String> recuperarAtividadesLote(Long idTarefa) {
		List<String> ret = new ArrayList<String>();
		if(idTarefa == null){
			return null;
		}

		TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTarefa);

		if (taskInstance == null ) {
			return null;
		}
		TaskController taskController = taskInstance.getTask().getTaskController();

		if (taskController != null && taskController.getVariableAccesses() != null) {
			String[] tokens;
			for (VariableAccess var : taskController.getVariableAccesses()) {
				if (var.isReadable() && (var.getMappedName() != null)) {
					tokens = var.getMappedName().split(":");
					if ((tokens != null) && (tokens.length > 0)) {
						if (tokens[0].equals("movimentarLote")) {
							ret.add(AtividadesLoteEnum.M.toString());
						} else if (tokens[0].equals("minutarLote")) {
							ret.add(AtividadesLoteEnum.E.toString());
						} else if (tokens[0].equals("assinarLote")) {
							ret.add(AtividadesLoteEnum.A.toString());
						} else if (tokens[0].equals("intimarLote")) {
							ret.add(AtividadesLoteEnum.I.toString());							
						} else if (tokens[0].equals("assinarInteiroTeorLote")) {
							ret.add(AtividadesLoteEnum.T.toString());
						} else if (tokens[0].equals("lancadorMovimentoLote")) {
							ret.add(AtividadesLoteEnum.MM.toString());
						} else if (tokens[0].equals("designarAudienciaLote")) {
							ret.add(AtividadesLoteEnum.DA.toString());
						} else if (tokens[0].equals("designarPericiaLote")) {
							ret.add(AtividadesLoteEnum.DP.toString());
						} else if (tokens[0].equals("renajudLote")) {
							ret.add(AtividadesLoteEnum.RE.toString());
						}
					}
				}
				if(ret.size() == 4){
					break;
				}
			}
		}
		ret.add("0");

		return ret;
	}
	
	private String recuperarVariavel(Long idTarefa, String nome) {
		String resultado = "";
		TaskInstance taskInstance = null;
		
		if (idTarefa != null) {
			taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTarefa);

			if(taskInstance.getTask() != null) {
				TaskController taskController = taskInstance.getTask().getTaskController();
				if (taskController != null && taskController.getVariableAccesses() != null) {
					String[] tokens;
					for (VariableAccess var : taskController.getVariableAccesses()) {
						if (var.isReadable() && (var.getMappedName() != null)) {
							tokens = var.getMappedName().split(":");
							if ((tokens != null) && (tokens.length > 0) && tokens[0].equals(nome)) {
								resultado = tokens[1];
								break;
							}
						}
					}
				}
			}
		}
		
		return resultado;
	}
	
	public List<String> recuperarSaidasTarefa(Long idTarefa){
		Util.beginTransaction();
		
		TaskInstanceUtil taskInstanceUtil = (TaskInstanceUtil) Component.getInstance(TaskInstanceUtil.class);
		BusinessProcess.instance().setTaskId(idTarefa);
		TaskInstance ti = org.jboss.seam.bpm.TaskInstance.instance();
		ProcessInstance pi = ti.getProcessInstance();
		
		ProcessoTrfHome.instance().setInstance(null);
		ProcessoTrfHome.instance().setId(pi.getContextInstance().getVariable("processo"));
		
		List<String> transicoes = taskInstanceUtil.getTransitions(idTarefa);
		
		Util.commitTransction();
		
		return transicoes;
	}

	public void iniciarProcesso(Processo processo, Long idTaskInstance) {
		try {
			
				TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTaskInstance);

				if (Objects.nonNull(taskInstance)) {
					ProcessoTrf processoTrf = processoJudicialManager.findById(processo.getIdProcesso());
					iniciarHomesProcessos(processoTrf);
					BusinessProcess.instance().setProcessId(taskInstance.getProcessInstance().getId());
					BusinessProcess.instance().setTaskId(idTaskInstance);
					JbpmUtil.restaurarVariaveis(taskInstance);
					if (Objects.isNull(taskInstance.getStart())) {
						BusinessProcess.instance().startTask();
					}
					jbpmEventsHandler.reexecutarActions(taskInstance);
					String actorId = Actor.instance().getId();
					processo.setActorId(actorId);
					processo.setIdJbpm(BusinessProcess.instance().getProcessId());
					taskInstance.setActorId(actorId);
					processoManager.merge(processo);
					

				}
			
		} catch (Exception e) {
			String msg = "iniciar a tarefa: ";
			log.error(msg, e);
			throw new AplicationException(AplicationException.createMessage(msg + e.getLocalizedMessage(),
					"iniciarProcesso()", "FluxoService", "BPM"));
		}

	}


	public boolean finalizarTarefa(Long idTarefa, Boolean isAssinatura, String transition) throws PJeBusinessException {
		return this.finalizarTarefaAssociandoDocumentoAoMovimento(idTarefa, isAssinatura, transition, null);
	}

	/**
	 * Finaliza a tarefa, se indicado um processoDocumento, sobrescreve a informação do movimento com a informação do {@link ProcessoDocumento},
	 *
	 * @param idTarefa
	 * @param isAssinatura
	 * @param transition
	 * @param processoDocumento
	 * @return
	 * @throws PJeBusinessException
	 */
	public boolean finalizarTarefaAssociandoDocumentoAoMovimentoSemTransacao(Long idTarefa, Boolean isAssinatura,
			String transition, ProcessoDocumento processoDocumento) throws PJeBusinessException {
		if (idTarefa == null) {
			throw new PJeBusinessException("O id da taskInstance não pode ser nulo");
		}

		ProcessInstance pi = null;

		BusinessProcess.instance().resumeTask(idTarefa);

		TaskInstance ti = org.jboss.seam.bpm.TaskInstance.instance();

		TramitacaoProcessualService tramitacaoProcessualService = ComponentUtil
				.getComponent(TramitacaoProcessualImpl.class);

		if (ti != null) {
			pi = ti.getProcessInstance();

			if (pi.hasEnded()) {
				return false;
			}

			if (ti.hasEnded()) {
				return false;
			}

			ProcessoTrfHome.instance().setInstance(null);
			ProcessoTrfHome.instance().setId(pi.getContextInstance().getVariable("processo"));

			ti.setActorId(Authenticator.getIdUsuarioLogado().toString());

			if (transition == null || (transition != null && transition.isEmpty())) {
				transition = (String) ti.getVariableLocally(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
			}

			try {
				LancadorMovimentosService lancadorMovimentoService = LancadorMovimentosService.instance();
				boolean lancarMovimentoTemporariamente = lancadorMovimentoService.deveGravarTemporariamente();

				if (BooleanUtils.isTrue(isAssinatura) || (!lancarMovimentoTemporariamente
						&& !tramitacaoProcessualService.isTransicaoDispensaRequeridos(transition))) {
					if (processoDocumento != null) {
						lancadorMovimentoService.lancarMovimentosTemporariosAssociandoAoDocumento(pi,
								processoDocumento);
					} else {
						lancadorMovimentoService.lancarMovimentosTemporarios(pi);
					}
				}
			} catch (Exception e) {
				JbpmUtil.clearAndClose(ManagedJbpmContext.instance());

				log.error(MessageFormat.format(
						"Erro ao finalizar a tarefa e associar ao movimento o documento de id {0}:{1}",
						(processoDocumento != null ? processoDocumento.getIdProcessoDocumento() : null),
						e.getLocalizedMessage()));

				throw e;
			}

			try {
				if (BooleanUtils.isTrue(isAssinatura)) {
					ProcessoDocumentoManager processoDocumentoManager = (ProcessoDocumentoManager) Component
							.getInstance(ProcessoDocumentoManager.class);

					Integer identificadorMinutaEmElaboracao = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(ti);

					if (identificadorMinutaEmElaboracao != null) {
						ProcessoDocumento pd = processoDocumentoManager.findById(identificadorMinutaEmElaboracao);

						MiniPacService miniPacService = ComponentUtil.getComponent(MiniPacService.class);

						miniPacService.processarMiniPac(pd.getProcessoTrf(), pd, false);

						if (pd.getTipoProcessoDocumento().getDocumentoAtoProferido()) {
							pi.getContextInstance().setVariable(Variaveis.ATO_PROFERIDO,
									identificadorMinutaEmElaboracao);
						}

						pi.getContextInstance().setVariable(Variaveis.ULTIMO_DOCUMENTO_JUNTADO_NESTE_FLUXO,
								identificadorMinutaEmElaboracao);

						pi.getContextInstance().deleteVariable(Variaveis.MINUTA_EM_ELABORACAO);

						pi.getContextInstance().deleteVariable(Variaveis.VARIAVEL_FLUXO_COLEGIADO_MINUTA_ACORDAO);
					}
				}

				if (transition != null) {
					ti.end(transition);
				} else if (BooleanUtils.isTrue(isAssinatura)) {
					ti.end();
				}

				TaskInstance tiNovo = org.jboss.seam.bpm.TaskInstance.instance();

				if (tiNovo.getActorId() != null && !tiNovo.getActorId().isEmpty()) {
					tiNovo.setActorId(null);
				}

				return true;
			} catch (Exception e) {
				JbpmUtil.clearAndClose(ManagedJbpmContext.instance());

				log.error(MessageFormat.format(
						"Erro ao  ao finalizar a tarefa e associar ao movimento o documento de id {0}:{1}",
						(processoDocumento != null ? processoDocumento.getIdProcessoDocumento() : null),
						e.getLocalizedMessage()));

				throw e;
			}
		} else {
			throw new PJeBusinessException("A taskInstance não pode ser nula");
		}
	}

	/**
	 * Finaliza a tarefa, se indicado um processoDocumento, sobrescreve a informação do movimento com a informação do {@link ProcessoDocumento},
	 *
	 * @param idTarefa
	 * @param isAssinatura
	 * @param transition
	 * @param processoDocumento
	 * @return
	 * @throws PJeBusinessException 
	 */
	public boolean finalizarTarefaAssociandoDocumentoAoMovimento(Long idTarefa, Boolean isAssinatura, String transition,
			ProcessoDocumento processoDocumento) throws PJeBusinessException {
		if (idTarefa == null) {
			throw new PJeBusinessException("O id da taskInstance não pode ser nulo");
		}

		Util.beginTransaction();

		SeSynchronizations seSynchronizations = (SeSynchronizations) Component
				.getInstance("org.jboss.seam.transaction.synchronizations");

		seSynchronizations.afterTransactionBegin();

		ProcessInstance pi = null;

		BusinessProcess.instance().resumeTask(idTarefa);

		TaskInstance ti = org.jboss.seam.bpm.TaskInstance.instance();

		TramitacaoProcessualService tramitacaoProcessualService = ComponentUtil
				.getComponent(TramitacaoProcessualImpl.class);

		if (ti != null) {
			pi = ti.getProcessInstance();

			if (pi.hasEnded()) {
				Util.commitTransction();

				return false;
			}

			if (ti.hasEnded()) {
				Util.commitTransction();

				return false;
			}

			ProcessoTrfHome.instance().setInstance(null);
			ProcessoTrfHome.instance().setId(pi.getContextInstance().getVariable("processo"));

			ti.setActorId(Authenticator.getIdUsuarioLogado().toString());

			if (transition == null || (transition != null && transition.isEmpty())) {
				transition = (String) ti.getVariableLocally(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
			}

			if (!this.variaveisObrigatoriasEstaoPreenchidas(ti, transition)){
				throw new PJeBusinessException("É preciso informar valor em todos os campos obrigatórios.");
			}

			try {
				LancadorMovimentosService lancadorMovimentoService = LancadorMovimentosService.instance();
				boolean lancarMovimentoTemporariamente = lancadorMovimentoService.deveGravarTemporariamente();

				if (BooleanUtils.isTrue(isAssinatura) || (!lancarMovimentoTemporariamente && !tramitacaoProcessualService.isTransicaoDispensaRequeridos(transition))) {
					if(processoDocumento != null) {
						lancadorMovimentoService.lancarMovimentosTemporariosAssociandoAoDocumento(pi, processoDocumento);
					} else {
						lancadorMovimentoService.lancarMovimentosTemporarios(pi);
					}
				}
			} catch (Exception e) {
				JbpmUtil.clearAndClose(ManagedJbpmContext.instance());

				Util.rollbackTransaction();

				log.error(MessageFormat.format(
						"Erro ao finalizar a tarefa e associar ao movimento o documento de id {0}:{1}",
						(processoDocumento != null ? processoDocumento.getIdProcessoDocumento() : null),
						e.getLocalizedMessage()));

				throw e;
			}

			try {
				if (BooleanUtils.isTrue(isAssinatura)) {
					ProcessoDocumentoManager processoDocumentoManager = (ProcessoDocumentoManager) Component
							.getInstance(ProcessoDocumentoManager.class);
					
					Integer identificadorMinutaEmElaboracao = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(ti);

					if(identificadorMinutaEmElaboracao != null) {
						ProcessoDocumento pd = processoDocumentoManager.findById(identificadorMinutaEmElaboracao);

						MiniPacService miniPacService = ComponentUtil.getComponent(MiniPacService.class);

					    miniPacService.processarMiniPac(pd.getProcessoTrf(), pd, false);
						
						if(pd.getTipoProcessoDocumento().getDocumentoAtoProferido()) {
							pi.getContextInstance().setVariable(Variaveis.ATO_PROFERIDO, identificadorMinutaEmElaboracao);
						}

						pi.getContextInstance().setVariable(Variaveis.ULTIMO_DOCUMENTO_JUNTADO_NESTE_FLUXO, identificadorMinutaEmElaboracao);
						
						pi.getContextInstance().deleteVariable(Variaveis.MINUTA_EM_ELABORACAO);
						
						pi.getContextInstance().deleteVariable(Variaveis.VARIAVEL_FLUXO_COLEGIADO_MINUTA_ACORDAO);
					}
				}

				if (transition != null) {
					ti.end(transition);
				} else if (BooleanUtils.isTrue(isAssinatura)) {
					ti.end();
				}

				TaskInstance tiNovo = org.jboss.seam.bpm.TaskInstance.instance();

				if (tiNovo.getActorId() != null && !tiNovo.getActorId().isEmpty()) {
					tiNovo.setActorId(null);
				}

				seSynchronizations.beforeTransactionCommit();

				Util.commitTransction();

				seSynchronizations.afterTransactionCommit(true);

				return true;
			} catch (Exception e) {
				JbpmUtil.clearAndClose(ManagedJbpmContext.instance());

				Util.rollbackTransaction();

				log.error(MessageFormat.format(
						"Erro ao  ao finalizar a tarefa e associar ao movimento o documento de id {0}:{1}",
						(processoDocumento != null ? processoDocumento.getIdProcessoDocumento() : null),
						e.getLocalizedMessage()));

				throw e;
			}
		} else {
			Util.commitTransction();

			throw new PJeBusinessException("A taskInstance não pode ser nula");
		}
	}

	public TransicaoDTO finalizarTarefaIndividual(Long idTarefa, String transition) {
		TransicaoDTO newTaskId = new TransicaoDTO();
		TaskInstanceHome tih = TaskInstanceHome.instance();
		BusinessProcess.instance().resumeTask(idTarefa);
		TaskInstance ti = org.jboss.seam.bpm.TaskInstance.instance();
		if (ti != null) {
			ProcessInstance pi = ti.getProcessInstance();
			ProcessoHome.instance().getInstance().setIdProcesso((int) pi.getContextInstance().getVariable("processo"));
			ComponentUtil.getTramitacaoProcessualService().gravaVariavelTarefa("pje:fluxo:transicao:dispensaRequeridos", transition);
			tih.setTaskId(idTarefa);
			tih.end(transition);
			newTaskId.setNewTaskId(tih.getTaskId()); 
		}
		return newTaskId;
	}

	/** Verifica se as variáveis de tarefas marcadas como obrigatórias foram preenchidas considerando se a transição atual dispensa validações
	 * @param taskInstance Tarefa atual
	 * @param transition Transição de saída
	 * @return true se todas variáveis estão devidamente prenchida ou false se uma ou mais variáveis obrigatórias não estiverem preenchidas
	 * */
	private boolean variaveisObrigatoriasEstaoPreenchidas(TaskInstance taskInstance, String transition) {
		if (transition != null && Boolean.TRUE.equals(tramitacaoProcessualService.isTransicaoDispensaRequeridos(transition))) {
			return true;
		}
		TaskController taskController = taskInstance.getTask().getTaskController();
		List<VariableAccess> variableAccesses = taskController.getVariableAccesses();
		if (ProjetoUtil.isNotVazio(variableAccesses)) {
			for (VariableAccess variavel : variableAccesses) {
				Object valueVariavel = taskInstance.getVariableLocally(variavel.getMappedName());
				if (variavel.isRequired() && (Objects.isNull(valueVariavel) || StringUtils.isBlank(String.valueOf(valueVariavel)))) {
					return false;
				}
			}
		}
		return true;
	}
	
	public void finalizaFluxoManualmente(Long idTarefa ) {
		this.fluxoManager.finalizaFluxoManualmente(idTarefa);
	}
}
