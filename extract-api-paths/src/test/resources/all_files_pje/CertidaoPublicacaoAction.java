package br.com.infox.pje.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.com.infox.cliente.home.SessaoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.actions.ModeloDocumentoAction;
import br.com.infox.pje.list.SessaoPautaRelacaoJulgamentoList;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinPessoaAssinaturaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.view.BaseAction;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrfSemFiltro;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

/**
 * Classe de controle da aba de certidão de publicação.
 */
@Name(CertidaoPublicacaoAction.NAME)
@Scope(ScopeType.PAGE)
public class CertidaoPublicacaoAction extends BaseAction<SessaoPautaProcessoTrf> implements ArquivoAssinadoUploader {

	private static final long serialVersionUID = -6178392064557288291L;

	public static final String NAME = "certidaoPublicacaoAction";
	
	private List<SessaoPautaProcessoTrf> sessaoPautaProcessos = new ArrayList<SessaoPautaProcessoTrf>(0);
	private TipoProcessoDocumento tipoProcessoDocumentoCertidaoPublicacao;
	private List<ModeloDocumento> modelosDocumentoCertidaoPublicacao;
	private ModeloDocumento modeloDocumentoCertidaoPublicacao;
	private String modeloDocumento;
	private ProcessoTrf processoSelecionado;
	private ProcessoDocumento certidaoPublicacaoSelecionada;
	private Map<ProcessoTrf, Boolean> processosSelecionadosInclusaoCertidao = new HashMap<ProcessoTrf, Boolean>(0);
	private Map<ProcessoTrf, Boolean> processosSelecionadosAssinaturaCertidao = new HashMap<ProcessoTrf, Boolean>(0);
	
	private Map<ProcessoTrf, ProcessoDocumento> cacheCertidoes = new LinkedHashMap<ProcessoTrf, ProcessoDocumento>(0);
	private Map<SessaoPautaProcessoTrf, PessoaMagistrado> cacheMagistrados = new LinkedHashMap<SessaoPautaProcessoTrf, PessoaMagistrado>(0);
	private List<ArquivoAssinadoHash> arquivosAssinados = new ArrayList<ArquivoAssinadoHash>();
	
	@In
	private ParametroUtil parametroUtil;
	
	@In
	private ProcessoJudicialService processoJudicialService;
	
	@In
	private SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager;
	
	@In
	private PessoaMagistradoManager pessoaMagistradoManager;
	
	@In
	private ModeloDocumentoManager modeloDocumentoManager;
	
	@In
	private ProcessoDocumentoBinManager processoDocumentoBinManager;
	
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	
	@In
	private SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager;
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	
	@In
	private ProcessoDocumentoBinPessoaAssinaturaManager processoDocumentoBinPessoaAssinaturaManager;
	
	private SessaoPautaRelacaoJulgamentoList sessaoPautaRelacaoJulgamentoList;
	
	
	/**
	 * Método responsável pela inicialização da classe.
	 */
	@Create
	public void init(){
		try {
			pesquisarProcessos();
		} catch (PontoExtensaoException | PJeBusinessException ex) {
			facesMessages.addFromResourceBundle(Severity.ERROR,"msg.erro.recuperar.processos", ex.getMessage());
			logger.error(ex);
		}
		initTipoProcessoDocumentoCertidaoPublicacao();
	}
	
	/**
	 * Método responsável por pesquisar os processos que já foram enviados para publicação.
	 *  
	 * @throws PontoExtensaoException Caso ocorra algum erro ao comunicar-se com o DJe.
	 * @throws PJeBusinessException 
	 */
	private void pesquisarProcessos() throws PontoExtensaoException, PJeBusinessException {
		if(getSessaoPautaRelacaoJulgamentoList() != null) {
			List<SessaoPautaProcessoTrf> sessaoPautaProcessosRelacaoJulgamento = this.sessaoPautaRelacaoJulgamentoList.getResultList();
			for (SessaoPautaProcessoTrf sessaoPautaProcessoRelacaoJulgamento : sessaoPautaProcessosRelacaoJulgamento) {
				Date dataPublicacaoDje = this.sessaoPautaProcessoTrfManager.recuperarDataPublicacaoDje(
						this.sessaoPautaProcessoTrfManager.recuperarExpedientePublicadoDje(sessaoPautaProcessoRelacaoJulgamento));
				
				if (dataPublicacaoDje != null) {
					this.sessaoPautaProcessos.add(sessaoPautaProcessoRelacaoJulgamento);
				}
			}
		}
	}
	
	/**
	 * Método responsável por inicializar a variável {@link CertidaoPublicacaoAction#tipoProcessoDocumentoCertidaoPublicacao} 
	 * com o {@link TipoProcessoDocumento} utilizado na criação da certidão de publicação.
	 */
	private void initTipoProcessoDocumentoCertidaoPublicacao() {
		this.tipoProcessoDocumentoCertidaoPublicacao = this.parametroUtil.getTipoProcessoDocumentoCertidaoPublicacao();
		initModelosDocumentoCertidaoPublicacao(this.tipoProcessoDocumentoCertidaoPublicacao);
	}
	
	/**
	 * Método responsável por inicializar a variável {@link CertidaoPublicacaoAction#modelosDocumentoCertidaoPublicacao}.
	 * com uma lista de {@link ModeloDocumento} associados ao tipo de documento.
	 * 
	 * @param tipoProcessoDocumento Tipo de documento.
	 */
	private void initModelosDocumentoCertidaoPublicacao(TipoProcessoDocumento tipoProcessoDocumento) {
		if (tipoProcessoDocumento != null) {
			try {
				this.modelosDocumentoCertidaoPublicacao = this.modeloDocumentoManager.getModelos(
						tipoProcessoDocumento, Authenticator.getLocalizacoesFilhasAtuais());
				
			} catch (PJeBusinessException ex) {
				facesMessages.addFromResourceBundle(Severity.ERROR,"msg.erro.recuperar.modelos", ex.getMessage());
				logger.error(ex);
			}
		}
	}
	
	/**
	 * Método responsável por obter a URL de detalhes do processo.
	 * 
	 * @param idProcessoTrf Identificador do processo.
	 * @return A URL de detalhes do processo.
	 */
	public String getUrlProcesso(int idProcessoTrf) {
		final String BASE_URL = "/Processo/ConsultaProcesso/Detalhe/detalheProcessoVisualizacao.seam?id=";
		
		return facesContext.getExternalContext().getRequestContextPath() + 
			BASE_URL + idProcessoTrf + "&ca=" + SecurityTokenControler.instance().gerarChaveAcessoProcesso(idProcessoTrf);
	}
	
	/**
	 * Método responsável por obter o nome das partes do processo.
	 * 
	 * @param consultaProcessoTrf Processo.
	 * @return O nome das partes do processo.
	 */
	public String getNomePartes(ConsultaProcessoTrfSemFiltro consultaProcessoTrfSemFiltro) {
		final String nomePoloAtivo = this.processoJudicialService.nomeParaExibicao(
				consultaProcessoTrfSemFiltro.getAutor(), consultaProcessoTrfSemFiltro.getQtAutor());
		
		final String nomePoloPassivo = processoJudicialService.nomeParaExibicao(
				consultaProcessoTrfSemFiltro.getReu(), consultaProcessoTrfSemFiltro.getQtReu());
		
		return nomePoloAtivo + " X " + nomePoloPassivo;
	}
	
	/**
	 * Método responsável por gravar o documento de certidão de publicação.
	 */
	public void gravarCertidaoPublicacao() throws PJeBusinessException {
		int qtd = 0;
		
		if (isDocumentoValido(this.modeloDocumento)) {
			if (isManaged()) {
				atualizarCertidaoPublicacao(this.certidaoPublicacaoSelecionada, this.modeloDocumento);
				selecionarCheckAssinaturaCertidao(this.processoSelecionado);
				qtd++;
			} else {
				Date dataAtual = Calendar.getInstance().getTime();
				for (ProcessoTrf processoTrf : this.processosSelecionadosInclusaoCertidao.keySet()) {
					if (this.processosSelecionadosInclusaoCertidao.get(processoTrf) == true) {
						criarCertidao(dataAtual, this.modeloDocumento, this.tipoProcessoDocumentoCertidaoPublicacao, processoTrf);
						selecionarCheckAssinaturaCertidao(processoTrf);
						qtd++;
					}
				}
			}
			getDownloadLinks();
			if (qtd > 0) {
				facesMessages.addFromResourceBundle(Severity.INFO,"msg.publicacao.operacao.concluida");
			} else {
				facesMessages.addFromResourceBundle(Severity.INFO,"msg.publicacao.nenhum.processo.selecionado");
			}
		} else {
			facesMessages.addFromResourceBundle(Severity.ERROR, "msg.documento.invalido");
		}
	}
	
	/**
	 * Método responsável por validar o documento.
	 * 
	 * @param modeloDocumento Documento.
	 * @return Verdadeiro se o documento não estiver vazio. Falso, caso contrário.
	 */
	private boolean isDocumentoValido(String modeloDocumento) {
		return StringUtils.isNotBlank(modeloDocumento);
	}
	
	/**
	 * Método responsável por criar o documento de certidão de publicação.
	 * 
	 * @param data Data atual.
	 * @param modeloDocumento Documento.
	 * @param tipoProcessoDocumento Tipo do documento.
	 * @throws PJeBusinessException Caso algo de errado ocorra durante a operação.
	 */
	private void criarCertidao(
			Date data, String modeloDocumento, TipoProcessoDocumento tipoProcessoDocumento, ProcessoTrf processoTrf) throws PJeBusinessException {
		
		ProcessoDocumento documento = gravarDocumento(
				data, gravarBinario(data, modeloDocumento), tipoProcessoDocumento, processoTrf);
		documento.setProcessoTrf(processoTrf);
		this.cacheCertidoes.put(processoTrf, documento);
		gravarDocumentoSessao(documento);
	}
	
	/**
	 * Método responsável por gravar o documento binário.
	 * 
	 * @param data Data atual.
	 * @param modeloDocumento Documento.
	 * @return {@link ProcessoDocumentoBin}.
	 */
	private ProcessoDocumentoBin gravarBinario(Date data, String modeloDocumento) {
		return processoDocumentoBinManager.inserirProcessoDocumentoBin(data, modeloDocumento);
	}
	
	/**
	 * Método responsável por gravar o documento.
	 * 
	 * @param data Data atual.
	 * @param processoDocumentoBin Binário.
	 * @param tipoProcessoDocumento Tipo de documento.
	 * @param processoTrf Processo.
	 * @return {@link ProcessoDocumento}.
	 */
	private ProcessoDocumento gravarDocumento(Date data, ProcessoDocumentoBin processoDocumentoBin,
			TipoProcessoDocumento tipoProcessoDocumento, ProcessoTrf processoTrf) {
		ProcessoDocumento processoDocumento = new ProcessoDocumento();
		try {
			final String descricaoDocumento = "Certidão de publicação";
			processoDocumento.setProcessoDocumento(descricaoDocumento);
			processoDocumento.setDataInclusao(data);
			processoDocumento.setProcessoDocumentoBin(processoDocumentoBin);
			processoDocumento.setTipoProcessoDocumento(tipoProcessoDocumento);
			processoDocumentoManager.inserirProcessoDocumento(processoDocumento, processoTrf, processoDocumentoBin);
		} catch (PJeBusinessException e) {
 			facesMessages.add(Severity.ERROR, "Erro ao gravar o Documento: {0}", e.getMessage());
 			logger.error(e);
		}
		return processoDocumento;
	}
	
	/**
	 * Método responsável por associar o documento à sessão de julgamento atual.
	 * 
	 * @param processoDocumento Documento.
	 * @throws PJeBusinessException Caso algo de errado ocorra durante a operação.
	 */
	private void gravarDocumentoSessao(ProcessoDocumento processoDocumento) throws PJeBusinessException {
		SessaoProcessoDocumento sessaoProcessoDocumento = new SessaoProcessoDocumento();
		sessaoProcessoDocumento.setProcessoDocumento(processoDocumento);
		sessaoProcessoDocumento.setSessao(SessaoHome.instance().getInstance());
		sessaoProcessoDocumento.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
		
		this.sessaoProcessoDocumentoManager.persistAndFlush(sessaoProcessoDocumento);
	}
	
	/**
	 * Método responsável por atualizar o documento de certidão de publicação.
	 * 
	 * @param processoDocumento Documento de certidão.
	 * @param modeloDocumento Texto.
	 */
	private void atualizarCertidaoPublicacao(ProcessoDocumento processoDocumento, String modeloDocumento) {
		processoDocumento.getProcessoDocumentoBin().setModeloDocumento(modeloDocumento);
		this.processoDocumentoManager.atualizarProcessoDocumento(processoDocumento);
	}
	
	/**
	 * Método responsável por inicializar os valores das variáveis de instância da classe.
	 */
	private void inicializarVariaveis() {
		this.modeloDocumentoCertidaoPublicacao = null;
		this.modeloDocumento = null;
		this.certidaoPublicacaoSelecionada = null;
		this.processoSelecionado = null;
		this.cacheCertidoes = new LinkedHashMap<ProcessoTrf, ProcessoDocumento>();
	}
	
	/**
	 * Método responsável por inicializar os valores das variáveis de instância associadas aos elementos de check.
	 */
	private void inicializarVariaveisProcessosSelecionados() {
		this.processosSelecionadosInclusaoCertidao = new HashMap<ProcessoTrf, Boolean>(0);
		this.processosSelecionadosAssinaturaCertidao = new HashMap<ProcessoTrf, Boolean>(0);
	}
	
	/**
	 * Método responsável selecionar a certidão de publicação de um processo para edição. 
	 * 
	 * @param processoTrf Processo.
	 */
	public void selecionarCertidaoPublicacao(ProcessoTrf processoTrf) {
		this.processoSelecionado = processoTrf;
		this.certidaoPublicacaoSelecionada = this.recuperarCertidaoPublicacao(processoTrf);
		if (this.certidaoPublicacaoSelecionada != null) {
			this.modeloDocumento = this.certidaoPublicacaoSelecionada.getProcessoDocumentoBin().getModeloDocumento();
		}
	}
	
	/**
	 * Método responsável por atualizar o modelo do documento selecionado.
	 */
	public void atualizarModeloDocumento() {
		if (this.modeloDocumentoCertidaoPublicacao != null) {
			this.modeloDocumento = ModeloDocumentoAction.instance().getConteudo(this.modeloDocumentoCertidaoPublicacao);
		} else {
			this.modeloDocumento = StringUtils.EMPTY;
		}
	}
	
	/**
	 * Método responsável por verificar se um processo que já foi enviado para publicação possui 
	 * o documento de certidão de publicação.
	 * 
	 * @param processoTrf Processo.
	 * @return Verdadeiro se o processo possui o documento de certidão de publicação. Falso, caso contrário.
	 */
	public boolean possuiCertidaoPublicacao(ProcessoTrf processoTrf) {
		return this.recuperarCertidaoPublicacao(processoTrf) != null;
	}
	
	/**
	 * Método responsável por verificar se o documento de certidão de publicacao foi assinado.
	 * 
	 * @param certidao Documento de certidão.
	 * @return Verdadeiro se o documento de certidão de publicação foi assinado. Falso, caso contrário.
	 */
	public boolean isCertidaoPublicacaoAssinada(ProcessoDocumento certidao) {
		if (certidao != null) {
			return processoDocumentoBinPessoaAssinaturaManager.getUltimaAssinaturaDocumento(certidao.getProcessoDocumentoBin()) != null;
		}
		return false;
	}
	
	/**
	 * Método responsável por recuperar a certidão de publicação de um processo.
	 * 
	 * @param processoTrf Processo.
	 * @return {@link ProcessoDocumento}.
	 */
	public ProcessoDocumento recuperarCertidaoPublicacao(ProcessoTrf processoTrf) {
		if (!this.cacheCertidoes.containsKey(processoTrf)) {
			this.cacheCertidoes.put(processoTrf, this.processoDocumentoManager.getProcessoDocumento(
				this.tipoProcessoDocumentoCertidaoPublicacao, processoTrf.getProcesso()));
		}
		return this.cacheCertidoes.get(processoTrf);
	}
	
	/**
	 * Método responsável por sinalizar se o botão de assinatura deve ser exibido.
	 * 
	 * @return Verdadeiro se algum checkbox da coluna "Assinar certidão" estiver selecionado. Falso, caso contrário.
	 */
	public boolean exibeBotaoAssinatura() {
		for (ProcessoTrf processoTrf : this.processosSelecionadosAssinaturaCertidao.keySet()) {
			if (this.processosSelecionadosAssinaturaCertidao.get(processoTrf) == true) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Método responsável por obter o link de download dos documentos para assinatura digital.
	 * 
	 * @return Link de download dos documentos para assinatura digital.
	 */
    public String getDownloadLinks(){
    	List<ProcessoDocumento> documentosAssinar = recuperarDocumentosParaAssinar();
		return documentoJudicialService.getDownloadLinks(documentosAssinar);
    }
    
    /**
     * Método responsável por concluir a operação de assinatura do(s) documento(s).
     */
    public void finalizarAssinatura() {
    	try {
    		List<ProcessoDocumento> documentosAssinar = recuperarDocumentosParaAssinar();
    		
			@SuppressWarnings("unchecked")
			List<ArquivoAssinadoHash> arquivosAssinados = (List<ArquivoAssinadoHash>)Contexts.getSessionContext().get("listaArquivoAssinadoHash");
			if(arquivosAssinados != null && !arquivosAssinados.isEmpty()){
				this.documentoJudicialService.gravarAssinaturaDeProcessoDocumento(arquivosAssinados, documentosAssinar);
				dataJuntadaAposAssinar(documentosAssinar);
			}
			Contexts.getSessionContext().remove("listaArquivoAssinadoHash");
			inicializarVariaveis();
	    	inicializarVariaveisProcessosSelecionados();
	    	facesMessages.addFromResourceBundle(Severity.INFO,"msg.publicacao.operacao.concluida");
		} catch (Exception e) {
			Contexts.getSessionContext().remove("listaArquivoAssinadoHash");
			logger.error(e);
			this.facesMessages.add(Severity.ERROR, "Erro ao assinar as certides de publicao, mensagem interna: {0}", e.getMessage());
		}
    }
    
    /**
     * Método responsável por recuperar o magistrado na sessão de julgamento.
     * 
     * @param sessaoPautaProcessoTrf Processo pautado na sessão de julgamento.
     * @return {@link PessoaMagistrado}.
     */
    public PessoaMagistrado recuperarMagistrado(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
    	if (!this.cacheMagistrados.containsKey(sessaoPautaProcessoTrf)) {
    		this.cacheMagistrados.put(sessaoPautaProcessoTrf, this.pessoaMagistradoManager.getMagistradoTitular(sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador()));
    	}
    	return this.cacheMagistrados.get(sessaoPautaProcessoTrf);
    }
    
    /**
     * Método responsável por associar o valor "true" ao checkbox do processo informado.
     * 
     * @param processoTrf Processo.
     */
    private void selecionarCheckAssinaturaCertidao(ProcessoTrf processoTrf) {
    	processosSelecionadosAssinaturaCertidao.put(processoTrf, Boolean.TRUE);
    }
    
	@Override
	public boolean isManaged() {
		return this.certidaoPublicacaoSelecionada != null;
	}
	
	@Override
	protected BaseManager<SessaoPautaProcessoTrf> getManager() {
		return this.sessaoPautaProcessoTrfManager;
	}

	@Override
	public EntityDataModel<SessaoPautaProcessoTrf> getModel() {
		return null;
	}
	
	// GETTERs and SETTERs

	public List<SessaoPautaProcessoTrf> getSessaoPautaProcessos() {
		return sessaoPautaProcessos;
	}

	public TipoProcessoDocumento getTipoProcessoDocumentoCertidaoPublicacao() {
		return tipoProcessoDocumentoCertidaoPublicacao;
	}

	public List<ModeloDocumento> getModelosDocumentoCertidaoPublicacao() {
		return modelosDocumentoCertidaoPublicacao;
	}
	
	public ModeloDocumento getModeloDocumentoCertidaoPublicacao() {
		return modeloDocumentoCertidaoPublicacao;
	}

	public void setModeloDocumentoCertidaoPublicacao(ModeloDocumento modeloDocumentoCertidaoPublicacao) {
		this.modeloDocumentoCertidaoPublicacao = modeloDocumentoCertidaoPublicacao;
	}

	public String getModeloDocumento() {
		return modeloDocumento;
	}

	public void setModeloDocumento(String modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	public Map<ProcessoTrf, Boolean> getProcessosSelecionadosInclusaoCertidao() {
		return processosSelecionadosInclusaoCertidao;
	}

	public void setProcessosSelecionadosInclusaoCertidao(Map<ProcessoTrf, Boolean> processosSelecionadosInclusaoCertidao) {
		this.processosSelecionadosInclusaoCertidao = processosSelecionadosInclusaoCertidao;
	}

	public Map<ProcessoTrf, Boolean> getProcessosSelecionadosAssinaturaCertidao() {
		return processosSelecionadosAssinaturaCertidao;
	}

	public void setProcessosSelecionadosAssinaturaCertidao(Map<ProcessoTrf, Boolean> processosSelecionadosAssinaturaCertidao) {
		this.processosSelecionadosAssinaturaCertidao = processosSelecionadosAssinaturaCertidao;
	}
	
	private List<ProcessoDocumento> recuperarDocumentosParaAssinar() {
		List<ProcessoDocumento> documentosAssinar = new ArrayList<ProcessoDocumento>(0);
		for (ProcessoTrf processoTrf : this.processosSelecionadosAssinaturaCertidao.keySet()) {
			if (this.processosSelecionadosAssinaturaCertidao.get(processoTrf)) {
				documentosAssinar.add(this.recuperarCertidaoPublicacao(processoTrf));
			}
		}
		return documentosAssinar;
	}

	private void dataJuntadaAposAssinar(List<ProcessoDocumento> documentosAssinar) {
		ProcessoTrf processoTrf = null;
		for (ProcessoDocumento pd : documentosAssinar) {
			if(processoTrf == null || processoTrf.getProcesso().getIdProcesso() != pd.getProcesso().getIdProcesso())
				processoTrf = EntityUtil.find(ProcessoTrf.class, pd.getProcesso().getIdProcesso());
			if(processoTrf.getProcessoStatus() == ProcessoStatusEnum.D) {
				pd.setDataJuntada(new Date());
				EntityUtil.getEntityManager().persist(pd);
				EntityUtil.flush();
			}
		}
		
	}

	@Override
	public String getActionName() {
		return NAME;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)
			throws Exception {
		addArquivoAssinado(arquivoAssinadoHash);
		List<ArquivoAssinadoHash> arqAssinados = (List<ArquivoAssinadoHash>)Contexts.getSessionContext().get("listaArquivoAssinadoHash");
		if(arqAssinados != null && !arqAssinados.isEmpty()){
			arquivosAssinados.addAll(arqAssinados);
		}
		Contexts.getSessionContext().set("listaArquivoAssinadoHash", getArquivosAssinados());
		
	}

	public List<ArquivoAssinadoHash> getArquivosAssinados() {
		return arquivosAssinados;
	}

	public void setArquivosAssinados(List<ArquivoAssinadoHash> arquivosAssinados) {
		this.arquivosAssinados = arquivosAssinados;
	}

	private void addArquivoAssinado(ArquivoAssinadoHash arquivoAssinadoHash) {
		this.arquivosAssinados.add(arquivoAssinadoHash);
	}

	public SessaoPautaRelacaoJulgamentoList getSessaoPautaRelacaoJulgamentoList() {
		sessaoPautaRelacaoJulgamentoList = (SessaoPautaRelacaoJulgamentoList) Component.getInstance(SessaoPautaRelacaoJulgamentoList.class);
		return sessaoPautaRelacaoJulgamentoList;
	}
	
}
