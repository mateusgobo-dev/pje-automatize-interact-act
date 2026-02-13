package br.jus.cnj.pje.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.StatusMessage.Severity;
import org.richfaces.component.UITree;
import org.richfaces.component.state.TreeState;
import org.richfaces.event.DropEvent;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.CaixaAdvogadoProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.JurisdicaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoCaixaAdvogadoProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteCaixaAdvogadoProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.je.pje.entity.vo.CaixaAdvogadoProcuradorVO;
import br.jus.je.pje.entity.vo.JurisdicaoVO;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;


/**
 * Componente que controla os componentes de paginação da tela de pesquisa do
 * acervo de processos do advogado e procuradores. advogado.xhtml.
 * 
 * Esta action atua junto a {@link PainelUsuarioExternoAction}. Foi preciso
 * seperar em duas em virtude da necessidade do uso do escopo diferenciado
 * 
 * @author Marco Pimenta
 *
 */
@Name("painelUsuarioExternoAcervoAction")
@Scope(ScopeType.PAGE)
public class PainelUsuarioExternoAcervoAction extends BaseAction<ProcessoTrf>{
	
	private static final long serialVersionUID = 1L;

	private Map<Integer, Boolean> idsSelecionados = new HashMap<Integer, Boolean>();
	
	/**
	 * Variável destinada a armazenar o identificador do componente rich:tab selecionado.
	 */
	@RequestParameter(value="selectedTab")
	private String selectedTab;	
	
	private String numeroProcessoConsulta;
	
	private Integer numeroSequencia;
	
	private Integer digitoVerificador;
	
	private Integer ano;
	
	private String ramoJustica;
	
	private String respectivoTribunal;
	
	private Integer numeroOrigem;	
	
	private Integer idProcessoEmCaixa;
	
	private CaixaAdvogadoProcuradorVO caixa;	
	
	private boolean pendentes;	
	
	@SuppressWarnings("rawtypes")
	private Map<Integer, TreeNodeImpl> mapaJurisdicoesAcervo = new HashMap<Integer, TreeNodeImpl>();	
	
	@In( value="trAc",required=false, create=true)
	@Out(value="trAc",required=false)
	private UITree richTreeAcervo;
	
	@Create
	public void init() {
		
	}
	
	public void marcarDesmarcarProcesso(Integer idProcesso) {				
		if (idsSelecionados.get(idProcesso) != null && idsSelecionados.get(idProcesso)) {
			// se já estiver sido selecionado então desmarca
			idsSelecionados.put(idProcesso, Boolean.FALSE);
			
		} else {
			idsSelecionados.put(idProcesso, Boolean.TRUE);
			
		}
	}
	
	public List<Integer> getProcessosSelecionados() {
		
		List<Integer> selecionados = new ArrayList<Integer>();
		
		Set<Integer> keySet = idsSelecionados.keySet();
		for (Integer idProcesso : keySet) {
			if (idsSelecionados.get(idProcesso)) {
				selecionados.add(idProcesso);
			}
		}
		
		return selecionados;		
	}
	
	@SuppressWarnings("rawtypes")
	public TreeNodeImpl getJurisdicoesAcervo() throws PJeBusinessException {
		Integer hashCriteriosPesquisaAtual = (Integer)this.getCriteriosPesquisaProcessos().hashCode();
		
		TreeNodeImpl jurisdicoesPesquisaAtual = null;
		if (mapaJurisdicoesAcervo != null && !mapaJurisdicoesAcervo.isEmpty()) {
			jurisdicoesPesquisaAtual = mapaJurisdicoesAcervo.get(hashCriteriosPesquisaAtual);
		}

		if (jurisdicoesPesquisaAtual == null) {
			jurisdicoesPesquisaAtual = obterTreeNodes(JurisdicaoManager.instance().obterJurisdicoesAcervo(this.getCriteriosPesquisaProcessos()));
			mapaJurisdicoesAcervo.put(hashCriteriosPesquisaAtual, jurisdicoesPesquisaAtual);

			fecharArvore(richTreeAcervo);
		}
		return jurisdicoesPesquisaAtual;
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
	
	public void carregarCaixasAcervo(JurisdicaoVO jurisdicao) throws PJeBusinessException {
		if (verificarCaixasCarregadas(jurisdicao)) {
			List<CaixaAdvogadoProcuradorVO> caixasAcervo = caixaAdvogadoProcuradorManagerInstance().obterCaixasAcervoJurisdicao(
					jurisdicao.getId(), this.getCriteriosPesquisaProcessos());
			
			carregarCaixas(jurisdicao, caixasAcervo);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private boolean verificarCaixasCarregadas(JurisdicaoVO jurisdicao) {
		TreeNode jurisdicaoNode = buscaTreeNodeJurisdicao(jurisdicao);
		return (jurisdicaoNode != null && jurisdicaoNode.getChild(-1) != null);  // Caso a caixa fake exista, significa que as caixas não foram carregadas.
	}

	@SuppressWarnings("rawtypes")
	private TreeNode buscaTreeNodeJurisdicao(JurisdicaoVO jurisdicao) {
		TreeNode jurisdicaoNode = null;
		TreeNodeImpl jurisdicoesPesquisaAtual = null;
		
		try {
			jurisdicoesPesquisaAtual = getJurisdicoesAcervo();
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
				caixas = caixaAdvogadoProcuradorManagerInstance().obterCaixasAcervoJurisdicao(jurisdicao.getId(), this.getCriteriosPesquisaProcessos());
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
	
	/**
	 * Esta função consolida as opções de pesaquisa do painel na aba de acervo - pesquisas gerais do painel
	 * e.g processos com um número N / processos de uma parte específica
	 * @return
	 */
	private ConsultaProcessoVO getCriteriosPesquisaProcessos() {
		ConsultaProcessoVO criteriosPesquisa = new ConsultaProcessoVO();

		criteriosPesquisa.setNumeroSequencia(this.numeroSequencia);
		criteriosPesquisa.setDigitoVerificador(this.digitoVerificador);
		criteriosPesquisa.setNumeroAno(this.ano);
		criteriosPesquisa.setNumeroOrgaoJustica(this.getNumeroOrgaoJustica());
		criteriosPesquisa.setNumeroOrigem(this.numeroOrigem);	
		
		// a consulta deve retornar sempre as caixas, mesmo que estejam vazias
		criteriosPesquisa.setApenasCaixasComResultados(false);
		if (StringUtils.isNotBlank(getNumeroProcessoConsulta())) {
			criteriosPesquisa.setNumeroProcesso(getNumeroProcessoConsulta());
		}
		
		return criteriosPesquisa;
	}

	/**
	 * Inclui um determinado processo em uma caixa. O identificador do processo a ser incluído
	 * deve ser repassado à action por meio do parâmetro "pid".
	 * 
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	public void removerProcessoDaCaixa(DropEvent event){
		
		try {
			
			List<Integer> idsProcessos = (List<Integer>)Contexts.getPageContext().get("listaSelecionados");
							
			if ((idsProcessos == null || idsProcessos.size() <= 0)
					&& (idProcessoEmCaixa == null || idProcessoEmCaixa <= 0)) {
				facesMessages.add("Nenhum processo foi selecionado para movimentação.");
				return;
				
			} else if (idProcessoEmCaixa != null) {
				// movimentação individual de processos.
				idsProcessos = new ArrayList<Integer>();
				idsProcessos.add(idProcessoEmCaixa);
				
			}
			
			
			if (caixa == null) {
				facesMessages.add(Severity.ERROR, "Processo não está em uma caixa.");
				return;
			}			
			CaixaAdvogadoProcurador caixa_ = ComponentUtil.getComponent(CaixaAdvogadoProcuradorManager.class).findById(caixa.getId());
			
			if (event.getDropValue() instanceof Integer) {
				// contém a jurisdição onde o usuário soltou(drop) o processo
				Integer idJurisdicao = (Integer)event.getDropValue();
				if (caixa_.getJurisdicao().getIdJurisdicao() != idJurisdicao) {
					facesMessages.add(Severity.ERROR, "Jurisdição de destino diferente da caixa dos processos.");
					return;
				}
			}
			
			if(!pendentes){
				List<ProcessoTrf> processos = new ArrayList<ProcessoTrf>();
				
				for (Integer idProcesso : idsProcessos) {
					ProcessoTrf processo = ComponentUtil.getComponent(ProcessoJudicialManager.class).findById(idProcesso);
					processos.add(processo);
				}
							
				ComponentUtil.getComponent(ProcessoCaixaAdvogadoProcuradorManager.class).remover(caixa_, true,
						(ProcessoTrf[]) processos.toArray(new ProcessoTrf[processos.size()]));
				
				resetSelecionados();
				
				limparCacheAcervo();
				init();
				facesMessages.add(Severity.INFO,
						"{0} processo(s) removido(s) de \"{1}\".",
						processos.size(),
						caixa_.getNomeCaixaAdvogadoProcurador());
			} else {
				List<ProcessoParteExpediente> expedientes = new ArrayList<ProcessoParteExpediente>();
				
				for(Integer idPpe: idsProcessos){
					ProcessoParteExpediente ppe = ComponentUtil.getComponent(ProcessoParteExpedienteManager.class).findById(idPpe);
					expedientes.add(ppe);
				}
				
				ComponentUtil.getComponent(ProcessoParteExpedienteCaixaAdvogadoProcuradorManager.class).remover(caixa_, true, 
									(ProcessoParteExpediente[])expedientes.toArray(new ProcessoParteExpediente[expedientes.size()]));
				
				resetSelecionados();
				
				init();
				
				facesMessages.add(Severity.INFO,
						"{0} expediente(s) removidos(s) de \"{1}\"",
						expedientes.size(),
						caixa_.getNomeCaixaAdvogadoProcurador());
				
			}
			limparCacheAcervo();
			
		} catch (PJeBusinessException e) {
			resetSelecionados();
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar mover o(s) processo(s): {0}.", e.getLocalizedMessage());
		}
	}
	
	private void limparCacheAcervo() {
		mapaJurisdicoesAcervo = new HashMap<Integer, TreeNodeImpl>();
		fecharArvore(richTreeAcervo);
	}
	
	
	public Integer getNumeroOrgaoJustica() {
		try {
			return Integer.parseInt(this.ramoJustica + this.respectivoTribunal);
		} catch (NumberFormatException ex) {
			return null;
		}
	}
	
	
	
	private CaixaAdvogadoProcuradorManager caixaAdvogadoProcuradorManagerInstance() {
		return ComponentUtil.getComponent(CaixaAdvogadoProcuradorManager.class);
	}
	
	public void resetSelecionados() {
		idsSelecionados = new HashMap<Integer, Boolean>();
	}

	public Map<Integer, Boolean> getIdsSelecionados() {
		return idsSelecionados;
	}

	public void setIdsSelecionados(Map<Integer, Boolean> idsSelecionados) {
		this.idsSelecionados = idsSelecionados;
	}
	
	public int getQtdeSelecionados() {
		return getProcessosSelecionados().size();
	}
	
	public String getSelectedTab() {
		return selectedTab;
	}

	public void setSelectedTab(String selectedTab) {
		this.selectedTab = selectedTab;
	}
	
	public String getNumeroProcessoConsulta() {
		return this.numeroProcessoConsulta;
	}

	public void setNumeroProcessoConsulta(String numeroProcessoConsulta) {
		this.numeroProcessoConsulta = numeroProcessoConsulta;
	}

	@Override
	protected BaseManager<ProcessoTrf> getManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityDataModel<ProcessoTrf> getModel() {
		// TODO Auto-generated method stub
		return null;
	}	
	
}
