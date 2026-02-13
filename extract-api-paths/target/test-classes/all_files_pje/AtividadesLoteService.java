package br.jus.cnj.pje.servicos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.log.Log;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.def.TaskController;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.component.tree.EventsLoteTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.manager.SituacaoProcessoManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.HistoricoMovimentacaoLoteManager;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessInstanceUtil;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTarefaEventoManager;
import br.jus.cnj.pje.nucleo.service.FluxoService;
import br.jus.cnj.pje.nucleo.service.PessoaFisicaService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.util.CustomJbpmTransactional;
import br.jus.cnj.pje.util.CustomJbpmTransactionalClass;
import br.jus.cnj.pje.util.TransitionComparator;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.HistoricoMovimentacaoLote;
import br.jus.pje.nucleo.entidades.HistoricoProcessoMovimentacaoLote;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrfLocal;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Componente de serviço responsável por concentrar as consultas e atividades 
 * necessárias à concretização de atividades em lote.
 * 
 * @author Guilherme Bispo
 * @author Bernardo Gouveia
 *
 */
@Name("atividadesLoteService")
@Scope(ScopeType.EVENT)
@CustomJbpmTransactionalClass
public class AtividadesLoteService {
	
	@Logger
	private Log log;
	
	@In(create = true, required = true)
	private SituacaoProcessoManager situacaoProcessoManager;
	
	@In(create = true, required = true)
	private DocumentoJudicialService documentoJudicialService;
	
	@In(create = true, required = true)
	private ProcessoDocumentoBinManager processoDocumentoBinManager;
	
	@In(create = true, required = true)
	private LancadorMovimentosService lancadorMovimentosService;
	
	@In
	private PessoaFisicaService pessoaFisicaService;
	
	@In
	private UsuarioService usuarioService;
	
	@In
	private Expressions expressions;
	
	@In
	private FluxoService fluxoService;
	
	@In
	private HistoricoMovimentacaoLoteManager historicoMovimentacaoLoteManager;
	
	@In(create = true)
	private ProcessoDocumentoHome processoDocumentoHome;
	
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	
	@In
	private ProcessoTarefaEventoManager processoTarefaEventoManager;
	
	@In
	private TramitacaoProcessualService tramitacaoProcessualService;
	
	@In
	private ModeloDocumentoManager modeloDocumentoManager;
	
	/**
	 * Recupera as transições disponíveis para uma dada {@link TaskInstance}, excluindo
	 * eventual transição que tenha como condição a expressão "#{true}".
	 * 
	 * @param ti a {@link TaskInstance} cujas transições se pretende recuperar
	 * @return as transições disponíveis
	 */
	public List<Transition> getAvaliableTransitions(TaskInstance ti){
		BusinessProcess.instance().setTaskId(ti.getId());
	  	BusinessProcess.instance().setProcessId(ti.getProcessInstance().getId());
	  	Set<Transition> ret = new LinkedHashSet<Transition>();
		List<Transition> at = ti.getAvailableTransitions();
		List<Transition> dt = ti.getTask().getTaskNode().getLeavingTransitions();
		if (at == null){
			return Collections.emptyList();
		}else{
			for (Transition t: dt){
				if (at.contains(t) && !"#{true}".equals(t.getCondition())){
					ret.add(t);
				}
			}

		}
		
		List<Transition> retorno = new ArrayList<Transition>(ret);
		if (ParametroUtil.instance().isOrdenarTransicoesAlfabeticamente()) {
			Collections.sort(retorno, new TransitionComparator());
		}
		return retorno;
	}
	 
	/**
	 * Recupera a {@link TaskInstance} que tenha o identificador dado.
	 * 
	 * @param id o identificador da {@link TaskInstance} a se recuperar
	 * @return a {@link TaskInstance}
	 */
	public TaskInstance getTaskInstanceById(Long id) {
		return ManagedJbpmContext.instance().getTaskInstance(id);
	}
	
	/**
	 * Recupera a transição padrão de uma dada tarefa, definida na variável de tarefa
	 * "frameDefaultLeavingTransition".
	 * 
	 * @param idTaskInstance o identificador da tarefa cuja transição padrão se pretende recuperar
	 * @return a transição, se ela existir, ou nulo.
	 */
	public Transition getFrameDefaultLeavingTransition(Long idTaskInstance) {
		Transition retorno = null;
		// se for assinatura em lote, considerar que pode estar homologando uma minuta, a qual usa a variável frameDefaultLeavingTrasition
		String variable = (String) this.getTaskInstanceById(idTaskInstance).getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		if (variable != null) {
			Transition transitionByName = this.getTransitionByName(this.getTaskInstanceById(idTaskInstance), variable);
			retorno = transitionByName;
		}
		return retorno;
	}
	
	/**
	 * Verifica se há movimentações processuais a serem lançados manualmente na tarefa.
	 * 
	 * @param processoTrf o processo a ser verificado
	 * @param nomeTaskInstance o nome da {@link TaskInstance}
	 * @return true, se o nó tiver sido definido com movimentações obrigatórias manuais
	 */
	public boolean hasEvents(ProcessoTrf processoTrf, String nomeTaskInstance) {
		return processoTarefaEventoManager.exigeLancamentoManual(processoTrf, nomeTaskInstance);
	}
	
	/**
	 * Verifica se existem movimentos registrados nesta tarefa.
	 * 
	 * @return true, se há movimentos.
	 */
	public boolean existeMovimentoRegistrado(ProcessoTrf processoTrf, String nomeTaskInstance) {
		return processoTarefaEventoManager.temMovimentoLancado(processoTrf, nomeTaskInstance);
	}
	
	@CustomJbpmTransactional
	private void end(Long id, Long idTransition, ProcessoTrf processoTrf, HistoricoMovimentacaoLote historicoMovimentacaoLote) throws Exception{
		TaskInstance ti = getTaskInstanceById(id);
		Transition transition = getTransitionById(idTransition, ti);
		end(id, transition, processoTrf, historicoMovimentacaoLote);
	}
	
	@CustomJbpmTransactional
	private void end(Long id, String transitionName, ProcessoTrf processoTrf, HistoricoMovimentacaoLote historicoMovimentacaoLote) throws Exception{
		TaskInstance ti = getTaskInstanceById(id);
		Transition transition = getTransitionByName(ti, transitionName);
		if (transition != null) {
			end(id, transition, processoTrf, historicoMovimentacaoLote);
		}
	}
	
	@CustomJbpmTransactional
	private void end(Long id, Transition transition, ProcessoTrf processoTrf, HistoricoMovimentacaoLote historicoMovimentacaoLote) throws Exception{
		TaskInstance ti = getTaskInstanceById(id);
		JbpmUtil.restaurarVariaveis(ti);
		ti.setActorId(Authenticator.getPessoaLogada().getLogin(), true);
		if (historicoMovimentacaoLote != null) {
			// Salva Historico
			Events.instance().raiseAsynchronousEvent(Eventos.EVENTO_MOVIMENTACAO_LOTE, transition.getId(), processoTrf, historicoMovimentacaoLote);
		}

		// Efetua a transição, com TaskInstance e Transition associados à sessão Hibernate do ContextoJBPM corrente.
		ti.end(transition);
	}
	
	public void transitarProcesso(Long id, Long idTransition, ProcessoTrf processoTrf, HistoricoMovimentacaoLote historicoMovimentacaoLote) throws Exception{

		TaskInstance taskInstance = getTaskInstanceById(id);

		Transition transitionRecuperadaNaSessionAtual = getTransitionById(idTransition, taskInstance);

		// Salva Historico
		Events.instance().raiseAsynchronousEvent(Eventos.EVENTO_MOVIMENTACAO_LOTE, transitionRecuperadaNaSessionAtual.getId(), processoTrf, historicoMovimentacaoLote);

		// Efetua a transição, com TaskInstance e Transition associados à sessão Hibernate do ContextoJBPM corrente.
		taskInstance.end(transitionRecuperadaNaSessionAtual);

	}
	
	@CustomJbpmTransactional
	public void movimentarProcesso(Long id, Long idTransition, Integer idProcesso, Long idHistorico) throws Exception{
		try{
			ProcessoJudicialManager pjm = (ProcessoJudicialManager) Component.getInstance("processoJudicialManager");
			ProcessoTrf proc = pjm.findById(idProcesso);
			HistoricoMovimentacaoLote hist = historicoMovimentacaoLoteManager.findById(idHistorico);
			movimentarProcesso(id, idTransition, proc, hist);
		}catch(PJeBusinessException e){
			String msg = String.format("Houve um erro ao tentar movimentar o processo %d da tarefa %d para a transição %d: %s", idProcesso, id, idTransition, e.getLocalizedMessage());;
			log.error(msg);
			throw new Exception(msg, e);
		}catch (Throwable e) {
			String msg = String.format("Houve um erro ao tentar movimentar o processo %d da tarefa %d para a transição %d: %s", idProcesso, id, idTransition, e.getLocalizedMessage());;
			log.error(msg);
			throw new Exception(msg, e);
		}
	}
	
	@CustomJbpmTransactional
	public void movimentarProcesso(Long id, Long idTransition, ProcessoTrf processoTrf, HistoricoMovimentacaoLote historicoMovimentacaoLote) throws Exception{
		inicializarHomes(processoTrf);
		inicializarFluxo(id);
		lancarMovimentos(id, idTransition, processoTrf, false);
		end(id, idTransition, processoTrf, historicoMovimentacaoLote);
	}
	
	@CustomJbpmTransactional
	public void movimentarAposIntimar(Long id, String transitionName, ProcessoTrf processoTrf, HistoricoMovimentacaoLote historicoMovimentacaoLote, boolean pendenciaIntimacao) throws Exception{
		inicializarHomes(processoTrf);
		ProcessInstanceUtil.instance().setVariable(Eventos.EVENTO_SINALIZACAO, Variaveis.VARIAVEL_FLUXO_INTIMACAO_LOTE);
		ProcessInstanceUtil.instance().setVariable(Variaveis.VARIAVEL_FLUXO_LOTE_PENDENCIA_INTIMACAO, pendenciaIntimacao);
		end(id, transitionName, processoTrf, historicoMovimentacaoLote);
	}

	private void inicializarHomes(ProcessoTrf processoTrf){
		fluxoService.iniciarHomesProcessos(processoTrf);
	}

	private void inicializarFluxo(Long idTaskInstance){
		fluxoService.iniciarBusinessProcess(idTaskInstance);
	}
	
	public boolean hasMinutaEmAberto(Long taskId, ProcessoTrf processo) {
		return documentoJudicialService.hasMinutaEmAberto(taskId, processo);
	}
	
	@CustomJbpmTransactional
	public void minutarProcessoSemMovimentar(Long idTask, String modeloDocumento, ProcessoTrf processoTrf) throws Exception{
		inicializarHomes(processoTrf);
		
		ProcessoHome processoHome = ProcessoHome.instance();
		
		juntarDocumentoAoProcesso(idTask, modeloDocumento, processoTrf, processoHome, Variaveis.MINUTA_EM_ELABORACAO);
	}
	
	@CustomJbpmTransactional
	public void minutarEMovimentarProcesso(Long idTask, String modeloDocumento, ProcessoTrf processoTrf, long idTransition, HistoricoMovimentacaoLote historicoMovimentacaoLote) throws Exception{
		inicializarHomes(processoTrf);
		
		ProcessoHome processoHome = ProcessoHome.instance();
		
		ProcessoDocumento processoDocumento = juntarDocumentoAoProcesso(idTask, modeloDocumento, processoTrf, processoHome, Variaveis.MINUTA_EM_ELABORACAO);
		
		movimentarProcesso(idTask, processoTrf, idTransition, historicoMovimentacaoLote, processoHome, processoDocumento);
	}

	public ProcessoDocumento juntarDocumentoAoProcesso(Long idTask, String modeloDocumento, ProcessoTrf processoTrf, ProcessoHome processoHome, String variavel) throws PJeBusinessException, Exception {
		Integer idDocEmElaboracao = JbpmUtil.instance().recuperarIdDocumentoEmElaboracao(ManagedJbpmContext.instance().getTaskInstance(idTask), variavel);
		return juntarDocumentoAoProcesso(idTask, modeloDocumento, processoTrf, processoHome, idDocEmElaboracao);
	}
	
	public ProcessoDocumento juntarDocumentoAoProcesso(Long idTask, String modeloDocumento, ProcessoTrf processoTrf, ProcessoHome processoHome, Integer idDocEmElaboracao) throws PJeBusinessException, Exception {
		BusinessProcess.instance().setProcessId(getTaskInstanceById(idTask).getProcessInstance().getId());
		
		String conteudoDocumento = ProcessoDocumentoHome.processarModelo(modeloDocumento);
		
		ProcessoDocumento processoDocumento = null;
		ProcessoDocumentoTrfLocal processoDocumentoTrfLocal = new ProcessoDocumentoTrfLocal();
		
		if (idDocEmElaboracao != null && idDocEmElaboracao > 0) {
			ProcessoDocumento docEmElaboracao = documentoJudicialService.getDocumento(idDocEmElaboracao);
			
			if(docEmElaboracao != null 
					&& docEmElaboracao.getProcessoDocumentoBin() != null 
					&& !ComponentUtil.getComponent(ProcessoDocumentoBinManager.class).existemSignatarios(docEmElaboracao.getProcessoDocumentoBin().getIdProcessoDocumentoBin())) {
				
				processoDocumento = documentoJudicialService.atualizaProcessoDocumento(
						docEmElaboracao, processoTrf, conteudoDocumento, processoHome.getTipoProcessoDocumento(), idTask);
				processoDocumento = documentoJudicialService.persist(processoDocumento, true);
			}
		}
		
		if(processoDocumento == null) {
			processoDocumento = documentoJudicialService.criarProcessoDocumento(conteudoDocumento, processoHome.getTipoProcessoDocumento(), processoTrf, idTask);
			processoDocumentoTrfLocal = new ProcessoDocumentoTrfLocal();
			processoDocumentoTrfLocal.setProcessoDocumento(processoDocumento);
			processoDocumentoManager.inserirProcessoDocumentoTrfLocal(processoDocumento, processoDocumentoTrfLocal);
		}
		documentoJudicialService.flush();
		return processoDocumento;		
	}
	
	private void movimentarProcesso(Long idTask, ProcessoTrf processoTrf, long idTransition,
			HistoricoMovimentacaoLote historicoMovimentacaoLote, ProcessoHome processoHome,
			ProcessoDocumento processoDocumento) throws Exception {
		processoHome.setIdProcessoDocumento(processoDocumento.getIdProcessoDocumento());
		processoHome.setPdFluxo(processoDocumento);

		inicializarFluxo(idTask);
		
		atribuirDocumentoAoFluxo(processoDocumento, idTask);
		
		lancarMovimentos(idTask, idTransition, processoTrf, false);
		
		// Se não houve erro ao criar o processoDocumento, movimentar processo.
		// Se ocorrer erro ao transitar, dá rollback no documento criado (TODO ver com a análise se esse é um comportamento desejado).
		this.end(idTask, idTransition, processoTrf, historicoMovimentacaoLote);
	}
	
	@CustomJbpmTransactional
	public void minutarEmLote(ProcessoTrf processo, String idModelo, Long idTask, Long idTransition, HistoricoMovimentacaoLote history){
		inicializarHomes(processo);
	}
	
	public void movimentar(Long idTask, ProcessoTrf proc, long idTransition, List<ArquivoAssinadoHash> arquivosAssinados, List<ProcessoDocumento> docs, HistoricoMovimentacaoLote historicoMovimentacaoLote) throws Exception{
		inicializarHomes(proc);
		inicializarFluxo(idTask);
		
		documentoJudicialService.gravarAssinaturaDeProcessoDocumento(arquivosAssinados, docs);

		boolean proceed = true;
		for (ProcessoDocumento  doc: docs ){
			if(documentoJudicialService.validaAssinaturasDocumento(doc, false).isEmpty()){
				proceed = false;
				break;
			}else{
				documentoJudicialService.finalizaDocumento(doc, proc, idTask, isDocumentoPrincipal(doc), false, false, pessoaFisicaService.find(usuarioService.getUsuarioLogado().getIdUsuario()), true);
			}
		}
		if(!proceed){
			throw new PJeBusinessException("Não foi possível finalizar a assinatura em lote do processo {0}.", null, proc.getNumeroProcesso());
		}
		lancarMovimentos(idTask, idTransition, proc, true);
		salvarUltimoAtoProferido(idTask);
		this.end(idTask, idTransition, proc, historicoMovimentacaoLote);
	}
	
 	/**
 	 *  Verifica se o documento informado é o principal.
 	 *
 	 * @param processoDocumento Documento a ser verificado.
 	 * @return Verdadeiro se o documento for o principal. Falso caso contrário
      */
 	private boolean isDocumentoPrincipal(ProcessoDocumento processoDocumento){
 		return processoDocumento != null && processoDocumento.getDocumentoPrincipal() ==  null;
 	}
	
	@CustomJbpmTransactional
	public void assinarEMovimentarProcesso(Long idTask, ProcessoTrf processoTrf, long idTransition, String assinatura, String certChain, List<ProcessoDocumento> listaProcessoDocumento, HistoricoMovimentacaoLote historicoMovimentacaoLote) throws Exception{
		inicializarHomes(processoTrf);
		inicializarFluxo(idTask);
		
		for (ProcessoDocumento  processoDocumento : listaProcessoDocumento ){
			ProcessoHome.instance().setIdProcessoDocumento(processoDocumento.getIdProcessoDocumento());
			processoDocumento = documentoJudicialService.getDocumento(processoDocumento.getIdProcessoDocumento());
			processoDocumento.getProcessoDocumentoBin().setCertChain(certChain);
			processoDocumento.getProcessoDocumentoBin().setSignature(assinatura);
			documentoJudicialService.finalizaDocumento(processoDocumento, processoTrf, idTask, true, true, false, pessoaFisicaService.find(usuarioService.getUsuarioLogado().getIdUsuario()), true);
		}

		lancarMovimentos(idTask, idTransition, processoTrf, true);
		salvarUltimoAtoProferido(idTask);
		this.end(idTask, idTransition, processoTrf, historicoMovimentacaoLote);
	}

	@CustomJbpmTransactional
	public void lancarMovimentos(Long idTask, Long idTransition, ProcessoTrf processoTrf, boolean isAssinatura){
		inicializarFluxo(idTask);
		
		ProcessInstance processInstance = getTaskInstanceById(idTask).getProcessInstance();
		TaskInstance taskInstance = getTaskInstanceById(idTask);
		
		boolean lancarMovimentoTemporariamente = lancadorMovimentosService.deveGravarTemporariamente();
		boolean dispensaRequerido = false;
		
		if (idTransition != null) {
			Transition transition = getTransitionById(idTransition, taskInstance);
			dispensaRequerido = tramitacaoProcessualService.isTransicaoDispensaRequeridos(transition.getName());
		}		
		
		//simular apresentacao do lancador em tela
		if (isAssinatura || (!lancarMovimentoTemporariamente && !dispensaRequerido)) {
			lancadorMovimentosService.homologarMovimentosTemporarios(processInstance);
		}
		
		EventsLoteTreeHandler.instance().registraEventos();
	}
	
	public void salvarUltimoAtoProferido(Long idTask){
		if (getIdMinutaEmElaboracao(idTask) != null) {
			Integer identificadorMinutaEmElaboracao = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(getTaskInstanceById(idTask));

			ProcessInstance processInstance = getTaskInstanceById(idTask).getProcessInstance();
			processInstance.getContextInstance().setVariable(Variaveis.ATO_PROFERIDO, identificadorMinutaEmElaboracao);
			processInstance.getContextInstance().setVariable(Variaveis.ULTIMO_DOCUMENTO_JUNTADO_NESTE_FLUXO, identificadorMinutaEmElaboracao);
			processInstance.getContextInstance().deleteVariable(Variaveis.MINUTA_EM_ELABORACAO);
		}
	}

	private void atribuirDocumentoAoFluxo(ProcessoDocumento documentoCriado, Long idTask){
		TaskInstance taskInstance = getTaskInstanceById(idTask);
		
		boolean adicionouVariavel = false;
		
		if ((taskInstance != null) && (taskInstance.getTask() != null)){
			TaskController taskController = taskInstance.getTask().getTaskController();
			if (taskController != null){
				ProcessInstance processInstance = getTaskInstanceById(idTask).getProcessInstance();
				processInstance.getContextInstance().setVariable(Variaveis.MINUTA_EM_ELABORACAO, documentoCriado.getIdProcessoDocumento());

				List<VariableAccess> list = taskController.getVariableAccesses();
				for (VariableAccess var : list){

					String type = var.getMappedName().split(":")[0];
					String name = var.getMappedName().split(":")[1];

					if (var.isWritable() && JbpmUtil.isTypeEditor(type, name)){
						if (documentoCriado != null && documentoCriado.getIdProcessoDocumento() != 0) {
							Contexts.getBusinessProcessContext().set(var.getMappedName(), documentoCriado.getIdProcessoDocumento());
							adicionouVariavel = true;
						}
					}
				}
			}
		}
		
		if(adicionouVariavel){
			Contexts.getBusinessProcessContext().flush();
		}
	}
	
	/**
	 * Verifica se existe uma minuta salva no fluxo.
	 * @param idTask
	 * @return true, caso exista; senão, false.
	 */
	public boolean verificaExistenciaDeMinuta(Long idTask) {
		return buscarMinutaSalvaNaTarefa(idTask) != null;
	}

	public Transition getTransitionById(Long idTransition, TaskInstance taskInstance){
		Transition t = null;
		List<Transition> transitions = taskInstance.getToken().getNode().getLeavingTransitions();
		for (Transition transition : transitions) {
			if (transition.getId() == idTransition) {
				t = transition;
				break;
			}
		}
		return t;
	}
	
	public SituacaoProcesso getSituacaoProcessoById(Long idTask){
		return situacaoProcessoManager.getById(idTask);
	}
	
	public SituacaoProcesso getSituacaoProcessoByIdSituacaoIdTarefa(Long idSituacao, Integer idTarefa) {
		return situacaoProcessoManager.getByIdSituacaoIdTarefa(idSituacao, idTarefa);
	}
	
	public SituacaoProcesso getSituacaoProcessoByIdProcesso(Integer idProcesso){
		return situacaoProcessoManager.getByIdProcesso(idProcesso);
	}
	
	private SituacaoProcesso obterSituacaoProcesso(ProcessoTrf processoTrf){
		try{
			return getSituacaoProcessoByIdProcesso(processoTrf.getIdProcessoTrf());
		} catch (Exception e){
			log.error("Erro ao tentar recuperar o contexto de execução do processo {0} para o registro da movimentação em lote: {1}.", processoTrf, e.getLocalizedMessage());
			return null;
		}
	}
	
	public HistoricoMovimentacaoLote adicionaHistoricoMovimentacao(String atividade){
		HistoricoMovimentacaoLote historicoMovimentacaoLote = null;
		try{
			historicoMovimentacaoLote = new HistoricoMovimentacaoLote();
			historicoMovimentacaoLote.setTipoAtividadeLote(atividade);
			historicoMovimentacaoLote.setDataMovimentacao(new Date());
			
			Usuario usuario = (Usuario) Contexts.getSessionContext().get("usuarioLogado");
			
			if(usuario != null){
				Pessoa pessoaLogada = (Pessoa) ProcessoHome.instance().getUsuarioLogado();
				historicoMovimentacaoLote.setIdUsuario(pessoaLogada.getIdUsuario());
			}
			EntityManager entityManager = EntityUtil.getEntityManager();
			entityManager.persist(historicoMovimentacaoLote);
			entityManager.flush();
		} catch (Exception e){
			//Insere no log, mas continua o processamento
			log.error("Erro ao salvar histórico de movimentação: {0}", e.getLocalizedMessage());
			e.printStackTrace();
		}
		return historicoMovimentacaoLote;
	}

	@Transactional
	public void salvaHistoricoProcessoMovimentacao(Object... args){
		if(args != null && args.length == 3 
				&& args[0] instanceof Long && args[1] instanceof ProcessoTrf && args[2] instanceof HistoricoMovimentacaoLote) {
			this.salvaHistoricoProcessoMovimentacaoComArgumentos((Long) args[0], (ProcessoTrf)args[1], (HistoricoMovimentacaoLote) args[2]);
		}else {
			log.error("Erro ao salvar Processo Historico de movimentação");
		}
	}

	/**
	 * Salva o histórico de movimentação em lote para o processo indicado.
	 * 
	 * O método é um observer para evitar que qualquer erro ao salvar o histórico, 
	 * impeça a efetiva movimentação do processo, já que a movimentação ocorre em uma transação
	 * e qualquer exceção, mesmo tratada com try/catch, dá rollback.
	 * 
	 * @param idTransicao
	 * @param processoTrf
	 * @param historicoMovimentacaoLote
	 */
	@Observer(Eventos.EVENTO_MOVIMENTACAO_LOTE)
	@Transactional
	public void salvaHistoricoProcessoMovimentacaoComArgumentos(Long idTransicao, ProcessoTrf processoTrf, HistoricoMovimentacaoLote historicoMovimentacaoLote){
		try{		
			//Obtem Situação Processo antes de movimentar para fins de historico
			SituacaoProcesso situacaoProcessoAntesMovimentacao = obterSituacaoProcesso(processoTrf);
			
			if(situacaoProcessoAntesMovimentacao != null && idTransicao != null && historicoMovimentacaoLote != null){
				
				HistoricoProcessoMovimentacaoLote historicoProcessoMovimentacaoLote = new HistoricoProcessoMovimentacaoLote();
				historicoProcessoMovimentacaoLote.setHistoricoMovimentacaoLote(historicoMovimentacaoLote);
				historicoProcessoMovimentacaoLote.setIdProcesso(processoTrf.getIdProcessoTrf());
				historicoProcessoMovimentacaoLote.setNomeFluxo(situacaoProcessoAntesMovimentacao.getNomeFluxo());
				historicoProcessoMovimentacaoLote.setIdTransicao(idTransicao);
				
				historicoProcessoMovimentacaoLote.setNomeTarefaOrigem(situacaoProcessoAntesMovimentacao.getNomeTarefa());
				historicoProcessoMovimentacaoLote.setIdProcessInstanceOrigem(situacaoProcessoAntesMovimentacao.getIdProcessInstance());
				historicoProcessoMovimentacaoLote.setIdTaskInstanceOrigem(situacaoProcessoAntesMovimentacao.getIdTaskInstance());
				historicoProcessoMovimentacaoLote.setIdTaskOrigem(situacaoProcessoAntesMovimentacao.getIdTask());
				
				EntityManager entityManager = EntityUtil.getEntityManager();

				entityManager.persist(historicoProcessoMovimentacaoLote);
				entityManager.flush();
				
			} else {
				//Insere no log, mas continua o processamento
				log.error("Erro ao salvar Processo Historico de movimentação");
			}
		} catch (Exception e){
			e.printStackTrace();
			//Insere no log, mas continua o processamento
			log.error("Erro ao salvar Historico de movimentação");
		}
	}
	
	public List<Integer> getIdsListFluxo(ProcessInstance processInstance, String actionExpressionStart){
		List<Integer> idsList = null;
		if (processInstance != null){
			Map<String, Event> mapaEventos = processInstance.getRootToken().getNode().getEvents();
			for (String eventName : mapaEventos.keySet()){
				Event event = mapaEventos.get(eventName);
				if (event.getEventType().equals(Event.EVENTTYPE_NODE_ENTER) || event.getEventType().equals(Event.EVENTTYPE_TASK_START)){
					List<Action> actions = event.getActions();
					for (Action action : actions){
						String actionExpression = action.getActionExpression();
						if (actionExpression != null && actionExpression.startsWith(actionExpressionStart)){
							String idsStr = actionExpression.substring(actionExpression.indexOf(",") + 1,
									actionExpression.indexOf(")"));
							String[] idsStrArr = idsStr.split(",");
							idsList = new ArrayList<Integer>(idsStrArr.length);
							for (String id : idsStrArr){
								idsList.add(Integer.parseInt(StringUtil.fullTrim(id)));
							}
						}
					}
				}
			}
		}
		return idsList;
	}

	public List<TipoProcessoDocumento> getTipoDocumentoItems(TaskInstance ti){
		List<TipoProcessoDocumento> listaTiposDocumentos = getListaTipoProcessoDocumento(ti);
		if (listaTiposDocumentos == null || listaTiposDocumentos.size() == 0) {
			try{
				listaTiposDocumentos = this.documentoJudicialService.getTiposDisponiveis();
			} catch (Exception e){}
		}
		return listaTiposDocumentos;
	}

	private List<TipoProcessoDocumento> getListaTipoProcessoDocumento(TaskInstance ti){
		DocumentoJudicialService djs = (DocumentoJudicialService) Component.getInstance(DocumentoJudicialService.class);
		return djs.getTiposDocumentosMinuta(ti, Authenticator.getPapelAtual());
	}
			
	/**
	 * Operação que recupera um modelo de documento conforme tipo processo documento e localização.
	 * 
	 * @param documento
	 * @param localizacao
	 * @return
	 * @throws Exception
	 */
	public List<ModeloDocumento> getModelosDisponiveisPorTipoProcessoDocumento(TipoProcessoDocumento documento, boolean usarLocalizacao) throws Exception {
		List<ModeloDocumento> retorno = null;
		if( documento != null ) {
			if (usarLocalizacao) {
				retorno = this.documentoJudicialService.getModelosLocais(documento);
			} else {
				retorno = this.documentoJudicialService.getModelosDisponiveisPorTipoProcessoDocumento(documento);
			}
			
		} else {
			List<TipoProcessoDocumento> listaTiposDocumentos = getListaTipoProcessoDocumento(null);
			if (listaTiposDocumentos == null || listaTiposDocumentos.size() == 0) {
				retorno = this.documentoJudicialService.getModelosDisponiveis();
			} else {
				retorno = this.documentoJudicialService.getModelosLocais(listaTiposDocumentos); 
			}
		}
		return retorno;
	}
	
	/**
	 * Operação que recupera modelos de documento a partir de um tipo de processo documento.
	 * @param documento
	 * @return
	 * @throws Exception
	 */
	public List<ModeloDocumento> getModelosDisponiveisPorTipoProcessoDocumento(TipoProcessoDocumento documento) throws Exception{
		return getModelosDisponiveisPorTipoProcessoDocumento(documento, false);
	}
	
	/**
	 * Método retorna a lista de documentos de um processo prontos para assinatura 
	 * dentro de uma determinada tarefa do fluxo 
	 * 
	 * @param idTask Id da tarefa onde o processo se encontra
	 * @return Lista de documentos prontos para assinar 
	 */
	public List<ProcessoDocumento> getDocumentoProntoParaAssinatura(Long idTask) {
		try {
			List<Integer> ids = getIdDocumentoProntoParaAssinatura(idTask);
			if(ids.isEmpty()){
				return Collections.emptyList();
			}
			List<ProcessoDocumento> documentosParaAssinar = new ArrayList<ProcessoDocumento>();
			for(Integer id : ids) {
				ProcessoDocumento documentoPrincipal = documentoJudicialService.getDocumento(id);
				if(documentoPrincipal != null && documentoPrincipal.getDocumentosVinculados() != null && !documentoPrincipal.getDocumentosVinculados().isEmpty()){
					documentosParaAssinar.addAll(atualizarVinculados(documentoPrincipal.getDocumentosVinculados()));
				}
				documentosParaAssinar.add(documentoPrincipal);
			}
			return documentosParaAssinar;
		} catch (PJeBusinessException e) {
			log.error("Erro ao tentar recuperar os documentos vinculados à tarefa de identificador [{0}]: {1}", idTask, e.getLocalizedMessage());
			return Collections.emptyList();
		}
	}

	/**
	 * Método atualiza documentos, pois outras janelas podem 
	 * modificar o mesmo objeto e essas modificações não são 
	 * buscadas diretamente no método documentoJudicialService.getDocumento(id)   
	 *  
	 * @param documentosVinculados lista de documentos vinculados para atualizar
	 * @return documentosVinculados atualizados
	 * @throws PJeBusinessException
	 */
	private List<ProcessoDocumento> atualizarVinculados( Collection<ProcessoDocumento> documentosVinculados ) throws PJeBusinessException {
		List<ProcessoDocumento> listaAtualizada = new ArrayList<ProcessoDocumento>(); 
		for( ProcessoDocumento documentoVinculado : documentosVinculados ){
			documentoJudicialService.refresh(documentoVinculado);
			listaAtualizada.add( documentoVinculado );
		}
		return listaAtualizada;
	}
	
	public boolean existeDocumentoProntoParaAssinatura(Long idTask){
		List<Integer> idDocumentoProntoParaAssinatura = getIdDocumentoProntoParaAssinatura(idTask);
		return idDocumentoProntoParaAssinatura != null && idDocumentoProntoParaAssinatura.size() > 0;
	}
	
	private List<Integer> getIdDocumentoProntoParaAssinatura(Long idTask){
		List<Integer> ids = new ArrayList<Integer>(recuperarMinutas(idTask));
		if(ids.isEmpty()){
			ids.addAll(getIdMinutaEmElaboracao(idTask));
		}
		return ids;
	}

	private Integer buscarMinutaSalvaNaTarefa(Long idTask){
		TaskInstance taskInstance = getTaskInstanceById(idTask);
		Integer retorno = null;
		
		if (taskInstance == null || taskInstance.getTask() == null || taskInstance.getTask().getTaskController() == null){
			return null;
		}
		
		TaskController taskController = taskInstance.getTask().getTaskController();
				
		List<VariableAccess> list = taskController.getVariableAccesses();
		
		for (VariableAccess var : list){

			String type = var.getMappedName().split(":")[0];
			String name = var.getMappedName().split(":")[1];

			if (var.isReadable() && JbpmUtil.isTypeEditor(type, name)) {
				
				Object idProcessoDocumento = taskInstance.getContextInstance().getVariable(var.getMappedName());

				if ((idProcessoDocumento != null) 
						&& (idProcessoDocumento instanceof Integer) 
						&& ((Integer) idProcessoDocumento).intValue() != 0) {
					retorno = ((Integer) idProcessoDocumento).intValue();
				}
			}
		}
		return retorno;
	}

	private List<Integer> recuperarMinutas(Long idTask){
		TaskInstance taskInstance = getTaskInstanceById(idTask);
		if (taskInstance == null || taskInstance.getTask() == null || taskInstance.getTask().getTaskController() == null){
			return Collections.emptyList();
		}
		List<Integer> ret = new ArrayList<Integer>();
		TaskController taskController = taskInstance.getTask().getTaskController();
		List<VariableAccess> list = taskController.getVariableAccesses();
		for (VariableAccess var : list){
			String type = var.getMappedName().split(":")[0];
			String name = var.getMappedName().split(":")[1];
			if (var.isReadable() && JbpmUtil.isTypeEditor(type, name)) {
				Object idProcessoDocumento = taskInstance.getContextInstance().getVariable(var.getMappedName());
				if (idProcessoDocumento != null 
						&& (idProcessoDocumento instanceof Integer) 
						&& ((Integer) idProcessoDocumento).intValue() != 0) {
					ret.add((Integer) idProcessoDocumento);
				}
			}
		}
		JbpmUtil.restaurarVariaveis(taskInstance);
		for (String var : taskInstance.getVariables().keySet()) {
			if (var.equals(Variaveis.MINUTA_EM_ELABORACAO)){
				Integer id = (Integer) taskInstance.getVariable(var);
				ret.add(id);
				break;
			}
		}
		return ret;
	}
	
	private List<Integer> getIdMinutaEmElaboracao(Long idTask){
		Object arrayMinutas = getTaskInstanceById(idTask).getProcessInstance().getContextInstance().getVariable(Variaveis.MINUTA_EM_ELABORACAO);
	 	if(arrayMinutas == null){
	 		return Collections.emptyList();
	 	}
	 	List<Integer> ret = new ArrayList<Integer>();
	 	if(arrayMinutas instanceof Integer){
			ret.add((Integer) arrayMinutas);
		}else if(arrayMinutas instanceof String){
			String[] ids = String.valueOf(arrayMinutas).split(",");
			if (ids != null && ids.length > 0){
				for (String  id : ids){
					ret.add(Integer.valueOf(id))	;
				}
			}
		}
		return ret;
	}

	public Transition getTransitionByName(TaskInstance taskInstance, String transitionName){
		List<Transition> availableTransitions = taskInstance.getToken().getNode().getLeavingTransitions();
		for (Transition transition : availableTransitions){
			if (transitionName != null && transitionName.equals(transition.getName())) {
				return transition;
			}
		}
		return null;
	}

	public boolean liberaCertificacao(ProcessoDocumento processoDocumento){
		return processoDocumentoManager.liberaCertificacao(processoDocumento) != null;
	}
		
	public Integer getAgrupamento(TipoProcessoDocumento tipoProcessoDocumento) {
		Integer idAgrupamento = null;

		if (tipoProcessoDocumento != null && tipoProcessoDocumento.getAgrupamento() != null && tipoProcessoDocumento.getAgrupamento().getIdAgrupamento() > 0) {
			idAgrupamento = tipoProcessoDocumento.getAgrupamento().getIdAgrupamento();
		}
		return idAgrupamento;
	}
	
	public String obtemModeloDocumento(){
		ModeloDocumento modeloDocumentoLocalTemp = processoDocumentoHome.getModeloDocumentoLocalTemp();
		String modeloDocumento = null;

		if (modeloDocumentoLocalTemp != null) {
			modeloDocumento = modeloDocumentoLocalTemp.getModeloDocumento();
		}

		return modeloDocumento;
	}
	
	public void setTipoDocumentoAssinarLote(String tipos) {
		Long idTask = TaskInstanceHome.instance().getTaskId();
		List<Integer> tiposDocumentos = new ArrayList<Integer>();
		String[] arrayTipos = null;
		
		if (tipos != null && !"".equals(tipos)) {
			arrayTipos = tipos.split(",");
		}
		if (arrayTipos != null && arrayTipos.length > 0) {
 			for (String  i : arrayTipos) {
				tiposDocumentos.add(Integer.valueOf(i))	;
			}
		}
		if (idTask != null) {
			String listaDocumentos = obterIdDocumentoTipoTarefa(idTask, tiposDocumentos, idTask) ;
			tramitacaoProcessualService.gravaVariavel(Variaveis.MINUTA_EM_ELABORACAO, listaDocumentos);	
		}
	}
	
	@SuppressWarnings("unchecked")
	private String obterIdDocumentoTipoTarefa(Long idTask, List<Integer> idTipoDocumento, Long idTaskAtual){
 		StringBuilder retorno = new StringBuilder();
  		StringBuilder sb = new StringBuilder();
  		String retornoString = "";
 		
		sb.append("select o from ProcessoDocumento o where ");
		sb.append("o.processo.idProcesso = :idProcesso ");
		sb.append(" and o.processoDocumento.tipoProcessoDocumento.idTipoProcessoDocumento in (:tipos) ");
		sb.append(" and o.processoDocumento.ativo = true ");
	 
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcesso", ProcessoHome.instance().getInstance().getIdProcesso());
		q.setParameter("tipos", idTipoDocumento);

		List<ProcessoDocumento> listaDocumento = q.getResultList();
 		
  		if (listaDocumento != null && listaDocumento.size() > 0)
		{
			Usuario usuario = Authenticator.getUsuarioLogado();
			Iterator<ProcessoDocumento> it = listaDocumento.iterator();
			
			while (it.hasNext())
			{
				ProcessoDocumento documento = it.next();
				ProcessoDocumentoBinPessoaAssinatura pdpa = processoDocumentoManager.pessoaDocumentoAssinado(documento.getProcessoDocumentoBin().getIdProcessoDocumentoBin());
				if (pdpa != null && pdpa.getPessoa().getIdUsuario().equals(usuario.getIdUsuario())) 
				{
					it.remove();
				}
			 	retorno.append(documento.getIdProcessoDocumento());
			 	retorno.append(",");
			}
			if (retorno != null && !"".equals(retorno.toString()))
			{
				retornoString = retorno.substring(0, retorno.length()-1)	;
			}
			
		}
 		return retornoString;
	}
	
	public ProcessoDocumentoBinPessoaAssinatura verificaAssinatura(ProcessoDocumento processoDocumento){
		if (processoDocumento != null) {
			return processoDocumentoManager.pessoaDocumentoAssinado(processoDocumento.getProcessoDocumentoBin().getIdProcessoDocumentoBin());
		}
		return null;
	}
}
