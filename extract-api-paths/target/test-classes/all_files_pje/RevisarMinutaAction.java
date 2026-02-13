package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.transaction.Transaction;
import org.jbpm.graph.exe.ProcessInstance;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.ibpm.component.tree.EventoBean;
import br.com.infox.ibpm.component.tree.EventsHomologarMovimentosTreeHandler;
import br.com.infox.ibpm.component.tree.EventsTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.list.ResultadoSentencaParteList;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.TipoProcessoDocumentoPapelService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.view.AjaxDataUtil;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.csjt.pje.business.service.ResultadoSentencaService;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

/**
 * @author cristof
 * 
 */
@Name(RevisarMinutaAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class RevisarMinutaAction implements Serializable, ArquivoAssinadoUploader {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "revisarMinutaAction";

	@In(create = true)
	private transient DocumentoJudicialService documentoJudicialService;
	
	@In
	private TramitacaoProcessualService tramitacaoProcessualService;

	@In(create = true, required = true)
	private transient ProcessoJudicialManager processoJudicialManager;

	@In(create = false, required = true)
	private FacesMessages facesMessages;

	@In(create = false, required = true)
	private TaskInstanceHome taskInstanceHome;

	@In(create = true, required = true)
	private transient TaskInstanceUtil taskInstanceUtil;
	
    @In
    private transient ProcessoDocumentoManager processoDocumentoManager;
	
	@In
	private Expressions expressions;
	
	@In
    private TipoProcessoDocumentoPapelService tipoProcessoDocumentoPapelService;
	
	private String transicaoSaida;

	private ModeloDocumento modeloDocumento;
	
	private boolean conteudoAlterado = false;
	
	private boolean conteudoVazio = true;

	@RequestParameter
	private Boolean iframe;

	@RequestParameter
	private Boolean edicao;
	/**
 	* Indica que ao assinar o ato, as movimentações devem ser também lançadas.
 	*/
	private boolean lancarMovimentoComAssinatura = false;
	
	/**
	 * PJEII-3000
	 * Criação do atributo para fazer consultas ao servico de resultado de sentenca.
	 */
	private ResultadoSentencaService resultadoSentencaService = ComponentUtil.getComponent(ResultadoSentencaService.NAME);
	
	private ProtocolarDocumentoBean protocolarDocumentoBean;

	/**
	 * Define o processo da task pendente
	 */
	private ProcessoTrf processoTrf;
	
	/**
	 * Indica qual o identificador desta action, caso na tela seja renderizadas várias destas actions, consegue-se saber qual é esta
	 * Este valor é utilizado para indicar à TaskInstanceHome o estado desta action 
	 */
	private String actionInstanceId;
		
	@In(create=true)
	private AjaxDataUtil ajaxDataUtil;
	
	@Create
	public void load() throws Exception {
		this.geraActionInstanceId();
		ProcessInstance pi = taskInstanceUtil.getProcessInstance();
						
		recuperarProcessoTrf(pi);
		
		protocolarDocumentoBean = new ProtocolarDocumentoBean(
				this.processoTrf.getIdProcessoTrf(),
				ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL | ProtocolarDocumentoBean.RECUPERA_DOCUMENTO_FLUXO,
				getActionName());
		
		ProcessoDocumento minutaEmElaboracao = recuperarMinutaEmElaboracao(pi, TaskInstance.instance());
		
		List<TipoProcessoDocumento> tiposDocumentoDisponiveis = documentoJudicialService.getTiposDocumentoMinuta();

		if (minutaEmElaboracao.getTipoProcessoDocumento() == null || !tiposDocumentoDisponiveis.contains(minutaEmElaboracao.getTipoProcessoDocumento())) {
			minutaEmElaboracao.setTipoProcessoDocumento(tiposDocumentoDisponiveis.size() == 1 ? tiposDocumentoDisponiveis.get(0) : null);
			onSelectProcessoDocumento(minutaEmElaboracao.getTipoProcessoDocumento());
		}
		
		getProtocolarDocumentoBean().setDocumentoPrincipal(minutaEmElaboracao);

		this.setConteudoVazio(
					!this.isManaged() 
					|| minutaEmElaboracao.getProcessoDocumentoBin().getModeloDocumento() == null
					|| minutaEmElaboracao.getProcessoDocumentoBin().getModeloDocumento().trim().isEmpty()
				);
		
		transicaoSaida = (String)taskInstanceUtil.getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		lancarMovimentoComAssinatura = !LancadorMovimentosService.instance().deveGravarTemporariamente();

		// Se houver arquivos anexados ainda não persistidos, adicioná-los à lista para exibição em tela.		
        if (minutaEmElaboracao.getDocumentosVinculados() != null && !minutaEmElaboracao.getDocumentosVinculados().isEmpty()){
            getProtocolarDocumentoBean().getArquivos().clear();
            getProtocolarDocumentoBean().getArquivos().addAll(minutaEmElaboracao.getDocumentosVinculados());
        }
		
		ResultadoSentencaParteList list = ComponentUtil.getComponent(ResultadoSentencaParteList.NAME);
		list.setMostraSentencas(true);
		
		this.validarAction();
	}
	
	public ProcessoDocumento recuperarMinutaEmElaboracao(ProcessInstance pi, org.jbpm.taskmgmt.exe.TaskInstance taskInstance) throws PJeBusinessException {
		ProcessoDocumento processoDocumento = null;
		
		Integer idMinuta = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(taskInstance);

		if (idMinuta != null) {
            processoDocumento = documentoJudicialService.getDocumento(idMinuta);
                        
            if (processoDocumento != null && !processoDocumento.getAtivo()) {
                JbpmUtil.instance().apagaMinutaEmElaboracao(taskInstance);
                processoDocumento = documentoJudicialService.getDocumento();
            }
        } else {
            if (ProcessoDocumentoHome.instance().getInstance().getProcessoDocumentoBin() != null) {
                processoDocumento = ProcessoDocumentoHome.instance().getInstance();
            }
        }
		
		if (processoDocumento == null){
			processoDocumento = documentoJudicialService.getDocumento();
		}
		
		return processoDocumento;
	}

	private void recuperarProcessoTrf(ProcessInstance pi) throws PJeBusinessException {
		Integer procId = (Integer) pi.getContextInstance().getVariable(Variaveis.VARIAVEL_PROCESSO);			
		processoTrf = this.processoJudicialManager.findById(procId);
	}
	
	/**
	 * Retorna o último documento do tipo Comunicação entre instâncias.
	 * 
	 * @return ProcessoDocumento do tipo Comunicação entre instâncias.
	 * @throws Exception
	 */
	public ProcessoDocumento getUltimaComunicacao() throws Exception{
		ProcessoDocumento documento = FluxoEL.instance().getAtoProferido();
		
		if (documento == null) {
			ProcessoTrf processoJudicial = FluxoEL.instance().getProcessoTrf();
			documento = documentoJudicialService.getUltimaComunicacao(processoJudicial.getProcesso());
		}
		return documento;
	}

	/**
	 * PJEII-3000
	 * Validação para apresentação ou não do botão 'Registrar Resultado Sentença'
	 * @return Boolean
	 */
	public Boolean getRenderedRegistrarResultadoSentenca() {
		/**
		 * PJEII-9246 PJE-JT Antonio Lucas 01/07/2013
		 * lógica duplicada removida
		 */
		return resultadoSentencaService.getRenderedRegistrarResultadoSentenca();
	}
	
	/**
	 * PJEII-3000
	 * Validação para verificar se já possui uma sentença não homologada.
	 * Caso haja, apresenta o nome do botão 'Editar Resultado da Sentença'
	 * Caso não, apresenta o nome do botão 'Registrar Resultado da Sentença' 
	 * @return Boolean
	 */
	public Boolean getPossuiSentencaNaoHomologada() {
		return resultadoSentencaService.getPossuiSentencaNaoHomologada();
	}
	
	/**
	 * PJEII-3000
	 * Validação para verificar se o Resultado da Sentança foi registrado.
	 * @return Boolean
	 * @deprecated mesma logica do {@link #getPossuiSentencaNaoHomologada}
	 */
	@Deprecated
	public Boolean isResultadoSentencaRegistrado() {
		return getPossuiSentencaNaoHomologada();
		/**
		 * PJEII-9246
		 * lógica repetida e desnecessária
		 * Ao gravar o resultadoSentenca ja grava o resultadoSentencaParte
		 * PJE-JT Antonio Lucas 01/07/2013
		 */
	}
	
	/**
	 * PJEII-3000
	 * Validação para verificar se o Resultado da Sentança foi registrado.
	 * @return Boolean
	 * @deprecated mesma logica do {@link #getPossuiSentencaNaoHomologada}
	 */
	@Deprecated
	public Boolean getResultadoSentencaRegistrado() {
		return this.isResultadoSentencaRegistrado();
	}
		
	public boolean isConteudoVazio() {
		return conteudoVazio;
	}

	public void setConteudoVazio(boolean conteudoVazio) {
		this.conteudoVazio = conteudoVazio;
		this.validarAction();
	}

	public boolean isConteudoAlterado() {
		return conteudoAlterado;
	}

	public void setConteudoAlterado(boolean conteudoAlterado) {
		this.conteudoAlterado = conteudoAlterado;
		this.validarAction();
	}

	public boolean isManaged() {
		return getProcessoDocumento().getIdProcessoDocumento() > 0;
	}

	public List<ModeloDocumento> getModelosDisponiveis() {
		try {
			return this.documentoJudicialService.getModelosDisponiveis();
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, "Não foi possível obter os modelos disponíveis [{0}:{1}]", e.getClass()
					.getCanonicalName(), e.getLocalizedMessage());
			return new ArrayList<ModeloDocumento>(0);
		}
	}

	public List<TipoProcessoDocumento> getTiposDisponiveis() {
		try {
			return this.documentoJudicialService.getTiposDisponiveis();
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, "Não foi possível obter os tipos de documentos disponíveis [{0}:{1}]", e
					.getClass().getCanonicalName(), e.getLocalizedMessage());
			return new ArrayList<TipoProcessoDocumento>(0);
		}
	}

	public ModeloDocumento getModeloDocumento() {
		return modeloDocumento;
	}

	public void setModeloDocumento(ModeloDocumento modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
		this.validarAction();
	}

	public void gravarAlteracoes() {
				
		try {
			ProcessInstance pi = taskInstanceUtil.getProcessInstance();
			
			ProcessoDocumento processoDocumento = getProtocolarDocumentoBean().getDocumentoPrincipal();
			processoDocumento.setExclusivoAtividadeEspecifica(true);
			
			if (processoDocumento.getIdProcessoDocumento() == 0) {
				processoDocumento = documentoJudicialService.persist(processoDocumento, processoTrf, true);
				pi.getContextInstance().setVariable(Variaveis.MINUTA_EM_ELABORACAO, processoDocumento.getIdProcessoDocumento());
			} 
			else {
				processoDocumento = documentoJudicialService.persist(processoDocumento, true);
			}
			documentoJudicialService.flush();
			
			getProtocolarDocumentoBean().setDocumentoPrincipal(processoDocumento);
			
			if (lancarMovimentoComAssinatura){
				LancadorMovimentosService.instance().setMovimentosTemporarios(pi, EventsHomologarMovimentosTreeHandler.instance().getEventoBeanList());
			}

			this.setConteudoAlterado(false);
			this.ajaxDataUtil.sucesso();
			this.validarAction();
		}
		catch (Exception e) {
			
			this.ajaxDataUtil.erro();
			
			facesMessages.add(Severity.ERROR, "Não foi possível gravar o documento [{0}:{1}]", e.getClass().getCanonicalName(), e.getLocalizedMessage());
		}
	}

	public void descartarAlteracoes() {
		try {
			documentoJudicialService.refresh(getProtocolarDocumentoBean().getDocumentoPrincipal());
			this.setConteudoAlterado(false);
			this.validarAction();
		} 
		catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Não foi possível recarregar o documento [{0}:{1}]", e.getClass().getCanonicalName(), e.getLocalizedMessage());
		} 
		catch (PJeDAOException e) {
			facesMessages.add(Severity.ERROR, "Não foi possível recarregar o documento [{0}:{1}]", e.getClass().getCanonicalName(), e.getLocalizedMessage());
		}
	}

	public void substituirModelo() {
		this.documentoJudicialService.substituirModelo(getProtocolarDocumentoBean().getDocumentoPrincipal(), this.modeloDocumento);
		this.validarAction();
	}

	public void finalizarDocumento() {
		try {
			ProcessInstance pi = taskInstanceUtil.getProcessInstance();
			if (!getProcessoDocumento().getProcessoDocumentoBin().getSignatarios().isEmpty()){
				ProcessoTrf processoJudicial = this.processoJudicialManager.findByProcessInstance(pi);
				Pessoa signatario = ((PessoaService) Component.getInstance("pessoaService")).findById(Authenticator.getUsuarioLogado().getIdUsuario());
				this.documentoJudicialService.finalizaDocumento(getProcessoDocumento(), processoJudicial, taskInstanceHome.getTaskId(), true, false, false, signatario, true);
				
				this.validarAction();
				if (this.transicaoSaida != null) {
					ProcessoHome.instance().setIdProcessoDocumento(getProcessoDocumento().getIdProcessoDocumento());
					pi.getContextInstance().setVariable(Variaveis.VARIAVEL_DOCUMENTO_TEXT_EDITOR_ASSINATURA_APOS_ASSINATURA, pi.getContextInstance().getVariable(Variaveis.VARIAVEL_DOCUMENTO_TEXT_EDITOR_ASSINATURA));
					taskInstanceHome.end(transicaoSaida);
					pi.getContextInstance().deleteVariable(Variaveis.VARIAVEL_DOCUMENTO_TEXT_EDITOR_ASSINATURA);
					getProtocolarDocumentoBean().getArquivos().clear();
					if(lancarMovimentoComAssinatura){
						LancadorMovimentosService.instance().apagarMovimentosTemporarios(pi);
						EventsHomologarMovimentosTreeHandler.instance().clearList();
						EventsHomologarMovimentosTreeHandler.instance().clearTree();
					}
				}
			} else {
				this.validarAction();
				facesMessages.add(Severity.INFO, "O documento não foi assinado!");
				return;
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, "Não foi possível finalizar o documento. {0}: {1}.", e.getClass()
					.getCanonicalName(), e.getLocalizedMessage());
		} catch (PJeDAOException e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, "Não foi possível finalizar o documento. {0}: {1}.", e.getClass()
					.getCanonicalName(), e.getLocalizedMessage());
		}
	}
	
	public boolean liberaCertificacao() {
		return ProcessoDocumentoHome.instance().liberaCertificacao(getProcessoDocumento());
	}
	
	public boolean isAppletAssinaturaRendered() {
		if(iframe != null && iframe && edicao != null){
			return false;
		}
		boolean rendered = false;
		if(liberaCertificacao()) {
			rendered = true;

			// Se houver algum anexo sem tipo de documento informado, não renderizar o botão de assinatura
			for (ProcessoDocumento pd : getProtocolarDocumentoBean().getArquivos()) {
			    if (pd.getTipoProcessoDocumento() == null){
			        rendered = false;
			        break;
			    }
            }
		}
		
		if (!"true".equals((String)taskInstanceUtil.getVariable(Variaveis.NAO_EXIGIR_MOVIMENTOS_PARA_ASSINAR))){
			if (!EventsHomologarMovimentosTreeHandler.instance().validacoesAntesLancamento()){
				rendered = false;
			}
		}
		
		return rendered;
	}
	
	/**
 	 * Metodo que verifica se  obrigatrio a assinatura pelo tipo processo documento 
 	 * e papel do usurio logado, verificando se o tipo de documento possui um agrupador de movimentos relacionado e há algum movimento selecinado
 	 * 
 	 * @return True se pode assinar
 	 */
	public boolean isHabilitaBotaoAssinar(){
 		return isManaged() && documentoJudicialService.verificaPossibilidadeAssinatura(
 				getProcessoDocumento(), 
 				Authenticator.getUsuarioLogado(), 
 				EventsHomologarMovimentosTreeHandler.instance().getEventoBeanList(), 
 				Authenticator.getPapelAtual()).getResultado();
	}
 	
 	/**
 	 * Verifica se tem permissao para assinatr o documento, desconsiderando a exigibilidade de movimentos
 	 * @return
 	 */
 	public boolean isPossuiPermissaoAssinatura() {
 		return isManaged() && documentoJudicialService.verificaPossibilidadeAssinaturaSemMovimentos(
 				getProcessoDocumento(), 
 				Authenticator.getUsuarioLogado(), 
 				Authenticator.getPapelAtual()).getResultado();
 	}
	
	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}
	
	public ProcessoDocumento getProcessoDocumento() {
		return getProtocolarDocumentoBean().getDocumentoPrincipal();
	}
	
	public List<ProcessoDocumento> getDocumentosVinculados(){
		return new ArrayList<ProcessoDocumento>(getProcessoDocumento().getDocumentosVinculados());
	}
	
	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) {
		this.protocolarDocumentoBean.addArquivoAssinado(arquivoAssinadoHash);
	}
	
	/**
	 * Apos a assinatura dos arquivos o assinador ira executar este metodo via ajax
	 */
	public void concluir() {

		try {					
			// Guarda a referencia ao processoDocumento
			ProcessoDocumento processoDocumento = getProcessoDocumento();

			this.protocolarDocumentoBean.concluirAssinaturaAction();			
			
			// Atualiza o processoDocumento
			this.documentoJudicialService.refresh(processoDocumento);
			
			Long taskId = taskInstanceHome.getTaskId();
			org.jbpm.taskmgmt.exe.TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(taskId);
			
			if (processoDocumento.getProcessoDocumentoBin().getSignatarios().isEmpty()){
				throw new Exception("O documento não foi assinado!");
			}
			
			Pessoa signatario = ((PessoaService) Component.getInstance("pessoaService")).findById(Authenticator.getUsuarioLogado().getIdUsuario());
			this.documentoJudicialService.finalizaDocumento(processoDocumento, processoTrf, taskId, true, false, false, signatario, true);
			
			ProcessoHome.instance().setIdProcessoDocumento(processoDocumento.getIdProcessoDocumento());
			
			ProcessInstance pi = taskInstance.getProcessInstance();
			Integer identificadorMinutaEmElaboracao = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(taskInstance);
			if(identificadorMinutaEmElaboracao != null) {
				if(processoDocumento.getTipoProcessoDocumento().getDocumentoAtoProferido()) {
					pi.getContextInstance().setVariable(Variaveis.ATO_PROFERIDO, identificadorMinutaEmElaboracao);
				}
			}
			
			pi.getContextInstance().setVariable(Variaveis.ULTIMO_DOCUMENTO_JUNTADO_NESTE_FLUXO, identificadorMinutaEmElaboracao);

			getProtocolarDocumentoBean().getArquivos().clear();

			LancadorMovimentosService.instance().lancarMovimentosTemporarios(pi);

			EventsHomologarMovimentosTreeHandler.instance().clearList();
			EventsHomologarMovimentosTreeHandler.instance().clearTree();
			
			EventsTreeHandler.instance().clearList();
			EventsTreeHandler.instance().clearTree();

			this.validarAction();
			if (this.transicaoSaida != null) {
				taskInstanceHome.end(transicaoSaida);
			}
			
			pi.getContextInstance().deleteVariable(Variaveis.MINUTA_EM_ELABORACAO);
			this.ajaxDataUtil.sucesso();
		}
		catch (Exception e) {
			
			this.ajaxDataUtil.erro();			
		
			try {
				Transaction.instance().rollback();
			}
			catch (Exception e1) {
				throw new RuntimeException(e1);	
			}
			
			facesMessages.clear();
			facesMessages.add(Severity.ERROR, e.getMessage());
		}
	}

	/**
	 * Realiza o download do documento para o usuário
	 * @param processoDocumento documento a ser baixado
	 */
	public void downloadDocumento(ProcessoDocumento processoDocumento) {
		try {
			processoDocumentoManager.downloadDocumento(processoDocumento);
		}
		catch (Exception e) {
			facesMessages.add(Severity.ERROR, e.getMessage());
		}
	}
	
	/**
	 * Recupera todos os movimentos 
	 * 
	 */
	public List<EventoBean> getMovimentosLancadosNaoHomologados(){
		return LancadorMovimentosService.instance().getMovimentosTemporarios(taskInstanceUtil.getProcessInstance());
	}
	
	
	@Override
	public String getActionName() {
		return NAME;
	}
	
	/**
	 * Verifica se existe algum agrupamento vinculado ao tipo de documento
	 * selecionado.
	 */
	public void onSelectProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		ProcessoHome.instance().onSelectProcessoDocumento(tipoProcessoDocumento, EventsHomologarMovimentosTreeHandler.instance());
		this.validarAction();
	}
	
	private void geraActionInstanceId() {
		actionInstanceId = this.getActionName();
		
	}

	public String getActionInstanceId() {
		return actionInstanceId;
	}

	public void setActionInstanceId(String actionInstanceId) {
		this.actionInstanceId = actionInstanceId;
	}
	
	public boolean validarAction() {
		boolean valido = (this.isManaged() && !this.isConteudoVazio() && !this.isConteudoAlterado());
		String mensagem = "";
		
		if(!valido) {
			if(!this.isManaged()) {
				mensagem = "Documento não foi salvo";
			}else if(this.isConteudoVazio()) {
				mensagem = "O documento está com o conteúdo vazio";
			}else if(this.isConteudoAlterado()) {
				mensagem = "Há informações não salvas no documento";
			}
		}
		this.gravaInformacaoValidacao(valido, mensagem);
		return valido;
	}
	
	private void gravaInformacaoValidacao(boolean valido, String mensagem) {
		TaskInstance.instance().setVariableLocally(Variaveis.PJE_PREFIXO_VARIAVEL_TAREFA_VALIDACAO_COMPONENTE_RESULTADO.concat(this.getActionInstanceId()), valido);
		TaskInstance.instance().setVariableLocally(Variaveis.PJE_PREFIXO_VARIAVEL_TAREFA_VALIDACAO_COMPONENTE_MENSAGEM.concat(this.getActionInstanceId()), mensagem);
	}
}
