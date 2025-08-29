/*
 * IBPM - Ferramenta de produtividade Java Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda. Este programa é software livre; você pode
 * redistribuí-lo e/ou modificá-lo sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela Free Software Foundation; versão 2 da
 * Licença. Este programa é distribuído na expectativa de que seja útil, porém, SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 * COMERCIABILIDADE OU ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA. Consulte a GNU GPL para mais detalhes. Você deve ter recebido uma cópia da GNU GPL junto
 * com este programa; se não, veja em http://www.gnu.org/licenses/
 */
package br.com.infox.ibpm.jbpm;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.faces.model.SelectItem;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.resteasy.spi.ApplicationException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.bpm.ProcessInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.transaction.Transaction;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.command.UnlockTokenCommand;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.graph.def.DelegationException;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.jpdl.el.impl.JbpmExpressionEvaluator;
import org.jbpm.taskmgmt.def.TaskController;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.editor.action.EditorAction;
import br.com.infox.ibpm.component.tree.AutomaticEventsTreeHandler;
import br.com.infox.ibpm.component.tree.EventsEditorTreeHandler;
import br.com.infox.ibpm.component.tree.EventsHomologarMovimentosTreeHandler;
import br.com.infox.ibpm.component.tree.EventsTreeHandler;
import br.com.infox.ibpm.component.tree.TarefasTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.actions.ModeloDocumentoAction;
import br.com.infox.ibpm.service.AssinaturaDocumentoService;
import br.com.infox.pje.dao.SituacaoProcessoDAO;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.Util;
import br.com.itx.exception.AplicationException;
import br.com.itx.exception.MovimentarFluxoException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.com.jt.pje.action.AssistenteAdmissibilidadeRecursoAction;
import br.com.jt.pje.action.VotoAction;
import br.jus.cnj.fluxo.TaskVariavel;
import br.jus.cnj.fluxo.Validador;
import br.jus.cnj.fluxo.interfaces.TaskVariavelAction;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.servicos.NoDeDesvioService;
import br.jus.cnj.pje.util.TransitionComparator;
import br.jus.cnj.pje.view.ErrorMovimentarFluxoAction;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.util.StringUtil;

@Name(TaskInstanceHome.NAME)
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.FRAMEWORK)
public class TaskInstanceHome implements Serializable{

	private static final String MSG_USUARIO_SEM_ACESSO = "Você não pode mais efetuar transações "
		+ "neste registro, verifique se ele não foi movimentado";

	private static final LogProvider log = Logging.getLogProvider(TaskInstanceHome.class);

	private static final long serialVersionUID = 1L;

	public static final String NAME = "taskInstanceHome";

	private static final Boolean OCCULT_TRANSITION = true; 
	
	private String transicaoSaida;

	private TaskInstance taskInstance;

	private Map<String, Object> instance;

	private String variavelDocumento;

	private Long taskId;

	private List<Transition> availableTransitions;

	private List<Transition> leavingTransitions;

	private ModeloDocumento modeloDocumento;

	private String varName;

	private String name;
	
	private Boolean assinar = Boolean.FALSE;

	private TaskInstance currentTaskInstance;
	
	private boolean immediateTaskButton = false;

	public static final String UPDATED_VAR_NAME = "isTaskHomeUpdated";

	@In(create=true)
	private TramitacaoProcessualService tramitacaoProcessualService; 
	
	@In(create=true)
	private VotoAction votoAction; 
	
	@In(create=true,required = false)
	private EditorAction editorAction;

	private Boolean iframe = Boolean.FALSE;


	private Boolean salvarDocumentoPorFluxo = Boolean.TRUE;

	public void createInstance(){
		taskInstance = org.jboss.seam.bpm.TaskInstance.instance();
		if (instance == null && taskInstance != null){
			
			desbloqueiaTokenLock();
			
			instance = new HashMap<String, Object>();
			TaskController taskController = taskInstance.getTask().getTaskController();
			if (taskController != null){
				List<VariableAccess> list = taskController.getVariableAccesses();
				
				/* Busca a variavel minutaEmElaboracao - se houver essa variável, recupera ela no lugar do conteúdo do editor */						
				Integer idMinutaEmElaboracao = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(taskInstance);
				Object conteudoMinutaEmElaboracao = null;
				if(idMinutaEmElaboracao!=null){
					conteudoMinutaEmElaboracao = JbpmUtil.instance().getConteudoMinutaEmElaboracao(idMinutaEmElaboracao);
				}

				if(conteudoMinutaEmElaboracao != null){
					ProcessoHome.instance().carregarDadosFluxo(idMinutaEmElaboracao);
					instance.put(Variaveis.MINUTA_EM_ELABORACAO, conteudoMinutaEmElaboracao);
				}
				
				for (VariableAccess var : list){
					String type = var.getMappedName().split(":")[0];
					String name = var.getMappedName().split(":")[1];
					Boolean isEditor = JbpmUtil.isTypeEditor(type, name);
					Object variable = JbpmUtil.instance().getConteudo(var, taskInstance);
					String modelo = (String) ProcessInstance.instance().getContextInstance()
							.getVariable(name + "Modelo");
					if ((isEditor || JbpmUtil.isTypeEditorEstruturado(type))){
						if(conteudoMinutaEmElaboracao != null){
							/* se houver variavel do editor de textos e houver minutaEmElaboracao - o sistema substitui o valor da variavel do editor pelo ID da minutaEmElaboracao */
							taskInstance.setVariableLocally(var.getMappedName(), idMinutaEmElaboracao);
						}else{
							/*
							 * [PJEII-5142] Invoca metodo para setar automaticamente
							 * valores nas combos tipo de documento e magistrado do
							 * editor estruturado caso as mesmas possuam apenas uma
							 * opcao de escolha.
							 */
							
							if (JbpmUtil.isTypeEditorEstruturado(type)) {
								this.editorAction.autoInitOpcaoCombo(name);
							}
							Integer id = null;
							Object value = taskInstance.getVariable(var.getMappedName());
							if (value instanceof Integer){
								if (isDocumentoAssinado((Integer) value)) {
									taskInstance.deleteVariable(var.getMappedName());
								} else {
									id = (Integer) value;
								}
							}
							if (id != null){
								ProcessoHome.instance().carregarDadosFluxo(id);
								instance.put(name, variable);
							} else {
								ProcessoHome.instance().setShowComponentesFluxo(true);
							}
						}
					}
					if (modelo != null){
						variavelDocumento = name;
						if (variable == null && !type.equals("frame")) {
							String s = modelo.split(",")[0].trim();
							modeloDocumento = EntityUtil.getEntityManager().find(ModeloDocumento.class,
									Integer.parseInt(s));
							variable = ModeloDocumentoAction.instance().getConteudo(modeloDocumento);
						}
					}
					if (!isEditor){
						instance.put(name + "-" + taskId, variable);
					}

					if ("form".equals(type)){
						varName = name;
						if (null != variable){
							AbstractHome<?> home = ComponentUtil.getComponent(name + "Home");
							home.setId(variable);
						}
					}
				}
				// Atualizar as transições possiveis. Isso é preciso, pois as
				// condições das transições são avaliadas antes
				// deste metodo ser executado.
				updateTransitions();
			}
		}
	}

	public Map<String, Object> getInstance(){
		createInstance();
		return instance;
	}

	// Método que será chamado pelo botão "Assinar Digitalmente"
	public void assinarDocumento(){
		assinarDocumento(false);
	}
	
	public String assinarDocumento(boolean transicaoAutomatica){
		assinar = Boolean.TRUE;
		String retorno = "movimentar";
		if(iframe != null && iframe) {
			retorno = "fecharIframe";
		}
		try {
			if(transicaoAutomatica) {
				retorno = transitar();
			}
			else {
				update();
			}
			AutomaticEventsTreeHandler.instance().clearList();
			AutomaticEventsTreeHandler.instance().clearTree();
			EventsEditorTreeHandler.instance().clearList();
			EventsEditorTreeHandler.instance().clearTree();
			ProcessoHome.instance().setTipoProcessoDocumento(null);
		}
		catch(Exception e) {
			FacesMessages.instance().clear();
			throw new AplicationException("Não há transição disponível para prosseguimento da atividade: " + e.getMessage());
		}
		return retorno;
	}
	
	/**
	 * Verifica se é necessário criar uma variável de ato proferido com o ID do documento juntado
	 * Apaga a variável de minutaEmElaboração
	 * Apaga as variáveis de minuta criadas para os editores de texto
	 * 
	 * @param taskInstance
	 * @param processoDocumento
	 */
	public void trataVariaveisDocumentosPosJuntada(org.jbpm.taskmgmt.exe.TaskInstance taskInstance, ProcessoDocumento processoDocumento) {
		ProcessoHome.instance().setIdProcessoDocumento(processoDocumento.getIdProcessoDocumento());
		
		org.jbpm.graph.exe.ProcessInstance pi = taskInstance.getProcessInstance();
		
		Integer identificadorMinutaEmElaboracao = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(taskInstance);
		if(identificadorMinutaEmElaboracao != null) {
			if(processoDocumento.getTipoProcessoDocumento().getDocumentoAtoProferido()) {
				pi.getContextInstance().setVariable(Variaveis.ATO_PROFERIDO, identificadorMinutaEmElaboracao);
			}
			pi.getContextInstance().setVariable(Variaveis.ULTIMO_DOCUMENTO_JUNTADO_NESTE_FLUXO, identificadorMinutaEmElaboracao);
			
			pi.getContextInstance().deleteVariable(Variaveis.MINUTA_EM_ELABORACAO);
		}
		this.apagaVariavelDocumentoEditor(taskInstance);
	}
	
	private void apagaVariavelDocumentoEditor(org.jbpm.taskmgmt.exe.TaskInstance taskInstance) {
		TaskController taskController = taskInstance.getTask().getTaskController();
		if (taskController != null){
			List<VariableAccess> list = taskController.getVariableAccesses();
			for (VariableAccess var : list){
				String type = var.getMappedName().split(":")[0];
				String name = var.getMappedName().split(":")[1];
				Boolean isEditor = JbpmUtil.isTypeEditor(type, name);

				if ((isEditor || JbpmUtil.isTypeEditorEstruturado(type))){
					taskInstance.deleteVariable(name);
				}
			}
		}		
	}

	private String transitar() {
		String saida = (String) tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		if(saida != null && !saida.isEmpty()){
			this.end(saida);
		}else{
			List<Transition> transitions = taskInstance.getAvailableTransitions();
			for (Transition transition : transitions) {
				if (!transition.getName().equalsIgnoreCase(NoDeDesvioService.getNomeNoDesvio(transition.getFrom().getProcessDefinition()))) {
					return end(transition.getName());
				}
			}
		}
		return "";
	}

	public Object getValueFromInstanceMap(String key){
		if (instance == null){
			return null;
		}
		Set<Entry<String, Object>> entrySet = instance.entrySet();
		for (Entry<String, Object> entry : entrySet){
			if (entry.getKey().split("-")[0].equals(key) && entry.getValue() != null){
				return entry.getValue();
			}
		}
		return null;
	}

	
	public void update(){
		modeloDocumento = null;
		taskInstance = org.jboss.seam.bpm.TaskInstance.instance();

		if ((taskInstance != null) && (taskInstance.getTask() != null)){
			TaskController taskController = taskInstance.getTask().getTaskController();
			if (taskController != null){
				List<VariableAccess> list = taskController.getVariableAccesses();
				for (VariableAccess var : list){

					String type = var.getMappedName().split(":")[0];
					String name = var.getMappedName().split(":")[1];
					Object value = getValueFromInstanceMap(name);

					if (var.isWritable()){
						if (salvarDocumentoPorFluxo &&
								(JbpmUtil.isTypeEditor(type, name) || JbpmUtil.isTypeEditorEstruturado(type) || type.equals("assistenteAdmissibilidadeRecursoJTEstruturado"))){
							Integer idDoc = null;
							Integer idMinutaElaboracao = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(taskInstance);							
							if(idMinutaElaboracao != null && isDocumentoAssinado(idMinutaElaboracao)) {
								taskInstance.getProcessInstance().getContextInstance().deleteVariable(Variaveis.MINUTA_EM_ELABORACAO);
								continue;
							}else {
								idDoc = idMinutaElaboracao;
							}
							
							// se houver a minuta em elaboracao, o sistema dará preferência para ela ao invés da variável do editor
							if(idDoc == null) {
								Object variable = taskInstance.getVariable(var.getMappedName());
								if (variable != null){
									if (variable instanceof Integer){
										idDoc = (Integer) variable;
										
										if(idDoc != null && isDocumentoAssinado(idDoc)) {
											
											Contexts.getBusinessProcessContext().flush();
											Util.setToEventContext(UPDATED_VAR_NAME, true);
											updateTransitions();

											return ;
										}
									}
								}
							}

							ProcessoHome.instance().setShowComponentesFluxo(true);
							
							Integer valueInt = null;
							AssistenteAdmissibilidadeRecursoAction recursoAction = AssistenteAdmissibilidadeRecursoAction.instance();
							if(type.equals("assistenteAdmissibilidadeRecursoJTEstruturado")){
								if(recursoAction.getExibirAssistente()){
									try{
										if(EditorAction.instance().getDocumento() == null){
											recursoAction.gravarDespacho();
										}else{
											recursoAction.gravarAssistente();
										}
									}catch(ApplicationException e){
										FacesMessages.instance().clear();
										FacesMessages.instance().add(Severity.ERROR, e.getMessage());
										return;
									}
								}else{
									valueInt = salvarEditor();
								}
							}
							
							if(JbpmUtil.isTypeEditorEstruturado(type)){
								valueInt = salvarEditor();
							}else{
								valueInt = ProcessoHome.instance().salvarProcessoDocumentoFluxo(value == null ? null : value.toString() , 
									idDoc, assinar, ProcessoHome.instance().getTipoProcessoDocumento() != null ? 
										ProcessoHome.instance().getTipoProcessoDocumento().getTipoProcessoDocumento() : null, getTaskId());
							}
							if (valueInt != 0){
								Events.instance().raiseEvent(ProcessoHome.AFTER_UPDATE_PD_FLUXO_EVENT, valueInt);
								Contexts.getBusinessProcessContext().set(var.getMappedName(), valueInt);
								taskInstance.getProcessInstance().getContextInstance().setVariable(Variaveis.MINUTA_EM_ELABORACAO, valueInt);
							}
							
							if (!this.validarMovimentosAntesDoLancamento()){
								return;
							}
							
							EventsTreeHandler.instance().registraEventos();
							EventsEditorTreeHandler.instance().registraEventos();
							EventsHomologarMovimentosTreeHandler.instance().registraEventos();

							if (!EventsTreeHandler.instance().validarComplementos()){
								return;
							}							

							assinar = Boolean.FALSE;
						} else if (type.equals("painelLiberacaoVoto") && Authenticator.isMagistrado()) { 
							if (votoAction.getVoto().getIdVoto() == 0) {
								votoAction.persist();
							} else {
								votoAction.update();
							}
						} else{
							Contexts.getBusinessProcessContext().set(var.getMappedName(), value);
						}
					}
				}
				Contexts.getBusinessProcessContext().flush();
				Util.setToEventContext(UPDATED_VAR_NAME, true);
				updateTransitions();
			}
		}
	}
	
	/**
	 * Método responsável por verificar se um documento foi assinado.
	 * @param id Identificador do documento.
	 * 
	 * @return Verdadeiro se o documento foi assinado. Falso, caso contrário.
	 */
	private boolean isDocumentoAssinado(int id) {
		AssinaturaDocumentoService assinaturaDocumentoService = ComponentUtil.getComponent(AssinaturaDocumentoService.NAME);
		return assinaturaDocumentoService.isDocumentoAssinado(id);
	}

	private Integer salvarEditor() {
		if (assinar) {
			EditorAction.instance().assinar();
		} else {
			EditorAction.instance().save();
		}
		Integer valueInt = EditorAction.instance().getDocumento().getProcessoDocumento().getIdProcessoDocumento();
		return valueInt;
	}

	private Boolean checkAccess(){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Processo o where ");
		sb.append("o.idProcesso = :id ");
		sb.append("and (o.actorId is null or o.actorId = :login)");
		Query q = EntityUtil.createQuery(sb.toString());
		q.setParameter("id", ProcessoHome.instance().getInstance().getIdProcesso());
		q.setParameter("login", Authenticator.getUsuarioLogado().getLogin());
		
		if (q.getResultList().isEmpty()){
			FacesMessages.instance().clear();
			throw new AplicationException(MSG_USUARIO_SEM_ACESSO);
		}			
		return Boolean.TRUE;
	}

	public void update(Object homeObject){
		if (checkAccess()){
			canDoOperation();
			
			if (homeObject instanceof AbstractHome<?>){
				AbstractHome<?> home = (AbstractHome<?>) homeObject;
				home.update();
			}

			update();

			if (this.validarMovimentosAntesDoLancamento()){
				EventsTreeHandler.instance().registraEventos();
				EventsEditorTreeHandler.instance().registraEventos();
				EventsHomologarMovimentosTreeHandler.instance().registraEventos();
			} else {
				FacesMessages.instance().add(Severity.ERROR,"Esta tarefa requer a escolha de pelo menos uma movimentação processual.");
			}

			AutomaticEventsTreeHandler.instance().registraEventos();
		}
	}

	public void persist(Object homeObject){
		if (checkAccess()){
			canDoOperation();
			if (homeObject instanceof AbstractHome<?>){
				AbstractHome<?> home = (AbstractHome<?>) homeObject;
				Object entity = home.getInstance();
				home.persist();
				Object idObject = EntityUtil.getEntityIdObject(entity);
				home.setId(idObject);
				if (varName != null){
					instance.put(varName, idObject);
				}
				update();
			}

			if (this.validarMovimentosAntesDoLancamento()){
				EventsTreeHandler.instance().registraEventos();
				EventsEditorTreeHandler.instance().registraEventos();
				EventsHomologarMovimentosTreeHandler.instance().registraEventos();
			}
			AutomaticEventsTreeHandler.instance().registraEventos();
		}
	}

	public void canDoOperation(){
		if (currentTaskInstance == null){
			currentTaskInstance = org.jboss.seam.bpm.TaskInstance.instance();
		}
		if (currentTaskInstance != null){
			if (canOpenTask(currentTaskInstance.getId())){
				return;
			}
			FacesMessages.instance().clear();
			throw new AplicationException(MSG_USUARIO_SEM_ACESSO);
		}
	}

	@Observer(Event.EVENTTYPE_TASK_CREATE)
	public void setCurrentTaskInstance(ExecutionContext context){
		try{
			this.currentTaskInstance = context.getTaskInstance();
		} catch (Exception ex){
			String action = "atribuir a taskInstance corrente ao currentTaskInstance: ";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.createMessage(action + ex.getLocalizedMessage(),
					"setCurrentTaskInstance()", "TaskInstanceHome", "BPM"));
		}
	}

	/**
	 * @author Rafael Carvalho (CSJT) [PJEII-364] Processos estão ficando inconsistentes, sem estar em nenhuma tarefa do fluxo - JIRA CNJ. Inclusao do @Transactional
	 *         e do metodo cleanAndClose no JbpmUtil. Com isto a transicao nao muda caso seja lancada alguma exception em algum evento.
	 * @param transition
	 * @return
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
	@Transactional
	public String end(String transition){
		TaskController taskController = taskInstance.getTask().getTaskController();
		transicaoSaida = transition;
		if (taskController != null) {
			List<TaskVariavel> taskVariaveis = TaskVariavel.recuperarTaskVariaveis(taskController); 
			
			Validador validador = new Validador();
			
			for (TaskVariavel taskVariavel : taskVariaveis) {
				TaskVariavelAction taskVariavelAction = taskVariavel.getAction();
				taskVariavelAction.validar(transition, validador);
			}
			
			if (validador.getPossuiErros()) {
				for (String erro : validador.getErros()) {
					FacesMessages.instance().add(Severity.ERROR, erro);
				}
				return null;
			}
			
			try {
				for (TaskVariavel taskVariavel : taskVariaveis) {
					TaskVariavelAction taskVariavelAction = taskVariavel.getAction();
					taskVariavelAction.movimentar(transition);
				}
			} 
			catch (Exception e) {
				FacesMessages.instance().add(Severity.ERROR, e.getMessage());
				return null;
			}
		}		
				
		if(!validaEndTask(transition)) {
			return null;
		}
		
		try{
			this.currentTaskInstance = null;
			ProcessoHome.instance().setIdProcessoDocumento(null);
			
			if (!tramitacaoProcessualService.isTransicaoDispensaRequeridos(transition)){
				salvarDocumentoPorFluxo = !tramitacaoProcessualService.isTransicaoDispensaRequeridos(transition);
				update();
				if(!LancadorMovimentosService.instance().deveGravarTemporariamente()) {
					EventsTreeHandler.instance().registraEventos();
					EventsEditorTreeHandler.instance().registraEventos();
					EventsHomologarMovimentosTreeHandler.instance().registraEventos();
				}
			}
			BusinessProcess.instance().endTask(transition);
			
			SituacaoProcessoDAO spDao = ComponentUtil.getComponent(SituacaoProcessoDAO.NAME);
			boolean existeSituacao = spDao.existeSituacaoComTarefaByProcessoSemFiltros(ProcessoHome.instance().getInstance().getIdProcesso());
			/** Processo foi para o limbo, lança exceção para fazer o rollback */
			if( !existeSituacao ) {
				throw new MovimentarFluxoException("Falha na movimentação do processo, favor entrar em contato com a central de atendimento ao usuário!");
			}

			return verificarEhRecuperarProximaTarefa();	
		} 
		catch (Exception e){
			Throwable t = e;
			
			try {
				ErrorMovimentarFluxoAction action = ComponentUtil.getComponent(ErrorMovimentarFluxoAction.NAME);
				// salvar pilha de logs de ações no fluxo
				
				if (ProcessInstance.instance() != null
						&& ProcessInstance.instance().getLoggingInstance() != null
						&& ProcessInstance.instance().getLoggingInstance().getLogs() != null) {
					action.setProcessLogs(ProcessInstance.instance().getLoggingInstance().getLogs());
				}
				
				if (e instanceof DelegationException || e instanceof JbpmException) { // erro do jbpm ao dar erro em EL... buscar erro que ajude mais na resolução de problemas
					if (action.getLastThrowable() != null) {
						t = action.getLastThrowable();
					}
				}
			} catch (Exception e2) {}

			log.error("Erro ao transitar o Processo com id: " + ProcessoHome.instance().getId(), e);
			JbpmContext context = JbpmConfiguration.getInstance().getCurrentJbpmContext();
			// Limpa sessao hibernate (JBPM) e fecha contexto evitando flush.
			JbpmUtil.clearAndClose(context);
			
			try{
				// Libera a associação entre a transação (do Seam) e a thread corrente. Dessa forma o 
				// Util.beginTransaction pode iniciar outra transação e associar à thread corrente.
				//
				// Não foi usado o Util.rollbackTransction, pois ele checa se a transação está ativa, porém, dependendo da exceção (método Work.isRollbackRequired()), 
				// o interceptor do Seam pode "marcar" a transação para rollback, o que deixa a transação como inativa,
				// mas deixa a thread associada à transação inativa (o que prejudica a próxima iteração, quando ocorrer o beginTransaction - exceção:
				// "thread is already associated with a transaction!").
				Transaction.instance().rollback();
			} catch (Exception e1){}
				
			// Remove dos contextos do Seam o contexto do JBPM (ManagedJbpmContext.instance()), dessa forma, na próxima 
			// iteração, o Seam irá criar outro contexto JBPM (ManagedJbpmContext.create()), associado a uma sessão nova do Hibernate, 
			// pois ao dar "rollback" no contexto JBPM a sessão tem que ser fechada (JbpmUtil.clearAndClose()). 
			// Isso é necessário, pois a implementação do JBPM do Seam não usa JTA.
			Contexts.removeFromAllContexts("org.jboss.seam.bpm.jbpmContext");
			
			// Iniciar uma transação, se não houver transação ativa.
			Util.beginTransaction();
			throw new MovimentarFluxoException("Falha na movimentação do processo, favor entrar em contato com a central de atendimento ao usuário!", t);
		}
	}

	/**
	 * Realiza as validações para o método end.
	 *		Valida acesso
	 *		Valida se a transição que será aberta não é vazia
	 *		Valida a dispensa de requeridos
	 * @param String transition
	 * @return Boolean atende os requisitos para prosseguir com o endtask.
	 */	
	private Boolean validaEndTask(String transition) throws AplicationException{	
		if (!checkAccess()){
			return false;
		}
		
		if (StringUtil.isEmpty(transition) ) {
			FacesMessages.instance().add(Severity.ERROR, "Favor selecionar a transição de saída.");
			return false;			
		}
		
		TaskInstance tempTask = org.jboss.seam.bpm.TaskInstance.instance();
		if (currentTaskInstance != null){
			if (tempTask == null || tempTask.getId() != currentTaskInstance.getId()){
				FacesMessages.instance().clear();
				throw new AplicationException(MSG_USUARIO_SEM_ACESSO);
			}
		}
		
		if(!validaDispensaRequeridos(transition)) {
			return false;
		}
		return true;
	}

	/**
	 * Verifica a dispensa (ou não) de preenchimento para o componente "eventsTree" de acordo com o 
	 * nome da transição informado na variável de fluxo "pje:fluxo:transicao:dispensaRequeridos".
	 * 
	 * @param String Nome da transição.
	 * @return Boolean 
	 */
	private boolean validaDispensaRequeridos(String transition) {
		if (!tramitacaoProcessualService.isTransicaoDispensaRequeridos(transition)){
			return this.validarComponentesTarefa() && this.validarMovimentosAntesDoLancamento();
		}
		return true;
	}
	
	private boolean validarMovimentosAntesDoLancamento(){
		if( LancadorMovimentosService.instance().possuiCondicaoLancamentoMovimentoObrigatorio() || 
				!LancadorMovimentosService.instance().deveGravarTemporariamente()) {
			
			Boolean validaAntesLancamento = EventsTreeHandler.instance().validacoesAntesLancamento();
			Boolean validaAntesLancamentoEditor = EventsEditorTreeHandler.instance().validacoesAntesLancamento();
			Boolean validaAntesLancamentoHomologar = EventsHomologarMovimentosTreeHandler.instance().validacoesAntesLancamento();
			
			if (!validaAntesLancamento || !validaAntesLancamentoEditor || !validaAntesLancamentoHomologar){
				FacesMessages.instance().add(Severity.ERROR,"Esta tarefa requer a escolha de pelo menos uma movimentação processual");
				return false;
			}		
		}
		return true;
	}
	
	/**
	 * É possível que os componentes da tarefa atual gerenciem a validação da sua informação e caso esta validação não esteja OK
	 * devem setar uma variável de TAREFA com o prefixo Variaveis.PJE_PREFIXO_VARIAVEL_TAREFA_VALIDACAO_COMPONENTE_RESULTADO, concatenado
	 * com o nome do componente e o valor FALSE
	 * também devem cadastrar a variável de TAREFA Variaveis.PJE_PREFIXO_VARIAVEL_TAREFA_VALIDACAO_COMPONENTE_MENSAGEM, concatenado com o nome do componente
	 * e a mensagem (STRING) da pendência de validação
	 * - Caso os componentes não gerem a variável de TAREFA e não gerem com o prefixo correto a validação de componentes considerará o componente como válido
	 * - Veja exemplo de implementacao em RevisarMinutaAction.java
	 * 
	 * @return true/false
	 */
	private boolean validarComponentesTarefa() {
		taskInstance = org.jboss.seam.bpm.TaskInstance.instance();

		boolean resultado = Boolean.TRUE;
		String mensagemErro = "";
		if ((taskInstance != null) && (taskInstance.getTask() != null)){
			Map<String, Object> variaveisLocais = taskInstance.getVariablesLocally();
			if(variaveisLocais != null && !variaveisLocais.isEmpty()) {
				for(Entry<String, Object> variavel : variaveisLocais.entrySet()) {
					String nomeVariavel = variavel.getKey();
					if(nomeVariavel.startsWith(Variaveis.PJE_PREFIXO_VARIAVEL_TAREFA_VALIDACAO_COMPONENTE_RESULTADO)) {
						Object valorVariavel = variavel.getValue();
						resultado = (boolean) valorVariavel;
						if(!resultado) {
							List<String> valores = Arrays.asList(nomeVariavel.split(Variaveis.PJE_PREFIXO_VARIAVEL_TAREFA_VALIDACAO_COMPONENTE_RESULTADO));
							String nomeComponente = "";
							if(!valores.isEmpty() && valores.size() == 2) {
								nomeComponente = valores.get(1);
								String nomeVariavelMensagem = Variaveis.PJE_PREFIXO_VARIAVEL_TAREFA_VALIDACAO_COMPONENTE_MENSAGEM.concat(nomeComponente);
								Object objectMensagem = taskInstance.getVariableLocally(nomeVariavelMensagem);
								if(objectMensagem != null) {
									mensagemErro = (String) objectMensagem;
								}
							}else {
								mensagemErro = "Não foi possível validar um dos componentes desta tarefa";
							}
							break;
						}
					}
				}
			}
		}
		
		if(!resultado && !mensagemErro.isEmpty()) {
			FacesMessages.instance().add(Severity.ERROR, mensagemErro);
		}
		return resultado;
	}
	
	
	public void limparComponentesTarefa(String excecao) {
		taskInstance = org.jboss.seam.bpm.TaskInstance.instance();

		if ((taskInstance != null) && (taskInstance.getTask() != null)){
			Map<String, Object> variaveisLocais = taskInstance.getVariablesLocally();
			if(variaveisLocais != null && !variaveisLocais.isEmpty()) {
				for(Entry<String, Object> variavel : variaveisLocais.entrySet()) {
					String nomeVariavel = variavel.getKey();
					if(nomeVariavel.startsWith(Variaveis.PJE_PREFIXO_VARIAVEL_TAREFA_VALIDACAO_COMPONENTE_RESULTADO) || nomeVariavel.startsWith(Variaveis.PJE_PREFIXO_VARIAVEL_TAREFA_VALIDACAO_COMPONENTE_MENSAGEM)) {
						String[] valores = nomeVariavel.split(":");
						if(!valores[valores.length -1].equals(excecao)) {
							taskInstance.deleteVariableLocally(nomeVariavel);
						}
							
					}
				}
			}
		}
		
	}
	
	/**
	 * Verifica qual a proxima tarefa a ser executada, o 'Fechar' ou 'Movimentar'.
	 * 
	 * @return 'Fechar' ou 'Movimentar'
	 */
	public String verificarEhRecuperarProximaTarefa(){
		String retornoMovimento = "fechar";
	 
		// verifica se o usuario é da localizacao/papel da swimlane da
		if (this.currentTaskInstance != null && canOpenTask(this.currentTaskInstance.getId()) &&  currentTaskInstance.isOpen()){
			setTaskId(currentTaskInstance.getId());
			Util.setToEventContext("newTaskId", getTaskId());
			Util.setToEventContext("isTarefaAssinatura", currentTaskInstance.getTask().getPriority() == 4);
			retornoMovimento = "movimentar";
			if(iframe != null && iframe) {
				retornoMovimento = "fecharIframe";
			}

		} else {
			Util.setToEventContext("canClosePanel", true);
		}
		Util.setToEventContext("taskCompleted", true);
		return retornoMovimento;
	}
	
	
	@Transactional
	public String saidaDireta(String transition){
		if (!checkAccess()){
			return null;
		}
		TaskInstance tempTask = org.jboss.seam.bpm.TaskInstance.instance();
		if (currentTaskInstance != null){
			if (tempTask == null || tempTask.getId() != currentTaskInstance.getId()){
				FacesMessages.instance().clear();
				throw new AplicationException(MSG_USUARIO_SEM_ACESSO);
			}
		}
		try{
			this.currentTaskInstance = null;
			ProcessoHome.instance().setIdProcessoDocumento(null);
			update();
			
			BusinessProcess.instance().endTask(transition);
			boolean fechar = false;
			if (this.currentTaskInstance == null){
				Util.setToEventContext("canClosePanel", true);
				fechar = true;
			}else{
				// verifica se o usuario é da localizacao/papel da swimlane da
				// tarefa criada
				if (canOpenTask(this.currentTaskInstance.getId())){
					if(currentTaskInstance.isOpen()){
						setTaskId(currentTaskInstance.getId());
						Util.setToEventContext("newTaskId", getTaskId());
					}else{
						Util.setToEventContext("canClosePanel", true);
						fechar = true;
					}
				}else{
					Util.setToEventContext("canClosePanel", true);
					fechar = true;
				}
			}
			Util.setToEventContext("taskCompleted", true);
			if(fechar){
				return "fechar";
			}
		} catch (Exception e){
			log.error("Erro ao transitar o Processo com id: " + ProcessoHome.instance().getId());
			JbpmContext context = JbpmConfiguration.getInstance().getCurrentJbpmContext();
			// Limpa sessao hibernate (JBPM) e fecha contexto evitando flush.
			JbpmUtil.clearAndClose(context);
			throw new AplicationException("Falha na movimentação do processo, favor entrar em contato com a central de atendimento ao usuário!", e);
		}
		if(iframe != null && iframe) {
			return "fecharIframe";
		}
		return "movimentar";
	}
	public void clearActorId(){
		try{
			String q = "update tb_processo set nm_actor_id = null where id_processo = :id";
			EntityUtil.createNativeQuery(q, "tb_processo")
					.setParameter("id", JbpmUtil.getProcesso().getIdProcesso()).executeUpdate();
		} catch (Exception ex){
			String action = "limpar as variaveis do painel para atualização: ";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.createMessage(action + ex.getLocalizedMessage(),
					"refreshPainel()", "JbpmEventsHandler", "BPM"));
		}
	}

	/**
	 * Verifica se a tarefa destino da transição apareceria no painel do usuario
	 * 
	 * @param currentTaskId
	 * @return
	 */
	private boolean canOpenTask(long currentTaskId){
		JbpmUtil.getJbpmSession().flush();
		Events.instance().raiseEvent(TarefasTreeHandler.FILTER_TAREFAS_TREE);
		StringBuilder sql = new StringBuilder("select count(o) from SituacaoProcesso o ");
		sql.append(" where o.idTaskInstance = :ti ");
		Query query = EntityUtil.getEntityManager().createQuery(sql.toString());
		query.setParameter("ti", currentTaskId);
		try {
			Long retorno = (Long) query.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	public void start(long taskId){
		setTaskId(taskId);
		BusinessProcess.instance().startTask();
	}

	public Long getTaskId(){
		return taskId;
	}

	public void setTaskId(Long taskId){
		setTaskId(taskId, true);
	}
	
	public void setTaskId(Long taskId, boolean loadFormData){
		this.taskId = taskId;
		BusinessProcess bp = BusinessProcess.instance();
		bp.setTaskId(taskId);
		taskInstance = org.jboss.seam.bpm.TaskInstance.instance();
		if (taskInstance != null){
			long processId = taskInstance.getProcessInstance().getId();
			bp.setProcessId(processId);
			try {
				ManagedJbpmContext.instance().getSession().refresh(org.jboss.seam.bpm.TaskInstance.instance());
			}
			catch (Exception e) {
				// DO NOTHING
			}
			updateTransitions();
			if(loadFormData){
				createInstance();
			}
		}
	}

	public List<Transition> getTransitions(boolean includeOccultTransition){
		try {
			if (taskId == null){
				setTaskId(org.jboss.seam.bpm.TaskInstance.instance().getId());
			}
			List<Transition> list = new ArrayList<Transition>();
			if (availableTransitions != null && availableTransitions.size() == 0 && taskInstance != null){
				updateTransitions();
			}
			if (availableTransitions == null){
				return list;
			}
			// pega da definicao para garantir a mesma ordem do XML
			for (Transition transition : leavingTransitions){
				if (availableTransitions.contains(transition) && (!hasOcculTransition(transition) || includeOccultTransition)){
					list.add(transition);
				}
			}
			
			if (ParametroUtil.instance().isOrdenarTransicoesAlfabeticamente()) {
				Collections.sort(list, new TransitionComparator());
			}
			
			return list;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public List<Transition> getTransitions(){
		return getTransitions(false);
	}

	/**
	 * PJEVSII-389 > Método antigo estava comparando string com string e não executava a EL
	 * 			   > Ou seja executava a comparação (#{EL})equals("#{true}")
	 * @param transition
	 * @return
	 */
	public static boolean hasOcculTransition(Transition transition){
		if(transition.getCondition() != null){
			ExecutionContext executionContext = ExecutionContext.currentExecutionContext();
			if(transition.getCondition().equals("#{true}")){
				return true;
			} else {
				Object result = JbpmExpressionEvaluator.evaluate(transition.getCondition(), executionContext);
				if(result instanceof Boolean){
					return !OCCULT_TRANSITION.equals(result);
				}
			}
		}
		return false;
	}

	public void updateTransitions(){
		taskInstance = org.jboss.seam.bpm.TaskInstance.instance();
		availableTransitions = taskInstance.getAvailableTransitions();
		leavingTransitions = taskInstance.getTask().getTaskNode().getLeavingTransitions();
	}

	/**
	 * Refeita a combobox com as transições utilizando um f:selectItem pois o componente do Seam (s:convertEntity) estava dando problemas com as
	 * entidades do JBPM.
	 * 
	 * @return Lista das transições.
	 */
	public List<SelectItem> getTranstionsSelectItems(){
		List<SelectItem> selectList = new ArrayList<SelectItem>();
		for (Transition t : getTransitions()){
			selectList.add(new SelectItem(t.getName(), t.getName()));
		}
		return selectList;
	}

	public void clear(){
		this.instance = null;
		this.taskInstance = null;
	}

	public ModeloDocumento getModeloDocumento(){
		createInstance();
		return modeloDocumento;
	}

	public void setModeloDocumento(ModeloDocumento modelo){
		this.modeloDocumento = modelo;
		instance.put(variavelDocumento, ModeloDocumentoAction.instance().getConteudo(modelo));
	}

	public String getHomeName(){
		return "taskInstanceHome";
	}

	public String getName(){
		return name;
	}

	public void setName(String transition){
		this.name = transition;
	}

	public static TaskInstanceHome instance(){
		return (TaskInstanceHome) Component.getInstance(TaskInstanceHome.NAME);
	}

	public boolean isImmediateTaskButton() {
		return immediateTaskButton;
	}

	public void setImmediateTaskButton(boolean immediateTaskButton) {
		this.immediateTaskButton = immediateTaskButton;
	}
	
	@Observer("org.jboss.seam.preDestroy." + TaskInstanceHome.NAME)
	@Transactional
	public void liberarTaskInstanceFluxo() {
		TaskInstanceHome component = TaskInstanceHome.instance();
		TaskInstance ti = component.taskInstance;
		if (ti != null) {
			try {
				// PJEII-17706 - O ator da tarefa estava sendo "apagado" após a tarefa ter sido concluída
				ti = ManagedJbpmContext.instance().getTaskInstance(ti.getId());
				
				if(ti.getEnd() == null && ti.getActorId() != null){
					ti.setActorId(null);
					log.info(MessageFormat.format("Liberando taskInstance {0} do usuário.", ti.getId()));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	}
	
	public void desbloqueiaProcesso(ConsultaProcessoVO processo){
		String sql = "update jbpm_taskinstance set actorid_ = null where id_ = :ti";
		HibernateUtil.getSession().createSQLQuery(sql)
				.addSynchronizedQuerySpace("jbpm_taskinstance")
				.setParameter("ti", processo.getIdTaskInstance()).executeUpdate();
		processo.setActorId(null);
	}

	public void verificaBloqueio(ConsultaProcessoVO processo) {
		org.jbpm.taskmgmt.exe.TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(processo.getIdTaskInstance());
		if(taskInstance != null){
			processo.setActorId(taskInstance.getActorId());
		}
	}
	
	public String verificarTransicaoBypassOnClick(String nomeTransacao) {
		String variavel = (String) tramitacaoProcessualService.recuperaVariavelTarefa("pje:fluxo:tarefas:bypassOnClick");
		if (variavel != null && variavel.equals(nomeTransacao)) {
			return "true";
		}
		
		return "false";
	}

	private void desbloqueiaTokenLock() {
		if(taskInstance != null) {
			org.jbpm.graph.exe.Token token = taskInstance.getToken();
			if(taskInstance.getToken().isLocked()) {
				UnlockTokenCommand unlockToken = new UnlockTokenCommand();
				unlockToken.execute(token);
			}
		}
	}
	
	public String getTransicaoSaida() {
		return transicaoSaida;
	}
	
	public void setTransicaoSaida(String transicaoSaida) {
		this.transicaoSaida = transicaoSaida;
	}

	public Boolean getIframe() {
		return iframe;
	}

	public void setIframe(Boolean iframe) {
		this.iframe = iframe;
	}

	public Boolean getSalvarDocumentoPorFluxo() {
		return salvarDocumentoPorFluxo;
	}

	public void setSalvarDocumentoPorFluxo(Boolean salvarDocumentoPorFluxo) {
		this.salvarDocumentoPorFluxo = salvarDocumentoPorFluxo;
	}
}