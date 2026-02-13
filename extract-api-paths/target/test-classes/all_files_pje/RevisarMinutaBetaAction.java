/**
 * 
 */
package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jbpm.graph.exe.ProcessInstance;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.component.tree.EventsHomologarMovimentosTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.list.ResultadoSentencaParteList;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.csjt.pje.business.service.ResultadoSentencaService;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

/**
 * @author cristof
 * 
 */
@Name(RevisarMinutaBetaAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class RevisarMinutaBetaAction implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8607415572367359198L;
	
	public static final String NAME = "revisarMinutaBetaAction";

	@In(create = true)
	private transient DocumentoJudicialService documentoJudicialService;

	@In(create = true, required = true)
	private transient ProcessoJudicialManager processoJudicialManager;

	@In(create = false, required = true)
	private FacesMessages facesMessages;

	@In(create = false, required = true)
	private TaskInstanceHome taskInstanceHome;

	@In(create = true, required = true)
	private transient TaskInstanceUtil taskInstanceUtil;
	
	@In
	private Expressions expressions;
	
	private boolean edicao;
	
	private String transicaoSaida;

	private ProcessoDocumento processoDocumento;

	private ModeloDocumento modeloDocumento;

	private Integer idMinuta;
	
	/**
 	* Indica que ao assinar o ato, as movimenta√ß√µes devem ser tamb√©m lan√ßadas.
 	*/
	private boolean lancarMovimentoComAssinatura = false;
	
	/**
	 * PJEII-3000
	 * Cria√ß√£o do atributo para fazer consultas ao servico de resultado de sentenca.
	 */
	private ResultadoSentencaService resultadoSentencaService = ComponentUtil
			.getComponent(ResultadoSentencaService.NAME);

	private boolean operacaoRealizadaComSucesso = false;
	
	@Create
	public void load() throws Exception {
		ProcessInstance pi = taskInstanceUtil.getProcessInstance();
		edicao = Boolean.parseBoolean(((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest()).getParameter("edicao"));
		idMinuta = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(TaskInstance.instance());

		if (idMinuta != null) {
			processoDocumento = documentoJudicialService.getDocumento(idMinuta);
			if (processoDocumento != null && !processoDocumento.getAtivo()) {
				JbpmUtil.instance().apagaMinutaEmElaboracao(TaskInstance.instance());
				processoDocumento = documentoJudicialService.getDocumento();
			}
		} 
		if (idMinuta == null || processoDocumento == null){
			idMinuta = null;
			processoDocumento = documentoJudicialService.getDocumento();
		}
		transicaoSaida = (String)taskInstanceUtil.getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		String lancarNaAssinatura = (String) pi.getContextInstance().getVariable(Variaveis.VARIABLE_CONDICAO_LANCAMENTO_MOVIMENTOS_TEMPORARIO);
		if(lancarNaAssinatura != null && !lancarNaAssinatura.isEmpty()){
			try{
				lancarMovimentoComAssinatura = !((Boolean) expressions.createValueExpression(lancarNaAssinatura).getValue());
			}catch (Exception e) {
				if(lancarNaAssinatura.equalsIgnoreCase("true")){
					lancarMovimentoComAssinatura = false;
				}
			}
		}
        //se houver arquivos anexados ainda n√£o persistidos, adicion√°-los √† lista para exibi√ß√£o em tela.		
        if (processoDocumento.getDocumentosVinculados() != null && !processoDocumento.getDocumentosVinculados().isEmpty()){
            ProcessoHome.instance().getProtocolarDocumentoBean().getArquivos().clear();
            ProcessoHome.instance().getProtocolarDocumentoBean().getArquivos().addAll(processoDocumento.getDocumentosVinculados());
        }
		
		ResultadoSentencaParteList list = ComponentUtil.getComponent(ResultadoSentencaParteList.NAME);
		list.setMostraSentencas(true);
	}
	
	public ProcessoDocumento getUltimoAto() throws Exception{
		ProcessInstance pi = taskInstanceUtil.getProcessInstance();
		if(pi != null) {
			Integer idAto = (Integer) pi.getContextInstance().getVariable(Variaveis.ATO_PROFERIDO);
			if (idAto != null) {
				return documentoJudicialService.getDocumento(idAto);
			} else {
				ProcessoTrf processoJudicial = processoJudicialManager.findByProcessInstance(pi);
				return documentoJudicialService.getUltimoAtoJudicial(processoJudicial.getProcesso());
			}
		}
		return null;
	}

	/**
	 * PJEII-3000
	 * Valida√ß√£o para apresenta√ß√£o ou n√£o do bot√£o 'Registrar Resultado Senten√ßa'
	 * @return Boolean
	 */
	public Boolean getRenderedRegistrarResultadoSentenca() {
		/**
		 * PJEII-9246 PJE-JT Antonio Lucas 01/07/2013
		 * l√≥gica duplicada removida
		 */
		return resultadoSentencaService.getRenderedRegistrarResultadoSentenca();
	}
	
	/**
	 * PJEII-3000
	 * Valida√ß√£o para verificar se j√° possui uma senten√ßa n√£o homologada.
	 * Caso haja, apresenta o nome do bot√£o 'Editar Resultado da Senten√ßa'
	 * Caso n√£o, apresenta o nome do bot√£o 'Registrar Resultado da Senten√ßa' 
	 * @return Boolean
	 */
	public Boolean getPossuiSentencaNaoHomologada() {
		return resultadoSentencaService.getPossuiSentencaNaoHomologada();
	}
	
	/**
	 * PJEII-3000
	 * Valida√ß√£o para verificar se o Resultado da Sentan√ßa foi registrado.
	 * @return Boolean
	 * @deprecated mesma logica do {@link #getPossuiSentencaNaoHomologada}
	 */
	@Deprecated
	public Boolean isResultadoSentencaRegistrado() {
		return getPossuiSentencaNaoHomologada();
		/**
		 * PJEII-9246
		 * l√≥gica repetida e desnecess√°ria
		 * Ao gravar o resultadoSentenca ja grava o resultadoSentencaParte
		 * PJE-JT Antonio Lucas 01/07/2013
		 */
	}
	
	/**
	 * PJEII-3000
	 * Valida√ß√£o para verificar se o Resultado da Sentan√ßa foi registrado.
	 * @return Boolean
	 * @deprecated mesma logica do {@link #getPossuiSentencaNaoHomologada}
	 */
	@Deprecated
	public Boolean getResultadoSentencaRegistrado() {
		return this.isResultadoSentencaRegistrado();
	}
	
	public boolean isManaged() {
		return idMinuta != null;
	}

	public List<ModeloDocumento> getModelosDisponiveis() {
		try {
			return this.documentoJudicialService.getModelosDisponiveis();
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, "N„o foi possÌ≠vel obter os modelos disponÌ≠veis [{0}:{1}]", e.getClass()
					.getCanonicalName(), e.getLocalizedMessage());
			return new ArrayList<ModeloDocumento>(0);
		}
	}

	public List<TipoProcessoDocumento> getTiposDisponiveis() {
		try {
			return this.documentoJudicialService.getTiposDisponiveis();
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, "N„o foi possÌvel obter os tipos de documentos disponÌ≠veis [{0}:{1}]", e
					.getClass().getCanonicalName(), e.getLocalizedMessage());
			return new ArrayList<TipoProcessoDocumento>(0);
		}
	}

	@Begin(join = true)
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	public ModeloDocumento getModeloDocumento() {
		return modeloDocumento;
	}

	public void setModeloDocumento(ModeloDocumento modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	public void gravarAlteracoes() {
		try {
			ProcessInstance pi = taskInstanceUtil.getProcessInstance();
			if (idMinuta == null) {
				Integer procId = (Integer) pi.getContextInstance().getVariable(Variaveis.VARIAVEL_PROCESSO);
				ProcessoTrf processoJudicial = this.processoJudicialManager.findById(procId);
				processoDocumento = documentoJudicialService.persist(processoDocumento, processoJudicial,
						true);
				ProcessoHome.instance().getProtocolarDocumentoBean().setDocumentoPrincipal(processoDocumento);
				idMinuta = processoDocumento.getIdProcessoDocumento();
				pi.getContextInstance().setVariable(Variaveis.MINUTA_EM_ELABORACAO, idMinuta);
			} else {
				processoDocumento = documentoJudicialService.persist(processoDocumento, true);
			}
			if(lancarMovimentoComAssinatura){
				LancadorMovimentosService.instance().setMovimentosTemporarios(pi, EventsHomologarMovimentosTreeHandler.instance().getEventoBeanList());
			}
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "N„o foi possÌvel gravar o documento [{0}:{1}]", e.getClass()
					.getCanonicalName(), e.getLocalizedMessage());
		} catch (PJeDAOException e) {
			facesMessages.add(Severity.ERROR, "N„o foi possÌ≠vel gravar o documento [{0}:{1}]", e.getClass()
					.getCanonicalName(), e.getLocalizedMessage());
		}
	}

	public void descartarAlteracoes() {
		try {
			documentoJudicialService.refresh(processoDocumento);
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "N„o foi possÌ≠vel recarregar o documento [{0}:{1}]", e.getClass()
					.getCanonicalName(), e.getLocalizedMessage());
		} catch (PJeDAOException e) {
			facesMessages.add(Severity.ERROR, "N„o foi possÌ≠vel recarregar o documento [{0}:{1}]", e.getClass()
					.getCanonicalName(), e.getLocalizedMessage());
		}
	}

	public void substituirModelo() {
		this.documentoJudicialService.substituirModelo(this.processoDocumento, this.modeloDocumento);
	}

	public void finalizarDocumento() {
		try {
			ProcessInstance pi = taskInstanceUtil.getProcessInstance();
			if (!processoDocumento.getProcessoDocumentoBin().getSignatarios().isEmpty()){
				ProcessoTrf processoJudicial = this.processoJudicialManager.findByProcessInstance(pi);
				Pessoa signatario = ((PessoaService) Component.getInstance("pessoaService")).findById(Authenticator.getUsuarioLogado().getIdUsuario());
				this.documentoJudicialService.finalizaDocumento(processoDocumento, processoJudicial, taskInstanceHome.getTaskId(), true, false, false, signatario, true);
				if (this.transicaoSaida != null) {
					Integer idMinuta = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(TaskInstance.instance());

					ProcessoHome.instance().setIdProcessoDocumento(processoDocumento.getIdProcessoDocumento());
					pi.getContextInstance().setVariable(Variaveis.ATO_PROFERIDO, idMinuta);
					pi.getContextInstance().setVariable(Variaveis.ULTIMO_DOCUMENTO_JUNTADO_NESTE_FLUXO, idMinuta);
					taskInstanceHome.end(transicaoSaida);

					JbpmUtil.instance().apagaMinutaEmElaboracao(TaskInstance.instance());
					ProcessoHome.instance().getProtocolarDocumentoBean().getArquivos().clear();
					if(lancarMovimentoComAssinatura){
						LancadorMovimentosService.instance().apagarMovimentosTemporarios(pi);
						EventsHomologarMovimentosTreeHandler.instance().clearList();
						EventsHomologarMovimentosTreeHandler.instance().clearTree();
					}
				}
			} else {
				facesMessages.add(Severity.INFO, "O documento n„o foi selecionado!");
				return;
			}
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "N„o foi possÌvel finalizar o documento. {0}: {1}.", e.getClass()
					.getCanonicalName(), e.getLocalizedMessage());
		} catch (PJeDAOException e) {
			facesMessages.add(Severity.ERROR, "N„o foi possÌvel finalizar o documento. {0}: {1}.", e.getClass()
					.getCanonicalName(), e.getLocalizedMessage());
		}
	}
	
	public boolean liberaCertificacao() {
		return ProcessoDocumentoHome.instance().liberaCertificacao(processoDocumento);
	}
	
	public boolean isAppletAssinaturaRendered() {
		boolean rendered = false;
		if(liberaCertificacao()) {
			rendered = true;

			if(ParametroJtUtil.instance().justicaTrabalho()) {
				if (processoDocumento != null && processoDocumento.getTipoProcessoDocumento() != null && ParametroUtil.instance().getTipoProcessoDocumentoSentenca() != null) {
					
					if(processoDocumento.getTipoProcessoDocumento().getIdTipoProcessoDocumento() == ParametroUtil.instance().getTipoProcessoDocumentoSentenca().getIdTipoProcessoDocumento() &&
							!getResultadoSentencaRegistrado()) {
						rendered = false;
					}
				}
			}
			
			//se houver algum anexo sem tipo de documento informado, n√£o renderizar o bot√£o de assinatura
			for (ProcessoDocumento pd : ProcessoHome.instance().getProtocolarDocumentoBean().getArquivos()) {
			    if (pd.getTipoProcessoDocumento() == null){
			        rendered = false;
			        break;
			    }
            }
			
		}
		return rendered;
	}

	public boolean isOperacaoRealizadaComSucesso() {
		return operacaoRealizadaComSucesso;
	}

	public void setOperacaoRealizadaComSucesso(boolean operacaoRealizadaComSucesso) {
		this.operacaoRealizadaComSucesso = operacaoRealizadaComSucesso;
	}
	public boolean isEdicao() {
		return edicao;
	}
	
	public void setEdicao(boolean edicao) {
		this.edicao = edicao;
	}
}
