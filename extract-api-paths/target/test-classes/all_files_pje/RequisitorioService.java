package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.manager.SituacaoProcessoManager;
import br.com.infox.utils.Constantes;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.cnj.pje.webservice.client.PjeApiClient;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrfLocal;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.Crypto;

@Name(RequisitorioService.NAME)
@Scope(ScopeType.CONVERSATION)
public class RequisitorioService implements Serializable, ArquivoAssinadoUploader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RequisitorioService.class);

	public static final String NAME = "requisitorioService";

	private static final long serialVersionUID = -3341292070832596635L;	
	
	private static final String API_REQUISITORIO = "/requisitorio/api";
	private static final String API_V1 = API_REQUISITORIO + "/v1";
	private static final String REQUISITORIO_PATH = API_V1 + "/requisitorios";
	
	private static final String ENVIO_MANUAL_PATH =  API_V1 + REQUISITORIO_PATH + "/envio-manual";	
	
	private static final String VARIAVEL_NUMERO_REQUISITORIO = "numeroRequisitorio";
	private static final String VARIAVEL_ID_REQUISITORIO = "idRequisitorio";
	private static final String VARIAVEL_TIPO_REQUISITORIO = "tipoRequisitorio";
	private static final String VARIAVEL_EXECUTADO = "executado";
	private static final String VARIAVEL_ID_PD_REQUISITORIO = "idPDRequisitorio";
	

	private boolean processoJaInstanciadoNoFluxoDoRequisitorio = false;
	private String parametroNumeroRequisitorio;
	private String parametroIdRequisitorio;
	private SituacaoProcesso situacaoProcessoFluxoRequisitorio;
	private String parametroTransition;
	private String oficioStr64;
	private String oficioStr;
	private String tipoRequisitorio;
	private ArquivoAssinadoHash arquivoAssinado;
	private boolean executado;

	@In private FluxoManager fluxoManager;
	@In private SituacaoProcessoManager situacaoProcessoManager;
	@In private DocumentoJudicialService documentoJudicialService;
	@In private ModeloDocumentoManager modeloDocumentoManager;
	@In private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;
	@In private ProcessoDocumentoManager processoDocumentoManager;

	@Create
	public void init() {
		String codFluxoRequisitorio = (String) TaskInstanceUtil.instance()
				.getVariable(Variaveis.NOME_VARIAVEL_COD_FLUXO_REQUISITORIO);
		Fluxo fluxoRequisitorio = fluxoManager.findByCodigo(codFluxoRequisitorio);
		int idProcessoTrf = ProcessoTrfHome.instance().getInstance().getIdProcessoTrf();
		if (fluxoRequisitorio != null && idProcessoTrf != 0) {
			situacaoProcessoFluxoRequisitorio = situacaoProcessoManager
					.getSituacaoProcessoByIdProcessoFluxo(idProcessoTrf, fluxoRequisitorio);
			setProcessoJaInstanciadoNoFluxoDoRequisitorio(situacaoProcessoFluxoRequisitorio != null);
		}
	}

	public void moverParaParametroTransition() {
		if (parametroTransition != null) {
			String transicaoSaida = (String) TaskInstanceUtil.instance().getVariable(parametroTransition);
			if (transicaoSaida != null) {
				TaskInstanceHome.instance().end(transicaoSaida);
			}
		}
	}
	public String getActionName() {
		return NAME;
	}

	public void vincularVariaveis() {
		Long idTaskInstanceAtual = TaskInstance.instance().getId();
		if (situacaoProcessoFluxoRequisitorio != null) {
			JbpmUtil.resumeTask(situacaoProcessoFluxoRequisitorio.getIdTaskInstance());
			JbpmUtil.setProcessVariable(VARIAVEL_NUMERO_REQUISITORIO, parametroNumeroRequisitorio);
			JbpmUtil.setProcessVariable(VARIAVEL_ID_REQUISITORIO, parametroIdRequisitorio);
			JbpmUtil.setProcessVariable(VARIAVEL_TIPO_REQUISITORIO, tipoRequisitorio);
			JbpmUtil.setProcessVariable(VARIAVEL_EXECUTADO, executado);
			JbpmUtil.resumeTask(idTaskInstanceAtual);
		}
	}

	@Transactional
	public void instanciarAtualizarProcessoNoFluxoDoRequisitorio() {
		String codFluxoRequisitorio = (String) TaskInstanceUtil.instance()
				.getVariable(Variaveis.NOME_VARIAVEL_COD_FLUXO_REQUISITORIO);
		Fluxo fluxoRequisitorio = fluxoManager.findByCodigo(codFluxoRequisitorio);
		int idProcessoTrf = ProcessoTrfHome.instance().getInstance().getIdProcessoTrf();
		if (fluxoRequisitorio != null && idProcessoTrf != 0) {
			try {
				ProcessoTrfHome.instance().inserirProcessoNoFluxo(idProcessoTrf, fluxoRequisitorio);
				JbpmUtil.setProcessVariable(VARIAVEL_NUMERO_REQUISITORIO, parametroNumeroRequisitorio);
				JbpmUtil.setProcessVariable(VARIAVEL_ID_REQUISITORIO, parametroIdRequisitorio);
				JbpmUtil.setProcessVariable(VARIAVEL_TIPO_REQUISITORIO, tipoRequisitorio);
				JbpmUtil.setProcessVariable(VARIAVEL_EXECUTADO, executado);
				
				Base64 decoder = new Base64();
				byte[] decodedBytes = decoder.decode(oficioStr64.getBytes(StandardCharsets.UTF_8));				
				ProcessoDocumento pdRequisitorio = criarDocumentoRequisitorio(new String(decodedBytes, StandardCharsets.ISO_8859_1));
				if (pdRequisitorio != null && pdRequisitorio.getIdProcessoDocumento() > 0) {
					JbpmUtil.setProcessVariable(VARIAVEL_ID_PD_REQUISITORIO, pdRequisitorio.getIdProcessoDocumento());
				}

				EntityUtil.flush();
				FacesMessages.instance().add(Severity.INFO, "Processo vinculado ao fluxo do Requisitrio com sucesso.");
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}
	
	public boolean existeBloqueioRequisitorio() {
		PjeApiClient pjeApiClient = ComponentUtil.getComponent(PjeApiClient.NAME);
		String tipoRequisitorio = JbpmUtil.getProcessVariable(VARIAVEL_TIPO_REQUISITORIO);
		Boolean resposta = null;
		try {
			resposta = pjeApiClient.getBooleanValue(REQUISITORIO_PATH + "/" + tipoRequisitorio + "/existe-bloqueio");
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return resposta;
	}

	public String getIdRequisitorioFluxo() {
		return JbpmUtil.getProcessVariable(Variaveis.VARIAVEL_FLUXO_ID_REQUISITORIO);
	}

	public String getNameVariableCODFluxoRequisitorio() {
		return Variaveis.NOME_VARIAVEL_COD_FLUXO_REQUISITORIO;
	}
	
	public boolean envioManualRequisitorioHabilitado() {
		PjeApiClient pjeApiClient = ComponentUtil.getComponent(PjeApiClient.NAME);
		String resposta = null;
		try {
			resposta = pjeApiClient.getStringValueSimple(ENVIO_MANUAL_PATH);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		String tipoRequisitorio = JbpmUtil.getProcessVariable(VARIAVEL_TIPO_REQUISITORIO);
		return resposta != null && (resposta.contains(tipoRequisitorio) || resposta.contains("A"));
	}

	public void lancarMovimentoRemessaRequisitorio() {
		boolean executado = JbpmUtil.getProcessVariable(VARIAVEL_EXECUTADO);
		String comTexto = executado ? "ao executado" : "ao tribunal";
		lancarMovimentoRemessa(comTexto);
	}

	private void lancarMovimentoRemessa(String comTexto) {
		int idPDRequisitorio = JbpmUtil.getProcessVariable(VARIAVEL_ID_PD_REQUISITORIO);
		ProcessoDocumento pdRequisitorio = EntityUtil.find(ProcessoDocumento.class, idPDRequisitorio);
		String tipoRequisitorio = JbpmUtil.getProcessVariable(VARIAVEL_TIPO_REQUISITORIO);
		int codigoDoElementoDominio = tipoRequisitorio.equals("R") ? Constantes.REQUISITORIO.RPV : Constantes.REQUISITORIO.PRECATORIO;
		MovimentoAutomaticoService
      .preencherMovimento()
      .deCodigo(Constantes.REQUISITORIO.EXPEDICAO_DOCUMENTOS)
      .associarAoDocumento(pdRequisitorio)
      .comComplementoDeCodigo(Constantes.REQUISITORIO.TIPO_DOCUMENTO)
      .doTipoDominio()
      .preencherComElementoDeCodigo(codigoDoElementoDominio)
      .comComplementoDeCodigo(Constantes.REQUISITORIO.DESTINO).doTipoLivre()
      .preencherComTexto(comTexto)
      .lancarMovimento();
	}

	private ProcessoDocumento criarDocumentoRequisitorio(String conteudo) {
		Usuario usuarioLogado = Authenticator.getUsuarioLogado();		
		TipoProcessoDocumento tipoProcessoDocumento = this.tipoRequisitorio.equals("R") 
			? ParametroUtil.instance().getTipoProcessoDocumentoRPV()
			: ParametroUtil.instance().getTipoProcessoDocumentoPrecatorio();

		ProcessoDocumento processoDocumento = documentoJudicialService.getNovoDocumento(conteudo);
		processoDocumento.setProcesso(JbpmUtil.getProcesso());
		ProcessoDocumentoBin processoDocumentoBin = processoDocumento.getProcessoDocumentoBin();
		processoDocumentoBin.setMd5Documento(Crypto.encodeMD5(processoDocumentoBin.getModeloDocumento()));
		processoDocumentoBin.setUsuario(usuarioLogado);
		processoDocumento.setTipoProcessoDocumento(tipoProcessoDocumento);
		processoDocumento.setInstancia("1");
		processoDocumento.setProcessoDocumento("Requisitório");
		processoDocumento.setSelected(Boolean.TRUE);
		ProcessoDocumentoTrfLocal pdTrfLocal = new ProcessoDocumentoTrfLocal();
		pdTrfLocal.setProcessoDocumento(processoDocumento);
		try {
			return ComponentUtil.getProcessoDocumentoManager().inserirProcessoDocumento(pdTrfLocal);
		} catch (PJeBusinessException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}
	
	public void gravarAssinaturaRequisitorio() {
		try {

			Integer idPDRequisitorio = JbpmUtil.getProcessVariable(VARIAVEL_ID_PD_REQUISITORIO);
			ProcessoDocumento pdRequisitorio = EntityUtil.find(ProcessoDocumento.class, idPDRequisitorio);
				
			if (arquivoAssinado != null) {
				pdRequisitorio.getProcessoDocumentoBin().setCertChain(arquivoAssinado.getCadeiaCertificado());
				pdRequisitorio.getProcessoDocumentoBin().setSignature(arquivoAssinado.getAssinatura());				
			}

			documentoJudicialService.finalizaDocumento(pdRequisitorio, ProcessoTrfHome.instance().getInstance(), null,
					false, true, false, Authenticator.getPessoaLogada(), false);			
					
			FacesMessages.instance().add(Severity.INFO, "Documento assinado com sucesso");
			EntityUtil.getEntityManager().flush();

			String defaultTransition = (String) TaskInstanceUtil.instance()
					.getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);

			TaskInstanceUtil.instance().getVariableFrameDefaultLeavingTransition();

			if (defaultTransition != null) {
				TaskInstanceHome.instance().end(defaultTransition);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	@Transactional
	public void atualizarConteudoOficio() {
		//CORRECAO: para atualizar conteúdo do ofício do MS no legacy.
		try {

			Integer idPDRequisitorio = JbpmUtil.getProcessVariable(VARIAVEL_ID_PD_REQUISITORIO);
			ProcessoDocumento pdRequisitorio = EntityUtil.find(ProcessoDocumento.class, idPDRequisitorio);
			String conteudo = new String(oficioStr.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
				
			documentoJudicialService.atualizaProcessoDocumento(pdRequisitorio, ProcessoTrfHome.instance().getInstance(), conteudo, 
					pdRequisitorio.getTipoProcessoDocumento(), pdRequisitorio.getIdJbpmTask());
					
			EntityUtil.getEntityManager().flush();

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		
	}
	
	public String getDownloadLinks() {
		Integer idPDRequisitorio = JbpmUtil.getProcessVariable(VARIAVEL_ID_PD_REQUISITORIO);
		ProcessoDocumento pdRequisitorio = EntityUtil.find(ProcessoDocumento.class, idPDRequisitorio);
		return documentoJudicialService.getDownloadLinks(Arrays.asList(pdRequisitorio));
	}

	public boolean isProcessoJaInstanciadoNoFluxoDoRequisitorio() {
		return processoJaInstanciadoNoFluxoDoRequisitorio;
	}

	public void setProcessoJaInstanciadoNoFluxoDoRequisitorio(boolean processoJaInstanciadoNoFluxoDoRequisitorio) {
		this.processoJaInstanciadoNoFluxoDoRequisitorio = processoJaInstanciadoNoFluxoDoRequisitorio;
	}

	public String getParametroNumeroRequisitorio() {
		return parametroNumeroRequisitorio;
	}

	public void setParametroNumeroRequisitorio(String parametroNumeroRequisitorio) {
		this.parametroNumeroRequisitorio = parametroNumeroRequisitorio;
	}

	public String getParametroIdRequisitorio() {
		return parametroIdRequisitorio;
	}

	public void setParametroIdRequisitorio(String parametroIdRequisitorio) {
		this.parametroIdRequisitorio = parametroIdRequisitorio;
	}

	public String getParametroTransition() {
		return parametroTransition;
	}

	public void setParametroTransition(String parametroTransition) {
		this.parametroTransition = parametroTransition;
	}
	
	public String getOficioStr() {
		return oficioStr;
	}

	public void setOficioStr(String oficioStr) {
		this.oficioStr = oficioStr;
	}

	public String getOficioStr64() {
		return oficioStr64;
	}

	public void setOficioStr64(String oficioStr64) {
		this.oficioStr64 = oficioStr64;
	}

	public String getTipoRequisitorio() {
		return tipoRequisitorio;
	}

	public void setTipoRequisitorio(String tipoRequisitorio) {
		this.tipoRequisitorio = tipoRequisitorio;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)
			throws Exception {
		setArquivoAssinado(arquivoAssinadoHash);
	}

	public ArquivoAssinadoHash getArquivoAssinado() {
		return arquivoAssinado;
	}

	public void setArquivoAssinado(ArquivoAssinadoHash arquivoAssinado) {
		this.arquivoAssinado = arquivoAssinado;
	}

	public boolean isExecutado() {
		return executado;
	}

	public void setExecutado(boolean executado) {
		this.executado = executado;
	}
}