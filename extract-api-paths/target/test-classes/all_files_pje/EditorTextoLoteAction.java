package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.ibpm.component.tree.EventsEditorTreeHandler;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.view.CkEditorGeraDocumentoAbstractAction;
import br.jus.cnj.pje.servicos.AtividadesLoteService;
import br.jus.cnj.pje.view.AtividadesLoteAction;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;


@Name(EditorTextoLoteAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EditorTextoLoteAction extends CkEditorGeraDocumentoAbstractAction implements Serializable, ArquivoAssinadoUploader {

	private static final long serialVersionUID = 6758707644654226885L;
	
	public static final String NAME = "editorTextoLoteAction";
	
	@RequestParameter
	private String[] idsProcessoSelecionado;
	private Boolean documentoAssinado;
	private Integer idMinuta;
	private boolean assinado;
	private boolean alert;
	private transient Map<String, ProtocolarDocumentoBean> grupos;
	private boolean controleVersaoHabilitado = false;
	
	@Logger
	private transient Log logger;
	
	@Create
	public void load(){
		if (idsProcessoSelecionado != null && idsProcessoSelecionado.length > 0) {
			grupos = new HashMap<String, ProtocolarDocumentoBean>();
			TaskInstance ti = null;
			ProtocolarDocumentoBean pdb = null;
			for (int i = 0; i < idsProcessoSelecionado.length; i++) {
    			ti = ManagedJbpmContext.instance().getTaskInstance(new Long(idsProcessoSelecionado[i]));
    			setTaskInstance(ti);
    			setProcessInstance(ti.getProcessInstance());
    			
    			pdb = new ProtocolarDocumentoBean(ComponentUtil.getTaskInstanceUtil().getProcesso(ti.getProcessInstance().getId()).getIdProcessoTrf(),
        				ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL | ProtocolarDocumentoBean.RECUPERA_DOCUMENTO_FLUXO, NAME);
        		
    			pdb.setDocumentoPrincipal(criarOuRecuperarDocumento(ti)); 
    			
    			grupos.put(idsProcessoSelecionado[i], pdb);

    			if(i == 0) {
					setProtocolarDocumentoBean(pdb);
					setTipoProcessoDocumento(getProtocolarDocumentoBean().getDocumentoPrincipal().getTipoProcessoDocumento());
					onSelectProcessoDocumentoEditorAssinatura(getTipoProcessoDocumento());
				}
    		}
			AtividadesLoteAction atividadesLoteAction = ComponentUtil.getComponent(AtividadesLoteAction.class);
			atividadesLoteAction.setModeloDocumento(null);
    	}
	}
	
	/**
	 * Cria ou recupera a minuta do processo caso o documento já exista limpa o conteúdo do modelo.
	 * 
	 * @param ti TaskInstance taskinstance do fluxo do processo.
	 * @return ProcessoDocumento entidade preenchida.
	 */
	private ProcessoDocumento criarOuRecuperarDocumento(TaskInstance ti) {
		ProcessoDocumento processoDocumento = recuperaProcessoDocumentoFluxo(ti.getProcessInstance());
		if (processoDocumento == null) {
			processoDocumento = ComponentUtil.getDocumentoJudicialService().getDocumento();
		}
		return processoDocumento;
	}
	
	@Override
	public void setModeloDocumentoSelecionado(ModeloDocumento modelo){
		if(getProtocolarDocumentoBean() != null && getProtocolarDocumentoBean().getDocumentoPrincipal() != null) {
			getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().setModeloDocumento(modelo.getModeloDocumento());
		} else {
			getProtocolarDocumentoBean().setDocumentoPrincipal(ComponentUtil.getDocumentoJudicialService().getDocumento());
		}
		AtividadesLoteAction atividadesLoteAction = ComponentUtil.getComponent(AtividadesLoteAction.class);
		atividadesLoteAction.setModeloDocumento(modelo.getModeloDocumento());
	}
	
	@Override
	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		super.setTipoProcessoDocumento(tipoProcessoDocumento);
		ProcessoHome.instance().setTipoProcessoDocumento(tipoProcessoDocumento);
	}
	
	@Override
	public String recuperarModeloDocumento(String modeloDocumento){
		selecionarModeloProcessoDocumento(modeloDocumento);
		return getModeloDocumento();
	}
	
	public String getModeloDocumento() {
		if (getProtocolarDocumentoBean() != null && getProtocolarDocumentoBean().getDocumentoPrincipal() != null &&
				getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin() != null) {

			return getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().getModeloDocumento();
		}
		return null;
	}

	@Override
	public boolean isDocumentoAssinado() throws PJeBusinessException{
		boolean retorno = (documentoAssinado != null && documentoAssinado);
		if(!retorno && getProtocolarDocumentoBean() != null
			&& getProtocolarDocumentoBean().getDocumentoPrincipal() != null 
			&& getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin() != null){
			retorno = (!getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().getSignatarios().isEmpty());
		}
		return retorno;
	}
	
	private ProtocolarDocumentoBean carregarDocumentoPrincipalPorVariavelFluxoOuCriarNovo(ProtocolarDocumentoBean protocolarDocumentoBean, TaskInstance ti) {
		ProcessoDocumento documentoPrincipal = recuperaProcessoDocumentoFluxo(ti.getProcessInstance());
		
		if(documentoPrincipal != null && documentoPrincipal.getIdProcessoDocumento() > 0){
			protocolarDocumentoBean.setDocumentoPrincipal(documentoPrincipal);
			protocolarDocumentoBean.loadArquivosAnexadosDocumentoPrincipal();
			assinado = !protocolarDocumentoBean.getDocumentoPrincipal().getProcessoDocumentoBin().getSignatarios().isEmpty();
		}else{
			protocolarDocumentoBean.setDocumentoPrincipal(ComponentUtil.getDocumentoJudicialService().getDocumento());
		}
		
		return protocolarDocumentoBean;
	}
	
	public ProcessoDocumento recuperaProcessoDocumentoFluxo(ProcessInstance processInstance){
		ProcessoDocumento documentoPrincipal = null; 
		if(processInstance != null){
			Integer idMinutaFluxo = (Integer) processInstance.getContextInstance().getVariable(Variaveis.MINUTA_EM_ELABORACAO);
			
		    if (idMinutaFluxo == null && getTaskInstance() != null) {
		    	idMinutaFluxo = (Integer) getTaskInstance().getProcessInstance().getContextInstance().getVariable(Variaveis.MINUTA_EM_ELABORACAO);
		    }
		    if (idMinutaFluxo != null) {
		        try {
					documentoPrincipal = ComponentUtil.getDocumentoJudicialService().getDocumento(idMinutaFluxo);
				} catch (PJeBusinessException e) {
					FacesMessages.instance().add(Severity.ERROR, "No foi possvel obter o documento principal [{0}:{1}]", e.getClass().getCanonicalName(), e.getLocalizedMessage());
				}
		    }
		}
		return documentoPrincipal;
	}
	
	public void onSelectProcessoDocumentoEditorAssinatura(TipoProcessoDocumento tipoProcessoDocumento)
	{
		this.setTipoProcessoDocumento(tipoProcessoDocumento);
		defineEstadoComponenteArvoreMovimentacoesProcessuais(tipoProcessoDocumento);
		ProcessoHome.instance().setTipoProcessoDocumento(tipoProcessoDocumento);
	}
	
	private void defineEstadoComponenteArvoreMovimentacoesProcessuais(TipoProcessoDocumento tipoProcessoDocumento) {
		reiniciaComponenteArvoreMovimentacoesProcessuais();

		if (tipoProcessoDocumento != null && tipoProcessoDocumento.getAgrupamento() != null){

			Integer idAgrupamentos = tipoProcessoDocumento.getAgrupamento().getIdAgrupamento();

			if (idAgrupamentos != null && idAgrupamentos > 0){
				EventsEditorTreeHandler.instance().setRootsSelectedMap(new HashMap<Evento, List<Evento>>());
				EventsEditorTreeHandler.instance().getRoots(idAgrupamentos);
			}
		}
	}
	
	private void reiniciaComponenteArvoreMovimentacoesProcessuais() {
		EventsEditorTreeHandler.instance().clearList();
		EventsEditorTreeHandler.instance().clearTree();
	}

	@Override
	public void removerAssinatura() {
		try {
			alert=ProcessoDocumentoHome.instance().verificarDocumentoEventoRelacionado(getProtocolarDocumentoBean().getDocumentoPrincipal());
			if (alert){
				return ;
			}
			br.com.infox.pje.service.AssinaturaDocumentoService assinaturaDocumentoService = ComponentUtil.getComponent("assinaturaDocumentoService");

			if(ComponentUtil.getDocumentoJudicialService().temAssinatura(getProtocolarDocumentoBean().getDocumentoPrincipal())){
				getProtocolarDocumentoBean().getDocumentoPrincipal().setDataJuntada(null);
				assinaturaDocumentoService.removeAssinatura(getProtocolarDocumentoBean().getDocumentoPrincipal());
			}

			if(getProtocolarDocumentoBean().getArquivos() != null && !getProtocolarDocumentoBean().getArquivos().isEmpty()) {
				for (ProcessoDocumento arquivo : getProtocolarDocumentoBean().getArquivos()) {
					if(ComponentUtil.getDocumentoJudicialService().temAssinatura(arquivo)){
						arquivo.setDataJuntada(null);
						assinaturaDocumentoService.removeAssinatura(arquivo);
					}
				}
			}

			assinado = false;

			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Assinatura removida com sucesso.");
		} catch (Exception e) {
			logger.error(e);
 			FacesMessages.instance().add(Severity.ERROR, "Erro ao remover a assinatura.");
		}
	}

	@Override
	public void descartarDocumento() throws PJeBusinessException {
		if (alert){
			return ;
		}
		
		carregarDocumentoPrincipalPorVariavelFluxoOuCriarNovo(getProtocolarDocumentoBean(), getTaskInstance());
		
		if (getProtocolarDocumentoBean().getDocumentoPrincipal() != null) {
			getProtocolarDocumentoBean().acaoRemoverTodos();
			salvar("");
		}
 		Contexts.getBusinessProcessContext().flush();
 	}

	@Override
	public String obterConteudoDocumentoAtual() {
		String conteudo = this.getModeloDocumento();
		return ComponentUtil.getControleVersaoDocumentoManager().obterConteudoDocumentoJSON(conteudo);
	}
	
	private ProcessoDocumentoBin refreshProcessoDocumentoBin(ProtocolarDocumentoBean protocolarDocumentoBean) throws PJeBusinessException {
		if(protocolarDocumentoBean.getDocumentoPrincipal() != null && protocolarDocumentoBean.getDocumentoPrincipal().getIdProcessoDocumento() != 0) {
			return ComponentUtil.getProcessoDocumentoBinManager().refresh(protocolarDocumentoBean.getDocumentoPrincipal().getProcessoDocumentoBin());
		}
		return protocolarDocumentoBean.getDocumentoPrincipal().getProcessoDocumentoBin();
	}

	@Override
	public void salvar(String conteudo) {
		AtividadesLoteAction atividadesLoteAction = ComponentUtil.getComponent(AtividadesLoteAction.class);
		AtividadesLoteService service = ComponentUtil.getComponent(AtividadesLoteService.class);

		try {
			Set<String> els = ModeloDocumentoManager.instance().getElsModelo(conteudo);
			ProtocolarDocumentoBean pdb = null;
			for(Entry<String, ProtocolarDocumentoBean> grupo : grupos.entrySet()) {
				pdb = grupo.getValue();
				service.minutarEmLote(pdb.getProcessoJudicial(), null, null, null, null);
				ComponentUtil.getModeloDocumentoManager().carregarInstanciasParaTraducao(pdb.getProcessoJudicial());
				pdb.getDocumentoPrincipal().setProcessoDocumentoBin(refreshProcessoDocumentoBin(pdb));
				pdb.getDocumentoPrincipal().getProcessoDocumentoBin().setModeloDocumento(ComponentUtil.getModeloDocumentoManager().obtemConteudo(conteudo.replaceAll("&#39;", "'")));
				for (String ct: els) {
                	Contexts.getConversationContext().remove(ct);
                }
			}
		} catch (PJeBusinessException e) {
			logger.error(e);
			FacesMessages.instance().add(Severity.ERROR,"Erro ao recuperar o conteudo do documento");
		}
		
		atividadesLoteAction.setModeloDocumento(conteudo);
		
		this.salvar();
	}
	
	public void salvar() {
		if (verificaCamposPreenchidos(true)){
			try {
				TaskInstance ti = null;
				ProtocolarDocumentoBean pdb = null;
				for(Entry<String, ProtocolarDocumentoBean> grupo : grupos.entrySet()) {
					String id = grupo.getKey();
					pdb = grupo.getValue();
					pdb.getDocumentoPrincipal().setTipoProcessoDocumento(getTipoProcessoDocumento());
					pdb.getDocumentoPrincipal().setProcessoDocumento(getTipoProcessoDocumento().getTipoProcessoDocumento());
					if(pdb.getArquivos().isEmpty()) {
						vincularAnexos(pdb);
					}
	    			pdb.gravarRascunho(controleVersaoHabilitado);
	    			idMinuta = pdb.getDocumentoPrincipal().getIdProcessoDocumento();
	    			ti = ManagedJbpmContext.instance().getTaskInstance(new Long(id));
	    			ti.getProcessInstance().getContextInstance().setVariable(Variaveis.MINUTA_EM_ELABORACAO, idMinuta);
				}
			} catch (Exception e){
				logger.error(e);
				FacesMessages.instance().add(Severity.ERROR, "No foi possvel gravar o documento [{0}:{1}]", e.getClass().getCanonicalName(), e.getLocalizedMessage());
			}
		}
	}

	/**
	 * Metodo responsavel por vincular os anexos a cada documento da lista
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws PJeBusinessException 
	 */
	private void vincularAnexos(ProtocolarDocumentoBean pdb) throws InstantiationException, IllegalAccessException{
		try {
			List<ProcessoDocumento> arquivos = getProtocolarDocumentoBean().getArquivos();
			
			if(!arquivos.isEmpty()) {
				ArrayList<ProcessoDocumento> arquivosFinal = new ArrayList<ProcessoDocumento>();
				ProcessoDocumento processoDocumentoTemp;
				for (ProcessoDocumento processoDocumentoAnexo : arquivos) {
					ProcessoDocumentoBin processoDocumentoBinTemp = EntityUtil.cloneEntity(processoDocumentoAnexo.getProcessoDocumentoBin(), false);
					processoDocumentoBinTemp.setIdProcessoDocumentoBin(0);
					processoDocumentoBinTemp.setBinario(true);
					ComponentUtil.getProcessoDocumentoBinManager().persist(processoDocumentoBinTemp);

					processoDocumentoTemp = ComponentUtil.getDocumentoJudicialService().retornaCopiaProcessoDocumento(processoDocumentoAnexo);
					processoDocumentoTemp.setIdProcessoDocumento(0);
					processoDocumentoTemp.setDocumentoPrincipal(pdb.getDocumentoPrincipal());
					processoDocumentoTemp.setIdJbpmTask(pdb.getDocumentoPrincipal().getIdJbpmTask());
					processoDocumentoTemp.setProcessoTrf(pdb.getProcessoJudicial());
					processoDocumentoTemp.setProcesso(pdb.getProcessoJudicial().getProcesso());
					processoDocumentoTemp.setProcessoDocumentoBin(processoDocumentoBinTemp);
					ComponentUtil.getDocumentoJudicialService().persist(processoDocumentoTemp, true);
					arquivosFinal.add(processoDocumentoTemp);
				}
				pdb.setArquivos(arquivosFinal);
				ComponentUtil.getDocumentoJudicialService().flush();
			}
		} catch (PJeBusinessException e) {
			logger.error(e);
		}
	}
	
	/**
	 * Copia o arquivo anexado para todos os documentos da lista
	 */
	@Override
	public void selecionarAnexosConcluido() {
		salvar();
	}
	
	public boolean verificaCamposPreenchidos(boolean mensagem) {
		boolean retorno = true;
		if (isDocumentoVazio() || !isTipoProcessoDocumentoDefinido()) {
			if (mensagem) {
				FacesMessages.instance().clear();
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "editorTexto.erro.salvarDocumentoVazio");
			}
			retorno = false;
		}
		return retorno;
	}
	
	public boolean isDocumentoVazio(){
		boolean result = Boolean.FALSE;

		if(getModeloDocumento() == null || "".equals(getModeloDocumento())){
			result = Boolean.TRUE;
		}

		return result;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.getProtocolarDocumentoBean().addArquivoAssinado(arquivoAssinadoHash);
	}
	
	@Override
	public String getActionName() {
		return NAME;
	}

	public boolean isAssinado() {
		return assinado;
	}

	public void setAssinado(boolean assinado) {
		this.assinado = assinado;
	}

	@Override
	public boolean podeAssinar() {
		return false;
	}

	@Override
	public String obterTiposVoto() {
		return null;
	}
}
