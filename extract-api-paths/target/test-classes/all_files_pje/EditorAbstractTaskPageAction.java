package br.com.infox.bpm.taskPage.FGPJE;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.bpm.ProcessInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.bpm.action.TaskAction;
import br.com.infox.bpm.action.TaskPageAction;
import br.com.infox.cliente.home.ProcessoDocumentoBinHome;
import br.com.infox.cliente.home.ProcessoDocumentoBinPessoaAssinaturaHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.ibpm.component.tree.EventosTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.actions.ModeloDocumentoAction;
import br.com.infox.pje.service.AssinaturaDocumentoService;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrfLocal;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;

public abstract class EditorAbstractTaskPageAction extends TaskAction implements Serializable {

	private static final long serialVersionUID = 1L;

	protected ProcessoDocumento processoDocumento;
	protected ProcessoDocumentoBin processoDocumentoBin;
	protected ProcessoDocumentoTrfLocal processoDocumentoTrfLocal;

	protected ModeloDocumento modeloDocumento;
	protected List<ModeloDocumento> modeloDocumentoItems;

	protected String certChain;
	protected String signature;

	protected AssinaturaDocumentoService documentoService = new AssinaturaDocumentoService();

	@In
	protected ProcessoDocumentoManager processoDocumentoManager;

	public void initPage() {
		if (processoDocumento != null) {
			return;
		}
		carregarOuCriarProcessoDocumento();
	}

	private void carregarOuCriarProcessoDocumento() {
		Integer idProcessoDocumento = (Integer) getTaskInstance().getVariable(getNomeVariavelIdDocumento());
		if (idProcessoDocumento != null) {
			processoDocumento = EntityUtil.find(ProcessoDocumento.class, idProcessoDocumento.intValue());
			obterProcessoDocumentoTrfLocal();
			processoDocumentoBin = getProcessoDocumento().getProcessoDocumentoBin();
			onSelectProcessoDocumento();
			carregaEventos();
		} else {
			criarProcessoDocumento();
		}
	}

	private void carregaEventos() {
		getEventosTreeHandler().carregaEventos(processoDocumento, processoDocumento.getTipoProcessoDocumento());
	}

	private void obterProcessoDocumentoTrfLocal() {
		processoDocumentoTrfLocal = EntityUtil.find(ProcessoDocumentoTrfLocal.class,
				processoDocumento.getIdProcessoDocumento());
		if (processoDocumentoTrfLocal == null) {
			processoDocumentoTrfLocal = new ProcessoDocumentoTrfLocal();
			processoDocumentoTrfLocal.setIdProcessoDocumentoTrf(processoDocumento.getIdProcessoDocumento());
			processoDocumentoTrfLocal.setProcessoDocumento(processoDocumento);
		}
	}

	protected abstract String getNomeVariavelIdDocumento();

	protected EventosTreeHandler getEventosTreeHandler() {
		return (EventosTreeHandler) EventosTreeHandler.instance();
	}

	public void onSelectProcessoDocumento() {
		ProcessoHome.instance().onSelectProcessoDocumento(processoDocumento.getTipoProcessoDocumento(), getEventosTreeHandler());
	}

	private void criarProcessoDocumento() {
		limparEventosTreeHandler();
		processoDocumento = new ProcessoDocumento();
		processoDocumentoBin = new ProcessoDocumentoBin();
		processoDocumento.setProcessoDocumentoBin(processoDocumentoBin);
		processoDocumentoTrfLocal = new ProcessoDocumentoTrfLocal();
		processoDocumentoTrfLocal.setProcessoDocumento(processoDocumento);
		processoDocumento.setIdJbpmTask(getTaskInstance().getId());
		if(getTaskInstance().getId() > 0) {
			processoDocumento.setExclusivoAtividadeEspecifica(Boolean.TRUE);
		}
		Contexts.getBusinessProcessContext().set(getNomeVariavelIdDocumento(), null);
		Contexts.getBusinessProcessContext().flush();
	}

	private void limparEventosTreeHandler() {
		getEventosTreeHandler().clearList();
		getEventosTreeHandler().clearTree();
		getEventosTreeHandler().setRegistred(false);
		ProcessoHome.instance().onSelectProcessoDocumento(processoDocumento.getTipoProcessoDocumento(), getEventosTreeHandler()); // p/
																	// renderEventsTree
																	// = false
	}

	protected String getNomeFluxoAtual() {
		return ProcessInstance.instance().getProcessDefinition().getName();
	}

	/**
	 * Metodo que testa se o documento do componente do fluxo já foi gravado ou
	 * não (possua conteudo).
	 * 
	 * @return
	 */
	public boolean isDocumentoVazio() {
		return processoDocumentoBin == null
				|| (!processoDocumentoBin.isBinario() && ProcessoDocumentoBinHome.isModeloVazio(processoDocumentoBin));

	}

	public void gravarDocumento() {
		try {
			inserirOuGravarDocumento();
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
			e.printStackTrace();
		}
	}

	protected void inserirOuGravarDocumento() throws Exception {
		try {
			setDescricaoProcessoDocumento();
			if (isDocumentoSalvo()) {
				updateDocumento();
			} else {
				inserirDocumento();
			}
			getEventosTreeHandler().registrarEventosJbpm(processoDocumento);
			EntityUtil.flush();
			updateTransitions();
		} catch (Exception e) {
			throw new Exception("Erro ao gravar documento", e);
		}
	}

	protected void setDescricaoProcessoDocumento() {
		processoDocumento.setProcessoDocumento(processoDocumento.getTipoProcessoDocumento().getTipoProcessoDocumento());
	}

	public boolean assinarDocumento() {
		try {
			inserirOuGravarDocumento();
		} catch (Exception e1) {
			FacesMessages.instance().add(Severity.ERROR, e1.getMessage());
			return false;
		}
		try {
			ProcessoDocumentoBinPessoaAssinaturaHome.instance().gravarAssinatura(getProcessoDocumento(), certChain,
					signature);
			getEventosTreeHandler().registraEventosProcessoDocumento(processoDocumento);
		} catch (CertificadoException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao assinar documento: " + e.getMessage(), e);
			return false;
		}
		EntityUtil.flush();
		criarProcessoDocumento();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Documento assinado com sucesso.");
		return true;
	}

	protected boolean isDocumentoSalvo() {
		return processoDocumento != null && processoDocumento.getIdProcessoDocumento() != 0;
	}

	private void inserirDocumento() {
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
			Contexts.getBusinessProcessContext().set(getNomeVariavelIdDocumento(),
					processoDocumento.getIdProcessoDocumento());
			Contexts.getBusinessProcessContext().flush();
		} catch (PJeBusinessException e) {
 			FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar o Documento: {0}", e.getMessage());
 			e.printStackTrace();
		}
	}

	private void updateDocumento() {
		EntityManager em = EntityUtil.getEntityManager();
		em.merge(processoDocumentoBin);
		em.merge(processoDocumento);
		em.merge(processoDocumentoTrfLocal);
		getEventosTreeHandler().registrarEventosJbpm(processoDocumento);
		em.flush();
	}

	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public ProcessoDocumentoBin getProcessoDocumentoBin() {
		return processoDocumentoBin;
	}

	public ProcessoDocumentoTrfLocal getProcessoDocumentoTrfLocal() {
		return processoDocumentoTrfLocal;
	}

	public String getCertChain() {
		return certChain;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public ModeloDocumento getModeloDocumento() {
		return modeloDocumento;
	}

	public void setModeloDocumento(ModeloDocumento modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	private List<ModeloDocumento> getModeloItems() {
		String nomeVariavelModelo = getNomeVariavelModelo();
		List<ModeloDocumento> modeloItems = ModeloDocumentoAction.instance().getModeloItems(nomeVariavelModelo);
		return modeloItems;
	}

	public List<ModeloDocumento> getModeloDocumentoItems() {
		if (modeloDocumentoItems == null) {
			modeloDocumentoItems = getModeloItems();
		}
		return modeloDocumentoItems;
	}

	private String getNomeVariavelModelo() {
		return TaskPageAction.instance().getPageName() + ModeloDocumentoAction.SUFIXO_VARIAVEL_MODELO;
	}

	public void processarModelo() {
		String modeloProcessado = ModeloDocumentoAction.instance().getConteudo(modeloDocumento);
		processoDocumentoBin.setModeloDocumento(modeloProcessado);
	}

}
