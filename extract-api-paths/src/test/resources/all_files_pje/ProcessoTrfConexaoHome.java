package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.faces.component.UIComponent;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.compass.core.util.StringUtils;
import org.hibernate.AssertionFailure;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jbpm.taskmgmt.exe.SwimlaneInstance;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.component.suggest.NumeroProcessoConexoSuggestBean;
import br.com.infox.cliente.component.suggest.NumeroProcessoTrfOrgaoJulgadorSuggestBean;
import br.com.infox.cliente.component.suggest.OrgaoJulgadorSuggestBean;
import br.com.infox.cliente.component.tree.ProcessoTrfConexaoTreeHandler;
import br.com.infox.component.tree.SearchTree2GridList;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.ibpm.jbpm.assignment.LocalizacaoAssignment;
import br.com.infox.trf.distribuicao.DistribuicaoHome;
import br.com.infox.trf.distribuicao.VaraTitulacao;
import br.com.infox.trf.eventos.DefinicaoEventos;
import br.com.infox.trf.webservice.MantemProcessosPreventos;
import br.com.itx.component.Util;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.PjeRestClientException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.InformacaoCriminalRascunhoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoRascunhoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfConexaoManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.view.fluxo.AnexarDocumentoPreventoAction;
import br.jus.cnj.pje.view.fluxo.ProcessoJudicialAction;
import br.jus.cnj.pje.webservice.client.criminal.ProcessoCriminalRestClient;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.je.entidades.ComplementoProcessoJE;
import br.jus.pje.nucleo.dto.InformacaoCriminalDTO;
import br.jus.pje.nucleo.dto.ParteDTO;
import br.jus.pje.nucleo.dto.ProcessoCriminalDTO;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRascunho;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.log.EntityLog;
import br.jus.pje.nucleo.entidades.log.EntityLogDetail;
import br.jus.pje.nucleo.enums.PrevencaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.TipoConexaoEnum;
import br.jus.pje.nucleo.enums.TipoOperacaoLogEnum;

@Name("processoTrfConexaoHome")
@BypassInterceptors
public class ProcessoTrfConexaoHome extends AbstractProcessoTrfConexaoHome<ProcessoTrfConexao> {

	private static final long serialVersionUID = 1L;
	private ProcessoTrf processoSelecionado;
	private ProcessoTrf processoEmEdicao;
	private ProcessoTrf processoAssociadoTransient;
	private List<ProcessoParte> processoParteList = new ArrayList<>(0);
	private List<ProcessoDocumento> processoDocumentoList = new ArrayList<>(0);
	private List<ProcessoAssunto> processoAssuntoList = new ArrayList<>(0);
	private SearchTree2GridList<ProcessoTrf> searchTree2GridList;
	private boolean rendaba;
	private String numeroCPF;
	private String numeroCNPJ;
	private TipoPessoa tipoPessoa;
	private String nomeParte;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private Date dtInicio;
	private Date dtFim;
	private Boolean cpfCnpj = Boolean.FALSE;
	private Boolean competencia;
	private List<ProcessoTrf> processosAssociados = new ArrayList<>(0);
	private String grid = "analisePrevencaoGrid";
	private Boolean habilitaAbaAssociados = Boolean.TRUE;
	private Date periodo;
	private Date dtPossivelPrevencao1;
	private Date dtPossivelPrevencao2;
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgador orgaoJulgadorConexo;
	private ProcessoTrfConexao conexao;
	private Boolean prevento = Boolean.FALSE;
	private Boolean existeProcessosAssociadosPersistidos = Boolean.FALSE;
	private List<ProcessoTrfConexao> processosTRFConexoes = new ArrayList<>(0);
	private List<ProcessoTrfConexao> processosTRFConexosAssociadosAoPrincipal = new ArrayList<>();
	private TipoConexaoEnum tipoConexao;
	private String justificativa;
	private List<ProcessoTrfConexao> processoAssociadoList;
	private String numeroProcessoAssociado;
	private String orgaoJulgadorAssociado;
	private PrevencaoEnum confirmacao;
	private ProtocolarDocumentoBean protocolarDocumentoBean;

	private boolean immediateTaskButton;

	private boolean flagMostraModalRemocaoCaracteresEspeciais = false;
	
	private boolean isAssinado;
	private boolean isMinutaGravada;
	
	private boolean telaAnexarDocumentoPrevento;
	private boolean btnSalvarDesabilitado = Boolean.FALSE;
	
	private static final LogProvider log = Logging.getLogProvider(ProcessoTrfConexaoHome.class);
	private static Object lock = new Object();

	public boolean isTelaAnexarDocumentoPrevento() {
		return telaAnexarDocumentoPrevento;
	}

	public void setTelaAnexarDocumentoPrevento(boolean telaAnexarDocumentoPrevento) {
		this.telaAnexarDocumentoPrevento = telaAnexarDocumentoPrevento;
	}
	
	public Boolean getExisteProcessosAssociadosPersistidos() {
		return existeProcessosAssociadosPersistidos;
	}

	public void setExisteProcessosAssociadosPersistidos(Boolean existeProcessosAssociadosPersistidos) {
		this.existeProcessosAssociadosPersistidos = existeProcessosAssociadosPersistidos;
	}

	@Override
	public void setId(Object id) {
		super.setId(id);
	}

	public Date getPeriodo() {
		return periodo;
	}

	public void setPeriodo(Date periodo) {
		this.periodo = periodo;
	}

	public Date getDtPossivelPrevencao1() {
		return dtPossivelPrevencao1;
	}

	public void setDtPossivelPrevencao1(Date dtPossivelPrevencao1) {
		this.dtPossivelPrevencao1 = dtPossivelPrevencao1;
	}

	public Date getDtPossivelPrevencao2() {
		return dtPossivelPrevencao2;
	}

	public void setDtPossivelPrevencao2(Date dtPossivelPrevencao2) {
		this.dtPossivelPrevencao2 = dtPossivelPrevencao2;
	}

	public static ProcessoTrfConexaoHome instance() {
		return ComponentUtil.getComponent(ProcessoTrfConexaoHome.class);
	}

	public void setGrid(String grid) {
		this.grid = grid;
	}
	
	public void addParte(ProcessoParte parte, String componentName) {
		this.processoParteList.add(parte);
		refreshGrid(componentName);
	}
	
	public void removeParte(ProcessoParte parte, String componentName) {
		this.processoParteList.remove(parte);
		refreshGrid(componentName);
	}
	
	public void addDocumento(ProcessoDocumento documento, String componentName) {
		this.processoDocumentoList.add(documento);
		refreshGrid(componentName);
	}
	
	@SuppressWarnings("unchecked")
	public void addAllDocumentos(String componentName) {
		GridQuery gridQuery = ((GridQuery)ComponentUtil.getComponent(componentName));
		
		this.processoDocumentoList.clear();
		this.processoDocumentoList.addAll(gridQuery.getFullList());
		
		refreshGrid(componentName);
	}
	
	public void removeDocumento(ProcessoDocumento documento, String componentName) {
		this.processoDocumentoList.remove(documento);
		refreshGrid(componentName);
	}
	
	public void removeAllDocumentos(String componentName) {
		this.processoDocumentoList.clear();
		refreshGrid(componentName);
	}
	
	public void addAssunto(ProcessoAssunto assunto, String componentName) {
		this.processoAssuntoList.add(assunto);
		refreshGrid(componentName);
	}
	
	public void removeAssunto(ProcessoAssunto assunto, String componentName) {
		this.processoAssuntoList.remove(assunto);
		refreshGrid(componentName);
	}

	public void refreshGrids() {
		rendaba = Boolean.FALSE;
		btnSalvarDesabilitado = Boolean.FALSE;
		processoAssuntoList.clear();
		processoDocumentoList.clear();
		processoParteList.clear();

		refreshGrid("processoParteDesmembramentoGrid");
		refreshGrid("processoDocumentoDesmembramentoGrid");
		refreshGrid("processoAssuntoDesmembramentoGrid");
	}

	public void desmembrar() {
		if (validarProcesso()) {
			setRendaba(Boolean.TRUE);
		}
	}

	private void separaPartesPassivaEAtiva(List<ProcessoParte> pa,	List<ProcessoParte> pp){
		for (ProcessoParte processoParte : processoParteList) {
			if (processoParte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.A)) {
				pa.add(processoParte);
			} else {
				pp.add(processoParte);
			}
		}
	}
	
	private Boolean validarProcesso() {
		Integer qtdErros = 0;
		int idTipoProcessoDocumentoPeticaoInicial = 0;
		
		Boolean peticaoInicial = Boolean.FALSE;
		
		if(processoDocumentoList != null && !processoDocumentoList.isEmpty()){
			idTipoProcessoDocumentoPeticaoInicial = processoDocumentoList.get(0).getProcessoTrf()
					.getClasseJudicial().getTipoProcessoDocumentoInicial().getIdTipoProcessoDocumento(); 
		}

		for (ProcessoDocumento procDoc : processoDocumentoList) {
			if (procDoc.getTipoProcessoDocumento().getIdTipoProcessoDocumento()==idTipoProcessoDocumentoPeticaoInicial) {
				peticaoInicial = Boolean.TRUE;
			}
		}

		if (!peticaoInicial) {
			FacesMessages.instance().add(StatusMessage.Severity.INFO,
					"O Processo é Inicial. Logo, é necessário uma Petição Inicial anexada a ele.");
			qtdErros++;
		}

		// Verifica a quantidade de assuntos vinculados ao processo. Deve existir pelo menos 1.
		if (processoAssuntoList.size() == 0) {
			FacesMessages.instance().add(StatusMessage.Severity.INFO,
					"Deve haver ao menos um assunto Vinculado ao Processo.");
			qtdErros++;
		}

		// Setando as listas de poloAtivo e poloPassivo
		List<ProcessoParte> pa = new ArrayList<ProcessoParte>(0);
		List<ProcessoParte> pp = new ArrayList<ProcessoParte>(0);
		separaPartesPassivaEAtiva(pa, pp);
		

		// Verifica se existe pelo menos uma parte no polo ativo.
		Integer tamAtivo = pa.size();
		if (tamAtivo == 0) {
			FacesMessages.instance().add(StatusMessage.Severity.INFO,
					"Deve haver ao menos uma parte no polo ativo vinculada ao processo.");
			qtdErros++;
		}

		// Verifica se existe pelo menos uma parte no polo passivo.
		Integer tamPassivo = pp.size();
		if (tamPassivo == 0) {
			FacesMessages.instance().add(StatusMessage.Severity.INFO,
					"Deve haver ao menos uma parte no polo passivo vinculada ao processo.");
			qtdErros++;
		}

		/**
		 * Verificação se o usuário selecionou todas as partes para o desmembramento. 
		 * Caso selecionou todas as partes dos dois polos, proíbe o desmembramento,
		 * caso selecionou todas as partes de apenas um polo, 
		 * as partes desse polo serao copiadas para os dois processos.  
		 */
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		if (processoTrf.getProcessoParteList().size() <= processoParteList.size()){
			FacesMessages.instance().add(StatusMessage.Severity.INFO,
					"Não podem ser movidas para o novo processo todas as partes vinculadas ao processo originário.");
			qtdErros++;
		}
		else{
			if (tamAtivo == processoTrf.getListaParteAtivo().size()) {
				FacesMessages.instance().add(StatusMessage.Severity.INFO,
						"Todas as partes do polo ativo foram selecionadas, então elas serão vinculadas aos dois processos.");
			}
			if (tamPassivo == processoTrf.getListaPartePassivo().size()) {
				FacesMessages.instance().add(StatusMessage.Severity.INFO,
						"Todas as partes do polo passivo foram selecionadas, então elas serão vinculadas aos dois processos.");
			}
		}

		if (qtdErros == 0) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	public ProcessoStatusEnum validar(ProcessoTrf processoTrf) {
		if (validarProcesso()) {
			processoTrf.setProcessoStatus(ProcessoStatusEnum.V);
			processoTrf.setDataAutuacao(new Date());
			getEntityManager().merge(processoTrf);
			EntityUtil.flush();

			Fluxo fluxo = processoTrf.getClasseJudicial().getFluxo();
			if (fluxo != null) {
				if (fluxo.getPublicado()) {
					try {
						executarDistribuicao(fluxo, processoTrf);
					} catch (Exception e) {
						e.printStackTrace();
						FacesMessages.instance().add(Severity.ERROR, "Erro ao distribuir: " + e.getMessage());
					}
				}
			}

			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Processo Cadastrado com Sucesso.");
		} else
			processoTrf.setProcessoStatus(ProcessoStatusEnum.E);

		return processoTrf.getProcessoStatus();
	}

	public void executarDistribuicao(Fluxo fluxo, ProcessoTrf processoTrf) throws Exception {
		VaraTitulacao varaTitulacao;
		synchronized (lock) {
			processoTrf.setAssuntoTrfList(new ArrayList<>(0));
			if (processoTrf.getProcessoAssuntoList() != null && !processoTrf.getProcessoAssuntoList().isEmpty()) {
				for (ProcessoAssunto processoAssunto : processoTrf.getProcessoAssuntoList()) {
					processoTrf.getAssuntoTrfList().add(processoAssunto.getAssuntoTrf());
				}
			}
			varaTitulacao = DistribuicaoHome.executarSorteio(processoTrf, null);
			processoTrf.setAssuntoTrfList(new ArrayList<>(0));
			processoTrf.setOrgaoJulgador(varaTitulacao.getOrgaoJulgador());
			processoTrf.setCargo(varaTitulacao.getCargo());
			processoTrf.setProcessoStatus(ProcessoStatusEnum.D);
			processoTrf.setDataDistribuicao(new Date());
			getEntityManager().merge(processoTrf);
			getEntityManager().flush();
		}
		Map<String, Object> variaveis = new HashMap<String, Object>();
		variaveis.put("orgaoJulgador", varaTitulacao.getOrgaoJulgador().getIdOrgaoJulgador());
		variaveis.put("titularidade", varaTitulacao.getCargo().getSigla());
		variaveis.put("desmembrado", true);
		ProcessoHome.instance().setInstance(processoTrf.getProcesso());
		ProcessoTrfHome.instance().setId(ProcessoHome.instance().getInstance().getIdProcesso());
		Contexts.getEventContext().set(Variaveis.PJE_FLUXO_VARIABLES_STARTSTATE , variaveis);
		ProcessoHome.instance().iniciarProcessoJbpm(fluxo, variaveis);
		SwimlaneInstance swimlaneInstance = org.jboss.seam.bpm.TaskInstance.instance().getSwimlaneInstance();
		String actorsExpression = swimlaneInstance.getSwimlane().getPooledActorsExpression();
		LocalizacaoAssignment.instance().setPooledActors(actorsExpression);
	}

	public void setIdProcessoSelecionado(Object id) {
		boolean idProcessoChanged = id != null && !(((Integer) Integer.parseInt((String)id)).equals(ProcessoTrfHome.instance().getId()));
		if (idProcessoChanged) { 
			ProcessoTrfHome.instance().setId(id);
			setProcessoSelecionado(ProcessoTrfHome.instance().getInstance());
		}
	}
	
	public Object getIdProcessoSelecionado(){
		if (processoSelecionado != null)
			return processoSelecionado.getIdProcessoTrf();
		else
			return null; 
	}

	public void gravar() {
		try {
			Util.beginTransaction();
			
			Integer numeroOrigem;
			ProcessoTrf processoTrfAntigo = ProcessoTrfHome.instance().getInstance();
			ProcessoTrf processoTrfNovo = EntityUtil.cloneEntity(processoTrfAntigo, Boolean.FALSE);

			Processo processoAntigo = processoTrfAntigo.getProcesso();
			Processo processoNovo = EntityUtil.cloneEntity(processoAntigo, Boolean.FALSE);
			
			if (processoTrfAntigo.getComplementoJE() != null) {
				ComplementoProcessoJE complementoProcessoJENovo = EntityUtil.cloneEntity(processoTrfAntigo.getComplementoJE(), Boolean.FALSE);
				complementoProcessoJENovo.setProcessoTrf(processoTrfNovo);
				processoTrfNovo.setComplementoJE(complementoProcessoJENovo);
			}

			processoNovo.setNumeroProcesso(null);
			processoTrfNovo.setNumeroSequencia(null);

			try {
				getEntityManager().persist(processoNovo);
				getEntityManager().flush();
			} catch (AssertionFailure e) {
				// Ignore
			}

			int idProcesso = processoNovo.getIdProcesso();
			try {
				getEntityManager().clear();
				processoNovo = getEntityManager().find(Processo.class, idProcesso);
				processoTrfNovo.setProcesso(processoNovo);
				processoTrfNovo.setIdProcessoTrf(idProcesso);
				processoTrfNovo.setProcessoOriginario(processoTrfAntigo);
				getEntityManager().persist(processoTrfNovo);
				getEntityManager().flush();
			} catch (AssertionFailure e) {
				// Ignore
			}

			numeroOrigem = processoTrfNovo.getJurisdicao().getNumeroOrigem();

			if (numeroOrigem == null) {
				FacesMessages.instance().add(StatusMessage.Severity.INFO, "A jurisdição do processo está sem código de origem definido.");
				Util.rollbackTransaction();
				return;
			}

			NumeroProcessoUtil.numerarProcesso(processoTrfNovo, processoTrfAntigo.getNumeroOrgaoJustica(), numeroOrigem);
			String numeroProcesso = NumeroProcessoUtil.formatNumeroProcesso(processoTrfNovo);
			processoNovo.setNumeroProcesso(numeroProcesso);
			getEntityManager().merge(processoNovo);
			getEntityManager().flush();

			atualizaOuCopiaPartes(processoTrfAntigo, processoTrfNovo);
			
			copiarDocumentosProcessoDesmembrado(processoNovo, processoDocumentoList);

			for (ProcessoAssunto assunto : processoAssuntoList) {
				updateProcessoAssunto(assunto, processoTrfNovo);
			}
			
			copiarMovimentacaoProcessoDesmembrado(processoTrfNovo, processoTrfAntigo);

			/* validar se é processo criminal. */
			if (processoTrfAntigo.getCompetencia().toString().equals("Criminal")) {
				criarProcessoDMBCriminal(processoTrfAntigo, processoTrfNovo);
			}

			FacesMessages.instance().clear();
			if (!validar(processoTrfNovo).equals(ProcessoStatusEnum.E)) {
				
				getInstance().setProcessoTrfConexo(processoTrfNovo);
				getInstance().setProcessoTrf(processoTrfAntigo);
				getInstance().setNumeroProcesso(processoTrfNovo.getNumeroProcesso());
				getInstance().setOrgaoJulgador(processoTrfNovo.getOrgaoJulgador().getOrgaoJulgador());
				getInstance().setSessaoJudiciaria(processoTrfNovo.getJurisdicao().getJurisdicao());
				getInstance().setTipoConexao(TipoConexaoEnum.DM);

				setProcessoSelecionado(processoTrfAntigo);
				setProcessoAssociadoTransient(processoTrfNovo);

				persist(getInstance());

				ProcessoTrfConexao processoTrfConexaoAntigo = new ProcessoTrfConexao();
				processoTrfConexaoAntigo.setProcessoTrfConexo(processoTrfAntigo);
				processoTrfConexaoAntigo.setProcessoTrf(processoTrfNovo);
				processoTrfConexaoAntigo.setNumeroProcesso(processoTrfAntigo.getNumeroProcesso());
				processoTrfConexaoAntigo.setOrgaoJulgador(processoTrfAntigo.getOrgaoJulgador().getOrgaoJulgador());
				processoTrfConexaoAntigo.setSessaoJudiciaria(processoTrfAntigo.getJurisdicao().getJurisdicao());
				processoTrfConexaoAntigo.setTipoConexao(TipoConexaoEnum.DM);

				setProcessoSelecionado(processoTrfNovo);
				setProcessoAssociadoTransient(processoTrfAntigo);

				persist(processoTrfConexaoAntigo);

				this.btnSalvarDesabilitado = Boolean.TRUE;
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.INFO,
						"Os atributos foram desmembrados para o processo: " + processoTrfNovo.getNumeroProcesso());
				
				Util.commitTransction();
				
			} else {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.INFO, "O processo não pode ser protocolado.");
				
				Util.rollbackTransaction();
			}
		} catch (Exception e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Erro ao desmenbrar o processo: " + e.getLocalizedMessage());
			log.error(e.getLocalizedMessage(), e);
			
			Util.rollbackTransaction();
		}
	}

	private void criarProcessoDMBCriminal(ProcessoTrf procTrfAnt, ProcessoTrf procTrfAntnovo)
			throws PJeBusinessException {

		ProcessoRascunhoManager processoRascunhoManager = ComponentUtil.getComponent(ProcessoRascunhoManager.class);
		InformacaoCriminalRascunhoManager icManager = ComponentUtil
				.getComponent(InformacaoCriminalRascunhoManager.class);
		ProcessoCriminalDTO procCriminalDTOantigo = processoRascunhoManager
				.recuperarRascunhoProcessoCriminal(procTrfAnt);
		ProcessoCriminalDTO procCriminalDTOnovo = processoRascunhoManager.recuperarRascunhoProcessoCriminal(procTrfAnt);

		try {
			ProcessoCriminalRestClient processoCriminalRestClient = ComponentUtil
					.getComponent(ProcessoCriminalRestClient.class);
			procCriminalDTOnovo.setNrProcesso(procTrfAntnovo.getProcesso().getNumeroProcesso());
			procCriminalDTOnovo.setPjeOrigem(ConfiguracaoIntegracaoCloud.getAppName());

			procCriminalDTOnovo = processoCriminalRestClient.createResource(procCriminalDTOantigo);
			List<InformacaoCriminalRascunho> icRascunhos = icManager
					.findAllByIdProcessoTrf(procTrfAnt.getIdProcessoTrf());
			List<InformacaoCriminalDTO> listaInfo = new ArrayList<InformacaoCriminalDTO>();

			for (InformacaoCriminalRascunho ic : icRascunhos) {
				ic = icManager.refresh(ic);
				InformacaoCriminalDTO info = new InformacaoCriminalDTO();
				info.setConteudo(ic.getInformacaoCriminal());
				info.setParte(new ParteDTO(ic.getProcessoParte().getId(), ic.getProcessoParte().getIdPessoa(), null,
						ic.getProcessoParte().getSituacao()));
				listaInfo.add(info);
			}

			if (!CollectionUtilsPje.isEmpty(listaInfo)) {
				processoCriminalRestClient.inserirInformacoesCriminaisAoProcessoCriminal(procCriminalDTOnovo,
						listaInfo);
			}

			MovimentoAutomaticoService.preencherMovimento().deCodigo(DefinicaoEventos.COD_MOVIMENTO_DESMEMBRADO_FEITO)
					.associarAoProcesso(procTrfAnt).lancarMovimento();
		} catch (PjeRestClientException e) {
			log.error(e);
			String mensagem = e.obterMensagemErroDetail();
			throw new PJeRuntimeException(mensagem, e);
		} catch (Exception e) {
			String msg = "Erro ao distribuir processo criminal: " + e.getMessage();
			e.printStackTrace();
			// this.textosProtocolacao[INDEX_ERRO] = msg;
			throw new PJeBusinessException(msg);
		}
	}

	/**
	 * Operação para copiar histórico das movimentações de um processo.
	 * 
	 * @param processoDestingo
	 * @param processoOrigem
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws PJeDAOException
	 * @throws PJeBusinessException
	 */
	private void copiarMovimentacaoProcessoDesmembrado(ProcessoTrf processoDestingo, ProcessoTrf processoOrigem) 
			throws InstantiationException, IllegalAccessException, PJeDAOException, PJeBusinessException {
		
		ProcessoEventoManager processoEventoManager = ComponentUtil.getComponent(ProcessoEventoManager.class);
		List<ProcessoEvento> listProcessoEventos = processoEventoManager.recuperaMovimentos(processoOrigem, null, true);
		for (ProcessoEvento movimentacao : listProcessoEventos) {
			processoEventoManager.persist(copiarMovimentoProcesso(movimentacao, processoDestingo));
		}
	}
	
	/**
	 * Operação que trata uma movimentação para um processo em específico.
	 * 
	 * @param movimentacao
	 * @param processo
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private ProcessoEvento copiarMovimentoProcesso(ProcessoEvento movimentacao, ProcessoTrf processo) 
			throws InstantiationException, IllegalAccessException {
		
		ProcessoEvento movimentacaoNova = EntityUtil.cloneEntity(movimentacao, false);
		movimentacaoNova.setProcesso(processo.getProcesso());
		return movimentacaoNova;
	}

	/**
	 * Verificação se o usuário selecionou todas as partes de um polo para o desmembramento. 
	 * caso selecionou as partes desse polo serao copiadas para os dois processos.
	 * senão, são movidas para o novo processo.  
	 */
	private void atualizaOuCopiaPartes(ProcessoTrf processoTrfAntigo , ProcessoTrf processoTrfNovo){
		//Verificar se um dos polos foi totalmente copiado e nesse caso ao inves de fazer o update, copiar as Partes desse polo.
		// Setando as listas de poloAtivo e poloPassivo
		List<ProcessoParte> pa = new ArrayList<>(0);
		List<ProcessoParte> pp = new ArrayList<>(0);
		separaPartesPassivaEAtiva(pa, pp);
		// Verifica se foram selecionadas todas as partes do polo ativo.
		Integer tamAtivo = pa.size();
		if (tamAtivo == processoTrfAntigo.getListaParteAtivo().size()) {
			processoTrfNovo.getProcessoParteList().addAll(copiaPartesProcesso(pa, processoTrfNovo));
		}
		else{
			updatePartesProcesso(pa, processoTrfNovo);
		}
		// Verifica se foram selecionadas todas as partes do polo passivo.
		Integer tamPassivo = pp.size();
		if (tamPassivo == processoTrfAntigo.getListaPartePassivo().size()) {
			processoTrfNovo.getProcessoParteList().addAll(copiaPartesProcesso(pp, processoTrfNovo));
		}
		else{
			updatePartesProcesso(pp, processoTrfNovo);
		}
	}
	
	private List<ProcessoParte> copiaPartesProcesso(List<ProcessoParte> processoPartes, ProcessoTrf processoTrfNovo ){
		List<ProcessoParte> retorno = new ArrayList<>();
		for (ProcessoParte processoParte : processoPartes){
			retorno.add(copiaProcessoParte(processoParte, processoTrfNovo));
		}
		return retorno;
	}

	private void updatePartesProcesso(List<ProcessoParte> processoPartes, ProcessoTrf processoTrfNovo ){
		for (ProcessoParte processoParte : processoPartes){
			updateProcessoParte(processoParte, processoTrfNovo);
		}
	}

	/**
	 * Copia uma Parte de um processo para outro. 
	 * Utilizado quando são selecionadas todas as partes de um polo. 
	 * Se não copiar, o processo originário ficaria sem partes nesse polo.  
	 * @param processoParte a parte a ser copiada
	 * @param processoTrf o processo de destino da nova parte.
	 */
	private ProcessoParte copiaProcessoParte(ProcessoParte processoParte, ProcessoTrf processoTrf) {
		processoParte = this.getEntityManager().find(ProcessoParte.class, processoParte.getIdProcessoParte());
		EntityManager entityManagerLog = ComponentUtil.getComponent("entityManagerLog");
		EntityLog log = LogUtil.createEntityLog(processoParte);
		log.setTipoOperacao(TipoOperacaoLogEnum.U);
		EntityLogDetail detail = new EntityLogDetail();
		detail.setEntityLog(log);
		detail.setNomeAtributo("processoTrf");
		detail.setValorAnterior(LogUtil.toStringForLog(processoParte.getProcessoTrf()));
		detail.setValorAtual(LogUtil.toStringForLog(processoTrf));
		log.getLogDetalheList().add(detail);
		
		ProcessoParte processoParteNova = new ProcessoParte();
		try {
			processoParteNova = EntityUtil.cloneEntity(processoParte, false);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		processoParteNova.setProcessoTrf(processoTrf);
		getEntityManager().persist(processoParteNova);
		entityManagerLog.persist(log);
		getEntityManager().flush();
		return processoParteNova;
	}

	
	private void updateProcessoParte(ProcessoParte processoParte, ProcessoTrf processoTrf) {
		EntityManager entityManagerLog = ComponentUtil.getComponent("entityManagerLog");
		EntityLog log = LogUtil.createEntityLog(processoParte);
		log.setTipoOperacao(TipoOperacaoLogEnum.U);
		EntityLogDetail detail = new EntityLogDetail();
		detail.setEntityLog(log);
		detail.setNomeAtributo("processoTrf");
		detail.setValorAnterior(LogUtil.toStringForLog(processoParte.getProcessoTrf()));
		detail.setValorAtual(LogUtil.toStringForLog(processoTrf));
		log.getLogDetalheList().add(detail);
		Query query = getEntityManager().createQuery(
				"update ProcessoParte o set " + "o.processoTrf = :processo where o = :processoParte");
		query.setParameter("processo", processoTrf);
		query.setParameter("processoParte", processoParte);
		query.executeUpdate();
		entityManagerLog.persist(log);
		getEntityManager().flush();
	}

	private void copiarDocumentosProcessoDesmembrado(Processo processoNovo, 
			List<ProcessoDocumento> processoDocumentoList) throws Exception {
		
		Map<Integer, List<Integer>> docsEscolhidos = agruparDocsEscolhidosPorPrincipal(processoDocumentoList);
		for (Integer idDocPrincipal : docsEscolhidos.keySet()) {
			ProcessoDocumento docPrincipal = getEntityManager().find(ProcessoDocumento.class, idDocPrincipal);
			ProcessoDocumento docPrincipalNovo = copiarDocumento(docPrincipal, processoNovo, Optional.empty());
			
			List<Integer> idsDocsAnexos = docsEscolhidos.get(idDocPrincipal);
			for (Integer idDocAnexo : idsDocsAnexos) {
				ProcessoDocumento docAnexo = getEntityManager().find(ProcessoDocumento.class, idDocAnexo);
				copiarDocumento(docAnexo, processoNovo, Optional.of(docPrincipalNovo));
			}
		}
	}
	
	/**
	 * Atualiza os documentos do processo durante desmembramento de processo.
	 * 
	 * @param processoDocumento
	 * @param processo
	 * @throws Exception 
	 */
	/*
	private void updateProcessoDocumento(ProcessoDocumento processoDocumento, Processo processo) throws Exception {
		ProcessoDocumento pd = new ProcessoDocumento();
		ProcessoDocumentoBin pdb = new ProcessoDocumentoBin();

		processoDocumento = getEntityManager().find(ProcessoDocumento.class, processoDocumento.getIdProcessoDocumento());
		ProcessoDocumentoBin pdbin = processoDocumento.getProcessoDocumentoBin();

		pdb = EntityUtil.cloneEntity(pdbin, false);
		copiarAssinaturasDocumento(pdb, pdbin);
		pd = EntityUtil.cloneEntity(processoDocumento, false);

		getEntityManager().persist(pdb);

		pd.setProcessoDocumentoBin(pdb);
		pd.setProcesso(processo);

		getEntityManager().persist(pd);
		EntityUtil.flush();
	}
	*/
	
	private ProcessoDocumento copiarDocumento(ProcessoDocumento docOriginal, Processo processoNovo, 
			Optional<ProcessoDocumento> docPrincipalNovo) throws Exception {
		
		ProcessoDocumentoBin pdbinOriginal = docOriginal.getProcessoDocumentoBin();
		ProcessoDocumentoBin pdbinNovo = EntityUtil.cloneEntity(pdbinOriginal, false);
		pdbinNovo.setBinario(pdbinOriginal.getBinario());
		copiarAssinaturasDocumento(pdbinNovo, pdbinOriginal);
		getEntityManager().persist(pdbinNovo);

		ProcessoDocumento docNovo = EntityUtil.cloneEntity(docOriginal, false);
		if (docPrincipalNovo.isPresent()) {
			docNovo.setDocumentoPrincipal(docPrincipalNovo.get());
		}
		docNovo.setProcessoDocumentoBin(pdbinNovo);
		docNovo.setProcesso(processoNovo);
		getEntityManager().persist(docNovo);

		EntityUtil.flush();
		
		return docNovo;
	}
	
	private Map<Integer, List<Integer>> agruparDocsEscolhidosPorPrincipal(List<ProcessoDocumento> processoDocumentoList) {
		Map<Integer, List<Integer>> docsEscolhidos = new HashMap<>();
		
		for (ProcessoDocumento doc : processoDocumentoList) {
			ProcessoDocumento principal = doc.getDocumentoPrincipal();
			if (principal == null) {
				if (!docsEscolhidos.containsKey(doc.getIdProcessoDocumento())) {
					docsEscolhidos.put(doc.getIdProcessoDocumento(), new ArrayList<Integer>());
				}
			}
			else {
				boolean principalJahAdicionado = docsEscolhidos.containsKey(principal.getIdProcessoDocumento());
				if (principalJahAdicionado) {
					docsEscolhidos.get(principal.getIdProcessoDocumento()).add(doc.getIdProcessoDocumento());
				}
				else {
					List<Integer> docsAnexos = new ArrayList<>(Arrays.asList(doc.getIdProcessoDocumento()));
					docsEscolhidos.put(principal.getIdProcessoDocumento(), docsAnexos);
				}
			}
		}
		return docsEscolhidos;
	}
	
	/**
	 * Operação que copia as assinaturas de um documento para um documento de destino.
	 * 
	 * @param docDestino
	 * @param docOrigem
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void copiarAssinaturasDocumento(ProcessoDocumentoBin docDestino, ProcessoDocumentoBin docOrigem) 
			throws InstantiationException, IllegalAccessException{
		
		if(docDestino.getSignatarios() == null){
			docDestino.setSignatarios(new ArrayList<ProcessoDocumentoBinPessoaAssinatura>());
		}
		for (ProcessoDocumentoBinPessoaAssinatura assinatura : docOrigem.getSignatarios()) {
			copiarAssinatura(assinatura, docDestino);
		}
	}
	
	/**
	 * Operação que copia uma assinatura para um documento.
	 * 
	 * @param assinatura
	 * @param documento
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void copiarAssinatura(ProcessoDocumentoBinPessoaAssinatura assinatura, ProcessoDocumentoBin documento) 
			throws InstantiationException, IllegalAccessException {
		
		ProcessoDocumentoBinPessoaAssinatura assinaturaCopiada = EntityUtil.cloneEntity(assinatura, false);
		assinaturaCopiada.setAssinatura(assinatura.getAssinatura());
		assinaturaCopiada.setCertChain(assinatura.getCertChain());
		assinaturaCopiada.setProcessoDocumentoBin(documento);
		assinaturaCopiada.setPessoa(assinatura.getPessoa());
		documento.getSignatarios().add(assinaturaCopiada);
	}

	private void updateProcessoAssunto(ProcessoAssunto processoAssunto, ProcessoTrf processoTrf) {
		ProcessoAssunto pa = new ProcessoAssunto();
		try {
			pa = EntityUtil.cloneEntity(processoAssunto, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		pa.setProcessoTrf(processoTrf);
		if(processoTrf.getAssuntoTrfList() == null){
			processoTrf.setAssuntoTrfList(new ArrayList<AssuntoTrf>());
		}
		getEntityManager().persist(pa);
		EntityUtil.flush();
		processoTrf.getProcessoAssuntoList().add(pa);
	}

	public List<TipoConexaoEnum> tipoConexaoItems() {
		List<TipoConexaoEnum> lista = new ArrayList<TipoConexaoEnum>();
		lista.add(TipoConexaoEnum.DM);
		lista.add(TipoConexaoEnum.DP);
		lista.add(TipoConexaoEnum.PR);
		return lista;
	}
	
	public List<PrevencaoEnum> confirmacaoConexaoItems() {
		List<PrevencaoEnum> lista = new ArrayList<PrevencaoEnum>();
		lista.add(PrevencaoEnum.PE);
		lista.add(PrevencaoEnum.PR);
		return lista;
	}
	
	public List<TipoConexaoEnum> tipoConexaoExternaItems() {
		List<TipoConexaoEnum> lista = new ArrayList<TipoConexaoEnum>();
		lista.add(TipoConexaoEnum.DM);
		lista.add(TipoConexaoEnum.DP);
		lista.add(TipoConexaoEnum.PR);
		lista.add(TipoConexaoEnum.AS);
		return lista;
	}

	public void setarProcessoSelecionado(ProcessoTrfConexao processoSelecionado) {
		searchTree2GridList = null;
		ProcessoTrfConexaoTreeHandler treeHandler = getComponent("processosFilhosSearchTree");
		treeHandler.clearTree();
		setProcessoSelecionado(processoSelecionado.getProcessoTrfConexo());
	}

	public ProcessoTrf getProcessoSelecionado() {
		return processoSelecionado;
	}

	public void setProcessoSelecionado(ProcessoTrf processoSelecionado) {
		this.processoSelecionado = processoSelecionado;
		//setInstancesProcessoTRFConexao(getProcessoTRFConexao(processoSelecionado));
	}

	public ProcessoTrf getProcessoAssociadoTransient() {
		return processoAssociadoTransient;
	}

	public void setProcessoAssociadoTransient(ProcessoTrf processoAssociadoTransient) {
		this.processoAssociadoTransient = processoAssociadoTransient;
	}

	public SearchTree2GridList<ProcessoTrf> getSearchTree2GridList() {
		if (searchTree2GridList == null) {
			ProcessoTrfConexaoTreeHandler treeHandler = getComponent("processosFilhosSearchTree");
			searchTree2GridList = new SearchTree2GridList<ProcessoTrf>(null, treeHandler);
			String[] filterName = new String[0];
			searchTree2GridList.setFilterName(filterName);
			searchTree2GridList.setGrid((GridQuery) getComponent("processosFilhosGrid"));
		}
		return searchTree2GridList;
	}

	public List<ProcessoParte> getProcessoParteList() {
		return processoParteList;
	}

	public void setProcessoParteList(List<ProcessoParte> processoParteList) {
		this.processoParteList = processoParteList;
	}

	public List<ProcessoDocumento> getProcessoDocumentoList() {
		return processoDocumentoList;
	}

	public void setProcessoDocumentoList(List<ProcessoDocumento> processoDocumentoList) {
		this.processoDocumentoList = processoDocumentoList;
	}

	public List<ProcessoAssunto> getProcessoAssuntoList() {
		return processoAssuntoList;
	}

	public void setProcessoAssuntoList(List<ProcessoAssunto> processoAssuntoList) {
		this.processoAssuntoList = processoAssuntoList;
	}

	public boolean isRendaba() {
		return rendaba;
	}

	public void setRendaba(boolean rendaba) {
		this.rendaba = rendaba;
	}

	public void clearCpfCnpj() {
		setNumeroCPF(null);
		setNumeroCNPJ(null);
	}

	public void limparTela(String obj) {
		getOrgaoJulgadorSuggest().setInstance(null);
		numeroCPF = null;
		numeroCNPJ = null;
		tipoPessoa = null;
		nomeParte = null;
		classeJudicial = null;
		orgaoJulgador = null;
		assuntoTrf = null;
		dtInicio = null;
		dtFim = null;
		cpfCnpj = Boolean.FALSE;
		UIComponent form = ComponentUtil.getUIComponent(obj);
		ComponentUtil.clearChildren(form);
	}

	public void Pesquisa() {
		if ((getNomeParte() != null) && (getTipoPessoa() != null)) {
			setCompetencia(Boolean.TRUE);
		} else {
			setCompetencia(null);
		}
		Contexts.getConversationContext().remove(grid);
	}

	public void setNumeroCPF(String numeroCPF) {
		this.numeroCPF = numeroCPF;
	}

	public String getNumeroCPF() {
		return numeroCPF;
	}

	public void setNumeroCNPJ(String numeroCNPJ) {
		this.numeroCNPJ = numeroCNPJ;
	}

	public String getNumeroCNPJ() {
		return numeroCNPJ;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	private OrgaoJulgadorSuggestBean getOrgaoJulgadorSuggest() {
		OrgaoJulgadorSuggestBean orgaoJulgadorSuggest = (OrgaoJulgadorSuggestBean) Component
				.getInstance("orgaoJulgadorSuggest");
		return orgaoJulgadorSuggest;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		if (getOrgaoJulgadorSuggest().getInstance() != null) {
			orgaoJulgador = getOrgaoJulgadorSuggest().getInstance();
		}
		return orgaoJulgador;
	}

	public void setDtFim(Date dtFim) {
		this.dtFim = dtFim;
	}

	public Date getDtFim() {
		return dtFim;
	}

	public void setDtInicio(Date dtInicio) {
		this.dtInicio = dtInicio;
	}

	public Date getDtInicio() {
		return dtInicio;
	}

	public void setCpfCnpj(Boolean cpfCnpj) {
		this.cpfCnpj = cpfCnpj;
	}

	public Boolean getCpfCnpj() {
		return cpfCnpj;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setCompetencia(Boolean competencia) {
		this.competencia = competencia;
	}

	public Boolean getCompetencia() {
		return competencia;
	}

	// lista carregada no geral
	private List<ProcessoTrfConexao> processoPreventoList = new ArrayList<ProcessoTrfConexao>();
	// lista carregada com os processos celecionados pelo check
	private List<ProcessoTrfConexao> preventoList = new ArrayList<ProcessoTrfConexao>();
	// lista carregada de acordo com a escolha na combo
	private List<ProcessoTrfConexao> preventoListFiltradoOJ = new ArrayList<ProcessoTrfConexao>();
	// lista que popula a combo
	private List<OrgaoJulgador> orgaoJulgadorCombo = new ArrayList<OrgaoJulgador>();
	private Boolean mostrarTabGridPrevento = Boolean.FALSE;
	private int idProcessoTrf = 0;
	// usado como value do combo de prevento
	private OrgaoJulgador orgaoJulgadorPrevento = new OrgaoJulgador();
	private String tab;
	private ProcessoDocumento processoDocumento = new ProcessoDocumento();
	private Processo processo = new Processo();

	public boolean selectedRowsListCarregada() {
		GridQuery grid = (GridQuery) Component.getInstance("aprovarProcessoPreventoGrid");
		return grid.getSelectedRowsList().size() > 0 ? true : false;
	}

	/**
	 * método usado pelo botão de gravar do popup de processo prevento para
	 * gravar um documento
	 */
	public void persistirDadosProcessoDocumento() {
		persistirDadosProcessoDocumento(false);
	}

	public void persistirDadosProcessoDocumento(boolean assinarDocumento) {
		ProcessoDocumentoHome pdh = ProcessoDocumentoHome.instance();
		setProcesso(processoPreventoList.get(0).getProcessoTrf().getProcesso());
		pdh.setModeloDocumento(ProcessoDocumentoBinHome.instance().getModeloDocumento());
		pdh.getInstance().setProcesso(getProcesso());
		if(assinarDocumento) {
			protocolarDocumentoBean = new ProtocolarDocumentoBean(ProcessoDocumentoHome.instance().getInstance().getProcesso().getIdProcesso(), ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL);
			protocolarDocumentoBean.setDocumentoPrincipal(ProcessoDocumentoHome.instance().getInstance());
			protocolarDocumentoBean.setArquivosAssinados(AnexarDocumentoPreventoAction.getAssinaturas());
			protocolarDocumentoBean.concluirAssinatura();
			
			isAssinado = Boolean.TRUE;
			isMinutaGravada = Boolean.FALSE; // Já foi assinado
			pdh.getInstance().setDataJuntada(new Date());
			persistirDadosProcessoDocumentoComAssinatura(pdh);
			if(isTelaAnexarDocumentoPrevento()){
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.INFO, "Documento assinado com sucesso!");
			}
		} else {
			isAssinado = Boolean.FALSE;
			isMinutaGravada = Boolean.TRUE;
			persistirDadosProcessoDocumentoSemAssinatura(pdh);
		}
	}
	
	private void persistirDadosProcessoDocumentoComAssinatura(ProcessoDocumentoHome pdh) {
		if (!br.com.infox.cliente.Util.isStringSemCaracterUnicode(ProcessoDocumentoBinHome.instance().getModeloDocumento())) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Existem caracteres não permitidos pelo sistema. Clique em Gravar antes de Assinar!");
			return;
		}
		if (pdh.persistComAssinatura() != null) {
			persistirProcessoPreventoList(pdh.getInstance(), true);
		}	
	}

	private void persistirDadosProcessoDocumentoSemAssinatura(ProcessoDocumentoHome pdh) {
		if (pdh.persistSemAssinatura() != null) {
			persistirProcessoPreventoList(pdh.getInstance(), false);
		}	
	}

	private void persistirProcessoPreventoList(ProcessoDocumento processoDocumento, boolean validaPrevencao){
		Util.setToEventContext("persistirDadosProcessoDocumento", "true");
		PessoaFisica pessoa = (PessoaFisica) Contexts.getSessionContext().get("pessoaLogada");

		for(ProcessoTrfConexao processoTrfConexao : processoPreventoList){
			//se não selecionou prevenção, colocar como não prevento
			if (processoTrfConexao.getPrevencao() == null
					|| processoTrfConexao.getPrevencao().equals(PrevencaoEnum.PE)) {
				processoTrfConexao.setPrevencao(PrevencaoEnum.RE);
			}
			processoTrfConexao.setProcessoDocumento(processoDocumento);
			processoTrfConexao.setPessoaFisica(pessoa);
			if(validaPrevencao){
				processoTrfConexao.setDtValidaPrevencao(new Date());
			}
			EntityManager em = getEntityManager();
			em.merge(processoTrfConexao);
			em.flush();
			instance().setConexao(processoTrfConexao);

			removerAlertaConexaoSeMesmoOrgao(processoTrfConexao, validaPrevencao);
		}
	}

	/**
	 * Caso o processo em conexo seja do mesmo órgão do processo analisado,
	 * retira o alerta da conexão do conexo também.
	 *
	 * @param processoTrfConexao
	 * @param validaPrevencao
     */
	private void removerAlertaConexaoSeMesmoOrgao(ProcessoTrfConexao processoTrfConexao, boolean validaPrevencao) {
		if (isMesmoOrgaoJulgador(processoTrfConexao)) {
			registraAnalisePrevencaoProcessosConexos(processoTrfConexao, validaPrevencao);
		}
	}

	/**
	 * Valida se o orgão julgador é o mesmo entre o processo selecionado e o processo conexo.
	 *
	 * @param processoTrfConexao
	 *
	 * @return {@code true} caso o processo conexo não seja null e pertença ao mesmo órgão julgador do
	 * processo selecionado.
     */
	private boolean isMesmoOrgaoJulgador(final ProcessoTrfConexao processoTrfConexao) {
		boolean retorno = false;
		ProcessoTrf processoConexo = processoTrfConexao.getProcessoTrfConexo();

		if (processoConexo != null) {
			ProcessoTrf processoSelecionado = processoTrfConexao.getProcessoTrf();
			retorno = processoSelecionado.getOrgaoJulgador().equals(processoConexo.getOrgaoJulgador());
		}

		return retorno;
	}

	/**
	 * Método que registra o resultado da análise de prevenção para o processo conexo
	 * @param processoTrfConexao contem o processo conexo que terá o resultado da análise de prevenção gravada
	 * @param validaPrevencao grava a data da validação da prevenção.
	 */
	private void registraAnalisePrevencaoProcessosConexos(ProcessoTrfConexao processoTrfConexao, boolean validaPrevencao) {
		// Busca o processoTrfConexão invertido. Conexo == conexão e conexão == conexo
		ProcessoTrfConexao processoTrfConexaoEmAnalise = ProcessoTrfConexaoManager.instance().findByProcessoEConexo(processoTrfConexao.getProcessoTrfConexo(), processoTrfConexao.getProcessoTrf());
		if( processoTrfConexaoEmAnalise != null) {
			PessoaFisica pessoa = (PessoaFisica) Contexts.getSessionContext().get("pessoaLogada");
			// Caso a análise ainda esteja pendente
			if( processoTrfConexaoEmAnalise.getDtValidaPrevencao() == null) {
				processoTrfConexaoEmAnalise.setPrevencao(processoTrfConexao.getPrevencao());
				processoTrfConexaoEmAnalise.setProcessoDocumento(processoTrfConexao.getProcessoDocumento());
				processoTrfConexaoEmAnalise.setOrgaoJulgador(processoTrfConexao.getOrgaoJulgador());
				processoTrfConexaoEmAnalise.setNumeroProcesso(processoTrfConexao.getNumeroProcesso());
				processoTrfConexaoEmAnalise.setPessoaFisica(pessoa);
				
				if(validaPrevencao){
					processoTrfConexaoEmAnalise.setDtValidaPrevencao(new Date());
				}
				EntityManager em = getEntityManager();
				em.merge(processoTrfConexaoEmAnalise);
				em.flush();
			}
		}
	}

	public void validarCaracteresEditorTexto() {
		if (!br.com.infox.cliente.Util.isStringSemCaracterUnicode(ProcessoDocumentoBinHome.instance().getModeloDocumento())) {
			flagMostraModalRemocaoCaracteresEspeciais = true;
		} else {
			persistirDadosProcessoDocumento();
		}
	}
	
	/**
	 * [PJEII-3250] - Cristiano Nascimento
	 * Remove os caracteres do editor de texto que não são convertidos ao formato ISO8859-1
	 * e depois atualiza o valor do editor de texto.
	 * @return void
	 */
	public void excluiCaracteresEspeciaisEditorTexto() {
		String texto = ProcessoDocumentoBinHome.instance().getModeloDocumento();
		
		for (int i=0; i< texto.length(); i++) {
			if (!br.com.infox.cliente.Util.isStringSemCaracterUnicode(Character.toString(texto.charAt(i)))) {
				texto = StringUtils.replace(texto, Character.toString(texto.charAt(i)), "");
				i--;
			} else if (Character.toString(texto.charAt(i)).equals("&")) {
				String charUTF8 = texto.substring(i, texto.indexOf(';', i) + 1);  
				if (!br.com.infox.cliente.Util.isStringSemCaracterEspecial(charUTF8)) {
					texto = StringUtils.replace(texto, charUTF8, "");
					i--;
				}
			}
		}
		
		ProcessoDocumentoBinHome.instance().getInstance().setModeloDocumento(texto);
	}

	// método chamado ao selecionar um item da combo de orgão julgador
	// método não usado pois, o combo foi desativado
	public void listaPreventoFiltradaGrid() {
		int i;
		preventoListFiltradoOJ = new ArrayList<ProcessoTrfConexao>();
		for (i = 0; i < processoPreventoList.size(); i++) {
			if (processoPreventoList.get(i).getOrgaoJulgador().equals(orgaoJulgadorPrevento.getOrgaoJulgador()))
				preventoListFiltradoOJ.add(processoPreventoList.get(i));
		}
	}

	private OrgaoJulgador buscaOJ(String nome) {
		String query = "select o from OrgaoJulgador o where o.orgaoJulgador = :nome";

		Query q = getEntityManager().createQuery(query);
		q.setParameter("nome", nome);

		return (OrgaoJulgador) q.getSingleResult();
	}

	// método que carrega combo de oj no popup de prevento
	// método não usado pois, o combo foi desativado
	@SuppressWarnings("static-access")
	public List<OrgaoJulgador> processoPreventoListCombo() {
		int i, j;
		boolean achou = false;
		orgaoJulgadorPrevento = new OrgaoJulgador();
		new Util().setToEventContext("persistirDadosProcessoDocumento", "false");
		orgaoJulgadorCombo = new ArrayList<OrgaoJulgador>();
		for (i = 0; i < processoPreventoList.size(); i++) {
			j = 0;
			achou = false;
			while (!achou && j < orgaoJulgadorCombo.size()) {
				if (orgaoJulgadorCombo.get(j).getOrgaoJulgador().equals(processoPreventoList.get(i).getOrgaoJulgador()))
					achou = true;
				else
					j++;
			}

			if (!achou)
				orgaoJulgadorCombo.add(buscaOJ(processoPreventoList.get(i).getOrgaoJulgador()));
		}

		return orgaoJulgadorCombo;
	}

	public void carregarPopUpPrevento(int id){

		setarIdProcTrf(id);
		ProcessoDocumento pd = new ProcessoDocumento();

		String sql = "select distinct o.processoDocumento " 
				   + "from ProcessoTrfConexao o "
				   + "where o.processoTrf = :processo "
				   + "and o.tipoConexao = 'PR' "
		           + "and o.dtValidaPrevencao is null";
		Query q = getEntityManager().createQuery(sql);
		q.setParameter("processo", processoPreventoList.get(0).getProcessoTrf());

		try {
			pd = (ProcessoDocumento) q.getSingleResult();
		} catch (NoResultException e) {
			pd = null;
		}

		ProcessoDocumentoHome pdh = getComponent("processoDocumentoHome");
		pdh.newInstance();
		if (pd != null) {
			pdh.setInstance(pd);
			ProcessoDocumentoBinHome.instance().setInstance(pd.getProcessoDocumentoBin());
			isMinutaGravada = Boolean.TRUE;
		}else{
			isMinutaGravada = Boolean.FALSE;
		}
		Integer idProcesso = ProcessoTrfConexaoHome.instance().getProcessoSelecionado().getIdProcessoTrf();
		ProcessoTrfHome.instance().setId(idProcesso);
		
		ProcessoJudicialAction processoJudicialAction = getComponent("processoJudicialAction");
		if( processoJudicialAction.getProcessoJudicial().getIdProcessoTrf() != idProcesso ) { 
			processoJudicialAction.init();
		}
	}

	public void mostrarTabDocumento(){
		setMostrarTabGridPrevento(Boolean.FALSE);
		setTab("documentosPreventos");
	}

	public void mostrarTabPreventos(){
		setMostrarTabGridPrevento(Boolean.TRUE);
		setTab("processosPreventos");
	}

	private Boolean mesmoOrgaoJulgador(ProcessoTrfConexao row) {
		boolean achou = true;
		int i = 0;
		while (i < preventoList.size() && achou) {
			if (!preventoList.get(i).getOrgaoJulgador().equals(row.getOrgaoJulgador()))
				achou = false;
			i++;
		}

		return achou;
	}

	// método chamado ao selecionar um prevento pelo check na grid de prevento
	// no popup de prevento
	public void criarListaPrevento(ProcessoTrfConexao row) {
		if (mesmoOrgaoJulgador(row)) {
			boolean achou = false;
			int i = 0;
			while (i < preventoList.size() && !achou) {
				if (preventoList.get(i).equals(row))
					achou = true;
				i++;
			}

			if (achou)
				preventoList.remove(row);
			else
				preventoList.add(row);
		}

		refreshGrid("aprovarProcessoPreventoGrid");
		refreshGrid("aprovarProcessoPreventoGridGrid");
		refreshGrid("aprovarProcessoPreventoGridDiv");
	}

	// metodo usado para alterar o disable do check de prevento da grid
	public Boolean verificaListaPrevento(ProcessoTrfConexao row) {
		GridQuery grid = (GridQuery) Component.getInstance("aprovarProcessoPreventoGrid");
		if (grid.getSelectedRowsList().size() > 0) {
			ProcessoTrfConexao ptc = (ProcessoTrfConexao) grid.getSelectedRowsList().get(0);
			return (ptc.getOrgaoJulgador().equals(row.getOrgaoJulgador()));
		} else
			return true;
	}

	// metodo usado para setar o valor do id do processo no click da lupa
	@SuppressWarnings("unchecked")
	public void setarIdProcTrf(int id) {
		GridQuery grid = (GridQuery) Component.getInstance("aprovarProcessoPreventoGrid");
		grid.getSelectedRowsList().clear();
		idProcessoTrf = id;
		this.setProcessoEmEdicao(((GenericManager) ComponentUtil.getComponent("genericManager")).find(ProcessoTrf.class, id));
		processoPreventoList = new ArrayList<ProcessoTrfConexao>();
		// lista carregada com os processos celecionados pelo check
		preventoList = new ArrayList<ProcessoTrfConexao>();
		// lista carregada de acordo com a escolha na combo
		preventoListFiltradoOJ = new ArrayList<ProcessoTrfConexao>();
		// lista que popula a combo
		orgaoJulgadorCombo = new ArrayList<OrgaoJulgador>();

		String query = "select distinct o " 
				     + "from ProcessoTrfConexao o "
				     + "where o.processoTrf.idProcessoTrf = :idProcessoTrf "
				     + "and o.tipoConexao = 'PR' "
		             + "and o.dtValidaPrevencao is null";
		
		Query q = getEntityManager().createQuery(query);
		q.setParameter("idProcessoTrf", idProcessoTrf);

		processoPreventoList = new ArrayList<ProcessoTrfConexao>();
		processoPreventoList = q.getResultList();
		if (processoPreventoList.size() > 0) {
			setProcessoSelecionado(processoPreventoList.get(0).getProcessoTrf());
		}
		int i;
		for (i = 0; i < processoPreventoList.size(); i++) {
			if (processoPreventoList.get(i).getProcessoTrfConexo() != null) {
				processoPreventoList.get(i).setNumeroProcesso(
						processoPreventoList.get(i).getProcessoTrfConexo().getNumeroProcesso());
				if (processoPreventoList.get(i).getProcessoTrfConexo().getOrgaoJulgador() != null)
					processoPreventoList.get(i).setOrgaoJulgador(
							processoPreventoList.get(i).getProcessoTrfConexo().getOrgaoJulgador().getOrgaoJulgador());
				if (processoPreventoList.get(i).getProcessoTrfConexo().getAssuntoTrf() != null)
					processoPreventoList.get(i).setAssunto(
							processoPreventoList.get(i).getProcessoTrfConexo().getAssuntoTrf().getAssuntoTrf());
			}
		}
	}

	// método usado para carregar a lista das conexoes do processo
	public List<ProcessoTrfConexao> getProcessoPreventoList() {
		return processoPreventoList;
	}

	public void setProcessoPreventoList(List<ProcessoTrfConexao> processoPreventoList) {
		this.processoPreventoList = processoPreventoList;
	}

	public void teste() throws Exception {
		MantemProcessosPreventos mp = new MantemProcessosPreventos();
		mp.getProcessosPreventosCNPJ("44.444.444/4444-44", 287);
	}

	public void setPreventoList(List<ProcessoTrfConexao> preventoList) {
		this.preventoList = preventoList;
	}

	public List<ProcessoTrfConexao> getPreventoList() {
		return preventoList;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoTrfConexao> getProcessoAssociadoList() {
		if (processoAssociadoList == null) {
			ProcessoTrfHome pTrf = ProcessoTrfHome.instance();
			String query = "select o from ProcessoTrfConexao o " + "where o.processoTrf.idProcessoTrf=:idProcTrf ";

			Query q = getEntityManager().createQuery(query);
			q.setParameter("idProcTrf", pTrf.getInstance().getIdProcessoTrf());

			List<ProcessoTrfConexao> lista = new ArrayList<ProcessoTrfConexao>();
			lista = q.getResultList();

			for (int i = 0; i < lista.size(); i++) {
				if (lista.get(i).getProcessoTrfConexo() != null) {

					if (lista.get(i).getProcessoTrfConexo().getNumeroProcesso() == null) {
						lista.get(i).setNumeroProcesso(lista.get(i).getProcessoTrf().getNumeroProcesso());
					}
					if (lista.get(i).getProcessoTrfConexo().getOrgaoJulgador() != null) {
						lista.get(i).setOrgaoJulgador(
								lista.get(i).getProcessoTrf().getOrgaoJulgador().getOrgaoJulgador());
					}
					if (lista.get(i).getProcessoTrfConexo().getClasseJudicial() != null) {
						lista.get(i).setSessaoJudiciaria(
								lista.get(i).getProcessoTrf().getClasseJudicial().getClasseJudicial());
					}
					if (lista.get(i).getProcessoTrfConexo().getNumeroProcesso() != null) {
						lista.get(i).setAssuntos(lista.get(i).getProcessoTrf().getAssuntoTrfList());
					} else {
						lista.get(i).setAssuntos(null);
					}
				}
			}
			processoAssociadoList = lista;
		}
		return processoAssociadoList;
	}

	public void setProcessoAssociadoList(List<ProcessoTrfConexao> processoAssociadoList) {
		this.processoAssociadoList = processoAssociadoList;
	}

	public List<ProcessoTrf> getProcessosAssociados() {
		return processosAssociados;
	}

	public void setProcessosAssociados(List<ProcessoTrf> processosAssociado) {
		this.processosAssociados = processosAssociado;
	}

	public Boolean getHabilitaAbaAssociados() {
		return habilitaAbaAssociados;
	}

	public void setHabilitaAbaAssociados(Boolean habilitaAbaAssociados) {
		this.habilitaAbaAssociados = habilitaAbaAssociados;
	}

	public Boolean getMostrarTabGridPrevento() {
		return mostrarTabGridPrevento;
	}

	public void setMostrarTabGridPrevento(Boolean mostrarTabGridPrevento) {
		this.mostrarTabGridPrevento = mostrarTabGridPrevento;
	}

	public void setPreventoListFiltradoOJ(List<ProcessoTrfConexao> preventoListFiltradoOJ) {
		this.preventoListFiltradoOJ = preventoListFiltradoOJ;
	}

	public List<ProcessoTrfConexao> getPreventoListFiltradoOJ() {
		return preventoListFiltradoOJ;
	}
	
	public List<ProcessoTrfConexao> listPreventosPendentes(Integer idProcessoTrf){
		return ProcessoTrfConexaoManager.instance().getProcessosPreventosPendentesAnalise(idProcessoTrf);
	}

	public int countPreventosPendentes(Integer idProcessoTrf){
		return ProcessoTrfConexaoManager.instance().getQuantidadeProcessosPreventosPendentesAnalise(idProcessoTrf);
	}

	public void setOrgaoJulgadorPrevento(OrgaoJulgador orgaoJulgadorPrevento) {
		this.orgaoJulgadorPrevento = orgaoJulgadorPrevento;
	}

	public OrgaoJulgador getOrgaoJulgadorPrevento() {
		return orgaoJulgadorPrevento;
	}

	public void setOrgaoJulgadorCombo(List<OrgaoJulgador> orgaoJulgadorCombo) {
		this.orgaoJulgadorCombo = orgaoJulgadorCombo;
	}

	public List<OrgaoJulgador> getOrgaoJulgadorCombo() {
		return orgaoJulgadorCombo;
	}

	@Override
	public String getTab() {
		return this.tab;
	}

	@Override
	public void setTab(String tab) {
		this.tab = tab;
	}

	@Override
	public void newInstance() {
		limparCampos();
		super.newInstance();
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (processoSelecionado == null) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Favor selecionar o processo principal!");
			return false;
		}

		if (processoAssociadoTransient == null) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Favor selecionar o processo associado!");
			return false;
		}

		return true;
	}

	
	public String persistExterno(){
		for (ProcessoTrfConexao p : getProcessosTRFConexoes()) {
			if(verificaDuplicidade(p.getProcessoTrf(),p.getNumeroProcesso())){
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, 
							"Associação entre "+p.getProcessoTrf() 
							+" e "+p.getNumeroProcesso()+" já cadastrada!");
			}
			else{
				getEntityManager().persist(p);
			}
		}
		getEntityManager().flush();
		FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro(s) cadastrado(s) com sucesso!");
		newInstance();
		return "persisted";
	}
	
	
	@Override
	public String persist() {
		String ret = null;

		if (!beforePersistOrUpdate()) {
			return ret;
		}

                try {

                    for (ProcessoTrfConexao p : getProcessosTRFConexoes()) {
                            if(getProcessoTrfConexaoManager().verificaDuplicidade(p.getProcessoTrf(),p.getProcessoTrfConexo())){
                                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, 
                                                            "Associação entre "+p.getProcessoTrf() 
                                                            +" e "+p.getProcessoTrfConexo()+" já cadastrada!");
                            }
                            else{
                                    getProcessoTrfConexaoManager().persist(p);
                            }
                    }

                    for (ProcessoTrfConexao p : getProcessosTRFConexosAssociadosAoPrincipal()) {
                            if(getProcessoTrfConexaoManager().verificaDuplicidade(p.getProcessoTrf(),p.getProcessoTrfConexo())){
                                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, 
                                                    "Associação entre "+p.getProcessoTrf() 
                                                    +" e "+p.getProcessoTrfConexo()+" já cadastrada!");
                            }
                            else{
                                    getProcessoTrfConexaoManager().persist(p);
                            }
                    }
                    getEntityManager().flush();
                    FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro(s) cadastrado(s) com sucesso!");
                    newInstance();

                } catch (PJeBusinessException ex) {
                    FacesMessages.instance().clear();
                    FacesMessages.instance().add(Severity.ERROR, "Erro ao realizar a associação do Processo");
                    log.error(ex);
                }
                
		return ret;
	}

	private boolean verificaDuplicidade(ProcessoTrf processoTrf,
			String processoTrfConexo) {
		Query query = EntityUtil
				.getEntityManager()
				.createQuery(
						"select 1 from ProcessoTrfConexao o where processoTrf = :procTrf and numeroProcesso = :procTrfConexo");
		query.setParameter("procTrfConexo", processoTrfConexo);
		query.setParameter("procTrf", processoTrf);
		Integer result = query.getResultList().size();
		if (result > 0)
			return true;
		else
			return false;
	}

	public void limparNumeroProcessoConexoSuggest() {
		Contexts.removeFromAllContexts("numeroProcessoConexoSuggest");
	}

	public void limparNumeroProcessoTrfOrgaoJulgadorSuggest() {
		Contexts.removeFromAllContexts("numeroProcessoTrfOrgaoJulgadorSuggest");
	}

    /**
     * Transferido método para ProcessoTrfConexaoManager
     * @deprecated
     */
	@Deprecated
	public boolean verificaDuplicidade() {
		Query query = EntityUtil
				.getEntityManager()
				.createQuery(
						"select o from ProcessoTrfConexao o where processoTrf = :procTrf and processoTrfConexo = :procTrfConexo");
		query.setParameter("procTrfConexo", getNumeroProcessoSuggestBean().getInstance());
		query.setParameter("procTrf", getNumeroProcessoTrfOrgaoJulgadorSuggestBean().getInstance());
		Integer result = query.getResultList().size();
		if (result > 0)
			return true;
		else
			return false;
	}
	
	@Override
	public String remove(ProcessoTrfConexao obj) {
                try {
                    setInstance(obj);
                    getProcessoTrfConexaoManager().remove(obj);

                    refreshGrid("processoTrfConexaoGrid");
                } catch (PJeBusinessException ex) {
                    FacesMessages.instance().clear();
                    FacesMessages.instance().add(Severity.ERROR, "Erro ao remover a associação do Processo");
                    log.error(ex);
                }

		return "removed";
	}

	@Override
	public String update() {
		String ret = null;

		if (!beforePersistOrUpdate()) {
			return ret;
		}

		if (getProcessoTrfConexaoManager().verificaDuplicidade(getNumeroProcessoSuggestBean().getInstance(), getNumeroProcessoTrfOrgaoJulgadorSuggestBean().getInstance())) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado!");
		} else {
			for (ProcessoTrfConexao p : getProcessosTRFConexoes()) {
				getEntityManager().persist(p);
			}

			for (ProcessoTrfConexao p : getProcessosTRFConexosAssociadosAoPrincipal()) {
				getEntityManager().persist(p);
			}

			getEntityManager().flush();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro alterado com sucesso!");
		}
		return ret;
	}

	public OrgaoJulgador getOrgaoJulgadorConexo() {
		return orgaoJulgadorConexo;
	}

	public void setOrgaoJulgadorConexo(OrgaoJulgador orgaoJulgadorConexo) {
		this.orgaoJulgadorConexo = orgaoJulgadorConexo;
	}

	private NumeroProcessoConexoSuggestBean getNumeroProcessoSuggestBean() {
		return getComponent("numeroProcessoConexoSuggest");
	}

	private NumeroProcessoTrfOrgaoJulgadorSuggestBean getNumeroProcessoTrfOrgaoJulgadorSuggestBean() {
		return getComponent("numeroProcessoTrfOrgaoJulgadorSuggest");
	}

	public void limparCampos() {
		processosTRFConexoes.clear();
		processosTRFConexosAssociadosAoPrincipal.clear();
		setJustificativa("");
		setTipoConexao(null);
		setExisteProcessosAssociadosPersistidos(Boolean.FALSE);
		Contexts.removeFromAllContexts("numeroProcessoConexoSuggest");
		Contexts.removeFromAllContexts("numeroProcessoTrfOrgaoJulgadorSuggest");
	}

	public Boolean verificarDocumento(ProcessoTrf obj) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(ProcessoTrfConexao.class);
		criteria.add(Restrictions.eq("processoTrf", obj));
		criteria.add(Restrictions.isNull("processoDocumento"));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		ProcessoTrfConexao classe = (ProcessoTrfConexao)criteria.uniqueResult();
		if (classe != null) {
			conexao = classe;
			return Boolean.TRUE;
		} else {
			Criteria criteria2 = HibernateUtil.getSession().createCriteria(ProcessoTrfConexao.class);
			criteria2.add(Restrictions.eq("processoTrf", obj));
			criteria2.add(Restrictions.isNotNull("processoDocumento"));
			criteria2.add(Restrictions.isNull("dtValidaPrevencao"));
			criteria2.setFirstResult(0);
			criteria2.setMaxResults(1);
			classe = (ProcessoTrfConexao)criteria2.uniqueResult();
			conexao = classe;
			return Boolean.FALSE;
		}
	}

	public Boolean verificarPendentePrevencao(ProcessoTrf obj) {
		String query = "select count(o) from ProcessoTrfConexao o where " + "o.processoTrf = :trf";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("trf", obj);
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	public void setConexao(ProcessoTrfConexao conexao) {
		this.conexao = conexao;
	}

	public ProcessoTrfConexao getConexao() {
		return conexao;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void documentoPrevencaoAssinatura() {
		ProcessoDocumentoBinHome pdbh = getComponent("processoDocumentoBinHome");
		pdbh.assinarDocumento();
		documentoPrevencao();
	}

	@SuppressWarnings("unchecked")
	public void documentoPrevencao() {
		ProcessoTrf trf = ProcessoTrfHome.instance().getInstance();
		ProcessoDocumento pd = getConexao().getProcessoDocumento();
		String query = "select o from ProcessoTrfConexao o where "
				+ "(o.processoTrf = :trf)" + "and o.processoDocumento = :documento";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("trf", trf);
		q.setParameter("documento", pd);

		List<ProcessoTrfConexao> list = q.getResultList();
		newInstance();
		for (int i = 0; i < list.size(); i++) {
			setInstance(list.get(i));
			if (getInstance().getPrevencao() == null || getInstance().getPrevencao().equals(PrevencaoEnum.PE)) {
				getInstance().setPrevencao(PrevencaoEnum.RE);
			}
			super.update();
			newInstance();
		}
		Util.setToEventContext("persistirDadosProcessoDocumento", "true");
		setPrevento(Boolean.FALSE);
		refreshGrid("analisePrevencaoGrid");
		refreshGrid("processoAnalisePrevencaoGrid");
	}

	@SuppressWarnings("unchecked")
	public void removerDocumento() {
		ProcessoTrf trf = ProcessoTrfHome.instance().getInstance();
		if(trf != null) {
			setProcessoSelecionado(trf);
		}
		ProcessoDocumento pd = null;
		String query = "select o from ProcessoTrfConexao o where " + "o.processoTrf = :trf";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("trf", trf);

		List<ProcessoTrfConexao> list = q.getResultList();
		newInstance();
		for (int i = 0; i < list.size(); i++) {
			setInstance(list.get(i));
			setProcessoAssociadoTransient(getInstance().getProcessoTrfConexo());
			if (getInstance().getProcessoDocumento() != null && getInstance().getProcessoDocumento().getAtivo().equals(Boolean.TRUE)) 
			{
				pd = getInstance().getProcessoDocumento();
				pd.setAtivo(Boolean.FALSE);
			}
			getInstance().setProcessoDocumento(null);
			getInstance().setPrevencao(PrevencaoEnum.PE);
			super.update();
			newInstance();
		}
		EntityUtil.getEntityManager().persist(pd);
		setPrevento(Boolean.FALSE);
		isMinutaGravada = Boolean.FALSE;
		refreshGrid("analisePrevencaoGrid");
	}

	public void setarPrevento() {
		setPrevento(Boolean.TRUE);
	}

	public void setPrevento(Boolean prevento) {
		this.prevento = prevento;
	}

	public Boolean getPrevento() {
		return prevento;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	public Processo getProcesso() {
		return processo;
	}

	public PrevencaoEnum[] getPrevencaoEnumValues() {
		PrevencaoEnum[] valoresEnum = { PrevencaoEnum.PR, PrevencaoEnum.RE };
		return valoresEnum;
	}

	public void associarProcessoFrame(){
		if(getProcessoSelecionado() == null){
			setProcessoSelecionado(ProcessoTrfHome.instance().getInstance());
		}
		associarProcesso();
	}

	public void associarProcesso() {
		Date dataAssociacao = new Date();
		processosTRFConexoes.add(associarPrincipalAoConexo(dataAssociacao));
		if(getProcessoAssociadoTransient() != null){
			processosTRFConexosAssociadosAoPrincipal.add(associarConexoAoPrincipal(dataAssociacao));
		}
	}
	
	
	private ProcessoTrfConexao associarPrincipalAoConexo(Date dataAssociacao) {
		ProcessoTrfConexao pc = criaProcessoTrfConexo(dataAssociacao);
		pc.setProcessoTrf(getProcessoSelecionado());
		if(getProcessoAssociadoTransient() != null){
			pc.setProcessoTrfConexo(getProcessoAssociadoTransient());
		}
		else{
			pc.setNumeroProcesso(getNumeroProcessoAssociado());
			pc.setPrevencao(getConfirmacao());
			if(getOrgaoJulgadorConexo() != null){
				pc.setOrgaoJulgador(getOrgaoJulgadorConexo().getOrgaoJulgador());
				setOrgaoJulgadorConexo(null);
			}
			setNumeroProcessoAssociado(null);
		}
		
		return pc;
	}

	private ProcessoTrfConexao associarConexoAoPrincipal(Date dataAssociacao) {
		ProcessoTrfConexao pc = criaProcessoTrfConexo(dataAssociacao);

		pc.setProcessoTrf(getProcessoAssociadoTransient());
		pc.setProcessoTrfConexo(getProcessoSelecionado());

		return pc;
	}

	private ProcessoTrfConexao criaProcessoTrfConexo(Date dataAssociacao) {
		ProcessoTrfConexao pc = new ProcessoTrfConexao();
		if(getOrgaoJulgadorConexo() != null){
			pc.setOrgaoJulgador(getOrgaoJulgadorConexo().getOrgaoJulgador());
			pc.setPrevencao(PrevencaoEnum.PE);
		}
		else{
			pc.setDtValidaPrevencao(dataAssociacao);
			pc.setPrevencao(PrevencaoEnum.PR);
		}
		pc.setAtivo(Boolean.TRUE);
		pc.setTipoConexao(tipoConexao);
		pc.setDtPossivelPrevencao(dataAssociacao);
		pc.setJustificativa(justificativa);
		return pc;
	}

	public List<ProcessoTrfConexao> getProcessosTRFConexoes() {
		return processosTRFConexoes;
	}

	public void setProcessosTRFConexoes(List<ProcessoTrfConexao> processosTRFConexoes) {
		this.processosTRFConexoes = processosTRFConexoes;
	}

	public List<ProcessoTrfConexao> getProcessosTRFConexosAssociadosAoPrincipal() {
		return processosTRFConexosAssociadosAoPrincipal;
	}

	public void setProcessosTRFConexosAssociadosAoPrincipal(
			List<ProcessoTrfConexao> processosTRFConexosAssociadosAoPrincipal) {
		this.processosTRFConexosAssociadosAoPrincipal = processosTRFConexosAssociadosAoPrincipal;
	}

	public TipoConexaoEnum getTipoConexao() {
		return tipoConexao;
	}

	public void setTipoConexao(TipoConexaoEnum tipoConexao) {
		this.tipoConexao = tipoConexao;
	}

	public String getJustificativa() {
		return justificativa;
	}

	public void setJustificativa(String justificativa) {
		this.justificativa = justificativa;
	}

	public TipoConexaoEnum[] getTipoConexaoEnumValues() {
		return TipoConexaoEnum.values();
	}

	public List<TipoConexaoEnum> tipoConexaoEnumItems() {
		List<TipoConexaoEnum> lista = new ArrayList<TipoConexaoEnum>();
		lista.add(TipoConexaoEnum.DP);
		lista.add(TipoConexaoEnum.PR);
		lista.add(TipoConexaoEnum.AS);
		return lista;
	}

	public String getNumeroProcessoConexo(ProcessoTrfConexao processoTrfConexo) {
		if (processoTrfConexo.getProcessoTrfConexo() != null)
			return processoTrfConexo.getProcessoTrfConexo().getNumeroProcesso();
		else {
			return processoTrfConexo.getNumeroProcesso();
		}
	}

	public void assinarDocumentoPrevencao() {
		ProcessoDocumentoBinHome.instance().assinarDocumento();
		documentoPrevencao();

	}

	public ProcessoTrf getProcessoEmEdicao(){
		return processoEmEdicao;
	}

	public void setProcessoEmEdicao(ProcessoTrf processoEmEdicao){
		this.processoEmEdicao = processoEmEdicao;
	}

	public String getNumeroProcessoAssociado() {
		return numeroProcessoAssociado;
	}

	public void setNumeroProcessoAssociado(String numeroProcessoAssociado) {
		this.numeroProcessoAssociado = numeroProcessoAssociado;
	}

	public String getOrgaoJulgadorAssociado() {
		return orgaoJulgadorAssociado;
	}

	public void setOrgaoJulgadorAssociado(String orgaoJulgadorAssociado) {
		this.orgaoJulgadorAssociado = orgaoJulgadorAssociado;
	}

	public PrevencaoEnum getConfirmacao() {
		return confirmacao;
	}

	public void setConfirmacao(PrevencaoEnum confirmacao) {
		this.confirmacao = confirmacao;
	}

	public boolean isImmediateTaskButton() {
		TaskInstanceHome.instance().setImmediateTaskButton(true);
		return immediateTaskButton;
	}

	public void setImmediateTaskButton(boolean immediateTaskButton) {
		this.immediateTaskButton = immediateTaskButton;
	}

	public boolean isFlagMostraModalRemocaoCaracteresEspeciais() {
		return flagMostraModalRemocaoCaracteresEspeciais;
	}

	public void setFlagMostraModalRemocaoCaracteresEspeciais(boolean flagMostraModalRemocaoCaracteresEspeciais) {
		this.flagMostraModalRemocaoCaracteresEspeciais = flagMostraModalRemocaoCaracteresEspeciais;
	}

   /**
    * Criação do método para obter instância do ProcessoTrfConexaoManager
    * @return the processoTrfConexaoManager
    */
    public ProcessoTrfConexaoManager getProcessoTrfConexaoManager() {
        return ProcessoTrfConexaoManager.instance();
    }

	public boolean isAssinado() {
		return isAssinado;
	}

	/**
     * Método para verificar se a minuta está gravada
     * @return Boolean
     */	
	
	public boolean isMinutaGravada() {
		return isMinutaGravada;
	}

	public boolean getBtnSalvarDesabilitado() {
		return btnSalvarDesabilitado;
	}
	
}
