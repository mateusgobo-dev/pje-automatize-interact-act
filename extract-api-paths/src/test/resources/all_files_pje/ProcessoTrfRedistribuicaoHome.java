package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import br.com.infox.utils.Constantes;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.TransformerUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.transaction.Transaction;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.pje.list.ProcessoDocumentoRedistribuicaoEncaminhamentoList;
import br.com.itx.component.SelectItemsQuery;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.com.jt.pje.manager.OrgaoJulgadorColegiadoOrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional.COD_COMPLEMENTO_MOTIVO_REDISTRIBUICAO;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional.COD_COMPLEMENTO_TIPO_DISTRIBUICAO_REDISTRIBUICAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.VinculacaoDependenciaEleitoralManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.servicos.AutuacaoService;
import br.jus.cnj.pje.servicos.DistribuicaoService;
import br.jus.cnj.pje.servicos.NoDeDesvioService;
import br.jus.cnj.pje.servicos.PrevencaoService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.PesquisaProcessoParadigmaAction;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService.MovimentoBuilder;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService.MovimentoBuilder.ComplementoBuilder;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.nucleo.entidades.Assunto;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCompetencia;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.entidades.ProcessoTrfLogDistribuicao;
import br.jus.pje.nucleo.entidades.ProcessoTrfRedistribuicao;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.VinculacaoDependenciaEleitoral;
import br.jus.pje.nucleo.enums.CasoCompetenciaEnum;
import br.jus.pje.nucleo.enums.ClasseJudicialInicialEnum;
import br.jus.pje.nucleo.enums.PrevencaoEnum;
import br.jus.pje.nucleo.enums.TipoConexaoEnum;
import br.jus.pje.nucleo.enums.TipoDistribuicaoEnum;
import br.jus.pje.nucleo.enums.TipoRedistribuicaoEnum;

@Name(ProcessoTrfRedistribuicaoHome.NAME)
public class ProcessoTrfRedistribuicaoHome extends AbstractProcessoTrfRedistribuicaoHome<ProcessoTrfRedistribuicao> {

	public static final String NAME = "processoTrfRedistribuicaoHome";
	private static final long serialVersionUID = 1L;
	private TipoRedistribuicaoEnum inTipoRedistribuicao;
	
	private List<Competencia> competencias = new ArrayList<Competencia>(0);
	private String mensagemProtocolacao;
	private boolean atualiza;
	private Jurisdicao jurisdicaoSorteio;
	private Jurisdicao jurisdicao;
	
	private ProcessoTrf processoTrfRedistribuicao;
	private String processoRedistribuicao;
	private Jurisdicao jurisdicaoRedistribuicao;
	private OrgaoJulgador orgaoJulgadorRedistribuicao;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiadoRedistribuicao;

	private ProcessoDocumento processoDocumentoVinculado;
	
	private Competencia competenciaRedistribuicao;
	private CasoCompetenciaEnum casoCompetencia;
		
	@In
	private VinculacaoDependenciaEleitoralManager vinculacaoDependenciaEleitoralManager;
	
	@In
	private OrgaoJulgadorManager orgaoJulgadorManager;

	@In
	private OrgaoJulgadorColegiadoManager orgaoJulgadorColegiadoManager;
	
	@In
	private ParametroService parametroService;
	
	@In(required=false)
	private ProcessInstance processInstance;
	
	@In(required=false)
	private TaskInstance taskInstance;
	
	public ProcessoDocumento getProcessoDocumentoVinculado() {
		return processoDocumentoVinculado;
	}

	public void setProcessoDocumentoVinculado(ProcessoDocumento processoDocumentoVinculado) {
		if (this.processoDocumentoVinculado != null && 
				this.processoDocumentoVinculado.equals(processoDocumentoVinculado)) {
			
			this.processoDocumentoVinculado = null; // Realiza o processo de desvincular.
		} else {
			this.processoDocumentoVinculado = processoDocumentoVinculado;
		}
	}

	// Atributo que define se a janela vai fechar, após submeter o formulário de redistribuição.
	private boolean podeFecharJanela = false;
	
	public String getMensagemProtocolacao() {
		return this.mensagemProtocolacao;
	}

	public boolean getHouveErroProtocolacao() {
		return this.mensagemProtocolacao != null && this.mensagemProtocolacao.contains("Erro");
	}
	
	public void gravarRedistribuicao() {
		String defaultTransition = null;
		ProcessoTrf processoTrf = obterProcessoTrf();
		Set<org.jbpm.graph.def.Transition> transitions = null;
		org.jbpm.graph.exe.Token token = null;
		if(taskInstance != null){
			token = taskInstance.getToken();
			defaultTransition = (String) taskInstance.getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		}else if(processInstance != null){
			token = processInstance.getRootToken();
		}else{
			processInstance = ManagedJbpmContext.instance().getProcessInstance(processoTrf.getProcesso().getIdJbpm());
			token = processInstance.getRootToken();
		}
		transitions = token.getAvailableTransitions();
		for (org.jbpm.graph.def.Transition transition : transitions) {
			if (!transition.getName().equalsIgnoreCase(NoDeDesvioService.getNomeNoDesvio(transition.getFrom().getProcessDefinition()))) {
				if(defaultTransition != null && !transition.getName().equals(defaultTransition)){
					continue;
				}
				token.signal(transition);
				break;
			}
		}
		newInstance();
	}

	public static ProcessoTrfRedistribuicaoHome instance() {
		return ComponentUtil.getComponent(NAME);
	}
	
	public void gravarRedistribuicaoTipoPrevencaoDependencia() {
		carregarTipoDistribuicaoProcesso();
		
		switch (this.getInTipoRedistribuicao()){
			case E:
				gravarRedistribuicaoReuniaoExecucoes();
				break;
			case J:
				gravarRedistribuicaoDeterminacaoJudicial();
				break;
			case M:
				gravarRedistribuicaoErroMaterial();
				break;
			case U:
				gravarRedistribuicaoCriacaoUnidadeJudiciaria();
				break;
			case X:
				gravarRedistribuicaoExtincaoUnidadeJudiciaria();
				break;
			case C:
				gravarRedistribuicaoAlteracaoCompetenciaOrgao();
				break;
			case I:
				gravarRedistribuicaoImpedimento();
				break;
			case S:
				gravarRedistribuicaoSuspeicao();
				break;
			case O:
				gravarRedistribuicaoSuspeicaoRelator();
				break;
			case W:
				gravarRedistribuicaoRecusaPrevencaoDependencia();
				break;
			default:
				FacesMessages.instance().clear();
				this.mensagemProtocolacao = "Motivo inválido para redistribuição pelo tipo prevenção / dependência";
				break;
			}
	}
	
	public void gravarRedistribuicaoTipoCompetenciaExclusiva() {
		carregarTipoDistribuicaoProcesso();
		
		switch (this.getInTipoRedistribuicao()){
			case W:
				gravarRedistribuicaoRecusaPrevencaoDependencia();
				break;
			case J:
				gravarRedistribuicaoDeterminacaoJudicial();
				break;
			default:
				FacesMessages.instance().clear();
				this.mensagemProtocolacao = "Motivo inválido para redistribuição pelo tipo competência exclusiva";
				break;
			}
	}
	
	/**
	 * Método responsável por gravar a redistribuição do processo quando escolhido o 
	 * tipo "Por encaminhamento" e lançar o movimento processual associado ao documento vinculado.
	 */
	public void gravarRedistribuicaoTipoEncaminhamento() {
		try{
			if(processoDocumentoVinculado == null){
				String msg = FacesUtil.getMessage("processoTrfRedistribuicao.erro.nenhumDocumentoSelecionado");
				String nomesTipos = obterNomesTiposDocumentos();
				if(nomesTipos != null && !nomesTipos.isEmpty()) {
					msg = FacesUtil.getMessage("processoTrfRedistribuicao.erro.documentoNaoSelecionadoTipo", (Object) nomesTipos);
				}
				throw new NegocioException(msg);
			}
			TipoRedistribuicaoEnum motivoRedistribuicao = TipoRedistribuicaoEnum.J;
			TipoDistribuicaoEnum tipoRedistribuicao = TipoDistribuicaoEnum.EN;
			this.identificaMotivoRedistribuicao(motivoRedistribuicao);

			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);

			getInstance().setInTipoDistribuicao(tipoRedistribuicao);
			getInstance().setJurisdicao(getJurisdicaoRedistribuicao());
			getInstance().setOrgaoJulgador(getOrgaoJulgadorRedistribuicao());
			if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiado(getOrgaoJulgadorColegiadoRedistribuicao());
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			}

			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			distribuicaoService.setCompetenciaConflito(getCompetenciaRedistribuicao());
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();

			this.lancarMovimentoRedistribuicao(processoTrf, tipoRedistribuicao, motivoRedistribuicao, this.processoDocumentoVinculado);

			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			if (!(e instanceof PJeBusinessException)) {
				rollback();
			}
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}

	/*
	 * Criação de método para tratar novo motivo de redistribuição (Afastamento do relator).
	 */
	public void gravarRedistribuicaoAfastamentoRelator() {
		try {
			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.A);

			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);
			getInstance().setInTipoDistribuicao(TipoDistribuicaoEnum.S);
	        if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			}
			getInstance().setOrgaoJulgadorAnterior(processoTrf.getOrgaoJulgador());
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			distribuicaoService.setCompetenciaConflito(getCompetenciaRedistribuicao());
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();
			
			this.lancarMovimentoRedistribuicao(processoTrf, TipoDistribuicaoEnum.S, TipoRedistribuicaoEnum.A, this.processoDocumentoVinculado);

			setAtualiza(true);
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}
	
	/**
	 * Método para tratar novo motivo de redistribuição (Alteração da Competência do Órgão).
	 */
	public void gravarRedistribuicaoAlteracaoCompetenciaOrgao() {
		try {
			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.C);
			TipoDistribuicaoEnum tipoDistribuicao = TipoDistribuicaoEnum.S;
			
			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);
			
			getInstance().setInTipoDistribuicao(tipoDistribuicao);
			if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			}
			getInstance().setOrgaoJulgadorAnterior(processoTrf.getOrgaoJulgador());
			
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			distribuicaoService.setCompetenciaConflito(processoTrf.getCompetencia());
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			
			this.lancarMovimentoRedistribuicao(processoTrf, tipoDistribuicao, TipoRedistribuicaoEnum.C, this.processoDocumentoVinculado);
			
			gravarRedistribuicao();
			setAtualiza(true);
			
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}
	
	/**
	 * Criação de novo motivo de redistribuição (Desaforamento).
	 */
	public void gravarRedistribuicaoDesaforamento() {
		try {
			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);
			
			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.D);
			
			if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			}
			getInstance().setOrgaoJulgadorAnterior(processoTrf.getOrgaoJulgador());
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			distribuicaoService.setCompetenciaConflito(getCompetenciaRedistribuicao());
			processoTrf.setJurisdicao(jurisdicaoRedistribuicao);
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();

			this.lancarMovimentoRedistribuicao(processoTrf, TipoDistribuicaoEnum.S, TipoRedistribuicaoEnum.D, this.processoDocumentoVinculado);
			
			setAtualiza(true);
			
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}
	
	/**
	 * Criação de novo motivo de redistribuição (Reunião de Execuções).
	 */
	public void gravarRedistribuicaoReuniaoExecucoes() {
		this.redistribuicaoTipoPrevencao(TipoRedistribuicaoEnum.E);
	}
	
	public void gravarRedistribuicaoImpedimento() {
		try {
			String causaDoImpedimento = (String) ((this.getInstance().getCausaImpedimento() != null)? " ," + this.getInstance().getCausaImpedimento():"");

			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.I, causaDoImpedimento);

			carregarTipoDistribuicaoProcesso();
			TipoDistribuicaoEnum tipoDistribuicao = getInstance().getInTipoDistribuicao(); 
			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);

	        if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			}
			getInstance().setOrgaoJulgadorAnterior(processoTrf.getOrgaoJulgador());			
			if(this.getJurisdicaoRedistribuicao() != null) {
				getInstance().setJurisdicao(getJurisdicaoRedistribuicao());
			}
			
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			distribuicaoService.setCompetenciaConflito(processoTrf.getCompetencia());
			if(competenciaRedistribuicao != null) {
				distribuicaoService.setCompetenciaConflito(competenciaRedistribuicao);
			}
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();
			
			this.lancarMovimentoRedistribuicao(processoTrf, tipoDistribuicao, TipoRedistribuicaoEnum.I, this.processoDocumentoVinculado);

			setAtualiza(true);
			
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}
	
	public void gravarRedistribuicaoDeterminacaoJudicial() {		
		switch (this.getInstance().getInTipoDistribuicao()){		
			case PD:
				gravarRedistribuicaoDependenciaDeterminacaoJudicial();
				break;
			case PP:
				gravarRedistribuicaoPrevencaoDeterminacaoJudicial();
				break;
			case S:
				gravarRedistribuicaoSorteioDeterminacaoJudicial();
				break;
			case CE:
				gravarRedistribuicaoCompetenciaExclusivaDeterminacaoJudicial();
				break;
			default:
				FacesMessages.instance().clear();
				this.mensagemProtocolacao = "Tipo inválido para distribuição por determinação judicial.";
				break;
			}
	}
	
	private void gravarRedistribuicaoCompetenciaExclusivaDeterminacaoJudicial() {
		try {
			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.J);
			
			ProcessoTrf processoTrf = obterProcessoTrf();			
			processoTrf.setIsIncidente(false);
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			if(ParametroUtil.instance().isPrimeiroGrau()) {
				processoTrf.setJurisdicao(getJurisdicaoRedistribuicao());
				processoTrf.setOrgaoJulgador(getOrgaoJulgadorRedistribuicao());
			}
	        if(!ParametroUtil.instance().isPrimeiroGrau() && (competenciaRedistribuicao != null)) {
				distribuicaoService.setCompetenciaConflito(competenciaRedistribuicao);
			}
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();

			this.lancarMovimentoRedistribuicao(processoTrf, TipoDistribuicaoEnum.CE, TipoRedistribuicaoEnum.J, this.processoDocumentoVinculado);
			
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}

	/** 
	 * Método para tratar novo motivo de redistribuição (em razão da posse do relator em cargo diretivo do tribunal).
	 */
	public void gravarRedistribuicaoRazaoPosseRelator() {
		try {
			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.K);

			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);
			getInstance().setInTipoDistribuicao(TipoDistribuicaoEnum.S);
	        if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			}
			getInstance().setOrgaoJulgadorAnterior(processoTrf.getOrgaoJulgador());
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			distribuicaoService.setCompetenciaConflito(getCompetenciaRedistribuicao());
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();
			
			this.lancarMovimentoRedistribuicao(processoTrf, TipoDistribuicaoEnum.S, TipoRedistribuicaoEnum.K, this.processoDocumentoVinculado);

			setAtualiza(true);
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}
	
	/**
	 * Criação de novo motivo de redistribuição (Erro material).
	 */
	public void gravarRedistribuicaoErroMaterial() {		
		switch (this.getInstance().getInTipoDistribuicao()){		
			case PD:
				gravarRedistribuicaoDependenciaErroMaterial();
				break;
			case PP:
				gravarRedistribuicaoPrevencaoErroMaterial();
				break;
			case S:
				gravarRedistribuicaoSorteioErroMaterial();
				break;
			default:
				FacesMessages.instance().clear();
				this.mensagemProtocolacao = "Tipo inválido para distribuição por erro material.";
				break;
			}
	}
	
	/**
	 * Método para tratar novo motivo de redistribuição (Impedimento do relator).
	 */
	public void gravarRedistribuicaoImpedimentoRelator() {
		try {
			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.N);

			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);
			getInstance().setInTipoDistribuicao(TipoDistribuicaoEnum.S);
	        if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			}
			getInstance().setOrgaoJulgadorAnterior(processoTrf.getOrgaoJulgador());
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			distribuicaoService.setCompetenciaConflito(getCompetenciaRedistribuicao());
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();
			
			this.lancarMovimentoRedistribuicao(processoTrf, TipoDistribuicaoEnum.S, TipoRedistribuicaoEnum.N, this.processoDocumentoVinculado);
			
			setAtualiza(true);
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}
	
	/**
	 * Método para tratar novo motivo de redistribuição (Suspeição do relator).
	 */
	public void gravarRedistribuicaoSuspeicaoRelator() {
		try {
			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.O);

			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);
			getInstance().setInTipoDistribuicao(TipoDistribuicaoEnum.S);

			if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			}
			getInstance().setOrgaoJulgadorAnterior(processoTrf.getOrgaoJulgador());
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			distribuicaoService.setCompetenciaConflito(getCompetenciaRedistribuicao());
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();
			
			this.lancarMovimentoRedistribuicao(processoTrf, TipoDistribuicaoEnum.S, TipoRedistribuicaoEnum.O, this.processoDocumentoVinculado);

			setAtualiza(true);
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}
	
	/**
	 * Criação do motivo de redistribuição Prevenção.
	 */
	public void gravarRedistribuicaoPorPrevencao() {
		this.redistribuicaoTipoPrevencao(TipoRedistribuicaoEnum.P);
	}
	
	/**
	 * Criação de novo motivo de redistribuição (Incompetência).
	 */
	public void gravarRedistribuicaoIncompetencia() {
		try {
			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.R);
			
			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);
			getInstance().setOrgaoJulgadorAnterior(processoTrf.getOrgaoJulgador());

			if (!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			} else {
				if (TipoDistribuicaoEnum.CE.equals(this.getInstance().getInTipoDistribuicao())) {
	 				processoTrf.setJurisdicao(getJurisdicaoRedistribuicao());
	 				processoTrf.setOrgaoJulgador(getOrgaoJulgadorRedistribuicao());
				}
			}

			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			if(getJurisdicaoRedistribuicao() != null) {
				processoTrf.setJurisdicao(getJurisdicaoRedistribuicao());
			}
			if(competenciaRedistribuicao != null){
				distribuicaoService.setCompetenciaConflito(competenciaRedistribuicao);
			}
			
			TipoDistribuicaoEnum tipoRedistribuicao = TipoDistribuicaoEnum.S;
			this.getInstance().setInTipoDistribuicao(tipoRedistribuicao);

			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();
			
			this.lancarMovimentoRedistribuicao(processoTrf, tipoRedistribuicao, TipoRedistribuicaoEnum.R, this.processoDocumentoVinculado);

			setAtualiza(true);
			
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}
	
	/**
	 * Desmembramento do tipo de redistribuição (Suspeição/Impedimento).
	 */
	public void gravarRedistribuicaoSuspeicao() {
		try {
			String causaDaSuspensao = (String) ((this.getInstance().getCausaImpedimento() != null)? " ," + this.getInstance().getCausaSuspeicao():"");

			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.I, causaDaSuspensao);
			
			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);
			if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			}
			getInstance().setOrgaoJulgadorAnterior(processoTrf.getOrgaoJulgador());
			getInstance().setOrgaoJulgador(orgaoJulgadorRedistribuicao);
			getInstance().setOrgaoJulgadorColegiado(orgaoJulgadorColegiadoRedistribuicao);
			
			if(this.getJurisdicaoRedistribuicao() != null) {
				getInstance().setJurisdicao(getJurisdicaoRedistribuicao());
			}
			
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			distribuicaoService.setCompetenciaConflito(processoTrf.getCompetencia());
			if(competenciaRedistribuicao != null) {
				distribuicaoService.setCompetenciaConflito(competenciaRedistribuicao);
			}

			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();
			
			this.lancarMovimentoRedistribuicao(processoTrf, TipoDistribuicaoEnum.S, TipoRedistribuicaoEnum.S, this.processoDocumentoVinculado);
			
			setAtualiza(true);
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}
	
	/**
	 * Método para tratar novo motivo de redistribuição (Afastamento temporário do titular).
	 */
	public void gravarRedistribuicaoAfastamentoTemporarioTitular() {
		try {
			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.T);
			
			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);
			getInstance().setInTipoDistribuicao(TipoDistribuicaoEnum.S);
			if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			}
			getInstance().setOrgaoJulgadorAnterior(processoTrf.getOrgaoJulgador());
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			distribuicaoService.setCompetenciaConflito(getCompetenciaRedistribuicao());
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();
			
			this.lancarMovimentoRedistribuicao(processoTrf, TipoDistribuicaoEnum.S, TipoRedistribuicaoEnum.T, this.processoDocumentoVinculado);
			
			setAtualiza(true);
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}
	
	/**
	 * Criação de novo motivo de redistribuição (Criação de unidade judiciária).
	 */
	public void gravarRedistribuicaoCriacaoUnidadeJudiciaria() {		
		switch (this.getInstance().getInTipoDistribuicao()){
			case S:
				gravarRedistribuicaoSorteioCriacaoUnidadeJudiciaria();
				break;
			default:
				FacesMessages.instance().clear();
				this.mensagemProtocolacao = "Tipo inválido para distribuição por criação de unidade judiciária.";
				break;
			}
	}
	
	/**
	 * Criação de novo motivo de redistribuição (Extinção de unidade judiciária).
	 */
	public void gravarRedistribuicaoExtincaoUnidadeJudiciaria() {		
		switch (this.getInstance().getInTipoDistribuicao()){		
			case S:
				gravarRedistribuicaoSorteioExtincaoUnidadeJudiciaria();
				break;
			default:
				FacesMessages.instance().clear();
				this.mensagemProtocolacao = "Tipo inválido para distribuição por extinção de unidade judiciária.";
				break;
			}
	}
	
	/**
	 * Criação de novo motivo de redistribuição (Recusa de prevenção / dependência).
	 */
	public void gravarRedistribuicaoRecusaPrevencaoDependencia() {		
		switch (this.getInstance().getInTipoDistribuicao()){		
			case S:
				gravarRedistribuicaoSorteioRecusaPrevencaoDependencia();
				break;
			case PP:
				this.redistribuicaoTipoPrevencao(TipoRedistribuicaoEnum.W);
				break;
			case PD:
				this.redistribuicaoTipoDependencia(TipoRedistribuicaoEnum.W);
				break;
			default:
				FacesMessages.instance().clear();
				this.mensagemProtocolacao = "Tipo inválido para distribuição por recusa de prevenção / dependência.";
				break;
			}
	}
	
	
	/**
	 * Criação do tipo de distribuição por sucessão
	 */
	public void gravarRedistribuicaoSucessao(){
		try {
			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.Z);
			
			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);
			getInstance().setInTipoDistribuicao(TipoDistribuicaoEnum.Z);
			getInstance().setJurisdicao(processoTrf.getJurisdicao());
			if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiado(processoTrf.getOrgaoJulgadorColegiado());
			}
			getInstance().setOrgaoJulgador(processoTrf.getOrgaoJulgador());
			
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			if(competenciaRedistribuicao != null) {
				distribuicaoService.setCompetenciaConflito(competenciaRedistribuicao);
			}else {
				distribuicaoService.setCompetenciaConflito(processoTrf.getCompetencia());
			}
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();
			
			this.lancarMovimentoRedistribuicao(processoTrf, TipoDistribuicaoEnum.Z, TipoRedistribuicaoEnum.Z, this.processoDocumentoVinculado);
			
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			setPodeFecharJanela(Boolean.TRUE);
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			setPodeFecharJanela(Boolean.FALSE);
		}
	}
	
	/**
	 * Criação do tipo de distribuição para a redistribuição por determinação judicial (Dependência).
	 * @throws PJeBusinessException 
	 */
	private void gravarRedistribuicaoDependenciaDeterminacaoJudicial() {
		this.redistribuicaoTipoDependencia(TipoRedistribuicaoEnum.J);
	}
	
	/**
	 * Criação do tipo de distribuição para a redistribuição por determinação judicial (Prevenção).
	 */
	private void gravarRedistribuicaoPrevencaoDeterminacaoJudicial() {
		ProcessoTrf processoPrevento = obterProcessoTrf();
		try {
			if (isProcessoVinculadoCadeia260(processoPrevento)) {
				tratarMensagemParaCadeia260Existente(processoPrevento.getProcesso().getNumeroProcesso());
			}else {
				this.redistribuicaoTipoPrevencao(TipoRedistribuicaoEnum.J);
			}
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}		
	}

	/**
	 * Metodo que adiciona mensagem para informar que existe cadeia preventa pelo artigo 260 para o processo a ser 
	 * redistribuido via decisao judicial por prevencao.
	 * @param processoTrf
	 */
	private void tratarMensagemParaCadeia260Existente(String numeroProcesso) {
		FacesMessages.instance().addFromResourceBundle(Severity.INFO, "erro.redistribuicao.decisaojudicialporPrevencao.cadeia.260.existente", numeroProcesso);
		podeFecharJanela = false;
	}

	/**
	 * Método que verifica se o processo é eleitoral e possui um processo paradigma relacionado
	 * e retorna o OJ do proceesso paradigma
	 * @param processoTrf
	 * @throws PJeBusinessException Excecao lancada se o processo nao tiver informacao referente ao complemento
	 */	
	private OrgaoJulgador recuperaOrgaoJulgadorProcessoEleitoralParadigmaNaRegiao(ProcessoTrf processo) {
		if(processo.getComplementoJE() != null) {
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			VinculacaoDependenciaEleitoral cadeia260 = null;
			cadeia260 = distribuicaoService.recuperarCadeiaParaEleicaoNaRegiao(processo);
			
			if (cadeia260 != null) {
				return cadeia260.getCargoJudicial().getOrgaoJulgador();
			}			
		}
		return null;
	}

	/**
	 * Com base no processoTrf, o metodo verifica se este ja se encontra vinculado a uma cadeia260.
	 * @param processoTrf
	 * @return	verdadeiro se o processo não estiver em alguma cadeia260.
	 * @throws PJeBusinessException
	 */
	private boolean isProcessoVinculadoCadeia260(ProcessoTrf processoTrf) throws PJeBusinessException {
		VinculacaoDependenciaEleitoral cadeia260 = vinculacaoDependenciaEleitoralManager.recuperaVinculacaoDependencia(processoTrf);
		return cadeia260 != null;
	}

	/**
	 * Verifica se o processo eh da justica eleitoral, se o parametro "listaAgrupamentosPrevencao260JE" esta ativo e se
	 * o processo esta enquadrado na prevencao eleitoral.
	 * @param distribuicaoService
	 * @param processoTrf
	 * @return	verdadeiro caso a justica seja eleitoral, caso o parametro "listaAgrupamentosPrevencao260JE" esteja
	 * 			definido e ativo e se o processo estiver enquadrado na prevencao eleitoral.
	 * @throws PJeBusinessException
	 */
	private boolean isProcessoEleitoralEnquadradoComParametroPrevencaoAtivo(ProcessoTrf processoTrf) throws PJeBusinessException {
		DistribuicaoService distribuicaoService = ComponentUtil.getComponent(DistribuicaoService.NAME);
		return distribuicaoService.isProcessoEleitoralEnquadradoComParametroPrevencaoAtivo(processoTrf);
	}
	
	/**
	 * Criação do tipo de distribuição para a redistribuição por determinação judicial (Sorteio).
	 */
	private void gravarRedistribuicaoSorteioDeterminacaoJudicial() {
		try {
			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.J);
			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);

			if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			}
			getInstance().setOrgaoJulgadorAnterior(processoTrf.getOrgaoJulgador());
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			distribuicaoService.setCompetenciaConflito(getCompetenciaRedistribuicao());
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();
			
			this.lancarMovimentoRedistribuicao(processoTrf, TipoDistribuicaoEnum.S, TipoRedistribuicaoEnum.J, this.processoDocumentoVinculado);

			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}
	
	/**
	 * Criação do tipo de distribuição para a redistribuição por erro material (Dependência).
	 */
	private void gravarRedistribuicaoDependenciaErroMaterial() {
		this.redistribuicaoTipoDependencia(TipoRedistribuicaoEnum.M);
	}
	
	/**
	 * Na redistribuição do tipo prevenção o sistema deverá:
	 * - encaminhar o processo prevento para o mesmo magistrado do processo paradigma
	 * - remover a marcação de processo incidente se houver
	 * - gerar uma conexão com o processo paradigma
	 * - o processo paradigma não é o processo originário (ou seja, não é o processo referência)
	 * 
	 * @param motivoRedistribuicao
	 */
	private void redistribuicaoTipoPrevencao(TipoRedistribuicaoEnum motivoRedistribuicao) {
		PesquisaProcessoParadigmaAction pesquisaProcessoParadigma = ComponentUtil.getComponent(PesquisaProcessoParadigmaAction.class);
		try {
			if (!pesquisaProcessoParadigma.getProcessoValidado()) {
				this.mensagemProtocolacao = obterMensagemErro(FacesUtil.getMessage("distribuicaoService.erroRedistribuicaoDependenciaSemProcessoParadigma"));
				FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
				podeFecharJanela = false;
				
				return;
			}
			this.identificaMotivoRedistribuicao(motivoRedistribuicao);
			
			getInstance().setJurisdicao(pesquisaProcessoParadigma.getJurisdicaoParadigma());
			getInstance().setOrgaoJulgadorColegiado(pesquisaProcessoParadigma.getOrgaoJulgadorColegiadoParadigma());
			getInstance().setOrgaoJulgador(pesquisaProcessoParadigma.getOrgaoJulgadorParadigma());
			getInstance().setOrgaoJulgadorCargo(pesquisaProcessoParadigma.getOrgaoJulgadorCargoParadigma());
			getInstance().setInTipoDistribuicao(TipoDistribuicaoEnum.PP);
			
			ProcessoTrf processoPrevento = obterProcessoTrf();
			OrgaoJulgador ojCadeia260 = this.recuperaOrgaoJulgadorProcessoEleitoralParadigmaNaRegiao(processoPrevento);
			if (ojCadeia260 != null) {
				processoPrevento.setOrgaoJulgador(ojCadeia260);
			}
			
			this.criarConexaoProcessoPrevento(processoPrevento);
			processoPrevento.setIsIncidente(false);

			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			distribuicaoService.setCompetenciaConflito(pesquisaProcessoParadigma.getCompetenciaParadigma());
			distribuicaoService.redistribuirProcesso(processoPrevento, getInstance());
			
			ProcessoHome.instance().update();
			gravarRedistribuicao();
			this.lancarMovimentoRedistribuicao(processoPrevento, TipoDistribuicaoEnum.PP, motivoRedistribuicao, this.processoDocumentoVinculado);
			
			setAtualiza(true);

			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoPrevento);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}		
	}
	
	/**
	 * Na redistribuição do tipo dependência o sistema deverá:
	 * - o processo dependente é um processo incidental
	 * - o processo paradigma será o processo de referência
	 * - não será criada uma conexão com o processo paradigma
	 * - o processo dependente será encaminhado para o mesmo magistardo do processo paradigma
	 * - processos recursais não podem ser redistribuídos por dependência, caso isso ocorra o processo perderá a referência ao processo do 1o grau
	 * 
	 * @param motivoRedistribuicao
	 * @param codigoMotivoRedistribuicao
	 */
	private void redistribuicaoTipoDependencia(TipoRedistribuicaoEnum motivoRedistribuicao) {
		PesquisaProcessoParadigmaAction pesquisaProcessoParadigma = ComponentUtil.getComponent(PesquisaProcessoParadigmaAction.class);
		try {
			if (!pesquisaProcessoParadigma.getProcessoValidado()) {
				this.mensagemProtocolacao = obterMensagemErro(FacesUtil.getMessage("distribuicaoService.erroRedistribuicaoDependenciaSemProcessoParadigma"));
				FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
				podeFecharJanela = false;
				
				return;
			}
			this.identificaMotivoRedistribuicao(motivoRedistribuicao);
			
			ProcessoTrf processoPrevento = obterProcessoTrf();
			if (!this.permiteTipoRedistribuicaoDependencia()) {
				this.mensagemProtocolacao = obterMensagemErro(FacesUtil.getMessage("distribuicaoService.erroTentativaRedistribuicaoProcessoRecursaoPorDependencia"));
				FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
				podeFecharJanela = false;
				
				return;
			}
			
			getInstance().setJurisdicao(pesquisaProcessoParadigma.getJurisdicaoParadigma());
			getInstance().setOrgaoJulgadorColegiado(pesquisaProcessoParadigma.getOrgaoJulgadorColegiadoParadigma());
			getInstance().setOrgaoJulgador(pesquisaProcessoParadigma.getOrgaoJulgadorParadigma());
			getInstance().setInTipoDistribuicao(TipoDistribuicaoEnum.PD);
			
			processoPrevento.setIsIncidente(true);
			processoPrevento.setProcessoDependencia(pesquisaProcessoParadigma.getProcessoTrfParadigma());
			processoPrevento.setDesProcReferencia(pesquisaProcessoParadigma.getProcessoParadigma());
			
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			distribuicaoService.setCompetenciaConflito(pesquisaProcessoParadigma.getCompetenciaParadigma());
			distribuicaoService.redistribuirProcesso(processoPrevento, getInstance());
			
			ProcessoHome.instance().update();
			gravarRedistribuicao();

			this.lancarMovimentoRedistribuicao(processoPrevento, TipoDistribuicaoEnum.PD, motivoRedistribuicao, this.processoDocumentoVinculado);
			
			setAtualiza(true);
			
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoPrevento);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
			
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}
	
	private void identificaMotivoRedistribuicao(TipoRedistribuicaoEnum motivoRedistribuicao) {
		this.identificaMotivoRedistribuicao(motivoRedistribuicao, null);
	}
	
	private void identificaMotivoRedistribuicao(TipoRedistribuicaoEnum motivoRedistribuicao, String complementoMotivo) {
		if (StringUtils.isBlank(complementoMotivo)) {
			complementoMotivo = StringUtils.EMPTY;
		}
		getInstance().setInTipoRedistribuicao(motivoRedistribuicao);
		getInstance().setMotivoRedistribuicao(String.format("Por %s %s", motivoRedistribuicao.getLabel(), complementoMotivo));
	}

	private void lancarMovimentoRedistribuicao(ProcessoTrf processo, TipoDistribuicaoEnum tipoDistribuicao, 
			TipoRedistribuicaoEnum motivoRedistribuicao, ProcessoDocumento documentoVinculado) throws PJeBusinessException {
		
		MovimentoBuilder movimentoRedistribuicao = MovimentoAutomaticoService.preencherMovimento()
				.deCodigo(CodigoMovimentoNacional.COD_MOVIMENTO_REDISTRIBUICAO);
		
		ComplementoBuilder complementoTipoRedistribuicao = movimentoRedistribuicao
				.new ComplementoBuilder(null, "", CodigoMovimentoNacional.NOME_COMPLEMENTO_TIPO_DE_DISTRIBUICAO_REDISTRIBUICAO);
		
		ComplementoBuilder complementoMotivoRedistribuicao = movimentoRedistribuicao
				.new ComplementoBuilder(null, "", CodigoMovimentoNacional.NOME_COMPLEMENTO_MOTIVO_DA_REDISTRIBUICAO);
		
		/*
		 * Processos da Justiça eleitoral que se enquadram no art. 260 CE, que forem redistribuídos pelo motivo 
		 * de terminação judicial e foram via prevenção ou encaminhamento têm complmento diferenciado
		 * 
		 * Não há os tipos de complemento de prevenção pelo art. 260 no SGT
		 */
		if (TipoRedistribuicaoEnum.J.equals(motivoRedistribuicao) && 
				(TipoDistribuicaoEnum.EN.equals(tipoDistribuicao) || TipoDistribuicaoEnum.PP.equals(tipoDistribuicao)) && 
						this.isProcessoEleitoralEnquadradoComParametroPrevencaoAtivo(processo)) {
			
			complementoTipoRedistribuicao.doTipoDominio().preencherComElementoDeCodigo(this.getCodigoTipoPrevencaoEleitoralArt260(processo));
		} else {
			// Não há o tipo de redistribuição "encaminhamento" no SGT
			if (TipoDistribuicaoEnum.EN.equals(tipoDistribuicao)) {
				complementoTipoRedistribuicao.preencherComTexto("encaminhamento");
			} else {
				String codTipoRedistribuicao;
				
				switch (tipoDistribuicao) {
					case CE:
						codTipoRedistribuicao = COD_COMPLEMENTO_TIPO_DISTRIBUICAO_REDISTRIBUICAO.COMPETENCIA_EXCLUSIVA;
						break;
					case I:
					case PD:
						codTipoRedistribuicao = COD_COMPLEMENTO_TIPO_DISTRIBUICAO_REDISTRIBUICAO.DEPENDENCIA;
						break;
					case PP:
						codTipoRedistribuicao = COD_COMPLEMENTO_TIPO_DISTRIBUICAO_REDISTRIBUICAO.PREVENCAO;
						break;
					case Z:
						codTipoRedistribuicao = COD_COMPLEMENTO_TIPO_DISTRIBUICAO_REDISTRIBUICAO.COMPETENCIA_EXCLUSIVA;
						break;
					case S:
					default:
						codTipoRedistribuicao = COD_COMPLEMENTO_TIPO_DISTRIBUICAO_REDISTRIBUICAO.SORTEIO;
						break;
				}
				
				complementoTipoRedistribuicao.doTipoDominio().preencherComElementoDeCodigo(codTipoRedistribuicao);
			}
		}
		
		// Não há o motivo de redistribuição "determinação judicial" no SGT
		if (TipoRedistribuicaoEnum.J.equals(motivoRedistribuicao)) {
			complementoMotivoRedistribuicao.preencherComTexto("Determinação judicial");
		} else {
			String codMotivoRedistribuicao;
			switch (motivoRedistribuicao) {
				case M:
					codMotivoRedistribuicao = COD_COMPLEMENTO_MOTIVO_REDISTRIBUICAO.ERRO_MATERIAL;
					break;
				case C:
					codMotivoRedistribuicao = COD_COMPLEMENTO_MOTIVO_REDISTRIBUICAO.ALTERACAO_COMPETENCIA_ORGAO;
					break;
				case P:
					codMotivoRedistribuicao = COD_COMPLEMENTO_MOTIVO_REDISTRIBUICAO.MODIFICACAO_DA_COMPETENCIA;
					break;
				case W:
					codMotivoRedistribuicao = COD_COMPLEMENTO_MOTIVO_REDISTRIBUICAO.RECUSA_PREVENCAO_DEPENDENCIA;
					break;
				
				case D:
					codMotivoRedistribuicao = COD_COMPLEMENTO_MOTIVO_REDISTRIBUICAO.DESAFORAMENTO;
					break;
				case E:
					codMotivoRedistribuicao = COD_COMPLEMENTO_MOTIVO_REDISTRIBUICAO.REUNIAO_EXECUCOES;
					break;
				case R:
					codMotivoRedistribuicao = COD_COMPLEMENTO_MOTIVO_REDISTRIBUICAO.INCOMPETENCIA;
					break;
				case I: /* 1G */
				case N: /* 2G magistrado */
				case Y: /* 2G colegiado */
					codMotivoRedistribuicao = COD_COMPLEMENTO_MOTIVO_REDISTRIBUICAO.IMPEDIMENTO;
					break;
				case S: /* 1G */
				case O: /* 2G */
					codMotivoRedistribuicao = COD_COMPLEMENTO_MOTIVO_REDISTRIBUICAO.SUSPEICAO;
					break;
				case U:
					codMotivoRedistribuicao = COD_COMPLEMENTO_MOTIVO_REDISTRIBUICAO.CRIACAO_UNIDADE_JUDICIARIA;
					break;
				case X:
					codMotivoRedistribuicao = COD_COMPLEMENTO_MOTIVO_REDISTRIBUICAO.EXTINCAO_UNIDADE_JUDICIARIA;
					break;
					
				case A:
				case K:
				case T:
				case Z:
					codMotivoRedistribuicao = COD_COMPLEMENTO_MOTIVO_REDISTRIBUICAO.SUCESSAO;
					break;
				
				default:
					codMotivoRedistribuicao = COD_COMPLEMENTO_MOTIVO_REDISTRIBUICAO.ERRO_MATERIAL;
					break;
			}
			complementoMotivoRedistribuicao.doTipoDominio().preencherComElementoDeCodigo(codMotivoRedistribuicao);
		}
		
		Integer idDocumentoVinculado = null;
		if (documentoVinculado != null) {
			idDocumentoVinculado = documentoVinculado.getIdProcessoDocumento();
		}
		
		movimentoRedistribuicao.comComplementoBuilder(complementoTipoRedistribuicao)
			.comComplementoBuilder(complementoMotivoRedistribuicao)
			.associarAoDocumentoDeId(idDocumentoVinculado)
			.lancarMovimento();
	}
	
	/**
	 * Criação do tipo de distribuição para a redistribuição por erro material (Prevenção).
	 */
	private void gravarRedistribuicaoPrevencaoErroMaterial() {
		this.redistribuicaoTipoPrevencao(TipoRedistribuicaoEnum.M);
	}
	
	/**
	 * Criação do tipo de distribuição para a redistribuição por erro material (Sorteio).
	 */
	private void gravarRedistribuicaoSorteioErroMaterial() {
		try {
			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.M);
			
			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);
			if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			}
			getInstance().setOrgaoJulgadorAnterior(processoTrf.getOrgaoJulgador());
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();

			distribuicaoService.setCompetenciaConflito(processoTrf.getCompetencia());
			
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();
			
			this.lancarMovimentoRedistribuicao(processoTrf, TipoDistribuicaoEnum.S, TipoRedistribuicaoEnum.M, this.processoDocumentoVinculado);
				
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}
	
	/**
	 * Criação do tipo de distribuição para a redistribuição por criação de unidade judiciária (Sorteio).
	 */
	private void gravarRedistribuicaoSorteioCriacaoUnidadeJudiciaria() {
		try {
			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.U);
			
			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);
			if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			}
			getInstance().setOrgaoJulgadorAnterior(processoTrf.getOrgaoJulgador());
			if(this.getJurisdicaoRedistribuicao() != null) {
				getInstance().setJurisdicao(this.getJurisdicaoRedistribuicao());
			}
			
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			distribuicaoService.setCompetenciaConflito(processoTrf.getCompetencia());
			if(competenciaRedistribuicao != null) {
				distribuicaoService.setCompetenciaConflito(competenciaRedistribuicao);
			}
			
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();
			
			this.lancarMovimentoRedistribuicao(processoTrf, TipoDistribuicaoEnum.S, TipoRedistribuicaoEnum.U, this.processoDocumentoVinculado);
			
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}
	
	/**
	 * Criação do tipo de distribuição para a redistribuição por extinção de unidade judiciária (Sorteio).
	 */
	private void gravarRedistribuicaoSorteioExtincaoUnidadeJudiciaria() {
		try {
			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.X);
			
			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);
			if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			}
			getInstance().setOrgaoJulgadorAnterior(processoTrf.getOrgaoJulgador());
			if(this.getJurisdicaoRedistribuicao() != null) {
				getInstance().setJurisdicao(getJurisdicaoRedistribuicao());
			}
			
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			distribuicaoService.setCompetenciaConflito(processoTrf.getCompetencia());
			if(competenciaRedistribuicao != null) {
				distribuicaoService.setCompetenciaConflito(competenciaRedistribuicao);
			}
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();
			
			this.lancarMovimentoRedistribuicao(processoTrf, TipoDistribuicaoEnum.S, TipoRedistribuicaoEnum.X, this.processoDocumentoVinculado);
				
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}
		
	/**
	 * Criação do tipo de distribuição para a redistribuição por recusa de prevenção / dependência (Sorteio).
	 */
	private void gravarRedistribuicaoSorteioRecusaPrevencaoDependencia() {
		try {
			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.W);

			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);
	        if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			}
			getInstance().setOrgaoJulgadorAnterior(processoTrf.getOrgaoJulgador());
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			Competencia competencia = processoTrf.getCompetencia();
			if(competencia != null){
				distribuicaoService.setCompetenciaConflito(competencia);
			}
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();
			
			this.lancarMovimentoRedistribuicao(processoTrf, TipoDistribuicaoEnum.S, TipoRedistribuicaoEnum.W, this.processoDocumentoVinculado);

			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} catch (Exception e) {
			rollback();
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}
	}
	
	public List<TipoRedistribuicaoEnum> getTipoRedistribuicaoItems() {
		List<TipoRedistribuicaoEnum> tiposRedistribuicaoEnumList = new ArrayList<TipoRedistribuicaoEnum>();

		if(ParametroJtUtil.instance().justicaEleitoral() && !ParametroUtil.instance().isPrimeiroGrau()){
			tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.C);
			tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.J);
			tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.X);
			tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.N);
			tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.Z);
			tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.O);
			tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.R);
		}else {
			// motivos comuns ao 1G e 2G (e superiores)
			tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.M);
			tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.C);
			tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.P);
			tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.W);
			tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.R);

			// motivos exclusivos do 1G
			if (ParametroUtil.instance().isPrimeiroGrau()) {
//				tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.D); /* desaforamento só faz sentido para processos criminais no 1G com tribunal de júri - ainda não há esse julgamento no PJe */
				tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.E);
				tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.I);
				tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.S);
				tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.U);
				tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.X);
			} else { // motivos exclusivos do 2G (e superiores)
				tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.A);
				tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.K);
				tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.J);
				tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.N);
				tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.O);
				tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.T);
				
				if(this.verificaPossibilidadeMotivoImpedimentoOJC()) {
					tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.Y);
				}
				tiposRedistribuicaoEnumList.add(TipoRedistribuicaoEnum.Z);
			}
		}

		// ordenação alfabética dos motivos de redistribuição
		Collections.sort(tiposRedistribuicaoEnumList, new Comparator<TipoRedistribuicaoEnum>(){
			@Override
			public int compare(TipoRedistribuicaoEnum tre1, TipoRedistribuicaoEnum tre2) {
				if (tre1.getLabel().compareTo(tre2.getLabel()) < 0) {
					return -1;
				}
				if (tre1.getLabel().compareTo(tre2.getLabel()) > 0) {
					return 1;
				}
				return 0;
			}
		});
		return tiposRedistribuicaoEnumList;
	}
	
	private boolean verificaPossibilidadeMotivoImpedimentoOJC() {
		ProcessoTrf processo = this.obterProcessoTrf();
		List<OrgaoJulgadorColegiado> colegiados = this.orgaoJulgadorColegiadoManager.obterAtivos(processo.getJurisdicao(), processo.getCompetencia());
		
		return (colegiados != null && colegiados.size() > 1);
	}

	public void setInTipoRedistribuicao(TipoRedistribuicaoEnum inTipoRedistribuicao) {
		this.inTipoRedistribuicao = inTipoRedistribuicao;
	}

	public TipoRedistribuicaoEnum getInTipoRedistribuicao() {
		return inTipoRedistribuicao;
	}

	@Override
	public void onClickSearchTab() {
		setInTipoRedistribuicao(null);
		resetaAbas();
		super.onClickSearchTab();
	}

	public void resetaAbasECarregaCompetencias(){
		resetaAbas();
		ProcessoTrfHome p = ProcessoTrfHome.instance();
		p.clearInstance();
		p.setarInstancia();
		p.carregaCompetenciasRedistribuicao();
	}

	public void resetaAbas(){
		ParametroUtil param = ParametroUtil.instance();
		if(!param.isPrimeiroGrau()){
			setOrgaoJulgadorColegiadoRedistribuicao(null);
		}
		setOrgaoJulgadorRedistribuicao(null);
		setJurisdicaoRedistribuicao(null);
		setCompetenciaRedistribuicao(null);
		getInstance().setCausaImpedimento(null);
		getInstance().setCausaSuspeicao(null);
		getInstance().setInTipoDistribuicao(null);
		this.setProcessoRedistribuicao(null);
		this.setProcessoTrfRedistribuicao(null);
		this.casoCompetencia = null;
	}
	
	public void atualizarAbaErroMaterial() {
		if(TipoRedistribuicaoEnum.M.equals(getInTipoRedistribuicao())) {
			resetaAbas();
		}
	}
	
	public void atualizarAbaCriacaoUnidadeJudiciaria() {
		if(TipoRedistribuicaoEnum.U.equals(getInTipoRedistribuicao())) {
			resetaAbas();
		}
	}
	
	public void atualizarAbaExtincaoUnidadeJudiciaria() {
		if(TipoRedistribuicaoEnum.X.equals(getInTipoRedistribuicao())) {
			resetaAbas();
		}
	}
	
	public void atualizarAbaRecusaPrevencaoDependencia() {
		if(TipoRedistribuicaoEnum.W.equals(getInTipoRedistribuicao())) {
			resetaAbas();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Competencia> getCompetenciaList() {
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct o from Competencia o ")
				.append("inner join o.orgaoJulgadorCompetenciaList ojc ")
				.append("where o.ativo = true and ojc.dataInicio <= :dataAtual AND (ojc.dataFim is null OR ojc.dataFim >= :dataAtual)");
		Query query = em.createQuery(sql.toString()).setParameter("dataAtual", new Date());
		List<Competencia> lista = query.getResultList();
		for (OrgaoJulgadorCompetencia c : obterProcessoTrf().getOrgaoJulgador()
				.getOrgaoJulgadorCompetenciaList()) {
			if (lista.contains(c.getCompetencia())) {
				lista.remove(c.getCompetencia());
			}
		}
		return lista;
	}

	public void setCompetencias(List<Competencia> competencias) {
		this.competencias = competencias;
	}

	public List<Competencia> getCompetencias() {
		return competencias;
	}

	public void setAtualiza(boolean atualiza) {
		this.atualiza = atualiza;
	}

	public boolean isAtualiza() {
		return atualiza;
	}

	public void setPodeFecharJanela(boolean podeFecharJanela) {
		this.podeFecharJanela = podeFecharJanela;
	}

	public boolean getPodeFecharJanela() {
		return podeFecharJanela;
	}
	
	public Jurisdicao getJurisdicaoSorteio(){
		return jurisdicaoSorteio;
	}
	
	public void setJurisdicaoSorteio(Jurisdicao jurisdicaoSorteio){
		this.jurisdicaoSorteio = jurisdicaoSorteio;
	}
	
	public Jurisdicao getJurisdicao(){
		return jurisdicao;
	}
	
	public void setJurisdicao(Jurisdicao jurisdicao){
		this.jurisdicao = jurisdicao;
	}
	
	public ProcessoTrf getProcessoTrfRedistribuicao() {
		return processoTrfRedistribuicao;
	}

	public void setProcessoTrfRedistribuicao(ProcessoTrf processoTrfRedistribuicao) {
		this.processoTrfRedistribuicao = processoTrfRedistribuicao;
	}

	public String getProcessoRedistribuicao() {
		return processoRedistribuicao;
	}

	public void setProcessoRedistribuicao(String processoRedistribuicao) {
		this.processoRedistribuicao = processoRedistribuicao;
	}

	public Jurisdicao getJurisdicaoRedistribuicao(){
		return jurisdicaoRedistribuicao;
	}
	
	public void setJurisdicaoRedistribuicao(Jurisdicao jurisdicaoRedistribuicao){
		this.jurisdicaoRedistribuicao = jurisdicaoRedistribuicao;
	}
	
	public OrgaoJulgador getOrgaoJulgadorRedistribuicao(){
		return orgaoJulgadorRedistribuicao;
	}
	
	public void setOrgaoJulgadorRedistribuicao(OrgaoJulgador orgaoJulgadorRedistribuicao){
		this.orgaoJulgadorRedistribuicao = orgaoJulgadorRedistribuicao;
	}
	
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiadoRedistribuicao(){
		return orgaoJulgadorColegiadoRedistribuicao;
	}
	
	public void setOrgaoJulgadorColegiadoRedistribuicao(OrgaoJulgadorColegiado orgaoJulgadorColegiadoRedistribuicao){
		this.orgaoJulgadorColegiadoRedistribuicao = orgaoJulgadorColegiadoRedistribuicao;
		setOrgaoJulgadorRedistribuicao(null);
	}
	
	public Competencia getCompetenciaRedistribuicao(){
		return competenciaRedistribuicao;
	}
	
	public void setCompetenciaRedistribuicao(Competencia competenciaRedistribuicao){
		this.competenciaRedistribuicao = competenciaRedistribuicao;
	}
	
	private Boolean permiteTipoRedistribuicaoDependencia() {
		return !ClasseJudicialInicialEnum.R.equals(obterProcessoTrf().getInicial());
	}

	public List<TipoDistribuicaoEnum> getTipoDistribuicaoDeterminacaoJudicial(){
		List<TipoDistribuicaoEnum> listaTiposDistribuicao = new ArrayList<TipoDistribuicaoEnum>();

		listaTiposDistribuicao.add(TipoDistribuicaoEnum.S); // Por sorteio

        if(!ParametroUtil.instance().isPrimeiroGrau()) {
        	listaTiposDistribuicao.add(TipoDistribuicaoEnum.EN);//Por encaminhamento
        }

		return listaTiposDistribuicao;	
	}
	
	public List<TipoDistribuicaoEnum> getTipoDistribuicaoErroMaterial(){
		List<TipoDistribuicaoEnum> listaTiposDistribuicao = new ArrayList<TipoDistribuicaoEnum>();

		listaTiposDistribuicao.add(TipoDistribuicaoEnum.S);

		if(this.permiteTipoRedistribuicaoDependencia()) {
			listaTiposDistribuicao.add(TipoDistribuicaoEnum.PD);
		}
               
      return listaTiposDistribuicao;
	}
	
	public List<TipoDistribuicaoEnum> getTipoDistribuicaoCriacaoUnidadeJudiciaria(){
		List<TipoDistribuicaoEnum> listaTiposDistribuicao = new ArrayList<TipoDistribuicaoEnum>();

		listaTiposDistribuicao.add(TipoDistribuicaoEnum.S);
               
		return listaTiposDistribuicao;
	}
	
	public List<TipoDistribuicaoEnum> getTipoDistribuicaoExtincaoUnidadeJudiciaria(){
		List<TipoDistribuicaoEnum> listaTiposDistribuicao = new ArrayList<TipoDistribuicaoEnum>();

		listaTiposDistribuicao.add(TipoDistribuicaoEnum.S);
               
		return listaTiposDistribuicao;
	}

	public List<TipoDistribuicaoEnum> getTipoDistribuicaoImpedimentoSuspeicao1G(){
		List<TipoDistribuicaoEnum> listaTiposDistribuicao = new ArrayList<TipoDistribuicaoEnum>();

		listaTiposDistribuicao.add(TipoDistribuicaoEnum.S);
               
		return listaTiposDistribuicao;
	}

	public List<TipoDistribuicaoEnum> getTipoDistribuicaoRecusaPrevencaoDependencia(){
		List<TipoDistribuicaoEnum> listaTiposDistribuicao = new ArrayList<TipoDistribuicaoEnum>();

		listaTiposDistribuicao.add(TipoDistribuicaoEnum.S);
		if(this.permiteTipoRedistribuicaoDependencia()) {
			listaTiposDistribuicao.add(TipoDistribuicaoEnum.PD);
		}
		listaTiposDistribuicao.add(TipoDistribuicaoEnum.PP);
               
		return listaTiposDistribuicao;
	}
	
	public List<TipoDistribuicaoEnum> getTipoDistribuicaoAlteracaoCompetenciaOrgaoJE(){
		List<TipoDistribuicaoEnum> listaTiposDistribuicao = new ArrayList<TipoDistribuicaoEnum>();
		listaTiposDistribuicao.add(TipoDistribuicaoEnum.S);
               
		return listaTiposDistribuicao;
    }
	
	public List<TipoDistribuicaoEnum> getTipoDistribuicaoExtincaoUnidadeJudiciariaJE(){
		List<TipoDistribuicaoEnum> listaTiposDistribuicao = new ArrayList<TipoDistribuicaoEnum>();
		listaTiposDistribuicao.add(TipoDistribuicaoEnum.S);
               
		return listaTiposDistribuicao;
    }
	
	public List<TipoDistribuicaoEnum> getTipoDistribuicaoImpedimentoJE(){
		List<TipoDistribuicaoEnum> listaTiposDistribuicao = new ArrayList<TipoDistribuicaoEnum>();
		listaTiposDistribuicao.add(TipoDistribuicaoEnum.S);
               
		return listaTiposDistribuicao;
    }
	
	public List<TipoDistribuicaoEnum> getTipoDistribuicaoSuspeicaoJE(){
		List<TipoDistribuicaoEnum> listaTiposDistribuicao = new ArrayList<TipoDistribuicaoEnum>();
		listaTiposDistribuicao.add(TipoDistribuicaoEnum.S);
               
		return listaTiposDistribuicao;
    }
	
	/**
	 * Registra a conexão do processo passado com o processo paradigma encontrado em this.pesquisaProcessoParadigma
	 * O tipo de conexão registrado será a dada pelo parâmetro TipoConexaoEnum {TipoConexaoEnum.DP / TipoConexaoEnum.PR}
	 * A conexão será criada nos dois lados do processo A para o B e do B para o A
	 * @param processo
	 * @param tipoConexao
	 */
	private void criarConexaoProcessoPrevento(ProcessoTrf processo) {
		PesquisaProcessoParadigmaAction pesquisaProcessoParadigma = ComponentUtil.getComponent(PesquisaProcessoParadigmaAction.class);
		if (processo != null && pesquisaProcessoParadigma.getProcessoValidado()) {
			ProcessoTrf processoA = processo;
			ProcessoTrf processoB = pesquisaProcessoParadigma.getProcessoTrfParadigma();
			String numeroProcessoB = pesquisaProcessoParadigma.getProcessoParadigma();
			Date dataAtual = new Date();
			
			/* registrando conexão de A para B */
			PrevencaoService prevencaoService = PrevencaoService.instance();
			if (!prevencaoService.verificaExistenciaPossivelPrevencao(processoA, processoB, numeroProcessoB)) {
				ProcessoTrfConexao processoTrfConexao1 = new ProcessoTrfConexao();
				processoTrfConexao1.setTipoConexao(TipoConexaoEnum.PR);
				processoTrfConexao1.setPrevencao(PrevencaoEnum.PR);
				processoTrfConexao1.setPessoaFisica(Authenticator.getPessoaLogada());
				processoTrfConexao1.setDtPossivelPrevencao(dataAtual);
				processoTrfConexao1.setDtValidaPrevencao(dataAtual);
				processoTrfConexao1.setDataRegistro(dataAtual);
				processoTrfConexao1.setJustificativa("Prevenção identificada durante redistribuição.");
				processoTrfConexao1.setAtivo(Boolean.TRUE);
	
				processoTrfConexao1.setProcessoTrf(processoA);
				processoTrfConexao1.setProcessoTrfConexo(pesquisaProcessoParadigma.getOrigemSistema().equals(Constantes.ORIGEM_SISTEMA_PJE) ? pesquisaProcessoParadigma.getProcessoTrfParadigma() : null);
				processoTrfConexao1.setNumeroProcesso(numeroProcessoB);
				processoTrfConexao1.setOrgaoJulgador(pesquisaProcessoParadigma.getOrgaoJulgadorParadigma().getOrgaoJulgador());
				processoTrfConexao1.setSessaoJudiciaria(pesquisaProcessoParadigma.getJurisdicaoParadigma().getJurisdicao());
				EntityUtil.getEntityManager().persist(processoTrfConexao1);
			}

			/* registrando conexão de B para A */
			if (pesquisaProcessoParadigma.getOrigemSistema().equals(Constantes.ORIGEM_SISTEMA_PJE) && processoB != null && !prevencaoService.verificaExistenciaPossivelPrevencao(processoB, processoA)) {
				ProcessoTrfConexao processoTrfConexao2 = new ProcessoTrfConexao();
				processoTrfConexao2.setTipoConexao(TipoConexaoEnum.PR);
				processoTrfConexao2.setPrevencao(PrevencaoEnum.PR);
				processoTrfConexao2.setPessoaFisica(Authenticator.getPessoaLogada());
				processoTrfConexao2.setDtPossivelPrevencao(dataAtual);
				processoTrfConexao2.setDtValidaPrevencao(dataAtual);
				processoTrfConexao2.setDataRegistro(dataAtual);
				processoTrfConexao2.setJustificativa("Prevenção identificada durante redistribuição - processo paradigma.");
				processoTrfConexao2.setAtivo(Boolean.TRUE);

				processoTrfConexao2.setProcessoTrf(processoB);
				processoTrfConexao2.setProcessoTrfConexo(processoA);
				EntityUtil.getEntityManager().persist(processoTrfConexao2);
			}
		}
	}
					
	public SelectItemsQuery orgaoJulgadorRedistribuicaoItems(){
		if((ParametroUtil.instance().isPrimeiroGrau()) &&
				(getJurisdicaoRedistribuicao() != null)) {
			if(getOrgaoJulgadorRedistribuicao() != null) {
				if(getOrgaoJulgadorRedistribuicao().getAtivo()) {
					return getComponent("orgaoJulgadorJurisdicaoProcessoRedistribuicaoPrevencaoItems");
				} else {
					return getComponent("orgaoJulgadorJurisdicaoProcessoRedistribuicaoPrevencaoTodosItems");
				}
			}
			else {
				return getComponent("orgaoJulgadorJurisdicaoProcessoRedistribuicaoPrevencaoTodosItems");
			}
		} else if(!ParametroUtil.instance().isPrimeiroGrau() && getJurisdicaoRedistribuicao() != null) {
			if(getOrgaoJulgadorColegiadoRedistribuicao() != null) {
				if(getOrgaoJulgadorRedistribuicao() != null) {
					if(getOrgaoJulgadorRedistribuicao().getAtivo()) {
						return getComponent("orgaoJulgadorVinculadoColegiadoProcessoRedistribuicaoPrevencaoItems");
					} else {
							return getComponent("orgaoJulgadorVinculadoColegiadoProcessoRedistribuicaoPrevencaoTodosItems");
					}
				} else {
					return getComponent("orgaoJulgadorVinculadoColegiadoProcessoRedistribuicaoPrevencaoTodosItems");
				}
			}
		}
		return null;
	}
		
	public void limparTelaRedistribuicaoTipoCompetenciaExclusiva() {
        if(!ParametroUtil.instance().isPrimeiroGrau()) {
			setOrgaoJulgadorColegiadoRedistribuicao(null);
		}
		setOrgaoJulgadorRedistribuicao(null);
		setJurisdicaoRedistribuicao(null);
		setCompetenciaRedistribuicao(null);
	}
	
	@SuppressWarnings("incomplete-switch")
	public boolean desabilitarBtnGravarRedistribuicao(){
		switch (getInTipoRedistribuicao()){
			case C:
				return false;
			case D:
				return desabilitarBtnGravarRedistribuicaoDesaforamento();
			case E:
				return desabilitarBtnGravarRedistribuicaoReuniaoExecucoes();
			case N:
				return desabilitarBtnGravarRedistribuicaoImpedimento();
			case O:
				return desabilitarBtnGravarRedistribuicaoSuspeicao();
			case I:
				return this.desabilitarBtnGravarRedistribuicaoImpedimento();
			case J:
				if(getInstance().getInTipoDistribuicao() != null){
					switch(getInstance().getInTipoDistribuicao()){
						case PD:
							return desabilitarBtnGravarRedistribuicaoPrevencaoDependencia();
						case PP:
							return desabilitarBtnGravarRedistribuicaoPrevencaoDependencia();
						case S:
							return false;
						case CE:
							return desabilitarBtnGravarRedistribuicaoTipoCompetenciaExclusiva();
						case EN:
							return desabilitarBtnGravarRedistribuicaoEncaminhamento();
					}
				}
				return true;
			
			case M:
				if(getInstance().getInTipoDistribuicao() != null){
					switch(getInstance().getInTipoDistribuicao()){
						case PD:
							return desabilitarBtnGravarRedistribuicaoPrevencaoDependencia();
						case PP:
							return desabilitarBtnGravarRedistribuicaoPrevencaoDependencia();
						case S:
							return false;
						case EN:
							return desabilitarBtnGravarRedistribuicaoEncaminhamento();
					}
				}
				return true;
			
			case P:
				return desabilitarBtnGravarRedistribuicaoPrevencaoDependencia();
				
			case R:
				return desabilitarBtnGravarRedistribuicaoIncompetencia();
				
			case S:
				return desabilitarBtnGravarRedistribuicaoSuspeicao();
			
			case Z:
				return desabilitarBtnGravarRedistribuicaoSucessao();
			
			case U:
				return false;
			
			case X:
				return false;
			
			case W:
				if(getInstance().getInTipoDistribuicao() != null){
					switch(getInstance().getInTipoDistribuicao()){
						case CE:
							return desabilitarBtnGravarRedistribuicaoTipoCompetenciaExclusiva();
						case S:
							return false;
						case EN:
							return desabilitarBtnGravarRedistribuicaoEncaminhamento();
					}
				}
				return true;
			
		}
		return false;
	}
		
	public boolean desabilitarBtnGravarRedistribuicaoDesaforamento(){		
		if((ParametroUtil.instance().isPrimeiroGrau())
				&& (getJurisdicaoRedistribuicao() != null)) {
			return false;
		}
		return true;
	}
		
	public boolean desabilitarBtnGravarRedistribuicaoReuniaoExecucoes(){		
		return desabilitarBtnGravarRedistribuicaoPrevencaoDependencia(); 
	}
	
	public boolean desabilitarBtnGravarRedistribuicaoImpedimento(){		
		if(getInstance().getCausaImpedimento() != null) {
			return false; 
		}
		return true;
	}
	
	public boolean desabilitarBtnGravarRedistribuicaoIncompetencia(){
		if((ParametroUtil.instance().isPrimeiroGrau())
			&& (getJurisdicaoRedistribuicao() != null)
			&& (getCompetenciaRedistribuicao() != null)) {
				return false;
		} else if(!ParametroUtil.instance().isPrimeiroGrau() && (getCompetenciaRedistribuicao() != null)) {			
				return false;
		}
		return true;
	}
	
	public boolean desabilitarBtnGravarRedistribuicaoSuspeicao(){		
		if(getInstance().getCausaSuspeicao() != null) {
			return false; 
		}
		return true;
	}
	
	public boolean desabilitarBtnGravarRedistribuicaoSucessao(){
		if(competenciaRedistribuicao != null){
			return false;
		}
		return true;
	}
	
	public boolean desabilitarBtnGravarRedistribuicaoTipoCompetenciaExclusiva(){
		if((ParametroUtil.instance().isPrimeiroGrau())
				&& (getJurisdicaoRedistribuicao() != null)
				&& (getOrgaoJulgadorRedistribuicao() != null)) {
					return false;
			} else if(!ParametroUtil.instance().isPrimeiroGrau() && (getCompetenciaRedistribuicao() != null)) {			
					return false;
			}
			return true;
	}
	
	/**
	 * Se for uma redistribuicao por motivos: impedimento / suspeicao / criacao
	 * de unidade judiciaria / extincao de unidade judiciaria e só houver um OJ
	 * na jurisdição, então permite a alteração da jurisdição
	 * 
	 * @return
	 */
	public boolean permiteSelecionarJurisdicaoRedistribuicao() {
		boolean permissao = false;
		if (this.inTipoRedistribuicao != null && (this.inTipoRedistribuicao.equals(TipoRedistribuicaoEnum.I) ||
				this.inTipoRedistribuicao.equals(TipoRedistribuicaoEnum.S) || 
				this.inTipoRedistribuicao.equals(TipoRedistribuicaoEnum.U) ||
				this.inTipoRedistribuicao.equals(TipoRedistribuicaoEnum.X))) {
			
			ProcessoTrf processo = this.obterProcessoTrf();
			List<OrgaoJulgador> orgaosJulgadoresJurisdicao = ComponentUtil.getComponent(OrgaoJulgadorManager.class)
					.findAllbyJurisdicao(processo.getJurisdicao());
			
			permissao = orgaosJulgadoresJurisdicao == null || orgaosJulgadoresJurisdicao.size() < 2;
		}
		return permissao;
	}
		
	public boolean desabilitarBtnGravarRedistribuicoesTipoSorteio(){
		if(getJurisdicaoSorteio() != null) {
			return false; 
		}
		return true;
	}
	
	public boolean desabilitarBtnGravarRedistribuicaoPrevencaoDependencia() {
		return !ComponentUtil.getComponent(PesquisaProcessoParadigmaAction.class).getProcessoValidado();
	}
	
	private boolean desabilitarBtnGravarRedistribuicaoEncaminhamento() {
		boolean retorno = getJurisdicaoRedistribuicao() == null || getProcessoDocumentoVinculado() == null;
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			retorno = retorno || getOrgaoJulgadorColegiadoRedistribuicao() == null;
		}
		return retorno;
	}

	/**
	 * Método responsável por carregar o tipo de distribuição do processo
	 */
	public void carregarTipoDistribuicaoProcesso(){
		if (getInstance().getInTipoDistribuicao() == null) {
			instance.setInTipoDistribuicao(ProcessoTrfHome.instance().getTipoDistribuicao());
			
			if (instance.getInTipoDistribuicao() == null) {
				// Recuperar o log de distribuição do processo
				ProcessoTrfLogDistribuicao logDistribuicao =  ProcessoTrfLogDistribuicaoHome.instance().recuperarPorProcesso(obterProcessoTrf());
				
				if (logDistribuicao != null) {
					instance.setInTipoDistribuicao(logDistribuicao.getInTipoDistribuicao());
				}
			}
		}
	}

	/**
	 * Redistribui o ProcessoTrf devido a impedimento de OrgaoJulgadorColegiado
	 */
	public void gravarRedistribuicaoImpedimentoOrgaoJulgadorColegiado() {
		try {
			this.identificaMotivoRedistribuicao(TipoRedistribuicaoEnum.Y);
			getInstance().setInTipoDistribuicao(TipoDistribuicaoEnum.S);
			
			ProcessoTrf processoTrf = obterProcessoTrf();
			processoTrf.setIsIncidente(false);
	        if(!ParametroUtil.instance().isPrimeiroGrau()) {
				getInstance().setOrgaoJulgadorColegiadoAnterior(processoTrf.getOrgaoJulgadorColegiado());
			}
			getInstance().setOrgaoJulgadorAnterior(processoTrf.getOrgaoJulgador());
			DistribuicaoService distribuicaoService = DistribuicaoService.instance();
			distribuicaoService.setCompetenciaConflito(getCompetenciaRedistribuicao());
			distribuicaoService.redistribuirProcesso(processoTrf, getInstance());
			ProcessoHome.instance().update();
			gravarRedistribuicao();
			
			this.lancarMovimentoRedistribuicao(processoTrf, TipoDistribuicaoEnum.S, TipoRedistribuicaoEnum.Y, this.processoDocumentoVinculado);

			setAtualiza(true);
			FacesMessages.instance().clear();
			this.mensagemProtocolacao = obterMensagemSucesso(processoTrf);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.mensagemProtocolacao);
			podeFecharJanela = true;
		} 
		catch (Exception e) {
			this.mensagemProtocolacao = obterMensagemErro(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
			podeFecharJanela = false;
		}		
	}

	/**
	 * Método responsável por inicializar os valores das variáveis de instância:
	 * <ul>
	 * 		<li>orgaoJulgadorRedistribuicao</li>
	 * 		<li>orgaoJulgadorColegiadoRedistribuicao</li>
	 * 		<li>jurisdicaoRedistribuicao</li>
	 * 		<li>processoDocumentoVinculado</li>
	 * 	</ul>
	 * 
	 * @param processoTrf
	 */
	public void iniciarEncaminhamento(ProcessoTrf processoTrf){
		setOrgaoJulgadorColegiadoRedistribuicao(processoTrf.getOrgaoJulgadorColegiado());
		setOrgaoJulgadorRedistribuicao(processoTrf.getOrgaoJulgador());
		setJurisdicaoRedistribuicao(processoTrf.getJurisdicao());

		ProcessoDocumentoRedistribuicaoEncaminhamentoList documentoList = 
				getComponent(ProcessoDocumentoRedistribuicaoEncaminhamentoList.NAME);
		
		List<ProcessoDocumento> list = documentoList.list(10);
		// Caso a consulta traga apenas um resultado, pré selecionar este documento. 
		if(list.size() == 1){
			setProcessoDocumentoVinculado(list.get(0));
		}
	}
	
	public void iniciarTipoComJurisdicao(ProcessoTrf processoTrf) {
		setJurisdicaoRedistribuicao(processoTrf.getJurisdicao());
	}
	
	/**
	 * Recupera uma lista de {@link OrgaoJulgadorColegiado} ativos, de competência ativa e vinculados à uma Jurisdicao.
	 * 
	 * @return Lista de {@link OrgaoJulgadorColegiado} ativos, de competência ativa e vinculados à uma Jurisdicao.
	 */
	public List<OrgaoJulgadorColegiado> getOrgaoJulgadorColegiadoListByJurisdicaoRedistribuicao(){
		return orgaoJulgadorColegiadoManager.getColegiadosByJurisdicao(jurisdicaoRedistribuicao);
	}
	
	/**
	 * Retorna uma lista de {@link OrgaoJulgador} vinculados ao {@link OrgaoJulgadorColegiado}.
	 * 
	 * @return Lista de {@link OrgaoJulgador} vinculados ao {@link OrgaoJulgadorColegiado}.
	 */
	public List<OrgaoJulgador> getOrgaosJulgadoresListByOrgaoJulgadorColegiadoRedistribuicao() {
		List<OrgaoJulgador> resultado = new ArrayList<OrgaoJulgador>(0);
		if (orgaoJulgadorColegiadoRedistribuicao != null) {
			for(OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorColegiadoOrgaoJulgador : 
				OrgaoJulgadorColegiadoOrgaoJulgadorManager.instance().obterAtivos(orgaoJulgadorColegiadoRedistribuicao)) {
		
				resultado.add(orgaoJulgadorColegiadoOrgaoJulgador.getOrgaoJulgador());
			}			
		} else if (jurisdicaoRedistribuicao != null) {
			resultado.addAll(orgaoJulgadorManager.obterItensAtivosPrimeiraInstancia(jurisdicaoRedistribuicao.getIdJurisdicao()));
		}
		return resultado;
	}
	
	public String obterMensagemNenhumDocumentoDisponivel() {
		return FacesUtil.getMessage("processoTrfRedistribuicao.erro.processoSemDocumentoAtoProferido");
	}
	
	@SuppressWarnings("unchecked")
	private String obterNomesTiposDocumentos() {
		String nomes = new String();
		TipoProcessoDocumentoManager tipoPdManager = ComponentUtil.getComponent(TipoProcessoDocumentoManager.NAME);
		List<TipoProcessoDocumento> tiposProcessoDocumentos = tipoPdManager.getTipoDocumentoAtoProferidoList();
			
		Collection<String> nomesTipoProcDoc = CollectionUtils.collect(tiposProcessoDocumentos, TransformerUtils.invokerTransformer("getTipoProcessoDocumento"));
		nomes = StringUtils.join(nomesTipoProcDoc,",");

		return nomes;
	}
	
	/**
	 * Método responsável por definir a {@link #competenciaRedistribuicao} do processo de
	 * acordo com o {@link CasoCompetenciaEnum}.
	 * 
	 */
	public CasoCompetenciaEnum casoCompetenciaConflito() {
		if (casoCompetencia == null) {
			casoCompetencia = CasoCompetenciaEnum.CASO1;

			List<Competencia> competenciasPossiveis = ProcessoTrfHome.instance().getCompetenciasPossiveis();
			ProcessoTrf processoTrf = obterProcessoTrf();
			
			if (CollectionUtilsPje.isEmpty(competenciasPossiveis)) {
				competenciasPossiveis = AutuacaoService.instance().recuperaCompetenciasPossiveis(processoTrf, processoTrf.getJurisdicao());
			}
			Competencia competenciaProcesso = processoTrf.getCompetencia();
			for (Competencia competenciaPossivel : competenciasPossiveis) {
				if (competenciasPossiveis.size() > 1) {
					if (isCompetenciasIguais(competenciaProcesso, competenciaPossivel)) {
						setCompetenciaRedistribuicao(competenciaPossivel);
						casoCompetencia = CasoCompetenciaEnum.CASO2;
						break;
					}
				} else if (competenciasPossiveis.size() == 1) {
					casoCompetencia = definirCaso3ou4(competenciaProcesso, competenciaPossivel);
					setCompetenciaRedistribuicao(competenciaPossivel);
					break;
				}
			}
			if (CollectionUtilsPje.isEmpty(competenciasPossiveis)) {
				casoCompetencia = definirCaso5ou6(competenciaProcesso);
			}
		}
		return casoCompetencia;
	}

	/**
	 * Método responsável por definir o retorno do método
	 * {@link #casoCompetenciaConflito()} de acordo com a competência do
	 * processo
	 * 
	 * @param competenciaProcesso
	 *            a competência do processo
	 * @param competenciaPossivel
	 *            a competência possível para este processo
	 * @return <code>CasoCompetenciaEnum</code>, caso a competência do processo
	 *         e a competência possível forem iguais, é retornado o
	 *         {@link CasoCompetenciaEnum#CASO4} e a competência de
	 *         redistribuição continua sendo a que está no processo. Caso sejam
	 *         diferentes, é retornado o {@link CasoCompetenciaEnum#CASO3} e ela
	 *         será selecionada para competência de redistribuição.
	 */
	private CasoCompetenciaEnum definirCaso3ou4(Competencia competenciaProcesso, Competencia competenciaPossivel) {
		CasoCompetenciaEnum retorno;
		if (isCompetenciasIguais(competenciaProcesso, competenciaPossivel)) {
			retorno = CasoCompetenciaEnum.CASO4;
		} else {
			retorno = CasoCompetenciaEnum.CASO3;
		}
		return retorno;
	}

	/**
	 * Método responsável por definir o retorno do método 
	 * {@link #casoCompetenciaConflito()} de acordo com a competência do
	 * processo
	 * 
	 * @param competenciaProcesso
	 *            a competência do processo
	 * @return <code>CasoCompetenciaEnum</code>, caso a competência do processo
	 *         exista, é retornado o {@link CasoCompetenciaEnum#CASO5}, e a
	 *         competência de redistribuição é a mesma do processo. Caso o
	 *         processo não tenha competência, é retonado o
	 *         {@link CasoCompetenciaEnum#CASO6} e não será possível
	 *         redistribuir este processo.
	 */
	private CasoCompetenciaEnum definirCaso5ou6(Competencia competenciaProcesso) {
		CasoCompetenciaEnum retorno;
		if (competenciaProcesso != null) {
			retorno = CasoCompetenciaEnum.CASO5;
			setCompetenciaRedistribuicao(competenciaProcesso);
		} else {
			retorno = CasoCompetenciaEnum.CASO6;
		}
		return retorno;
	}	

	/**
	 * Método responsável por verificar se a
	 * {@link ProcessoTrf#getCompetencia()} é igual a {@link Competencia}
	 * possível
	 * 
	 * @param competenciaProcesso
	 *            a competência do processo
	 * @param competenciaPossivel
	 *            a competência possível para este processo de acordo com a
	 *            {@link ClasseJudicial} e {@link Assunto}
	 * @return <code>Boolean</code>, <code>true</code> se a competência possível
	 *         for igual a que está no processo.
	 */
	private boolean isCompetenciasIguais(Competencia competenciaProcesso, Competencia competenciaPossivel) {
		return (competenciaProcesso != null && competenciaProcesso.equals(competenciaPossivel));
	}
	
	/**
 	 * 
 	 * Método chamado na ação de gravar dos tipos de redistribuição "Suspeição" e "Impedimento"
 	 * 
 	 * @return void
 	 */
 	public void gravarSuspeicaoOuImpedimento() {
 		if (TipoRedistribuicaoEnum.S.equals(getInTipoRedistribuicao())) {
 			gravarRedistribuicaoSuspeicao();
 		} else if (TipoRedistribuicaoEnum.I.equals(getInTipoRedistribuicao())) {
 			gravarRedistribuicaoImpedimento();
 		}
 	}

	//faz rollback na transação do jbpm e da aplicação, de modo que o processo não fique fora do fluxo
	private void rollback(){
		JbpmContext context = JbpmConfiguration.getInstance().getCurrentJbpmContext();
		// Limpa sessao hibernate (JBPM) e fecha contexto evitando flush.
		JbpmUtil.clearAndClose(context);
		
		try{
			// Libera a associação entre a transação (do Seam) e a thread corrente. Dessa forma o 
			// Util.beginTransaction pode iniciar outra transação e associar à thread corrente.
			//
			// Não foi usado o Util.rollbackTransction, pois ele checa se a transação está ativa, porém, dependendo da exceção (método Work.isRollbackRequired()), 
			// o interceptor do Seam pode "marcar" a transação para rollback, o que deixa a transação como inativa,
			// mas deixa a thread associada à transação inativa (o que prejudica a próxima iteração, quando ocorrer o beginTransaction - exceção:
			// "thread is already associated with a transaction!").
			Transaction.instance().rollback();
		} catch (Exception e1){}
			
		// Remove dos contextos do Seam o contexto do JBPM (ManagedJbpmContext.instance()), dessa forma, na próxima 
		// iteração, o Seam irá criar outro contexto JBPM (ManagedJbpmContext.create()), associado a uma sessão nova do Hibernate, 
		// pois ao dar "rollback" no contexto JBPM a sessão tem que ser fechada (JbpmUtil.clearAndClose()). 
		// Isso é necessário, pois a implementação do JBPM do Seam não usa JTA.
		Contexts.removeFromAllContexts("org.jboss.seam.bpm.jbpmContext");
		
		// Iniciar uma transação, se não houver transação ativa.
		Util.beginTransaction();		
	}
	
	/**
	 * Mtodo responsvel por obter a entidade gerenciada {@link ProcessoTrf}.
	 * 
	 * @return A entidade gerenciada {@link ProcessoTrf} atualizada.
	 */
	private ProcessoTrf obterProcessoTrf() {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		if (EntityUtil.getEntityManager().contains(processoTrf)) {
			EntityUtil.getEntityManager().refresh(processoTrf);
		}
		return processoTrf;
	}
	
	/**
	 * Metodo que retorna o codigo para o processo de acordo com a eleicao
	 * -Prevencao por estado
	 * -Prevencao por municipio.
	 * 
	 * @param processo
	 * @return Codigo
	 * @throws PJeBusinessException
	 */
	private String getCodigoTipoPrevencaoEleitoralArt260(ProcessoTrf processo) throws PJeBusinessException{
		if(processo == null || processo.getComplementoJE() == null){
			throw new PJeBusinessException("Não foi possível efetuar o lançamento de movimentao para a redistribuição do processo, sem dados eleitorais.");
		}
		return (processo.getComplementoJE().getEleicao().isGeral()) ? COD_COMPLEMENTO_TIPO_DISTRIBUICAO_REDISTRIBUICAO.PREVENCAO_ART260_ELEICAO_ESTADUAL : COD_COMPLEMENTO_TIPO_DISTRIBUICAO_REDISTRIBUICAO.PREVENCAO_ART260_ELEICAO_MUNICIPAL;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoTrfRedistribuicao> recuperarPorProcesso(ProcessoTrf processoTrf) {
		StringBuilder sb = new StringBuilder();
		sb.append(" select p from ProcessoTrfRedistribuicao p");
		sb.append(" where p.processoTrf = :processoTrf");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processoTrf", processoTrf);
		
		List<ProcessoTrfRedistribuicao> results = q.getResultList();
		return results;
	}
	
	private String obterMensagemSucesso(ProcessoTrf processoTrf) {
		return FacesUtil.getMessage("processoTrfRedistribuicao.sucesso", new Object[]{processoTrf.getNumeroProcesso(), processoTrf.getOrgaoJulgador()});
	}
	
	private String obterMensagemErro(String erro) {
		return FacesUtil.getMessage("processoTrfRedistribuicao.erro", new Object[]{erro});
	}
}
