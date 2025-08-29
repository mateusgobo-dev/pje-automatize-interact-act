/**
 * pje-web
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.view.fluxo;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.international.StatusMessages;
import org.richfaces.event.UploadEvent;

import br.com.infox.cliente.home.ProcessoDocumentoBinHome;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.home.SessaoProcessoDocumentoHome;
import br.com.infox.cliente.home.SessaoProcessoDocumentoVotoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.manager.ModeloDocumentoLocalManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.fluxo.Validador;
import br.jus.cnj.fluxo.interfaces.TaskVariavelAction;
import br.jus.cnj.pje.business.dao.SessaoProcessoDocumentoVotoDAO;
import br.jus.cnj.pje.editor.lool.LibreOfficeManager;
import br.jus.cnj.pje.editor.lool.LoolException;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.TipoProcessoDocumentoPapelService;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.je.pje.entity.vo.BinarioVO;
import br.jus.pje.je.enums.TipoDocumentoColegiadoEnum;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.enums.TipoEditorEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de controle da votação em colegiado. O frame respectivo está em
 * WEB-INF/xhtml/flx/votacaoColegiado.xhtml
 * 
 * @author savio.cruz
 * @author cristof
 * @since 1.6.0
 */
@Name(VotacaoColegiadoLibreOfficeAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class VotacaoColegiadoLibreOfficeAction extends TramitacaoFluxoAction implements Serializable, TaskVariavelAction, ArquivoAssinadoUploader {

	public static final String NAME = "votacaoColegiadoLibreOfficeAction";

	private static final long serialVersionUID = -7751124437133759445L;
    private static final int EMENTA = 4096;
    private static final int RELATORIO = 8192;
    private static final int VOTO = 16384;
    
    private static final String DEFAULT_TRANSITION_VOTO = "defaultTransitionVoto";
    private static final String DEFAULT_TRANSITION_EMENTA = "defaultTransitionEmenta";
    private static final String DEFAULT_TRANSITION_RELATORIO = "defaultTransitionRelatorio";
    private static final String OCULTA_DEFAULT_TRANSITION = "oculto";
    

    private static final Map<String, String> prms = new HashMap<String, String>();

	static {
		prms.put("ocultaEmenta", "pje:fluxo:votacaoColegiado:ocultaEmenta");
		prms.put("ocultaRelatorio", "pje:fluxo:votacaoColegiado:ocultaRelatorio");
		prms.put("ocultaVoto", "pje:fluxo:votacaoColegiado:ocultaVoto");
		prms.put("ocultaProclamacaoJulgamento", "pje:fluxo:votacaoColegiado:anteciparProclamacaoJulgamento");
		prms.put("controlaLiberacao", "pje:fluxo:votacaoColegiado:controlaLiberacao");
		prms.put("minutando", "pje:fluxo:votacaoColegiado:minutaColegiadoEmElaboracao");
		prms.put("permiteAssinarRelatorio", "pje:fluxo:votacaoColegiado:permiteAssinarRelatorio");
		prms.put("permiteAssinarEmenta", "pje:fluxo:votacaoColegiado:permiteAssinarEmenta");
		prms.put("permiteAssinarVoto", "pje:fluxo:votacaoColegiado:permiteAssinarVoto");
	}

	private SessaoProcessoDocumentoHome sessaoProcessoDocumentoHome;
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	
	@In(create = true)
	private SessaoProcessoDocumentoVotoHome sessaoProcessoDocumentoVotoHome;
	
	@In
	private SessaoProcessoDocumentoVotoDAO sessaoProcessoDocumentoVotoDAO;	

	@In(create = true, required = true)
	private transient TaskInstanceUtil taskInstanceUtil;
	
	@In(create = true)
	private TaskInstanceHome taskInstanceHome; 

	@In(create = true)
	private transient ProcessoDocumentoManager processoDocumentoManager;
	
	@In
    private TipoProcessoDocumentoPapelService tipoProcessoDocumentoPapelService;
	
	@In
	private ModeloDocumentoLocalManager modeloDocumentoLocalManager;
	
	private TipoDocumentoColegiadoEnum documentoColegiado;

	private Boolean minutando;

	private Boolean ocultaEmenta;
	private Boolean ocultaRelatorio;
	private Boolean ocultaVoto;
	private Boolean ocultaProclamacaoJulgamento;
	private Boolean controlaLiberacao = true;
	private Boolean permiteAssinarRelatorio = true;
	private Boolean permiteAssinarVoto = false;
	private Boolean permiteAssinarEmenta = false;
	private String defaultTransition;
	private Map<Integer, SessaoProcessoDocumento> docs = new HashMap<Integer, SessaoProcessoDocumento>();
	private String abaSelecionada;	
	private static final String STR_VOTO = "Voto";
	public static final String STR_EMENTA = "Ementa";
	public static final String STR_RELATORIO = "Relatorio";
	private ArquivoAssinadoHash arquivoAssinado;
	
	private boolean mostrarPDF = false;
	private LibreOfficeManager libreOfficeManager;
	private String nomeModeloDocumento;
	
	/**
	 * Recupera a transicao padrão
	 */
	private String carregarVariavelDefaultTransition(){
		if (defaultTransition == null) {
			defaultTransition = (String) taskInstanceUtil.getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION); 
		}
		return defaultTransition;
	}
	
	/**
	 * Metodo responsavel por controlar a transição padrão com validação da inclusão dos documentos (relatório, voto e ementa)
	 */
	public void controlarTransicao(){
		try {
			List<ProcessoDocumento> listaProcessoDocumento = documentoJudicialService.getDocumentosPorTipos(getProcessoJudicial(), 
					ParametroUtil.instance().getTipoProcessoDocumentoRelatorio().getIdTipoProcessoDocumento(),
					ParametroUtil.instance().getTipoProcessoDocumentoVoto().getIdTipoProcessoDocumento(),
					ParametroUtil.instance().getTipoProcessoDocumentoEmenta().getIdTipoProcessoDocumento());
			
			if (listaProcessoDocumento.size() < 3){
				facesMessages.addFromResourceBundle(Severity.ERROR,"votacaoColegiada.relatorioVotoEmenta.documentoEmBranco");
				return;
			}
			taskInstanceHome.saidaDireta(defaultTransition);
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro na verificação dos documentos (relatório, voto e ementa):  " + e.getMessage(), e);
		}
	}

	@Override
	protected Map<String, String> getParametrosConfiguracao() {
		return prms;
	}

	@Override
	public void init() {
		sessaoProcessoDocumentoHome = ComponentUtil.getComponent(SessaoProcessoDocumentoHome.NAME);
		super.init();
		carregarVariavelDefaultTransition();
		if (!ocultaRelatorio){
			setRelatorio();
		}else{
			setVoto();
		}
		
	}

	public boolean liberaCertificacao() {
		return liberaCertificacao(sessaoProcessoDocumentoHome.getInstance().getProcessoDocumento());
	}

	public boolean liberaCertificacao(ProcessoDocumento pd) {
		if(isRelatorio() && !permiteAssinarRelatorio){
			return false;
		}else if(isVoto() && !permiteAssinarVoto){
			return false;
		}else if (isEmenta() && !permiteAssinarEmenta){
			return false;
		}
		return processoDocumentoManager.liberaCertificacao(pd) != null;
	}
	
	@Override
	public void validar(String transicaoSelecionada, Validador validador) {
		
	}

	@Override
	public void movimentar(String transicaoSelecionada) throws Exception {
		
		SessaoProcessoDocumentoVoto spdvAtual = sessaoProcessoDocumentoVotoHome.getInstance();
		
		if (spdvAtual.getTipoVoto()!= null && sessaoProcessoDocumentoVotoHome.getVotoAntigo() != null &&
				!spdvAtual.getTipoVoto().equals(sessaoProcessoDocumentoVotoHome.getVotoAntigo())) {		
			sessaoProcessoDocumentoVotoHome.updateVoto();
		}
	}

	public TipoDocumentoColegiadoEnum getDocumentoColegiado() {
		return documentoColegiado;
	}

	public void setRelatorio() {
		mostrarPDF = false;
		this.documentoColegiado = TipoDocumentoColegiadoEnum.REL;
		sessaoProcessoDocumentoHome.carregarDocumento('R');
		docs.put(RELATORIO, sessaoProcessoDocumentoHome.getInstance());
		FacesMessages.instance().clear();
		
		try {
			carregarDocumentoLibreOffice();
		} catch (LoolException e) {
			e.printStackTrace();
		}
	}

	private void carregarDocumentoLibreOffice() throws LoolException {
		ProcessoDocumentoBin processoDocumentoBin = ProcessoDocumentoBinHome.instance().getInstance();
		
		String nomeDocumento = processoDocumentoBin.getNomeDocumentoWopi();
		
		if (nomeDocumento != null ) {
			this.libreOfficeManager = new LibreOfficeManager(nomeDocumento);
			this.libreOfficeManager.gravar(processoDocumentoBin);
			carregarPdfInicial();
		} else if (processoDocumentoBin.getIdProcessoDocumentoBin()>0) {
			String modeloDocumentoHtmlPreenchido = processoDocumentoBin.getModeloDocumento();
			this.libreOfficeManager = new LibreOfficeManager(TaskInstance.instance().getId()+"" ,"docx");
			this.libreOfficeManager.carregarModeloDocumento(modeloDocumentoHtmlPreenchido);
			this.libreOfficeManager.gravar(processoDocumentoBin);
			gravarDocumento();
		}
		
		
		
	}
	
	public void carregarPdfInicial() {
		this.mostrarPDF = true;
		ProcessoDocumentoBin bin = ProcessoDocumentoBinHome.instance().getInstance();
		ProcessoDocumentoBinManager binManager = (ProcessoDocumentoBinManager) Component.getInstance(ProcessoDocumentoBinManager.class);
		try {
			binManager.refresh(bin);
		} catch (PJeBusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BinarioVO binario = new BinarioVO();
		binario.setIdBinario(bin.getIdProcessoDocumentoBin());
		binario.setMimeType(bin.getExtensao());
		binario.setNomeArquivo(bin.getNomeArquivo());
		binario.setNumeroStorage(bin.getNumeroDocumentoStorage());
		Contexts.getSessionContext().set("download-binario", binario);
		
	}

	public void setVoto() {
		mostrarPDF = false;
		this.documentoColegiado = TipoDocumentoColegiadoEnum.VOT;
		sessaoProcessoDocumentoHome.carregarDocumento('V');
		docs.put(VOTO, sessaoProcessoDocumentoHome.getInstance());
		FacesMessages.instance().clear();
		
		try {
			carregarDocumentoLibreOffice();
		} catch (LoolException e) {
			e.printStackTrace();
		}
	}

	public void setEmenta() {
		mostrarPDF = false;
		this.documentoColegiado = TipoDocumentoColegiadoEnum.EME;
		sessaoProcessoDocumentoHome.carregarDocumento('E');
		docs.put(EMENTA, sessaoProcessoDocumentoHome.getInstance());
		FacesMessages.instance().clear();
		
		try {
			carregarDocumentoLibreOffice();
		} catch (LoolException e) {
			e.printStackTrace();
		}
	}
	
	public void setProclamacaoJulgamento() {
		setVoto();
	}

	public boolean isRelatorio() {
		return documentoColegiado != null && documentoColegiado.equals(TipoDocumentoColegiadoEnum.REL);
	}

	public boolean isVoto() {
		return documentoColegiado != null && documentoColegiado.equals(TipoDocumentoColegiadoEnum.VOT);
	}

	public boolean isEmenta() {
		return documentoColegiado != null && documentoColegiado.equals(TipoDocumentoColegiadoEnum.EME);
	}

	public void setDocumentoColegiado(TipoDocumentoColegiadoEnum documentoColegiado) {
		this.documentoColegiado = documentoColegiado;
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

	public Boolean getOcultaProclamacaoJulgamento() {
		return ocultaProclamacaoJulgamento;
	}

	public void setOcultaProclamacaoJulgamento(
			Boolean ocultaProclamacaoJulgamento) {
		this.ocultaProclamacaoJulgamento = ocultaProclamacaoJulgamento;
	}

	public Boolean getOcultaRelatorio() {
		return ocultaRelatorio;
	}
	
	public boolean isRelatorioAssinado(){
		SessaoProcessoDocumento spd = docs.get(RELATORIO);
		if(spd == null){
			setRelatorio();
			spd = docs.get(RELATORIO);
		}
		if(spd.getProcessoDocumento() == null || spd.getProcessoDocumento().getProcessoDocumentoBin() == null){
			return false;
		}else if(!spd.getProcessoDocumento().getProcessoDocumentoBin().getSignatarios().isEmpty()){
			return true;
		}
		return false;
	}

	public void setOcultaRelatorio(boolean ocultaRelatorio) {
		this.ocultaRelatorio = ocultaRelatorio;
	}

	public Boolean getControlaLiberacao() {
		return controlaLiberacao;
	}
	
	public String getDefaultTransition() {
		return defaultTransition;
	}

	public void setDefaultTransition(String defaultTransition) {
		this.defaultTransition = defaultTransition;
	}

	public void gravarRelatorio(){
		if(isRelatorio()){
			
			try {
				this.libreOfficeManager.gravar(ProcessoDocumentoBinHome.instance().getInstance());
			} catch (LoolException e) {
				FacesMessages.instance().add(Severity.ERROR, e.getMessage());
				e.printStackTrace();
				return;
			}
			
			this.mostrarPDF = true;
			
			if(sessaoProcessoDocumentoHome.isManaged()){
				sessaoProcessoDocumentoHome.update();
			}else{
				sessaoProcessoDocumentoHome.persistRelatorio();
			}
		}
	}
	
	public void assinarRelatorio(){
		if(isRelatorio()){
			if(sessaoProcessoDocumentoHome.isManaged()){
				if(arquivoAssinado != null){
					sessaoProcessoDocumentoHome.getInstance().getProcessoDocumento().getProcessoDocumentoBin().setCertChain(arquivoAssinado.getCadeiaCertificado());
					sessaoProcessoDocumentoHome.getInstance().getProcessoDocumento().getProcessoDocumentoBin().setSignature(arquivoAssinado.getAssinatura());
				}
				sessaoProcessoDocumentoHome.updateComAssinatura();
			}else{
				sessaoProcessoDocumentoHome.persistRelatorioComAssinatura(arquivoAssinado);
			}
			taskInstanceHome.updateTransitions();
			ComponentUtil.getTramitacaoProcessualService().movimentarProcessoJudicial(DEFAULT_TRANSITION_RELATORIO);
		}
	}
	
	public void assinarVoto(){
		if(isVoto()){
			if(sessaoProcessoDocumentoVotoHome.isManaged()){
				if(arquivoAssinado != null){
					sessaoProcessoDocumentoVotoHome.getInstance().getProcessoDocumento().getProcessoDocumentoBin().setCertChain(arquivoAssinado.getCadeiaCertificado());
					sessaoProcessoDocumentoVotoHome.getInstance().getProcessoDocumento().getProcessoDocumentoBin().setSignature(arquivoAssinado.getAssinatura());
				}
				sessaoProcessoDocumentoVotoHome.updateVotoComAssinatura();
			}else{
				sessaoProcessoDocumentoVotoHome.persistVotoComAssinatura();
			}
			taskInstanceHome.updateTransitions();
			ComponentUtil.getTramitacaoProcessualService().movimentarProcessoJudicial(DEFAULT_TRANSITION_VOTO);
		}
	}
	
	public void assinarEmenta(){
		if(isEmenta()){
			if(sessaoProcessoDocumentoHome.isManaged()){
				if(arquivoAssinado != null){
					sessaoProcessoDocumentoHome.getInstance().getProcessoDocumento().getProcessoDocumentoBin().setCertChain(arquivoAssinado.getCadeiaCertificado());
					sessaoProcessoDocumentoHome.getInstance().getProcessoDocumento().getProcessoDocumentoBin().setSignature(arquivoAssinado.getAssinatura());
				}
				sessaoProcessoDocumentoHome.updateComAssinatura();
			}else{
				sessaoProcessoDocumentoHome.persistEmentaComAssinatura(arquivoAssinado);
			}
			taskInstanceHome.updateTransitions();
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
 		if (sessaoProcessoDocumentoHome.getInstance().getProcessoDocumento() != null){
 			retorno = tipoProcessoDocumentoPapelService.verificarExigibilidadeNaoAssina(
 					Authenticator.getPapelAtual(), 
 					sessaoProcessoDocumentoHome.getInstance().getProcessoDocumento().getTipoProcessoDocumento());
 		}
 		return retorno;
 	}
 	
 	public Boolean isOcultaBotaoAssinarRelatorio(){
 		Boolean retorno = (Boolean) taskInstanceUtil.getVariable(Parametros.PJE_FLUXO_OCULTAR_BOTAO_ASSINAR_RELATORIO);
 		if (retorno != null && retorno){
 			retorno = !isOcultarBotaoAssinar();
 		} else {
 			retorno = Boolean.FALSE;
 		}
 		return retorno;
	}
	
	/**
	 * Método responsável por ocultar o botão, no xthml, de transição se a
	 * defautTransition conter o texto "oculto".
	 * 
	 * @return
	 */
	public boolean isDefautTransitionOculta() {
		return StringUtils.isNotBlank(defaultTransition) && !defaultTransition.contains(OCULTA_DEFAULT_TRANSITION);
	}
	
	/**
 	 * Método responsável por remover a assinatura do documento
 	 * 
 	 * @param ProcessoDocumento processoDocumento
 	 */
 	public void removerAssinatura(ProcessoDocumento processoDocumento) {
 		sessaoProcessoDocumentoHome.removerAssinatura(processoDocumento);
 		taskInstanceHome.updateTransitions();
	}
	
	/**
	 * Retorna 'verdadeiro(true)' se o voto tiver sido proferido, preenchido e gravado.
	 * 
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-20738
	 * @return isVotoRelatorProferido - verdadeiro se o voto for proferido
	 */
	public boolean isVotoRelatorProferido() {
		return isDocumentoProferido(TipoDocumentoColegiadoEnum.VOT);
	}

	/**
	 * Retorna 'verdadeiro(true)' se a ementa tiver sido proferido, preenchido e gravado.
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-20738
	 * @return isEmentaProferida - verdadeiro se a ementa for proferida
	 */
	public boolean isEmentaProferida() {
		return isDocumentoProferido(TipoDocumentoColegiadoEnum.EME);
	}

	/**
	 * Retorna 'verdadeiro(true)' se o documento do tipo passado como parãmetro tiver sido proferido, preenchido e gravado.
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-20738
	 * @param documentoColegiado - tipo do documento
	 * @return isDocumentoProferido - retorna true se o documento tiver sido preenchido, proferido.
	 */
	public boolean isDocumentoProferido(TipoDocumentoColegiadoEnum documentoColegiado) {
		TipoDocumentoColegiadoEnum documentoColegiadoAbaAnterior = this.documentoColegiado;
		configuraDocumentoParaAba(documentoColegiado);
		String modeloDocumento = ProcessoDocumentoBinHome.instance().getModeloDocumento();
		boolean isDocumentoProferido = !StringUtil.isEmpty(modeloDocumento);
		configuraDocumentoParaAba(documentoColegiadoAbaAnterior);
		return isDocumentoProferido;
	}

	/**
	 * Configura a aba de acordo com o documento passado como parâmetro (TipoDocumentoColegiadoEnum)
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-20738
	 * @param documentoColegiado - TipoDocumentoColegiadoEnum
	 */
	private void configuraDocumentoParaAba(TipoDocumentoColegiadoEnum documentoColegiado) {
		if(documentoColegiado == null){
			return;
		}
		switch (documentoColegiado) {
			case REL:
				setRelatorio();
				break;
			case VOT:
				setVoto();
				break;
			case EME:
				setEmenta();
				break;
			default:
				break;
		}
	}	
		
	public void saveOrUpdateRelatorio() {

		String update = null;
		if (isRelatorio()) {

			if (sessaoProcessoDocumentoHome.isManaged()) {
				update = sessaoProcessoDocumentoHome.update();
				if (update == null) {
					limparMensagens();
					FacesMessages.instance().add(
							StatusMessage.Severity.WARN,
							FacesUtil.getMessage(ARQ_PROPERTIES,
									"votacao.relatorio.EmBranco"));
					return;
				}
				limparMensagens();
				FacesMessages.instance().add(
						StatusMessage.Severity.INFO,
						FacesUtil.getMessage(ARQ_PROPERTIES,
								"votacao.relatorio.atualizacao"));
				return;
			} else {
				if (!verificarModeloPreenchido()) {
					sessaoProcessoDocumentoHome.persistRelatorio();
					;
					limparMensagens();
					FacesMessages.instance().add(
							StatusMessage.Severity.INFO,
							FacesUtil.getMessage(ARQ_PROPERTIES,
									"votacao.relatorio.sucesso"));
					return;
				}
				limparMensagens();
				FacesMessages.instance().add(
						StatusMessage.Severity.WARN,
						FacesUtil.getMessage(ARQ_PROPERTIES,
								"votacao.relatorio.EmBranco"));

			}

		}

	}

	public void updateVotoComValidacao() {

			sessaoProcessoDocumentoVotoHome.updateVoto();
			limparMensagens();
			FacesMessages.instance().add(
					StatusMessage.Severity.INFO,
					FacesUtil.getMessage(ARQ_PROPERTIES,
							"votacaoColegiada.votoAtualizacao"));

	}

	private boolean verificarTipoVotoIsNull() {
		SessaoProcessoDocumentoVoto instance = sessaoProcessoDocumentoVotoHome
				.getInstance();
		boolean tipoVotoIsNull = false;
		if ((instance.getTipoVoto() == null || StringUtils.isEmpty(instance
				.getTipoVoto().getTipoVoto()))) {
			tipoVotoIsNull = true;
			FacesMessages.instance().clear();
			FacesMessages.instance().add(
					StatusMessage.Severity.ERROR,
					FacesUtil.getMessage(ARQ_PROPERTIES,
							"votacaoColegiada.votoObrigatorio"));
		}

		return tipoVotoIsNull;
	}

	public void persistVotoComValidacao() {

			sessaoProcessoDocumentoVotoHome.persistVoto();
			limparMensagens();
			FacesMessages.instance().add(
					StatusMessage.Severity.INFO,
					FacesUtil.getMessage(ARQ_PROPERTIES,
							"votacaoColegiada.VotoSucesso"));
			return;

	}
	
	public void gravarVoto () {
		try {
			this.libreOfficeManager.gravar(ProcessoDocumentoBinHome.instance().getInstance());
		} catch (LoolException e) {
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
			e.printStackTrace();
			return;
		}
		
		this.mostrarPDF = true;
		
		if ( SessaoProcessoDocumentoVotoHome.instance().isManaged() ) {
			updateVotoComValidacao();
		} else {
			persistVotoComValidacao();
		}
	}
	
	public void gravarEmenta() {
		try {
			this.libreOfficeManager.gravar(ProcessoDocumentoBinHome.instance().getInstance());
		} catch (LoolException e) {
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
			e.printStackTrace();
			return;
		}
		
		this.mostrarPDF = true;
		
		if ( sessaoProcessoDocumentoHome.isManaged() ) {
			sessaoProcessoDocumentoHome.updateEmenta();
		} else {
			sessaoProcessoDocumentoHome.persistEmenta();
		}
	}

	public void evento(ActionEvent event) {

		if (!sessaoProcessoDocumentoHome.getAssinado()
				&& getAbaSelecionada().equals(STR_RELATORIO)) {

			saveOrUpdateRelatorio();

		} else if (!sessaoProcessoDocumentoHome.getAssinado()
				&& getAbaSelecionada().equals(STR_EMENTA)) {
			saveOrUpdateEmenta();

		} else if (!SessaoProcessoDocumentoVotoHome.instance().getAssinado()
				&& getAbaSelecionada().equals(STR_VOTO)) {
			if (verificarVotoValido()) {
				saveOrUpdateVoto();
			} else {

				limparMensagens();
				FacesMessages.instance().add(
						StatusMessage.Severity.ERROR,
						FacesUtil.getMessage(ARQ_PROPERTIES,
								"votacaoColegiada.votoObrigatorio"));
				if (verificarModeloPreenchido()) {
					limparMensagens();
					FacesMessages.instance().add(
							StatusMessage.Severity.WARN,
							FacesUtil.getMessage(ARQ_PROPERTIES,
									"votacaoColegiado.VotoEmBranco"));
					return;
				}
			}

		}

	}

	public void saveOrUpdateVoto() {
		if (!SessaoProcessoDocumentoVotoHome.instance().isManaged()) {
			SessaoProcessoDocumentoVotoHome.instance().persistVoto();
			limparMensagens();
			FacesMessages.instance().add(
					StatusMessage.Severity.INFO,
					FacesUtil.getMessage(ARQ_PROPERTIES,
							"votacaoColegiada.VotoSucesso"));
			return;
		}
		SessaoProcessoDocumentoVotoHome.instance().updateVoto();
		limparMensagens();
		FacesMessages.instance().add(
				StatusMessage.Severity.INFO,
				FacesUtil.getMessage(ARQ_PROPERTIES,
						"votacaoColegiada.votoAtualizacao"));

	}

	private boolean verificarVotoValido() {
		boolean isVoto = false;
		if (isVoto() && !verificarTipoVotoIsNull()) {
			isVoto = true;
		}
		return isVoto;

	}

	public void saveOrUpdateEmenta() {

		if (isEmenta()) {

			if (sessaoProcessoDocumentoHome.isManaged()) {
				if (verificarModeloPreenchido()) {
					limparMensagens();
					FacesMessages.instance().add(
							StatusMessage.Severity.WARN,
							FacesUtil.getMessage(ARQ_PROPERTIES,
									"votacao.ementa.EmBranco"));
					return;
				}
				sessaoProcessoDocumentoHome.updateEmenta();
				limparMensagens();
				FacesMessages.instance().add(
						StatusMessage.Severity.INFO,
						FacesUtil.getMessage(ARQ_PROPERTIES,
								"votacao.ementa.atualizacao"));
				return;
			} else {
				if (!verificarModeloPreenchido()) {
					sessaoProcessoDocumentoHome.persistEmenta();
					limparMensagens();
					FacesMessages.instance().add(
							StatusMessage.Severity.INFO,
							FacesUtil.getMessage(ARQ_PROPERTIES,
									"votacao.ementa.sucesso"));
					return;
				}
				limparMensagens();
				FacesMessages.instance().add(
						StatusMessage.Severity.WARN,
						FacesUtil.getMessage(ARQ_PROPERTIES,
								"votacao.ementa.EmBranco"));

			}

		}

	}
	public boolean  verificarModeloPreenchido() {
		ProcessoDocumentoBinHome pdbinHome = ProcessoDocumentoBinHome
				.instance();
		String modelo = pdbinHome.getModeloDocumento();
	    return ProcessoDocumentoBinHome.isModeloVazio(modelo);
		
		
	} 

	public boolean verificarAba(String aba) {

		return getAbaSelecionada().equals(aba);

	}

	public String getAbaSelecionada() {

		return abaSelecionada;
	}

	public void setAbaSelecionada(String abaSelecionada) {
		this.abaSelecionada = abaSelecionada;
	}

	private void limparMensagens() {
		StatusMessages.instance().clear();
		StatusMessages.instance().clearGlobalMessages();
		FacesMessages.instance().clear();
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)
			throws Exception {
		arquivoAssinado = arquivoAssinadoHash;
	}

	@Override
	public String getActionName() {
		return NAME;
	}
	
	public String getDownloadLinks(){
		return documentoJudicialService.getDownloadLinks(Arrays.asList(sessaoProcessoDocumentoHome.getInstance().getProcessoDocumento()));
	}
	
	public String getDownloadLinksVoto(){
		return documentoJudicialService.getDownloadLinks(Arrays.asList(sessaoProcessoDocumentoVotoHome.getInstance().getProcessoDocumento()));
	}
	
	public ArquivoAssinadoHash getArquivoAssinado() {
		return arquivoAssinado;
	}
	
	public void setArquivoAssinado(ArquivoAssinadoHash arquivoAssinado) {
		this.arquivoAssinado = arquivoAssinado;
	}
	
	public boolean isMostrarPDF() {
		return mostrarPDF;
	}
	
	public String getNomeModeloDocumento() {
		return nomeModeloDocumento;
	}

	public void setNomeModeloDocumento(String nomeModeloDocumento) {
		this.nomeModeloDocumento = nomeModeloDocumento;
	}
	
	public List<ModeloDocumento> pesquisarModeloDocumento(Object valor) throws PJeBusinessException{
		ProcessoDocumentoHome.instance().setModeloDocumentoCombo(null);
		String txt = ((String) valor).trim();
		List<ModeloDocumento> ret = new ArrayList<ModeloDocumento>();
		ModeloDocumentoManager manager = ComponentUtil.getComponent(ModeloDocumentoManager.class);
		if (txt.matches("\\d*")) {
			ret.add(manager.findById(txt));
		}else
			try {
				String textoPesquisa = txt.replaceAll("\\s", "%");
				Search search = new Search(ModeloDocumentoLocal.class);
				search.setDistinct(true);
				search.setMax(15);
				Criteria nomeCriteria = Criteria.contains("tituloModeloDocumento", textoPesquisa);
				Criteria tipoDocumentoCriteria = Criteria.equals("tipoProcessoDocumento", ProcessoHome.instance().getTipoProcessoDocumento());
				search.addCriteria(nomeCriteria);
				search.addCriteria(tipoDocumentoCriteria);
				search.addCriteria(Criteria.equals("ativo", Boolean.TRUE));
				List<ModeloDocumento> modelo = manager.list(search);
				ret.addAll(modelo);
			} catch (NoSuchFieldException e) {
				facesMessages.add(Severity.ERROR, "Houve um erro ao buscar os modelos de documento.");
			}
		return ret;
	}
	
	public void carregarNovoDocumento() {
		this.libreOfficeManager = new LibreOfficeManager(TaskInstance.instance().getId()+""+ProcessoHome.instance().getTipoProcessoDocumento(), "odt");
		try {
			this.libreOfficeManager.carregarNovoDocumento();
			this.mostrarPDF = true;
		} catch (LoolException e) {
			facesMessages.add(Severity.ERROR, e.getMessage(), e);
		}
		gravarDocumento();
	}

	public LibreOfficeManager getLibreOfficeManager() {
		return libreOfficeManager;
	}
	
	public void removerDocumento(){
		if ( this.documentoColegiado==TipoDocumentoColegiadoEnum.VOT ) {
			sessaoProcessoDocumentoVotoHome.removerVotoMagistrado();
		} else {
			sessaoProcessoDocumentoHome.removerDocumento();
		}
		
		this.mostrarPDF = false;
	}	
	
	public void substituirModelo() {
		ModeloDocumento modeloDocumento = ProcessoDocumentoHome.instance().getModeloDocumentoCombo();
		ModeloDocumentoLocal modeloLocal = this.modeloDocumentoLocalManager.findById(ProcessoDocumentoHome.instance().getModeloDocumentoCombo().getIdModeloDocumento());
		ProcessoDocumentoBin pdBin = ProcessoDocumentoBinHome.instance().getInstance();
		if ( modeloLocal.getTipoEditor()==TipoEditorEnum.L ) {
			try {
				this.documentoJudicialService.substituirModeloODT(pdBin, modeloDocumento);
				String base64ODT = pdBin.getModeloDocumento();
				this.libreOfficeManager = new LibreOfficeManager(TaskInstance.instance().getId()+""+ProcessoHome.instance().getTipoProcessoDocumento() ,"odt");
				this.libreOfficeManager.carregarModeloDocumento(Base64.getDecoder().decode(base64ODT));
				this.nomeModeloDocumento = null;
			} catch (Exception e) {
				facesMessages.add(Severity.ERROR, "Erro ao substituir modelo ODT", e);
			}
		} else {
			this.documentoJudicialService.substituirModelo(pdBin, modeloDocumento);
			try {
				String modeloDocumentoHtmlPreenchido = pdBin.getModeloDocumento();
				this.libreOfficeManager = new LibreOfficeManager(TaskInstance.instance().getId()+""+ProcessoHome.instance().getTipoProcessoDocumento() ,"docx");
				this.libreOfficeManager.carregarModeloDocumento(modeloDocumentoHtmlPreenchido);
				this.nomeModeloDocumento = null;
			} catch (LoolException e) {
				facesMessages.add(Severity.ERROR, e.getMessage(), e);
			}
		}
		
		gravarDocumento();
		
	}
	
	private void gravarDocumento() {
		
		if ( this.documentoColegiado==TipoDocumentoColegiadoEnum.REL ) {
			gravarRelatorio();
		} else if ( this.documentoColegiado==TipoDocumentoColegiadoEnum.VOT ) {
			gravarVoto();
		} else {
			gravarEmenta();
		}
		
	}

	public void uploadDragDrop(String name, String conteudo) {
		
		String extension = name.substring(name.lastIndexOf('.')+1, name.length());
		InputStream inputStream;
		try {
			inputStream = new ByteArrayInputStream(conteudo.getBytes("ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			facesMessages.add(Severity.ERROR, "Erro ao ler o arquivo enviado", e);
			return;
		}
		
		try {
			this.libreOfficeManager = new LibreOfficeManager(TaskInstance.instance().getId()+""+ProcessoHome.instance().getTipoProcessoDocumento(), extension);
			this.libreOfficeManager.carregarDocumentoImportacao(inputStream);
		} catch (LoolException e) {
			facesMessages.add(Severity.ERROR, e.getMessage(), e);
		}
		
		gravarDocumento();
		
	}
	
	public void listener(UploadEvent uploadEvent) {
		
		String fileName = uploadEvent.getUploadItem().getFileName();
		String extension = fileName.substring(fileName.lastIndexOf('.')+1, fileName.length());
		
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(uploadEvent.getUploadItem().getFile());
		} catch (FileNotFoundException e) {
			facesMessages.add(Severity.ERROR, "Erro ao ler arquivo enviado", e);
			return;
		}
		
		try {
			this.libreOfficeManager = new LibreOfficeManager(TaskInstance.instance().getId()+""+ProcessoHome.instance().getTipoProcessoDocumento(), extension);
			this.libreOfficeManager.carregarDocumentoImportacao(inputStream);
		} catch (LoolException e) {
			facesMessages.add(Severity.ERROR, e.getMessage(), e);
		}
		
		gravarDocumento();
		
	}
	
	public int getIdProcessoDocumento() {
		if ( this.documentoColegiado==TipoDocumentoColegiadoEnum.VOT ) {
			return sessaoProcessoDocumentoVotoHome.getInstance().getProcessoDocumento()!=null ? sessaoProcessoDocumentoVotoHome.getInstance().getProcessoDocumento().getIdProcessoDocumento() : 0;
		} else {
			return sessaoProcessoDocumentoHome.getInstance().getProcessoDocumento()!=null ? sessaoProcessoDocumentoHome.getInstance().getProcessoDocumento().getIdProcessoDocumento() : 0;
		} 
	}
	
}