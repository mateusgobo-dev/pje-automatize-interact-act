package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.Length;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jbpm.graph.exe.ProcessInstance;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteVisibilidadeSigiloManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoPushManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoSegredoManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.SituacaoProcessualManager;
import br.jus.cnj.pje.nucleo.manager.VisualizadoresSigiloManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.visao.beans.ConsultaPessoaBean;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoParteVisibilidadeSigilo;
import br.jus.pje.nucleo.entidades.ProcessoPush;
import br.jus.pje.nucleo.entidades.ProcessoSegredo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoVisibilidadeSegredo;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.SituacaoProcessual;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.VisualizadoresSigilo;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.TipoNomePessoaEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Classe destinada a permitir o acesso a um processo judicial dado, mantida a conversação.
 * 
 * @author cristof
 * 
 */
@Name(ProcessoJudicialAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ProcessoJudicialAction implements Serializable{

	public static final String NAME = "processoJudicialAction";

	private static final long serialVersionUID = 5521820732051866568L;

	@Logger
	private Log logger;
	
	@RequestParameter
	private Integer id;

	@RequestParameter
	private Integer idProcesso;
	
	@In(create = false, required = false)
	private ProcessInstance processInstance;

	@In
	private transient ProcessoJudicialService processoJudicialService;
	
	@In
	private FacesMessages facesMessages;

	@In(required= false)
	private Identity identity;
	
	private ProcessoTrf processoJudicial;
	
	private boolean protocolado = false;
	
	private boolean possuiAlertas = false;
	
	private List<ProcessoParte> partesSigilosas_;
	
	private Map<Integer, List<ProcessoParteVisibilidadeSigilo>> visualizadoresPorParte;
	
	private List<ProcessoSegredo> solicitacoesSegredo;
	
	private List<ProcessoSegredo> solicitacoesSegredoPendentes;
	
	private EntityDataModel<Pessoa> possiveisVisualizadoresModel;
	
	private long numeroPartes_;
	
	private long numeroVisualizadores_;
	
	private long numeroPartesSigilosas_;
	
	private List<ProcessoVisibilidadeSegredo> visualizadores_;
	
	private boolean orgaoColegiado;
	
	private boolean visualizaSigiloso;
	
	private boolean manipulaSigiloso;
	
	private int nivelAcessoSigilo = 0;
	
	private boolean manipulaObjeto = false;
	
	private boolean incluirMotivoSegredo = false;
	
	private String motivoSegredo;
	
	private boolean exibeSolicitacoesSegredo;
	
	private boolean incluirVisualizador = false;
	
	private boolean tornarParteSigilosa_ = false;
	
	private boolean tornarParteVisivel_ = false;
	
	private int limiteExibicaoVisualizadores = 6;
	
	private int limiteExibicaoPartesSigilosas_ = 6;
	
	private String textoPesquisa;
	
	private ProtocolarDocumentoBean protocolarBean;
	
	private String objetoProcessual;
	
	private boolean exibePartesExcluidas = false;
	
	private boolean exibeSituacoes = false;
	
	private boolean exibeSituacoesAtuais = false;
	
	private List<SituacaoProcessual> situacoesAtuais;
	
	private List<SituacaoProcessual> situacoes;
	
	private List<SituacaoProcessual> situacoesAnteriores;

	private boolean haDocumentosNaoLidos = false;
	
	private UsuarioLocalizacaoMagistradoServidor localizacaoDestinoProcesso = null;
	
	private ConsultaPessoaBean consultaVisualizadorParteSigilosa = new ConsultaPessoaBean();
	
	private ProcessoParte parteIncluirVisualizador;
	
	/**
	 * Inicializa o componente, recuperando o processo judicial vinculado a partir da instância de fluxo,
	 * se existente, do parâmetro idProcesso ou do parâmetro id.
	 * 
	 */
	@Create
	public void init(){

		if (processInstance != null && processInstance.getContextInstance().getVariable(Variaveis.VARIAVEL_PROCESSO) != null){
			try{
				logger.trace("Tentando recuperar o processo judicial pela instância de fluxo.");
				processoJudicial = processoJudicialService.findByProcessInstance(processInstance);
				logger.trace("Processo judicial {0} recuperado.", processoJudicial.getNumeroProcesso());
			} catch (PJeBusinessException e){
				facesMessages.add(Severity.WARN,
						"Não foi possível obter o processo judicial vinculado à instância de processo {0}.", processInstance.getId());
				return;
			}
		}else if ((idProcesso != null) || (id != null)){
			Integer idCorreto = idProcesso != null ? idProcesso : id;
			try{
				processoJudicial = processoJudicialService.findById(idCorreto);
			} catch (PJeBusinessException e){
				facesMessages.add(Severity.WARN, "Não foi possível obter o processo judicial com id {0}.", idCorreto);
				return;
			}
		} else {
			/**
			 * Há telas que não estão em fluxo e precisam utilizar esta classe.
			 * Neste caso, essas telas colocam na sessão um parâmetro de nome 'idProcessoTrf'
			 * para neste ponto seja possível recuperá-lo e montar o objeto ProcessoTrf.			 
			 */
			try {
				if (Contexts.getPageContext().get("idProcessoTrf") != null){
					Object objectPage = Contexts.getPageContext().get("idProcessoTrf");
					idProcesso = Integer.valueOf(objectPage.toString());
					processoJudicial = processoJudicialService.findById(idProcesso);
				}else if(Util.getFromSessionContext("idProcessoTrf") != null){
					Object objectSession = Util.getFromSessionContext("idProcessoTrf");	
					idProcesso = Integer.valueOf(objectSession.toString());
					processoJudicial = processoJudicialService.findById(idProcesso);
					Util.removeFromSessionContext("idProcessoTrf");
				} else {
					logger.error("Não foi possível obter o id do processo judicial pela variável de sessão.");
					return;
				}
			} catch (NumberFormatException e) {
				facesMessages.add(Severity.WARN, "Não foi possível obter o processo judicial com id {0}.", idProcesso);
				return;
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.WARN, "Não foi possível obter o processo judicial com id {0}.", idProcesso);
				return;
			}
			
		}
		if(processoJudicial != null) {
			protocolado = processoJudicial.getProcessoStatus().equals(ProcessoStatusEnum.D);
		}
		initFlags();
		carregaContadores();
		verificaNivelAcesso();
		
		orgaoColegiado = false;
		if(!ParametroUtil.instance().isPrimeiroGrau()) {
			orgaoColegiado = false;
		}
		
		/*
		 * 	PJEII-18003
		 *  ajutando variável "processo" no fluxo, em alguns casos ela foi excluída está se
		 *  perdendo, o que acarreta em problemas nas tarefas seguintes
		 */
		if (processInstance != null && processInstance.getContextInstance().getVariable(Variaveis.VARIAVEL_PROCESSO) == null) {
			processInstance.getContextInstance().setVariable(Variaveis.VARIAVEL_PROCESSO, processoJudicial.getIdProcessoTrf());
		}
	}
	
	private void initFlags(){
		try {
			possuiAlertas = processoJudicialService.possuiAlertasAtivos(processoJudicial);
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar identificar se o processo {0} tinha alertas.", processoJudicial);
		} catch (Exception e){
			logger.error("Houve um erro ao tentar identificar se o processo {0} tinha alertas: {1}", processoJudicial, e.getLocalizedMessage());
		}
	}

	/**
	 * Recupera uma lista com os tipos de polos presentes no processo.
	 * 
	 * @return a lista de polos existentes
	 */
	public List<String> getPolos(){
		List<String> ret = new ArrayList<String>();
		if (processoJudicial.getListaPartePrincipalAtivo().size() > 0){
			ret.add("Polo ativo");
		}
		if (processoJudicial.getListaPartePrincipalPassivo().size() > 0){
			ret.add("Polo passivo");
		}
		if (processoJudicial.getListaPartePrincipal(ProcessoParteParticipacaoEnum.T).size() > 0){
			ret.add("Terceiros");
		}
		return ret;
	}

	public Pessoa obtemPessoa(ProcessoParte pp){
		return pp.getPessoa();
	}

	public Pessoa obtemPessoa(ProcessoParteRepresentante ppr){
		return ppr.getRepresentante();
	}

	public List<ProcessoParteRepresentante> getChildren(ProcessoParteRepresentante pp){
		return pp.getParteRepresentante().getProcessoParteRepresentanteList();
	}

	public List<ProcessoParteRepresentante> getChildren(ProcessoParte pp){
		return pp.getProcessoParteRepresentanteList();
	}
	
	/**
	 * Metodo que verifica se o representante esta ativo no processo.
	 * 
	 * @param ProcessoParte
	 * @return retorna uma lista de representantes ativos do processo.
	 */
	/*
	 * Método criado para correção da issue [PJE-II-2656] thiago.carvalho - 19/11/2012
	 */
	public List<ProcessoParteRepresentante> getChildrenAtivos(ProcessoParte pp) {
		List<ProcessoParteRepresentante> listaChildrenAtivos = null;
		if (pp != null && pp.getProcessoParteRepresentanteList() != null) {
			listaChildrenAtivos = new ArrayList<ProcessoParteRepresentante>(0);
			for (ProcessoParteRepresentante processoParteRepresentante : pp.getProcessoParteRepresentanteList()) {
				if (processoParteRepresentante.getInSituacao().equals(ProcessoParteSituacaoEnum.A)) {
					listaChildrenAtivos.add(processoParteRepresentante);
				}
			}
		}
		return listaChildrenAtivos;
	}
	
	/**
	 * Metodo que verifica se o representante esta ativo no processo.
	 * 
	 * @param ProcessoParteRepresentante
	 * @return retorna uma lista de representantes ativos do processo.
	 */
	/*
	 * Método criado para correção da issue [PJE-II-2656] thiago.carvalho - 20/11/2012
	 */
	public List<ProcessoParteRepresentante> getChildrenAtivos(ProcessoParteRepresentante ppr) {
		List<ProcessoParteRepresentante> listaChildrenAtivos = null;
		if (ppr != null && ppr.getParteRepresentante() != null && ppr.getParteRepresentante().getProcessoParteRepresentanteList() != null) {
			listaChildrenAtivos = new ArrayList<ProcessoParteRepresentante>(0);
			for (ProcessoParteRepresentante processoParteRepresentante : ppr.getParteRepresentante().getProcessoParteRepresentanteList()) {
				if (processoParteRepresentante.getInSituacao().equals(ProcessoParteSituacaoEnum.A)) {
					listaChildrenAtivos.add(processoParteRepresentante);
				}
			}
		}
		return listaChildrenAtivos;
	}

	public String getNomeExibicaoPolo(ProcessoParteParticipacaoEnum polo){

		String nomeExibicao = processoJudicialService.getNomeExibicaoPolo(processoJudicial, polo);
		
		if(ProcessoParteParticipacaoEnum.A.equals(polo)){
			return nomeExibicao; 
		}
		else if(ProcessoParteParticipacaoEnum.P.equals(polo)){
			return nomeExibicao;
		}
		else{
			return nomeExibicao;
		}
	}

	public String getNomeExibicaoPoloAtivo(){
		return getNomeExibicaoPolo(ProcessoParteParticipacaoEnum.A);
	}

	public String getNomeExibicaoPoloPassivo(){
		return	getNomeExibicaoPolo(ProcessoParteParticipacaoEnum.P);
	}

	public String getNomeExibicaoPoloTerceiros(){
		return getNomeExibicaoPolo(ProcessoParteParticipacaoEnum.T);
	}
	
	private void carregaContadores(){
		try {
			numeroPartes_ = processoJudicialService.contagemPartes(processoJudicial, true);
			numeroVisualizadores_ = processoJudicialService.contagemVisualizadores(processoJudicial);
			numeroPartesSigilosas_ = processoJudicialService.contagemPartesSigilosas(processoJudicial, true);
		} catch (PJeBusinessException e) {
			logger.warn("Houve um erro ao tentar recuperar os contadores do processo.");
		}
	}
	
	/**
	 * Inicializa as informações sigilosas do processo e as informações de acesso do usuário atual.
	 */
	private void verificaNivelAcesso(){
		visualizaSigiloso = false;
		manipulaSigiloso = false;
		nivelAcessoSigilo = 0;
		
		UsuarioLocalizacao loc;
		try {
			loc = ComponentUtil.getComponent(UsuarioService.class).getLocalizacaoAtual();
			if(loc != null){
				visualizaSigiloso = processoJudicialService.visivel(processoJudicial, loc, identity);
				manipulaSigiloso = processoJudicialService.manipulavel(processoJudicial, loc, identity);
				if(visualizaSigiloso || manipulaSigiloso) {
					nivelAcessoSigilo = Authenticator.recuperarNivelAcessoUsuarioLogado();
				}
			}
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Erro ao tentar inicializar os dados sigilosos.");
		}
	}
	
	/**
	 * Carrega as solicitações de segredo processual já apresentadas quanto aos presentes autos.
	 */
	private void loadSolicitacoesSegredo_(){
		try {
			solicitacoesSegredo = ComponentUtil.getComponent(ProcessoSegredoManager.class).getSolicitacoes(processoJudicial);
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar recuperar as solicitações de segredo ou sigilo para o processo.");
		}
	}
	
	/**
	 * Carrega as solicitações de segredo processual ainda não apreciadas quanto aos presentes autos.
	 */
	private void loadSolicitacoesSegredoPendentes_(){
		try {
			solicitacoesSegredoPendentes = ComponentUtil.getComponent(ProcessoSegredoManager.class).getSolicitacoesPendentes(processoJudicial);
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar recuperar as solicitações de segredo ou sigilo para o processo.");
		}
	}
	
	/**
	 * Acata uma solicitação de aplicação de segredo processual.
	 * 
	 * @param sol a solicitação a ser acatada.
	 */
	public void confirmarSolicitacaoSegredo(ProcessoSegredo sol){
		if(!manipulaSigiloso){
			facesMessages.add(Severity.ERROR, "Seu perfil não tem autorização para modificar o grau de sigilo do processo.");
			return;
		}
		try {
			processoJudicialService.confirmarSolicitacaoSegredo(sol);
			loadSolicitacoesSegredoPendentes_();
			facesMessages.add(Severity.INFO, "Solicitação de sigilo confirmada.");
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar confirmar a solicitação de segredo ou sigilo para o processo.");
		}
	}
	
	/**
	 * Recusa uma solicitação de aplicação de segredo processual.
	 * 
	 * @param sol a solicitação a ser recusada
	 */
	public void recusarSolicitacaoSegredo(ProcessoSegredo sol){
		if(!manipulaSigiloso){
			facesMessages.add(Severity.ERROR, "Seu perfil não tem autorização para modificar o grau de sigilo do processo.");
			return;
		}
		try {
			processoJudicialService.recusarSolicitacaoSegredo(sol);
			loadSolicitacoesSegredoPendentes_();
			facesMessages.add(Severity.INFO, "Solicitação de sigilo recusada.");
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar recusar a solicitação de segredo ou sigilo para o processo.");
		}
	}
	
	/**
	 * Inverte flag de tela quanto à exibição de informações necessárias à inversão do processo como sigiloso.
	 */
	public void inverterIncluirSegredo(){
		if(incluirMotivoSegredo){
			incluirMotivoSegredo = false;
		}else{
			incluirMotivoSegredo = true;
		}
	}
	
	/**
	 * Inverte flag de tela quanto à exibição de informações necessárias à inclusão de novos visualizadores de processo sigiloso.
	 */
	public void inverterIncluirVisualizador(){
		incluirVisualizador = !incluirVisualizador;
		textoPesquisa = null;
		possiveisVisualizadoresModel = null;
	}
	
	/**
	 * Inverte flag de tela quanto à exibição de informações necessárias à operação de tornar uma ou mais partes sigilosas.
	 */
	public void inverterTornarParteSigilosa(){
		if(!tornarParteSigilosa_){
			motivoSegredo = null;
		}
		tornarParteSigilosa_ = !tornarParteSigilosa_;
	}
	
	/**
	 * Inverte flag de tela quanto à exibição de informações necessárias à operação de tornar uma ou mais partes visíveis.
	 */
	public void inverterTornarParteVisivel(){
		if(!tornarParteVisivel_){
			limiteExibicaoPartesSigilosas_ = -1;
			motivoSegredo = null;
		}
		tornarParteVisivel_ = !tornarParteVisivel_;
	}

	/**
	 * Inverte flag de tela quanto à exibição de informações das solicitações de aplicação de segredo de justiça
	 */
	public void inverterExibeSolicitacoesSegredoApreciadas(){
		if(exibeSolicitacoesSegredo){
			exibeSolicitacoesSegredo = false;
			solicitacoesSegredoPendentes.clear();
			solicitacoesSegredoPendentes = null;
			solicitacoesSegredo.clear();
			solicitacoesSegredo = null;
		}else{
			exibeSolicitacoesSegredo = true;
			loadSolicitacoesSegredo_();
		}
	}
	
	public void inverterExibirPartesExcluidas(){
		exibePartesExcluidas = !exibePartesExcluidas;
	}
	
	public void inverterExibirSituacoesAtuais(){
		exibeSituacoesAtuais = !exibeSituacoesAtuais;
	}
	
	public void inverterExibirSituacoes(){
		exibeSituacoes = !exibeSituacoes;
	}
	
	/**
	 * Modifica o valor do limite de número de visualizadores visíveis em tela, invertendo de 6 para todos e vice versa.
	 */
	public void verTodosVisualizadores(){
		if(limiteExibicaoVisualizadores == -1){
			limiteExibicaoVisualizadores = 6;
		}else{
			limiteExibicaoVisualizadores = -1;
		}
		loadVisualizadores_();
	}
	
	/**
	 * Modifica o valor do limite de número de partes sigilosas visíveis em tela, invertendo de 6 para todos e vice versa.
	 */
	public void verTodasPartesSigilosas(){
		if(limiteExibicaoPartesSigilosas_ == -1){
			limiteExibicaoPartesSigilosas_ = 6;
		}else{
			limiteExibicaoPartesSigilosas_ = -1;
		}
		loadPartesSigilosas_();
	}
	
	/**
	 * Inverte o sigilo processual, ou seja, torna um processo público quando ele for sigiloso e vice-versa.
	 */
	public void inverterSigilo(){
		if(!manipulaSigiloso){
			facesMessages.add(Severity.ERROR, "Seu perfil não tem autorização para modificar o grau de sigilo do processo.");
			return;
		}
		try{
			UsuarioLogin usuarioLogin = Authenticator.getPessoaLogada();
			if(usuarioLogin != null){
				processoJudicialService.inverterSigilo(processoJudicial, usuarioLogin, motivoSegredo);
				incluirMotivoSegredo = false;
				loadSolicitacoesSegredoPendentes_();
				if(processoJudicial.getSegredoJustica()){
					// Ao tornar o processo sigiloso, alterar o nivel de acesso para 1 (Segredo de Justiça)
					alterarNivelAcessoSigilo(1);
					facesMessages.add(Severity.INFO, "O processo foi tornado sigiloso.");
					liberarVisualizacaoPartes(Boolean.TRUE);
					removeProcessoPushPessoaSemVisibilidade(processoJudicial);
				}else{
					// Ao tornar o processo publico, alterar o nivel de acesso para 0 (Zero)
					alterarNivelAcessoSigilo(0);
					facesMessages.add(Severity.INFO, "O processo foi tornado público.");
				}
				motivoSegredo = null;
				
			}
		}catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar modificar a situação de segredo ou sigilo para o processo.");
		}
	}
	
	public void removeProcessoPushPessoaSemVisibilidade(ProcessoTrf processo) throws PJeBusinessException {
		ProcessoPushManager processoPushManager = ProcessoPushManager.instance();
		List<ProcessoPush> ppList = processoPushManager.recuperarProcessoPushPorProcesso(processo);
		List<ProcessoVisibilidadeSegredo> pvsList = processo.getVisualizadores();
		List<Pessoa> pessoaVisualizadoraList = new ArrayList<Pessoa>();
		
		for(ProcessoVisibilidadeSegredo pvs : pvsList) {
			pessoaVisualizadoraList.add(pvs.getPessoa());
		}
		for(ProcessoPush pp : ppList) {
			if(pp.getPessoa() != null && !pessoaVisualizadoraList.contains(pp.getPessoa())) {
				processoPushManager.removeProcessoPush(pp);
			} else if(pp.getPessoa() == null && pp.getPessoaPush() != null){
				processoPushManager.removeProcessoPush(pp);
			}
		}
	}
	
	public void alterarNivelAcessoSigilo(int novoNivelAcesso) {
		this.processoJudicial.setNivelAcesso(novoNivelAcesso);
		adicionarVisualizadores(null, novoNivelAcesso);
		ComponentUtil.getProcessoTrfManager().update(this.processoJudicial);
	}

	public String getLabelNivelAcesso(int nivelAcesso) {
		return ComponentUtil.getProcessoTrfManager().getLabelNivelAcesso(nivelAcesso);
	}

	public String getTooltipNivelAcesso(int nivelAcesso) {
		return ComponentUtil.getProcessoTrfManager().getTooltipNivelAcesso(nivelAcesso);
	}

	/**
	 * Torna o processo visível para todos os servidores vinculados a um dado órgão julgador.
	 */
	public void liberarVisualizacaoOrgaoJulgador(){
		liberarVisualizacaoOrgao(false);
	}
	
	public void liberarVisualizacaoColegiado(){
		liberarVisualizacaoOrgao(true);
	}
	
	private void liberarVisualizacaoOrgao(boolean colegiado){
		if(!manipulaSigiloso){
			facesMessages.add(Severity.ERROR, "Seu perfil não tem autorização para modificar o grau de acesso ao processo.");
			return;
		}
		try{
			if(!processoJudicial.getSegredoJustica()){
				facesMessages.add(Severity.WARN, "A liberação de visualização somente pode acontecer para processos sigilosos.");
				return;
			}
			int cont = processoJudicialService.liberarVisualizacaoOrgaoJulgador(processoJudicial, colegiado);
			String msg = null;
			if(cont == 0){
				facesMessages.add(Severity.WARN, "Todos os servidores vinculados ao órgão já estão autorizados a visualizar o processo.");
				return;
			}else if(cont == 1){
				msg = "Um servidor vinculado ao órgão foi autorizado a visualizar o processo.";
			}else{
				msg = String.format("%d servidores vinculados ao órgão foram autorizados a visualizar este processo.", cont);
			}
			loadVisualizadores_();
			facesMessages.add(Severity.INFO, msg);
		}catch (PJeBusinessException e){
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar liberar o acesso aos servidores pertencentes ao órgão julgador.");
		}
	}
	
	public void liberarVisualizacaoPartes(){
		liberarVisualizacaoPartes(Boolean.FALSE);
	}
	
	public void liberarVisualizacaoPartes(Boolean isHabilitarSigilo){
		if(!manipulaSigiloso){
			facesMessages.add(Severity.ERROR, "Seu perfil não tem autorização para modificar o grau de acesso ao processo.");
			return;
		}
		try{
			if(!processoJudicial.getSegredoJustica()){
				facesMessages.add(Severity.WARN, "A liberação de visualização somente pode acontecer para processos sigilosos.");
				return;
			}
			int cont = processoJudicialService.liberarVisualizacaoTodasPartes(processoJudicial);
			String msg = null;
			if(cont == 0){
				facesMessages.add(Severity.WARN, "Todas as partes vinculadas estão autorizadas a visualizar este processo.");
				return;
			}else if(isHabilitarSigilo){
				msg = "Todas as partes e seus representantes foram autorizados a visualizar este processo, caso seja necessário remover a visualização de alguma das partes utilize a aba \"Segredo ou sigilo\".";
			}else if(cont == 1){
				msg = "Uma parte vinculada foi autorizada a visualizar o processo.";
			}else{
				msg = String.format("%d partes vinculadas foram autorizados a visualizar este processo.", cont);
			}
			loadVisualizadores_();
			facesMessages.add(Severity.INFO, msg);
		}catch (PJeBusinessException e){
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar liberar o acesso às partes do processo.");
		}
	}
	
	
	/**
	 * Carrega o número de visualizadores do processo judicial sigiloso, assim como esses visualizadores até 
	 * o limite definido em {@link #limiteExibicaoVisualizadores}.
	 */
	private void loadVisualizadores_(){
		try {
			numeroVisualizadores_= processoJudicialService.contagemVisualizadores(processoJudicial);
			visualizadores_ = processoJudicialService.recuperaVisualizadores(processoJudicial, null, limiteExibicaoVisualizadores);
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Não foi possível recuperar os visualizadores autorizados do processo.");
		}
	}
	
	/**
	 * Carrega o número de partes sigilosas do processo judicial, assim como essas partes até 
	 * o limite definido em {@link #limiteExibicaoPartesSigilosas_}.
	 */
	private void loadPartesSigilosas_(){
		try {
			numeroPartesSigilosas_ = processoJudicialService.contagemPartesSigilosas(processoJudicial, true);
			partesSigilosas_ = processoJudicialService.recuperaPartesSigilosas(processoJudicial, true, null, limiteExibicaoPartesSigilosas_);
			carregarVisualizadoresPartesSigilosas();
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Não foi possível a lista de partes ativas sigilosas do processo.");
		}
	}
	
	private void carregarVisualizadoresPartesSigilosas() {
		visualizadoresPorParte = new HashMap<>();
		ProcessoParteVisibilidadeSigiloManager processoParteVisibilidadeSigiloManager = 
				ComponentUtil.getComponent(ProcessoParteVisibilidadeSigiloManager.class);
		for (ProcessoParte parte : partesSigilosas_) {
			List<ProcessoParteVisibilidadeSigilo> visualizadores = processoParteVisibilidadeSigiloManager.recuperarVisualizadores(parte);
			visualizadoresPorParte.put(parte.getIdProcessoParte(), visualizadores);
		}
	}
	
	public List<ProcessoParteVisibilidadeSigilo> getVisualizadoresDaParte(ProcessoParte parte) {
		if (visualizadoresPorParte == null) {
			return Collections.emptyList();
		}
		List<ProcessoParteVisibilidadeSigilo> visualizadores = visualizadoresPorParte.get(parte.getIdProcessoParte());
		return (visualizadores != null) ? visualizadores : Collections.emptyList();
	}
	
	/**
	 * Recupera a lista de visualizadores carregada.
	 * 
	 * @return a lista de visualizadores
	 */
	public List<ProcessoVisibilidadeSegredo> getVisualizadores(){
		if(visualizadores_ == null){
			if(!processoJudicial.getSegredoJustica()){
				return Collections.emptyList();
			}else{
				loadVisualizadores_();
			}
		}
		return visualizadores_;
	}
	
	/**
	 * Método responsável por pesquisar as pessoas de acordo com a informação inserida no campo {@link #textoPesquisa}.
	 */
	public void pesquisaPessoas() {
		possiveisVisualizadoresModel = pesquisaPessoas(textoPesquisa);
	}
	
	/**
	 * Inclui a pessoa com o identificador dado na lista de visualizadores do processo sigiloso.
	 * 
	 * @param idPessoa o identificador da pessoa a ser acrescentada como visualizadora
	 */
	public void incluirVisualizador(int idPessoa){
		if(!manipulaSigiloso){
			facesMessages.add(Severity.ERROR, "Seu perfil não tem autorização para modificar o grau de acesso ao processo.");
			return;
		}
		if(idPessoa == 0){
			facesMessages.add(Severity.WARN, "Não é possível incluir como visualizador uma pessoa ainda não cadastrada no sistema.");
			return;
		}
		try{
			Pessoa p = ComponentUtil.getComponent(PessoaService.class).findById(idPessoa);
			if(p != null){
				Procuradoria procuradoria = recuperarProcuradoriaVinculada(processoJudicial, p);
				if(processoJudicialService.acrescentaVisualizador(processoJudicial, p, procuradoria)){
					facesMessages.add(Severity.INFO, "{0} acrescentado como visualizador deste processo.", p.getNome());
					loadVisualizadores_();
				}else if(processoJudicial.getSegredoJustica()){
					facesMessages.add(Severity.WARN, "{0} já consta como visualizador deste processo.", p.getNome());
				}else{
					facesMessages.add(Severity.ERROR, "Por alguma remota razão, não foi possível acrescentar {0} visualizador deste processo.", p.getNome());
				}
			}
		}catch(PJeBusinessException e){
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar incluir a pessoa como visualizador.");
		}
	}
	
	/**
	 * Remove, da lista de visualizadores de um processo sigiloso, uma dada pessoa.
	 * 
	 * @param idPessoa o identificador da pessoa a ser retirada da lista
	 */
	public void removerVisualizador(int idPessoa){
		if(!manipulaSigiloso){
			facesMessages.add(Severity.ERROR, "Seu perfil não tem autorização para modificar o grau de acesso ao processo.");
			return;
		}
		if(idPessoa == 0){
			facesMessages.add(Severity.WARN, "Não é possível tirar a qualidade de visualizador de pessoa ainda não cadastrada no sistema.");
			return;
		}
		try{
			Pessoa p = ComponentUtil.getComponent(PessoaService.class).findById(idPessoa);
			if(p != null){
				if(processoJudicialService.removeVisualizador(processoJudicial, p)){
					facesMessages.add(Severity.INFO, "{0} retirado da lista de visualizadores deste processo.", p.getNome());
					loadVisualizadores_();
					ProcessoPushManager processoPushManager = ProcessoPushManager.instance();
					processoPushManager.removeProcessoPush(processoJudicial, p);
				}else if(processoJudicial.getSegredoJustica()){
					facesMessages.add(Severity.INFO, "{0} não é visualizador deste processo.", p.getNome());
				}else{
					facesMessages.add(Severity.WARN, "Por alguma remota razão, não foi possível retirar {0} da qualidade de visualizador deste processo.", p.getNome());
				}
			}
		}catch(PJeBusinessException e){
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar incluir a pessoa como visualizador.");
		}
	}
	
	/**
	 * Torna todas as partes do processo sigilosas.
	 */
	public void tornarPartesSigilosas(){
		if(!manipulaSigiloso){
			facesMessages.add(Severity.ERROR, "Seu perfil não tem autorização para modificar o grau de sigilo do processo.");
			return;
		}
		try {
			int cont = processoJudicialService.tornarPartesSigilosas(processoJudicial, motivoSegredo, ProcessoParteParticipacaoEnum.A, ProcessoParteParticipacaoEnum.P, ProcessoParteParticipacaoEnum.T);
			if(cont == 0){
				facesMessages.add(Severity.WARN, "Não havia qualquer parte a ser tornada sigilosa no processo.");
				return;
			}else if(cont == 1){
				facesMessages.add(Severity.INFO, "Uma parte foi tornada sigilosa no processo.");
			}else{
				facesMessages.add(Severity.INFO, "{0} partes foram tornadas sigilosas no processo.", cont);
			}
			inverterTornarParteSigilosa();
			loadPartesSigilosas_();
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar tornar as partes sigilosas.");
		}
	}
	
	/**
	 * Torna todas as partes do processo visíveis.
	 */
	public void tornarPartesVisiveis(){
		if(!manipulaSigiloso){
			facesMessages.add(Severity.ERROR, "Seu perfil não tem autorização para modificar o grau de sigilo do processo.");
			return;
		}
		try {
			int cont = processoJudicialService.tornarPartesVisiveis(processoJudicial, motivoSegredo, ProcessoParteParticipacaoEnum.A, ProcessoParteParticipacaoEnum.P, ProcessoParteParticipacaoEnum.T);
			if(cont == 0){
				facesMessages.add(Severity.WARN, "Não havia qualquer parte a ser tornada visivel no processo.");
				return;
			}else if(cont == 1){
				facesMessages.add(Severity.INFO, "Uma parte foi tornada visível no processo.");
			}else{
				facesMessages.add(Severity.INFO, "{0} partes foram tornadas visíveis no processo.", cont);
			}
			inverterTornarParteVisivel();
			loadPartesSigilosas_();
			motivoSegredo = null;
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar tornar as partes sigilosas.");
		}
	}
	
	/**
	 * Torna a parte processual sigilosa.
	 * 
	 * @param idParte o identificador da parte a ser tornada sigilosa
	 */
	public void tornarParteSigilosa(int idParte){
		if(!manipulaSigiloso){
			facesMessages.add(Severity.ERROR, "Seu perfil não tem autorização para modificar o grau de sigilo do processo.");
			return;
		}
		try{
			ProcessoParte parte = processoJudicialService.recuperaParte(processoJudicial, idParte);
			if(parte == null){
				facesMessages.add(Severity.ERROR, "Não há parte acessível com o identificador {0}.", idParte);
				return;
			}
			if(processoJudicialService.tornarParteSigilosa(parte, motivoSegredo)){
				facesMessages.add(Severity.INFO, "A parte [{0}] foi tornada sigilosa.", parte.getNomeParte());
				loadPartesSigilosas_();
				motivoSegredo = null;
			}else{
				facesMessages.add(Severity.INFO, "A parte [{0}] já estava cadastrada como sigilosa.", parte.getNomeParte());
			}
		}catch (PJeBusinessException e){
			facesMessages.add(Severity.ERROR, "Erro ao tentar tornar sigilosa a parte.");
		}
	}
	
	/**
	 * Torna uma parte visível.
	 * 
	 * @param idParte o identificador da parte a ser tornada visível
	 */
	public void tornarParteVisivel(int idParte){
		if(!manipulaSigiloso){
			facesMessages.add(Severity.ERROR, "Seu perfil não tem autorização para modificar o grau de sigilo do processo.");
			return;
		}
		try{
			ProcessoParte parte = processoJudicialService.recuperaParte(processoJudicial, idParte);
			if(parte == null){
				facesMessages.add(Severity.ERROR, "Não há parte acessível com o identificador {0}.", idParte);
				return;
			}
			if(processoJudicialService.tornarParteVisivel(parte, motivoSegredo)){
				facesMessages.add(Severity.INFO, "A parte [{0}] foi tornada visível.", parte.getNomeParte());
				loadPartesSigilosas_();
				motivoSegredo = null;
			}else{
				facesMessages.add(Severity.INFO, "A parte [{0}] já estava visível.", parte.getNomeParte());
			}
		}catch (PJeBusinessException e){
			facesMessages.add(Severity.ERROR, "Erro ao tentar tornar parte visível.");
		}
	}
	
	public ProtocolarDocumentoBean getProtocolarBean() {
		Boolean criaNovoDocumento = new Boolean("FALSE");
		if(protocolarBean != null && ComponentUtil.getComponent(ProcessoDocumentoManager.class).verificarDocumentoDeAtividadeEspecifica(protocolarBean.getDocumentoPrincipal())) {
			criaNovoDocumento = true;
		}
		
		if(protocolarBean == null || criaNovoDocumento) {
			if(identity.hasRole(Papeis.INTERNO)){
				protocolarBean = new ProtocolarDocumentoBean(getProcessoJudicial().getIdProcessoTrf(), 
						ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL 
						| ProtocolarDocumentoBean.LANCAR_MOVIMENTACAO
						| ProtocolarDocumentoBean.PERMITE_SELECIONAR_MOVIMENTACAO
						| ProtocolarDocumentoBean.PERMITIR_VINCULACAO_RESPOSTA
						| ProtocolarDocumentoBean.UTILIZAR_MODELOS);
			}
			else{
				protocolarBean = new ProtocolarDocumentoBean(getProcessoJudicial().getIdProcessoTrf(), 
						ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL 
						| ProtocolarDocumentoBean.LANCAR_MOVIMENTACAO
						| ProtocolarDocumentoBean.PERMITIR_VINCULACAO_RESPOSTA);
			}
		}
		return protocolarBean;
	}
	
	public void iniciarFluxoDigitalizacao(){
		iniciarNovoFluxo(ComponentUtil.getComponent(ParametroService.class).valueOf(Parametros.CODIGO_FLUXO_DIGITALIZACAO), 
				Papeis.PODE_INICIAR_FLUXO_DIGITALIZACAO, 
				"A atividade de digitalização foi criada e está pendente de execução pelo responsável.", 
				"Houve um erro ao tentar criar a atividade de digitalização.");
	}
	
	public void iniciarNovoFluxo(String codigoFluxo, String papel, String msgSucesso, String msgErro) {
		iniciarNovoFluxo(codigoFluxo, papel, msgSucesso, msgErro, null);
	}
	
	public void iniciarNovoFluxo(String codigoFluxo, String papel, String msgSucesso, String msgErro, Map<String,Object> variaveis) {
		if(StringUtils.isNotBlank(papel) && !identity.hasRole(papel)){
			facesMessages.add(Severity.ERROR, "Você não tem autorização para iniciar a atividade.");
		} else {
			try{
				processoJudicialService.incluirNovoFluxo(getProcessoJudicial(), codigoFluxo, variaveis);
				facesMessages.add(Severity.INFO, msgSucesso);
			}catch (Throwable e){
				logger.error("Houve um erro ao tentar iniciar o fluxo de código {0}: {1}", codigoFluxo, e.getLocalizedMessage());
				facesMessages.add(Severity.ERROR, msgErro);
			}
		}
	}
	
	public Integer getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(Integer idProcesso) {
		this.idProcesso = idProcesso;
	}
	
	public boolean isProtocolado() {
		return protocolado;
	}

	public void setProtocolado(boolean protocolado) {
		this.protocolado = protocolado;
	}

	/**
	 * Recupera o processo judicial vinculado a esta action.
	 * 
	 * @return o processo judicial
	 */
	public ProcessoTrf getProcessoJudicial(){
		return processoJudicial;
	}
	
	public boolean isPossuiAlertas() {
		return possuiAlertas;
	}

	/**
	 * Recupera a lista de solicitações de aplicação de sigilo ao processo ainda pendentes de 
	 * apreciação.
	 * 
	 * @return a lista de solicitações pendentes
	 */
	public List<ProcessoSegredo> getSolicitacoesSegredoPendentes(){
		if(solicitacoesSegredoPendentes == null){
			loadSolicitacoesSegredoPendentes_();
		}
		return solicitacoesSegredoPendentes;
	}
	
	/**
	 * Recupera a lista de solicitações de aplicação de segredo de justiá ao processo.
	 * 
	 * @return a lista de solicitações
	 */
	public List<ProcessoSegredo> getSolicitacoesSegredo(){
		if(solicitacoesSegredo == null){
			loadSolicitacoesSegredo_();
		}
		return solicitacoesSegredo;
	}
	
	/**
	 * Recupera a lista de partes sigilosas já carregada.
	 * 
	 * @return a lista de partes sigilosas
	 * @see #verTodasPartesSigilosas()
	 */
	public List<ProcessoParte> getPartesSigilosas() {
		if(partesSigilosas_ == null){
			loadPartesSigilosas_();
		}
		return partesSigilosas_;
	}

	/**
	 * Indica se o usuário atual tem acesso à visualização dos dados sigilosos de um processo.
	 * 
	 * @return true, se ele tem acesso
	 */
	public boolean isVisualizaSigiloso() {
		return visualizaSigiloso;
	}
	
	/**
	 * Indica se o usuário atual tem acesso às funcionalidades de manipulação dos dados sigilosos de um processo.
	 * 
	 * @return true, se ele puder manipular tais informações
	 */
	public boolean isManipulaSigiloso() {
		return manipulaSigiloso;
	}

	/**
	 * Recupera propriedade destinada ao preenchimento de informações sobre a modificação
	 * de aspectos sigilosos do processo.
	 * 
	 * @return a propriedade
	 */
	public String getMotivoSegredo() {
		return motivoSegredo;
	}

	/**
	 * Atribui à propriedade motivoSegredo valor destinado ao preenchimento de informações sobre a modificação
	 * de aspectos sigilosos do processo.
	 * 
	 * @return o motivo ou justificativa a ser preenchido
	 */
	public void setMotivoSegredo(String motivoSegredo) {
		this.motivoSegredo = motivoSegredo;
	}
	
	/**
	 * Recupera flag de tela destinado à viabilizar a renderização de formulário pertinente à 
	 * modificação da situação de sigilo do processo.
	 * 
	 * @return true, se o formulário deve ser exibido
	 * @see #inverterIncluirSegredo()
	 */
	public boolean isIncluirMotivoSegredo() {
		return incluirMotivoSegredo;
	}
	
	/**
	 * Recupera flag de tela destinado à viabilizar a renderização da lista de solicitações de sigilo do processo.
	 * 
	 * @return true, se a lista deve ser exibida
	 * @see #inverterExibeSolicitacoesSegredoApreciadas()
	 */
	public boolean isExibeSolicitacoesSegredo() {
		return exibeSolicitacoesSegredo;
	}
	
	/**
	 * Recupera flag indicativa de que o processo está em tramitação em um órgão colegiado.
	 * 
	 * @return true, se o processo tramita em órgão colegiado
	 */
	public boolean isOrgaoColegiado() {
		return orgaoColegiado;
	}
	
	/**
	 * Recupera flag de tela indicativo de que deve ser exibido formuário para a inclusão de um visualizador.
	 *  
	 * @return true, se o formulário deve ser exibido
	 * @see #inverterIncluirVisualizador()
	 */
	public boolean isIncluirVisualizador() {
		return incluirVisualizador;
	}

	/**
	 * Recupera o valor do campo destinado à pesquisa de pessoas.
	 * 
	 * @return o valor do campo.
	 */
	public String getTextoPesquisa() {
		return textoPesquisa;
	}

	/**
	 * Atribui a um campo de pesquisa um dado valor para aproveitamento em alguma funcionalidade de tela.
	 * 
	 * @param textoPesquisa o valor a ser atribuído.
	 */
	public void setTextoPesquisa(String textoPesquisa) {
		this.textoPesquisa = textoPesquisa;
	}
	
	/**
	 * Recupera o número total de visualizadores do processo judicial.
	 * 
	 * @return o número de visualizadores
	 * @see #loadVisualizadores_()
	 * @see ProcessoJudicialService#contagemVisualizadores(ProcessoTrf)
	 */
	public long getNumeroVisualizadores() {
		return numeroVisualizadores_;
	}
	
	/**
	 * Recupera o número total de partes ativas do processo judicial.
	 * 
	 * @return o número total de partes
	 * @see ProcessoJudicialService#contagemPartes(ProcessoTrf, boolean)
	 */
	public long getNumeroPartes() {
		return numeroPartes_;
	}
	
	/**
	 * Recupera o número total de partes sigilosas do processo judicial.
	 * 
	 * @return o número total de partes sigilosas
	 * @see #loadPartesSigilosas_()
	 * @see ProcessoJudicialService#contagemPartesSigilosas(ProcessoTrf, boolean)
	 */
	public long getNumeroPartesSigilosas(){
		return numeroPartesSigilosas_;
	}
	
	/**
	 * Recupera flag de tela destinada a indicar se um formulário destinado a tornar uma parte
	 * sigilosa deve ser exibido.
	 * 
	 * @return true, se o formulário deve ser exibido
	 * @see #inverterTornarParteSigilosa()
	 */
	public boolean isTornarParteSigilosa() {
		return tornarParteSigilosa_;
	}
	
	/**
	 * Recupera flag de tela destinada a indicar se um formulário destinado a tornar uma parte
	 * visível deve ser exibido.
	 * 
	 * @return true, se o formulário deve ser exibido
	 * @see #inverterTornarParteVisivel()
	 */
	public boolean isTornarParteVisivel() {
		return tornarParteVisivel_;
	}
	
	public boolean isExibePartesExcluidas() {
		return exibePartesExcluidas;
	}
	
	/**
	 * Recupera a lista de partes que estão em situação baixado, inativo ou excluído do processo atual.
	 * 
	 * @return a lista de partes excluídas
	 */
	public List<ProcessoParte> getPartesExcluidas(){
		return processoJudicialService.getPartesExcluidas(processoJudicial);
	}
	
	public List<SituacaoProcessual> getSituacoesAtuais(){
		if(situacoesAtuais == null){
			situacoesAtuais = ComponentUtil.getComponent(SituacaoProcessualManager.class).recuperaSituacoesAtuais(processoJudicial);
		}
		return situacoesAtuais;
	}
	
	public List<SituacaoProcessual> getSituacoes() {
		if(situacoes == null){
			situacoes = processoJudicial.getSituacoes();
		}
		return situacoes;
	}
	
	
	public boolean isExibeSituacoes() {
		return exibeSituacoes;
	}
	
	public boolean isExibeSituacoesAtuais() {
		return exibeSituacoesAtuais;
	}
	
	public void inverterManipulaObjeto(){
		manipulaObjeto = !manipulaObjeto;
	}
	
	public boolean isManipulaObjeto() {
		return manipulaObjeto;
	}

	@Length(max=3000)
	public String getObjetoProcessual(){
		if(objetoProcessual == null){
			objetoProcessual = processoJudicial.getObjeto();
		}
		return objetoProcessual;
	}
	
	public void setObjetoProcessual(String objetoProcessual) {
		this.objetoProcessual = objetoProcessual;
	}
	
	public void gravarObjeto(){
		try{
			if(StringUtils.isNotBlank(objetoProcessual)) {
				processoJudicial.setObjeto(objetoProcessual);
				ComponentUtil.getComponent(ProcessoJudicialManager.class).persistAndFlush(processoJudicial);
				facesMessages.add(Severity.INFO, FacesUtil.getMessage("msg.processoJudicialAction.gravarObjeto"));
			}
		}catch(Throwable t){
			facesMessages.add(Severity.ERROR, FacesUtil.getMessage("erro.processoJudicialAction.gravarObjeto"));
		}
	}
	
	public boolean isHaDocumentosNaoLidos() {
		return haDocumentosNaoLidos;
	}

	public void setHaDocumentosNaoLidos(boolean haDocumentosNaoLidos) {
		this.haDocumentosNaoLidos = haDocumentosNaoLidos;
	}
	
	public void verificarDocumentosNaoLidosNoProcesso(){
		if (getProcessoJudicial() != null){
			this.haDocumentosNaoLidos = ComponentUtil.getComponent(ProcessoDocumentoManager.class).getDocumentosNaoLidos(getProcessoJudicial(), null).size() > 0;
		}
	}
	
	public boolean isHaDocumentosNaoLidosNoProcesso(){
		this.verificarDocumentosNaoLidosNoProcesso();
		if(Authenticator.isUsuarioExterno()){
			return Boolean.FALSE;
		} else {
			return this.haDocumentosNaoLidos;
		}
	}

	public UsuarioLocalizacaoMagistradoServidor getLocalizacaoDestinoProcesso() {
		return localizacaoDestinoProcesso;
	}

	public void setLocalizacaoDestinoProcesso(
			UsuarioLocalizacaoMagistradoServidor localizacaoDestinoProcesso) {
		this.localizacaoDestinoProcesso = localizacaoDestinoProcesso;
	}

	/**
	 * Verifica se a opção "Tornar parte sigilosa" deverá ser exibida.
	 * @return	verdadeiro caso o usuário possua o papel "manipulaSigiloso" e se o número de partes sigilosas 
	 * 			for menor que o número de partes. 
	 */
	public Boolean exibeTornarParteSigilosa(){
		return manipulaSigiloso && (getNumeroPartesSigilosas() < getNumeroPartes());
	}
	
	/**
	 * Verifica se a opção "Tornar parte visível" deverá ser exibida.
	 * @return 	verdadeiro caso o usuário possua o papel "manipulaSigiloso" e se o número de partes sigilosas
	 * 			for maior que zero. 
	 */
	public Boolean exibeTornarParteVisivel(){
		return manipulaSigiloso && (getNumeroPartesSigilosas() > 0);		
	}

	/**
	 * Verifica se o panel de Visualizadores deverá ser exibido.
	 * @return verdadeiro se o processo estiver em segredo de justiça e se a lista de visualizadores não estiver vazia.
	 */
	public Boolean exibirPanelVisualizadores(){
		return processoJudicial.getSegredoJustica() && !getVisualizadores().isEmpty();
	}

	/**
	 * Obtém o título a ser exibido ao se tratar de Objeto do Processo.
	 * @param exibirTitulo	booleano passado por parâmetro nos includes
	 * @return 	a string "Objeto do processo" será exibida caso o parâmetro exibirTitulo seja true. Caso não haja definição 
	 * 			do parâmetro no include, o tipo Boolean converte o null em false, e assim retornará uma string vazia.
	 */
	public String obterTituloObjetoProcesso(Boolean exibirTitulo) {
		return exibirTitulo ? "Objeto do processo": StringUtils.EMPTY;
	}
	
	public List<SituacaoProcessual> getSituacoesAnteriores() {
		if (situacoesAnteriores == null) {
			situacoesAnteriores = processoJudicial.getSituacoes();
		}
		situacoesAnteriores.removeAll(situacoesAtuais);
		return situacoesAnteriores;
	}

	public void setSituacoesAnteriores(List<SituacaoProcessual> situacoesAnteriores) {
		this.situacoesAnteriores = situacoesAnteriores;
	}

	public EntityDataModel<Pessoa> getPossiveisVisualizadoresModel() {
		return possiveisVisualizadoresModel;
	}
	
	public boolean isCompetenciaExigeEscolhaOrgaoJulgador(Competencia competencia){
		boolean retorno = false;
		if(competencia != null){
			retorno = competencia.getIndicacaoOrgaoJulgadorObrigatoria();
		}
		return retorno;
	}

	/**
	 * Habilita o protocolo se:
	 * - tiver sido selecionada uma competencia conflito
	 * - 
	 * @param processoTrf
	 * @param qtdCompetenciasPossiveis
	 * @param competenciaConflito
	 * @return
	 */
	public boolean isHabilitaProtocolo(ProcessoTrf processoTrf, Integer qtdCompetenciasPossiveis, Competencia competenciaConflito){
		boolean retorno = false;
		
		if(qtdCompetenciasPossiveis > 0 && competenciaConflito != null && (!competenciaConflito.getIndicacaoOrgaoJulgadorObrigatoria() || processoTrf.getOrgaoJulgador() != null)) {
			retorno = true;
		}
		return retorno;
	}
	
	public List<OrgaoJulgador> getOrgaosJulgadoresJurisdicaoCompetencia(Jurisdicao jurisdicao, Competencia competencia){
		return ComponentUtil.getOrgaoJulgadorManager().findAllbyJurisdicaoCompetencia(jurisdicao, competencia);
	}
	
	public void atualizarOrgaoJulgador(ProcessoTrf processoTrf) {
		ComponentUtil.getProcessoTrfManager().update(processoTrf);
	}
	
	public void validarCompetenciaOrgaoJulgador(ProcessoTrf processoTrf) {
		if(processoTrf.getOrgaoJulgador() != null && processoTrf.getCompetencia() != null && processoTrf.getCompetencia().getIndicacaoOrgaoJulgadorObrigatoria()) {
			processoTrf.setOrgaoJulgador(null);
			processoTrf.setCompetencia(null);
			ComponentUtil.getProcessoTrfManager().update(processoTrf);
		}
	}
	
	public void atualizarOrgaoJulgadorMudancaCompetencia(ProcessoTrf processoTrf) {
		if(processoTrf.getProcessoStatus().equals(ProcessoStatusEnum.E)){
			processoTrf.setOrgaoJulgador(null);
			ComponentUtil.getProcessoTrfManager().update(processoTrf);
		}
	}

	private class PessoasRetriever implements DataRetriever<Pessoa> {
		private PessoaManager pessoaManager;
		
		public PessoasRetriever(PessoaManager pessoaManager){
			this.pessoaManager = pessoaManager;
		}
		
		@Override
		public Object getId(Pessoa obj) {
			return this.pessoaManager.getId(obj);
		}

		@Override
		public Pessoa findById(Object id) throws Exception {
			return this.pessoaManager.findById(id);
		}

		@Override
		public List<Pessoa> list(Search search) {
			return this.pessoaManager.list(search);
		}

		@Override
		public long count(Search search) {
			return this.pessoaManager.count(search);
		}
	}
	
	public boolean isPermiteAlterarNivelAcesso() {
		return this.processoJudicial.getSegredoJustica() && this.manipulaSigiloso  
				&& this.nivelAcessoSigilo >= this.processoJudicial.getNivelAcesso(); 
	}
	
	public static ProcessoJudicialAction instance() {
		return ComponentUtil.getComponent(ProcessoJudicialAction.class);
	}
	
	public void setProcessoJudicial(ProcessoTrf processo){
		this.processoJudicial = processo;
	}

	public String recuperarParteFormatada(boolean comTabela, boolean mostrarOAB, ProcessoParteParticipacaoEnum... participacoes) throws PJeBusinessException {
		String retorno = "";
		if( processoJudicial == null ) {
			processoJudicial = ProcessoTrfHome.instance().getProcessoTrf();
		}
		if( processoJudicial != null ) {
			retorno = ComponentUtil.getProcessoJudicialManager().recuperarParteFormatada(processoJudicial, comTabela, mostrarOAB, false, participacoes);
		}
		return retorno;
	}

	public void inverterExibicaoVisualizadorParteSigilosa(ProcessoParte parte){
		parteIncluirVisualizador = parte;
		consultaVisualizadorParteSigilosa.inverterExibicao();
	}
	
	public void pesquisaPessoasParteSigilosa() {
		consultaVisualizadorParteSigilosa.setPessoas(
				pesquisaPessoas(consultaVisualizadorParteSigilosa.getTextoPesquisa()));
	}
	
	public void incluirVisualizadorParteSigilosa(int idPessoa) {
		if (!manipulaSigiloso) {
			facesMessages.add(Severity.ERROR, "Seu perfil não tem autorização para modificar o grau de acesso a parte sigilosa.");
			return;
		}
		if (idPessoa == 0) {
			facesMessages.add(Severity.WARN, "Não é possível incluir como visualizador uma pessoa ainda não cadastrada no sistema.");
			return;
		}
		try {
			Pessoa pessoa = ComponentUtil.getComponent(PessoaService.class).findById(idPessoa);
			if (pessoa != null) {
				ProcessoParteVisibilidadeSigiloManager processoParteVisibilidadeSigiloManager = 
						ComponentUtil.getComponent(ProcessoParteVisibilidadeSigiloManager.class);
				if (processoParteVisibilidadeSigiloManager.acrescentarVisualizador(parteIncluirVisualizador, pessoa)) {
					facesMessages.add(Severity.INFO, "{0} acrescentado como visualizador desta parte.", pessoa.getNome());
					loadPartesSigilosas_();
				} else if(parteIncluirVisualizador.getParteSigilosa()) {
					facesMessages.add(Severity.WARN, "{0} já consta como visualizador desta parte.", pessoa.getNome());
				} else {
					facesMessages.add(Severity.ERROR, "Por alguma remota razão, não foi possível acrescentar {0} visualizador desta parte.", pessoa.getNome());
				}
			}
		} catch(PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar incluir a pessoa como visualizador.");
		}
	}
	
	public void removerVisualizadorParteSigilosa(ProcessoParteVisibilidadeSigilo visualizador) throws PJeBusinessException {
		if(!manipulaSigiloso){
			facesMessages.add(Severity.ERROR, "Seu perfil não tem autorização para modificar o grau de acesso a parte sigilosa.");
			return;
		}
		ProcessoParteVisibilidadeSigiloManager processoParteVisibilidadeSigiloManager = 
				ComponentUtil.getComponent(ProcessoParteVisibilidadeSigiloManager.class);
		processoParteVisibilidadeSigiloManager.removerVisualizador(visualizador);
		carregarVisualizadoresPartesSigilosas();
	}
	
	public EntityDataModel<Pessoa> pesquisaPessoas(String textoPesquisa) {
		EntityDataModel<Pessoa> pessoasModel = null;
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		
		String strippedTxt = InscricaoMFUtil.retiraMascara(textoPesquisa);
		if (strippedTxt.matches("\\d*") && (strippedTxt.length() == 8 || strippedTxt.length() == 11 || strippedTxt.length() == 14)) {
			criterios.add(Criteria.or(
				Criteria.equals("pessoaDocumentoIdentificacaoList.tipoDocumento.codTipo", "CPF"), 
				Criteria.equals("pessoaDocumentoIdentificacaoList.tipoDocumento.codTipo", "CPJ")));
			
			criterios.add(Criteria.equals("ativo", true));
			criterios.add(Criteria.equals("pessoaDocumentoIdentificacaoList.ativo",Boolean.TRUE));
			criterios.add(Criteria.equals("pessoaDocumentoIdentificacaoList.numeroDocumento", InscricaoMFUtil.acrescentaMascaraMF(strippedTxt)));
		} else if (textoPesquisa.length() >= 3) {
			
			Criteria nomeParteCriteria = Criteria.contains("nomesPessoa.nome", textoPesquisa);
			criterios.add(nomeParteCriteria);

			Criteria tipoNomeCriteria = Criteria.in("nomesPessoa.tipo", new TipoNomePessoaEnum[] {TipoNomePessoaEnum.C, TipoNomePessoaEnum.D, TipoNomePessoaEnum.A} );
			criterios.add(tipoNomeCriteria);
			
			criterios.add(Criteria.equals("ativo", true));
		}
		
		try {
			if (!criterios.isEmpty()) {
				pessoasModel = new EntityDataModel<Pessoa>(
						Pessoa.class, FacesContext.getCurrentInstance(), new PessoasRetriever(ComponentUtil.getComponent(PessoaManager.class)));
				
				pessoasModel.setDistinct(true);
				pessoasModel.setCriterias(criterios);
				pessoasModel.addOrder("o.nome", Order.ASC);	
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			facesMessages.add(Severity.ERROR, "Ocorreu um erro ao recuperar as pessoas.");
		}
		return pessoasModel;
	}

	public ConsultaPessoaBean getConsultaVisualizadorParteSigilosa() {
		return consultaVisualizadorParteSigilosa;
	}

	public void setConsultaVisualizadorParteSigilosa(ConsultaPessoaBean consultaVisualizadorParteSigilosa) {
		this.consultaVisualizadorParteSigilosa = consultaVisualizadorParteSigilosa;
	}

	public ProcessoParte getParteIncluirVisualizador() {
		return parteIncluirVisualizador;
	}

	public void setParteIncluirVisualizador(ProcessoParte parteIncluirVisualizador) {
		this.parteIncluirVisualizador = parteIncluirVisualizador;
	}

	public boolean verificarVisilidadeRetificacao() {
		boolean resultado = false;
		
		Integer idOrgaoJulgador = Authenticator.getIdOrgaoJulgadorAtual();
		Integer idOrgaoJulgadorColegiado = Authenticator.getIdOrgaoJulgadorColegiadoAtual();
		if (idOrgaoJulgador != null) {
			resultado = this.getProcessoJudicial().getOrgaoJulgador().getIdOrgaoJulgador() == idOrgaoJulgador;
		} else if (idOrgaoJulgadorColegiado != null) {
			resultado = this.getProcessoJudicial().getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado() == idOrgaoJulgadorColegiado;
		} else if(idOrgaoJulgador == null && idOrgaoJulgadorColegiado == null){
			resultado = true;
		}
		if(!resultado) {
			resultado = processoJudicialService.existeFluxoDeslocadoParaLocalizacao(this.getProcessoJudicial());
		}
		
		return resultado;
	}

	private Procuradoria recuperarProcuradoriaVinculada(ProcessoTrf processo, Pessoa pessoa) {
		if (processo == null || pessoa == null) {
			return null;
		}
		ProcessoParteManager processoParteManager = ComponentUtil.getComponent(ProcessoParteManager.class);
		ProcessoParte processoParte = processoParteManager.findProcessoParte(processo, pessoa, false);
		Procuradoria procuradoria = null;
		if (processoParte != null && processoParte.getIsAtivo()) {
			procuradoria = processoParte.getProcuradoria();
		} else if (processoParte == null && pessoa.getInTipoPessoa() == TipoPessoaEnum.J) {
			ProcuradoriaManager procuradoriaManager = ComponentUtil.getComponent(ProcuradoriaManager.class);
			procuradoria = procuradoriaManager.findByPessoaJuridica(PessoaJuridica.class.cast(pessoa));
		}
		return procuradoria;
	}
	

	/**
	 * Adiciona automaticamente os visualizadores do órgão julgador cadastrados na tela de VisualizadoresSigilo no processo, caso o nível de sigilo seja 5.
	 * @param processoTrf caso seja chamado pelo fluxo, nivelSigilo	nível de sigilo do processo
	 */
	public void adicionarVisualizadores(ProcessoTrf processoTrf, int nivelSigilo){
		if(nivelSigilo == 5) {
			if(processoTrf != null) {
				setProcessoJudicial(processoTrf);								
			}
			VisualizadoresSigiloManager visualizadoresSigiloManager = ComponentUtil.getComponent(VisualizadoresSigiloManager.NAME);
			List<VisualizadoresSigilo> visualizadores = visualizadoresSigiloManager.getVisualizadoresSigiloOJ(Authenticator.getOrgaoJulgadorAtual());
			for (VisualizadoresSigilo visualizador : visualizadores) {
				if(processoTrf != null) {
					Procuradoria procuradoria = recuperarProcuradoriaVinculada(processoJudicial, visualizador.getFuncionario().getPessoa());
					try {
						processoJudicialService.acrescentaVisualizador(processoJudicial, visualizador.getFuncionario().getPessoa(), procuradoria);
					} catch (PJeBusinessException e) {
						e.printStackTrace();
					}
				} else {
					incluirVisualizador(visualizador.getFuncionario().getIdUsuario());
				}						
			}
		}		
	}
}

