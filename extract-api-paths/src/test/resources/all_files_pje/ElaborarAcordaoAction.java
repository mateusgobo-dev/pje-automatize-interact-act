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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.itx.util.EntityUtil;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.Util;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProcessoJbpmUtil;
import br.com.infox.ibpm.component.tree.EventsHomologarMovimentosTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.list.ProcessoDocumentoElaboracaoAcordaoList;
import br.com.infox.utils.Constantes.TIPO_JUSTICA;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.vo.AcordaoCompilacao;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.pje.je.enums.TipoDocumentoColegiadoEnum;
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

/**
 * Componente de controle do frame WEB-INF/xhtml/flx/elaborarAcordao.xhtml.
 * 
 */
@Name(ElaborarAcordaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ElaborarAcordaoAction extends TramitacaoFluxoAction implements Serializable, ArquivoAssinadoUploader{

	private static final long serialVersionUID = 5722152258318816865L;

	public static final String NAME = "elaborarAcordaoAction";
	public static final Context CONTEXTO = Contexts.getConversationContext();
    
    private static final int ABA_ACORDAO = 1, ABA_EMENTA = 2, ABA_RELATORIO = 3, ABA_VOTO = 4, ABA_NOTAS_ORAIS = 5, 
    		ABA_VOTO_VENCEDOR = 6, ABA_SELECAO_DOCS_ACORDAO = 7;
    
	private static final int EMENTA = 4096, RELATORIO = 8192, VOTO = 16384, NOTAS_ORAIS = 32768; 
    
    private static final Map<String, String> parametrosFluxo;
    
    private int aba;
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
    
    private ProcessoDocumentoElaboracaoAcordaoList processoDocumentoElaboracaoAcordaoList;
    
    private OrgaoJulgador orgaoJulgadorAtual;
    private OrgaoJulgador orgaoJulgadorRelator;
    private OrgaoJulgador orgaoJulgadorVencedor;
	
    static{
    	parametrosFluxo = new HashMap<String, String>();
    	parametrosFluxo.put("papeisAssinatura", Variaveis.ACORDAO_PAPEIS_ASSINATURA);
    	parametrosFluxo.put("permiteIncluirEmenta", Variaveis.ACORDAO_PERMITE_INCLUIR_EMENTA);
    	parametrosFluxo.put("permiteIncluirRelatorio", Variaveis.ACORDAO_PERMITE_INCLUIR_RELATORIO);
    	parametrosFluxo.put("permiteIncluirVoto", Variaveis.ACORDAO_PERMITE_INCLUIR_VOTO);
    	parametrosFluxo.put("permiteIncluirNotasOrais", Variaveis.ACORDAO_PERMITE_INCLUIR_NOTAS_ORAIS);
    }
    
    @Override
    public void init(){
    	super.init();
    	validaParametrosAcordao();
    	validaParametrosNotasOrais();
    	inicializarPapeis();
		iniciarAbas();
		inicializarDocumentos();
		inicializaInformacoesProcessoSelecaoAcordao();
		verificaDocumentosAcordao(false);
		obterOrgaosJulgadores();		
    }
    
    /**
     * Método chamado para inicializar as abas do frame ao abrir a página
     */
    public void iniciarAbas(){
        if (!getOcultaAcordao()) {
            setAba(ABA_ACORDAO);
        } else if (!getOcultaNotasOrais()) {
            setAba(ABA_NOTAS_ORAIS);
        } else {
            setAba(ABA_EMENTA);
        }
    }
    
    /**
     * Método responsável por realizar a verificação se o usuário logado tem permissão para 
     * assinatura dos documentos do acórdão. 
     * 
     * @return true se tem permissão
     */
    public void inicializarPapeis(){
    	String[] possiveisSignatarios = papeisAssinatura.split(",");
    	Identity identity = ComponentUtil.getIdentity();
    	for(String papel: possiveisSignatarios){
    		if(identity.hasRole(papel)){
    			podeAssinar = true;
    			break;
    		}
    	}
    }
    
	public void gravarDocumento(Integer idAba){
    	try{
	    	switch (idAba) {
	    		case ABA_ACORDAO:
	    			if(getPermiteAlterarAcordao()){
	    				ComponentUtil.getDocumentoJudicialService().persist(acordaoCompilacao.getAcordao(), true);
	    			}
					break;
				case ABA_EMENTA:
					ComponentUtil.getDocumentoJudicialService().persist(acordaoCompilacao.getEmentaRelatorDoAcordao().getProcessoDocumento(), true);
					break;
				case ABA_RELATORIO:
					ComponentUtil.getDocumentoJudicialService().persist(acordaoCompilacao.getRelatorio().getProcessoDocumento(), true);
					break;
				case ABA_VOTO:
					ProcessoDocumento pd = obterDocumentoVotoRelatorProcesso();
					
					if(!Util.isDocumentoPreenchido(pd)) {
						FacesMessages.instance().addFromResourceBundle(Severity.WARN, "erro.voto.gravarDocumentoVoto");
						return;
					}
					
					ComponentUtil.getDocumentoJudicialService().persist(pd, true);
					break;
				case ABA_VOTO_VENCEDOR:
					SessaoProcessoDocumentoVoto vra = acordaoCompilacao.getVotoRelatorDoAcordao();
					
					if(vra != null && !Util.isDocumentoPreenchido(vra.getProcessoDocumento())) {
						FacesMessages.instance().addFromResourceBundle(Severity.WARN, "erro.voto.gravarDocumentoVoto");
						return;
					}
	
					ComponentUtil.getDocumentoJudicialService().persist(vra.getProcessoDocumento(), true);
					
					if(decisao.getOjAcompanhado() == null){
						vra.setOjAcompanhado(sessaoJulgamento.getOrgaoJulgadorVencedor());
					}

					ComponentUtil.getSessaoProcessoDocumentoVotoManager().persist(vra);
					break;
				case ABA_NOTAS_ORAIS:
					ComponentUtil.getDocumentoJudicialService().persist(acordaoCompilacao.getNotasOrais().getProcessoDocumento(), true);
					break;
				default:
					break;
	    	}  	
	    	
	    	if(!getPermiteAlterarAcordao()) {
	    		ComponentUtil.getSessaoPautaProcessoTrfManager().atualizarConteudoAcordao(acordaoCompilacao.getAcordao(), acordaoCompilacao);
	    	}
	    	
	    	verificaDocumentosAcordao(false);
	    	
			LancadorMovimentosService.instance().setMovimentosTemporarios(this.getProcessIntance(), EventsHomologarMovimentosTreeHandler.instance().getEventoBeanList());
	    	
	    	ComponentUtil.getDocumentoJudicialService().flush();
	    	FacesMessages.instance().add(Severity.INFO, "Gravação realizada.");
    	}
    	catch (PJeBusinessException e){
    		FacesMessages.instance().add(Severity.ERROR, "Houve um erro ao tentar gravar o documento: {0}.", e.getLocalizedMessage());
    	}
    }
	
	private ProcessInstance getProcessIntance() {
   		TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(acordaoCompilacao.getTaskInstance().getId());
   		taskInstance.setActorId(Authenticator.getPessoaLogada().getLogin(), true);
   		
   		ProcessInstance processInstance = taskInstance.getProcessInstance();

   		return processInstance;
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
			pd = acordaoCompilacao.getVotoRelatorDoProcesso().getProcessoDocumento();
		}
		return pd;
	}
    
	/**
	 * Verifica todos os documentos do acórdão de acordo com as regras de validação 
	 * configuradas nas variáveis de tarefa.
	 */
	public void verificaDocumentosAcordao(boolean exibeMensagens){
		msgErros = ComponentUtil.getSessaoPautaProcessoTrfManager().verificaVariaveisDocumentoAcordao(acordaoCompilacao);

		if(msgErros != null && !msgErros.isEmpty()){
			this.acordaoValidado = false;
		}else{
			this.acordaoValidado = true;
		}
		
		if(!acordaoValidado && msgErros != null && exibeMensagens){
			for(String msgErro : msgErros){
				FacesMessages.instance().add(Severity.WARN, FacesUtil.getMessage("conclusaoAcordao.acordaoNaoConcluido"), msgErro);
			}
		}
	}
	
    public boolean getPermiteAlterarAcordao() {
		return ComponentUtil.getSessaoPautaProcessoTrfManager().getPermiteAlterarAcordao();
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
    		ComponentUtil.getProcessoDocumentoAcordaoManager().gravarDocumentosComposicaoAcordao(
					getProcessoDocumentoElaboracaoAcordaoList().getListaDocsSelecionados());
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			FacesMessages.instance().add(Severity.ERROR, "Não foi possível gravar documentos selecionados (" + e.getLocalizedMessage() + ").");
			return;
		}
    	
    	FacesMessages.instance().add(Severity.INFO, "Documentos para elaboração do acórdão foram gravados com sucesso.");
    }
    
    /**
     * Valida se ocorreu seleção de documentos para gravar a elaboração do acórdão.
     * 
     */
    private boolean ocorreuSelecaoDocumentosGravacaoAcordao(){
    	return getProcessoDocumentoElaboracaoAcordaoList().getListaDocsSelecionados() != null && 
    			!getProcessoDocumentoElaboracaoAcordaoList().getListaDocsSelecionados().isEmpty();
    }
    
    /**
     * Operação que recupera lista de documentos para seleção de um acórdão e que encontram-se em aberto.
     * @return
     */
    @SuppressWarnings("boxing")
	public List<ProcessoDocumentoAcordao> getDocumentosParaAcordaoEmAberto(){
    	if(getProcessoJudicial() != null){
    		return ComponentUtil.getProcessoDocumentoAcordaoManager().recuperarDocumentosParaAcordaoEmAberto(getProcessoJudicial().getIdProcessoTrf());
    	}
    	return Collections.emptyList();
    }
    
    /**
     * Operação que marca a seleção da aba de documentos da elaboração do acórdão e
     * que carrega as informações iniciais da página.
     */
    @SuppressWarnings({ "boxing"})
	public void acionarSelecaoDocumentosElaboracaoAcordao(){
    	setAba(ABA_SELECAO_DOCS_ACORDAO);
    	if(!getProcessoDocumentoElaboracaoAcordaoList().getInicializado()){
    		extrairListaInicialSelecaoDocumentosElaboracaoAcordao();
    		getProcessoDocumentoElaboracaoAcordaoList().setInicializado(Boolean.TRUE);
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
			getProcessoDocumentoElaboracaoAcordaoList().getListaDocsSelecionados().add(processoDocumentoAcordao.getProcessoDocumento());
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
    	List<ProcessoDocumento> documentosEmAbertoTela = getProcessoDocumentoElaboracaoAcordaoList().getListaDocsSelecionados();
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
		Object parametro = ComponentUtil.getTramitacaoProcessualService().recuperaVariavelTarefa(
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
    	String variavel = (String)ComponentUtil.getTramitacaoProcessualService().recuperaVariavelTarefa("tiposDisponiveisParaSelecaoIds");
    	if(variavel != null){
    		retorno = extrairIdsVariavelTiposDocElaboracaoAcordao(variavel);
    	} else {
    		if( this.processoJudicial != null ) {
    			List<ProcessoDocumento> procDocs = ComponentUtil.getProcessoDocumentoManager().recuperaDocumentosJuntados(this.processoJudicial, this.processoJudicial.getDataDistribuicao());
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
    	String strIdTipoDocAcordao = ComponentUtil.getParametroService().valueOf(Parametros.TIPODOCUMENTOACORDAO);
    	if(strIdTipoDocAcordao != null && !strIdTipoDocAcordao.trim().isEmpty()){
    		TipoProcessoDocumento tipoDocAcordao = new TipoProcessoDocumento();
    		tipoDocAcordao.setIdTipoProcessoDocumento(Integer.valueOf(strIdTipoDocAcordao));
    		ProcessoDocumento acordao = ComponentUtil.getProcessoDocumentoManager().getUltimoProcessoDocumento(tipoDocAcordao, getProcessoJudicial().getProcesso());
    		if(acordao != null){
    			getProcessoDocumentoElaboracaoAcordaoList().setDataUltimoAcordao(acordao.getDataJuntada());
    		}
    	}
    }
    
    /**
     * Operação que adiciona o documento selecioado à lista e atualiza documento processado.
     * 
     * @param processoDocumento
     */
    public void selecionarDocElaboracaoAcordao(ProcessoDocumento processoDocumento){
    	if (!getProcessoDocumentoElaboracaoAcordaoList().getListaDocsSelecionados().contains(processoDocumento)){
    		getProcessoDocumentoElaboracaoAcordaoList().selecionarDocumento(processoDocumento);
    	}
    	setVisualizacaoDocsSelecionadosHabilitada(Boolean.FALSE);
    }
    
    /**
     * Operação que adiciona o documento selecioado à lista e atualiza documento processado.
     * 
     * @param processoDocumento
     */
    public void removerDocElaboracaoAcordao(ProcessoDocumento processoDocumento){
    	getProcessoDocumentoElaboracaoAcordaoList().removerDocumento(processoDocumento);
    	setVisualizacaoDocsSelecionadosHabilitada(Boolean.FALSE);
    }
    
    /**
     * Operação que adiciona o documento selecioado à lista e atualiza documento processado.
     * 
     * @param processoDocumento
     */
    public void removerTodosDocElaboracaoAcordao(){
    	getProcessoDocumentoElaboracaoAcordaoList().removerTodosDocumento();
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
				ComponentUtil.getModeloDocumentoManager().obtemConteudo(getModeloAcordaoDocsSelecionados()));
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

    		concluirJulgamentoExterno();
    		
			FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("conclusaoAcordao.acordaoConcluido"));
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("conclusaoAcordao.acordaoNaoConcluido"), e.getLocalizedMessage());
    	}
    }
    
    public void concluirJulgamentoExterno() throws Exception {
    	ComponentUtil.getSessaoPautaProcessoTrfManager().compilarAcordao(acordaoCompilacao);
		fecharDocumentosSelecionadosElaboracaoAcordaoEmAberto();
		ComponentUtil.getProcessoDocumentoAcordaoManager().fecharDocumentosParaAcordaoEmAberto(
				getProcessoJudicial().getIdProcessoTrf(), getAcordaoCompilacao().getAcordao());

		TaskInstanceHome tih = TaskInstanceHome.instance();
		tih.verificarEhRecuperarProximaTarefa();
	}
    
	public void concluirJulgamentoPjeOffice(){
    	try {
			if(arquivosAssinados != null && !arquivosAssinados.isEmpty()){
				ComponentUtil.getDocumentoJudicialService().gravarAssinaturaDeProcessoDocumento(arquivosAssinados, acordaoCompilacao.getProcessoDocumentosParaAssinatura());
			}
			concluirJulgamento();
	    } catch (Exception e) {
	    	e.printStackTrace();
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
    	ComponentUtil.getProcessoDocumentoAcordaoManager().fecharDocumentosParaAcordaoEmAberto(
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
 				ModeloDocumento modeloDocumento = ComponentUtil.getModeloDocumentoManager().findById(getModelo().getIdModeloDocumento());
 				getVotoVencedor().getProcessoDocumentoBin().setModeloDocumento(ComponentUtil.getModeloDocumentoManager().obtemConteudo(modeloDocumento));
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
			modelos = ComponentUtil.getModeloDocumentoManager().recuperaModelosPorTipoProcessoDocumento(ComponentUtil.getParametroUtil().getTipoProcessoDocumentoVoto());
		}
		return modelos;
	}
	
    /**
     * @return ProcessoTrf corrente
     */
    @Override
	public ProcessoTrf getProcessoJudicial() {
    	if(processoJudicial == null){
    		processoJudicial = ComponentUtil.getTramitacaoProcessualService().recuperaProcesso();
    	}
        return processoJudicial;
    }

    /**
     * Método executado para mudar para a aba de Relatório e carregar o documento
     */
    public void carregarRelatorio() {
        this.tipoDocumento = TipoDocumentoColegiadoEnum.REL;
        ComponentUtil.getSessaoProcessoDocumentoHome().carregarDocumentosAcordao('R');
    }

    /**
     * Método executado para mudar para a aba de Voto e carregar o documento
     */
    public void carregarVoto() {
        this.tipoDocumento = TipoDocumentoColegiadoEnum.VOT;
        ComponentUtil.getSessaoProcessoDocumentoHome().carregarDocumentosAcordao('V');
    }

    /**
     * Método executado para mudar para a aba de VotoVencedor e carregar o documento
     */
    public void carregarVotoVencedor() {
        this.tipoDocumento = TipoDocumentoColegiadoEnum.VOT_VENC;
        ComponentUtil.getSessaoProcessoDocumentoHome().carregarDocumentosAcordao('v');
    }

    /**
     * Método executado para mudar para a aba de Ementa e carregar o documento
     */
    public void carregarEmenta() {
        this.tipoDocumento = TipoDocumentoColegiadoEnum.EME;
        ComponentUtil.getSessaoProcessoDocumentoHome().carregarDocumentoEmenta('E');
    }

    /**
     * Método executado para mudar para a aba de NotasOrais e carregar o documento
     */
    public void carregarNotasOrais() {
        this.tipoDocumento = TipoDocumentoColegiadoEnum.NOT_ORA;
        ComponentUtil.getSessaoProcessoDocumentoHome().carregarDocumentoEmenta('N');
    }

    /**
     * Verifica a existência de parâmetros necessários para as Notas Orais
     */
    private void validaParametrosNotasOrais() {
    	if(!getOcultaNotasOrais()){
	        // verifica a existência do parametro com o codigo do Tipo Documento de Notas Orais
	        String idTipoDocumento = ComponentUtil.getParametroService().valueOf(Parametros.ID_TIPO_DOCUMENTO_NOTAS_ORAIS);
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
		String idParametro = ComponentUtil.getParametroService().valueOf(Parametros.ID_TIPO_PROCESSO_DOCUMENTO_ACORDAO);
		TipoProcessoDocumento tpd = ComponentUtil.getTipoProcessoDocumentoManager().findByCodigoTipoProcessoDocumento(idParametro);
		return ComponentUtil.getModeloDocumentoLocalManager().getModeloDocumentoPorTipo(tpd);
	}

    /**
     * 
     * @return Lista de ModeloDocumento para o TipoProcessoDocumento "Notas Orais"
     * @throws PJeBusinessException Exceção de negócio
     */
    public List<ModeloDocumento> getModelosDocumentoNotasOrais() throws PJeBusinessException {
        // obtem o parametro com o codigo do TipoProcessoDocumento Notas Orais
        String idParametro = ComponentUtil.getParametroService().valueOf(Parametros.ID_TIPO_DOCUMENTO_NOTAS_ORAIS);

        TipoProcessoDocumento tpd = ComponentUtil.getTipoProcessoDocumentoManager().findByCodigoTipoProcessoDocumento(idParametro);
        return ComponentUtil.getModeloDocumentoLocalManager().getModeloDocumentoPorTipo(tpd);
    }
    
    /**
     * Método executado para mudar para a aba de Acórdao e carregar o documento
     */
    public void carregarAcordao() throws PJeBusinessException {
        this.tipoDocumento = TipoDocumentoColegiadoEnum.ACO;
        ComponentUtil.getSessaoProcessoDocumentoHome().carregarDocumentosAcordao('A');
    }
    
    /**
     * Verifica a existência de parâmetros necessários para geração do acórdão
     */
    private void validaParametrosAcordao() {
        // verifica a existência do parametro com o codigo do Modelo de Documento do Acórdão
        String idModelo = ComponentUtil.getParametroService().valueOf(Parametros.ID_MODELO_DOCUMENTO_ACORDAO);
        if (idModelo == null) {
            throw new AplicationException("Não foi possível encontrar o parâmetro: " + Parametros.ID_MODELO_DOCUMENTO_ACORDAO + "! O mesmo não está configurado.");
        }

        // verifica a existência do parametro com o codigo do Modelo de Documento do Inteiro Teor
        idModelo = ComponentUtil.getParametroService().valueOf(Parametros.ID_MODELO_DOCUMENTO_INTEIRO_TEOR);
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
            String idModelo = ComponentUtil.getParametroService().valueOf(Parametros.ID_MODELO_DOCUMENTO_ACORDAO);
            ModeloDocumento modeloAcordao = ComponentUtil.getModeloDocumentoManager().findById(new Integer(idModelo));
            conteudoDocumentoAcordao = ComponentUtil.getModeloDocumentoManager().obtemConteudo(modeloAcordao);
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
        return ComponentUtil.getProcessoDocumentoManager().liberaCertificacao(ComponentUtil.getProcessoDocumentoHome(true).getInstance()) != null;
    }
    
    /**
     * 
     * @return Conteúdo da Proclamação da Decisão
     */
    public String getProclamacaoDecisao() {
        return ComponentUtil.getSessaoProcessoDocumentoHome().getProclamacaoDecisao();
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
			Boolean existeVariavelFluxoDefinida = (Boolean) ComponentUtil.getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.VARIAVEL_SELECIONAR_MODELOS_ACORDAO);
			selecionarModelosAcordao = existeVariavelFluxoDefinida != null ? existeVariavelFluxoDefinida : false;
		}
		return selecionarModelosAcordao;
	}

    /**
     * @return the ocultaNotasOrais
     */
    public Boolean getOcultaNotasOrais() {
    	if(ocultaNotasOrais == null){
            Boolean existeVariavelOcultaNotasOrais = (Boolean) ComponentUtil.getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.VARIAVEL_OCULTA_NOTAS_ORAIS);
            ocultaNotasOrais = existeVariavelOcultaNotasOrais != null ? existeVariavelOcultaNotasOrais : true;
    	}
        return ocultaNotasOrais;
    }

    /**
     * @return the ocultaAcordao
     */
    public Boolean getOcultaAcordao() {
    	if(ocultaAcordao == null){
            Boolean existeVariavelOcultaAcordao = (Boolean) ComponentUtil.getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.VARIAVEL_OCULTA_ACORDAO);
            ocultaAcordao = existeVariavelOcultaAcordao != null ? existeVariavelOcultaAcordao : false;
    	}
        return ocultaAcordao;
    }

    public Boolean getVencedorDiverso(){
    	if(vencedorDiverso == null && sessaoJulgamento != null){
        	vencedorDiverso = !sessaoJulgamento.getOrgaoJulgadorVencedor().equals( sessaoJulgamento.getOrgaoJulgadorRelator() != null ? 
        			sessaoJulgamento.getOrgaoJulgadorRelator() : processoJudicial.getOrgaoJulgador());
    	}
    	return vencedorDiverso;
    }
    
	public int getAba(){
		return aba;
	}

	public void setAba(int aba) {
		this.aba = aba;
	}
	
	public ProcessoDocumento getEmenta(){
		return acordaoCompilacao != null && acordaoCompilacao.getEmentaRelatorDoAcordao() != null ? acordaoCompilacao.getEmentaRelatorDoAcordao().getProcessoDocumento() : null;
	}
	
	public ProcessoDocumento getRelatorio(){
		return acordaoCompilacao != null && acordaoCompilacao.getRelatorio() != null ? acordaoCompilacao.getRelatorio().getProcessoDocumento() : null;
	}
	
	public ProcessoDocumento getVoto(){
		return acordaoCompilacao != null && acordaoCompilacao.getVotoRelatorDoProcesso() != null ? acordaoCompilacao.getVotoRelatorDoProcesso().getProcessoDocumento() : ComponentUtil.getDocumentoJudicialService().getDocumento();
	}
	
	public ProcessoDocumento getVotoVencedor(){
		return acordaoCompilacao != null && acordaoCompilacao.getVotoRelatorDoAcordao() != null ? acordaoCompilacao.getVotoRelatorDoAcordao().getProcessoDocumento() : null;
	}
	
	public ProcessoDocumento getVencedor(){
		return getVotoVencedor();
	}
	
	public ProcessoDocumento getNotasOrais() throws PJeBusinessException {
		if (acordaoCompilacao.getNotasOrais() == null) {
			acordaoCompilacao.setNotasOrais(criarDocumento(sessaoJulgamento, NOTAS_ORAIS));
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
			sb.append(ComponentUtil.getDocumentoJudicialService().getDownloadLinks(docsParaAssinatura, true));
			}
		return sb.toString();
		}
	
	/**
	 * Metodo que carrega o modelo de acórdão no editor, com as variaveis de EL já processadas.
	 */
	public void carregarModeloAcordao(){
		try {
			ComponentUtil.getAcordaoModelo().setAcordaoCompilacao(acordaoCompilacao);
			
			ModeloDocumento modeloDocumento = ComponentUtil.getProcessoDocumentoHome(true).getModeloDocumentoCombo();
			if (modeloDocumento == null){
				modeloDocumento = ComponentUtil.getSessaoPautaProcessoTrfManager().getModeloDocumentoAcordao();
			}
			
			String conteudoAcordao = ComponentUtil.getModeloDocumentoManager().obtemConteudo(modeloDocumento);
			
			if(conteudoAcordao != null){
				acordaoCompilacao.getAcordao().getProcessoDocumentoBin().setModeloDocumento(conteudoAcordao);
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
		List<SessaoProcessoDocumento> listSessaoProcessoDocumento = ComponentUtil.getSessaoProcessoDocumentoManager().recuperaElementosJulgamento(processoJudicial, null, orgaoJulgadorVencedor);
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
		return ComponentUtil.getAssinaturaDocumentoService().isProcessoDocumentoAssinado(doc);
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
			removerAssinaturaProcessoDocumento(processoDocumentoVotoSelecionado);
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
			ComponentUtil.getDocumentoJudicialService().finalizaDocumento(getProcessoDocumentoVotoSelecionado(), processoJudicial, taskInstance.getId(), false, true, false, Authenticator.getPessoaLogada(), false);
			ComponentUtil.getDocumentoJudicialService().flush();
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
		
		SessaoPautaProcessoTrf sppt = ComponentUtil.getSessaoPautaProcessoTrfManager().getSessaoPautaProcessoTrfJulgado(ProcessoJbpmUtil.getProcessoTrf());
        		
		if (sppt == null) {
			throw new RuntimeException("Não foi possível recuperar a sessão de julgamento do processo!");
		}
		
		try {
			ComponentUtil.getSessaoPautaProcessoTrfManager().atualizaSessaoProcessoDocumentos(sppt.getProcessoTrf(), sppt.getSessao());
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
			return;
		}
		
		ComponentUtil.getSessaoPautaProcessoTrfHome(true).setInstanciaParaFluxo();
		
		acordaoCompilacao = ComponentUtil.getSessaoPautaProcessoTrfManager().recuperarAcordaoCompilacao(sppt, taskInstance);
						
		sessaoJulgamento = acordaoCompilacao.getSessaoPautaProcessoTrf();
		
		if(sessaoJulgamento == null || sessaoJulgamento.getIdSessaoPautaProcessoTrf() == 0){
			sessaoJulgamento = null;
			return;
		}

		julgado = ComponentUtil.getSessaoProcessoDocumentoManager().existeAcordaoPendente(getProcessoJudicial(), sessaoJulgamento);

		if(julgado){
			return;
		}
		
		inicializaRelatorio();
		inicializaEmentaDoRelatorDoAcordao();
		inicializaNotasOrais();
		
		if(isHabilitarMultiDocs()){
			inicializarDocumentosVotoOrgaoJulgadorVencedor();
		}else{
			inicializaVotoDoRelatorDoProcesso();
		}
		
		if (acordaoCompilacao.isRelatorParaAcordaoDiferenteRelatorOriginario()) {
			inicializaVotoDoRelatorDoAcordao();	
		}else{
			decisao = ComponentUtil.getSessaoProcessoDocumentoVotoManager().recuperarVoto(sppt.getSessao(), sppt.getProcessoTrf(), sppt.getProcessoTrf().getOrgaoJulgador()); 
		}
		
		carregarMapaAssinaturas();
	}

	/**
	 * Método responsável por verificar se permiti a inclusão de um relatório no acórdão e se não
	 * existir um e for obrigatório, cria-se um vazio.
	 * 
	 */
	private void inicializaRelatorio() {

		if (permiteIncluirRelatorio && acordaoCompilacao.getRelatorio() == null) {
			try {			
				acordaoCompilacao.setRelatorio(criarDocumento(acordaoCompilacao.getSessaoPautaProcessoTrf(), RELATORIO));
			}
			catch (PJeBusinessException e) {
				e.printStackTrace();
				FacesMessages.instance().add(Severity.ERROR, "Erro ao criar um novo relatório, mensagem interna: " + e.getMessage());
			}
		}
	}

	/**
	 * Método responsável por verificar se permiti a inclusão do voto do relator do processo no acórdão e se não
	 * existir um e for obrigatório, cria-se um vazio.
	 * 
	 */
	private void inicializaVotoDoRelatorDoProcesso() {

		if (permiteIncluirVoto && acordaoCompilacao.getVotoRelatorDoProcesso() == null) {
			try {
				SessaoProcessoDocumento spd = criarDocumento(acordaoCompilacao.getSessaoPautaProcessoTrf(), VOTO);
			
				acordaoCompilacao.getVotos().add((SessaoProcessoDocumentoVoto) spd);
			}
			catch (PJeBusinessException e) {
				e.printStackTrace();
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
			spd = criarDocumento(acordaoCompilacao.getSessaoPautaProcessoTrf(), VOTO);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			FacesMessages.instance().add(Severity.ERROR, "Erro ao criar um novo voto, mensagem interna: " + e.getMessage());
		}
		return spd;
	}

	private void inicializaEmentaDoRelatorDoAcordao() {

		// Se nao existir uma ementa e o fluxo permite criar entao cria uma nova
		if (permiteIncluirEmenta && acordaoCompilacao.getEmentaRelatorDoAcordao() == null) {			
			try {
				acordaoCompilacao.setEmentaRelatorDoAcordao(criarDocumento(sessaoJulgamento, EMENTA));
			}
			catch (PJeBusinessException e) {
				e.printStackTrace();
				FacesMessages.instance().add(Severity.ERROR, "Erro ao criar uma nova ementa, mensagem interna: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Método responsável por realizar a cópia do conteúdo da ementa do relator
	 * do processo para a nova ementa que está em elaboração
	 */
	public void copiarConteudoEmentaRelator() {
		
		SessaoProcessoDocumento ementaRelator = ComponentUtil.getSessaoPautaProcessoTrfManager().recuperarEmentaAtivaPorSessaoEhProcessoEhOrgaoJulgador(
				sessaoJulgamento.getSessao(), sessaoJulgamento.getProcessoTrf(),
				sessaoJulgamento.getProcessoTrf().getOrgaoJulgador());
		
		if (ementaRelator != null && ementaRelator.getProcessoDocumento() != null
				&& ementaRelator.getProcessoDocumento().getProcessoDocumentoBin() != null) {
			acordaoCompilacao.getEmentaRelatorDoAcordao().getProcessoDocumento().getProcessoDocumentoBin()
					.setModeloDocumento(ementaRelator.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());
		} 
	}

	private void inicializaVotoDoRelatorDoAcordao() {

		// Se nao existir o voto do relator do acórdao entao cria um novo
		if (acordaoCompilacao.getVotoRelatorDoAcordao() == null) {
			try {
				SessaoProcessoDocumentoVoto vrp = acordaoCompilacao.getVotoRelatorDoProcesso();
				
				SessaoProcessoDocumentoVoto vra = getVotoVencedor_(sessaoJulgamento, vrp != null ? vrp.getProcessoDocumento() : null);
				
				acordaoCompilacao.getVotos().add(vra);
			}
			catch (PJeBusinessException e) {
				e.printStackTrace();
				FacesMessages.instance().add(Severity.FATAL, "Não foi possível preparar o documento do voto do relator do acórdão, mensagem interna: " + e.getMessage());
			}
		}
			
		decisao = acordaoCompilacao.getVotoRelatorDoAcordao();		
	}
	
	private void inicializaNotasOrais() {

		// Se nao existir uma ementa e o fluxo permite criar entao cria uma nova
		if (permiteIncluirNotasOrais && acordaoCompilacao.getNotasOrais() == null) {			
			try {
				acordaoCompilacao.setNotasOrais(criarDocumento(sessaoJulgamento, NOTAS_ORAIS));
			}
			catch (PJeBusinessException e) {
				e.printStackTrace();
				FacesMessages.instance().add(Severity.ERROR, "Erro ao criar uma nova ementa, mensagem interna: " + e.getMessage());
			}
		}		
	}

	private void carregarMapaAssinaturas(){
		List<SessaoProcessoDocumentoVoto> votos = acordaoCompilacao.getVotosSemVotoRelatorParaAcordao();
		for(SessaoProcessoDocumento spd: votos){
			mapaAssinaturas.put(spd, ComponentUtil.getDocumentoJudicialService().validaAssinaturasDocumento(spd.getProcessoDocumento(), true, false));
		}
	}
		
	private SessaoProcessoDocumentoVoto getVotoVencedor_(SessaoPautaProcessoTrf sessaoJulgamento, ProcessoDocumento votoRelator) throws PJeBusinessException{
		SessaoProcessoDocumentoVoto ret = ComponentUtil.getSessaoProcessoDocumentoVotoManager().recuperarVoto(sessaoJulgamento.getSessao(), sessaoJulgamento.getProcessoTrf(), sessaoJulgamento.getOrgaoJulgadorVencedor());
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
		ComponentUtil.getSessaoProcessoDocumentoVotoManager().persist(ret);
		return ret;
	}
	
	private ProcessoDocumento getNovoDocumento(ProcessoTrf processo, TipoProcessoDocumento tipo) throws PJeBusinessException{
		ProcessoDocumento ret = ComponentUtil.getDocumentoJudicialService().getDocumento();
		ret.setTipoProcessoDocumento(tipo);
		ret.setProcessoDocumento(ret.getTipoProcessoDocumento().getTipoProcessoDocumento());
		ret.setProcesso(processo.getProcesso());
		ret.setProcessoTrf(processo);
		ret.getProcessoDocumentoBin().setModeloDocumento(" ");
		return ComponentUtil.getDocumentoJudicialService().persist(ret, true);
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
	
	private SessaoProcessoDocumento criarDocumento(SessaoPautaProcessoTrf julgamento, Integer tipoDocumento) throws PJeBusinessException{
		TipoProcessoDocumento tipo = null;
		switch (tipoDocumento) {
		case EMENTA:
			tipo = ParametroUtil.instance().getTipoProcessoDocumentoEmenta();
			break;
		case RELATORIO:
			tipo = ParametroUtil.instance().getTipoProcessoDocumentoRelatorio();
			break;
		case VOTO:
			tipo = ParametroUtil.instance().getTipoProcessoDocumentoVoto();
			break;
		case NOTAS_ORAIS:
			tipo = ParametroUtil.instance().getTipoProcessoDocumentoNotasOrais();
			break;
		default:
			throw new PJeBusinessException("Tipo de documento novo não suportado");
		}
		ProcessoDocumento doc = getNovoDocumento(julgamento.getProcessoTrf(), tipo);
		
		if(tipoDocumento.equals(VOTO)){
			SessaoProcessoDocumentoVoto ret = new SessaoProcessoDocumentoVoto();
			ret.setLiberacao(true);
			ret.setOrgaoJulgador(julgamento.getProcessoTrf().getOrgaoJulgador());
			ret.setSessao(julgamento.getSessao());
			ret.setProcessoDocumento(doc);
			ret.setOjAcompanhado(julgamento.getProcessoTrf().getOrgaoJulgador());
			ret.setProcessoTrf(julgamento.getProcessoTrf());
			return ComponentUtil.getSessaoProcessoDocumentoManager().persist(ret);
		}
		else{
			SessaoProcessoDocumento ret = new SessaoProcessoDocumento();
			ret.setLiberacao(true);
			ret.setOrgaoJulgador(julgamento.getOrgaoJulgadorVencedor());
			ret.setSessao(julgamento.getSessao());
			ret.setProcessoDocumento(doc);
			return ComponentUtil.getSessaoProcessoDocumentoManager().persist(ret);
		}
		
	}
	
	public Boolean getJulgado() {
		return julgado;
	}
	
	public Map<SessaoProcessoDocumento, List<ProcessoDocumentoBinPessoaAssinatura>> getMapaAssinaturas() {
		return mapaAssinaturas;
	}

	@Override
	protected Map<String, String> getParametrosConfiguracao() {
		return parametrosFluxo;
	}
	
	public ModeloDocumento getModelo() {
		return modelo;
	}
	
	public void setModelo(ModeloDocumento modelo) {
		this.modelo = modelo;
	}
	
	public boolean isPodeAssinar() {
		return podeAssinar && isApresentaBotaoAssinar();
	}
	
	/**
 	 * Metodo que verifica se  obrigatrio a assinatura pelo tipo processo documento 
 	 * e papel do usurio logado
 	 * @return boolean
 	 */
 	public boolean isApresentaBotaoAssinar(){
 		return !ComponentUtil.getTipoProcessoDocumentoPapelService().verificarExigibilidadeNaoAssina(
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
			Boolean existeVariavelHabilitarMultiDocs = (Boolean) ComponentUtil.getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.VARIAVEL_HABILITAR_MULTI_DOCS);
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
			justicaEleitoral = TIPO_JUSTICA.ELEITORAL.equalsIgnoreCase(ComponentUtil.getParametroUtil().instance().getTipoJustica());
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
			relator = Authenticator.getOrgaoJulgadorAtual() != null 
						? Authenticator.getOrgaoJulgadorAtual().equals(ComponentUtil.getSessaoPautaProcessoTrfHome(true).getInstance().getOrgaoJulgadorRelator())
						: false;
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
			Boolean existeVarSomenteLeitura = (Boolean) ComponentUtil.getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.ACORDAO_EDITOR_SOMENTE_LEITURA);
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
	 * Metodo acessor que verifica se pode editar a nota oral, levando em conta a variavel de somente leitura e 
	 * a variavel de permissao para inclusao de nota oral, alem de verificar a data de juntada.
	 * 
	 * @return True se as regras de edicao forem atendidas
	 */
	public Boolean getEditarNotasOrais() throws PJeBusinessException {
		if (editarNotasOrais == null) {			
			editarNotasOrais = isEdicaoDocumento(Variaveis.ACORDAO_PERMITE_INCLUIR_NOTAS_ORAIS, 
					!ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(getNotasOrais()));
		}
		return editarNotasOrais;
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
		Boolean variavel = (Boolean) ComponentUtil.getTramitacaoProcessualService().recuperaVariavelTarefa(variavelFluxo);
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
			isAssinado = ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(processoDocumento);
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
		return !getJusticaEleitoral() && (isEmentaInexistente || !ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(getEmenta()));
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
		if (getVencedorDiverso()) {
			retorno = isExibeAba(getVoto(), false, getVariavelFluxo(Variaveis.ACORDAO_VOTO_RELATOR_PROCESSO_NAO_OBRIGATORIO));
		}else {
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
		return getVencedorDiverso() && isExibeAba(getVotoVencedor(), getEditarVotoVencedor(), getVariavelFluxo(Variaveis.ACORDAO_VOTO_VENCEDOR_NAO_OBRIGATORIO));
	}
	
	/**
	 * Método responsável por retornar voto do relator do processo.
	 * @return <code>SessaoProcessoDocumentoVoto</code>, o voto do relator.
	 */
	public SessaoProcessoDocumentoVoto getVotoRelator() {
		return acordaoCompilacao.getVotoRelatorDoProcesso() != null ? 
				acordaoCompilacao.getVotoRelatorDoProcesso() : ComponentUtil.getSessaoProcessoDocumentoVotoManager().recuperarVotoRelator(ComponentUtil.getSessaoPautaProcessoTrfHome(true).getInstance());
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

	public ProcessoDocumentoElaboracaoAcordaoList getProcessoDocumentoElaboracaoAcordaoList() {
		if(this.processoDocumentoElaboracaoAcordaoList == null){
			this.processoDocumentoElaboracaoAcordaoList = ComponentUtil.getProcessoDocumentoElaboracaoAcordaoList();
		}
		return this.processoDocumentoElaboracaoAcordaoList;
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
		this.orgaoJulgadorRelator = sessaoJulgamento.getOrgaoJulgadorRelator() != null
				? sessaoJulgamento.getOrgaoJulgadorRelator() : processoJudicial.getOrgaoJulgador();
		this.orgaoJulgadorVencedor = sessaoJulgamento.getOrgaoJulgadorVencedor();
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
	
	public ArrayList<String> getMsgErros() {
		return msgErros;
	}
	
	public void setMsgErros(ArrayList<String> msgErros) {
		this.msgErros = msgErros;
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
	 * Método responsável por retirar a assinatura do documento selecionado
	 * 
	 * @param processoDocumento Documento selecionado pelo usuário
	 */
	public void removerAssinaturaProcessoDocumento(ProcessoDocumento processoDocumento){
		ComponentUtil.getAssinaturaDocumentoService().removeAssinatura(processoDocumento);
		ComponentUtil.getSessaoPautaProcessoTrfManager().atualizarEstadoAssinaturasDocumentos(getAcordaoCompilacao());
		if (ComponentUtil.getParametroUtil().getTipoProcessoDocumentoEmenta().equals(processoDocumento.getTipoProcessoDocumento())){
			editarEmenta = null;
		}
	}

	public void processarEmenta() {
		if (ProcessoDocumentoHome.instance().getModeloDocumentoCombo() != null) {
			ModeloDocumento modeloDocumento = EntityUtil.find(ModeloDocumento.class, ProcessoDocumentoHome.instance().getModeloDocumentoCombo().getIdModeloDocumento());
			String conteudoModelo = ProcessoDocumentoHome.instance().processarModelo(modeloDocumento.getModeloDocumento());
			getEmenta().getProcessoDocumentoBin().setModeloDocumento(conteudoModelo);
		}
	}
}