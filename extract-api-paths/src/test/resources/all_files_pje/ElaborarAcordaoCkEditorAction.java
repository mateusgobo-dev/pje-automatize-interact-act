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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jbpm.graph.def.Transition;
import org.json.JSONException;

import com.google.gson.Gson;

import br.com.infox.cliente.Util;
import br.com.infox.cliente.home.ProcessoDocumentoBinHome;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.home.SessaoPautaProcessoTrfHome;
import br.com.infox.cliente.home.SessaoProcessoDocumentoHome;
import br.com.infox.cliente.home.SessaoProcessoDocumentoVotoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProcessoJbpmUtil;
import br.com.infox.ibpm.component.tree.EventsEditorTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.list.ProcessoDocumentoElaboracaoAcordaoCkList;
import br.com.infox.pje.manager.ModeloDocumentoLocalManager;
import br.com.infox.pje.service.AssinaturaDocumentoService;
import br.com.infox.utils.Constantes.TIPO_JUSTICA;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ControleVersaoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoAcordaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TipoVotoManager;
import br.jus.cnj.pje.nucleo.service.SessaoJulgamentoService;
import br.jus.cnj.pje.nucleo.service.TipoProcessoDocumentoPapelService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.nucleo.view.CkEditorGeraDocumentoAbstractAction;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.AcordaoCompilacao;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.je.pje.business.dto.RespostaDTO;
import br.jus.je.pje.business.dto.RespostaTiposVotoDTO;
import br.jus.pje.je.enums.TipoDocumentoColegiadoEnum;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoAcordao;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;

/**
 * Componente de controle do frame WEB-INF/xhtml/flx/elaborarAcordaoCkEditor.xhtml.
 *
 */
@Name(ElaborarAcordaoCkEditorAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ElaborarAcordaoCkEditorAction extends CkEditorGeraDocumentoAbstractAction implements Serializable, ArquivoAssinadoUploader{

	private ProcessoDocumentoBinManager processoDocumentoBinManager;
	private List<ProcessoDocumentoBinPessoaAssinatura> listaAssinatura;
	private boolean assinado;
	private boolean renderEventsTree;
	private boolean alert;
	private boolean obrigatorio;
	private Integer idMinuta;
	private String limparDocumento;
	private String transicaoSaida;
	private Integer idAgrupamentos;
	@In(create = true)
	private ControleVersaoDocumentoManager controleVersaoDocumentoManager;
	protected String transicaoPadrao;
	protected static final String ARQ_PROPERTIES = "entity_messages";
	private TipoVotoManager tipoVotoManager;

	private static final long serialVersionUID = 5722152258318816865L;

	public static final String NAME = "elaborarAcordaoCkEditorAction";

    public static enum AbaElaboracaoAcordaoEnum {
		ABA_NENHUMA("", null),
		ABA_ACORDAO("Acordão", null), 
		ABA_EMENTA("Ementa", 4096), 
		ABA_RELATORIO("Relatório", 8192),
		ABA_VOTO("Voto Relator", 16384),
		ABA_NOTAS_ORAIS("Notas Orais", 32768),
    	ABA_VOTO_VENCEDOR("Voto Vencedor", null), 
		ABA_SELECAO_DOCS_ACORDAO("Selecionar documentos para acórdão", null);
		public final String nome;
		public final Integer id;
		
		static final AbaElaboracaoAcordaoEnum[] VALUES = AbaElaboracaoAcordaoEnum.values();
		
		AbaElaboracaoAcordaoEnum(String desc, Integer id) {
			this.nome = desc;
			this.id = id;
		}
		
		public Integer getId() {
			return id;
		}
		
		public String getNome() {
			return nome;
		}
		
		public static AbaElaboracaoAcordaoEnum findByName(String name) {
			if (name==null)
				return ABA_NENHUMA;
			
			for (AbaElaboracaoAcordaoEnum e: VALUES) {
				if (name.equalsIgnoreCase(e.nome) || name.equals(e.name()))
					return e;
			}
			
			return ABA_NENHUMA;
		}
		
		public String toString() {
			return getNome();
		}
	};

    private static final Map<String, String> parametrosFluxo;

    private TipoDocumentoColegiadoEnum tipoDocumento;
    private Boolean selecionarModelosAcordao;
    private Boolean habilitarMultiDocs;
    private Boolean ocultaNotasOrais;
    private Boolean ocultaAcordao;
    private Boolean vencedorDiverso;
    private String conteudoDocumentoAcordao;
	private Boolean permiteIncluirEmenta = false;
	private Boolean permiteIncluirRelatorio = false;
	private Boolean permiteIncluirVoto = false;
	private Boolean permiteIncluirNotasOrais = false;
	private ArrayList<String> msgErros = new ArrayList<String>(0);

    private boolean inicializado = false;

    private ProcessoTrf processoJudicial;

    private List<Integer> listaTiposDocSelecaoProcesso;

    private ModeloDocumento modeloAcordaoDocsSelecionados;

    private String conteudoModeloDocAcordaoDocsSelecionados;

    private Boolean habilitadoSelecaoDocumentosElaboracaoAcordao = Boolean.FALSE;

    private Boolean visualizacaoDocsSelecionadosHabilitada = Boolean.FALSE;

    private Integer idTipoDocumentoAcordao;

    private SessaoPautaProcessoTrf sessaoJulgamento;

    private SessaoProcessoDocumentoVoto decisao;


    private Boolean julgado = false;

	private ModeloDocumento modelo;

	private List<ModeloDocumento> modelos;

    private SessaoPautaProcessoTrfHome sessaoPautaProcessoTrfHome;

    private SessaoProcessoDocumentoHome sessaoProcessoDocumentoHome;

    private ProcessoDocumentoHome processoDocumentoHome;

    private ProcessoDocumentoBinHome processoDocumentoBinHome;

    @In(value="sessaoJulgamentoServiceCNJ")
    private transient SessaoJulgamentoService sessaoJulgamentoService;

    private transient SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager;

    private transient SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager;

    private transient ProcessoDocumentoManager processoDocumentoManager;

    private transient ModeloDocumentoManager modeloDocumentoManager;

    private transient ModeloDocumentoLocalManager modeloDocumentoLocalManager;

    private transient TipoProcessoDocumentoManager tipoProcessoDocumentoManager;

    private transient ParametroService parametroService;

    private transient TramitacaoProcessualService tramitacaoProcessualService;

    private DocumentoJudicialService documentoJudicialService;

    private Identity identity;

    private ParametroUtil parametroUtil;

    private SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager;

	private AssinaturaDocumentoService assinaturaDocumentoService;

    private ProcessoDocumentoAcordaoManager processoDocumentoAcordaoManager;

    private TipoProcessoDocumentoPapelService tipoProcessoDocumentoPapelService;

    @Logger
    private Log logger;

    private Map<SessaoProcessoDocumento, List<ProcessoDocumentoBinPessoaAssinatura>> mapaAssinaturas = new HashMap<SessaoProcessoDocumento, List<ProcessoDocumentoBinPessoaAssinatura>>();
    private String papeisAssinatura = Papeis.MAGISTRADO;
    private boolean podeAssinar = false;
    private boolean acordaoValidado = false;
    private Boolean justicaEleitoral;
    private Boolean relator;
    private Boolean somenteLeitura;
    private Boolean editarVoto;
    private Boolean editarVotoVencedor;
    private Boolean editarEmenta;
    private Boolean editarRelatorio;
    private Boolean editarNotasOrais;
    private List<ArquivoAssinadoHash> arquivosAssinados;
	private AcordaoCompilacao acordaoCompilacao;
	private ProcessoDocumento processoDocumentoVotoSelecionado;
    private ProcessoDocumentoElaboracaoAcordaoCkList processoDocumentoElaboracaoAcordaoCkList;
    private OrgaoJulgador orgaoJulgadorAtual;
    private OrgaoJulgador orgaoJulgadorRelator;
    private OrgaoJulgador orgaoJulgadorVencedor;
    private boolean uploadArquivoAssinadoRealizado;
    private Boolean sessaoExistente;
    private TipoVoto tipoVoto;

	static{
    	parametrosFluxo = new HashMap<String, String>();
    	parametrosFluxo.put("papeisAssinatura", Variaveis.ACORDAO_PAPEIS_ASSINATURA);
    	parametrosFluxo.put("permiteIncluirEmenta", Variaveis.ACORDAO_PERMITE_INCLUIR_EMENTA);
    	parametrosFluxo.put("permiteIncluirRelatorio", Variaveis.ACORDAO_PERMITE_INCLUIR_RELATORIO);
    	parametrosFluxo.put("permiteIncluirVoto", Variaveis.ACORDAO_PERMITE_INCLUIR_VOTO);
    	parametrosFluxo.put("permiteIncluirNotasOrais", Variaveis.ACORDAO_PERMITE_INCLUIR_NOTAS_ORAIS);
    }

	public void load() throws Exception {
		TaskInstanceUtil taskInstanceUtil = ComponentUtil.getTaskInstanceUtil();
		setProtocolarDocumentoBean(new ProtocolarDocumentoBean(taskInstanceUtil.getProcesso(
				taskInstanceUtil.getProcessInstance().getId()).getIdProcessoTrf(),
				ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL | ProtocolarDocumentoBean.RECUPERA_DOCUMENTO_FLUXO,NAME));


		transicaoSaida = (String)taskInstanceUtil.getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);

		Boolean obrigatorio = (Boolean) getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.VARIAVEL_OBRIGATORIO);
 		setObrigatorio(obrigatorio != null ? obrigatorio : false);

		limparDocumento = (String) getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.VARIAVEL_LIMPAR_DOCUMENTO_FRAME);
		if (limparDocumento == null)
		{
			limparDocumento = "";
		}

		carregarDocumentoPrincipal();

		//isDocumentoAssinado(getProtocolarDocumentoBean().getDocumentoPrincipal());

		listaAssinatura = getProcessoDocumentoBinManager().obtemAssinaturas(getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin());
	}

    @Create
    public void init(){
		carregaTransicaoPadrao();
		processoJudicial = getTramitacaoProcessualService().recuperaProcesso();
		carregarParametros();
    	validaParametrosAcordao();
    	validaParametrosNotasOrais();
    	inicializarPapeis();
    	inicializarDocumentos();
		iniciarAbas();
		inicializaInformacoesProcessoSelecaoAcordao();
		verificaDocumentosAcordao(false);
		obterOrgaosJulgadores();
    }

    /**
     * Método chamado para inicializar as abas do frame ao abrir a página
     */
    public void iniciarAbas(){
        if (!getOcultaAcordao()) {
            setAba(AbaElaboracaoAcordaoEnum.ABA_ACORDAO);
        } else if (!getOcultaNotasOrais()) {
            setAba(AbaElaboracaoAcordaoEnum.ABA_NOTAS_ORAIS);
        } else {
            setAba(AbaElaboracaoAcordaoEnum.ABA_EMENTA);
        }
    }

    /**
     * Método responsável por realizar a verificação se o usuário logado tem permissão para
     * assinatura dos documentos do acórdão.
     * @return true se tem permissão
     */
     public void inicializarPapeis(){
    	 String[] possiveisSignatarios = papeisAssinatura.split(",");
    	 for(String papel: possiveisSignatarios){
    		 if(getIdentity().hasRole(papel)){
    			 podeAssinar = true;
    			 break;
    		 }
    	 }
     }

	public void gravarDocumento(){
    	try{
	    	if(getPermiteAlterarAcordao() || getAba() != AbaElaboracaoAcordaoEnum.ABA_ACORDAO) {

	    		ProcessoDocumento processoDocumento = getProcessoDocumentoDeAcordoComAbaSelecionada();
	
	    		if((getAba() == AbaElaboracaoAcordaoEnum.ABA_VOTO) || (getAba() == AbaElaboracaoAcordaoEnum.ABA_VOTO_VENCEDOR)){
	
	    			if(!Util.isDocumentoPreenchido(processoDocumento)) {
	    				FacesMessages.instance().addFromResourceBundle(Severity.WARN, "erro.voto.gravarDocumentoVoto");
						return;
					}
	    			
	    			SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto = null;
	    			
	    			if(getAba() == AbaElaboracaoAcordaoEnum.ABA_VOTO){
	    				sessaoProcessoDocumentoVoto = getVotoRelator();
	    			}
	
	    			if(getAba() == AbaElaboracaoAcordaoEnum.ABA_VOTO_VENCEDOR){
	    				sessaoProcessoDocumentoVoto = acordaoCompilacao.getVotoVencedor();
	    				if(decisao.getOjAcompanhado() == null){
	    					sessaoProcessoDocumentoVoto.setOjAcompanhado(sessaoJulgamento.getOrgaoJulgadorVencedor());
						}
	    			}
	    			
	    			if (sessaoProcessoDocumentoVoto!=null){
	    				ComponentUtil.getSessaoProcessoDocumentoVotoManager().persistAndFlush(sessaoProcessoDocumentoVoto);
	    			}
	
	    		}
	    		if (getTaskInstance() != null) {
	    			getProtocolarDocumentoBean().getDocumentoPrincipal().setIdJbpmTask(getTaskInstance().getId());
	    			processoDocumento.setExclusivoAtividadeEspecifica(Boolean.TRUE);
	    		}
	    		getProtocolarDocumentoBean().setDocumentoPrincipal(processoDocumento);
		    	salvarVersao(processoDocumento);
		    	getProtocolarDocumentoBean().gravarRascunho();
		    	
		    	ComponentUtil.getDocumentoJudicialService().flush();
				
		    	FacesMessages.instance().clear();
		    	FacesMessages.instance().add(Severity.INFO, "Gravação realizada.");

	    	}
			
    	} catch (Exception e){
    		logger.error("Houve um erro ao tentar gravar o documento: {0}.", e);
    		FacesMessages.instance().add(Severity.ERROR, "Houve um erro ao tentar gravar o documento: {0}.", e.getLocalizedMessage());
    	}
    }
	
	/**
	 * Realiza a gravacao do tipo de voto.
	 */
	public void gravarTipoVoto() {
		if (getTipoVoto()!=null){
			switch (getAba()) {
				case ABA_VOTO:
					if(podeAlterarAbaVotoRelator()) {
						getVotoRelator().setTipoVoto(getTipoVoto());
					}
					break;
		
				case ABA_VOTO_VENCEDOR:
					if(podeAlterarAbaVotoVencedor()) {
						acordaoCompilacao.getVotoVencedor().setTipoVoto(getTipoVoto());
					}
					break;
				default:
					break;
			}
			setIdTipoVotoSelecionado(getTipoVoto().getIdTipoVoto());
			gravarDocumento();
		}
	}

	@Override
	public void selecionarTipoVoto(String idTipoVoto) {
		int id = Integer.parseInt(idTipoVoto);

		try {
			switch (getAba()) {
				case ABA_VOTO:
					if(podeAlterarAbaVotoRelator()) {
						getVotoRelator().setTipoVoto(getTipoVotoManager().findById(id));

						setIdTipoVotoSelecionado(id);
					}
					break;

				case ABA_VOTO_VENCEDOR:
					if(podeAlterarAbaVotoVencedor()) {
						decisao.setTipoVoto(getTipoVotoManager().findById(id));

						setIdTipoVotoSelecionado(id);
					}
					break;
				default:
					break;
			}

		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao selecionar o tipo de voto: {0}.", e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
		}
	}

	private void salvarVersao(ProcessoDocumento pd) {
		try{
			selecionarTipoProcessoDocumento(pd.getTipoProcessoDocumento().getTipoProcessoDocumento());
			
			
			// Verifica quem venceu != relator

			getProtocolarDocumentoBean().getDocumentoPrincipal().setProcessoDocumento(getTipoProcessoDocumento().getTipoProcessoDocumento());
			getProtocolarDocumentoBean().getDocumentoPrincipal().setTipoProcessoDocumento(getTipoProcessoDocumento());

			if(getProtocolarDocumentoBean().getDocumentoPrincipal().getIdProcessoDocumento() == 0){
				getProtocolarDocumentoBean().loadArquivosAnexadosDocumentoPrincipal();
			}
		}catch (Exception e){
			FacesMessages.instance().add(Severity.ERROR, "Não foi possível gravar o documento [{0}:{1}]", e.getClass().getCanonicalName(), e.getLocalizedMessage());
		}
	}

	/**
	 * Obtém o voto (ProcessoDocumento) do relator do processo.
	 * @return ProcessoDocumento voto do relator do processo.
	 */
	private ProcessoDocumento obterDocumentoVotoRelatorProcesso() {
		ProcessoDocumento pd = null;
		if(isHabilitarMultiDocs()) {
			pd = getProcessoDocumentoVotoSelecionado();
		} else {
			SessaoProcessoDocumentoVoto votoRelator = acordaoCompilacao.getVotoRelatorDoProcesso();
			if (votoRelator != null) {
				pd = votoRelator.getProcessoDocumento();
			}
		}
		return pd;
	}

	/**
	 * Verifica todos os documentos do acórdão de acordo com as regras de validação
	 * configuradas nas variáveis de tarefa.
	 */
	public void verificaDocumentosAcordao(boolean exibeMensagens){
		msgErros = getSessaoPautaProcessoTrfManager().verificaVariaveisDocumentoAcordao(acordaoCompilacao);
		getTramitacaoProcessualService().apagaVariavel(Variaveis.VARIAVEL_FLUXO_COLEGIADO_MINUTA_ACORDAO);
		if(msgErros != null && !msgErros.isEmpty()){
			this.acordaoValidado = false;
			getTramitacaoProcessualService().apagaVariavel(Variaveis.MINUTA_EM_ELABORACAO);
		}else{
			this.acordaoValidado = true;
			if(ComponentUtil.getSessaoProcessoDocumentoManager().recuperaPorProcessoDocumento(acordaoCompilacao.getAcordao()) != null 
			   && (getTramitacaoProcessualService().recuperaVariavel(Variaveis.MINUTA_EM_ELABORACAO) == null 
			   		|| !getTramitacaoProcessualService().recuperaVariavel(Variaveis.MINUTA_EM_ELABORACAO).equals(acordaoCompilacao.getAcordao().getIdProcessoDocumento()))) {
				getTramitacaoProcessualService().gravaVariavel(Variaveis.MINUTA_EM_ELABORACAO, acordaoCompilacao.getAcordao().getIdProcessoDocumento());
			}
		}
		
		if(!acordaoValidado && msgErros != null && exibeMensagens){
			for(String msgErro : msgErros){
				FacesMessages.instance().add(Severity.WARN, FacesUtil.getMessage("conclusaoAcordao.acordaoNaoConcluido"), msgErro);
			}
		}
	}

	/**
	 * Verifica se o editor na aba 'ácordão' deve ficar em modo de somente leitura.
	 *
	 * @return boolean
	 */
	public boolean isEditorAcordaoSomenteLeitura(){
		return (getSomenteLeitura() || isAssinado() || !getPermiteAlterarAcordao());
	}

    public boolean getPermiteAlterarAcordao() {
		return getSessaoPautaProcessoTrfManager().getPermiteAlterarAcordao();
	}

	/**
     * Operação utilizada para gravar a relação de documentos que irão participar da composição do acórdão.
     *
     */
	public void gravarDocumentosElaboracaoAcordao(){

    	if(!ocorreuSelecaoDocumentosGravacaoAcordao()){
    		FacesMessages.instance().add(Severity.ERROR, "É necessário selecionar documentos para a composição do acórdão.");
			return;
    	}

    	try {
			getProcessoDocumentoAcordaoManager().gravarDocumentosComposicaoAcordao(getProcessoDocumentoElaboracaoAcordaoCkList().getListaDocsSelecionados());

			// Apaga os documentos que foram salvos sem conteúdo
	    	getProtocolarDocumentoBean().removeRascunhosEmBranco(ProcessoJbpmUtil.getProcessoTrf());
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, "Não foi possível gravar documentos selecionados (" + e.getLocalizedMessage() + ").");
			return;
		}

    	FacesMessages.instance().add(Severity.INFO, "Documentos para elaboração do acórdão foram gravados com sucesso.");

    	regerarConteudo();
    }

    /**
     * Valida se ocorreu seleção de documentos para gravar a elaboração do acórdão.
     *
     */
    private boolean ocorreuSelecaoDocumentosGravacaoAcordao(){
    	return getProcessoDocumentoElaboracaoAcordaoCkList().getListaDocsSelecionados() != null &&
    			!getProcessoDocumentoElaboracaoAcordaoCkList().getListaDocsSelecionados().isEmpty();
    }

    /**
     * Operação que recupera lista de documentos para seleção de um acórdão e que encontram-se em aberto.
     * @return
     */
    @SuppressWarnings("boxing")
	public List<ProcessoDocumentoAcordao> getDocumentosParaAcordaoEmAberto(){
    	if(getProcessoJudicial() != null){
    		return getProcessoDocumentoAcordaoManager().recuperarDocumentosParaAcordaoEmAberto(getProcessoJudicial().getIdProcessoTrf());
    	}
    	return Collections.emptyList();
    }

    /**
     * Operação que marca a seleção da aba de documentos da elaboração do acórdão e
     * que carrega as informações iniciais da página.
     */
    @SuppressWarnings({ "boxing"})
	public void acionarSelecaoDocumentosElaboracaoAcordao(){
    	setAba(AbaElaboracaoAcordaoEnum.ABA_SELECAO_DOCS_ACORDAO);
    	if(!getProcessoDocumentoElaboracaoAcordaoCkList().getInicializado()){
    		extrairListaInicialSelecaoDocumentosElaboracaoAcordao();
    		getProcessoDocumentoElaboracaoAcordaoCkList().setInicializado(Boolean.TRUE);
    	}
    }

    /**
     * Operação que recupera a seleção inicial de documentos que é utilziada pelo componente visual
     * que trata a seleção de documentos do acórdão.
     *
     */
    private void extrairListaInicialSelecaoDocumentosElaboracaoAcordao(){
    	List<ProcessoDocumentoAcordao> documentosAcordao = getDocumentosParaAcordaoEmAberto();
		for (ProcessoDocumentoAcordao processoDocumentoAcordao : documentosAcordao) {
			getProcessoDocumentoElaboracaoAcordaoCkList().getListaDocsSelecionados().add(processoDocumentoAcordao.getProcessoDocumento());
		}
    }

    /**
     * Operação que, a partir de uma lista de documentos em aberto para composição do acórdão, monta o conteúdo
     * que fará parte de um acórdão em um modelo de documento.
     *
     *
     * @return conteúdo do documento.
     */
    public String getConteudoDocumentosEmAbertoParaAcordao(){
    	StringBuilder resposta = new StringBuilder();
    	List<ProcessoDocumento> documentosEmAbertoTela = getProcessoDocumentoElaboracaoAcordaoCkList().getListaDocsSelecionados();
    	if(documentosEmAbertoTela != null && !documentosEmAbertoTela.isEmpty()){
    		for (ProcessoDocumento processoDocumento : documentosEmAbertoTela) {
    			resposta.append(processoDocumento.getProcessoDocumentoBin().getModeloDocumento() + "<br/><br/>");
			}
    	} else {
    		List<ProcessoDocumentoAcordao> documentosEmAberto = getDocumentosParaAcordaoEmAberto();
    		for (ProcessoDocumentoAcordao processoDocumentoAcordao : documentosEmAberto) {
    			resposta.append(processoDocumentoAcordao.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento() + "<br/><br/>");
    		}
    	}
    	return resposta.toString();
    }

    /**
     * Operação que inicializa quais tipos de documentos que devem ser apresentados para seleção no acórdão e
     * qual o modelo de documento que precisa ser utilizado na visualização dos documentos selecionados.
     *
     */
	private void inicializaInformacoesProcessoSelecaoAcordao(){
		inicializarHabilitarSelecaoDocumentos();
    	inicializaListaModeloDocumentosElaboracaoAcordao();
    	inicializaDataUlimoAcordao();
    }

	/**
	 * Operação que recupera parâmetro de fluxo para poder
	 */
	private void inicializarHabilitarSelecaoDocumentos(){
		Object parametro = getTramitacaoProcessualService().recuperaVariavelTarefa(
				Variaveis.PARAMETRO_FLUXO_HABILITA_SELECAO_DOCS_ACORDAO);
		if(parametro == null || parametro instanceof String){
			this.habilitadoSelecaoDocumentosElaboracaoAcordao = Boolean.valueOf((String)parametro);
		} else if(parametro instanceof Boolean){
			this.habilitadoSelecaoDocumentosElaboracaoAcordao = (Boolean) parametro;
		}
	}

    /**
	 * Inicializa os tipos de documentos que precisam participar da seleção do acórdão.
	 *
	 */
    private void inicializaListaModeloDocumentosElaboracaoAcordao(){
    	setListaTiposDocSelecaoProcesso(extrairListaTiposDocumentosVariavelFluxo());
    }

    /**
     * Operação que extrai os ids dos tipos de docuemntos que foram configurados no fluxo e que participam
     * da seleção dos documentos que participam da composição do acórdão.
     *
     */
    private List<Integer> extrairListaTiposDocumentosVariavelFluxo(){
    	List<Integer> retorno = null;
    	String variavel = (String)getTramitacaoProcessualService().recuperaVariavelTarefa("tiposDisponiveisParaSelecaoIds");
    	if(variavel != null){
    		retorno = extrairIdsVariavelTiposDocElaboracaoAcordao(variavel);
    	} else {
    		if( this.processoJudicial != null ) {
    			List<ProcessoDocumento> procDocs = getProcessoDocumentoManager().recuperaDocumentosJuntados(this.processoJudicial, this.processoJudicial.getDataDistribuicao());
	    		retorno = new ArrayList<Integer>();
	    		for (ProcessoDocumento processoDocumento : procDocs) {
	    			retorno.add(processoDocumento.getTipoProcessoDocumento().getIdTipoProcessoDocumento());
	    		}
    		}
    	}
    	return retorno;
    }

    /**
     * Operação que extrai os ids a partir da string que representa os tipos de docuemntos que participam
     * da seleção dos documentos que participam da composição do acórdão.
     *
     */
    private List<Integer> extrairIdsVariavelTiposDocElaboracaoAcordao(String variavel){
    	List<Integer> retorno = new ArrayList<Integer>();
    	StringTokenizer idsTiposDocumentosTokens = new StringTokenizer(variavel, ",");
    	while (idsTiposDocumentosTokens.hasMoreTokens()) {
    		retorno.add(Integer.valueOf(idsTiposDocumentosTokens.nextToken()));
    	}
    	return retorno;
    }

    /**
	 * Inicializa informação da data do ultimo documento acórdão que consta na lista de documentos do processo.
	 * A informação é utilizada para listar os documentos que foram selecioados na composição do acórdão.
	 *
	 */
    @SuppressWarnings("boxing")
	private void inicializaDataUlimoAcordao(){
    	String strIdTipoDocAcordao = getParametroService().valueOf(Parametros.TIPODOCUMENTOACORDAO);
    	if(strIdTipoDocAcordao != null && !strIdTipoDocAcordao.trim().isEmpty()){
    		TipoProcessoDocumento tipoDocAcordao = new TipoProcessoDocumento();
    		tipoDocAcordao.setIdTipoProcessoDocumento(Integer.valueOf(strIdTipoDocAcordao));
    		ProcessoDocumento acordao = getProcessoDocumentoManager().getUltimoProcessoDocumento(tipoDocAcordao, getProcessoJudicial().getProcesso());
    		if(acordao != null){
    			getProcessoDocumentoElaboracaoAcordaoCkList().setDataUltimoAcordao(acordao.getDataJuntada());
    		}
    	}
    }

    /**
     * Operação que adiciona o documento selecioado à lista e atualiza documento processado.
     *
     * @param processoDocumento
     */
    public void selecionarDocElaboracaoAcordao(ProcessoDocumento processoDocumento){
   		getProcessoDocumentoElaboracaoAcordaoCkList().selecionarDocumento(processoDocumento);
    	setVisualizacaoDocsSelecionadosHabilitada(Boolean.FALSE);
    }

    /**
     * Operação que adiciona o documento selecioado à lista e atualiza documento processado.
     *
     * @param processoDocumento
     */
    public void removerDocElaboracaoAcordao(ProcessoDocumento processoDocumento){
    	getProcessoDocumentoElaboracaoAcordaoCkList().removerDocumento(processoDocumento);
    	setVisualizacaoDocsSelecionadosHabilitada(Boolean.FALSE);
    }

    /**
     * Operação que adiciona o documento selecioado à lista e atualiza documento processado.
     *
     * @param processoDocumento
     */
    public void removerTodosDocElaboracaoAcordao(){
    	getProcessoDocumentoElaboracaoAcordaoCkList().removerTodosDocumento();
    	setVisualizacaoDocsSelecionadosHabilitada(Boolean.FALSE);
    }

    /**
     * Operação acionada para visualização dos documentos selecionados na elaboração do acórdão.
     */
    public void visualizarDocumentosSelecionados(){
    	if (getVisualizacaoDocsSelecionadosHabilitada()){
    		setVisualizacaoDocsSelecionadosHabilitada(Boolean.FALSE);
    	} else {
    		setVisualizacaoDocsSelecionadosHabilitada(Boolean.TRUE);
    		processarModeloDocumento();
    	}
    }

	/**
	 * Operação que processa o conteúdo do modelo do documento
	 *
	 */
	public void processarModeloDocumento() {
		setConteudoModeloDocAcordaoDocsSelecionados(
				getModeloDocumentoManager().obtemConteudo(getModeloAcordaoDocsSelecionados()));
	}

	/**
	 * Parametro que habilita se é possível selecionar documentos para compor o acórdão no processo.
	 *
	 * @return
	 */
	public Boolean isHabilitadoSelecaoDocumentosElaboracaoAcordao(){
		return this.habilitadoSelecaoDocumentosElaboracaoAcordao;
	}

	/**
	 * Método de conclusão do julgamento do acórdão, verifica se há uma seleção de documentos para a
	 * geração do acórdão e marca essa lista de documentos com o acórdão gerado e sua data de juntada
	 * e finalmente tramita a tarefa.
	 *
	 */
    @SuppressWarnings("boxing")
	public void concluirJulgamento(){

    	try {

    		getSessaoPautaProcessoTrfManager().compilarAcordao(acordaoCompilacao);
    		fecharDocumentosSelecionadosElaboracaoAcordaoEmAberto();
    		getProcessoDocumentoAcordaoManager().fecharDocumentosParaAcordaoEmAberto(
    				getProcessoJudicial().getIdProcessoTrf(), getAcordaoCompilacao().getAcordao());

    		ProcessoHome.instance().setIdProcessoDocumento(getAcordao().getIdProcessoDocumento());
			TaskInstanceHome tih = TaskInstanceHome.instance();
			tih.verificarEhRecuperarProximaTarefa();

    		FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("conclusaoAcordao.acordaoConcluido"));
    	}
    	catch (Exception e) {
    		FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("conclusaoAcordao.acordaoNaoConcluido"), e.getLocalizedMessage());
    	}
    }

    public void concluirJulgamentoPjeOffice(){
    	try {
			if(arquivosAssinados != null && !arquivosAssinados.isEmpty()){
				this.getDocumentoJudicialService().gravarAssinaturaDeProcessoDocumento(arquivosAssinados, acordaoCompilacao.getProcessoDocumentosParaAssinatura());
			}
			concluirJulgamento();
	    } catch (Exception e) {
	    	FacesMessages.instance().add(Severity.ERROR, "Ocorreu um erro ao tentar gravar as assinaturas dos documentos");
	    }
    }

    /**
	 * Verifica se há uma seleção de documentos para a geração do acórdão e marca essa lista de
	 * documentos com o acórdão gerado e sua data de juntada.
	 * [PJEII-21041]
	 */
    @SuppressWarnings("boxing")
	private void fecharDocumentosSelecionadosElaboracaoAcordaoEmAberto() throws PJeBusinessException{
		getProcessoDocumentoAcordaoManager().fecharDocumentosParaAcordaoEmAberto(
				getProcessoJudicial().getIdProcessoTrf(), getAcordaoCompilacao().getAcordao());
    }

    /**
     * Metodo que altera o modelo de documento do voto vencedor
     */
	public void modificarModeloVotoVencedor(){
		try {
			if (getVotoVencedor()!=null
 					&& getVotoVencedor().getProcessoDocumentoBin()!=null
 					&& getModelo()!=null){
 				ModeloDocumento modeloDocumento = getModeloDocumentoManager().findById(getModelo().getIdModeloDocumento());
 				getVotoVencedor().getProcessoDocumentoBin().setModeloDocumento(getModeloDocumentoManager().obtemConteudo(modeloDocumento));
			}
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar atualizar o modelo: {0}.", e.getLocalizedMessage());
		}
	}

	/**
	 * Metodo que retorna lista de modelos pelo tipo processo documento voto
	 * @return List<ModeloDocumento>
	 */
	public List<ModeloDocumento> getModelos() {
		if(modelos == null || modelos.isEmpty()){
			modelos = getModeloDocumentoManager().recuperaModelosPorTipoProcessoDocumento(getParametroUtil().getTipoProcessoDocumentoVoto());
		}
		return modelos;
	}

    /**
     * @return ProcessoTrf corrente
     */
	public ProcessoTrf getProcessoJudicial() {
    	if(processoJudicial == null){
    		processoJudicial = getTramitacaoProcessualService().recuperaProcesso();
    	}
        return processoJudicial;
    }

    /**
     * Método executado para mudar para a aba de Relatório e carregar o documento
     */
    public void carregarRelatorio() {
        this.tipoDocumento = TipoDocumentoColegiadoEnum.REL;
        getSessaoProcessoDocumentoHome().carregarDocumentosAcordao('R');
    }

    /**
     * Método executado para mudar para a aba de Voto e carregar o documento
     */
    public void carregarVoto() {
        this.tipoDocumento = TipoDocumentoColegiadoEnum.VOT;
        getSessaoProcessoDocumentoHome().carregarDocumentosAcordao('V');
    }

    /**
     * Método executado para mudar para a aba de VotoVencedor e carregar o documento
     */
    public void carregarVotoVencedor() {
        this.tipoDocumento = TipoDocumentoColegiadoEnum.VOT_VENC;
        getSessaoProcessoDocumentoHome().carregarDocumentosAcordao('v');
        setIdTipoVotoSelecionado(SessaoProcessoDocumentoVotoHome.instance().getVotoAntigo().getIdTipoVoto());
    }

    /**
     * Método executado para mudar para a aba de Ementa e carregar o documento
     */
    public void carregarEmenta() {
        this.tipoDocumento = TipoDocumentoColegiadoEnum.EME;
        getSessaoProcessoDocumentoHome().carregarDocumentoEmenta('E');
    }

    /**
     * Método executado para mudar para a aba de NotasOrais e carregar o documento
     */
    public void carregarNotasOrais() {
        this.tipoDocumento = TipoDocumentoColegiadoEnum.NOT_ORA;
        getSessaoProcessoDocumentoHome().carregarDocumentoEmenta('N');
    }

    /**
     * Verifica a existência de parâmetros necessários para as Notas Orais
     */
    private void validaParametrosNotasOrais() {
    	if(!getOcultaNotasOrais()){
	        // verifica a existência do parametro com o codigo do Tipo Documento de Notas Orais
	        String idTipoDocumento = getParametroService().valueOf(Parametros.ID_TIPO_DOCUMENTO_NOTAS_ORAIS);
	        if (idTipoDocumento == null) {
	            throw new AplicationException("Não foi possível encontrar o parâmetro: " + Parametros.ID_TIPO_DOCUMENTO_NOTAS_ORAIS + "! O mesmo não está configurado.");
	        }
    	}
    }

    /**
     *
     * @return Flag indicando se a aba selecionada é a de NotasOrais
     */
    public boolean isAbaNotasOrais() {
        return tipoDocumento != null && tipoDocumento.equals(TipoDocumentoColegiadoEnum.NOT_ORA);
    }

    /**
     *
	 * @return Lista de ModeloDocumento para o TipoProcessoDocumento "Acórdão"
	 * @throws PJeBusinessException Exceção de negócio
	 */
	public List<ModeloDocumento> getModelosDocumentoAcordao()	throws PJeBusinessException {
		String idParametro = getParametroService().valueOf(Parametros.ID_TIPO_PROCESSO_DOCUMENTO_ACORDAO);
		TipoProcessoDocumento tpd = getTipoProcessoDocumentoManager().findByCodigoTipoProcessoDocumento(idParametro);
		return getModeloDocumentoLocalManager().getModeloDocumentoPorTipo(tpd);
	}

    /**
     *
     * @return Lista de ModeloDocumento para o TipoProcessoDocumento "Notas Orais"
     * @throws PJeBusinessException Exceção de negócio
     */
    public List<ModeloDocumento> getModelosDocumentoNotasOrais() throws PJeBusinessException {
        // obtem o parametro com o codigo do TipoProcessoDocumento Notas Orais
        String idParametro = getParametroService().valueOf(Parametros.ID_TIPO_DOCUMENTO_NOTAS_ORAIS);

        TipoProcessoDocumento tpd = getTipoProcessoDocumentoManager().findByCodigoTipoProcessoDocumento(idParametro);
        return getModeloDocumentoLocalManager().getModeloDocumentoPorTipo(tpd);
    }

    /**
     * Método executado para mudar para a aba de Acórdao e carregar o documento
     */
    public void carregarAcordao() throws PJeBusinessException {
        this.tipoDocumento = TipoDocumentoColegiadoEnum.ACO;
        getSessaoProcessoDocumentoHome().carregarDocumentosAcordao('A');
    }

    /**
     * Verifica a existência de parâmetros necessários para geração do acórdão
     */
    private void validaParametrosAcordao() {
        // verifica a existência do parametro com o codigo do Modelo de Documento do Acórdão
        String idModelo = getParametroService().valueOf(Parametros.ID_MODELO_DOCUMENTO_ACORDAO);
        if (idModelo == null) {
            throw new AplicationException("Não foi possível encontrar o parâmetro: " + Parametros.ID_MODELO_DOCUMENTO_ACORDAO + "! O mesmo não está configurado.");
        }

        // verifica a existência do parametro com o codigo do Modelo de Documento do Inteiro Teor
        idModelo = getParametroService().valueOf(Parametros.ID_MODELO_DOCUMENTO_INTEIRO_TEOR);
        if (idModelo == null) {
            throw new AplicationException("Não foi possível encontrar o parâmetro: " + Parametros.ID_MODELO_DOCUMENTO_INTEIRO_TEOR + "! O mesmo não está configurado.");
        }
    }

    /**
     * Gera o documento do Acórdão para ser exibido
     * @throws PJeBusinessException Exceção de negócio
     */
    public String getConteudoDocumentoAcordao() throws PJeBusinessException {
    	if(conteudoDocumentoAcordao == null || conteudoDocumentoAcordao.isEmpty()){
            // obtem o parametro com o codigo do Modelo de Documento do Acórdão
            String idModelo = getParametroService().valueOf(Parametros.ID_MODELO_DOCUMENTO_ACORDAO);
            ModeloDocumento modeloAcordao = getModeloDocumentoManager().findById(new Integer(idModelo));
            conteudoDocumentoAcordao = getModeloDocumentoManager().obtemConteudo(modeloAcordao);
    	}
        return conteudoDocumentoAcordao;
    }

    /**
     * Grava o documento composto pelos documentos selecionados para compor o Acórdão
     * @param documentosSelecionados
     * @throws PJeBusinessException Exceção de negócio
     */
    public void salvarDocumentosSelecionadosElaboracaoAcordao(ProcessoTrf processo, List<ProcessoDocumento> documentosSelecionados) throws PJeBusinessException {

    	FacesMessages.instance().add(Severity.INFO, "Documentos selecionados para compor o acórdão gravados com sucesso.");
    }

    /**
     * Grava o documento composto pelos documentos selecionados para compor o Acórdão
     * @param documentosSelecionados
     * @throws PJeBusinessException Exceção de negócio
     */
    public Boolean hasDocumentosSelecionadosElaboracaoAcordao() throws PJeBusinessException {
    	return false;
    }

    /**
     *
     * @return Flag indicando se o usuário logado tem permissão de assinar os documentos
     */
    public boolean liberaCertificacao() {
        return getProcessoDocumentoManager().liberaCertificacao(getProcessoDocumentoHome().getInstance()) != null;
    }

    /**
     *
     * @return Conteúdo da Proclamação da Decisão
     */
    public String getProclamacaoDecisao() {
        return getSessaoProcessoDocumentoHome().getProclamacaoDecisao();
    }

    /**
     * @return the sessaoPautaProcessoTrfHome
     */
    public SessaoPautaProcessoTrfHome getSessaoPautaProcessoTrfHome() {
    	if (sessaoPautaProcessoTrfHome == null) {
    		sessaoPautaProcessoTrfHome = ComponentUtil.getSessaoPautaProcessoTrfHome();
    	}
        return sessaoPautaProcessoTrfHome;
    }

    /**
     * @param sessaoPautaProcessoTrfHome the sessaoPautaProcessoTrfHome to set
     */
    public void setSessaoPautaProcessoTrfHome(SessaoPautaProcessoTrfHome sessaoPautaProcessoTrfHome) {
        this.sessaoPautaProcessoTrfHome = sessaoPautaProcessoTrfHome;
    }

    /**
     * @return the sessaoProcessoDocumentoHome
     */
    public SessaoProcessoDocumentoHome getSessaoProcessoDocumentoHome() {
    	if (sessaoProcessoDocumentoHome == null) {
    		sessaoProcessoDocumentoHome = ComponentUtil.getSessaoProcessoDocumentoHome();
    	}
        return sessaoProcessoDocumentoHome;
    }

    /**
     * @param sessaoProcessoDocumentoHome the sessaoProcessoDocumentoHome to set
     */
    public void setSessaoProcessoDocumentoHome(SessaoProcessoDocumentoHome sessaoProcessoDocumentoHome) {
        this.sessaoProcessoDocumentoHome = sessaoProcessoDocumentoHome;
    }

    /**
     * @return the processoDocumentoBinHome
     */
    public ProcessoDocumentoBinHome getProcessoDocumentoBinHome() {
    	if (processoDocumentoBinHome == null) {
    		processoDocumentoBinHome = ComponentUtil.getProcessoDocumentoBinHome();
    	}
        return processoDocumentoBinHome;
    }

    /**
     * @param processoDocumentoBinHome the processoDocumentoBinHome to set
     */
    public void setProcessoDocumentoBinHome(ProcessoDocumentoBinHome processoDocumentoBinHome) {
        this.processoDocumentoBinHome = processoDocumentoBinHome;
    }

    /**
     * @return the modeloDocumentoManager
     */
    public ModeloDocumentoManager getModeloDocumentoManager() {
    	if (modeloDocumentoManager == null) {
    		modeloDocumentoManager = ComponentUtil.getModeloDocumentoManager();
    	}
        return modeloDocumentoManager;
    }

    /**
     * @param modeloDocumentoManager the modeloDocumentoManager to set
     */
    public void setModeloDocumentoManager(ModeloDocumentoManager modeloDocumentoManager) {
        this.modeloDocumentoManager = modeloDocumentoManager;
    }

    /**
     * @return the modeloDocumentoLocalManager
     */
    public ModeloDocumentoLocalManager getModeloDocumentoLocalManager() {
    	if (modeloDocumentoLocalManager == null) {
    		modeloDocumentoLocalManager = ComponentUtil.getModeloDocumentoLocalManager();
    	}
        return modeloDocumentoLocalManager;
    }

    /**
     * @param modeloDocumentoLocalManager the modeloDocumentoLocalManager to set
     */
    public void setModeloDocumentoLocalManager(ModeloDocumentoLocalManager modeloDocumentoLocalManager) {
        this.modeloDocumentoLocalManager = modeloDocumentoLocalManager;
    }

    /**
     * @return the tipoProcessoDocumentoManager
     */
    public TipoProcessoDocumentoManager getTipoProcessoDocumentoManager() {
    	if (tipoProcessoDocumentoManager == null) {
    		tipoProcessoDocumentoManager = ComponentUtil.getTipoProcessoDocumentoManager();
    	}
        return tipoProcessoDocumentoManager;
    }

    /**
     * @param tipoProcessoDocumentoManager the tipoProcessoDocumentoManager to set
     */
    public void setTipoProcessoDocumentoManager(TipoProcessoDocumentoManager tipoProcessoDocumentoManager) {
        this.tipoProcessoDocumentoManager = tipoProcessoDocumentoManager;
    }

    /**
     * @return the tramitacaoProcessualService
     */
    public TramitacaoProcessualService getTramitacaoProcessualService() {
    	if (tramitacaoProcessualService == null) {
    		tramitacaoProcessualService = ComponentUtil.getTramitacaoProcessualService();
    	}
        return tramitacaoProcessualService;
    }

    /**
     * @param tramitacaoProcessualService the tramitacaoProcessualService to set
     */
    public void setTramitacaoProcessualService(TramitacaoProcessualService tramitacaoProcessualService) {
        this.tramitacaoProcessualService = tramitacaoProcessualService;
    }

    /**
     * @return the parametroService
     */
    public ParametroService getParametroService() {
    	if (parametroService == null) {
    		parametroService = ComponentUtil.getParametroService();
    	}
        return parametroService;
    }

    /**
     * @param parametroService the parametroService to set
     */
    public void setParametroService(ParametroService parametroService) {
        this.parametroService = parametroService;
    }

    /**
	 * Informa se o acórdão pode ser selecionado dentre varios modelos de acórdão.
	 *
	 * @return selecionarModelosAcordao com valor true ou false de acordo com o
	 * valor da variavel de tarefa "pje:fluxo:elaborarAcordao:acordao:selecionarModelos"
	 *
	 */
	public Boolean getSelecionarModelosAcordao() {
		if (selecionarModelosAcordao == null) {
			Boolean existeVariavelFluxoDefinida = (Boolean) getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.VARIAVEL_SELECIONAR_MODELOS_ACORDAO);
			selecionarModelosAcordao = existeVariavelFluxoDefinida != null ? existeVariavelFluxoDefinida : false;
		}
		return selecionarModelosAcordao;
	}

    /**
     * @return the ocultaNotasOrais
     */
    public Boolean getOcultaNotasOrais() {
    	if(ocultaNotasOrais == null){
            Boolean existeVariavelOcultaNotasOrais = (Boolean) getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.VARIAVEL_OCULTA_NOTAS_ORAIS);
            ocultaNotasOrais = existeVariavelOcultaNotasOrais != null ? existeVariavelOcultaNotasOrais : true;
    	}
        return ocultaNotasOrais;
    }

    /**
     * @return the ocultaAcordao
     */
    public Boolean getOcultaAcordao() {
    	if(ocultaAcordao == null){
            Boolean existeVariavelOcultaAcordao = (Boolean) getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.VARIAVEL_OCULTA_ACORDAO);
            ocultaAcordao = existeVariavelOcultaAcordao != null ? existeVariavelOcultaAcordao : false;
    	}
        return ocultaAcordao;
    }

    /**
     * @return the sessaoProcessoDocumentoManager
     */
    public SessaoProcessoDocumentoManager getSessaoProcessoDocumentoManager() {
    	if (sessaoProcessoDocumentoManager == null) {
    		sessaoProcessoDocumentoManager = ComponentUtil.getSessaoProcessoDocumentoManager();
    	}
        return sessaoProcessoDocumentoManager;
    }

    /**
     * @return the sessaoProcessoDocumentoVotoManager
     */
    public SessaoProcessoDocumentoVotoManager getSessaoProcessoDocumentoVotoManager() {
    	if (sessaoProcessoDocumentoVotoManager == null) {
    		sessaoProcessoDocumentoVotoManager = ComponentUtil.getSessaoProcessoDocumentoVotoManager();
    	}
        return sessaoProcessoDocumentoVotoManager;
    }

    /**
     * @return the processoDocumentoManager
     */
    public ProcessoDocumentoManager getProcessoDocumentoManager() {
    	if (processoDocumentoManager == null) {
    		processoDocumentoManager = ComponentUtil.getProcessoDocumentoManager();
    	}
        return processoDocumentoManager;
    }

    /**
     * @return the processoDocumentoAcordaoManager
     */
    public ProcessoDocumentoAcordaoManager getProcessoDocumentoAcordaoManager() {
    	if (processoDocumentoAcordaoManager == null) {
    		processoDocumentoAcordaoManager = ComponentUtil.getProcessoDocumentoAcordaoManager();
    	}
    	return this.processoDocumentoAcordaoManager;
    }

    private ControleVersaoDocumentoManager getControleVersaoDocumentoManager() {
    	if (controleVersaoDocumentoManager == null) {
			controleVersaoDocumentoManager = ComponentUtil.getControleVersaoDocumentoManager();
		}
		return controleVersaoDocumentoManager;
	}

	public TipoVotoManager getTipoVotoManager() {
		if (tipoVotoManager == null) {
			tipoVotoManager = ComponentUtil.getTipoVotoManager();
		}
		return tipoVotoManager;
	}

	public void setTipoVotoManager(TipoVotoManager tipoVotoManager) {
		this.tipoVotoManager = tipoVotoManager;
	}

	/**
     * @return the processoDocumentoHome
     */
    public ProcessoDocumentoHome getProcessoDocumentoHome() {
    	if (processoDocumentoHome == null) {
    		processoDocumentoHome = ComponentUtil.getProcessoDocumentoHome();
    	}
        return processoDocumentoHome;
    }

    public Boolean getVencedorDiverso(){
    	this.vencedorDiverso = false;
    	if(sessaoJulgamento != null){
        	vencedorDiverso = !sessaoJulgamento.getOrgaoJulgadorVencedor().equals( sessaoJulgamento.getOrgaoJulgadorRelator() != null ?
        			sessaoJulgamento.getOrgaoJulgadorRelator() : processoJudicial.getOrgaoJulgador());
    	}
    	return vencedorDiverso;
    }

	public AbaElaboracaoAcordaoEnum getAba(){
		return AbaElaboracaoAcordaoEnum.findByName(getAbaSelecionada());
	}

	public void setAba(AbaElaboracaoAcordaoEnum aba) {
		setAbaSelecionada(aba==null ? null : aba.getNome());		
	}

	@Override
	public void setAbaSelecionada(String abaSelecionada) {
		if (!Objects.equals(getAbaSelecionada(), abaSelecionada)) {
			super.setAbaSelecionada(abaSelecionada);
			
			limparVotoSelecionado();

			try {
				load();
			} catch (Exception e) {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao carregar as informações da tela. " + e.getLocalizedMessage());
			}
		}
	}
	
	public ProcessoDocumento getEmenta(){
		return acordaoCompilacao != null && acordaoCompilacao.getEmentaRelatorDoAcordao() != null ? acordaoCompilacao.getEmentaRelatorDoAcordao().getProcessoDocumento() : null;
	}

	public ProcessoDocumento getRelatorio(){
		return acordaoCompilacao != null && acordaoCompilacao.getRelatorio() != null ? acordaoCompilacao.getRelatorio().getProcessoDocumento() : null;
	}

	public ProcessoDocumento getVoto(){
		return acordaoCompilacao != null && acordaoCompilacao.getVotoRelatorDoProcesso() != null ? acordaoCompilacao.getVotoRelatorDoProcesso().getProcessoDocumento() : getDocumentoJudicialService().getDocumento();
	}

	public ProcessoDocumento getVotoVencedor(){
		return acordaoCompilacao != null && acordaoCompilacao.getVotoVencedor() != null ? acordaoCompilacao.getVotoVencedor().getProcessoDocumento() : null;
	}

	public ProcessoDocumento getVencedor(){
		return getVotoVencedor();
	}

	public ProcessoDocumento getNotasOrais() throws PJeBusinessException {
		if (acordaoCompilacao.getNotasOrais() == null) {
			OrgaoJulgador orgaoJulgador = null;
			if (sessaoExistente) {
				orgaoJulgador = sessaoJulgamento.getOrgaoJulgadorRelator();
			}
			acordaoCompilacao.setNotasOrais(criarDocumento(sessaoJulgamento, AbaElaboracaoAcordaoEnum.ABA_NOTAS_ORAIS, null, orgaoJulgador));
		}
		return acordaoCompilacao.getNotasOrais().getProcessoDocumento();
	}

	public ProcessoDocumento getAcordao(){
		if(getOcultaAcordao()){
			return null;
		}
		return acordaoCompilacao.getAcordao();
	}

	public List<ProcessoDocumento> getDemaisVotos(){
		return acordaoCompilacao.getProcessoDocumentoVotosSemVotoRelatorParaAcordao();
	}

	public String getDownloadLinks(){
		List<ProcessoDocumento> docsParaAssinatura = acordaoCompilacao.getProcessoDocumentosParaAssinatura();
		StringBuilder sb = new StringBuilder();
		verificaDocumentosAcordao(false);
		if(msgErros != null && msgErros.isEmpty()){
			sb.append(getDocumentoJudicialService().getDownloadLinks(docsParaAssinatura, true));
			}
		return sb.toString();
		}

	/**
	 * Metodo que carrega o modelo de acórdão no editor, com as variaveis de EL já processadas.
	 */
	public void carregarModeloAcordao(){
		try {
			String modeloDocumento = getModeloDocumentoManager().obtemConteudo(getProcessoDocumentoHome().getModeloDocumentoCombo());
			if(modeloDocumento != null){
				acordaoCompilacao.getAcordao().getProcessoDocumentoBin().setModeloDocumento(modeloDocumento);
			}
		} catch (Exception e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "erro.acordao.selecionarModelo");
		}
	}

	/**
	 * Recupera a lista com os documentos de voto do órgão julgador vencedor
	 *
	 * @return List<ProcessoDocumento> contendo os documentos do órgão julgador vencedor
	 */
	public List<ProcessoDocumento> getListaDocumentosVotoMagistradoVencedor(){
		OrgaoJulgador orgaoJulgadorVencedor = this.acordaoCompilacao.getSessaoPautaProcessoTrf().getOrgaoJulgadorVencedor();
		List<SessaoProcessoDocumento> listSessaoProcessoDocumento = getSessaoProcessoDocumentoManager().recuperaElementosJulgamento(processoJudicial, null, orgaoJulgadorVencedor);
		List<ProcessoDocumento> listaDocumentosVoto = new ArrayList<ProcessoDocumento>();

		for(SessaoProcessoDocumento spd: listSessaoProcessoDocumento) {
			if(SessaoProcessoDocumentoVoto.class.isAssignableFrom(spd.getClass()) && spd.getProcessoDocumento() != null) {
				SessaoProcessoDocumentoVoto v = (SessaoProcessoDocumentoVoto) spd;
				listaDocumentosVoto.add(v.getProcessoDocumento());
			}
		}

		return listaDocumentosVoto;
	}

	/**
	 * Retorna valor booleano caso seja necessario destacar com formatação especial o voto na grid
	 *
	 * @param ProcessoDocumento selecionado
	 * @return true caso necessario destacar, false caso contrario
	 */
	public boolean formatarDocumentoVotoSelecionado(ProcessoDocumento doc){
		if(doc != null && (doc.getIdProcessoDocumento() == getProcessoDocumentoVotoSelecionado().getIdProcessoDocumento())){
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * Verifica se o documento de voto selecionado foi assinado.
	 *
	 * @param ProcessoDocumento selecionado
	 * @return
	 */
	public boolean isProcessoDocumentoVotoAssinado(ProcessoDocumento doc){
		return getAssinaturaDocumentoService().isProcessoDocumentoAssinado(doc);
	}

	/**
	 * verifica se o orgão julgador do usuario corrente é o orgão julgador vencedor da sessão de julgamento
	 *
	 * @return true caso o orgão julgador atual for o orgão julgador vencedor da sessão de julgamento
	 */
	public boolean isMagistradoLogadoVencedorSessaoJulgamento() {
		int idOrgaoJulgadorAtual = Authenticator.getIdOrgaoJulgadorAtual();
		return idOrgaoJulgadorAtual != 0 && idOrgaoJulgadorAtual == obterIdOrgaoJulgadorVencedor();
	}

	/**
	 * Obtém o id do órgão julgador vencedor da sessão.
	 * @return int
	 */
	private int obterIdOrgaoJulgadorVencedor() {
		int retornaZero = 0;
		return isOrgaoJulgadorVencedorNulo() ? retornaZero : acordaoCompilacao.getSessaoPautaProcessoTrf().getOrgaoJulgadorVencedor().getIdOrgaoJulgador();
	}

	/**
	 * Verifica se o orgão julgador vencedor da sessão está nulo ou indefinido.
	 * @return true se o órgão julgador estiver nulo ou indefinido.
	 */
	private boolean isOrgaoJulgadorVencedorNulo() {
		return acordaoCompilacao == null
				|| acordaoCompilacao.getSessaoPautaProcessoTrf() == null
				|| acordaoCompilacao.getSessaoPautaProcessoTrf().getOrgaoJulgadorVencedor() == null
				||  acordaoCompilacao.getSessaoPautaProcessoTrf().getOrgaoJulgadorVencedor().getIdOrgaoJulgador() == 0;
	}

	/**
	 * Verifica se é possivel a edição do documento de voto selecionado observando se o orgão julgador atual do usuário logado é o orgão
	 * vencedor da sessão de julgamento e se o documento não esta assinado.
	 *
	 * @return true caso seja permitido alterar o conteudo do docuento de voto e false caso contrario
	 */
	public boolean isPodeEditarProcessoDocumentoVotoSelecionado(){
		return isMagistradoLogadoVencedorSessaoJulgamento() && !isProcessoDocumentoVotoSelecionadoAssinado();
	}

	/**
	 * Retira a assinatura do documento de voto selecionado após edição do mesmo pelo usuario do orgão julgador vencedor da sessão de julgamento
	 *
	 * @author eduardo.pereira@tse.jus.br
	 * @see <a href="http://www.cnj.jus.br/jira/browse/PJEII-20524">PJEII-20524</a>
	 */
	public void removerAssinaturaProcessoDocumentoVotoSelecionado(){
		if(processoDocumentoVotoSelecionado != null) {
			this.getAssinaturaDocumentoService().removeAssinatura(processoDocumentoVotoSelecionado);
		}
	}

	/**
	 * Assina o documento de voto selecionado pelo usuario do orgão julgador vencedor da sessão de julgamento
	 *
	 * @author eduardo.pereira@tse.jus.br
	 * @see <a href="http://www.cnj.jus.br/jira/browse/PJEII-20524">PJEII-20524</a>
	 */
	public void assinarProcessoDocumentoVotoSelecionado(){
		try {
			getDocumentoJudicialService().finalizaDocumento(getProcessoDocumentoVotoSelecionado(), processoJudicial, getTaskInstance().getId(), false, true, false, Authenticator.getPessoaLogada(), false);
			getDocumentoJudicialService().flush();
			FacesMessages.instance().clear();
		}catch (Exception e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "erro.voto.assinarDocumentoVoto", e.getLocalizedMessage());
		}
	}

	/**
	 * Verifica se o documento de voto selecionado esta assinado.
	 *
	 * @return true caso o documento esteja assinado, false caso contrario
	 *
	 * @author eduardo.pereira@tse.jus.br
	 * @see <a href="http://www.cnj.jus.br/jira/browse/PJEII-20524">PJEII-20524</a>
	 */
	public boolean isProcessoDocumentoVotoSelecionadoAssinado(){
		return this.isProcessoDocumentoVotoAssinado(getProcessoDocumentoVotoSelecionado());
	}

	/**
	 * Método responsável por realizar a inicialização dos documentos que compõem o acórdão bem como, verificar se
	 * o mesmo encontra-se em uma sessão de julgamento ou foi julgado.
	 */
	private void inicializarDocumentos(){
		inicializado = true;

		this.arquivosAssinados = new ArrayList<ArquivoAssinadoHash>();

		SessaoPautaProcessoTrf sppt = getSessaoPautaProcessoTrfManager().getSessaoPautaProcessoTrfJulgado(ProcessoJbpmUtil.getProcessoTrf());
		sessaoExistente = sppt != null;

		if (sessaoExistente) {
			try {
				getSessaoPautaProcessoTrfManager().atualizaSessaoProcessoDocumentos(sppt.getProcessoTrf(), sppt.getSessao());
			} catch (Exception e) {
				FacesMessages.instance().add(Severity.ERROR, e.getMessage());
				return;
			}

		}

		acordaoCompilacao = sessaoPautaProcessoTrfManager.recuperarAcordaoCompilacaoParaElaboracao(sppt, getTaskInstance());
		
		getSessaoPautaProcessoTrfHome().setInstanciaParaFluxo();
		try {
			acordaoCompilacao = sessaoPautaProcessoTrfManager.recuperarAcordaoCompilacao(sppt, getTaskInstance());
		} catch (PJeRuntimeException e) {
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
		}
				
		sessaoJulgamento = acordaoCompilacao.getSessaoPautaProcessoTrf();
		
		if(sessaoJulgamento == null || sessaoJulgamento.getIdSessaoPautaProcessoTrf() == 0){
			sessaoJulgamento = null;
			return;
		}
		
        if (acordaoCompilacao.getAcordao().getProcesso() == null) {
            acordaoCompilacao.getAcordao().setProcesso(ProcessoJbpmUtil.getProcessoTrf().getProcesso());
            acordaoCompilacao.getAcordao().setProcessoTrf(ProcessoJbpmUtil.getProcessoTrf());
        }

        isHabilitarMultiDocs();
        inicializaNotasOrais();
        inicializaEmentaDoRelatorDoAcordao();
        inicializaRelatorio();

		if (isHabilitarMultiDocs()) {
			inicializarDocumentosVotoOrgaoJulgadorVencedor();
		} else {
			inicializaVotoDoRelatorDoProcesso();
		}

		if (sessaoExistente) {
			julgado = getSessaoProcessoDocumentoManager().existeAcordaoPendente(getProcessoJudicial(), sessaoJulgamento);

			if(julgado){
				return;
			}

			if (acordaoCompilacao.isRelatorParaAcordaoDiferenteRelatorOriginario()) {
				inicializaVotoDoRelator();
				inicializaVotoVencedor();
				
			} else {
				decisao = getSessaoProcessoDocumentoVotoManager().recuperarVoto(sppt.getSessao(), sppt.getProcessoTrf(), sppt.getProcessoTrf().getOrgaoJulgador());
			}

			carregarMapaAssinaturas();
			
			getDocumentoJudicialService().associarDocumentoAcordaoASessao(acordaoCompilacao);
			getDocumentoJudicialService().associarDocumentosAoDocumentoAcordao(acordaoCompilacao);
			
		}
	}

	/**
	 * Método responsável por verificar se permiti a inclusão de um relatório no acórdão e se não
	 * existir um e for obrigatório, cria-se um vazio.
	 *
	 */
	private void inicializaRelatorio() {
		this.permiteIncluirRelatorio = true;

		// Verifica se a variável que permite a inclusão de voto está definida
		Object isExisteVariavel = getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.ACORDAO_PERMITE_INCLUIR_RELATORIO);
		if (isExisteVariavel != null) {
			// Caso a variável esteja definida, o atributo permiteIncluirRelatorio recebe seu valor, caso contrário, será permitido ao usuário incluir
			this.permiteIncluirRelatorio = (Boolean) isExisteVariavel;
		}

		if (permiteIncluirRelatorio && acordaoCompilacao.getRelatorio() == null) {
			OrgaoJulgador orgaoJulgador = null;
			if (sessaoExistente) {
				orgaoJulgador = sessaoJulgamento.getOrgaoJulgadorRelator();
			}
			try {
				acordaoCompilacao.setRelatorio(criarDocumento(acordaoCompilacao.getSessaoPautaProcessoTrf(), AbaElaboracaoAcordaoEnum.ABA_RELATORIO, null, orgaoJulgador));
			} catch (PJeBusinessException e) {
				logger.error(e);
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "erro.acordao.novoRelatorio", e.getMessage());
			}
		}
	}

	/**
	 * Método responsável por verificar se permiti a inclusão do voto do relator do processo no acórdão e se não
	 * existir um e for obrigatório, cria-se um vazio.
	 *
	 */
	private void inicializaVotoDoRelatorDoProcesso() {
		this.permiteIncluirVoto = true;

		// Verifica se a variável que permite a inclusão de relatório está definida
		Object isExisteVariavel = getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.ACORDAO_PERMITE_INCLUIR_VOTO);
		if (isExisteVariavel != null) {
			// Caso a variável esteja definida, o atributo permiteIncluirVoto recebe seu valor, caso contrário, será permitido ao usuário incluir
			this.permiteIncluirVoto = (Boolean) isExisteVariavel;
		}

		if (permiteIncluirVoto && acordaoCompilacao.getVotoRelatorDoProcesso() == null) {
			try {
				OrgaoJulgador orgaoJulgador = null;
				if (sessaoExistente) {
					orgaoJulgador = sessaoJulgamento.getOrgaoJulgadorRelator();
				}
				SessaoProcessoDocumento spd = criarDocumento(acordaoCompilacao.getSessaoPautaProcessoTrf(), AbaElaboracaoAcordaoEnum.ABA_VOTO, null, orgaoJulgador);

				acordaoCompilacao.getVotos().add((SessaoProcessoDocumentoVoto) spd);
			}
			catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao criar um novo voto, mensagem interna: " + e.getMessage());
			}
		}
	}

	/**
	 * Carrega o documento de voto mais recente do órgão julgador atualmente logado para apresentação no editor.
	 */
	private void inicializarDocumentosVotoOrgaoJulgadorVencedor(){
		List<ProcessoDocumento> listaDocsVotos = this.getListaDocumentosVotoMagistradoVencedor();
		if(listaDocsVotos != null && !listaDocsVotos.isEmpty()) {
			setProcessoDocumentoVotoSelecionado(listaDocsVotos.get(0));
		} else if(acordaoCompilacao.getVotoRelatorDoProcesso() != null) {
			setProcessoDocumentoVotoSelecionado(acordaoCompilacao.getVotoRelatorDoProcesso().getProcessoDocumento());
		} else {
			SessaoProcessoDocumento spd = criarVoto();
			setProcessoDocumentoVotoSelecionado(spd.getProcessoDocumento());
		}
	}

	/**
	 * Criar um novo documento do tipo voto.
	 * @return SessaoProcessoDocumento
	 */
	private SessaoProcessoDocumento criarVoto() {
		SessaoProcessoDocumento spd = new SessaoProcessoDocumento();
		try {
			OrgaoJulgador orgaoJulgador = null;
			if (sessaoExistente) {
				orgaoJulgador = sessaoJulgamento.getOrgaoJulgadorRelator();
			}
			spd = criarDocumento(acordaoCompilacao.getSessaoPautaProcessoTrf(), AbaElaboracaoAcordaoEnum.ABA_VOTO, null, orgaoJulgador);
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao criar um novo voto, mensagem interna: " + e.getMessage());
		}
		return spd;
	}

	private void inicializaEmentaDoRelatorDoAcordao() {
		this.permiteIncluirEmenta = true;

		// Verifica se a variável que permite a inclusão de ementa está definida
		Object isExisteVariavel = getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.ACORDAO_PERMITE_INCLUIR_EMENTA);
		if (isExisteVariavel != null) {
			// Caso a variável esteja definida, o atributo permiteIncluirEmenta recebe seu valor, caso contrário, será permitido ao usuário incluir
			this.permiteIncluirEmenta = (Boolean) isExisteVariavel;
		}

		OrgaoJulgador ojRelator = null;
		
		if (orgaoJulgadorAtual==null){
			obterOrgaosJulgadores();
		}
		
		if (sessaoExistente) {
			if(TipoSituacaoPautaEnum.JG.equals(sessaoJulgamento.getSituacaoJulgamento()) 
					&& sessaoJulgamento.getOrgaoJulgadorVencedor() != null 
					&& (sessaoJulgamento.getOrgaoJulgadorVencedor().equals(orgaoJulgadorAtual) 
					|| orgaoJulgadorAtual==null)) {
				ojRelator = sessaoJulgamento.getOrgaoJulgadorVencedor();
			} else {
				ojRelator = sessaoJulgamento.getOrgaoJulgadorRelator();
			}

			acordaoCompilacao.setEmentaRelatorDoAcordao(ComponentUtil.getSessaoPautaProcessoTrfManager().recuperarEmentaAtivaPorSessaoEhProcessoEhOrgaoJulgador(sessaoJulgamento.getSessao(), sessaoJulgamento.getProcessoTrf(), ojRelator));
		}

        // Se nao existir uma ementa e o fluxo permite criar, entao cria-se uma nova
        if (permiteIncluirEmenta && acordaoCompilacao.getEmentaRelatorDoAcordao() == null) {
            try {
                acordaoCompilacao.setEmentaRelatorDoAcordao(criarDocumento(sessaoJulgamento, AbaElaboracaoAcordaoEnum.ABA_EMENTA, null, ojRelator));
            } catch (PJeBusinessException e) {
                logger.error(e);
                FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "erro.acordao.novaEmenta", e.getMessage());
            }
        }
	}

	private void inicializaVotoDoRelator() {

		// Se nao existir o voto do relator do acórdao entao cria um novo
		if (acordaoCompilacao.getVotoRelator() == null) {
			try {
				SessaoProcessoDocumentoVoto vrp = acordaoCompilacao.getVotoRelatorDoProcesso();

				SessaoProcessoDocumentoVoto vra = getVotoVencedor_(sessaoJulgamento, vrp != null ? vrp.getProcessoDocumento() : null);

				acordaoCompilacao.getVotos().add(vra);
			} catch (PJeBusinessException e) {
				logger.error(e);
				FacesMessages.instance().addFromResourceBundle(Severity.FATAL, "erro.acordao.buscarVotoRelator", e.getMessage());
			}
		}

		decisao = acordaoCompilacao.getVotoRelator();
	}

	private void inicializaVotoVencedor() {

		// Se nao existir o voto do relator do acórdao entao cria um novo
		if (acordaoCompilacao.getVotoVencedor() == null) {
			try {
				SessaoProcessoDocumentoVoto vrp = acordaoCompilacao.getVotoRelatorDoProcesso();

				SessaoProcessoDocumentoVoto vra = getVotoVencedor_(sessaoJulgamento, vrp != null ? vrp.getProcessoDocumento() : null);

				acordaoCompilacao.getVotos().add(vra);
			} catch (PJeBusinessException e) {
				logger.error(e);
				FacesMessages.instance().addFromResourceBundle(Severity.FATAL, "erro.acordao.buscarVotoRelator", e.getMessage());
			}
		}

		decisao = acordaoCompilacao.getVotoVencedor();		
	}

	private void inicializaNotasOrais() {
		this.permiteIncluirNotasOrais = true;

		// Verifica se a variável que permite a inclusão das notas está definida
		Object isExisteVariavel = getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.ACORDAO_PERMITE_INCLUIR_NOTAS_ORAIS);
		if (isExisteVariavel != null) {
			// Caso a variável esteja definida, o atributo permiteIncluirNotasOrais recebe seu valor, caso contrário, será permitido ao usuário incluir
			this.permiteIncluirNotasOrais = (Boolean) isExisteVariavel;
		}

		// Se nao existir uma nota oral e o fluxo permitir a inclusão, entao cria uma nova
		if (permiteIncluirNotasOrais && acordaoCompilacao.getNotasOrais() == null) {
			try {
				TipoProcessoDocumento tipoProcessoDocumento = ParametroUtil.instance().getTipoProcessoDocumentoNotasOrais();

				ProcessoDocumento processoDocumento = ComponentUtil.getProcessoDocumentoManager().getUltimoProcessoDocumento(tipoProcessoDocumento, getProcessoJudicial().getProcesso());

				OrgaoJulgador orgaoJulgador = null;
				if (sessaoExistente) {
					orgaoJulgador = sessaoJulgamento.getOrgaoJulgadorVencedor() == null ? 
                                                sessaoJulgamento.getOrgaoJulgadorRelator() 
                                                : sessaoJulgamento.getOrgaoJulgadorVencedor();
				}
				if (processoDocumento != null) {
					acordaoCompilacao.setNotasOrais(criarDocumento(sessaoJulgamento, AbaElaboracaoAcordaoEnum.ABA_NOTAS_ORAIS, processoDocumento, orgaoJulgador));
				} else {
					acordaoCompilacao.setNotasOrais(criarDocumento(sessaoJulgamento, AbaElaboracaoAcordaoEnum.ABA_NOTAS_ORAIS, null, orgaoJulgador));
				}
			} catch (PJeBusinessException e) {
				logger.error(e);
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "erro.acordao.novaNotaOral", e.getMessage());
			}
		}
	}

	private void carregarMapaAssinaturas(){
		List<SessaoProcessoDocumentoVoto> votos = acordaoCompilacao.getVotosSemVotoRelatorParaAcordao();
		for(SessaoProcessoDocumento spd: votos){
			mapaAssinaturas.put(spd, getDocumentoJudicialService().validaAssinaturasDocumento(spd.getProcessoDocumento(), true, false));
		}
	}

	private SessaoProcessoDocumentoVoto getVotoVencedor_(SessaoPautaProcessoTrf sessaoJulgamento, ProcessoDocumento votoRelator) throws PJeBusinessException{
		SessaoProcessoDocumentoVoto ret = getSessaoProcessoDocumentoVotoManager().recuperarVoto(sessaoJulgamento.getSessao(), sessaoJulgamento.getProcessoTrf(), sessaoJulgamento.getOrgaoJulgadorVencedor());
		if(ret == null){
			//Não existe sequer o voto vencedor, mas o vencedor foi outro julgador que não o relator. Tem-se que criar o voto e seu respectivo documento
			ret = new SessaoProcessoDocumentoVoto();
			ret.setCheckAcompanhaRelator(false);
			ret.setDestaqueSessao(false);
			ret.setImpedimentoSuspeicao(false);
			ret.setSessao(sessaoJulgamento.getSessao());
			ret.setLiberacao(true);
			ret.setProcessoTrf(sessaoJulgamento.getProcessoTrf());
			ret.setOrgaoJulgador(sessaoJulgamento.getOrgaoJulgadorVencedor());
			ret.setOjAcompanhado(sessaoJulgamento.getOrgaoJulgadorVencedor());
		}
		TipoProcessoDocumento tipoVoto = null;
		if(votoRelator == null){
			tipoVoto =ParametroUtil.instance().getTipoProcessoDocumentoVoto();
		}
		else{
			tipoVoto = votoRelator.getTipoProcessoDocumento();
		}
		// Existe um voto vencedor, mas ele não tem documento, senão este método não estaria sendo chamado
		ProcessoDocumento doc = getNovoDocumento(sessaoJulgamento.getProcessoTrf(), tipoVoto);
		ret.setProcessoDocumento(doc);
		getSessaoProcessoDocumentoVotoManager().persist(ret);
		return ret;
	}

	private ProcessoDocumento getNovoDocumento(ProcessoTrf processo, TipoProcessoDocumento tipo) throws PJeBusinessException{
		ProcessoDocumento ret = getDocumentoJudicialService().getDocumento();
		ret.setTipoProcessoDocumento(tipo);
		ret.setProcessoDocumento(ret.getTipoProcessoDocumento().getTipoProcessoDocumento());
		ret.setProcesso(processo.getProcesso());
		ret.setProcessoTrf(processo);
		ret.getProcessoDocumentoBin().setModeloDocumento(" ");
		// Impede que seja criado um documento em branco
		return getDocumentoJudicialService().persist(ret, true, true);
	}

	public SessaoPautaProcessoTrf getSessaoJulgamento() {
		return acordaoCompilacao.getSessaoPautaProcessoTrf();
	}

	public SessaoProcessoDocumentoVoto getDecisao(){
		if(!inicializado){
			inicializarDocumentos();
		}
		return decisao;
	}
	
	
	private TipoProcessoDocumento findTipoProcessoDocumento(AbaElaboracaoAcordaoEnum tipoDocumento) throws PJeBusinessException {
		switch (tipoDocumento) {
			case ABA_EMENTA:
				return ParametroUtil.instance().getTipoProcessoDocumentoEmenta();
			case ABA_RELATORIO:
				return ParametroUtil.instance().getTipoProcessoDocumentoRelatorio();
			case ABA_VOTO:
				return ParametroUtil.instance().getTipoProcessoDocumentoVoto();
			case ABA_NOTAS_ORAIS:
				return ParametroUtil.instance().getTipoProcessoDocumentoNotasOrais();
			default:
				throw new PJeBusinessException("Tipo de documento novo não suportado: " + tipoDocumento);
		}
	}

	private SessaoProcessoDocumento criarDocumento(SessaoPautaProcessoTrf julgamento, AbaElaboracaoAcordaoEnum tipoDocumento, ProcessoDocumento processoDocumento, OrgaoJulgador orgaoJulgador) throws PJeBusinessException{	
		if (processoDocumento == null) {
			TipoProcessoDocumento tipo = findTipoProcessoDocumento(tipoDocumento);
			if(tipo == null) {
				return null;
			}
			processoDocumento = getNovoDocumento(julgamento != null ? julgamento.getProcessoTrf() : ProcessoJbpmUtil.getProcessoTrf(), tipo);
		}

		SessaoProcessoDocumento ret = new SessaoProcessoDocumento();
		ret.setProcessoDocumento(processoDocumento);

		// Atribui os valores comuns aos objetos de sessão, caso a sessão tenha ocorrido
		if (sessaoExistente) {
			ret.setLiberacao(true);
			ret.setOrgaoJulgador(orgaoJulgador);
			ret.setSessao(julgamento.getSessao());
			ret.setProcessoDocumento(processoDocumento);
		}

		if (!tipoDocumento.equals(AbaElaboracaoAcordaoEnum.ABA_VOTO) && sessaoExistente) {
			ret = getSessaoProcessoDocumentoManager().persist(ret);

		} else if (tipoDocumento.equals(AbaElaboracaoAcordaoEnum.ABA_VOTO)) {
			SessaoProcessoDocumentoVoto retVoto = new SessaoProcessoDocumentoVoto();
			try {
				BeanUtils.copyProperties(retVoto, ret);
			} catch (Exception e) {
				throw new PJeBusinessException(e);
			}

			if (sessaoExistente) {
				retVoto.setOjAcompanhado(julgamento.getProcessoTrf().getOrgaoJulgador());
				retVoto.setProcessoTrf(julgamento.getProcessoTrf());
				return getSessaoProcessoDocumentoManager().persist(retVoto);
			} else {
				retVoto.setOrgaoJulgador(ProcessoJbpmUtil.getProcessoTrf().getOrgaoJulgador());
			}

			return retVoto;
		}

		return ret;
	}

	public Boolean getJulgado() {
		return julgado;
	}

	public Map<SessaoProcessoDocumento, List<ProcessoDocumentoBinPessoaAssinatura>> getMapaAssinaturas() {
		return mapaAssinaturas;
	}

	protected Map<String, String> getParametrosConfiguracao() {
		return parametrosFluxo;
	}

	public ModeloDocumento getModelo() {
		return modelo;
	}

	public void setModelo(ModeloDocumento modelo) {
		this.modelo = modelo;
	}

	@Override
	public boolean podeAssinar() {
		if(getAba() != AbaElaboracaoAcordaoEnum.ABA_NOTAS_ORAIS){
			return podeAssinar && isApresentaBotaoAssinar();
		}
		return Boolean.FALSE;
	}

	public boolean isPodeAssinarAcordao(){
		return (podeAssinar() && acordaoValidado);
	}

	/**
	 * Metodo que verifica se  obrigatrio a assinatura pelo tipo processo documento
	 * e papel do usurio logado
	 * @return boolean
	 */
	public boolean isApresentaBotaoAssinar(){
		return !getTipoProcessoDocumentoPapelService().verificarExigibilidadeNaoAssina(
				Authenticator.getPapelAtual(),
				acordaoCompilacao.getAcordao().getTipoProcessoDocumento());
	}

	public AcordaoCompilacao getAcordaoCompilacao() {
		return acordaoCompilacao;
	}

	public ProcessoDocumento getProcessoDocumentoVotoSelecionado() {
		return processoDocumentoVotoSelecionado;
	}

	public void setProcessoDocumentoVotoSelecionado(ProcessoDocumento processoDocumentoVotoSelecionado) {
		this.processoDocumentoVotoSelecionado = processoDocumentoVotoSelecionado;
	}

	/**
	 * Informa se a aba voto ira exibir os multiplos documentos de voto da entidade "SessaoProcessoMultDocsVoto" de acordo com configuração
	 * da variavel de fluxo "pje:fluxo:elaborarAcordao:voto:habilitarMultiDocs"
	 *
	 * @return habilitarMultiDocs com valor true ou false de acordo com o
	 * valor da variavel de tarefa "pje:fluxo:elaborarAcordao:voto:habilitarMultiDocs"
	 */
	public Boolean isHabilitarMultiDocs() {
		if (habilitarMultiDocs == null) {
			Boolean existeVariavelHabilitarMultiDocs = (Boolean) getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.VARIAVEL_HABILITAR_MULTI_DOCS);
			habilitarMultiDocs = existeVariavelHabilitarMultiDocs != null ? existeVariavelHabilitarMultiDocs : false;
		}
		return habilitarMultiDocs;
	}

	public void setHabilitarMultiDocs(Boolean habilitarMultiDocs) {
		this.habilitarMultiDocs = habilitarMultiDocs;
	}

	/**
	 * Verifica se o parametro do sistema se refera a Justica Eleitora.
	 *
	 * @return True se for Justica Eleitoral
	 */
	@SuppressWarnings("static-access")
	public Boolean getJusticaEleitoral() {
		if (justicaEleitoral == null) {
			justicaEleitoral = TIPO_JUSTICA.ELEITORAL.equalsIgnoreCase(getParametroUtil().instance().getTipoJustica());
		}
		return justicaEleitoral;
	}

	/**
	 * Verifica se o OrgaoJulgadorAtual e o relator da processo pautado.
	 *
	 * @return True se for relator do processo pautado
	 */
	public Boolean getRelator() {
		if (relator == null) {
			relator = Objects.equals(Authenticator.getOrgaoJulgadorAtual(),
										getSessaoPautaProcessoTrfHome().getInstance().getOrgaoJulgadorRelator());
		}
		return relator;
	}

	/**
	 * Verifica se a EL somente leitura referente a variavel de fluxo dos editores esta configurada.
	 *
	 * @return True se o editor for somente leitura
	 */
	public Boolean getSomenteLeitura() {
		if (somenteLeitura == null) {
			Boolean existeVarSomenteLeitura = (Boolean) getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.ACORDAO_EDITOR_SOMENTE_LEITURA);
			somenteLeitura = existeVarSomenteLeitura != null ? existeVarSomenteLeitura : Boolean.FALSE;
		}
		return somenteLeitura;
	}

	/**
	 * Metodo acessor que verifica se pode editar o relatorio, levando em conta a variavel de somente leitura e
	 * a variavel de permissao para inclusao do relatorio, alem de verificar o relator e a assinatura do documento.
	 *
	 * @return True se as regras de edicao forem atendidas
	 */
	public Boolean getEditarRelatorio() {
		if (editarRelatorio == null) {
			ProcessoDocumento relatorio = getRelatorio();
			if (relatorio != null) {
				boolean edicaoRelatorioValida = isEdicaoVotoOuRelatorio(isDocumentoAssinado(relatorio));
				editarRelatorio = isEdicaoDocumento(Variaveis.ACORDAO_PERMITE_INCLUIR_RELATORIO, edicaoRelatorioValida);
			} else {
				editarRelatorio = isEdicaoDocumento(Variaveis.ACORDAO_PERMITE_INCLUIR_RELATORIO, true);
			}
			if (sessaoExistente && isOrgaosPreenchidos() && !editarRelatorio) {
				editarRelatorio = orgaoJulgadorAtual.equals(orgaoJulgadorRelator) 
						&& !isDocumentoAssinado(relatorio) 
						&& !orgaoJulgadorVencedor.equals(orgaoJulgadorRelator);
			}
			if (editarRelatorio==null)
				return false;
		}
		return editarRelatorio;
	}

	/**
	 * Metodo acessor que verifica se pode editar o voto, levando em conta a variavel de somente leitura e
	 * a variavel de permissao para inclusao do voto, alem de verificar o relator e a assinatura do documento.
	 *
	 * @return True se as regras de edicao de voto forem atendidas
	 */
	public Boolean getEditarVoto() {
		if(editarVoto == null){
			editarVoto = verificarPermissaoEdicaoDoc(getVoto(), Variaveis.ACORDAO_PERMITE_INCLUIR_VOTO);
		}
		return editarVoto;
	}

	/**
	 * Metodo acessor que verifica se pode editar o voto vencedor, levando em conta a variavel de somente leitura e
	 * a variavel de permissao para inclusao do voto, alem de verificar o relator e a assinatura do documento.
	 *
	 * @return True se as regras de edicao forem atendidas
	 */
	public Boolean getEditarVotoVencedor() {
		if(editarVotoVencedor == null){
			editarVotoVencedor = verificarPermissaoEdicaoDoc(getVotoVencedor(), Variaveis.ACORDAO_PERMITE_INCLUIR_VOTO);
		}
		return editarVotoVencedor;
	}

	/**
	 * Verifica se o documento possui permissão para edição. Para isso, se o processoDocumento estiver nulo, levará em
	 * consideração o valor da variável de fluxo e da variável usada para somente leitura.
	 * Caso o processoDocumento não esteja nulo, além das validações descritas, também validará se é relator ou se o
	 * processoDocumento não foi assinado.
	 * @param processoDocumento
	 * @param variavelFluxo
	 * @return	true se, no caso do processoDocumento for nulo, a variável de fluxo for true e a variável somente leitura
	 * 			for falso, e no caso do processoDocumento nulo, além das validações citadas, valida tb se é relator ou
	 * 			se o processoDocumento não foi assinado.
	 */
	private boolean verificarPermissaoEdicaoDoc(ProcessoDocumento processoDocumento, String variavelFluxo) {
		boolean permiteEditarDoc = false;
		if (processoDocumento != null) {
			boolean edicaoVotoValida = isEdicaoVotoOuRelatorio(isDocumentoAssinado(processoDocumento));
			permiteEditarDoc = isEdicaoDocumento(variavelFluxo, edicaoVotoValida);
		} else {
			permiteEditarDoc = isEdicaoDocumento(variavelFluxo, true);
		}
		return permiteEditarDoc;
	}

	/**
	 * Metodo acessor que verifica se pode editar a ementa, levando em conta a variavel de somente leitura e
	 * a variavel de permissao para inclusao da ementa, alem de verificar a data de juntada.
	 *
	 * @return True se as regras de edicao forem atendidas
	 */
	public Boolean getEditarEmenta() {
		if (editarEmenta == null) {
			editarEmenta = isEdicaoDocumento(Variaveis.ACORDAO_PERMITE_INCLUIR_EMENTA, isEdicaoEmenta());
		}
		return editarEmenta;
	}

	/**
	 * Metodo que passada a variavel de fluxo e a variavel de verificacao de edicao para cada aba, sera
	 * 		verificado as regras de somente leitura, tanto para a justica eleitoral como para as demais.
	 *
	 * @param varFluxoPermiteIncluir Variavel de fluxo
	 * @param isPermitidoEditarDocumento True se pode editar caso nao seja do CNJ
	 * @return True se habilita a edicao
	 */
	private Boolean isEdicaoDocumento(String varFluxoPermiteIncluir, Boolean isPermitidoEditarDocumento) {
		boolean retorno = false;
		if (!getSomenteLeitura()) {
			boolean variavelFluxoPermiteAlterar = getVariavelFluxo(varFluxoPermiteIncluir);
			if (variavelFluxoPermiteAlterar) {
				if (isPermitidoEditarDocumento || getJusticaEleitoral()) {
				retorno = true;
			}
		}
		}
		return retorno;
	}

	/**
	 * Método que retorna o valor da variavel de fluxo, através da variavelFluxo
	 * passada por parâmetro.
	 * @param	variavelFluxo
	 * @return	true se a variavelFluxo existir e for true.
	 */
	private Boolean getVariavelFluxo(String variavelFluxo){
		Boolean variavel = (Boolean) getTramitacaoProcessualService().recuperaVariavelTarefa(variavelFluxo);
		return  variavel != null && variavel;
	}

	/**
	 * Metodo que verifica se o documento foi assinado. Para ser considerado assinado, é necessário que tenha um
	 * registro na tabela <b>"tb_proc_doc_bin_pess_assin"</b>.
	 *
	 * @param  processoDocumento é o processo documento a ser analisado
	 * @return <b>true</b> se o documento existir no processo e se o documento estiver assinado.
	 */
	private boolean isDocumentoAssinado(ProcessoDocumento processoDocumento){
		boolean isAssinado = false;
		if (processoDocumento != null) {
			isAssinado = getAssinaturaDocumentoService().isDocumentoAssinado(processoDocumento);
		}
		return isAssinado;
	}

	/**
	 * Metodo que verifica se o voto ou relatorio podem ser editados, verifica a regra separando a justica eleitoral das
	 * demais.
	 *
	 * @param documentoAssinado Parametro que indica se o documento de voto ou relatorio foi assinado.
	 * @return True se voto ou relatorio esta apto para edicao
	 */
	private boolean isEdicaoVotoOuRelatorio(boolean documentoAssinado) {
		return !getJusticaEleitoral() && (getRelator() || !documentoAssinado);
	}

	/**
	 * Metodo que verifica se a ementa pode ser editada, verifica a regra separando a justica eleitoral das demais.
	 * Além disso, verifica se o documento foi assinado. Para isso, é necessário que tenha um registro na tabela
	 * <b>"tb_proc_doc_bin_pess_assin"</b>.
	 *
	 * @return	true se o tipo de justiça não for "JE" e se o documento Ementa não existir. Caso exista, retornará true
	 * 			se este não estiver assinado.
	 */
	private boolean isEdicaoEmenta() {
		boolean isEmentaInexistente = getEmenta() == null || StringUtils.isBlank(getEmenta().getProcessoDocumentoBin().getModeloDocumento());
		return !getJusticaEleitoral() && (isEmentaInexistente || !getAssinaturaDocumentoService().isDocumentoAssinado(getEmenta()));
	}

	/**
	 * Verifica se a aba referente ao tipo do processo documento do parâmetro será exibida.
	 * @return true se a ementa existir ou se for permitida a edição de uma ementa.
	 */
	public boolean isExibeAba(ProcessoDocumento processoDocumento, boolean varPermiteIncluir, boolean varDocumentoNaoObrigatorio){
		boolean isDocumentoCriado = processoDocumento != null && StringUtils.isNotBlank(processoDocumento.getProcessoDocumentoBin().getModeloDocumento());
		return isDocumentoCriado || varPermiteIncluir || !varDocumentoNaoObrigatorio;
	}

	/**
	 * Verifica se a aba "Ementa" será exibida.
	 * @return true se a ementa existir ou se for permitida a edição de uma ementa.
	 */
	public boolean isExibeAbaEmenta(){
		return isExibeAba(getEmenta(), getVariavelFluxo(Variaveis.ACORDAO_PERMITE_INCLUIR_EMENTA), getVariavelFluxo(Variaveis.ACORDAO_EMENTA_NAO_OBRIGATORIO));
	}

	/**
	 * Verifica se a aba "Relatório" será exibida.
	 * @return true se o relatório existir ou se for permitida a edição de um relatório.
	 */
	public boolean isExibeAbaRelatorio(){
		return isExibeAba(getRelatorio(), getVariavelFluxo(Variaveis.ACORDAO_PERMITE_INCLUIR_RELATORIO), getVariavelFluxo(Variaveis.ACORDAO_RELATORIO_NAO_OBRIGATORIO));
	}

	/**
	 * Verifica se a aba "Voto" será exibida. Para isso, o órgão julgador vencedor deverá ser o mesmo do relator do
	 * processo e deve ser possível editar o voto.
	 * @return true se o relator for o vencedor, além de existir voto ou se for permitida a edição de um voto.
	 */
	public boolean isExibeAbaVoto(){
		boolean retorno = false;
		if (getVencedorDiverso() != null && vencedorDiverso) {
			retorno = isExibeAba(getVoto(), false, getVariavelFluxo(Variaveis.ACORDAO_VOTO_RELATOR_PROCESSO_NAO_OBRIGATORIO));
		} else {
			boolean votoVencedorNaoObrigatorio = getVariavelFluxo(Variaveis.ACORDAO_VOTO_VENCEDOR_NAO_OBRIGATORIO);
			if(votoVencedorNaoObrigatorio == false){
				retorno = isExibeAba(getVoto(), getVariavelFluxo(Variaveis.ACORDAO_PERMITE_INCLUIR_VOTO),votoVencedorNaoObrigatorio);
			}
			else{
				retorno = isExibeAba(getVoto(), getVariavelFluxo(Variaveis.ACORDAO_PERMITE_INCLUIR_VOTO),
						getVariavelFluxo(Variaveis.ACORDAO_VOTO_RELATOR_PROCESSO_NAO_OBRIGATORIO));
			}
		}
		return retorno;
	}

	/**
	 * Verifica se a aba "Voto Vencedor" será exibida. Para isso, o órgão julgador vencedor deverá ser diverso ao relator
	 * e deve ser possível editar o voto.
	 * @return true se o voto vencedor for diverso do relator e se for possível editar o voto vencedor.
	 */
	public boolean isExibeAbaVotoVencedor(){
		if (!sessaoExistente) {
			return false;
		}
		return getVencedorDiverso() && isExibeAba(getVotoVencedor(), getEditarVotoVencedor(), getVariavelFluxo(Variaveis.ACORDAO_VOTO_VENCEDOR_NAO_OBRIGATORIO));
	}

	/**
	 * Metodo acessor que verifica se pode editar as notas orais, levando em conta a variavel de somente leitura e
	 * a variavel de permissao para inclusao da nota oral, alem de verificar se o documento está assinado.
	 *
	 * @return True se as regras de edicao forem atendidas
	 */
	public Boolean getEditarNotasOrais() {
		if (editarNotasOrais == null) {
			editarNotasOrais = permiteIncluirNotasOrais && !isDocumentoAssinado(acordaoCompilacao.getNotasOrais().getProcessoDocumento()) && !getSomenteLeitura();
		}
		return editarNotasOrais;
	}

	/**
	 * Método responsável por retornar voto do relator do processo.
	 * @return <code>SessaoProcessoDocumentoVoto</code>, o voto do relator.
	 */
	public SessaoProcessoDocumentoVoto getVotoRelator() {
		return acordaoCompilacao.getVotoRelatorDoProcesso() != null ?
				acordaoCompilacao.getVotoRelatorDoProcesso() : getSessaoProcessoDocumentoVotoManager().recuperarVotoRelator(getSessaoPautaProcessoTrfHome().getInstance());
	}

	public List<Integer> getListaTiposDocSelecaoProcesso() {
		return this.listaTiposDocSelecaoProcesso;
	}

	public void setListaTiposDocSelecaoProcesso(
			List<Integer> listaDocsSelecionadosProcesso) {
		this.listaTiposDocSelecaoProcesso = listaDocsSelecionadosProcesso;
	}

	public Integer getIdTipoDocumentoAcordao() {
		return this.idTipoDocumentoAcordao;
	}

	public void setIdTipoDocumentoAcordao(Integer idTipoDocumentoAcordao) {
		this.idTipoDocumentoAcordao = idTipoDocumentoAcordao;
	}

	public ModeloDocumento getModeloAcordaoDocsSelecionados() {
		return this.modeloAcordaoDocsSelecionados;
	}

	public void setModeloAcordaoDocsSelecionados(
			ModeloDocumento modeloAcordaoDocsSelecionados) {
		this.modeloAcordaoDocsSelecionados = modeloAcordaoDocsSelecionados;
	}

	public ProcessoDocumentoElaboracaoAcordaoCkList getProcessoDocumentoElaboracaoAcordaoCkList() {
		if(this.processoDocumentoElaboracaoAcordaoCkList == null){
			this.processoDocumentoElaboracaoAcordaoCkList = ComponentUtil.getProcessoDocumentoElaboracaoAcordaoCkList();
		}
		return this.processoDocumentoElaboracaoAcordaoCkList;
	}

	public String getConteudoModeloDocAcordaoDocsSelecionados() {
		return this.conteudoModeloDocAcordaoDocsSelecionados;
	}

	public void setConteudoModeloDocAcordaoDocsSelecionados(
			String conteudoModeloDocAcordaoDocsSelecionados) {
		this.conteudoModeloDocAcordaoDocsSelecionados = conteudoModeloDocAcordaoDocsSelecionados;
	}

	public Boolean getVisualizacaoDocsSelecionadosHabilitada() {
		return this.visualizacaoDocsSelecionadosHabilitada;
	}

	public void setVisualizacaoDocsSelecionadosHabilitada(
			Boolean visualizacaoDocsSelecionadosHabilitada) {
		this.visualizacaoDocsSelecionadosHabilitada = visualizacaoDocsSelecionadosHabilitada;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)
			throws Exception {
		this.arquivosAssinados.add(arquivoAssinadoHash);
		uploadArquivoAssinadoRealizado = Boolean.TRUE;
	}

	@Override
	public String getActionName() {
		return NAME;
	}

	public List<ArquivoAssinadoHash> getArquivosAssinados() {
		return arquivosAssinados;
	}

	public void setArquivosAssinados(List<ArquivoAssinadoHash> arquivosAssinados) {
		this.arquivosAssinados = arquivosAssinados;
	}

	public boolean isAcordaoValidado() {
		return acordaoValidado;
	}

	public void setAcordaoValidado(boolean acordaoValidado) {
		this.acordaoValidado = acordaoValidado;
	}

	/**
	 * Método responsável por obter os órgãos julgadores do usuário logado, do
	 * relator e do órgão vencedor.
	 */
	private void obterOrgaosJulgadores() {
		this.orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();
		if (sessaoExistente) {
			this.orgaoJulgadorRelator = sessaoJulgamento.getOrgaoJulgadorRelator() != null
					? sessaoJulgamento.getOrgaoJulgadorRelator() : processoJudicial.getOrgaoJulgador();
			this.orgaoJulgadorVencedor = sessaoJulgamento.getOrgaoJulgadorVencedor();
		}
	}

	/**
	 * Método responsável por verificar a condição para edição da aba do
	 * "Voto relator".
	 *
	 * @return <code>Boolean</code>, <code>true</code> se for permitido alterar
	 *         o voto do relator.
	 */
	public boolean podeAlterarAbaVotoRelator() {
		Boolean permiteIncluirVoto = getVariavelFluxo(Variaveis.ACORDAO_PERMITE_INCLUIR_VOTO);
		boolean documentoAssinado = isDocumentoAssinado(getVoto());
		boolean retorno = false;
		
		if (isOrgaosPreenchidos()) {
			retorno = orgaoJulgadorAtual.equals(orgaoJulgadorRelator) && orgaoJulgadorVencedor.equals(orgaoJulgadorRelator)
					&& permiteIncluirVoto && !documentoAssinado;
		}
		
		return retorno;
	}
	
	/**
	 * Método responsável por verificar a condição para edição da aba do
	 * "Voto relator".
	 *
	 * @return <code>Boolean</code>, <code>true</code> se for permitido alterar
	 *         o voto do relator.
	 */
	public boolean podeAlterarAbaTipoVotoRelator() {
		Boolean permiteIncluirVoto = getVariavelFluxo(Variaveis.ACORDAO_PERMITE_INCLUIR_VOTO);
		boolean documentoAssinado = isDocumentoAssinado(getVoto());

		if (sessaoExistente && isOrgaosPreenchidos()) {
			return orgaoJulgadorAtual.equals(orgaoJulgadorRelator) && orgaoJulgadorVencedor.equals(orgaoJulgadorRelator)
					&& permiteIncluirVoto && !documentoAssinado;
		}

		return permiteIncluirVoto && !documentoAssinado;
	}

	/**
	 * Método responsável por verificar se os órgãos julgadores estão
	 * preenchidos
	 *
	 * @return <code>Boolean</code>, <code>true</code> se os atributos
	 *         OrgaoJulgadorAtual, OrgaoJulgadorVencedor e OrgaoJulgadorRelator
	 */
	private boolean isOrgaosPreenchidos() {
		return orgaoJulgadorAtual != null && orgaoJulgadorVencedor != null	&& orgaoJulgadorRelator != null;
	}

	/**
	 * Método responsável por verificar a condição para edição da aba do
	 * "Voto vencedor".
	 *
	 * @return <code>Boolean</code>, <code>true</code> se for permitido alterar
	 *         o voto do vencedor.
	 */
	public boolean podeAlterarAbaVotoVencedor() {
		Boolean permiteIncluirVoto = getVariavelFluxo(Variaveis.ACORDAO_PERMITE_INCLUIR_VOTO);
		boolean documentoAssinado = isDocumentoAssinado(getVotoVencedor());
		boolean retorno = false;
		
		if (isOrgaosPreenchidos()) {
			retorno = orgaoJulgadorAtual.equals(orgaoJulgadorVencedor) && !orgaoJulgadorVencedor.equals(orgaoJulgadorRelator)
					&& permiteIncluirVoto && !documentoAssinado; 
		}
		
		return retorno; 
	}
	
	public boolean podeAlterarAbaTipoVotoVencedor() {
		Boolean permiteIncluirVoto = getVariavelFluxo(Variaveis.ACORDAO_PERMITE_INCLUIR_VOTO);
		boolean documentoAssinado = isDocumentoAssinado(getVotoVencedor());
		boolean retorno = false;

		if (sessaoExistente && isOrgaosPreenchidos()) {
			retorno = orgaoJulgadorAtual.equals(orgaoJulgadorVencedor) && !orgaoJulgadorVencedor.equals(orgaoJulgadorRelator)
					&& permiteIncluirVoto && !documentoAssinado;
		}

		return retorno;
	}


	public ArrayList<String> getMsgErros() {
		return msgErros;
	}

	public void setMsgErros(ArrayList<String> msgErros) {
		this.msgErros = msgErros;
	}

	@Override
	public String recuperarModeloDocumento(String modeloDocumento) {
		selecionarModeloProcessoDocumento(modeloDocumento);
		return getModeloDocumento();
	}

	private ProcessoDocumento getProcessoDocumentoDeAcordoComAbaSelecionada(){
		ProcessoDocumento processoDocumento = null;
		switch (getAba()) {
			case ABA_ACORDAO:
				if (acordaoCompilacao.getAcordao() != null) {
					processoDocumento = acordaoCompilacao.getAcordao();
				}
				break;
			case ABA_EMENTA:
				if (acordaoCompilacao.getEmentaRelatorDoAcordao() != null) {
					processoDocumento = acordaoCompilacao.getEmentaRelatorDoAcordao().getProcessoDocumento();
				}
				break;
			case ABA_RELATORIO:
				if (acordaoCompilacao.getRelatorio() != null) {
					processoDocumento = acordaoCompilacao.getRelatorio().getProcessoDocumento();
				}
				break;
			case ABA_VOTO:
				processoDocumento = obterDocumentoVotoRelatorProcesso();
				break;
			case ABA_VOTO_VENCEDOR:
				if (acordaoCompilacao.getVotoVencedor() != null) {
					processoDocumento = acordaoCompilacao.getVotoVencedor().getProcessoDocumento();
				}
				break;
			case ABA_NOTAS_ORAIS:
				if (acordaoCompilacao.getNotasOrais() != null) {
					processoDocumento = acordaoCompilacao.getNotasOrais().getProcessoDocumento();
				}
				break;
			default:
				break;
		}

		//if (processoDocumento != null && processoDocumento.getDocumentoPrincipal() != null) {
		//	processoDocumento = processoDocumento.getDocumentoPrincipal();
		//}

		return processoDocumento;
	}

	private String getModeloDocumentoDeAcordoComAbaSelecionada(){
		return getProcessoDocumentoDeAcordoComAbaSelecionada().getProcessoDocumentoBin().getModeloDocumento();
	}

	public String getModeloDocumento() {
		if (getProtocolarDocumentoBean().getDocumentoPrincipal() != null &&
				getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin() != null) {

			return getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().getModeloDocumento();
		}

		return getModeloDocumentoDeAcordoComAbaSelecionada();
	}

	@Override
	public boolean isDocumentoAssinado() throws PJeBusinessException {
		return isDocumentoAssinado(getProcessoDocumentoDeAcordoComAbaSelecionada());
	}

	@Override
	public void removerAssinatura() {
		for(ProcessoDocumento processoDocumento : acordaoCompilacao.getProcessoDocumentosParaAssinatura()){
			if(isDocumentoAssinado(processoDocumento)){
				this.getAssinaturaDocumentoService().removeAssinatura(processoDocumento);
			}
		}
	}

	@Override
	public void descartarDocumento() throws PJeBusinessException {
		idMinuta = getProtocolarDocumentoBean().getDocumentoPrincipal().getIdProcessoDocumento();
		getProtocolarDocumentoBean().setDocumentoPrincipal(getDocumentoJudicialService().getDocumento(idMinuta));
		if (getProtocolarDocumentoBean().getDocumentoPrincipal() != null) {
			getProtocolarDocumentoBean().acaoRemoverTodos();
			salvar("");
		}
		getTramitacaoProcessualService().apagaVariavel(Variaveis.MINUTA_EM_ELABORACAO);
		getTramitacaoProcessualService().apagaVariavel(Variaveis.VARIAVEL_FLUXO_COLEGIADO_MINUTA_ACORDAO);
 		Contexts.getBusinessProcessContext().flush();
	}

	public String obterConteudoDocumentoAtual() {
		String conteudo = this.getModeloDocumento();
		String conteudoJson = "";
		try {
			conteudoJson = controleVersaoDocumentoManager.obterConteudoDocumentoJSON(conteudo);
		} catch (JSONException e) {
			logger.error(e.getMessage());
			conteudoJson = "";
		} 
		return conteudoJson;
	}

	@Override
	public void salvar(String conteudo) {
	 	getProcessoDocumentoDeAcordoComAbaSelecionada().getProcessoDocumentoBin().setModeloDocumento(conteudo);
    	gravarDocumento();
	}

	/**
	 * Recupera a transição padrão que tenha sido definida em fluxo para esta tarefa.
	 *
	 * @return a transição padrão a ser adotada
	 */
	public String getTransicaoPadrao() {
		return transicaoPadrao;
	}

	/**
	 * Carrega, a partir do fluxo, os parâmetros de configuração definidos na action.
	 * Os parâmetros serão recuperados na propriedade chave do mapa retornado de
	 * {@link #getParametrosConfiguracao()} a partir do valor da variável associada
	 * a essa chave. O valor da variável será recuperado inicialmente da tarefa e,
	 * caso seja nulo, do fluxo.
	 *
	 */
	protected void carregarParametros(){
		Map<String, String> parametros = getParametrosConfiguracao();
		if(parametros == null){
			return;
		}
		for(Entry<String, String> param: parametros.entrySet()){
			carregarParametro(param.getKey(), param.getValue());
		}
	}

	/**
	 * Carrega na propriedade dada o valor da variável de tarefa ou,
	 * se inexistente a variável de tarefa, da variável de fluxo que tem
	 * o nome dado.
	 *
	 * A atribuição do valor somente será feita se o valor da variável puder
	 * ser atribuído à propriedade. Em outras palavras, se o objeto recuperado
	 * da variável for de classe idêntica ou derivada da classe de declaração da
	 * propriedade.
	 *
	 * @param propriedade a propriedade do componente no qual o valor será gravado
	 * @param variavel o nome da variável de tarefa ou de fluxo do qual o valor será
	 * recuperado para gravação.
	 */
	protected void carregarParametro(String propriedade, String variavel){
		Field f = getField(this.getClass(), propriedade);
		if(f == null){
			return;
		}else{
			Object v = getTramitacaoProcessualService().recuperaVariavelTarefa(variavel);
			if(v == null || !f.getType().isAssignableFrom(v.getClass())){
				v = getTramitacaoProcessualService().recuperaVariavel(variavel);
			}
			if(v == null){
				if(f.getType().isAssignableFrom(Boolean.class)){
					try{
						boolean access = f.isAccessible();
						f.setAccessible(true);
						if(f.get(this) == null){
							f.set(this, false);
						}
						f.setAccessible(access);
					} catch (Throwable e) {
						logger.error("Erro ao determinar valor padrão para a propriedade booleana [{0}]: {1}", propriedade, e.getLocalizedMessage());
					}
				}
			}else if(f.getType().isAssignableFrom(v.getClass())){
				try {
					if(f.isAccessible()){
						f.set(this, v);
					}else{
						f.setAccessible(true);
						f.set(this, v);
						f.setAccessible(false);
					}
				} catch (Throwable e) {
					logger.error("Erro ao tentar carregar o valor da propriedade [{0}] a partir da variável [{1}]: {2}", propriedade, variavel, e.getLocalizedMessage());
				}
			}
		}
	}

	private void carregaTransicaoPadrao(){
		if(getTaskInstance() != null){
			Object aux = getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
			if(aux != null){
				if(aux instanceof String){
					for(Transition t: getTaskInstance().getAvailableTransitions()){
						if(t.getName().equals((String) aux)){
							transicaoPadrao = (String) aux;
							break;
						}
					}
					if(transicaoPadrao == null){
						logger.warn("O nó [{0}] tem configurada como padrão a transição de nome [{1}], mas não há transição tal disponível.",
								getTaskInstance().getName(), aux);
					}
				}else{
					logger.warn("O nó [{0}] tem configurada na variável de transição padrão valor de classe diversa de java.lang.String ([{1}]).",
							getTaskInstance().getName(), aux.getClass().getCanonicalName());
				}
			}
		}
	}

	/**
	 * Recupera o objeto {@link Field} da propriedade informada
	 * na classe dada, buscando nas classes superiores, se existente.
	 *
	 * @param clazz a classe cuja propriedade se pretende recuperar
	 * @param propriedade a propriedade a ser recuperada
	 * @return o campo ou null, se inexistente a propriedade na classe e em suas
	 * superclasses.
	 */
	private Field getField(Class<?> clazz, String propriedade){
		if(clazz == null){
			return null;
		}
		try{
			return clazz.getDeclaredField(propriedade);
		}catch(NoSuchFieldException e){
			return getField(clazz.getSuperclass(), propriedade);
		}
	}

	public boolean isAssinado() {
		return assinado;
	}

	public void setAssinado(boolean assinado) {
		this.assinado = assinado;
	}

	public List<ProcessoDocumentoBinPessoaAssinatura> getListaAssinatura() {
		return listaAssinatura;
	}

	public void setListaAssinatura(List<ProcessoDocumentoBinPessoaAssinatura> listaAssinatura) {
		this.listaAssinatura = listaAssinatura;
	}

	public boolean movimentacaoIncompleta()
	{
		return precisaTerMovimentacao() && !enventosSelecionados();
	}

	private boolean precisaTerMovimentacao()
	{
		return EventsEditorTreeHandler.instance().getRoots() != null && EventsEditorTreeHandler.instance().getRoots().size() > 0;
	}

	private boolean enventosSelecionados()
	{
		return EventsEditorTreeHandler.instance().getEventoBeanList() != null &&  EventsEditorTreeHandler.instance().getEventoBeanList().size() > 0;
	}

	public boolean isAlert() {
		return alert;
	}

	public void setAlert(boolean alert) {
		this.alert = alert;
	}

	@Override
	public boolean isDocumentoPersistido(){
		if (getProtocolarDocumentoBean().getDocumentoPrincipal() != null && getProtocolarDocumentoBean().getDocumentoPrincipal().getIdProcessoDocumento() > 0) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	public boolean isObrigatorio() {
		return obrigatorio;
	}

	public void setObrigatorio(boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	public void finalizarDocumento()	{
		if (EventsEditorTreeHandler.instance().validarMovimentacao()) {
			ProcessoHome.instance().setIdProcessoDocumento(getProtocolarDocumentoBean().getDocumentoPrincipal().getIdProcessoDocumento());
			EventsEditorTreeHandler.instance().registraEventos();
		}
		
		Integer identificadorMinutaEmElaboracao = (Integer) tramitacaoProcessualService.recuperaVariavel(Variaveis.MINUTA_EM_ELABORACAO);
		if(identificadorMinutaEmElaboracao == null) {
			identificadorMinutaEmElaboracao = (Integer) tramitacaoProcessualService.recuperaVariavel(Variaveis.VARIAVEL_FLUXO_COLEGIADO_MINUTA_ACORDAO);
		}

		if(identificadorMinutaEmElaboracao != null) {
			tramitacaoProcessualService.gravaVariavel(Variaveis.ATO_PROFERIDO, identificadorMinutaEmElaboracao);
			
			tramitacaoProcessualService.gravaVariavel(Variaveis.ULTIMO_DOCUMENTO_JUNTADO_NESTE_FLUXO, identificadorMinutaEmElaboracao);
			
			if ((assinado || (!assinado && limparDocumento.equals("1"))) &&  !limparDocumento.equals("2")) {
				getTramitacaoProcessualService().apagaVariavel(Variaveis.MINUTA_EM_ELABORACAO);
				getTramitacaoProcessualService().apagaVariavel(Variaveis.VARIAVEL_FLUXO_COLEGIADO_MINUTA_ACORDAO);
			}
		}
		ComponentUtil.getTaskInstanceHome().saidaDireta(transicaoSaida);
		Contexts.getBusinessProcessContext().flush();
	}
	
	public String getTransicaoSaida() {
		return this.transicaoSaida;
	}

	/**
	 * Recarrega o documento principal
	 *
	 * @throws PJeBusinessException
	 */
	private void carregarDocumentoPrincipal() throws PJeBusinessException {
		switch (getAba()) {
			case ABA_ACORDAO:
				getProtocolarDocumentoBean().setDocumentoPrincipal(getAcordao());
				break;
			case ABA_EMENTA:
				getProtocolarDocumentoBean().setDocumentoPrincipal(getEmenta());
				break;
			case ABA_RELATORIO:
				getProtocolarDocumentoBean().setDocumentoPrincipal(getRelatorio());
				break;
			case ABA_VOTO:
				getProtocolarDocumentoBean().setDocumentoPrincipal(getVotoRelator().getProcessoDocumento());
				this.tipoVoto = getVotoRelator() != null ? getVotoRelator().getTipoVoto() : null;
				break;
			case ABA_VOTO_VENCEDOR:
				getProtocolarDocumentoBean().setDocumentoPrincipal(getVotoVencedor());
				this.tipoVoto = acordaoCompilacao.getVotoVencedor() != null ? acordaoCompilacao.getVotoVencedor().getTipoVoto() : null;
				break;
			case ABA_NOTAS_ORAIS:
				getProtocolarDocumentoBean().setDocumentoPrincipal(getNotasOrais());
				break;
			default:
				break;
		}

		idMinuta = getProtocolarDocumentoBean().getDocumentoPrincipal().getIdProcessoDocumento();

		ProcessoDocumento documentoPrincipal = getDocumentoJudicialService().getDocumento(idMinuta);

		if(documentoPrincipal != null && documentoPrincipal.getIdProcessoDocumento() > 0){
			getProtocolarDocumentoBean().loadArquivosAnexadosDocumentoPrincipal();
			assinado = getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().getSignatarios().size() > 0;
			setTipoProcessoDocumento(getProtocolarDocumentoBean().getDocumentoPrincipal().getTipoProcessoDocumento());
			onSelectProcessoDocumentoEditorAssinatura(getTipoProcessoDocumento());
		}
	}

	public void onSelectProcessoDocumentoEditorAssinatura(TipoProcessoDocumento tipoProcessoDocumento)
	{
		this.setTipoProcessoDocumento(tipoProcessoDocumento);
		defineEstadoComponenteArvoreMovimentacoesProcessuais(tipoProcessoDocumento);
	}

	/**
	 * Define o estado do componente de arvore de movimentações processuais.
	 *
	 * @param tipoProcessoDocumento
	 */
	private void defineEstadoComponenteArvoreMovimentacoesProcessuais(TipoProcessoDocumento tipoProcessoDocumento) {
		reiniciaComponenteArvoreMovimentacoesProcessuais();

		if (tipoProcessoDocumento != null && tipoProcessoDocumento.getAgrupamento() != null){

			idAgrupamentos = tipoProcessoDocumento.getAgrupamento().getIdAgrupamento();

			if (idAgrupamentos != null && idAgrupamentos > 0){
				setRenderEventsTree(true);
				EventsEditorTreeHandler.instance().setRootsSelectedMap(new HashMap<Evento, List<Evento>>());
				EventsEditorTreeHandler.instance().getRoots(idAgrupamentos);
			}
		}
	}

	/**
	 * Reinicia o estado do componente de arvore de movimentações processuais, esvaziando sua listagem e não permitindo a sua renderização.
	 */
	private void reiniciaComponenteArvoreMovimentacoesProcessuais() {
		EventsEditorTreeHandler.instance().clearList();
		EventsEditorTreeHandler.instance().clearTree();
		setRenderEventsTree(false);
	}

	public boolean isRenderEventsTree() {
		return renderEventsTree;
	}

	public void setRenderEventsTree(boolean renderEventsTree) {
		this.renderEventsTree = renderEventsTree;
	}

	@Override
	public String obterTiposVoto() {
		RespostaDTO respostaDTO = new RespostaDTO();

		try {
			respostaDTO.setSucesso(Boolean.TRUE);

			RespostaTiposVotoDTO respostaTiposVotoDTO = new RespostaTiposVotoDTO();

			switch (getAba()) {
				case ABA_VOTO:
					respostaTiposVotoDTO.setPodeAlterar(podeAlterarAbaVotoRelator());

					if(getVotoRelator() != null) {
						TipoVoto tipoVotoRelator = getVotoRelator().getTipoVoto();
						if(tipoVotoRelator != null) {
							respostaTiposVotoDTO.setSelecao(criarTipoVotoDTO(tipoVotoRelator));
						}
					}

					respostaTiposVotoDTO.setTipos(criarListaTiposVoto(getTipoVotoManager().tiposVotosRelator()));
					break;

				case ABA_VOTO_VENCEDOR:
					respostaTiposVotoDTO.setPodeAlterar(podeAlterarAbaVotoVencedor());

					if(getDecisao() != null) {
						TipoVoto tipoVotoVencedor = getDecisao().getTipoVoto();
						if(tipoVotoVencedor != null) {
							respostaTiposVotoDTO.setSelecao(criarTipoVotoDTO(tipoVotoVencedor));
						}
					}

					respostaTiposVotoDTO.setTipos(criarListaTiposVoto(getTipoVotoManager().tiposVotosVogais()));
					break;
				default:
					break;
			}

			respostaDTO.setResposta(respostaTiposVotoDTO);
		} catch (Exception e) {
			respostaDTO.setSucesso(Boolean.FALSE);
			respostaDTO.setMensagem(e.getLocalizedMessage());
		}

		String strRetornoTiposVotoJSON = new Gson().toJson(respostaDTO, RespostaDTO.class);

		return strRetornoTiposVotoJSON;
	}

	@Override
	public String getTiposDocumentosDisponiveis() {
		JSONArray retorno = new JSONArray();

		TipoProcessoDocumento tipoProcessoDocumento = new TipoProcessoDocumento();

		try {
			switch (getAba()) {
				case ABA_ACORDAO:
					tipoProcessoDocumento = processoDocumentoHome.getTipoDocumentoAcordao();
					break;
				case ABA_EMENTA:
					tipoProcessoDocumento = processoDocumentoHome.getTipoDocumentoEmenta();
					break;
				case ABA_RELATORIO:
					tipoProcessoDocumento = getParametroUtil().getTipoProcessoDocumentoRelatorio();
					break;
				case ABA_VOTO:
					tipoProcessoDocumento = getParametroUtil().getTipoProcessoDocumentoVoto();
					break;
				case ABA_VOTO_VENCEDOR:
					tipoProcessoDocumento = getParametroUtil().getTipoProcessoDocumentoVoto();
					break;
				case ABA_NOTAS_ORAIS:
					tipoProcessoDocumento = processoDocumentoHome.getTipoDocumentoNotasOrais();
					break;
				default:
					break;
			}
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR,
					"Houve um erro de banco de dados ao tentar obter os tipos de documentos disponíveis.");
		}

		super.setTipoProcessoDocumento(tipoProcessoDocumento);

		retorno.put(tipoProcessoDocumento.getTipoProcessoDocumento());

		return retorno.toString();
	}

	/**
	 * Método responsável por regerar o conteúdo do documento do acórdão com
	 * base no modelo selecionado para esse documento
	 */
	public void regerarConteudo() {
		if (getPermiteAlterarAcordao()) {
			carregarModeloAcordao();
		}
	}

	/**
	 * Método responsável por realizar a cópia do conteúdo da ementa do relator
	 * do processo para a nova ementa que está em elaboração
	 */
	public void copiarConteudoEmentaRelator() {

		SessaoProcessoDocumento ementaRelator = getSessaoPautaProcessoTrfManager().recuperarEmentaAtivaPorSessaoEhProcessoEhOrgaoJulgador(
				sessaoJulgamento.getSessao(), sessaoJulgamento.getProcessoTrf(),
				sessaoJulgamento.getProcessoTrf().getOrgaoJulgador());

		if (ementaRelator != null && ementaRelator.getProcessoDocumento() != null
				&& ementaRelator.getProcessoDocumento().getProcessoDocumentoBin() != null) {
			acordaoCompilacao.getEmentaRelatorDoAcordao().getProcessoDocumento().getProcessoDocumentoBin()
					.setModeloDocumento(ementaRelator.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());
		}
	}

	/**
	 * Método acessor que verifica se é possível copiar o conteúdo da ementa do
	 * relator para o conteúdo da ementa atual, normalmente o magitrado que
	 * venceu a votação. Basicamente é preciso poder editar a Ementa além do
	 * Relator do acórdão ser diferente do Relator originário
	 *
	 * @return True se as regras para possibilicar a códia forem atendias ou false caso contrário.
	 */
	public Boolean getPermiteCopiarEmenta() {
		return getEditarEmenta() && acordaoCompilacao.isRelatorParaAcordaoDiferenteRelatorOriginario();
	}

	public boolean isUploadArquivoAssinadoRealizado(){
		return uploadArquivoAssinadoRealizado;
	}

	public void assinarDocumento(){
		concluirJulgamentoPjeOffice();
		limparArquivosAssinadosAnteriormente();
	}

	private void limparArquivosAssinadosAnteriormente() {
		arquivosAssinados.clear();
		uploadArquivoAssinadoRealizado = Boolean.FALSE;
	}

	public Boolean getSessaoExistente() {
		return sessaoExistente;
	}

	public void setSessaoExistente(Boolean sessaoExistente) {
		this.sessaoExistente = sessaoExistente;
	}

	private ProcessoDocumentoBinManager getProcessoDocumentoBinManager() {
		if (processoDocumentoBinManager == null) {
			processoDocumentoBinManager = ComponentUtil.getProcessoDocumentoBinManager();
		}
		return processoDocumentoBinManager;
	}

	public DocumentoJudicialService getDocumentoJudicialService() {
		if (documentoJudicialService == null) {
			documentoJudicialService = ComponentUtil.getDocumentoJudicialService();
		}
		return documentoJudicialService;
	}

	public Identity getIdentity() {
		if (identity == null) {
			identity = ComponentUtil.getIdentity();
		}
		return identity;
	}

	public ParametroUtil getParametroUtil() {
		if (parametroUtil == null) {
			parametroUtil = ComponentUtil.getParametroUtil();
		}
		return parametroUtil;
	}

	public SessaoPautaProcessoTrfManager getSessaoPautaProcessoTrfManager() {
		if (sessaoPautaProcessoTrfManager == null) {
			sessaoPautaProcessoTrfManager = ComponentUtil.getSessaoPautaProcessoTrfManager();
		}
		return sessaoPautaProcessoTrfManager;
	}

	public AssinaturaDocumentoService getAssinaturaDocumentoService() {
		if (assinaturaDocumentoService == null) {
			assinaturaDocumentoService = ComponentUtil.getAssinaturaDocumentoService();
		}
		return assinaturaDocumentoService;
	}

	public TipoProcessoDocumentoPapelService getTipoProcessoDocumentoPapelService() {
		if (tipoProcessoDocumentoPapelService == null) {
			tipoProcessoDocumentoPapelService = ComponentUtil.getTipoProcessoDocumentoPapelService();
		}
		return tipoProcessoDocumentoPapelService;
	}
	
	public TipoVoto getTipoVoto() {
		if (tipoVoto==null 
				&& decisao!=null 
				&& AbaElaboracaoAcordaoEnum.ABA_VOTO_VENCEDOR == getAba()){
			this.tipoVoto = decisao.getTipoVoto();
		}
		if (tipoVoto==null 
				&& AbaElaboracaoAcordaoEnum.ABA_VOTO == getAba() 
				&& getVotoRelator() !=null){
			this.tipoVoto = getVotoRelator().getTipoVoto();
		}
		return tipoVoto;
	}

	public void setTipoVoto(TipoVoto tipoVoto) {
		this.tipoVoto = tipoVoto;
	}

}
