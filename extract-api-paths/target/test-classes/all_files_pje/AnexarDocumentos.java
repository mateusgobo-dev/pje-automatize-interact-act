package br.com.infox.cliente.actions.anexarDocumentos;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.compass.core.util.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.Strings;

import com.lowagie.text.pdf.PdfReader;

import br.com.infox.cliente.Util;
import br.com.infox.cliente.home.ProcessoAudienciaHome;
import br.com.infox.cliente.home.ProcessoDocumentoBinHome;
import br.com.infox.cliente.home.ProcessoDocumentoBinPessoaAssinaturaHome;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.core.certificado.VerificaCertificado;
import br.com.infox.editor.dao.AdvogadoLocalizacaoCabecalhoDao;
import br.com.infox.ibpm.component.tree.EventosTreeHandler;
import br.com.infox.ibpm.component.tree.EventsTipoDocumentoTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.component.FileHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.PdfUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.DocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.AdvogadoLocalizacaoCabecalho;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.entidades.PessoaAssistenteAdvogadoLocal;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoAssociacao;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.TipoDocumentoEnum;

@Name(AnexarDocumentos.NAME)
@Scope(ScopeType.CONVERSATION)
public class AnexarDocumentos implements Serializable, ArquivoAssinadoUploader {

	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(AnexarDocumentos.class);
	
	public static final String NAME = "anexarDocumentos";

	private ProcessoDocumento pdPdf;
	private ProcessoDocumentoBin pdbPdf;
	private ProcessoDocumento pdHtml;
	private ProcessoDocumentoBin pdbHtml;
	
	private List<ProcessoDocumento> documentosSalvos = new ArrayList<ProcessoDocumento>();

	private String certChain;
	private String signature;
	private boolean abreToogle = false;
	private boolean processoRemetido;
	private boolean documentoInicial = false;

	private ModeloDocumentoLocal modeloDocumento;

	private boolean permiteAssinatura = false;

	private boolean flagLimparTela = true;
	
	private boolean flagMostraModalRemocaoCaracteresEspeciais = false;

	private boolean flag = false;

	private EntityManager em = EntityUtil.getEntityManager();

	private boolean verificaDocumento = true;
	private boolean liberarConsultaPublica;
	private boolean renderedConsultaPublica;	
	private String mensagemPdf;
	private String mensagemDocNaoAssinado;
	
	private ArrayList<TipoProcessoDocumentoTrf> tipoProcessoDocumentoItemsHtmlList;
	private ArrayList<TipoProcessoDocumentoTrf> tipoProcessoDocumentoItemsPdfList;
	
	private ProtocolarDocumentoBean protocolarDocumentoBean;
	
	@In
	private ProcessoJudicialService processoJudicialService;
	
	@In
	private DocumentoJudicialService documentoJudicialService;

	@In
	private AdvogadoLocalizacaoCabecalhoDao advogadoLocalizacaoCabecalhoDao;
	
	@In
	private DocumentoBinManager documentoBinManager;
	
	@In(required= false)
	private Identity identity;
	
	public static AnexarDocumentos instance() {
		return ComponentUtil.getComponent(AnexarDocumentos.NAME);
	}
	
	public void concluirInclusaoPeticaoDocumento() {
		this.protocolarDocumentoBean.concluirAssinatura();
	}
	
	public static String getName() {
		return NAME;
	}

	/**
	 * Método chamado ao clicar na aba "Anexar Documentos". Cria uma nova
	 * instância do documento, limpa as listas da classe e no final chama o
	 * método verificarDocumento.
	 */
	public void actionAbaAnexar() {
		if(this.protocolarDocumentoBean == null){
			ProcessoTrf processoJudicial = ProcessoTrfHome.instance().getInstance();
			if (this.identity.hasRole(Papeis.INTERNO)) {
				this.protocolarDocumentoBean = new ProtocolarDocumentoBean(processoJudicial.getIdProcessoTrf(),
						ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL | ProtocolarDocumentoBean.LANCAR_MOVIMENTACAO
						| ProtocolarDocumentoBean.PERMITE_SELECIONAR_MOVIMENTACAO | ProtocolarDocumentoBean.PERMITIR_VINCULACAO_RESPOSTA
						| ProtocolarDocumentoBean.UTILIZAR_MODELOS, getName());
			}
			else {
				this.protocolarDocumentoBean = new ProtocolarDocumentoBean(processoJudicial.getIdProcessoTrf(),
						ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL
						| ProtocolarDocumentoBean.LANCAR_MOVIMENTACAO
						| ProtocolarDocumentoBean.PERMITIR_VINCULACAO_RESPOSTA, getName());
			}
		}
		ProcessoDocumentoHome.instance().setIdDocumentoDestacar(0);
		newInstance();
		if (ProcessoDocumentoBinHome.instance().getInstance() != null
				&& ProcessoDocumentoBinHome.instance().getInstance().getIdProcessoDocumentoBin() != 0) {
			ProcessoDocumentoBinHome.instance().setInstance(null);
		}
		verificarDocumento();
		refreshGrids();
	}

	/**
	 * Verifica se existe um documento do tipo "Petição Inicial" e se ele está
	 * assinado. Caso não esteja assinado, seta ele como o documento corrente.
	 * Caso esteja assinado, continua varrendo a lista de documentos do processo
	 * até encontrar um que ainda não esteja assinado e que tenha sido
	 * adicionado por um usuário do mesmo papel que o usuário logado. Caso
	 * encontre, seta esse documento como o corrente. Caso não encontre nenhum
	 * documento que se encaixe nas duas condições anteriores, mostra um
	 * documento novo na tela. Em paralelo a essas verificações, o método
	 * alimenta as listas listaPdf(Lista dos Pdfs) e a listaGravados(Lista dos
	 * Pdfs Gravados no Banco).
	 */
	private void verificarDocumento() {
		modeloDocumento = new ModeloDocumentoLocal();
		boolean achou = false;
		ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, ProcessoTrfHome.instance().getInstance()
				.getIdProcessoTrf());
		TipoProcessoDocumento tipoPeticaoInicial = processoTrf.getClasseJudicial().getTipoProcessoDocumentoInicial();
		// [PJEII-1243] Padronizado para recuperar o id do TipoProcessoDocumento configurado na tabela de parâmetros como Petição inicial
		List<Integer> idTipoProcessoDocumentoList = new ArrayList<Integer>();
		if (ParametroUtil.instance().getTipoProcessoDocumentoTermoAberturaExecucao() != null) {
			idTipoProcessoDocumentoList.add(ParametroUtil.instance().getTipoProcessoDocumentoTermoAberturaExecucao().getIdTipoProcessoDocumento());
		}
		if (ParametroUtil.instance().getTipoProcessoDocumentoTermoAberturaLiquidacao() != null) {
			idTipoProcessoDocumentoList.add(ParametroUtil.instance().getTipoProcessoDocumentoTermoAberturaLiquidacao().getIdTipoProcessoDocumento());
		}
		idTipoProcessoDocumentoList.add(tipoPeticaoInicial.getIdTipoProcessoDocumento());
		
		for (ProcessoDocumento pd : processoTrf.getProcesso().getProcessoDocumentoList()) {
			for(Integer idTipoProcessoDocumento : idTipoProcessoDocumentoList){
				if (pd.getTipoProcessoDocumento().getIdTipoProcessoDocumento() == idTipoProcessoDocumento.intValue()) {
					achou = true;
					if (!ProcessoHome.instance().verificarPessoaAssinatura(pd)) {
						setPdHtml(pd);
						setPdbHtml(pd.getProcessoDocumentoBin());
						ProcessoDocumentoHome.instance().setInstance(pdHtml);
						setDocumentoInicial(true);
						return;
					}
				}
			}
		}
		if (!achou) {
			if(ProcessoTrfHome.instance() != null && (ProcessoTrfHome.instance().isCadastroProcessoClet() || ProcessoTrfHome.instance().isProcessoExecucaoLiquidacao(processoTrf.getIdProcessoTrf()))){
				if(ProcessoTrfHome.instance().getTipoNaturezaClet().equals("L")){
					pdHtml.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoTermoAberturaLiquidacao());
					pdHtml.setProcessoDocumento(pdHtml.getTipoProcessoDocumento().getTipoProcessoDocumento());
					setModeloDocumento(ParametroUtil.instance().getModeloTermoAberturaLiquidacao());
				}else{
					pdHtml.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoTermoAberturaExecucao());
					pdHtml.setProcessoDocumento(pdHtml.getTipoProcessoDocumento().getTipoProcessoDocumento());
					setModeloDocumento(ParametroUtil.instance().getModeloTermoAberturaExecucao());
				}
			}else{
				pdHtml.setTipoProcessoDocumento(tipoPeticaoInicial);
				pdHtml.setProcessoDocumento(pdHtml.getTipoProcessoDocumento().getTipoProcessoDocumento());
				setModeloDocumento(ParametroUtil.instance().getModeloLocalPeticaoInicial());
			}
			Localizacao localizacaoAtual = Authenticator.getLocalizacaoAtual();
			AdvogadoLocalizacaoCabecalho localizacaoCabecalho = advogadoLocalizacaoCabecalhoDao.getAdvogadoLocalizacaoCabecalho(localizacaoAtual);
			
			// se o modelo de documento for vazio ou então tiver o conteúdo igual ao do cabeçalho, caso esse exista
			if(pdbHtml.getModeloDocumento() == null || (localizacaoCabecalho != null && localizacaoCabecalho.getCabecalho().getConteudo().equals(pdbHtml.getModeloDocumento()))){
				setarConteudoDoCabecalhoModelo(localizacaoCabecalho);
			}
			setDocumentoInicial(true);
			return;
		} 
		
		if (verificaDocumento) {
			for (ProcessoDocumento pd : ProcessoTrfHome.instance().getInstance().getProcesso()
					.getProcessoDocumentoList()) {
				if (!ProcessoHome.instance().verificarPessoaAssinatura(pd) && pd.getPapel() != null
						&& pd.getPapel().equals(Authenticator.getPapelAtual()) && pd.getLocalizacao() != null
						&& pd.getLocalizacao().equals(Authenticator.getLocalizacaoAtual())
						&& Strings.isEmpty(pd.getProcessoDocumentoBin().getExtensao())
						&& !pd.getTipoProcessoDocumento().equals(ParametroUtil.instance().getTipoProcessoDocumentoAcordao())) {
					setPdHtml(pd);
					setPdbHtml(pd.getProcessoDocumentoBin());
					ProcessoDocumentoHome.instance().setInstance(pdHtml);
					return;
				}
			}
		}
	}

	private void setarConteudoDoCabecalhoModelo(AdvogadoLocalizacaoCabecalho localizacaoCabecalho) {
		String cabecalho = null;
		String conteudoModeloDocumento = null;
		StringBuilder documento = new StringBuilder();
		
		if(localizacaoCabecalho != null && (localizacaoCabecalho.getCabecalho().getAtivo() != null && localizacaoCabecalho.getCabecalho().getAtivo() == true)) {
			cabecalho = ProcessoDocumentoHome.processarModelo(localizacaoCabecalho.getCabecalho().getConteudo());
		}
		
		if (modeloDocumento != null) {
			conteudoModeloDocumento = ProcessoDocumentoHome.processarModelo(modeloDocumento.getModeloDocumento());
		} 
		
		if(cabecalho != null) {
			documento.append(cabecalho);
			documento.append("\n");
		}
		
		if(conteudoModeloDocumento != null) {
			documento.append(conteudoModeloDocumento);
		}
		
		String teste = documento.length() == 0 ? null : documento.toString();
		pdbHtml.setModeloDocumento(teste);
	}

	public void setPdPdf(ProcessoDocumento pdPdf) {
		this.pdPdf = pdPdf;
	}

	public ProcessoDocumento getPdPdf() {
		return pdPdf;
	}

	public void setPdbPdf(ProcessoDocumentoBin pdbPdf) {
		this.pdbPdf = pdbPdf;
	}

	public ProcessoDocumentoBin getPdbPdf() {
		return pdbPdf;
	}

	public void newInstance() {
		newInstanceHtml();
		newInstancePdf();
		setDocumentoInicial(false);
		setPermiteAssinatura(false);
		setLiberarConsultaPublica(false);
		setRenderedConsultaPublica(false);		
		ProcessoDocumentoHome.instance().newInstance();
		ProcessoDocumentoBinHome.instance().newInstance();
		ProcessoDocumentoBinPessoaAssinaturaHome.instance().newInstance();
		ProcessoDocumentoBinHome.instance().setIsAssinarDocumento(false);
	}
	
	public void newInstanceRegistroIntimacao(){
		newInstanceHtml();
		newInstancePdf();
	}

	private void newInstanceHtml() {
		pdHtml = new ProcessoDocumento();
		pdbHtml = new ProcessoDocumentoBin();

		// [PJEII-1029] Setando o Processo no ProcessoDocumento, a fim de evitar o NullPointerException
		pdHtml.setProcesso(ProcessoTrfHome.instance().getInstance().getProcesso());
		
		// [PJEII-3937] Adicionando cabeçalho no documento, caso exista
		Localizacao localizacaoAtual = Authenticator.getLocalizacaoAtual();
		AdvogadoLocalizacaoCabecalho localizacaoCabecalho = advogadoLocalizacaoCabecalhoDao.getAdvogadoLocalizacaoCabecalho(localizacaoAtual);
		String cabecalho = null;
		
		if(localizacaoCabecalho != null && (localizacaoCabecalho.getCabecalho().getAtivo() != null && localizacaoCabecalho.getCabecalho().getAtivo() == true)) {
			cabecalho = ProcessoDocumentoHome.processarModelo(localizacaoCabecalho.getCabecalho().getConteudo());
		}
		
		pdbHtml.setModeloDocumento(cabecalho == null ? null : cabecalho);
	}

	private void newInstancePdf() {
		pdPdf = new ProcessoDocumento();
		pdbPdf = new ProcessoDocumentoBin();
		FileHome.instance().clear();
	}

	@SuppressWarnings("static-access")
	public void ajustarProcessoDocumento(ProcessoDocumento processoDocumento) {
		processoDocumento.setProcesso(ProcessoTrfHome.instance().getInstance().getProcesso());
		processoDocumento.setUsuarioInclusao(Authenticator.instance().getUsuarioLogado());
		processoDocumento.setNomeUsuarioInclusao(Authenticator.instance().getUsuarioLogado().getNome());
		processoDocumento.setDataInclusao(new Date());
		processoDocumento.setAtivo(true);
		processoDocumento.setPapel(Authenticator.instance().getPapelAtual());
		processoDocumento.setLocalizacao(Authenticator.instance().getLocalizacaoAtual());
	}

	@SuppressWarnings("static-access")
	public void ajustarProcessoDocumentoBin(ProcessoDocumentoBin processoDocumentoBin, FileHome fileHome) {
		processoDocumentoBin.setExtensao(fileHome.getFileType());
		processoDocumentoBin.setMd5Documento(fileHome.getMD5());
		processoDocumentoBin.setNomeArquivo(fileHome.getFileName());
		processoDocumentoBin.setUsuario(Authenticator.instance().getUsuarioLogado());
		processoDocumentoBin.setDataInclusao(new Date());
		processoDocumentoBin.setSize(fileHome.getSize());
		processoDocumentoBin.setBinario(Boolean.TRUE);
	}

	private boolean verificaSituacaoHtml() {
		return (pdHtml != null && pdbHtml != null && pdHtml.getTipoProcessoDocumento() != null
				&& !Strings.isEmpty(pdbHtml.getModeloDocumento()) && !Strings.isEmpty(pdHtml.getProcessoDocumento()) && !ProcessoDocumentoBinHome.isModeloVazio(pdbHtml.getModeloDocumento()));
	}

	public void addPdf() {
		if (!isDocumentoBinValido(FileHome.instance())) {
			return;
		}
		// Se não houver um html confeccionado, não pode inserir um pdf.
		if (!verificaSituacaoHtml()) {
			FacesMessages.instance().add(Severity.ERROR,
					"Operação não permitida. É obrigatória a inclusão de petição/documento no editor.");
			return;
		}
		pdPdf.setDocumentoPrincipal(pdHtml);
		pdHtml.getDocumentosVinculados().add(pdPdf);
		ajustarProcessoDocumento(pdPdf);
		ajustarProcessoDocumentoBin(pdbPdf, FileHome.instance());

		gravarPdf(pdPdf, pdbPdf);
		newInstancePdf();
		existePdf();
		FacesMessages.instance().add(Severity.INFO, "Documento anexado com sucesso.");
	}

	public void removerPdf(ProcessoDocumento obj) {
		ProcessoDocumentoHome.instance().removerDocumento(obj, obj.getProcesso().getIdProcesso());
		pdHtml.getDocumentosVinculados().remove(obj);
		newInstancePdf();
	}

	public void gravarPdf(ProcessoDocumento processoDocumento, ProcessoDocumentoBin processoDocumentoBin) {
		ProcessoDocumentoHome pdHome = ProcessoDocumentoHome.instance();

		processoDocumentoBin.setValido(pdHome.estaValido());
		
		// Inserindo o binário no storage
		try {
			FileHome fileHome = FileHome.instance();
			processoDocumentoBin.setNumeroDocumentoStorage(documentoBinManager.persist(fileHome.getData(),fileHome.getContentType()));
			 
		} catch (PJeBusinessException e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR,e.getMessage(), e.getParams());
			e.printStackTrace();
			return;
		}
		
		// Inserindo o processoDocumentoBin
		EntityUtil.getEntityManager().persist(processoDocumentoBin);
		EntityUtil.flush();

		// Inserindo o processoDocumento
		processoDocumento.setProcessoDocumentoBin(processoDocumentoBin);
		if(!Util.isStringSemCaracterUnicode(pdbHtml.getModeloDocumento())) {
			excluiCaracteresEspeciaisEditorTexto(pdbHtml.getModeloDocumento());
		}
		EntityUtil.getEntityManager().persist(processoDocumento);
		EntityUtil.flush();

		documentosSalvos.add(processoDocumento);
		
		// Inserindo o processoDocumentoAssociacao
		ProcessoDocumentoAssociacao pda = new ProcessoDocumentoAssociacao();
		pda.setProcessoDocumento(ProcessoDocumentoHome.instance().getInstance());
		pda.setDocumentoAssociado(processoDocumento);
		EntityUtil.getEntityManager().persist(pda);
		EntityUtil.flush();
	}

	public void gravarDocumentosAudiencia() {
		gravarDocumentos();
		ProcessoAudienciaHome.instance().getInstance().setProcessoDocumento(ProcessoDocumentoHome.instance().getInstance());
		ProcessoAudienciaHome.instance().update();
	}
	
	public void gravarDocumentos() {
		ProcessoDocumentoBinHome.instance().setIsAssinarDocumento(false);
		ProcessoDocumentoBinHome.instance().setInstance(pdbHtml);
		ProcessoDocumentoHome.instance().setInstance(pdHtml);
		
		// [PJEII-2623] [PJEII-2116] [PJEII-1868] - Cristiano Nascimento
		// inclusao de validação de caracteres unicode no editor de texto. Caso exista algum caracter que o sistema nao consiga converter para ISO8859-1, 
		// o sistema abre um modal de confirmacao de exclusão de caracteres especiais. 
		if (validaCaracteresEspeciaisEditorTexto(ProcessoDocumentoBinHome.instance().getModeloDocumento())) {
			salvarDocumentos(ProcessoDocumentoBinHome.instance());
		}
	}

	/**
	 * [PJEII-2623] [PJEII-2116] [PJEII-1868] - Cristiano Nascimento
	 * Metodo refatorado do gravarDocumentos para ser usado tanto no método gravarDocumentos() 
	 * e excluiCaracteresEspeciaisEditorTexto()
	 * @return void
	 */
	private void salvarDocumentos(ProcessoDocumentoBinHome processoDocumentoBinHome) {
		if (ProcessoDocumentoHome.instance().getInstance().getIdProcessoDocumento() != 0) {
			ProcessoDocumentoHome.instance().update();
			EntityUtil.getEntityManager().merge(ProcessoDocumentoHome.instance().getInstance());
			EntityUtil.flush();
			FacesMessages.instance().clear();
			FacesMessages.instance().add("Registro modificado com sucesso.");
		} else {
			ProcessoDocumentoBinHome.instance().isModelo(true);
			ProcessoDocumentoHome.instance().persist();
			documentosSalvos.add(ProcessoDocumentoHome.instance().getInstance());
			FacesMessages.instance().clear();
			FacesMessages.instance().add("Registro inserido com sucesso.");
		}
		abreToogle = true;
		verificarProcesso();
		refreshGrids();
	}
	
	/**
	 * [PJEII-2623] [PJEII-2116] [PJEII-1868] - Cristiano Nascimento
	 * Caso exista algum caracter que o sistema nao consiga converter para ISO8859-1, 
	 * o sistema abre um modal de confirmacao de exclusão de caracteres especiais. 
	 * Caso o usuario confirme, o sistema excluirá os caracteres especiais.
	 * @return boolean
	 */
	public boolean validaCaracteresEspeciaisEditorTexto(String texto){
		
		if (Util.isStringSemCaracterUnicode(texto)) {
			flagMostraModalRemocaoCaracteresEspeciais = false;
			return true;
		}
		flagMostraModalRemocaoCaracteresEspeciais = true;
		return false;
	}

	/**
	 * [PJEII-2623] [PJEII-2116] [PJEII-1868]- Cristiano Nascimento
	 * Remove os caracteres do editor de texto que não são convertidos ao formato ISO8859-1
	 * e depois salva o documento com o editor atualizado.
	 * @return void
	 */
	public void excluiCaracteresEspeciaisEditorTexto() {
		String texto = ProcessoDocumentoBinHome.instance().getModeloDocumento();
		excluiCaracteresEspeciaisEditorTexto(texto);
	}
	
	public void excluiCaracteresEspeciaisEditorTexto(String texto) {
		for (int i=0; i< texto.length(); i++) {
			if (!Util.isStringSemCaracterUnicode(Character.toString(texto.charAt(i)))) {
				texto = StringUtils.replace(texto, Character.toString(texto.charAt(i)), "");
				i--;
			}
		}
		pdbHtml.setModeloDocumento(texto);
		salvarDocumentos(ProcessoDocumentoBinHome.instance());
	}
	
	public void refreshGrids() {
		Contexts.removeFromAllContexts("processoTrfDocumentoGrid");
		Contexts.removeFromAllContexts("processoTrfDocumentoAdvogadoGrid");
		Contexts.removeFromAllContexts("documentosNaoAssinadosMagAuxiliarGrid");
		Contexts.removeFromAllContexts("documentosNaoAssinadosGrid");
		Contexts.removeFromAllContexts("documentoProcessoGrid");
		Contexts.removeFromAllContexts("processoTrfDocumentoImpressoGrid");
		Contexts.removeFromAllContexts("assinaturasGrid");
		Contexts.removeFromAllContexts("processoDocumentoList");
		Contexts.removeFromAllContexts("processoTrfDocumentoPaginatorGrid");
		ProcessoDocumentoHome.instance().refreshGridsDocumentos();
		ProcessoDocumentoBinPessoaAssinaturaHome.instance().limpar();
	}

	public String setDownloadInstance(ProcessoDocumento obj) {
		exportData(obj);
		return "/download.xhtml";
	}

	public void addMenssagemAssinado() {
		FacesMessages.instance().add("Documento assinado com sucesso.");
	}

	private void exportData(ProcessoDocumento obj) {
		FileHome fileHome = new FileHome();
		ProcessoDocumentoBin bin = obj.getProcessoDocumentoBin();
		fileHome.setFileName(bin.getNomeArquivo());
		try {
			byte[] data = documentoBinManager.getData(bin.getNumeroDocumentoStorage());
			fileHome.setData(data);
		} catch (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Erro ao abrir o documento.");
			log.error("Erro ao abrir documento." + e.getMessage());
		}
		Contexts.getConversationContext().set("fileHome", fileHome);
	}

	public void setPdHtml(ProcessoDocumento pdHtml) {
		this.pdHtml = pdHtml;
	}

	public ProcessoDocumento getPdHtml() {
		return pdHtml;
	}

	public void setPdbHtml(ProcessoDocumentoBin pdbHtml) {
		this.pdbHtml = pdbHtml;
	}

	public ProcessoDocumentoBin getPdbHtml() {
		return pdbHtml;
	}

	public String getUrlDocsField() {
		ProcessoDocumentoBinHome.instance().setInstance(pdbHtml);
		ProcessoDocumentoHome.instance().setInstance(pdHtml);
		if (ProcessoDocumentoHome.instance().getInstance().getIdProcessoDocumento() != 0) {
			ProcessoDocumentoHome.instance().update();
		} else {
			ProcessoDocumentoHome.instance().persist();
		}

		StringBuilder sb = new StringBuilder();
		if (ProcessoDocumentoHome.instance() != null
				&& ProcessoDocumentoHome.instance().getInstance().getIdProcessoDocumento() != 0) {
			if (sb.length() > 0) {
				sb.append(',');
			}
			sb.append(gerarLinkDownload(ProcessoDocumentoHome.instance().getInstance()));
		}
		if (pdHtml.getIdProcessoDocumento() != 0) {
			for (ProcessoDocumento obj : listaPdfAssociados(pdHtml)) {
				boolean assinouDocumento = pessoaLogadaAssinouDocumento(obj.getProcessoDocumentoBin());
				if (!assinouDocumento) {
					if (sb.length() > 0) {
						sb.append(',');
					}
					sb.append(gerarLinkDownload(obj));
				}
			}
		}
		return sb.toString();
	}

	private String gerarLinkDownload(ProcessoDocumento pd) {
		StringBuilder sb = new StringBuilder();
		sb.append("id=");
		sb.append(String.valueOf(pd.getIdProcessoDocumento()));
		sb.append("&codIni=");
		sb.append(ProcessoDocumentoHome.instance().getCodData(pd));
		sb.append("&md5=");
		sb.append(pd.getProcessoDocumentoBin().getMd5Documento());
		sb.append("&isBin=");
		sb.append(pd.getProcessoDocumentoBin().getExtensao() != null);
		return sb.toString();
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

	public void limparTelaNewIntance() {
		limparTela();
		ProcessoDocumentoHome.instance().newInstance();
	}

	public void limparTela() {
		if (flagLimparTela) {
			boolean naoAssinado = false;
			for (ProcessoDocumento obj : listaPdfAssociados(ProcessoDocumentoHome.instance().getInstance())) {
				if (!ProcessoHome.instance().verificarPessoaAssinatura(obj)) {
					naoAssinado = true;
				}
			}
			if (!naoAssinado) {
				newInstanceHtml();
				newInstancePdf();
				setDocumentoInicial(false);
				setPermiteAssinatura(false);
				setLiberarConsultaPublica(false);
				setRenderedConsultaPublica(false);
				setProtocolarDocumentoBean(null);
				ProcessoDocumentoHome.instance().clearInstance();
			}
			refreshGrids();
		}
	}

	public void onSelectProcessoDocumento() {
		ProcessoDocumentoHome.instance().setInstance(pdHtml);
		ProcessoDocumentoHome.instance().onSelectProcessoDocumento();
	}

	public void removerDocumentos() {
		if (pdHtml != null && pdHtml.getIdProcessoDocumento() != 0) {
			for (ProcessoDocumento obj : listaPdfAssociados(pdHtml)) {
				ProcessoDocumentoHome.instance().removerDocumento(obj,
						ProcessoTrfHome.instance().getInstance().getIdProcessoTrf());
			}
			ProcessoDocumentoHome.instance().removerDocumento(pdHtml,
					ProcessoTrfHome.instance().getInstance().getIdProcessoTrf());
		}

		ProcessoDocumentoHome.instance().setInstance(null);
		Contexts.removeFromAllContexts("processoDocumentoHome");
		Contexts.removeFromAllContexts("processoDocumentoBinHome");

		newInstance();
		actionAbaAnexar();
		if (ProcessoDocumentoHome.instance().getInstance() != null
				&& ProcessoDocumentoHome.instance().getInstance().getIdProcessoDocumento() != 0) {
			ProcessoDocumentoHome.instance().setInstance(null);
		}
		if (ProcessoDocumentoBinHome.instance().getInstance() != null
				&& ProcessoDocumentoBinHome.instance().getInstance().getIdProcessoDocumentoBin() != 0) {
			ProcessoDocumentoBinHome.instance().setInstance(null);
		}
	}

	public ArrayList<TipoProcessoDocumentoTrf> tipoProcessoDocumentoItemsHtml() {
		if (tipoProcessoDocumentoItemsHtmlList == null) {
			tipoProcessoDocumentoItemsHtmlList = tipoProcessoDocumentoItems(TipoDocumentoEnum.P);
			if(ProcessoTrfHome.instance() != null && ProcessoTrfHome.instance().isCadastroProcessoClet()){
				if(tipoProcessoDocumentoItemsHtmlList == null){
					tipoProcessoDocumentoItemsHtmlList = new ArrayList<TipoProcessoDocumentoTrf>();
				}
				TipoProcessoDocumentoTrf tipoTermoExecucao = EntityUtil.find(TipoProcessoDocumentoTrf.class, ParametroUtil.instance().getTipoProcessoDocumentoTermoAberturaExecucao().getIdTipoProcessoDocumento());
				if(!tipoProcessoDocumentoItemsHtmlList.contains(tipoTermoExecucao)){
					tipoProcessoDocumentoItemsHtmlList.add(tipoTermoExecucao);
				}
				TipoProcessoDocumentoTrf tipoTermoLiquidacao = EntityUtil.find(TipoProcessoDocumentoTrf.class, ParametroUtil.instance().getTipoProcessoDocumentoTermoAberturaLiquidacao().getIdTipoProcessoDocumento());
				if(!tipoProcessoDocumentoItemsHtmlList.contains(tipoTermoLiquidacao)){
					tipoProcessoDocumentoItemsHtmlList.add(tipoTermoLiquidacao);
				}
			}
		}
		
		return tipoProcessoDocumentoItemsHtmlList;
	}

	public ArrayList<TipoProcessoDocumentoTrf> tipoProcessoDocumentoItemsPdf() {
		if (tipoProcessoDocumentoItemsPdfList == null) {
		
			boolean isDocumentoInicial = this.documentoInicial;
	
			this.documentoInicial = false;
			tipoProcessoDocumentoItemsPdfList = tipoProcessoDocumentoItems(TipoDocumentoEnum.D);
			this.documentoInicial = isDocumentoInicial;
		}
		return tipoProcessoDocumentoItemsPdfList;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<TipoProcessoDocumentoTrf> tipoProcessoDocumentoItems(TipoDocumentoEnum tipo) {
		ArrayList<TipoProcessoDocumentoTrf> lista;
		StringBuilder sb = new StringBuilder();
		sb.append("select o from TipoProcessoDocumentoTrf o ");
		sb.append("where o.ativo = true ");
		sb.append("and (o.inTipoDocumento = :tipo or o.inTipoDocumento = 'T') ");
		if (!documentoInicial) {
			sb.append("and o.tipoProcessoDocumento != 'Petição Inicial' ");
		}
		sb.append("and o in (select tpd.tipoProcessoDocumento from AplicacaoClasseTipoProcessoDocumento tpd ");
		sb.append("          where tpd.aplicacaoClasse.idAplicacaoClasse = :aplicacaoSistema) ");
		sb.append("and o.idTipoProcessoDocumento != :despacho ");
		sb.append("and o.idTipoProcessoDocumento != :sentenca ");
		sb.append("and o.idTipoProcessoDocumento != :decisao ");
		sb.append("and o.idTipoProcessoDocumento != :acordao ");
		sb.append("and o in ( select p.tipoProcessoDocumento from TipoProcessoDocumentoPapel p ");
		sb.append("where p.papel.identificador = :papel) ");
		sb.append("order by o.tipoProcessoDocumento ");

		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("tipo", tipo);
		q.setParameter("aplicacaoSistema", Integer.parseInt((String) Contexts.getApplicationContext().get("aplicacaoSistema")) );
		q.setParameter("despacho", Integer.parseInt((String)Contexts.getApplicationContext().get("idTipoProcessoDocumentoDespacho")));
		q.setParameter("sentenca", Integer.parseInt((String)Contexts.getApplicationContext().get("idTipoProcessoDocumentoSentenca")));
		q.setParameter("decisao", Integer.parseInt((String)Contexts.getApplicationContext().get("idTipoProcessoDocumentoDecisao")));
		q.setParameter("acordao", Integer.parseInt((String)Contexts.getApplicationContext().get("idTipoProcessoDocumentoAcordao")));
		q.setParameter("papel", (String)Authenticator.getPapelAtual().getIdentificador());
		
		lista = (ArrayList<TipoProcessoDocumentoTrf>) q.getResultList();
		
		if (lista == null || lista.isEmpty()) {
			lista = new ArrayList<TipoProcessoDocumentoTrf>();
		}
		return lista;
	}

	public List<TipoProcessoDocumentoTrf> tipoProcessoDocumentoHabilitacaoAutosItems() {
		List<TipoProcessoDocumentoTrf> list = tipoProcessoDocumentoItems(TipoDocumentoEnum.P);
		List<TipoProcessoDocumentoTrf> listHabilitacaoAutos = new ArrayList<TipoProcessoDocumentoTrf>();
		for (TipoProcessoDocumentoTrf tipoProcessoDocumento : list) {
			if (tipoProcessoDocumento.getHabilitacaoAutos()) {
				listHabilitacaoAutos.add(tipoProcessoDocumento);
			}
		}
		return listHabilitacaoAutos;

	}

	@SuppressWarnings("unchecked")
	public ArrayList<ProcessoDocumento> listaPdfAssociados(ProcessoDocumento pd) {
		ArrayList<ProcessoDocumento> lista = new ArrayList<ProcessoDocumento>();
		if (pd.getIdProcessoDocumento() != 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("select o.documentoAssociado from ProcessoDocumentoAssociacao o ");
			sb.append("where o.processoDocumento = :pd ");
			sb.append(" order by o.documentoAssociado.idProcessoDocumento asc");

			Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
			q.setParameter("pd", pd);
			lista = (ArrayList<ProcessoDocumento>) q.getResultList();
		}
		return lista;
	}
	
	/**
	 * Recupera a quantidade de pdf's associados ao processo documento.
	 * Faz uma operação de agrupamento no banco de dados (count) para diminuir 
	 * melhorar a consulta ao banco (evitar full scan table)
	 * 
	 * @param pd processo documento
	 * 
	 * @return quantidade de pdf's associados ao documento do processo.
	 */
	public Long qtdPdfAssociados(ProcessoDocumento pd) {
		Long lista = 0L;
		if (pd.getIdProcessoDocumento() != 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("select count(o.documentoAssociado) from ProcessoDocumentoAssociacao o ");
			sb.append("where o.processoDocumento = :pd");
			Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
			q.setParameter("pd", pd);
			try {
				lista = (Long) q.getSingleResult();
			} catch(NoResultException ex) {
				lista = 0L;
			}
		}
		return lista;
	}

	public Boolean existePdf() {
		long lista = qtdPdfAssociados(ProcessoDocumentoHome.instance().getInstance());
		if (lista > 0) {
			setMensagemPdf("Assinando este documento, você protocolará também " + lista
					+ " documento(s) anexado(s), deseja prosseguir?");
			setMensagemDocNaoAssinado("Excluindo este documento, você estará excluindo " + lista
					+ " documento(s) anexado(s), deseja prosseguir?");
			return true;
		} else {
			setMensagemPdf(null);
			setMensagemDocNaoAssinado("Deseja realmente excluir o documento?");
			return false;
		}
	}

	public String mensagemDocNaoAssinado(ProcessoDocumento pd) {
		if (pd != null) {
			ProcessoDocumentoHome.instance().setInstance(pd);
			existePdf();
		}
		return getMensagemDocNaoAssinado();
	}

	public void setarDocumentos() {
		newInstanceHtml();
		newInstancePdf();

		setPdHtml(ProcessoDocumentoHome.instance().getInstance());
		setPdbHtml(ProcessoDocumentoHome.instance().getInstance().getProcessoDocumentoBin());
		ProcessoTrfHome.instance()
				.setInstance(EntityUtil.find(ProcessoTrf.class, pdHtml.getProcesso().getIdProcesso()));

		verificarProcesso();
		verificarDocumento();
	}

	private void verificarProcesso() {
		ProcessoDocumentoHome.instance().setInstance(pdHtml);
		ProcessoDocumentoBinHome.instance().setInstance(pdbHtml);
		setProcessoRemetido(ProcessoTrfHome.instance().verificaRemetido2Grau(
				EntityUtil.find(ProcessoTrf.class, pdHtml.getProcesso().getIdProcesso())));
	}

	public void setProcessoRemetido(boolean processoRemetido) {
		this.processoRemetido = processoRemetido;
	}

	public boolean isProcessoRemetido() {
		return processoRemetido;
	}

	public void setDocumentoInicial(boolean documentoInicial) {
		this.documentoInicial = documentoInicial;
	}

	public boolean isDocumentoInicial() {
		return documentoInicial;
	}

	public void setModeloDocumento(ModeloDocumentoLocal modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	public ModeloDocumentoLocal getModeloDocumento() {
		return modeloDocumento;
	}

	public void processarModelo() {
		if (modeloDocumento != null) {
			Localizacao localizacaoAtual = Authenticator.getLocalizacaoAtual();
			AdvogadoLocalizacaoCabecalho localizacaoCabecalho = advogadoLocalizacaoCabecalhoDao.getAdvogadoLocalizacaoCabecalho(localizacaoAtual);
			setarConteudoDoCabecalhoModelo(localizacaoCabecalho);
		} else {
			pdbHtml.setModeloDocumento(null);
		}
	}

	public boolean assinar() {
		try {
			if((ProcessoDocumentoHome.instance().getInstance().getTipoProcessoDocumento() == ProcessoTrfHome.instance().getInstance().getClasseJudicial().getTipoProcessoDocumentoInicial()) &&
					temPeticaoInicial(ProcessoDocumentoHome.instance().getInstance())){
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Já existe uma Petição Inicial cadastrada.");
				return false;
			}	
			ProcessoDocumento pd = pdHtml;
			pd.setProcessoDocumentoBin(pdbHtml);
			ProcessoDocumentoBin pdb = pdbHtml;
			ProcessoTrf proc = processoJudicialService.findById(pd.getProcesso().getIdProcesso());
			if(certChain != null || signature != null){
				pdb.setCertChain(certChain);
				pdb.setSignature(signature);
				pd = documentoJudicialService.finalizaDocumento(pd, proc, null, false);
				flagLimparTela = true;
				if (!EventsTipoDocumentoTreeHandler.instance().getEventoBeanList().isEmpty()) {
					EventosTreeHandler.instance().setEventoBeanList(
							EventsTipoDocumentoTreeHandler.instance().getEventoBeanList());
				}
				EntityUtil.getEntityManager().flush();
				pdbHtml = pd.getProcessoDocumentoBin();
				pdHtml = pd;
				ProcessoDocumentoHome.instance().setInstance(pdHtml);
				ProcessoDocumentoBinHome.instance().setInstance(pdbHtml);
				documentosSalvos.add(pd);
				return true;
			}
		} catch (PJeBusinessException e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR,e.getMessage(), e.getParams());
		} catch (PJeDAOException e) {
			String msgErro = "Erro na gravação da assinatura: " + e.getMessage();
		  	FacesMessages.instance().add(Severity.ERROR, msgErro);
		  	log.error("Erro na gravação da assinatura: " + e.getMessage());
		} catch (CertificadoException e) {
		  	String msgErro = "Erro na verificação do certificado: " + e.getMessage();
		  	FacesMessages.instance().add(Severity.ERROR, msgErro);
		  	log.error("Erro na verificação do certificado" + e.getMessage());
		}
		return false;
	}
	
	public boolean assinarPDF() {
		try {
			ProcessoDocumentoBinHome pdbInstance = ProcessoDocumentoBinHome.instance();
			if(!VerificaCertificado.instance().isModoTesteCertificado()){
				try {
					ProcessoDocumento pd = pdPdf;
					pd.setProcessoDocumentoBin(pdbPdf);
					ProcessoDocumentoBin pdb = pdbPdf;
					ProcessoTrf proc = processoJudicialService.findById(pd.getProcesso().getIdProcesso());
					if(certChain != null || signature != null){
						pdb.setCertChain(certChain);
						pdb.setSignature(signature);
						pd = documentoJudicialService.finalizaDocumento(pd, proc, null, false);
						flagLimparTela = true;
						if (!EventsTipoDocumentoTreeHandler.instance().getEventoBeanList().isEmpty()) {
							EventosTreeHandler.instance().setEventoBeanList(
									EventsTipoDocumentoTreeHandler.instance().getEventoBeanList());
						}
						EntityUtil.getEntityManager().flush();
						pdbPdf = pd.getProcessoDocumentoBin();
						pdPdf = pd;
						ProcessoDocumentoHome.instance().setInstance(pdPdf);
						ProcessoDocumentoBinHome.instance().setInstance(pdbPdf);
						
						documentosSalvos.add(pd);
						
						return true;
					}
				} catch (PJeBusinessException e) {
					FacesMessages.instance().addFromResourceBundle(Severity.ERROR,e.getMessage(), e.getParams());
					return false;
				}				
			}else{
				if (pdbPdf.getIdProcessoDocumentoBin() != 0) {
					pdbInstance.setId(pdbPdf.getIdProcessoDocumentoBin());
				} else {
					pdbInstance.setInstance(pdbPdf);
				}
				pdbInstance.setCertChain(certChain);
				pdbInstance.setSignature(signature);

				ProcessoDocumentoHome pdInstance = ProcessoDocumentoHome.instance();
				ProcessoDocumentoBinHome.instance().isModelo(false);
				if (pdPdf.getIdProcessoDocumento() != 0) {
					pdInstance.setId(pdPdf.getIdProcessoDocumento());
				} else {
					pdInstance.setInstance(pdPdf);
				}
				if(pdPdf.getProcesso() != null && pdPdf.getProcesso().getIdProcesso() != 0){
					ProcessoHome.instance().setId(pdPdf.getProcesso().getIdProcesso());
				}
				pdInstance.persistComAssinatura();
				pdInstance.apreciarDocumentoAutomaticamente();
				
				documentosSalvos.add(pdInstance.getInstance());
				
				return true;
			}
			return false;
		} catch (CertificadoException e) {
			String msgErro = "Erro na verificação do certificado: " + e.getMessage();
			FacesMessages.instance().add(Severity.ERROR, msgErro);
			log.error("Erro na verificação do certificado" + e.getMessage());
		}
		return false;
	}

	
	public void assinarIndividual(boolean clear){
        boolean success = false;
        // [PJEII-2623] [PJEII-2116] [PJEII-1868] - Cristiano Nascimento
	  	// Validação de caracteres especiais antes de assinar digitalmente. 
        // Caso exista caracteres no editor de texto que o sistema não consegue converter para ISO-8859, o sistema exibirá uma mensagem de erro.
		// Não foi implementado um modal de confirmação para exclusão de caracteres, pois o botão Assinar Digitalmente não tem a opção de autocomplete e não aceita alteração no componente.
        if (validaCaracteresEspeciaisEditorTexto(pdbHtml.getModeloDocumento())) {
            success = assinar();
        } else {
            String msgErro = "Foram encontrados caracteres que o sistema não reconhece no editor de texto. Favor clicar em Gravar o documento antes de assinar digitalmente.";
            FacesMessages.instance().add(Severity.ERROR, msgErro);
        }
        if (success && clear) {
            limparTela();
        }
    }

	public void assinarIndividual() {
		assinarIndividual(true);
	}

	public boolean isAssinaturaIndividual() {
		if (permiteAssinatura) {
			return false;
		}
		ProcessoDocumento processoDocumento = ProcessoDocumentoHome.instance().getInstance();
		if (processoDocumento.getIdProcessoDocumento() != 0) {
			return qtdPdfAssociados(processoDocumento).equals(0L);
		}
		return true;
	}

	public void setPermiteAssinatura(boolean permiteAssinatura) {
		this.permiteAssinatura = permiteAssinatura;
	}

	public boolean isPermiteAssinatura() {
		return permiteAssinatura;
	}

	public void validacaoPdf() {
		if (!isDocumentoBinValido(FileHome.instance())) {
			return;
		}
	}

	private boolean isDocumentoBinValido(FileHome file) {
		if ((file == null || file.getData() == null) && !flag) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Nenhum documento selecionado.");
			return false;
		}
		try {
			br.com.itx.util.FileUtil.validarTipoTamanhoArquivo(file, "application/pdf");
		} catch (PJeBusinessException ex) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, ex.getCode());
			newInstancePdf();
			flag = true;
			return false;
		}
		//--Verificação do tamanho máximo da página de um pdf
		if (pdPdf != null && pdPdf.getTipoProcessoDocumento() != null) {
			TipoProcessoDocumentoTrf tpd = EntityUtil.find(TipoProcessoDocumentoTrf.class, pdPdf.getTipoProcessoDocumento().getIdTipoProcessoDocumento());
			if (tpd != null) {
				try {
					PdfReader pdf = new PdfReader(file.getData());
					if (!PdfUtil.verificarTamanhoValidoPagina(pdf, tpd) && Authenticator.getPapelAtual().getNome().equals(ParametroUtil.instance().getPapelAdvogado().getNome())) {
						FacesMessages.instance().add(StatusMessage.Severity.ERROR, ParametroUtil.getParametro("textoEmailUploadDocs") + ParametroUtil.getParametro("textoEmailUploadDocs2"));
						return false;
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	
	 public boolean validarPdf(FileHome fileHome)
	  {
	  if (!isDocumentoBinValido(fileHome)) {
	  return false;
	  }
	  return true;
	 
	  }

	public boolean getAbreToogle() {
		return abreToogle;
	}

	public void setAbreToogle(boolean abreToogle) {
		this.abreToogle = abreToogle;
	}

	/**
	 * Método responsável por verificar se o usuário logado possui permissão para assinar o documento (RN311).
	 * 
	 * @return Verdadeiro se o usuário não assinou o documento e, caso seja assistente, 
	 * o perfil possua o recurso de assinar digitalmente. Falso, caso contrário. 
	 */
	public boolean liberaCertificacao() {
		UsuarioLocalizacao usuarioLocalizacao = Authenticator.getUsuarioLocalizacaoAtual();
		if (!pessoaAssinouDocumento()) {
			if (usuarioLocalizacao.getPapel().equals(ParametroUtil.instance().getPapelAssistenteAdvogado())) {
				return ((PessoaAssistenteAdvogadoLocal) usuarioLocalizacao).getAssinadoDigitalmente();
			}
			return true;
		}
		return false;
	}

	/**
	 * Verifica se a pessoa já assinou o documento
	 * 
	 * @return
	 */
	private boolean pessoaAssinouDocumento() {
		Usuario usuarioLogado = Authenticator.getUsuarioLogado();
		if(usuarioLogado == null){
			return false;
		}
		if (pdHtml != null && pdHtml.getProcessoDocumentoBin() != null && pdHtml.getProcessoDocumentoBin().getIdProcessoDocumentoBin() != 0) {
			String q = "SELECT COUNT(a.idProcessoDocumentoBinPessoaAssinatura) FROM ProcessoDocumentoBinPessoaAssinatura AS a "
					+ "	WHERE a.processoDocumentoBin.idProcessoDocumentoBin = :idDoc "
					+ "		AND a.pessoa.idUsuario = :idUsuario";
			em = EntityUtil.getEntityManager();
			Query query = em.createQuery(q);
			query.setParameter("idDoc", pdHtml.getProcessoDocumentoBin().getIdProcessoDocumentoBin());
			query.setParameter("idUsuario", usuarioLogado.getIdUsuario());
			query.setMaxResults(1);
			Number cont = (Number) query.getSingleResult();
			return cont.intValue() > 0;
		}
		return false;
	}

	/**
	 * Verifica se a pessoa já assinou o documento
	 * 
	 * @return
	 */
	private boolean pessoaLogadaAssinouDocumento(ProcessoDocumentoBin bin) {
		StringBuilder sb = new StringBuilder();

		sb.append(" select count(o.idProcessoDocumentoBinPessoaAssinatura) from ");
		sb.append(" ProcessoDocumentoBinPessoaAssinatura o ");
		sb.append(" where o.processoDocumentoBin = :idBin and o.pessoa = :pessoa");
		em = EntityUtil.getEntityManager();
		Query query = em.createQuery(sb.toString());
		query.setParameter("idBin", bin);
		query.setParameter("pessoa", Authenticator.getPessoaLogada());
		try {
			Long count = (Long) query.getSingleResult();
			return count.compareTo(0L) > 0;
		} catch(NoResultException ex) {
			return false;
		}
	}
	
	/**
	 * Este método verifica se o processo já possui uma petição inicial diferente da petição editada.
	 * @param documentoEditado - Documento carregado no editor.
	 * @return
	 */
	private boolean temPeticaoInicial(ProcessoDocumento documentoEditado) {
		TipoProcessoDocumento tipoPeticaoInicial = ProcessoTrfHome.instance().getInstance().getClasseJudicial().getTipoProcessoDocumentoInicial();
		
		if( ProcessoTrfHome.instance().getInstance() != null && ProcessoTrfHome.instance().getInstance().getProcesso() != null ){
			// PJEII-6364 - Impede que seja cadastrada mais de uma petição inicial ao protocolar um processo
		  	for(ProcessoDocumento pd : ProcessoTrfHome.instance().getInstance().getProcesso().getProcessoDocumentoList())
		  	{
		  		if(pd.getTipoProcessoDocumento().getIdTipoProcessoDocumento() == tipoPeticaoInicial.getIdTipoProcessoDocumento())
		  			return true;
			}
		}		
		return false;
	}

	public void setVerificaDocumento(boolean verificaDocumento) {
		this.verificaDocumento = verificaDocumento;
	}

	public boolean isVerificaDocumento() {
		return verificaDocumento;
	}

	public void setFlagLimparTela(boolean flagLimparTela) {
		this.flagLimparTela = flagLimparTela;
	}

	public boolean getFlagLimparTela() {
		return flagLimparTela;
	}

	public String getMensagemPdf() {
		return mensagemPdf;
	}

	public void setMensagemPdf(String mensagemPdf) {
		this.mensagemPdf = mensagemPdf;
	}

	public String getMensagemDocNaoAssinado() {
		return mensagemDocNaoAssinado;
	}

	public void setMensagemDocNaoAssinado(String mensagemDocNaoAssinado) {
		this.mensagemDocNaoAssinado = mensagemDocNaoAssinado;
	}

	public void setLiberarConsultaPublica(boolean liberarConsultaPublica) {
		this.liberarConsultaPublica = liberarConsultaPublica;
	}

	public boolean getLiberarConsultaPublica() {
		return liberarConsultaPublica;
	}

	public void setRenderedConsultaPublica(boolean renderedConsultaPublica) {
		this.renderedConsultaPublica = renderedConsultaPublica;
	}

	public boolean getRenderedConsultaPublica() {
		return renderedConsultaPublica;
	}
	
	public boolean isFlagMostraModalRemocaoCaracteresEspeciais() {
		return flagMostraModalRemocaoCaracteresEspeciais;
	}

	public void setFlagMostraModalRemocaoCaracteresEspeciais(boolean flagModalRemocaoCaracteresEspeciais) {
		this.flagMostraModalRemocaoCaracteresEspeciais = flagModalRemocaoCaracteresEspeciais;
	}

	public List<ProcessoDocumento> getDocumentosSalvos(){
		return documentosSalvos;
	}

	public void setDocumentosSalvos(List<ProcessoDocumento> documentosSalvos){
		this.documentosSalvos = documentosSalvos;
	}
	
	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}

	public void setProtocolarDocumentoBean(ProtocolarDocumentoBean protocolarDocumentoBean) {
		this.protocolarDocumentoBean = protocolarDocumentoBean;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.protocolarDocumentoBean.addArquivoAssinado(arquivoAssinadoHash);
	}

	@Override
	public String getActionName() {
		return NAME;
	}

}
