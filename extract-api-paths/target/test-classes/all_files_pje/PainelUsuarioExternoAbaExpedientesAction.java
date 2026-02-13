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

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.richfaces.component.UITree;
import org.richfaces.component.state.TreeState;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

import br.com.infox.cliente.actions.CaixaAdvogadoProcuradorAction;
import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.utils.Constantes;
import br.com.infox.utils.Constantes.URL_TOMAR_CIENCIA_RESPOSTA_EXPEDIENTE;
import br.com.itx.component.UrlUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
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
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.je.pje.entity.vo.CaixaAdvogadoProcuradorVO;
import br.jus.je.pje.entity.vo.JurisdicaoVO;
import br.jus.je.pje.entity.vo.SituacaoExpedienteVO;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoCaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteCaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoExpedienteEnum;
import br.jus.pje.nucleo.enums.TipoUsuarioExternoEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Componente de controle da aba expedientes do painel do usuario externo
 * Esta classe é a controladora da página Painel/painel_usuario_externo/include/abaExpedientes.xhtml
 * 
 * @author Zeniel Chaves
 *
 */
@Name("painelUsuarioExternoAbaExpedientesAction")
@Scope(ScopeType.PAGE)
public class PainelUsuarioExternoAbaExpedientesAction extends BaseAction<ProcessoParteExpediente> {
	
	private static final long serialVersionUID = -6592182243493983778L;

	private static ParametroUtil parametroUtil = ComponentUtil.getComponent(ParametroUtil.NAME);

	private static final String nomeAba = "abaExpedientes";

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.view.BaseAction#getManager()
	 */
	@Override
	protected BaseManager<ProcessoParteExpediente> getManager() {
		return processoParteExpedienteManager;
	}
	
	private void initProcessoParteExpedienteManager() {
		processoParteExpedienteManager = ComponentUtil.getComponent(ProcessoParteExpedienteManager.class);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.view.BaseAction#getModel()
	 */
	@Override
	public EntityDataModel<ProcessoParteExpediente> getModel() {
		return null;
	}
	
	private void initCaixaAdvogadoProcuradorManager() {
		caixaAdvogadoProcuradorManager = ComponentUtil.getComponent(CaixaAdvogadoProcuradorManager.class);
	}
	
	private CaixaAdvogadoProcuradorManager caixaAdvogadoProcuradorManager;
	
	private ProcessoParteExpedienteManager processoParteExpedienteManager;

	private Integer idLocalizacaoAtual;
	
	public boolean getIsAmbienteColegiado() {
		return !parametroUtil.isPrimeiroGrau();
	}
	
	/**
	 * Campos do formulário de pesquisa menu contexto
	 */
	private String inputConsultaContextoExpedientes;
	
	/**
	 * Campos do formulário de pesquisa tabela
	 */
	private Integer numeroSequencia;
	
	private Integer digitoVerificador;
	
	private Integer ano;
	
	private String ramoJustica;
	
	private String respectivoTribunal;
	
	private Integer numeroOrigem;
	
	private Integer campoNumeroExpediente;
	
	private String campoClasse;
	
	private String campoAssunto;
	
	private Date dataCriacaoExpedienteInicial;
	
	private Date dataCriacaoExpedienteFinal;
	
	private String nomeDestinatario;
	
	private String documentoIdentificacaoDestinatario;
	
	private String codigoOABRepresentante;
		
	private OrgaoJulgador orgaoJulgador;

	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;

	private PrioridadeProcesso prioridade;
			
	private SituacaoExpedienteVO situacaoExpedienteSelecionada;
	
	private PesquisaExpedientesVO criteriosPesquisaExpedientesMenuContexto = null;

	private PesquisaExpedientesVO criteriosPesquisaExpedientesTabela = null;
	
	private Boolean resultadoEncontrado = false;
	
	public Boolean verificaSeConsultaEncontrouRegistros() {
		return this.resultadoEncontrado;
	}

	public String getInputConsultaContextoExpedientes() {
		return inputConsultaContextoExpedientes;
	}

	public void setInputConsultaContextoExpedientes(String inputConsultaContextoExpedientes) {
		this.inputConsultaContextoExpedientes = inputConsultaContextoExpedientes;
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

	public Integer getNumeroOrgaoJustica() {
		try {
			return Integer.parseInt(this.ramoJustica + this.respectivoTribunal);
		} catch (NumberFormatException ex) {
			return null;
		}
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
	
	public Integer getCampoNumeroExpediente() {
		return campoNumeroExpediente;
	}

	public void setCampoNumeroExpediente(Integer campoNumeroExpediente) {
		this.campoNumeroExpediente = campoNumeroExpediente;
	}

	public String getCampoClasse() {
		return campoClasse;
	}

	public void setCampoClasse(String campoClasse) {
		this.campoClasse = campoClasse;
	}

	public String getCampoAssunto() {
		return campoAssunto;
	}

	public void setCampoAssunto(String campoAssunto) {
		this.campoAssunto = campoAssunto;
	}

	public Date getDataCriacaoExpedienteInicial() {
		return dataCriacaoExpedienteInicial;
	}

	public void setDataCriacaoExpedienteInicial(Date dataCriacaoExpedienteInicial) {
		this.dataCriacaoExpedienteInicial = dataCriacaoExpedienteInicial;
	}

	public Date getDataCriacaoExpedienteFinal() {
		return dataCriacaoExpedienteFinal;
	}

	public void setDataCriacaoExpedienteFinal(Date dataCriacaoExpedienteFinal) {
		this.dataCriacaoExpedienteFinal = dataCriacaoExpedienteFinal;
	}

	public String getNomeDestinatario() {
		return nomeDestinatario;
	}

	public void setNomeDestinatario(String nomeDestinatario) {
		this.nomeDestinatario = nomeDestinatario;
	}

	public String getDocumentoIdentificacaoDestinatario() {
		return documentoIdentificacaoDestinatario;
	}

	public void setDocumentoIdentificacaoDestinatario(String documentoIdentificacaoDestinatario) {
		this.documentoIdentificacaoDestinatario = documentoIdentificacaoDestinatario;
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

	public PrioridadeProcesso getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(PrioridadeProcesso prioridade) {
		this.prioridade = prioridade;
	}
		
	private JurisdicaoVO jurisdicao;
		
	private CaixaAdvogadoProcuradorVO caixa;
		
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

	public TipoSituacaoExpedienteEnum getTipoSituacaoExpediente() {
		TipoSituacaoExpedienteEnum situacaoExpediente = null;
		if(getSituacaoExpedienteSelecionada() != null) {
			situacaoExpediente = getSituacaoExpedienteSelecionada().getSituacaoExpediente();
		}
		
		return situacaoExpediente;
	}

	public SituacaoExpedienteVO getSituacaoExpedienteSelecionada() {
		return situacaoExpedienteSelecionada;
	}
	
	public void setSituacaoExpedienteSelecionada(SituacaoExpedienteVO situacaoExpedienteSelecionada) {
		this.situacaoExpedienteSelecionada = situacaoExpedienteSelecionada;
	}
	
	/**
	 * Variaveis relacionadas à pesquisa
	 * ordem, pesquisa ativa ou não, situação marcada
	 */	
	private String ordenacao;

	private Order order = Order.DESC;

	public String getOrdenacao() {
		return ordenacao;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
	
	
	public void setOrdenacao(String ordenacao) {
		setOrdenacao(ordenacao, null);
	}

	public void setOrdenacao(String ordenacao, Order order) {
		this.ordenacao = ordenacao;
		if(ordenacao == null || ordenacao.isEmpty()){
			this.setOrder(order == null ? Order.DESC : order);
			dataModelExpedientes.addOrder("ptf.in_prioridade", order);
			dataModelExpedientes.addOrder("ppe.id_processo_parte_expediente", order);
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
				dataModelExpedientes.addOrder("ptf.nr_ano", order);
				dataModelExpedientes.addOrder("ptf.nr_sequencia", order);
			} else if(tks[0].equals("dataDistribuicao")){
				dataModelExpedientes.addOrder("ptf.dt_distribuicao", order);
			} else if(tks[0].equals("dtCriacao")){
				dataModelExpedientes.addOrder("ppe.id_processo_parte_expediente", order);
			}
		}
		getExpedientesTabela().setRefreshPage(Boolean.TRUE);
	}

	public void inverterOrdenacao() {
		this.setOrder(this.getOrder().equals(Order.DESC) ? Order.ASC : Order.DESC);
		setOrdenacao(this.ordenacao, this.getOrder());
	}
		
	private PesquisaExpedientesVO getCriteriosPesquisaExpedientesMenuContexto() {
		criteriosPesquisaExpedientesMenuContexto = new PesquisaExpedientesVO(getTipoSituacaoExpediente());
		criteriosPesquisaExpedientesMenuContexto.setNumeroProcesso(this.inputConsultaContextoExpedientes);
        return criteriosPesquisaExpedientesMenuContexto;
    }

	/**
	 * Esta função consolida as opções de pesquisa do painel na aba de agrupadores - pesquisas gerais do painel
	 * e.g. expedientes em uma dada situação / expedientes de um processo específico / expedientes para um destinatário X
	 * @return
	 */
	private PesquisaExpedientesVO getCriteriosPesquisaExpedientesTabela() {
        criteriosPesquisaExpedientesTabela = new PesquisaExpedientesVO(
        													this.getTipoSituacaoExpediente(),
        													this.getJurisdicao(),
        													this.getCaixa());

        criteriosPesquisaExpedientesTabela.setNumeroSequencia(this.numeroSequencia);
        criteriosPesquisaExpedientesTabela.setDigitoVerificador(this.digitoVerificador);
        criteriosPesquisaExpedientesTabela.setNumeroAno(this.ano);
        criteriosPesquisaExpedientesTabela.setNumeroOrgaoJustica(this.getNumeroOrgaoJustica());
        criteriosPesquisaExpedientesTabela.setNumeroOrigem(this.numeroOrigem);

        criteriosPesquisaExpedientesTabela.setIdProcessoParteExpediente(this.campoNumeroExpediente);
        criteriosPesquisaExpedientesTabela.setIntervaloDataCriacaoExpediente(this.dataCriacaoExpedienteInicial, this.dataCriacaoExpedienteFinal);
        criteriosPesquisaExpedientesTabela.setClasseJudicial(this.campoClasse);
        criteriosPesquisaExpedientesTabela.setAssuntoJudicial(this.campoAssunto);
        criteriosPesquisaExpedientesTabela.setPrioridadeObj(this.prioridade);
        criteriosPesquisaExpedientesTabela.setOrgaoJulgadorObj(this.orgaoJulgador);
        criteriosPesquisaExpedientesTabela.setOrgaoJulgadorColegiadoObj(this.orgaoJulgadorColegiado);
        criteriosPesquisaExpedientesTabela.setNomeDestinatario(this.nomeDestinatario);
        criteriosPesquisaExpedientesTabela.setDocumentoIdentificacao(this.documentoIdentificacaoDestinatario);
        criteriosPesquisaExpedientesTabela.setOabRepresentanteDestinatario(this.codigoOABRepresentante);
        criteriosPesquisaExpedientesTabela.setApenasCaixasComResultados(false);

		if(this.getCriteriosPesquisaExpedientesMenuContexto() != null && this.getCriteriosPesquisaExpedientesMenuContexto().isPesquisaAlterada()) {
			if(this.getCriteriosPesquisaExpedientesMenuContexto().getNumeroProcesso() != null) {
				criteriosPesquisaExpedientesTabela.setNumeroProcesso(this.getCriteriosPesquisaExpedientesMenuContexto().getNumeroProcesso());
			}
		}

        return criteriosPesquisaExpedientesTabela;
    }	

	public boolean getPesquisaAtivada() {
		return getCriteriosPesquisaExpedientesTabela().isPesquisaAlterada();
	}
	
	public void limpaPesquisaTabela() {
        this.numeroSequencia = null;
        this.digitoVerificador = null;
        this.ano = null;
        this.ramoJustica = null;
        this.respectivoTribunal = null;
        this.numeroOrigem = null;
        this.campoNumeroExpediente = null;
        this.campoClasse = null;
        this.campoAssunto = null;
        this.dataCriacaoExpedienteInicial = null;
        this.dataCriacaoExpedienteFinal = null;
        this.prioridade = null;
        this.orgaoJulgador = null;
        this.orgaoJulgadorColegiado = null;
        this.nomeDestinatario = null;
        this.documentoIdentificacaoDestinatario = null;
        this.codigoOABRepresentante = null;
        
		this.resetSelecionados();
        
        this.getExpedientesTabela().setRefreshPage(Boolean.TRUE);
	}
////////////////////
	private ExpedientesRetriever retrieverExpedientes;
	
	private EntityDataModel<ProcessoParteExpediente> dataModelExpedientes;
	
	@SuppressWarnings("rawtypes")
	private Map<Integer, TreeNodeImpl> mapaJurisdicoesExpedientes = new HashMap<Integer, TreeNodeImpl>();
	private Map<Integer, List<CaixaAdvogadoProcuradorVO>> mapaCaixasJurisdicao = new HashMap<Integer, List<CaixaAdvogadoProcuradorVO>>();
	private Map<Integer, List<SituacaoExpedienteVO>> mapaSituacoesExpedientes = new HashMap<Integer, List<SituacaoExpedienteVO>>();
	private Map<Integer, Long> mapaCountExpedientesTabela = new HashMap<Integer, Long>();
	
	@In( value="trPend",required=false, create=true)
	@Out(value="trPend",required=false)
	private UITree richTreeExpedientes;

	// modais e funções relacionadas
	private Long cnt;
	
	private String nomeNovaCaixa;
	
	private boolean criarCaixa = false;
	
	public Boolean getCriarCaixa() {
		return criarCaixa;
	}

	public String getNomeNovaCaixa() {
		return nomeNovaCaixa;
	}
	
	public void setNomeNovaCaixa(String nomeNovaCaixa) {
		this.nomeNovaCaixa = nomeNovaCaixa;
	}
	
	private boolean checkboxSelectAll = false;
	
	private List<ProcessoParteExpediente> listaSelecionados;
	
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
	
	public List<ProcessoParteExpediente> getListaSelecionados(){
		return this.listaSelecionados;
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
			listaSelecionados.addAll(this.getExpedientesTabela().page);
		}
	}
	
	public void selectOne(ProcessoParteExpediente procParteExpediente){
		if(this.isSelecionado(procParteExpediente)){
			listaSelecionados.remove(procParteExpediente);
		}else{
			listaSelecionados.add(procParteExpediente);
		}
	}
	
	public Boolean isSelecionado(ProcessoParteExpediente item) {
		if(item != null) {
			if(this.listaSelecionados == null || this.listaSelecionados.isEmpty()){
				this.listaSelecionados = new ArrayList<ProcessoParteExpediente>();
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
		this.caixaSelecionada = null;
		this.forcarAlteracaoParaProcessos = true;
	}
	
	/**
	 * Verifica se a árvore da situação indicada é a árvore com o hash de ativa
	 * 
	 * @param idTipoSituacaoExpediente
	 * @return
	 */
	public Boolean verificaSeSituacaoSelecionada(SituacaoExpedienteVO situacaoExpedienteSelecionada) {
		return (this.getSituacaoExpedienteSelecionada() != null && this.getSituacaoExpedienteSelecionada().equals(situacaoExpedienteSelecionada));
	}

	private void abrirAgrupadorSituacoes(SituacaoExpedienteVO situacaoExpedienteSelecionada) {
		this.jurisdicao = null;
		this.setSituacaoExpedienteSelecionada(situacaoExpedienteSelecionada);
	}

	private void fecharAgrupadorSituacoes() {
		this.jurisdicao = null;
		this.situacaoExpedienteSelecionada = null;
		
		this.fecharArvore(richTreeExpedientes);
	}
	
	public void alteraAberturaAgrupadorSituacoes(SituacaoExpedienteVO situacaoExpedienteSelecionada) {
		if(this.verificaSeSituacaoSelecionada(situacaoExpedienteSelecionada)) {
			this.fecharAgrupadorSituacoes();
		}else {
			this.abrirAgrupadorSituacoes(situacaoExpedienteSelecionada);			
		}
	}
	
	@SuppressWarnings("rawtypes")
	public TreeNodeImpl carregarJurisdicoesExpedientes(SituacaoExpedienteVO situacaoExpedienteSelecionada) throws PJeBusinessException {
		this.setSituacaoExpedienteSelecionada(situacaoExpedienteSelecionada);
		return getJurisdicoesExpedientes();
	}
	
	/**
	 * Faz a verificação se a consulta de expedientes já está no CACHE - há um CACHE por situação
	 * @return
	 * @throws PJeBusinessException
	 */
	@SuppressWarnings("rawtypes")
	public TreeNodeImpl getJurisdicoesExpedientes() throws PJeBusinessException {
		Integer hashCriteriosPesquisaAtual = (Integer)this.getCriteriosPesquisaExpedientesMenuContexto().hashCode();
		
		TreeNodeImpl jurisdicoesPesquisaAtual = null;
		if (mapaJurisdicoesExpedientes != null && !mapaJurisdicoesExpedientes.isEmpty()) {
			jurisdicoesPesquisaAtual = mapaJurisdicoesExpedientes.get(hashCriteriosPesquisaAtual);
		}
		
		if(jurisdicoesPesquisaAtual == null) {
			jurisdicoesPesquisaAtual = obterTreeNodes(processoParteExpedienteManager.getJurisdicoesExpedientes(this.getCriteriosPesquisaExpedientesMenuContexto()));
			mapaJurisdicoesExpedientes.put(hashCriteriosPesquisaAtual, jurisdicoesPesquisaAtual);
		}
		
		return jurisdicoesPesquisaAtual;
	}
	
	public List<SituacaoExpedienteVO> getAgrupadoresSituacoesExpedientes() throws PJeBusinessException{
		PesquisaExpedientesVO pesquisaAgrupadoresSituacoes = this.getCriteriosPesquisaExpedientesMenuContexto();
		// para buscar a lista de situações no menu de contexto, NÃO se deve passsar a situação como paâmetro de pesquisa  
		pesquisaAgrupadoresSituacoes.setTipoSituacaoExpediente(null);
		Integer hashCriteriosPesquisaAtual = (Integer)pesquisaAgrupadoresSituacoes.hashCode();

		List<SituacaoExpedienteVO> situacoesExpedientesPesquisaAtual = null;
		if (mapaSituacoesExpedientes != null && !mapaSituacoesExpedientes.isEmpty()) {
			situacoesExpedientesPesquisaAtual = mapaSituacoesExpedientes.get(hashCriteriosPesquisaAtual);
		}
		
		if(situacoesExpedientesPesquisaAtual == null) {
			situacoesExpedientesPesquisaAtual = this.processoParteExpedienteManager.getCountSituacoesExpedientes(pesquisaAgrupadoresSituacoes);
			mapaSituacoesExpedientes.put(hashCriteriosPesquisaAtual, situacoesExpedientesPesquisaAtual);
		}
		this.computaResultadoEncontrado(situacoesExpedientesPesquisaAtual);
		
		return situacoesExpedientesPesquisaAtual;
	}
	
	private long buscaCountCache(Integer idJurisdicao, PesquisaExpedientesVO criteriosPesquisa) throws PJeBusinessException {
		Long contagemExpedientes = null;
		Integer hashCriteriosPesquisaAtual = (Integer)criteriosPesquisa.hashCode();
		
		if (mapaCountExpedientesTabela != null && !mapaCountExpedientesTabela.isEmpty()) {
			contagemExpedientes = mapaCountExpedientesTabela.get(hashCriteriosPesquisaAtual);
		}
		
		if(contagemExpedientes == null) {
			contagemExpedientes = processoParteExpedienteManager.getCountExpedientesJurisdicao(idJurisdicao, criteriosPesquisa, null);
			mapaCountExpedientesTabela.put(hashCriteriosPesquisaAtual, contagemExpedientes);
		}
		return contagemExpedientes;
	}
	
	private List<CaixaAdvogadoProcuradorVO> getCaixasJurisdicao(JurisdicaoVO jurisdicao, Boolean apenasCaixasAtivas) throws PJeBusinessException{
		PesquisaExpedientesVO criteriosPesquisa = this.getCriteriosPesquisaExpedientesMenuContexto();
		List<CaixaAdvogadoProcuradorVO> caixasJurisdicao = null;
		if(jurisdicao != null) {
			criteriosPesquisa.setIdJurisdicao(jurisdicao.getId());
			criteriosPesquisa.setApenasCaixasAtivas(apenasCaixasAtivas);
		
			Integer hashCriteriosPesquisaAtual = (Integer)criteriosPesquisa.hashCode();
			
			if (mapaCaixasJurisdicao != null && !mapaCaixasJurisdicao.isEmpty()) {
				caixasJurisdicao = mapaCaixasJurisdicao.get(hashCriteriosPesquisaAtual);
			}
	
			if (caixasJurisdicao == null) {
				caixasJurisdicao = processoParteExpedienteManager.getCaixasExpedientesJurisdicao(jurisdicao.getId(), criteriosPesquisa);
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
	private CaixaAdvogadoProcuradorVO montarCaixaDeEntrada(JurisdicaoVO jurisdicao) throws PJeBusinessException {
		CaixaAdvogadoProcuradorVO caixaDeEntrada = null;
		if(jurisdicao != null && jurisdicao.getId() != 0) {
			PesquisaExpedientesVO pesquisaExpedientesSemCaixa = this.getCriteriosPesquisaExpedientesMenuContexto();
			pesquisaExpedientesSemCaixa.setIdJurisdicao(jurisdicao.getId());
			pesquisaExpedientesSemCaixa.setApenasSemCaixa(true);
			
			BigInteger contadorExpedientes = BigInteger.valueOf(this.buscaCountCache(jurisdicao.getId(), pesquisaExpedientesSemCaixa));
			caixaDeEntrada = new CaixaAdvogadoProcuradorVO(
					-1, 
					"Caixa de entrada", 
					"Esta é a caixa de entrada desta jurisdição", 
					jurisdicao.getId(), 
					jurisdicao.getDescricao(), 
					jurisdicao.getAdmin(), 
					Boolean.TRUE,
					Boolean.TRUE,
					contadorExpedientes);
		}			
		
		return caixaDeEntrada;
	}
	
	private void computaResultadoEncontrado(List <SituacaoExpedienteVO> situacoesExpedientesPesquisa) {
		this.resultadoEncontrado = false;
		for(SituacaoExpedienteVO sitExpedientes: situacoesExpedientesPesquisa) {
			if(sitExpedientes.getContador() > 0) {
				this.resultadoEncontrado = true;
				break;
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void carregarCaixasExpedientes(JurisdicaoVO jurisdicao) throws PJeBusinessException {
		TreeNode jurisdicaoNode = buscaTreeNodeJurisdicao(jurisdicao);
		// verifica se as caixas ainda não foram carregadas
		if(jurisdicaoNode != null) {
			if(jurisdicaoNode.getChild(-1) != null) {
				List<CaixaAdvogadoProcuradorVO> caixasExpedientes = getCaixasJurisdicao(jurisdicao, false);
				
				carregarCaixas(jurisdicao, caixasExpedientes);
			}
			else {
				Iterator<TreeNode> children = jurisdicaoNode.getChildren();
				while(children.hasNext()) {
					Map.Entry<Object, TreeNode> child = (Entry<Object, TreeNode>) children.next();
					CaixaAdvogadoProcuradorVO cx = (CaixaAdvogadoProcuradorVO)child.getValue().getData();
					if(cx != null) {
						if(caixa != null && caixa.getId() == cx.getId()) {
							child.getValue().setData(caixa);
							break;
						}
					}
				}
			}
		}
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
	
	private void refreshCaixa() {
		if(caixa != null)
		{
			try {
				CaixaAdvogadoProcurador cx = caixaAdvogadoProcuradorManager.findById(caixa.getId());
				CaixaAdvogadoProcuradorVO vo = this.montaCaixaZerada(cx); 
				setCaixa(vo);
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void atualizarCaixa() throws PJeBusinessException {
		refreshCaixa();
		carregarCaixasExpedientes(jurisdicao);
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
		}else if (this.jurisdicao == null) {
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
				carregarCaixas(jurisdicao, Arrays.asList(caixaVO));
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

	public void removerCaixa(){
		removerCaixa(this.caixa);
	}
	
	/**
	 * Remove uma caixa, excluindo as vinculações de todos os respectivos processos.
	 */
	@SuppressWarnings("rawtypes")
	public void removerCaixa(CaixaAdvogadoProcuradorVO caixa){
		if(caixa == null || jurisdicao == null){
			facesMessages.add(Severity.ERROR, "É necessário selecionar a caixa antes de sua remoção.");
			return;
		}
		try {
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

			this.mapaJurisdicoesExpedientes = new HashMap<Integer, TreeNodeImpl>();
			this.mapaCountExpedientesTabela = new HashMap<Integer, Long>();
			this.mapaCaixasJurisdicao = new HashMap<Integer, List<CaixaAdvogadoProcuradorVO>>();
			carregarCaixasExpedientes(jurisdicao);
			this.selecionaJurisdicaoCaixa(this.jurisdicao, this.montarCaixaDeEntrada(this.jurisdicao));
			setCaixa(null);
			
			this.lancaEventoAlteracaoPainel();

			facesMessages.add(Severity.INFO, "Caixa [{0}] removida com sucesso.", cx.getNomeCaixaAdvogadoProcurador());
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar remover a caixa: {0}.", e.getLocalizedMessage());
		} catch (NoSuchFieldException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar remover a caixa: {0}.", e.getLocalizedMessage());
		}
	}
	
	public void comboMoverParaCaixaSelecionada() throws PJeBusinessException {
		CaixaAdvogadoProcuradorVO caixaVO = this.getCaixaSelecionada();
		if(caixaVO != null) {
			List<ProcessoParteExpediente> listaProcessoParteExpediente = this.carregaListaSelecionados();
			if(caixaVO.getPadrao()) {
				this.moverExpedientesParaJurisdicao(caixaVO.getJurisdicao(), listaProcessoParteExpediente, this.getForcarAlteracaoParaProcessos());
			}else {
				if(ComponentUtil.getComponent(CaixaAdvogadoProcuradorManager.class).isCaixaAtiva(caixaVO.getId())) {
					this.moverExpedienteParaCaixa(caixaVO, listaProcessoParteExpediente, this.getForcarAlteracaoParaProcessos());
					finalizarMovimentacaoExpediente();
				}else {
					facesMessages.add(Severity.INFO, "Não foi possível mover o expediente, pois a caixa [{0}] está inativa.", caixaVO.getNome());
				}
			}
		}
	}
	
	private List<ProcessoParteExpediente> carregaListaSelecionados() throws PJeBusinessException{
		List <ProcessoParteExpediente> listaCarregada = new ArrayList<ProcessoParteExpediente>();
		for(ProcessoParteExpediente ppeSelecionado: this.listaSelecionados){
			listaCarregada.add(processoParteExpedienteManager.findById(ppeSelecionado.getIdProcessoParteExpediente()));
		}
		
		return listaCarregada;
	}
	
	
	/**
	* Remove o expediente de caixas - ele voltará para a caixa de entrada
	*
	* @param jurisdicaoVO
	* @param listaProcessos
	* @param forcarAlteracaCaixa - se true, faz a movimentação também do processo independente se ele está ou não em uma caixa diferente
	*/
	private void moverExpedientesParaJurisdicao(JurisdicaoVO jurisdicaoVO, List<ProcessoParteExpediente> listaProcessoParteExpediente, Boolean forcarAlteracaCaixa){
		if (this.caixa == null) {
			facesMessages.add(Severity.ERROR, "Expedientes não estão em uma caixa.");
			return;
		}			
		if (this.caixa.getIdJurisdicao() != jurisdicaoVO.getId()) {
			facesMessages.add(Severity.ERROR, "Jurisdição de destino diferente da caixa dos expedientes.");
			return;
		}
		
		try {
			ProcessoParteExpedienteCaixaAdvogadoProcuradorManager procParteExpCaixaAdvProcManager = ComponentUtil.getComponent(ProcessoParteExpedienteCaixaAdvogadoProcuradorManager.class);

			CaixaAdvogadoProcurador caixaOrigemExpediente = caixaAdvogadoProcuradorManager.findById(this.caixa.getId());

			if(caixaOrigemExpediente != null && caixaOrigemExpediente.getIdCaixaAdvogadoProcurador() != null) {
				procParteExpCaixaAdvProcManager.remover(caixaOrigemExpediente, true, 
						(ProcessoParteExpediente[])listaProcessoParteExpediente.toArray(new ProcessoParteExpediente[listaProcessoParteExpediente.size()]));
				
				List<ProcessoTrf> listaDeProcessosNaoGerenciados = this.getProcessosDosExpedientes(this.jurisdicao.getId(), listaProcessoParteExpediente);
				if(forcarAlteracaCaixa) {
					CaixaAdvogadoProcurador caixaOrigemProcesso;
					for (ProcessoTrf processoTrf: listaDeProcessosNaoGerenciados) {	
						caixaOrigemProcesso = null;
						ProcessoTrf processo = ProcessoJudicialManager.instance().findById(processoTrf.getIdProcessoTrf());
	
						// pode acontecer que o processo não esteja no acervo do usuário - esses processos não devem ser movimentados
						if(processo != null) {
							ConsultaProcessoVO criteriosPesquisa = new ConsultaProcessoVO();
							criteriosPesquisa.setApenasCaixasComResultados(true);
							criteriosPesquisa.setNumeroProcesso(processo.getNumeroProcesso());
							
							List<CaixaAdvogadoProcuradorVO> caixasOrigemProcesso = caixaAdvogadoProcuradorManager.obterCaixasAcervoJurisdicao(jurisdicaoVO.getId(), criteriosPesquisa);
							
							if (caixasOrigemProcesso != null && !caixasOrigemProcesso.isEmpty()) {
								// assume-se que o processo esteja no máximo em 1 caixa p/ o usuário atual
								caixaOrigemProcesso = caixaAdvogadoProcuradorManager.findById(caixasOrigemProcesso.get(0).getId());
							}
							
							// move os processos se estiverem ( em qualquer caixa se o usuário mandou forçar a alteração)
							// esta regra é diferente da aba acervo - lá movia os expedientes que já estavam na mesma caixa do processo - aqui move processos em qualquer caixa só se o usuário mandou forçar a mudança 
							if(caixaOrigemProcesso != null && (forcarAlteracaCaixa)) {
								ProcessoCaixaAdvogadoProcuradorManager.instance().remover(caixaOrigemProcesso, true, new ProcessoTrf[]{processo});					
							}
						}
					}
				}
			}

			resetSelecionados();
			limparCacheExpedientes();
			this.carregarCaixasExpedientes(jurisdicao);
			this.lancaEventoAlteracaoPainel();
			
			facesMessages.add(Severity.INFO,
					"{0} expediente(s) removidos(s) de \"{1}\"",
					listaProcessoParteExpediente.size(),
					this.caixa.getNomeJurisdicao() + " > " + this.caixa.getNome());

			
		} catch (PJeBusinessException e) {
			resetSelecionados();
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar mover o(s) processo(s): {0}.", e.getLocalizedMessage());
		}
	}
	
	private ProcessoTrf getProcessoDoExpediente(Integer idJurisdicao, ProcessoParteExpediente expediente) throws PJeBusinessException {
		
		ConsultaProcessoVO criteriosPesquisa = new ConsultaProcessoVO();
		criteriosPesquisa.setNumeroProcesso(expediente.getProcessoJudicial().getNumeroProcesso());
		
		criteriosPesquisa.setIdJurisdicao(idJurisdicao);
		
		List<ProcessoTrf> processosEncontrados = ProcessoJudicialManager.instance().getProcessosJurisdicao(idJurisdicao, criteriosPesquisa, null);
		ProcessoTrf processoGerenciado = null;
		if(processosEncontrados != null && !processosEncontrados.isEmpty()) {
			processoGerenciado = ProcessoJudicialManager.instance().findById(processosEncontrados.get(0).getIdProcessoTrf());
		}
		
		return processoGerenciado;
	}
	
	private List<ProcessoTrf> getProcessosDosExpedientes(Integer idJurisdicao, List<ProcessoParteExpediente> expedientes) throws PJeBusinessException {
		List<String> numerosProcessos = new ArrayList<String>();
		
		for (ProcessoParteExpediente processoParteExpediente : expedientes) {
			numerosProcessos.add(processoParteExpediente.getProcessoJudicial().getNumeroProcesso());
		}
		
		ConsultaProcessoVO criteriosPesquisa = new ConsultaProcessoVO();
		criteriosPesquisa.setNumerosProcessos(numerosProcessos);
		criteriosPesquisa.setIdJurisdicao(idJurisdicao);
		
		List<ProcessoTrf> processosEncontrados = ProcessoJudicialManager.instance().getProcessosJurisdicao(idJurisdicao, criteriosPesquisa, null);
		
		return processosEncontrados;
	}
	
	/**
	 * Move o expediente para uma caixa identificada 
	 * 
	 * @param caixaDestinoVO
	 * @param listaProcessoParteExpediente
	 * @param forcarAlteracaCaixa - forçar a alteração dos processos vinculados, independente se estão ou não em caixas distintas
	 */
	private void moverExpedienteParaCaixa(CaixaAdvogadoProcuradorVO caixaDestinoVO, List<ProcessoParteExpediente> listaProcessoParteExpediente, Boolean forcarAlteracaCaixa) {
		try {
			ProcessoParteExpedienteCaixaAdvogadoProcuradorManager processoParteExpedienteCaixaAdvogadoProcuradorManager = 
					ProcessoParteExpedienteCaixaAdvogadoProcuradorManager.instance();

			CaixaAdvogadoProcurador caixaOrigemExpediente = null;
			if(this.caixa != null) {
				caixaOrigemExpediente = caixaAdvogadoProcuradorManager.findById(this.caixa.getId());
			}
			CaixaAdvogadoProcurador caixaDestino = caixaAdvogadoProcuradorManager.findById(caixaDestinoVO.getId());
			
			Boolean listaLimitada = false;
			List<ProcessoParteExpediente> listaProcessoParteExpedienteUtilizada;
			if(listaProcessoParteExpediente.size() > Constantes.MAX_DISTRIBUICOES_PROCESSOS_LOTE) {
				listaProcessoParteExpedienteUtilizada = listaProcessoParteExpediente.subList(0, Constantes.MAX_DISTRIBUICOES_PROCESSOS_LOTE);
				listaLimitada = true;
			}else {
				listaProcessoParteExpedienteUtilizada = listaProcessoParteExpediente;	
			}

			processoParteExpedienteCaixaAdvogadoProcuradorManager.incluirEmCaixa(caixaDestino, caixaOrigemExpediente, 
					(ProcessoParteExpediente[])listaProcessoParteExpedienteUtilizada.toArray(new ProcessoParteExpediente[listaProcessoParteExpedienteUtilizada.size()]));
			
			if(forcarAlteracaCaixa) {
			CaixaAdvogadoProcurador caixaOrigemProcesso;
			List<ProcessoTrf> listaDeProcessosNaoGerenciados = this.getProcessosDosExpedientes(this.jurisdicao.getId(), listaProcessoParteExpedienteUtilizada);
			
			for (ProcessoTrf processoTrf: listaDeProcessosNaoGerenciados) {
				caixaOrigemProcesso = null;
				ProcessoTrf processo =  ProcessoJudicialManager.instance().findById(processoTrf.getIdProcessoTrf());
					
					// pode acontecer que o processo não esteja no acervo do usuário - esses processos não devem ser movimentados
					if(processo != null) {
						ConsultaProcessoVO criteriosPesquisa = new ConsultaProcessoVO();
						criteriosPesquisa.setApenasCaixasComResultados(true);
						criteriosPesquisa.setNumeroProcesso(processo.getNumeroProcesso());
						if(this.jurisdicao != null){
							criteriosPesquisa.setIdJurisdicao(this.jurisdicao.getId());					
						}
						
						// serão buscadas apenas as caixas que tenham 
						List<CaixaAdvogadoProcuradorVO> caixasOrigemProcesso = caixaAdvogadoProcuradorManager.obterCaixasAcervoJurisdicao(jurisdicao.getId(), criteriosPesquisa);
						
						if (caixasOrigemProcesso != null && !caixasOrigemProcesso.isEmpty()) {
							// assume-se que o processo esteja no máximo em 1 caixa p/ o usuário atual
							caixaOrigemProcesso = caixaAdvogadoProcuradorManager.findById(caixasOrigemProcesso.get(0).getId());
						}
						ProcessoCaixaAdvogadoProcuradorManager.instance().incluirEmCaixa(caixaDestino, caixaOrigemProcesso, new ProcessoTrf[]{processo});
					}
				}
			}
			if(listaProcessoParteExpedienteUtilizada.size() == 1) {
				facesMessages.add(Severity.INFO, "Expediente movido para \"{0}\"", caixaDestinoVO.getNomeJurisdicao() + " > " + caixaDestinoVO.getNome());
			}else {
				facesMessages.add(Severity.INFO, "{0} expedientes movidos para \"{1}\"", listaProcessoParteExpedienteUtilizada.size(), caixaDestinoVO.getNomeJurisdicao() + " > " + caixaDestinoVO.getNome());
				if(listaLimitada) {
					facesMessages.add(Severity.WARN, "Apesar de terem sido selecionados {0} expedientes para serem movidos, "
							+ "o limite do sistema é de {1}. Para mover todos os demais repita a operação com os itens pendentes.",
							listaProcessoParteExpediente.size(), Constantes.MAX_DISTRIBUICOES_PROCESSOS_LOTE);
				}

			}
		} catch (PJeBusinessException e) {
			resetSelecionados();
			facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
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
			
			/* Recupera as caixas ativas da jurisdicao na localizacao atual. */
			List<CaixaAdvogadoProcuradorVO> caixas = caixaAdvogadoProcuradorManager.obterCaixasExpedientesJurisdicao(jurisdicao.getId(), getCriteriosPesquisaExpedientesMenuContexto());
			
			if(caixas.size() == 0) {
				facesMessages.add(Severity.INFO, "Nenhum processo distribuído. Não há caixas ativas nessa jurisdição.");
				return;
			}
			
			Boolean houveExpedientesParaMover = false;
			CaixaAdvogadoProcuradorAction caixaAction = (CaixaAdvogadoProcuradorAction) Component.getInstance(CaixaAdvogadoProcuradorAction.class);

			// Percorre a lista de caixas disponíveis na jurisdição, buscando encontrar os expedientes que se enquadram nos seus filtros para movê-los
			for(CaixaAdvogadoProcuradorVO caixaDestinoVO : caixas) {
				// não utiliza a caixa padrão / caixa de entrada
				if(!caixaDestinoVO.getPadrao() && caixaDestinoVO.getAtivo()) {
					CaixaAdvogadoProcurador caixaDestino = caixaAdvogadoProcuradorManager.findById(caixaDestinoVO.getId());
					List<ProcessoParteExpediente> expedientes = caixaAction.processoParteExpedientesCorrespondentesFiltroCaixa(this.getTipoSituacaoExpediente(), caixaDestino);
	
					if(!expedientes.isEmpty()) {
						this.moverExpedienteParaCaixa(caixaDestinoVO, expedientes, true); // move expedientes para a caixa, forçando o deslocamento do processo associado
						
						houveExpedientesParaMover = true;
					}
				}
			}
			if(houveExpedientesParaMover) {
				finalizarMovimentacaoExpediente();
			}else {
				facesMessages.add(Severity.INFO, "Nenhum expediente distribuído. Não há expedientes que se encaixam nos filtros das caixas, a caixa está em um período de inativação ou não há filtro configurado.");
			}
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar executar distribuição automática por filtro: {0}.", e.getLocalizedMessage());
		}
		
		/* Efetua refresh na listagem de processos da jurisdição */
		pesquisarExpedientes();
	}

/////////////////////////
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
	
	@SuppressWarnings("rawtypes")
	private TreeNode buscaTreeNodeJurisdicao(JurisdicaoVO jurisdicao) {
		TreeNode jurisdicaoNode = null;
		TreeNodeImpl jurisdicoesPesquisaAtual = null;
		
		try {
			jurisdicoesPesquisaAtual = getJurisdicoesExpedientes();
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
		this.limparCacheExpedientes();
		this.inputConsultaContextoExpedientes = null;
		this.limpaPesquisaTabela();
		this.jurisdicao = null;
		this.caixa = null;
		
		this.resetSelecionados();
		
		this.getExpedientesTabela().setRefreshPage(Boolean.TRUE);
		this.fecharArvore(richTreeExpedientes);
		
	}
	
	@SuppressWarnings("rawtypes")
	private void limparCacheExpedientes() {
		this.mapaJurisdicoesExpedientes = new HashMap<Integer, TreeNodeImpl>();
		this.mapaSituacoesExpedientes = new HashMap<Integer, List<SituacaoExpedienteVO>>();
		this.mapaCaixasJurisdicao = new HashMap<Integer, List<CaixaAdvogadoProcuradorVO>>();
		this.mapaCountExpedientesTabela = new HashMap<Integer, Long>();
		
		this.getExpedientesTabela().setRefreshPage(Boolean.TRUE);
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
////////////////////
	
	public void pesquisarContextoExpedientes() {
		this.limpaPesquisaTabela();
		this.caixa = null;
		this.jurisdicao = null;
	}
	
	public void pesquisarExpedientes() {
		this.getExpedientesTabela().setRefreshPage(Boolean.TRUE);
	}
	
	public void selecionaJurisdicao(JurisdicaoVO jurisdicao) throws PJeBusinessException {
		this.selecionaJurisdicaoCaixa(jurisdicao, this.montarCaixaDeEntrada(jurisdicao));
		this.carregarCaixasExpedientes(jurisdicao);
	}

	public void selecionaJurisdicaoCaixa(JurisdicaoVO jurisdicao, CaixaAdvogadoProcuradorVO caixa) {
		setJurisdicao(jurisdicao);
		setCaixa(caixa);
		if(this.ordenacao != null && !StringUtil.fullTrim(this.ordenacao).isEmpty()){
			setOrdenacao(this.ordenacao);
		}
		this.limpaPesquisaTabela();
		this.getExpedientesTabela().setRefreshPage(Boolean.TRUE);
	}
	
	/**
	 * Recupera o modelo de dados pertinentes à pesquisa atual - para o dataTable
	 * 
	 * @return o modelo de dados
	 */
	public EntityDataModel<ProcessoParteExpediente> getExpedientesTabela(){
		return dataModelExpedientes;
	}
	
/////////////////
	public String getLabelPrazoLegal(ProcessoParteExpediente processoParteExpediente) {
	    String label = StringUtils.EMPTY;
	    if (processoParteExpediente.getDtCienciaParte() == null 
	    		&& !TipoPrazoEnum.S.equals(processoParteExpediente.getTipoPrazo()) && !TipoPrazoEnum.C.equals(processoParteExpediente.getTipoPrazo())) {
	        label = FacesUtil.getMessage("entity_messages", "expediente.dataLimiteCiencia") + ": ";
	    }
	    if (processoParteExpediente.getDtCienciaParte() != null || TipoPrazoEnum.C.equals(processoParteExpediente.getTipoPrazo())) {
	        label = FacesUtil.getMessage("entity_messages", "expediente.dataLimiteManifestacao") + ": ";
	    }
	    return label;
	}

	/**
	 * Método responsável por montar o link do expediente a ser respondido.
	 * @param idProcessoTrf
	 * @param idProcessoParteExpediente
	 * @return Link de resposta do especiente.
	 */
	public String montarLinkRespostaExpediente(Integer idProcessoTrf, Integer idProcessoParteExpediente) {
		String linkRetorno = StringUtils.EMPTY;
		String chave = SecurityTokenControler.instance().gerarChaveAcessoProcesso(idProcessoTrf);
		linkRetorno = UrlUtil.montarLinkTomarCienciaRespostaExpediente(
				URL_TOMAR_CIENCIA_RESPOSTA_EXPEDIENTE.RESPOSTA_EXPEDIENTE,idProcessoTrf, idProcessoParteExpediente, chave);
		return linkRetorno;
	}
	public boolean exibirBotaoResponderExpediente(ProcessoParteExpediente processoParteExpediente) {
		Date dataAtual = new Date();

		/* só pode responder se já houver ciência, ainda estiver no prazo e se não for: assistente de procuradoria ou de advogado */
		if(processoParteExpediente != null && processoParteExpediente.getDtCienciaParte() != null && (
				(processoParteExpediente.getDtPrazoLegal() != null && processoParteExpediente.getDtPrazoLegal().after(dataAtual)) 
				|| processoParteExpediente.getTipoPrazo().isSemPrazo())) {
			
			TipoUsuarioExternoEnum tipoUsuarioExternoAtual = Authenticator.getTipoUsuarioExternoAtual();
			
			return (!(TipoUsuarioExternoEnum.AA.equals(tipoUsuarioExternoAtual) || TipoUsuarioExternoEnum.AP.equals(tipoUsuarioExternoAtual)));
		}
		return false;
	}
	
	public boolean exibirBotaoTomarCiencia(ProcessoParteExpediente processoParteExpediente) {
		Date dataAtual = new Date();

		if(processoParteExpediente != null && (
					(processoParteExpediente.getDtPrazoLegal() != null && processoParteExpediente.getDtPrazoLegal().after(dataAtual)) 
					|| processoParteExpediente.getTipoPrazo().isSemPrazo())) {
			TipoUsuarioExternoEnum tipoUsuarioExternoAtual = Authenticator.getTipoUsuarioExternoAtual();
			
			/* assistente de procuradoria ou de advogado não pode dar ciência */
			return !(TipoUsuarioExternoEnum.AA.equals(tipoUsuarioExternoAtual) || TipoUsuarioExternoEnum.AP.equals(tipoUsuarioExternoAtual));
		}
		return false;		
	}

/////////////////
	
	/**
	 * Recuperador de processos pendentes de manifestação.
	 * 
	 * @author cristof
	 *
	 */
	private class ExpedientesRetriever implements DataRetriever<ProcessoParteExpediente>{
		
		private Long count;
		
		private FacesMessages facesMessages;
		
		private ProcessoParteExpedienteManager manager;
		
		public ExpedientesRetriever(ProcessoParteExpedienteManager manager, Long count, FacesMessages facesMessages) {
			this.manager = manager;
			this.count = count;
			this.facesMessages = facesMessages;
		}

		@Override
		public Object getId(ProcessoParteExpediente p) {
			return manager.getId(p);
		}

		@Override
		public ProcessoParteExpediente findById(Object id) throws Exception {
			return manager.findById(id);
		}

		@Override
		public List<ProcessoParteExpediente> list(Search search) {
			if(jurisdicao != null) {
				try {
					PesquisaExpedientesVO criteriosPesquisa = getCriteriosPesquisaExpedientesTabela();
					if(caixa == null || caixa.getPadrao()) {
						return manager.getExpedientesJurisdicao(jurisdicao.getId(), criteriosPesquisa, search);
					}else {
						return manager.getExpedientesJurisdicaoCaixa(jurisdicao.getId(), caixa.getId(), criteriosPesquisa, search);
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
				PesquisaExpedientesVO criteriosPesquisa = getCriteriosPesquisaExpedientesTabela();
				criteriosPesquisa.setIdJurisdicao(jurisdicao.getId());
				try {
					if(caixa == null || caixa.getPadrao()) {
						criteriosPesquisa.setApenasSemCaixa(true);
					}else {
						criteriosPesquisa.setApenasSemCaixa(false);
						criteriosPesquisa.setIdCaixaAdvProc(caixa.getId());
					}
					count = buscaCountCache(jurisdicao.getId(), criteriosPesquisa);
				}catch (Exception e) {
					facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar os expedientes.");
				}
			}
			return count;
		}		
	};

	public String getPathToCollapsedExpandedIcon(Object vo, boolean collapsed) {
		String name = "";
		if(vo instanceof JurisdicaoVO) {
			name= collapsed ? "/img/fontawesome/fa_chevron_right.png" : "/img/fontawesome/fa_chevron_down.png";
		}
		return name;
	}
	
	@Create
	public void init() throws PJeBusinessException{
		initProcessoParteExpedienteManager();
		initCaixaAdvogadoProcuradorManager();
		
		idLocalizacaoAtual = Authenticator.getIdLocalizacaoAtual();

		retrieverExpedientes = new ExpedientesRetriever(this.processoParteExpedienteManager, cnt, facesMessages);		

		dataModelExpedientes = new EntityDataModel<ProcessoParteExpediente>(ProcessoParteExpediente.class, super.facesContext, retrieverExpedientes);
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

	private void finalizarMovimentacaoExpediente() throws PJeBusinessException {
		this.resetSelecionados();
		this.limparCacheExpedientes();
		this.carregarCaixasExpedientes(jurisdicao);
		this.lancaEventoAlteracaoPainel();
	}

}
