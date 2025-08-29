package br.com.infox.bpm.taskPage.FGPJE;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.jboss.seam.annotations.In;
import org.jboss.seam.bpm.ProcessInstance;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;
import br.com.infox.bpm.action.TaskPageAction;
import br.com.infox.cliente.home.ProcessoDocumentoBinHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.ibpm.component.tree.EventsEditorTreeHandler;
import br.com.infox.ibpm.component.tree.EventsHomologarMovimentosTreeHandler;
import br.com.infox.ibpm.component.tree.EventsTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.actions.ModeloDocumentoAction;
import br.com.infox.pje.manager.ProcessoDocumentoTrfLocalManager;
import br.com.infox.pje.service.AssinaturaDocumentoService;
import br.com.infox.pje.service.IntimacaoPartesService;
import br.com.itx.component.Util;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrfLocal;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;

public abstract class MinutarTaskPageAction extends DarCienciaPartesTaskAction implements Serializable{

	private static final String MSG_EXCLUSAO_MINUTA = "Desistência por parte do usuário.";
	private static final long serialVersionUID = 1L;
	public static final String NAME = "minutarTaskPageAction";

	private static final String NOME_VARIAVEL_MODELO_ANTIGA = "MinutaModelo";

	private static final String AGRUPAMENTO_DECISAO = "Conclusão para Decisão";
	private static final String AGRUPAMENTO_MAGISTRADO = "Magistrado";
	private static final String AGRUPAMENTO_JULGAMENTO = "Conclusão para Julgamento";
	private static final String AGRUPAMENTO_DESPACHO = "Conclusão para Despacho";
	protected static final String EXP_MINUTA_ASSINADA = "isMinutaAssinada";

	protected ProcessoDocumento processoDocumento;
	protected ProcessoDocumentoBin processoDocumentoBin;
	protected ProcessoDocumentoTrfLocal processoDocumentoTrfLocal = new ProcessoDocumentoTrfLocal();

	protected ModeloDocumento modeloDocumento;
	protected List<ModeloDocumento> modeloDocumentoItems;

	protected String certChain;
	protected String signature;
	protected boolean minutaGravada = false;
	private boolean liberarConsultaPublica;

	protected AssinaturaDocumentoService documentoService = new AssinaturaDocumentoService();

	@In
	private GenericManager genericManager;
	@In
	private IntimacaoPartesService intimacaoPartesService;
	@In
	protected ProcessoDocumentoManager processoDocumentoManager;
	@In
	private ProcessoDocumentoTrfLocalManager processoDocumentoTrfLocalManager;
	@In
	private AssinaturaDocumentoService assinaturaDocumentoService;

	public ProcessoDocumento getUltimaMinuta(){
		return AtoMagistradoTaskPageAction.instance().getUltimaMinuta();
	}

	private void carregarOuCriarMinuta(){
		ProcessoDocumento ultimaMinutaNaoAssinada = getUltimaMinuta();
		if (isMinutaInvalida(ultimaMinutaNaoAssinada)){
			criarMinuta();
		}
		else{
			processoDocumento = ultimaMinutaNaoAssinada;
			processoDocumentoBin = processoDocumento.getProcessoDocumentoBin();
			processoDocumentoTrfLocal = genericManager.find(ProcessoDocumentoTrfLocal.class,
					processoDocumento.getIdProcessoDocumento());
			onSelectProcessoDocumento();
			// getEventosTreeHandler().carregaEventos(processoDocumento, processoDocumento.getTipoProcessoDocumento());
		}
	}

	private boolean isMinutaInvalida(ProcessoDocumento ultimaMinutaNaoAssinada){
		return ultimaMinutaNaoAssinada == null || documentoService.isProcessoDocumentoAssinado(ultimaMinutaNaoAssinada);
	}

	public void initPage(){
		if (processoDocumento != null){
			return;
		}
		carregarOuCriarMinuta();
	}

	public void onSelectProcessoDocumento(){
		ProcessoHome.instance().onSelectProcessoDocumento(processoDocumento.getTipoProcessoDocumento(), EventsTreeHandler.instance());
	}

	private void criarMinuta(){
		processoDocumento = new ProcessoDocumento();
		processoDocumentoBin = new ProcessoDocumentoBin();
		processoDocumento.setProcessoDocumentoBin(processoDocumentoBin);
		processoDocumentoTrfLocal = new ProcessoDocumentoTrfLocal();
		processoDocumentoTrfLocal.setProcessoDocumento(processoDocumento);
		processoDocumento.setIdJbpmTask(getTaskInstance().getId());
		if(getTaskInstance().getId() > 0) {
			processoDocumento.setExclusivoAtividadeEspecifica(Boolean.TRUE);
		}
	}

	protected String getNomeFluxoAtual(){
		return ProcessInstance.instance().getProcessDefinition().getName();
	}

	/**
	 * Metodo que testa se o documento do componente do fluxo já foi gravado ou não (possua conteudo).
	 * 
	 * @return
	 */
	public boolean isDocumentoVazio(){
		return processoDocumentoBin == null
			|| (!processoDocumentoBin.isBinario() && ProcessoDocumentoBinHome.isModeloVazio(processoDocumentoBin));

	}

	public boolean getShowAtoMagistradoButton(){
		if (isUserMagistrado()){
			return false;
		}
		return minutaGravada;
	}

	protected boolean isUserMagistrado(){
		return Authenticator.isMagistrado();
	}

	public void gravarMinuta() throws Exception{
		try{
			processoDocumentoBin.getModeloDocumento();
			if (isMinutaAtualSalva()){
				updateMinuta();
			}
			else{
				inserirMinuta();
			}
			minutaGravada = true;
			updateTransitions();
		} catch (Exception e){
			throw new Exception("Erro ao gravar minuta", e);
		}
	}

	/**
	 * Método de assinatura de documento na minuta. Depois de assinar a minuta o processo segue para o proximo passo do fluxo.
	 */
	public void assinarMinuta(){
		try{

			if (EventsTreeHandler.instance().getEventoBeanList().size() == 0){
				FacesMessages.instance().add(Severity.ERROR, "Nenhum evento selecionado");
				return;
			}

			EventsTreeHandler.instance().registraEventos();
			EventsEditorTreeHandler.instance().registraEventos();
			EventsHomologarMovimentosTreeHandler.instance().registraEventos();
			gravarMinuta();

			processoDocumento.setPapel(Authenticator.getPapelAtual());
			assinaturaDocumentoService.assinarDocumento(processoDocumento.getProcessoDocumentoBin(), signature,
					certChain);
			processoDocumentoManager.persist(processoDocumento);

			// getEventosTreeHandler().registraEventosProcessoDocumento(processoDocumento);
			processoDocumentoTrfLocalManager.criarDocumentoPublico(processoDocumento, liberarConsultaPublica);
			Util.setToEventContext(EXP_MINUTA_ASSINADA, true);

			updateTransitions();
			end(getNomeTarefaDestinoAposAssinatura());

			if (processoDocumento.getTipoProcessoDocumento().equals(
					ParametroUtil.instance().getTipoProcessoDocumentoSentenca())){
				intimarPartesAutomaticamente(processoDocumento);
			}
			else{
				setAvisoIntimacao("Processo remetido para ''Secretaria''.");
			}
			FacesMessages.instance().clear();

			Util.setToEventContext("canClosePanel", true);
		} catch (Exception e){
			e.printStackTrace();
			FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar minuta: " + e.getMessage(), e);
		}
	}

	@Override
	public void end(String transitionName){
		// getEventosTreeHandler().clearList();
		EventsTreeHandler.instance().clearList();
		if (isTarefaConhecimentoSecretaria(transitionName) && !isMinutaAssinada()){
			inativarMinuta();
		}
		super.end(transitionName);
	}

	protected abstract boolean isTarefaConhecimentoSecretaria(String transitionName);

	protected abstract String getNomeTarefaDestinoAposAssinatura();

	protected boolean isMinutaAtualSalva(){
		return processoDocumento != null && processoDocumento.getIdProcessoDocumento() != 0;
	}

	private void inserirMinuta(){
		try {
			ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
			Date dataInclusao = new Date();
			Usuario usuarioLogado = Authenticator.getUsuarioLogado();
			processoDocumento.setDataInclusao(dataInclusao);
			processoDocumento.setUsuarioInclusao(usuarioLogado);
			processoDocumentoBin.setDataInclusao(dataInclusao);
			processoDocumentoBin.setUsuario(usuarioLogado);
			processoDocumento = processoDocumentoManager.inserirProcessoDocumento(processoDocumento, processoTrf,
					processoDocumentoBin);
			processoDocumentoManager.inserirProcessoDocumentoTrfLocal(processoDocumento, processoDocumentoTrfLocal);
			EventsTreeHandler.instance().registraEventos();
			EventsEditorTreeHandler.instance().registraEventos();
			EventsHomologarMovimentosTreeHandler.instance().registraEventos();
		} catch (PJeBusinessException e) {
 			FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar o Documento: {0}", e.getMessage());
 			e.printStackTrace();
		}
	}

	private void updateMinuta(){
		genericManager.update(processoDocumentoBin);
		genericManager.update(processoDocumento);
		genericManager.update(processoDocumentoTrfLocal);
		EventsTreeHandler.instance().registraEventos();
		EventsEditorTreeHandler.instance().registraEventos();
		EventsHomologarMovimentosTreeHandler.instance().registraEventos();
		// getEventosTreeHandler().registrarEventosJbpm(processoDocumento);
	}

	public boolean isAssinaturaLiberada(){
		boolean possuiVisibilidadeMinuta = isUserMagistrado();
		return possuiVisibilidadeMinuta && (!isMinutaAtualSalva() || !isMinutaAssinada());
	}

	public boolean isMinutaAssinada(){
		return documentoService.isProcessoDocumentoAssinado(processoDocumento);
	}

	protected void inativarMinuta(){
		inativarMinuta(processoDocumento);
	}

	public void inativarMinuta(ProcessoDocumento processoDocumento){
		boolean minutaSalva = processoDocumento != null && processoDocumento.getIdProcessoDocumento() != 0;

		if (minutaSalva){
			try{
				processoDocumentoManager.excluirDocumento(processoDocumento, Authenticator.getUsuarioLogado(), MSG_EXCLUSAO_MINUTA);
			} catch (PJeBusinessException e){
				e.printStackTrace();
			}
		}
	}

	public ProcessoDocumento getProcessoDocumento(){
		return processoDocumento;
	}

	public ProcessoDocumentoBin getProcessoDocumentoBin(){
		return processoDocumentoBin;
	}

	public ProcessoDocumentoTrfLocal getProcessoDocumentoTrfLocal(){
		return processoDocumentoTrfLocal;
	}

	public String getCertChain(){
		return certChain;
	}

	public void setCertChain(String certChain){
		this.certChain = certChain;
	}

	public String getSignature(){
		return signature;
	}

	public void setSignature(String signature){
		this.signature = signature;
	}

	public ModeloDocumento getModeloDocumento(){
		return modeloDocumento;
	}

	public void setModeloDocumento(ModeloDocumento modeloDocumento){
		this.modeloDocumento = modeloDocumento;
	}

	private List<ModeloDocumento> getModeloItems(){
		String nomeVariavelModelo = getNomeVariavelModelo();
		List<ModeloDocumento> modeloItems = ModeloDocumentoAction.instance().getModeloItems(nomeVariavelModelo);
		if (modeloItems == null || modeloItems.isEmpty()){
			modeloItems = ModeloDocumentoAction.instance().getModeloItems(NOME_VARIAVEL_MODELO_ANTIGA);
		}
		return modeloItems;
	}

	public List<ModeloDocumento> getModeloDocumentoItems(){
		if (modeloDocumentoItems == null){
			modeloDocumentoItems = getModeloItems();
		}
		return modeloDocumentoItems;
	}

	private String getNomeVariavelModelo(){
		return TaskPageAction.instance().getPageName() + ModeloDocumentoAction.SUFIXO_VARIAVEL_MODELO;
	}

	public void processarModelo(){
		String modeloProcessado = ModeloDocumentoAction.instance().getConteudo(modeloDocumento);
		processoDocumentoBin.setModeloDocumento(modeloProcessado);
	}

	public boolean possuiEventoDespacho(){
		boolean possuiDespacho = ProcessoTrfHome.instance().possuiEventoTestandoExcludente(AGRUPAMENTO_DESPACHO,
				AGRUPAMENTO_MAGISTRADO)
			&& ProcessoTrfHome.instance().possuiEventoTestandoExcludente(AGRUPAMENTO_DESPACHO,
					AGRUPAMENTO_JULGAMENTO);
		return possuiDespacho;
	}

	public boolean possuiEventoDecisao(){
		boolean possuiDespacho = ProcessoTrfHome.instance().possuiEventoTestandoExcludente(AGRUPAMENTO_DECISAO,
				AGRUPAMENTO_MAGISTRADO)
			&& ProcessoTrfHome.instance().possuiEventoTestandoExcludente(AGRUPAMENTO_DECISAO,
					AGRUPAMENTO_JULGAMENTO);
		return possuiDespacho;
	}

	public void setLiberarConsultaPublica(boolean liberarConsultaPublica){
		this.liberarConsultaPublica = liberarConsultaPublica;
	}

	public boolean getLiberarConsultaPublica(){
		return liberarConsultaPublica;
	}

}
