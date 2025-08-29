package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.Strings;
import org.richfaces.component.UITree;

import br.com.infox.cliente.bean.PreCadastroPessoaBean;
import br.com.infox.cliente.comparator.ProcessoParteComparator;
import br.com.infox.cliente.component.suggest.ProfissaoSuggestBean;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.component.tree.SearchTree2GridList;
import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.infox.pje.manager.PessoaJuridicaManager;
import br.com.infox.pje.manager.PessoaProcuradoriaEntidadeManager;
import br.com.infox.pje.manager.TipoParteConfigClJudicialManager;
import br.com.itx.component.SelectItemsQuery;
import br.com.itx.component.Util;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pagination.PageDataModel;
import br.jus.cnj.pje.amqp.model.dto.ProcessoParteCloudEvent;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.AMQPEventManager;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.InformacaoCriminalRascunhoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAdvogadoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAutoridadeManager;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteRepresentanteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoPushManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoRascunhoManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.TipoParteManager;
import br.jus.cnj.pje.nucleo.service.EnderecoService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventVerbEnum;
import br.jus.cnj.pje.view.fluxo.PreparaAtoComunicacaoAction;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.beans.criminal.ConteudoInformacaoCriminalBean;
import br.jus.pje.nucleo.entidades.CaracteristicaFisica;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrf;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRascunho;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaNomeAlternativo;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteAdvogado;
import br.jus.pje.nucleo.entidades.ProcessoParteEndereco;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteHistorico;
import br.jus.pje.nucleo.entidades.ProcessoParteMin;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoRascunho;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoParteConfigClJudicial;
import br.jus.pje.nucleo.entidades.TipoParteConfiguracao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;

@Name("processoParteHome") 
@SuppressWarnings("unchecked")
public class ProcessoParteHome extends AbstractProcessoParteHome<ProcessoParte> {

	private static final long serialVersionUID = 1L;

	private ProcessoParteParticipacaoEnum inParticipacao = ProcessoParteParticipacaoEnum.A;
	private TipoParte tipoParte;
	private List<TipoParte> tipoPartes;
	private String urlOpenner;
	private String codInParticipacao;
	private String novaInsercao;
	private Boolean parteSigilosaTran;
	private String nomePessoa;
	private Boolean listaPopulada = Boolean.FALSE;
	private List<Object> listaObj = new ArrayList<>(0);
	private List<Object> listaNaoMarcados = new ArrayList<>(0);
	private SearchTree2GridList<ProcessoParte> searchTree2GridPoloAtivoList;
	private SearchTree2GridList<ProcessoParte> searchTree2GridPoloPassivoList;
	private SearchTree2GridList<ProcessoParte> searchTree2GridOutrosParticipantesList;
	private String justificativaSituacao;
	private Boolean mostrarInativosPoloAtivo = Boolean.FALSE;
	private Boolean mostrarInativosPoloPassivo = Boolean.FALSE;
	private Boolean mostrarInativosOutrosParticipantes = Boolean.FALSE;
	public static final String POLO_ATIVO = "A";
	public static final String POLO_PASSIVO = "P";
	public static final String POLO_OUTROS_PARTICIPANTES = "T";
	public static final String PROCESSO_PARTE_VINC_PESSOA_ENDERECO_GRID= "processoParteVinculoPessoaEnderecoGrid";
	private Boolean isAtivo = Boolean.TRUE;
	private Boolean flgVinculandoParte = false;
	private ProcessoParte processoParteConsulta;
	private Procuradoria procuradoriaAnterior;
	private Boolean houveInversaoPolo = Boolean.FALSE;
	private Boolean classeJudicialAnterioExigeFiscalDaLei = Boolean.FALSE;
	private Integer idTipoParteAdvogado;	
	private String polo;
	
	@In(create = true, required = false)
	@Out(required = false)
	public UIData dataTablePoloAtivo;
	
	@In(create = true, required = false)
	@Out(required = false)
	public UIData dataTablePoloPassivo;
	
	@In(create = true, required = false)
	@Out(required = false)
	public UIData dataTableOutrosParticipantes;
	
	@In
	private ProcessoJudicialService processoJudicialService;
	
	@In
	private ProcessoParteManager processoParteManager;
	
	@In
	private PessoaAutoridadeManager pessoaAutoridadeManager;
	
	@In
	private PessoaFisicaManager pessoaFisicaManager;
	
	@In
	private PessoaJuridicaManager pessoaJuridicaManager;
	
	@In
	private TipoParteConfigClJudicialManager tipoParteConfigClJudicialManager;
	
	@In
	private TipoParteManager tipoParteManager;
	
	@Logger
	private Log logger;
	
	public Boolean getIsAtivo() {
		return isAtivo;
	}

	public Boolean getMostrarInativosPoloAtivo() {
		return mostrarInativosPoloAtivo;
	}

	public void setMostrarInativosPoloAtivo(Boolean mostrarInativosPoloAtivo) {
		this.mostrarInativosPoloAtivo = mostrarInativosPoloAtivo;
	}

	public Boolean getMostrarInativosPoloPassivo() {
		return mostrarInativosPoloPassivo;
	}

	public void setMostrarInativosPoloPassivo(Boolean mostrarInativosPoloPassivo) {
		this.mostrarInativosPoloPassivo = mostrarInativosPoloPassivo;
	}

	public Boolean getMostrarInativosOutrosParticipantes() {
		return mostrarInativosOutrosParticipantes;
	}

	public void setMostrarInativosOutrosParticipantes(Boolean mostrarInativosOutrosParticipantes) {
		this.mostrarInativosOutrosParticipantes = mostrarInativosOutrosParticipantes;
	}

	public String getJustificativaSituacao() {
		return justificativaSituacao;
	}

	public void setJustificativaSituacao(String justificativaSituacao) {
		this.justificativaSituacao = justificativaSituacao;
	}

	public ProcessoParteSituacaoEnum[] getSituacaoValues() {
		return ProcessoParteSituacaoEnum.values();
	}

	public List<ProcessoParteSituacaoEnum> getSituacaoInativoValues() {
	    List<ProcessoParteSituacaoEnum> values = new ArrayList<ProcessoParteSituacaoEnum>();
	    values.add(ProcessoParteSituacaoEnum.I);
	    values.add(ProcessoParteSituacaoEnum.S);
	    if (getInstance().getPartePrincipal()){
	        values.add(ProcessoParteSituacaoEnum.B);
	    }
		return values;
	}

	public void adicionarMensagemErro(String campo, String mensagem) {
		FacesMessages.instance().clear();
		FacesMessages.instance().addToControl(campo, mensagem);
	}

	public SearchTree2GridList<ProcessoParte> getSearchTree2GridPoloAtivoSemOrdemList() {
		ProcessoParte searchBean = getComponent("processoParteSearch");
		Contexts.removeFromAllContexts("processoPartePoloAtivoSemOrdemSearchTree");
		AbstractTreeHandler<ProcessoParte> treeHandler = getComponent("processoPartePoloAtivoSemOrdemSearchTree");
		searchTree2GridPoloAtivoList = new SearchTree2GridList<ProcessoParte>(searchBean, treeHandler);
		return searchTree2GridPoloAtivoList;
	}

	public SearchTree2GridList<ProcessoParte> getSearchTree2GridPoloAtivoList() {
		ProcessoParte searchBean = getComponent("processoParteSearch");
		Contexts.removeFromAllContexts("processoPartePoloAtivoSearchTree");
		AbstractTreeHandler<ProcessoParte> treeHandler = getComponent("processoPartePoloAtivoSearchTree");
		searchTree2GridPoloAtivoList = new SearchTree2GridList<ProcessoParte>(searchBean, treeHandler);
		return searchTree2GridPoloAtivoList;
	}
	
	@SuppressWarnings("rawtypes")
	public PageDataModel<SearchTree2GridList<ProcessoParte>> getSearchDataModelPoloAtivoList() {
		ProcessoParte searchBean = getComponent("processoParteSearch");
		Contexts.removeFromAllContexts("processoPartePoloAtivoSearchTree");
		AbstractTreeHandler<ProcessoParte> treeHandler = getComponent("processoPartePoloAtivoSearchTree");
		treeHandler.setDataTable(dataTablePoloAtivo);
		searchTree2GridPoloAtivoList = new SearchTree2GridList<ProcessoParte>(searchBean, treeHandler);
		return new PageDataModel(searchTree2GridPoloAtivoList.getList(), treeHandler.getNumRows().intValue());
	}

	public SearchTree2GridList<ProcessoParte> getSearchTree2GridPoloPassivoSemOrdemList() {
		ProcessoParte searchBean = getComponent("processoParteSearch");
		Contexts.removeFromAllContexts("processoPartePoloPassivoSemOrdemSearchTree");
		AbstractTreeHandler<ProcessoParte> treeHandler = getComponent("processoPartePoloPassivoSemOrdemSearchTree");
		searchTree2GridPoloPassivoList = new SearchTree2GridList<ProcessoParte>(searchBean, treeHandler);
		return searchTree2GridPoloPassivoList;
	}

	public SearchTree2GridList<ProcessoParte> getSearchTree2GridPoloPassivoList() {
		ProcessoParte searchBean = getComponent("processoParteSearch");
		Contexts.removeFromAllContexts("processoPartePoloPassivoSearchTree");
		AbstractTreeHandler<ProcessoParte> treeHandler = getComponent("processoPartePoloPassivoSearchTree");
		searchTree2GridPoloPassivoList = new SearchTree2GridList<ProcessoParte>(searchBean, treeHandler);
		return searchTree2GridPoloPassivoList;
	}
	
	@SuppressWarnings("rawtypes")
	public PageDataModel<SearchTree2GridList<ProcessoParte>> getSearchDataModelPoloPassivoList() {
		ProcessoParte searchBean = getComponent("processoParteSearch");
		Contexts.removeFromAllContexts("processoPartePoloPassivoSearchTree");
		AbstractTreeHandler<ProcessoParte> treeHandler = getComponent("processoPartePoloPassivoSearchTree");
		treeHandler.setDataTable(dataTablePoloPassivo);
		searchTree2GridPoloPassivoList = new SearchTree2GridList<ProcessoParte>(searchBean, treeHandler);
		return new PageDataModel(searchTree2GridPoloPassivoList.getList(), treeHandler.getNumRows().intValue());
	}

	public SearchTree2GridList<ProcessoParte> getSearchTree2GridOutrosParticipantesSemOrdemList() {
		if (searchTree2GridOutrosParticipantesList == null) {
			ProcessoParte searchBean = getComponent("processoParteSearch");
			Contexts.removeFromAllContexts("processoParteOutrosParticipantesSemOrdemSearchTree");
			AbstractTreeHandler<ProcessoParte> treeHandler = getComponent("processoParteOutrosParticipantesSemOrdemSearchTree");
			searchTree2GridOutrosParticipantesList = new SearchTree2GridList<ProcessoParte>(searchBean, treeHandler);
		}
		return searchTree2GridOutrosParticipantesList;
	}

	public SearchTree2GridList<ProcessoParte> getSearchTree2GridOutrosParticipantesList() {
		if (searchTree2GridOutrosParticipantesList == null) {
			ProcessoParte searchBean = getComponent("processoParteSearch");
			AbstractTreeHandler<ProcessoParte> treeHandler = getComponent("processoParteOutrosParticipantesSearchTree");
			searchTree2GridOutrosParticipantesList = new SearchTree2GridList<ProcessoParte>(searchBean, treeHandler);
		}
		return searchTree2GridOutrosParticipantesList;
	}

	public void limparTrees() {
		if (searchTree2GridPoloAtivoList != null) {
			searchTree2GridPoloAtivoList.refreshTreeList();
			searchTree2GridPoloAtivoList = null;
		}
		if (searchTree2GridPoloPassivoList != null) {
			searchTree2GridPoloPassivoList.refreshTreeList();
			searchTree2GridPoloPassivoList = null;
		}
		if (searchTree2GridOutrosParticipantesList != null) {
			searchTree2GridOutrosParticipantesList.refreshTreeList();
			searchTree2GridOutrosParticipantesList = null;
		}
	}

	@Override
	public void newInstance() {
		limparTrees();
		refreshGrid("processoParteHomeGrid");
		getInstance().setCheckado(Boolean.FALSE);
		setListaPopulada(Boolean.FALSE);
		super.newInstance();
		setProcuradoriaAnterior(null);
	}
	
	public boolean possuiParteInativaPoloAtivo() {
		boolean resultado = possuiParteInativa(ProcessoParteParticipacaoEnum.A);
		if (!resultado) {
			this.mostrarInativosPoloAtivo = Boolean.FALSE;
		}
		return resultado;
	}
	
	public boolean possuiParteInativaPoloPassivo() {
		boolean resultado = possuiParteInativa(ProcessoParteParticipacaoEnum.P);
		if (!resultado) {
			this.mostrarInativosPoloPassivo = Boolean.FALSE;
		}
		return resultado;
	}
	
	public boolean possuiParteInativaOutrosParticipantes() {
		boolean resultado = possuiParteInativa(ProcessoParteParticipacaoEnum.T);
		if (!resultado) {
			this.mostrarInativosOutrosParticipantes =  Boolean.FALSE;
		}
		return resultado;
	}

	public boolean possuiParteInativa(ProcessoParteParticipacaoEnum inParticipacao) {
		return processoParteManager.possuiParteInativa(ProcessoTrfHome.instance().getInstance(), inParticipacao);
	}

	public void instanciarRow(ProcessoParte parte) {
		if (parte.getParteSigilosa()) {
			setParteSigilosaTran(Boolean.TRUE);
			parte.setParteSigilosa(Boolean.FALSE);
		} else {
			setParteSigilosaTran(Boolean.FALSE);
			parte.setParteSigilosa(Boolean.TRUE);
		}
		setInstance(parte);
	}

	public void inserirPrazos(ProcessoParte obj) {
		getInstance().setPrazoLegal(obj.getPrazoLegal());
		getInstance().setPrazoProcessual(obj.getPrazoProcessual());
	}

	public void inserirAdvogado(UsuarioLocalizacao obj){

		getInstance().setTipoParte(ParametroUtil.instance().getTipoParteAdvogado());
		getInstance().setInParticipacao(ProcessoParteParticipacaoEnum.A);
		getInstance().setProcessoTrf(ProcessoTrfHome.instance().getInstance());
		getInstance().setPessoa(EntityUtil.find(PessoaFisica.class, obj.getUsuario().getIdUsuario()));
		
		ProcessoTrfHome.instance().getInstance().getProcessoParteList().add(getInstance());

		persist();
		
	}
	 
	/**
	 *  Metodo para remover todos as pessoas associadas a processoParte
	 */
	public void removerProcessoParteAssociados(ProcessoParte obj) {
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder builder = new StringBuilder();
		builder.append("select o from ProcessoParteAdvogado o ");
		builder.append("where o.processoParte = :processoParte");
		Query q = em.createQuery(builder.toString());
		q.setParameter("processoParte", obj);
		List<ProcessoParteAdvogado> listaPPA = q.getResultList();
		for (ProcessoParteAdvogado ppa : listaPPA) {
			LogUtil.removeEntity(ppa);
			EntityUtil.flush();
			limparPP(obj, null, ppa);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoParteRepresentante o ");
		sb.append("where o.processoParte = :processoParte or ");
		sb.append("o.representante = :pessoa");
		Query q2 = em.createQuery(sb.toString());
		q2.setParameter("processoParte", obj);
		q2.setParameter("pessoa", obj.getPessoa());
		List<ProcessoParteRepresentante> listaPPR = q2.getResultList();
		for (ProcessoParteRepresentante ppr : listaPPR) {
			LogUtil.removeEntity(ppr);
			EntityUtil.flush();
			limparPP(obj, ppr, null);
		}
	}

	public void limparPP(ProcessoParte pp, ProcessoParteRepresentante ppr, ProcessoParteAdvogado ppa) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoParte o ");
		sb.append("where o.processoTrf = :processoTrf ");
		sb.append("and o.inParticipacao = :participacao");
		Query qA = getEntityManager().createQuery(sb.toString());
		qA.setParameter("processoTrf", ProcessoTrfHome.instance().getInstance());
		qA.setParameter("participacao", pp.getInParticipacao());
		List<ProcessoParte> listaPP = qA.getResultList();
		for (ProcessoParte processoParte : listaPP) {
			processoParte.getProcessoParteRepresentanteList().remove(ppr);
			processoParte.getProcessoParteAdvogadoList().remove(ppa);
		}
	}

	@Override
	public String remove(ProcessoParte obj) {
		ProcessoTrf processoTrf = getProcessoTrfAtual();
		processoTrf.getProcessoParteList().remove(obj);
		removerProcessoParteAssociados(obj);
		getEntityManager().merge(processoTrf);
		LogUtil.removeEntity(obj);
		EntityUtil.flush();
		refreshGrid("processoParteGrid");
		refreshGrid("pessoaParteProcessoGrid");
		refreshGrid("processoPoloAtivoGrid");
		refreshGrid("processoAbaPartePoloAtivoGrid");
		refreshGrid("processoAbaPartePoloPassivoGrid");
		refreshGrid("processoAbaParteTerceiroGrid");
		refreshGrid("processoPoloPassivoGrid");
		refreshGrid("processoFiscalLeiGrid");
		refreshGrid("processoIncidentePoloAtivoGrid");
		refreshGrid("processoIncidentePoloPassivoGrid");
		return "deleted";
	}

	public void removeParte(ProcessoParte processoParte, String gridId) {
		ProcessoTrf processoTrf = getProcessoTrfAtual();
		processoTrf.getProcessoParteList().remove(processoParte);
		getEntityManager().merge(processoTrf);
		getEntityManager().remove(processoParte);
		getEntityManager().flush();
		refreshGrid(gridId);
		refreshGrid("processoParteGrid");
		refreshGrid("pessoaParteProcessoGrid");
		refreshGrid("processoPoloAtivoGrid");
		refreshGrid("processoAbaPartePoloAtivoGrid");
		refreshGrid("processoAbaPartePoloPassivoGrid");
		refreshGrid("processoAbaParteTerceiroGrid");
		refreshGrid("processoPoloPassivoGrid");
		refreshGrid("processoFiscalLeiGrid");
	}

	public void addParte(Pessoa parte, String gridId) {
		if (!validadeField(tipoParte, "comboTipoParte") || !validadeField(inParticipacao, "comboParticipacaoParte")) {
			return;
		}
		cadastrarParte(getProcessoTrfAtual(), parte, false, tipoParte, inParticipacao, null, false);
		refreshGrid(gridId);
		refreshGrid("processoParteGrid");
		refreshGrid("processoPoloAtivoGrid");
		refreshGrid("processoAbaPartePoloAtivoGrid");
		refreshGrid("processoAbaPartePoloPassivoGrid");
		refreshGrid("processoAbaParteTerceiroGrid");
		refreshGrid("pessoaParteProcessoGrid");
		refreshGrid("processoPoloPassivoGrid");
		refreshGrid("processoFiscalLeiGrid");
	}

	private boolean validadeField(Object object, String idComponent) {
		if (object == null) {
			FacesMessages.instance().addToControl(idComponent, StatusMessage.Severity.ERROR, "campo obrigatório");
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Campo Obrigatório");
			return false;
		} else {
			return true;
		}
	}

	public void anularTipoParte() {
		tipoParte = null;
	}

	public void initCadProcessoParte() {
		SelectItemsQuery queryTipo = getComponent("tipoParteItems");
		if(queryTipo != null){
			queryTipo.setMaxResults(1);
			if (queryTipo.getResultCount() > 0) {
				tipoParte = (TipoParte) queryTipo.getResultList().get(0);
			}
		}
	}

	public ProcessoTrfHome getProcessoTrfHome() {
		return ComponentUtil.getComponent("processoTrfHome");
	}

	public void setInParticipacao(ProcessoParteParticipacaoEnum inParticipacao) {
		this.inParticipacao = inParticipacao;
	}

	public ProcessoParteParticipacaoEnum getInParticipacao() {
		return inParticipacao;
	}

	public void setTipoParte(TipoParte tipoParte) {
		this.tipoParte = tipoParte;
	}

	public TipoParte getTipoParte() {
		return tipoParte;
	}
	
	public List<TipoParte> getTipoPartes() {
		return tipoPartes;
	}
	

	public List<ProcessoParte> getPartesProcesso(ProcessoTrf processo) {
		List<ProcessoParte> list = new ArrayList<ProcessoParte>(processo.getProcessoParteList());
		Collections.sort(list, new ProcessoParteComparator());
		return list;
	}

	public ProcessoTrf getProcessoTrfAtual() {
		return getProcessoTrfHome().getInstance();
	}

	public String getNomePoloPassivo() {
		String ret = "Polo Passivo";
		ProcessoTrfHome processo = (ProcessoTrfHome) Component.getInstance("processoTrfHome");
		if ((processo.getInstance() != null) && (processo.getInstance().getClasseJudicial() != null)) {
			ClasseJudicial classeJudicial = processo.getInstance().getClasseJudicial();
			TipoParte tipoParte = tipoParteManager.tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.P);
			ret = tipoParte.getTipoParte();
		}
		return ret;
	}

	public String getNomePoloAtivo() {
		String ret = "Polo Ativo";
		ProcessoTrfHome processo = (ProcessoTrfHome) Component.getInstance("processoTrfHome");
		if ((processo.getInstance() != null) && (processo.getInstance().getClasseJudicial() != null)) {
			ClasseJudicial classeJudicial = processo.getInstance().getClasseJudicial();
			TipoParte tipoParte = tipoParteManager.tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.A);
			ret = tipoParte.getTipoParte();
		}
		return ret;
	}

	public void setarCombo() {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		ClasseJudicial classeJudicial = processoTrf.getClasseJudicial();
		if ((ProcessoTrfHome.instance().getListaAtivos().size() == 0) || (!verificaPoloAtivo())) {
			if (codInParticipacao.equals("A")) {
				TipoParte tipoParte = tipoParteManager.tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.A);
				setTipoParte(tipoParte);
			}
		}
		if ((ProcessoTrfHome.instance().getListaPassivos().size() == 0)) {
			if (codInParticipacao.equals("P")) {
				TipoParte tipoParte = tipoParteManager.tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.A);
				setTipoParte(tipoParte);
			}
		}
	}

	public void limparVinculacaoParte(Boolean flgVinculandoParte, String polo) {
		if (!polo.isEmpty()) {
			resetarVinculacaoParte(flgVinculandoParte, polo);
		}
	}
	
	private void removerContextProcessoParteVinculoPessoaEnderecoGrid() {
		Contexts.removeFromAllContexts(PROCESSO_PARTE_VINC_PESSOA_ENDERECO_GRID);
	}

	public void resetarVinculacaoParte(Boolean flgVinculandoParte, String polo) {
		Contexts.removeFromAllContexts("profissaoSuggest");
		Contexts.removeFromAllContexts("pessoaDocumentoIdentificacaoPreCadastroGrid");
		removerContextProcessoParteVinculoPessoaEnderecoGrid();
		Contexts.removeFromAllContexts("processoParteVinculoPessoaMeioContatoGrid");
		PessoaDocumentoIdentificacaoHome.instance().newInstance();
	  	PessoaDocumentoIdentificacaoHome.instance().setId(null);
	  	PessoaDocumentoIdentificacaoHome.instance().irNovo();
	  	PessoaFisicaHome.instance().newInstance();
	  	PessoaJuridicaHome.instance().newInstance();
	  	PessoaAutoridadeHome.instance().newInstance();
		MeioContatoHome.instance().newInstance();
		ProfissaoSuggestBean.instance().newInstance();
		PreCadastroPessoaBean preBean = (PreCadastroPessoaBean) Component.getInstance("preCadastroPessoaBean");
		preBean.resetarBean();
		if (ParametroJtUtil.instance().justicaFederal()) {
			preBean.setInTipoPessoa(TipoPessoaEnum.J);
			preBean.setIsBrasileiro(true);
			preBean.setIsOrgaoPublico(true);
		} else if((ParametroJtUtil.instance().justicaTrabalho())
				&& (polo.equals(POLO_PASSIVO))){
			preBean.setInTipoPessoa(TipoPessoaEnum.J);
			preBean.setIsOrgaoPublico(Boolean.FALSE);
		} else {
			preBean.setInTipoPessoa(TipoPessoaEnum.F);
			preBean.setIsBrasileiro(true);
			preBean.setIsOrgaoPublico(false);
		}
		preBean.setIsPartes(true);
		ProcessoTrf processoTrfInstance = ProcessoTrfHome.instance().getInstance();

		super.newInstance();
		limparTrees();
		this.polo = polo;
		setFlgVinculandoParte(flgVinculandoParte);
		setTipoParte(null);

		getInstance().setInParticipacao(ProcessoParteParticipacaoEnum.valueOf(polo));
		codInParticipacao = polo;
		inParticipacao = getInstance().getInParticipacao();
		tipoPartes = new ArrayList<TipoParte>();
		carregarTipoPartes(processoTrfInstance,polo);
		ProcessoParteRepresentanteHome ppr = ProcessoParteRepresentanteHome.instance();
		ppr.setFlgInclusao(true);
		ppr.setFlgVinculandoRepresentante(false);

	}
	
	/**
	 * De acordo com a {@link ClasseJudicial} contida na instância e o Polo selecionado
	 * carrega a opção de TipoPartes. 
	 * @param processoTrfInstance
	 * @param polo
	 */
	private void carregarTipoPartes(ProcessoTrf processoTrfInstance, String polo) {
		List<TipoParteConfigClJudicial> partesClasseJudicial = processoTrfInstance.getClasseJudicial().getTipoParteConfigClJudicial();
		for(TipoParteConfigClJudicial parteClasse : partesClasseJudicial){
			if(parteClasse.getTipoParteConfiguracao().getTipoParte().getTipoPrincipal() != null && parteClasse.getTipoParteConfiguracao().getTipoParte().getTipoPrincipal() && parteClasse.getTipoParteConfiguracao().getTipoParte().getAtivo() != null && parteClasse.getTipoParteConfiguracao().getTipoParte().getAtivo()){
				classficarPolo(polo, parteClasse);
			}
		}
		verificarTamanhoTipo();
	}

	/**
	 * Conforme regras de negócio se a {@link ClasseJudicial} seleciona
	 * na instância conter apenas um {@link TipoParte} ela já será setada na combo.
	 */
	private void verificarTamanhoTipo(){
		if(tipoPartes != null && tipoPartes.size() == 1){
			instance.setTipoParte(tipoPartes.get(0));
		}
	}
	
	/**
	 * Classifica o Polo de acordo com a {@link TipoParteConfiguracao} e o polo informados.
	 * @param polo
	 * @param parteClasse
	 */
	private void classficarPolo(String polo, TipoParteConfigClJudicial parteClasse) {
		if(POLO_ATIVO.equals(polo)){
			montarPolo(parteClasse,parteClasse.getTipoParteConfiguracao().getPoloAtivo());
		}else if(POLO_PASSIVO.equals(polo)){
			montarPolo(parteClasse,parteClasse.getTipoParteConfiguracao().getPoloPassivo());
		}else{
			montarPolo(parteClasse, parteClasse.getTipoParteConfiguracao().getOutrosParticipantes());
		}
	}

	/**
	 * De acordo com as regras, verifica se a {@link TipoParteConfiguracao} contém a opção OAB selecionada
	 * @return
	 */
	public boolean isOABSelecionado(){
		boolean selecionado = Boolean.FALSE;
		TipoParte tipoParte = instance.getTipoParte();
		ClasseJudicial classeJudicial = ProcessoTrfHome.instance().getInstance().getClasseJudicial();
		if(tipoParte != null){
			List<TipoParteConfigClJudicial> tipoParteConfigClJudicial =  tipoParteConfigClJudicialManager.recuperarTipoParteConfiguracao(classeJudicial);
			for(TipoParteConfigClJudicial config : tipoParteConfigClJudicial){
				if(config.getTipoParteConfiguracao().getTipoParte().equals(instance.getTipoParte())){
					selecionado = config.getTipoParteConfiguracao().getOab() != null && config.getTipoParteConfiguracao().getOab();
					break;
				}
			}
		}
		return selecionado;
	}
	
	/**
	 * Conforme as regras o o polo.
	 * @param parte
	 * @param polo
	 */
	private void montarPolo(TipoParteConfigClJudicial parte, Boolean polo) {
		if(polo != null && polo && !tipoPartes.contains(parte.getTipoParteConfiguracao().getTipoParte())){
			tipoPartes.add(parte.getTipoParteConfiguracao().getTipoParte());
		}
	}
	
	public void reset() {
		resetarVinculacaoParte(false, POLO_ATIVO);
		tipoParte = null;
		urlOpenner = null;
		novaInsercao = null;
		parteSigilosaTran = null;
		nomePessoa = null;
		listaPopulada = Boolean.FALSE;
		listaObj = new ArrayList<Object>(0);
		listaNaoMarcados = new ArrayList<Object>(0);
		limparTrees();
	}
	
	/**
	 * Verifica se o TipoParte foi selecionado.
	 * @return
	 */
	public boolean isTipoParteSecionado(){
		return instance.getTipoParte() != null;
	}

	public void inserir(PessoaJuridica pessoa) {
		Pessoa pessoaF = pessoa;
		inserir(pessoaF);
	}

	public void inserir() {
		
		Pessoa pessoa = null;
		PreCadastroPessoaBean preBean = (PreCadastroPessoaBean) Component.getInstance("preCadastroPessoaBean");
		boolean orgaoPublicoPJ = preBean.getIsOrgaoPublico() && preBean.getOrgaoPubSelec() != null;
		
		if(orgaoPublicoPJ){
			preBean.setIsConfirmado(Boolean.TRUE);
			preBean.setInTipoPessoa(TipoPessoaEnum.J);
			preBean.setPessoaJuridica((PessoaJuridica)preBean.getOrgaoPubSelec());
			preBean.setHasPessoaJuridica(Boolean.TRUE);
		}
		
		pessoa = preBean.getPessoa();
		
		ProcessoTrf processoTrfInstance = ProcessoTrfHome.instance().getInstance();
		ProcessoParteRepresentanteHome pprHome = ProcessoParteRepresentanteHome.instance();
		ProcessoParteParticipacaoEnum participacaoEnum = getInstance().getInParticipacao();
		ClasseJudicial classeJudicial = processoTrfInstance.getClasseJudicial();
		String ignorarValidacao = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
				.getParameter("ignorarValidacao");
		
		FacesMessages.instance().clear();

		if(!orgaoPublicoPJ){
			try {
				associarEnderecoParteProcesso(getInstance());
			} catch (Exception e) {
				FacesMessages.instance().add(Severity.WARN, e.getLocalizedMessage());
				return;
			}
		}

		if (!preBean.getIsConfirmado()) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Confirme a pessoa antes de adicioná-la como parte");
			return;
		}

		if (getInstance().getTipoParte() == null) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Escolha um tipo de parte");
			return;
		}

		List<ProcessoParte> listaParteProcesso = processoTrfInstance.getListaPartePoloObj(true, participacaoEnum);
		try {
			validarParteExistente(pessoa, listaParteProcesso);
		} catch (PJeException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getCode());
			return;
		}
		removerContextProcessoParteVinculoPessoaEnderecoGrid();
		boolean permiteEnderecoDesconhecido = permiteEnderecoDesconhecido(pessoa);
		if( ! ( pessoa instanceof PessoaAutoridade ) && 
				(getInstance().getEnderecos() != null && getInstance().getEnderecos().size() > 0 && getInstance().getEnderecos().get(0) == null) &&
				!getInstance().getIsEnderecoDesconhecido() && !orgaoPublicoPJ && permiteEnderecoDesconhecido){
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "É necessário cadastrar/selecionar um endereço ou marcar 'Endereço Desconhecido'.");
			return;
		}
				

		if (participacaoEnum.name().equals(POLO_ATIVO)) {
			List<ProcessoParte> listaPartePassivo = processoTrfInstance.getListaPartePassivo();
			
			for (ProcessoParte pp : listaPartePassivo) {
				if (pessoa != null && pp.getPessoa() != null && pp.getPessoa().getIdPessoa().equals(pessoa.getIdPessoa()) && ignorarValidacao == null) {
					((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).setAttribute(
							"avisoPessoaCadastrada", "passivo");
					return;
				}
			}
			this.setTipoParte(tipoParteManager.tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.A));
		} else if (participacaoEnum.name().equals(POLO_PASSIVO)) {
			List<ProcessoParte> listaParteAtivo = processoTrfInstance.getListaParteAtivo();
			
			for (ProcessoParte pp : listaParteAtivo) {
				if (pessoa != null && pp.getPessoa() != null && pp.getPessoa().getIdPessoa().equals(pessoa.getIdPessoa()) && ignorarValidacao == null) {
					((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).setAttribute(
							"avisoPessoaCadastrada", "ativo");
					return;
				}
			}
			this.setTipoParte(tipoParteManager.tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.P));
		}
		
		getInstance().setPessoa(pessoa);
		getInstance().setProcessoTrf(processoTrfInstance);
		getInstance().setPartePrincipal(true);
		getInstance().setInParticipacao(participacaoEnum);
		
		ProcuradoriaManager procuradoriaManager = 
				(ProcuradoriaManager)Component.getInstance("procuradoriaManager");
		
		List<Procuradoria> procuradorias = procuradoriaManager.getlistProcuradorias(pessoa);
		if(procuradorias != null && procuradorias.size() == 1){
			getInstance().setProcuradoria(procuradorias.get(0));
		}

		if (participacaoEnum.name().equals(POLO_ATIVO) && (Authenticator.isAdvogado() || Authenticator.isAssistenteAdvogado())) {
			ProcessoParte pp = processoParteManager.recuperarParteAdvogadoPoloAtivoSemRepresentando(processoTrfInstance);
			if (pp != null) {
				ProcessoParteRepresentante representacao = pprHome.criarRepresentacao(pp, getInstance(), pp.getTipoParte());
				getInstance().getProcessoParteRepresentanteList().add(representacao);
			}
		}
		processoTrfInstance.getProcessoParteList().add(getInstance());
		try {
			habilitarVisualizacaoSegredoJustica(pessoa, getInstance().getProcuradoria());
			/*
			 * Limpa a lista auxiliar que contem os "Outros Nomes" das partes de um processo.
			 * Assim ao entrar na listagem de novo, a grid não exibirá os botoes de excluir na grid da aba "outros nomes" 
			 */
			GridQuery g = getComponent("pessoaNomeAlternativoGrid", true);
			List<PessoaNomeAlternativo> itensGrid = null;
			if (g != null) {
				itensGrid = g.getResultList();
			}
			if (itensGrid != null && !itensGrid.isEmpty()) {
				for (PessoaNomeAlternativo pna : itensGrid) {
					pna.setListaExclusaoOutrosNomes(new ArrayList<String>());
				}
			}
			Integer idProcesso = getInstance().getProcessoTrf().getIdProcessoTrf();
			
			persist(false);

			if(!this.isRetificacao() && this.getInstance().getInParticipacao().equals(ProcessoParteParticipacaoEnum.P)){
				this.criarNovaInformacaoCriminalRascunho(idProcesso, new Long(this.getInstance().getIdProcessoParte()));
			}
			
			EntityUtil.flush(EntityUtil.getEntityManager());

			if(this.isRetificacao()) {
				dispararMensagemPosAlteracaoOuRemocaoParte(this.getInstance(), CloudEventVerbEnum.POST);
				sinalizarAlteracoesCadastroPartes(processoTrfInstance, this.getInstance());
			}

			this.newInstance();
			
		} catch (Exception e) {
			adicionarMensagemErro("errosCadastroParte", "Erro inesperado ao incluir a parte. Tente novamente.");
			logger.error("Erro ao adicionar parte: {0}", e, pessoa);
			return;
		}
		FacesMessages.instance().clear();
	}

	private void criarNovaInformacaoCriminalRascunho(Integer idProcessoJudicial, Long idProcessoParte) throws PJeBusinessException{
		ClasseJudicialManager cjm = ComponentUtil.getComponent(ClasseJudicialManager.NAME);
		
		if(cjm.isClasseCriminalOuInfracional(this.getInstance().getProcessoTrf().getClasseJudicial())){
			ProcessoParteManager ppm = ComponentUtil.getComponent(ProcessoParteManager.NAME);
			ProcessoRascunhoManager prm = ComponentUtil.getComponent(ProcessoRascunhoManager.NAME);
			InformacaoCriminalRascunhoManager icrm = ComponentUtil.getComponent(InformacaoCriminalRascunhoManager.NAME);
			
			ProcessoParteMin pp = ppm.recuperarProcessoParteMinPorId(idProcessoParte);
			ProcessoRascunho pr = prm.recuperarOuCriarProcessoRascunho(idProcessoJudicial);

			ConteudoInformacaoCriminalBean conteudo = new ConteudoInformacaoCriminalBean(); 
			
			InformacaoCriminalRascunho ic = new InformacaoCriminalRascunho();
			
			ic.setProcessoParte(pp);
			ic.setProcessoRascunho(pr);
			ic.setInformacaoCriminal(conteudo);
			
			icrm.persist(ic);
			
		}
	}

	/**
	 * Lanca evento de alteração do relacionamento de pessoa com procuradoria, por padrao com os identificadores: pessoa / procuradoria / procuradoriaPadrao / pessoa
	 * 
	 * @param pessoa
	 * @param procuradoria
	 * @param processo
	 */
	private void lancarEventoAlteracaoProcuradoriaParte(Pessoa pessoa, Procuradoria procuradoriaNova, Procuradoria procuradoriaAnterior, ProcessoTrf processo){
		Map<String, Object> payloadEvento = new HashMap<String, Object>();
		int idPessoa = 0;
		if(pessoa.getIdPessoa() != null){
			idPessoa = pessoa.getIdPessoa();
		}
		payloadEvento.put("idPessoa", idPessoa);
		int idProcuradoriaNova = 0;
		if(procuradoriaNova != null && procuradoriaNova.getIdProcuradoria() != 0){
			idProcuradoriaNova = procuradoriaNova.getIdProcuradoria();
		}
		payloadEvento.put("idProcuradoriaNova", idProcuradoriaNova);
		int idProcuradoriaAnterior = 0;
		if(procuradoriaAnterior != null && procuradoriaAnterior.getIdProcuradoria() != 0){
			idProcuradoriaAnterior = procuradoriaAnterior.getIdProcuradoria();
		}
		payloadEvento.put("idProcuradoriaAnterior", idProcuradoriaAnterior);
		int idProcesso = 0;
		if(processo != null && processo.getIdProcessoTrf() != 0){
			idProcesso = processo.getIdProcessoTrf();
		}
		payloadEvento.put("idProcesso", idProcesso);
		
		Events.instance().raiseEvent(Eventos.ALTERACAO_PROCURADORIA_PARTE, payloadEvento);
		
		logger.debug(Severity.INFO, "EVENTO - Alteracao no relacionamento de procuradoria e parte.");		
	}
	
	public boolean atualizarParte() {
		PreCadastroPessoaBean preBean = (PreCadastroPessoaBean) Component.getInstance("preCadastroPessoaBean");
		if (preBean.getIsConfirmado()) {
			if (preBean.getInTipoPessoa() == TipoPessoaEnum.F) {
				PessoaFisicaHome pfHome = (PessoaFisicaHome) Component.getInstance("pessoaFisicaHome");
				String update = pfHome.update();
				if (update == null) {
					return false;
				}
				Procuradoria procuradoriaNova = getInstance().getProcuradoria();
				
				Procuradoria procuradoriaAnterior = getProcuradoriaAnterior();
				Pessoa pessoa = getInstance().getPessoa();
				ProcessoTrf processo = getInstance().getProcessoTrf();
				if(procuradoriaNova != null) {
					ProcessoParteExpedienteManager ppeManager = (ProcessoParteExpedienteManager) Component.getInstance(ProcessoParteExpedienteManager.NAME);
					List<ProcessoParteExpediente> listExpedientes = ppeManager.recuperaExpedientesAbertosPorProcessoParte(getInstance());
					for(ProcessoParteExpediente ppe : listExpedientes) {
						ppe.setProcuradoria(procuradoriaNova);
						getEntityManager().persist(ppe);
						EntityUtil.flush();
						
					}
				}
				this.lancarEventoAlteracaoProcuradoriaParte(pessoa, procuradoriaNova, procuradoriaAnterior, processo);
				this.setProcuradoriaAnterior(procuradoriaNova);
				
			} else if (preBean.getInTipoPessoa() == TipoPessoaEnum.J) {
				PessoaJuridicaHome pjHome = (PessoaJuridicaHome) Component.getInstance("pessoaJuridicaHome");
				String update = pjHome.update();
				if (update == null) {
					return false;
				}				
			} else {
				PessoaAutoridadeHome paHome = (PessoaAutoridadeHome) Component.getInstance("pessoaAutoridadeHome");
				paHome.getInstance().setInTipoPessoa(TipoPessoaEnum.A);
				paHome.update();
			}
			try {
				associarEnderecoParteProcesso(getInstance());
			} catch (Exception e) {
				FacesMessages.instance().add(Severity.WARN, e.getLocalizedMessage());
				return false;
			}
			
			ProcuradoriaManager procuradoriaManager = (ProcuradoriaManager)Component.getInstance("procuradoriaManager");
			/**
			 * Caso seja informada uma pessoa com um vínculo de somente uma procuradoria é necessário
			 * atualizar o registro na home.
			 */
			List<Procuradoria> procuradorias = procuradoriaManager.getlistProcuradorias(preBean.getPessoa());
			if(procuradorias != null && procuradorias.size() == 1){
				getInstance().setProcuradoria(procuradorias.get(0));
			}
			
			this.update();
			EntityUtil.getEntityManager().flush();
			refreshGrid(PROCESSO_PARTE_VINC_PESSOA_ENDERECO_GRID);

			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Registro atualizado com sucesso.");

		}
		
		/*
		 * Limpa a lista auxiliar que ontem os "Outros Nomes" das partes de um processo.
		 * Assim ao entrar na listagem denovo, a grid não exibirá os botoes de excluir na grid da aba "outros nomes"
		 */
		GridQuery g = getComponent("pessoaNomeAlternativoGrid",false);
		
		if(g!=null){
			List<PessoaNomeAlternativo> itensGrid = g.getResultList();
			if(itensGrid != null){
				for(PessoaNomeAlternativo pna : itensGrid){
					pna.setListaExclusaoOutrosNomes(new ArrayList<String>());
				}
			}
		}
		return true;
	}

	public void inserirRepresentante() {
		PreCadastroPessoaBean preBean = (PreCadastroPessoaBean) Component.getInstance("preCadastroPessoaBean");
		if (!preBean.getIsConfirmado()) {
			adicionarMensagemErro(StringUtils.EMPTY, "Confirme a pessoa antes de adicioná-la como representante.");
			return;
		}
		
		if (getInstance().getTipoParte() == null) {
			adicionarMensagemErro(StringUtils.EMPTY, "Escolha o tipo de representação.");
			return;
		}
		
		this.setTipoParte(getInstance().getTipoParte());
		
		ProcessoParteRepresentanteHome pprHome = ProcessoParteRepresentanteHome.instance();
		if (pprHome.getPartes() == null || pprHome.getPartes().isEmpty()) {
			adicionarMensagemErro(StringUtils.EMPTY, "Associe ao menos uma pessoa a este representante.");
			return;
		}
		
		ProcessoTrf processoTrfInstance = getProcessoTrfAtual();
		ProcessoParteManager processoParteManager = ComponentUtil.getComponent("processoParteManager");
		ProcessoParteParticipacaoEnum polo = ProcessoParteParticipacaoEnum.valueOf(pprHome.getCodInParticipacao());
		
		String ignorarValidacao = new Util().getRequest().getParameter("ignorarValidacao");
		if (isRepresentanteJaCadastradoNoPolo(preBean, processoTrfInstance.getListaParteAtivo(), polo, POLO_PASSIVO, ignorarValidacao)) {
	    	adicionarMensagemRepresentateJaCadastrado(FacesUtil.getMessage("processoParteRepresentante.ja.cadastrado.poloAtivo"), "ativo");
			return;
		} else if (isRepresentanteJaCadastradoNoPolo(preBean, processoTrfInstance.getListaPartePassivo(), polo, POLO_ATIVO, ignorarValidacao)) {
	    	adicionarMensagemRepresentateJaCadastrado(FacesUtil.getMessage("processoParteRepresentante.ja.cadastrado.poloPassivo"), "passivo");
			return;
		}
		
		ProcessoParte parte = processoParteManager.findProcessoParte(processoTrfInstance, getInstance().getTipoParte(), preBean.getPessoa(), polo);
		if (parte != null) {
			setId(parte.getIdProcessoParte());
		}

		// Independente de criada uma nova instância de ProcessoParte abaixo, manter-se-á o valor do atributo
		final Boolean isEnderecoDesconhecido = getInstance().getIsEnderecoDesconhecido();

		if (getInstance().getIdProcessoParte() == 0) {
			newInstance();
		}
		
		ProcessoParte representante = getInstance();
		representante.setPessoa(preBean.getPessoa());
		representante.setProcessoTrf(processoTrfInstance);
		representante.setTipoParte(this.getTipoParte());
		representante.setInParticipacao(polo);
		representante.setIsEnderecoDesconhecido(isEnderecoDesconhecido);
		representante.setInSituacao(ProcessoParteSituacaoEnum.A);
		
		if (this.getInstance().getIdProcessoParte() == 0) {
			List<ProcessoParte> listaParteProcesso = processoTrfInstance.getListaPartePoloObj(true,representante.getInParticipacao());
			try {
				validarParteExistente(preBean.getPessoa(), listaParteProcesso);
			} catch (PJeException e) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getCode());
				return;
			}
		}
		
		List<ProcessoParteRepresentante> representacoesAtuais = representante.getProcessoParteRepresentanteList2();
		for (ProcessoParteRepresentante processoParteRepresentante : representacoesAtuais) {
			processoParteRepresentante.setInSituacao(ProcessoParteSituacaoEnum.I);
		}
				
		for (ProcessoParte processoParte : pprHome.getPartes()) {
			ProcessoParteRepresentante representacaoNova = pprHome.criarRepresentacao(representante, processoParte, tipoParte);
			if (representacoesAtuais.contains(representacaoNova)) {
				representacoesAtuais.get(representacoesAtuais.indexOf(representacaoNova)).setInSituacao(ProcessoParteSituacaoEnum.A);
			} else {
				representacoesAtuais.add(representacaoNova);
			}
		}
		
		try {
			associarEnderecoParteProcesso(getInstance());
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.WARN, "Houve um erro ao tentar associar o endereo  parte. {0}", e.getLocalizedMessage());
			return;
		}
		
		if (processoTrfInstance.getProcessoStatus().equals(ProcessoStatusEnum.D)) {
			try {
				processoJudicialService.acrescentaVisualizador(processoTrfInstance, representante.getPessoa(), null, false);
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.WARN, "Houve um erro ao tentar adicionar o representante como visulizador do processo. {0}", e.getLocalizedMessage());
				return;
			}
		}
		
		ProcessoPushManager.instance().inserirNoPush(representante);
		persist();
		FacesMessages.instance().clear();
	}

	/**
	 * Adiciona a mensagem que será exibida em tela, caso o representante já esteja cadastrado em outro polo.
	 * @param chaveMsg Chave da mensagem que é declarada no entity_messages
	 * @param polo Polo onde pode haver já cadastrado o representante
	 */
	private void adicionarMensagemRepresentateJaCadastrado(String chaveMsg, String polo) {
		adicionarMensagemErro(StringUtils.EMPTY, chaveMsg);
		((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).setAttribute("avisoPessoaCadastrada", polo);
	}

	/**
	 * Verifica se o representante já está cadastrado no polo indicado no
	 * parâmetro.
	 * 
	 * @param preBean
	 * @param partesPolo
	 * @param poloProcessoParte
	 * @param polo
	 * @param ignorarValidacao
	 * @return	verdadeiro se o polo do processo parte for igual ao polo analisado, se já existir o representante 
	 * 			no polo analisado e se a flag ignorarValidacao for nula.
	 */
	private boolean isRepresentanteJaCadastradoNoPolo(PreCadastroPessoaBean preBean, List<ProcessoParte> partesPolo, 
			ProcessoParteParticipacaoEnum poloProcessoParte, String polo, String ignorarValidacao) {
		return ignorarValidacao == null && polo.equals(poloProcessoParte.name()) 
					&& isRepresentanteJaCadastradoOutroPolo(partesPolo, preBean.getPessoa());
	}
	
	/**
	 * Método utilizado para verificar se existe algum representante já cadastrado no outro polo.
	 * 
	 * @param List<ProcessoParte> listaParte
	 * @param Pessoa pessoa
	 * @return boolean.
	 */
	public boolean isRepresentanteJaCadastradoOutroPolo(List<ProcessoParte> listaParte, Pessoa pessoa){
		for (ProcessoParte pp : listaParte) {
			ProcessoParteRepresentanteManager processoParteRepresentanteManager = getProcessoParteRepresentanteManager();
			List<ProcessoParteRepresentante> representantes = processoParteRepresentanteManager.retornarRepresentantesParte(pp);
			
			for(ProcessoParteRepresentante representante: representantes){
				if (pessoa != null && representante.getRepresentante() != null 
						&& pessoa.getIdPessoa().equals(representante.getRepresentante().getIdPessoa())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean pesquisa(ProcessoParte p, List<ProcessoParte> lista) {
		for (ProcessoParte parte : lista) {
			if (parte.getIdProcessoParte() == p.getIdProcessoParte()) {
				return true;
			}
		}
		return false;
	}

	public boolean pesquisa2(ProcessoParte p, List<ProcessoParteRepresentante> lista) {
		for (ProcessoParteRepresentante parte : lista) {
			if (parte.getProcessoParte().getIdProcessoParte() == p.getIdProcessoParte()) {
				return true;
			}
		}
		return false;
	}

	public void inserir(Pessoa pessoa) {
		inserir(pessoa, Boolean.FALSE);
	}

	public void inserir(Pessoa pessoa, Boolean partePrincipal) {
		ProcessoParteParticipacaoEnum participacaoEnum = ProcessoParteParticipacaoEnum
				.valueOf(ProcessoParteParticipacaoEnum.class, codInParticipacao);
		newInstance();
		ProcessoTrf processoTrfInstance = ProcessoTrfHome.instance().getInstance();
		ClasseJudicial classeJudicial = processoTrfInstance.getClasseJudicial();
		if (processoTrfInstance.getListaParteAtivo().size() == 0) {
			if (codInParticipacao.equals("A")) {
				
				tipoParte = tipoParteManager.tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.A);
			}
		}
		if (processoTrfInstance.getListaPartePassivo().size() == 0) {
			if (codInParticipacao.equals("P")) {
				tipoParte = tipoParteManager.tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.P);
			}
		}
		if (tipoParte != null) {
			getInstance().setPessoa(pessoa);
			getInstance().setProcessoTrf(processoTrfInstance);
			getInstance().setTipoParte(tipoParte);
			getInstance().setInParticipacao(participacaoEnum);
			getInstance().setPartePrincipal(partePrincipal);
			ProcessoTrf processoTrf = getProcessoTrfAtual();
			processoTrf.getProcessoParteList().add(getInstance());
			getEntityManager().persist(getInstance());
			EntityUtil.flush(getEntityManager());
			if ((processoTrfInstance.getListaParteAtivo().size() == 1) && (!ParametroUtil.instance().isJusPostulandi())) {
				Pessoa p = (Pessoa) ProcessoHome.instance().getUsuarioLogado();
				if (p.getTipoPessoa().equals(ParametroUtil.instance().getTipoAdvogado())) {
					ProcessoParte pp = new ProcessoParte();
					pp.setInParticipacao(ProcessoParteParticipacaoEnum.A);
					pp.setPessoa(p);
					pp.setProcessoTrf(processoTrfInstance);
					pp.setTipoParte(getTipoParteAdv());
					processoTrf.getProcessoParteList().add(pp);
					getEntityManager().persist(pp);
					
					ProcessoParteAdvogado ppa = new ProcessoParteAdvogado();
					ppa.setProcessoParte(pp);
					ppa.setPessoaAdvogado(((PessoaFisica) p).getPessoaAdvogado());
					getInstance().getProcessoParteAdvogadoList().add(ppa);
					getEntityManager().persist(ppa);
					
					ProcessoParteRepresentante ppr = new ProcessoParteRepresentante();
					ppr.setProcessoParte(getInstance());
					ppr.setRepresentante(pessoa);
					ppr.setTipoRepresentante(getInstance().getTipoParte());
					pp.getProcessoParteRepresentanteList().add(ppr);
					getEntityManager().persist(ppr);
					
					getEntityManager().flush();
				}
			}
			refreshGrid("processoPoloAtivoGrid");
			refreshGrid("processoPoloPassivoGrid");
			refreshGrid("processoAbaPartePoloAtivoGrid");
			refreshGrid("processoAbaPartePoloPassivoGrid");
			refreshGrid("processoAbaParteTerceiroGrid");
			refreshGrid("cadastroPartesGrid");
			refreshGrid("cadastroPartesAdvGrid");
			refreshGrid("cadastroPartesProcuradorGrid");
		} else {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Escolha um Tipo da Parte");
		}
	}

	public void setUrlOpenner(String urlOpenner) {
		this.urlOpenner = urlOpenner;
	}

	public String getUrlOpenner() {
		return urlOpenner;
	}

	public void setCodInParticipacao(String codInParticipacao) {
		this.codInParticipacao = codInParticipacao;
	}

	public String getCodInParticipacao() {
		return codInParticipacao;
	}

	public void setNovaInsercao(String novaInsercao) {
		this.novaInsercao = novaInsercao;
	}

	public String getNovaInsercao() {
		return novaInsercao;
	}

	public void entrarPagina() {
		tipoParte = null;
		newInstance();
		setNovaInsercao("N");
		setTab("search");
		PessoaFisicaHome.instance().newInstance();
		PessoaJuridicaHome.instance().newInstance();
	}

	public static ProcessoParteHome instance() {
		return ComponentUtil.getComponent("processoParteHome");
	}

	public void atualizarSigilo() {
		instance.setParteSigilosa(parteSigilosaTran);
		update();
		refreshGrid("sigiloPoloAtivoGrid");
	}

	public Boolean getParteSigilosaTran() {
		return parteSigilosaTran;
	}

	public void setParteSigilosaTran(Boolean parteSigilosaTran) {
		this.parteSigilosaTran = parteSigilosaTran;
	}

	public String getNomePessoa() {
		return nomePessoa;
	}

	public void setNomePessoa(String nomePessoa) {
		this.nomePessoa = nomePessoa;
	}

	public void popularLista() {
		if (!getListaPopulada()) {
			limparChecks();

			EntityManager em = EntityUtil.getEntityManager();
			StringBuilder sqlRepresentante = new StringBuilder();
			sqlRepresentante.append("select o.representante from ProcessoParteRepresentante o ");
			sqlRepresentante.append("where o.processoParte = :processoParte ");
			Query q = em.createQuery(sqlRepresentante.toString());
			q.setParameter("processoParte", getInstance());
			List<Pessoa> listPessoa = q.getResultList();

			StringBuilder sqlAdvogado = new StringBuilder();
			sqlAdvogado.append("select o.pessoaAdvogado from ProcessoParteAdvogado o ");
			sqlAdvogado.append("where o.processoParte = :processoParte ");
			Query q2 = em.createQuery(sqlAdvogado.toString());
			q2.setParameter("processoParte", getInstance());
			listPessoa.addAll(q2.getResultList());

			List<ProcessoParte> listaPartes = new ArrayList<ProcessoParte>(0);
			for (Pessoa pessoa : listPessoa) {
				StringBuilder lista = new StringBuilder();
				lista.append("select o from ProcessoParte o ");
				lista.append("where o.processoTrf = :processoTrf ");
				lista.append("and o.pessoa = :pessoa ");
				Query qp = getEntityManager().createQuery(lista.toString());
				qp.setParameter("processoTrf", ProcessoTrfHome.instance().getInstance());
				qp.setParameter("pessoa", pessoa);
				listaPartes.add((ProcessoParte) qp.getResultList().get(0));
			}

			GridQuery gridQuery1 = getComponent("processoParteVinculoAdvogadoGrid");
			gridQuery1.getSelectedRowsList().addAll(listaPartes);
			GridQuery gridQuery2 = getComponent("processoParteRepresentanteAutorGrid");
			gridQuery2.getSelectedRowsList().addAll(listaPartes);
			GridQuery gridQuery3 = getComponent("processoParteVinculoRepresentanteGrid");
			gridQuery3.getSelectedRowsList().addAll(listaPartes);
			setListaPopulada(Boolean.TRUE);
		}
	}

	public void limparChecks() {
		GridQuery gridQuery = getComponent("processoParteVinculoAdvogadoGrid");
		gridQuery.getSelectedRowsList().clear();

		gridQuery = getComponent("processoParteRepresentanteAutorGrid");
		gridQuery.getSelectedRowsList().clear();

		gridQuery = getComponent("processoParteVinculoRepresentanteGrid");
		gridQuery.getSelectedRowsList().clear();

		gridQuery = getComponent(PROCESSO_PARTE_VINC_PESSOA_ENDERECO_GRID);
		gridQuery.getSelectedRowsList().clear();

	}
	
	public void associarEnderecoParteProcesso(ProcessoParte parte, boolean autoridade) throws Exception {
		Identity usuarioLogado = Identity.instance();
 		PreCadastroPessoaBean preBean = (PreCadastroPessoaBean) Component.getInstance("preCadastroPessoaBean");
 		GridQuery gridQuery = getComponent(PROCESSO_PARTE_VINC_PESSOA_ENDERECO_GRID);

		List<Object> marcados = gridQuery.getSelectedRowsList();
		if( gridQuery.getSelectedRow() != null ) {
			marcados.clear();
			marcados.add(gridQuery.getSelectedRow());
		}
		List<Object> naoMarcados = gridQuery.getResultList();
		naoMarcados.removeAll(marcados);
		parte.getProcessoParteEnderecoList().clear();
		listaObj = marcados;

		if(!autoridade){
			boolean endDesconhecido = parte.getIsEnderecoDesconhecido();
			boolean permiteEnderecoDesconhecido = permiteEnderecoDesconhecido(preBean.getPessoa());
		
			if (permiteEnderecoDesconhecido && !endDesconhecido && (listaObj == null || listaObj.isEmpty())) {
				String msg = "Selecione ao menos um endereço para utilizar no processo ou use a opção 'Endereço desconhecido'.";
				throw new Exception(msg);
			} else if (permiteEnderecoDesconhecido && !usuarioLogado.hasRole("servidor") && endDesconhecido && (listaObj != null && !listaObj.isEmpty())) {
				String msg = "Existem endereços utilizáveis no processo. Desmarque a opção 'Endereço desconhecido' e escolha um endereco.";
				throw new Exception(msg);

			} else if (!permiteEnderecoDesconhecido && (listaObj == null || listaObj.isEmpty())) {
				String msg = "Selecione ao menos um endereço para utilizar no processo.";
				throw new Exception(msg);
			}
		}

		Query qryDelete = getEntityManager().createQuery(" delete from ProcessoParteEndereco o " + "where o.processoParte.idProcessoParte = ? ");
		qryDelete.setParameter(1, parte.getIdProcessoParte());
		qryDelete.executeUpdate();

		for (Object auxMarcado : marcados) {
			ProcessoParteEndereco aux = new ProcessoParteEndereco();
			aux.setEndereco((Endereco) auxMarcado);
			aux.setProcessoParte(parte);
			parte.getProcessoParteEnderecoList().add(aux);
		}
	}

	public void associarEnderecoParteProcesso(ProcessoParte parte) throws Exception {
		associarEnderecoParteProcesso(parte, false);
	}

	public boolean parteTemEnderecoValido() {
		ProcessoParte parte = getInstance();
		return !((!parte.getIsEnderecoDesconhecido() && (listaObj == null || listaObj.isEmpty())));
	}

	public void associarAdvogado() {
		GridQuery gridQuery = getComponent("processoParteVinculoAdvogadoGrid");
		listaObj = gridQuery.getSelectedRowsList();
		listaNaoMarcados = gridQuery.getResultList();
		listaNaoMarcados.removeAll(listaObj);

		removerProcessoParte();
		persistirPartes();
	}

	public void associarRepresentanteAutor() {
		GridQuery gridQuery = getComponent("processoParteRepresentanteAutorGrid");
		listaObj = gridQuery.getSelectedRowsList();
		listaNaoMarcados = gridQuery.getResultList();
		listaNaoMarcados.removeAll(listaObj);

		removerProcessoParte();
		persistirPartes();
	}

	public void associarRepresentantePessoa() {
		GridQuery gridQuery = getComponent("processoParteVinculoRepresentanteGrid");
		listaObj = gridQuery.getSelectedRowsList();
		listaNaoMarcados = gridQuery.getResultList();
		listaNaoMarcados.removeAll(listaObj);

		removerProcessoParte();
		persistirPartes();
	}

	public void persistirPartes() {
		for (Object obj : listaObj) {
			ProcessoParte parte = (ProcessoParte) obj;
			Pessoa pessoa = parte.getPessoa();

			if ((Pessoa.instanceOf(pessoa, PessoaAdvogado.class)) && (parte.getTipoParte().equals(ParametroUtil.instance().getTipoParteAdvogado()))) {
				ProcessoParteAdvogado ppa = new ProcessoParteAdvogado();
				ppa.setProcessoParte(ProcessoParteHome.instance().getInstance());
				ppa.setPessoaAdvogado(((PessoaFisica) pessoa).getPessoaAdvogado());

				if (verificarAdvogado(ppa).size() == 0) {
					getInstance().getProcessoParteAdvogadoList().add(ppa);
					getEntityManager().persist(ppa);
					EntityUtil.flush(getEntityManager());
				}
			} else {
				ProcessoParteRepresentante ppr = new ProcessoParteRepresentante();
				ppr.setProcessoParte(ProcessoParteHome.instance().getInstance());
				ppr.setRepresentante(pessoa);
				ppr.setTipoRepresentante(parte.getTipoParte());
				if (verificarRepresentante(ppr).size() == 0) {
					getInstance().getProcessoParteRepresentanteList().add(ppr);
					getEntityManager().persist(ppr);
					getEntityManager().flush();
				}
			}
		}
		setListaPopulada(Boolean.FALSE);
		popularLista();
		refreshGrid("processoParteVinculoAdvogadoGrid");
		refreshGrid("processoParteRepresentanteAutorGrid");
		refreshGrid("processoParteVinculoRepresentanteGrid");
		refreshGrid("processoAbaPartePoloAtivoGrid");
		refreshGrid("processoAbaPartePoloPassivoGrid");
	}

	public void removerProcessoParte() {
		for (Object obj : listaNaoMarcados) {
			ProcessoParte pp = (ProcessoParte) obj;
			Pessoa pessoa = pp.getPessoa();

			if (Pessoa.instanceOf(pessoa, PessoaAdvogado.class)) {
				ProcessoParteAdvogado ppa = new ProcessoParteAdvogado();
				ppa.setProcessoParte(ProcessoParteHome.instance().getInstance());
				ppa.setPessoaAdvogado(((PessoaFisica) pessoa).getPessoaAdvogado());

				if (verificarAdvogado(ppa).size() > 0) {
					ProcessoParteAdvogado processoParteAdvogado = verificarAdvogado(ppa).get(0);
					getInstance().getProcessoParteAdvogadoList().remove(processoParteAdvogado);
					getEntityManager().remove(processoParteAdvogado);
					EntityUtil.flush(getEntityManager());
				}
			} else {
				ProcessoParteRepresentante ppr = new ProcessoParteRepresentante();
				ppr.setProcessoParte(ProcessoParteHome.instance().getInstance());
				ppr.setRepresentante(pessoa);
				ppr.setTipoRepresentante(pp.getTipoParte());
				if (verificarRepresentante(ppr).size() > 0) {
					ProcessoParteRepresentante processoParteRepresentante = verificarRepresentante(ppr).get(0);
					getInstance().getProcessoParteRepresentanteList().remove(processoParteRepresentante);
					getEntityManager().remove(processoParteRepresentante);
					EntityUtil.flush(getEntityManager());
				}
			}
		}
	}

	public List<ProcessoParteRepresentante> verificarRepresentante(ProcessoParteRepresentante ppr) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ppr from ProcessoParteRepresentante ppr ");
		sb.append("where ppr.processoParte = :processoParte ");
		sb.append("and ppr.representante = :representante ");
		sb.append("and ppr.tipoRepresentante = :tipoRepresentante");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processoParte", ppr.getProcessoParte());
		q.setParameter("representante", ppr.getRepresentante());
		q.setParameter("tipoRepresentante", ppr.getTipoRepresentante());
		return q.getResultList();
	}

	public List<ProcessoParteAdvogado> verificarAdvogado(ProcessoParteAdvogado ppa) {
		StringBuilder sql = new StringBuilder();
		sql.append("select ppa from ProcessoParteAdvogado ppa ");
		sql.append("where ppa.processoParte = :processoParte ");
		sql.append("and ppa.pessoaAdvogado = :pessoaAdvogado");
		Query q = getEntityManager().createQuery(sql.toString());
		q.setParameter("processoParte", ppa.getProcessoParte());
		q.setParameter("pessoaAdvogado", ppa.getPessoaAdvogado());

		return q.getResultList();
	}

	public List<ProcessoParteEndereco> listaEnderecosProcessoParte(ProcessoParte ppa) {
		StringBuilder sql = new StringBuilder();
		sql.append("select ppe from ProcessoParteEndereco ppe ");
		sql.append("where ppe.processoParte = :processoParte");
		Query q = getEntityManager().createQuery(sql.toString());
		q.setParameter("processoParte", ppa);

		return q.getResultList();
	}

	public Boolean getListaPopulada() {
		return listaPopulada;
	}

	public void setListaPopulada(Boolean listaPopulada) {
		this.listaPopulada = listaPopulada;
	}

	public Boolean verificaPoloAtivo() {
		List<Object> lista;
		GridQuery gq = getComponent("processoPoloAtivoGrid");
		lista = gq.getResultList();
		TipoParte poloAtivo = tipoParteManager.tipoPartePorClasseJudicial(ProcessoTrfHome.instance().getInstance().getClasseJudicial(), ProcessoParteParticipacaoEnum.A);
		if (lista.size() >= 1) {
			for (Object object : lista) {
				ProcessoParte pp = (ProcessoParte) object;
				if (pp.getTipoParte().equals(poloAtivo)) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

	public void limparPPart(ProcessoParte pp, ProcessoParteRepresentante ppr, ProcessoParteRepresentante ppa) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoParte o ");
		sb.append("where o.processoTrf = :processoTrf ");
		sb.append("and o.inParticipacao = :participacao");
		Query qA = getEntityManager().createQuery(sb.toString());
		qA.setParameter("processoTrf", ProcessoTrfHome.instance().getInstance());
		qA.setParameter("participacao", pp.getInParticipacao());
		List<ProcessoParte> listaPP = qA.getResultList();
		for (ProcessoParte processoParte : listaPP) {
			processoParte.getProcessoParteRepresentanteList().remove(ppa);
			processoParte.getProcessoParteRepresentanteList2().remove(ppr);
		}
	}

	public void removerProcessoParticipantesAssociados(ProcessoParte obj) {
		EntityManager em = EntityUtil.getEntityManager();

		StringBuilder builder = new StringBuilder();
		builder.append("select o from ProcessoParteRepresentante o ");
		builder.append("where o.processoParte = :processoParte");
		Query q = em.createQuery(builder.toString());
		q.setParameter("processoParte", obj);
		List<ProcessoParteRepresentante> listaPPA = q.getResultList();
		for (ProcessoParteRepresentante ppa : listaPPA) {
			obj.getProcessoParteRepresentanteList().remove(ppa);
			ProcessoParte pp = ppa.getParteRepresentante();
			pp.getProcessoParteRepresentanteList2().remove(ppa);
			EntityUtil.getEntityManager().remove(ppa);
			EntityUtil.flush();

		}

		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoParteRepresentante o ");
		sb.append("where o.parteRepresentante = :parteRepresentante");
		Query q2 = em.createQuery(sb.toString());
		q2.setParameter("parteRepresentante", obj);
		List<ProcessoParteRepresentante> listaPPR = q2.getResultList();
		for (ProcessoParteRepresentante ppr : listaPPR) {
			obj.getProcessoParteRepresentanteList2().remove(ppr);
			ProcessoParte pp = ppr.getProcessoParte();
			pp.getProcessoParteRepresentanteList().remove(ppr);
			EntityUtil.getEntityManager().remove(ppr);
			EntityUtil.flush();

		}
	}

	public void inativarProcessoParticipantesAssociados() {
		ProcessoParte processoParte = getInstance();
		for (ProcessoParteRepresentante processoParteRepresentante : processoParte.getProcessoParteRepresentanteList()) {
			processoParteRepresentante.setInSituacao(processoParte.getInSituacao());
			removerVisualizacaoSegredoJustica(processoParteRepresentante.getRepresentante(), true);

		}

		for (ProcessoParteRepresentante processoParteRepresentante : processoParte.getProcessoParteRepresentanteList2()) {
			processoParteRepresentante.setInSituacao(processoParte.getInSituacao());
			removerVisualizacaoSegredoJustica(processoParteRepresentante.getRepresentante(), true);
		}
	}

	public void reativarProcessoParticipantesAssociados() {
		ProcessoParte processoParte = getInstance();
		for (ProcessoParteRepresentante processoParteRepresentante : processoParte.getProcessoParteRepresentanteList()) {
			if (processoParteRepresentante.getProcessoParte().getInSituacao() == ProcessoParteSituacaoEnum.A) {
				processoParteRepresentante.setInSituacao(ProcessoParteSituacaoEnum.A);

				habilitarVisualizacaoSegredoJustica(processoParteRepresentante.getRepresentante(), null);				
			}
		}

		for (ProcessoParteRepresentante processoParteRepresentante : processoParte.getProcessoParteRepresentanteList2()) {
			if (processoParteRepresentante.getProcessoParte().getInSituacao() == ProcessoParteSituacaoEnum.A) {
				processoParteRepresentante.setInSituacao(ProcessoParteSituacaoEnum.A);

				habilitarVisualizacaoSegredoJustica(processoParteRepresentante.getRepresentante(), null);				
			}
		}
	}

	public void removerProcessoParteEndereco(ProcessoParte obj) {
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ProcessoParteEndereco o ");
		sb.append("where o.processoParte = :processoParte");
		Query q = em.createQuery(sb.toString());
		q.setParameter("processoParte", obj);
		q.executeUpdate();
	}

	public String removerParte(ProcessoParte obj) {
		if(verificaAplicacaoRegrasRemocaoParte(obj)) {
			try {
				ProcessoTrf processoTrf = getProcessoTrfAtual();
				removerProcessoParticipantesAssociados(obj);
				this.removerInformacaoCriminalRascunho(processoTrf.getIdProcessoTrf(), new Long(obj.getIdProcessoParte()));
				processoTrf.getProcessoParteList().remove(obj);
				EntityUtil.getEntityManager().remove(obj);
				EntityUtil.flush();
				resetarVinculacaoParte(false, POLO_ATIVO);
				PreCadastroPessoaBean.instance().resetarVariaveisPesquisa();
				return "deleted";

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	private Boolean removerInformacaoCriminalRascunho(Integer idProcessoJudicial, Long idProcessoParte){
		InformacaoCriminalRascunhoManager icrManager = ComponentUtil.getComponent(InformacaoCriminalRascunhoManager.NAME);
		return icrManager.excluirInformacaoCriminalRascunho(idProcessoJudicial, idProcessoParte);
	}

	/**
	 * metodo responsavel pela aplicacao das regras de exclusao de processos parte
	 * @param procParte
	 * @return true se todas as regras forem atendidas / false
	 */
	public boolean verificaAplicacaoRegrasRemocaoParte(ProcessoParte procParte) {
		boolean retorno = true;
		if(Authenticator.isAdvogado()) {
			if(pessoaLogadaIsPessoaParte(procParte)){
				if(procParteIsPoloAtivo(procParte)) {
					if(tipoParteIsAdvogado(procParte)) {
						retorno = false; 
					}
				}
			}
		}
		
		return retorno;
	}

	/**
	 * metodo responsavel por verificar se o tipo da parte do processo parte é advogado
	 * @param procParte
	 * @return true / false
	 */
	private boolean tipoParteIsAdvogado(ProcessoParte procParte) {
		if(procParte.getTipoParte().getIdTipoParte() == obtemIdTipoParteAdvogado()) {
			return true;
		}else {
			return false;
		}
	}

	/**
	 * metodo responsavel por verificar se o processo parte a ser excluído é do polo ativo
	 * @param procParte
	 * @return true se processo parte pertence ao polo ativo / false
	 */
	private boolean procParteIsPoloAtivo(ProcessoParte procParte) {
		if(procParte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.A)) {
			return true;
		}else {
			return false;
		}
	}

	/**
	 * metodo verifica se pessoa logada atualmente é a pessoa da parte
	 * @param procParte
	 * @return true se pessoa logada eh pessoa da parte / false
	 */
	private boolean pessoaLogadaIsPessoaParte(ProcessoParte procParte) {
		if (procParte.getPessoa().getIdUsuario().equals(Authenticator.getUsuarioLogado().getIdUsuario())){
			return true;
		}else {
			return false;
		}
	}

	/**
	 * @see ProcessoParteHome#validarInativacao() 
	 * @param processoParte
	 * @return <code>true</code> caso seja permitida a inativação da parte, <code>false</code> caso contrario.
	 */
	@Deprecated
	public boolean validarInativacao(ProcessoParte processoParte) {
		if (processoParte.getPartePrincipal()) {
			if (!processoParte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.T)
					&& ProcessoTrfHome.instance().getInstance().getListaPartePrincipal(processoParte.getInParticipacao()).size() <= 1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Método que valida as condições para inativação de uma parte na retificação do processo de acordo com as regras descritas na issue 
	 * <a href="http://www.cnj.jus.br/jira/browse/PJEII-10984">PJEII-10984.</a></br>
	 * <p><ul>
	 * Regras:
	 * <li>
	 *  A : Sempre deve haver ao menos uma parte no polo ativo. Caso o usuário tente inativar a parte, deve ser apresentada a mensagem:
	 *  "Deve haver ao menos uma parte no polo ativo."
	 * </li>
	 * <li>
	 *  B : Há classes que não exigem uma parte no polo passivo. O sistema deve verificar essa regra na retificação da autuação.
	 *  Caso o usuário tente inativar a parte e a classe exige uma parte no polo passivo, deve ser apresentar a mensagem:
	 *  "A classe judicial exige ao menos uma parte no polo passivo."
	 * </li>
	 * <li>
	 *  C : Há classes que exigem uma autoridade no polo passivo. O sistema deve verificar essa regra na retificação da autuação.
	 *  Caso o usuário tente inativar a autoridade coatora e a classe exige uma autoridade no polo passivo, deve ser apresenta a mensagem:
	 *  "A classe judicial exige ao menos uma autoridade no polo passivo."
	 * </li>
	 * <li>
	 *  C (Revisada Dr. Paulo): caso a classe judicial exija autoridade no polo passivo, o sistema deverá impedir a inativação de autoridade
	 *  caso seja a única existente como principal, ou seja, caso exista mais de uma autoridade no polo passivo como parte principal,o 
	 *  número excedente a um pode ser inativado.
	 * </li></ul></p>
	 * @see <a href="http://www.cnj.jus.br/jira/browse/PJEII-10984">PJEII-10984</a>
	 * @see <a href="http://www.cnj.jus.br/jira/browse/PJEII-12612">PJEII-12612</a>
	 * @return <code>true</code> caso seja permitida a inativação da parte, <code>false</code> caso contrario.
	 */
	public boolean validarInativacao(){
		FacesMessages.instance().clear();

		if(getInstance().getInParticipacao() == ProcessoParteParticipacaoEnum.A){
			return validarInativacaoPartePoloAtivo();
		}
		
		if(getInstance().getInParticipacao() == ProcessoParteParticipacaoEnum.P){
			return validarInativacaoPartePoloPassivo(); 
		}

		return true;
	}

	private boolean validarInativacaoPartePoloAtivo() {
		if(isPartePrincipal() && isUltimaPartePrincipalPoloAtivoRemovida()){
			FacesMessages.instance().add(Severity.ERROR, "Deve haver ao menos uma parte no polo ativo.");
			return false;
		}
		return true;
	}

	private boolean validarInativacaoPartePoloPassivo() {
		if(isPartePrincipal() && isPoloPassivoExigido() && isUltimaPartePrincipalPoloPassivoRemovida()){
			FacesMessages.instance().add(Severity.ERROR, "A classe judicial exige ao menos uma parte no polo passivo.");
			return false;
		}

		if(isPartePessoaAutoridade() && isClasseJudicialExigeAutoridade() && isUltimaAutoridadePoloPassivoRemovida()){
			FacesMessages.instance().add(Severity.ERROR,"A classe judicial exige ao menos uma autoridade no polo passivo.");
			return false;
		}

		return true;
	}

	private Boolean isClasseJudicialExigeAutoridade() {
		return getProcessoTrfAtual().getClasseJudicial().getExigeAutoridade();
	}

	private boolean isPartePrincipal(){
		return getInstance().getTipoParte().getTipoPrincipal();
	}

	private boolean isPoloPassivoExigido(){
		return getProcessoTrfAtual().getClasseJudicial().getReclamaPoloPassivo();
	}

	private boolean isPartePessoaAutoridade() {
		return PessoaAutoridade.class.isAssignableFrom(getInstance().getPessoa().getClass());
	}

	private boolean isUltimaPartePrincipalPoloAtivoRemovida() {
		return getProcessoTrfAtual().getListaPartePrincipalAtivo().isEmpty();
	}

	private boolean isUltimaAutoridadePoloPassivoRemovida() {
		return getProcessoTrfAtual().getAutoridadesPoloPassivo().isEmpty();
	}

	private boolean isUltimaPartePrincipalPoloPassivoRemovida() {
		return getProcessoTrfAtual().getListaPartePrincipalPassivo().isEmpty();
	}

	public String inativarParticipante() {
		String retornoInativacao = null;
		ProcessoParte processoParte = getInstance();

		if (validarInativacao()) {
			UsuarioLogin usuarioLogin = Authenticator.getUsuarioLogado();
			ProcessoParteHistorico processoParteHistorico = new ProcessoParteHistorico();
			processoParteHistorico.setInSituacao(processoParte.getInSituacao());
			processoParteHistorico.setJustificativa(getJustificativaSituacao());
			processoParteHistorico.setDataHistorico(new Date());
			processoParteHistorico.setProcessoParte(processoParte);
			processoParteHistorico.setUsuarioLogin(usuarioLogin);
	
			processoParte.getProcessoParteHistoricoList().add(processoParteHistorico);
			inativarProcessoParticipantesAssociados();
			inativarRepresentantes();
			
			removerVisualizacaoSegredoJustica(processoParte.getPessoa(), true);
			
			getEntityManager().merge(processoParte);
			getEntityManager().flush();
			
			if(this.isRetificacao()) {
				dispararMensagemPosAlteracaoOuRemocaoParte(this.getInstance(), CloudEventVerbEnum.PATCH);
				sinalizarAlteracoesCadastroPartes(processoParte.getProcessoTrf(), processoParte);
			}
			
			refreshGrid("processoParteGrid");
			refreshGrid("pessoaParteProcessoGrid");
			refreshGrid("processoPoloAtivoGrid");
			refreshGrid("processoAbaPartePoloAtivoGrid");
			refreshGrid("processoAbaPartePoloPassivoGrid");
			refreshGrid("processoAbaParteTerceiroGrid");
			refreshGrid("processoPoloPassivoGrid");
			refreshGrid("processoFiscalLeiGrid");
			refreshGrid("processoIncidentePoloAtivoGrid");
			refreshGrid("processoIncidentePoloPassivoGrid");
			limparTrees();
			retornoInativacao = "inativado";
		} else {
			rollbackAlteracaoEntidade(processoParte);
		}

		this.isAtivo = false;
		setJustificativaSituacao(null);

		return retornoInativacao;
	}

	/**
	 * Inativa a parte representante caso todas as suas representações já estejam inativas
	 */
	private void inativarRepresentantes() {
		ProcessoParte pp = getInstance();
		//Não é necessário continuar se a parte que está sendo inativada for um representante
		if(!pp.getPartePrincipal()) {
			return;
		}
		//Busca os representantes da parte
		List<ProcessoParte> representantes = obtemProcessoParte_Representante(pp);
		
		for (ProcessoParte representante : representantes) {
			//flag para verificar se todas as representações estão inativas
			boolean todosInativos = true;
			
			//Busca as partes que são representadas pelo representante
			for (ProcessoParteRepresentante parteRepresentada : representante.getProcessoParteRepresentanteList2()) {
				//Se houver ao menos uma representação ativa, não podemos inativar o representante
				if(parteRepresentada.getInSituacao() == ProcessoParteSituacaoEnum.A) {
					todosInativos = false;
					break;
				}
			}
			//Inativa a parte representante se todas as suas representações estiverem inativas
			if(todosInativos) {
				representante.setInSituacao(ProcessoParteSituacaoEnum.B.equals(pp.getInSituacao()) ? ProcessoParteSituacaoEnum.I : pp.getInSituacao());
				
				removerVisualizacaoSegredoJustica(representante.getPessoa(), true);
			}
		}
	}
	
	public String reativarParticipante() {

		ProcessoParte processoParte = getInstance();
		processoParte.setInSituacao(ProcessoParteSituacaoEnum.A);
		UsuarioLogin usuarioLogin = Authenticator.getUsuarioLogado();
		ProcessoParteHistorico processoParteHistorico = new ProcessoParteHistorico();
		processoParteHistorico.setInSituacao(processoParte.getInSituacao());
		processoParteHistorico.setJustificativa(getJustificativaSituacao());
		processoParteHistorico.setDataHistorico(new Date());
		processoParteHistorico.setProcessoParte(processoParte);
		processoParteHistorico.setUsuarioLogin(usuarioLogin);

		processoParte.getProcessoParteHistoricoList().add(processoParteHistorico);
		
		reativarProcessoParticipantesAssociados();


		habilitarVisualizacaoSegredoJustica(processoParte.getPessoa(), processoParte.getProcuradoria());
		
		
		getEntityManager().merge(processoParte);
		ProcessoPushManager.instance().inserirNoPush(processoParte);
		getEntityManager().flush();
		
		if(this.isRetificacao()) {
			dispararMensagemPosAlteracaoOuRemocaoParte(processoParte, CloudEventVerbEnum.PATCH);
		}
		
		refreshGrid("processoParteGrid");
		refreshGrid("pessoaParteProcessoGrid");
		refreshGrid("processoPoloAtivoGrid");
		refreshGrid("processoAbaPartePoloAtivoGrid");
		refreshGrid("processoAbaPartePoloPassivoGrid");
		refreshGrid("processoAbaParteTerceiroGrid");
		refreshGrid("processoPoloPassivoGrid");
		refreshGrid("processoFiscalLeiGrid");
		refreshGrid("processoIncidentePoloAtivoGrid");
		refreshGrid("processoIncidentePoloPassivoGrid");
		limparTrees();
		setJustificativaSituacao(null);
		this.isAtivo = true;
		return "reativado";
	}
	
	public void prepararBeans(ProcessoParte parte) {
		prepararBeans(parte, true);
	}
	
	public void prepararBeans(ProcessoParte parte, boolean resetarVinculacaoRepresentante) {
		PessoaDocumentoIdentificacaoHome pessoaDocumentoIdentificacaoHome = (PessoaDocumentoIdentificacaoHome) Component.getInstance("pessoaDocumentoIdentificacaoHome");
		pessoaDocumentoIdentificacaoHome.newInstance();
		this.carregarBeans(parte, resetarVinculacaoRepresentante);
	}

	public void carregarBeans(ProcessoParte parte) {
		carregarBeans(parte, true);
	}
	
	public void carregarBeans(ProcessoParte parte, boolean resetarVinculacaoRepresentante) {

		resetarVinculacaoParte(true, parte.getInParticipacao().toString());

		PreCadastroPessoaBean preB = (PreCadastroPessoaBean) Component.getInstance("preCadastroPessoaBean");
		PessoaHome pHome = PessoaHome.instance();
		if(parte.getPessoa()!=null){
			pHome.setInstance(parte.getPessoa());
		}
		PessoaFisicaHome pfHome = PessoaFisicaHome.instance();
		PessoaJuridicaHome pjHome = PessoaJuridicaHome.instance();
		PessoaAutoridadeHome paaHome = PessoaAutoridadeHome.instance();

		if (parte.getPartePrincipal()) {
			TipoPessoaEnum tipoPessoa = null;
			
			if(parte.getPessoa() instanceof PessoaAutoridade) {
				tipoPessoa = TipoPessoaEnum.A;
			} else if(parte.getPessoa() instanceof PessoaFisica) {
				tipoPessoa = TipoPessoaEnum.F;
			} else if(parte.getPessoa() instanceof PessoaJuridica){
				tipoPessoa = TipoPessoaEnum.J;
			}

			if(tipoPessoa != null) {
				if (tipoPessoa == TipoPessoaEnum.A) {
					preB.setInTipoPessoa(TipoPessoaEnum.A);
					pHome.getInstance().setInTipoPessoa(TipoPessoaEnum.A);
					preB.setPessoaAutoridade((PessoaAutoridade) parte.getPessoa());
					paaHome.setInstance((PessoaAutoridade) parte.getPessoa());
	
				} else if (tipoPessoa == TipoPessoaEnum.F) {
					preB.setPessoaFisica((PessoaFisica) parte.getPessoa());
					if(preB.getPessoaFisica().getNomeSocial() != null) {
						preB.setInformarNomeSocial(true);
					}
					pfHome.setInstance((PessoaFisica) parte.getPessoa());
					preB.initCadastroPessoaFisica();
					preB.setInTipoPessoa(TipoPessoaEnum.F);
					pHome.getInstance().setInTipoPessoa(TipoPessoaEnum.F);
	
				} else if (tipoPessoa == TipoPessoaEnum.J) {
					preB.setInTipoPessoa(TipoPessoaEnum.J);
					preB.setPessoaJuridica((PessoaJuridica) parte.getPessoa());
					pjHome.setInstance((PessoaJuridica) parte.getPessoa());
					pHome.getInstance().setInTipoPessoa(TipoPessoaEnum.J);
				}
			} else {
				PessoaAutoridade pessAuto = pessoaAutoridadeManager.encontraPessoaAutoridadePorPessoa(parte.getPessoa());
				PessoaFisica pessFisi = pessoaFisicaManager.encontraPessoaFisicaPorPessoa(parte.getPessoa());
				PessoaJuridica pessJuri = pessoaJuridicaManager.encontraPessoaJuridicaPorPessoa(parte.getPessoa());
				
				if(pessFisi != null) {
					preB.setPessoaFisica(pessFisi);
					if(preB.getPessoaFisica().getNomeSocial() != null) {
						preB.setInformarNomeSocial(true);
					}
					pfHome.setInstance(pessFisi);
					preB.initCadastroPessoaFisica();
					preB.setInTipoPessoa(TipoPessoaEnum.F);
					pHome.getInstance().setInTipoPessoa(TipoPessoaEnum.F);
				} else  if (pessAuto != null) {
					preB.setPessoaAutoridade(pessAuto);
					paaHome.setInstance(pessAuto);
					preB.setInTipoPessoa(TipoPessoaEnum.A);
					pHome.getInstance().setInTipoPessoa(TipoPessoaEnum.A);
				} else if(pessJuri != null) {
					preB.setPessoaJuridica(pessJuri);
					pjHome.setInstance(pessJuri);
					preB.setInTipoPessoa(TipoPessoaEnum.J);
					pHome.getInstance().setInTipoPessoa(TipoPessoaEnum.J);
				}
			}

			preB.setIsConfirmado(true);
			setFlgVinculandoParte(true);

		} else {

			ProcessoParteRepresentanteHome ppr = ProcessoParteRepresentanteHome.instance();
			ppr.resetarVinculacaoRepresentante(true, parte.getInParticipacao().toString(), resetarVinculacaoRepresentante);
			preB = (PreCadastroPessoaBean) Component.getInstance("preCadastroPessoaBean");

			// Parte 2 do cadastro de representantes - pre cadastro
			if (Pessoa.instanceOf(parte.getPessoa(), PessoaAdvogado.class) && parte.getTipoParte().equals(ParametroUtil.instance().getTipoParteAdvogado())) {
				preB.setPessoaAdvogado(((PessoaFisica) parte.getPessoa()).getPessoaAdvogado());
				// Seta a pessoaFisica também, afinal, advogado extende de pessoaFisica
				preB.setPessoaFisica((PessoaFisica) parte.getPessoa());
				if(preB.getPessoaFisica().getNomeSocial() != null) {
					preB.setInformarNomeSocial(true);
				}
				pfHome.setInstance((PessoaFisica) parte.getPessoa());
				preB.confirmarPessoa();
			} else if (parte.getPessoa() instanceof PessoaFisica) {
				preB.setPessoaFisica((PessoaFisica) parte.getPessoa());
				if(preB.getPessoaFisica().getNomeSocial() != null) {
					preB.setInformarNomeSocial(true);
				}
				pfHome.setInstance((PessoaFisica) parte.getPessoa());
				preB.initCadastroPessoaFisica();
				preB.confirmarPessoa();

			} else if (parte.getPessoa() instanceof PessoaJuridica) {
				preB.setPessoaJuridica((PessoaJuridica) parte.getPessoa());

			} else if (parte.getPessoa() instanceof PessoaAutoridade) {
				preB.setPessoaAutoridade((PessoaAutoridade) parte.getPessoa());
			}

			this.setCodInParticipacao(parte.getInParticipacao().toString());
			List<ProcessoParte> partesRepresentadas = new ArrayList<ProcessoParte>(0);
			ProcessoParte p = EntityUtil.find(ProcessoParte.class, parte.getIdProcessoParte());
			if (p.getProcessoParteRepresentanteList2().size() > 0) {
				for (ProcessoParteRepresentante parteRep : p.getProcessoParteRepresentanteList2()) {
					if (parteRep.isAtivo()) {
						partesRepresentadas.add(parteRep.getProcessoParte());
					}
				}
			}

			preB.setIsConfirmado(true);
			ppr.setPartes(partesRepresentadas);
			ppr.setFlgInclusao(false);
			setFlgVinculandoParte(true);
		}

		setInstance(parte);
		setProcuradoriaAnterior(parte.getProcuradoria());
		this.remarcarEnderecosSelecionados();
		pHome.setInstance(parte.getPessoa());
		FacesMessages.instance().clear();
	}
	

	private TipoParte getTipoParteAdv() {
		String query = "select tpcj.tipoParte from ProcessoTrf o " + "inner join o.classeJudicial cj "
				+ "inner join cj.tipoParteClasseJudicialList tpcj " + "where o = :processoTrf and " + "tpcj.tipoParte = :tipoParte and "
				+ "tpcj.classificacaoParte = 'T'";

		Query q = getEntityManager().createQuery(query);
		q.setParameter("processoTrf", ProcessoTrfHome.instance().getInstance());
		q.setParameter("tipoParte", ParametroUtil.instance().getTipoParteAdvogado());
		List<TipoParte> list = q.getResultList();
		if (list.size() != 0) {
			return list.get(0);
		} else
			return null;
	}

	public List<Object> getListaObj() {
		return listaObj;
	}

	public Boolean getFecharAtualizarTela() {
		return FacesMessages.instance().getCurrentMessages().isEmpty();
	}

	public void remarcarEnderecosSelecionados() {
		GridQuery grid = getComponent(PROCESSO_PARTE_VINC_PESSOA_ENDERECO_GRID);
		Query query = getEntityManager().createQuery(
				" select ppe.endereco " + " from ProcessoParteEndereco ppe " + " where ppe.processoParte.idProcessoParte = ?");
		query.setParameter(1, getInstance().getIdProcessoParte());
		List<Endereco> enderecos = query.getResultList();
		
		if( enderecos.size() >= 1 ){
			grid.setSelectedRow(enderecos.get(0));
		}
	}

	public void setFlgVinculandoParte(boolean flgVinculandoParte) {
		this.flgVinculandoParte = flgVinculandoParte;
	}

	public Boolean getFlgVinculandoParte() {
		return flgVinculandoParte;
	}
	
	public Procuradoria getProcuradoriaAnterior(){
		return this.procuradoriaAnterior;
	}

	public void setProcuradoriaAnterior(Procuradoria procuradoriaAnterior){
		this.procuradoriaAnterior = procuradoriaAnterior;
	}

	/**
	 * Recupera o primeiro polo passivo selecionado pelo usuario para cadastrar
	 * expediente.
	 * 
	 * @return Polo passivo do processo selecionado pelo usuario.
	 */
	public ProcessoParte getPrimeiroPoloPassivoSelecionado() {

		if (searchTree2GridPoloPassivoList.getList().isEmpty()) {
			return null;
		}

		for (EntityNode<ProcessoParte> valor : searchTree2GridPoloPassivoList.getList()) {
			if (valor.getSelected()) {
				return valor.getEntity();
			}
		}

		return null;
	}

	/**
	 * @return Nome do reu selecionado pelo usuario.
	 */
	@Deprecated
	public String getNomeReuAtual() {
		return ((PreparaAtoComunicacaoAction) ComponentUtil.getComponent("preparaAtoComunicacaoAction")).getNomeReuAtual();
	}

	/**
	 * @return Endereco do reu selecionado pelo usuario.
	 */
	public String getEnderecoReuAtual() {
		ProcessoParte processoParte = getPrimeiroPoloPassivoSelecionado();

		if (processoParte != null && !processoParte.getProcessoParteEnderecoList().isEmpty()) {

			ProcessoParteEndereco enderecoMaisAtual = null; // sera considerado o endereco mais atual o modificado por ultimo
			if (processoParte != null && !processoParte.getProcessoParteEnderecoList().isEmpty()) {

				for (ProcessoParteEndereco processoParteEndereco : processoParte.getProcessoParteEnderecoList()) {
					if (processoParteEndereco.getEndereco() != null && processoParteEndereco.getEndereco().getCorrespondencia() != null
							&& processoParteEndereco.getEndereco().getCorrespondencia()) {

						if (enderecoMaisAtual == null) {
							enderecoMaisAtual = processoParteEndereco;
						} else if (processoParteEndereco.getEndereco().getDataAlteracao()
								.compareTo(enderecoMaisAtual.getEndereco().getDataAlteracao()) > 0) {
							enderecoMaisAtual = processoParteEndereco;
						}
					}
				}
			}

			return enderecoMaisAtual != null && enderecoMaisAtual.getEndereco() != null ? enderecoMaisAtual.getEndereco().getEnderecoCompleto()
					: null;
		}

		return null;
	}

	/**
	 * Verifica se existe pelo menos um Advogado entre as partes que representam
	 * um Polo Ativo
	 * 
	 * @return true se existir um advogado como Polo Ativo
	 */
	public Boolean verificaAdvogadoPoloAtivo() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o.idProcessoParte) from ProcessoParte o ");
		sb.append("where o.inParticipacao = 'A' ");
		sb.append("and o.tipoParte.idTipoParte = 7 ");
		sb.append("and o.processoTrf.idProcessoTrf = :processoTrf");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processoTrf", ProcessoTrfHome.instance().getProcessoTrfIdProcessoTrf());

		try {
			Long count = (Long) q.getSingleResult();
			return count.compareTo(0L) > 0;
		} catch(NoResultException ex) {
			return false;
		}
	}

	public String situacaoPessoaFisica(ProcessoParte pp) {
		String cpjCnpj = pp.getPessoa().getDocumentoCpfCnpj();
		String situacaoPessoa = "Não validado";
		if (cpjCnpj != null && !cpjCnpj.isEmpty()) {
			cpjCnpj = cpjCnpj.replaceAll("\\.", "").replaceAll("-", "").replaceAll("/", "");
			if (pp.getPessoa() instanceof PessoaFisica) {
				String sql = "select drpf.situacaoCadastral from DadosReceitaPessoaFisica drpf where drpf.numCPF = :numeroCPF";
				Query q = getEntityManager().createQuery(sql);
				q.setParameter("numeroCPF", cpjCnpj);
				List<String> dadosReceitaList = q.getResultList();
				if (dadosReceitaList.size() > 0) {
					situacaoPessoa = br.jus.pje.ws.externo.srfb.util.SituacaoCadastroPessoaFisicaReceita
							.getDescricaoSituacao(dadosReceitaList.get(0));
				}
			} else if (pp.getPessoa() instanceof PessoaJuridica) {
				String sql = "select drpj.statusCadastralPessoaJuridica from DadosReceitaPessoaJuridica drpj where drpj.numCNPJ = :numeroCNPJ";
				Query q = getEntityManager().createQuery(sql);
				q.setParameter("numeroCNPJ", cpjCnpj);
				List<String> dadosReceitaList = q.getResultList();
				if (dadosReceitaList.size() > 0) {
					situacaoPessoa = br.jus.pje.ws.externo.srfb.util.SituacaoCadastroPessoaJuridicaReceita
					.getDescricaoSituacao(dadosReceitaList.get(0));
				}
			}
		}
		return situacaoPessoa;
	}
	
	/**
	 * Verifica se a opo 'Endereo desconhecido' deve ser exibida caso obedea os seguintes critrios:
	 * <ol>
	 * <li>- Para usurios internos - Quando o polo ativo no tiver endereo cadastrado</li>
	 * <li>- Quando o polo passivo ou o polo outros interessados no tiver endereo listado</li>
	 * </ol>
	 * @param pessoa
	 * @return
	 */
	public boolean permiteEnderecoDesconhecido(Pessoa pessoa) {
		boolean retorno = false;
		try {
			Identity usuarioLogado = Identity.instance();
			GridQuery gridParteEndereco = ((GridQuery) getComponent(PROCESSO_PARTE_VINC_PESSOA_ENDERECO_GRID));
			List<Endereco> enderecos = gridParteEndereco.getResultList();
			
			if(enderecos == null || enderecos.isEmpty()) {
				if(usuarioLogado.hasRole("servidor") || !isPoloAtivo()){
					retorno = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        return retorno;
    }
	
	/**
	 * Verifica se o polo e ativo
	 * @return
	 */
	private boolean isPoloAtivo(){
		boolean retorno = false;
		if(getInstance() != null && getInstance().getInParticipacao() != null && !getInstance().getInParticipacao().name().isEmpty()) {
			if(getInstance().getInParticipacao().name().equals(POLO_ATIVO)){
				retorno = true;
			}
		} else {
			if(inParticipacao.name().equals(POLO_ATIVO)){
				retorno = true;
			}
		}
		return retorno;
	}

	public void inserirParte(PessoaJuridica obj) {
		this.codInParticipacao = getInstance().getInParticipacao().toString();
		PreCadastroPessoaBean preBean = (PreCadastroPessoaBean) Component.getInstance("preCadastroPessoaBean");
		preBean.setIsConfirmado(Boolean.TRUE);
		preBean.setInTipoPessoa(TipoPessoaEnum.J);
		preBean.setPessoaJuridica(obj);
		preBean.setHasPessoaJuridica(Boolean.TRUE);

		Pessoa pessoa = null;
		ProcessoTrf processoTrfInstance = ProcessoTrfHome.instance().getInstance();
		ClasseJudicial classeJudicial = processoTrfInstance.getClasseJudicial(); 
		Procuradoria proc = null;
		
		if (preBean.getIsConfirmado()) {
			if (this.getCodInParticipacao().equals("A") && tipoParte == null) {
				this.setTipoParte(tipoParteManager.tipoPartePorClasseJudicial(classeJudicial,ProcessoParteParticipacaoEnum.A));
			} else if (this.getCodInParticipacao().equals("P") && tipoParte == null) {
				this.setTipoParte(tipoParteManager.tipoPartePorClasseJudicial(classeJudicial,ProcessoParteParticipacaoEnum.P));
			} else if ((this.getCodInParticipacao().equals("T"))) {
				this.setTipoParte(getInstance().getTipoParte());
			}

			String ret = this.update();
			if (ret == null) {
				return;
			}

			if (preBean.getInTipoPessoa() == TipoPessoaEnum.F) {
				pessoa = preBean.getPessoaFisica();
			} else {
				pessoa = preBean.getPessoaJuridica();
			}
			
			if(getInstance().getProcuradoria() != null){
				proc = getInstance().getProcuradoria();
			}
			
			preBean.resetarBean();
			preBean.setHasPessoaJuridica(Boolean.TRUE);

		} else {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Confirme a pessoa antes de adicioná-la como parte");
			return;
		}

		ProcessoParteParticipacaoEnum participacaoEnum = ProcessoParteParticipacaoEnum
				.valueOf(ProcessoParteParticipacaoEnum.class, codInParticipacao);
		newInstance();
		
		List<ProcessoParte> listaParteProcesso = processoTrfInstance.getListaPartePoloObj(true, participacaoEnum);
		try {
			validarParteExistente(pessoa, listaParteProcesso);
		} catch (PJeException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getCode());
			return;
		}

		if (tipoParte != null) {
			getInstance().setPessoa(pessoa);
			getInstance().setProcessoTrf(processoTrfInstance);
			getInstance().setTipoParte(tipoParte);
			getInstance().setInParticipacao(participacaoEnum);
			inParticipacao = getInstance().getInParticipacao();
			
			if(proc != null){
				getInstance().setProcuradoria(proc);
			}
			
			ProcuradoriaManager procuradoriaManager = (ProcuradoriaManager)Component.getInstance("procuradoriaManager");
			List<Procuradoria> procuradorias = procuradoriaManager.getlistProcuradorias(pessoa);
			if(procuradorias != null && procuradorias.size() == 1){
				getInstance().setProcuradoria(procuradorias.get(0));
			}

			if (getInstance().getInParticipacao().equals(ProcessoParteParticipacaoEnum.A)) {
				getInstance().setPartePrincipal(
						tipoParteManager.tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.A).equals(getInstance().getTipoParte()));
				
			} else {
				if (getInstance().getInParticipacao().equals(ProcessoParteParticipacaoEnum.P)) {
					getInstance().setPartePrincipal(
							tipoParteManager.tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.P).equals(getInstance().getTipoParte()));
				}
			}

			Pessoa logada = (Pessoa) Contexts.getSessionContext().get("pessoaLogada");
			if (Pessoa.instanceOf(logada, PessoaAdvogado.class)) {
				if (processoTrfInstance.getListaParteAtivo().size() == 1) {
					if (codInParticipacao.equals("A")) {
						String s = "select ppa " + "from ProcessoParte ppa " + "where ppa.processoTrf = :processoTrf " + "and ppa.pessoa = :pessoa";
						Query q = EntityUtil.getEntityManager().createQuery(s);
						q.setParameter("processoTrf", processoTrfInstance);
						q.setParameter("pessoa", logada);
						List<ProcessoParte> partesEncontradas = q.getResultList();
						ProcessoParte parteEncontrada = null;
						if (partesEncontradas.size() > 0) {
							parteEncontrada = partesEncontradas.get(0);
						}
						if (parteEncontrada != null && parteEncontrada.getPessoa().getIdUsuario().intValue() == logada.getIdUsuario().intValue()) {
							ProcessoParteRepresentante parteRep = new ProcessoParteRepresentante();
							parteRep.setProcessoParte(getInstance());
							parteRep.setRepresentante(logada);
							parteRep.setTipoRepresentante(parteEncontrada.getTipoParte());
							parteRep.setParteRepresentante(parteEncontrada);
							getInstance().getProcessoParteRepresentanteList().add(parteRep);
						}
					}
				}
			}

			getInstance().setInParticipacao(participacaoEnum);
			getInstance().setPartePrincipal(true);
			ProcessoTrf processoTrf = getProcessoTrfAtual();
			processoTrf.getProcessoParteList().add(getInstance());
			persist();
			EntityUtil.flush(EntityUtil.getEntityManager());

			refreshGrid("processoPoloAtivoGrid");
			refreshGrid("processoPoloPassivoGrid");
			refreshGrid("processoAbaPartePoloAtivoGrid");
			refreshGrid("processoAbaPartePoloPassivoGrid");
			refreshGrid("processoAbaParteTerceiroGrid");
			removerContextProcessoParteVinculoPessoaEnderecoGrid();
			Contexts.removeFromAllContexts("processoParteVinculoPessoaMeioContatoGrid");
			FacesMessages.instance().clear();
			preBean.resetarVariaveisPesquisa();
		} else {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Escolha um Tipo da Parte");
		}
		FacesMessages.instance().clear();
		FacesMessages.instance().add(StatusMessage.Severity.INFO, "Alteração gravada com sucesso");
		setTipoParte(null);
		resetarVinculacaoParte(true, codInParticipacao);
	}

	public boolean isParteNoProcesso(Pessoa p, ProcessoTrf processo) {
		ProcessoTrf processoTrf = getProcessoTrfAtual();
		
		for (ProcessoParte processoParte : processoTrf.getProcessoParteList()) {
			if (processoParte.getPessoa().equals(p)) {
				return true;
			}
		}
		
		return false;		
	}
	
	public String situacaoAdvogado(ProcessoParte processoParte) {
		return ComponentUtil.getComponent(PessoaAdvogadoManager.class).situacaoAdvogado(processoParte);
	}
	
	public String situacaoAdvogado(EntityNode<ProcessoParte> processoParteEntityNode) {
		return this.situacaoAdvogado(processoParteEntityNode.getEntity());
	}

	public void setarInstanciaPessoa(ProcessoParte parte) {
		int id = parte.getPessoa().getIdUsuario();

		PessoaFisica pf = EntityUtil.find(PessoaFisica.class, id);
		PessoaJuridica pj = EntityUtil.find(PessoaJuridica.class, id);

		if (pf != null) {
			PessoaFisicaHome.instance().setId(id);
			return;
		}

		if (pj != null) {
			PessoaJuridicaHome.instance().setId(id);
			return;
		}
	}

	private Boolean isTipoParteAmbos(ClasseJudicial classe, TipoParte tipo) {
		return processoParteManager.isTipoParteAmbos(classe, tipo);
	}

	/**
	 * @see inverterPolo(ProcessoTrf processoTrf)
	 */	
	public void inverterPolo() {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		getEntityManager().refresh(processoTrf);
		getInstance().setProcessoTrf(processoTrf);
		processoParteManager.inverterPolo(processoTrf);
		processoTrf.getProcessoPartePoloPassivoSemAdvogadoList().stream()
			.filter(ProcessoParte::getIsAtivo)
			.forEach(parte -> this.avisarAlteracaoParte(parte, CloudEventVerbEnum.POST));
		limparTrees();
	}
	
	private void avisarAlteracaoParte(ProcessoParte parte, CloudEventVerbEnum evento) {
		if(this.isRetificacao() && isProcessoPartePoloPassivoCriminalPrincipal(parte)){
			dispararMensagemPosAlteracaoOuRemocaoParte(parte, evento);
			sinalizarAlteracoesCadastroPartes(parte.getProcessoTrf(), parte);
		}
	}
	
	private boolean isProcessoPartePoloPassivoCriminalPrincipal(ProcessoParte processoParte) {
		return (ProcessoParteParticipacaoEnum.P.equals(processoParte.getInParticipacao()) && processoParte.getPartePrincipal() && ProcessoTrfHome.instance().getEhClasseCriminal());
	}

	/**
	 * Executa a duplicação das partes listadas no processo. Com isso, todas as partes de um polo serão cadastradas também no polo inverso.
	 */
	public void duplicar() {
		Map<ProcessoParteParticipacaoEnum,List<ProcessoParte>> listasPartes = new HashMap<ProcessoParteParticipacaoEnum,List<ProcessoParte>>(2);
		listasPartes.put(ProcessoParteParticipacaoEnum.P, processoParteManager.recuperar(false, ProcessoTrfHome.instance().getInstance(), null, ProcessoParteParticipacaoEnum.A));
		listasPartes.put(ProcessoParteParticipacaoEnum.A, processoParteManager.recuperar(false, ProcessoTrfHome.instance().getInstance(), null, ProcessoParteParticipacaoEnum.P));
		
		for (Map.Entry<ProcessoParteParticipacaoEnum,List<ProcessoParte>> partes : listasPartes.entrySet()) {
			List<ProcessoParte> duplicatasProcessoParte = new ArrayList<ProcessoParte>(partes.getValue().size());
			Map <ProcessoParte, List<Pessoa>> representantes = new HashMap<ProcessoParte, List<Pessoa>>(partes.getValue().size());
			for (ProcessoParte pp : partes.getValue()) {
				if (parteNaoAmbosPolos(pp, listasPartes.get(pp.getInParticipacao()))) {
					ProcessoParte ppa = cadastrarParte(pp, partes.getKey());
					duplicatasProcessoParte.add(ppa);
					if (!ppa.getTipoParte().getTipoPrincipal()) {
						preparaRepresentantes(pp, ppa, representantes);
					}
				} else if(!pp.getTipoParte().getTipoPrincipal()) {
					preparaRepresentantes(pp, processoParteManager.findProcessoParte(ProcessoTrfHome.instance().getInstance(), pp.getTipoParte(), pp.getPessoa(), partes.getKey()), representantes);
				}
			}
			for (Map.Entry<ProcessoParte, List<Pessoa>> representante : representantes.entrySet()) {
				for (Pessoa pessoaRepresentada : representante.getValue()) {
					for (ProcessoParte parteDuplicada : duplicatasProcessoParte) {
						if (comparaParteOriginal(parteDuplicada,pessoaRepresentada,representante.getKey())) {
							cadastraNovaRepresentacao(parteDuplicada, representante.getKey(), representante.getKey().getInSituacao());
						}
					}
				}
			}
		}
		limparTrees();
	}

	private void preparaRepresentantes(ProcessoParte pp, ProcessoParte ppa, Map<ProcessoParte, List<Pessoa>> representantes) {
		List <Pessoa> representadosOriginais = getProcessoParteRepresentanteManager().consultarRepresentados(pp.getPessoa(), ProcessoTrfHome.instance().getInstance());
		if (ProjetoUtil.isNotVazio(representadosOriginais)) {
			representantes.put(ppa, representadosOriginais);
		}
	}

	private boolean comparaParteOriginal(ProcessoParte parteDuplicada, Pessoa pessoaRepresentada,
			ProcessoParte representanteDuplicado) {
		return parteDuplicada.getPessoa().equals(pessoaRepresentada) && !parteDuplicada.getTipoParte().equals(representanteDuplicado.getTipoParte());
	}

	private void cadastraNovaRepresentacao(ProcessoParte parteDuplicada, ProcessoParte representanteDuplicado,
			ProcessoParteSituacaoEnum processoParteSituacaoEnum) {
		ProcessoParteRepresentante pprDuplicado = new ProcessoParteRepresentante();
		pprDuplicado.setProcessoParte(parteDuplicada);
		pprDuplicado.setRepresentante(representanteDuplicado.getPessoa());
		pprDuplicado.setInSituacao(processoParteSituacaoEnum);
		pprDuplicado.setParteRepresentante(representanteDuplicado);
		pprDuplicado.setTipoRepresentante(representanteDuplicado.getTipoParte());
		getEntityManager().merge(pprDuplicado);
		EntityUtil.flush();
	}

	private ProcessoParte cadastrarParte(ProcessoParte pp, ProcessoParteParticipacaoEnum polo) {
		TipoParte tipoParte = new TipoParte();
		if (isTipoParteAmbos(ProcessoTrfHome.instance().getInstance().getClasseJudicial(), pp.getTipoParte())) {
			tipoParte = pp.getTipoParte();
		} else {
			tipoParte = tipoParteManager.tipoPartePorClasseJudicial(ProcessoTrfHome.instance().getInstance().getClasseJudicial(), polo);
		}
		Procuradoria procuradoria = null;
		if(pp.getPartePrincipal() && pp.getProcuradoria() != null){
			procuradoria = pp.getProcuradoria();
		}
		
		return cadastrarParte(pp.getProcessoTrf(), pp.getPessoa(), pp.getParteSigilosa(), tipoParte, polo, procuradoria, pp.getPartePrincipal());
	}
	
	private boolean parteNaoAmbosPolos(ProcessoParte pp, List<ProcessoParte> list) {
		for (ProcessoParte parte : list){
			if (parte.getPessoa().equals(pp.getPessoa()) && (parte.getPartePrincipal().equals(pp.getPartePrincipal()) || parte.getTipoParte().equals(pp.getTipoParte()))) {
				return false;
			}
		}
		return true;
	}
	
	private ProcessoParte cadastrarParte(ProcessoTrf processoTrf, Pessoa pessoa, Boolean parteSigilosa,	TipoParte tipoParte, ProcessoParteParticipacaoEnum polo, Procuradoria procuradoria, Boolean partePrincipal) {
		ProcessoParte pp = new ProcessoParte();
		pp.setProcessoTrf(processoTrf);
		pp.setParteSigilosa(parteSigilosa);
		pp.setPessoa(pessoa);
		pp.setTipoParte(tipoParte);
		pp.setInParticipacao(polo);
		pp.setProcuradoria(procuradoria);
		pp.setPartePrincipal(partePrincipal);
		getEntityManager().persist(pp);
		EntityUtil.flush();
		ProcessoTrfHome.instance().getInstance().getProcessoParteList().add(pp);
		return pp;
	}
	
	/**
	 * Recupera a lista de partes principais do processo judicial que figuram no polo ativo.
	 *  
	 * @return a lista de partes
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getProcessoPartePoloAtivoSemVinculacaoList() {
		return getProcessoParteSemVinculacaoList(POLO_ATIVO);
	}

	/**
	 * Recupera a lista de partes principais do processo judicial que figuram em seu polo passivo.
	 *  
	 * @return a lista de partes
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getProcessoPartePoloPassivoSemVinculacaoList() {		
		return getProcessoParteSemVinculacaoList(POLO_PASSIVO);
	}
	
	/**
	 * Recupera a lista de terceiros do processo judicial que figuram como terceiros interessados.
	 *  
	 * @return a lista de partes
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getProcessoParteTerceiroSemVinculacaoList() {		
		return getProcessoParteSemVinculacaoList(POLO_OUTROS_PARTICIPANTES);
	}
	
	/**
	 * Recupera a lista de partes principais do processo judicial (sem representantes) que figuram no polo indicado no parâmetro.
	 * 
	 * @param polo Indica se o polo desejado é ativo (A), passivo (P) ou terceiros (T). 
	 * @return a Lista de partes.
	 * @see Transient
	 */
	@Transient
	private List<ProcessoParte> getProcessoParteSemVinculacaoList(String polo) {
		List<ProcessoParte> listaDePartes = null;
		if( polo.equalsIgnoreCase(POLO_ATIVO) ) {
			listaDePartes =  ProcessoTrfHome.instance().getInstance().getListaPartePrincipalAtivo();
		} else if( polo.equalsIgnoreCase(POLO_PASSIVO) ) {
			listaDePartes =  ProcessoTrfHome.instance().getInstance().getListaPartePrincipalPassivo();
		} else {
			listaDePartes =  ProcessoTrfHome.instance().getInstance().getListaPartePrincipalTerceiro();
		}
		return listaDePartes;
	}	
	
	 public Boolean nodeOpened(UITree tree) {  
		 return Boolean.TRUE;  
	 }

	/**
	 * Verifica se existe um advogado na lista de partes do polo Ativo
	 * 
	 * @return
	 */
	public boolean verificarExistenciaAdvogado() {
		for (ProcessoParte pp : ProcessoTrfHome.instance().getListaAtivos()) {
			if (pp.getTipoParte().equals(ParametroUtil.instance().getTipoParteAdvogado())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retorna o autor do processo
	 * 
	 * @param processo
	 * @return autor do processo
	 */
	public String getAutor(ProcessoTrf processo) {
		StringBuilder sb = new StringBuilder();
		sb.append("Select o from ConsultaProcessoTrf o ");
		sb.append("where o.idProcessoTrf = :processoTrf ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processoTrf", processo.getIdProcessoTrf());
		ConsultaProcessoTrf consultaProcessoTrf = (ConsultaProcessoTrf) q.getResultList().get(0);
		return consultaProcessoTrf.getAutor();

	}

	/**
	 * Retorna o réu do processo
	 * 
	 * @param processo
	 * @return réu do processo
	 */
	public String getReu(ProcessoTrf processo) {
		StringBuilder sb = new StringBuilder();
		sb.append("Select o from ConsultaProcessoTrf o ");
		sb.append("where o.idProcessoTrf = :processoTrf ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processoTrf", processo.getIdProcessoTrf());
		ConsultaProcessoTrf consultaProcessoTrf = (ConsultaProcessoTrf) q.getResultList().get(0);
		return consultaProcessoTrf.getReu();
	}

	/**
	 * Retorna o Polo.
	 * 
	 * @return Polo.
	 */
	public String getPolo() {
		return polo;
	}

	public String mostrarMsg(ProcessoParte obj) {
		if (obj == null) {
			return null;
		}
		StringBuilder msg = new StringBuilder();
		if (obj.getTipoParte() != null) {
			msg.append(obj.getTipoParte().getTipoParte());
			if (!Strings.isEmpty(situacaoAdvogado(obj))) {
				msg.append(" - ");
				msg.append(situacaoAdvogado(obj));
			}
		}
		return msg.toString();
	}
	
	
	public List<ProcessoParte> obtemProcessoParte_Parte(ProcessoParteParticipacaoEnum processoParteParticipacaoEnum){
		
		String order = "ppa.pessoa.nome";
		
		StringBuilder hql = new StringBuilder();
		hql.append("select ppa ");
		hql.append("from ProcessoParte ppa ");
		hql.append("where ppa.processoTrf.idProcessoTrf = "
				+ ProcessoTrfHome.instance().getInstance().getIdProcessoTrf() + " ");
		hql.append("and ppa.inParticipacao = '" + processoParteParticipacaoEnum + "' ");
		hql.append("and ppa not in ");
		hql.append("(select distinct ppa2 from ProcessoParteRepresentante ppr ");
		hql.append("inner join ppr.parteRepresentante ppa2 ");
		hql.append("where ppa2 = ppa) ");
		if (order != null && order.trim().length() > 0) {
			hql.append("order by " + order);
		}
		
		Query q = getEntityManager().createQuery(hql.toString());
		return q.getResultList();
		
	}
	
	
	public List<ProcessoParte> obtemProcessoParte_Representante(ProcessoParte processoParte){
		
		StringBuilder hql = new StringBuilder();
		hql.append("select distinct ppa2 from ProcessoParteRepresentante ppr ");
		hql.append("inner join ppr.parteRepresentante ppa2 ");
		hql.append("where ppa2.processoTrf.idProcessoTrf = "
				+ ProcessoTrfHome.instance().getInstance().getIdProcessoTrf() + " ");
		hql.append("and ppr.processoParte =:parent ");
		hql.append("and ppa2.inSituacao = 'A' ");
		hql.append("and ppr.inSituacao = 'A'");
		
		Query q = getEntityManager().createQuery(hql.toString());
		q.setParameter("parent", processoParte);
		return q.getResultList();
		
	}
	
	/**
	 * Recupera lista de todos os representantes de uma parte
	 * @param processoParte
	 * @return lista de representantes de uma parte
	 */
	public List<ProcessoParte> obtemProcessoParte_RepresentanteTodos(ProcessoParte processoParte){
		
		StringBuilder hql = new StringBuilder();
		hql.append("select distinct ppa2 from ProcessoParteRepresentante ppr ");
		hql.append("inner join ppr.parteRepresentante ppa2 ");
		hql.append("where ppa2.processoTrf.idProcessoTrf = "
				+ ProcessoTrfHome.instance().getInstance().getIdProcessoTrf() + " ");
		hql.append("and ppr.processoParte =:" + "parent" + " ");
		
		Query q = getEntityManager().createQuery(hql.toString());
		q.setParameter("parent", processoParte);
		return q.getResultList();
		
	}
	
	/**
	 * Método responsável por verificar a exigência de fiscal da lei.
	 * 
	 * @param processoTrf Processo.
	 */
	public void verificarExigenciaFiscalDaLei(ProcessoTrf processoTrf) {
		if (BooleanUtils.isTrue(processoTrf.getClasseJudicial().getExigeFiscalLei())) {
			if (this.classeJudicialAnterioExigeFiscalDaLei) {
				this.removerFiscalLei(processoTrf, processoTrf.getListaPartePrincipalTerceiro());
			}
			this.incluirFiscalLei(processoTrf);
		} else {
			if (this.classeJudicialAnterioExigeFiscalDaLei) {
				this.removerFiscalLei(processoTrf, processoTrf.getListaPartePrincipalTerceiro());
			}
		}
	}
	
	private void incluirFiscalLei(ProcessoTrf processoTrf) {
		Pessoa fiscalDaLei = PessoaManager.instance().getFiscalLei(processoTrf.getJurisdicao());
		if (fiscalDaLei != null) {
			TipoParte tipoParteFiscalLei = ParametroUtil.instance().getTipoParteFiscalLei();
			if (tipoParteFiscalLei != null) {
				ProcessoParte parteFiscalDaLei = null;

				for (ProcessoParte processoParte : processoTrf.getListaPartePrincipalTerceiro()) {
					if (this.tipoParteManager.isFiscalLei(processoParte.getTipoParte())) {
						parteFiscalDaLei = processoParte;
						break;
					}
				}

				if (parteFiscalDaLei == null) {
					parteFiscalDaLei = new ProcessoParte();
					setInstance(parteFiscalDaLei);
					getInstance().setProcessoTrf(processoTrf);
					getInstance().setInParticipacao(ProcessoParteParticipacaoEnum.T);
					getInstance().setPessoa(fiscalDaLei);
					getInstance().setTipoParte(tipoParteFiscalLei);
					getInstance().setPartePrincipal(Boolean.TRUE);
					getInstance().setProcuradoria(recuperarProcuradoria(fiscalDaLei));
					persist(parteFiscalDaLei);
					processoTrf.getProcessoParteList().add(parteFiscalDaLei);
				}

				try {
					this.processoJudicialService.acrescentaVisualizador(processoTrf, fiscalDaLei,
							ComponentUtil.getComponent(PessoaProcuradoriaEntidadeManager.class)
									.getProcuradoriaPadraoPessoa(fiscalDaLei));

				} catch (PJeBusinessException e) {
					this.logger.error("Erro ao acrescentar visualizador: " + e.getLocalizedMessage());
				}

			}
		}
	}
	
	private void removerFiscalLei(ProcessoTrf processoTrf, List<ProcessoParte> partes) {
		for (ProcessoParte processoParte : partes) {
			if (this.tipoParteManager.isFiscalLei(processoParte.getTipoParte())) {
				removerParte(processoParte);
				try {
					processoJudicialService.removeVisualizador(processoTrf, processoParte.getPessoa());
				} catch (PJeBusinessException e) {
					this.logger.error("Erro ao remover visualizador: " + e.getLocalizedMessage());
				}
			}
		}
	}
	
 	/**
 	 * Método responsável por recuperar apenas uma procuradoria dentre todas que representam a pessoa. 
 	 * 
 	 * @param pessoa Pessoa.
 	 * @return Apenas uma procuradoria dentre todas que representam a pessoa
 	 */
 	private Procuradoria recuperarProcuradoria(Pessoa pessoa) {
 		ProcuradoriaManager procuradoriaManager = (ProcuradoriaManager)Component.getInstance(ProcuradoriaManager.NAME);
 		List<Procuradoria> procuradorias = procuradoriaManager.getlistProcuradorias(pessoa, TipoProcuradoriaEnum.P);
 		if (!procuradorias.isEmpty()) {
 			return procuradorias.get(0);
 		}
 		return null;
 	}
	
	/**
	 * Operação que verifica se um tipo parte é do tipo fiscal da lei e se
	 * o processo é de uma classe judicial que exige fiscal da lei.
	 * 
	 * @param processoParte
	 * @return boolean
	 */
	public boolean checkRenderFiscalDaLei(ProcessoParte processoParte) {
		return processoParte != null
				&& BooleanUtils.isTrue(processoParte.getProcessoTrf().getClasseJudicial().getExigeFiscalLei())
				&& this.tipoParteManager.isFiscalLei(processoParte.getTipoParte());
	}

	public Boolean getClasseJudicialAnterioExigeFiscalDaLei() {
		return classeJudicialAnterioExigeFiscalDaLei;
	}

	public void setClasseJudicialAnterioExigeFiscalDaLei(
			Boolean classeJudicialAnterioExigeFiscalDaLei) {
		this.classeJudicialAnterioExigeFiscalDaLei = classeJudicialAnterioExigeFiscalDaLei;
	}
	
	public boolean isParteInativa(ProcessoParte processoParte) {
		return processoParte != null && !processoParte.getIsAtivo();
	}
	
	public boolean isParteBaixada(ProcessoParte processoParte){
	    return processoParte != null && processoParte.getIsBaixado();
	}
	
	/**
	 * Verifica se é PessaoFisica a pessoa vinculada ao ProcessoParte passado como parâmetro 
	 * @param idProcessoParte
	 * @return
	 */
	public boolean ehPessoaFisica(int idProcessoParte) {
		if (this.processoParteConsulta == null)			
			this.processoParteConsulta = EntityUtil.find(ProcessoParte.class, idProcessoParte);
		
		return this.processoParteConsulta.getPessoa() instanceof PessoaFisica;
	}	
	
	/**
	 * Retorna a lista de características físicas da pessoa vinculada ao ProcessoParte passado como parâmetro
	 * @param idProcessoParte
	 * @return
	 */
	public List<CaracteristicaFisica> caracteristicasFisicas(int idProcessoParte) {
		return this.ehPessoaFisica(idProcessoParte) ?
			((PessoaFisica) this.processoParteConsulta.getPessoa()).getCaracteristicasFisicas() :
					new ArrayList<CaracteristicaFisica>();
	}
	
	/**
	 * Verifica se o usuario possui algum endereço cadastrado.
	 * 
	 * @param usuario Usuário.
	 * @return Verdadeiro se o usuario não possui algum endereço cadastrado. Falso, caso contrário.
	 */
	public boolean verificaEnderecosUsuario(Pessoa usuario) {		
		EnderecoService enderecoService = (EnderecoService) Component.getInstance("enderecoService");
		Endereco endereco = enderecoService.recuperaEnderecoRecente(usuario);
		return endereco == null;
	}
	
	public void setaValorCampoEnderecoDesconhecido(boolean enderecoDesconhecido) {		
		getInstance().setIsEnderecoDesconhecido(enderecoDesconhecido);
	}
	
	/**
	 * @return novo ProcessoParteRepresentanteManager.
	 */
	private ProcessoParteRepresentanteManager getProcessoParteRepresentanteManager() {
		return ComponentUtil.getComponent(ProcessoParteRepresentanteManager.NAME);
	}
	
	/**
	 * @param processoParte parte processual
	 * @return String contendo Nome da Parte, OAB(caso seja advogado) e CPF
	 */
	public String getProcessoParteToString(ProcessoParte processoParte){
		ProcessoParteManager processoParteManager = ComponentUtil.getComponent("processoParteManager");;

		return processoParteManager.processoParteToString(processoParte);
	}
	
	/**
	 * Valida se a Pessoa (Parte) já está na lista de Partes do Processo, caso esteja lança uma exceção
	 * @param pessoa Pessoa - pessoa a ser validada
	 * @param listaParteProcesso List<ProcessoParte> - lista com Pessoas (Partes) do Processo
	 */
	private void validarParteExistente(Pessoa pessoa, List<ProcessoParte> listaParteProcesso) throws PJeBusinessException {
		for (ProcessoParte processoParte : listaParteProcesso){
			if (processoParte.getPartePrincipal() && processoParte.getPessoa().getIdPessoa().equals(pessoa.getIdPessoa()) && 
					getInstance().getTipoParte().equals(processoParte.getTipoParte())) {
				
				throw new PJeBusinessException(
						String.format("Parte já cadastrada (%s, Situação: %s).",
						processoParte.getNomeParte(), processoParte.getInSituacao().getLabel()));
			}
		}
	}
	
	private void habilitarVisualizacaoSegredoJustica(Pessoa pessoa, Procuradoria procuradoria){
		if(getProcessoTrfAtual().getSegredoJustica()){
			try {
				if(processoJudicialService.habilitarVisibilidadeSePartePoloAtivoFiscalLei(getProcessoTrfAtual(), pessoa, procuradoria) > 0){
					FacesMessages.instance().add(StatusMessage.Severity.INFO, String.format("A parte '%s' foi cadastrada na lista de permissão de visualização do processo.",pessoa.getNome()));
				}
			} catch (PJeBusinessException e) {
				throw new PJeRuntimeException(e);
			}
		}
	}
	
	private void removerVisualizacaoSegredoJustica(Pessoa pessoa, boolean flush){
		if(getProcessoTrfAtual().getSegredoJustica()){
			try {
				if(processoJudicialService.removeVisualizador(getProcessoTrfAtual(), pessoa, flush)){
					FacesMessages.instance().add(StatusMessage.Severity.INFO, String.format("A parte '%s' foi removida da lista de permissão de visualização do processo.",pessoa.getNome()));
				}
			} catch (PJeBusinessException e) {
				throw new PJeRuntimeException(e);
			}
		}
	}
	
	/**
	 * Exibirá o botão Inserir caso o objeto processoParteHome não seja um objeto gerenciado e, caso tratar-se de uma
	 * Pessoa Autoridade, também verifica se já exista um Id para o usuário da Pessoa Autoridade.
	 * @param 	isPessoaAutoridade
	 * @return	verdadeiro se o objeto processoParteHome não estiver gerenciado e se existir um usuário (idUsuario) de 
	 * 			uma PessoaAutoridade, falso se não existir.
	 */
	public boolean isExibeBotaoInserir(Boolean isPessoaAutoridade){
		boolean retorno = false;
		if (!isManaged()) {
			if (isPessoaAutoridade) {
				PessoaAutoridadeHome pessoaAutoridadeHome = (PessoaAutoridadeHome) Component.getInstance("pessoaAutoridadeHome");
				retorno = pessoaAutoridadeHome.getInstance().getIdUsuario() != null;
			} else {
				retorno = true;
			}
		}
		return retorno;
	}
	
	/**
	 * Método responsável por identificar se houve inversão de pólos através do
	 * método {@link #inverterPolo()}.
	 * 
	 * @return <code>Boolean</code>, <code>True</code> se houve inversão
	 */
	public Boolean getHouveInversaoPolo() {
		return houveInversaoPolo;
	}

	public void setHouveInversaoPolo(Boolean houveInversaoPolo) {
		this.houveInversaoPolo = houveInversaoPolo;
	}
	
	/**
	 * metodo get melhorado da propriedade @idTipoParteAdvogado
	 * somente busca no banco o parametro uma vez
	 * @return idTipoParteAdvogado
	 */
	private Integer obtemIdTipoParteAdvogado() {
		if(idTipoParteAdvogado == null) {
			idTipoParteAdvogado = Integer.parseInt(ParametroUtil.getParametro(Parametros.TIPOPARTEADVOGADO));
		}
		return idTipoParteAdvogado;
	}

	private void rollbackAlteracaoEntidade(ProcessoParte processoParte) {
		if (processoParte.getIdProcessoParte() != 0) {
			getEntityManager().refresh(processoParte);
		}
	}
	
	/**
	 * Método responsável por sinalizar nos fluxos que estiverem observando a alteracao de cadastro de partes, que houve alteracao
	 * @param processoTrf
	 */
	private void sinalizarAlteracoesCadastroPartes(ProcessoTrf processoTrf, ProcessoParte processoParte) {
		if(ProcessoStatusEnum.D.equals(processoTrf.getProcessoStatus())) {
            Events.instance().raiseAsynchronousEvent(Eventos.EVENTO_ATUALIZAR_CAIXAS_PROCURADORES, processoTrf);
        }
		processoJudicialService.sinalizarFluxo(processoTrf, Variaveis.VARIAVEL_FLUXO_CADASTRO_PARTES_ALTERADO, true, false, true);
	}
	
	private boolean isRetificacao() {
		return ProcessoStatusEnum.D.equals(getInstance().getProcessoTrf().getProcessoStatus());
	}
	
	/**
	 * 
	 * Metodo responsavel por filtrar alteracao de cadastro de partes em processos criminais para envio de mensagens
	 * 
	 * @param processoParte ProcessoParte.
	 * @param event EventVerbEnum.
	 */
	private void dispararMensagemPosAlteracaoOuRemocaoParte(ProcessoParte processoParte, CloudEventVerbEnum event) {
		if (processoParte != null && ProcessoStatusEnum.D.equals(processoParte.getProcessoTrf().getProcessoStatus())) {
			boolean isAvisarCriminal = ProcessoParteParticipacaoEnum.P.equals(processoParte.getInParticipacao()) && processoParte.getPartePrincipal() && ComponentUtil.getComponent(ClasseJudicialManager.class).isClasseCriminalOuInfracional(processoParte.getProcessoTrf().getClasseJudicial());
			if (BooleanUtils.isTrue(isAvisarCriminal)){
				enviarMensagem(processoParte, event);
			}
		}
	}
	
	/**
	 * Envia uma mensagem para o serviço de mensageria.
	 * 
	 * @param processoParte ProcessoParte.
	 * @param event EventVerbEnum.
	 */
	protected void enviarMensagem(ProcessoParte processoParte, CloudEventVerbEnum... event) {
		AMQPEventManager.instance().enviarMensagem(processoParte, ProcessoParteCloudEvent.class, event);
	}
}