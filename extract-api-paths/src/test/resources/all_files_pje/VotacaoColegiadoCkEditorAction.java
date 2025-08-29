package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.log.Log;
import org.jbpm.graph.exe.ExecutionContext;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import br.com.infox.cliente.home.ProcessoDocumentoBinHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProcessoJbpmUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.PdfUtil;
import br.com.jt.pje.manager.DerrubadaVotoManager;
import br.jus.cnj.fluxo.Validador;
import br.jus.cnj.fluxo.interfaces.TaskVariavelAction;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.nucleo.view.CkEditorGeraDocumentoAbstractAction;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.je.pje.business.dto.RespostaDTO;
import br.jus.je.pje.business.dto.RespostaTiposVotoDTO;
import br.jus.pje.je.enums.TipoDocumentoColegiadoEnum;
import br.jus.pje.jt.entidades.ControleVersaoDocumento;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Componente de controle da votao em colegiado utilizando o ckEditor. O frame respectivo está em
 * WEB-INF/xhtml/flx/votacaoColegiadoCkEditor.xhtml
 * 
 * @author eduardo.pereira
 * @author filipe.sousa
 * @since 2.0.1
 */
@Name(VotacaoColegiadoCkEditorAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class VotacaoColegiadoCkEditorAction extends CkEditorGeraDocumentoAbstractAction implements Serializable, TaskVariavelAction, ArquivoAssinadoUploader {

	private static final long serialVersionUID = -2223012830424818073L;

	public static final String NAME = "votacaoColegiadoCkEditorAction";
	
    private static final String EMENTA = ParametroUtil.instance().getTipoProcessoDocumentoEmenta().getTipoProcessoDocumento();
    
    private static final String RELATORIO = ParametroUtil.instance().getTipoProcessoDocumentoRelatorio().getTipoProcessoDocumento();
    
    private static final String VOTO = ParametroUtil.instance().getTipoProcessoDocumentoVoto().getTipoProcessoDocumento();
    
    private static final String OCULTA_DEFAULT_TRANSITION = "oculto";
    
    private static final String DEFAULT_TRANSITION_VOTO = "defaultTransitionVoto";
    
    private static final String DEFAULT_TRANSITION_EMENTA = "defaultTransitionEmenta";
    
    private static final String DEFAULT_TRANSITION_RELATORIO = "defaultTransitionRelatorio";    
	
	@Logger
	private transient Log logger;
	
	private Boolean minutando;

	private Boolean ocultaEmenta;
	
	private Boolean ocultaRelatorio;
	
	private Boolean ocultaVoto;
	
	private Boolean exibeProclamacaoJulgamento;
	
	private Boolean controlaLiberacao = true;
	
	private Boolean permiteAssinarRelatorio = true;
	
	private Boolean permiteAssinarVoto = false;
	
	private Boolean permiteAssinarEmenta = false;
	
	private String transicaoPadrao;
	
	private boolean ativaPluginTipoVoto;	

	private transient ArquivoAssinadoHash arquivoAssinado;
	
	private boolean uploadArquivoAssinadoRealizado;
	
	private boolean liberaRelatorio = false;
	
	private boolean liberaVoto = false;
	
	private boolean liberaEmenta = false;
	
	private boolean destacarVoto = false;

	private String minutaVoto = null;
	
	private Boolean exibeMinutaVoto = false;

	private boolean isSessaoExistente = true;
	
	private ProcessoDocumento ementa = null;
    
	private ProcessoDocumento relatorio = null;
	
	private ProcessoDocumento voto = null;

	private Sessao sessaoJulgamento;

	private transient DocumentoJudicialService documentoJudicialService = ComponentUtil.getDocumentoJudicialService();
	
    @In
    private TramitacaoProcessualService tramitacaoProcessualService;

	private SessaoProcessoDocumento sessaoProcessoDocumentoRelatorio;

	private SessaoProcessoDocumento sessaoProcessoDocumentoEmenta;

	private SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVotoRelator;
	
	private TipoVoto tipoVoto;
	
	private Sessao sessaoSugerida;	
	
	private static final String PREFIXO_SESSAO = "docs_";

	public boolean isLiberaRelatorio() {
		return liberaRelatorio;
	}

	public void setLiberaRelatorio(boolean liberaRelatorio) {
		this.liberaRelatorio = liberaRelatorio;
	}

	public boolean isLiberaVoto() {
		return liberaVoto;
	}

	public void setLiberaVoto(boolean liberaVoto) {
		this.liberaVoto = liberaVoto;
	}

	public boolean isLiberaEmenta() {
		return liberaEmenta;
	}

	public void setLiberaEmenta(boolean liberaEmenta) {
		this.liberaEmenta = liberaEmenta;
	}
	
	public boolean isDestacarVoto() {
		return destacarVoto;
	}

	public void setDestacarVoto(boolean destacarVoto) {
		this.destacarVoto = destacarVoto;
	}

	public boolean isAtivaPluginTipoVoto() {
		return ativaPluginTipoVoto;
	}

	public void setAtivaPluginTipoVoto(boolean ativaPluginTipoVoto) {
		this.ativaPluginTipoVoto = ativaPluginTipoVoto;
	}
	
	public String getTextoProclamacaoJulgamento() {
		return getSessaoProcessoDocumentoVotoRelator()==null ? null : getSessaoProcessoDocumentoVotoRelator().getTextoProclamacaoJulgamento();
	}
	
	public void setTextoProclamacaoJulgamento(String textoProclamacaoJulgamento) {
		if (getSessaoProcessoDocumentoVotoRelator()!=null)
			getSessaoProcessoDocumentoVotoRelator().setTextoProclamacaoJulgamento(textoProclamacaoJulgamento);
	}
	
	public void persistProclamacaoJulgamento() {
		ComponentUtil.getSessaoProcessoDocumentoVotoManager().mergeAndFlush(getSessaoProcessoDocumentoVotoRelator());
	}
	
	public void load(){
		inicializarDocumento();
	}

	@Create
	public void init() {
		tramitacaoProcessualService.gravaVariavelTarefa(Variaveis.MOSTRAR_BOTAO_SALVAR, false);
		
		sessaoJulgamento = ComponentUtil.getSessaoJulgamentoManager().getSessaoJulgamento(ProcessoJbpmUtil.getProcessoTrf());
		isSessaoExistente = sessaoJulgamento != null && sessaoJulgamento.getIdSessao() > 0;
		sessaoSugerida = ComponentUtil.getTramitacaoProcessualService().recuperaProcesso().getSessaoSugerida();
		inicializar();
		selecionarAbasDefault();
		
		logger.info("init");
		logger.info(JbpmUtil.instance().getNomeTarefaAnteriorFromCurrentExecutionContext());
		if (JbpmUtil.instance().getCurrentTransition()!=null) {
			logger.info(JbpmUtil.instance().getCurrentTransition().getName());
			logger.info(JbpmUtil.instance().getCurrentTransition().getTo()==null ? null : JbpmUtil.instance().getCurrentTransition().getTo().getFullyQualifiedName());
			logger.info(JbpmUtil.instance().getCurrentTransition().getProcessDefinition()==null ? null : JbpmUtil.instance().getCurrentTransition().getProcessDefinition().getName());
		}
	}
	
	public void inicializar() {
		carregarVariavelDefaultTransition();
		carregarVariaveisParametrizadas();
		recuperarDocumentosDoProcesso();
	}
	
	/**
	 * Verifica se o documento especificado está ou não em elaboração.
	 * @param spd 
	 * @return 
	 */
	private boolean isDocumentoEmElaboracao(SessaoProcessoDocumento spd) {
		if (spd==null)
			return false;
		ProcessoDocumento doc = spd.getProcessoDocumento();
		if (doc==null)
			return false;
		if (doc.getDataJuntada()!=null)
			return false;
		if (!Boolean.TRUE.equals(doc.getAtivo()))
			return false;
		if (ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(doc))
			return false;
		return true;
	}
	
	/**
	 * Inicia uma nova minuta.<br>
	 * Substitui os documentos que não estão em elaboração, por novos documentos em branco.
	 */
	public void iniciarNovaMinuta() {
		logger.info("setIniciarNovaMinutaDentro");
		logger.info(JbpmUtil.instance().getNomeTarefaAnteriorFromCurrentExecutionContext());
		if (JbpmUtil.instance().getCurrentTransition()!=null) {
			logger.info(JbpmUtil.instance().getCurrentTransition().getName());
			logger.info(JbpmUtil.instance().getCurrentTransition().getTo()==null ? null : JbpmUtil.instance().getCurrentTransition().getTo().getFullyQualifiedName());
			logger.info(JbpmUtil.instance().getCurrentTransition().getProcessDefinition()==null ? null : JbpmUtil.instance().getCurrentTransition().getProcessDefinition().getName());
		}
		if (ExecutionContext.currentExecutionContext()!=null) {
			logger.info(ExecutionContext.currentExecutionContext().getNode()==null ? null : ExecutionContext.currentExecutionContext().getNode().getFullyQualifiedName());
			logger.info(ExecutionContext.currentExecutionContext().getTask()==null ? null : ExecutionContext.currentExecutionContext().getTask().getName());
			logger.info(ExecutionContext.currentExecutionContext().getAction()==null ? null : ExecutionContext.currentExecutionContext().getAction().getName());
			logger.info(ExecutionContext.currentExecutionContext().getEvent()==null ? null : ExecutionContext.currentExecutionContext().getEvent().getEventType());
		}
		
		setArquivoAssinado(null);
		
		if (!isDocumentoEmElaboracao(this.sessaoProcessoDocumentoRelatorio)) {
			if (this.sessaoProcessoDocumentoRelatorio!=null)
				this.sessaoProcessoDocumentoRelatorio.setProcessoDocumento(null);
			setupSessaoProcessoDocumentoRelatorio(this.sessaoProcessoDocumentoRelatorio);
			this.sessaoProcessoDocumentoRelatorio.setLiberacao(false);
			setLiberaRelatorio(false);
			try {
				ComponentUtil.getSessaoProcessoDocumentoManager().persistSessaoProcessoDocumento(this.sessaoProcessoDocumentoRelatorio, ParametroUtil.instance().getTipoProcessoDocumentoRelatorio(), isLiberaRelatorio());
			} catch (PJeBusinessException ex) {
				logger.error(Level.SEVERE, "Erro ao salvar relatório.", ex);
			}
		}
		
		if (!isDocumentoEmElaboracao(this.sessaoProcessoDocumentoVotoRelator)) {
			if (this.sessaoProcessoDocumentoVotoRelator!=null)
				this.sessaoProcessoDocumentoVotoRelator.setProcessoDocumento(null);
			setupSessaoProcessoDocumentoVotoRelator(this.sessaoProcessoDocumentoVotoRelator);
			this.sessaoProcessoDocumentoVotoRelator.setLiberacao(false);
			this.sessaoProcessoDocumentoVotoRelator.setTextoProclamacaoJulgamento("");
			setLiberaVoto(false);
			setTipoVoto(null);
			try {
				ComponentUtil.getSessaoProcessoDocumentoManager().persistSessaoProcessoDocumento(this.sessaoProcessoDocumentoVotoRelator, ParametroUtil.instance().getTipoProcessoDocumentoVoto(), isLiberaVoto());
			} catch (PJeBusinessException ex) {
				logger.error(Level.SEVERE, "Erro ao salvar voto.", ex);
			}
		}
		
		if (!isDocumentoEmElaboracao(this.sessaoProcessoDocumentoEmenta)) {
			if (this.sessaoProcessoDocumentoEmenta!=null)
				this.sessaoProcessoDocumentoEmenta.setProcessoDocumento(null);
			setupSessaoProcessoDocumentoEmenta(this.sessaoProcessoDocumentoEmenta);
			this.sessaoProcessoDocumentoEmenta.setLiberacao(false);
			setLiberaEmenta(false);
			try {
				ComponentUtil.getSessaoProcessoDocumentoManager().persistSessaoProcessoDocumento(this.sessaoProcessoDocumentoEmenta, ParametroUtil.instance().getTipoProcessoDocumentoEmenta(), isLiberaEmenta());
			} catch (PJeBusinessException ex) {
				logger.error(Level.SEVERE, "Erro ao salvar ementa.", ex);
			}
		}
	}

	@Destroy
	public void destroy() {
		limparDocumentosAtribuidosEmSessao();
	}
	
	public OrgaoJulgador getOrgaoJulgadorAtual() {
		return Authenticator.getOrgaoJulgadorAtual();
	}

	/**
	 * Recupera todos os documentos (Relatório, Ementa e Voto Relator) vinculados ao processo e os atribui à sessão para evitar sobrecarga de acessos à base de dados
	 */
	private void recuperarDocumentosDoProcesso() {
		limparDocumentosAtribuidosEmSessao();

		// Recupera os documentos associados ao projeto e os coloca em sessão
		Map<TipoProcessoDocumento, SessaoProcessoDocumento> hashDocumentos = null;
		//if (isSessaoExistente) {
			hashDocumentos = documentoJudicialService.recuperaDocumentosComSessaoJulgamento(sessaoJulgamento, ProcessoJbpmUtil.getProcessoTrf(), getOrgaoJulgadorAtual());
		//} else {
			//hashDocumentos = documentoJudicialService.recuperaDocumentosSemSessaoJulgamento(ProcessoJbpmUtil.getProcessoTrf());
		//}

		// Inclui na sessão o documento do tipo Relatório, Voto Relator e Ementa
		setupSessaoProcessoDocumentoRelatorio(hashDocumentos.get(ParametroUtil.instance().getTipoProcessoDocumentoRelatorio()));
		setupSessaoProcessoDocumentoEmenta(hashDocumentos.get(ParametroUtil.instance().getTipoProcessoDocumentoEmenta()));
		setupSessaoProcessoDocumentoVotoRelator((SessaoProcessoDocumentoVoto)hashDocumentos.get(ParametroUtil.instance().getTipoProcessoDocumentoVoto()));
				
		recuperarLiberacoes();
	}

	/**
	 * Limpa a referência aos documentos de todos os processos previamente atribuídos à sessão
	 */
	private void limparDocumentosAtribuidosEmSessao() {
		String[] namesContext = Contexts.getSessionContext().getNames();
		for (int i = 0; i < namesContext.length; i++) {
			if (namesContext[i].startsWith(PREFIXO_SESSAO)) {
				Contexts.getSessionContext().remove(namesContext[i]);
			}
		}
	}

	public void selecionarAbasDefault() {
		setAtivaPluginTipoVoto(false);
		if (!ocultaRelatorio){
			setAbaSelecionada(RELATORIO);
		}else{
			setAbaSelecionada(VOTO);
		}
	}
	
	/**
	 * Recupera a transicao padro
	 */
	private String carregarVariavelDefaultTransition(){
		if (transicaoPadrao == null) {
			transicaoPadrao = (String) ComponentUtil.getTaskInstanceUtil().getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		}
		return transicaoPadrao;
	}
	
	private void carregarVariaveisParametrizadas(){
		ocultaEmenta = recuperarVariavel("pje:fluxo:votacaoColegiado:ocultaEmenta", false);
		ocultaRelatorio = recuperarVariavel("pje:fluxo:votacaoColegiado:ocultaRelatorio", false);
		ocultaVoto = recuperarVariavel("pje:fluxo:votacaoColegiado:ocultaVoto", false);
		exibeProclamacaoJulgamento = recuperarVariavel("pje:fluxo:votacaoColegiado:anteciparProclamacaoJulgamento", false);
		controlaLiberacao = recuperarVariavel("pje:fluxo:votacaoColegiado:controlaLiberacao", true);
		minutando = recuperarVariavel("pje:fluxo:votacaoColegiado:minutaColegiadoEmElaboracao", false);
		permiteAssinarRelatorio = recuperarVariavel("pje:fluxo:votacaoColegiado:permiteAssinarRelatorio", true);
		permiteAssinarEmenta = recuperarVariavel("pje:fluxo:votacaoColegiado:permiteAssinarEmenta", false);
		permiteAssinarVoto = recuperarVariavel("pje:fluxo:votacaoColegiado:permiteAssinarVoto", false);
		exibeMinutaVoto = recuperarVariavel(Variaveis.VARIAVEL_EXIBE_MINUTA_VOTO, false);
	}
	
	private Boolean recuperarVariavel(String variavelFluxo, Boolean valorDefault){
		Boolean variavelRetorno = valorDefault;
		Object variavelTarefa = ComponentUtil.getTramitacaoProcessualService().recuperaVariavelTarefa(variavelFluxo);
		
		if(variavelTarefa == null){
			variavelTarefa = ComponentUtil.getTramitacaoProcessualService().recuperaVariavel(variavelFluxo);
		}
		
		if(variavelTarefa instanceof String){
			variavelRetorno = Boolean.valueOf((String)variavelTarefa);
		}else if(variavelTarefa instanceof Boolean){
			variavelRetorno = (Boolean)variavelTarefa;
		}
		
		return variavelRetorno;		
	}
	
	public void setMinuta() {
		minutaVoto = getMinutaVoto();
		FacesMessages.instance().clear();
	}
	
	
	public String getMinutaVoto(){
    	StringBuilder minuta = new StringBuilder();
    	Processo processo = ComponentUtil.getTramitacaoProcessualService().recuperaProcesso().getProcesso();
    	
    	TipoProcessoDocumento tpd = ParametroUtil.instance().getTipoProcessoDocumentoEmenta();
        ementa = ComponentUtil.getProcessoDocumentoManager().getUltimoProcessoDocumento(tpd, processo);
        if(ementa != null) {
        	minuta.append(ementa.getProcessoDocumentoBin().getModeloDocumento());
        }
        
        tpd = ParametroUtil.instance().getTipoProcessoDocumentoRelatorio();
        relatorio = ComponentUtil.getProcessoDocumentoManager().getUltimoProcessoDocumento(tpd, processo);
        if(relatorio != null) {
        	minuta.append(relatorio.getProcessoDocumentoBin().getModeloDocumento());
        }
        
        tpd = ParametroUtil.instance().getTipoProcessoDocumentoVoto();
        voto = ComponentUtil.getProcessoDocumentoManager().getUltimoProcessoDocumento(tpd, processo);
        if(voto != null) {
        	minuta.append(voto.getProcessoDocumentoBin().getModeloDocumento());
        }
        
        minuta.append("<br/><br/>");
        minutaVoto = minuta.toString(); 
    	return minutaVoto;
    }	

	public Boolean getExibeMinutaVoto() {
		return exibeMinutaVoto;
	}
	
	public void downloadDocumento() {
		String nomeArquivo = "Minuta do voto";
		PdfUtil.download(minutaVoto, nomeArquivo, ComponentUtil.getTramitacaoProcessualService().recuperaProcesso());
	}
	
	@Override
	public void setAbaSelecionada(String abaSelecionada) {
		super.setAbaSelecionada(abaSelecionada);
		
		ProcessoDocumento pd = getProcessoDocumentoSelecionado(); 
		ProcessoDocumentoBinHome.instance().setInstance(pd==null ? null : pd.getProcessoDocumentoBin());
		
		setAtivaPluginTipoVoto(VOTO.equalsIgnoreCase(getAbaSelecionada()));

		setTipoProcessoDocumento(pd==null ? null : pd.getTipoProcessoDocumento());
		
		inicializarDocumento();
	}

	/**
	 * Metodo responsavel por controlar a transio padro com validao da incluso dos documentos (relatrio, voto e ementa)
	 */
	public void controlarTransicao(){
		try {
			List<ProcessoDocumento> listaProcessoDocumento = ComponentUtil.getDocumentoJudicialService().getDocumentosPorTipos(ComponentUtil.getTramitacaoProcessualService().recuperaProcesso(), 
					ParametroUtil.instance().getTipoProcessoDocumentoRelatorio().getIdTipoProcessoDocumento(),
					ParametroUtil.instance().getTipoProcessoDocumentoVoto().getIdTipoProcessoDocumento(),
					ParametroUtil.instance().getTipoProcessoDocumentoEmenta().getIdTipoProcessoDocumento());
			
			if (listaProcessoDocumento.size() < 3){
				limparMensagens();
				ComponentUtil.getFacesMessages().addFromResourceBundle(Severity.ERROR,"votacaoColegiada.relatorioVotoEmenta.documentoEmBranco");
				return;
			}
			ComponentUtil.getTaskInstanceHome().saidaDireta(transicaoPadrao);
		} catch (PJeBusinessException e) {
			limparMensagens();
			FacesMessages.instance().add(Severity.ERROR, "Erro na verificao dos documentos (relatrio, voto e ementa):  " + e.getMessage(), e);
		}
	}
	
	@Override
	public String getNomeTipoDocumentoPrincipal() {
		TipoProcessoDocumento tipo = getProcessoDocumentoSelecionado().getTipoProcessoDocumento();
		if (tipo==null)
			return null;
		return tipo.getTipoProcessoDocumento();
	}

	@Override
	public void validar(String transicaoSelecionada, Validador validador) {
		ProcessoTrf processoTrf = getSessaoProcessoDocumentoSelecionado().getProcessoDocumento().getProcessoTrf();
		validador.isNull(processoTrf.getExigeRevisor(), "Por Favor, informe o se o processo exige Revisor");
		validador.isTrue(processoTrf.getExigeRevisor() == Boolean.TRUE && processoTrf.getOrgaoJulgadorRevisor() == null, "Por Favor, informe o Revisor do Processo");
	}

	@Override
	public void movimentar(String transicaoSelecionada) throws Exception {
		
		SessaoProcessoDocumentoVoto spdvAtual = ComponentUtil.getSessaoProcessoDocumentoVotoHome().getInstance();
		
		if (spdvAtual.getTipoVoto()!= null && ComponentUtil.getSessaoProcessoDocumentoVotoHome().getVotoAntigo() != null &&
				!spdvAtual.getTipoVoto().equals(ComponentUtil.getSessaoProcessoDocumentoVotoHome().getVotoAntigo())) {		
			ComponentUtil.getSessaoProcessoDocumentoVotoHome().updateVoto();
		}
	}

	private void inicializarDocumento() {
		if (isDocumentoPersistido()) {
			int idProcessoTrf = ProcessoJbpmUtil.getProcessoTrf().getIdProcessoTrf();
			setProtocolarDocumentoBean(new ProtocolarDocumentoBean(idProcessoTrf, ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL | ProtocolarDocumentoBean.RECUPERA_DOCUMENTO_FLUXO, NAME));
		}
	}
	
	public void recuperarLiberacoes() {
		setLiberaRelatorio(getSessaoProcessoDocumentoRelatorio()==null ? false : getSessaoProcessoDocumentoRelatorio().getLiberacao());
		setLiberaVoto(getSessaoProcessoDocumentoVotoRelator()==null ? false : getSessaoProcessoDocumentoVotoRelator().getLiberacao());
		setLiberaEmenta(getSessaoProcessoDocumentoEmenta()==null ? false : getSessaoProcessoDocumentoEmenta().getLiberacao());
		setDestacarVoto(getSessaoProcessoDocumentoVotoRelator().getDestaqueSessao());
	}
	
	public void atualizarLiberacaoRelatorio() {
		limparMensagens();
		if (!isRelatorioVazio()) {
			boolean liberacaoRelOk = liberarRelatorio(isLiberaRelatorio()); 
			if (!liberacaoRelOk) {
				ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.WARN, "Erro ao liberar o relatório.");
			} else {
				ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.INFO, "Status de liberação de documento atualizada com sucesso.");
			}
		}
	}
	
	@Transactional()
	public void atualizarLiberacaoVoto() {
		boolean statusLiberacao = isLiberaVoto();

		//sessaoProcessoDocumentoVotoRelator.setImpedimentoSuspeicao(true);
		//sessaoProcessoDocumentoVotoRelator.setTextoProclamacaoJulgamento("sessaoProcessoDocumentoVotoRelator");
		//ComponentUtil.getSessaoProcessoDocumentoVotoDAO().persist(sessaoProcessoDocumentoVotoRelator);
		//ComponentUtil.getSessaoProcessoDocumentoVotoDAO().flush();
		//if (1!=0)
		//	return;

		limparMensagens();

		boolean liberacaoVotoOk = false;
		boolean liberacaoEmentaOk = false;
		boolean liberacaoRelOk = false;

		if (getIdTipoVotoSelecionado()==0) {
			ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.WARN, "Selecione o tipo de voto desejado.");
		} else {
			if (!isVotoVazio()) {
				liberacaoVotoOk = liberarVoto(statusLiberacao);
				if (!liberacaoVotoOk) {
					ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.WARN, "Erro ao liberar o voto.");
				} else if (!statusLiberacao) {
					ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.INFO, "Status de liberação de documento atualizada com sucesso.");
				}
			}
		}

		if (statusLiberacao) {
			if (!isRelatorioVazio()) {
				liberacaoRelOk = liberarRelatorio(statusLiberacao);
				if (!liberacaoRelOk) {
					ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.WARN, "Erro ao liberar o relatório.");
				}
			}

			if (!isEmentaVazia()) {
				liberacaoEmentaOk = liberarEmenta(statusLiberacao);
				if (!liberacaoEmentaOk) {
					ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.WARN, "Erro ao liberar a ementa.");
				}
			}

		}

		if (liberacaoVotoOk && liberacaoRelOk && liberacaoEmentaOk) {
			limparMensagens();
			ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.INFO, "Status de liberação de todos os documento atualizada com sucesso.");
		}

		recuperarDocumentosDoProcesso();
	}

	private boolean isVotoVazio() {
		if (!isDocumentoPersistido(getSessaoProcessoDocumentoVotoRelator()) || isDocumentoVazio(getSessaoProcessoDocumentoVotoRelator().getProcessoDocumento())) {
			ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.WARN, "Não é possível liberar o voto vazio.");
			setLiberaVoto(false);
			return true;
		}

		return false;
	}

	private boolean isRelatorioVazio() {
		if (!isDocumentoPersistido(getSessaoProcessoDocumentoRelatorio()) || isDocumentoVazio(getSessaoProcessoDocumentoRelatorio().getProcessoDocumento())) {
			ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.WARN, "Não é possível liberar o relatório vazio.");
			setLiberaRelatorio(false);
			return true;
		}

		return false;
	}

	private boolean isEmentaVazia() {
		if (!isDocumentoPersistido(getSessaoProcessoDocumentoEmenta()) || isDocumentoVazio(getSessaoProcessoDocumentoEmenta().getProcessoDocumento())) {
			ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.WARN, "Não é possível liberar a ementa vazia.");
			setLiberaEmenta(false);
			return true;
		}
		
		return false;
	}

	public void atualizarLiberacaoEmenta() {
		limparMensagens();
		if (!isEmentaVazia()) {
			boolean liberacaoEmentaOk = liberarEmenta(isLiberaEmenta()); 
			if (!liberacaoEmentaOk) {
				ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.WARN, "Erro ao liberar a ementa.");
			} else {
				ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.INFO, "Status de liberação de documento atualizada com sucesso.");
			}
		}
	}
	
	public void atualizarTipoVoto(){
		if (getTipoVoto()!=null && getTipoVoto().getIdTipoVoto() > 0){
			String idTipoVoto = String.valueOf(tipoVoto.getIdTipoVoto());
			selecionarTipoVoto(idTipoVoto);
			if (getSessaoProcessoDocumentoVotoRelator().getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento() == null) {
				getSessaoProcessoDocumentoVotoRelator().getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento("");
			}
			saveOrUpdateVoto();
		}
	}
	
	private boolean liberarRelatorio(boolean liberar) {
		boolean retorno = true;

		if (isDocumentoPersistido(getSessaoProcessoDocumentoRelatorio())) {
			getSessaoProcessoDocumentoRelatorio().setLiberacao(liberar);
			try {
				ComponentUtil.getSessaoProcessoDocumentoManager().persistSessaoProcessoDocumento(getSessaoProcessoDocumentoRelatorio(), ParametroUtil.instance().getTipoProcessoDocumentoRelatorio(), liberar);
			} catch (PJeBusinessException e) {
				logger.error(Severity.ERROR, "Erro ao liberar o relatório!", e.getLocalizedMessage());
				retorno = false;
			}
			setLiberaRelatorio(liberar);
		} else {
			setLiberaRelatorio(false);
			retorno = false;
		}

		return retorno;
	}

	private boolean liberarEmenta(boolean liberar) {		
		boolean retorno = true;

		if (isDocumentoPersistido(getSessaoProcessoDocumentoEmenta())) {
			getSessaoProcessoDocumentoEmenta().setLiberacao(liberar);
			try {
				ComponentUtil.getSessaoProcessoDocumentoManager().persistSessaoProcessoDocumento(getSessaoProcessoDocumentoEmenta(), ParametroUtil.instance().getTipoProcessoDocumentoEmenta(), liberar);
			} catch (PJeBusinessException e) {
				logger.error(Severity.ERROR, "Erro ao liberar a ementa!", e.getLocalizedMessage());
				retorno = false;
			}
			setLiberaEmenta(liberar);

		} else {
			setLiberaEmenta(false);
			retorno = false;
		}

		return retorno;
	}

	private boolean liberarVoto(boolean liberar) {		
		boolean retorno = true;

		if (isDocumentoPersistido(getSessaoProcessoDocumentoVotoRelator())) {
			getSessaoProcessoDocumentoVotoRelator().setLiberacao(liberar);
			if (getSessaoProcessoDocumentoVotoRelator().getOrgaoJulgador() == null && getSessaoProcessoDocumentoVotoRelator().getProcessoDocumento() != null) {
				getSessaoProcessoDocumentoVotoRelator().setOrgaoJulgador(getSessaoProcessoDocumentoVotoRelator().getProcessoDocumento().getProcessoTrf().getOrgaoJulgador());
			}
			if (((SessaoProcessoDocumentoVoto)getSessaoProcessoDocumentoVotoRelator()).getOjAcompanhado() == null && getSessaoProcessoDocumentoVotoRelator().getProcessoDocumento() != null) {
				((SessaoProcessoDocumentoVoto)getSessaoProcessoDocumentoVotoRelator()).setOjAcompanhado(getSessaoProcessoDocumentoVotoRelator().getProcessoDocumento().getProcessoTrf().getOrgaoJulgador());
			}

			if (saveOrUpdateVoto() && liberar)
				ComponentUtil.getSessaoProcessoDocumentoVotoHome().liberarRelatorioEmenta(true);
			setLiberaVoto(liberar);
			
		} else {
			setLiberaVoto(false);
			retorno = false;
		}
		return retorno;
	}
	
	public void atualizarDestacarVoto() {
		if (!isDocumentoPersistido() ||  getSessaoProcessoDocumentoVotoRelator().getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento() == null
				 || getSessaoProcessoDocumentoVotoRelator().getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento().equals("")) {
			ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.WARN, "Não é possível destacar o voto vazio.");
			setDestacarVoto(false);
		} else {
			((SessaoProcessoDocumentoVoto) getSessaoProcessoDocumentoVotoRelator()).setDestaqueSessao(isDestacarVoto());
			try {
				ComponentUtil.getSessaoProcessoDocumentoVotoHome().persistVoto((SessaoProcessoDocumentoVoto) getSessaoProcessoDocumentoVotoRelator(), isSessaoExistente);

				ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.INFO, "Registro alterado com sucesso");
			} catch (PJeBusinessException e) {
				ComponentUtil.getFacesMessages().addFromResourceBundle(Severity.ERROR, "Erro ao destacar o voto", e.getLocalizedMessage());
				logger.error(e.getMessage());
			}
		}
	}
	
	public void setProclamacaoJulgamento() {
		ComponentUtil.getSessaoProcessoDocumentoVotoHome().setInstance(getSessaoProcessoDocumentoVotoRelator());
	}
	
	public TipoDocumentoColegiadoEnum getTipoDocumentoColegiadoSelecionado() {
		String aba = getAbaSelecionada();
		
		if(RELATORIO.equalsIgnoreCase(aba)){
			return TipoDocumentoColegiadoEnum.REL;
		} 
		
		if(VOTO.equalsIgnoreCase(aba)){
			return TipoDocumentoColegiadoEnum.VOT;
		} 
		
		if(EMENTA.equalsIgnoreCase(aba)){
			return TipoDocumentoColegiadoEnum.EME;
		}
		
		return null;
	}

	private boolean isRelatorio() {
		return TipoDocumentoColegiadoEnum.REL.equals(getTipoDocumentoColegiadoSelecionado());
	}

	private boolean isVoto() {
		return TipoDocumentoColegiadoEnum.VOT.equals(getTipoDocumentoColegiadoSelecionado());
	}

	private boolean isEmenta() {
		return TipoDocumentoColegiadoEnum.EME.equals(getTipoDocumentoColegiadoSelecionado());
	}

	public Boolean getMinutando() {
		return minutando;
	}

	public void setMinutando(boolean minutando) {
		this.minutando = minutando;
	}

	public Boolean getOcultaEmenta() {
		return ocultaEmenta;
	}

	public void setOcultaEmenta(boolean ocultaEmenta) {
		this.ocultaEmenta = ocultaEmenta;
	}

	public Boolean getOcultaVoto() {
		return ocultaVoto;
	}

	public void setOcultaVoto(boolean ocultaVoto) {
		this.ocultaVoto = ocultaVoto;
	}

	public Boolean getExibeProclamacaoJulgamento() {
		return exibeProclamacaoJulgamento;
	}

	public void setExibeProclamacaoJulgamento(Boolean exibeProclamacaoJulgamento) {
		this.exibeProclamacaoJulgamento = exibeProclamacaoJulgamento;
	}

	public Boolean getOcultaRelatorio() {
		return ocultaRelatorio;
	}

	public void setOcultaRelatorio(boolean ocultaRelatorio) {
		this.ocultaRelatorio = ocultaRelatorio;
	}

	public Boolean getControlaLiberacao() {
		return controlaLiberacao;
	}
	
	public String getTransicaoPadrao() {
		return transicaoPadrao;
	}

	public void setTransicaoPadrao(String transicaoPadrao) {
		this.transicaoPadrao = transicaoPadrao;
	}
	
	private boolean isDocumentoSelecionadoAssinado(){
		boolean retorno = false;
		ProcessoDocumento pd = getProcessoDocumentoSelecionado();
		if (pd != null){
			retorno = ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(pd);
			if (retorno){
				limparMensagens();
				ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.WARN, "Documento está assinado");
			}
		}
		return retorno;
	}

	public void gravarRelatorio(){
		if(!isDocumentoSelecionadoAssinado()) {
			saveOrUpdateRelatorio();
		}
	}

	private void saveOrUpdateRelatorio() {
		if(isModeloVazio(getSessaoProcessoDocumentoRelatorio().getProcessoDocumento())){
			return;
		}

		if(isRelatorio()){
			try {
				ComponentUtil.getSessaoProcessoDocumentoManager().persistSessaoProcessoDocumento(getSessaoProcessoDocumentoRelatorio(), ParametroUtil.instance().getTipoProcessoDocumentoRelatorio(), isLiberaRelatorio());
			} catch (Exception e) {
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "sessao.salvarRelatorio.erro", e.getMessage());
				logger.error(e.getMessage());
			}
			
			recuperarDocumentosDoProcesso();
		}
	}
	
	public void concluirAssinatura(){
		logger.info("Concluiu assinaturas");
		return;
	}
	
	public void assinarDocumento(){
		if(!isDocumentoSelecionadoAssinado()) {
			if(isRelatorio()) {
				assinarRelatorio();
			}
			
			if(isVoto()) {
				assinarVoto();
			}
			
			if(isEmenta()) {
				assinarEmenta();
			}
		}
	}
	
	private void assinarRelatorio(){
		if(isRelatorio()){
			ComponentUtil.getSessaoProcessoDocumentoHome().setInstance(getSessaoProcessoDocumentoRelatorio());
			if(ComponentUtil.getSessaoProcessoDocumentoHome().isManaged()){
				if(arquivoAssinado != null){
					ComponentUtil.getSessaoProcessoDocumentoHome().getInstance().getProcessoDocumento().getProcessoDocumentoBin().setCertChain(arquivoAssinado.getCadeiaCertificado());
					ComponentUtil.getSessaoProcessoDocumentoHome().getInstance().getProcessoDocumento().getProcessoDocumentoBin().setSignature(arquivoAssinado.getAssinatura());
				}
				ComponentUtil.getSessaoProcessoDocumentoHome().updateComAssinatura();
			}else{
				ComponentUtil.getSessaoProcessoDocumentoHome().persistRelatorioComAssinatura(arquivoAssinado);
			}
			ComponentUtil.getTaskInstanceHome().updateTransitions();
			ComponentUtil.getTramitacaoProcessualService().movimentarProcessoJudicial(DEFAULT_TRANSITION_RELATORIO);
		}
	}
	
	private void assinarVoto(){
		if(isVoto()){
			ComponentUtil.getSessaoProcessoDocumentoHome().setInstance(getSessaoProcessoDocumentoVotoRelator());
			ComponentUtil.getSessaoProcessoDocumentoVotoHome().setInstance(getSessaoProcessoDocumentoVotoRelator());
			if(ComponentUtil.getSessaoProcessoDocumentoVotoHome().isManaged()){
				if(arquivoAssinado != null){
					ComponentUtil.getSessaoProcessoDocumentoVotoHome().getInstance().getProcessoDocumento().getProcessoDocumentoBin().setCertChain(arquivoAssinado.getCadeiaCertificado());
					ComponentUtil.getSessaoProcessoDocumentoVotoHome().getInstance().getProcessoDocumento().getProcessoDocumentoBin().setSignature(arquivoAssinado.getAssinatura());
				}
				ComponentUtil.getSessaoProcessoDocumentoVotoHome().updateVotoComAssinatura();
			}else{
				ComponentUtil.getSessaoProcessoDocumentoVotoHome().persistVotoComAssinatura();
			}
			ComponentUtil.getTaskInstanceHome().updateTransitions();
			ComponentUtil.getTramitacaoProcessualService().movimentarProcessoJudicial(DEFAULT_TRANSITION_VOTO);
		}
	}
	
	private void assinarEmenta(){
		if(isEmenta()){
			ComponentUtil.getSessaoProcessoDocumentoHome().setInstance(getSessaoProcessoDocumentoEmenta());
			if(ComponentUtil.getSessaoProcessoDocumentoHome().isManaged()){
				if(arquivoAssinado != null){
					ComponentUtil.getSessaoProcessoDocumentoHome().getInstance().getProcessoDocumento().getProcessoDocumentoBin().setCertChain(arquivoAssinado.getCadeiaCertificado());
					ComponentUtil.getSessaoProcessoDocumentoHome().getInstance().getProcessoDocumento().getProcessoDocumentoBin().setSignature(arquivoAssinado.getAssinatura());
				}
				ComponentUtil.getSessaoProcessoDocumentoHome().updateComAssinatura();
			}else{
				ComponentUtil.getSessaoProcessoDocumentoHome().persistEmentaComAssinatura(arquivoAssinado);
			}
			ComponentUtil.getTaskInstanceHome().updateTransitions();
			ComponentUtil.getTramitacaoProcessualService().movimentarProcessoJudicial(DEFAULT_TRANSITION_EMENTA);
		}
	}	
	
	/**
 	 * Metodo responsvel por verificar o papel e o tipo de documento processo
 	 * para exigir ou no assinatura.
 	 * 
 	 * @return Boolean
 	 */
 	public boolean isOcultarBotaoAssinar(){
 		boolean retorno = false;
 		if (ComponentUtil.getSessaoProcessoDocumentoHome().getInstance().getProcessoDocumento() != null){
 			retorno = ComponentUtil.getTipoProcessoDocumentoPapelService().verificarExigibilidadeNaoAssina(
 					Authenticator.getPapelAtual(), 
 					ComponentUtil.getSessaoProcessoDocumentoHome().getInstance().getProcessoDocumento().getTipoProcessoDocumento());
 		}
 		return retorno;
 	}
 	
 	public Boolean isOcultaBotaoAssinarRelatorio(){
 		Boolean retorno = (Boolean) ComponentUtil.getTaskInstanceUtil().getVariable(Parametros.PJE_FLUXO_OCULTAR_BOTAO_ASSINAR_RELATORIO);
 		if (retorno != null && retorno){
 			retorno = !isOcultarBotaoAssinar();
 		} else {
 			retorno = Boolean.FALSE;
 		}
 		return retorno;
	}
	
	/**
	 * Mtodo responsvel por ocultar o boto, no xthml, de transio se a
	 * transicaoPadrao conter o texto "oculto".
	 * 
	 * @return
	 */
	public boolean isTransicaoPadraoOculta() {
		return StringUtils.isNotBlank(transicaoPadrao) && !transicaoPadrao.contains(OCULTA_DEFAULT_TRANSITION);
	}
		
	public void updateVotoComValidacao() {
		if(!isDocumentoSelecionadoAssinado() && !verificarTipoVotoIsNull()) {
			limparMensagens();
			ComponentUtil.getSessaoProcessoDocumentoVotoHome().updateVoto();
		}
	}

	private boolean verificarTipoVotoIsNull() {
		SessaoProcessoDocumentoVoto instance = (SessaoProcessoDocumentoVoto) getSessaoProcessoDocumentoVotoRelator();
		boolean tipoVotoIsNull = false;
		if ((instance.getTipoVoto() == null || StringUtils.isEmpty(instance.getTipoVoto().getTipoVoto()))) {
			tipoVotoIsNull = true;
			limparMensagens();
			ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.ERROR, "votacaoColegiada.votoObrigatorio");
		}
		return tipoVotoIsNull;
	}

	// Método utilizado na versão antiga do CK Editor
	public void persistVotoComValidacao() {
		if(!isDocumentoSelecionadoAssinado()) {
			limparMensagens();
			if (!verificarTipoVotoIsNull()) {
				ComponentUtil.getSessaoProcessoDocumentoVotoHome().persistVoto();
			}else{
				ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.WARN, "Erro na validação do voto");
			}
		}
	}

	private boolean saveOrUpdateVoto() {
		if (verificarTipoVotoIsNull()) {
			return false;
		}
		limparMensagens();

		try {
			SessaoProcessoDocumentoVoto spdv = getSessaoProcessoDocumentoVotoRelator();
			spdv.getProcessoDocumento().setExclusivoAtividadeEspecifica(Boolean.TRUE);
			TipoProcessoDocumento tpd = ParametroUtil.instance().getTipoProcessoDocumentoVoto();
			ComponentUtil.getSessaoProcessoDocumentoManager().persistSessaoProcessoDocumento(spdv, tpd, isLiberaVoto());
			recuperarDocumentosDoProcesso();
			return true;
		} catch (Exception e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "sessao.salvarVoto.erro", e.getMessage());
			logger.error(e.getMessage());
		}

		return false;
	}
	
	@Override
	public boolean isFormularioPreenchido() {
		boolean retorno = true;
		if(isVoto() && ((SessaoProcessoDocumentoVoto) getSessaoProcessoDocumentoVotoRelator()).getTipoVoto() == null) {
			retorno = false;
		} 
		return retorno;
	}

	private void saveOrUpdateEmenta() {
		if(isModeloVazio(getSessaoProcessoDocumentoEmenta().getProcessoDocumento())){
			return;
		}
		if (isEmenta()) {
			
			try {
				ComponentUtil.getSessaoProcessoDocumentoManager().persistSessaoProcessoDocumento(getSessaoProcessoDocumentoEmenta(), ParametroUtil.instance().getTipoProcessoDocumentoEmenta(), isLiberaEmenta());
			} catch (Exception e) {
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "sessao.salvarEmenta.erro", e.getMessage());
				logger.error(e.getMessage());
			}
			
			recuperarDocumentosDoProcesso();
		}
	}

	public boolean  isModeloVazio(ProcessoDocumento processoDocumento) {
		try {
			if (processoDocumento == null || processoDocumento.getProcessoDocumentoBin() == null)
				return true;
			String conteudoDocumento = processoDocumento.getProcessoDocumentoBin().getModeloDocumento();
			return StringUtil.isEmpty(StringUtil.removeHtmlTags(conteudoDocumento));
		} catch (Exception e) {
			logger.error(Severity.ERROR, "Erro ao obter o conteúdo do documento atual. Mensagem interna: " + e);
		}		
		return true;
	} 

	public boolean verificarAba(String aba) {
		return getAbaSelecionada().equalsIgnoreCase(aba);
	}

	private void limparMensagens() {
		StatusMessages.instance().clear();
		StatusMessages.instance().clearGlobalMessages();
		ComponentUtil.getFacesMessages().clear();
	}
	
	public boolean isUploadArquivoAssinadoRealizado(){
		return uploadArquivoAssinadoRealizado;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)
			throws Exception {
		arquivoAssinado = arquivoAssinadoHash;
		uploadArquivoAssinadoRealizado = Boolean.TRUE;
	}

	public String getActionName() {
		return NAME;
	}
	
	@Override
	public String getDownloadLinks(){
		
		List<ProcessoDocumento> listPDs = new ArrayList<ProcessoDocumento>();									
		listPDs.add(getProcessoDocumentoSelecionado());							    
		
		return ComponentUtil.getDocumentoJudicialService().getDownloadLinks(listPDs);
	}
	
	public String getDownloadLinksVoto(){
		return ComponentUtil.getDocumentoJudicialService().getDownloadLinks(Arrays.asList(ComponentUtil.getSessaoProcessoDocumentoVotoHome().getInstance().getProcessoDocumento()));
	}
	
	public ArquivoAssinadoHash getArquivoAssinado() {
		return arquivoAssinado;
	}
	
	public void setArquivoAssinado(ArquivoAssinadoHash arquivoAssinado) {
		this.arquivoAssinado = arquivoAssinado;
	}
	
	public SessaoProcessoDocumento getSessaoProcessoDocumentoSelecionado() {
		String aba = getAbaSelecionada();
		if(RELATORIO.equalsIgnoreCase(aba)){
			return sessaoProcessoDocumentoRelatorio;
		} 
		
		if(VOTO.equalsIgnoreCase(aba)){
			return sessaoProcessoDocumentoVotoRelator;
		} 
		
		if(EMENTA.equalsIgnoreCase(aba)){
			return sessaoProcessoDocumentoEmenta;
		}
		
		return null;
	}

	@Override
	public boolean isDocumentoAssinado() throws PJeBusinessException {
		ProcessoDocumento pd = getProcessoDocumentoSelecionado(); 
		if (pd == null || pd.getIdProcessoDocumento() == 0) {
			return false;
		}
		return ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(pd);
	}

	@Override
	public void removerAssinatura() {
		ProcessoDocumento documento = getProcessoDocumentoSelecionado();
		if(ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(documento)) {	
			ComponentUtil.getAssinaturaDocumentoService().removeAssinatura(documento);
	 		ComponentUtil.getTaskInstanceHome().updateTransitions();
		} 
	}

	@Override
	public void descartarDocumento() {

		try {
			if (!isDocumentoAssinado()) {
				limparMensagens();
				if (isVoto()) {
					SessaoProcessoDocumentoVoto spdv = getSessaoProcessoDocumentoVotoRelator();
					ProcessoDocumento pd = spdv.getProcessoDocumento();
					ProcessoDocumentoBin pdb = pd.getProcessoDocumentoBin();
					
					SessaoProcessoDocumentoVotoManager spdvm = SessaoProcessoDocumentoVotoManager.instance();					
					List<SessaoProcessoDocumentoVoto> votosAcompanhantes = spdvm.getVotosAcompanhantes(spdv, spdv.getOrgaoJulgador());
					for (SessaoProcessoDocumentoVoto vot : votosAcompanhantes) {
						vot.setOjAcompanhado(vot.getOrgaoJulgador());
						spdvm.persist(vot);
					}
					DerrubadaVotoManager.instance().analisarTramitacaoFluxoVotoDerrubado(spdv);

					ComponentUtil.getSessaoProcessoDocumentoVotoHome().liberarRelatorioEmenta(Boolean.FALSE);
					
					spdvm.remove(spdv);
					ComponentUtil.getControleVersaoDocumentoManager().deletarTodasVersoesIdDocumento(pdb.getIdProcessoDocumentoBin());
					ProcessoDocumentoBinManager.instance().remove(pdb);
					ProcessoDocumentoManager.instance().remove(pd);
					spdvm.flush();
					
					setupSessaoProcessoDocumentoVotoRelator(null);
				}

				ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.INFO,
						"Documento descartado com sucesso");
			}
		} catch (PJeBusinessException e) {
			ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.ERROR,
					"Ocorreu erro ao descartado documento.");
			logger.error(e, "[VotacaoColegiadoCkEditorAction] - Ocorreu erro ao descartar documento.");
		} catch (Exception e) {
			ComponentUtil.getFacesMessages().addFromResourceBundle(StatusMessage.Severity.ERROR,
					"Ocorreu erro ao descartar documento.");
			logger.error(e, "[VotacaoColegiadoCkEditorAction] - Ocorreu erro ao descartar documento.");
		}
	}

	@Override
	public String obterConteudoDocumentoAtual() {
		String retorno = "";
		try {
			ProcessoDocumento pd = getProcessoDocumentoSelecionado();
			if (pd!=null && pd.getProcessoDocumentoBin()!=null){
				retorno = ComponentUtil.getControleVersaoDocumentoManager().obterConteudoDocumentoJSON(pd.getProcessoDocumentoBin().getModeloDocumento());
			}
		} catch (Exception e) {
			logger.error(Severity.ERROR, "Erro ao obter o conteúdo do documento atual. Mensagem interna: " + e);
		}
		return retorno;
	}

	@Override
	public void salvar(String conteudo) {
		try {
			if(!isDocumentoAssinado()) {
				saveOrUpdateDeAcordoComAbaSelecionada(conteudo);
			}
		} catch (PJeBusinessException e) {
			limparMensagens();
			logger.error(Severity.ERROR, "[VotacaoColegiadoCkEditorAction] - Erro ao salvar o conteúdo do documento atual. Mensagem interna: " + e);
			ComponentUtil.getFacesMessages().add(Severity.ERROR, "Erro ao salvar o conteúdo do documento atual.");
		}
	}

	private void saveOrUpdateDeAcordoComAbaSelecionada(String conteudo) {
		limparMensagens();

		ProcessoDocumento pd = null;
		if (RELATORIO.equalsIgnoreCase(getAbaSelecionada())) {
			getSessaoProcessoDocumentoRelatorio().getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(conteudo);
			pd = getSessaoProcessoDocumentoRelatorio().getProcessoDocumento();
			saveOrUpdateRelatorio();
	    } 
		if (VOTO.equalsIgnoreCase(getAbaSelecionada())) {
			getSessaoProcessoDocumentoVotoRelator().getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(conteudo);
			pd = getSessaoProcessoDocumentoVotoRelator().getProcessoDocumento();
	        saveOrUpdateVoto();
	    }
		if (EMENTA.equalsIgnoreCase(getAbaSelecionada())) {
			getSessaoProcessoDocumentoEmenta().getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(conteudo);
			pd = getSessaoProcessoDocumentoEmenta().getProcessoDocumento();
			saveOrUpdateEmenta();
		}
		try {
			if (pd != null && pd.getIdProcessoDocumento() > 0){
				ComponentUtil.getControleVersaoDocumentoManager().salvarVersaoDocumento(pd);
				ComponentUtil.getFacesMessages().add(Severity.INFO, "Documento salvo com sucesso");
				logger.info(Severity.INFO, "Documento salvo com sucesso");				
			}
		} catch (PJeBusinessException e) {
			logger.error("[VotacaoColegiadoCkEditorAction] Houve um erro ao tentar gravar: {0}.", e.getLocalizedMessage());
			ComponentUtil.getFacesMessages().add(Severity.ERROR, e.getLocalizedMessage());
		}
	}
	
	private boolean isDocumentoPersistido(SessaoProcessoDocumento spd){
		if (spd==null)
			return false;
		if (spd.getIdSessaoProcessoDocumento()==0)
			return false;
		
		ProcessoDocumento processoDocumento = spd.getProcessoDocumento();
		if (processoDocumento==null)
			return false;
		if (processoDocumento.getIdProcessoDocumento()==0)
			return false;

		return true;
	}
	
	@Override
	public boolean isDocumentoPersistido(){
		SessaoProcessoDocumento sessaoProcessoDocumentoSelecionado = getSessaoProcessoDocumentoSelecionado();
		return isDocumentoPersistido(sessaoProcessoDocumentoSelecionado);
	}
	
	private boolean isDocumentoVazio(ProcessoDocumento pd){
		if (pd==null)
			return true;
		
		ProcessoDocumentoBin pdb = pd.getProcessoDocumentoBin();
		if (pdb==null)
			return true;
		
		String conteudo = pdb.getModeloDocumento(); 
		if (conteudo==null)
			return true;
		
		if (StringUtil.isEmpty(StringUtil.removeHtmlTags(conteudo)))
			return true;
		
		return false; 
	}
	
	public boolean isDocumentoVazio(){
		ProcessoDocumento pd = getProcessoDocumentoSelecionado(); 
		return isDocumentoVazio(pd);
	}
	
	@Override
	public String getTiposDocumentosDisponiveis() {
		JSONArray retorno = new JSONArray();
		if (RELATORIO.equalsIgnoreCase(getAbaSelecionada())) {
			retorno.put(RELATORIO);
		}
		if (VOTO.equalsIgnoreCase(getAbaSelecionada())) {
			retorno.put(VOTO);
		}
		if (EMENTA.equalsIgnoreCase(getAbaSelecionada())) {
			retorno.put(EMENTA);
		}
		return retorno.toString();
	}
	
	@Override
	public String getAbasWidget() {
		JSONArray retorno = new JSONArray();
		if(!getOcultaRelatorio()) {
			retorno.put(RELATORIO);
		}
		if(!getOcultaVoto()) {
			retorno.put(VOTO);
		}
		if(!getOcultaEmenta()) {
			retorno.put(EMENTA);
		}
		return retorno.toString();
	}
	
	private ProcessoDocumento getProcessoDocumentoSelecionado(){
		SessaoProcessoDocumento sessaoProcessoDocumento = getSessaoProcessoDocumentoSelecionado();
		if (sessaoProcessoDocumento==null)
			return null;
		ProcessoDocumento documento = sessaoProcessoDocumento.getProcessoDocumento();
		return documento;
	}
	
	@Override
	public String recuperarModeloDocumento(String modeloDocumento) {
		String retorno = null;
		selecionarModeloProcessoDocumento(modeloDocumento);
		ProcessoDocumento pd = getProcessoDocumentoSelecionado();
		if (pd != null && pd.getProcessoDocumentoBin() != null) {
			retorno = pd.getProcessoDocumentoBin().getModeloDocumento();
		}
		return retorno;
	}

	public SessaoProcessoDocumentoVoto getSessaoProcessoDocumentoVotoSelecionado() {
		return getSessaoProcessoDocumentoVotoRelator();
	}

	@Override
	public String obterTiposVoto() {		
		RespostaDTO respostaDTO = new RespostaDTO();
		try {
			respostaDTO.setSucesso(Boolean.TRUE);
			RespostaTiposVotoDTO respostaTiposVotoDTO = new RespostaTiposVotoDTO();
			respostaTiposVotoDTO.setPodeAlterar(!ocultaVoto);
			TipoVoto tv = null;
			if (getTipoVoto()!=null) {
				tv = getTipoVoto();
			} else {
				SessaoProcessoDocumentoVoto spdv = ComponentUtil.getSessaoProcessoDocumentoVotoHome().getInstance();
				if(spdv.getTipoVoto() != null) {
					tv = spdv.getTipoVoto();
				} else if(!VOTO.equalsIgnoreCase(getAbaSelecionada())) {
					tv = new TipoVoto();
				}
			}
			respostaTiposVotoDTO.setSelecao(tv == null ? null : criarTipoVotoDTO(tv));
			respostaTiposVotoDTO.setTipos(criarListaTiposVoto(ComponentUtil.getTipoVotoManager().listTipoVotoAtivoComRelator()));
			respostaDTO.setResposta(respostaTiposVotoDTO);
		} catch (Exception e) {
			logger.error(e, "Erro obterTipoVotos");
			respostaDTO.setSucesso(Boolean.FALSE);
			respostaDTO.setMensagem(e.getLocalizedMessage());
		}
		
		return new Gson().toJson(respostaDTO, RespostaDTO.class);
	}
	
	public List<TipoVoto> getTiposVotos(){
		return ComponentUtil.getTipoVotoManager().listTipoVotoAtivoComRelator();
	}
	
	@Override
	public void selecionarTipoVoto(String idTipoVoto) {
		try {
			if(!isDocumentoAssinado()) {
				int id = Integer.parseInt(idTipoVoto);
				setIdTipoVotoSelecionado(id);
				getSessaoProcessoDocumentoVotoRelator().setTipoVoto(getTipoVoto());
			}
		} catch (PJeBusinessException e) {
			limparMensagens();
			logger.error("Houve um erro ao selecionar o tipo de voto: {0}.", e);
			ComponentUtil.getFacesMessages().add(Severity.ERROR, "Houve um erro ao selecionar o tipo de voto.");
		}
	}
	
	@Override
	public String verificarPluginTipoVoto() {		
		JSONObject retorno = new JSONObject();
		boolean ativo = false;
		if(getProcessoDocumentoSelecionado() != null && isAtivaPluginTipoVoto() && !ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(getProcessoDocumentoSelecionado())) {
			ativo = true;
		}
		try {
			retorno.put("sucesso", ativo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return retorno.toString();
	}
	
	@Override
	public void setModeloDocumentoSelecionado(ModeloDocumento modelo){
		try {
			if(!isDocumentoAssinado()) {
				ProcessoDocumento pd = getProcessoDocumentoSelecionado();
				if (pd != null && pd.getProcessoDocumentoBin() != null) {
					pd.getProcessoDocumentoBin().setModeloDocumento(ComponentUtil.getModeloDocumentoManager().obtemConteudo(modelo));
					if (RELATORIO.equalsIgnoreCase(getAbaSelecionada()) || EMENTA.equalsIgnoreCase(getAbaSelecionada())) {
						getSessaoProcessoDocumentoSelecionado().setProcessoDocumento(pd);
					} else if (VOTO.equalsIgnoreCase(getAbaSelecionada())) {
						getSessaoProcessoDocumentoVotoSelecionado().setProcessoDocumento(pd);
					}
				}
			}
		} catch (PJeBusinessException e) {
			limparMensagens();
			logger.error("Houve um erro ao selecionar o modelo do documento: {0}.", e);
			ComponentUtil.getFacesMessages().add(Severity.ERROR, "Houve um erro ao selecionar o modelo do documento.");
		}
	}
	
	@Override
	public String obterVersoesDocumentoJSON() {
		String retorno = "";
		try {
			ProcessoDocumento pd = getProcessoDocumentoSelecionado();
			if (pd != null && pd.getProcessoDocumentoBin() != null) {
				retorno = ComponentUtil.getControleVersaoDocumentoManager().obterVersoesDocumentoJSON(pd.getProcessoDocumentoBin());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return retorno;
	}

	@Override
	public void aplicarVersaoDocumento(int versao) {
		limparMensagens();
		try {
			if(!isDocumentoAssinado()) {
				ProcessoDocumento pd = getProcessoDocumentoSelecionado();
				if (pd!=null){
					ControleVersaoDocumento cvd = ComponentUtil.getControleVersaoDocumentoManager().obterVersaoDocumento(versao, pd.getProcessoDocumentoBin());
					getProcessoDocumentoSelecionado().getProcessoDocumentoBin().setModeloDocumento(cvd.getConteudo());
				}
			}
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao aplicar Versao no Documento {0}.", e);
			ComponentUtil.getFacesMessages().add(Severity.ERROR, "Houve um erro ao aplicar versão do documento.");
		}
	}
	
	@Override
	public String obterVersoesDocumentoJSONProximas() {
		String retorno = "";
		ProcessoDocumento pd = getProcessoDocumentoSelecionado(); 
		try {
			if (pd != null && pd.getProcessoDocumentoBin() != null) {
				retorno = ComponentUtil.getControleVersaoDocumentoManager().obterVersoesDocumentoJSONPaginada(pd.getProcessoDocumentoBin(), getLimit(), getOffset());
				paginarProximasControleVersaoDocumento();
			}
		} catch (PJeBusinessException e) {
			logger.error(e.getMessage());
		}
		
		return retorno;
	}
	
	@Override
	public boolean podeAssinar() {
		boolean retorno = false;
		if( isTipoProcessoDocumentoDefinido() && isDocumentoPersistido() && !isDocumentoVazio()) {
			ProcessoDocumento pd = getProcessoDocumentoSelecionado(); 
			if(pd !=null && !ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(pd)) {
				retorno = podeAssinarTipo();
			}
		}
		return retorno;
	}

	/**
	 * @param retorno
	 * @return boolean
	 */
	private boolean podeAssinarTipo() {
		boolean retorno = false;
		if(isRelatorio() && permiteAssinarRelatorio) {
			retorno = true;
		}
		if(isVoto() && permiteAssinarVoto)  {
			retorno = true;
		}
		if(isEmenta() && permiteAssinarEmenta) {
			retorno = true;
		}
		return retorno;
	}
	
	@Override
	public boolean isTipoProcessoDocumentoDefinido(){
		boolean result = Boolean.TRUE;
		if(getTipoProcessoDocumento() == null){
			result = Boolean.FALSE;
		}
		return result;
	}

	@Override
	public TipoProcessoDocumento getTipoProcessoDocumento() {
		TipoProcessoDocumento tipoProcessoDocumento = null;
		if(isDocumentoPersistido()){
			ProcessoDocumento pd = getProcessoDocumentoSelecionado();
			tipoProcessoDocumento = (pd != null && pd.getTipoProcessoDocumento() != null)?(pd.getTipoProcessoDocumento()):null;
			if(tipoProcessoDocumento == null){
				try {
					tipoProcessoDocumento = ComponentUtil.getTipoProcessoDocumentoManager().findById(getProtocolarDocumentoBean().getDocumentoPrincipal().getTipoProcessoDocumento().getIdTipoProcessoDocumento());
				} catch (PJeBusinessException e) {
					limparMensagens();
					logger.error(Severity.WARN,"Não foi possível recuperar o tipo de documento para o documento persistido em questão!");
					ComponentUtil.getFacesMessages().add(Severity.ERROR,"Não foi possível recuperar o tipo de documento para o documento persistido em questão!");
				}
			} 
		} else {
			tipoProcessoDocumento = super.getTipoProcessoDocumento();
		}
		return tipoProcessoDocumento;
	}
	
	public boolean isRelatorioAssinado(){
		if (getSessaoProcessoDocumentoRelatorio()==null)
			return false;
		
		boolean retorno = false;
		ProcessoDocumento pd = getSessaoProcessoDocumentoRelatorio().getProcessoDocumento(); 
		if(pd != null 
			&& pd.getTipoProcessoDocumento() != null
			&& RELATORIO.equals(pd.getTipoProcessoDocumento().toString()) 
			&& pd.getProcessoDocumentoBin() != null) {				
			retorno = ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(pd);
		}
		return retorno;
	}
	
	public boolean isVotoAssinado() {
		if (getSessaoProcessoDocumentoVotoRelator()==null)
			return false;
		
		boolean retorno = false;
		ProcessoDocumento pd = getSessaoProcessoDocumentoVotoRelator().getProcessoDocumento(); 
		if(pd != null 
			&& pd.getTipoProcessoDocumento() != null
			&& VOTO.equals(pd.getTipoProcessoDocumento().toString()) 
			&& pd.getProcessoDocumentoBin() != null) {				
			retorno = ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(pd);
		}
		return retorno;
	}
	
	public boolean isEmentaAssinada() {
		if (getSessaoProcessoDocumentoEmenta()==null)
			return false;
		
		boolean retorno = false;		
		ProcessoDocumento pd = getSessaoProcessoDocumentoEmenta().getProcessoDocumento(); 
		if(pd != null 
			&& pd.getTipoProcessoDocumento() != null
			&& EMENTA.equals(pd.getTipoProcessoDocumento().toString()) 
			&& pd.getProcessoDocumentoBin() != null) {				
			retorno = ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(pd);
		}
		return retorno;
	}
	
	public boolean isVotoRelatorProferido() {
		return isModeloVazio(getSessaoProcessoDocumentoVotoRelator()==null ? null : getSessaoProcessoDocumentoVotoRelator().getProcessoDocumento());
	}

	public boolean isEmentaProferida() {
		return isModeloVazio(getSessaoProcessoDocumentoEmenta()==null ? null : getSessaoProcessoDocumentoEmenta().getProcessoDocumento());
	}

	public Boolean getIsSessaoExistente() {
		return isSessaoExistente;
	}

	public void setIsSessaoExistente(Boolean isSessaoExistente) {
		this.isSessaoExistente = isSessaoExistente;
	}

	public Sessao getSessaoJulgamento() {
		return sessaoJulgamento;
	}

	public void setSessaoJulgamento(Sessao sessaoJulgamento) {
		this.sessaoJulgamento = sessaoJulgamento;
	}

	private void initSessaoProcessoDocumento(SessaoProcessoDocumento sessaoProcessoDocumento, TipoProcessoDocumento tipoProcessoDocumento) {
		if (sessaoProcessoDocumento.getProcessoDocumento()==null) {
			ProcessoDocumento novoProcDocRelatorio = ComponentUtil.getDocumentoJudicialService().getDocumento();

			Usuario usuarioLogado = Authenticator.getUsuarioLogado();
			novoProcDocRelatorio.setUsuarioInclusao(usuarioLogado);
			novoProcDocRelatorio.setAtivo(true);
			novoProcDocRelatorio.setDataInclusao(new Date());
			//novoProcDocRelatorio.setProcesso(ComponentUtil.getTramitacaoProcessualService().recuperaProcesso().getProcesso());
			//novoProcDocRelatorio.setProcesso(ProcessoHome.instance().getInstance());
			//novoProcDocRelatorio.setProcessoDocumento(novoProcDocRelatorio.getTipoProcessoDocumento().getTipoProcessoDocumento());
			novoProcDocRelatorio.getProcessoDocumentoBin().setUsuario(usuarioLogado);		
			novoProcDocRelatorio.getProcessoDocumentoBin().setModeloDocumento("");
			novoProcDocRelatorio.setProcessoTrf(ProcessoTrfHome.instance().getInstance());
			novoProcDocRelatorio.setProcesso(ProcessoTrfHome.instance().getInstance().getProcesso());
			novoProcDocRelatorio.setTipoProcessoDocumento(tipoProcessoDocumento);

			sessaoProcessoDocumento.setProcessoDocumento(novoProcDocRelatorio);
		}
		sessaoProcessoDocumento.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
		sessaoProcessoDocumento.setSessao(sessaoJulgamento); //Segundo entendimento, em 29/08/2018, no CNJ, com o Zeniel, a sessão de julgamento só deve ser informada, quando do momento do fechamento da pauta de sessão.
		
		logger.info("Novo SessaoProcessoDocumento criado: " + sessaoProcessoDocumento);
	}

	public SessaoProcessoDocumento getSessaoProcessoDocumentoRelatorio() {
		if (sessaoProcessoDocumentoRelatorio==null)
			setupSessaoProcessoDocumentoRelatorio(null);
		return sessaoProcessoDocumentoRelatorio;
	}
	
	private void setupSessaoProcessoDocumentoRelatorio(SessaoProcessoDocumento sessaoProcessoDocumento) {
		initSessaoProcessoDocumento(sessaoProcessoDocumentoRelatorio = sessaoProcessoDocumento!=null ? sessaoProcessoDocumento : new SessaoProcessoDocumento(), 
				ParametroUtil.instance().getTipoProcessoDocumentoRelatorio());
		Contexts.getSessionContext().set(PREFIXO_SESSAO + ProcessoJbpmUtil.getProcessoTrf().getIdProcessoTrf() + "_R", sessaoProcessoDocumentoRelatorio);		
		setLiberaRelatorio(sessaoProcessoDocumentoRelatorio.getLiberacao());
		assert this.getSessaoProcessoDocumentoRelatorio() != null;
	}
	
	public void setSessaoProcessoDocumentoRelatorio(SessaoProcessoDocumento sessaoProcessoDocumentoRelatorio) {
		if (!Objects.equals(this.sessaoProcessoDocumentoRelatorio, sessaoProcessoDocumentoRelatorio)) {
			setupSessaoProcessoDocumentoRelatorio(sessaoProcessoDocumentoRelatorio);
		}
	}

	public SessaoProcessoDocumento getSessaoProcessoDocumentoEmenta() {
		if (sessaoProcessoDocumentoEmenta==null)
			setupSessaoProcessoDocumentoEmenta(null);
		return sessaoProcessoDocumentoEmenta;
	}

	private void setupSessaoProcessoDocumentoEmenta(SessaoProcessoDocumento sessaoProcessoDocumentoEmenta) {
		initSessaoProcessoDocumento(sessaoProcessoDocumentoEmenta = (sessaoProcessoDocumentoEmenta!=null) ? sessaoProcessoDocumentoEmenta : new SessaoProcessoDocumento(), ParametroUtil.instance().getTipoProcessoDocumentoEmenta());
		this.sessaoProcessoDocumentoEmenta = sessaoProcessoDocumentoEmenta;
		Contexts.getSessionContext().set(PREFIXO_SESSAO + ProcessoJbpmUtil.getProcessoTrf().getIdProcessoTrf() + "_E", sessaoProcessoDocumentoEmenta);			
		setLiberaEmenta(sessaoProcessoDocumentoEmenta.getLiberacao());
		assert this.getSessaoProcessoDocumentoEmenta() != null;
	}

	public void setSessaoProcessoDocumentoEmenta(SessaoProcessoDocumento sessaoProcessoDocumentoEmenta) {
		if (!Objects.equals(this.sessaoProcessoDocumentoEmenta, sessaoProcessoDocumentoEmenta)) {
			setupSessaoProcessoDocumentoEmenta(sessaoProcessoDocumentoEmenta);
		}
	}

	public SessaoProcessoDocumentoVoto getSessaoProcessoDocumentoVotoRelator() {
		if (sessaoProcessoDocumentoVotoRelator==null)
			setupSessaoProcessoDocumentoVotoRelator(null);
		return sessaoProcessoDocumentoVotoRelator;
	}

	private void setupSessaoProcessoDocumentoVotoRelator(SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVotoRelator) {
		initSessaoProcessoDocumento(sessaoProcessoDocumentoVotoRelator = (sessaoProcessoDocumentoVotoRelator!=null) ? sessaoProcessoDocumentoVotoRelator : new SessaoProcessoDocumentoVoto(), ParametroUtil.instance().getTipoProcessoDocumentoVoto());
		ProcessoTrf processoTrf = ProcessoJbpmUtil.getProcessoTrf();
		sessaoProcessoDocumentoVotoRelator.setOjAcompanhado(Authenticator.getOrgaoJulgadorAtual());
		sessaoProcessoDocumentoVotoRelator.setProcessoTrf(processoTrf);

		this.sessaoProcessoDocumentoVotoRelator = sessaoProcessoDocumentoVotoRelator;
		Contexts.getSessionContext().set(PREFIXO_SESSAO + ProcessoJbpmUtil.getProcessoTrf().getIdProcessoTrf() + "_V", sessaoProcessoDocumentoVotoRelator);

		setLiberaVoto(sessaoProcessoDocumentoVotoRelator.getLiberacao());
		setDestacarVoto(sessaoProcessoDocumentoVotoRelator.getDestaqueSessao());
		this.setTipoVoto(sessaoProcessoDocumentoVotoRelator.getTipoVoto());
		ComponentUtil.getSessaoProcessoDocumentoVotoHome().setInstance(sessaoProcessoDocumentoVotoRelator);
		assert this.getSessaoProcessoDocumentoVotoRelator() != null;
	}
	
	public void setSessaoProcessoDocumentoVotoRelator(SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVotoRelator) {
		if (!Objects.equals(this.sessaoProcessoDocumentoVotoRelator, sessaoProcessoDocumentoVotoRelator)) {			
			setupSessaoProcessoDocumentoVotoRelator(sessaoProcessoDocumentoVotoRelator);
		}
	}

	public TipoVoto getTipoVoto() {
		if (tipoVoto==null){
			int idTipoVoto = getIdTipoVotoSelecionado();
			if (idTipoVoto!=0) {
				try {
					tipoVoto = ComponentUtil.getTipoVotoManager().findById(getIdTipoVotoSelecionado());
				} catch (PJeBusinessException ex) {
					logger.error("Erro ao obter tipoVoto", ex);
				}
			}
			this.tipoVoto = getSessaoProcessoDocumentoVotoRelator().getTipoVoto();
			if (this.getSessaoProcessoDocumentoVotoRelator()!=null) {
				assert this.getSessaoProcessoDocumentoVotoRelator().getTipoVoto()==null;
				this.getSessaoProcessoDocumentoVotoRelator().setTipoVoto(tipoVoto);
			}
		}
		return tipoVoto;
	}

	public void setTipoVoto(TipoVoto tipoVoto) {
		if (!Objects.equals(this.tipoVoto,tipoVoto)) {
			this.tipoVoto = tipoVoto;
			super.setIdTipoVotoSelecionado(tipoVoto==null ? 0 : tipoVoto.getIdTipoVoto());
			if (this.getSessaoProcessoDocumentoVotoRelator()!=null)
				this.getSessaoProcessoDocumentoVotoRelator().setTipoVoto(tipoVoto);
		}
	}

	@Override
	public int getIdTipoVotoSelecionado() {
		int id = super.getIdTipoVotoSelecionado();
		if (id==0) {
			if (tipoVoto!=null) 
				id = tipoVoto.getIdTipoVoto();
			if ((id==0) && (this.getSessaoProcessoDocumentoVotoRelator()!=null) && this.getSessaoProcessoDocumentoVotoRelator().getTipoVoto()!=null)
				id = this.getSessaoProcessoDocumentoVotoRelator().getTipoVoto().getIdTipoVoto();
		}
		return id;
	}

	@Override	
	public void setIdTipoVotoSelecionado(int idTipoVotoSelecionado) {
		if (this.getIdTipoVotoSelecionado() != idTipoVotoSelecionado) {
			super.setIdTipoVotoSelecionado(idTipoVotoSelecionado);
			TipoVoto tv;
			try {
				tv = ComponentUtil.getTipoVotoManager().findById(idTipoVotoSelecionado);
			} catch (PJeBusinessException ex) {
				tv = null;
			}
			this.tipoVoto = tv;
			if (this.getSessaoProcessoDocumentoVotoRelator()!=null)
				this.getSessaoProcessoDocumentoVotoRelator().setTipoVoto(tv);
		}
	}
	
	public Sessao getSessaoSugerida() {
		return sessaoSugerida;
	}

	public void setSessaoSugerida(Sessao sessaoSugerida) {
		this.sessaoSugerida = sessaoSugerida;
	}

	public String getDataHoraSessao(Sessao sessaoSugerida){
		String retorno = "";
		if(sessaoSugerida != null) {
			retorno = ComponentUtil.getSessaoManager().getDataHoraSessao(sessaoSugerida);
		}
		return retorno;
	}
	
	public List<Sessao> getSessoesJulgamento(){
		List<Sessao> retorno = new ArrayList<Sessao>();
		if(ComponentUtil.getTramitacaoProcessualService().recuperaProcesso() != null ) {
			retorno = ComponentUtil.getSessaoManager().getSessoesJulgamentoFuturas(ComponentUtil.getTramitacaoProcessualService().recuperaProcesso().getOrgaoJulgadorColegiado());
		}
		return retorno;
	}
	
	public boolean renderizaComboSessaoSugerida(){
		boolean retorno = false;
		if(ComponentUtil.getTramitacaoProcessualService().recuperaProcesso() != null ) {
			retorno = ComponentUtil.getProcessoTrfManager().permiteIndicarPauta(ComponentUtil.getTramitacaoProcessualService().recuperaProcesso());
		}
		return retorno;
	}

	public void gravarDataSugestaoSessao() {
		if(ComponentUtil.getTramitacaoProcessualService().recuperaProcesso() != null ) {
			ComponentUtil.getProcessoTrfManager().gravarSugestaoSessao(ComponentUtil.getTramitacaoProcessualService().recuperaProcesso(), this.getSessaoSugerida());
		}
	}
}