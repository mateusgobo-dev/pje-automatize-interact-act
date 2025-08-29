/**
 * 
 */
package br.jus.cnj.pje.view;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.international.StatusMessage.Severity;
import org.richfaces.component.UITree;
import org.richfaces.component.state.TreeState;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

import br.com.infox.cliente.actions.CaixaAdvogadoProcuradorAction;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.utils.Constantes;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.entidades.vo.PesquisaExpedientesVO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.CaixaAdvogadoProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.PrioridadeProcessoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoCaixaAdvogadoProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteCaixaAdvogadoProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.je.pje.entity.vo.CaixaAdvogadoProcuradorVO;
import br.jus.je.pje.entity.vo.JurisdicaoVO;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoCaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteCaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoExpedienteEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Componente de controle da tela acessada por usuários externos, tais como advogados, procuradores etc.
 * Esta classe é a controladora da página Painel/PainelUsuario/advogado.xhtml
 * 
 * @author Paulo Cristovão de Araújo Silva Filho
 *
 */
@Name("painelUsuarioExternoAction")
@Scope(ScopeType.PAGE)
public class PainelUsuarioExternoAction extends BaseAction<ProcessoTrf> {
	
	private static final long serialVersionUID = -6592182243493983776L;
	
	private static final String nomeAba = "abaAcervo";
	
	private void initCaixaAdvogadoProcuradorManager() {
		caixaAdvogadoProcuradorManager = ComponentUtil.getComponent(CaixaAdvogadoProcuradorManager.class);
	}
	
	private CaixaAdvogadoProcuradorManager caixaAdvogadoProcuradorManager;
	
	private ProcessoJudicialManager processoJudicialManager = ProcessoJudicialManager.instance();
	
	@SuppressWarnings("rawtypes")
	private TreeNodeImpl treeNodeJurisdicoes;
	
	private Long count;
	
	private EntityDataModel<ProcessoTrf> processosAcervo;
	
	private boolean pendentes;
	
	private JurisdicaoVO jurisdicao;
	
	private CaixaAdvogadoProcuradorVO caixa;
	
	private boolean cxEdit = false;
	
	private boolean cxRemove = false;
	
	private String ordenacao;

	private Order order = Order.DESC;
		
	private String campoAssunto;
	
	private String campoClasse;
	
	private PrioridadeProcesso prioridade;
	
	private Date dataDistribuicaoInicial;
	
	private Date dataDistribuicaoFinal;
	
	private String nomeParte;
	
	private String documentoIdentificacaoParte;
	
	private String codigoOABRepresentante;
	
	private Integer numeroSequencia;
	
	private Integer digitoVerificador;
	
	private Integer ano;
	
	private String ramoJustica;
	
	private String respectivoTribunal;
	
	private Integer numeroOrigem;
	
	private OrgaoJulgador orgaoJulgador;

	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;

	private String nomeNovaCaixa;
	
	private ExpedicaoExpedienteEnum meioComunicacao;
	
	private TipoProcessoDocumento tipoProcessoDocumento;
	
	private Integer idLocalizacaoAtual;
	
	private Integer idProcuradoriaAtual;
	
	private TipoProcuradoriaEnum tipoProcuradoriaAtual;

	private static ParametroUtil parametroUtil = ComponentUtil.getComponent(ParametroUtil.NAME);
	
	private boolean criarCaixa = false;
	
	private AcervoRetriever retrieverAcervo;
	
	private Map<Integer,BigInteger> contadoresAcervo = new HashMap<Integer,BigInteger>();
	
	@SuppressWarnings("rawtypes")
	private Map<Integer, TreeNodeImpl> mapaJurisdicoesAcervo = new HashMap<Integer, TreeNodeImpl>();
	private Map<Integer, List<CaixaAdvogadoProcuradorVO>> mapaCaixasJurisdicao = new HashMap<Integer, List<CaixaAdvogadoProcuradorVO>>();
	private Map<Integer, Long> mapaCountProcessosTabela = new HashMap<Integer, Long>();
		
	@In( value="trAc",required=false, create=true)
	@Out(value="trAc",required=false)
	private UITree richTreeAcervo;
	
	/**
	 * Variável destinada a armazenar o identificador do componente rich:tab selecionado.
	 */
	@RequestParameter(value="selectedTab")
	private String selectedTab;
	
	/**
	 * Campos do formulário de pesquisa menu contexto
	 */
	private String inputConsultaContextoAcervo;	
	
	public boolean getIsAmbienteColegiado() {
		return !parametroUtil.isPrimeiroGrau();
	}
	
	private Boolean resultadoEncontrado = true;
	
	public Boolean verificaSeConsultaEncontrouRegistros() {
		return this.resultadoEncontrado;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	private ProcessoTrf processoTrf;
	
	/**
	 * Recuperador de dados do acervo.
	 * 
	 * @author cristof
	 *
	 */
	private class AcervoRetriever implements DataRetriever<ProcessoTrf>{
		
		private Long count;
		
		private ProcessoJudicialManager manager;
		
		public AcervoRetriever(ProcessoJudicialManager manager, Long count, FacesContext facesContext) {
			this.manager = manager;
			this.count = count;
		}
		
		@Override
		public Object getId(ProcessoTrf p) {
			return manager.getId(p);
		}

		@Override
		public ProcessoTrf findById(Object id) throws Exception {
			return manager.findById(id);
		}

		@Override
		public List<ProcessoTrf> list(Search search) {
			if(jurisdicao != null) {
				ConsultaProcessoVO criteriosPesquisa = getCriteriosPesquisaProcessos();
				try {
					if(caixa == null || caixa.getPadrao()) {
						criteriosPesquisa.setApenasSemCaixa(true);
						return manager.getProcessosJurisdicao(jurisdicao.getId(), criteriosPesquisa, search);
					}else {
						return manager.getProcessosJurisdicaoCaixa(jurisdicao.getId(), caixa.getId(), criteriosPesquisa, search);
					}
				}catch (Exception e) {
					facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar os expedientes.");
				}
			}
			return Collections.emptyList();
		}

		@Override
		public long count(Search search) {
			if(jurisdicao != null){
				search.setMax(0);
				ConsultaProcessoVO criteriosPesquisa = getCriteriosPesquisaProcessos();
				try {
					if(caixa == null || caixa.getPadrao()) {
						criteriosPesquisa.setApenasSemCaixa(true);
					}else {
						criteriosPesquisa.setApenasSemCaixa(false);
						criteriosPesquisa.setIdCaixaAdvProc(caixa.getId());
					}
					count = buscaCountCache(jurisdicao.getId(), criteriosPesquisa);
				}catch (Exception e) {
					facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar os processos.");
					count = 0L;
				}
			}
			return count;
		}		
	};
	
	@Create
	public void init() throws PJeBusinessException{
		HttpSession session = (HttpSession)facesContext.getExternalContext().getSession(false);
		session.setMaxInactiveInterval(3600);
		
		idLocalizacaoAtual = Authenticator.getIdLocalizacaoAtual();
		idProcuradoriaAtual = Authenticator.getIdProcuradoriaAtualUsuarioLogado();
		tipoProcuradoriaAtual = Authenticator.getTipoProcuradoriaAtualUsuarioLogado();
		
		initCaixaAdvogadoProcuradorManager();
		
		retrieverAcervo = new AcervoRetriever(processoJudicialManager, count, facesContext);
		
		processosAcervo = new EntityDataModel<ProcessoTrf>(ProcessoTrf.class, ProcessoParte.class, super.facesContext, retrieverAcervo);

		if(jurisdicao != null) {
			pesquisar(pendentes);
		}
		
		if(cxEdit){
			redirecionarEdicaoCaixa();
		} else if(cxRemove) {
			removerCaixa();
		}
	}
	
	@Override
	protected BaseManager<ProcessoTrf> getManager() {
		return ComponentUtil.getComponent(ProcessoJudicialManager.class);
	}

	@Override
	public EntityDataModel<ProcessoTrf> getModel() {
		return null;
	}
	
	/**
	 * Prepara os critérios de pesquisa para a tela.
	 */
	public void pesquisar(){
		pesquisar(pendentes);
	}
	
	/**
	 * Prepara os critérios de pesquisa para retorno, segundo o tipo de pesquisa.
	 * 
	 * @param pendentes indica se se pretende recuperar os processos pendentes 
	 * de manifestação ou todos do acervo do usuário
	 */
	public void pesquisar(boolean pendentes){
		//
	}
	
	public void setOrdenacao(String ordenacao) {
		setOrdenacao(ordenacao, this.getOrder());
	}

	public void setOrdenacao(String ordenacao, Order order) {
		this.ordenacao = ordenacao;
		if(ordenacao == null || ordenacao.isEmpty()){
			this.setOrder(order == null ? Order.DESC : order);
			processosAcervo.addOrder("ptf.dt_ultimo_movimento", order);
		} else {
			String[] tks = ordenacao.split(" ");
			if(order == null) {
				if(tks[1].trim().equalsIgnoreCase("desc")) {
					order = Order.DESC;
				}
				else {
					order = Order.ASC;
				}
			}
			this.setOrder(order);  
			if(tks[0].equals("numeroProcesso")){
				processosAcervo.addOrder("ptf.nr_ano", order);
				processosAcervo.addOrder("ptf.nr_sequencia", order);
			} else if(tks[0].equals("dataDistribuicao")){
				processosAcervo.addOrder("ptf.dt_distribuicao", order);
			} else if(tks[0].equals("dtCriacao")){
			}
		}
	}

	public void inverterOrdenacao() {
		this.setOrder(this.getOrder().equals(Order.DESC) ? Order.ASC : Order.DESC);
		setOrdenacao(this.ordenacao, this.getOrder());
		this.getProcessosAcervo().setRefreshPage(Boolean.TRUE);
	}
	
	/**
	 * Limita a pesquisa a uma jurisdição com o identificador dado.
	 * 
	 * @param idJurisdicao o identificador da jurisdição.
	 */
	public void limitarJurisdicao(JurisdicaoVO jurisdicao){
		this.jurisdicao = jurisdicao; 
	}
	
	/**
	 * Indica se a pesquisa atual se refere apenas aos processos que têm prazos pendentes.
	 * 
	 * @return true, se a pesquisa for apenas para os processos pendentes de atuação
	 */
	public boolean isPendentes() {
		return pendentes;
	}
	
	/**
	 * Recupera o modelo de dados pertinentes à pesquisa atual.
	 * 
	 * @return o modelo de dados
	 */
	public EntityDataModel<ProcessoTrf> getProcessosAcervo(){
		return processosAcervo;
	}
		
	private List<CaixaAdvogadoProcuradorVO> getCaixasJurisdicao(JurisdicaoVO jurisdicao, Boolean apenasCaixasAtivas) throws PJeBusinessException{
		ConsultaProcessoVO criteriosPesquisa = this.getCriteriosPesquisaAcervoMenuContexto();
		List<CaixaAdvogadoProcuradorVO> caixasJurisdicao = null;
		if(jurisdicao != null) {
			criteriosPesquisa.setIdJurisdicao(jurisdicao.getId());
			criteriosPesquisa.setApenasCaixasAtivas(apenasCaixasAtivas);
		
			Integer hashCriteriosPesquisaAtual = (Integer)criteriosPesquisa.hashCode();
			
			if (mapaCaixasJurisdicao != null && !mapaCaixasJurisdicao.isEmpty()) {
				caixasJurisdicao = mapaCaixasJurisdicao.get(hashCriteriosPesquisaAtual);
			}
	
			if (caixasJurisdicao == null) {
				caixasJurisdicao = processoJudicialManager.getCaixasAcervoJurisdicao(jurisdicao.getId(), criteriosPesquisa);
				CaixaAdvogadoProcuradorVO caixaDeEntrada = this.montarCaixaDeEntrada(jurisdicao);
				if(caixaDeEntrada != null) {
					caixasJurisdicao.add(0, caixaDeEntrada);
				}			
				mapaCaixasJurisdicao.put(hashCriteriosPesquisaAtual, caixasJurisdicao);
			}
		}
		
		return caixasJurisdicao;
	}
	
	/**
	 * Monta um VO para a caixa de entrada - que será a caixa padrão de todas as jurisdições - com o contador apenas dos expedientes que estão nessa caixa
	 * Não será montada a caixa de entrada se o usuário não for admin da jurisdição
	 * @param jurisdicao
	 * @return
	 * @throws PJeBusinessException
	 */
	private CaixaAdvogadoProcuradorVO montarCaixaDeEntrada(JurisdicaoVO jurisdicaoVO) throws PJeBusinessException {
		CaixaAdvogadoProcuradorVO caixaDeEntrada = null;
		if(jurisdicaoVO != null && jurisdicaoVO.getId() != 0) {
			ConsultaProcessoVO pesquisaProcessosSemCaixa = this.getCriteriosPesquisaAcervoMenuContexto();
			
			pesquisaProcessosSemCaixa.setIdJurisdicao(jurisdicaoVO.getId());
			pesquisaProcessosSemCaixa.setApenasSemCaixa(true);
			
			BigInteger contadorItens = BigInteger.valueOf(this.buscaCountCache(jurisdicaoVO.getId(), pesquisaProcessosSemCaixa));
			caixaDeEntrada = new CaixaAdvogadoProcuradorVO(
					-1, 
					"Caixa de entrada", 
					"Esta é a caixa de entrada desta jurisdição", 
					jurisdicaoVO.getId(), 
					jurisdicaoVO.getDescricao(), 
					jurisdicaoVO.getAdmin(), 
					Boolean.TRUE,
					Boolean.TRUE,
					contadorItens);
		}			
		
		return caixaDeEntrada;
	}
	
	public void limpaPesquisaTabela() {
        this.numeroSequencia = null;
        this.digitoVerificador = null;
        this.ano = null;
        this.ramoJustica = null;
        this.respectivoTribunal = null;
        this.numeroOrigem = null;
        this.campoAssunto = null;
        this.campoClasse = null;
        this.dataDistribuicaoInicial = null;
        this.dataDistribuicaoFinal = null;
        this.prioridade = null;
        this.orgaoJulgador = null;
        this.orgaoJulgadorColegiado = null;
        this.nomeParte = null;
        this.documentoIdentificacaoParte = null;
        this.codigoOABRepresentante = null;
        
		this.resetSelecionados();
		mapaCountProcessosTabela = new HashMap<Integer, Long>();
		this.getProcessosAcervo().setRefreshPage(Boolean.TRUE);
	}

	/**
	 * Esta função consolida as opções de pesaquisa do painel na aba de acervo - pesquisas gerais do painel
	 * e.g processos com um número N / processos de uma parte específica
	 * @return
	 */
	private ConsultaProcessoVO getCriteriosPesquisaProcessos() {
		Integer jurisdicaoId = null;
		if(this.getJurisdicao() != null) {
			jurisdicaoId = this.getJurisdicao().getId();
		}
		Integer caixaId = null;
		if(this.getCaixa() != null && !this.getCaixa().getPadrao()) {
			caixaId = this.getCaixa().getId();
		}
		
		ConsultaProcessoVO universoPesquisa = null;
		if(this.getCriteriosPesquisaAcervoMenuContexto() != null && this.getCriteriosPesquisaAcervoMenuContexto().isPesquisaAlterada()) {
			universoPesquisa = this.getCriteriosPesquisaAcervoMenuContexto();
		}

		ConsultaProcessoVO criteriosPesquisa = new ConsultaProcessoVO(jurisdicaoId, caixaId, universoPesquisa);
		
		if(universoPesquisa != null && universoPesquisa.getNumeroProcesso() != null) {
			criteriosPesquisa.setNumeroProcesso(universoPesquisa.getNumeroProcesso());
		}
		criteriosPesquisa.setNumeroSequencia(this.numeroSequencia);
		criteriosPesquisa.setDigitoVerificador(this.digitoVerificador);
		criteriosPesquisa.setNumeroAno(this.ano);
		criteriosPesquisa.setNumeroOrgaoJustica(this.getNumeroOrgaoJustica());
		criteriosPesquisa.setNumeroOrigem(this.numeroOrigem);	
		
		criteriosPesquisa.setAssuntoJudicial(this.campoAssunto);
		criteriosPesquisa.setClasseJudicial(this.campoClasse);
		criteriosPesquisa.setIntervaloDataDistribuicao(this.dataDistribuicaoInicial, this.dataDistribuicaoFinal);
		criteriosPesquisa.setPrioridadeObj(this.prioridade);
		criteriosPesquisa.setOrgaoJulgadorObj(this.orgaoJulgador);
		criteriosPesquisa.setOrgaoJulgadorColegiadoObj(this.orgaoJulgadorColegiado);
		criteriosPesquisa.setNomeParte(this.nomeParte);
		criteriosPesquisa.setDocumentoIdentificacaoParte(this.documentoIdentificacaoParte);
		criteriosPesquisa.setOabRepresentanteParte(this.codigoOABRepresentante);
		
		return criteriosPesquisa;
	}	
	
	@SuppressWarnings("rawtypes")
	public TreeNodeImpl getJurisdicoesAcervo() throws PJeBusinessException {
		Integer hashCriteriosPesquisaAtual = (Integer)this.getCriteriosPesquisaAcervoMenuContexto().hashCode();
		
		TreeNodeImpl jurisdicoesPesquisaAtual = null;
		if (mapaJurisdicoesAcervo != null && !mapaJurisdicoesAcervo.isEmpty()) {
			jurisdicoesPesquisaAtual = mapaJurisdicoesAcervo.get(hashCriteriosPesquisaAtual);
		}

		if (jurisdicoesPesquisaAtual == null) {
			jurisdicoesPesquisaAtual = this.obterTreeNodes(processoJudicialManager.getJurisdicoesAcervo(this.getCriteriosPesquisaAcervoMenuContexto()));
			mapaJurisdicoesAcervo.put(hashCriteriosPesquisaAtual, jurisdicoesPesquisaAtual);
		}
		
		this.computaResultadoEncontrado(jurisdicoesPesquisaAtual);
		return jurisdicoesPesquisaAtual;
	}
	
	@SuppressWarnings("rawtypes") 
	private void computaResultadoEncontrado(TreeNodeImpl jurisdicoesPesquisa) {
		this.resultadoEncontrado = (jurisdicoesPesquisa != null);
	}
	
	private long buscaCountCache(Integer idJurisdicao, ConsultaProcessoVO criteriosPesquisa) throws PJeBusinessException {
		Long contagemResultados = null;
		Integer hashCriteriosPesquisaAtual = (Integer)criteriosPesquisa.hashCode();
		
		if (mapaCountProcessosTabela != null && !mapaCountProcessosTabela.isEmpty()) {
			contagemResultados = mapaCountProcessosTabela.get(hashCriteriosPesquisaAtual);
		}
		
		if(contagemResultados == null) {
			contagemResultados = processoJudicialManager.getCountProcessosJurisdicao(idJurisdicao, criteriosPesquisa, null);
			mapaCountProcessosTabela.put(hashCriteriosPesquisaAtual, contagemResultados);
		}
		return contagemResultados;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private TreeNodeImpl<?> obterTreeNodes(List<JurisdicaoVO> jurisdicoesVO) {
		TreeNodeImpl rootNode = new TreeNodeImpl();
		TreeNodeImpl child = null;
		for (JurisdicaoVO jurisdicaoVO : jurisdicoesVO) {
			child = new TreeNodeImpl();
			child.setData(jurisdicaoVO);
			child.setParent(rootNode);
			child.addChild(-1, new TreeNodeImpl());  // Carrega uma caixa fake.
			
			rootNode.addChild(jurisdicaoVO.getId(), child);
		}
		return rootNode;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void carregarCaixasAcervo(JurisdicaoVO jurisdicao) throws PJeBusinessException {
		TreeNode jurisdicaoNode = buscaTreeNodeJurisdicao(jurisdicao);
		// verifica se as caixas ainda não foram carregadas
		if(jurisdicaoNode != null) {
			if(jurisdicaoNode.getChild(-1) != null) {
				List<CaixaAdvogadoProcuradorVO> caixasAcervo = getCaixasJurisdicao(jurisdicao, false);
				
				carregarCaixas(jurisdicao, caixasAcervo);
			}
			else {
				Iterator<TreeNode> children = jurisdicaoNode.getChildren();
				if(caixa != null) {
					while(children.hasNext()) {
						Map.Entry<Object, TreeNode> child = (Entry<Object, TreeNode>) children.next();
						CaixaAdvogadoProcuradorVO cx = (CaixaAdvogadoProcuradorVO)child.getValue().getData();
						if(cx != null) {
							if(caixa.getId() == cx.getId()) {
								child.getValue().setData(caixa);
								break;
							}
						}
					}				
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private TreeNode buscaTreeNodeJurisdicao(JurisdicaoVO jurisdicao) {
		TreeNode jurisdicaoNode = null;
		TreeNodeImpl jurisdicoesPesquisaAtual = null;
		
		try {
			jurisdicoesPesquisaAtual = this.treeNodeJurisdicoes;
			if(this.treeNodeJurisdicoes == null) {
				jurisdicoesPesquisaAtual = getJurisdicoesAcervo();
			}
		} 
		catch (PJeBusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jurisdicaoNode = jurisdicoesPesquisaAtual.getChild(jurisdicao.getId());
		return jurisdicaoNode;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void carregarCaixas(JurisdicaoVO jurisdicao, List<CaixaAdvogadoProcuradorVO> caixas) throws PJeBusinessException {
		TreeNode jurisdicaoNode = buscaTreeNodeJurisdicao(jurisdicao);
		if(jurisdicaoNode != null) {
			if(caixas == null || caixas.size() == 0) {
				caixas = refreshCaixas();
			}
			deleteNodeChildren(jurisdicaoNode);
			
			TreeNodeImpl child = null;
			for (CaixaAdvogadoProcuradorVO caixaAdvogadoProcuradorVO : caixas) {
				child = new TreeNodeImpl();
				child.setData(caixaAdvogadoProcuradorVO);
				child.setParent(jurisdicaoNode);
				jurisdicaoNode.addChild(caixaAdvogadoProcuradorVO.getId(), child);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void deleteNodeChildren(TreeNode parent) {
		if(parent == null) {
			return;
		}
		Iterator<TreeNode> children = parent.getChildren();
		while(children.hasNext()) {
			children.next();
			children.remove();
		}
	}
		
	public JurisdicaoVO getJurisdicao() {
		return jurisdicao;
	}

	public void setJurisdicao(JurisdicaoVO jurisdicao) {
		this.jurisdicao = (jurisdicao == null || jurisdicao.getId() == 0) ? null : jurisdicao; 
	}
	
	public CaixaAdvogadoProcuradorVO getCaixa() {
		return caixa;
	}

	public void setCaixa(CaixaAdvogadoProcuradorVO caixa) {
		this.caixa = caixa; 
	}
	
	public void atualizarCaixa() throws PJeBusinessException {
		refreshCaixa();
		carregarCaixasAcervo(jurisdicao);
	}

	public List<CaixaAdvogadoProcuradorVO> refreshCaixas() {
		List<CaixaAdvogadoProcuradorVO> caixas = new ArrayList<CaixaAdvogadoProcuradorVO>(0);
		List<CaixaAdvogadoProcurador> cx = caixaAdvogadoProcuradorManager.list(jurisdicao.getId(), this.idLocalizacaoAtual);
		for (CaixaAdvogadoProcurador c : cx) {
			CaixaAdvogadoProcuradorVO vo = this.montaCaixaZerada(c);
			if(caixa != null && caixa.getId() == vo.getId()) {
				setCaixa(vo);
			}
			caixas.add(vo);
		}
		return caixas;
	}

	
	private void refreshCaixa() {
		if(caixa != null)
		{
			try {
				CaixaAdvogadoProcurador cx = ComponentUtil.getComponent(CaixaAdvogadoProcuradorManager.class).findById(caixa.getId());
				CaixaAdvogadoProcuradorVO vo = this.montaCaixaZerada(cx); 
				setCaixa(vo);
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
		}
	}
	
	private CaixaAdvogadoProcuradorVO montaCaixaZerada(CaixaAdvogadoProcurador c) {
		return new CaixaAdvogadoProcuradorVO(
				c.getIdCaixaAdvogadoProcurador(), 
				c.getNomeCaixaAdvogadoProcurador(), 
				c.getDsCaixaAdvogadoProcurador(), 
				jurisdicao.getId(), 
				jurisdicao.getDescricao(), 
				Boolean.TRUE, 
				Boolean.TRUE, 
				BigInteger.ZERO); 
		
	}	
			
	/**
	 * Recupera a ordenação definida para a pesquisa.
	 * 
	 * @return a ordenação.
	 */
	public String getOrdenacao() {
		return ordenacao;
	}
	
	
	/**
	 * Recupera a lista de prioridades ativas da instalação.
	 * 
	 * @return a lista de prioridades
	 */
	public List<PrioridadeProcesso> getPrioridades(){
		try {
			return ComponentUtil.getComponent(PrioridadeProcessoManager.class).listActive();
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar recuperar as listas de prioridades: {0}.", e.getLocalizedMessage());
			return Collections.emptyList();
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<CaixaAdvogadoProcuradorVO> listaCaixasDisponiveisParaSelecionar() throws PJeBusinessException{
		this.listaDeCaixasParaSelecao = new ArrayList<CaixaAdvogadoProcuradorVO>();
		if(this.jurisdicao == null) {
			return new ArrayList<CaixaAdvogadoProcuradorVO>();
		}
		
		List<CaixaAdvogadoProcuradorVO> caixasTemp = this.getCaixasJurisdicao(this.jurisdicao, true);
		// clona o arrayList para não alterar seus valores originais
		this.listaDeCaixasParaSelecao = new ArrayList(caixasTemp);
		
		// remove a caixa atualmente selecionada da lista
		if(this.caixa != null) {
			listaDeCaixasParaSelecao.remove(this.caixa);
		}
		return this.listaDeCaixasParaSelecao;
	}
			
	/**
	 * Remove o processo de caixas - ele voltará para a caixa de entrada
	 * @param jurisdicaoVO
	 * @param listaProcessos
	 * @param forcarAlteracaCaixa - se true, faz a movimentação também do expediente independente se ele está ou não em uma caixa diferente
	 */
	private void moverProcessosParaJurisdicao(JurisdicaoVO jurisdicaoVO, List<ProcessoTrf> listaProcessos, Boolean forcarAlteracaCaixa){
		if (this.caixa == null) {
			facesMessages.add(Severity.ERROR, "Processos não estão em uma caixa.");
			return;
		}			
		if (this.caixa.getIdJurisdicao() != jurisdicaoVO.getId()) {
			facesMessages.add(Severity.ERROR, "Jurisdição de destino diferente da caixa dos processos.");
			return;
		}
		if(listaProcessos == null || listaProcessos.size() < 1) {
			facesMessages.add("Nenhum processo foi selecionado para movimentao.");
			return;
		}
		
		try {
			ProcessoParteExpedienteCaixaAdvogadoProcuradorManager processoParteExpedienteCaixaAdvogadoProcuradorManager = 
					ProcessoParteExpedienteCaixaAdvogadoProcuradorManager.instance();

			ProcessoCaixaAdvogadoProcuradorManager processoCaixaAdvogadoProcuradorManager = 
					ProcessoCaixaAdvogadoProcuradorManager.instance();

			CaixaAdvogadoProcurador caixaOrigemProcesso = caixaAdvogadoProcuradorManager.findById(this.caixa.getId());
			
			if(caixaOrigemProcesso != null && caixaOrigemProcesso.getIdCaixaAdvogadoProcurador() != null) {
				processoCaixaAdvogadoProcuradorManager.remover(caixaOrigemProcesso, true, 
						(ProcessoTrf[])listaProcessos.toArray(new ProcessoTrf[listaProcessos.size()]));
				
				if(forcarAlteracaCaixa) {
					CaixaAdvogadoProcurador caixaOrigemExpediente;
					for (ProcessoTrf processoMovimentado: listaProcessos) {	
						List<ProcessoParteExpediente> listaProcessoParteExpediente = this.getExpedientesDoProcesso(jurisdicaoVO.getId(), processoMovimentado);
						
						for (ProcessoParteExpediente ppe: listaProcessoParteExpediente) {
							caixaOrigemExpediente = null;
							
							PesquisaExpedientesVO criteriosPesquisa = new PesquisaExpedientesVO();
							criteriosPesquisa.setApenasCaixasComResultados(true);
							criteriosPesquisa.setNumeroProcesso(processoMovimentado.getNumeroProcesso());
							criteriosPesquisa.setIdProcessoParteExpediente(ppe.getIdProcessoParteExpediente());
							if(this.jurisdicao != null){
								criteriosPesquisa.setIdJurisdicao(this.jurisdicao.getId());					
							}
							
							// serão buscadas apenas as caixas que tenham o expediente e que o usuário tenha permissão sobre elas
							List<CaixaAdvogadoProcuradorVO> caixasOrigemExpedientes = caixaAdvogadoProcuradorManager.obterCaixasExpedientesJurisdicao(jurisdicao.getId(), criteriosPesquisa);
							
							if (caixasOrigemExpedientes != null && !caixasOrigemExpedientes.isEmpty()) {
								// assume-se que o expediente esteja no máximo em 1 caixa p/ o usuário atual
								caixaOrigemExpediente = caixaAdvogadoProcuradorManager.findById(caixasOrigemExpedientes.get(0).getId());
							}
							
							// move os processos se estiverem na mesma caixa do expediente ou se o usuário mandou forçar a alteração
							processoParteExpedienteCaixaAdvogadoProcuradorManager.remover(caixaOrigemExpediente, true, ppe);					
						}
					}
				}
			}

			this.finalizaMoverProcessos();
			
			facesMessages.add(Severity.INFO,
					"{0} processos(s) removidos(s) de \"{1}\"",
					listaProcessos.size(),
					this.caixa.getNomeJurisdicao() + " > " + this.caixa.getNome());

			
		} catch (PJeBusinessException e) {
			resetSelecionados();
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar mover o(s) processo(s): {0}.", e.getLocalizedMessage());
		}
	}
	
	private CaixaAdvogadoProcurador getCaixaProcesso(Integer idJurisdicao, ProcessoTrf processo, CaixaAdvogadoProcurador caixaDestino) throws PJeBusinessException {
		CaixaAdvogadoProcurador caixaProcesso = null;
		
		ConsultaProcessoVO criteriosPesquisa = new ConsultaProcessoVO();
		criteriosPesquisa.setApenasCaixasComResultados(true);
		criteriosPesquisa.setNumeroProcesso(processo.getNumeroProcesso());
		
		List<CaixaAdvogadoProcuradorVO> caixasProcesso = caixaAdvogadoProcuradorManager.obterCaixasAcervoJurisdicao(idJurisdicao, criteriosPesquisa);

		if (caixasProcesso != null && !caixasProcesso.isEmpty()) {
			for (int i =0; i <  caixasProcesso.size(); i++) {
				CaixaAdvogadoProcurador caixaProcessoTeste = caixaAdvogadoProcuradorManager.findById(caixasProcesso.get(i).getId());
				if(!caixaProcessoTeste.equals(caixaDestino)) {
					caixaProcesso = caixaAdvogadoProcuradorManager.findById(caixasProcesso.get(i).getId());
				}
			}
		}
		return caixaProcesso;
	}
	
	private List<ProcessoParteExpediente> getExpedientesDoProcesso(Integer idJurisdicao, ProcessoTrf processo) throws PJeBusinessException {
		PesquisaExpedientesVO criteriosPesquisaExpedientes = new PesquisaExpedientesVO();
		criteriosPesquisaExpedientes.setNumeroProcesso(processo.getNumeroProcesso());
		
		criteriosPesquisaExpedientes.setIdJurisdicao(idJurisdicao);					
		
		return ProcessoParteExpedienteManager.instance().getExpedientes(criteriosPesquisaExpedientes, null);
	}
	
	/**
	 * Move o processo para uma caixa identificada 
	 * 
	 * @param caixaDestinoVO
	 * @param listaProcessos
	 * @param forcarAlteracaCaixa - forçar a alteração dos expedientes vinculados, independente se estão ou não em caixas distintas
	 */
	private void moverProcessosParaCaixa(CaixaAdvogadoProcuradorVO caixaDestinoVO, List<ProcessoTrf> listaProcessos, Boolean forcarAlteracaCaixa) {
		try {
			CaixaAdvogadoProcurador caixaDestino = caixaAdvogadoProcuradorManager.findById(caixaDestinoVO.getId());
			Boolean listaLimitada = false;
			List<ProcessoTrf> listaProcessosUtilizada;
			if(listaProcessos.size() > Constantes.MAX_DISTRIBUICOES_PROCESSOS_LOTE) {
				listaProcessosUtilizada = listaProcessos.subList(0, Constantes.MAX_DISTRIBUICOES_PROCESSOS_LOTE);
				listaLimitada = true;
			}else {
				listaProcessosUtilizada = listaProcessos;	
			}
			for(ProcessoTrf processo : listaProcessosUtilizada){
				this.moverProcessoParaCaixa(
						jurisdicao,
						processo,
						caixaDestino,
						forcarAlteracaCaixa);
			}
			
			if(listaProcessosUtilizada.size() == 1) {
				facesMessages.add(Severity.INFO, "Processo movido para \"{0}\"", caixaDestinoVO.getNomeJurisdicao() + " > " + caixaDestinoVO.getNome());
			}else {
				facesMessages.add(Severity.INFO, "{0} processos movidos para \"{1}\"", listaProcessosUtilizada.size(), caixaDestinoVO.getNomeJurisdicao() + " > " + caixaDestinoVO.getNome());
				if(listaLimitada) {
					facesMessages.add(Severity.WARN, "Apesar de terem sido selecionados {0} processos para serem movidos, "
							+ "o limite do sistema é de {1}. Para mover todos os processos repita a operação com os itens pendentes.",
							listaProcessos.size(), Constantes.MAX_DISTRIBUICOES_PROCESSOS_LOTE);
				}
			}
		} catch (PJeBusinessException e) {
			resetSelecionados();
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
		}
	}
	
	private void moverProcessoParaCaixa(JurisdicaoVO jurisdicao, ProcessoTrf processo, CaixaAdvogadoProcurador caixaDestino, Boolean forcarAlteracaoCaixa){
		try {
			ProcessoCaixaAdvogadoProcuradorManager processoCaixaAdvogadoProcuradorManager = ProcessoCaixaAdvogadoProcuradorManager.instance();
			CaixaAdvogadoProcuradorManager caixaAdvogadoProcuradorManager = CaixaAdvogadoProcuradorManager.instance();
			ProcessoParteExpedienteCaixaAdvogadoProcuradorManager processoParteExpedienteCaixaAdvogadoProcuradorManager = ProcessoParteExpedienteCaixaAdvogadoProcuradorManager.instance();
	
			CaixaAdvogadoProcurador caixaOrigemProcesso = null;
			CaixaAdvogadoProcurador caixaOrigemExpediente = null;
	
			caixaOrigemProcesso = this.getCaixaProcesso(jurisdicao.getId(), processo, caixaDestino);
			Util.beginTransaction();
			processoCaixaAdvogadoProcuradorManager.incluirEmCaixa(caixaDestino, caixaOrigemProcesso, new ProcessoTrf[]{processo});
			if(forcarAlteracaoCaixa) {
				List<ProcessoParteExpediente> listaProcessoParteExpediente = this.getExpedientesDoProcesso(jurisdicao.getId(), processo);
				for (ProcessoParteExpediente ppe: listaProcessoParteExpediente) {
					caixaOrigemExpediente = null;
					
					PesquisaExpedientesVO criteriosPesquisa = new PesquisaExpedientesVO();
					criteriosPesquisa.setApenasCaixasComResultados(true);
					criteriosPesquisa.setNumeroProcesso(processo.getNumeroProcesso());
					criteriosPesquisa.setIdProcessoParteExpediente(ppe.getIdProcessoParteExpediente());
					if(jurisdicao != null){
						criteriosPesquisa.setIdJurisdicao(jurisdicao.getId());
					}
					
					// serão buscadas apenas as caixas que tenham o expediente e que o usuário tenha permissão sobre elas
					List<CaixaAdvogadoProcuradorVO> caixasOrigemExpedientes = caixaAdvogadoProcuradorManager.obterCaixasExpedientesJurisdicao(jurisdicao.getId(), criteriosPesquisa);
					
					if (caixasOrigemExpedientes != null && !caixasOrigemExpedientes.isEmpty()) {
						// assume-se que o expediente esteja no máximo em 1 caixa p/ o usuário atual
						caixaOrigemExpediente = caixaAdvogadoProcuradorManager.findById(caixasOrigemExpedientes.get(0).getId());
					}
					processoParteExpedienteCaixaAdvogadoProcuradorManager.incluirEmCaixa(caixaDestino, caixaOrigemExpediente, ppe);
				}
			}
            HibernateUtil.getSession().flush();
            Util.commitTransction();
		} catch (PJeBusinessException e) {
			this.resetSelecionados();
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
		}
	}
	
	public void comboMoverParaCaixaSelecionada() throws PJeBusinessException {
		CaixaAdvogadoProcuradorVO caixaVO = this.getCaixaSelecionada();
		if(caixaVO != null) {
			List<ProcessoTrf> listaProcessos = carregaListaSelecionados();

			if(caixaVO.getPadrao()) {
				this.moverProcessosParaJurisdicao(caixaVO.getJurisdicao(), listaProcessos, this.getForcarAlteracaoParaProcessos());
			}else {
				if(ComponentUtil.getComponent(CaixaAdvogadoProcuradorManager.class).isCaixaAtiva(caixaVO.getId())) {
					this.moverProcessosParaCaixa(caixaVO, listaProcessos, this.getForcarAlteracaoParaProcessos());
					this.finalizaMoverProcessos();
				}else {
					facesMessages.add(Severity.INFO, "Não foi possível mover o processo, pois a caixa [{0}] está inativa.", caixaVO.getNome());
				}
			}
		}
	}

	private List<ProcessoTrf> carregaListaSelecionados() throws PJeBusinessException{
		List <ProcessoTrf> listaCarregada = new ArrayList<ProcessoTrf>();
		for(ProcessoTrf procSelecionado: this.listaSelecionados){
			listaCarregada.add(processoJudicialManager.findById(procSelecionado.getIdProcessoTrf()));
		}
		
		return listaCarregada;
	}
	
	public List<ProcessoTrf> getListaSelecionados(){
		return this.listaSelecionados;
	}
	
	/**
	 * Cria uma nova caixa a partir dos parâmetros repassados à action.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void criarNovaCaixa() {
		if (StringUtils.isBlank(nomeNovaCaixa)) {
			facesMessages.add(Severity.ERROR, "Nome da caixa inválido.");
			criarCaixa = false;
			return;
		}else if (jurisdicao == null) {
			facesMessages.add(Severity.ERROR, "Não é possível criar uma nova caixa sem a prévia escolha de uma jurisdição.");
			criarCaixa = false;
			return;
		}
		try {
			CaixaAdvogadoProcuradorVO caixaVO = caixaAdvogadoProcuradorManager.criarNovaCaixa(StringUtil.fullTrim(nomeNovaCaixa),
					this.jurisdicao);
			
			TreeNode jurisdicaoNode = buscaTreeNodeJurisdicao(jurisdicao);
			
			if(jurisdicaoNode != null && jurisdicaoNode.getChildren().hasNext()) {
				TreeNodeImpl child = new TreeNodeImpl();
				child.setData(caixaVO);
				child.setParent(jurisdicaoNode);
				jurisdicaoNode.addChild(caixaVO.getId(), child);
				
				jurisdicaoNode = this.reorderTreeNodeCaixas(jurisdicaoNode);
			}
			else {
				this.carregarCaixas(jurisdicao, Arrays.asList(caixaVO));				
			}
			// limpa o cache de caixas da jurisdicao, pois tem que ser buscado novamente do banco
			this.mapaCaixasJurisdicao = new HashMap<Integer, List<CaixaAdvogadoProcuradorVO>>();

			this.lancaEventoAlteracaoPainel();
			facesMessages.add(Severity.INFO, "Caixa [{0}] criada com sucesso.", nomeNovaCaixa);
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Erro ao tentar criar a caixa: {0}", e.getLocalizedMessage());
		}
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private TreeNode reorderTreeNodeCaixas(TreeNode jurisdicaoNode){
		if(jurisdicaoNode != null && jurisdicaoNode.getChildren() != null){
			Iterator<TreeNode> children = jurisdicaoNode.getChildren();
			Map<Integer, CaixaAdvogadoProcuradorVO> mapOrderedChild = new HashMap<Integer, CaixaAdvogadoProcuradorVO>();
			Integer numElement = 0;
			while (children.hasNext()) {
				Map.Entry<Object, TreeNode> child = (Entry<Object, TreeNode>) children.next();
				CaixaAdvogadoProcuradorVO cx = (CaixaAdvogadoProcuradorVO)child.getValue().getData();
				// ignora a caixa de entrada para a ordenação
				if(!cx.getPadrao()) {
					mapOrderedChild.put(numElement, cx);
					children.remove();
					numElement++;;
				}
			}
			
			Integer k = 0;
			for(Integer i=0; i < numElement; i++) {
				k = i;
				CaixaAdvogadoProcuradorVO cx = mapOrderedChild.get(i);
				if(cx != null) {
					for(Integer j=0; j < numElement; j++) {
						if(i != j){
							CaixaAdvogadoProcuradorVO cxTenant = mapOrderedChild.get(j);
							if(cxTenant != null) {
								if(StringUtil.normalize(cx.getNome().toLowerCase()).compareTo(StringUtil.normalize(cxTenant.getNome().toLowerCase())) > 0) {
									cx = cxTenant;
									k = j;
								}
							}
						}
					}
					TreeNodeImpl child = new TreeNodeImpl();
					child.setData(cx);
					child.setParent(jurisdicaoNode);
					jurisdicaoNode.addChild(cx.getId(), child);

					mapOrderedChild.remove(k);
					if(k != i) {
						i--;
					}
				}
			}
		}
		
		return jurisdicaoNode;
	}

	@SuppressWarnings("rawtypes")
	private void limparCacheAcervo() {
		mapaJurisdicoesAcervo = new HashMap<Integer, TreeNodeImpl>();
		mapaCountProcessosTabela = new HashMap<Integer, Long>();
		mapaCaixasJurisdicao = new HashMap<Integer, List<CaixaAdvogadoProcuradorVO>>();
		
		this.getProcessosAcervo().setRefreshPage(Boolean.TRUE);
	}
		
	private void fecharArvore(UITree tree) {
		if (tree != null) {
			TreeState componentState = (TreeState)tree.getComponentState();
			try {
				componentState.collapseAll(tree);
			} catch (IOException e) {
				// Nada a fazer.
			}
		}
	}
	
	private void finalizaMoverProcessos() throws PJeBusinessException {		
		resetSelecionados();
		limparCacheAcervo();
		this.carregarCaixasAcervo(jurisdicao);
		
		this.lancaEventoAlteracaoPainel();
	}
	
	public void lancaEventoAlteracaoPainel() {
		Map<String, Object> parametros = new HashMap<String, Object>();
		parametros.put(Constantes.COD_PARAMETRO_ABA_ALTERCAO, nomeAba);
		Events.instance().raiseEvent(Eventos.EVENTO_ALTERACAO_CONTEUDO_PAINEL_EXTERNO, parametros);
	}
	
	@Observer(Eventos.EVENTO_ALTERACAO_CONTEUDO_PAINEL_EXTERNO)
	public void observaAlteracaoConteudoPainel(Map<String, Object> parametrosEvento) {
		String abaAlteracao = (String) parametrosEvento.get(Constantes.COD_PARAMETRO_ABA_ALTERCAO);
		if(!abaAlteracao.equals(nomeAba)) {
			this.recarregarPagina();
		}
	}
		
	public void recarregarPagina() {
		this.limparCacheAcervo();
		this.inputConsultaContextoAcervo = null;
		this.limpaPesquisaTabela();		
		this.jurisdicao = null;
		this.caixa = null;

		this.resetSelecionados();
		
		this.getProcessosAcervo().setRefreshPage(Boolean.TRUE);
		this.fecharArvore(richTreeAcervo);
	}	
	
	/**
	 * Redireciona o usuário à página de edição de caixas.
	 * 
	 */
	public void redirecionarEdicaoCaixa(){
		if(caixa == null){
			facesMessages.add(Severity.ERROR, "É necessário selecionar a caixa antes de sua edição.");
			return;
		}
	}
	
	/**
	 * Remove uma caixa, excluindo as vinculações de todos os respectivos processos.
	 */
	public void removerCaixa(){
		if(caixa == null || jurisdicao == null){
			facesMessages.add(Severity.ERROR, "É necessário selecionar a caixa antes de sua remoção.");
			return;
		}
		try {
			CaixaAdvogadoProcuradorManager caixaAdvogadoProcuradorManager = ComponentUtil.getComponent(CaixaAdvogadoProcuradorManager.class);
			CaixaAdvogadoProcurador cx = caixaAdvogadoProcuradorManager.findById(caixa.getId());
			
			// Remove os processos da caixa
			Search s = new Search(ProcessoCaixaAdvogadoProcurador.class);
			s.setDistinct(true);
			s.addCriteria(Criteria.equals("caixaAdvogadoProcurador", cx));
			ProcessoCaixaAdvogadoProcuradorManager processoCaixaAdvogadoProcuradorManager = ComponentUtil.getComponent(ProcessoCaixaAdvogadoProcuradorManager.class);
			List<ProcessoCaixaAdvogadoProcurador> tags = processoCaixaAdvogadoProcuradorManager.list(s); 
			for(ProcessoCaixaAdvogadoProcurador tag: tags){
				processoCaixaAdvogadoProcuradorManager.remove(tag);
			}
			// Remove os expedientes da caixa
			Search sPpe = new Search(ProcessoParteExpedienteCaixaAdvogadoProcurador.class);
			sPpe.setDistinct(true);
			sPpe.addCriteria(Criteria.equals("caixaAdvogadoProcurador", cx));
			ProcessoParteExpedienteCaixaAdvogadoProcuradorManager processoParteExpedienteCaixaAdvogadoProcuradorManager = ComponentUtil.getComponent(ProcessoParteExpedienteCaixaAdvogadoProcuradorManager.class);
			List<ProcessoParteExpedienteCaixaAdvogadoProcurador> cxsPpe = processoParteExpedienteCaixaAdvogadoProcuradorManager.list(sPpe);
			for(ProcessoParteExpedienteCaixaAdvogadoProcurador ppeCap : cxsPpe){
				processoParteExpedienteCaixaAdvogadoProcuradorManager.remove(ppeCap);
			}
			// Remove a caixa
			caixaAdvogadoProcuradorManager.remove(cx);
			caixaAdvogadoProcuradorManager.flush();

			this.limparCacheAcervo();

			this.carregarCaixasAcervo(jurisdicao);
			this.selecionaJurisdicaoCaixa(this.jurisdicao, this.montarCaixaDeEntrada(this.jurisdicao));
			this.setCaixa(null);
			this.lancaEventoAlteracaoPainel();
			facesMessages.add(Severity.INFO, "Caixa [{0}] removida com sucesso.", cx.getNomeCaixaAdvogadoProcurador());
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar remover a caixa: {0}.", e.getLocalizedMessage());
		} catch (NoSuchFieldException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar remover a caixa: {0}.", e.getLocalizedMessage());
		}
	}
	
	/**
	 * Efetua distribuição automática de expedientes ou processos que foram selecionados
	 * utilizando critérios de filtros da caixa de procurador/advogado. 
	 */
	public void distribuirUtilizandoFiltro() {
		try {
			/* Força para retirar a seleção de alguma caixa caso tenha alguma selecionada */
			caixa = null; 
			
			/* Recupera as caixas ativas da jurisdicao na localizacao atual - ordenados pelo nome da caixa. */
			List<CaixaAdvogadoProcuradorVO> caixas = this.getCaixasJurisdicao(jurisdicao, true);
			
			if(caixas.size() == 0) {
				facesMessages.add(Severity.INFO, "Nenhum processo distribuído. Não há caixas ativas nessa jurisdição.");
				return;
			}

			Boolean houveProcessosParaMover = false;
			CaixaAdvogadoProcuradorAction caixaAction = (CaixaAdvogadoProcuradorAction) Component.getInstance(CaixaAdvogadoProcuradorAction.class);

			// Percorre a lista de caixas disponíveis na jurisdição, buscando encontrar os processos que se enquadram nos seus filtros para movê-los
			for(CaixaAdvogadoProcuradorVO caixaDestinoVO : caixas) {
				// não utiliza a caixa padrão / caixa de entrada
				if(!caixaDestinoVO.getPadrao()) {
					CaixaAdvogadoProcurador caixaDestino = caixaAdvogadoProcuradorManager.findById(caixaDestinoVO.getId());
					List<ProcessoTrf> processos = caixaAction.processosCorrespondentesFiltroCaixa(caixaDestino);
					
					if(!processos.isEmpty()) {
						this.moverProcessosParaCaixa(caixaDestinoVO, processos, true); // move processos para a caixa forçando o deslocamento dos expedientes associados
						houveProcessosParaMover = true;
					}
				}
			}
			if(houveProcessosParaMover) {
				this.finalizaMoverProcessos();
			}else {
				facesMessages.add(Severity.INFO, "Nenhum processo distribuído. Não há processos que se encaixam nos filtros das caixas ou não há filtro configurado.");
			}
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar executar distribuição automática por filtro: {0}.", e.getLocalizedMessage());
		}
		
		/* Efetua refresh na listagem de processos da jurisdição */
		pesquisarAcervo(); 
	}
	
	/**
	 * Indica se houve determinação de exibir o formulário de criação de caixa, o que é definido 
	 * pelo parâmetro {@link #ccx_}.
	 * 
	 * @return true, se o formulário deve ser exibido.
	 */
	public Boolean getCriarCaixa() {
		return criarCaixa;
	}

	public ExpedicaoExpedienteEnum getMeioComunicacao() {
		return meioComunicacao;
	}

	public void setMeioComunicacao(ExpedicaoExpedienteEnum meioComunicacao) {
		this.meioComunicacao = meioComunicacao;
	}

	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}
		
	/* Getters e setters*/
	
	public String getCampoAssunto() {
		return campoAssunto;
	}
	
	public void setCampoAssunto(String campoAssunto) {
		this.campoAssunto = campoAssunto;
	}

	public String getCampoClasse() {
		return campoClasse;
	}

	public void setCampoClasse(String campoClasse) {
		this.campoClasse = campoClasse;
	}

	public PrioridadeProcesso getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(PrioridadeProcesso prioridade) {
		this.prioridade = prioridade;
	}

	public Date getDataDistribuicaoFinal() {
		return dataDistribuicaoFinal;
	}
	
	public void setDataDistribuicaoFinal(Date dataDistribuicaoFinal) {
		this.dataDistribuicaoFinal = dataDistribuicaoFinal;
	}

	public Date getDataDistribuicaoInicial() {
		return dataDistribuicaoInicial;
	}

	public void setDataDistribuicaoInicial(Date dataDistribuicaoInicial) {
		this.dataDistribuicaoInicial = dataDistribuicaoInicial;
	}
	
	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getDocumentoIdentificacaoParte() {
		return documentoIdentificacaoParte;
	}

	public void setDocumentoIdentificacaoParte(String documentoIdentificacaoParte) {
		this.documentoIdentificacaoParte = documentoIdentificacaoParte;
	}

	public String getCodigoOABRepresentante() {
		return codigoOABRepresentante;
	}

	public void setCodigoOABRepresentante(String codigoOABRepresentante) {
		this.codigoOABRepresentante = codigoOABRepresentante;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}	
	
	public String getNomeNovaCaixa() {
		return nomeNovaCaixa;
	}
	
	public void setNomeNovaCaixa(String nomeNovaCaixa) {
		this.nomeNovaCaixa = nomeNovaCaixa;
	}
	
	public String getSelectedTab() {
		return selectedTab;
	}

	public void setSelectedTab(String selectedTab) {
		this.selectedTab = selectedTab;
	}
	
	public TipoSituacaoExpedienteEnum[] getSituacoesExpedientes() {
		return TipoSituacaoExpedienteEnum.values();
	}
	
	public boolean getCxEdit() {
		return cxEdit;
	}

	public void setCxEdit(boolean cxEdit) {
		this.cxEdit = cxEdit;
	}

	public boolean getCxRemove() {
		return cxRemove;
	}

	public void setCxRemove(boolean cxRemove) {
		this.cxRemove = cxRemove;
	}

	public String getNomePainel(){
		StringBuilder textoPainel = new StringBuilder();
		textoPainel.append("Painel do ");
		if(idProcuradoriaAtual != null){
			if(tipoProcuradoriaAtual.equals(TipoProcuradoriaEnum.P)) {
				textoPainel.append("Procurador").toString();
			}
			else if(tipoProcuradoriaAtual.equals(TipoProcuradoriaEnum.D)) {
				textoPainel.append("Defensor").toString();
			}
		}
		else {
			textoPainel.append("Advogado").toString();
		}
		return textoPainel.toString();
	}
	
	private boolean checkboxSelectAll = false;
	
	private List<ProcessoTrf> listaSelecionados;
	
	private CaixaAdvogadoProcuradorVO caixaSelecionada;

	private Integer idCaixaSelecionada;
	
	
	public Integer getIdCaixaSelecionada() {
		return idCaixaSelecionada;
	}

	public void setIdCaixaSelecionada(Integer idCaixaSelecionada) throws PJeBusinessException {
		this.idCaixaSelecionada = idCaixaSelecionada;
		this.identificaCaixaSelecionada(idCaixaSelecionada);
	}

	private List<CaixaAdvogadoProcuradorVO> listaDeCaixasParaSelecao;
	
	private Boolean forcarAlteracaoParaProcessos = true;

	public boolean isCheckboxSelectAll() {
		return checkboxSelectAll;
	}

	public void setCheckboxSelectAll(boolean checkboxSelectAll) {
		this.checkboxSelectAll = checkboxSelectAll;
	}
	
	public CaixaAdvogadoProcuradorVO getCaixaSelecionada() {
		return caixaSelecionada;
	}
	
	private void identificaCaixaSelecionada(Integer idCaixaSelecionada) throws PJeBusinessException {
		CaixaAdvogadoProcuradorVO cxSelecionada = null;
		if(this.getListaDeCaixasParaSelecao() != null) {
			for(CaixaAdvogadoProcuradorVO cx: this.getListaDeCaixasParaSelecao()) {
				if(cx.getId() == idCaixaSelecionada) {
					cxSelecionada = cx;
					break;
				}
			}
		}
		this.setCaixaSelecionada(cxSelecionada);
	}

	public void setCaixaSelecionada(CaixaAdvogadoProcuradorVO caixaSelecionada) {
		this.caixaSelecionada = caixaSelecionada;
	}
	
	public List<CaixaAdvogadoProcuradorVO> getListaDeCaixasParaSelecao() throws PJeBusinessException {
		return listaDeCaixasParaSelecao;
	}

	public Boolean getForcarAlteracaoParaProcessos() {
		return forcarAlteracaoParaProcessos;
	}

	public void setForcarAlteracaoParaProcessos(Boolean forcarAlteracaoParaProcessos) {
		this.forcarAlteracaoParaProcessos = forcarAlteracaoParaProcessos;
	}

	public void selectAll() {
		if(this.isCheckboxSelectAll() == Boolean.FALSE){
			this.resetSelecionados();
		}else{
			listaSelecionados.addAll(this.getProcessosAcervo().page);
		}
	}
	
	public void selectOne(ProcessoTrf processo){
		if(this.isSelecionado(processo)){
			listaSelecionados.remove(processo);
		}else{
			listaSelecionados.add(processo);
		}
	}
	
	public Boolean isSelecionado(ProcessoTrf item) {
		if(item != null) {
			if(this.listaSelecionados == null || this.listaSelecionados.isEmpty()){
				this.listaSelecionados = new ArrayList<ProcessoTrf>();
				return false;
			}else {
				return this.listaSelecionados.contains(item);
			}
		}
		return false;
	}
		
	public void resetSelecionados(){
		this.listaSelecionados = null;
		this.checkboxSelectAll = false;
	}

	public Map<Integer,BigInteger> getContadoresAcervo() {
		return contadoresAcervo;
	}
	
	@SuppressWarnings("unchecked")
	public Map<Integer,BigInteger> retornaMapaExpedientes(){
		return (Map<Integer,BigInteger>)Contexts.getApplicationContext().get("contadorExpedientesCaixas");
	}
	
	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}
	
	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	public Integer getDigitoVerificador() {
		return digitoVerificador;
	}

	public void setDigitoVerificador(Integer digitoVerificador) {
		this.digitoVerificador = digitoVerificador;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public String getRamoJustica() {
		return ramoJustica;
	}

	public void setRamoJustica(String ramoJustica) {
		this.ramoJustica = ramoJustica;
	}

	public String getRespectivoTribunal() {
		return respectivoTribunal;
	}

	public void setRespectivoTribunal(String respectivoTribunal) {
		this.respectivoTribunal = respectivoTribunal;
	}

	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}

	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}
	
	public Integer getNumeroOrgaoJustica() {
		try {
			return Integer.parseInt(this.ramoJustica + this.respectivoTribunal);
		} catch (NumberFormatException ex) {
			return null;
		}
	}
	
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public void pesquisarContextoAcervo() {
		this.limpaPesquisaTabela();
		this.caixa = null;
		this.jurisdicao = null;
	}
	
	public void pesquisarAcervo() {
		mapaCountProcessosTabela = new HashMap<Integer, Long>();
		this.getProcessosAcervo().setRefreshPage(Boolean.TRUE);
	}
	
	public void selecionaJurisdicao(JurisdicaoVO jurisdicao) throws PJeBusinessException {
		this.selecionaJurisdicaoCaixa(jurisdicao, this.montarCaixaDeEntrada(jurisdicao));
		this.carregarCaixasAcervo(jurisdicao);
	}

	public void selecionaJurisdicaoCaixa(JurisdicaoVO jurisdicao, CaixaAdvogadoProcuradorVO caixa) {
		setJurisdicao(jurisdicao);
		setCaixa(caixa);
		setOrdenacao(this.ordenacao);
		this.limpaPesquisaTabela();
		this.getProcessosAcervo().setRefreshPage(Boolean.TRUE);
	}
		
	public boolean getPesquisaAtivada() {
		return getCriteriosPesquisaProcessos().isPesquisaAlterada();
	}
	
	public String getInputConsultaContextoAcervo() {
		return inputConsultaContextoAcervo;
	}
	
	public void setInputConsultaContextoAcervo(String inputConsultaContextoAcervo) {
		if(inputConsultaContextoAcervo.equals("")){
			this.inputConsultaContextoAcervo = null;
		} else {
			this.inputConsultaContextoAcervo = inputConsultaContextoAcervo;			
		}
	}
	
	private ConsultaProcessoVO getCriteriosPesquisaAcervoMenuContexto() {
		ConsultaProcessoVO criteriosPesquisaAcervoMenuContexto = new ConsultaProcessoVO();

		criteriosPesquisaAcervoMenuContexto.setNumeroProcesso(this.inputConsultaContextoAcervo);

        return criteriosPesquisaAcervoMenuContexto;
    }	
		
	public Boolean adviseNodeOpened(UITree tree) {
		Boolean ret = Boolean.FALSE;
		
		if(tree != null && tree.getTreeNode() != null && tree.getTreeNode().getData() instanceof JurisdicaoVO){
			JurisdicaoVO jurisdicao = (JurisdicaoVO)tree.getTreeNode().getData();
			if(this.jurisdicao != null && this.jurisdicao.getId() == jurisdicao.getId()){
				ret = Boolean.TRUE;
			}
		}
		return ret; 
	}
}
