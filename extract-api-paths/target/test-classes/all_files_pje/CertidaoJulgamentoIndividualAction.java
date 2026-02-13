/**
 * 
 */
package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.codehaus.jettison.json.JSONArray;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.home.SessaoHome;
import br.com.infox.cliente.home.SessaoPautaProcessoTrfHome;
import br.com.infox.cliente.home.SessaoProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.view.CkEditorGeraDocumentoAbstractAction;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.util.StringUtil;

@Name(CertidaoJulgamentoIndividualAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class CertidaoJulgamentoIndividualAction extends CkEditorGeraDocumentoAbstractAction implements Serializable, ArquivoAssinadoUploader {
	@In(create = false, required = true)
	private FacesMessages facesMessages;
	private boolean assinado;
	private static final long serialVersionUID = 1L;
	public static final String NAME = "certidaoJulgamentoIndividualAction";
	private String modeloDocumento;
	private SessaoProcessoDocumento certidaoJulgamento = null;
	private Integer idSessaoPTRF;
	private SessaoPautaProcessoTrf sessaoPauta = null;
	private boolean uploadArquivoAssinadoRealizado;
	private boolean warning;
	private List<ProcessoDocumentoBinPessoaAssinatura> listaAssinatura;
	private static final LogProvider log = Logging.getLogProvider(CertidaoJulgamentoIndividualAction.class);
	ModeloDocumentoLocal modeloDocumentoLocal = null;
	
	@Override
	public String getActionName() {
		return NAME;
	}
	
	public Integer getIdSessaoPTRF() {
		return idSessaoPTRF;
	}

	public void setIdSessaoPTRF(Integer idSessaoPTRF) {
		this.idSessaoPTRF = idSessaoPTRF;
	}

	public void inicializar() {
		recuperarSessaoPauta();
		if(sessaoPauta != null) {
			setProtocolarDocumentoBean(new ProtocolarDocumentoBean(sessaoPauta.getProcessoTrf().getIdProcessoTrf(), ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL, NAME));
			carregarCertidaoJulgamento();
			carregarModeloDocumento();
			if(certidaoJulgamento == null) {
				recuperarNovaCertidaoJulgamento();
			}
			listaAssinatura = ComponentUtil.getProcessoDocumentoBinManager().obtemAssinaturas(getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin());
			this.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoCertidaoJulgamento());
			assinado = getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().getSignatarios() != null && !getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().getSignatarios().isEmpty();
			if(certidaoJulgamento == null) {
				facesMessages.add(Severity.ERROR, "Não foi possível recuperar uma certidão de julgamento!");
			}
		}
		limparArquivosAssinadosAnteriormente();
		if(getProtocolarDocumentoBean() == null) {
			facesMessages.add(Severity.ERROR, "Não foi possível recuperar informações do processo!");
		}
	}
	
	@Override
	public boolean isTipoProcessoDocumentoDefinido(){
		return true;
	}

	public boolean apresentarBotaoAssinar(){
		return !ComponentUtil.getTipoProcessoDocumentoPapelService().verificarExigibilidadeNaoAssina(Authenticator.getPapelAtual(), ParametroUtil.instance().getTipoProcessoDocumentoCertidaoJulgamento());
	}
	
	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.getProtocolarDocumentoBean().addArquivoAssinado(arquivoAssinadoHash);
		uploadArquivoAssinadoRealizado = Boolean.TRUE;
	}
	
	public void assinarDocumento() {
		atualizarCertidaoJulgamento();
		if (verificaCamposPreenchidos()) {
			atualizaDocumentoPrincipal();
			finalizarDocumento();	
		}
 	}
	
	public boolean verificaCamposPreenchidos() {
		boolean retorno = true;
 		warning = false;
		if (isDocumentoVazio() || !isTipoProcessoDocumentoDefinido()) {
			warning = true;
			facesMessages.clear();
			facesMessages.addFromResourceBundle(Severity.ERROR, "editorTexto.erro.salvarDocumentoVazio");
			retorno = false;
		} else { 
			if (certidaoJulgamento == null || certidaoJulgamento.getProcessoDocumento() == null ) {
				warning = true;
				facesMessages.clear();
				facesMessages.add(Severity.ERROR, "Certidão não está com todos os campos necessários à sua assinatura");
				retorno = false;
			} else {
				if( certidaoJulgamento.getProcessoDocumento().getProcessoTrf() == null ) {
					certidaoJulgamento.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(modeloDocumento);
					ProcessoTrf processo = ComponentUtil.getProcessoJudicialManager().recuperarProcesso(certidaoJulgamento.getProcessoDocumento());
					if(processo != null) {
						certidaoJulgamento.getProcessoDocumento().setProcessoTrf(processo);
						certidaoJulgamento.getProcessoDocumento().setProcesso(processo.getProcesso());
					} else {
						warning = true;
						facesMessages.clear();
						facesMessages.add(Severity.ERROR, "Certidão não está com todos os campos necessários à sua assinatura");
						retorno = false;
					}
				}
			}
		}
		return retorno;
	}
	
	private void recuperarSessaoPauta() {
		if(idSessaoPTRF != null) {
			sessaoPauta = ComponentUtil.getSessaoPautaProcessoTrfManager().getSessaoPautaProcessoTrfByID(idSessaoPTRF);
		}
	}
	
	private void atualizarCertidaoJulgamento() {
		if(certidaoJulgamento.getProcessoDocumento() == null || certidaoJulgamento.getProcessoDocumento().getIdProcessoDocumento() < 1
				|| certidaoJulgamento.getProcessoDocumento().getProcesso() == null || certidaoJulgamento.getProcessoDocumento().getProcessoTrf() == null ) {
			recuperarSessaoPauta();
			if(sessaoPauta != null) {
				carregarCertidaoJulgamento();
			}
		}
	}
	
	/**
	 * Atualiza o documento principal
	 *
	 * @throws PJeBusinessException
	 */
	public void atualizaDocumentoPrincipal() {
		getProtocolarDocumentoBean().setDocumentoPrincipal(certidaoJulgamento.getProcessoDocumento());
		getProtocolarDocumentoBean().getDocumentoPrincipal().setProcesso(certidaoJulgamento.getProcessoDocumento().getProcesso());
		getProtocolarDocumentoBean().getDocumentoPrincipal().setProcessoTrf(certidaoJulgamento.getProcessoDocumento().getProcessoTrf());
	}
	
	public boolean isUploadArquivoAssinadoRealizado(){
		return uploadArquivoAssinadoRealizado;
	}

	@Override
	public void salvar(String conteudo) {
		try {
			if(certidaoJulgamento == null ) {
				facesMessages.add(Severity.ERROR, "Não foi possível recuperar os dados para salvar a certidão");
			} else {
				getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().setModeloDocumento(conteudo);
				if(certidaoJulgamento == null || certidaoJulgamento.getProcessoDocumento() == null) {
					atualizarCertidaoJulgamento();
				}
				certidaoJulgamento.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(conteudo);
				ComponentUtil.getSessaoProcessoDocumentoManager().mergeAndFlush(certidaoJulgamento);
				getProtocolarDocumentoBean().gravarRascunho();
				this.modeloDocumento = conteudo;
				facesMessages.add(Severity.INFO, "Certidão gravada com sucesso!");
			}
		}
		catch (Exception e) {			
			facesMessages.add(Severity.ERROR, "Erro ao gravar a certidão de julgamento: " + e.getLocalizedMessage());			
		}
	}

	@Override
	public String getEstilosFormatacao() {
		return ComponentUtil.getEditorEstiloService().recuperarEstilosJSON();
	}
	
	@Override
	public boolean isFormularioPreenchido() {
		boolean retorno = false;
		if(getModeloDocumento() != null) {
			retorno = true;
		}
		return retorno;
	}
	
	private void carregarCertidaoJulgamento() {
		certidaoJulgamento = ComponentUtil.getSessaoProcessoDocumentoManager().recuperarSessaoProcessoDocumentoAtivoPorSessaoEhProcessoEhTipoProcessoDocumento(sessaoPauta.getSessao(), sessaoPauta.getProcessoTrf(), ParametroUtil.instance().getTipoProcessoDocumentoCertidaoJulgamento(), null);
		if(certidaoJulgamento != null && certidaoJulgamento.getProcessoDocumento() != null) {
			getProtocolarDocumentoBean().setDocumentoPrincipal(certidaoJulgamento.getProcessoDocumento());
			modeloDocumento = certidaoJulgamento.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento();
		}
	}
	
	private void recuperarNovaCertidaoJulgamento() {
		if(idSessaoPTRF != null) {
			certidaoJulgamento = new SessaoProcessoDocumento();
			sessaoPauta = ComponentUtil.getSessaoPautaProcessoTrfManager().getSessaoPautaProcessoTrfByID(idSessaoPTRF);
			if(sessaoPauta != null) {
				ProcessoDocumento pd;
				try {
					pd = ComponentUtil.getProcessoDocumentoManager().registrarProcessoDocumento(modeloDocumento, "Certidão de julgamento", ParametroUtil.instance().getTipoProcessoDocumentoCertidaoJulgamento(), sessaoPauta.getProcessoTrf());
					ComponentUtil.getSessaoProcessoDocumentoManager().registrarSessaoProcessoDocumento(pd, sessaoPauta);
					getProtocolarDocumentoBean().setDocumentoPrincipal(pd);
				} catch (PJeBusinessException e) {
					certidaoJulgamento = null;
				}
				
			} else {
				certidaoJulgamento = null;
			}
		} 
	}
	
	private void traduzirModelo(ModeloDocumento modelo) throws PJeBusinessException {
		ProcessoHome.instance().setInstance(sessaoPauta.getProcessoTrf().getProcesso());
		ProcessoTrfHome.instance().setProcessoTrf(sessaoPauta.getProcessoTrf());
		ProcessoTrfHome.instance().setInstance(sessaoPauta.getProcessoTrf());
		SessaoProcessoDocumentoHome.instance().setInstance(certidaoJulgamento);
		SessaoProcessoDocumentoHome.instance().setSessaoPautaProcessoTrf(sessaoPauta);
		SessaoProcessoDocumentoHome.instance().setSessao(sessaoPauta.getSessao());
		SessaoPautaProcessoTrfHome.instance().setInstance(sessaoPauta);
		SessaoHome.instance().setInstance(sessaoPauta.getSessao());
		recuperarModeloDocumentoLocal();
		if(getModeloDocumentoLocal() != null) {
			modeloDocumento = ComponentUtil.getModeloDocumentoManager().traduzirModelo(getModeloDocumentoLocal().getTipoEditor(), modelo.getModeloDocumento());
		}
	}
	
	private void carregarModeloDocumento() {
		try {
			ModeloDocumento modelo = ParametroUtil.instance().getModeloCertidaoJulgamento();
			if(modelo != null) {
				if(certidaoJulgamento == null || (certidaoJulgamento != null && (certidaoJulgamento.getProcessoDocumento() == null || certidaoJulgamento.getProcessoDocumento().getProcessoDocumentoBin() == null))) {
					traduzirModelo(modelo);
				} else {
					modeloDocumento = certidaoJulgamento.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento();
					if(StringUtil.isEmpty(modeloDocumento.trim()) && certidaoJulgamento.getProcessoDocumento().getDataJuntada() == null) {
						traduzirModelo(modelo);
					}
				}
			} else {
				facesMessages.add(Severity.ERROR, "Erro ao gerar conteúdo da certidão");
			}
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar modelo da certidão de julgamento, mensagem interna: " + e.getLocalizedMessage() + " " + e.getMessage());
		}
	}
	
	public String getModeloDocumento() {
		return modeloDocumento;
	}
	
	private ModeloDocumentoLocal getModeloDocumentoLocal() {
		return modeloDocumentoLocal;
	}

	private void recuperarModeloDocumentoLocal() {
		if(modeloDocumentoLocal == null && ParametroUtil.instance().getModeloCertidaoJulgamento() != null) {
			modeloDocumentoLocal = ComponentUtil.getModeloDocumentoLocalManager().findById(ParametroUtil.instance().getModeloCertidaoJulgamento().getIdModeloDocumento());
		}
	}

	public void setModeloDocumento(String modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	@Override
	public String getTiposDocumentosDisponiveis() {
		JSONArray retorno = new JSONArray();
		
		if(ParametroUtil.instance().getTipoProcessoDocumentoCertidaoJulgamento() != null) {
			retorno.put(ParametroUtil.instance().getTipoProcessoDocumentoCertidaoJulgamento().getTipoProcessoDocumento());
		}
		return retorno.toString();
	}

	@Override
	public String recuperarModeloDocumento(String modeloDocumento) {
		selecionarModeloProcessoDocumento(modeloDocumento);
		return getModeloDocumento();
	}
	
	@Override
	public boolean isDocumentoAssinado() throws PJeBusinessException {
		if(certidaoJulgamento != null && certidaoJulgamento.getProcessoDocumento() != null && ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(certidaoJulgamento.getProcessoDocumento().getIdProcessoDocumento())) {
			assinado = true;
		}
		return assinado;
	}

	@Override
	public void removerAssinatura() {
		// No implementado
	}

	@Override
	public void descartarDocumento() throws PJeBusinessException {
		if(certidaoJulgamento != null && certidaoJulgamento.getProcessoDocumento() != null) {
			getProtocolarDocumentoBean().setDocumentoPrincipal(certidaoJulgamento.getProcessoDocumento());
			if (getProtocolarDocumentoBean().getDocumentoPrincipal() != null) {
				getProtocolarDocumentoBean().acaoRemoverTodos();
				salvar("");
			}
		}
	}
	
	@Override
	public String obterConteudoDocumentoAtual() {
		String conteudo = this.getModeloDocumento();
		return ComponentUtil.getControleVersaoDocumentoManager().obterConteudoDocumentoJSON(conteudo);

	}

	@Override
	public String obterTiposVoto() {
		return null;
	}
	
	/**
	 * Verifica se o modelo de documento est nulo ou vazio.
	 *
	 * @return true se vazio ou nulo, false caso contrrio.
	 */
	public boolean isDocumentoVazio(){
		boolean retorno = Boolean.FALSE;

		if(getModeloDocumento() == null || "".equals(getModeloDocumento())){
			retorno = Boolean.TRUE;
		}

		return retorno;
	}
	
	@Override
	public boolean podeAssinar() {
		boolean retorno = false;
		if( isTipoProcessoDocumentoDefinido() && isDocumentoPersistido() && !isDocumentoVazio() && !isAssinado()) {
			retorno = apresentarBotaoAssinar();
		}
		return retorno;
	}
	
	public void finalizarDocumento() {
		try {
			this.getProtocolarDocumentoBean().concluirAssinaturaAction();
			if (!isDocumentoAssinado()){
				facesMessages.add(Severity.INFO, "O documento não foi assinado!");
			}
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Não foi possível finalizar o documento. {0}: {1}.",
					e.getClass().getCanonicalName(), e.getLocalizedMessage());

		} catch (PJeDAOException e) {
			facesMessages.add(Severity.ERROR, "Não foi possível finalizar o documento. {0}: {1}.",
					e.getClass().getCanonicalName(), e.getLocalizedMessage());

		} catch (CertificadoException e){
			facesMessages.add(Severity.ERROR, "Houve uma inconsistência na verificação da assinatura. {0}: {1}.",
					e.getClass().getCanonicalName(), e.getLocalizedMessage());
		} catch (Exception e) {
			log.error("Erro na finalização do documento da certidão " + e.getLocalizedMessage());
			facesMessages.add(Severity.ERROR, "Erro na finalização do documento. {0}: {1}.",
					e.getClass().getCanonicalName(), e.getLocalizedMessage());
		}
	}

	private void limparArquivosAssinadosAnteriormente() {
		getProtocolarDocumentoBean().getArquivosAssinados().clear();
		uploadArquivoAssinadoRealizado = Boolean.FALSE;
	}
	
	public List<ProcessoDocumentoBinPessoaAssinatura> getListaAssinatura() {
		return listaAssinatura;
	}

	public void setListaAssinatura(List<ProcessoDocumentoBinPessoaAssinatura> listaAssinatura) {
		this.listaAssinatura = listaAssinatura;
	}
	
	public boolean isWarning() {
		return warning;
	}

	public void setWarning(boolean warning) {
		this.warning = warning;
	}
	
	public boolean isAssinado() {
		return assinado;
	}
}












