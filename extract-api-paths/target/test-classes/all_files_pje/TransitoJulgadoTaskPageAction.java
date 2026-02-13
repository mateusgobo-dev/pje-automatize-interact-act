package br.com.infox.bpm.taskPage.FGPJE;

import java.util.Date;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.bpm.action.TaskAction;
import br.com.infox.cliente.home.ProcessoDocumentoBinPessoaAssinaturaHome;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.ibpm.component.tree.AutomaticEventsTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.service.TipoProcessoDocumentoPapelService;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrfLocal;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.Crypto;

@Name(value = TransitoJulgadoTaskPageAction.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class TransitoJulgadoTaskPageAction extends TaskAction {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "transitoJulgadoTaskPageAction";

	private static final String NOME_VAR_TRANSITO_JULGADO = "textEditSignature:Transito_em_Julgado";

	private Date dataTransitoJulgado;

	private String modeloDocumento;

	private String certChain;
	private String signature;

	private ProcessoDocumento processoDocumento;
	private ProcessoDocumentoBin processoDocumentoBin;
	private ProcessoDocumentoTrfLocal processoDocumentoTrfLocal;

	public void initPage() {
		if (getProcessoDocumento() != null) {
			return;
		}
		carregarTransitoJulgado();
	}

	private void carregarTransitoJulgado() {
		Integer idProcessoDocumento = (Integer) getTaskInstance().getVariable(NOME_VAR_TRANSITO_JULGADO);
		if (idProcessoDocumento != null) {
			ProcessoDocumentoBinPessoaAssinaturaHome pdbHome = new ProcessoDocumentoBinPessoaAssinaturaHome();
			setProcessoDocumento(EntityUtil.find(ProcessoDocumento.class, idProcessoDocumento.intValue()));
			String id = String.valueOf(getProcessoDocumento().getProcessoDocumentoBin().getIdProcessoDocumentoBin());
			if (pdbHome.listaAssinatura(id)) {
				criarProcessoDocumento();
			} else {
				setProcessoDocumentoTrfLocal(EntityUtil.find(ProcessoDocumentoTrfLocal.class,
						idProcessoDocumento.intValue()));
				processoDocumentoBin = getProcessoDocumento().getProcessoDocumentoBin();
			}
		} else {
			criarProcessoDocumento();
		}
	}

	private void criarProcessoDocumento() {
		setProcessoDocumento(new ProcessoDocumento());
		processoDocumentoBin = new ProcessoDocumentoBin();
		getProcessoDocumento().setProcessoDocumentoBin(processoDocumentoBin);
		setProcessoDocumentoTrfLocal(new ProcessoDocumentoTrfLocal());
		getProcessoDocumentoTrfLocal().setProcessoDocumento(getProcessoDocumento());
		getProcessoDocumento().setIdJbpmTask(getTaskInstance().getId());
		if(getTaskInstance().getId() > 0) {
			getProcessoDocumento().setExclusivoAtividadeEspecifica(Boolean.TRUE);
		}
		setDataTransitoJulgado(null);
		Contexts.getBusinessProcessContext().set(NOME_VAR_TRANSITO_JULGADO, null);
		Contexts.getBusinessProcessContext().flush();
	}

	public void assinarTransitoJulgado() {
		gravarTransitoJulgado();
		try {
			ProcessoDocumentoBinPessoaAssinaturaHome.instance().gravarAssinatura(getProcessoDocumento(), certChain,
					signature);
		} catch (CertificadoException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao assinar documento: " + e.getMessage(), e);
			return;
		}
		int idProcesso = ProcessoHome.instance().getInstance().getIdProcesso();
		ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, idProcesso);
		processoTrf.setDtTransitadoJulgado(dataTransitoJulgado);
		getEntityManager().merge(processoTrf);
		AutomaticEventsTreeHandler.instance().registraEventosProcessoDocumento(processoDocumento);
		EntityUtil.flush();
		criarProcessoDocumento();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Documento assinado com sucesso.");

	}

	public void gravarTransitoJulgado() {
		EntityManager em = EntityUtil.getEntityManager();
		Date dataInclusao = new Date();
		getProcessoDocumentoBin().setDataInclusao(dataInclusao);
		em.persist(getProcessoDocumentoBin());
		Authenticator.instance();
		Usuario usuarioLogado = Authenticator.getUsuarioLogado();
		getProcessoDocumento().setUsuarioInclusao(usuarioLogado);
		getProcessoDocumento().setAtivo(true);
		getProcessoDocumento().setDataInclusao(dataInclusao);
		getProcessoDocumento().setProcesso(ProcessoHome.instance().getInstance());
		getProcessoDocumento().setProcessoDocumento(
				getProcessoDocumento().getTipoProcessoDocumento().getTipoProcessoDocumento());
		getProcessoDocumentoBin().setUsuario(usuarioLogado);
		getProcessoDocumento().setProcessoDocumentoBin(getProcessoDocumentoBin());
		em.persist(getProcessoDocumento());
		em.flush();
		getProcessoDocumentoTrfLocal().setProcessoDocumento(getProcessoDocumento());
		getProcessoDocumentoTrfLocal().setIdProcessoDocumentoTrf(getProcessoDocumento().getIdProcessoDocumento());
		em.persist(getProcessoDocumentoTrfLocal());
		Contexts.getBusinessProcessContext().set(NOME_VAR_TRANSITO_JULGADO, processoDocumento.getIdProcessoDocumento());
		AutomaticEventsTreeHandler.instance().registrarEventosJbpm(getProcessoDocumento());
		em.flush();
		Contexts.getBusinessProcessContext().flush();
		ProcessoDocumentoHome.instance().setInstance(getProcessoDocumento());
		FacesMessages.instance().add(Severity.INFO, "Documento gravado com sucesso.");
	}
	
	/**
 	 * Verifica se o botao de assinatura deve ser apresentado devido ao vinculo do papel com o tipo de documento.
 	 * 
 	 * @return True se o papel esta vinculado.
 	 */
 	public boolean isApresentaBotaoAssinar(){ 
 		TipoProcessoDocumentoPapelService tipoProcessoDocumentoPapelService = ComponentUtil.getComponent("tipoProcessoDocumentoPapelService");
 		return !tipoProcessoDocumentoPapelService.verificarExigibilidadeNaoAssina(
 				Authenticator.getPapelAtual(), getProcessoDocumento().getTipoProcessoDocumento());
	}

	public void onSelectProcessoDocumento() {
		ProcessoHome.instance().onSelectProcessoDocumento(processoDocumento.getTipoProcessoDocumento(), AutomaticEventsTreeHandler.instance());
	}

	public void setDataTransitoJulgado(Date dataTransitoJulgado) {
		this.dataTransitoJulgado = dataTransitoJulgado;
	}

	public Date getDataTransitoJulgado() {
		return dataTransitoJulgado;
	}

	public void setModeloDocumento(String modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	public String getModeloDocumento() {
		return modeloDocumento;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	public String getCertChain() {
		return certChain;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getSignature() {
		return signature;
	}

	public void setProcessoDocumentoBin(ProcessoDocumentoBin processoDocumentoBin) {
		this.processoDocumentoBin = processoDocumentoBin;
	}

	public ProcessoDocumentoBin getProcessoDocumentoBin() {
		return processoDocumentoBin;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumentoTrfLocal(ProcessoDocumentoTrfLocal processoDocumentoTrfLocal) {
		this.processoDocumentoTrfLocal = processoDocumentoTrfLocal;
	}

	public ProcessoDocumentoTrfLocal getProcessoDocumentoTrfLocal() {
		return processoDocumentoTrfLocal;
	}

	public boolean isTarefaBaixa(String transitionName) {
		return TaskNamesPrimeiroGrau.BAIXA.equals(transitionName);
	}

}
