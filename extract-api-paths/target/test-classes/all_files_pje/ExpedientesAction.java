package br.com.infox.pje.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoVisibilidadeManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.BaseAction;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.filters.ProcessoParteExpedienteFilter;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Classe responsável pelo controle de requisições da aba "Expedientes".
 */
@Name(ExpedientesAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ExpedientesAction extends BaseAction<ProcessoParteExpediente> {
	
	private static final long serialVersionUID = 5649971889822752993L;

	public static final String NAME = "expedientesAction";
	private static final int QTD_LISTA_VAZIA = 0;
	
	private String numeroProcesso;
	private String nomeDestinatario;
	private String numeroCPF;
	private String numeroCNPJ;
	private boolean documentoCPF;
	private Date dataInicioCriacao;
	private Date dataFimCriacao;
	private Date dataInicioPrazoLegal;
	private Date dataFimPrazoLegal;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private ExpedicaoExpedienteEnum meioComunicacao;
	private TipoProcessoDocumento tipoProcessoDocumento;
	private boolean destaque = true;
	
	private EntityDataModel<ProcessoParteExpediente> expedientesPendente;
	private EntityDataModel<ProcessoParteExpediente> expedientesCorreioPendente;
	private EntityDataModel<ProcessoParteExpediente> expedientesMandadoPendente;
	private EntityDataModel<ProcessoParteExpediente> expedientesSemRegistroIntimacao;
	private EntityDataModel<ProcessoParteExpediente> expedientesConfirmadoDestinatarioDentroPrazo;
	private EntityDataModel<ProcessoParteExpediente> expedientesConfirmadoSistemaDentroPrazo;
	private EntityDataModel<ProcessoParteExpediente> expedientesEncerradoUltimoDezDias;
	private EntityDataModel<ProcessoParteExpediente> expedientesSemPrazo;
	
	private Boolean expedientesPendenteCheckAll;
	private Boolean expedientesSemRegistroIntimacaoCheckAll;
	private Boolean expedientesConfirmadoDestinatarioDentroPrazoCheckAll;
	private Boolean expedientesConfirmadoSistemaDentroPrazoCheckAll;
	private Boolean expedientesEncerradoUltimoDezDiasCheckAll;
	private Boolean expedientesSemPrazoCheckAll;
	
	private Map<ProcessoParteExpediente, Boolean> expedientesPendenteCheck;
	private Map<ProcessoParteExpediente, Boolean> expedientesSemRegistroIntimacaoCheck;
	private Map<ProcessoParteExpediente, Boolean> expedientesConfirmadoDestinatarioDentroPrazoCheck;
	private Map<ProcessoParteExpediente, Boolean> expedientesConfirmadoSistemaDentroPrazoCheck;
	private Map<ProcessoParteExpediente, Boolean> expedientesEncerradoUltimoDezDiasCheck;
	private Map<ProcessoParteExpediente, Boolean> expedientesSemPrazoCheck;
	
	@In
	private ProcessoParteExpedienteManager processoParteExpedienteManager;
	
	@In
	private UsuarioLocalizacaoVisibilidadeManager usuarioLocalizacaoVisibilidadeManager;
	
	public ExpedientesAction() {
		this.expedientesPendenteCheckAll = Boolean.FALSE;
		this.expedientesSemRegistroIntimacaoCheckAll = Boolean.FALSE;
		this.expedientesConfirmadoDestinatarioDentroPrazoCheckAll = Boolean.FALSE;
		this.expedientesConfirmadoSistemaDentroPrazoCheckAll = Boolean.FALSE;
		this.expedientesEncerradoUltimoDezDiasCheckAll = Boolean.FALSE;
		this.expedientesSemPrazoCheckAll = Boolean.FALSE;
		
		this.expedientesPendenteCheck = new LinkedHashMap<ProcessoParteExpediente, Boolean>(0);
		this.expedientesSemRegistroIntimacaoCheck = new LinkedHashMap<ProcessoParteExpediente, Boolean>(0);
		this.expedientesConfirmadoDestinatarioDentroPrazoCheck = new LinkedHashMap<ProcessoParteExpediente, Boolean>(0);
		this.expedientesConfirmadoSistemaDentroPrazoCheck = new LinkedHashMap<ProcessoParteExpediente, Boolean>(0);
		this.expedientesEncerradoUltimoDezDiasCheck = new LinkedHashMap<ProcessoParteExpediente, Boolean>(0);
		this.expedientesSemPrazoCheck = new LinkedHashMap<ProcessoParteExpediente, Boolean>(0);
	}
	
	/**
	 * Método responsável pela inicialização da classe.
	 */
	@Create
	public void init() {
		pesquisar();
	}
	
	/**
	 * Método responsável por proceder com a pesquisa dos expedientes de cada agrupador.
	 */
	public void pesquisar() {
		habilitarFiltroOrgaoJulgadorCargo();
		
		pesquisarExpedientesPendente();
		pesquisarExpedientesSemRegistroIntimacao();
		pesquisarExpedientesConfirmadoDestinatarioDentroPrazo();
		pesquisarExpedientesConfirmadoSistemaDentroPrazo();
		pesquisarExpedientesEncerradoUltimoDezDias();
		pesquisarExpedientesSemPrazo();
	}
	
	/**
	 * Habilita FilterDef relacionado ao cargo (orgaoJulgadorCargo) em que o processo está relacionado. 
	 */
	private void habilitarFiltroOrgaoJulgadorCargo() {
		Integer idUsrLocAtual = Authenticator.getUsuarioLocalizacaoAtual().getIdUsuarioLocalizacao();		
		UsuarioLocalizacaoMagistradoServidor usrLocMagistrado = EntityUtil.getEntityManager().find(UsuarioLocalizacaoMagistradoServidor.class, idUsrLocAtual);
		
		if(usuarioLocalizacaoVisibilidadeManager.temOrgaoVisivel(usrLocMagistrado)){
			HibernateUtil.setFilterParameter(ProcessoParteExpedienteFilter.FILTER_ORGAO_JULGADOR_CARGO, "idUsuarioLocalizacao", idUsrLocAtual);
			HibernateUtil.setFilterParameter(ProcessoParteExpedienteFilter.FILTER_ORGAO_JULGADOR_CARGO, "dataAtual", DateUtils.truncate(new Date(), Calendar.DATE));
		}
	}

	/**
	 * Método responsável por inicializar os campos de pesquisa.
	 */
	public void limpar() {
		this.numeroProcesso = null;
		this.nomeDestinatario = null;
		this.documentoCPF = false;
		this.dataInicioCriacao = null;
		this.dataFimCriacao = null;
		this.dataInicioPrazoLegal = null;
		this.dataFimPrazoLegal = null;
		this.classeJudicial = null;
		this.assuntoTrf = null;
		this.meioComunicacao = null;
		this.tipoProcessoDocumento = null;
		this.destaque = true;

		limparNumeroIdentidicacao();
	}
	
	/**
	 * Método responsável por inicializar os campos de pesquisa CPF e CNPJ.
	 */
	public void limparNumeroIdentidicacao() {
		this.numeroCPF = null;
		this.numeroCNPJ = null;
	}
	
	/**
	 * Método responsável por aplicar os filtros de pesquisa ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @throws NoSuchFieldException Caso algum campo declarado não esteja mapeado.
	 */
	private void aplicarFiltros(Search search) throws NoSuchFieldException {
		aplicarFiltroOrgaoJulgadorColegiado(search);
		aplicarFiltroLocalizacaoFisica(search);
		aplicarFiltroProcesso(search);
		aplicarFiltroNomeDestinatario(search);
		aplicarFiltroNumeroIdentificacao(search);
		aplicarFiltroClasseJudicial(search);
		aplicarFiltroAssunto(search);
		aplicarFiltroMeioComunicacao(search);
		aplicarFiltroDataCriacao(search);
		aplicarFiltroPrazoLegal(search);
		aplicarFiltroDestaque(search);
		aplicarFiltroTipoDocumento(search);
	}
	
	/**
	 * Método responsável por aplicar o filtro "Órgão julgador colegiado do usuário logado" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroOrgaoJulgadorColegiado(Search search) throws NoSuchFieldException {
		OrgaoJulgadorColegiado orgaoJulgadorColegiado = Authenticator.getOrgaoJulgadorColegiadoAtual();
		if (orgaoJulgadorColegiado != null) {
			search.addCriteria(Criteria.equals("processoJudicial.orgaoJulgadorColegiado", orgaoJulgadorColegiado));
		}
	}

	/***
	 * Método responsável por aplicar o filtro de localização física do usuário logado, utiliza a hierarquia 
	 * de localizações a partir da localização física do usuário logado
	 * 
	 * @param search
	 * @throws NoSuchFieldException
	 */
	private void aplicarFiltroLocalizacaoFisica(Search search) throws NoSuchFieldException {
		List<Integer> idsLocalizacoesFisicas = Authenticator.getIdsLocalizacoesFilhasAtuaisList();
		if(CollectionUtilsPje.isNotEmpty(idsLocalizacoesFisicas)) {
			search.addCriteria(Criteria.in("processoJudicial.orgaoJulgador.localizacao.idLocalizacao", idsLocalizacoesFisicas.toArray()));
		}
	}
	
	/**
	 * Método responsável por aplicar o filtro de pesquisa "Processo" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroProcesso(Search search) throws NoSuchFieldException {
		if (StringUtils.isNotBlank(this.numeroProcesso)) {
			search.addCriteria(Criteria.contains("processoJudicial.processo.numeroProcesso", this.numeroProcesso));
		}
	}
	
	/**
	 * Método responsável por aplicar o filtro de pesquisa "Nome do destinatário" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroNomeDestinatario(Search search) throws NoSuchFieldException {
		if (StringUtils.isNotBlank(this.nomeDestinatario)) {
			search.addCriteria(Criteria.contains("nomePessoaParte", this.nomeDestinatario));
		}
	}
	
	/**
	 * Método responsável por aplicar o filtro de pesquisa "CPF ou CNPJ" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroNumeroIdentificacao(Search search) throws NoSuchFieldException {
		String numeroIdentificacao = 
				StringUtils.isNotBlank(this.numeroCPF) ? this.numeroCPF : StringUtils.isNotBlank(this.numeroCNPJ) ? this.numeroCNPJ : null;
				
		if (InscricaoMFUtil.validarCpfCnpj(numeroIdentificacao)) {
			search.addCriteria(Criteria.equals("pessoaParte.pessoaDocumentoIdentificacaoList.numeroDocumento", numeroIdentificacao));
		}
	}
	
	/**
	 * Método responsável por aplicar o filtro de pesquisa "Classe judicial" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroClasseJudicial(Search search) throws NoSuchFieldException {
		if (this.classeJudicial != null) {
			search.addCriteria(Criteria.equals("processoJudicial.classeJudicial", this.classeJudicial));
		}
	}
	
	/**
	 * Método responsável por aplicar o filtro de pesquisa "Assunto" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroAssunto(Search search) throws NoSuchFieldException {
		if (this.assuntoTrf != null) {
			search.addCriteria(Criteria.equals("processoJudicial.assuntoTrfList.idAssuntoTrf", this.assuntoTrf.getIdAssuntoTrf()));
		}
	}
	
	/**
	 * Método responsável por aplicar o filtro de pesquisa "Meio de comunicação" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroMeioComunicacao(Search search) throws NoSuchFieldException {
		if (this.meioComunicacao != null) {
			search.addCriteria(Criteria.equals("processoExpediente.meioExpedicaoExpediente", this.meioComunicacao));
		}
	}
	
	/**
	 * Método responsável por aplicar o filtro de pesquisa "Tipo de documento" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-18526
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroTipoDocumento(Search search) throws NoSuchFieldException {
		if (this.tipoProcessoDocumento != null) {
			search.addCriteria(Criteria.equals("processoExpediente.tipoProcessoDocumento.idTipoProcessoDocumento", this.tipoProcessoDocumento.getIdTipoProcessoDocumento()));
		}
	}

	
	/**
	 * Método responsável por aplicar o filtro de pesquisa "Data de criação" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroDataCriacao(Search search) throws NoSuchFieldException {
		if (this.dataInicioCriacao != null) {
			search.addCriteria(Criteria.greaterOrEquals("processoExpediente.dtCriacao", DateUtil.getBeginningOfDay(this.dataInicioCriacao)));
		}
		if (this.dataFimCriacao != null) {
			search.addCriteria(Criteria.lessOrEquals("processoExpediente.dtCriacao", DateUtil.getEndOfDay(this.dataFimCriacao)));
		}
	}
	
	/**
	 * Método responsável por aplicar o filtro de pesquisa "Data da distribuição" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroPrazoLegal(Search search) throws NoSuchFieldException {
		if (this.dataInicioPrazoLegal != null) {
			search.addCriteria(Criteria.greaterOrEquals("dtPrazoLegal", DateUtil.getBeginningOfDay(this.dataInicioPrazoLegal)));
		}
		if (this.dataFimPrazoLegal != null) {
			search.addCriteria(Criteria.lessOrEquals("dtPrazoLegal", DateUtil.getEndOfDay(this.dataFimPrazoLegal)));
		}
	}
	
	/**
	 * Método responsável por aplicar o filtro de pesquisa "Em destaque" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroDestaque(Search search) throws NoSuchFieldException {
		search.addCriteria(Criteria.equals("destaque", this.destaque));
	}
	
	/**
	 * Método responsável por pesquisar os expedientes pendentes.
	 */
	public void pesquisarExpedientesPendente() {
		Criteria[] criterias = {
			Criteria.isNull("dtCienciaParte"),
			Criteria.isNull("pendencia"),
			Criteria.or(Criteria.greater("prazoLegal", 0), Criteria.greater("dtPrazoLegal", new Date())),
			Criteria.equals("fechado", Boolean.FALSE),
			Criteria.equals("processoExpediente.inTemporario", Boolean.FALSE)
		};
		
		DataRetriever<ProcessoParteExpediente> dataRetriever = new ExpedientesRetriever(
			this.processoParteExpedienteManager, this.facesMessages, criterias, "o.dtPrazoLegal", Order.ASC);					
		
		this.expedientesPendente = 
			new EntityDataModel<ProcessoParteExpediente>(ProcessoParteExpediente.class,	this.facesContext, dataRetriever);
	}
	
	/**
	 * Método responsável por pesquisar os expedientes sem registro de intimação cujo meio de envio é "Correios".
	 */
	public void pesquisarExpedientesSemRegistroIntimacao() {
		Criteria[] criterias = {
			Criteria.in("processoExpediente.meioExpedicaoExpediente", new Object[] { ExpedicaoExpedienteEnum.C, ExpedicaoExpedienteEnum.G }),
			Criteria.equals("fechado", false),
			Criteria.empty("registroIntimacaoList")
		};
		
		DataRetriever<ProcessoParteExpediente> dataRetriever = new ExpedientesRetriever(
			this.processoParteExpedienteManager, this.facesMessages, criterias, "o.dtPrazoLegal", Order.ASC);					
		
		this.expedientesSemRegistroIntimacao = 
			new EntityDataModel<ProcessoParteExpediente>(ProcessoParteExpediente.class,	this.facesContext, dataRetriever);
	}
	
	/**
	 * Método responsável por pesquisar os expedientes confirmados pelo destinatário e dentro do prazo.
	 */
	public void pesquisarExpedientesConfirmadoDestinatarioDentroPrazo() {
		Criteria[] criterias = {
			Criteria.and(
				Criteria.equals("cienciaSistema", Boolean.FALSE),
				Criteria.equals("fechado", Boolean.FALSE),
				Criteria.not(Criteria.isNull("dtCienciaParte")),
				Criteria.or(	
						Criteria.greater("prazoLegal", 0), 
						Criteria.greaterOrEquals("dtPrazoLegal", new Date())
					)
				)
		};
		DataRetriever<ProcessoParteExpediente> dataRetriever = new ExpedientesRetriever(
			this.processoParteExpedienteManager, this.facesMessages, criterias, "o.dtPrazoLegal", Order.ASC);					
		
		this.expedientesConfirmadoDestinatarioDentroPrazo = 
			new EntityDataModel<ProcessoParteExpediente>(ProcessoParteExpediente.class,	this.facesContext, dataRetriever);
	}
	
	/**
	 * Método responsável por pesquisar os expedientes confirmados pelo sistema e dentro do prazo.
	 */
	public void pesquisarExpedientesConfirmadoSistemaDentroPrazo() {
		Criteria[] criterias = {
			Criteria.equals("cienciaSistema", Boolean.TRUE),
			Criteria.not(Criteria.isNull("dtCienciaParte")),
			Criteria.greaterOrEquals("dtPrazoLegal", new Date()), 
			Criteria.notEquals("prazoLegal", 0)
		};
		
		DataRetriever<ProcessoParteExpediente> dataRetriever = new ExpedientesRetriever(
			this.processoParteExpedienteManager, this.facesMessages, criterias, "o.dtPrazoLegal", Order.ASC);
		
		this.expedientesConfirmadoSistemaDentroPrazo = 
			new EntityDataModel<ProcessoParteExpediente>(ProcessoParteExpediente.class,	this.facesContext, dataRetriever);
	}
	
	/**
	 * Método responsável por pesquisar os expedientes encerrados nos últimos dez dias.
	 */
	public void pesquisarExpedientesEncerradoUltimoDezDias() {
		Date dataAtual = Calendar.getInstance().getTime();
		Criteria[] criterias = {
			Criteria.greaterOrEquals("dtPrazoLegal", DateUtil.dataMenosDias(dataAtual, 10)), 
			Criteria.less("dtPrazoLegal", dataAtual),
			Criteria.notEquals("prazoLegal", 0),
			Criteria.or(
				Criteria.isNull("resposta"), 
				Criteria.equals("fechado", Boolean.TRUE))
		};
		
		DataRetriever<ProcessoParteExpediente> dataRetriever = new ExpedientesRetriever(
			this.processoParteExpedienteManager, this.facesMessages, criterias, "o.dtPrazoProcessual", Order.ASC);					
		
		this.expedientesEncerradoUltimoDezDias = 
			new EntityDataModel<ProcessoParteExpediente>(ProcessoParteExpediente.class,	this.facesContext, dataRetriever);
	}
	
	/**
	 * Método responsável por pesquisar os expedientes sem prazo.
	 */
	public void pesquisarExpedientesSemPrazo() {
		Date dataAtual = Calendar.getInstance().getTime();
		Criteria[] criterias = {
			Criteria.greaterOrEquals("processoExpediente.dtCriacao", DateUtil.dataMenosDias(dataAtual, 30)), 
			Criteria.isNull("pendencia"),
			Criteria.equals("tipoPrazo", TipoPrazoEnum.S),
			Criteria.isNull("dtPrazoLegal"),
			Criteria.equals("processoExpediente.inTemporario", Boolean.FALSE)
		};
		
		DataRetriever<ProcessoParteExpediente> dataRetriever = new ExpedientesRetriever(
			this.processoParteExpedienteManager, this.facesMessages, criterias, "o.processoExpediente", Order.DESC);					
		
		this.expedientesSemPrazo = 
			new EntityDataModel<ProcessoParteExpediente>(ProcessoParteExpediente.class,	this.facesContext, dataRetriever);
	}
	
	/**
	 * Método responsável por selecionar (ou retirar a seleção) de todos os componentes checkbox da lista.
	 * 
	 * @param entityDataModel Componente de paginação de dados.
	 * @param map Variável que armazena o status dos componentes checkbox da lista.
	 * @param status Variável que indica qual será o status dos componentes checkbox da lista.
	 */
	public void selecionarTodosCheck(EntityDataModel<ProcessoParteExpediente> entityDataModel, 
			Map<ProcessoParteExpediente, Boolean> map, boolean status) {
		
		List<ProcessoParteExpediente> list = entityDataModel.getPage();
		for (ProcessoParteExpediente element : list) {
			map.put(element, status);
		}
	}
	
	/**
	 * Método responsável por verificar se algum componente checkbox da lista está selecionado.
	 * 
	 * @return Verdadeiro se algum componente checkbox da lista está selecionado. Falso, caso contrário.
	 */
	public boolean verificarCheck(Map<ProcessoParteExpediente, Boolean> map) {
		for (Map.Entry<ProcessoParteExpediente, Boolean> entry : map.entrySet()) {
			if (entry.getValue() == true) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Método responsável por atualizar o destaque dos expedientes selecionados da lista de
	 * expedientes pendentes.
	 */
	public void atualizarDestaqueExpedientesPendente() {
		atualizarDestaque(this.expedientesPendenteCheck);
		this.expedientesPendenteCheck = new LinkedHashMap<ProcessoParteExpediente, Boolean>(0);
	}
	
	/**
	 * Método responsável por atualizar o destaque dos expedientes selecionados da lista de
	 * expedientes sem registro de intimação.
	 */
	public void atualizarDestaqueExpedientesSemRegistroIntimacao() {
		atualizarDestaque(this.expedientesSemRegistroIntimacaoCheck);
		this.expedientesSemRegistroIntimacaoCheck = new LinkedHashMap<ProcessoParteExpediente, Boolean>(0);
	}
	
	/**
	 * Método responsável por atualizar o destaque dos expedientes selecionados da lista de
	 * expedientes confirmados pelo destinatário e dentro do prazo.
	 */
	public void atualizarDestaqueExpedientesConfirmadoDestinatarioDentroPrazo() {
		atualizarDestaque(this.expedientesConfirmadoDestinatarioDentroPrazoCheck);
		this.expedientesConfirmadoDestinatarioDentroPrazoCheck = new LinkedHashMap<ProcessoParteExpediente, Boolean>(0);
	}
	
	/**
	 * Método responsável por atualizar o destaque dos expedientes selecionados da lista de
	 * expedientes confirmados pelo sistema e dentro do prazo.
	 */
	public void atualizarDestaqueExpedientesConfirmadoSistemaDentroPrazo() {
		atualizarDestaque(this.expedientesConfirmadoSistemaDentroPrazoCheck);
		this.expedientesConfirmadoSistemaDentroPrazoCheck = new LinkedHashMap<ProcessoParteExpediente, Boolean>(0);
	}
	
	/**
	 * Método responsável por atualizar o destaque dos expedientes selecionados da lista de
	 * expedientes encerrados nos últimos dez dias.
	 */
	public void atualizarDestaqueExpedientesEncerradoUltimoDezDias() {
		atualizarDestaque(this.expedientesEncerradoUltimoDezDiasCheck);
		this.expedientesEncerradoUltimoDezDiasCheck = new LinkedHashMap<ProcessoParteExpediente, Boolean>(0);
	}
	
	/**
	 * Método responsável por atualizar o destaque dos expedientes selecionados da lista de
	 * expedientes encerrados nos últimos dez dias.
	 */
	public void atualizarDestaqueExpedientesSemPrazo() {
		atualizarDestaque(this.expedientesSemPrazoCheck);
		this.expedientesSemPrazoCheck = new LinkedHashMap<ProcessoParteExpediente, Boolean>(0);
	}
	
	/**
	 * Método responsável por atualizar o destaque dos expedientes selecionados da lista.
	 */
	private void atualizarDestaque(Map<ProcessoParteExpediente, Boolean> map){
		if (map.containsValue(Boolean.TRUE)) {
			for (Map.Entry<ProcessoParteExpediente, Boolean> entry : map.entrySet()) {
				if (entry.getValue() == true) {
					ProcessoParteExpediente processoParteExpediente = entry.getKey();
					processoParteExpediente.setDestaque(!processoParteExpediente.getDestaque());
				}
			}
			EntityUtil.getEntityManager().flush();
			pesquisar();
		}
	}
	
	/**
	 * Classe interna responsável pela pesquisa de expedientes.
	 */
	private class ExpedientesRetriever implements DataRetriever<ProcessoParteExpediente>{
		private Long count;
		private FacesMessages facesMessages;		
		private ProcessoParteExpedienteManager processoParteExpedienteManager;
		private Criteria[] criterias;
		
		public ExpedientesRetriever(ProcessoParteExpedienteManager processoParteExpedienteManager, 
				FacesMessages facesMessages, Criteria[] criterias, String attributeOrder, Order order){
			
			this.processoParteExpedienteManager = processoParteExpedienteManager;
			this.facesMessages = facesMessages;
			this.criterias = criterias;
		}
		
		@Override
		public Object getId(ProcessoParteExpediente obj) {
			return this.processoParteExpedienteManager.getId(obj);
		}

		@Override
		public ProcessoParteExpediente findById(Object id) throws Exception {
			return this.processoParteExpedienteManager.findById(id);
		}

		@Override
		public List<ProcessoParteExpediente> list(Search search) {
			List<ProcessoParteExpediente> retorno = new ArrayList<ProcessoParteExpediente>(0);
			try {
				atualizarDadosPesquisa(search, 10);
				retorno = this.processoParteExpedienteManager.list(search);
			} catch (NoSuchFieldException ex){
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar os registros.");
			}
			return retorno;
		}

		@Override
		public long count(Search search) {
			if(this.count == null){
				try {
					atualizarDadosPesquisa(search, 0);
					this.count = this.processoParteExpedienteManager.count(search);
				} catch (NoSuchFieldException ex) {
					this.facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar o número de registros.");
					return QTD_LISTA_VAZIA;
				}
			}
			return count;
		}
		
		private void atualizarDadosPesquisa(Search search, int qtdRegistros) throws NoSuchFieldException {
			aplicarFiltros(search);
			
			for (Criteria criteria : criterias) {
				search.addCriteria(criteria);
			}

			search.setMax(qtdRegistros);
		}
	}

	@Override
	protected BaseManager<ProcessoParteExpediente> getManager() {
		return this.processoParteExpedienteManager;
	}

	@Override
	public EntityDataModel<ProcessoParteExpediente> getModel() {
		return null;
	}
	
	// GETTERs AND SETTERs

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNomeDestinatario() {
		return nomeDestinatario;
	}

	public void setNomeDestinatario(String nomeDestinatario) {
		this.nomeDestinatario = nomeDestinatario;
	}

	public String getNumeroCPF() {
		return numeroCPF;
	}

	public void setNumeroCPF(String numeroCPF) {
		this.numeroCPF = numeroCPF;
	}

	public String getNumeroCNPJ() {
		return numeroCNPJ;
	}

	public void setNumeroCNPJ(String numeroCNPJ) {
		this.numeroCNPJ = numeroCNPJ;
	}

	public Date getDataInicioCriacao() {
		return dataInicioCriacao;
	}

	public void setDataInicioCriacao(Date dataInicioCriacao) {
		this.dataInicioCriacao = dataInicioCriacao;
	}

	public Date getDataFimCriacao() {
		return dataFimCriacao;
	}

	public void setDataFimCriacao(Date dataFimCriacao) {
		this.dataFimCriacao = dataFimCriacao;
	}

	public Date getDataInicioPrazoLegal() {
		return dataInicioPrazoLegal;
	}

	public void setDataInicioPrazoLegal(Date dataInicioPrazoLegal) {
		this.dataInicioPrazoLegal = dataInicioPrazoLegal;
	}

	public Date getDataFimPrazoLegal() {
		return dataFimPrazoLegal;
	}

	public void setDataFimPrazoLegal(Date dataFimPrazoLegal) {
		this.dataFimPrazoLegal = dataFimPrazoLegal;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public ExpedicaoExpedienteEnum getMeioComunicacao() {
		return meioComunicacao;
	}

	public void setMeioComunicacao(ExpedicaoExpedienteEnum meioComunicacao) {
		this.meioComunicacao = meioComunicacao;
	}

	public boolean isDestaque() {
		return destaque;
	}

	public void setDestaque(boolean destaque) {
		this.destaque = destaque;
	}

	public boolean isDocumentoCPF() {
		return documentoCPF;
	}

	public void setDocumentoCPF(boolean documentoCPF) {
		this.documentoCPF = documentoCPF;
	}

	public EntityDataModel<ProcessoParteExpediente> getExpedientesPendente() {
		return expedientesPendente;
	}

	public EntityDataModel<ProcessoParteExpediente> getExpedientesCorreioPendente() {
		return expedientesCorreioPendente;
	}

	public EntityDataModel<ProcessoParteExpediente> getExpedientesMandadoPendente() {
		return expedientesMandadoPendente;
	}

	public EntityDataModel<ProcessoParteExpediente> getExpedientesSemRegistroIntimacao() {
		return expedientesSemRegistroIntimacao;
	}

	public EntityDataModel<ProcessoParteExpediente> getExpedientesConfirmadoDestinatarioDentroPrazo() {
		return expedientesConfirmadoDestinatarioDentroPrazo;
	}

	public EntityDataModel<ProcessoParteExpediente> getExpedientesConfirmadoSistemaDentroPrazo() {
		return expedientesConfirmadoSistemaDentroPrazo;
	}

	public EntityDataModel<ProcessoParteExpediente> getExpedientesEncerradoUltimoDezDias() {
		return expedientesEncerradoUltimoDezDias;
	}

	public EntityDataModel<ProcessoParteExpediente> getExpedientesSemPrazo() {
		return expedientesSemPrazo;
	}
	
	public Boolean getExpedientesPendenteCheckAll() {
		return expedientesPendenteCheckAll;
	}

	public void setExpedientesPendenteCheckAll(Boolean expedientesPendenteCheckAll) {
		this.expedientesPendenteCheckAll = expedientesPendenteCheckAll;
	}

	public Boolean getExpedientesSemRegistroIntimacaoCheckAll() {
		return expedientesSemRegistroIntimacaoCheckAll;
	}

	public void setExpedientesSemRegistroIntimacaoCheckAll(Boolean expedientesSemRegistroIntimacaoCheckAll) {
		this.expedientesSemRegistroIntimacaoCheckAll = expedientesSemRegistroIntimacaoCheckAll;
	}

	public Boolean getExpedientesConfirmadoDestinatarioDentroPrazoCheckAll() {
		return expedientesConfirmadoDestinatarioDentroPrazoCheckAll;
	}

	public void setExpedientesConfirmadoDestinatarioDentroPrazoCheckAll(Boolean expedientesConfirmadoDestinatarioDentroPrazoCheckAll) {
		this.expedientesConfirmadoDestinatarioDentroPrazoCheckAll = expedientesConfirmadoDestinatarioDentroPrazoCheckAll;
	}

	public Boolean getExpedientesConfirmadoSistemaDentroPrazoCheckAll() {
		return expedientesConfirmadoSistemaDentroPrazoCheckAll;
	}

	public void setExpedientesConfirmadoSistemaDentroPrazoCheckAll(Boolean expedientesConfirmadoSistemaDentroPrazoCheckAll) {
		this.expedientesConfirmadoSistemaDentroPrazoCheckAll = expedientesConfirmadoSistemaDentroPrazoCheckAll;
	}

	public Boolean getExpedientesEncerradoUltimoDezDiasCheckAll() {
		return expedientesEncerradoUltimoDezDiasCheckAll;
	}

	public void setExpedientesEncerradoUltimoDezDiasCheckAll(Boolean expedientesEncerradoUltimoDezDiasCheckAll) {
		this.expedientesEncerradoUltimoDezDiasCheckAll = expedientesEncerradoUltimoDezDiasCheckAll;
	}
	
	public Boolean getExpedientesSemPrazoCheckAll() {
		return expedientesSemPrazoCheckAll;
	}

	public void setExpedientesSemPrazoCheckAll(Boolean expedientesSemPrazoCheckAll) {
		this.expedientesSemPrazoCheckAll = expedientesSemPrazoCheckAll;
	}

	public Map<ProcessoParteExpediente, Boolean> getExpedientesPendenteCheck() {
		return expedientesPendenteCheck;
	}

	public void setExpedientesPendenteCheck(Map<ProcessoParteExpediente, Boolean> expedientesPendenteCheck) {
		this.expedientesPendenteCheck = expedientesPendenteCheck;
	}

	public Map<ProcessoParteExpediente, Boolean> getExpedientesSemRegistroIntimacaoCheck() {
		return expedientesSemRegistroIntimacaoCheck;
	}

	public void setExpedientesSemRegistroIntimacaoCheck(Map<ProcessoParteExpediente, Boolean> expedientesSemRegistroIntimacaoCheck) {
		this.expedientesSemRegistroIntimacaoCheck = expedientesSemRegistroIntimacaoCheck;
	}

	public Map<ProcessoParteExpediente, Boolean> getExpedientesConfirmadoDestinatarioDentroPrazoCheck() {
		return expedientesConfirmadoDestinatarioDentroPrazoCheck;
	}

	public void setExpedientesConfirmadoDestinatarioDentroPrazoCheck(
			Map<ProcessoParteExpediente, Boolean> expedientesConfirmadoDestinatarioDentroPrazoCheck) {
		
		this.expedientesConfirmadoDestinatarioDentroPrazoCheck = expedientesConfirmadoDestinatarioDentroPrazoCheck;
	}

	public Map<ProcessoParteExpediente, Boolean> getExpedientesConfirmadoSistemaDentroPrazoCheck() {
		return expedientesConfirmadoSistemaDentroPrazoCheck;
	}

	public void setExpedientesConfirmadoSistemaDentroPrazoCheck(
			Map<ProcessoParteExpediente, Boolean> expedientesConfirmadoSistemaDentroPrazoCheck) {
		
		this.expedientesConfirmadoSistemaDentroPrazoCheck = expedientesConfirmadoSistemaDentroPrazoCheck;
	}

	public Map<ProcessoParteExpediente, Boolean> getExpedientesEncerradoUltimoDezDiasCheck() {
		return expedientesEncerradoUltimoDezDiasCheck;
	}

	public void setExpedientesEncerradoUltimoDezDiasCheck(Map<ProcessoParteExpediente, Boolean> expedientesEncerradoUltimoDezDiasCheck) {
		this.expedientesEncerradoUltimoDezDiasCheck = expedientesEncerradoUltimoDezDiasCheck;
	}

	public Map<ProcessoParteExpediente, Boolean> getExpedientesSemPrazoCheck() {
		return expedientesSemPrazoCheck;
	}

	public void setExpedientesSemPrazoCheck(Map<ProcessoParteExpediente, Boolean> expedientesSemPrazoCheck) {
		this.expedientesSemPrazoCheck = expedientesSemPrazoCheck;
	}

	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}
	
	
	
}
