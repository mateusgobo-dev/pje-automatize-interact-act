package br.jus.cnj.pje.view;
 
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
 
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.infox.pje.manager.PessoaJuridicaManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAutoridadeManager;
import br.jus.cnj.pje.nucleo.manager.UnificacaoPessoasManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLoginManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.TipoPessoaManager;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.DesunificacaoVO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Unificacao;
import br.jus.pje.nucleo.entidades.UnificacaoPessoas;
import br.jus.pje.nucleo.entidades.UnificacaoPessoasObjeto;
import br.jus.pje.nucleo.util.ArrayUtil;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;
 
/**
 * 
 * @author luiz.mendes
 *
 * passo 1 - separarObjetosUnificados
 * passo 2 - get da view
 * passo 3 - finalizar
 */
@Name("desunificacaoAction")
@Scope(ScopeType.CONVERSATION)
public class DesunificacaoAction extends BaseAction<UnificacaoPessoas> implements Serializable {
 
	private static final long serialVersionUID = 1L;
 	
 	private UnificacaoPessoasManager unificacaoPessoasManager = ComponentUtil.getComponent("unificacaoPessoasManager");
 	private TipoPessoaManager tipoPessoaManager = ComponentUtil.getComponent("tipoPessoaManager");
 	private PessoaManager pessoaManager = ComponentUtil.getComponent("pessoaManager");
 	private PessoaFisicaManager pessoaFisicaManager = ComponentUtil.getComponent("pessoaFisicaManager");
 	private PessoaJuridicaManager pessoaJuridicaManager = ComponentUtil.getComponent("pessoaJuridicaManager");
 	private PessoaAutoridadeManager pessoaAutoridadeManager = ComponentUtil.getComponent("pessoaAutoridadeManager");
 	private FacesMessages facesMessagesLocal = (FacesMessages) Component.getInstance("org.jboss.seam.international.statusMessages");
 	private UsuarioManager usuarioManager = ComponentUtil.getComponent("usuarioManager");
 	private UsuarioLoginManager usuarioLoginManager = ComponentUtil.getComponent("usuarioLoginManager");
 	
 	private EntityDataModel<UnificacaoPessoas> model;
 	
 	private static final Integer IDENTIFICADOR_PESQUISA_PESSOA_PRINCIPAL = 0;
 	private static final Integer IDENTIFICADOR_PESQUISA_PESSOA_SECUNDARIA = 1;
 	private static final Integer IDENTIFICADOR_PESQUISA_TODAS_PESSOAS = 2;
 	
 	private static final Integer IDENTIFICADOR_PESQUISA_UNIFICACAO_ATIVA = 0;
 	private static final Integer IDENTIFICADOR_PESQUISA_UNIFICACAO_INATIVA = 1;
 	private static final Integer IDENTIFICADOR_PESQUISA_TODAS_UNIFICACAOES = 2;
 	
 	private Integer searchPessoaPrincipal = IDENTIFICADOR_PESQUISA_PESSOA_PRINCIPAL;
 	private String searchIdPessoa = null;
 	private String searchNomePessoa = null;
 	private Integer searchSituacaoUnificacao = IDENTIFICADOR_PESQUISA_UNIFICACAO_ATIVA;
 	private String activeTab = "search";
 	
 	private DesunificacaoVO desunificacaoVO = null;
 	
 	@RequestParameter(value="pessoaGridCount")
 	private Integer unificacaoPessoasGridCount;	
 	
 	public DesunificacaoAction() {}
 
 	@Override
 	protected BaseManager<UnificacaoPessoas> getManager() {
 		return unificacaoPessoasManager;
 	}
 	
 	@Override
 	public EntityDataModel<UnificacaoPessoas> getModel() {
 		return this.model;
 	}
 	
 	public String getSearchPessoaPrincipal() {
 		return searchPessoaPrincipal.toString();
 	}
 	
 	public void setSearchPessoaPrincipal(String searchPessoaPrincipal) {
 		if(searchPessoaPrincipal.equalsIgnoreCase(IDENTIFICADOR_PESQUISA_PESSOA_PRINCIPAL.toString())) {
 			this.searchPessoaPrincipal = IDENTIFICADOR_PESQUISA_PESSOA_PRINCIPAL;
 		}else if(searchPessoaPrincipal.equalsIgnoreCase(IDENTIFICADOR_PESQUISA_PESSOA_SECUNDARIA.toString())) {
 			this.searchPessoaPrincipal = IDENTIFICADOR_PESQUISA_PESSOA_SECUNDARIA;
 		}else {
 			this.searchPessoaPrincipal = IDENTIFICADOR_PESQUISA_TODAS_PESSOAS;
 		}
 	}
 	
 	public String getIdPessoa() {
 		return searchIdPessoa;
 	}
 	
 	public void setIdPessoa(String idPessoa) {
 		this.searchIdPessoa = idPessoa;
 	}
 	
 	public String getSearchSituacaoUnificacao() {
 		return searchSituacaoUnificacao.toString();
 	}
 	
 	public void setSearchSituacaoUnificacao(String searchSituacaoUnificacao) {
 		if(searchSituacaoUnificacao.equalsIgnoreCase(IDENTIFICADOR_PESQUISA_UNIFICACAO_ATIVA.toString())) {
 			this.searchSituacaoUnificacao = IDENTIFICADOR_PESQUISA_UNIFICACAO_ATIVA;
 		}else if(searchSituacaoUnificacao.equalsIgnoreCase(IDENTIFICADOR_PESQUISA_UNIFICACAO_INATIVA.toString())) {
 			this.searchSituacaoUnificacao = IDENTIFICADOR_PESQUISA_UNIFICACAO_INATIVA;
 		}else {
 			this.searchSituacaoUnificacao = IDENTIFICADOR_PESQUISA_TODAS_UNIFICACAOES;
 		}
 	}
 	
 	public String getSearchNomePessoa() {
 		return searchNomePessoa;
 	}
 	
 	public void setSearchNomePessoa(String searchNomePessoa) {
 		this.searchNomePessoa = searchNomePessoa;
 	}
 	
 	public Pessoa getPessoaPrincipal() {
 		return desunificacaoVO.getPessoaPrincipal();
 	}
 	
 	public List<Pessoa> getPessoaPrincipalList(){
 		List<Pessoa> retorno = new ArrayList<Pessoa>();
 		retorno.add(getPessoaPrincipal());
 		return retorno;
 	}
 	
 	public List<Unificacao> getUnificacaoList() {
 		List<Unificacao> retorno = new ArrayList<Unificacao>();
 		retorno.add(getUnificacao());
 		return retorno;
 	}
 	
 	public List<UnificacaoPessoas> getUnificacaoPessoasAsList() {
 		List<UnificacaoPessoas> retorno = new ArrayList<UnificacaoPessoas>();
 		retorno.add(getUnificacaoPessoas());
 		return retorno;
 	}
 	
 	public Pessoa getPessoaSecundaria() {
 		return desunificacaoVO.getPessoaSecundaria();
 	}
 	
 	public List<Pessoa> getPessoaSecundariaList() {
 		List<Pessoa> retorno = new ArrayList<Pessoa>();
 		retorno.add(getPessoaSecundaria());
 		return retorno;
 	}
 
 	public Unificacao getUnificacao() {
 		return desunificacaoVO.getUnificacao();
 	}
 	
 	public UnificacaoPessoas getUnificacaoPessoas() {
 		if(desunificacaoVO != null) {
 			return desunificacaoVO.getUnificacaoPessoa();
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Metodo responsavel por verificar se a pessoa principal é uma pessoa fisica.
 	 * caso o tipo de pessoa esteja configurado incorretamente no banco de dados, verifica se a pessoa existe na tabela de pessoa fisica
 	 * @return true se a pessoa principal for fisica / false
 	 */
 	public boolean isPessoaPrincipalTipoFisica() {
 		if(desunificacaoVO != null) {
 			try {
 				return desunificacaoVO.isPessoaPrincipalTipoFisica();
 			} catch (Exception e) {
 				return pessoaFisicaManager.verficarPessoaExisteTabelaPessoaFisica(getPessoaPrincipal());
 			}
 		}else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Metodo responsavel por verificar se a pessoa principal é uma pessoa juridica.
 	 * caso o tipo de pessoa esteja configurado incorretamente no banco de dados, verifica se a pessoa existe na tabela de pessoa juridica
 	 * @return true se a pessoa principal for juridica / false
 	 */
 	public boolean isPessoaPrincipalTipoJuridica() {
 		if(desunificacaoVO != null) {
 			try {
 				return desunificacaoVO.isPessoaPrincipalTipoJuridica();
 			}catch (Exception e) {
 				return pessoaJuridicaManager.verficarPessoaExisteTabelaPessoaJuridica(getPessoaPrincipal());
 			}
 		}else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Metodo responsavel por verificar se a pessoa principal  uma pessoa autoridade.
 	 * caso o tipo de pessoa esteja configurado incorretamente no banco de dados, verifica se a pessoa existe na 
 	 * tabela de pessoa autoridade
 	 * @return true se a pessoa principal for autoridade / false
 	*/
 	public boolean isPessoaPrincipalTipoAutoridade() {
	 	if(desunificacaoVO != null) {
		 	try {
		 		return desunificacaoVO.isPessoaPrincipalTipoAutoridade();
		 	}catch (Exception e) {
		 		return pessoaAutoridadeManager.verficarPessoaExisteTabelaEnteAutoridade(getPessoaPrincipal());
		 	}
	 	}else {
	 		return false;
	 	}
 	}
 	
 	/**
 	 * Metodo responsavel por verificar se a pessoa secundaria  uma pessoa fisica
 	 * @return true se a pessoa secundaria for fisica / false
 	 */
 	public boolean isPessoaSecundariaTipoFisica() {
 		if(desunificacaoVO != null) {
 			try {
 				return desunificacaoVO.isPessoaSecundariaTipoFisica();
 			} catch (Exception e) {
 				return pessoaFisicaManager.verficarPessoaExisteTabelaPessoaFisica(getPessoaSecundaria());
 			}
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Metodo responsavel por verificar se a pessoa secundaria  uma pessoa juridica
 	 * @return true se a pessoa secundaria for juridica / false
 	 */
 	public boolean isPessoaSecundariaTipoJuridica() {
 		if(desunificacaoVO != null) {
 			try {
 				return desunificacaoVO.isPessoaSecundariaTipoJuridica();
 			} catch (Exception e) {
 				return pessoaJuridicaManager.verficarPessoaExisteTabelaPessoaJuridica(getPessoaSecundaria());
 			}
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Metodo responsavel por verificar se a pessoa secundaria  uma pessoa autoridade
 	 * @return true se a pessoa secundaria for autoridade / false
 	 */
 	public boolean isPessoaSecundariaTipoAutoridade() {
 		if(desunificacaoVO != null) {
 			try {
 				return desunificacaoVO.isPessoaSecundariaTipoAutoridade();
 			} catch (Exception e) {
 				return pessoaAutoridadeManager.verficarPessoaExisteTabelaEnteAutoridade(getPessoaSecundaria());
 			}
 		} else {
 			return false;
 		}
 	}
 	
 	public boolean getTipoPessoaSecundariaCorreto() {
 		return desunificacaoVO.isTipoPessoaSecundariaCorreto();
 	}
 	
 	public boolean getTipoPessoaPrincipalCorreto() {
 		return desunificacaoVO.isTipoPessoaPrincipalCorreto();
 	}
 
 	public void setTab(String tab) {
 		this.activeTab = tab;
 	}
 
 	public String getTab() {
 		return activeTab;
 	}
 	
 	@Create
 	public void init () {
 		iniciarProcessoDesunificacao();
 	}
 	
 	/**
 	 * metodo responsavel por inicializar ou reinicializar os objetos da desunificacao.
 	 */
 	public void iniciarProcessoDesunificacao() {
 		limparTodosCamposFiltros();
 		pesquisar();
 	}
 
 	public void pesquisar(){
 		List<Criteria> criterios = new ArrayList<Criteria>(0);
 		criterios.addAll(getCriteriosTelaPesquisa());
 			
 		try {			
 			model = new EntityDataModel<UnificacaoPessoas>(UnificacaoPessoas.class, super.facesContext, getRetriever());
 			model.setCriterias(criterios);
 		} catch (Exception e) {
 			mostraMensagem(true, "pje.desunificacao.erroAoExecutarPesquisa", e.getMessage());
 			e.printStackTrace();
 		}
 	}
 			
 	/**
 	 * Este mtodo adiciona os critrios de pesquisa preenchidos na tela.
 	 * 
 	 * @return Lista de critrios de pesquisa
 	 */
 	private List<Criteria> getCriteriosTelaPesquisa(){
 		List<Criteria> criterios = new ArrayList<Criteria>(0);
 		//NOME PESSOA
 		if(searchNomePessoa != null && !searchNomePessoa.isEmpty()) {
 			if(searchPessoaPrincipal == IDENTIFICADOR_PESQUISA_PESSOA_PRINCIPAL) {
 				criterios.add(Criteria.in("unificacao.pessoaPrincipal.idPessoa", obtemIDPessoaByNome(searchNomePessoa) ));	
 			}else if (searchPessoaPrincipal == IDENTIFICADOR_PESQUISA_PESSOA_SECUNDARIA) {
 				criterios.add(Criteria.in("pessoaSecundariaUnificada.idPessoa", obtemIDPessoaByNome(searchNomePessoa)));
 			}else {
 				criterios.add(
 						Criteria.or(
 								Criteria.in("unificacao.pessoaPrincipal.idPessoa", obtemIDPessoaByNome(searchNomePessoa)), 
 								Criteria.in("pessoaSecundariaUnificada.idPessoa", obtemIDPessoaByNome(searchNomePessoa))));
 			}
 			
 		}
 		//ID PESSOA
 		if(searchIdPessoa != null && !searchIdPessoa.isEmpty()) {
 			if(searchPessoaPrincipal == IDENTIFICADOR_PESQUISA_PESSOA_PRINCIPAL) {
 				criterios.add(Criteria.equals("unificacao.pessoaPrincipal.idPessoa", Integer.parseInt(searchIdPessoa)));	
 			}else if (searchPessoaPrincipal == IDENTIFICADOR_PESQUISA_PESSOA_SECUNDARIA) {
 				criterios.add(Criteria.equals("pessoaSecundariaUnificada.idPessoa", Integer.parseInt(searchIdPessoa)));
 			}else {
 				criterios.add(
 						Criteria.or(
 								Criteria.equals("unificacao.pessoaPrincipal.idPessoa", searchIdPessoa), 
 								Criteria.equals("pessoaSecundariaUnificada.idPessoa", searchIdPessoa)));
 			}
 		}
 		//UNIFICACAO ATIVA / INATIVA
 		if(searchSituacaoUnificacao == IDENTIFICADOR_PESQUISA_UNIFICACAO_INATIVA) {
 			criterios.add(Criteria.equals("ativo", Boolean.FALSE));
 		}else if(searchSituacaoUnificacao == IDENTIFICADOR_PESQUISA_UNIFICACAO_ATIVA) {
 			criterios.add(Criteria.equals("ativo", Boolean.TRUE));
 		}
 	return criterios;
 	}
 				
 	private Integer[] obtemIDPessoaByNome(String nome) {
 		Integer[] retorno = null;
 		
 		List<Pessoa> pessoas = pessoaManager.findByName(nome,0,50);
 		
 		if(pessoas != null && !pessoas.isEmpty()) {
 			List<Integer> idPessoas = new ArrayList<Integer>(0);
 			for (Pessoa pessoa : pessoas) {
 				if(!idPessoas.contains(pessoa.getIdPessoa())) {
 					idPessoas.add(pessoa.getIdPessoa());
 				}
 			}
 			
 			retorno = new Integer[idPessoas.size()];
 				
 				int counter = 0;
 				for (Integer idPess : idPessoas) {
 					retorno[counter] = idPess;
 					counter++;
 				}	
 		}
 		
 		if(retorno != null) {
 			return retorno;
 		} else {
 			return inviabilizaPesquisaPorIDPessoa();
 		}
 	}
 
 	@Override
 	protected DataRetriever<UnificacaoPessoas> getRetriever() {
 		final UnificacaoPessoasManager manager = (UnificacaoPessoasManager)getManager();
 		final Integer tableCount = unificacaoPessoasGridCount;
 		DataRetriever<UnificacaoPessoas> retriever = new DataRetriever<UnificacaoPessoas>() {
 			@Override
 			public UnificacaoPessoas findById(Object id) throws Exception {
 				try {
 					return manager.findById(id);
 				} catch (PJeBusinessException e) {
 					throw new Exception(e);
 				}
 			}
 						
 			@Override
 			public List<UnificacaoPessoas> list(Search search) {
 				return manager.list(search);
 			}
 			
 			@Override
 			public long count(Search search) {
 				if (tableCount != null && tableCount >=0){
 					return tableCount;
 				}
 			
 				return manager.count(search);
 			}
 			
 			@Override
 			public Object getId(UnificacaoPessoas obj){
 				return manager.getId(obj);
 			}
 		};
 	return retriever;
 	}		
 	
 	/**
 	 * Metodo responsavel por montar as mensagems para exibi ao na tela
 	 * @param tipoMensagem - true se for uma mensagem de erro / false se for uma mensagem de informacao / null se for uma mensagem de warn
 	 * @param localizacaoMensagem - localizacao da mensagem no arquivo entitymessages.properties
 	 * @param complementoDaMensagem - mensagem a ser adicionada  mensagem do arquivo entitymessages.properties.
 	 */
 	private void mostraMensagem(Boolean tipoMensagem, String localizacaoMensagem, String complementoDaMensagem) {
 		String tempComplementoMensagem = "";
 		if(complementoDaMensagem != null) {
 			tempComplementoMensagem = complementoDaMensagem;
 		}
 		facesMessagesLocal.clear();
 		facesMessagesLocal.clearGlobalMessages();
 		if(localizacaoMensagem == null) {
 			facesMessagesLocal.add(Severity.ERROR, tempComplementoMensagem);
 			return;
 		}
 		if (tipoMensagem == null) {
 			facesMessagesLocal.add(Severity.WARN, FacesUtil.getMessage("entity_messages", localizacaoMensagem + tempComplementoMensagem));
 		}else  if (tipoMensagem) {
 			facesMessagesLocal.add(Severity.ERROR, FacesUtil.getMessage("entity_messages", localizacaoMensagem + tempComplementoMensagem));
 		} else {
 			facesMessagesLocal.add(Severity.INFO, FacesUtil.getMessage("entity_messages", localizacaoMensagem + tempComplementoMensagem));
 		}
 	}
 	
 	/**
 	 * metodo auxiliar responsavel por retornar a data de unificacao (se unificacao ainda for ativa) ou desunificacao (se 
 	 * unificacao for inativa) e formatar a data 
 	 * @param unifPes
 	 * @return String com data dd/MM/YYYY HH:mm:ss
 	 */
 	public String exibeDataUnificadoDesunificado (UnificacaoPessoas unifPes) {
 		if(unifPes.getAtivo()) {		
 			return DateUtil.dateToString(unifPes.getDataUnificacao(), "dd/MM/YYYY HH:mm:ss");
 		}else {
 			return "("+DateUtil.dateToString(unifPes.getDataDesunificacao(), "dd/MM/YYYY HH:mm:ss")+")";
 		}
 	}
 	
 	/**
 	 * metodo responsavel por encaminhar a unificacao de pessoas selecionada em tela para desunificacao.
 	 * @param unificacaoPessoaSelecionada
 	 */
 	public void desunificarPessoa(UnificacaoPessoas unificacaoPessoaSelecionada) {
 		if(unificacaoPessoaSelecionada != null) {
 			desunificacaoVO = new DesunificacaoVO();
 			desunificacaoVO.setUnificacaoPessoa(unificacaoPessoaSelecionada);
 			verificarTipoPessoaCorreto(false);
 			verificarTipoPessoaCorreto(true);
 			inserirPessoaPorTiposPessoa();
 			inserirUsuarioPessoaSecundaria();
 			inserirUsuarioLoginPessoaSecundaria();
 			activeTab = "Desunificacao";
 			separarObjetosUnificados();
 		}
 	}

	private void inserirPessoaPorTiposPessoa() {
 		try{
 			if(desunificacaoVO.isPessoaSecundariaTipoFisica()) {
 				desunificacaoVO.setPessoaFisicaSecundaria(pessoaFisicaManager.encontraPessoaFisicaPorPessoa(desunificacaoVO.getPessoaSecundaria()));
 			}
 			if(desunificacaoVO.isPessoaSecundariaTipoJuridica()) {
 				desunificacaoVO.setPessoaJuridicaSecundaria(pessoaJuridicaManager.encontraPessoaJuridicaPorPessoa(desunificacaoVO.getPessoaSecundaria()));
 			}
 			if(desunificacaoVO.isPessoaSecundariaTipoAutoridade()) {
 				desunificacaoVO.setPessoaAutoridadeSecundaria(pessoaAutoridadeManager.encontraPessoaAutoridadePorPessoa(desunificacaoVO.getPessoaSecundaria()));
 			}
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
	}
	
	private void inserirUsuarioPessoaSecundaria() {
		desunificacaoVO.setUsuarioPessoaSecundaria(usuarioManager.encontrarPorPessoa(desunificacaoVO.getPessoaSecundaria()));
	}
	
	private void inserirUsuarioLoginPessoaSecundaria() {
		desunificacaoVO.setUsuarioLoginPessoaSecundaria(usuarioLoginManager.encontrarPorPessoa(desunificacaoVO.getPessoaSecundaria()));
	}

	/**
	 * metodo responsavel por verificar se o tipo de pessoa setado esta corretamente lancado na respectiva tabela
	 * ex. TipoPessoaEnum.F dever ter lancamento na tabela de pessoaFisica
	 * @param _isPessoaPrincipal - booleano que indica se a pessoa passada em parametro  a pessoa principal da unificacao
	 */
	private void verificarTipoPessoaCorreto(boolean _isPessoaPrincipal) {
		if(_isPessoaPrincipal && tipoPessoaManager.verificarTipoPessoaCorreto(desunificacaoVO.getPessoaPrincipal())) {
				desunificacaoVO.setTipoPessoaPrincipalCorreto(Boolean.TRUE);
		}
		
		if(!_isPessoaPrincipal && tipoPessoaManager.verificarTipoPessoaCorreto(desunificacaoVO.getPessoaSecundaria())) {
			desunificacaoVO.setTipoPessoaSecundariaCorreto(Boolean.TRUE);
		}
	}

 
 	/**
 	 * metodo responsavel por limpar os dados dos atributos utilizados no filtro.
 	 */
 	public void limparTodosCamposFiltros() {
 		searchPessoaPrincipal = IDENTIFICADOR_PESQUISA_PESSOA_PRINCIPAL;
 		searchIdPessoa = null;
 		searchNomePessoa = null;
 		searchSituacaoUnificacao = IDENTIFICADOR_PESQUISA_UNIFICACAO_ATIVA;
 		activeTab = "search";
 	}
 	
 	public boolean isUnificacaoAtiva() {
 		return desunificacaoVO.getUnificacao().getAtivo();
 	}
 
 	/**
 	 * metodo que encaminha para conclusao da desunificacao.
 	 */
 	public void finalizarDesunificacaoPessoas() {
 		try {
 			unificacaoPessoasManager.finalizarDesunificacaoPessoas(desunificacaoVO);
 			iniciarProcessoDesunificacao();
 			desunificacaoVO = null;
 			mostraMensagem(false, "pje.desunificacao.desunificacaoConcluida", null);
 		}catch (Exception e) {
 			desunificacaoVO = null;
 			mostraMensagem(true, "pje.desunificacao.desunificacaoErro", e.getLocalizedMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	/**
	 * para evitar excluir do filtro de pessoas devido ao fato de nao existir pessoas com as caracteristicas passadas e 
	 * evitar exibir todos os resultados
	 * mais uma vez, este metodo  responsavel por gerar um Integer[] com a id -1.
	 * @return Integer[] com valor -1
	 */
	private Integer[] inviabilizaPesquisaPorIDPessoa() {
		return ArrayUtil.getArrayInteiroNegativo();
	}
	
	/**
	 * metodo responsavel por separar por listas os objetos unificados
	 */
	private void separarObjetosUnificados() {
		if(desunificacaoVO.getUnificacaoPessoa().getUnificacaoPessoasObjetos() != null && desunificacaoVO.getUnificacaoPessoa().getUnificacaoPessoasObjetos().size() > 0) {
			for (UnificacaoPessoasObjeto unifPessObj : desunificacaoVO.getUnificacaoPessoa().getUnificacaoPessoasObjetos()) {
				switch (unifPessObj.getInTipoObjetoUnificacao()) {
				
					case LOG_ACESSO:
						desunificacaoVO.adicionarLogAcessoUnificadoObject(unifPessObj);
						break;
						
					case CARACTERISTICA_FISICA:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarCaractFisUnificadaObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarCaractFisUnificadaObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getCaractFisUnificadasNaoEncontradasObject().contains(unifPessObj)){
									desunificacaoVO.getCaractFisUnificadasNaoEncontradasObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case MEIO_CONTATO_CADASTRADOS:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarMeioContatoCadastradoUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarMeioContatoCadastradoUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getMeioContatoCadastradoUnificadoNaoEncontradoObject().contains(unifPessObj)){
									desunificacaoVO.getMeioContatoCadastradoUnificadoNaoEncontradoObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case MEIO_CONTATO_PROPRIETARIA:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarMeioContatoProprietariasUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarMeioContatoProprietariasUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getMeiosContatoProprietariasUnificadosAlteradosOuNaoEncontradasObject().contains(unifPessObj)){
									desunificacaoVO.getMeiosContatoProprietariasUnificadosAlteradosOuNaoEncontradasObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case NOMES_ALTERNATIVOS_CADASTRADOS:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarNomeAlternativoCadastradoUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarNomeAlternativoCadastradoUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getNomesAlternativosCadastradosUnificadosAlteradosOuNaoEncontradosObject().contains(unifPessObj)){
									desunificacaoVO.getNomesAlternativosCadastradosUnificadosAlteradosOuNaoEncontradosObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case NOMES_ALTERNATIVOS_PROPRIETARIA:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarNomeAlternativoProprietariasUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarNomeAlternativoProprietariasUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getNomesAlternativosProprietariasUnificadosAlteradosOuNaoEncontradasObject().contains(unifPessObj)){
									desunificacaoVO.getNomesAlternativosProprietariasUnificadosAlteradosOuNaoEncontradasObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case CONEXOES_PREVENCAO:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarConexaoProcessosUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarConexaoProcessosUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getConexaoProcessoUnificadosNaoEncontradosObject().contains(unifPessObj)){
									desunificacaoVO.getConexaoProcessoUnificadosNaoEncontradosObject().add(unifPessObj);
								}
							}
						}
						break;
					
					case SEGREDO_PROCESSO:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarSegredosProcessosUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarSegredosProcessosUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getSegredosProcessosUnificadosNaoEncontradosObject().contains(unifPessObj)){
									desunificacaoVO.getSegredosProcessosUnificadosNaoEncontradosObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case SIGILO_PROCESSO_PARTE:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarSigiloProcessosPartesUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarSigiloProcessosPartesUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getSigiloProcessosParteUnificadosNaoEncontradosObject().contains(unifPessObj)){
									desunificacaoVO.getSigiloProcessosParteUnificadosNaoEncontradosObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case CAIXA_REPRESENTANTE:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarCaixaRepresentanteUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarCaixaRepresentanteUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getCaixaRepresentanteUnificadosNaoEncontradosObject().contains(unifPessObj)){
									desunificacaoVO.getCaixaRepresentanteUnificadosNaoEncontradosObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case SESSAO_ENTE_EXTERNO:
						desunificacaoVO.adicionarSessaoEnteExternoUnificadoObject(unifPessObj);
						break;
						
					case PROCESSO_REDISTRIBUICAO:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarRedistribuicaoProcessoUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarRedistribuicaoProcessoUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getRedistribuicaoProcessosUnificadosNaoEncontradosObject().contains(unifPessObj)){
									desunificacaoVO.getRedistribuicaoProcessosUnificadosNaoEncontradosObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case PROCESSO_PARTE_HISTORICO:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarProcessoParteHistoricoUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarProcessoParteHistoricoUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getProcessoParteHistoricosUnificadosNaoEncontradosObject().contains(unifPessObj)){
									desunificacaoVO.getProcessoParteHistoricosUnificadosNaoEncontradosObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case PROCESSO_TAG:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarProcessoTagUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarProcessoTagUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getProcessosTagsUnificadosNaoEncontradosObject().contains(unifPessObj)){
									desunificacaoVO.getProcessosTagsUnificadosNaoEncontradosObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case LEMBRETE:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarLembreteUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarLembreteUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getLembretesUnificadosNaoEncontradosObject().contains(unifPessObj)){
									desunificacaoVO.getLembretesUnificadosNaoEncontradosObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case PERMISSAO_LEMBRETE:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarPermissaoLembreteUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarPermissaoLembreteUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getPermissaoLembretesUnificadosNaoEncontradosObject().contains(unifPessObj)){
									desunificacaoVO.getPermissaoLembretesUnificadosNaoEncontradosObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case PROCESSOS_PROTOCOLADOS:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarProcessosUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarProcessosUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getProcessosUnificadosNaoEncontradosObject().contains(unifPessObj)){
									desunificacaoVO.getProcessosUnificadosNaoEncontradosObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case PARAMETROS_ALTERADOS:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarParametrosUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarParametrosUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getParametrosUnificadosNaoEncontradosObject().contains(unifPessObj)){
									desunificacaoVO.getParametrosUnificadosNaoEncontradosObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case ENTITY_LOGS:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarEntityLogsUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarEntityLogsUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getEntityLogsUnificadosNaoEncontradosObject().contains(unifPessObj)){
									desunificacaoVO.getEntityLogsUnificadosNaoEncontradosObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case SOLICITACAO_NO_DESVIO:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarSolicitacoesNoDesvioUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarSolicitacoesNoDesvioUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getSolicitacoesNoDesvioNaoEncontradasObject().contains(unifPessObj)){
									desunificacaoVO.getSolicitacoesNoDesvioNaoEncontradasObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case SESSAO_PAUTA_PROC_INCLUSORA:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarSessaoPautaProcessoInclusoraUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarSessaoPautaProcessoInclusoraUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getSessaoPautaProcessoInclusoraNaoEncontradasObject().contains(unifPessObj)){
									desunificacaoVO.getSessaoPautaProcessoInclusoraNaoEncontradasObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case SESSAO_PAUTA_PROC_EXCLUSORA:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarSessaoPautaProcessoExclusoraUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarSessaoPautaProcessoExclusoraUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getSessaoPautaProcessoExclusoraNaoEncontradasObject().contains(unifPessObj)){
									desunificacaoVO.getSessaoPautaProcessoExclusoraNaoEncontradasObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case SESSAO_INCLUSORA:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarSessaoInclusoraUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarSessaoInclusoraUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getSessaoInclusoraNaoEncontradasObject().contains(unifPessObj)){
									desunificacaoVO.getSessaoInclusoraNaoEncontradasObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case SESSAO_EXCLUSORA:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarSessaoExclusoraUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarSessaoExclusoraUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getSessaoExclusoraNaoEncontradasObject().contains(unifPessObj)){
									desunificacaoVO.getSessaoExclusoraNaoEncontradasObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case QUADRO_AVISO:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarAvisoQuadroAvisoUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarAvisoQuadroAvisoUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getAvisoQuadroAvisoNaoEncontradasObject().contains(unifPessObj)){
									desunificacaoVO.getAvisoQuadroAvisoNaoEncontradasObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case PROCESSO_DOCUMENTO_FAVORITO:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarProcessoDocumentoFavoritoUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarProcessoDocumentoFavoritoUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getProcessosDocumentoFavoritosNaoEncontradosObject().contains(unifPessObj)){
									desunificacaoVO.getProcessosDocumentoFavoritosNaoEncontradosObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case NOTAS_SESSAO_JULG:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarNotaSessaoJulgamentoUnificadaObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarNotaSessaoJulgamentoUnificadaObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getNotasSessaoJulgamentoNaoEncontradasObject().contains(unifPessObj)){
									desunificacaoVO.getNotasSessaoJulgamentoNaoEncontradasObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case MODELOS_PROCLAMACAO_JULG:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarModeloProclamacaoJulgamentoUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarModeloProclamacaoJulgamentoUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getModelosProclamacaoJulgamentoNaoEncontradosObject().contains(unifPessObj)){
									desunificacaoVO.getModelosProclamacaoJulgamentoNaoEncontradosObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case LOG_HIST_MOVIMENTACAO:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarLogHistoricoMovimentacaoUnificadoObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarLogHistoricoMovimentacaoUnificadoObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getLogsHistoricoMovimentacaoNaoEncontradosObject().contains(unifPessObj)){
									desunificacaoVO.getLogsHistoricoMovimentacaoNaoEncontradosObject().add(unifPessObj);
								}
							}
						}
						break;
						
					case VISIBILIDADE_DOC_IDENTIFICACAO:
						if(desunificacaoVO.getUnificacao().getAtivo()) {
							desunificacaoVO.adicionarVisibilidadeDocumentoIdentificacaoUnificadaObject(unifPessObj);
						} else {
							if(unifPessObj.getEncontradoDesunificacao()) {
								desunificacaoVO.adicionarVisibilidadeDocumentoIdentificacaoUnificadaObject(unifPessObj);
							}else {
								if(!desunificacaoVO.getVisibilidadesDocIdentificacaoNaoEncontradasObject().contains(unifPessObj)){
									desunificacaoVO.getVisibilidadesDocIdentificacaoNaoEncontradasObject().add(unifPessObj);
								}
							}
						}
						break;
						
				}
			}
		}
	}
	
	public List<UnificacaoPessoasObjeto> getLogAcessoUnificadosObject() {
		return desunificacaoVO.getLogAcessoUnificadosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getCaractFisUnificadasObject() {
		return desunificacaoVO.getCaractFisUnificadasObject();
	}
	
	public List<UnificacaoPessoasObjeto> getCaractFisUnificadasNaoEncontradasObject() {
		return desunificacaoVO.getCaractFisUnificadasNaoEncontradasObject();
	}
	
	public List<UnificacaoPessoasObjeto> getMeioContatoCadastradoUnificadoNaoEncontradoObject() {
		return desunificacaoVO.getMeioContatoCadastradoUnificadoNaoEncontradoObject();
	}

	public List<UnificacaoPessoasObjeto> getMeiosContatoCadastradosUnificadosObject() {
		return desunificacaoVO.getMeiosContatoCadastradosUnificadosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getMeiosContatoProprietariasUnificadosObject() {
		return desunificacaoVO.getMeiosContatoProprietariasUnificadosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getMeiosContatoProprietariasUnificadosAlteradosOuNaoEncontradasObject() {
		return desunificacaoVO.getMeiosContatoProprietariasUnificadosAlteradosOuNaoEncontradasObject();
	}
	
	public List<UnificacaoPessoasObjeto> getNomesAlternativosCadastradosUnificadosObject() {
		return desunificacaoVO.getNomesAlternativosCadastradosUnificadosObject();
	}

	public List<UnificacaoPessoasObjeto> getNomesAlternativosCadastradosUnificadosAlteradosOuNaoEncontradosObject() {
		return desunificacaoVO.getNomesAlternativosCadastradosUnificadosAlteradosOuNaoEncontradosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getNomesAlternativosProprietariasUnificadosObject() {
		return desunificacaoVO.getNomesAlternativosProprietariasUnificadosObject();
	}

	public List<UnificacaoPessoasObjeto> getNomesAlternativosProprietariasUnificadosAlteradosOuNaoEncontradasObject() {
		return desunificacaoVO.getNomesAlternativosProprietariasUnificadosAlteradosOuNaoEncontradasObject();
	}
	
	public List<UnificacaoPessoasObjeto> getConexoesPrevencaoUnificadosObject() {
		return desunificacaoVO.getConexoesPrevencaoUnificadosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getConexaoProcessoUnificadosNaoEncontradosObject() {
		return desunificacaoVO.getConexaoProcessoUnificadosNaoEncontradosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getSegredosProcessosUnificadosObject() {
		return desunificacaoVO.getSegredosProcessosUnificadosObject();
	}

	public List<UnificacaoPessoasObjeto> getSegredosProcessosUnificadosNaoEncontradosObject() {
		return desunificacaoVO.getSegredosProcessosUnificadosNaoEncontradosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getSigiloProcessosParteUnificadosObject() {
		return desunificacaoVO.getSigiloProcessosParteUnificadosObject();
	}

	public List<UnificacaoPessoasObjeto> getSigiloProcessosParteUnificadosNaoEncontradosObject() {
		return desunificacaoVO.getSigiloProcessosParteUnificadosNaoEncontradosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getCaixaRepresentanteUnificadosObject() {
		return desunificacaoVO.getCaixaRepresentanteUnificadosObject();
	}

	public List<UnificacaoPessoasObjeto> getCaixaRepresentanteUnificadosNaoEncontradosObject() {
		return desunificacaoVO.getCaixaRepresentanteUnificadosNaoEncontradosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getSessaoEnteExternoUnificadosObject() {
		return desunificacaoVO.getSessaoEnteExternoUnificadosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getRedistribuicaoProcessosUnificadosNaoEncontradosObject() {
		return desunificacaoVO.getRedistribuicaoProcessosUnificadosNaoEncontradosObject();
	}

	public List<UnificacaoPessoasObjeto> getRedistribuicaoProcessosUnificadosObject() {
		return desunificacaoVO.getRedistribuicaoProcessosUnificadosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getProcessoParteHistoricosUnificadosObject() {
		return desunificacaoVO.getProcessoParteHistoricosUnificadosObject();
	}

	public List<UnificacaoPessoasObjeto> getProcessoParteHistoricosUnificadosNaoEncontradosObject() {
		return desunificacaoVO.getProcessoParteHistoricosUnificadosNaoEncontradosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getProcessosTagsUnificadosObject() {
		return desunificacaoVO.getProcessosTagsUnificadosObject();
	}

	public List<UnificacaoPessoasObjeto> getProcessosTagsUnificadosNaoEncontradosObject() {
		return desunificacaoVO.getProcessosTagsUnificadosNaoEncontradosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getLembretesUnificadosObject() {
		return desunificacaoVO.getLembretesUnificadosObject();
	}

	public List<UnificacaoPessoasObjeto> getLembretesUnificadosNaoEncontradosObject() {
		return desunificacaoVO.getLembretesUnificadosNaoEncontradosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getPermissaoLembretesUnificadosObject() {
		return desunificacaoVO.getPermissaoLembretesUnificadosObject();
	}

	public List<UnificacaoPessoasObjeto> getPermissaoLembretesUnificadosNaoEncontradosObject() {
		return desunificacaoVO.getPermissaoLembretesUnificadosNaoEncontradosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getProcessosUnificadosObject() {
		return desunificacaoVO.getProcessosUnificadosObject();
	}

	public List<UnificacaoPessoasObjeto> getProcessosUnificadosNaoEncontradosObject() {
		return desunificacaoVO.getProcessosUnificadosNaoEncontradosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getParametrosUnificadosObject() {
		return desunificacaoVO.getParametrosUnificadosObject();
	}

	public List<UnificacaoPessoasObjeto> getParametrosUnificadosNaoEncontradosObject() {
		return desunificacaoVO.getParametrosUnificadosNaoEncontradosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getEntityLogsUnificadosObject() {
		return desunificacaoVO.getEntityLogsUnificadosObject();
	}

	public List<UnificacaoPessoasObjeto> getEntityLogsUnificadosNaoEncontradosObject() {
		return desunificacaoVO.getEntityLogsUnificadosNaoEncontradosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getSolicitacoesNoDesvioUnificadasObject() {
		return desunificacaoVO.getSolicitacoesNoDesvioUnificadasObject();
	}

	public List<UnificacaoPessoasObjeto> getSolicitacoesNoDesvioNaoEncontradasObject() {
		return desunificacaoVO.getSolicitacoesNoDesvioNaoEncontradasObject();
	}
	
	public List<UnificacaoPessoasObjeto> getSessaoPautaProcessoInclusoraUnificadasObject() {
		return desunificacaoVO.getSessaoPautaProcessoInclusoraUnificadasObject();
	}
	
	public List<UnificacaoPessoasObjeto> getSessaoPautaProcessoInclusoraNaoEncontradasObject() {
		return desunificacaoVO.getSessaoPautaProcessoInclusoraNaoEncontradasObject();
	}
	
	public List<UnificacaoPessoasObjeto> getSessaoPautaProcessoExclusoraUnificadasObject() {
		return desunificacaoVO.getSessaoPautaProcessoExclusoraUnificadasObject();
	}

	public List<UnificacaoPessoasObjeto> getSessaoPautaProcessoExclusoraNaoEncontradasObject() {
		return desunificacaoVO.getSessaoPautaProcessoExclusoraNaoEncontradasObject();
	}
	
	public List<UnificacaoPessoasObjeto> getSessaoInclusoraUnificadasObject() {
		return desunificacaoVO.getSessaoInclusoraUnificadasObject();
	}

	public List<UnificacaoPessoasObjeto> getSessaoInclusoraNaoEncontradasObject() {
		return desunificacaoVO.getSessaoInclusoraNaoEncontradasObject();
	}
	
	public List<UnificacaoPessoasObjeto> getSessaoExclusoraUnificadasObject() {
		return desunificacaoVO.getSessaoExclusoraUnificadasObject();
	}

	public List<UnificacaoPessoasObjeto> getSessaoExclusoraNaoEncontradasObject() {
		return desunificacaoVO.getSessaoExclusoraNaoEncontradasObject();
	}
	
	public List<UnificacaoPessoasObjeto> getAvisoQuadroAvisoUnificadasObject() {
		return desunificacaoVO.getAvisoQuadroAvisoUnificadasObject();
	}

	public List<UnificacaoPessoasObjeto> getAvisoQuadroAvisoNaoEncontradasObject() {
		return desunificacaoVO.getAvisoQuadroAvisoNaoEncontradasObject();
	}
	
	public List<UnificacaoPessoasObjeto> getProcessosDocumentoFavoritosUnificadosObject() {
		return desunificacaoVO.getProcessosDocumentoFavoritosUnificadosObject();
	}

	public List<UnificacaoPessoasObjeto> getProcessosDocumentoFavoritosNaoEncontradosObject() {
		return desunificacaoVO.getProcessosDocumentoFavoritosNaoEncontradosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getNotasSessaoJulgamentoUnificadasObject() {
		return desunificacaoVO.getNotasSessaoJulgamentoUnificadasObject();
	}
	
	public List<UnificacaoPessoasObjeto> getNotasSessaoJulgamentoNaoEncontradasObject() {
		return desunificacaoVO.getNotasSessaoJulgamentoNaoEncontradasObject();
	}
	
	public List<UnificacaoPessoasObjeto> getModelosProclamacaoJulgamentoUnificadosObject() {
		return desunificacaoVO.getModelosProclamacaoJulgamentoUnificadosObject();
	}

	public List<UnificacaoPessoasObjeto> getModelosProclamacaoJulgamentoNaoEncontradosObject() {
		return desunificacaoVO.getModelosProclamacaoJulgamentoNaoEncontradosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getLogsHistoricoMovimentacaoUnificadosObject() {
		return desunificacaoVO.getLogsHistoricoMovimentacaoUnificadosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getLogsHistoricoMovimentacaoNaoEncontradosObject() {
		return desunificacaoVO.getLogsHistoricoMovimentacaoNaoEncontradosObject();
	}
	
	public List<UnificacaoPessoasObjeto> getVisibilidadesDocIdentificacaoUnificadasObject() {
		return desunificacaoVO.getVisibilidadesDocIdentificacaoUnificadasObject();
	}
	
	public List<UnificacaoPessoasObjeto> getVisibilidadesDocIdentificacaoNaoEncontradasObject() {
		return desunificacaoVO.getVisibilidadesDocIdentificacaoNaoEncontradasObject();
	}
	
}
