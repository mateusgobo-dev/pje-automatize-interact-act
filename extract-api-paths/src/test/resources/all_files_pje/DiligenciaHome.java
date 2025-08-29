package br.com.infox.cliente.home;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.LazyInitializationException;
import org.jboss.resteasy.spi.ApplicationException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.transaction.Transaction;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.actions.anexarDocumentos.AnexarDocumentos;
import br.com.infox.cliente.bean.PreCadastroPessoaBean;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.core.certificado.util.VerificaCertificadoPessoa;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.pje.manager.EventoAgrupamentoManager;
import br.com.itx.component.FileHome;
import br.com.itx.component.Util;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.servicos.DateService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisServiceImpl;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Agrupamento;
import br.jus.pje.nucleo.entidades.Diligencia;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaOficialJustica;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteVisita;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoResultadoDiligencia;
import br.jus.pje.nucleo.entidades.Visita;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.ProcessoExpedienteCentralMandadoStatusEnum;
import br.jus.pje.nucleo.enums.TipoCalculoMeioComunicacaoEnum;
import br.jus.pje.nucleo.enums.TipoComunicacaoEnum;
import br.jus.pje.nucleo.enums.TipoExpedienteEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@Name(DiligenciaHome.NAME)
@Scope(ScopeType.CONVERSATION)
public class DiligenciaHome extends AbstractDiligenciaHome<Diligencia> implements ArquivoAssinadoUploader{

	public static final String NAME = "diligenciaHome";
	private static final long serialVersionUID = 1L;
	private static final String TIPO_COMPLEMENTO_RESULTADO_DILIGENCIA = "6";
	private static final String DILIGENCIA_NAO_CUMPRIDA = "não cumprido";

	private List<Visita> visitaList = new ArrayList<Visita>(0);
	private Boolean documentoInserido = Boolean.FALSE;
	private Integer contador;
	private String numProc = "";
	private ModeloDocumentoLocal modeloDocumento;
	private ProcessoDocumento pdHtml;
	private ProcessoDocumentoBin pdbHtml;
	private ProcessoDocumento pdPdf;
	private ProcessoDocumentoBin pdbPdf;
	private String certChain;
	private String signature;
	private Boolean abreToggle = Boolean.FALSE;
	private Boolean isDataCumprimentoBloqueada = Boolean.FALSE;
	private Boolean finalizar = Boolean.FALSE;
	private String urlDocsField;
	private PessoaFisica pessoaFisica; 
	private PessoaJuridica pessoaJuridica;
	private PessoaAutoridade pessoaAutoridade;
	
	private ProtocolarDocumentoBean protocolarDocumentoBean;
	
	@Logger
	private Log log;
	
	@RequestParameter(value="idProcessoSelecionado")
	private Integer idProcessoSelecionado;
	
	@RequestParameter(value="idProcessoParteExpedienteSelecionado")
	private Integer idProcessoParteExpedienteSelecionado;

	@In
	private ProcessoParteExpedienteManager processoParteExpedienteManager;

	private Date dataCumprimentoMandato;
	private boolean exibeTipoCalculoPrazo = false;
	private ProcessoExpedienteCentralMandado mandado;
	private Integer idProcessoParteExpediente;
	
	public static DiligenciaHome instance() {
		return ComponentUtil.getDiligenciaHome();
	}
	
	@Override
	public void create() {
		super.create();
		mandado = ProcessoExpedienteCentralMandadoHome.instance().getInstance();
		Pessoa pessoa = null;
		pessoa = (Pessoa) Authenticator.getUsuarioLogado();
		PessoaOficialJustica pessoaOficialJustica = ((PessoaFisica) pessoa).getPessoaOficialJustica();
		for (Diligencia diligencia : mandado.getDiligenciaList()) {
			if (diligencia.getPessoaOficialJustica() != null
					&& diligencia.getPessoaOficialJustica().equals(pessoaOficialJustica) && diligencia.getAtivo()) {
				instance = diligencia;
				abreToggle = Boolean.TRUE;
				dataCumprimentoMandato = diligencia.getDtCumprimento();
				if (dataCumprimentoMandato != null) {
					setIsDataCumprimentoBloqueada(Boolean.TRUE);
				}
				if (isExpedienteExigeTipoCalculo(mandado.getProcessoExpediente())) {
					exibeTipoCalculoPrazo = true;
				}
				break;
			}
		}
		
		protocolarDocumentoBean = getProtocolarDocumentoBean();
		
		setIdProcessoParteExpediente(idProcessoParteExpedienteSelecionado);
	}
	
	public List<ProcessoParteExpediente> getListaDePartesDoExpediente() {
		return this.mandado.getProcessoExpediente().getProcessoParteExpedienteList();
	}

	
	public void selecionarTipoResultado() {
		if (ParametroUtil.instance().getTipoResultadoDiligenciaCumprido() == null) {
			FacesMessages.instance().add(Severity.ERROR, "O parâmetro 'idTipoResultadoDiligenciaCumprido' não foi definido.");
			return;
		}
		
		//Se o resultado da diligencia for "Cumprido"
		if (this.isCumprido()) {
			ProcessoExpediente pe = ProcessoExpedienteCentralMandadoHome.instance().getInstance().getProcessoExpediente();
			ProcessoParteExpediente ppe = ProcessoExpedienteCentralMandadoHome.instance().getInstance().getParteExpedienteUnica();
			if (ppe == null){
			    /* se não houver o parteExpedienteUnica, busca o primeiro PPE da lista, pode ser só o primeiro item, pois a configuração é igual par todos os PPE vinculados ao mesmo PE */
			    List<ProcessoParteExpediente> partesExpediente = pe.getProcessoParteExpedienteList();
			    try {
			    	ppe = partesExpediente.get(0);
			    	ProcessoExpedienteCentralMandadoHome.instance().getInstance().setParteExpedienteUnica(ppe);
			    }catch (LazyInitializationException ex) {
			    	try {
						pe = ComponentUtil.getProcessoExpedienteManager().findById(pe.getIdProcessoExpediente());
						ppe = pe.getProcessoParteExpedienteList().get(0);
						ProcessoExpedienteCentralMandadoHome.instance().getInstance().setParteExpedienteUnica(ppe);
					} catch (PJeBusinessException e) {
						log.error("Erro ao recuperar processo expediente de id " + pe.getIdProcessoExpediente() + ": " + e.getLocalizedMessage());
					}			    	
			    }
			}	
			if(ppe != null && isExpedienteExigeTipoCalculo(pe)){
				exibeTipoCalculoPrazo = true;
			}
		} else {//Se o resultado da diligencia for DIFERENTE de "Cumprido"
			exibeTipoCalculoPrazo = false;
			setDataCumprimentoMandato(dataCumprimentoMandato);
		}
	}
	
	/**
	 * Obtém os tipos de prazo da central de mandados disponíveis para seleção pelo usuário.
	 * 
	 * @return sequência de tipos de prazo central de mandados disponíveis
	 */
	public List<TipoCalculoMeioComunicacaoEnum> obtemTiposPrazoCentralMandado(){
		return Arrays.asList(TipoCalculoMeioComunicacaoEnum.values());
	}
	
	/**
	 *Verifica se o expediente passado no parametro é do tipo que exige que
	 *se informe o tipo de calculo do meio de comunicação  
	 * @param expediente: ProcessoExpediente
	 * @return boolean
	 */
	private boolean isExpedienteExigeTipoCalculo(ProcessoExpediente expediente){
		//Se o meio for central de mandados
		return expediente != null && ExpedicaoExpedienteEnum.M.equals( expediente.getMeioExpedicaoExpediente() );
	}

	/**
	 * PJEII-20844 - se o oficial herdar o papel (pje:oficialJustica:permiteAlterarContagemPrazoResposta) permitir que o Oficial possa alterar a contagem de prazo para resposta
	 * @return boolean
	 */
	public boolean isOficialPodeAlterarContagemPrazo() {
		return Authenticator.isPapelOficialJustica() && Identity.instance().hasRole(Papeis.PJE_OFICIAL_JUSTICA_ALTERA_CONTAGEM);
	}

	@SuppressWarnings("unchecked")
	public void setarDiligencia() {
		TipoProcessoDocumento tipoProcessoDocumentoDiligencia = ParametroUtil.instance().getTipoProcessoDocumentoDiligencia();
		getProtocolarDocumentoBean().setTiposDocumentosPossiveis(tipoProcessoDocumentoDiligencia == null ? 
				Collections.<TipoProcessoDocumento>emptyList() : Arrays.asList(tipoProcessoDocumentoDiligencia));
		
		if(getInstance().getTipoResultadoDiligencia() == null){
			newInstance();
		}
		GridQuery gq = (GridQuery) Component.getInstance("visitaGrid");
		setVisitaList(gq.getFullList());
		AnexarDocumentos.instance().setPdHtml(pdHtml);
		ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class,
				ProcessoExpedienteCentralMandadoHome.instance().getInstance()
						.getProcessoExpediente().getProcessoTrf()
						.getIdProcessoTrf());
		ProcessoTrfHome.instance().setInstance(processoTrf);
		ProcessoHome.instance().setInstance(processoTrf.getProcesso());
	}

	@Override
	public void newInstance() {
		setDocumentoInserido(Boolean.FALSE);
		iniciarDocumentos();
		contador = 0;
		super.newInstance();
	}

	public void setVisitaList(List<Visita> visitaList) {
		this.visitaList = visitaList;
	}

	public List<Visita> getVisitaList() {
		return visitaList;
	}

	public void inserirDiligenciaVisitaList() {
		for (Visita visita : visitaList) {
			visita.setDiligencia(getInstance());
			getEntityManager().merge(visita);
			getEntityManager().flush();
		}
		getEntityManager().clear();
	}

	public void inserir() {
		if (ParametroUtil.instance().getTipoProcessoDocumentoDiligencia() == null) {
			FacesMessages.instance().add(Severity.ERROR, 
					"O parâmetro de sistema idTipoProcessoDocumentoDiligencia não existe ou não está cadastrado corretamente.");
			
			return;
		}
		if (dataCumprimentoMandato != null) {
			Date dataAtual = DateService.instance().getDataHoraAtual();
			if (!validaDataCumprimentoMandato(dataAtual)) {
				getStatusMessages().add(Severity.ERROR, 
						"A data de cumprimento do mandado deve estar entre a data de distribuição do expediente ({0}) e a data atual ({1})",
					new SimpleDateFormat("dd/MM/yyyy HH:mm").format(
							ProcessoExpedienteCentralMandadoHome.instance().getInstance().getDtDistribuicaoExpediente()),
					new SimpleDateFormat("dd/MM/yyyy HH:mm").format(dataAtual));

				return;
			}
		} else if (isCumprido()) {
			getStatusMessages().add(Severity.ERROR, "A data de cumprimento do mandado deve ser informada");
			return;
		}
		//Cria uma instancia da entidade Diligencia
		PessoaOficialJustica pessoaOficialJustica = ((PessoaFisica) Authenticator.getUsuarioLogado()).getPessoaOficialJustica();		
		Diligencia  diligencia = getInstance();
		diligencia.setDtCumprimento(dataCumprimentoMandato);
		setIsDataCumprimentoBloqueada(Boolean.TRUE);
		diligencia.setProcessoExpedienteCentralMandado(ProcessoExpedienteCentralMandadoHome.instance().getInstance());
		diligencia.setPessoaOficialJustica(pessoaOficialJustica);
		diligencia.setAtivo(Boolean.TRUE);
		setInstance(diligencia);	
		persist();
		FacesMessages.instance().clear();
		abreToggle = Boolean.TRUE;
	}


	private boolean validaDataCumprimentoMandato(Date dataAtual) {
		Date dataDistribuicao = ProcessoExpedienteCentralMandadoHome.instance().getInstance().getDtDistribuicaoExpediente();
		if (dataCumprimentoMandato.before(dataDistribuicao) || dataCumprimentoMandato.after(dataAtual)) {
			return false;
		}
		return true;
	}

	public void iniciarDocumentos() {
		modeloDocumento = new ModeloDocumentoLocal();
		newInstanceHtml();
		newInstancePdf();
		ProcessoDocumentoExpedienteHome.instance().newInstance();
		pdHtml.setProcessoDocumento("Diligência");
		pdHtml.setTipoProcessoDocumento(ParametroUtil.instance()
				.getTipoProcessoDocumentoDiligencia());
		pdHtml.setProcesso(ProcessoExpedienteCentralMandadoHome.instance()
				.getInstance().getProcessoExpediente().getProcessoTrf()
				.getProcesso());
		pdHtml.setExclusivoAtividadeEspecifica(Boolean.TRUE);
	}

	private ProcessoDocumento processoDocumento() {
		String query = "select o from ProcessoDocumento o where o.processo.numeroProcesso = :numProc";

		Query q = getEntityManager().createQuery(query);
		q.setParameter("numProc", numProc);

		return (ProcessoDocumento) q.getResultList().get(0);
	}

	@SuppressWarnings("static-access")
	public String inserirAtualizarDoc() {
		ProcessoDocumentoHome processoDocHome = ProcessoDocumentoHome
				.instance();
		ProcessoDocumentoBinHome processoDocBinHome = ProcessoDocumentoBinHome
				.instance();
		FileHome file = FileHome.instance();

		if (contador >= 1) {
			ProcessoDocumentoHome.instance().setModelo(Boolean.FALSE);
		} else {
			ProcessoDocumentoHome.instance().setModelo(Boolean.TRUE);
		}

		if (contador >= 1) {
			if (file == null || file.getData() == null) {
				FacesMessages.instance().add(Severity.ERROR,
						"Selecione algum arquivo .pdf");
				return "problem";
			} else if (!file.getFileType().equalsIgnoreCase("PDF")) {
				FacesMessages.instance().add(Severity.ERROR,
						"O arquivo deve ser pdf");
				return null;
			}
		}

		ProcessoDocumentoBin pd = processoDocBinHome.getInstance();
		if ((contador < 1) && (pd.getSignatarios().isEmpty())) {
			FacesMessages.instance().add(Severity.ERROR,
					"Assine Digitalmente o Documento.");
			return "problem";
		}

		ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class,
				ProcessoExpedienteCentralMandadoHome.instance().getInstance()
						.getProcessoExpediente().getProcessoTrf()
						.getIdProcessoTrf());
		if (processoTrf.getIdProcessoTrf() == 0) {
			ProcessoTrfHome.instance().setInstance(getProcessoTrf());
		} else {
			ProcessoHome.instance().setInstance(processoTrf.getProcesso());
			ProcessoTrfHome.instance().setInstance(processoTrf);
		}

		processoDocHome.instance().getInstance()
				.setProcessoDocumento("Diligência");
		processoDocHome
				.instance()
				.getInstance()
				.setTipoProcessoDocumento(
						ParametroUtil.instance()
								.getTipoProcessoDocumentoDiligencia());

		processoDocBinHome.setIsAssinarDocumento(true);
		processoDocHome.instance().getInstance().setExclusivoAtividadeEspecifica(Boolean.TRUE);
		if (processoDocHome.persist() == null) {
			return null;
		}
		contador++;
		setDocumentoInserido(Boolean.TRUE);
		if (numProc.equals(""))
			numProc = ProcessoDocumentoHome.instance().getInstance()
					.getProcesso().toString();
		// Inserção do ProcessoDocumento na entidade
		// ProcessoDocumentoExpediente.
		ProcessoDocumentoExpediente processoDocumentoExpediente = ProcessoDocumentoExpedienteHome
				.instance().getInstance();
		processoDocumentoExpediente.setAnexo(Boolean.TRUE);
		processoDocumentoExpediente.setProcessoDocumento(processoDocumento());
		processoDocumentoExpediente
				.setProcessoExpediente(ProcessoExpedienteCentralMandadoHome
						.instance().getInstance().getProcessoExpediente());

		getEntityManager().persist(processoDocumentoExpediente);
		getEntityManager().flush();
		getEntityManager().clear();
		
		iniciarDocumentos();
		
		return "persisted";
	}
	
	private Boolean isCumprido() {
		return getInstance().getTipoResultadoDiligencia().equals(
				ParametroUtil.instance().getTipoResultadoDiligenciaCumprido());
	}
	
	private Boolean isRedistribuicao() {
		return getInstance().getTipoResultadoDiligencia().equals(
				ParametroUtil.instance().getTipoResultadoDiligenciaRedistribuicao());
	}

	public void atualizar() {
		this.concluirPeticionamento();
	    Util.beginAndJoinTransaction();
		
		ProcessoExpedienteCentralMandado pecm = new ProcessoExpedienteCentralMandado();
		ProcessoExpedienteCentralMandado instance = ProcessoExpedienteCentralMandadoHome.instance().getInstance();
		pecm.setStatusExpedienteCentral(ProcessoExpedienteCentralMandadoStatusEnum.C);
		instance.setStatusExpedienteCentral(ProcessoExpedienteCentralMandadoStatusEnum.C);

		boolean isRedistribuicao = this.isRedistribuicao();
		
		boolean isCumprido = this.isCumprido();
		
		if(!isRedistribuicao) {
			try {
				ProcessoParteExpediente ppe = null;
				if (idProcessoParteExpediente != null) {
					ppe = processoParteExpedienteManager.findById(idProcessoParteExpediente);
				}

				if(ppe != null) {
					registraCumprimento(isCumprido, ppe, instance, isRedistribuicao);
				} else if(!ParametroUtil.instance().isGerarUmMandadoPorEndereco()){
					for (ProcessoParteExpediente ppeFor : getListaDePartesDoExpediente()) {
						registraCumprimento(isCumprido, ppeFor, instance, isRedistribuicao);
					}
				}
			} catch (Exception e) {
				log.error("Ocorreu erro em DiligenciaHome.atualizar(): " + e.getLocalizedMessage());
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR, e.getMessage());
				return;
			}

		} else {
			atualizarQuandoRedistribuicao(pecm, instance);
		}

		getEntityManager().merge(instance);
		getEntityManager().flush();

		super.update();
		inserirDiligenciaVisitaList();

		FacesMessages.instance().clear();
		Contexts.removeFromAllContexts("processoExpedienteCentralMandadoGrid");
		refreshGrid("processoExpedienteCentralMandadoGrid");
	}

	private void atualizarQuandoDevolvido(boolean isCumprido, ProcessoParteExpediente ppe, ProcessoExpedienteCentralMandado instance) {

		PrazosProcessuaisService prazosService = new PrazosProcessuaisServiceImpl();
		OrgaoJulgador o = ppe.getProcessoJudicial().getOrgaoJulgador();
		OrgaoJulgador o1 = EntityUtil.getEntityManager().find(OrgaoJulgador.class, o.getIdOrgaoJulgador());					
		
		if (isCumprido) {
		    Pessoa p = ComponentUtil.getPessoaFisicaService().find(ComponentUtil.getUsuarioService().getUsuarioLogado().getIdUsuario());
		    ppe.setCienciaSistema(Boolean.FALSE);
		    ppe.setNomePessoaCiencia(p.getNome());
		    ppe.setPessoaCiencia(p);

		    Calendario calendario = prazosService.obtemCalendario(o1);
	        ComponentUtil.getProcessoParteExpedienteManager().registraCiencia(ppe, dataCumprimentoMandato, true, calendario);
		} else {
		    ppe.setFechado(true);
		    Events.instance().raiseEvent(Eventos.EVENTO_EXPEDIENTE_FECHADO, ppe.getProcessoJudicial());
		}
		this.getEntityManager().persist(ppe);
		instance.getProcessoExpediente().getProcessoTrf().setMandadoDevolvido(Boolean.TRUE);		
		dispararFluxoDiligencia(getInstance().getProcessoDocumento());		
	}

	private void atualizarQuandoRedistribuicao(
			ProcessoExpedienteCentralMandado pecm,
			ProcessoExpedienteCentralMandado instance) {
		pecm.setCentralMandado(instance.getCentralMandado());
		pecm.setProcessoExpediente(instance.getProcessoExpediente());
		pecm.setProcessoExpedienteCentralMandadoAnterior(instance);
		pecm.setEnviadoScm(instance.getEnviadoScm());
		pecm.setStatusExpedienteCentral(ProcessoExpedienteCentralMandadoStatusEnum.R);
		pecm.setPessoaGrupoOficialJustica(instance
				.getPessoaGrupoOficialJustica());
		pecm.setDtDistribuicaoExpediente(instance
				.getDtDistribuicaoExpediente());
		
		pecm.setUrgencia(instance.getUrgencia());
		if (new ParametroJtUtil().justicaTrabalho()) {
			lancarMovimentoDevolucaoJT(getInstance(), true);
		} else {
			lancarMovimentoDevolucao(getInstance(),	true);
		}
		
		getEntityManager().persist(pecm);
		ProcessoExpedienteCentralMandadoHome
				.instance()
				.getInstance()
				.setStatusExpedienteCentral(
						ProcessoExpedienteCentralMandadoStatusEnum.C);
		getEntityManager().merge(
				ProcessoExpedienteCentralMandadoHome.instance()
						.getInstance());
	}
	
	private ProcessoDocumento getProcessoDocumento() {
		return getProtocolarDocumentoBean().getDocumentoPrincipal();
	}
	
	private List<ProcessoDocumento> getDocumentosVinculados(){
		return new ArrayList<ProcessoDocumento>(getProcessoDocumento().getDocumentosVinculados());
	}
	
	private void concluirPeticionamento() {
		try {
			ProcessoDocumento pdPrincipal = this.getProcessoDocumento();
			this.getProtocolarDocumentoBean().setDocumentoPrincipal(pdPrincipal);
			this.getProtocolarDocumentoBean().setArquivos(new ArrayList<>(this.getDocumentosVinculados()));
			DocumentoJudicialService.instance().gravarAssinaturaDeProcessoDocumento(
					this.getProtocolarDocumentoBean().getArquivosAssinados(), this.getProtocolarDocumentoBean().getProcessoDocumentosParaAssinatura());			
		
			boolean resultado = getProtocolarDocumentoBean().concluir();
			
			if (resultado == false) {
				throw new Exception("Não foi possível concluir a assinatura da diligência!");
			} else {
				if(this.isCumprido()) {
					if (ProcessoExpedienteCentralMandadoHome.instance().getInstance().getParteExpedienteUnica() != null && TipoCalculoMeioComunicacaoEnum.JCD.equals
							(ProcessoExpedienteCentralMandadoHome.instance().getInstance().getParteExpedienteUnica().getTipoCalculoMeioComunicacao())) {
						
						getInstance().setDtCumprimento(pdPrincipal.getDataJuntada());
					}
				}
				FacesMessages.instance().add(Severity.INFO, "A assinatura da diligência foi concluída com sucesso.");
			}
		} catch (Exception e) {
			tratarExcecaoErroAssinatura(e);
		}
	}

	/**
	 * Limpa os arquivos assinados e exibe para o usuário a mensagem de erro que causou a exceção.
	 * 
	 * @param O erro que causou a falha da operação.
	 */
	private void tratarExcecaoErroAssinatura(Exception e) {
		this.protocolarDocumentoBean.setArquivosAssinados(new ArrayList<ArquivoAssinadoHash>());
		
		try {
			Transaction.instance().rollback();
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
		
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
	}

	public void setDocumentoInserido(Boolean documentoInserido) {
		this.documentoInserido = documentoInserido;
	}

	public Boolean getDocumentoInserido() {
		return documentoInserido;
	}

	/*
	 * Método criado para testar a redistribuição de uma visita sem precisar
	 * assinar digitalmente.
	 */
	public String inserirAtualizarDocSemAssinatura() {
		ProcessoDocumentoHome processoDocHome = ProcessoDocumentoHome
				.instance();
		FileHome fileData = FileHome.instance();

		if (contador >= 1) {
			ProcessoDocumentoHome.instance().setModelo(Boolean.FALSE);
		} else {
			ProcessoDocumentoHome.instance().setModelo(Boolean.TRUE);
		}

		if (contador >= 1) {
			if (fileData == null) {
				FacesMessages.instance().add(Severity.ERROR,
						"Selecione algum arquivo .pdf");
				return "problem";
			} else if (!fileData.getFileType().equalsIgnoreCase("PDF")) {
				FacesMessages.instance().add(Severity.ERROR,
						"O arquivo deve ser pdf");
				return null;
			}
		}
		
		processoDocHome.persist();
		contador++;
		setDocumentoInserido(Boolean.TRUE);
		if (numProc.equals(""))
			numProc = ProcessoDocumentoHome.instance().getInstance()
					.getProcesso().toString();
		// Inserção do ProcessoDocumento na entidade
		// ProcessoDocumentoExpediente.
		ProcessoDocumentoExpediente processoDocumentoExpediente = ProcessoDocumentoExpedienteHome
				.instance().getInstance();
		processoDocumentoExpediente.setAnexo(Boolean.TRUE);
		processoDocumentoExpediente.setProcessoDocumento(processoDocumento());
		processoDocumentoExpediente
				.setProcessoExpediente(ProcessoExpedienteCentralMandadoHome
						.instance().getInstance().getProcessoExpediente());

		getEntityManager().persist(processoDocumentoExpediente);
		getEntityManager().flush();
		getEntityManager().clear();
		
		iniciarDocumentos();
		FacesMessages.instance().clear();

		return "persisted";
	}

	/**
	 * Retorna o Processo em execução no Jbpm. * @return Processo
	 */
	public static ProcessoTrf getProcessoTrf() {
		Integer idProcesso = JbpmUtil.getProcessVariable("processo");
		if (idProcesso == null) {
			return null;
		}
		return EntityUtil.find(ProcessoTrf.class, idProcesso);
	}
	
	/**
	 * Método que lança o movimento de devolucao do mandado por parte do oficial
	 * de justiça.
	 */
	public void lancarMovimentoDevolucao(Diligencia diligencia,
			boolean redistribuir) {
		ProcessoExpedienteCentralMandado pecm = ProcessoExpedienteCentralMandadoHome
				.instance().getInstance();
		ProcessoExpediente processoExp = pecm.getProcessoExpediente();
		String codigoMovimento = null, complemento = null;
		Processo processo = processoExp.getProcessoTrf().getProcesso();

		codigoMovimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_COMUNICACAO_MANDADO_DEVOLVIDO;
		/**
		 * Sera lancado um movimento de acordo com o tipo de comunicacao
		 * cadastrada no tipo de documento complementado com o resultado de
		 * diligencia escolhido, se redistribuir for true o complemento sera nao cumprido.
		 */
		complemento =  getInstance().getTipoResultadoDiligencia().getCodigoResultadoDiligencia();
		
		if (complemento == null || complemento.trim().equals("") ){
			FacesMessages
				.instance()
				.add(Severity.ERROR,
						"Não foi parametrizado um código de complemento para esse tipo de resultado de diligência");
			return;
		}	

		// Registrar o movimento
		MovimentoAutomaticoService.preencherMovimento()
				.deCodigo(codigoMovimento)
				.comComplementoDeCodigo(TIPO_COMPLEMENTO_RESULTADO_DILIGENCIA)
				.doTipoDominio().preencherComElementoDeCodigo(complemento)
				.associarAoProcesso(processo).lancarMovimento();		
	}

	/**
	 * Método que lança o movimento de devolucao do mandado por parte do oficial de justiça.
	 * 
	 * @param Diligencia
	 * @param redistribuir
	 */
	public void lancarMovimentoDevolucaoJT(Diligencia diligencia,
			boolean redistribuir) {

		ProcessoExpedienteCentralMandado pecm = ProcessoExpedienteCentralMandadoHome
				.instance().getInstance();
		ProcessoExpediente processoExp = pecm.getProcessoExpediente();
		TipoProcessoDocumento tpdRecebimento = processoExp
				.getTipoProcessoDocumento();
		TipoProcessoDocumento tpdDevolucao = null;
		Agrupamento agrupamento = null;
		String codigoMovimento = null;
		Evento eventoProcessual = null;
		boolean codigoDiferente = false;
		Processo processo = processoExp.getProcessoTrf().getProcesso();

		if (tpdRecebimento.getInTipoExpediente() != null) {

			for (TipoProcessoDocumento tpd : getTipoProcessoDocumentoList(
					TipoComunicacaoEnum.D, tpdRecebimento.getInTipoExpediente())) {
				tpdDevolucao = tpd;
				agrupamento = tpdDevolucao.getAgrupamento();

				if (agrupamento != null) {
					String codEvento = null;
					List<Evento> eventoList = ComponentUtil.getComponent(EventoAgrupamentoManager.class)
							.recuperarEventos(tpd.getAgrupamento());
					
					for (Evento evento : eventoList) {
						eventoProcessual = LancadorMovimentosService.instance().getEventoProcessualById(evento.getId());
						if (eventoProcessual != null) {
							codEvento = eventoProcessual.getCodEvento();
							if (codEvento != null) {
								if (codigoMovimento == null) {
									codigoMovimento = codEvento;
								}
							} else if (codigoMovimento
									.equalsIgnoreCase(codEvento)) {
								codigoDiferente = true;
							}
						}
					}
				}
			}
		} else {
			FacesMessages.instance().clear();
			FacesMessages
					.instance()
					.add(Severity.ERROR,
							"Não existe um Tipo de Expediente configurado para o Tipo de Documento.");
		}
		FacesMessages.instance().clear();
		if (codigoMovimento == null || eventoProcessual == null) {
			FacesMessages
					.instance()
					.add(Severity.ERROR,
							"Não existe um Evento configurado para este Tipo de Processo Expediente");
		}
		if (agrupamento == null) {
			FacesMessages
					.instance()
					.add(Severity.ERROR,
							"Não existe um agrupamento configurado para este Tipo de Processo Expediente");
		}
		if (tpdDevolucao == null) {
			FacesMessages
					.instance()
					.add(Severity.ERROR,
							"Não existe devolução configurada para este Tipo de Expediente");
		}
		if (codigoDiferente) {
			FacesMessages
					.instance()
					.add(Severity.ERROR,
							"Existe mais de um evento de devolução configurado para este Tipo de Expediente");
		}
		if (codigoMovimento == null || agrupamento == null
				|| tpdDevolucao == null || eventoProcessual == null
				|| codigoDiferente) {
			return;
		}

		/**
		 * Sera lancado um movimento de acordo com o tipo de comunicacao
		 * cadastrada no tipo de documento complementado com o resultado de
		 * diligencia escolhido, se redistribuir for true o complemento sera nao cumprido.
		 */
		String complemento = redistribuir ? DILIGENCIA_NAO_CUMPRIDA
				: getInstance().getTipoResultadoDiligencia()
						.getTipoResultadoDiligencia();
		
		MovimentoAutomaticoService.preencherMovimento()
				.deCodigo(eventoProcessual.getCodEvento())
				.associarAoProcesso(processo).comProximoComplementoVazio()
				.preencherComTexto(complemento).lancarMovimento();
	}

	/**
	 * Método que retorna a lista de TipoProcessoDocumento de um tipo de Expediente e de um tipo de comunicacao.
	 * 
	 * @param tipoDeComunicacao
	 * @param tipoDeExpediente
	 * @return List<TipoProcessoDocumento>
	 */
	@SuppressWarnings("unchecked")
	private List<TipoProcessoDocumento> getTipoProcessoDocumentoList(
			TipoComunicacaoEnum tipoComunicacao,
			TipoExpedienteEnum tipoExpediente) {

		StringBuilder sb = new StringBuilder();
		sb.append("select o from TipoProcessoDocumento o ")
				.append("where o.inTipoComunicacao = :inTipoComunicacao and o.inTipoExpediente = :inTipoExpediente ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("inTipoComunicacao", tipoComunicacao);
		q.setParameter("inTipoExpediente", tipoExpediente);

		List<TipoProcessoDocumento> tipoProcessoDocumentoList = q.getResultList();

		return tipoProcessoDocumentoList;
	}

	private void registraCumprimento(boolean isCumprido, ProcessoParteExpediente ppe, ProcessoExpedienteCentralMandado instance, boolean isRedistribuicao) throws ApplicationException {
		atualizarQuandoDevolvido(isCumprido, ppe, instance);
		
		lancarMovimentoDevolucao(getInstance(),	isRedistribuicao);
		getEntityManager().merge(instance.getProcessoExpediente().getProcessoTrf());
		
	}
	
	public ModeloDocumentoLocal getModeloDocumento() {
		return modeloDocumento;
	}

	public void setModeloDocumento(ModeloDocumentoLocal modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	public ProcessoDocumento getPdHtml() {
		return pdHtml;
	}

	public void setPdHtml(ProcessoDocumento pdHtml) {
		this.pdHtml = pdHtml;
	}

	public ProcessoDocumentoBin getPdbHtml() {
		return pdbHtml;
	}

	public void setPdbHtml(ProcessoDocumentoBin pdbHtml) {
		this.pdbHtml = pdbHtml;
	}

	public ProcessoDocumento getPdPdf() {
		return pdPdf;
	}

	public void setPdPdf(ProcessoDocumento pdPdf) {
		this.pdPdf = pdPdf;
	}

	public ProcessoDocumentoBin getPdbPdf() {
		return pdbPdf;
	}

	public void setPdbPdf(ProcessoDocumentoBin pdbPdf) {
		this.pdbPdf = pdbPdf;
	}

	public String getCertChain() {
		return certChain;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public Boolean getAbreToggle() {
		return abreToggle;
	}

	public void setAbreToggle(Boolean abreToggle) {
		this.abreToggle = abreToggle;
	}

	public Boolean getFinalizar() {
		return finalizar;
	}

	public void setFinalizar(Boolean finalizar) {
		this.finalizar = finalizar;
	}

	public void validacaoPdf() {
		if (!isDocumentoBinValido(FileHome.instance())) {
			return;
		}
	}

	private boolean isDocumentoBinValido(FileHome file) {
		if ((file == null || file.getData() == null)) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Nenhum documento selecionado.");
			return false;
		}
		try {
			br.com.itx.util.FileUtil.validarTipoTamanhoArquivo(file, "application/pdf");
		} catch (PJeBusinessException ex) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, ex.getCode());
			return false;
		}
		return true;
	}

	public void gravarDocumentos() {
		ProcessoDocumentoBinHome.instance().setIsAssinarDocumento(false);
		ProcessoDocumentoBinHome.instance().setInstance(pdbHtml);
		ProcessoDocumentoHome.instance().setInstance(pdHtml);
		if (ProcessoDocumentoHome.instance().getInstance()
				.getIdProcessoDocumento() != 0) {
			ProcessoDocumentoHome.instance().update();
			EntityUtil.getEntityManager().merge(
					ProcessoDocumentoHome.instance().getInstance());
			EntityUtil.flush();
			FacesMessages.instance().clear();
			FacesMessages.instance().add("Registro modificado com sucesso.");
		} else {
			ProcessoDocumentoBinHome.instance().isModelo(true);
			ProcessoDocumentoHome.instance().persist();
			FacesMessages.instance().clear();
			FacesMessages.instance().add("Registro inserido com sucesso.");
		}
	}

	public void addPdf() {
		if (!isDocumentoBinValido(FileHome.instance())) {
			return;
		}
		// Se não houver um html confeccionado, não pode inserir um pdf.
		if (!verificaSituacaoHtml()) {
			FacesMessages
					.instance()
					.add(Severity.ERROR,
							"Operação não permitida. É obrigatória a inclusão de petição/documento no editor.");
			return;
		}
		AnexarDocumentos.instance().ajustarProcessoDocumento(pdPdf);
		AnexarDocumentos.instance().ajustarProcessoDocumentoBin(pdbPdf,
				FileHome.instance());

		AnexarDocumentos.instance().setPdbHtml(pdbHtml);
		AnexarDocumentos.instance().gravarPdf(pdPdf, pdbPdf);
		newInstancePdf();
		FacesMessages.instance().add(Severity.INFO,
				"Documento anexado com sucesso.");
	}

	private boolean verificaSituacaoHtml() {
		return (pdHtml != null && pdbHtml != null
				&& pdHtml.getTipoProcessoDocumento() != null
				&& !Strings.isEmpty(pdbHtml.getModeloDocumento()) && !Strings
					.isEmpty(pdHtml.getProcessoDocumento()));
	}

	private void newInstanceHtml() {
		ProcessoDocumentoHome.instance().newInstance();
		ProcessoDocumentoBinHome.instance().newInstance();
		pdHtml = ProcessoDocumentoHome.instance().getInstance();
		pdbHtml = ProcessoDocumentoBinHome.instance().getInstance();
	}

	private void newInstancePdf() {
		pdPdf = new ProcessoDocumento();
		pdbPdf = new ProcessoDocumentoBin();
		FileHome.instance().clear();
	}

	public void removerPdf(ProcessoDocumento obj) {
		ProcessoDocumentoHome.instance().removerDocumento(obj,
				obj.getProcesso().getIdProcesso());
		newInstancePdf();
	}

	public String getUrlDocsField() {
		AnexarDocumentos.instance().setPdbHtml(pdbHtml);
		ProcessoDocumentoHome.instance().setInstance(pdHtml);
		urlDocsField = AnexarDocumentos.instance().getUrlDocsField();
		return urlDocsField;
	}

	public void setUrlDocsField(String url) {
		this.urlDocsField = url;

	}

	public void assinarIndividual() {
		AnexarDocumentos ad = AnexarDocumentos.instance();
		try {
			VerificaCertificadoPessoa
					.verificaCertificadoPessoaLogada(certChain);
			ad.setPdHtml(pdHtml);
			ad.setPdbHtml(pdbHtml);
			ad.setCertChain(certChain);
			ad.setSignature(signature);
			ProcessoDocumentoHome.instance().setInstance(pdHtml);
			boolean assinou = ad.assinar();
			if (assinou) {
				iniciarDocumentos();
				setFinalizar(Boolean.TRUE);
			}
		} catch (CertificadoException e) {
			String msgErro = "Erro na verificação do certificado: "
					+ e.getMessage();
			FacesMessages.instance().add(Severity.ERROR, msgErro);
			log.error("Erro na verificação do certificado" + e.getMessage());
			return;
		}
	}

	@SuppressWarnings("static-access")
	public void processarModelo() {
		if (modeloDocumento != null) {
			pdbHtml.setModeloDocumento(ProcessoDocumentoHome.instance()
					.processarModelo(modeloDocumento.getModeloDocumento()));
		} else {
			pdbHtml.setModeloDocumento(null);
		}
	}

	public Date getDataCumprimentoMandato() {
		return dataCumprimentoMandato;
	}

	public void setDataCumprimentoMandato(Date dataCumprimentoMandato) {
		this.dataCumprimentoMandato = dataCumprimentoMandato;
	}
	
	public boolean isExibeTipoCalculoPrazo() {
		return exibeTipoCalculoPrazo;
	}

	public void setExibeTipoCalculoPrazo(boolean exibeTipoCalculoPrazo) {
		this.exibeTipoCalculoPrazo = exibeTipoCalculoPrazo;
	}

	/*
	 * renderização de informação em lista.
	 */
	public List<ProcessoParteExpediente> getProcessoParteExpedienteList() {

		List<ProcessoParteExpediente> retorno = new ArrayList<ProcessoParteExpediente>();

		List<ProcessoParteExpedienteVisita> listProcessoParteExpedienteVisita;
		for (Visita visita : getVisitaList()) {
			listProcessoParteExpedienteVisita = visita
					.getProcessoParteExpedienteVisitaList();
			for (ProcessoParteExpedienteVisita processoParteExpedienteVisita : listProcessoParteExpedienteVisita) {
				retorno.add(processoParteExpedienteVisita
						.getProcessoParteExpediente());
			}
		}

		return retorno;
	}

	public List<TipoResultadoDiligencia> getTipoResultadoDiligenciaRedistribuido() {
		TipoResultadoDiligencia tipoResultadoDiligenciaRedistribuido = null;
		List<TipoResultadoDiligencia> lista = new ArrayList<TipoResultadoDiligencia>();

		try {
			tipoResultadoDiligenciaRedistribuido = ParametroUtil.instance()
					.getTipoResultadoDiligenciaRedistribuicao();
		} catch (Exception e) {
			FacesMessages
					.instance()
					.add(Severity.ERROR,
							"Ocorreu o seguinte erro ao tentar recuperar "
									+ "o parâmetro 'idTipoResultadoDiligenciaRedistribuicao': "
									+ e.getMessage());
		}

		// Se o código do tipo de resultado de diligência não foi cadastrado...
		if (tipoResultadoDiligenciaRedistribuido == null) {
			FacesMessages
					.instance()
					.add(Severity.INFO,
							"O parâmetro 'idTipoResultadoDiligenciaRedistribuicao' não foi cadastrado "
									+ " ou está configurado com um valor sem correspondência "
									+ "na tabela de tipos de resultado de diligência.");
		} else {
			lista.add(tipoResultadoDiligenciaRedistribuido);
		}

		return lista;
	}
	
	/**
	 * Método responsável por disponibilizar a aba para cadastro de novo endereço à pessoa diligenciada, 
	 * quando o resultado da diligência for Redistribuído.
	 * @return boolean Retorna verdadeiro caso seja encontrada a parte (Pessoa), a fim de permitir a renderização da aba de inclusão de endereços para ela.
	 */
	public boolean renderizaTabEndereco(){		
		List<ProcessoParteExpediente> listaParte = VisitaHome.instance().getProcessoParteExpedienteList();
		if(listaParte != null && !listaParte.isEmpty()){
			Pessoa pessoa = listaParte.get(0).getPessoaParte(); 

			try {
				PreCadastroPessoaBean preCadastroPessoaBean = (PreCadastroPessoaBean) Component.getInstance(PreCadastroPessoaBean.class, true);
				if(pessoa.getInTipoPessoa().equals(TipoPessoaEnum.F)){
					pessoaFisica = ComponentUtil.getPessoaFisicaManager().findById(pessoa.getIdPessoa());
					preCadastroPessoaBean.initCadastroPessoaFisica();
					preCadastroPessoaBean.setPessoaFisica(pessoaFisica);

				}else if(pessoa.getInTipoPessoa().equals(TipoPessoaEnum.J)){
					pessoaJuridica = ComponentUtil.getPessoaJuridicaManager().findById((pessoa).getIdPessoa());
					preCadastroPessoaBean.initCadastroPessoaJuridica();
					preCadastroPessoaBean.setPessoaJuridica(pessoaJuridica);

				}else{
					pessoaAutoridade = ComponentUtil.getPessoaAutoridadeManager().findById(pessoa.getIdPessoa());
					preCadastroPessoaBean.setPessoaAutoridade(pessoaAutoridade);				
				}
				preCadastroPessoaBean.confirmarPessoa();
				return true;

			} catch (PJeBusinessException e) {
				log.error("Ocorreu erro ao recuperar objeto Pessoa em DiligenciaHome.renderizaTabEnderecos(): " + e.getLocalizedMessage());
			}
		}
		return false;

	}
	
	/**
	 * retorna o objeto protocolarDocumentoMBean fazendo as verificações
	 * para evitar duplicidade do objeto.
	 * 
	 * @return ProtocolarDocumentoBean
	 */
	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {	
		if(protocolarDocumentoBean == null){
			protocolarDocumentoBean = createProtocolarDocumentoBean();
		}
		return protocolarDocumentoBean;
	}
	
	public void setProtocolarDocumentoBean(ProtocolarDocumentoBean protocolarDocumentoBean) {
		this.protocolarDocumentoBean = protocolarDocumentoBean;
	}

	public boolean isDocumentoRespostaDiligenciaPdfAnexado() {
		if (protocolarDocumentoBean != null
				&& protocolarDocumentoBean.getDocumentoPrincipal() != null
				&& protocolarDocumentoBean.getDocumentoPrincipal().getProcessoDocumentoBin() != null
				&& protocolarDocumentoBean.getDocumentoPrincipal().getProcessoDocumentoBin().getNomeArquivo() != null) {
			return true;
		}

		return false;
	}

	public void vincularDocumento() {
		ProcessoDocumento documentoPrincipal = getProtocolarDocumentoBean() == null ? null
				: getProtocolarDocumentoBean().getDocumentoPrincipal();

		if (documentoPrincipal != null && documentoPrincipal.getTipoProcessoDocumento() != null) {
			documentoPrincipal.setExclusivoAtividadeEspecifica(Boolean.TRUE);

			Diligencia diligencia = getInstance();

			diligencia.setProcessoDocumento(documentoPrincipal);

			getEntityManager().merge(diligencia);
			getEntityManager().flush();
		}
	}

	public Boolean possuiProcessoDocumento(ProcessoExpedienteCentralMandado mandado){
		if (mandado == null) {
			return false;
		}
		Diligencia diligencia = getDiligenciaAtual(mandado, Authenticator.getPessoaLogada());
		if (diligencia != null) {
			Boolean result = !(diligencia.getProcessoDocumento() == null);
			return result;
		}
		return false;
	}
	
	public Diligencia getDiligenciaAtual(ProcessoExpedienteCentralMandado processoExpedienteCentralMandado, Pessoa pessoa){
		PessoaOficialJustica pessoaOficialJustica = ((PessoaFisica) pessoa).getPessoaOficialJustica();
		for (Diligencia diligencia : processoExpedienteCentralMandado.getDiligenciaList()) {
			if (diligencia.getAtivo() && pessoaOficialJustica.equals(diligencia.getPessoaOficialJustica())) {
				return diligencia;
			}
		}
		return null;
	}

	public void cancelarDiligencia() {
		ProcessoExpedienteCentralMandado pecm = ProcessoExpedienteCentralMandadoHome.instance().getInstance();
		ProcessoDocumentoHome processoDocHome = ProcessoDocumentoHome.instance();
		DocumentoJudicialService documentoJudicialService = (DocumentoJudicialService) Component.getInstance("documentoJudicialService", ScopeType.EVENT);
		pecm.getDiligenciaList().remove(instance);
		if (!isManaged()) {
			instance = EntityUtil.getEntityManager().merge(instance);
		}
		if (getProtocolarDocumentoBean().getDocumentoPrincipal()!=null) {
			try {
				getProtocolarDocumentoBean().removeArquivos();
			} catch (Exception e) {
				e.printStackTrace();
			}
   		    
			exibeTipoCalculoPrazo = false;
			
			getEntityManager().remove(instance);
			getEntityManager().flush();
			
			ProcessoDocumento pd = getProtocolarDocumentoBean().getDocumentoPrincipal();
			processoDocHome.removerDocumento(pd, pecm.getProcessoExpediente().getProcessoTrf().getIdProcessoTrf());
			
			getProtocolarDocumentoBean().setDocumentoPrincipal(documentoJudicialService.getDocumento());
			setInstance(createInstance());
		}
		abreToggle = Boolean.FALSE;
	}

	public ProtocolarDocumentoBean createProtocolarDocumentoBean() {
		if(idProcessoSelecionado != null){
			protocolarDocumentoBean = new ProtocolarDocumentoBean(idProcessoSelecionado, 
						ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL 
						| ProtocolarDocumentoBean.LANCAR_MOVIMENTACAO
						| ProtocolarDocumentoBean.VINCULAR_DATA_JUNTADA
						| ProtocolarDocumentoBean.UTILIZAR_MODELOS
						| ProtocolarDocumentoBean.CARREGA_DOCUMENTO_PENDENTE_DE_ATIVIDADE_ESPECIFICA,getActionName());
//			ArrayList<TipoProcessoDocumento> tiposDocTexto = new ArrayList<TipoProcessoDocumento>();
//			tiposDocTexto.add(ParametroUtil.instance().getTipoProcessoDocumentoDiligencia());
//			protocolarDocumentoBean.setTiposDocumentosTexto(tiposDocTexto);
			protocolarDocumentoBean.setTipoPrincipal(ParametroUtil.instance().getTipoProcessoDocumentoDiligencia());
		}
		return protocolarDocumentoBean;
	}

	/**
	 * [PJEII-21868] Inicia-se o fluxo cadastrado para o tipo de documento "Diligência" em   
	 * 'Configuração -> Documento -> Tipo de Documento'.  
	 *  
	 * No caso, o método ProcessoJudicialService.iniciarFluxoIncidental é executado. Caso não 
	 * haja nenhum fluxo cadastrado para o tipo de documento, nenhum fluxo adicional é executado.
	 * 
	 * O objetivo é transferir o agrupador de processo com mandado devolvido para as caixas de tarefas.
	 * Para isso, foi criado um novo fluxo apenas para exibição do mandado devolvido.
	 * Quando o mandado é lido no novo fluxo criado, o processo também é retirado do agrupador de mesmo nome. 
	 * 
	 */
	private void dispararFluxoDiligencia(ProcessoDocumento doc){
		if(doc != null && 
		   doc.getIdProcessoDocumento() > 0 && 
		   doc.getTipoProcessoDocumento() != null && 
		   doc.getTipoProcessoDocumento().getFluxo() != null) {
			Events.instance().raiseEvent(Eventos.INICIAR_FLUXO_PETICAO_INCIDENTAL, doc.getIdProcessoDocumento());
		}
	}
		
	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.protocolarDocumentoBean.addArquivoAssinado(arquivoAssinadoHash);
	}

	@Override
	public String getActionName() {
		return NAME;
	}

	public Integer getIdProcessoParteExpediente() {
		return idProcessoParteExpediente;
	}

	public void setIdProcessoParteExpediente(Integer idProcessoParteExpediente) {
		this.idProcessoParteExpediente = idProcessoParteExpediente;
	}

	public Boolean getIsDataCumprimentoBloqueada() {
		return isDataCumprimentoBloqueada;
	}

	public void setIsDataCumprimentoBloqueada(Boolean isDataCumprimentoBloqueada) {
		this.isDataCumprimentoBloqueada = isDataCumprimentoBloqueada;
	}
}
