package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.transaction.Transaction;

import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.com.infox.cliente.home.ProcessoParteExpedienteHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpDocCertidaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.service.PessoaFisicaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoExpDocCertidao;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Esta é a classe de controle da tela
 * elaborarCertidaoPopUp.xhtml
 * @author Rodrigo Santos Menezes
 *
 */
@Name(ElaborarCertidaoExpedienteAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ElaborarCertidaoExpedienteAction implements ArquivoAssinadoUploader{
	
	public static final String NAME = "elaborarCertidaoExpedienteAction";
	
	private ProtocolarDocumentoBean protocolarDocumentoBean;
	
	private ProcessoTrf processoJudicial;
	
	private ProcessoParteExpediente processoParteExpediente;
	
	private ProcessoDocumento processoDocumentoCertidao;
	
	private boolean certidaoFinalizada = false;
	
	SecurityTokenControler stc = ComponentUtil.getComponent(SecurityTokenControler.NAME);
	
	@In
	private FacesMessages facesMessages;
	
	@In
	private PessoaFisicaService pessoaFisicaService;
	
	@In
	private UsuarioService usuarioService;
	
	@In
	private ProcessoParteExpedienteManager processoParteExpedienteManager;
	
	@In(create = true)
	private PrazosProcessuaisService prazosProcessuaisService;
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	
	@In
	private ProcessoExpDocCertidaoManager processoExpDocCertidaoManager; 
	
	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@RequestParameter(value="ppe")
	private Integer idPpe; 
	
	@RequestParameter(value="idProcesso")
	private Integer idProcesso;
	
	@Logger
	private Log log;
	
	@Create
	public void init(){
		if(stc.verificaChaveAcesso()!= null){
			try {
				processoJudicial = carregarProcessoJudicial();
				protocolarDocumentoBean = getProtocolarDocumentoBean();
				processoParteExpediente = carregarProcessoParteExpediente();
				if(processoJudicial == null || 
						processoJudicialComProcessoParteExpedienteDeProcessoDiferente(processoJudicial, processoParteExpediente)){
					facesMessages.add(Severity.ERROR, "Ocorreu um erro ao recuperar o processo judicial");
				} else if(processoParteExpediente != null){
					processoDocumentoCertidao = carregarProcessoDocumentoCertidao(processoParteExpediente);
				}
			} catch (PJeBusinessException e) {
				log.error(processoParteExpediente, "Erro ao carregar o processoParteExpediente para o processamento da certidão do expediente.");
				e.printStackTrace();
			}
			if(processoDocumentoCertidao != null && 
					processoDocumentoCertidao.getProcessoDocumentoBin() != null &&
					processoDocumentoCertidao.getProcessoDocumentoBin().getSignatarios().size() > 0){
				setCertidaoFinalizada(true);
			}
		}
	}
	
	private ProcessoTrf carregarProcessoJudicial() throws PJeBusinessException{
		ProcessoTrf proc;
		if(idProcesso != null && idProcesso != 0){
			proc = processoJudicialManager.findById(idProcesso);
		} else {
			proc = null;
		}
		
		return proc;
	}
	
	private ProcessoParteExpediente carregarProcessoParteExpediente() throws PJeBusinessException{
		ProcessoParteExpediente procParEx;
		if(idPpe != null && idPpe != 0){
			procParEx = processoParteExpedienteManager.findById(idPpe);
		} else {
			procParEx =  null;
		}
		return procParEx;
	}
	
	private ProcessoDocumento carregarProcessoDocumentoCertidao(ProcessoParteExpediente ppe){
		ProcessoDocumento docCert;
		
		docCert = processoExpDocCertidaoManager.retornaCertidao(ppe);
		
		if(docCert == null){
			docCert = documentoJudicialService.getDocumento();
			docCert.setProcesso(processoJudicial.getProcesso());
			docCert.setProcessoTrf(processoJudicial);
			docCert.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoCertidao());
		}
		
		protocolarDocumentoBean.setDocumentoPrincipal(docCert);
		
		return docCert;
	}
	
	private boolean processoJudicialComProcessoParteExpedienteDeProcessoDiferente(ProcessoTrf proc, ProcessoParteExpediente ppe){
		return proc != null && ppe != null && proc.getIdProcessoTrf() != ppe.getProcessoJudicial().getIdProcessoTrf();
	}
	
	public void concluirCertidao() {
		try {
			this.documentoJudicialService.gravarAssinaturaDeProcessoDocumento(this.protocolarDocumentoBean.getArquivosAssinados(), this.protocolarDocumentoBean.getProcessoDocumentosParaAssinatura());			
			
			this.salvar(false);
			
			boolean resultado = getProtocolarDocumentoBean().concluir();
			
			this.tratarPrazos();
			
			if (resultado == false) {
				throw new Exception("Não foi possível concluir o protocolo do documento!");
			}
			else {
				ProcessoParteExpedienteHome.instance().refreshGrid("processoParteExpedienteMenuGrid");
				this.facesMessages.add(Severity.INFO, "A certidão do expediente foi cadastrada");
			}
			setCertidaoFinalizada(true);
		}
		catch (Exception e) {
			e.printStackTrace();
			this.protocolarDocumentoBean.setArquivosAssinados(new ArrayList<ArquivoAssinadoHash>());
				
			try {
				Transaction.instance().rollback();
			} 
			catch (Exception e1) {
				e1.printStackTrace();
			}

			this.facesMessages.clear();
			this.facesMessages.add(Severity.ERROR, e.getMessage());		
		}			
	}
	
	private void tratarPrazos(){

		Map<OrgaoJulgador, Calendario> mapaCalendarios = new HashMap<OrgaoJulgador, Calendario>();
		
		Calendario calendario = mapaCalendarios.get(processoParteExpediente.getProcessoJudicial().getOrgaoJulgador());
			
		if (calendario == null) {
			
			OrgaoJulgador o = processoParteExpediente.getProcessoJudicial().getOrgaoJulgador();
			calendario = prazosProcessuaisService.obtemCalendario(o);
			mapaCalendarios.put(o, calendario);
		}
			
		FacesMessages.instance().clear();
		
		Pessoa p = (Pessoa) pessoaFisicaService.find(usuarioService.getUsuarioLogado().getIdUsuario());
			
		processoParteExpediente.setCienciaSistema(false);
		
		if(p != null && processoParteExpediente.getDtCienciaParte() == null){
			processoParteExpediente.setNomePessoaCiencia(p.getNome());
			processoParteExpediente.setPessoaCiencia(p);
		}
			
		processoParteExpedienteManager.registraCiencia(processoParteExpediente, new Date(), false, calendario);

		FacesMessages.instance().clear();

	}
	
	private void salvar(boolean deveFazerFlush){
		try {
			ProcessoExpDocCertidao procExpDocCert = new ProcessoExpDocCertidao();
			if(processoDocumentoCertidao != null && processoParteExpediente != null){
				procExpDocCert = processoExpDocCertidaoManager.retornaPorProcessoDocumentoEhProcessoParteExpediente(protocolarDocumentoBean.getDocumentoPrincipal(), processoParteExpediente);
				if(procExpDocCert == null){
					procExpDocCert = new ProcessoExpDocCertidao();
					procExpDocCert.setProcessoDocumentoCertidao(protocolarDocumentoBean.getDocumentoPrincipal());
					procExpDocCert.setProcessoParteExpediente(processoParteExpediente);
				}
				try {
					processoExpDocCertidaoManager.persist(procExpDocCert);
					if(deveFazerFlush){
						processoExpDocCertidaoManager.flush();
					}
				} catch (PJeBusinessException e) {
					e.printStackTrace();
				}			
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	public void salvar(){
		salvar(true);
	}
	
	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)
			throws Exception {
		this.protocolarDocumentoBean.addArquivoAssinado(arquivoAssinadoHash);
	}

	@Override
	public String getActionName() {
		return NAME;
	}
	
	
	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		if(protocolarDocumentoBean == null && processoJudicial != null){
			protocolarDocumentoBean =  new ProtocolarDocumentoBean(processoJudicial.getIdProcessoTrf(), 
																   true, 
																   false, 
																   true, 
																   true, 
																   false, 
																   false, 
																   false, 
																   NAME);
		}
		return protocolarDocumentoBean;
	}
	
	public ProcessoDocumento getProcessoDocumentoCertidao() {
		return processoDocumentoCertidao;
	}
	
	public void setProcessoDocumentoCertidao(ProcessoDocumento processoDocumentoCertidao) {
		this.processoDocumentoCertidao = processoDocumentoCertidao;
	}
	
	public ProcessoParteExpediente getProcessoParteExpediente() {
		return processoParteExpediente;
	}
	
	public void setProcessoParteExpediente(ProcessoParteExpediente processoParteExpediente) {
		this.processoParteExpediente = processoParteExpediente;
	}
	
	public boolean isCertidaoFinalizada() {
		return certidaoFinalizada;
	}
	
	public void setCertidaoFinalizada(boolean certidaoFinalizada) {
		this.certidaoFinalizada = certidaoFinalizada;
	}
}
