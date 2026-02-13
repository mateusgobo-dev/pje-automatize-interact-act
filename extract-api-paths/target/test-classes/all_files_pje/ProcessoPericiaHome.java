package br.com.infox.cliente.home;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.actions.anexarDocumentos.AnexarDocumentos;
import br.com.infox.cliente.component.suggest.ProcessoParteSuggestBean;
import br.com.infox.cliente.component.tree.EspecialidadeTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.component.FileHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.vo.DesignarPericia;
import br.jus.cnj.pje.webservice.IPericiaService;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaPeritoDisponibilidade;
import br.jus.pje.nucleo.entidades.PessoaPeritoIndisponibilidade;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoPericia;
import br.jus.pje.nucleo.enums.PericiaStatusEnum;
import br.jus.pje.nucleo.enums.TitularidadeOrgaoEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name("processoPericiaHome")
public class ProcessoPericiaHome extends AbstractProcessoPericiaHome<ProcessoPericia> {
	
	private static final long serialVersionUID = 1L;
	private static final LogProvider logger = Logging.getLogProvider(ProcessoPericiaHome.class);
	
	@In(create=true)
	private IPericiaService periciaService;
	
	@In(create = true)
	private PessoaPeritoEspecialidadeHome pessoaPeritoEspecialidadeHome;

	private Boolean ocultarDadosProcesso = Boolean.FALSE;
	private Boolean mostrarDivCancelamento = false;
	private boolean visibleDivPericia = true;
	private boolean visibleDivMarcaPericia = false;
	private boolean visibleDivGridHoraDisponivel = false;
	private boolean periciaParteRadio = true;
	private Time proximoHorarioFila;
	private Calendar dataSelecionada = Calendar.getInstance();
	private boolean redesignarPericia = false;
	private ProcessoPericia processoPericiaAnterior = null;
	private Boolean mostrarBtnDesignar = Boolean.TRUE;
	private Boolean mostrarDivRealizacao = Boolean.FALSE;
	private Boolean mostrarDivAprovarRealizacao = Boolean.FALSE;
	private Boolean mostrarDivAnexarDoc = Boolean.FALSE;
	private Boolean mostrarDivEditorModeloDoc = Boolean.TRUE;
	private Boolean mostrarBotaoAnexarDoc = Boolean.FALSE;
	private List<PessoaPeritoDisponibilidade> pessoaPeritoList = new ArrayList<PessoaPeritoDisponibilidade>(0);
	private Boolean isNewInstance = Boolean.TRUE;
	private Integer contadorFluxo = 0;
	private Boolean vincularDocLaudo = Boolean.FALSE;
	private Boolean ausenciaParte = Boolean.FALSE;
	private Boolean mostrarDivRealizarPericia = Boolean.TRUE;
	private PericiaStatusEnum periciaStatus;
	private PessoaPerito pessoaPerito;
	private Date dtMarcacaoInicio;
	private Date dtMarcacaoFim;
	private Localizacao orgaoJulgador;
	private TitularidadeOrgaoEnum titularidade;
	private Especialidade especialidade;
	private String pessoaPericiado;
	private Integer numeroSequencia;
	private Integer digitoVerificador;
	private Integer ano;
	private String ramoJustica;
	private String respectivoTribunal;
	private Integer numeroOrigem;
	private String nomePericiado;
	private Date dataMarcacaoInicial;
	private Date dataMarcacaoFinal;
	private boolean exibeModalConfirmacao = false;
	private List<DesignarPericia> horariosDisponiveis = new ArrayList<>();
	private DesignarPericia disponibilidadePeritoEscolhida;
	
	
	public void fluxoDesignarPericia() {
		if (isNewInstance) {
			newInstance();
			Boolean possuiPericias = this.isPossuiPericiasAgendadas();
			setVisibleDivPericia(possuiPericias);
			if(possuiPericias) {
				this.ocultaDivDesignarPericia();
			}else {
				this.mostraDivDesignarPericia();
			}
			setIsNewInstance(Boolean.FALSE);
			contadorFluxo++;
		}
	}

	public void fluxoOperacoesPericia() {
		if (contadorFluxo != 0) {
			newInstance();
		}
		setMostrarBtnDesignar(Boolean.FALSE);
	}
	
	public Boolean verificaOcultarDadosProcesso() {
		Boolean ocultarDadosProcesso = (Boolean) ComponentUtil.getTramitacaoProcessualService().recuperaVariavel(Variaveis.VARIAVEL_FLUXO_PERICIA_OCULTAR_DADOS_PROCESSO);
		if(ocultarDadosProcesso != null) {
			this.setOcultarDadosProcesso(ocultarDadosProcesso);
		}
		
		return this.getOcultarDadosProcesso();
	}

	public Boolean getOcultarDadosProcesso() {
		return ocultarDadosProcesso;
	}

	public void setOcultarDadosProcesso(Boolean ocultarDadosProcesso) {
		this.ocultarDadosProcesso = ocultarDadosProcesso;
	}

	public void setMostrarBtnDesignar(Boolean mostrarBtnDesignar) {
		this.mostrarBtnDesignar = mostrarBtnDesignar;
	}

	public Boolean getMostrarBtnDesignar() {
		return mostrarBtnDesignar;
	}

	public void setIsNewInstance(Boolean isNewInstance) {
		this.isNewInstance = isNewInstance;
	}

	public Boolean getIsNewInstance() {
		return isNewInstance;
	}

	public void updateRemarcacao() {
		if (redesignarPericia) {
			instance.setPericiaAnterior(processoPericiaAnterior);
			processoPericiaAnterior.setStatus(PericiaStatusEnum.R);
			EntityManager em = getEntityManager();
			em.merge(processoPericiaAnterior);
			em.persist(processoPericiaAnterior);
		}
	}

	public void redesignarPericia(ProcessoPericia pp) {
		processoPericiaAnterior = pp;
		try {
			setInstance(EntityUtil.cloneEntity(pp, false));
			instance.setPessoaPerito(processoPericiaAnterior.getPessoaPerito());
			instance.setPessoaPericiado(processoPericiaAnterior.getPessoaPericiado());
			redesignarPericia = true;
			proxDivConfirmaPericia();
		} catch (InstantiationException e) {
			logger.error(e.getMessage());
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage());
		}

	}

	@Override
	public void newInstance() {
		limparInstance();		
		finalizarProcesso();
		setMostrarDivRealizacao(Boolean.FALSE);
		setMostrarDivAprovarRealizacao(Boolean.FALSE);
		setIsNewInstance(Boolean.TRUE);
		refreshGrid("processoPericiaGrid");
	}

	public void limparInstance() {
		if (!this.exibeModalConfirmacao) {
			super.newInstance();
			EspecialidadeTreeHandler tree = ComponentUtil.getComponent("especialidadeTree");
			tree.clearTree();
			proxDivMarcaPericia();
			redesignarPericia = false;
			proximoHorarioFila = null;
			especialidade = null;
			contadorFluxo = 0;
			limpaDadosHorario();
		}
	}
	
	public void limparPesquisa() {
		numeroSequencia = null;
		digitoVerificador = null;
		ano = null;
		ramoJustica = null;
		respectivoTribunal = null;
		numeroOrigem = null;
		nomePericiado = null;
		dataMarcacaoInicial = null;
		dataMarcacaoFinal = null;
	}
	
	public void backButton() {
		newInstance();
		proxDivMarcaPericia();
	}

	@Override
	public String persist() {
		atribuirValoresPericia();
		this.disponibilidadePeritoEscolhida.setRedesignarPericia(this.redesignarPericia);
		this.disponibilidadePeritoEscolhida.setProcessoPericia(instance);
		this.disponibilidadePeritoEscolhida.setProcessoPericiaAntigo(processoPericiaAnterior);
		this.periciaService.designarPericia(this.disponibilidadePeritoEscolhida);
		this.exibeModalConfirmacao = false;
		ProcessoPericia pericia = instance;
		if (redesignarPericia) {
			movimentarProcesso(Variaveis.PJE_FLUXO_PERICIA_AGUARDA_REDESIGNACAO, pericia);
		} else {
			movimentarProcesso(Variaveis.PJE_FLUXO_PERICIA_AGUARDA_DESIGNACAO, pericia);
		}
		
		this.newInstance();
		return null;
	}

	private void atribuirValoresPericia() {
		Context session = Contexts.getSessionContext();
		Pessoa pessoa = (Pessoa) session.get("usuarioLogado");

		instance.setPessoaMarcador(pessoa);
		instance.setProcessoTrf(ProcessoTrfHome.instance().getInstance());
		instance.setStatus(PericiaStatusEnum.M);
		
		if (this.disponibilidadePeritoEscolhida != null && this.disponibilidadePeritoEscolhida.getDataHora() != null) {
			Time horario = new Time(this.disponibilidadePeritoEscolhida.getDataHora().getTime().getTime());
			instance.setHoraMarcada(horario);
			instance.setDataMarcacao(this.dataSelecionada.getTime());
		}
	}


	public void consultarHorarios() throws PJeBusinessException {
		try {
			Date data = periciaService.obterDataMarcacaoDisponivel(instance.getEspecialidade(), instance.getPessoaPerito(), this.dataSelecionada.getTime());
			proximoHorarioFila = new Time(data.getTime());
			instance.setDataMarcacao(DateUtil.getDataSemHora(data));
		} catch (PJeBusinessException ex) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, ex.getLocalizedMessage());
		}
	}

	public void proxDivMarcaPericia() {
		this.setVisibleDivGridHoraDisponivel(false);
		Boolean possuiPericias = this.isPossuiPericiasAgendadas();
		this.setVisibleDivPericia(possuiPericias);
		if(possuiPericias) {
			this.ocultaDivDesignarPericia();
		}else {
			this.mostraDivDesignarPericia();
		}
	}

	public void proxDivConfirmaPericia() {
		this.setVisibleDivGridHoraDisponivel(true);
		Boolean possuiPericias = this.isPossuiPericiasAgendadas();
		setVisibleDivPericia(possuiPericias);
		this.setVisibleDivMarcaPericia(Boolean.FALSE);
		this.setMostrarBtnDesignar(Boolean.FALSE);		
	}

	public void finalizarProcesso() {
		this.setVisibleDivGridHoraDisponivel(false);
		Boolean possuiPericias = this.isPossuiPericiasAgendadas();
		this.setVisibleDivPericia(possuiPericias);
		if(possuiPericias) {
			this.ocultaDivDesignarPericia();
		}else {
			this.mostraDivDesignarPericia();
		}

	}

	public void confirmaCancelamento() {
		try {
			Pessoa pessoaLogada = (Pessoa) Contexts.getSessionContext().get("pessoaLogada");
			ProcessoPericia pericia = getInstance();
			pericia.setPessoaCancela(pessoaLogada);
			pericia.setStatus(PericiaStatusEnum.C);
			super.update();
			alterarDivCancelamento();
			
			movimentarProcesso(Variaveis.PJE_FLUXO_PERICIA_AGUARDA_CANCELAMENTO, pericia);
		} catch (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "A pessoa logada não é um servidor!");
		}
	}

	public static ProcessoPericiaHome instance() {
		return ComponentUtil.getComponent("processoPericiaHome");
	}

	public void setarLinha(ProcessoPericia pp) {
		instance = pp;
		alterarDivCancelamento();
	}

	public void alterarDivCancelamento() {
		if (getMostrarDivCancelamento()) {
			setMostrarDivCancelamento(Boolean.FALSE);
			setVisibleDivPericia(Boolean.TRUE);
		} else {
			setMostrarDivCancelamento(Boolean.TRUE);
			setVisibleDivPericia(Boolean.FALSE);
		}
	}

	@Observer(ProcessoParteSuggestBean.PROCESSO_PARTE_SUGGEST_EVENT_SELECTED)
	public void setProcessoPartePericiado(ProcessoParte processoParte) {
		instance.setPessoaPericiado(processoParte.getPessoa());
	}

	public Time getProximoHorarioFila() {
		return proximoHorarioFila;
	}

	public Date getDataSelecionada() {
		return dataSelecionada.getTime();
	}

	public void setDataSelecionada(Date dataSelecionada) {
		this.dataSelecionada.setTime(dataSelecionada);
	}

	public void setMostrarDivCancelamento(Boolean mostrarDivCancelamento) {
		this.mostrarDivCancelamento = mostrarDivCancelamento;
	}

	public Boolean getMostrarDivCancelamento() {
		return mostrarDivCancelamento;
	}

	public boolean getVisibleDivPericia() {
		return visibleDivPericia;
	}

	public void setVisibleDivPericia(boolean visibleDivPericia) {
		this.visibleDivPericia = visibleDivPericia;
	}

	public boolean getVisibleDivMarcaPericia() {
		return visibleDivMarcaPericia;
	}

	public void setVisibleDivMarcaPericia(boolean visibleDivMarcaPericia) {
		this.visibleDivMarcaPericia = visibleDivMarcaPericia;
	}

	public boolean isVisibleDivGridHoraDisponivel() {
		return visibleDivGridHoraDisponivel;
	}

	public void setVisibleDivGridHoraDisponivel(boolean visibleDivGridHoraDisponivel) {
		this.visibleDivGridHoraDisponivel = visibleDivGridHoraDisponivel;
	}

	public boolean getPericiaParteRadio() {
		return periciaParteRadio;
	}

	public void setPericiaParteRadio(boolean periciaParteRadio) {
		this.periciaParteRadio = periciaParteRadio;
	}

	@Override
	public String update() {
		if (ausenciaParte) {
			getInstance().setStatus(PericiaStatusEnum.A);
			closeRealizar();
			return super.update();
		}
		ProcessoHome.instance().setInstance(getInstance().getProcessoTrf().getProcesso());
		ProcessoDocumentoHome pdHome = ProcessoDocumentoHome.instance();

		String ret = pdHome.persist();
		if (ret != null) {
			getInstance().setProcessoDocumento(pdHome.getInstance());
		}
		getInstance().setStatus(PericiaStatusEnum.P);
		return super.update();

	}

	public void changeStatus(String status) {
		if (status.equals("C")) {
			getInstance().setStatus(PericiaStatusEnum.C);
		} else if (status.equals("F")) {
			getInstance().setStatus(PericiaStatusEnum.F);
		} else if (status.equals("M")) {
			getInstance().setStatus(PericiaStatusEnum.M);
		} else if (status.equals("N")) {
			getInstance().setStatus(PericiaStatusEnum.N);
		} else if (status.equals("P")) {
			getInstance().setStatus(PericiaStatusEnum.P);
		} else if (status.equals("R")) {
			getInstance().setStatus(PericiaStatusEnum.R);
		} else if (status.equals("A")) {
			getInstance().setStatus(PericiaStatusEnum.A);
		}

		setMostrarDivAprovarRealizacao(Boolean.FALSE);
		setVisibleDivPericia(Boolean.TRUE);
		refreshGrid("processoPericiaAprovarRealizacaoGrid");
		getEntityManager().merge(getInstance());
		getEntityManager().flush();
	}

	public void setarLinha(ProcessoPericia pp, String acao) {
		instance = pp;
		alterarVariavelDiv(acao);
	}

	public String alterarVariavelDiv(String acao) {
		if (acao.equals("cancelar")) {
			if (getMostrarDivCancelamento()) {
				setMostrarDivCancelamento(Boolean.FALSE);
				setVisibleDivPericia(Boolean.TRUE);
			} else {
				setMostrarDivCancelamento(Boolean.TRUE);
				setVisibleDivPericia(Boolean.FALSE);
			}
		} else {
			if (acao.equals("aprovarRealizacao")) {
				setMostrarDivAprovarRealizacao(Boolean.TRUE);
				setVisibleDivPericia(Boolean.FALSE);
			}
		}
		return "";
	}

	public void alterarAnexarDocDiv() {
		if (getMostrarDivAnexarDoc()) {
			setMostrarDivAnexarDoc(Boolean.FALSE);
		} else {
			setMostrarDivAnexarDoc(Boolean.TRUE);
		}

	}

	public void carregarModelo() {
	}

	public void cancelarDivPericia() {
		this.setVisibleDivPericia(true);
		this.setVisibleDivGridHoraDisponivel(false);
		this.setVisibleDivMarcaPericia(false);
	}

	public void setMostrarDivRealizacao(Boolean mostrarDivRealizacao) {
		this.mostrarDivRealizacao = mostrarDivRealizacao;
	}

	public Boolean getMostrarDivRealizacao() {
		return mostrarDivRealizacao;
	}

	public void setMostrarDivAnexarDoc(Boolean mostrarDivAnexarDoc) {
		this.mostrarDivAnexarDoc = mostrarDivAnexarDoc;
	}

	public Boolean getMostrarDivAnexarDoc() {
		return mostrarDivAnexarDoc;
	}

	public List<PessoaPeritoDisponibilidade> getPessoaPeritoList() {
		return pessoaPeritoList;
	}

	public void setPessoaPeritoList(List<PessoaPeritoDisponibilidade> pessoaPeritoList) {
		this.pessoaPeritoList = pessoaPeritoList;
	}

	@SuppressWarnings("unchecked")
	public List<PessoaPeritoDisponibilidade> listarHorarioDisponivel() {
		this.proxDivConfirmaPericia();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT pd FROM PessoaPeritoDisponibilidade pd ");
		sb.append("WHERE pd.pessoaPeritoEspecialidade.especialidade.idEspecialidade = :idEspecialista ");
		sb.append("AND pd.pessoaPeritoEspecialidade.pessoaPerito.idUsuario = :idPerito");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idEspecialista", getInstance().getEspecialidade().getIdEspecialidade());
		q.setParameter("idPerito", getInstance().getPessoaPerito().getIdUsuario());
		if (q.getResultList().size() == 0) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"Não existe horário disponível para o perito selecionado.");
			this.setPessoaPeritoList(null);
		} else {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"setado, qnt da lista: " + q.getResultList().size());
			this.setPessoaPeritoList(q.getResultList());
		}
		return getPessoaPeritoList();
	}

	public void setMostrarDivEditorModeloDoc(Boolean mostrarDivEditorModeloDoc) {
		this.mostrarDivEditorModeloDoc = mostrarDivEditorModeloDoc;
	}

	public Boolean getMostrarDivEditorModeloDoc() {
		return mostrarDivEditorModeloDoc;
	}

	public void alteraModelo() {
		ProcessoDocumentoHome pdHome = ProcessoDocumentoHome.instance();
		pdHome.setModelo(!pdHome.getModelo());
		alterarVariavelDiv("enviarArquivo");
	}

	public void setMostrarBotaoAnexarDoc(Boolean mostrarBotaoAnexarDoc) {
		this.mostrarBotaoAnexarDoc = mostrarBotaoAnexarDoc;
	}

	public Boolean getMostrarBotaoAnexarDoc() {
		return mostrarBotaoAnexarDoc;
	}

	public Boolean getMostrarDivAprovarRealizacao() {
		return mostrarDivAprovarRealizacao;
	}

	public void setMostrarDivAprovarRealizacao(Boolean mostrarDivAprovarRealizacao) {
		this.mostrarDivAprovarRealizacao = mostrarDivAprovarRealizacao;
	}

	public String showRealizar(ProcessoPericia obj) {
		ProcessoDocumentoHome pdHome = ProcessoDocumentoHome.instance();
		pdHome.newInstance();
		ProcessoTrfHome processoTrfHome = ComponentUtil.getComponent("processoTrfHome");
		processoTrfHome.setInstance(obj.getProcessoTrf());
		pdHome.setModelo(true);
		setInstance(obj);
		mostrarDivRealizacao = Boolean.TRUE;
		
		ProcessoHome.instance().setInstance(obj.getProcessoTrf().getProcesso());
		
		AnexarDocumentos.instance().actionAbaAnexar();
		
		return "/Painel/Perito/listView.seam";
	}

	public void closeRealizar() {
		mostrarDivRealizacao = Boolean.FALSE;
		ausenciaParte = Boolean.FALSE;
		mostrarDivRealizarPericia = Boolean.TRUE;
		setMostrarBotaoAnexarDoc(Boolean.FALSE);
		getEntityManager().flush();
	}

	public Boolean verificaPagamentoProcesso(ProcessoPericia obj) {
		String query = "select count(o) from PagamentoPericia o " + "where pagamento = 'R' "
				+ "and processoPericia.idProcessoPericia = :pPericia";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("pPericia", obj.getIdProcessoPericia());
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	public Boolean getVincularDocLaudo() {
		return vincularDocLaudo;
	}

	public void setVincularDocLaudo(Boolean vincularDocLaudo) {
		this.vincularDocLaudo = vincularDocLaudo;
	}

	public void setAusenciaParte(Boolean ausenciaParte) {
		this.ausenciaParte = ausenciaParte;
	}

	public Boolean getAusenciaParte() {
		return ausenciaParte;
	}

	public void setMostrarDivRealizarPericia(Boolean mostrarDivRealizarPericia) {
		this.mostrarDivRealizarPericia = mostrarDivRealizarPericia;
	}

	public Boolean getMostrarDivRealizarPericia() {
		return mostrarDivRealizarPericia;
	}

	public void anexarDocumento() {
		ProcessoDocumentoHome pdHome = ProcessoDocumentoHome.instance();
		ProcessoTrfHome.instance().setInstance(getInstance().getProcessoTrf());
		pdHome.getInstance().setProcesso(getInstance().getProcessoTrf().getProcesso());

		FileHome file = FileHome.instance();
		if (file.getSize() > 0) {
			pdHome.setModelo(false);
		}

		String ret = pdHome.persist();

		if (!ret.equals("")) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Arquivo vinculado com sucesso.");
			pdHome.getInstance().setTipoProcessoDocumento(null);
			pdHome.getInstance().setProcessoDocumento(null);
		}
	}

	public void persistParteAusente() {
		instance.setStatus(PericiaStatusEnum.A);
		EntityManager em = getEntityManager();
		em.merge(getInstance());
		em.flush();
		closeRealizar();
		AnexarDocumentos.instance().limparTela();
	}

	public PericiaStatusEnum getPericiaStatus() {
		return periciaStatus;
	}

	public void setPericiaStatus(PericiaStatusEnum periciaStatus) {
		this.periciaStatus = periciaStatus;
	}

	public PericiaStatusEnum[] getPericiaStatusItems() {
		return PericiaStatusEnum.values();
	}

	public PessoaPerito getPessoaPerito() {
		return pessoaPerito;
	}

	public void setPessoaPerito(PessoaPerito pessoaPerito) {
		this.pessoaPerito = pessoaPerito;
	}

	public Date getDtMarcacaoInicio() {
		return dtMarcacaoInicio;
	}

	public void setDtMarcacaoInicio(Date dtMarcacaoInicio) {
		this.dtMarcacaoInicio = dtMarcacaoInicio;
	}

	public Date getDtMarcacaoFim() {
		return dtMarcacaoFim;
	}

	public void setDtMarcacaoFim(Date dtMarcacaoFim) {
		this.dtMarcacaoFim = dtMarcacaoFim;
	}

	public Localizacao getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(Localizacao orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public TitularidadeOrgaoEnum getTitularidade() {
		return titularidade;
	}

	public void setTitularidade(TitularidadeOrgaoEnum titularidade) {
		this.titularidade = titularidade;
	}

	public TitularidadeOrgaoEnum[] getTitularidadeItems() {
		return TitularidadeOrgaoEnum.values();
	}

	public Especialidade getEspecialidade() {
		return especialidade;
	}

	public void setEspecialidade(Especialidade especialidade) {
		this.especialidade = especialidade;
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

	public String getNomePericiado() {
		return nomePericiado;
	}

	public void setNomePericiado(String nomePericiado) {
		this.nomePericiado = nomePericiado;
	}

	public Date getDataMarcacaoInicial() {
		return dataMarcacaoInicial;
	}

	public void setDataMarcacaoInicial(Date dataMarcacaoInicial) {
		this.dataMarcacaoInicial = dataMarcacaoInicial;
	}

	public Date getDataMarcacaoFinal() {
		return dataMarcacaoFinal;
	}

	public void setDataMarcacaoFinal(Date dataMarcacaoFinal) {
		this.dataMarcacaoFinal = dataMarcacaoFinal;
	}

	/**
	 * Método para verificar verificar se um processo possui tarefa aberta.
	 */
	public boolean verificarProcessoTarefaAberta(ProcessoPericia processoPericia) {
		EntityManager em = getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select count(o) from SituacaoProcesso o ");
		sql.append("where idProcesso = :nrProcesso");
		Query query = em.createQuery(sql.toString());

		query.setParameter("nrProcesso", processoPericia.getProcessoTrf().getIdProcessoTrf());
		try {
			Long retorno = (Long) query.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	/**
	 * Método utilizado para verificar se o perito tem alguma indisponibilidade.
	 * 
	 * @param dataMarcada
	 * @param horaMarcada
	 * @return Boolean
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	public Boolean isIndisponibilidadePerito(Date dataMarcada, Time horaMarcada) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaPeritoIndisponibilidade o ");
		sb.append("where o.pessoaPeritoEspecialidade.pessoaPerito.idUsuario = :idPerito ");
		sb.append("and o.ativo = true ");
		sb.append("and o.pessoaPeritoEspecialidade.especialidade.idEspecialidade = :idEspecialidade ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idPerito", getInstance().getPessoaPerito().getIdUsuario());
		q.setParameter("idEspecialidade", getInstance().getEspecialidade().getIdEspecialidade());
		List<PessoaPeritoIndisponibilidade> listIndiponibilidade = q
				.getResultList();

		for (PessoaPeritoIndisponibilidade list : listIndiponibilidade) {
			if (getInstance().getDataMarcacao().getTime() >= list.getDtInicio().getTime()
					&& getInstance().getDataMarcacao().getTime() <= list.getDtFim().getTime()) {
				if (proximoHorarioFila.getHours() >= list.getHoraInicio().getHours()
						&& proximoHorarioFila.getHours() <= list.getHoraFim().getHours()) {
					proximoHorarioFila = null;
					FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Perito não disponível nesta Data");
					return false;
				}
			}
		}

		return true;
	}

	public void limparListas() {
		proximoHorarioFila = null;
		pessoaPeritoList = null;
	}

	/**
	 * Método para assinar o documento na realização da perícia.
	 * 
	 * @return
	 */
	public String assinarPericia() {
		if (ProcessoDocumentoHome.instance().getInstance() == null || !getEntityManager().contains(ProcessoDocumentoHome.instance().getInstance()) || ProcessoDocumentoHome.instance().getInstance().getTipoProcessoDocumento() == null) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Favor preencher corretamente o documento.");
			return null;
		}
		getInstance().setProcessoDocumento(ProcessoDocumentoHome.instance().getInstance());
		getInstance().setStatus(PericiaStatusEnum.P);
		if (!getEntityManager().contains(getInstance())) {
			getEntityManager().merge(getInstance());
		}
		super.persist();

		setMostrarBotaoAnexarDoc(Boolean.TRUE);
		FacesMessages.instance().clear();
		FacesMessages.instance().add(StatusMessage.Severity.INFO, "Documento assinado com sucesso");
		
		closeRealizar();
		AnexarDocumentos.instance().limparTela();
		
		return null;
	}

	public void setPessoaPericiado(String pessoaPericiado) {
		this.pessoaPericiado = pessoaPericiado;
	}

	public String getPessoaPericiado() {
		return pessoaPericiado;
	}
	
	public void marcaNovaPericia() {
		this.mostraDivDesignarPericia();
	}
	
	public void voltarDefinicaoDataPericia() {
		this.setVisibleDivGridHoraDisponivel(Boolean.FALSE);
		Boolean possuiPericias = this.isPossuiPericiasAgendadas();
		this.setVisibleDivPericia(possuiPericias);
		this.mostraDivDesignarPericia();
	}
	
	private void mostraDivDesignarPericia() {
		this.setVisibleDivMarcaPericia(Boolean.TRUE);
		this.setMostrarBtnDesignar(Boolean.FALSE);
	}
	
	private void ocultaDivDesignarPericia() {
		this.setVisibleDivMarcaPericia(Boolean.FALSE);
		this.setMostrarBtnDesignar(Boolean.TRUE);		
	}
	
	private Boolean isPossuiPericiasAgendadas() {
		StringBuilder sb = new StringBuilder()
			.append("select count(pp) from ProcessoPericia pp ")
			.append("WHERE ")
			.append(" pp.processoTrf = :processo ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processo", ProcessoTrfHome.instance().getInstance());
		
		Long qtde = (Long) q.getSingleResult();

		return qtde > 0;
	}
	
	public void consultarHorariosDisponiveis() {
		//Pela regra, o cadastro manual pode ser feito de forma retroativa.
		boolean isSomenteHorarioFuturo = false;
		try {
			horariosDisponiveis = periciaService.obterHorariosPerito(instance.getEspecialidade(), instance.getPessoaPerito(), this.dataSelecionada, isSomenteHorarioFuturo);
			
			if (horariosDisponiveis.isEmpty()) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Não há horários disponíveis para a data escolhida!");
			}
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getCode(), e);
		}
	}
	
	public Boolean isPermiteDesignarPericia() {
		return Authenticator.isUsuarioInterno() && this.getMostrarBtnDesignar();
	}
	
	private void movimentarProcesso(String variavel, ProcessoPericia pericia) {
		TramitacaoProcessualService tps = ComponentUtil.getTramitacaoProcessualService();
 		String transicaoPadrao = (String) tps.recuperaVariavelTarefa(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
 		
 		tps.gravaVariavel(Variaveis.PJE_FLUXO_PERICIA, pericia);
 		tps.gravaVariavel(Eventos.EVENTO_SINALIZACAO, variavel);
 		
 		if (StringUtils.isNotBlank(transicaoPadrao)) {
 			TaskInstanceHome.instance().end(transicaoPadrao);
 		}
 		
 		ProcessoJudicialService pjs = ComponentUtil.getComponent(ProcessoJudicialService.class);
 		Map<String, Object> novasVariaveis = new HashMap<String, Object>();
    	novasVariaveis.put(Variaveis.PJE_FLUXO_PERICIA, pericia);
 		pjs.sinalizarFluxo(tps.recuperaProcesso(), variavel, true, true, true, novasVariaveis);
	}

	public List<DesignarPericia> getHorariosDisponiveis() {
		return horariosDisponiveis ;
	}
	
	public void setHorarioPericia(DesignarPericia disponibilidade) {
		this.disponibilidadePeritoEscolhida = disponibilidade;
		setExibeModalConfirmacao(true);
	}
	
	public String getHorarioPericiaFormatado() {
		return DateUtil.getDataFormatada(this.disponibilidadePeritoEscolhida.getDataHora().getTime(), "HH:mm");
	}
	
	public String getDataPericiaFormatada() {
		return DateUtil.getDataFormatada(this.dataSelecionada.getTime(), "dd/MM/yyyy");
	}
	
	public boolean isExibeModalConfirmacao() {
		return exibeModalConfirmacao;
	}
	
	public void setExibeModalConfirmacao(boolean exibeModalConfirmacao) {
		this.exibeModalConfirmacao = exibeModalConfirmacao;
	}

	private void limpaDadosHorario() {
		this.horariosDisponiveis = new ArrayList<>();
		this.setExibeModalConfirmacao(false);
		this.disponibilidadePeritoEscolhida = null;
		this.dataSelecionada = Calendar.getInstance();
	}	
	
}
