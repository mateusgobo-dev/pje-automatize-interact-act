package br.com.infox.cliente.home;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;

import br.com.infox.pje.manager.ProcessoTrfManager;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.ProcessoInstance;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.AssertionFailure;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.bpm.ProcessInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.component.suggest.ProcessoAudienciaCadAudSuggestBean;
import br.com.infox.cliente.component.tree.AssuntoTrfTreeHandler;
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.cliente.jbpm.actions.JbpmEventsHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.list.ProcessoAudienciaList;
import br.com.infox.pje.list.ConsultaProcessoSimplesList;
import br.com.infox.pje.manager.ProcessoAudienciaManager;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.com.jt.pje.manager.SalaManager;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoInstanceManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.SalaService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.fluxo.AtaAudienciaAction;
import br.jus.csjt.pje.business.pdf.GeradorPdfUnificado;
import br.jus.csjt.pje.business.pdf.PdfException;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.BloqueioPauta;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoAudienciaPessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.SalaHorario;
import br.jus.pje.nucleo.entidades.TempoAudienciaOrgaoJulgador;
import br.jus.pje.nucleo.entidades.TipoAudiencia;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;
import br.jus.pje.nucleo.enums.EtapaAudienciaEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.StatusAudienciaEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name(ProcessoAudienciaHome.NAME)
public class ProcessoAudienciaHome extends AbstractHome<ProcessoAudiencia>{

	public static final String NAME = "processoAudienciaHome";
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ProcessoAudienciaHome.class.getName());
	
	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@In
	private transient TramitacaoProcessualService tramitacaoProcessualService;

	private Boolean salaEncontrada = Boolean.FALSE;
	private Boolean abaDocumentoProcessoAudiencia = Boolean.FALSE;
	private Integer tempoAudiencia;
	private ProcessoAudiencia processoAudiencia;
	private Boolean mostrarDiv = Boolean.TRUE;
	private Boolean abaRealizarAudiencia = Boolean.FALSE;
	private Boolean abaCadastroTestemunha = Boolean.FALSE;
	private Boolean abaDocumentoRealizarAudiencia = Boolean.FALSE;
	private Boolean mostrarDivCancelamento = Boolean.FALSE;
	private Boolean mostrarDivRemarcacao = Boolean.FALSE;
	private Boolean abaRemarcarAudiencia = Boolean.FALSE;
	private Boolean isNewInstance = Boolean.TRUE;
	private Boolean mostrarBtnDesignar = Boolean.TRUE;
	private Boolean achouSalaDisponivel = Boolean.FALSE;
	private Integer contadorFluxo = 0;
	private Boolean rendTestemunha = Boolean.FALSE;
	private Boolean rendDocumento = Boolean.FALSE;
	private String tab;
	private Calendar dtIni = Calendar.getInstance();
	private Calendar dtFim = null;
	private Boolean salaIndisponivelEmQualquerDia = Boolean.FALSE;
	private Boolean semSalaParaAudiencia = Boolean.TRUE;
	private Boolean horaInvalida = Boolean.FALSE;
	private StatusAudienciaEnum statusAudiencia;
	private Pessoa pessoaConciliador;
	private Boolean inAcordo = Boolean.TRUE;
	private Double valorAcordo;
	private TipoAudiencia tipoAudienciaPadrao;
	private Boolean exibeModalAlterarTipoAudiencia;
	private List<StatusAudienciaEnum> statusAudienciaList;
	private Jurisdicao jurisdicao;
	private OrgaoJulgador orgaoJulgador;
	private List<OrgaoJulgador> listaOrgaoJulgador;
	private List<Sala> listaSala;
	public static final int TEMPO_RESERVA_SALA = 10; 
	public static final int TEMPO_PESQUISA_BLOQUEIO = 90;
 	private static final String PARAMETRO_CODIGO_FLUXO_INTIMACAO = "tjrj:fluxo:cjsIas:codigoFluxoIntimacaoCejusc"; 

	private Integer distanciaMinimaPesquisa;
	private Date dtInicioPesquisa;
	private EtapaAudienciaEnum etapaAudiencia;
	private String etapaRealizacaoAudiencia = null; // Valores: I: Inicio, T: Testemunhas, D: Documentos
	private TipoAudiencia tipoAudiencia;
	private String tipoDesignacao = ProcessoAudiencia.TIPO_DESIGNACAO_SUGERIDA;
	private Sala salaAudienciaTemp;
	private List<ProcessoAudiencia> audienciasEmConflito = null;
	private List<TipoAudiencia> tiposAudiencia = null;
	private List<Sala> salasAudiencia = null;
	private List<PessoaFisica> conciliadores;
	private List<PessoaFisica> realizadores;	
	private List<SalaHorario> diasDisponiveisSemana = new ArrayList<SalaHorario>(0); 
	private List<ProcessoAudiencia> horariosEncontrados = new ArrayList<ProcessoAudiencia>(0);
	private List<ProcessoAudiencia> pautaAudienciaSala = null;
	private List<BloqueioPauta> bloqueiosPauta = null;
	private Boolean termoAudiencia = Boolean.FALSE;
	public StatusAudienciaEnum statusAudienciaValues;
	private ProcessoDocumento processoDocumento;
	private Boolean confirmaMarcacaoManual = Boolean.FALSE;
	private List<String> observacoesConfirmacao = new ArrayList<String>(0);
	private String tipoPolo;
	private Boolean ocultarDadosProcesso;
	private Boolean ocultarDocumentosProcesso;
	private Boolean permitirDesignarMultiplasAudiencias;
	private Boolean buscarHorariosReservados = Boolean.FALSE;
	private Boolean selecionarOrgaoJulgador = Boolean.FALSE;
	
	public void inicializaCombos(){
		try {
			jurisdicao =  recuperaJurisdicaoInicial();
			orgaoJulgador = recuperaOrgaoJulgadorInicial();
			listaOrgaoJulgador = refazListaOrgaoJulgador();
			listaSala = refazListaSala();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	private OrgaoJulgador recuperaOrgaoJulgadorInicial() {
		if (Authenticator.getLocalizacaoUsuarioLogado() != null) {
			Localizacao localizacao = Authenticator.getLocalizacaoFisicaAtual();
			return ComponentUtil.getOrgaoJulgadorService().getOrgaoJulgadorByLocalizacao(localizacao);
		}
		
		return null;
	}
	private Jurisdicao recuperaJurisdicaoInicial() {
		if (orgaoJulgador==null){
			orgaoJulgador=recuperaOrgaoJulgadorInicial();
		}
		if (orgaoJulgador!=null){
			return orgaoJulgador.getJurisdicao();
		}
		return null;
	}
	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public List<OrgaoJulgador> getListaOrgaoJulgador() {
		return this.listaOrgaoJulgador ;
	}
	
	public void setListaOrgaoJulgador(List<OrgaoJulgador> lista){
		this.listaOrgaoJulgador=lista;
	}

	public TipoAudiencia getTipoAudienciaPadrao() {
		return tipoAudienciaPadrao;
	}

	public void setTipoAudienciaPadrao(TipoAudiencia tipoAudienciaPadrao) {
		this.tipoAudienciaPadrao = tipoAudienciaPadrao;
	}

	public  List<OrgaoJulgador> refazListaOrgaoJulgador(){
		
		if (jurisdicao==null){
			listaOrgaoJulgador = ComponentUtil.getOrgaoJulgadorService().findAll();
			orgaoJulgador = null;			
		}
		
		else {
			listaOrgaoJulgador = ComponentUtil.getOrgaoJulgadorService().findAllbyJurisdicao(jurisdicao);
		}
		if (orgaoJulgador!=null){
			if (!listaOrgaoJulgador.contains(orgaoJulgador)){
				orgaoJulgador=null;
			}
		}
	
		refazListaSala();
		
		return listaOrgaoJulgador;
	}
	
	public  List<Sala> refazListaSala(){
		Localizacao localizacao = null;
		if (orgaoJulgador != null) {
			localizacao = orgaoJulgador.getLocalizacao();
		}
		this.listaSala = new SalaService().getSalaListByLocalizacaoAndTipoAudiencia(localizacao , tipoAudiencia);
		return listaSala;
	}
	
	public List<Sala> getListaSala(){
		return this.listaSala;
	}
	
	public void setListaSala(List<Sala>listaSala){
		this.listaSala = listaSala;
	}
	
	public void setJurisdicao(Jurisdicao jurisdicao){
		this.jurisdicao = jurisdicao;
	}
	public Jurisdicao getJurisdicao(){
		return jurisdicao;
	}
	
	public List<Jurisdicao> getListaJurisdicao(){
		@SuppressWarnings("unchecked")
		List<Jurisdicao> resultList = getEntityManager()
				.createQuery(
						"select j from Jurisdicao j where j.ativo = true")
				.getResultList();

		return resultList;
	}

	@Override
	public void create(){
		super.create();
		
		if (TaskInstanceUtil.instance().getProcessInstance() != null) {
			ProcessoAudiencia audienciaSelecionada = (ProcessoAudiencia) ComponentUtil.getTramitacaoProcessualService()
					.recuperaVariavel(Variaveis.VARIAVEL_FLUXO_AUDIENCIA_SELECIONADA);

			if (audienciaSelecionada == null) {
				audienciaSelecionada = this.processoJudicialManager
						.getProximaAudienciaDesignada(ProcessoTrfHome.instance().getInstance());
			}
			
			if (audienciaSelecionada != null) {
				setIsNewInstance(false);
				setId(audienciaSelecionada.getIdProcessoAudiencia());
				setProcessoAudiencia(audienciaSelecionada);
				if (getPermitirDesignarMultiplasAudiencias()) {
					setEtapaAudiencia(EtapaAudienciaEnum.M);
				}
			}
		}
		
		if (JbpmEventsHandler.instance() != null && (JbpmEventsHandler.instance().getTaskId() != null
				|| JbpmEventsHandler.instance().getNewTaskId() != null)) {
			
			fluxoDesignarAudiencia();
		}
	}

	@Override
	protected ProcessoAudiencia loadInstance(){
		if (JbpmEventsHandler.instance() != null 
				&& (JbpmEventsHandler.instance().getTaskId() != null
				|| JbpmEventsHandler.instance().getNewTaskId() != null)) {
			fluxoDesignarAudiencia();
		}
		return super.loadInstance();
	}

	@Override
	public String getTab(){
		return tab;
	}

	@Override
	public void setTab(String tab){
		this.tab = tab;
	}

	public Boolean getRendDocumento(){
		return rendDocumento;
	}

	public void setRendDocumento(Boolean rendDocumento){
		this.rendDocumento = rendDocumento;
	}

	public Boolean getRendTestemunha(){
		return rendTestemunha;
	}

	public void setRendTestemunha(Boolean rendTestemunha){
		this.rendTestemunha = rendTestemunha;
	}

	public void showTestemunha(){
		if (getInstance().getInAcordo() != null || !getInstance().getInAcordo()){
			getInstance().setVlAcordo(0.00);
		}
		getEntityManager().merge(getInstance());
		EntityUtil.flush();
		setRendTestemunha(Boolean.TRUE);
		setTab("cadastroTestemunhaAba");
	}

	public void showDocumento(){
		setRendDocumento(Boolean.TRUE);
		ProcessoDocumentoHome.instance().newInstance();
		setTab("anexarDocumentoAudiencia");
	}

	// Método chamado ao selecionar a tarefa Cadastrar Expediente no fluxo.
	@SuppressWarnings("unchecked")
	public void fluxoDesignarAudiencia(){
		if (isNewInstance){
			newInstance();
			setEtapaAudiencia(EtapaAudienciaEnum.M);
			setIsNewInstance(Boolean.FALSE);
			contadorFluxo++;
			carregarTipoAudienciaPadrao();
		}
			List<Integer> idsOJs  = 
					(List<Integer>) tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.VARIAVEL_FLUXO_AUDIENCIA_VALORES_IDS_COMBO_OJ);
			this.selecionarOrgaoJulgador = (idsOJs != null && idsOJs.size() > 0) ? Boolean.TRUE : Boolean.FALSE;		
			if (this.selecionarOrgaoJulgador) {				
				listaOrgaoJulgador =  ComponentUtil.getOrgaoJulgadorService().findbyIds(idsOJs);				
				if(ProcessoTrfHome.instance().getInstance().getOrgaoJulgador() != null && 
				  listaOrgaoJulgador.contains(ProcessoTrfHome.instance().getInstance().getOrgaoJulgador())) {
					orgaoJulgador = ProcessoTrfHome.instance().getInstance().getOrgaoJulgador();
				}
			}
	}

	// Método chamado ao selecionar a tarefa Visualizar Expedientes no fluxo.
	public void fluxoOperacoesAudiencia(){
		if (contadorFluxo != 0){
			newInstance();
		}
		setMostrarBtnDesignar(Boolean.FALSE);
	}

	public void showDivMarcar(){
		newInstance();
		carregarTipoAudienciaPadrao();
		setEtapaAudiencia(EtapaAudienciaEnum.M);
		setTab("marcacaoAudienciaTab");
	}

	public void showDivRealizar(ProcessoAudiencia obj){
		setInstance(obj);
		setEtapaAudiencia(EtapaAudienciaEnum.R);
		setTab("acoesAudienciaTab");
	}

	public void showDivCancelarConverter(ProcessoAudiencia obj, String tipo){
		setInstance(obj);
		if ("c".equals(tipo)){
			setEtapaAudiencia(EtapaAudienciaEnum.C);
		}
		else if ("d".equals(tipo)){
			setEtapaAudiencia(EtapaAudienciaEnum.D);
		}
		setTab("acoesAudienciaTab");
	}

	public void showDivRemarcar(ProcessoAudiencia audiencia){
		setEtapaAudiencia(EtapaAudienciaEnum.L);
		setTab("marcacaoAudienciaTab");
		setInstance(audiencia);
	}

	public Boolean getIsNewInstance(){
		return isNewInstance;
	}

	public void setIsNewInstance(Boolean isNewInstance){
		this.isNewInstance = isNewInstance;
	}

	public void setMostrarBtnDesignar(Boolean mostrarBtnDesignar){
		this.mostrarBtnDesignar = mostrarBtnDesignar;
	}

	public Boolean getMostrarBtnDesignar(){
		return mostrarBtnDesignar;
	}
	
	public String dataPrevistaFormatada(){
		return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(getInstance().getDtInicio());
	}

	@Override
	public void setId(Object id){
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed){
			if (!getInstance().getProcessoTrf().getProcesso().getNumeroProcesso().equals("")){
				getProcessoAudienciaCadAudSuggestBean().setInstance(getInstance().getProcessoTrf());
			}
			setInstance(getEntityManager().find(ProcessoAudiencia.class, id));
		}
	}
	
	/**
	 * Responsável por limpar todos os filtros de pesquisa da Pauta de Audiência
	 */
	public void limparCampos() {
		orgaoJulgador = null;
		inicializaCombos();
		ProcessoAudienciaList processoAudienciaList = ComponentUtil.getComponent(ProcessoAudienciaList.class);
		processoAudienciaList.setNomeRealizador(null);
		processoAudienciaList.setNomeConciliador(null);
		setTipoAudiencia(new TipoAudiencia());
		processoAudienciaList.getEntity().setSalaAudiencia(null);
		processoAudienciaList.setParte(null);
		processoAudienciaList.setNomeAdvogado(null);
		ComponentUtil.getComponent(ClasseJudicialTreeHandler.class).clearTree();
		ComponentUtil.getComponent(AssuntoTrfTreeHandler.class).clearTree();
		processoAudienciaList.setNumeroSequencia(null);
		processoAudienciaList.setNumeroDigitoVerificador(null);
		processoAudienciaList.setAno(null);
		processoAudienciaList.setNumeroOrigemProcesso(null);
		if(statusAudienciaList != null)statusAudienciaList.clear();
	}

	@Override
	public void newInstance(){
		super.newInstance();
		processoAudiencia = null;
		getInstance().setStatusAudiencia(StatusAudienciaEnum.F);
		getInstance().setInAcordo(Boolean.FALSE);

		OrgaoJulgador orgaoJulgador = ProcessoTrfHome.instance().getInstance().getOrgaoJulgador();
		if (orgaoJulgador != null && orgaoJulgador.getTempoAudiencia() != null){
			setTempoAudiencia(Integer.parseInt(orgaoJulgador.getTempoAudiencia()));
		}
		setBuscarHorariosReservados(Boolean.FALSE);
		setSalaEncontrada(Boolean.FALSE);
		setEtapaAudiencia(EtapaAudienciaEnum.I);
		setAchouSalaDisponivel(Boolean.FALSE);
		setIsNewInstance(Boolean.TRUE);
		setSalaAudienciaTemp(null);
		setDtInicioPesquisa(null);
		setTempoAudiencia(null);
		setTipoAudiencia(null);
		setTipoDesignacao(ProcessoAudiencia.TIPO_DESIGNACAO_SUGERIDA);
		setHorariosEncontrados(new ArrayList<ProcessoAudiencia>(0));
		ProcessoExpedienteHome.instance().newInstance();
		contadorFluxo = 0;
		refreshGrid("processoConsultaAudiencia2Grid");
		getHorariosEncontrados().clear();
	}

	public static ProcessoAudienciaHome instance(){
		return ComponentUtil.getComponent(ProcessoAudienciaHome.class);
	}

	public void limparAbas(){
		setRendDocumento(Boolean.FALSE);
		setRendTestemunha(Boolean.FALSE);
		setStatusAudiencia(null);
		setPessoaConciliador(null);
		setInAcordo(true);
		setValorAcordo(null);
	}

	/**
	 * Método que retorna o nome do dia da semana de acordo com o número correspondente
	 * 
	 * @param numDia
	 * @return String
	 */
	public String diaSemana(int numDia){
		String resultado = null;
		try {
			resultado = new DateFormatSymbols(new Locale("pt", "BR")).getWeekdays()[numDia];
		} catch (ArrayIndexOutOfBoundsException ex) {
			// Nothing to do.
		}
		return resultado;
	}

	private String diaAudienciaString(){
		final Calendar dataAtual = Calendar.getInstance();
		dataAtual.setTime(getInstance().getDtInicio());
		String dia = diaSemana(dataAtual.get(Calendar.DAY_OF_WEEK));
		return dia;
	}

	private SalaHorario getSalaLiberada(){
		String query = "SELECT s FROM SalaHorario AS s WHERE " +
				"	s.ativo = true " +
				"	AND s.sala.idSala = :idSala " +
				"	AND s.diaSemana.idDiaSemana = :diaSemana " +
				"	AND s.sala NOT IN " +
				"		(SELECT pa.salaAudiencia FROM ProcessoAudiencia AS pa WHERE " +
				"			(pa.statusAudiencia = 'M') AND " +
				"			((pa.dtInicio < :dataIni AND pa.dtFim >= :dataFim) " +
				"			OR (pa.dtInicio < :dataFim and pa.dtFim >  :dataIni) " +
				"			OR (pa.dtInicio <= :dataIni and pa.dtFim >= :dataFim) " +
				"			OR (pa.dtInicio >= :dataFim and pa.dtFim <= :dataIni)" +
				"			)" +
				"		)";			

		Query q = getEntityManager().createQuery(query.toString());
		q.setParameter("dataIni", getInstance().getDtInicio());
		q.setParameter("dataFim", getInstance().getDtFim());
		q.setParameter("idSala", getInstance().getSalaAudiencia().getIdSala());

		Calendar dataInicio = Calendar.getInstance();
		dataInicio.setTime(getInstance().getDtInicio());
		q.setParameter("diaSemana", dataInicio.get(Calendar.DAY_OF_YEAR) + 1);

		q.setMaxResults(1);
		return (SalaHorario) EntityUtil.getSingleResult(q);
	}

	public void carregarDadoSala(){
		if (getInstance().getDtInicio() != null && getInstance().getSalaAudienciaTemp() != null){
			getInstance().setProcessoTrf(ProcessoTrfHome.instance().getInstance());
			tempoAudiencia = Integer.parseInt(ProcessoTrfHome.instance().getInstance().getOrgaoJulgador().getTempoAudiencia());
			Sala salaAudienciaEncontrada = getSalaLiberada().getSala();
			if (salaAudienciaEncontrada != null){
				setSalaEncontrada(Boolean.TRUE);
				getInstance().setSalaAudiencia(salaAudienciaEncontrada);
			}
			else{
				setSalaEncontrada(Boolean.FALSE);
			}
		}
	}

	public void setarAbaExpediente(){
		ProcessoTrfHome.instance().setTab("processoExpedienteTab");
	}

	public void alterarAbaRemarcarAudiencia(ProcessoAudiencia obj){
		setInstance(obj);
		setMostrarDivMarcarAudiencia(Boolean.TRUE);
	}

	public Boolean getAbaRemarcarAudiencia(){
		return this.abaRemarcarAudiencia;
	}

	public void setAbaRemarcarAudiencia(Boolean abaRemarcarAudiencia){
		this.abaRemarcarAudiencia = abaRemarcarAudiencia;
	}

	private ProcessoAudienciaCadAudSuggestBean getProcessoAudienciaCadAudSuggestBean(){
		ProcessoAudienciaCadAudSuggestBean processoAudienciaCadAudSuggest = (ProcessoAudienciaCadAudSuggestBean) Component
				.getInstance("processoAudienciaCadAudSuggest");
		return processoAudienciaCadAudSuggest;
	}

	public String dataInicioFormatada(){
		if (getInstance().getDtInicio() != null && getInstance().getSalaAudiencia() != null){
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			return format.format(getInstance().getDtInicio());
		}
		return "";
	}

	public String dataFimFormatada(){
		if (getInstance().getDtFim() != null && getInstance().getSalaAudiencia() != null){
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			return format.format(getInstance().getDtFim());
		}
		return "";
	}

	public String diaSemanaFormatado() {
		String dia = "";
		if (getInstance().getSalaAudiencia() != null) {
			dia = diaSemana(dtIni.get(Calendar.DAY_OF_WEEK));
		}
		return dia;
	}

	public Boolean getSalaEncontrada(){
		return salaEncontrada;
	}

	public void setSalaEncontrada(Boolean salaEncontrada){
		this.salaEncontrada = salaEncontrada;
	}

	public void recarregarGrid(){
		refreshGrid("processoConsultaAudienciaGrid");
	}

	public String updatePersist(){
		getInstance().setDtInicio(dtIni.getTime());
		getInstance().setDtFim(dtFim.getTime());
		if (getSalaLiberada() != null){
			setSalaEncontrada(Boolean.FALSE);
			Date dataAtual = new Date();
			
			processoAudiencia.setStatusAudiencia(StatusAudienciaEnum.R);
			processoAudiencia.setDtRemarcacao(dataAtual);
			tempoAudiencia = Integer.parseInt(ProcessoTrfHome.instance().getInstance().getOrgaoJulgador().getTempoAudiencia());

			EntityManager em = getEntityManager();
			em.merge(processoAudiencia);

			getInstance().setProcessoAudienciaPai(processoAudiencia);
			getInstance().setStatusAudiencia(StatusAudienciaEnum.M);
			getInstance().setDtMarcacao(dataAtual);
			em.persist(getInstance());
			try{
				em.flush();
			} catch (AssertionFailure e){
				logger.warning(e.getMessage());
			}
			newInstance();
			setMostrarDivMarcarAudiencia(Boolean.FALSE);
			ProcessoTrfHome.instance().setTab("processoExpedienteTab");
			ProcessoExpedienteHome.instance().iniciarCadastro();
		}
		else{
			setSalaEncontrada(Boolean.TRUE);
		}
		refreshGrid("processoConsultaAudiencia2Grid");

		return "";
	}

	public void addSalaProcessoAudiencia(Sala obj, String gridId){
		if (getInstance() != null){
			getInstance().setSalaAudiencia(obj);
			refreshGrid(gridId);
		}
	}

	public Boolean getMostrarDiv(){
		return mostrarDiv;
	}

	public void setMostrarDiv(Boolean mostrarDiv){
		this.mostrarDiv = mostrarDiv;
	}

	public void showDiv(){
		setMostrarDiv(Boolean.TRUE);
	}

	public void hideDiv(){
		setMostrarDiv(Boolean.FALSE);
	}

	public Boolean getAbaRealizarAudiencia(){
		return abaRealizarAudiencia;
	}

	public void setAbaRealizarAudiencia(Boolean AbaRealizarAudiencia){
		this.abaRealizarAudiencia = AbaRealizarAudiencia;
	}

	public void alteraAbaRealizarAudiencia(ProcessoAudiencia obj){
		setInstance(obj);
		setAbaRealizarAudiencia(Boolean.TRUE);
		ComponentUtil.getProcessoTrfHome().setTab("processoAudienciaTab");
	}

	public Boolean getAbaDocumentoRealizarAudiencia(){
		return abaDocumentoRealizarAudiencia;
	}

	public void setAbaDocumentoRealizarAudiencia(Boolean abaDocumentoRealizarAudiencia){
		this.abaDocumentoRealizarAudiencia = abaDocumentoRealizarAudiencia;
	}

	public Boolean getAbaCadastroTestemunha(){
		return abaCadastroTestemunha;
	}

	public void setAbaCadastroTestemunha(Boolean abaCadastroTestemunha){
		this.abaCadastroTestemunha = abaCadastroTestemunha;
	}

	public void alteraAbaCadastroTestemunha(){
		setAbaCadastroTestemunha(true);
		ComponentUtil.getProcessoTrfHome().setTab("cadastroTestemunhaTab");
	}

	public void alteraAbaDocumentoRealizarAudiencia(){
		setAbaDocumentoRealizarAudiencia(Boolean.TRUE);
		ComponentUtil.getProcessoTrfHome().setTab("cadastroTestemunhaTab");
	}

	public void alteraAbaDocumentoProcessoAudiencia(){
		setAbaDocumentoProcessoAudiencia(Boolean.TRUE);
		ComponentUtil.getProcessoTrfHome().setTab("documentoProcessoAudienciaTab");
	}

	public void setMostrarDivCancelamento(Boolean mostrarDivCancelamento, ProcessoAudiencia obj){
		setInstance(obj);
		this.mostrarDivCancelamento = mostrarDivCancelamento;
	}

	public Boolean getMostrarDivCancelamento(){
		return mostrarDivCancelamento;
	}

	public Boolean getAbaDocumentoProcessoAudiencia(){
		return abaDocumentoProcessoAudiencia;
	}

	public void setAbaDocumentoProcessoAudiencia(Boolean abaDocumentoProcessoAudiencia){
		this.abaDocumentoProcessoAudiencia = abaDocumentoProcessoAudiencia;
	}

	public void remarcacaoAudiencia(Boolean mostrarDivRemarcacao, ProcessoAudiencia obj){
		newInstance();
		setProcessoAudiencia(obj);
		setMostrarDivMarcarAudiencia(mostrarDivRemarcacao);
		try{
			setInstance(EntityUtil.cloneEntity(getProcessoAudiencia(), Boolean.FALSE));
		} catch (Exception e){
			logger.warning(e.getMessage());
		}
	}

	public void cancelarRemarcacao(){
		this.mostrarDivRemarcacao = Boolean.FALSE;
		setInstance(null);
	}

	public void setMostrarDivRemarcacao(Boolean mostrarDivRemarcacao){
		this.mostrarDivRemarcacao = mostrarDivRemarcacao;
	}

	public Boolean getMostrarDivRemarcacao(){
		return mostrarDivRemarcacao;
	}

	public void setProcessoAudiencia(ProcessoAudiencia processoAudiencia){
		this.processoAudiencia = processoAudiencia;
	}

	public ProcessoAudiencia getProcessoAudiencia(){
		return processoAudiencia;
	}

	public String tempoAudiencia(){
		if (getInstance().getDtFim() != null && getInstance().getDtInicio() != null){
			Long duracao = getInstance().getDtFim().getTime() - getInstance().getDtInicio().getTime();
			return Long.toString((duracao / 1000 / 60));
		}
		return "";
	}

	private Boolean mostrarDivMarcarAudiencia = Boolean.FALSE;
	private List<ProcessoParte> partesNaoIntimadas = new ArrayList<>();

	public void setMostrarDivMarcarAudiencia(Boolean mostrarDivMarcarAudiencia){
		this.mostrarDivMarcarAudiencia = mostrarDivMarcarAudiencia;
	}

	public Boolean getMostrarDivMarcarAudiencia(){
		return mostrarDivMarcarAudiencia;
	}

	public void alterarMostrarDivMarcarAudiencia(){
		if (getMostrarDivMarcarAudiencia()){
			setMostrarDivMarcarAudiencia(Boolean.FALSE);
		}
		else{
			newInstance();
			setMostrarDivMarcarAudiencia(Boolean.TRUE);
		}
	}

	public String poloAtivo(){
		if (!ProcessoTrfHome.instance().getInstance().getListaParteAtivo().isEmpty()){
			return ProcessoTrfHome.instance().getInstance().getListaParteAtivo().get(0).toString();
		}
		else{
			return "";
		}
	}

	public String poloPassivo(){
		if (!ProcessoTrfHome.instance().getInstance().getListaPartePassivo().isEmpty()){
			return ProcessoTrfHome.instance().getInstance().getListaPartePassivo().get(0).toString();
		}
		else{
			return "";
		}
	}

	/**
	 * método que escreve na coluna a data antiga da audiência
	 * 
	 * @param idPAFather
	 * @return
	 */
	public String dataRemarcacao(Integer idPAFather){
		if (idPAFather != null){
			String query = "select o from ProcessoAudiencia o where " + "o.idProcessoAudiencia = :idPAFather";

			Query q = getEntityManager().createQuery(query);

			q.setParameter("idPAFather", idPAFather);

			ProcessoAudiencia pa = (ProcessoAudiencia) EntityUtil.getSingleResult(q);
			if (pa != null) {
				return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(pa.getDtInicio());
			} else {
				return "";
			}
		}
		else{
			return "";
		}
	}

	public void setAchouSalaDisponivel(Boolean achouSalaDisponivel){
		this.achouSalaDisponivel = achouSalaDisponivel;
	}

	public Boolean getAchouSalaDisponivel(){
		return achouSalaDisponivel;
	}

	/**
	 * metodo para popular combo que foi desativado por enquanto
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Sala> salasOJ(){
		String query = "select s from Sala s where s.orgaoJulgador.idOrgaoJulgador= :idOJ order by s.salaAudiencia";

		Query q = getEntityManager().createQuery(query);
		q.setParameter("idOJ", ProcessoTrfHome.instance().getInstance().getOrgaoJulgador().getIdOrgaoJulgador());

		List<Sala> lista = q.getResultList();

		return lista;
	}

	public String getProcessoPorExtenso(ProcessoAudiencia obj){
		String output = obj.getProcessoTrf()
			+ "<br/>"
			+ obj.getProcessoTrf().getClasseJudicial()
			+ "<br/>"
			+ obj.getProcessoTrf().getAssuntoTrfList();
		return output;
	}

	public String getStatusAudiencia(String status){
		if (status.equals("M")){
			return "Designada";
		}
		else if (status.equals("C")){
			return "Cancelada";
		}
		else if (status.equals("D")){
			return "Convertida em Diligência";
		}
		else if (status.equals("R")){
			return "Redesignada";
		}
		else if (status.equals("F")){
			return "Realizada";
		}
		else if (status.equals("A")){
			return "Antecipada";
		}
		else if (status.equals("N")){
			return "Não Realizada";
		}
		else{
			return "---";
		}
	}

	public Date getDataAudiencia(ProcessoAudiencia obj){
		if (obj.getDtCancelamento() != null){
			return obj.getDtCancelamento();
		}
		if (obj.getDtRemarcacao() != null){
			return obj.getDtRemarcacao();
		}
		if (obj.getDtMarcacao() != null){
			return obj.getDtMarcacao();
		}
		return obj.getDtInicio();
	}

	public String getPartesSeparadas(ProcessoAudiencia obj){
		ProcessoParteManager processoParteManager = ComponentUtil.getComponent(ProcessoParteManager.class);
		StringBuilder output = new StringBuilder();
		output.append(processoParteManager.nomeExibicao(obj.getProcessoTrf(), ProcessoParteParticipacaoEnum.A))
			.append("<br/> X <br/>")
			.append(processoParteManager.nomeExibicao(obj.getProcessoTrf(), ProcessoParteParticipacaoEnum.P));
		return output.toString();
	}

	public void setSalaIndisponivelEmQualquerDia(Boolean salaIndisponivelEmQualquerDia){
		this.salaIndisponivelEmQualquerDia = salaIndisponivelEmQualquerDia;
	}

	public Boolean getSalaIndisponivelEmQualquerDia(){
		return salaIndisponivelEmQualquerDia;
	}

	public void setSemSalaParaAudiencia(Boolean semSalaParaAudiencia){
		this.semSalaParaAudiencia = semSalaParaAudiencia;
	}

	public Boolean getSemSalaParaAudiencia(){
		return semSalaParaAudiencia;
	}

	public String refreshNaGrid(){
		refreshGrid("processoConsultaAudienciaGrid");
		return "";
	}

	public void refreshGridAtivoPassivo(){
		refreshGrid("processeParteAtivoTestemunhaGrid");
		refreshGrid("processePartePassivoTestemunhaGrid");
	}

	@SuppressWarnings("unchecked")
	public String numeroParticipacoes(ProcessoAudienciaPessoa pessoa){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoAudienciaPessoa o where ");
		sb.append("o.testemunha = true and o.pessoa = :idPessoa");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idPessoa", pessoa.getPessoa());

		List<ProcessoAudienciaPessoa> lista = q.getResultList();

		return String.valueOf(lista.size());
	}

	public StatusAudienciaEnum[] getStatusAudienciaEnumValues(){
		return StatusAudienciaEnum.values();
	}
	
	public List<StatusAudienciaEnum> getStatusAudienciaList(){
		return statusAudienciaList;
	}
	
	public void setStatusAudienciaList(List<String> statusAudienciaList) {
		List<StatusAudienciaEnum> lista = new ArrayList<StatusAudienciaEnum>();
		for (String str : statusAudienciaList) {
			lista.add(StatusAudienciaEnum.valueOf(str));
		}
		this.statusAudienciaList = lista;
	}

	public Boolean getHoraInvalida(){
		return horaInvalida;
	}

	public void setHoraInvalida(Boolean horaInvalida){
		this.horaInvalida = horaInvalida;
	}

	public StatusAudienciaEnum getStatusAudiencia(){
		return statusAudiencia;
	}

	public void setStatusAudiencia(StatusAudienciaEnum statusAudiencia){
		this.statusAudiencia = statusAudiencia;
	}

	public Pessoa getPessoaConciliador(){
		return pessoaConciliador;
	}

	public void setPessoaConciliador(Pessoa pessoaConciliador){
		this.pessoaConciliador = pessoaConciliador;
	}

	public Boolean getInAcordo(){
		return inAcordo;
	}

	public void setInAcordo(Boolean inAcordo){
		this.inAcordo = inAcordo;
	}

	public Double getValorAcordo(){
		return valorAcordo;
	}

	public void setValorAcordo(Double valorAcordo){
		this.valorAcordo = valorAcordo;
	}

	@Override
	public String persist(){
		String ret = null;
		getInstance().setDiaSearch(diaAudienciaString());
		getInstance().setDtInicio(dtIni.getTime());
		if (dtFim != null){
			getInstance().setDtFim(dtFim.getTime());
		}
		getInstance().setProcessoTrf(ProcessoTrfHome.instance().getInstance());

		if (getSalaLiberada() != null){
			setSalaEncontrada(Boolean.FALSE);
			Pessoa pessoaLogada = (Pessoa) Contexts.getSessionContext().get("pessoaLogada");
			instance.setPessoaMarcador(pessoaLogada);
			instance.setDtMarcacao(new Date());
			instance.setInAtivo(Boolean.TRUE);
			instance.setStatusAudiencia(StatusAudienciaEnum.M);
			ret = super.update();
			setSemSalaParaAudiencia(Boolean.FALSE);
			setAchouSalaDisponivel(Boolean.FALSE);
			ProcessoExpedienteHome.instance().iniciarCadastro();
			ProcessoTrfHome.instance().setTab("processoExpedienteTab");
		}
		else{
			setSemSalaParaAudiencia(Boolean.TRUE);
			setAchouSalaDisponivel(Boolean.TRUE);
		}
		FacesMessages.instance().clear();
		refreshGrid("processoConsultaAudiencia2Grid");
		return ret;
	}

	public void novaAudienciaFluxo(){
		ProcessoExpedienteHome.instance().setVisualizarAbas(Boolean.FALSE);
		newInstance();
	}

	/**
	 * Método utilizado para concluir a realização de uma audiência
	 */
	public void concluirAudiencia(){
		ProcessoAudiencia audiencia = instance;
		if (AtaAudienciaAction.instance().getAtaAudiencia() == null) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Audiência não finalizada.");
		} else {
			audiencia.setProcessoDocumento(AtaAudienciaAction.instance().getAtaAudiencia());
			registrarMovimentoAudiencia(audiencia);
			newInstance();
			setEtapaAudiencia(EtapaAudienciaEnum.M);
			AtaAudienciaAction.instance().limparTela();
			
			movimentarProcesso(Variaveis.PJE_FLUXO_AUDIENCIA_AGUARDA_REALIZACAO, audiencia);
			
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Audiência finalizada com sucesso.");
		}
	}

	/**
	 * Procura pelo primeiro horário disponível para audiência de cada sala selecionada
	 * 
	 * @return
	 */
	public boolean salasDisponiveisAudiencia() {
		if (tempoAudiencia <= 0) {
			FacesMessages.instance().add(Severity.ERROR, "Tempo de audiência inválido.");
			return false;
		}
		
		ProcessoTrf processoTrf = new ProcessoTrf();
		if (selecionarOrgaoJulgador) {
			processoTrf.setOrgaoJulgador(orgaoJulgador);
		}

		audienciasEmConflito = null;
		if (tipoDesignacao.equals(ProcessoAudiencia.TIPO_DESIGNACAO_SUGERIDA)) {
			try {
				horariosEncontrados = ComponentUtil.getProcessoAudienciaManager().obterHorariosLivres(
				processoTrf.getOrgaoJulgador() != null ? processoTrf : ProcessoTrfHome.instance().getInstance(), 
				tipoAudiencia, salaAudienciaTemp, dtInicioPesquisa,buscarHorariosReservados,tempoAudiencia,etapaAudiencia);
				
				return true;
			} catch (PJeBusinessException ex) {
				FacesMessages.instance().add(Severity.ERROR, ex.getLocalizedMessage());
				return false;
			}
		} else {
			if (etapaAudiencia.equals(EtapaAudienciaEnum.M)) {
				getInstance().setDtInicio(dtInicioPesquisa);
				getInstance().setDtFim(DateUtil.adicionarTempoData(dtInicioPesquisa, Calendar.MINUTE, tempoAudiencia));
				getInstance().setSalaAudiencia(salaAudienciaTemp);
				marcarAudiencia(getInstance(), false);
			} else {
				ProcessoAudiencia novaAudiencia = new ProcessoAudiencia();
				novaAudiencia.setDtInicio(dtInicioPesquisa);
				novaAudiencia.setDtFim(DateUtil.adicionarTempoData(dtInicioPesquisa, Calendar.MINUTE, tempoAudiencia));
				novaAudiencia.setSalaAudiencia(salaAudienciaTemp);
				novaAudiencia.setTipoDesignacao(tipoDesignacao);
				remarcarAudiencia(novaAudiencia, false);
			}
			return true;
		}
	}
	
	public List<PessoaFisica> getRealizadoresOrgaoEOrgaoDeslocado() throws PJeBusinessException {
        return getRealizadores(true);
	}

	public List<PessoaFisica> getRealizadores() throws PJeBusinessException {
        return getRealizadores(false);
	}


	public boolean confirmarMarcacao(){
		confirmaMarcacaoManual = Boolean.TRUE;
		observacoesConfirmacao.clear();
		return salasDisponiveisAudiencia();
	}

	/**
	 * método chamado quando o usuário decide começar uma nova consulta e quando não é achada uma sala
	 */
	public void zerarDados(){
		horariosEncontrados = new ArrayList<ProcessoAudiencia>(0);
		setAudienciasEmConflito(null);
		tempoAudiencia = null;
		dtInicioPesquisa = null;
		salaAudienciaTemp = null;
		tipoAudiencia = null;
		etapaRealizacaoAudiencia = "I";
		buscarHorariosReservados = false;
		
		carregarTipoAudienciaPadrao();
		
		Contexts.getConversationContext().set("processoExpedienteHome", new ProcessoExpedienteHome());
		Contexts.getConversationContext().set("processoAudienciaPessoaHome", new ProcessoAudienciaPessoaHome());
		FacesMessages.instance().clear();
		observacoesConfirmacao.clear();
	}

	private void carregarTipoAudienciaPadrao(){
		StringBuilder sb = new StringBuilder();
		sb.append("select o.tipoAudiencia from OjClasseTipoAudiencia o ");
		sb.append("where o.classeJudicial=(select o1.classeJudicial from ProcessoTrf o1 where o1=#{processoTrfHome.instance}) and ");
		sb.append("o.orgaoJulgador=(select o2.orgaoJulgador from ProcessoTrf o2 where o2=#{processoTrfHome.instance}) and ");
		sb.append("(o.dtFim = null or o.dtFim >= now()) ");
		Query q = getEntityManager().createQuery(sb.toString());
		tipoAudienciaPadrao = EntityUtil.getSingleResult(q);
		tipoAudiencia = EntityUtil.getSingleResult(q);
		atualizaTipoAudiencia();
	}

	public StatusAudienciaEnum[] statusAudienciaValues(){
		return StatusAudienciaEnum.values();
	}

	/**
	 * Controla a gravação de audiências de forma sincronizada, verificando se a audiência já não foi gravada por outra pessoa.
	 * 
	 * @param audiencia
	 * @param ignorarConflitos Indica se possíveis conflitos de horário com outras audiências devem ser ignorados
	 */
	private boolean gravarAudiencia(ProcessoTrf processoTrf, ProcessoAudiencia audiencia, boolean ignorarConflitos){
		//Realiza o lock da sala para garantir que outra transação não agende uma audiência no mesmo horário da sala
		EntityUtil.getEntityManager().lock(audiencia.getSalaAudiencia(), LockModeType.PESSIMISTIC_WRITE);
		EntityUtil.getEntityManager().refresh(audiencia.getSalaAudiencia());

		if (!ignorarConflitos){
			List<ProcessoAudiencia> audienciasEmConflito = getAudienciasEntreDatas(audiencia);
			
			audienciasEmConflito.remove(getInstance());
			
			if (audienciasEmConflito.size() > 0){
				this.setAudienciasEmConflito(audienciasEmConflito);
				return false;
			}
		}
		processoTrf.getProcessoAudienciaList().add(audiencia);
		audiencia.setProcessoTrf(processoTrf);
		audiencia.setTipoAudiencia(tipoAudiencia);
		audiencia.setInAtivo(true);
		audiencia.setPessoaMarcador(Authenticator.getPessoaLogada());
		audiencia.setDtMarcacao(new Date());
		audiencia.setTipoDesignacao(tipoDesignacao);

		try{
			if (EtapaAudienciaEnum.L.equals(etapaAudiencia)){
				getEntityManager().persist(getInstance()); // Se estiver remarcando uma audiência, grava a audiência antiga também
			}
			getEntityManager().persist(audiencia);
			getEntityManager().merge(processoTrf);
			getEntityManager().flush();
			
		} catch (Exception e){
			e.printStackTrace();
		}
		return true;
	}

	public void anexarDoc(){
		ProcessoDocumentoHome pdh = ProcessoDocumentoHome.instance();
		if (pdh.getModelo() && getTermoAudiencia()){
			setProcessoDocumento(pdh.getInstance());
			setTermoAudiencia(Boolean.FALSE);
		}
		pdh.persist();
	}

	/**
	 * Atualiza a pauta de audiência de acordo com a sala desejada
	 * 
	 * @param s
	 */
	@SuppressWarnings("unchecked")
	public void atualizarPautaAudiencia(Sala s, Date data){
		pautaAudienciaSala = getEntityManager()
				.createQuery(
						"FROM ProcessoAudiencia p where p.salaAudiencia = :sala "
							+ "AND (p.dtInicio between :dtInicio and :dtFim) "
							+ "order by p.dtInicio")
				.setParameter("sala", s)
				.setParameter("dtInicio", DateUtil.getBeginningOfDay(data))
				.setParameter("dtFim", DateUtil.getEndOfDay(data))
				.getResultList();
	}

	/**
	 * Criado por rodrigo_cnj Este método recupera o tempo de audiencia relacionado com orgaoJulgador e o tipo de audiencia.
	 * 
	 * @param tipoAudiencia
	 */
	public void atualizarTempoAudienciaPadrao(){
        if (selecionarOrgaoJulgador && orgaoJulgador != null) {
            this.tempoAudiencia = getTempoAudienciaPadraoOrgaoJulgador(orgaoJulgador, getTipoAudiencia());
        }
        else {
            this.tempoAudiencia = getTempoAudienciaPadraoOrgaoJulgador(ProcessoTrfHome.instance().getInstance().getOrgaoJulgador(), getTipoAudiencia());
        }
	}

	/**
	 * Este método serve tanto para cancelar uma audiência quanto para convertê-la em diligência, dependendo do que está setado no atributo
	 * etapaAudiencia
	 */
	public void cancelarAudiencia(){
		EtapaAudienciaEnum etapa = etapaAudiencia;
		Pessoa p = Authenticator.getPessoaLogada();
		ProcessoAudiencia audiencia = getInstance();
		audiencia.setPessoaCancelamento(p);
		audiencia.setDtCancelamento(new Date());
		if (etapaAudiencia.equals(EtapaAudienciaEnum.C)){
			audiencia.setStatusAudiencia(StatusAudienciaEnum.C);
		}
		else if (etapaAudiencia.equals(EtapaAudienciaEnum.D)){
			audiencia.setStatusAudiencia(StatusAudienciaEnum.D);
		}
		super.persist();
		registrarMovimentoAudiencia();
		setEtapaAudiencia(EtapaAudienciaEnum.M);
		
		if (etapa.equals(EtapaAudienciaEnum.C)) {
			movimentarProcesso(Variaveis.PJE_FLUXO_AUDIENCIA_AGUARDA_CANCELAMENTO, audiencia);
		} else if (etapa.equals(EtapaAudienciaEnum.D)) {
			movimentarProcesso(Variaveis.PJE_FLUXO_AUDIENCIA_AGUARDA_CONVERSAO_DILIGENCIA, audiencia);
		}
		
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Dados gravados com sucesso");
	}

	/**
	 * Retorna uma lista de audiências entre as datas informadas
	 * 
	 * @param filtro
	 * @param orderBy
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoAudiencia> getAudienciasEntreDatas(ProcessoAudiencia filtro){
		Query q = getEntityManager()
				.createQuery(
						"FROM ProcessoAudiencia p "
							+ "WHERE ((:dtInicio >= p.dtInicio AND :dtInicio < p.dtFim) "
							+ "OR 	 (:dtFim > p.dtInicio AND :dtFim <= p.dtFim) "
							+ "OR 	 (:dtInicio <= p.dtInicio AND :dtFim >= p.dtFim)) "
							+ "AND	 (p.salaAudiencia = :sala) "
							+ "AND	 (p.statusAudiencia = 'M') "
							+ (filtro.getIdProcessoAudiencia() == 0 ? " " : "AND (p.idProcessoAudiencia != :idProcessoAudiencia) "))
				.setParameter("dtInicio", filtro.getDtInicio())
				.setParameter("dtFim", filtro.getDtFim())
				.setParameter("sala", filtro.getSalaAudiencia());

		if (filtro.getIdProcessoAudiencia() != 0){
			q.setParameter("idProcessoAudiencia", filtro.getIdProcessoAudiencia());
		}		
		return q.getResultList();
	}

	/**
	 * Retorna a lista de possíveis conciliadores, utilizada na realização da audiência
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<PessoaFisica> getConciliadores(){
		if (conciliadores == null){
			StringBuilder hql = new StringBuilder();
			hql.append("select distinct o from PessoaFisica o inner join o.usuarioLocalizacaoList uL "
				+ "where (uL.papel.identificador  = 'conciliador') "
				+ "and (uL.localizacaoFisica = :localizacao ");

			List<Localizacao> localizacoesPai = getLocalizacoesPai(ProcessoTrfHome.instance().getInstance().getOrgaoJulgador().getLocalizacao());
			List<Localizacao> localizacoesFilhas = getLocalizacoesFilhas(ProcessoTrfHome.instance().getInstance().getOrgaoJulgador().getLocalizacao());
			if (localizacoesPai.size() > 0){
				hql.append("or uL.localizacaoFisica in (:localizacoesPai) ");
			}
			if (localizacoesFilhas.size() > 0){
				hql.append("or (uL.localizacaoFisica in (:localizacoesFilhas)) ");
			}
			hql.append(")");

			Query q = getEntityManager().createQuery(hql.toString());

			if (localizacoesPai.size() > 0){
				q.setParameter("localizacoesPai", localizacoesPai);
			}
			if (localizacoesFilhas.size() > 0){
				q.setParameter("localizacoesFilhas", localizacoesFilhas);
			}

			q.setParameter("localizacao", ProcessoTrfHome.instance().getInstance().getOrgaoJulgador().getLocalizacao());
			setConciliadores(q.getResultList());
		}

		return conciliadores;
	}

	@SuppressWarnings("unchecked")
	public List<PessoaFisica> getRealizadores(boolean isIncluirOrgaoDeslocado) throws PJeBusinessException {
		if (realizadores == null){
			StringBuilder hql = new StringBuilder();
			hql.append("select distinct o from PessoaFisica o inner join o.usuarioLocalizacaoList uL "
				+ "where (uL.papel.identificador  = 'conciliador' or uL.papel.identificador = 'magistrado' "
				+ "or uL.papel.identificador = 'juizLeigo' or uL.papel.identificador  = 'mediador') "
				+ "and (uL.localizacaoFisica = :localizacao ");

			List<Localizacao> localizacoesPai = getLocalizacoesPai(ProcessoTrfHome.instance().getInstance().getOrgaoJulgador().getLocalizacao());
			List<Localizacao> localizacoesFilhas = getLocalizacoesFilhas(ProcessoTrfHome.instance().getInstance().getOrgaoJulgador().getLocalizacao().getEstruturaFilho());

			List<Localizacao> localizacoesPaiDeslocado =  null;
			List<Localizacao> localizacoesFilhasDeslocado = null;
			OrgaoJulgador orgaoJulgadorDeslocado = null;

			if (isIncluirOrgaoDeslocado) {
				ProcessoInstanceManager processoInstanceManager = (ProcessoInstanceManager) Component.getInstance(ProcessoInstanceManager.class);
				org.jbpm.graph.exe.ProcessInstance processInstance = TaskInstanceUtil.instance().getProcessInstance();
				Integer idLocalizacao = processoInstanceManager.findById(processInstance.getId()).getIdLocalizacao();

				if (idLocalizacao != null) {
					LocalizacaoManager localizacaoManager = (LocalizacaoManager) Component.getInstance(LocalizacaoManager.class);
					Localizacao localizacao = localizacaoManager.findById(idLocalizacao);

					if (localizacao != null) {
						OrgaoJulgadorManager orgaoJulgadorManager = (OrgaoJulgadorManager) Component.getInstance(OrgaoJulgadorManager.class);
						orgaoJulgadorDeslocado = orgaoJulgadorManager.getOrgaoJulgadorByLocalizacao(localizacao);

						if (orgaoJulgadorDeslocado != null) {
							localizacoesPaiDeslocado = getLocalizacoesPai(orgaoJulgadorDeslocado.getLocalizacao());
							localizacoesFilhasDeslocado = getLocalizacoesFilhas(orgaoJulgadorDeslocado.getLocalizacao().getEstruturaFilho());
							hql.append("or uL.localizacaoFisica = :localizacaoDeslocado ");
						}

						if (localizacoesPaiDeslocado != null && localizacoesPaiDeslocado.size() > 0) {
							hql.append("or uL.localizacaoFisica in (:localizacoesPaiDeslocado) ");
						}

						if (localizacoesFilhasDeslocado != null && localizacoesFilhasDeslocado.size() > 0) {
							hql.append(	"or (uL.localizacaoFisica in (:localizacoesFilhasDeslocado)) ");
						}
					}
				}
			}
			if (localizacoesPai.size() > 0){
				hql.append("or uL.localizacaoFisica in (:localizacoesPai) ");
			}
			if (localizacoesFilhas.size() > 0){
				hql.append("or (uL.localizacaoFisica in (:localizacoesFilhas)) ");
			}

			hql.append(")");

			Query q = getEntityManager().createQuery(hql.toString());

			if (localizacoesPai.size() > 0){
				q.setParameter("localizacoesPai", localizacoesPai);
			}
			if (localizacoesFilhas.size() > 0){
				q.setParameter("localizacoesFilhas", localizacoesFilhas);
			}

			if (isIncluirOrgaoDeslocado) {
				if (orgaoJulgadorDeslocado != null) {
					q.setParameter("localizacaoDeslocado", orgaoJulgadorDeslocado.getLocalizacao());
				}

				if (localizacoesPaiDeslocado != null && localizacoesPaiDeslocado.size() > 0) {
					q.setParameter("localizacoesPaiDeslocado", localizacoesPaiDeslocado);
				}

				if (localizacoesFilhasDeslocado != null && localizacoesFilhasDeslocado.size() > 0) {
					q.setParameter("localizacoesFilhasDeslocado", localizacoesFilhasDeslocado);
				}
			}

			q.setParameter("localizacao", ProcessoTrfHome.instance().getInstance().getOrgaoJulgador().getLocalizacao());
			setRealizadores(q.getResultList());
		}

		return realizadores;
	}

	private List<Localizacao> getLocalizacoesPai(Localizacao loc){
		LocalizacaoManager localizacaoManager = ComponentUtil.getComponent("localizacaoManager");
		return localizacaoManager.getArvoreAscendente(loc.getIdLocalizacao(), true);
	}

	private List<Localizacao> getLocalizacoesFilhas(Localizacao loc){
		LocalizacaoManager localizacaoManager = ComponentUtil.getComponent("localizacaoManager");
		return localizacaoManager.getArvoreDescendente(loc.getIdLocalizacao(), true);
	}

	/**
	 * Retorna o tempo de audiência padrão, em minutos, para um determinado órgão julgador e tipo de audiência
	 * 
	 * @param t
	 * @param o
	 * @return
	 */
	public Integer getTempoAudienciaPadraoOrgaoJulgador(OrgaoJulgador o, TipoAudiencia t) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(TempoAudienciaOrgaoJulgador.class);
		criteria.add(Restrictions.eq("tipoAudiencia.idTipoAudiencia", t != null ? t.getIdTipoAudiencia() : 0));
		criteria.add(Restrictions.eq("orgaoJulgador.idOrgaoJulgador", o.getIdOrgaoJulgador()));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		TempoAudienciaOrgaoJulgador tempoAudienciaOrgaoJulgador = (TempoAudienciaOrgaoJulgador)criteria.uniqueResult();
		Integer tempoAudiencia = 0;
		if (tempoAudienciaOrgaoJulgador != null) {
			tempoAudiencia = tempoAudienciaOrgaoJulgador.getTempoAudiencia();
		}
		return tempoAudiencia;
	}

	/**
	 * Inicializa uma determinada etapa da audiência
	 * 
	 * @param obj
	 * @param etapa
	 */
	public void iniciarEtapaAudiencia(ProcessoAudiencia obj, EtapaAudienciaEnum etapa){
		zerarDados();
		setEtapaAudiencia(etapa);
		if (etapa == EtapaAudienciaEnum.L){
			obj.setStatusAudiencia(StatusAudienciaEnum.R);
		}
		setInstance(obj);
		if (etapa.equals(EtapaAudienciaEnum.L)){
			setTab("marcacaoAudienciaTab");
		}
		else if (etapa.equals(EtapaAudienciaEnum.C) || etapa.equals(EtapaAudienciaEnum.D) || etapa.equals(EtapaAudienciaEnum.R)){
			setTab("acoesAudienciaTab");
		}
	}

	/**
	 * Método utilizado para controlar as etapas da realização de uma audiência
	 * 
	 * @param etapa
	 */
	public void iniciarEtapaRealizacaoAudiencia(String etapa){
		setEtapaRealizacaoAudiencia(etapa);

		if (etapa.equalsIgnoreCase("T"))
			setTab("cadastroTestemunhaAba");
		else if (etapa.equalsIgnoreCase("D")){
			AtaAudienciaAction.instance().iniciar();
			setTab("anexarDocumentoAudiencia");
		}
	}

	/**
	 * Realiza a marcação de audiência
	 * 
	 * @param processoAudiencia
	 * @param ignorarConflitos Indica se possíveis conflitos de horário com outras audiências devem ser ignorados
	 */
	public void marcarAudiencia(ProcessoAudiencia audiencia, boolean ignorarConflitos){
		if (!ignorarConflitos && !confirmaMarcacaoManual){
			verificarImpedimentosData(audiencia);
			verificarImpedimentoAdvogado(audiencia);
			if (observacoesConfirmacao.size() > 0){
				return;
			}
		}

		confirmaMarcacaoManual = false;

		audiencia.setStatusAudiencia(StatusAudienciaEnum.M);
		if (gravarAudiencia(ProcessoTrfHome.instance().getInstance(), audiencia, ignorarConflitos)){
			setInstance(audiencia);
			registrarMovimentoAudiencia();
			ProcessoExpedienteHome.instance().iniciarCadastro();
			movimentarProcesso(Variaveis.PJE_FLUXO_AUDIENCIA_AGUARDA_DESIGNACAO, audiencia);
			realizarIntimacaoAutomatica(audiencia, StatusAudienciaEnum.M.getLabel());
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle("processoAudiencia.salaReservada");
		}
	}

	private void realizarIntimacaoAutomatica(ProcessoAudiencia audiencia, String statusAudiencia) {
		try {						
			if (isProcessoFromCejusc(audiencia)) {	
				String codigoFluxo = ComponentUtil.getComponent(ParametroService.class).valueOf(PARAMETRO_CODIGO_FLUXO_INTIMACAO);

				if (codigoFluxo != null && !codigoFluxo.trim().isEmpty()) {
					Fluxo fluxo = ComponentUtil.getComponent(FluxoManager.class).findByCodigo(codigoFluxo);
					if(fluxo == null) {
							throw new IllegalArgumentException("Não foi encontrado o fluxo " + codigoFluxo + " definido para gerar intimações automáticas.");
					}
					Map<String, Object> variaveis = new HashMap<String, Object>();
					variaveis.put("tjrj:fluxo:cjsIas:statusAudiencia", statusAudiencia);   	
					ProcessoJudicialService processoJudicialService = ComponentUtil.getComponent(ProcessoJudicialService.class);				
					processoJudicialService.incluirNovoFluxo(audiencia.getProcessoTrf(), codigoFluxo, variaveis);
					
				}
			}
		} catch (Exception e) {
			logger.severe(e.getMessage());
		}
	}

	private boolean isProcessoFromCejusc(ProcessoAudiencia audiencia) {
		ConsultaProcessoSimplesList consultaProcessoSimplesList = ComponentUtil.getComponent(ConsultaProcessoSimplesList.class);
		List<TaskInstance> tarefas = new ArrayList<>();
		tarefas = consultaProcessoSimplesList.getTasksAbertas(audiencia.getProcessoTrf());
		
		return !(tarefas.stream().filter(x -> x.getName().equalsIgnoreCase("Gerenciar Audiência [ACJ]")).collect(Collectors.toList()).isEmpty());
	}
	

	public void verificarImpedimentosData(ProcessoAudiencia audiencia){
		observacoesConfirmacao.clear();

		// Verifica se data estah no bloqueio de pauta
		Sala sala = audiencia.getSalaAudiencia();
		Calendar dtInicio = Calendar.getInstance();
		dtInicio.setTime(audiencia.getDtInicio());
		SalaManager salaManager = ComponentUtil.getSalaManager();
		if (!salaManager.isDataForaDeBloqueios(sala, dtInicio, tempoAudiencia)){
			observacoesConfirmacao.add("A data escolhida está bloqueada");
		}
		
		// Verifica existencia de feriado
		if (!sala.getIgnoraFeriado()){
			OrgaoJulgador orgaoJulgador = ProcessoTrfHome.instance().getInstance().getOrgaoJulgador();
			
			if (!ComponentUtil.getPrazosProcessuaisService().ehDiaUtilJudicial(
					DateUtil.getBeginningOfDay(audiencia.getDtInicio()), orgaoJulgador)){
				
				observacoesConfirmacao.add("Há feriado na data escolhida para a audiência");
			}
		}

		//Verifica se a sala está aberta no horário escolhido
		Calendar dtInicioAudiencia = Calendar.getInstance();
		dtInicioAudiencia.setTime(audiencia.getDtInicio());
		
		Calendar dtFimAudiencia = Calendar.getInstance();
		dtFimAudiencia.setTime(audiencia.getDtFim());
		boolean salaAberta = salaManager.isSalaAberta(sala, dtInicioAudiencia, dtFimAudiencia);
		
		if (!salaAberta) {
			observacoesConfirmacao.add("Sala indisponível no horário marcado.");
		}
	}
	
	/**
	 * Método responsável por verificar se existe incompatibilidade de horários na designição de audiência para advogados.
	 * 
	 * @param audiencia Audiência.
	 */
	private void verificarImpedimentoAdvogado(ProcessoAudiencia audiencia) {
		Map<ProcessoTrf, Pessoa> advogados = ComponentUtil.getProcessoAudienciaManager().getAdvogadosChoqueHorario(
			ProcessoTrfHome.instance().getInstance(), audiencia.getDtInicio(), tempoAudiencia);

		for (Map.Entry<ProcessoTrf, Pessoa> advogado : advogados.entrySet()) {
			observacoesConfirmacao.add(String.format(
				"Já existe audiência designada para essa data e horário no processo %s para o advogado %s", 
				advogado.getKey().getProcesso().getNumeroProcesso(), advogado.getValue().getNome()));
		}
	}

	/**
	 * Realiza a marcação de audiência automática
	 * 
	 * @param processoAudiencia
	 * @param ignorarConflitos Indica se possíveis conflitos de horário com outras audiências devem ser ignorados
	 */
	public ProcessoAudiencia marcarAudienciaAutomatica(OrgaoJulgador orgaoJulgador, TipoAudiencia tipoAudiencia) {
		setTipoAudiencia(tipoAudiencia);
		setTempoAudiencia(getTempoAudienciaPadraoOrgaoJulgador(orgaoJulgador, tipoAudiencia));
		
		if (ProcessoTrfHome.instance().getInstance().getViolacaoFaixaValoresCompetencia() != null || 
				ProcessoTrfHome.instance().isFaixaIncopativelAudiencia()) {
			
			if (!ProcessoTrfHome.instance().getInstance().getClasseJudicial().getDesignacaoAudienciaErroValorCausa()) {
				FacesMessages.instance().add(Severity.ERROR, "O valor da causa não permite designação automática da audiência.");
				return null;
			}
		}
		
		while (salasDisponiveisAudiencia()) {
			for (ProcessoAudiencia audiencia : horariosEncontrados) {
				audiencia.setStatusAudiencia(StatusAudienciaEnum.M);
				if (gravarAudiencia(ProcessoTrfHome.instance().getInstance(), audiencia, false)) {
					registrarMovimentoAudiencia(audiencia);
					return audiencia;
				}
			}
			ProcessoAudiencia primeiraEncontrada = horariosEncontrados.get(0);
			Date proximaData = DateUtil.adicionarTempoData(primeiraEncontrada.getDtInicio(), Calendar.DAY_OF_YEAR, 1);
			DateUtil.getBeginningOfDay(proximaData);
			this.setDtInicioPesquisa(proximaData);
		}
		return null;
	}
	
	private void registrarMovimentoAudiencia(ProcessoAudiencia audiencia) {
		try{
			ComponentUtil.getProcessoAudienciaManager().lancarMovimentoAudiencia(
					audiencia.getTipoAudiencia(),
					audiencia.getDtInicioFormatada(),
					ProcessoTrfHome.instance().getInstance().getOrgaoJulgador().getOrgaoJulgador(),
					ProcessoTrfHome.instance().getInstance().getProcesso(),
					audiencia.getProcessoDocumento(),
					audiencia.getStatusAudiencia()
					);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Lança as movimentações do agrupamento Audiência após uma alteração
	 */
	public void registrarMovimentoAudiencia(){
		registrarMovimentoAudiencia(instance.getStatusAudiencia());
	}

	/**
	 * Lança as movimentações do agrupamento Audiência com o complemento diferente da situação da audiência
	 * 
	 * @param complemento
	 */
	public void registrarMovimentoAudiencia(StatusAudienciaEnum complemento){
		try{
			if (TaskInstanceUtil.instance().getProcessInstance() != null) {
				ProcessoAudiencia audienciaSelecionada = (ProcessoAudiencia) ComponentUtil.getTramitacaoProcessualService().recuperaVariavel(Variaveis.VARIAVEL_FLUXO_AUDIENCIA_SELECIONADA);
				
				// Recuperar o identificador do documento gerado no fluxo de realização de audiência (ata de audiência)
				Integer identificadorDocumento = (Integer) ProcessInstance.instance().getContextInstance().getVariable(Variaveis.MINUTA_EM_ELABORACAO);
				
				// Se a realização da audiência foi configurada por fluxo...
				if (audienciaSelecionada != null && identificadorDocumento != null && identificadorDocumento != 0) {
					ProcessoDocumento processoDocumento = EntityUtil.find(ProcessoDocumento.class, identificadorDocumento);
					ProcessoDocumentoHome.instance().setInstance(processoDocumento);
				}
			}
			
			/**
			 * Quando for cancelamento de audiência, não vincular documento ao movimento, pois nada é produzido no cancelamento.
			 */
			if (EtapaAudienciaEnum.C.equals(etapaAudiencia)){
				ComponentUtil.getProcessoAudienciaManager().lancarMovimentoAudiencia(
						instance.getTipoAudiencia(),
						instance.getDtInicioFormatada(),
						orgaoJulgador != null ? orgaoJulgador.getOrgaoJulgador() :	ProcessoTrfHome.instance().getInstance().getOrgaoJulgador().getOrgaoJulgador(),
						ProcessoTrfHome.instance().getInstance().getProcesso(),
						null,
						complemento);					
			} else {
				ComponentUtil.getProcessoAudienciaManager().lancarMovimentoAudiencia(
						instance.getTipoAudiencia(),
						instance.getDtInicioFormatada(),
						orgaoJulgador != null ? orgaoJulgador.getOrgaoJulgador() : ProcessoTrfHome.instance().getInstance().getOrgaoJulgador().getOrgaoJulgador(),
						ProcessoTrfHome.instance().getInstance().getProcesso(),
						ProcessoDocumentoHome.instance().getInstance(),
						complemento);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Remarca uma audiência
	 * 
	 * @param audiencia
	 * @param ignoraConflitos
	 */
	public void remarcarAudiencia(ProcessoAudiencia audiencia, boolean ignorarConflitos){
		// Se audiencia vier null, é uma remarcação direto da tela, precisa ser construído o objeto
		if( audiencia == null ) {
			audiencia = new ProcessoAudiencia();
			audiencia.setDtInicio(dtInicioPesquisa);
			audiencia.setDtFim(DateUtil.adicionarTempoData(dtInicioPesquisa, Calendar.MINUTE, tempoAudiencia));
			audiencia.setSalaAudiencia(salaAudienciaTemp);
			audiencia.setTipoDesignacao(tipoDesignacao);
		}

		if (!ignorarConflitos && !confirmaMarcacaoManual){
			verificarImpedimentosData(audiencia);
			if (observacoesConfirmacao.size() > 0){
				return;
			}
		}

		confirmaMarcacaoManual = false;
		StatusAudienciaEnum complemento;
		
		audiencia.setStatusAudiencia(StatusAudienciaEnum.M);
		// Apesar da audiência ter o status "Designada", o movimento deve
		// ter o complemento como "Redesignada".
		complemento = StatusAudienciaEnum.R;
		
		if (gravarAudiencia(ProcessoTrfHome.instance().getInstance(), audiencia, ignorarConflitos)){
			setInstance(audiencia);
			registrarMovimentoAudiencia(complemento);
			ProcessoExpedienteHome.instance().iniciarCadastro();
			
			movimentarProcesso(Variaveis.PJE_FLUXO_AUDIENCIA_AGUARDA_REDESIGNACAO, audiencia);
			realizarIntimacaoAutomatica(audiencia, StatusAudienciaEnum.R.getLabel());
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle("processoAudiencia.salaReservada");
		}
	}

	public TipoAudiencia getTipoAudiencia(){
		return this.tipoAudiencia;
	}

	public EtapaAudienciaEnum getEtapaAudiencia(){
		return etapaAudiencia;
	}

	public void setEtapaAudiencia(EtapaAudienciaEnum etapaAudiencia){
		this.etapaAudiencia = etapaAudiencia;
	}

	public Integer getTempoAudiencia(){
		if (tipoAudiencia == null){
			tempoAudiencia = null;
		}
		return tempoAudiencia;
	}

	public void setTempoAudiencia(Integer tempoAudiencia){
		this.tempoAudiencia = tempoAudiencia;
	}

	public void setSalaAudienciaTemp(Sala salaAudienciaTemp){
		this.salaAudienciaTemp = salaAudienciaTemp;
	}

	public Sala getSalaAudienciaTemp(){
		return salaAudienciaTemp;
	}

	public void setHorariosEncontrados(List<ProcessoAudiencia> horariosEncontrados){
		this.horariosEncontrados = horariosEncontrados;
	}

	public List<ProcessoAudiencia> getHorariosEncontrados(){
		return horariosEncontrados;
	}

	public void setTipoAudiencia(TipoAudiencia tipoAudiencia){
		this.tipoAudiencia = tipoAudiencia;
	}

	public void setDtInicioPesquisa(Date dtInicioPesquisa){
		this.dtInicioPesquisa = dtInicioPesquisa;
	}

	public Date getDtInicioPesquisa(){
		return dtInicioPesquisa;
	}

	public void setEtapaRealizacaoAudiencia(String etapaRealizacaoAudiencia){
		this.etapaRealizacaoAudiencia = etapaRealizacaoAudiencia;
	}

	public String getEtapaRealizacaoAudiencia(){
		return etapaRealizacaoAudiencia;
	}

	public void setConciliadores(List<PessoaFisica> conciliadores){
		this.conciliadores = conciliadores;
	}

	public void setDiasDisponiveisSemana(List<SalaHorario> diasDisponiveisSemana){
		this.diasDisponiveisSemana = diasDisponiveisSemana;
	}

	public List<SalaHorario> getDiasDisponiveisSemana(){
		return diasDisponiveisSemana;
	}

	public void setDistanciaMinimaPesquisa(Integer distanciaMinimaPesquisa){
		this.distanciaMinimaPesquisa = distanciaMinimaPesquisa;
	}

	public Integer getDistanciaMinimaPesquisa(){
		return distanciaMinimaPesquisa;
	}

	public void setTiposAudiencia(List<TipoAudiencia> tiposAudiencia){
		this.tiposAudiencia = tiposAudiencia;
	}

	@SuppressWarnings("unchecked")
	public List<TipoAudiencia> getTiposAudiencia(){
		if (tiposAudiencia == null){
			tiposAudiencia = getEntityManager().createQuery("select o from TipoAudiencia o where o.ativo = true order by tipoAudiencia")
					.getResultList();
		}
		return tiposAudiencia;
	}

	public void setSalasAudiencia(List<Sala> salasAudiencia){
		this.salasAudiencia = salasAudiencia;
	}
	
	public void atualizaTipoAudiencia(){
		atualizarTempoAudienciaPadrao();
		atualizarDistanciaMinima();
		getSalasAudiencia();
	}

	private void atualizarDistanciaMinima() {
        if (this.tipoAudiencia != null) {
            
            ProcessoTrf processoTrf = new ProcessoTrf();
            if (selecionarOrgaoJulgador && orgaoJulgador != null) {
            	processoTrf.setOrgaoJulgador(orgaoJulgador);
            }
            this.distanciaMinimaPesquisa = ComponentUtil.getProcessoAudienciaManager()
                                            .getPrazoMinimoMarcacaoAudiencia(processoTrf.getOrgaoJulgador() != null ? processoTrf : ProcessoTrfHome.instance().getInstance(), this.tipoAudiencia);
        }
	}

	public void modalAtualizaTipoAudiencia(){
		if(tipoAudienciaPadrao != null && tipoAudiencia.getIdTipoAudiencia() != tipoAudienciaPadrao.getIdTipoAudiencia()){
			exibeModalAlterarTipoAudiencia = true;
		}else{
			atualizaTipoAudiencia();
		}
	}
	
	public void confirmarAlteracaoTipoAudiencia(){
		atualizaTipoAudiencia();
		exibeModalAlterarTipoAudiencia = false;
	}
	
	public void cancelarAlteracaoTipoAudiencia(){
		tipoAudiencia = getEntityManager().find(TipoAudiencia.class, tipoAudienciaPadrao.getIdTipoAudiencia());
		atualizaTipoAudiencia();
		exibeModalAlterarTipoAudiencia = false;
	}

	public List<Sala> getSalasAudienciaCompetencia() {

		StringBuilder sb = new StringBuilder("SELECT sala FROM Sala as sala LEFT JOIN sala.tipoAudienciaList as tipo ");
		sb.append(" LEFT JOIN sala.competenciaList as competencia ");
		sb.append(" WHERE sala.ativo = true AND sala.tipoSala = 'A' AND tipo.idTipoAudiencia = :idTipoAudiencia ");
		sb.append(" AND (sala.orgaoJulgador = :orgaoJulgador ");

		Competencia competencia = null;

		ProcessoInstance processoInstance = getProcessoInstance();
		if(processoInstance != null && processoInstance.getIdProcesso() != null){
			competencia = ProcessoTrfManager.instance().find(ProcessoTrf.class, processoInstance.getIdProcesso()).getCompetencia();
		}

		OrgaoJulgador 	orgaoJulgadorDeslocado = getOrgaoJulgadorSalaAudiencia(sb);

		verificarPrimeiroGrauSalaAudiencia(sb);
		sb.append(" AND  competencia.idCompetencia = :idCompetenciaProcesso");
		sb.append(" ORDER BY sala.sala");

		Query q = getEntityManager().createQuery(sb.toString());

		setarParametrosSalaAudiencia(q, orgaoJulgadorDeslocado );

		if(competencia != null){
			q.setParameter("idCompetenciaProcesso",competencia.getIdCompetencia());
		}

		salasAudiencia = q.getResultList();

		return salasAudiencia;
	}


	@SuppressWarnings("unchecked")
	public List<Sala> getSalasAudiencia() {
		if (tipoAudiencia != null){

			if(ParametroUtil.instance().isAssociaCompetenciaSalaAudiencia()){
				return getSalasAudienciaCompetencia();
			}

			StringBuilder sb = new StringBuilder("SELECT sala FROM Sala as sala LEFT JOIN sala.tipoAudienciaList as tipo ")
					.append("WHERE sala.ativo = true AND sala.tipoSala = 'A' AND tipo.idTipoAudiencia = :idTipoAudiencia ")
					.append("AND (sala.orgaoJulgador = :orgaoJulgador ");

			OrgaoJulgador 	orgaoJulgadorDeslocado = getOrgaoJulgadorSalaAudiencia(sb);

			verificarPrimeiroGrauSalaAudiencia(sb);

			sb.append(" ORDER BY sala.sala");

			Query q = getEntityManager().createQuery(sb.toString());
			setarParametrosSalaAudiencia(q, orgaoJulgadorDeslocado );

			salasAudiencia = q.getResultList();
		}
		return salasAudiencia;
	}

	private void setarParametrosSalaAudiencia(Query q,	OrgaoJulgador orgaoJulgadorDeslocado ) {

		q.setParameter("idTipoAudiencia", tipoAudiencia.getIdTipoAudiencia());
		q.setParameter("orgaoJulgador", selecionarOrgaoJulgador ? getOrgaoJulgador() : ProcessoTrfHome.instance().getInstance().getOrgaoJulgador());

		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			q.setParameter("orgaoJulgadorColegiado", ProcessoTrfHome.instance().getInstance().getOrgaoJulgadorColegiado());
		}

		if (orgaoJulgadorDeslocado != null) {
			q.setParameter("orgaoJulgadorDeslocado",  orgaoJulgadorDeslocado );
		}

		q.setParameter("tipoAudiencia", tipoAudiencia != null ? tipoAudiencia.getIdTipoAudiencia() : tipoAudiencia);
	}

	private final void  verificarPrimeiroGrauSalaAudiencia(StringBuilder sb) {
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			sb.append("OR sala.orgaoJulgadorColegiado = :orgaoJulgadorColegiado) ");
		} else{
			sb.append(") ");
		}
		sb.append(" AND (tipo.idTipoAudiencia = :tipoAudiencia OR sala.tipoAudienciaList is empty) ");
	}

	private final ProcessoInstance getProcessoInstance(){
		ProcessoInstance processoInstance = null;
		try{
			ProcessoInstanceManager processoInstanceManager = (ProcessoInstanceManager) Component.getInstance(ProcessoInstanceManager.class);
			org.jbpm.graph.exe.ProcessInstance processInstance = TaskInstanceUtil.instance().getProcessInstance();
			processoInstance = processoInstanceManager.findById(processInstance.getId());
		}
		catch (PJeBusinessException e) {
			e.printStackTrace();
			logger.severe(e.getMessage());
		}
		return processoInstance;
	}

	private final  OrgaoJulgador getOrgaoJulgadorSalaAudiencia(StringBuilder sb) {

		OrgaoJulgador orgaoJulgadorDeslocado = null;
		try {

			Integer idLocalizacao = null;
			ProcessoInstance processoInstance = getProcessoInstance();
			if (processoInstance != null) {
				idLocalizacao = processoInstance.getIdLocalizacao();
			}

			if (idLocalizacao != null && idLocalizacao.intValue() > 0) {
				LocalizacaoManager localizacaoManager = (LocalizacaoManager) Component.getInstance(LocalizacaoManager.class);
				Localizacao localizacao = localizacaoManager.findById(idLocalizacao);

				OrgaoJulgadorManager orgaoJulgadorManager = (OrgaoJulgadorManager) Component.getInstance(OrgaoJulgadorManager.class);
				orgaoJulgadorDeslocado = orgaoJulgadorManager.getOrgaoJulgadorByLocalizacao(localizacao);

				if (orgaoJulgadorDeslocado != null) {
					sb.append("OR sala.orgaoJulgador = :orgaoJulgadorDeslocado ");
				}
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			logger.severe(e.getMessage());
		}
		return orgaoJulgadorDeslocado;
	}

	/**
	 * Método responsável por retornar se existe alguma audiência pendente (marcada ou designada) para uma data futura.
	 * 
	 * @return boolean : se existe alguma audiência pendente no processo.
	 */
	public boolean existeAudienciaPendenteProcesso(){
		boolean retorno = false;
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());

		// Percorre todas as audiencias do processo
		for (ProcessoAudiencia processoAudiencia : ProcessoTrfHome.instance().getInstance().getProcessoAudienciaList()){

			// Se a audiencia for designada (marcada) para uma data futura
			if (processoAudiencia.getStatusAudiencia() == StatusAudienciaEnum.M && processoAudiencia.getDtInicio().after(cal.getTime())){
				retorno = true;
				break;
			}
		}
		return retorno;
	}
	
	/**
	 * Método responsável por verificar se existe alguma audiência com a situação designada para um dado processo.
	 * 
	 * @return boolean Verdadeiro caso exista alguma audiência com a situação designada. Falso, caso contrário.
	 */
	public boolean existeAudienciaDesignadaProcesso(){
		List<ProcessoAudiencia> processoAudienciaList = ProcessoTrfHome.instance().getInstance().getProcessoAudienciaList();
		if (processoAudienciaList != null) {
			// Percorre todas as audiências do processo
			for (ProcessoAudiencia processoAudiencia : ProcessoTrfHome.instance().getInstance().getProcessoAudienciaList()){
				// Se existir alguma audiência com a situação designada, retornar verdadeiro.
				if (processoAudiencia.getStatusAudiencia() == StatusAudienciaEnum.M){
					return true;
				}
			}			
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public void consultaBloqueiosPauta(Date dataInicio){
		Date dataFinal = DateUtil.adicionarTempoData(dataInicio, Calendar.DAY_OF_YEAR, TEMPO_PESQUISA_BLOQUEIO);
		Query q = getEntityManager().createQuery(
				"select o from BloqueioPauta o "
					+ "where ((:dataInicio <= dtInicial and dtInicial <= :dataFinal) "
					+ "or (:dataInicio <= dtFinal and dtFinal <= :dataFinal)) "
					+ " and ativo = true and salaAudiencia.orgaoJulgador = :orgaoJulgador)");
		q.setParameter("dataInicio", dataInicio);
		q.setParameter("dataFinal", dataFinal);
		q.setParameter("orgaoJulgador", ProcessoTrfHome.instance().getInstance().getOrgaoJulgador());

		setBloqueiosPauta(q.getResultList());
	}

	public Date verificaDataExistenteNoBloqueio(Date dataSala, Sala sala){
		List<BloqueioPauta> lista = sala.getBloqueioPautaList();
		for (BloqueioPauta bloqueio : lista){
			if (bloqueio.getSalaAudiencia().equals(sala)){
				if (DateUtil.isDataEntre(dataSala, bloqueio.getDtInicial(), bloqueio.getDtFinal())){
					dataSala = DateUtil.adicionarTempoData(bloqueio.getDtFinal(), Calendar.DAY_OF_YEAR, 1);
				}
			}
		}
		return dataSala;
	}

	@SuppressWarnings("unchecked")
	public List<Sala> getSalasOrgaoPorTipo(OrgaoJulgador orgaoJulgador, TipoAudiencia tipoAudiencia){
		StringBuilder sb = new StringBuilder("SELECT DISTINCT(o) FROM Sala o JOIN o.tipoAudienciaList tipoAudiencia ")
				.append("WHERE o.ativo = true AND o.orgaoJulgador = :orgaoJulgador AND tipoAudiencia = :tipoAudiencia ")
				.append("ORDER BY o.sala");

		return getEntityManager().createQuery(sb.toString())
				.setParameter("orgaoJulgador", orgaoJulgador)
				.setParameter("tipoAudiencia", tipoAudiencia)
				.getResultList();
	}

	/***
	 * 
	 * @param orgaoJulgadorDestino
	 * @return true se gerou conflito com uma audiência já marcada, false se não gerou conflito;
	 * @throws Exception
	 */
	public boolean remarcarPorRedistribuicao(OrgaoJulgador orgaoJulgadorDestino) {
		boolean conflitante = false;
		TipoAudiencia tipoAudienciaOJDestino = getInstance().getTipoAudiencia();
		if (tipoAudienciaOJDestino == null){
			clearInstance();
			return false;
		}
		tipoAudiencia = tipoAudienciaOJDestino;  
		List<Sala> salaAudienciaList = getSalasAudiencia(); 
		 
		if (salaAudienciaList.isEmpty()){
			clearInstance();
			return false;
		}
		
		ProcessoAudiencia audiencia = new ProcessoAudiencia();
		audiencia.setDtInicio(getInstance().getDtInicio());
		audiencia.setDtFim(getInstance().getDtFim());
		audiencia.setProcessoTrf(getInstance().getProcessoTrf());
		audiencia.setTipoDesignacao(getInstance().getTipoDesignacao());

		Sala salaDisponivel = null;

		for (Sala salaAudiencia : salaAudienciaList){
			audiencia.setSalaAudiencia(salaAudiencia);
			if (!isConfiltante(audiencia)){
				salaDisponivel = salaAudiencia;
				break;
			}
		}

		if (salaDisponivel == null) {
			conflitante = true;
			if (salaAudienciaList.size() > 0) {
				salaDisponivel = salaAudienciaList.get(0);
			} else {
				clearInstance();
				return false;
			}
		}

		audiencia.setSalaAudiencia(salaDisponivel);

		this.setTipoAudiencia(tipoAudienciaOJDestino);

		remarcarAudiencia(audiencia, true);

		return conflitante;
	}

	public boolean isConfiltante(ProcessoAudiencia audiencia){
		return getAudienciasEntreDatas(audiencia).size() != 0;
	}
	
	@Override
	public String inactive(ProcessoAudiencia instance) {
		newInstance();
		return super.inactive(instance);
	}

	public void setTipoDesignacao(String tipoDesignacao){
		this.tipoDesignacao = tipoDesignacao;
	}

	public String getTipoDesignacao(){
		return tipoDesignacao;
	}

	public void setPautaAudienciaSala(List<ProcessoAudiencia> pautaAudienciaSala){
		this.pautaAudienciaSala = pautaAudienciaSala;
	}

	public List<ProcessoAudiencia> getPautaAudienciaSala(){
		return pautaAudienciaSala;
	}

	public void setAudienciasEmConflito(List<ProcessoAudiencia> audienciasEmConflito){
		this.audienciasEmConflito = audienciasEmConflito;
	}

	public List<ProcessoAudiencia> getAudienciasEmConflito(){
		return audienciasEmConflito;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento){
		this.processoDocumento = processoDocumento;
	}

	public ProcessoDocumento getProcessoDocumento(){
		return processoDocumento;
	}

	public void setTermoAudiencia(Boolean termoAudiencia){
		this.termoAudiencia = termoAudiencia;
	}

	public Boolean getTermoAudiencia(){
		return termoAudiencia;
	}

	public Integer getProcessoAudienciaIdProcessoAudiencia(){
		return (Integer) getId();
	}

	public void setProcessoAudienciaIdProcessoAudiencia(Integer id){
		setId(id);
	}

	public void setRealizadores(List<PessoaFisica> realizadores){
		this.realizadores = realizadores;
	}

	public List<BloqueioPauta> getBloqueiosPauta(){
		return bloqueiosPauta;
	}

	public void setBloqueiosPauta(List<BloqueioPauta> bloqueiosPauta){
		this.bloqueiosPauta = bloqueiosPauta;
	}

	public Boolean getConfirmaMarcacaoManual(){
		return confirmaMarcacaoManual;
	}

	public void setConfirmaMarcacaoManual(Boolean confirmaMarcacaoManual){
		this.confirmaMarcacaoManual = confirmaMarcacaoManual;
	}

	public List<String> getObservacoesConfirmacao(){
		return observacoesConfirmacao;
	}

	public void setObservacoesConfirmacao(List<String> observacoesConfirmacao){
		this.observacoesConfirmacao = observacoesConfirmacao;
	}

	public void setTipoPolo(String tipoPolo){
		this.tipoPolo = tipoPolo;
	}

	public String getTipoPolo(){
		return tipoPolo;
	}

	public Boolean getExibeModalAlterarTipoAudiencia() {
		return exibeModalAlterarTipoAudiencia;
	}

	public void setExibeModalAlterarTipoAudiencia(
			Boolean exibeModalAlterarTipoAudiencia) {
		this.exibeModalAlterarTipoAudiencia = exibeModalAlterarTipoAudiencia;
	}
	
	public Boolean getOcultarDadosProcesso() {
		if (this.ocultarDadosProcesso == null) {
			this.ocultarDadosProcesso = BooleanUtils.isTrue((Boolean)ComponentUtil.getTramitacaoProcessualService()
					.recuperaVariavelTarefa(Variaveis.VARIAVEL_FLUXO_AUDIENCIA_OCULTAR_DADOS_PROCESSO));
		}
		return this.ocultarDadosProcesso;
	}

	public Boolean getOcultarDocumentosProcesso() {
		if (this.ocultarDocumentosProcesso == null) {
			this.ocultarDocumentosProcesso = BooleanUtils.isTrue((Boolean) ComponentUtil.getTramitacaoProcessualService()
					.recuperaVariavelTarefa(Variaveis.VARIAVEL_FLUXO_AUDIENCIA_OCULTAR_DOCUMENTOS_PROCESSO));
		}
		return this.ocultarDocumentosProcesso;
	}

	public Boolean getPermitirDesignarMultiplasAudiencias() {
		if (this.permitirDesignarMultiplasAudiencias == null) {
			this.permitirDesignarMultiplasAudiencias = BooleanUtils.isTrue((Boolean) ComponentUtil.getTramitacaoProcessualService()
					.recuperaVariavelTarefa(Variaveis.VARIAVEL_FLUXO_AUDIENCIA_PERMITIR_DESIGNAR_MULTIPLAS));
		}
		return this.permitirDesignarMultiplasAudiencias;
	}
	
	public Boolean isOcultarBotaoParaCejusc(ProcessoAudiencia audiencia) {
		return isProcessoFromCejusc(audiencia);
	}
	
	

	/**
	 * Salva o documento não assinado e adiciona ao processo documento.
	 */
	protected void salvarDocumentoNaoAssinado() {
		ProcessoDocumentoHome home = ProcessoDocumentoHome.instance();
		home.persistSemAssinatura();

		ProcessoDocumento documento = home.getInstance();
		ProcessoAudiencia audiencia = getInstance();
		
		audiencia.setProcessoDocumento(documento);	
		setProcessoDocumento(documento);
	}
	
	public void marcarAudienciaDePautaEspecifica(String identificadorPautaEspecifica, String modoMarcacao){
	    ProcessoTrf processo = ProcessoTrfHome.instance().getInstance();
        ComponentUtil.getProcessoAudienciaManager().setPrazosProcessuaisService(ComponentUtil.getPrazosProcessuaisService());
	    
        TipoAudiencia tipoAudiencia = ProcessoTrfHome.instance().tipoAudienciaPadrao();
        if (tipoAudiencia == null) {
            tipoAudiencia = processo.getClasseJudicial().getTipoPrimeiraAudiencia();
        }
        
        if (tipoAudiencia == null){
        	logger.severe(String.format("[PautaEspecifica] Não há tipo de audiência configurado para órgão julgador %s ou para a classe judicial %s",
        			processo.getOrgaoJulgador().getOrgaoJulgador(), processo.getClasseJudicialStr()));
        	return;
        }
        setTipoAudiencia(tipoAudiencia);

        List<Sala> salas = processo.getOrgaoJulgador().getSalaList();
        if (ProjetoUtil.isVazio(salas)){
        	logger.severe("[PautaEspecifica] Audiência de pauta específica no designada. Não há salas para órgão julgador ".concat(processo.getOrgaoJulgador().getOrgaoJulgador()));
        	return;
        }
        CollectionUtilsPje.sortCollection(salas, true, "sala");
        setTempoAudiencia(getTempoAudienciaPadraoOrgaoJulgador(processo.getOrgaoJulgador(), tipoAudiencia));
        
        try {
    		Integer prazoMinimoDesignacao = ComponentUtil.getProcessoAudienciaManager().recuperarPrazoMinimoMarcacaoAudiencia(processo.getOrgaoJulgador(), tipoAudiencia);
    		if (prazoMinimoDesignacao != null) {
    			distanciaMinimaPesquisa = prazoMinimoDesignacao;
    		}else{
    			logger.severe(String.format("[PautaEspecifica] Não há prazo mínimo para marcação de audiência configurado para órgão julgador %s para o tipo de audiência %s.",
            			processo.getOrgaoJulgador().getOrgaoJulgador(), tipoAudiencia.getTipoAudiencia()));
            	return;
    		}
            Calendar dataInicio = DateUtil.getCalendarFromDate(DateUtil.getDataAtual());
            dataInicio.add(Calendar.DAY_OF_YEAR, prazoMinimoDesignacao);
            
            boolean isAudienciaPautaEspecificaMarcada = true;
			
            ProcessoAudiencia audiencia = ComponentUtil.getProcessoAudienciaManager().getProximaAudienciaPautaEspecifica(identificadorPautaEspecifica, null, salas, dataInicio,getTempoAudiencia());
            
            if ("preferencial".equals(modoMarcacao) && salasDisponiveisAudiencia()){
            	ProcessoAudiencia proximaAudienciaPautaNormal = getHorariosEncontrados().get(0);
            	if (audiencia == null || DateUtil.isDataComHoraMaior(audiencia.getDtInicio(), proximaAudienciaPautaNormal.getDtInicio())){
            		audiencia = proximaAudienciaPautaNormal;
            		isAudienciaPautaEspecificaMarcada = false;
            	}
            }
            
            if (audiencia != null){
            	audiencia.setTipoDesignacao(ProcessoAudiencia.TIPO_DESIGNACAO_SUGERIDA);
            	marcarAudiencia(audiencia, false);
            	
            	if (StatusAudienciaEnum.M.equals(audiencia.getStatusAudiencia())){
            		processo.getProcessoAudienciaList().add(audiencia);
            		logger.info(String.format("[PautaEspecifica] Audiência de pauta %s designada: [%s]", isAudienciaPautaEspecificaMarcada ? "específica" : "normal", audiencia.getProcessoAudienciaStr(" ")));
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("[PautaEspecifica] Audiência de pauta específica não designada. ".concat(e.getMessage()));
            FacesMessages.instance().clear();
            FacesMessages.instance().add(Severity.ERROR, "[PautaEspecifica] Audiência de pauta específica não designada. ".concat(e.getMessage()));       
            return;
        }
	}
	
	/**
	 * Método a ser utilizado em fluxo para marcação de uma audiência automática, 
	 * quando o parâmetro <code>pje:audiencia:designacaoEmFluxo</code> indicar que a marcação de audiência será feita no fluxo.
	 */
	public void marcarAudienciaAutomaticaNoFluxo(){
	    ProcessoTrf processo = ProcessoTrfHome.instance().getInstance();
	    
        //Tipo de Audiência
        TipoAudiencia tipoAudiencia = ProcessoTrfHome.instance().tipoAudienciaPadrao();
        if (tipoAudiencia == null) {
            tipoAudiencia = processo.getClasseJudicial().getTipoPrimeiraAudiencia();
        }
        setTipoAudiencia(tipoAudiencia);
        
        this.marcarAudienciaAutomatica(processo.getOrgaoJulgador(), tipoAudiencia);
	}
	
	/**
	 * Método responsável por recuperar o status da audiência configurada por fluxo
	 * @return
	 */
	public StatusAudienciaEnum getStatusAudienciaSelecionadaNoFluxo() {
		return (StatusAudienciaEnum) ComponentUtil.getTramitacaoProcessualService().recuperaVariavel(Variaveis.VARIAVEL_FLUXO_STATUS_AUDIENCIA_SELECIONADA);
	}

	/**
	 * Método responsável por definir o status da audiência configurada por fluxo
	 * @param statusAudiencia
	 */
	public void setStatusAudienciaSelecionadaNoFluxo(StatusAudienciaEnum statusAudiencia) {
		ComponentUtil.getTramitacaoProcessualService().gravaVariavel(Variaveis.VARIAVEL_FLUXO_STATUS_AUDIENCIA_SELECIONADA, statusAudiencia);
		this.instance.setStatusAudiencia(statusAudiencia);
	}
	
	/**
	 * Método responsável por verificar se o usuário logado é um administrador para liberar os filtros de consulta  jurisdição e Orgão Julgador Pauta de audiência
	 * @return
	 */
	public boolean desabilitaFiltrosJurisdicaoOrgaoJulgador() {
		Identity identity = Identity.instance();
		if(Authenticator.getUsuarioLogado() != null) {
			if(ParametroUtil.instance().isPrimeiroGrau()) {
				if(Authenticator.getOrgaoJulgadorAtual() != null) {
					return true;
				}
			}else if(Authenticator.getOrgaoJulgadorColegiadoAtual() != null ) {
					return true;
			}
		}
		else if(!identity.hasRole("administrador") && !identity.hasRole("admin") && !identity.hasRole(Papeis.ADMINISTRADOR)){
				return true;
		}
		
		return false;
	}
	
	/**
	 * Método responsável por baixar todos os documentos dos processos selecionados pelo usuário
	 * @param audienciasProcessos
	 */
	public void baixarDocumentosDosProcessos(List<ProcessoAudiencia> audienciasProcessos) {
		// Se a pesquisa não retornou registros...
		if (audienciasProcessos == null || audienciasProcessos.size() == 0) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "A pesquisa não retornou registros.");			
		}
		else
		{
			// Instanciar variáveis
			List<String> processosCarregados = new ArrayList<String>();
			GeradorPdfUnificado geradorPdf = new GeradorPdfUnificado();
			geradorPdf.setResurcePath(new Util().getUrlProject());
	
			// Criar o arquivo ZIP
			ByteArrayOutputStream bas = new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(bas);
			
			// Percorrer a lista de processos selecionados pelo usuário
			for (ProcessoAudiencia audienciaProcesso : audienciasProcessos) {
				ProcessoTrf processoTrf = audienciaProcesso.getProcessoTrf();
				
				// Se o processo já foi carregado...
				if (processosCarregados.contains(String.valueOf(processoTrf.getIdProcessoTrf()))) {
					continue;
				} else {
					processosCarregados.add(String.valueOf(processoTrf.getIdProcessoTrf()));
				}
				
				try {
					// Recuperar a lista de documentos do processo atual
					List<ProcessoDocumento> processoDocumentoList = 
							ProcessoDocumentoHome.instance().listarDocumentosDoProcesso(processoTrf.getIdProcessoTrf());
	
					// Ordenar a lista de documentos do processo atual
					Collections.sort(processoDocumentoList,
							new Comparator<ProcessoDocumento>() {	
								@Override
								public int compare(ProcessoDocumento pd1, ProcessoDocumento pd2) {
									Date d1 = (pd1.getDataJuntada() == null ? pd1.getDataInclusao() : pd1.getDataJuntada());
									Date d2 = (pd2.getDataJuntada() == null ? pd2.getDataInclusao() : pd2.getDataJuntada());
									return d1.compareTo(d2);
								}
							});
	
					// Percorrer a lista de documentos do processo atual
					for (Iterator<ProcessoDocumento> iterator = processoDocumentoList.iterator(); iterator.hasNext();) {
						ProcessoDocumento processoDocumento = iterator.next();
						
						if (ProcessoDocumentoHome.isUsuarioExterno()
								&& ProcessoDocumentoHome.instance()
										.existePendenciaCienciaSemCache(processoDocumento)) {
							iterator.remove();
						}
					}
					
					// Gerar arquivo PDF único contendo todos os documentos do processo
					byte[] bytesArquivoPdf = geradorPdf.gerarPdfUnificado(processoTrf, processoDocumentoList);
					
					// Adicionar o arquivo PDF ao arquivo ZIP
					ZipEntry z = new ZipEntry(processoTrf.getNumeroProcesso() + ".pdf");
					zos.putNextEntry(z);
					zos.write(bytesArquivoPdf);
					zos.closeEntry();
				} catch (PdfException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			OutputStream out = null;
			
			try {
				// Fechar o arquivo ZIP
				zos.close();
				
				// Baixar o arquivo ZIP
				byte[] bytesArquivoZip = bas.toByteArray();
				FacesContext facesContext = FacesContext.getCurrentInstance();
				HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
				response.setContentType("application/zip");
				response.setHeader("Content-Disposition", "attachment; filename=\"DocumentosProcessos.zip\"");
				response.setContentLength(bytesArquivoZip.length);
				out = response.getOutputStream();
				out.write(bytesArquivoZip);
				out.flush();
				facesContext.responseComplete();
			} catch (IOException ex) {
				FacesMessages.instance().add(Severity.ERROR, "Ocorreu um erro ao tentar baixar o arquivo: DocumentosProcessos.zip");
			} catch (Exception exc) {
				exc.printStackTrace();
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * Método responsável por verificar se existe alguma audiência agendada para
	 * o processo que esteja pendente a partir da data de consulta
	 * 
	 * @see ProcessoAudienciaManager#procurarAudienciasAbertasPorProcesso(ProcessoTrf,
	 *      Date)
	 * 
	 * @param processoTrf
	 *            {@link ProcessoTrf} a ser pesquisado
	 * 
	 * @param adicionarAlerta
	 *            Indicador para adicionar mensagem de alerta ao processo caso
	 *            exista audiência agendada para o processo no futuro.
	 * @see {@link AlertaHome#inserirAlerta(ProcessoTrf, String, CriticidadeAlertaEnum)}
	 * 
	 * @return <code>true</code> caso exista audiência agendada no futuro
	 *         para o processo
	 * 
	 *         <code>false</code> caso não exista audiência agendada no futuro para
	 *         o processo
	 * 
	 */
	public boolean existeAudienciaAberta(ProcessoTrf processoTrf,
			boolean adicionarAlerta) {
		
		List<ProcessoAudiencia> audienciasAbertas = ComponentUtil.getProcessoAudienciaManager()
				.procurarAudienciasAbertasPorProcesso(processoTrf, new Date());
		
		if (CollectionUtils.isNotEmpty(audienciasAbertas)) {
			
			if (adicionarAlerta) {
				StringBuilder msg = new StringBuilder("Este processo possui audiência(s) marcada(s) para o dia ");
				
				List<String> datas = new ArrayList<String>();
				for (ProcessoAudiencia processoAudiencia : audienciasAbertas) {
					datas.add(DateUtil.dateToString(processoAudiencia.getDtInicio(), "dd/MM/yyyy"));
				}
				
				msg.append(StringUtil.concatList(datas, ", ", " e "));
				
				AlertaHome.instance().inserirAlerta(processoTrf, msg.toString(), CriticidadeAlertaEnum.A);
				EntityUtil.getEntityManager().flush();				
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
 	 * Mtodo responsvel por realizar a verificao da data de marcao de audincia se a mesma 
 	 * for do tipo "Designao sugerida" no permitir a adio de hora, caso contrrio 
 	 * retornar o padro completo: dia/mes/ano hora:minuto
 	 * 
 	 * @return o padro da data correto
 	 */
 	public String retornaPadraoTipoMarcacaoAudiencia(){
 		String padrao = ComponentUtil.getProcessoAudienciaManager().retornaPadraoTipoMarcacaoAudiencia(getTipoDesignacao());
 		return padrao;
 	}

    /**
     * Método responsável por cancelar todas as audiências marcadas para o processo especificado
     * @param processoTrf Processo para o qual as audiências serão canceladas
     * @param motivoCancelamento Motivo do cancelamento das audiências
     */
	public void cancelarAudiencias(ProcessoTrf processoTrf, String motivoCancelamento) {
		List<ProcessoAudiencia> lista = ComponentUtil.getProcessoAudienciaManager().procurarAudienciasAbertasPorProcesso(processoTrf, null); 

		for (ProcessoAudiencia processoAudiencia : lista) {
			this.cancelarAudiencia(processoAudiencia, motivoCancelamento);
		}
	}
	
    /**
     * Método responsável por cancelar a audiência especificada
     * @param processoAudiencia Audiência que será cancelada
     * @param motivoCancelamento Motivo do cancelamento da audiência
     */
	public void cancelarAudiencia(ProcessoAudiencia processoAudiencia, String motivoCancelamento) {
		this.setInstance(processoAudiencia);
		processoAudiencia.setDsMotivo(motivoCancelamento);
		this.setEtapaAudiencia(EtapaAudienciaEnum.C);
		this.cancelarAudiencia();
	}

	public Boolean getBuscarHorariosReservados() {
      
		return buscarHorariosReservados;
    }

	public void setBuscarHorariosReservados(Boolean buscarHorariosReservados) {
		this.buscarHorariosReservados = buscarHorariosReservados;
	}
	
	public Boolean getSelecionarOrgaoJulgador() {
		return selecionarOrgaoJulgador;
	}

	public void setSelecionarOrgaoJulgador(Boolean selecionarOrgaoJulgador) {
		this.selecionarOrgaoJulgador = selecionarOrgaoJulgador;
	}

	/**
	 * Recupera a lista de salas vinculadas ao rgo julgador do processo atual de acordo com o tipo de audincia escolhido
	 *
	 * @return List<Sala>
	 */
	@SuppressWarnings("unchecked")
	public List<Sala> getSalasOrgaoJulgadorProcesso(Sala s){
		String hql = "select distinct(s) from Sala s left join s.tipoAudienciaList tal where s.ativo = true "
			+ "and s.orgaoJulgador = ? and (tal.idTipoAudiencia = ? or tal.tipoAudiencia is null) ";

		if (s != null){
			hql += " AND s = ? ";
		}

		Query query = getEntityManager().createQuery(hql);

		if (selecionarOrgaoJulgador) {
			query.setParameter(1, orgaoJulgador);
		}
		else {
			query.setParameter(1, ProcessoTrfHome.instance().getInstance().getOrgaoJulgador());
		}

		query.setParameter(2, getTipoAudiencia().getIdTipoAudiencia());

		if (s != null){
			query.setParameter(3, s);
		}
		List<Sala> returnValue = query.getResultList();
		return returnValue;
	}

	private void movimentarProcesso(String variavel, ProcessoAudiencia audiencia) {
		if (ParametroUtil.instance().isDesativaSinalizacaoMovimentacaoAudiencia() == false) {
			TramitacaoProcessualService tps = ComponentUtil.getTramitacaoProcessualService();
			String transicaoPadrao = (String) tps.recuperaVariavelTarefa(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);

			tps.gravaVariavel(Variaveis.PJE_FLUXO_AUDIENCIA, audiencia);
			tps.gravaVariavel(Eventos.EVENTO_SINALIZACAO, variavel);

			if (StringUtils.isNotBlank(transicaoPadrao)) {
				TaskInstanceHome.instance().end(transicaoPadrao);
			}

			ProcessoJudicialService pjs = ComponentUtil.getComponent(ProcessoJudicialService.class);
			Map<String, Object> novasVariaveis = new HashMap<String, Object>();
			novasVariaveis.put(Variaveis.PJE_FLUXO_AUDIENCIA, audiencia);
			pjs.sinalizarFluxo(tps.recuperaProcesso(), variavel, true, true, true, novasVariaveis);
		}
	}
}
