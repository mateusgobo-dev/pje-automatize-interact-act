package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.tree.LocalizacaoEstruturaServidorSegundoGrauTreeHandler;
import br.com.infox.cliente.component.tree.LocalizacaoEstruturaServidorTreeHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.entidades.vo.LembreteVO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.LembreteManager;
import br.jus.cnj.pje.nucleo.manager.LembretePermissaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.pje.nucleo.entidades.Lembrete;
import br.jus.pje.nucleo.entidades.LembretePermissao;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Name("lembreteAction")
@Scope(ScopeType.CONVERSATION)
public class LembreteAction extends BaseAction<Lembrete> {
	
	private static final long serialVersionUID = 7714475730901165736L;
	private Lembrete lembrete;
	private Lembrete lembretePesquisa;
	private Date dataInicial;
	private Date dataFinal;
	private LembretePermissao lembretePermissao;
	private List<LembreteVO> lembretes;
	private Map<String, Set<ProcessoDocumento>> processoDocumentoAgrupados = new HashMap<String,Set<ProcessoDocumento>>();
	private List<LembretePermissao> lembretePermissaos;
	private Integer idProcessoDocumentoTemp;
	private Boolean ativo;
	private ProcessoTrf processoJudicial;
	private String nomeExibicaoPoloAtivo;
	private String nomeExibicaoPoloPassivo;
	private String tabSelecionada;
	private boolean exibirBtnAdicionar = true;
	private boolean isUsuarioExterno = Authenticator.isUsuarioExterno();

	private static final String ALERTA_ERRO_AO_REALIZAR_OPERACAO = "alerta.erroAoRealizarOperacao";

	@In(value="lembreteManager")
	private LembreteManager lembreteManager;
	
	@RequestParameter
	private Integer idProcessoTrf;
	
	@RequestParameter
	private Integer idProcessoDocumento;
	
	@RequestParameter
	private Integer idLembrete;
	
	public Integer getIdUsuario(){
		return Authenticator.getIdUsuarioLogado();
	}
	
	@Create
	public void inicializar(){
		if (idLembrete==null){
			iniciarNovoLembrete();
		}else{
			lembrete = findById(idLembrete);
			setTabSelecionada("form");
			if (lembrete!=null){
				this.idProcessoTrf = lembrete.getProcessoTrf().getIdProcessoTrf();
				recuperaListaPermissoesPorIdLembrete();
				setInstance(lembrete); 
			}
		}
		carregaDadosProcesso();
		setTabSelecionada("form");
	}
	
	public void limparTree() {
        if (ParametroUtil.instance().isPrimeiroGrau()) {
            LocalizacaoEstruturaServidorTreeHandler tree = ComponentUtil.getComponent(
                    "localizacaoEstruturaServidorTree");
            tree.clearTree();
        }else {
        	LocalizacaoEstruturaServidorSegundoGrauTreeHandler tree2 = ComponentUtil.getComponent(
        			"localizacaoEstruturaServidorSegundoGrauTree");
        	tree2.clearTree();
        }
    }
	
	
	/**
	 * Metodo que retorna a quantidade de lembretes para o processo
	 * 
	 * @param idProcessoTrf
	 * @return Integer - Quantidade de lembretes para os parametros
	 */
	public Integer retornaQuantidadeLembretesPorSituacaoPorIdProcesso(Integer idProcesso){
		return lembreteManager.retornaQuantidadeLembretesPorSituacaoPorIdProcesso(true,idProcesso);
	}
	
	public void selecionarTab(String tab){
		setTabSelecionada(tab);
	}
	
	private Map<Integer,Integer> qtdLembretesProcessoDocumento = new HashMap<Integer,Integer>();
	
	/**
	 * Metodo que retorna a quantidade de lembretes para o documento
	 * 
	 * @param idProcessoDocumento
	 * @return Integer - Quantidade de lembretes para os parametros
	 */
	public Integer retornaQuantidadeLembretesPorSituacaoPorIdProcessoDocumento(Integer idProcessoDocumento){
		if(!qtdLembretesProcessoDocumento.containsKey(idProcessoDocumento)) {
			Integer qtd = lembreteManager.retornaQuantidadeLembretesPorSituacaoPorIdProcessoDocumento(true,idProcessoDocumento);
			qtdLembretesProcessoDocumento.put(idProcessoDocumento, qtd);
		}
		return qtdLembretesProcessoDocumento.get(idProcessoDocumento); 
	}

	/**
	 * Metodo responsável por carregas os dados do processo para o cabeçalho
	 */
	private void carregaDadosProcesso() {
		try{
			if (idProcessoTrf!=null){
				ProcessoJudicialService processoJudicialService = ComponentUtil.getComponent(ProcessoJudicialService.class);
				processoJudicial = processoJudicialService.findById(idProcessoTrf);
			}
		}catch(Exception e){
			logger.error(e.getLocalizedMessage(), e);
			facesMessages.addFromResourceBundle(Severity.ERROR, "alerta.erroAoRealizarOperacao");
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo que verifica se existe lembrete no processo
	 * para os icones da toolBarProcessoJbpm.xhtml
	 * @param idProcessoTrf
	 * @return Boolean (Verdadeiro ou Falso)
	 */
	public Boolean verificarLempretePoridProcesso(Integer idProcessoTrf){
		if (this.idProcessoTrf == null){
			this.idProcessoTrf = idProcessoTrf;
		}
		return lembreteManager.verificaLembretesPorSituacaoPorIdProcessoTrf(true,idProcessoTrf);
	}
	
	/**
	 * Metodo que verifica se existe lembrete no documento para os icones da processoDocumentoBin.xhtml
	 * 
	 * @param idProcessoTrf
	 * @return Boolean (Verdadeiro ou Falso)
	 */
	public Boolean verificarLempretePoridProcessoDocumento(Integer idProcessoDocumento){
		return lembreteManager.verificaLembretesPorSituacaoPorIdProcessoDocumento(true,idProcessoDocumento);
	}
	
	/**
	 * Metodo responsável por verificar se a lista dos lembretes são de processo ou de documento.
	 * 
	 * @return String com o nome do local de exibição.
	 */
	public String retornaNomeLocalExibicao(Integer idProcessoTrf,Integer idProcessoDocumento){
		if (idProcessoDocumento != null && idProcessoDocumento > 0){
			return "do Documento";
		}
		if (idProcessoTrf!=null && idProcessoTrf > 0){
			return "do Processo";
		}
		return "";
	}
	
	/**
	 * Metodo responsável por limpar o valor da localização selecionada
	 */
	public void limparValorLembretePermissaoLocalidade() {
		this.lembretePermissao.setLocalizacao(null);
	}

	/**
	 * Metodo responsável por limpar o valor do papel selecionado
	 */
	public void limparValorLembretePermissaoPapel() {
		this.lembretePermissao.setPapel(null);
	}

	/**
	 * Metodo responsável por limpar o valor do usuário selecionado
	 */
	public void limparValorLembretePermissaoUsuario() {
		this.lembretePermissao.setUsuario(null);
	}
	
	/**
	 * Metodo responsável por carregar um lembrete para edição pelo id.
	 * 
	 * @param idLembrete ID do lembrete
	 */
	public void editarLembrete(Integer idLembrete){
		this.lembrete = findById(idLembrete);
		if (this.lembrete.getAtivo()==null){
			this.lembrete.setAtivo(true);
		}
		setInstance(this.lembrete);
		setTabSelecionada("form");
		recuperaListaPermissoesPorIdLembrete();
	}
	
	/**
	 * Metodo responsável por carregar uma permissão de lembrete para edição.
	 * 
	 * @param idLembretePermissao
	 */
	public void editarLembretePermissao(Integer idLembretePermissao){
		try {
			LembretePermissaoManager lembretePermissaoManager = ComponentUtil.getComponent(LembretePermissaoManager.class);
			this.lembretePermissao = lembretePermissaoManager.findById(idLembretePermissao);
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage(), e);
			facesMessages.addFromResourceBundle(Severity.ERROR, "alerta.erroAoRealizarOperacao");
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo responsável por inativar o lembrete incluíndo a data de exclusão e o usuário logado no momento.
	 * 
	 * @param idlembrete ID do lembrete
	 */
	public void inativarLembretesPorId(Integer idlembreteDelecao){
		try{
			lembreteManager.inativaLembretesPorId(idlembreteDelecao);
			setLembretes(null);
			iniciarNovoLembrete();
			facesMessages.clear();
			facesMessages.addFromResourceBundle(Severity.INFO, "Alerta_deleted");
		}catch(Exception e){
			logger.error(e.getLocalizedMessage(), e);
			facesMessages.addFromResourceBundle(Severity.ERROR, "alerta.erroAoRealizarOperacao");
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo responsvel por inativar o lembrete inclundo a data de excluso e o usurio logado no momento.
	 * 
	 * @param idlembrete ID do lembrete
	 */
	public void inativarLembretesPorDocumento(ProcessoDocumento documento){
		try{
			lembreteManager.inativaLembretesPorDocumento(documento);
			setLembretes(null);
			facesMessages.clear();
			facesMessages.addFromResourceBundle(Severity.INFO, "Alerta_deleted");
		}catch(Exception e){
			logger.error(e.getLocalizedMessage(), e);
			facesMessages.addFromResourceBundle(Severity.ERROR, "alerta.erroAoRealizarOperacao");
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo responsável por excluir a permissão do lembrete
	 * 
	 * @param idLembretePermissao ID do lembrete
	 */
	public void excluirLembretePermissao(Integer idLembretePermissao){
		try {
			LembretePermissao lembretePermissao = null;
			if (idLembretePermissao > 0){
				LembretePermissaoManager lembretePermissaoManager = ComponentUtil.getComponent(LembretePermissaoManager.class);
				lembretePermissao = lembretePermissaoManager.findById(idLembretePermissao);
				lembretePermissaos.remove(lembretePermissao);
				lembretePermissaoManager.remove(lembretePermissao);
			}else{
				lembretePermissaos.remove(lembretePermissaos.size()-1);
			}
			facesMessages.clear();
			facesMessages.addFromResourceBundle(Severity.INFO, "Alerta_deleted");
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage(), e);
			facesMessages.addFromResourceBundle(Severity.ERROR, "alerta.erroAoRealizarOperacao");
			e.printStackTrace();
		}
	}

	/**
	 * Metodo que alimenta lista de permissões do lembrete selecionado.
	 */
	private void recuperaListaPermissoesPorIdLembrete() {
		if (lembrete != null && lembrete.getIdLembrete() != null){
			LembretePermissaoManager lembretePermissaoManager = ComponentUtil.getComponent(LembretePermissaoManager.class);
			lembretePermissaos = lembretePermissaoManager.recuperaListaDePermissoesPorIdLembrete(lembrete.getIdLembrete());
		}
	}
	
	/**
	 * Metodo responsável por incluir uma permissão na lista de permissões do lembrete
	 */
	public void adicionaLembretePermissaoNaLista(){
		if (lembretePermissaos==null){
			lembretePermissaos = new ArrayList<LembretePermissao>();
		}
		if (lembretePermissao != null && lembretePermissao.getLocalizacao() != null
				&& !lembretePermissaos.contains(lembretePermissao)){
			
			if (lembrete!=null && lembrete.getIdLembrete()!=null){
				try {
					lembretePermissao.setLembrete(lembrete);
					LembretePermissaoManager lembretePermissaoManager = ComponentUtil.getComponent(LembretePermissaoManager.class);
					lembretePermissaoManager.persistAndFlush(lembretePermissao);
				} catch (PJeBusinessException e) {
					logger.error(e.getLocalizedMessage(), e);
					facesMessages.addFromResourceBundle(Severity.ERROR, "alerta.erroAoRealizarOperacao");
					e.printStackTrace();
				}
			}
			
			lembretePermissaos.add(lembretePermissao);
			setLembretePermissao(null);
		} else {
			facesMessages.addFromResourceBundle(Severity.ERROR, "Registro já inserido");
		}
	}
	
	/**
	 * Metodo responsável por remover uma permissão da lista de permissões de lembrete.
	 * 
	 * @param lembretePermissao 
	 */
	public void removerLembretePermissaoDaLista(LembretePermissao lembretePermissao){
		lembretePermissaos.remove(lembretePermissao);
	}
	
	/**
	 * Metodo que adiciona a entidade documento na entidade lembrete
	 */
	private void adicionarProcessoDocumentoNoLembrete() {
		if (idProcessoDocumento != null && idProcessoDocumento > 0){
			ProcessoDocumento processoDocumento;
			try {
				ProcessoDocumentoManager processoDocumentoManager = ComponentUtil.getComponent(ProcessoDocumentoManager.class);
				processoDocumento = processoDocumentoManager.findById(idProcessoDocumento);
				this.idProcessoTrf = processoDocumento.getProcessoTrf().getIdProcessoTrf();
				lembrete.setProcessoDocumento(processoDocumento);
			} catch (PJeBusinessException e) {
				logger.error(e.getLocalizedMessage(), e);
				facesMessages.addFromResourceBundle(Severity.ERROR, "alerta.erroAoRealizarOperacao");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Metodo que adiciona a entidade processo na entidade lembrete.
	 */
	private void adicionaProcessoNoLembrete() {
		if (idProcessoTrf != null && idProcessoTrf > 0){
			ProcessoTrf processoTrf = new ProcessoTrf();
			processoTrf.setIdProcessoTrf(idProcessoTrf);
			lembrete.setProcessoTrf(processoTrf);
		}
	}
	
	/**
	 * Metodo responsável por persistir os dados do lembrete no banco de dados.
	 */
	public void salvar(){
		try{
			facesMessages.clear();
			if (lembrete.getProcessoTrf() == null || lembrete.getProcessoTrf().getIdProcessoTrf() == 0) {
				facesMessages.addFromResourceBundle(Severity.ERROR, ALERTA_ERRO_AO_REALIZAR_OPERACAO);
				logger.error("No foi possvel cadastrar este lembrete: IdProcessoTrf no preenchido corretamente.");
				return;
			}
			if (StringUtil.isNullOrEmpty(lembrete.getDescricao())){
				facesMessages.addFromResourceBundle(Severity.ERROR, "Informe a descrição.");
				return;
			}
			if (ProjetoUtil.isVazio(lembretePermissaos)){
				facesMessages.addFromResourceBundle(Severity.ERROR, "alerta.permissao.visualizacao");
			}else{
				if (lembrete.getIdLembrete() != null){
					mergeAndFlush();
				} else {
					persistAndFlush();
					salvarListaPermissaoComLembrete();
				}
				iniciarNovoLembrete();
				setLembretes(null);
			}
		}catch(Exception e){
			logger.error(e.getLocalizedMessage(), e);
			facesMessages.addFromResourceBundle(Severity.ERROR, "alerta.erroAoRealizarOperacao");
			e.printStackTrace();
		}
	}

	/**
	 * Metoro responsável por persistir os dados de permissão para o lembrete no banco de dados 
	 */
	private void salvarListaPermissaoComLembrete() {
		try{
			if (lembrete != null && lembrete.getIdLembrete() != null){
				LembretePermissaoManager lembretePermissaoManager = ComponentUtil.getComponent(LembretePermissaoManager.class);
				for (LembretePermissao lembretePermissao : lembretePermissaos) {
					if (lembretePermissao.getLembrete() == null){
						lembretePermissao.setLembrete(lembrete);
						lembretePermissaoManager.persistAndFlush(lembretePermissao);
					}else{
						lembretePermissaoManager.merge(lembretePermissao);
					}
				}
			}
		}catch(Exception e){
			logger.error(e.getLocalizedMessage(), e);
			facesMessages.addFromResourceBundle(Severity.ERROR, "alerta.erroAoRealizarOperacao");
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo responsável por atualizar a permissão
	 */
	public void atualizarLembretePermissao(){
		try{
			if(lembretePermissao.getIdLembretePermissao() != null){
				LembretePermissaoManager lembretePermissaoManager = ComponentUtil.getComponent(LembretePermissaoManager.class);
				lembretePermissaoManager.merge(lembretePermissao);
			}
			
			setLembretePermissao(null);
			setExibirBtnAdicionar(true);
		}catch(Exception e){
			logger.error(e.getLocalizedMessage(), e);
			facesMessages.addFromResourceBundle(Severity.ERROR, "alerta.erroAoRealizarOperacao");
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo responsável por iniciar uma nova lembrete
	 */
	public void iniciarNovoLembrete(){
		if (lembrete != null){
			if (lembrete.getProcessoTrf() != null){
				setIdProcessoTrf(lembrete.getProcessoTrf().getIdProcessoTrf());
			}
			if (lembrete.getProcessoDocumento() != null){
				setIdProcessoDocumento(lembrete.getProcessoDocumento().getIdProcessoDocumento());
			}
		}
		if (ProjetoUtil.isNotVazio(lembretePermissaos)){
			lembretePermissaos.clear();
		}
		lembrete = new Lembrete();
		lembrete.setUsuarioLocalizacao(Authenticator.getUsuarioLocalizacaoAtual());
		lembrete.setDataInclusao(new Date());
		lembrete.setAtivo(true);
		adicionarProcessoDocumentoNoLembrete();
		adicionaProcessoNoLembrete();
		setInstance(lembrete);
	}
	
	/**
	 * Metodo responsável por recuperar o nome da exibição do polo para cabeçalho.
	 * 
	 * @param polo ProcessoParteParticipacao
	 */
	public void getNomeExibicaoPolo(ProcessoParteParticipacaoEnum polo){
		ProcessoJudicialService processoJudicialService = ComponentUtil.getComponent(ProcessoJudicialService.class);
		if(ProcessoParteParticipacaoEnum.A.equals(polo)){
			this.nomeExibicaoPoloAtivo = processoJudicialService.getNomeExibicaoPolo(processoJudicial, polo);; 
		}
		else if(ProcessoParteParticipacaoEnum.P.equals(polo)){
			this.nomeExibicaoPoloPassivo = processoJudicialService.getNomeExibicaoPolo(processoJudicial, polo);;
		}
	}
	
	/**
	 * Metodo responsável por retornar o nome do polo ativo para cabeçalho.
	 * @return nomeExibicaoPoloAtivo
	 */
	public String getNomeExibicaoPoloAtivo(){
		if(StringUtils.isEmpty(this.nomeExibicaoPoloAtivo)){
			getNomeExibicaoPolo(ProcessoParteParticipacaoEnum.A);
		}
		return nomeExibicaoPoloAtivo;
	}
	
	/**
	 * Metodo responsável por retornar o nome do polo passivo para cabeçalho
	 * @return nomeExibicaoPoloPassivo
	 */
	public String getNomeExibicaoPoloPassivo(){
		if(StringUtils.isEmpty(this.nomeExibicaoPoloPassivo)){
			getNomeExibicaoPolo(ProcessoParteParticipacaoEnum.P);
		}
		return nomeExibicaoPoloPassivo;
	}
	
	@Override
	protected BaseManager<Lembrete> getManager() {
		return lembreteManager;
	}

	@Override
	public EntityDataModel<Lembrete> getModel() {
		return null;		
	}

	public Lembrete getLembrete() {
		if (lembrete == null){
			lembrete = new Lembrete();
			lembrete.setUsuarioLocalizacao(Authenticator.getUsuarioLocalizacaoAtual());
			lembrete.setDataInclusao(new Date());
			lembrete.setAtivo(true);
		}
		return lembrete;
	}

	public void setLembrete(Lembrete lembrete) {
		this.lembrete = lembrete;
	}
	
	/**
	 * Metodo responsável por limpar a lista de lembretes antes da pesquisa
	 */
	public void pesquisarLembretes(){
		setLembretes(null);
	}
	
	/**
	 * Metodo responsável por limpar os paramentos da pesquisa
	 */
	public void limparParametrosPesquisa(){
		setLembretePesquisa(null);
		setDataFinal(null);
		setDataFinal(null);
	}
	
	/**
	 * Recupera lista de lembretes verificando os parametros de localização
	 * @return List<LembreteVO> lista formatada de lembretes
	 */
	public List<LembreteVO> getLembretes() {
		if (ProjetoUtil.isVazio(lembretes) && lembretePesquisa != null){
			lembretes = lembreteManager.recuperarLembretes(lembretePesquisa,dataInicial,dataFinal);
		}
		return lembretes;
	}
	
	/**
	 * Recupera lista de lembretes para o lembretesInclude com passagem dos parametros
	 * @param idProcessoTrf
	 * @param idProcessoDocumento
	 * @return List<LembreteVO> lista formatada de lembretes
	 */
	public List<LembreteVO> getLembretes(Integer idProcessoTrf,Integer idProcessoDocumento, Boolean ativo) {
		if (idProcessoDocumento > 0){
			setIdProcessoDocumentoTemp(idProcessoDocumento);
			recuperaLembretes(idProcessoTrf,idProcessoDocumento,ativo,false);
		}
		
		return lembretes;
	}
	
	/**
	 * Recupera lista de lembretes de todos documentos do processo
	 * e agrupa por permissao
	 * @param idProcessoDocumento
	 * @return 
	 */
	public Map<String, Set<ProcessoDocumento>> recuperarDocumentosAgrupadosPorLembretePermissao(Integer idProcessoTrf) {
		processoDocumentoAgrupados.put("documentosUsuario", new HashSet<ProcessoDocumento>());
		processoDocumentoAgrupados.put("documentosUnidade", new HashSet<ProcessoDocumento>());
		recuperaLembretes(idProcessoTrf, null, true, true);
		
		agruparDocumentosPorPermissao();
		
		return processoDocumentoAgrupados;
	}
	
	/**
	 * Agrupa documentos por unidade e por usuario de acordo com a permissao do lembrete
	 * @param documentosLembretes
	 */
	private void agruparDocumentosPorPermissao(){
		for(LembreteVO lembrete: lembretes){
			for(LembretePermissao permissao: lembrete.getLembretePermissoes()){
				if(lembrete.getProcessoDocumento() != null){
					if(permissao.getUsuario() != null && permissao.getUsuario().equals(Authenticator.getUsuarioLogado())){
						processoDocumentoAgrupados.get("documentosUsuario").add(lembrete.getProcessoDocumento());
					}else{
						processoDocumentoAgrupados.get("documentosUnidade").add(lembrete.getProcessoDocumento());
					}
				}
			}
		}
	}
	
	/**
	 * Metodo responsável por recuperar os lembrete pelos parametros de processo ou documento.
	 * Caso estes sejam nulos, recupera pelo usuário logado.
	 * @param idProcessoTrf
	 * @param idProcessoDocumento
	 * @param ativo
	 * @param todosDocumentos
	 */
	private void recuperaLembretes(Integer idProcessoTrf, Integer idProcessoDocumento, Boolean ativo, Boolean todosDocumentos) {
		if (idProcessoDocumento!=null && idProcessoDocumento > 0){
			lembretes = lembreteManager.recuperarLembretesPorSituacaoPorIdProcessoDocumento(ativo, idProcessoDocumento);
		}
		if (idProcessoTrf!=null && idProcessoTrf > 0){
			List<LembreteVO> lembretesTemp = lembreteManager.recuperarLembretesPorSituacaoPorIdProcessoTrf(ativo, idProcessoTrf);
			if (lembretesTemp != null && !lembretesTemp.isEmpty()) {
				lembretes = lembretesTemp;
			}
		}
		if(todosDocumentos != null && todosDocumentos){
			lembretes = lembreteManager.recuperarLembretesPorSituacaoPorIdProcessoTrf(ativo, idProcessoTrf, todosDocumentos);
		}
	}
	
	public void setLembretes(List<LembreteVO> lembretes) {
		this.lembretes = lembretes;
	}

	public LembretePermissao getLembretePermissao() {
		if (lembretePermissao==null){
			lembretePermissao = new LembretePermissao();
		}
		if (lembretePermissao.getOrgaoJulgadorColegiado()==null){
			lembretePermissao.setOrgaoJulgadorColegiado(Authenticator.getOrgaoJulgadorColegiadoAtual());
		}
		if (lembretePermissao.getOrgaoJulgador()==null){
			lembretePermissao.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
		}
		if (lembretePermissao.getLocalizacao()==null){
			lembretePermissao.setLocalizacao(Authenticator.getLocalizacaoAtual());
		}
		return lembretePermissao;
	}

	public void setLembretePermissao(LembretePermissao lembretePermissao) {
		this.lembretePermissao = lembretePermissao;
	}

	public List<LembretePermissao> getLembretePermissaos() {
		if (lembretePermissaos==null){
			lembretePermissaos = new ArrayList<LembretePermissao>();
		}
		return lembretePermissaos;
	}

	public void setLembretePermissaos(List<LembretePermissao> lembretePermissaos) {
		this.lembretePermissaos = lembretePermissaos;
	}

	public Integer getIdProcessoTrf() {
		return idProcessoTrf;
	}

	public void setIdProcessoTrf(Integer idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}

	public Integer getIdProcessoDocumento() {
		return idProcessoDocumento;
	}

	public void setIdProcessoDocumento(Integer idProcessoDocumento) {
		this.idProcessoDocumento = idProcessoDocumento;
	}
	
	public Integer getIdProcessoDocumentoTemp() {
		return idProcessoDocumentoTemp;
	}

	public void setIdProcessoDocumentoTemp(Integer idProcessoDocumentoTemp) {
		this.idProcessoDocumentoTemp = idProcessoDocumentoTemp;
	}

	public Integer getIdLembrete() {
		return idLembrete;
	}

	public void setIdLembrete(Integer idLembrete) {
		this.idLembrete = idLembrete;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public ProcessoTrf getProcessoJudicial() {
		return processoJudicial;
	}

	public void setProcessoJudicial(ProcessoTrf processoJudicial) {
		this.processoJudicial = processoJudicial;
	}

	public Lembrete getLembretePesquisa() {
		if (lembretePesquisa==null){
			lembretePesquisa = new Lembrete();
			lembretePesquisa.setAtivo(Boolean.TRUE);
		}
		return lembretePesquisa;
	}

	public void setLembretePesquisa(Lembrete lembretePesquisa) {
		this.lembretePesquisa = lembretePesquisa;
	}

	public Date getDataInicial() {
		return dataInicial;
	}

	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}

	public Date getDataFinal() {
		return dataFinal;
	}

	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}

	public String getTabSelecionada() {
		if (tabSelecionada==null){
			tabSelecionada = "search";
		}
		return tabSelecionada;
	}

	public void setTabSelecionada(String tabSelecionada) {
		this.tabSelecionada = tabSelecionada;
	}

	public boolean isExibirBtnAdicionar() {
		return exibirBtnAdicionar;
	}

	public void setExibirBtnAdicionar(boolean exibirBtnAdicionar) {
		this.exibirBtnAdicionar = exibirBtnAdicionar;
	}

	public Map<String, Set<ProcessoDocumento>> getProcessoDocumentoAgrupados() {
		return processoDocumentoAgrupados;
	}

	public void setProcessoDocumentoAgrupados(Map<String, Set<ProcessoDocumento>> processoDocumentoAgrupados) {
		this.processoDocumentoAgrupados = processoDocumentoAgrupados;
	}

	public boolean isUsuarioExterno() {
		return isUsuarioExterno;
	}

	public void setUsuarioExterno(boolean isUsuarioExterno) {
		this.isUsuarioExterno = isUsuarioExterno;
	}

}
