package br.jus.cnj.pje.servicos;

import java.security.SecureRandom;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import br.com.infox.cliente.home.ProcessoTrfRedistribuicaoHome;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.home.ProcessoAudienciaHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.pje.dao.ProcessoTrfDAO;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.com.jt.pje.manager.OrgaoJulgadorColegiadoOrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.CaixaAdvogadoProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.ComplementoProcessoJEManager;
import br.jus.cnj.pje.nucleo.manager.DistanciaMaximaDistribuicaoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorCargoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoAlertaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoCaixaAdvogadoProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteCaixaAdvogadoProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoPesoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfConexaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfRedistribuicaoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.cnj.pje.nucleo.manager.VinculacaoDependenciaEleitoralManager;
import br.jus.cnj.pje.nucleo.service.DefinicaoCompetenciaService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.je.entidades.ComplementoProcessoJE;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.jt.entidades.HistoricoDeslocamentoOrgaoJulgador;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.AgrupamentoClasseJudicial;
import br.jus.pje.nucleo.entidades.AssociacaoDimensaoPessoalEnum;
import br.jus.pje.nucleo.entidades.AssuntoAgrupamento;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ClasseJudicialAgrupamento;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.DimensaoFuncional;
import br.jus.pje.nucleo.entidades.DimensaoPessoal;
import br.jus.pje.nucleo.entidades.DimensaoPessoalPessoa;
import br.jus.pje.nucleo.entidades.DimensaoPessoalTipoPessoa;
import br.jus.pje.nucleo.entidades.DistanciaMaximaDistribuicao;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.ItemsLog;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoCompetencia;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCompetencia;
import br.jus.pje.nucleo.entidades.PesoPrevencao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoCaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteCaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoPesoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.entidades.ProcessoTrfLogDistribuicao;
import br.jus.pje.nucleo.entidades.ProcessoTrfRedistribuicao;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.VinculacaoDependenciaEleitoral;
import br.jus.pje.nucleo.enums.ClasseComposicaoJulgamentoEnum;
import br.jus.pje.nucleo.enums.CompetenciaEnum;
import br.jus.pje.nucleo.enums.ComposicaoJulgamentoEnum;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;
import br.jus.pje.nucleo.enums.CriticidadeEnum;
import br.jus.pje.nucleo.enums.EtapaAudienciaEnum;
import br.jus.pje.nucleo.enums.PrevencaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.SimNaoFacultativoEnum;
import br.jus.pje.nucleo.enums.TipoConexaoEnum;
import br.jus.pje.nucleo.enums.TipoDistribuicaoEnum;
import br.jus.pje.nucleo.enums.TipoRedistribuicaoEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name(DistribuicaoService.NAME)
@Scope(ScopeType.EVENT)
public class DistribuicaoService {

	public final static String NAME = "distribuicaoService";
	private static final LogProvider log = Logging.getLogProvider(DistribuicaoService.class);

	private ProcessoTrf processoTrf;
	private ProcessoTrfRedistribuicao processoTrfRedistribuicao;
	private List<OrgaoJulgador> orgaoJulgadorList = new ArrayList<OrgaoJulgador>(0);
	private List<OrgaoJulgador> orgaoJulgadorImpedidoList = new ArrayList<OrgaoJulgador>(0);
	private List<OrgaoJulgadorColegiado> orgaoJulgadorColegiadoList = new ArrayList<OrgaoJulgadorColegiado>(0);
	private List<OrgaoJulgadorColegiado> orgaoJulgadorColegiadoPorCompetenciaList = new ArrayList<OrgaoJulgadorColegiado>(0);
	private List<OrgaoJulgadorCargo> orgaoJulgadorCargoList = new ArrayList<OrgaoJulgadorCargo>(0);
	private List<OrgaoJulgadorCargo> orgaoJulgadorCargoExcluidoList = new ArrayList<OrgaoJulgadorCargo>(0);
	private OrgaoJulgadorCargo orgaoJulgadorCargoSorteado = null;
	private OrgaoJulgadorCargo orgaoJulgadorCargoAnterior = null;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiadoAnterior = null;
	private ProcessoTrfLogDistribuicao logDist = new ProcessoTrfLogDistribuicao();
	private Competencia competenciaConflito = null;
	private List<Competencia> competenciaList = new ArrayList<Competencia>(0);
	private List<AssuntoTrf> assuntoTrfList = new ArrayList<AssuntoTrf>(0);
	private Double pesoProcessual = 0.0;
	private Double pesoDistribuicao = 0.0;
	private Double pesoProcessualAnterior = 0.0;
	private Double pesoDistribuicaoAnterior = 0.0;
	private final Date dataAtual = DateUtil.getBeginningOfDay(new Date());
	private final boolean isPrimeiroGrau = ParametroUtil.instance().isPrimeiroGrau();
	private Boolean compensacaoPesoEleitoralAtivo;
	private int codNucleo = 102;
	
	public static DistribuicaoService instance() {
		return ComponentUtil.getComponent(DistribuicaoService.class);
	}

	private void limparService() {
		orgaoJulgadorList.clear();
		orgaoJulgadorImpedidoList.clear();
		orgaoJulgadorColegiadoList.clear();
		orgaoJulgadorColegiadoPorCompetenciaList.clear();
		orgaoJulgadorCargoList.clear();
		orgaoJulgadorCargoSorteado = null;
		orgaoJulgadorCargoAnterior = null;
		orgaoJulgadorColegiadoAnterior = null;
		logDist = new ProcessoTrfLogDistribuicao();
		competenciaList.clear();
		assuntoTrfList.clear();
		pesoProcessual = 0.0;
		pesoDistribuicao = 0.0;
		pesoProcessualAnterior = 0.0;
		pesoDistribuicaoAnterior = 0.0;
	}

	public void distribuirProcesso(ProcessoTrf processoTrf, Competencia competenciaConflito) throws Exception {
		limparService();
		this.setCompetenciaConflito(competenciaConflito);
		distribuirProcesso(processoTrf);
	}

	public void distribuirProcesso(ProcessoTrf processoTrf) throws Exception {
		log.info(String.format("Iniciando a distribuicao do processo [%d]", processoTrf.getIdProcessoTrf()));
		limparService();
		this.processoTrf = processoTrf;
		ProcessoTrfHome.instance().validarAutuacao(processoTrf);
		List<ProcessoAssunto> processoAssuntoList = processoTrf.getProcessoAssuntoList();

		if (processoAssuntoList == null || processoAssuntoList.isEmpty()) {
			throw new IllegalArgumentException("A lista de Assuntos vazia.");
		}
		
		if(processoTrf.getCompetencia().getIndicacaoOrgaoJulgadorObrigatoria()) {
			if(this.processoTrf.getOrgaoJulgador() == null) {
				throw new IllegalArgumentException("É obrigatória a indicação de um órgão julgador para a distribuição.");
			}else {
				distribuirOrgaoJulgadorSelecionado();
			}
		} else if(isJEeParametroPrevencaoAtivo()){
			distribuirPorEleicao();
		} else if(this.processoTrf.getIsIncidente()) {
			distribuirIncidental();
		} else {
			distribuir();
		}
	}

	private void distribuirOrgaoJulgadorSelecionado() throws Exception {
		log.info(String.format("Distribuir o processo [%d] por competência exclusiva", processoTrf.getIdProcessoTrf()));
		
		TipoDistribuicaoEnum tipoDistribuicao = TipoDistribuicaoEnum.CE;

		OrgaoJulgador orgaoJulgadorPesquisado = ComponentUtil.getOrgaoJulgadorManager().findById(this.processoTrf.getOrgaoJulgador().getIdOrgaoJulgador());
		
		if(orgaoJulgadorPesquisado.getOrgaoJulgadorCargoList() != null) {
			for (OrgaoJulgadorCargo orgaoJulgadorCargo : orgaoJulgadorPesquisado.getOrgaoJulgadorCargoList()) {
				if(orgaoJulgadorCargo.getRecebeDistribuicao()) {
					this.processoTrf.setOrgaoJulgadorCargo(orgaoJulgadorCargo);
					break;
				}
			}
		}
		
		logDist.setProcessoTrf(this.processoTrf);
		logDist.setInTipoDistribuicao(tipoDistribuicao);
		logDist.setOrgaoJulgador(this.processoTrf.getOrgaoJulgador());
		logDist.setOrgaoJulgadorCargo(this.processoTrf.getOrgaoJulgadorCargo());
		logDist.setOrgaoJulgadorColegiado(this.processoTrf.getOrgaoJulgadorColegiado());
		
		processaConfiguracoesRevisor();

		atualizarAcumuladorCargoJudicial(this.processoTrf.getOrgaoJulgadorCargo(), this.orgaoJulgadorCargoAnterior);

		ajustarFluxo(this.processoTrf.getIdProcessoTrf());
		EntityUtil.getEntityManager().persist(logDist);
		this.processoTrf.setProcessoStatus(ProcessoStatusEnum.D);
		this.processoTrf.setDataDistribuicao(new Date());
		verificarClasseSigilosa();
	}

	public boolean isJEeParametroPrevencaoAtivo() {
		return ParametroUtil.instance().isParametroPrevencaoAtivoNaJusticaEleitoral();
	}
	
	/**
	 * Na justica eleitoral serao feitas algumas verificacoes peculiare, no caso do artigo 260.
	 * 
	 * @throws PJeBusinessException
	 * @throws Exception
	 */
	private void distribuirPorEleicao() throws PJeBusinessException, Exception {
		log.info(String.format("Verificando a distribuicao do processo [%d] por eleicao", processoTrf.getIdProcessoTrf()));
		this.processoPreventoEleicaoOrigem = buscarProcessoPrevencaoEleicaoOrigemComplemento(this.processoTrf);
		
		if (this.processoPreventoEleicaoOrigem != null && verificarEnquadramento(this.processoTrf)) {
			log.info(String.format("O processo [%d] se enquadra no 260", processoTrf.getIdProcessoTrf()));
			distribuirPorEleicaoOrigem();
		} else if(this.processoTrf.getIsIncidente()) {
			distribuirIncidental();
		} else {
			distribuir();
		}
	}
	
	public void redistribuirProcesso(ProcessoTrf processoTrf, ProcessoTrfRedistribuicao processoTrfRedistribuicao, List<Competencia> competencias) throws Exception {
		limparService();
		this.competenciaList = competencias;
		redistribuirProcesso(processoTrf, processoTrfRedistribuicao, false);
	}

	@SuppressWarnings("incomplete-switch")
	private void redistribuirProcesso(ProcessoTrf processoTrf, ProcessoTrfRedistribuicao processoTrfRedistribuicao, boolean limpaService) throws Exception {
		if (limpaService) {
			limparService();
		}
		if(!isTaskInstanceAtualAberta()) {
			throw new Exception("Processo já foi movimentado!");
		}
		this.processoTrf = processoTrf;
		this.processoTrfRedistribuicao = processoTrfRedistribuicao;
		ProcessoTrfHome.instance().validarRedistribuicao(processoTrf, processoTrfRedistribuicao.getInTipoRedistribuicao());

		boolean isProcessoRedistribuicaoPossuiVinculacaoDependenciaEleitoral = verificaSePrevencao260SeraRealizada();
		
		switch (processoTrfRedistribuicao.getInTipoRedistribuicao()) {
			case A:
				redistribuirProcessoPorAfastamentoRelator();
				break;
			
			case C:
				distribuir();
				break;
			case D:
				redistribuirProcessoPorDesaforamento();
				break;
			
			case E:
				redistribuirProcessoPorPrevencao();
				break;
			
			case I:
				if(ParametroJtUtil.instance().justicaEleitoral()){
					switch(this.processoTrfRedistribuicao.getInTipoDistribuicao()){
					case PD: 
						redistribuirProcessoPorPrevencao();
						break;
					case S:
						distribuir();
						break;
					}
				}else{
					redistribuirProcessoPorImpedimento();
				}
				break;
			case J:
				switch(this.processoTrfRedistribuicao.getInTipoDistribuicao()){
					case S:
						distribuir();
						break;
					case EN:
						if (isProcessoRedistribuicaoPossuiVinculacaoDependenciaEleitoral) {
			 				distribuirPorEleicaoOrigem();
						} else {
							redistribuirProcessoPorEncaminhamento();
						}
						break;
				}		
				break;
			
			case K:
				redistribuirProcessoPorRazaoPosseRelator();
				break;
		
			case M:
				switch(this.processoTrfRedistribuicao.getInTipoDistribuicao()){
					case PD: 
						redistribuirProcessoPorPrevencao();
						break;
					case PP:
						redistribuirProcessoPorPrevencao();
						break;				
					case S:
						distribuir();
						break;
				}
				break;
			
			case N:
				redistribuirProcessoPorImpedimentoRelator();
				break;
			
			case O:
				redistribuirProcessoPorSuspeicaoRelator();
				break;
			
			case P:
				redistribuirProcessoPorPrevencao();
				break;
			
			case R:
				redistribuirProcessoPorIncompetencia();
				break;
			
			case S:
				redistribuirProcessoPorSuspeicao();
				break;
				
			case Z:
				redistribuirProcessoPorSucessao();
				break;
				
			case T:
				redistribuirProcessoPorAfastamentoTemporarioTitular();
				break;
			
			case Y: 
				redistribuirProcessoPorImpedimentoOrgaoJulgadorColegiado();
				break;
				
			case U:
				distribuir();
				break;
			
			case X:
				distribuir();
				break;
			
			case W:
				switch(this.processoTrfRedistribuicao.getInTipoDistribuicao()){
					case S:
						distribuir();
						break;
					case PD: 
						redistribuirProcessoPorPrevencao();
						break;
					case PP:
						redistribuirProcessoPorPrevencao();
						break;				
				}
				break;
		
			default:
				throw new Exception("Tipo de redistribuição inválido!");
		}
			
		gravarProcessoRedistribuicao();
	}

	/**
	 * Metodo que sera utilizado na redistribuicao, com a funcao de verificar se a prevencao ira ser realizada.
	 * 
	 * @return
	 * @throws PJeBusinessException
	 */
	private boolean verificaSePrevencao260SeraRealizada() throws PJeBusinessException {
		boolean isProcessoRedistribuicaoPossuiVinculacaoDependenciaEleitoral = Boolean.FALSE;
		if (isJEeParametroPrevencaoAtivo() && verificarEnquadramento(this.processoTrfRedistribuicao.getProcessoTrf())) {
			this.vinculacaoDependenciaEleitoral = ComponentUtil.getComponent(VinculacaoDependenciaEleitoralManager.class).recuperaVinculacaoDependencia(this.processoTrfRedistribuicao.getProcessoTrf().getComplementoJE());
 			if(this.vinculacaoDependenciaEleitoral != null){
 				isProcessoRedistribuicaoPossuiVinculacaoDependenciaEleitoral = Boolean.TRUE;
 			}
		}
		return isProcessoRedistribuicaoPossuiVinculacaoDependenciaEleitoral;
	}
	
	// Se a classe judicial for sigilosa, o processo é sigiloso
	private void verificarClasseSigilosa() {
		Boolean classeJudicialSegredoJustica = this.processoTrf.getClasseJudicial() != null && 
				this.processoTrf.getClasseJudicial().getSegredoJustica() != null && 
				this.processoTrf.getClasseJudicial().getSegredoJustica();
	 	if(classeJudicialSegredoJustica){
	 		this.processoTrf.setSegredoJustica(true);
	 		}
	 	}

	/***
	 * Método responsável por tratar as audiências já marcadas. 
	 * Elas podem ser canceladas (em caso de mudança da cidade-sede da jurisdição do processo) ou redesignadas.
	 * @param processoTrf
	 * @throws Exception
	 */
	public void tratarAudienciasMarcadas() throws Exception {
		EntityManager em = EntityUtil.getEntityManager();

		Query query = em
				.createQuery("select o from ProcessoAudiencia o where o.statusAudiencia = 'M' and o.processoTrf.idProcessoTrf = :idProcessoTrf");
		query.setParameter("idProcessoTrf", processoTrf.getIdProcessoTrf());

		@SuppressWarnings("unchecked")
		List<ProcessoAudiencia> processoAudiencias = query.getResultList();

		if (processoAudiencias.size() > 0) {

			ProcessoHome processoHome = ProcessoHome.instance();
			Processo oldInstance = processoHome.getInstance();
			processoHome.setInstance(processoTrf.getProcesso());

			ProcessoAudienciaHome processoAudienciaHome = ProcessoAudienciaHome.instance();

			SimpleDateFormat formarter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

			ProcessoAlertaManager procAlertaManager = ComponentUtil.getComponent(ProcessoAlertaManager.class);
			
			for (ProcessoAudiencia pa : processoAudiencias) {

				processoAudienciaHome.setInstance(pa);
				pa.setDsMotivo("Redistribuição do processo");
					
				// No processo de redistribuição, as audiências marcadas como designada serão canceladas.
				processoAudienciaHome.setEtapaAudiencia(EtapaAudienciaEnum.C);
				processoAudienciaHome.cancelarAudiencia();
				String textoAlerta = "";
				
				if (!this.processoTrfRedistribuicao
						.getOrgaoJulgador()
						.getJurisdicao()
						.getMunicipioSede()
						.equals(this.processoTrfRedistribuicao.getOrgaoJulgadorAnterior().getJurisdicao()
								.getMunicipioSede())) {

					textoAlerta = "Processo redistribuído com cancelamento de audiência previamente designada no juízo de origem para "
							+ formarter.format(pa.getDtInicio());

				} else {
					boolean conflitante = processoAudienciaHome.remarcarPorRedistribuicao(this.processoTrfRedistribuicao.getOrgaoJulgador());

					ProcessoAudiencia processoAudienciaMarcada = processoAudienciaHome.getInstance();
					if (processoAudienciaMarcada.getSalaAudiencia() != null){
						StringBuilder stringBuilder = new StringBuilder();
						stringBuilder.append("Processo redistribuído com transferência de audiência previamente designada. A audiência foi transferida para a sala de audiências ");
						stringBuilder.append(processoAudienciaMarcada.getSalaAudiencia().getSala());
						stringBuilder.append(" deste juízo, devendo ser realizada no dia ");
						stringBuilder.append(formarter.format(pa.getDtInicio()));
						stringBuilder.append(".");
						if (conflitante) {
							stringBuilder.append(" ATENÇÃO: HÁ CONFLITO DE PAUTA DE AUDIÊNCIAS EM RAZÃO DESSA TRANSFERÊNCIA!");
						}
						textoAlerta = stringBuilder.toString();
					} else {
						textoAlerta = "Processo redistribuído sem transferência de audiência previamente designada. A audiência foi cancelada.";
					}
				}

				procAlertaManager.incluirAlertaAtivo(processoTrf, textoAlerta, CriticidadeAlertaEnum.C);
			}

			processoHome.setInstance(oldInstance);
		}

	}

	public void redistribuirProcesso(ProcessoTrf processoTrf, ProcessoTrfRedistribuicao processoTrfRedistribuicao) throws Exception {
		if(isJEeParametroPrevencaoAtivo()){
			boolean enquadra = verificarEnquadramento(processoTrfRedistribuicao.getProcessoTrf());
			
			if(!enquadra){
				retirarProcessoPreventoArt260CEDaCadeia(processoTrf);
			} else if (enquadra && (processoTrfRedistribuicao.getInTipoDistribuicao() != null && 
					processoTrfRedistribuicao.getInTipoDistribuicao().isSorteio() || processoTrfRedistribuicao.getInTipoDistribuicao().isDependencia())) {
				
				retirarProcessoPreventoArt260CEDaCadeia(processoTrf);
			} 
		}
		redistribuirProcesso(processoTrf, processoTrfRedistribuicao, true);
	}
	
	private void redistribuirProcessoPorAfastamentoRelator() throws Exception {
		distribuir();
	}
	
	private void redistribuirProcessoPorDesaforamento() throws Exception {
		distribuir();
	}	

	/**
	 * Método responsável por redistribuir um processo quando escolhido o motivo "Determinação judicial" e o tipo "Por encaminhamento".
	 * 
	 * @throws Exception
	 */
	private void redistribuirProcessoPorEncaminhamento() throws Exception {
		distribuir();
	}

	private void redistribuirProcessoPorImpedimento() throws Exception {
		distribuir();
	}
	
	private void redistribuirProcessoPorRazaoPosseRelator() throws Exception {
		distribuir();
	}
	
	private void redistribuirProcessoPorImpedimentoRelator() throws Exception {
		distribuir();
	}
	
	private void redistribuirProcessoPorSuspeicaoRelator() throws Exception {
		setCompetenciaConflito(this.processoTrf.getCompetencia());
		distribuir();
	}
	
	private void redistribuirProcessoPorIncompetencia() throws Exception {
		distribuir();
	}
	
	private void redistribuirProcessoPorSuspeicao() throws Exception {
		setCompetenciaConflito(this.processoTrf.getCompetencia());
		distribuir();
	}
	
	private void redistribuirProcessoPorSucessao() throws Exception {
		distribuir();
	}
	
	private void redistribuirProcessoPorAfastamentoTemporarioTitular() throws Exception {
		distribuir();
	}
	
	private void redistribuirProcessoPorPrevencao() throws Exception {
		distribuir();
	}

	private void distribuir() throws Exception {
		logDist.setProcessoTrf(this.processoTrf);

		// Dimensão Espacial
		buscarOrgaosJurisdicao();

		// Dimensão Material e Procedimental
		buscarOrgaosCompetencia();

		avaliarConflitoCompetencia();
		
		if (!isPrimeiroGrau) {
			unificarOrgaosJulgadores();
		}

		avaliarRedistribuicao();

		// Não validar a competência para os casos abaixo.
		if (this.getCompetenciaConflito() != null && this.processoTrfRedistribuicao != null && 
			!TipoDistribuicaoEnum.EN.equals(this.processoTrfRedistribuicao.getInTipoDistribuicao()) &&
	 		!TipoDistribuicaoEnum.PD.equals(this.processoTrfRedistribuicao.getInTipoDistribuicao()) &&
	 		!TipoDistribuicaoEnum.PP.equals(this.processoTrfRedistribuicao.getInTipoDistribuicao()) &&
	 		!TipoDistribuicaoEnum.Z.equals(this.processoTrfRedistribuicao.getInTipoDistribuicao())) {
			excluirOrgaosCompetenciaConflitante();
		}

		verificarListaOrgaoJulgador();

		if (this.processoTrfRedistribuicao != null && this.processoTrfRedistribuicao.getOrgaoJulgadorCargo() != null) {
			orgaoJulgadorCargoList.add(this.processoTrfRedistribuicao.getOrgaoJulgadorCargo());
		} else {
			buscarCargosElegiveis();
		}

		sortearCargoJudicial();

		if (!isPrimeiroGrau) {
			sortearOrgaoJulgadorColegiado();
		}

		processaConfiguracoesRevisor();
		
		atualizarAcumuladorCargoJudicial(this.orgaoJulgadorCargoSorteado, this.orgaoJulgadorCargoAnterior);

		EntityUtil.getEntityManager().persist(logDist);

		this.processoTrf.setProcessoStatus(ProcessoStatusEnum.D);
		this.processoTrf.setDataDistribuicao(new Date());
		
		verificarClasseSigilosa();
		
		criarVinculacaoCadeiaEleitoral();
		
		ajustarFluxo(this.processoTrf.getIdProcessoTrf());
		
		ComponentUtil.getProcessoMagistradoManager().desativarVinculacoes(this.processoTrf);		
		
		Events.instance().raiseEvent(ProcessoTrfHome.EVENT_PROCESSO_DISTRIBUIDO, this.processoTrf);
	}

	private void avaliarConflitoCompetencia() throws PJeBusinessException {
		if (this.getCompetenciaConflito() == null) {
			/*
			 * A supressão dessas verificações é feita em razão de a chamada à distribuição, nos casos
			 * pertinentes, ser precedida de chamada à DefinicaoCompetenciaService, que está integralmente
			 * conforme a especificação.
			 * O método buscarOrgaosDimensaoTipoPessoa está com falha quanto à correta identificação da
			 * competência.
			 */
			// Dimensão Alçada
			buscarOrgaosCompetenciaAlcada();

			// Dimensão Funcional
			buscarOrgaosDimensaoFuncional();

			// Dimensão Pessoal (pessoa)
			buscarOrgaosDimensaoPessoa();

			// Dimensão Pessoal (tipo de pessoa)
			buscarOrgaosDimensaoTipoPessoa();
			
			if (this.processoTrfRedistribuicao != null && !isPrimeiroGrau) {
				this.competenciaConflito = recuperarCompetenciaProcesso(this.processoTrf);

				if (this.competenciaConflito == null) {
					throw new PJeBusinessException("Não há competência ativa para a classe judicial e assuntos.");
				}
			}
			return;
		}
		
		this.processoTrf.setCompetencia(getCompetenciaConflito());
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format("Competência definida: %s", getCompetenciaConflito().getCompetencia()));
	}	
	
	private void avaliarRedistribuicao() throws Exception {
		if (this.processoTrfRedistribuicao != null) {
			this.processoTrf = recarregaParaEvitarLazyInitializationException(processoTrf.getIdProcessoTrf());
			this.processoTrf.getProcessoParteList();
			
			if (processoTrfRedistribuicao.getProcessoTrf().getIdProcessoTrf() != 0) {
				ProcessoTrf processoTrfRed = recarregaParaEvitarLazyInitializationException(processoTrfRedistribuicao.getProcessoTrf().getIdProcessoTrf());
				processoTrfRed.getAssuntoTrfList();
				processoTrfRedistribuicao.setProcessoTrf(processoTrfRed);
			}		

			logDist.setInTipoDistribuicao(this.processoTrfRedistribuicao.getInTipoDistribuicao());
			
			if (processoTrfRedistribuicao.getJurisdicao() != null) {
				processoTrf.setJurisdicao(processoTrfRedistribuicao.getJurisdicao());
			}

		 	avaliaSeRedistribuicaoEhPorEncaminhamentoDependenciaPrevencaoOuSucessao();
			avaliaSeRedistribuicaoEhPorAfastamentoImpedimentoSuspeicaoOuPosseCargoDiretivo();
			avaliaSeRedistribuicaoEhPorDesaforamentoOuIncompetencia();
			processarImpedimentoOrgaoJulgadorColegiado();
			avaliaOutrosTiposDeRedistribuicao();
			
			if (!isPrimeiroGrau) {
				removerOjPlantonista();
			}
			
			adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
				"Redistribuído por %s. %n Órgãos judiciais selecionados após exclusão dos impedidos: %s", 
					this.processoTrfRedistribuicao.getInTipoRedistribuicao().getLabel(), listarOrgaos()));
		}
	}

	private ProcessoTrf recarregaParaEvitarLazyInitializationException(Integer idProcesso) {
		//[PJEVII-4876] Reload na entidade para resolver erro LazyInitializationException
		return ComponentUtil.getComponent(ProcessoTrfDAO.class).find(ProcessoTrf.class, idProcesso);
	}
	
	private void avaliaSeRedistribuicaoEhPorEncaminhamentoDependenciaPrevencaoOuSucessao() {
		if (TipoDistribuicaoEnum.EN.equals(this.processoTrfRedistribuicao.getInTipoDistribuicao()) ||
				TipoDistribuicaoEnum.PD.equals(this.processoTrfRedistribuicao.getInTipoDistribuicao()) ||
				TipoDistribuicaoEnum.PP.equals(this.processoTrfRedistribuicao.getInTipoDistribuicao()) ||
				TipoDistribuicaoEnum.Z.equals(this.processoTrfRedistribuicao.getInTipoDistribuicao())) {
			/*
			 * Caso exista algum órgão julgador selecionado, o sistema irá realizar o direcionamento do processo para este órgão. 
			 * Caso contrário, o sistema deverá realizar o sorteio entre os órgãos julgadores recuperados de acordo com a jurisdição,
			 * órgão julgador colegiado (para processos de segunda instância) e competência do processo.
			 */
			if (this.processoTrfRedistribuicao.getOrgaoJulgador() != null) {
				orgaoJulgadorList.clear();
				orgaoJulgadorList.add(processoTrfRedistribuicao.getOrgaoJulgador());
			} else {
				OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent(OrgaoJulgadorManager.NAME);
				orgaoJulgadorList = orgaoJulgadorManager.obterAtivos(
					processoTrfRedistribuicao.getJurisdicao(), processoTrfRedistribuicao.getOrgaoJulgadorColegiado(), competenciaConflito);
				orgaoJulgadorList.remove(processoTrf.getOrgaoJulgador());
			}
			orgaoJulgadorColegiadoList.clear();
			orgaoJulgadorColegiadoList.add(processoTrfRedistribuicao.getOrgaoJulgadorColegiado());
		}
	}	
	
	private void avaliaSeRedistribuicaoEhPorAfastamentoImpedimentoSuspeicaoOuPosseCargoDiretivo() throws Exception {
		if (TipoRedistribuicaoEnum.A.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.I.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.K.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.N.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.O.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.S.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.T.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao())) {
			orgaoJulgadorCargoExcluidoList.add(this.processoTrf.getOrgaoJulgadorCargo());
			excluiPresidenteRelatorDeProcessoEmRedistribuicaoJE();

			String tiposRedistribuicao = null;
			
			if (isPrimeiroGrau) {
				tiposRedistribuicao = "'I','S'";
			} else {
				tiposRedistribuicao = "'A','K','N','O','T'";
			}
			
			this.orgaoJulgadorImpedidoList = ComponentUtil.getComponent(ProcessoTrfRedistribuicaoManager.class).recuperar(processoTrf, tiposRedistribuicao);
			avaliaSeRedistribuicaoEhPorImpedimentoOuSuspeicaoECompetenciaExclusiva();
			avaliaSeRedistribuicaoEhPorAfastamentoImpedimentoSuspeicaoOuCargoDiretivo();
		}
	}
	
	private void avaliaSeRedistribuicaoEhPorImpedimentoOuSuspeicaoECompetenciaExclusiva() {
		if (TipoRedistribuicaoEnum.I.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) || 
				TipoRedistribuicaoEnum.S.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.N.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.O.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ) {
			this.orgaoJulgadorImpedidoList.add(this.processoTrf.getOrgaoJulgador());
			
			if (this.processoTrfRedistribuicao.getOrgaoJulgador() != null && 
					TipoDistribuicaoEnum.CE.equals(this.processoTrfRedistribuicao.getInTipoDistribuicao())) {
				orgaoJulgadorList.clear();
				orgaoJulgadorList.add(processoTrfRedistribuicao.getOrgaoJulgador());
			}
		}
	}	

	private void avaliaSeRedistribuicaoEhPorAfastamentoImpedimentoSuspeicaoOuCargoDiretivo() throws Exception {
		if (isPrimeiroGrau) {
			removerOrgaosImpedidosSorteio();
			return;
		} 
		
		if (TipoRedistribuicaoEnum.A.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.K.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.N.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.O.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.T.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao())) {
			OrgaoJulgadorColegiado ojc = this.processoTrf.getOrgaoJulgadorColegiado();
			this.orgaoJulgadorColegiadoList.clear();
			this.orgaoJulgadorColegiadoList.add(ojc);
			List<OrgaoJulgadorColegiadoOrgaoJulgador> ojcOjsList = OrgaoJulgadorColegiadoOrgaoJulgadorManager.instance().obterAtivos(ojc);
			avaliaOrgaoJulgadoresColegiadosDaRedistribuicao(ojcOjsList);
		}
	}

	private void avaliaOrgaoJulgadoresColegiadosDaRedistribuicao(List<OrgaoJulgadorColegiadoOrgaoJulgador> ojcOjsList) throws Exception {
		if (!ojcOjsList.isEmpty()) {
			this.orgaoJulgadorList.clear();
			buscarOrgaosCompetenciaAtiva(ojcOjsList);
			removerOrgaosImpedidosSorteio();
		} 
		
		if (ojcOjsList.isEmpty() || this.orgaoJulgadorList.isEmpty()) {
			this.orgaoJulgadorColegiadoList = this.orgaoJulgadorColegiadoPorCompetenciaList;
			this.orgaoJulgadorList.clear();
			
			for (OrgaoJulgadorColegiado ojcs : this.orgaoJulgadorColegiadoList) {
				List<OrgaoJulgadorColegiadoOrgaoJulgador> ojcsOjsList = OrgaoJulgadorColegiadoOrgaoJulgadorManager.instance().obterAtivos(ojcs);
				buscarOrgaosCompetenciaAtiva(ojcsOjsList);
				removerOrgaosImpedidosSorteio();
			}
		}
	}

	private void avaliaSeRedistribuicaoEhPorDesaforamentoOuIncompetencia() throws Exception {
		if (TipoRedistribuicaoEnum.D.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.R.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao())) {
			this.orgaoJulgadorList.clear();
			processoTrf.setJurisdicao(((ProcessoTrfRedistribuicaoHome) Component.getInstance("processoTrfRedistribuicaoHome")).getJurisdicaoRedistribuicao());
			if (isPrimeiroGrau) {
				OrgaoJulgadorCargoManager ojcm = (OrgaoJulgadorCargoManager) Component.getInstance("orgaoJulgadorCargoManager");
				List<OrgaoJulgadorCargo> cargosCompetentes = ojcm.recuperaCompetentes(processoTrf.getJurisdicao(), getCompetenciaConflito());
				avaliaOrgaoJulgadorPorDesaforamentoOuIncompetencia(cargosCompetentes);
				return;
			}

			if (TipoRedistribuicaoEnum.R.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao())) {
				this.orgaoJulgadorColegiadoList = this.orgaoJulgadorColegiadoPorCompetenciaList;
				this.orgaoJulgadorList.clear();
				
				for (OrgaoJulgadorColegiado ojcs : this.orgaoJulgadorColegiadoList) {
					List<OrgaoJulgadorColegiadoOrgaoJulgador> ojcsOjsList = OrgaoJulgadorColegiadoOrgaoJulgadorManager.instance().obterAtivos(ojcs);
					buscarOrgaosCompetenciaAtiva(ojcsOjsList);
				}
			}
		}
	}

	private void avaliaOrgaoJulgadorPorDesaforamentoOuIncompetencia(List<OrgaoJulgadorCargo> cargosCompetentes) {
		for (OrgaoJulgadorCargo cj : cargosCompetentes) {
			if (!cj.equals(processoTrf.getOrgaoJulgadorCargo())	&& !orgaoJulgadorList.contains(cj.getOrgaoJulgador())) {
				orgaoJulgadorList.add(cj.getOrgaoJulgador());
			}
		}
	}
	
	/**
	 * Quando a aplicacao for do segundo grau e o tipo de redistribuicao for Impedimento de orgao Julgador Colegiado
	 * Deve-se entao impedir todos os orgao julgadores pertencentes ao orgao julgador colegiado atual do processo 
	 */
	private void processarImpedimentoOrgaoJulgadorColegiado() {
		if (TipoRedistribuicaoEnum.Y.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao())) {
			OrgaoJulgadorColegiado ojc = this.processoTrf.getOrgaoJulgadorColegiado();
											
			List<OrgaoJulgadorColegiadoOrgaoJulgador> orgaoJulgadoresDoColegiado = 
					OrgaoJulgadorColegiadoOrgaoJulgadorManager.instance().obterAtivos(ojc);
			
			for (OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorDoColegiado : orgaoJulgadoresDoColegiado) {
				orgaoJulgadorImpedidoList.add(orgaoJulgadorDoColegiado.getOrgaoJulgador());
				orgaoJulgadorList.remove(orgaoJulgadorDoColegiado.getOrgaoJulgador());
			}
		}
	}	
	
	private void avaliaOutrosTiposDeRedistribuicao() throws Exception {
		if (this.processoTrfRedistribuicao.getInTipoDistribuicao() != null) {
			if (TipoDistribuicaoEnum.CE.equals(this.processoTrfRedistribuicao.getInTipoDistribuicao())) {
				avaliaSeRedistribuicaoEhPorCompetenciaExclusivaErroMaterialCriacaoUnidadeExtincaoOuRecusa();
				return;
			}  
				
			if (TipoDistribuicaoEnum.S.equals(this.processoTrfRedistribuicao.getInTipoDistribuicao())) {
				avaliaSeRedistribuicaoEhPorAlteracaoCompetenciaDeterminacaoJudicialErroMaterialCriacaoUnidadeExtincaoOuRecusa();
			}
		}
	}

	private void avaliaSeRedistribuicaoEhPorCompetenciaExclusivaErroMaterialCriacaoUnidadeExtincaoOuRecusa() throws Exception {
		if (TipoRedistribuicaoEnum.M.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.U.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.X.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.W.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao())) {
			if (isPrimeiroGrau) {
				if (!TipoRedistribuicaoEnum.C.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao())) {
					this.orgaoJulgadorList.clear();
					this.orgaoJulgadorList.add(this.processoTrf.getOrgaoJulgador());
				}
				return;
			} 
			
			this.orgaoJulgadorColegiadoList = this.orgaoJulgadorColegiadoPorCompetenciaList;
			OrgaoJulgadorColegiado ojc = this.processoTrf.getOrgaoJulgadorColegiado();
			
			if (!TipoRedistribuicaoEnum.W.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) && this.orgaoJulgadorColegiadoList.contains(ojc)) {
				this.orgaoJulgadorColegiadoList.remove(ojc);
			}
			
			this.orgaoJulgadorList.clear();
			for (OrgaoJulgadorColegiado ojcs : this.orgaoJulgadorColegiadoList) {
				List<OrgaoJulgadorColegiadoOrgaoJulgador> ojcsOjsList =	OrgaoJulgadorColegiadoOrgaoJulgadorManager.instance().obterAtivos(ojcs);
				buscarOrgaosCompetenciaAtiva(ojcsOjsList);
			}							
		}
	}	
	
	private void avaliaSeRedistribuicaoEhPorAlteracaoCompetenciaDeterminacaoJudicialErroMaterialCriacaoUnidadeExtincaoOuRecusa() throws Exception {
		if (TipoRedistribuicaoEnum.C.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.J.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.M.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.U.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.X.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
				TipoRedistribuicaoEnum.W.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao())) {
			
			if (isPrimeiroGrau) {
				OrgaoJulgador orgaoJulgador = this.processoTrfRedistribuicao.getOrgaoJulgadorAnterior();
				this.orgaoJulgadorList.remove(orgaoJulgador);
				return;
			} 
			
			OrgaoJulgadorColegiado ojc = this.processoTrf.getOrgaoJulgadorColegiado();
			this.orgaoJulgadorColegiadoList = this.orgaoJulgadorColegiadoPorCompetenciaList;
			
			if (TipoRedistribuicaoEnum.J.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) || 
					TipoRedistribuicaoEnum.W.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao())) {
				if (!this.orgaoJulgadorColegiadoList.contains(ojc)) {
					this.orgaoJulgadorColegiadoList.add(ojc);
				}
			}
			
			this.orgaoJulgadorList.clear();
			
			for (OrgaoJulgadorColegiado ojcs : this.orgaoJulgadorColegiadoList) {
				List<OrgaoJulgadorColegiadoOrgaoJulgador> ojcsOjsList = OrgaoJulgadorColegiadoOrgaoJulgadorManager.instance().obterAtivos(ojcs);
				buscarOrgaosCompetenciaAtiva(ojcsOjsList);
			}
		}
	}
	
	private void verificarListaOrgaoJulgador() throws PJeBusinessException {
		if (orgaoJulgadorList.isEmpty()) {
			throw new PJeBusinessException(String.format("%s %s", 
				FacesUtil.getMessage("distribuicaoService.erroSemOrgaosJulgadoresDisponiveis"), 
					FacesUtil.getMessage("distribuicaoService.orientacoesCasoHouverErro")));
		}
	}
	
	/**
 	 * Se for justia eleitoral, e o processo em distribuio se enquadrar no agrupamento PEO de classes de assuntos da preveno art 260, criar o vinculo.
 	 * 
 	 * @throws PJeBusinessException
 	 */
 	private void criarVinculacaoCadeiaEleitoral() throws PJeBusinessException {
 		if (isJEeParametroPrevencaoAtivo()) {
 			boolean processoEnquadra260 = verificarEnquadramento(this.processoTrf);
 			if (processoEnquadra260) {
 				if(this.processoTrf != null){
 					this.vinculacaoDependenciaEleitoral =  criarOuRecuperarVinculoDependenciaEleitoral(this.processoTrf);
 				} else if (this.processoTrfRedistribuicao != null){
 					this.vinculacaoDependenciaEleitoral =  criarOuRecuperarVinculoDependenciaEleitoral(this.processoTrfRedistribuicao.getProcessoTrf());
 				}
 				
 				if(this.vinculacaoDependenciaEleitoral != null){
 					definirVinculoDependenciaEleitoralProcessoEmDistribuicao(this.vinculacaoDependenciaEleitoral,this.processoTrf);
 				}
 			}
		}
 	}

	private void ajustarFluxo(Integer idProcessoTrf){
		Integer idLocalizacaoAnterior = null;
		Integer idOrgaoJulgadorCargoAnterior = null;
		Integer idOrgaoJulgadorColegiadoAnterior = null;
		if(this.orgaoJulgadorCargoAnterior != null){
			idLocalizacaoAnterior = this.orgaoJulgadorCargoAnterior.getOrgaoJulgador().getLocalizacao().getIdLocalizacao();
			idOrgaoJulgadorCargoAnterior = this.orgaoJulgadorCargoAnterior.getIdOrgaoJulgadorCargo();
		}
		if(this.orgaoJulgadorColegiadoAnterior != null){
			idOrgaoJulgadorColegiadoAnterior = this.orgaoJulgadorColegiadoAnterior.getIdOrgaoJulgadorColegiado();
		}
		ComponentUtil.getProcessoJudicialManager().ajustarFluxo(idProcessoTrf,idLocalizacaoAnterior,idOrgaoJulgadorCargoAnterior,idOrgaoJulgadorColegiadoAnterior);
	}

	/**
	 * Processa as definições de revisor para o processo copiando as informações
	 * definidas na classe judicial e setando no processo de acordo com a
	 * instância processual
	 * 
	 * @throws Exception
	 */
	private void processaConfiguracoesRevisor() throws Exception {
		if (!isPrimeiroGrau) {
			// Verifica a configuracao da classe em relacao ao revisor 
			if (SimNaoFacultativoEnum.S.equals(processoTrf.getClasseJudicial().getExigeRevisor())) {
				processoTrf.setExigeRevisor(true);
				OrgaoJulgadorColegiadoOrgaoJulgador ojcOrgaoJulgador = recuperarOrgaoJulgadorRevisorPadrao();
				processoTrf.setOrgaoJulgadorRevisor(ojcOrgaoJulgador.getOrgaoJulgadorRevisor().getOrgaoJulgador());
				
			} else if (SimNaoFacultativoEnum.N.equals(processoTrf.getClasseJudicial().getExigeRevisor())) {
				processoTrf.setExigeRevisor(false);
				
			// Verifica se ja foi definido se o processo exige o revisor
			} else if (SimNaoFacultativoEnum.F.equals(processoTrf.getClasseJudicial().getExigeRevisor()) &&
					processoTrf.getExigeRevisor() == Boolean.TRUE) {
				OrgaoJulgadorColegiadoOrgaoJulgador ojcOrgaoJulgador = recuperarOrgaoJulgadorRevisorPadrao();
				processoTrf.setOrgaoJulgadorRevisor(ojcOrgaoJulgador.getOrgaoJulgadorRevisor().getOrgaoJulgador());
			}

			// Verifica a configuracao da classe em relacao a composicao de julgamento
			if (ClasseComposicaoJulgamentoEnum.I.equals(processoTrf.getClasseJudicial().getComposicaoJulgamento())) {
				processoTrf.setComposicaoJulgamento(ComposicaoJulgamentoEnum.I);
				
			} else if (ClasseComposicaoJulgamentoEnum.R.equals(processoTrf.getClasseJudicial().getComposicaoJulgamento())) {
				processoTrf.setComposicaoJulgamento(ComposicaoJulgamentoEnum.R);
			}
		} else {
			if (SimNaoFacultativoEnum.S.equals(processoTrf.getClasseJudicial().getExigeRevisor())) {
				processoTrf.setExigeRevisor(true);
			} else {
				processoTrf.setExigeRevisor(false);
			}
		}
	}

	private OrgaoJulgadorColegiadoOrgaoJulgador recuperarOrgaoJulgadorRevisorPadrao() throws Exception {
		OrgaoJulgadorColegiadoOrgaoJulgador ojcOrgaoJulgador = OrgaoJulgadorColegiadoOrgaoJulgadorManager.instance()
			.recuperarPorOrgaoJulgadorColegiadoEhOrgaoJulgador(processoTrf.getOrgaoJulgadorColegiado(), processoTrf.getOrgaoJulgador());
		
		if (ojcOrgaoJulgador == null || ojcOrgaoJulgador.getOrgaoJulgadorRevisor() == null) {
			throw new Exception(MessageFormat.format("Não foi possível recuperar o cadastro do Orgão Julgador Revisor para o Orgão Julgador (id:{0}, nome:{1}) no Colegiado (id:{2}, nome:{3})!"
					, processoTrf.getOrgaoJulgador().getIdOrgaoJulgador()
					, processoTrf.getOrgaoJulgador().getOrgaoJulgador()
					, processoTrf.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado()
					, processoTrf.getOrgaoJulgadorColegiado().getOrgaoJulgadorColegiado()
			));
		}
		
		return ojcOrgaoJulgador;
	}
	
	/**
	 * Na justiça Eleitoral, quem faz a redistribuição
	 * pode ser o presidente (indicado pelo parametro
	 * 'pje:redistribuicao:presidenteRelatorProcessoEmRedistribuicao'), caso
	 * seja, ele não pode ser gravado como o ultimo relator do processo pois o
	 * processo fica em posse dele apenas para se realizar a redistribuição.
	 * Portanto o relator original (quem solicitou a redistribuição ao
	 * presidente, dependendo do fluxo e do parametro mencionado definido como
	 * 'true') deve ser o orgão julgador anterior da redistribuição. Isto
	 * impacta nas regras de sorteio e gravação de movimentação processual, onde
	 * neste caso tanto o relator original, quanto o presidente (que fica em
	 * posse do processo temporariamente) não são inclusos como orgão julgadores
	 * elegiveis para o sorteio de redistribuição.
	 * 
	 * @exception PJeBusinessException
	 **/
	private void excluiPresidenteRelatorDeProcessoEmRedistribuicaoJE() throws PJeBusinessException {
		if (ParametroJtUtil.instance().justicaEleitoral()
				&&	Boolean.getBoolean(ParametroUtil.getParametro(Parametros.PRESIDENTE_RELATOR_PROCESSO_EM_REDISTRIBUICAO))) {

			HistoricoDeslocamentoOrgaoJulgador ultimo = ComponentUtil.getHistoricoDeslocamentoOrgaoJulgadorManager().obterHistoricoSemDataRetorno(this.processoTrf);
			if (ultimo != null) {
				OrgaoJulgadorCargo orgaoJulgadorCargo = ultimo.getOrgaoJulgadorCargoOrigem();
				if (orgaoJulgadorCargo != null) {
					for (Competencia competencia : orgaoJulgadorCargo.getCompetencia()) {
						if(CompetenciaEnum.EXCLUSIVA_PRESIDENCIA.getLabel().equalsIgnoreCase(competencia.getCompetencia())){
							orgaoJulgadorCargoExcluidoList.add(ultimo.getOrgaoJulgadorCargoOrigem());
							break;
						}
					}	
				}
			}
		}
	}

	private void sortearOrgaoJulgadorColegiado() throws Exception {
		List<OrgaoJulgadorColegiadoOrgaoJulgador> ojcojList = 
				OrgaoJulgadorColegiadoOrgaoJulgadorManager.instance().obterAtivos(this.orgaoJulgadorCargoSorteado.getOrgaoJulgador());
		
		if (this.competenciaConflito != null && this.processoTrfRedistribuicao != null && 
				!(TipoDistribuicaoEnum.EN.equals(this.processoTrfRedistribuicao.getInTipoDistribuicao()) ||
						TipoRedistribuicaoEnum.P.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()))) {
			
			this.orgaoJulgadorColegiadoList.clear();
			buscarOrgaosColegiadosCompetenciaAtiva(ojcojList);
		}
		List<OrgaoJulgadorColegiado> ojcTemp = new ArrayList<OrgaoJulgadorColegiado>();
		if (ojcojList.size() > 0) {
			for (OrgaoJulgadorColegiadoOrgaoJulgador ojcoj : ojcojList) {
				if (this.orgaoJulgadorColegiadoList.contains(ojcoj.getOrgaoJulgadorColegiado())) {
					ojcTemp.add(ojcoj.getOrgaoJulgadorColegiado());
				}
			}

			int x = 0;
			if (ojcTemp.size() > 1) {
				SecureRandom random = new SecureRandom();
				x = random.nextInt(ojcTemp.size() - 1);
			}

			if (ojcTemp.size() > 0) {
				this.orgaoJulgadorColegiadoAnterior = this.processoTrf.getOrgaoJulgadorColegiado();

				OrgaoJulgadorColegiado orgaoJulgadorColegiadoSorteado = ojcTemp.get(x);
				this.processoTrf.setOrgaoJulgadorColegiado(orgaoJulgadorColegiadoSorteado);
				this.logDist.setOrgaoJulgadorColegiado(orgaoJulgadorColegiadoSorteado);
				
				adicionarLog(this.logDist, CriticidadeEnum.I, String.format("%s: %s", 
					this.processoTrf.getIsIncidente() ? "Órgão julgador colegiado originário" : "Órgão julgador colegiado sorteado",
							orgaoJulgadorColegiadoSorteado.getOrgaoJulgadorColegiado()));
				
				adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
					"Quantidade de participantes no sorteio: %s. Mínimo permitido: %s", orgaoJulgadorList.size(), 
						processoTrf.getOrgaoJulgadorColegiado().getMinimoParticipanteDistribuicao()));
				
			} else {
				throw new Exception(String.format(
					"A competência não está configurada corretamente ou não há órgão julgador colegiado competente vinculado ao cargo %s.",
						this.orgaoJulgadorCargoSorteado.getCargo()));				
			}			
			
		} else {
			throw new Exception(String.format(
				"Não há órgão julgador colegiado competente vinculado ao cargo %s.",
					this.orgaoJulgadorCargoSorteado.getCargo()));
		}
	}

	private void unificarOrgaosJulgadores() throws PJeBusinessException {
		List<OrgaoJulgadorCargo> cargosCompetentes = ComponentUtil.getOrgaoJulgadorCargoManager().recuperaCompetentes(
				this.processoTrf.getJurisdicao(), recuperarCompetenciaProcesso(this.processoTrf));
		
		List<OrgaoJulgador> orgaosJulgadoresCompetentes = new ArrayList<OrgaoJulgador>(0);
		for(OrgaoJulgadorCargo cargo: cargosCompetentes){
			orgaosJulgadoresCompetentes.add(cargo.getOrgaoJulgador());
		}
		
		for (OrgaoJulgadorColegiado ojc : this.orgaoJulgadorColegiadoList) {
			List<OrgaoJulgadorColegiadoOrgaoJulgador> ojcojList = OrgaoJulgadorColegiadoOrgaoJulgadorManager.instance().obterAtivos(ojc);
			for (OrgaoJulgadorColegiadoOrgaoJulgador ojcoj : ojcojList) {
				OrgaoJulgador oj = ojcoj.getOrgaoJulgador();
				if (!this.orgaoJulgadorList.contains(oj) && orgaosJulgadoresCompetentes.contains(oj)) {
					this.orgaoJulgadorList.add(oj);
				}
			}
		}
	}

	private void excluirOrgaosCompetenciaConflitante() {
		List<OrgaoJulgador> orgaoJulgadorTempList = new ArrayList<OrgaoJulgador>(0);
		for (OrgaoJulgador o : orgaoJulgadorList) {
			for (OrgaoJulgadorCompetencia ojc : o.getOrgaoJulgadorCompetenciaAtivoList()) {
				if (ojc.getCompetencia().equals(this.getCompetenciaConflito())) {
					orgaoJulgadorTempList.add(o);
					break;
				}
			}
		}
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
			"Conflito de competência \n Competência selecionada pelo usuário: %s \n Órgãos judiciais selecionados: %s", 
				this.getCompetenciaConflito(), listarOrgaos()));
		
	}
	
	/**
	 *
     * Processos incidentais devem marcar a flag isIncidente de ProcessoTrf, que passou a ser persistida.
     * Nao ha mais necessidade de verificar se h um processo referncia. Se a flag foi marcada  porque o processo  incidental.
     * O tratamento do processo referncia  feito em distribuirIncidental().
	 * @throws Exception
	 */
	private void distribuirIncidental() throws Exception {
		log.info(String.format("Distribuir o processo [%d] de forma incidental", processoTrf.getIdProcessoTrf()));
		//O processo referência está no PJE
		if (this.processoTrf.getProcessoReferencia() != null) {
			//O órgão julgador colegiado do processo incidental deve ser o mesmo do processo referência
			if (!isPrimeiroGrau) {
				this.orgaoJulgadorColegiadoAnterior = this.processoTrf.getOrgaoJulgadorColegiado();
				this.processoTrf.setOrgaoJulgadorColegiado(this.processoTrf.getProcessoReferencia().getOrgaoJulgadorColegiado());
			}
			//O órgão julgador do processo incidental deve ser o mesmo do processo referência
			this.orgaoJulgadorCargoAnterior = this.processoTrf.getOrgaoJulgadorCargo();
			tratarOrgaoJulgadorConformeCadeia260();
		} else {
			//Não é um processo que esteja no PJE e o usuário informou o número do processo referência
			if (this.processoTrf.getDesProcReferencia() != null) {
				//O orgão julgador do processo incidental deve ser o mesmo do processo originário (informado pelo usuário na tela de 'Dados Iniciais')
				orgaoJulgadorList.add(this.processoTrf.getOrgaoJulgador());
				//Caso o usuario selecionar na tela 'Dados Iniciais' o cargo, o processo deve ir para este
				if (this.processoTrf.getOrgaoJulgadorCargo() != null) {
					orgaoJulgadorCargoList.add(this.processoTrf.getOrgaoJulgadorCargo());
				}else {
					buscarCargosElegiveis();
				}
				sortearCargoJudicial();
				this.orgaoJulgadorColegiadoAnterior = this.processoTrf.getOrgaoJulgadorColegiado();
			} else {
				throw new Exception("Deve ser informado o número do processo originário ou o número do processo referência.");
			}
		}
		
		TipoDistribuicaoEnum tipoDistribuicao = TipoDistribuicaoEnum.I;
		if(this.processoTrfRedistribuicao != null) {
			if(this.processoTrfRedistribuicao.getInTipoRedistribuicao() != null) {
				if (TipoRedistribuicaoEnum.J.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) || 
						TipoRedistribuicaoEnum.E.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) ||
						TipoRedistribuicaoEnum.X.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao())) {
					
					if(this.processoTrfRedistribuicao.getInTipoDistribuicao() != null ){
						tipoDistribuicao = this.processoTrfRedistribuicao.getInTipoDistribuicao();
					}
				} else {
					if (TipoRedistribuicaoEnum.P.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao())) {
			                tipoDistribuicao = TipoDistribuicaoEnum.PP;
			        }
				}
			}
			this.processoTrfRedistribuicao.setInTipoDistribuicao(tipoDistribuicao);
		}
		
		logDist.setProcessoTrf(this.processoTrf);
		logDist.setInTipoDistribuicao(tipoDistribuicao);
		logDist.setOrgaoJulgador(this.processoTrf.getOrgaoJulgador());
		logDist.setOrgaoJulgadorCargo(this.processoTrf.getOrgaoJulgadorCargo());
		logDist.setOrgaoJulgadorColegiado(this.processoTrf.getOrgaoJulgadorColegiado());
		
		// Verifica se o processo pertence ao segundo grau da justica estatual
		processaConfiguracoesRevisor();
		criarVinculacaoCadeiaEleitoral();
		atualizarAcumuladorCargoJudicial(this.orgaoJulgadorCargoSorteado, this.orgaoJulgadorCargoAnterior);

		ajustarFluxo(this.processoTrf.getIdProcessoTrf());
		EntityUtil.getEntityManager().persist(logDist);
		this.processoTrf.setProcessoStatus(ProcessoStatusEnum.D);
		this.processoTrf.setDataDistribuicao(new Date());
		verificarClasseSigilosa();
	}
	
	/**
	 * Faz o tratamento dos campos Orgao Julgador e Orgao Julgador Cargo, conforme a existencia de uma cadeia preventa 
	 * pelo artigo 260, com base no processoTrf.
	 * @throws PJeBusinessException Excecao lancada se o processo nao tiver informacao referente ao complemento
	 */
	private void tratarOrgaoJulgadorConformeCadeia260() throws PJeBusinessException {
		VinculacaoDependenciaEleitoral cadeia260 = new VinculacaoDependenciaEleitoral();
		OrgaoJulgador orgaoJulgador = new OrgaoJulgador();
		OrgaoJulgadorCargo orgaoJulgadorCargo = new OrgaoJulgadorCargo();
		
		if (isProcessoEleitoralEnquadradoComParametroPrevencaoAtivo(processoTrf)) {
			cadeia260 = recuperarCadeiaParaEleicaoNaRegiao(processoTrf);
			if (cadeia260 != null) {
				orgaoJulgador = cadeia260.getCargoJudicial().getOrgaoJulgador();
				orgaoJulgadorCargo = cadeia260.getCargoJudicial();
				preencherDadosOrgaoJulgador(orgaoJulgador, orgaoJulgadorCargo);
			} else {
				preencherOrgaoJulgadorConformeProcessoReferencia();
			}
		} else {
			preencherOrgaoJulgadorConformeProcessoReferencia();
		}
	}

	/**
	 * Preenche os dados do Orgao Julgador do processo, conforme os dados do processo Referencia
	 */
	private void preencherOrgaoJulgadorConformeProcessoReferencia() {
		OrgaoJulgador orgaoJulgador = processoTrf.getProcessoReferencia().getOrgaoJulgador();
		OrgaoJulgadorCargo orgaoJulgadorCargo = processoTrf.getProcessoReferencia().getOrgaoJulgadorCargo(); 
		preencherDadosOrgaoJulgador(orgaoJulgador, orgaoJulgadorCargo);
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
	public boolean isProcessoEleitoralEnquadradoComParametroPrevencaoAtivo(ProcessoTrf processoTrf) throws PJeBusinessException {
		return ParametroUtil.instance().isParametroPrevencaoAtivoNaJusticaEleitoral() && verificarEnquadramento(processoTrf);
	}
	
	/**
	 * Recupera a cadeia preventa art. 260 conforme o processoTrf passado pelo parametro.
	 * @param	processoTrf
	 * @return	retorna a cadeia 260 conforme o processo, eleicao e regiao, ou nulo caso nao exista a cadeia preventa 
	 * 			260 para o processo.
	 */
	public VinculacaoDependenciaEleitoral recuperarCadeiaParaEleicaoNaRegiao(ProcessoTrf processoTrf){
		try {
			return ComponentUtil.getComponent(VinculacaoDependenciaEleitoralManager.class).recuperaVinculacaoDependencia(processoTrf.getComplementoJE());
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Preenche os dados dos campos referentes ao Orgao Julgador e ao cargo do processoTrf utilizado na instancia.
	 * @param orgaoJulgador
	 * @param orgaoJulgadorCargo
	 */
	private void preencherDadosOrgaoJulgador(OrgaoJulgador orgaoJulgador, OrgaoJulgadorCargo orgaoJulgadorCargo) {
		processoTrf.setOrgaoJulgador(orgaoJulgador);
		processoTrf.setOrgaoJulgadorCargo(orgaoJulgadorCargo);
		orgaoJulgadorCargoSorteado = orgaoJulgadorCargo;
	}
	
	private void buscarOrgaosJurisdicao() {
		Jurisdicao jurisdicao = this.processoTrf.getJurisdicao();
		if (this.processoTrfRedistribuicao != null && this.processoTrfRedistribuicao.getJurisdicao() != null) {
			 jurisdicao = this.processoTrfRedistribuicao.getJurisdicao();
		}

		// recupera OJs da jurisdição
		this.orgaoJulgadorList = ComponentUtil.getComponent(OrgaoJulgadorManager.class).findAllbyJurisdicao(jurisdicao);
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
			"Dimensão espacial \n Jurisdição: %s \n Órgãos judiciais selecionados: %s", 
				jurisdicao.getJurisdicao(), listarOrgaos()));
		
		// recupera OJCs da jurisdição
		if (!this.isPrimeiroGrau) {
			this.orgaoJulgadorColegiadoList = ComponentUtil.getComponent(OrgaoJulgadorColegiadoManager.class)
					.getColegiadosByJurisdicao(jurisdicao);
			
			adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
				"Dimensão espacial \n Jurisdição: %s \n Órgãos judiciais colegiados selecionados: %s", 
					jurisdicao.getJurisdicao(), listarOrgaosColegiados()));
		}
	}
		
	/**
	 * Filtra os OJs e OJCs (OJCs apenas quando não é 1G) a partir da competência indicada para a distribuição, 
	 * se não houver competência indicada, busca a partir da lista de classes e assuntos indicados.
	 */
	private void buscarOrgaosCompetencia() {
		List<OrgaoJulgador> ojsCompetencia = new ArrayList<OrgaoJulgador>(0);
		List<OrgaoJulgadorColegiado> ojcsCompetencia = new ArrayList<OrgaoJulgadorColegiado>(0);
		
		if (!this.isOrgaoJulgadorListVazia() && (this.isPrimeiroGrau || !this.isOrgaoJulgadorColegiadoListVazia())) {
			if(this.competenciaConflito != null) {
				ojsCompetencia = ComponentUtil.getComponent(OrgaoJulgadorManager.class).findAllbyCompetencia(this.competenciaConflito);
				if (!this.isPrimeiroGrau) {
					ojcsCompetencia = ComponentUtil.getComponent(OrgaoJulgadorColegiadoManager.class).getColegiadosByCompetencia(this.competenciaConflito);
				}
			} else {
				this.recuperaAssuntosProcesso();
				if(!this.isProcessoAssuntoListVazia()) {
					ojsCompetencia = ComponentUtil.getComponent(OrgaoJulgadorManager.class).findAllbyClasseAssunto(this.processoTrf.getClasseJudicial(), this.getAssuntoTrfList());
					if (!this.isPrimeiroGrau) {
						ojcsCompetencia = ComponentUtil.getComponent(OrgaoJulgadorColegiadoManager.class).getColegiadosByClasseAssunto(this.processoTrf.getClasseJudicial(), this.getAssuntoTrfList());
					}
				}
			}
		}
		
		// Interseçao da lista de órgãos julgadores da competencia com a lista de órgãos julgadores tratado pela distribuicao.	
		this.orgaoJulgadorList = ojsCompetencia.stream().filter(p -> this.orgaoJulgadorList.contains(p)).collect(Collectors.toList());
		
		// Interseçao da lista de órgãos julgadores colegiados da competência com a lista de órgãos julgadores colegiados tratado pela distribuicao.
		this.orgaoJulgadorColegiadoList = ojcsCompetencia.stream().filter(p -> this.orgaoJulgadorColegiadoList.contains(p)).collect(Collectors.toList());
		
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
				"Dimensão material e procedimental \n Órgãos judiciais selecionados: %s", listarOrgaos()));
		
		if (!this.isPrimeiroGrau) {
			this.orgaoJulgadorColegiadoPorCompetenciaList = orgaoJulgadorColegiadoList.stream().collect(Collectors.toList());
			
			adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
					"Dimensão material e procedimental \n Órgãos judiciais colegiados selecionados: %s", listarOrgaosColegiados()));
		}
	}
	
	@SuppressWarnings("unchecked")
	private void buscarOrgaosCompetenciaAlcada() {
		if (this.processoTrf.getValorCausa() != null && this.processoTrf.getValorCausa().doubleValue() > 0.0
				&& this.orgaoJulgadorList.size() > 0) {

			EntityManager em = EntityUtil.getEntityManager();
			StringBuilder sql = new StringBuilder();
			sql.append("select distinct org from OrgaoJulgador org ")
					.append("inner join org.orgaoJulgadorCompetenciaList ojc ")
					.append("inner join ojc.competencia comp ")
					.append("left join comp.dimensaoAlcada alc ")
					.append("where org in (:orgaosJulgadores) ")
					.append("and ((ojc.dataFim is null and ojc.dataInicio <= :dataAtual)) or (ojc.dataFim != null and :dataAtual between ojc.dataInicio and ojc.dataFim))");

			Query query = em.createQuery(sql.toString());
			query.setParameter("orgaosJulgadores", orgaoJulgadorList);
			query.setParameter("dataAtual", this.dataAtual);
			this.orgaoJulgadorList = query.getResultList();
			
			adicionarLog(this.logDist, CriticidadeEnum.I, String.format("Dimensão alçada \n Órgãos judiciais selecionados: %s", listarOrgaos()));
			
			if (!isPrimeiroGrau) {
				buscarOrgaosColegiadosCompetenciaAlcada();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void buscarOrgaosColegiadosCompetenciaAlcada() {
		if (this.processoTrf.getValorCausa() != null && this.processoTrf.getValorCausa().doubleValue() > 0.0
				&& this.orgaoJulgadorColegiadoList.size() > 0) {

			EntityManager em = EntityUtil.getEntityManager();
			StringBuilder sql = new StringBuilder();

			sql.append("select distinct ojc ")
				.append("from OrgaoJulgadorColegiado ojc ")
				.append("inner join ojc.orgaoJulgadorColegiadoCompetenciaList ojcl ")
				.append("inner join ojcl.competencia com ")
				.append("left join com.dimensaoAlcada alc ")
				.append("where ojc in (:orgaosColegiados) ")
				.append("and (alc is null or (:valorProcesso between alc.intervaloInicial and alc.intervaloFinal)) ")
				.append("and ((ojcl.dataFim is null and ojcl.dataInicio <= :dataAtual)) or ")
				.append("(ojcl.dataFim != null and :dataAtual between ojcl.dataInicio and ojcl.dataFim))");

			Query query = em.createQuery(sql.toString());
			query.setParameter("orgaosColegiados", this.orgaoJulgadorColegiadoList);
			query.setParameter("valorProcesso", this.processoTrf.getValorCausa());
			query.setParameter("dataAtual", this.dataAtual);
			this.orgaoJulgadorColegiadoList = query.getResultList();
			
			adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
					"Dimensão alçada \n Órgãos colegiados selecionados: %s", listarOrgaosColegiados()));
		}
	}

	private void buscarOrgaosDimensaoFuncional() {
		List<OrgaoJulgador> orgaoJulgadorTempList = new ArrayList<OrgaoJulgador>(0);

		for (OrgaoJulgador orgao : this.orgaoJulgadorList) {
			if (existeDimensaoFuncional(orgao)) {
				if (!isDimensaoFuncionalAplicavel(orgao, this.processoTrf)) {
					continue;
				}
			}
			orgaoJulgadorTempList.add(orgao);
		}
		this.orgaoJulgadorList = orgaoJulgadorTempList;
		
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
				"Dimensão funcional \n Órgãos judiciais selecionados: %s", listarOrgaos()));
		
		if (!isPrimeiroGrau) {
			buscarOrgaosColegiadosDimensaoFuncional();
		}
	}

	private void buscarOrgaosColegiadosDimensaoFuncional() {
		List<OrgaoJulgadorColegiado> orgaoJulgadorColegiadoTempList = new ArrayList<OrgaoJulgadorColegiado>(0);

		for (OrgaoJulgadorColegiado orgaoColegiado : this.orgaoJulgadorColegiadoList) {
			if (existeDimensaoFuncionalOrgaoColegiado(orgaoColegiado)) {
				if (!isDimensaoFuncionalAplicavelOrgaoColegiado(orgaoColegiado, this.processoTrf)) {
					continue;
				}
			}
			orgaoJulgadorColegiadoTempList.add(orgaoColegiado);
		}
		this.orgaoJulgadorColegiadoList = orgaoJulgadorColegiadoTempList;
		
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
				"Dimensão funcional \n Órgãos colegiados selecionados: %s", listarOrgaosColegiados()));
	}

	private void buscarOrgaosDimensaoPessoa() {
		List<OrgaoJulgador> orgaoJulgadorTempList = new ArrayList<OrgaoJulgador>(0);
		for (OrgaoJulgador orgao : this.orgaoJulgadorList) {
			if (existeDimensaoPessoal(orgao)) {
				if (!isDimensaoPessoalAplicavel(orgao, this.processoTrf)
						&& !isDimensaoTipoPessoalAplicavel(orgao, this.processoTrf)) {
					continue;
				}
			}
			orgaoJulgadorTempList.add(orgao);
		}
		this.orgaoJulgadorList = orgaoJulgadorTempList;
		
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
				"Dimensão pessoal (pessoa) \n Órgãos judiciais selecionados: %s", listarOrgaos()));
		
		if (!isPrimeiroGrau) {
			buscarOrgaosDimensaoPessoaOrgaoColegiado();
		}
	}

	private void buscarOrgaosDimensaoPessoaOrgaoColegiado() {
		List<OrgaoJulgadorColegiado> orgaoJulgadorColegiadoTempList = new ArrayList<OrgaoJulgadorColegiado>(0);
		for (OrgaoJulgadorColegiado orgaoColegiado : this.orgaoJulgadorColegiadoList) {
			if (existeDimensaoPessoalOrgaoColegiado(orgaoColegiado)) {
				if (!isDimensaoPessoalAplicavelOrgaoColegiado(orgaoColegiado, this.processoTrf)) {
					continue;
				}
			}
			orgaoJulgadorColegiadoTempList.add(orgaoColegiado);
		}
		this.orgaoJulgadorColegiadoList = orgaoJulgadorColegiadoTempList;
		
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
				"Dimensão pessoal (pessoa) \n Órgãos colegiados selecionados: %s", listarOrgaosColegiados()));
	}

	private void buscarOrgaosDimensaoTipoPessoa() {
		List<OrgaoJulgador> orgaoJulgadorTempList = new ArrayList<OrgaoJulgador>(0);
		for (OrgaoJulgador orgao : this.orgaoJulgadorList) {
			if (existeDimensaoPessoal(orgao)) {
				if (!isDimensaoTipoPessoalAplicavel(orgao, this.processoTrf)) {
					continue;
				}
			}
			orgaoJulgadorTempList.add(orgao);
		}
		this.orgaoJulgadorList = orgaoJulgadorTempList;
		
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
				"Dimensão pessoal (tipo de pessoa) \n Órgãos judiciais selecionados: %s", listarOrgaos()));
		
		if (!isPrimeiroGrau) {
			buscarOrgaosDimensaoTipoPessoaOrgaoColegiado();
		}
	}

	private void buscarOrgaosDimensaoTipoPessoaOrgaoColegiado() {
		List<OrgaoJulgadorColegiado> orgaoJulgadorColegiadoTempList = new ArrayList<OrgaoJulgadorColegiado>(0);
		for (OrgaoJulgadorColegiado orgaoColegiado : this.orgaoJulgadorColegiadoList) {
			if (existeDimensaoPessoalOrgaoColegiado(orgaoColegiado)) {
				if (!isDimensaoTipoPessoalAplicavelOrgaoColegiado(orgaoColegiado, this.processoTrf)) {
					continue;
				}
			}
			orgaoJulgadorColegiadoTempList.add(orgaoColegiado);
		}
		this.orgaoJulgadorColegiadoList = orgaoJulgadorColegiadoTempList;
		
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
				"Dimensão pessoal (tipo de pessoa) \n Órgãos colegiados selecionados: %s", listarOrgaosColegiados()));
	}

	private boolean existeDimensaoFuncional(OrgaoJulgador o) {
		int qtdCompetencias = o.getOrgaoJulgadorCompetenciaAtivoList().size();
		int qtdDimensaoFuncional = 0;
		for (OrgaoJulgadorCompetencia orgaoComp : o.getOrgaoJulgadorCompetenciaAtivoList()) {
			if (orgaoComp.getCompetencia().getDimensaoFuncionalList().size() > 0) {
				qtdDimensaoFuncional++;
			}
		}
		if (qtdCompetencias == qtdDimensaoFuncional && qtdDimensaoFuncional > 0) {
			return true;
		}
		return false;
	}

	private boolean existeDimensaoFuncionalOrgaoColegiado(OrgaoJulgadorColegiado o) {
		int qtdCompetencias = o.getOrgaoJulgadorColegiadoCompetenciaAtivoList().size();
		int qtdDimensaoFuncional = 0;
		for (OrgaoJulgadorColegiadoCompetencia orgaoComp : o.getOrgaoJulgadorColegiadoCompetenciaAtivoList()) {
			if (orgaoComp.getCompetencia().getDimensaoFuncionalList().size() > 0) {
				qtdDimensaoFuncional++;
			}
		}
		if (qtdCompetencias == qtdDimensaoFuncional && qtdDimensaoFuncional > 0) {
			return true;
		}
		return false;
	}

	private boolean existeDimensaoPessoal(OrgaoJulgador o) {
		int qtdCompetencias = o.getOrgaoJulgadorCompetenciaAtivoList().size();
		int qtdDimensaoPessoal = 0;
		for (OrgaoJulgadorCompetencia orgaoComp : o.getOrgaoJulgadorCompetenciaAtivoList()) {
			if (orgaoComp.getCompetencia().getDimensaoPessoalList().size() > 0) {
				qtdDimensaoPessoal++;
			}
		}
		if (qtdCompetencias == qtdDimensaoPessoal && qtdDimensaoPessoal > 0) {
			return true;
		}
		return false;
	}

	private boolean existeDimensaoPessoalOrgaoColegiado(OrgaoJulgadorColegiado o) {
		int qtdCompetencias = o.getOrgaoJulgadorColegiadoCompetenciaAtivoList().size();
		int qtdDimensaoPessoal = 0;
		for (OrgaoJulgadorColegiadoCompetencia orgaoComp : o.getOrgaoJulgadorColegiadoCompetenciaAtivoList()) {
			if (orgaoComp.getCompetencia().getDimensaoPessoalList().size() > 0) {
				qtdDimensaoPessoal++;
			}
		}
		if (qtdCompetencias == qtdDimensaoPessoal && qtdDimensaoPessoal > 0) {
			return true;
		}
		return false;
	}

	private boolean isDimensaoFuncionalAplicavel(OrgaoJulgador o, ProcessoTrf p) {
		List<PessoaAutoridade> pessoaAutoridadeList = new ArrayList<PessoaAutoridade>(0);
		for (ProcessoParte pp : p.getProcessoPartePoloPassivoSemAdvogadoList()) {
			if (pp.getPessoa() instanceof PessoaAutoridade) {
				pessoaAutoridadeList.add((PessoaAutoridade) pp.getPessoa());
			}
		}
		if (pessoaAutoridadeList.size() > 0) {
			for (OrgaoJulgadorCompetencia ojc : o.getOrgaoJulgadorCompetenciaAtivoList()) {
				for (DimensaoFuncional df : ojc.getCompetencia().getDimensaoFuncionalList()) {
					if(df.estaIncluido(pessoaAutoridadeList.toArray(new PessoaAutoridade[]{}))){
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isDimensaoFuncionalAplicavelOrgaoColegiado(OrgaoJulgadorColegiado o, ProcessoTrf p) {
		List<PessoaAutoridade> pessoaAutoridadeList = new ArrayList<PessoaAutoridade>(0);
		for (ProcessoParte pp : p.getProcessoPartePoloPassivoSemAdvogadoList()) {
			if (pp.getPessoa() instanceof PessoaAutoridade) {
				pessoaAutoridadeList.add((PessoaAutoridade) pp.getPessoa());
			}
		}
		if (pessoaAutoridadeList.size() > 0) {
			for (OrgaoJulgadorColegiadoCompetencia ojc : o.getOrgaoJulgadorColegiadoCompetenciaAtivoList()) {
				for (DimensaoFuncional df : ojc.getCompetencia().getDimensaoFuncionalList()) {
					if(df.estaIncluido(pessoaAutoridadeList.toArray(new PessoaAutoridade[]{}))){
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isDimensaoPessoalAplicavel(OrgaoJulgador o, ProcessoTrf p) {
		List<Pessoa> pessoaPoloAtivoList = new ArrayList<Pessoa>(0);
		List<Pessoa> pessoaPoloPassivoList = new ArrayList<Pessoa>(0);

		for (ProcessoParte pp : p.getProcessoParteSemAdvogadoList()) {
			if (!(pp.getPessoa() instanceof PessoaAutoridade)) {
				if (ProcessoParteParticipacaoEnum.A.equals(pp.getInParticipacao())) {
					pessoaPoloAtivoList.add(pp.getPessoa());
				} else if (ProcessoParteParticipacaoEnum.P.equals(pp.getInParticipacao())) {
					pessoaPoloPassivoList.add(pp.getPessoa());
				}
			}
		}

		for (OrgaoJulgadorCompetencia ojc : o.getOrgaoJulgadorCompetenciaAtivoList()) {
			for (DimensaoPessoal dp : ojc.getCompetencia().getDimensaoPessoalList()) {
				if (dp.getPessoasAfetadasList().size() == 0) {
					return true;
				}
				for (DimensaoPessoalPessoa dpp : dp.getPessoasAfetadasList()) {
					if (ProcessoParteParticipacaoEnum.A.equals(dpp.getPolo())) {
						if (AssociacaoDimensaoPessoalEnum.A.equals(dpp.getTipoAssociacao())) {
							if (pessoaPoloAtivoList.contains(dpp.getPessoa())) {
								return true;
							}
						} else if (AssociacaoDimensaoPessoalEnum.E.equals(dpp.getTipoAssociacao())) {
							if (!pessoaPoloAtivoList.contains(dpp.getPessoa())) {
								return true;
							}
						}
					} else if (ProcessoParteParticipacaoEnum.P.equals(dpp.getPolo())) {
						if (AssociacaoDimensaoPessoalEnum.A.equals(dpp.getTipoAssociacao())) {
							if (pessoaPoloPassivoList.contains(dpp.getPessoa())) {
								return true;
							}
						} else if (AssociacaoDimensaoPessoalEnum.E.equals(dpp.getTipoAssociacao())) {
							if (!pessoaPoloPassivoList.contains(dpp.getPessoa())) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	private boolean isDimensaoPessoalAplicavelOrgaoColegiado(OrgaoJulgadorColegiado o, ProcessoTrf p) {
		List<Pessoa> pessoaPoloAtivoList = new ArrayList<Pessoa>(0);
		List<Pessoa> pessoaPoloPassivoList = new ArrayList<Pessoa>(0);

		for (ProcessoParte pp : p.getProcessoParteSemAdvogadoList()) {
			if (!(pp.getPessoa() instanceof PessoaAutoridade)) {
				if (ProcessoParteParticipacaoEnum.A.equals(pp.getInParticipacao())) {
					pessoaPoloAtivoList.add(pp.getPessoa());
				} else if (ProcessoParteParticipacaoEnum.P.equals(pp.getInParticipacao())) {
					pessoaPoloPassivoList.add(pp.getPessoa());
				}
			}
		}

		for (OrgaoJulgadorColegiadoCompetencia ojc : o.getOrgaoJulgadorColegiadoCompetenciaAtivoList()) {
			for (DimensaoPessoal dp : ojc.getCompetencia().getDimensaoPessoalList()) {
				if (dp.getPessoasAfetadasList().size() == 0) {
					return true;
				}
				for (DimensaoPessoalPessoa dpp : dp.getPessoasAfetadasList()) {
					if (ProcessoParteParticipacaoEnum.A.equals(dpp.getPolo())) {
						if (AssociacaoDimensaoPessoalEnum.A.equals(dpp.getTipoAssociacao())) {
							if (pessoaPoloAtivoList.contains(dpp.getPessoa())) {
								return true;
							}
						} else if (AssociacaoDimensaoPessoalEnum.E.equals(dpp.getTipoAssociacao())) {
							if (!pessoaPoloAtivoList.contains(dpp.getPessoa())) {
								return true;
							}
						}
					} else if (ProcessoParteParticipacaoEnum.P.equals(dpp.getPolo())) {
						if (AssociacaoDimensaoPessoalEnum.A.equals(dpp.getTipoAssociacao())) {
							if (pessoaPoloPassivoList.contains(dpp.getPessoa())) {
								return true;
							}
						} else if (AssociacaoDimensaoPessoalEnum.E.equals(dpp.getTipoAssociacao())) {
							if (!pessoaPoloPassivoList.contains(dpp.getPessoa())) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	private boolean isDimensaoTipoPessoalAplicavel(OrgaoJulgador o, ProcessoTrf p) {
		List<TipoPessoa> tipoPessoaPoloAtivoList = new ArrayList<TipoPessoa>(0);
		List<TipoPessoa> tipoPessoaPoloPassivoList = new ArrayList<TipoPessoa>(0);

		for (ProcessoParte pp : p.getProcessoParteSemAdvogadoList()) {
			if (!(pp.getPessoa() instanceof PessoaAutoridade)) {
				if (pp.getIsAtivo()) {
					if (ProcessoParteParticipacaoEnum.A.equals(pp.getInParticipacao())) {
						if (!tipoPessoaPoloAtivoList.contains(pp.getPessoa().getTipoPessoa())) {
							tipoPessoaPoloAtivoList.add(pp.getPessoa().getTipoPessoa());
						}
					} else if (ProcessoParteParticipacaoEnum.P.equals(pp.getInParticipacao())) {
						if (!tipoPessoaPoloPassivoList.contains(pp.getPessoa().getTipoPessoa())) {
							tipoPessoaPoloPassivoList.add(pp.getPessoa().getTipoPessoa());
						}
					}
				}
			}
		}
		for (OrgaoJulgadorCompetencia ojc : o.getOrgaoJulgadorCompetenciaAtivoList()) {
			for (DimensaoPessoal dp : ojc.getCompetencia().getDimensaoPessoalList()) {
				if (dp.getTiposDePessoasAfetadosList().size() == 0) {
					return true;
				}
				for (DimensaoPessoalTipoPessoa dpp : dp.getTiposDePessoasAfetadosList()) {
					if (ProcessoParteParticipacaoEnum.A.equals(dpp.getPolo())) {
						if (AssociacaoDimensaoPessoalEnum.A.equals(dpp.getTipoAssociacao())) {
							if (tipoPessoaPoloAtivoList.contains(dpp.getTipoPessoa())) {
								return true;
							}
						} else if (AssociacaoDimensaoPessoalEnum.E.equals(dpp.getTipoAssociacao())) {
							if (!tipoPessoaPoloAtivoList.contains(dpp.getTipoPessoa())) {
								return true;
							}
						}
					} else if (ProcessoParteParticipacaoEnum.P.equals(dpp.getPolo())) {
						if (AssociacaoDimensaoPessoalEnum.A.equals(dpp.getTipoAssociacao())) {
							if (tipoPessoaPoloPassivoList.contains(dpp.getTipoPessoa())) {
								return true;
							}
						} else if (AssociacaoDimensaoPessoalEnum.E.equals(dpp.getTipoAssociacao())) {
							if (!tipoPessoaPoloPassivoList.contains(dpp.getTipoPessoa())) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	private boolean isDimensaoTipoPessoalAplicavelOrgaoColegiado(OrgaoJulgadorColegiado o, ProcessoTrf p) {
		List<TipoPessoa> tipoPessoaPoloAtivoList = new ArrayList<TipoPessoa>(0);
		List<TipoPessoa> tipoPessoaPoloPassivoList = new ArrayList<TipoPessoa>(0);

		for (ProcessoParte pp : p.getProcessoParteSemAdvogadoList()) {
			if (!(pp.getPessoa() instanceof PessoaAutoridade)) {
				if (pp.getIsAtivo()) {
					if (ProcessoParteParticipacaoEnum.A.equals(pp.getInParticipacao())) {
						if (!tipoPessoaPoloAtivoList.contains(pp.getPessoa().getTipoPessoa())) {
							tipoPessoaPoloAtivoList.add(pp.getPessoa().getTipoPessoa());
						}
					} else if (ProcessoParteParticipacaoEnum.P.equals(pp.getInParticipacao())) {
						if (!tipoPessoaPoloPassivoList.contains(pp.getPessoa().getTipoPessoa())) {
							tipoPessoaPoloPassivoList.add(pp.getPessoa().getTipoPessoa());
						}
					}
				}
			}
		}

		for (OrgaoJulgadorColegiadoCompetencia ojc : o.getOrgaoJulgadorColegiadoCompetenciaAtivoList()) {
			for (DimensaoPessoal dp : ojc.getCompetencia().getDimensaoPessoalList()) {
				if (dp.getTiposDePessoasAfetadosList().size() == 0) {
					return true;
				}
				for (DimensaoPessoalTipoPessoa dpp : dp.getTiposDePessoasAfetadosList()) {
					if (ProcessoParteParticipacaoEnum.A.equals(dpp.getPolo())) {
						if (AssociacaoDimensaoPessoalEnum.A.equals(dpp.getTipoAssociacao())) {
							if (tipoPessoaPoloAtivoList.contains(dpp.getTipoPessoa())) {
								return true;
							}
						} else if (AssociacaoDimensaoPessoalEnum.E.equals(dpp.getTipoAssociacao())) {
							if (!tipoPessoaPoloAtivoList.contains(dpp.getTipoPessoa())) {
								return true;
							}
						}
					} else if (ProcessoParteParticipacaoEnum.P.equals(dpp.getPolo())) {
						if (AssociacaoDimensaoPessoalEnum.A.equals(dpp.getTipoAssociacao())) {
							if (tipoPessoaPoloPassivoList.contains(dpp.getTipoPessoa())) {
								return true;
							}
						} else if (AssociacaoDimensaoPessoalEnum.E.equals(dpp.getTipoAssociacao())) {
							if (!tipoPessoaPoloPassivoList.contains(dpp.getTipoPessoa())) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	private void buscarCargosElegiveis() {
		List<OrgaoJulgadorCargo> orgaoJulgadorCargoTempList = new ArrayList<OrgaoJulgadorCargo>(0);

		Double menorPesoCargo = Double.MIN_VALUE;

		for (OrgaoJulgador orgaoJulgador : this.orgaoJulgadorList) {
			if (orgaoJulgador.getAtivo() && validarRelatorInstanciaSuperior(orgaoJulgador)) {
				for (OrgaoJulgadorCargo orgaoJulgadorCargo : orgaoJulgador.getOrgaoJulgadorCargoList()) {
					if (orgaoJulgadorCargo.getRecebeDistribuicao() && orgaoJulgadorCargo.getAtivo()) {
						if (this.processoTrfRedistribuicao != null && this.processoTrfRedistribuicao.getInTipoRedistribuicao().equals(TipoRedistribuicaoEnum.P) && this.processoTrf.getOrgaoJulgadorCargo().equals(orgaoJulgadorCargo) ) {
							continue;
						}
						orgaoJulgadorCargoTempList.add(orgaoJulgadorCargo);
						if (menorPesoCargo.equals(Double.MIN_VALUE)) {
							menorPesoCargo = orgaoJulgadorCargo.getAcumuladorDistribuicao();
						} else {
							if (orgaoJulgadorCargo.getAcumuladorDistribuicao() <= menorPesoCargo) {
								menorPesoCargo = orgaoJulgadorCargo.getAcumuladorDistribuicao();
							}
						}

					}
				}
			}
		}
		SecureRandom random = new SecureRandom();
		Double distanciaMaximaDistribuicao = calcularDistanciaMaximaDistribuicao(orgaoJulgadorCargoTempList.size())
				+ menorPesoCargo;
		if (distanciaMaximaDistribuicao > 0.0) {
			for (OrgaoJulgadorCargo orgaoJulgadorCargo : orgaoJulgadorCargoTempList) {
				if (orgaoJulgadorCargo.getAcumuladorDistribuicao() <= distanciaMaximaDistribuicao
						|| random.nextInt(100) < 25) {
					orgaoJulgadorCargoList.add(orgaoJulgadorCargo);
				}
			}
		} else {
			orgaoJulgadorCargoList = orgaoJulgadorCargoTempList;

		}
		orgaoJulgadorCargoList.removeAll(orgaoJulgadorCargoExcluidoList);
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
				"Cargos elegíveis (após aplicação da distância máxima de distribuição): %s", listarCargos()));
	}
	
	
	/**
	 * Valida, para instancia superior, se um orgão julgador possui magistrado relator.
	 * @param orgaoJulgador Órgão Julgador a ser verificado
	 * @return <code>true</code> caso o órgão julgador possua um relator válido ou caso não se trate de instancia superior. 
	 */
	private Boolean validarRelatorInstanciaSuperior(OrgaoJulgador orgaoJulgador){
		return (isPrimeiroGrau || ComponentUtil.getComponent(UsuarioLocalizacaoMagistradoServidorManager.class).getRelator(orgaoJulgador) != null);
	}

	private Double calcularDistanciaMaximaDistribuicao(int quantidadeCargos) {
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select d from DistanciaMaximaDistribuicao d ")
			.append("where (:quantidadeCargos between d.intervaloInicial and d.intervaloFinal)) ");
		
		Query query = em.createQuery(sql.toString());
		query.setParameter("quantidadeCargos", quantidadeCargos);
		query.setMaxResults(1);
		DistanciaMaximaDistribuicao distanciaMaximaDistribuicao = (DistanciaMaximaDistribuicao) EntityUtil.getSingleResult(query);
		if (distanciaMaximaDistribuicao == null) {
			return 0.0;
		}
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
				"Distância máxima de distribuição: %s", distanciaMaximaDistribuicao.getDistanciaMaxima()));
		
		return distanciaMaximaDistribuicao.getDistanciaMaxima();
	}
	
	/**
	 * Verifica se a validação de número mínimo de participantes em órgão julgador colegadiado deve ser realizada  
	 * @return <code>true</code> caso seja necessária a validação, <code>false</code> em caso contrário 
	 */
	private boolean deveValidarMinimoParticipantes() {
		boolean retorno = false;
		if(!isPrimeiroGrau && !this.processoTrf.getIsIncidente() && 
				(this.processoTrfRedistribuicao == null || 
					!TipoRedistribuicaoEnum.P.equals(this.processoTrfRedistribuicao.getInTipoRedistribuicao()) && 
					!TipoDistribuicaoEnum.EN.equals(this.processoTrfRedistribuicao.getInTipoDistribuicao()))) {
			
			retorno = true;
		}
		return retorno;
	}
	
	private void sortearCargoJudicial(){
		if (orgaoJulgadorCargoList.size() == 0) {
			throw new RuntimeException(
					"Não há cargos judiciais configurados para todos os órgãos julgadores candidatos a receber o processo.");
		}
		
		if(deveValidarMinimoParticipantes()) {
			Set<OrgaoJulgadorCargo> listOjcaInapto = new HashSet<OrgaoJulgadorCargo>();
			for(OrgaoJulgadorCargo orgaoJulgadorCargo : orgaoJulgadorCargoList){
				processarMinimoParticipantes(listOjcaInapto, orgaoJulgadorCargo);
			}
			removerOrgaosJulgadoresCargoInaptos(listOjcaInapto);
		}
				
		int x = 0;

		if (orgaoJulgadorCargoList.size() > 1) {
			SecureRandom random = new SecureRandom();
			x = random.nextInt(orgaoJulgadorCargoList.size());
		}
		
		this.orgaoJulgadorCargoSorteado = orgaoJulgadorCargoList.get(x);
		
		EntityUtil.getEntityManager().refresh(this.orgaoJulgadorCargoSorteado);

		// Realiza o lock do cargo sorteado para garantir que outra transação não interfira no cálculo do acumulador de distribuição.
		EntityUtil.getEntityManager().lock(this.orgaoJulgadorCargoSorteado, LockModeType.READ);

		this.orgaoJulgadorCargoAnterior = this.processoTrf.getOrgaoJulgadorCargo();
		this.processoTrf.setOrgaoJulgador(this.orgaoJulgadorCargoSorteado.getOrgaoJulgador());
		this.processoTrf.setOrgaoJulgadorCargo(this.orgaoJulgadorCargoSorteado);
		this.processoTrf.setCargo(this.orgaoJulgadorCargoSorteado.getCargo());

		if (processoTrfRedistribuicao == null) {
			if (this.processoTrf.getIsIncidente()) {
				this.logDist.setInTipoDistribuicao(TipoDistribuicaoEnum.I);
			} else {
				this.logDist.setInTipoDistribuicao(TipoDistribuicaoEnum.S);
			}
		}
		
		this.logDist.setOrgaoJulgador(this.orgaoJulgadorCargoSorteado.getOrgaoJulgador());
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format("%s: %s", 
				this.processoTrf.getIsIncidente() ? "Órgão julgador originário" : "Órgão julgador sorteado",
					this.orgaoJulgadorCargoSorteado.getOrgaoJulgador().getOrgaoJulgador()));
		
		this.logDist.setOrgaoJulgadorCargo(this.orgaoJulgadorCargoSorteado);
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format("%s: %s", 
				this.processoTrf.getIsIncidente() ? "Cargo originário" : "Cargo sorteado",
						this.orgaoJulgadorCargoSorteado.getDescricao()));
	}

	/**
	 * Remove da lista de OrgaoJulgadorCargo os cargos inaptos
	 * @param listOrgaoJulgadorCargoInapto 
	 */
	private void removerOrgaosJulgadoresCargoInaptos(Set<OrgaoJulgadorCargo> listOrgaoJulgadorCargoInapto) {
		if(listOrgaoJulgadorCargoInapto.containsAll(orgaoJulgadorCargoList)){
			throw new RuntimeException("Não há órgãos julgadores com o mínimo "
					+ "de participantes necessários para a distribuição/redistribuição");
		} else {
			orgaoJulgadorCargoList.removeAll(listOrgaoJulgadorCargoInapto);
		}
	}

	/**
	 * Processa quais OrgaoJulgadorCargo estao inaptos com relacao ao numero minimo no OrgaoJulgadorColegiado
	 * @param listOjcaInapto
	 * @param orgaoJulgadorCargo
	 */
	private void processarMinimoParticipantes(Set<OrgaoJulgadorCargo> listOjcaInapto, OrgaoJulgadorCargo orgaoJulgadorCargo) {
		
		List<OrgaoJulgadorColegiado> orgaosColegiadosNaCompetencia = new ArrayList<OrgaoJulgadorColegiado>();
		buscarOrgaosColegiadosCompetenciaAtiva(
			OrgaoJulgadorColegiadoOrgaoJulgadorManager.instance().obterAtivos(orgaoJulgadorCargo.getOrgaoJulgador()), orgaosColegiadosNaCompetencia);
		
		for (OrgaoJulgadorColegiado orgaoJulgadorColegiado : orgaosColegiadosNaCompetencia) {
			if(!possuiMinimoDeParticipantes(orgaoJulgadorColegiado)){
				listOjcaInapto.add(orgaoJulgadorCargo);
			}
		}
	}
	
	/**
	 * Verifica se o orgão julgador colegiado possui o mínimo de participantes aptos para a distribuição 
	 * @param ojc OrgaoJulgadorColegiado
	 * @return
	 */
	private boolean possuiMinimoDeParticipantes(OrgaoJulgadorColegiado ojc){
		Map<OrgaoJulgadorColegiado, Set<OrgaoJulgador>> ojcMap = new HashMap<OrgaoJulgadorColegiado, Set<OrgaoJulgador>>();
				
		for(OrgaoJulgadorColegiadoOrgaoJulgador ojcoj : OrgaoJulgadorColegiadoOrgaoJulgadorManager.instance().obterAtivos(ojc)){ 
			
			//adiciona no mapa nova lista vinculada ao OrgaoJulgadorColegiado
			if(ojcMap.get(ojc) == null){
				ojcMap.put(ojc, new HashSet<OrgaoJulgador>());
			}

			//adiciona na lista vinculada, se o OrgaoJulgador estiver apto
			if(orgaoJulgadorList.contains(ojcoj.getOrgaoJulgador())){
				ojcMap.get(ojc).add(ojcoj.getOrgaoJulgador());
			}
		}
		
		int min = ojc.getMinimoParticipanteDistribuicao();
		int aptos = ojcMap.get(ojc).size();
		
		return !(min > aptos);
	}
	
	/**
	 * Método que averigua as regras de contagem de peso no Orgão Julgador de
	 * DESTINO da distribuição ou redistribuição da Justiça Eleitoral,
	 * retornando um valor booleano autorizando ou não a contagem de peso. As
	 * regras são listadas abaixo e são validas apenas no ambito da Justiça
	 * Eleitoral:
	 * 
	 * <ul>
	 * <li>
	 * Processos em Redistribuição, saindo da cadeia formada em virtude da
	 * prevenção do art. 260 do CE: processos que não contam peso no OJ origem
	 * mas contam no OJ destino (Distribuido por Prevenção do art 260 do CE,
	 * redistribuido por outros motivos). Obs: para os processos redistribuidos
	 * em Lote, que permanecem na cadeia formada pelo art 260 do CE, a regra é
	 * tratada no Observer "RedistribuicaoLoteObserver", onde neste caso não é
	 * adicionando nem subtraido peso tanto da origem quanto do destino</li>
	 * 
	 * <li>
	 * Processos em Distribuição, entrando em uma cadeia formada em virtude da
	 * prevenção do art. 260 do CE:neste caso não contar peso no OJ.</li>
	 * </ul>
	 * 
	 * @return Boolean <code>true</code> para contagem de peso no OJ de destino,
	 *         <code>false</code> caso contrario
	 * @throws PJeBusinessException
	 * @author eduardo.pereira@tse.jus.br
	 */
	private boolean isDeveAtualizarAcumuladorCargoJudicialParaJE() throws PJeBusinessException{
			boolean isAtualizaAcumuladorCargoJudicial = false;
			
			if(getCompensacaoPesoEleitoralAtivo()){
				isAtualizaAcumuladorCargoJudicial = true;
			} else if(this.processoTrfRedistribuicao != null){
				isAtualizaAcumuladorCargoJudicial = isDeveAtualizarProcesssoRedistribuicaoJE();
			} else if(this.processoTrf != null){
				isAtualizaAcumuladorCargoJudicial = isDeveAtualizarProcessoDistribuicaoJE(); 
			}
			return isAtualizaAcumuladorCargoJudicial;
	}

	/**
	 * Verifica se para o processo em redistribuicao pode acumular o cargo judicial (peso do processo)
	 * - Se o processo se enquadrar no art 260 e nao for por motivo de prevencao atualiza o peso
	 * - Se o processo nao se enquadrar no at 260 atualiza o peso
	 * 
	 * @return true o processo em redistribuicao deve atualizar o peso do OJC
	 * @throws PJeBusinessException
	 */
	private boolean isDeveAtualizarProcesssoRedistribuicaoJE() throws PJeBusinessException {
		boolean isAtualizaAcumuladorCargoJudicial = false;
		final boolean processoEnquadra260 = verificarEnquadramento(this.processoTrfRedistribuicao.getProcessoTrf());
		
		if(processoEnquadra260 && !TipoDistribuicaoEnum.PP.equals(this.processoTrfRedistribuicao.getInTipoDistribuicao())){
			isAtualizaAcumuladorCargoJudicial = true;
		} else if(!processoEnquadra260){
			isAtualizaAcumuladorCargoJudicial = true;
		}
		return isAtualizaAcumuladorCargoJudicial;
	}

	/**
	 * Verifica se para o processo em distribuicao pode acumular o cargo judicial (peso do processo)
	 * - Se o processo nao se enquadrar no art 260 atualiza o peso
	 * - Se o processo se enquadrar no at 260, e nao existir processo prevento atualiza o peso
	 * 
	 * @return true o processo em distribuicao deve atualizar o peso do OJC
	 * @throws PJeBusinessException
	 */
	private boolean isDeveAtualizarProcessoDistribuicaoJE() throws PJeBusinessException {
		boolean isAtualizaAcumuladorCargoJudicial = false;
		final boolean processoEnquadra260 = verificarEnquadramento(this.processoTrf);
		
		if(processoEnquadra260){
			ProcessoTrf processoPrevento = buscarProcessoPrevencaoEleicaoOrigemComplemento(this.processoTrf);
			if(processoPrevento == null){
				isAtualizaAcumuladorCargoJudicial = true;
			}
		} else {
			isAtualizaAcumuladorCargoJudicial = true;
		}
		return isAtualizaAcumuladorCargoJudicial;
	}
	
	/**
	 * <li>
	 * Método que averigua as regras de contagem de peso no Orgão Julgador de
	 * ORIGEM da redistribuição da Justiça Eleitoral, retornando um valor
	 * booleano autorizando ou não a contagem de peso. As regras são listadas
	 * abaixo e são validas apenas no ambito da Justiça Eleitoral:
	 * <li>
	 * Processos em Redistribuição, antes distribuido por outros motivos que não
	 * a prevenção do art.260 do CE, entrando em cadeia formada em virtude da
	 * prevenção do art. 260 do CE: Neste caso, não contar peso no OJ de
	 * destino, mas decrementar do OJ de origem</li>
	 * 
	 * @return Boolean <code>true</code> para contagem de peso no OJ de origem,
	 *         <code>false</code> caso contrario
	 * @throws PJeBusinessException
	 * @author eduardo.pereira@tse.jus.br
	 */
	private boolean isDeveAtualizarAcumuladorCargoJudicialAnteriorParaJE() throws PJeBusinessException{
		boolean isAtualizaAcumuladorCargoJudicial = true;
		if(isJEeParametroPrevencaoAtivo()){
			if(this.processoTrfRedistribuicao != null){
				isAtualizaAcumuladorCargoJudicial = isDeveAtualizarProcesssoRedistribuicaoJE();
			}
		}
		return isAtualizaAcumuladorCargoJudicial;
	}
	
	private void atualizarAcumuladorCargoJudicial(OrgaoJulgadorCargo cargoDestino, OrgaoJulgadorCargo cargoOrigem) throws Exception {
		log.info("Atualizando o cargo judicial do orgao julgador");
		if(isJEeParametroPrevencaoAtivo() && !isDeveAtualizarAcumuladorCargoJudicialParaJE()){
			log.info("Nao deve atualizar o cargo judicial do orgao julgador JE");
			return;
		}
		
		if(isRedistribuicao(cargoDestino, cargoOrigem) && (cargoOrigem.getOrgaoJulgador().getJurisdicao().getIdJurisdicao() == codNucleo || cargoDestino.getOrgaoJulgador().getJurisdicao().getIdJurisdicao() == codNucleo) ){
			log.info("Nao deve fazer a compensação quando for Nucleo 4.0");
			return;
		}
		
		this.pesoProcessualAnterior = this.processoTrf.getValorPesoProcessual() == null ? 0.00 : this.processoTrf.getValorPesoProcessual();
		this.pesoDistribuicaoAnterior = this.processoTrf.getValorPesoDistribuicao() == null ? 0.00 : this.processoTrf.getValorPesoDistribuicao();
		this.pesoProcessual = calcularPesoProcessual();
		this.pesoDistribuicao = calcularPesoDistribuicao(cargoDestino, pesoProcessual);
		this.processoTrf.setValorPesoProcessual(pesoProcessual);
		this.processoTrf.setValorPesoDistribuicao(pesoDistribuicao);
		
		ComponentUtil.getOrgaoJulgadorCargoManager().ajustarAcumuladores(
				cargoDestino, this.pesoProcessual, this.pesoDistribuicao);

		if(this.isDeveAtualizarAcumuladorCargoJudicialAnteriorParaJE()) {
			if (cargoOrigem != null) {
				ComponentUtil.getOrgaoJulgadorCargoManager().ajustarAcumuladores(
						cargoOrigem, -this.pesoProcessualAnterior, -this.pesoDistribuicaoAnterior);
			}
		}
	}
	
	private boolean isRedistribuicao(OrgaoJulgadorCargo cargoDestino, OrgaoJulgadorCargo cargoOrigem) {
		return cargoDestino != null && cargoOrigem != null ? true : false;
	}

	private void gravarProcessoRedistribuicao() throws Exception {
		TramitacaoProcessualService tramitacaoProcessualService = ComponentUtil.getTramitacaoProcessualService();
		
		OrgaoJulgador orgaoJulgadorAnterior = this.orgaoJulgadorCargoAnterior.getOrgaoJulgador();
		tramitacaoProcessualService.gravaVariavel(Variaveis.VARIAVEL_FLUXO_REDISTRIBUICAO_ORGAO_JULGADOR_ANTERIOR, 
				orgaoJulgadorAnterior.getIdOrgaoJulgador());
				
		tramitacaoProcessualService.gravaVariavel(Variaveis.VARIAVEL_FLUXO_REDISTRIBUICAO_CIDADE_SEDE_ORGAO_JULGADOR_ANTERIOR, 
				orgaoJulgadorAnterior.getJurisdicao().getMunicipioSede() == null ? 
						null : orgaoJulgadorAnterior.getJurisdicao().getMunicipioSede().getIdMunicipio());
		
		OrgaoJulgador orgaoJulgadorAtual = this.orgaoJulgadorCargoSorteado.getOrgaoJulgador();
		tramitacaoProcessualService.gravaVariavel(Variaveis.VARIAVEL_FLUXO_REDISTRIBUICAO_ORGAO_JULGADOR_ATUAL, 
				orgaoJulgadorAtual.getIdOrgaoJulgador());
		
		tramitacaoProcessualService.gravaVariavel(Variaveis.VARIAVEL_FLUXO_REDISTRIBUICAO_CIDADE_SEDE_ORGAO_JULGADOR_ATUAL, 
				orgaoJulgadorAtual.getJurisdicao().getMunicipioSede() == null ? 
						null : orgaoJulgadorAtual.getJurisdicao().getMunicipioSede().getIdMunicipio());

		this.processoTrfRedistribuicao.setProcessoTrf(this.processoTrf);
		this.processoTrfRedistribuicao.setDataRedistribuicao(new Date());
		
		if(!ParametroUtil.instance().isPrimeiroGrau()) {
			this.processoTrfRedistribuicao.setOrgaoJulgadorColegiado(this.processoTrf.getOrgaoJulgadorColegiado());
			this.processoTrfRedistribuicao.setOrgaoJulgadorColegiadoAnterior(this.orgaoJulgadorColegiadoAnterior);
			
			tramitacaoProcessualService.gravaVariavel(Variaveis.VARIAVEL_FLUXO_REDISTRIBUICAO_ORGAO_JULGADOR_COLEGIADO_ANTERIOR, 
					this.orgaoJulgadorColegiadoAnterior.getIdOrgaoJulgadorColegiado());
			
			tramitacaoProcessualService.gravaVariavel(Variaveis.VARIAVEL_FLUXO_REDISTRIBUICAO_ORGAO_JULGADOR_COLEGIADO_ATUAL, 
					this.processoTrf.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado());
		}
		
		this.processoTrfRedistribuicao.setOrgaoJulgador(this.orgaoJulgadorCargoSorteado.getOrgaoJulgador());
		this.processoTrfRedistribuicao.setOrgaoJulgadorAnterior(this.orgaoJulgadorCargoAnterior.getOrgaoJulgador());
		this.processoTrfRedistribuicao.setJurisdicao(this.orgaoJulgadorCargoSorteado.getOrgaoJulgador().getJurisdicao());
		this.processoTrfRedistribuicao.setUsuario((Usuario) Contexts.getSessionContext().get("usuarioLogado"));

		excluiPresidenteRelatorDeProcessoEmRedistribuicaoJE();

		atualizaJurisdicaoOrgaoJulgadorExpedienteCaixaAdvogado();

		EntityUtil.getEntityManager().merge(this.processoTrfRedistribuicao);
		EntityUtil.getEntityManager().flush();
	}

	private void atualizaJurisdicaoOrgaoJulgadorExpedienteCaixaAdvogado() throws PJeBusinessException {		
		List<ProcessoParteExpediente> processoParteExpedientes = ComponentUtil.getProcessoParteExpedienteManager().recuperaExpedientesNaoFechados(this.processoTrf);
		Date dataRedistribuicao = this.processoTrfRedistribuicao.getDataRedistribuicao();
		ProcessoParteExpedienteCaixaAdvogadoProcuradorManager processoParteExpedienteCaixaAdvogadoProcuradorManager = ComponentUtil.getComponent(ProcessoParteExpedienteCaixaAdvogadoProcuradorManager.class);
		CaixaAdvogadoProcuradorManager caixaAdvogadoProcuradorManager = ComponentUtil.getComponent(CaixaAdvogadoProcuradorManager.class);
		for (ProcessoParteExpediente ppe : processoParteExpedientes) {						
			if (dataRedistribuicao.after(ppe.getProcessoExpediente().getDtCriacao())){
				List<ProcessoParteExpedienteCaixaAdvogadoProcurador> listProcessoParteExpCaixaAdvProc = processoParteExpedienteCaixaAdvogadoProcuradorManager.listExpedienteEmCaixa(ppe);
				
				for (ProcessoParteExpedienteCaixaAdvogadoProcurador ppec : listProcessoParteExpCaixaAdvProc) {
					CaixaAdvogadoProcurador caixaAdvogadoProcurador =  caixaAdvogadoProcuradorManager.findById(ppec.getCaixaAdvogadoProcurador().getIdCaixaAdvogadoProcurador());

					// Se a caixa existe e a jurisdição ou o órgão julgador ou o órgão julgador colegiado forem diferentes do processo...
					if (caixaAdvogadoProcurador != null && 
							(
								(caixaAdvogadoProcurador.getJurisdicao() != null && !caixaAdvogadoProcurador.getJurisdicao().equals(this.processoTrf.getJurisdicao())) ||  
								(caixaAdvogadoProcurador.getOrgaoJulgador() != null && !caixaAdvogadoProcurador.getOrgaoJulgador().equals(this.processoTrf.getOrgaoJulgador())) ||
								(caixaAdvogadoProcurador.getOrgaoJulgadorColegiado() != null && !caixaAdvogadoProcurador.getOrgaoJulgadorColegiado().equals(this.processoTrf.getOrgaoJulgadorColegiado()))
							)
					   ) {
						// Excluir o vínculo do processo com a caixa
						processoParteExpedienteCaixaAdvogadoProcuradorManager.remover(caixaAdvogadoProcurador, false, false, ppec.getProcessoParteExpediente());						

					}
				}
			}
		}
		
		// Recuperar a lista de Caixas de Advogados vinculadas ao processo redistribuído
		ProcessoCaixaAdvogadoProcuradorManager processoCaixaAdvogadoProcuradorManager = ComponentUtil.getComponent(ProcessoCaixaAdvogadoProcuradorManager.class);
		List<ProcessoCaixaAdvogadoProcurador> listaCaixas = processoCaixaAdvogadoProcuradorManager.recuperarPorProcesso(this.processoTrf);
		
		for (ProcessoCaixaAdvogadoProcurador processoCaixa : listaCaixas) {
			CaixaAdvogadoProcurador caixaAdvogadoProcurador =  caixaAdvogadoProcuradorManager.findById(processoCaixa.getCaixaAdvogadoProcurador().getIdCaixaAdvogadoProcurador());

			// Se a caixa existe e a jurisdição ou o órgão julgador ou o órgão julgador colegiado forem diferentes do processo...
			if (caixaAdvogadoProcurador != null && 
					(
						(caixaAdvogadoProcurador.getJurisdicao() != null && !caixaAdvogadoProcurador.getJurisdicao().equals(this.processoTrf.getJurisdicao())) ||  
						(caixaAdvogadoProcurador.getOrgaoJulgador() != null && !caixaAdvogadoProcurador.getOrgaoJulgador().equals(this.processoTrf.getOrgaoJulgador())) ||
						(caixaAdvogadoProcurador.getOrgaoJulgadorColegiado() != null && !caixaAdvogadoProcurador.getOrgaoJulgadorColegiado().equals(this.processoTrf.getOrgaoJulgadorColegiado()))
					)
			   ) {
				// Excluir o vínculo do processo com a caixa
				processoCaixaAdvogadoProcuradorManager.remove(processoCaixa);
			}
		}
	}

	private Double buscarPesoPrevencaoIncidental() throws Exception {

		int qtd = 0;
		
		List<ProcessoTrfConexao> conexos = ComponentUtil.getComponent(ProcessoTrfConexaoManager.class).getListProcessosAssociados(this.processoTrf.getProcessoReferencia().getIdProcessoTrf());

		for (ProcessoTrfConexao ptc : conexos) {
			if (TipoConexaoEnum.DP.equals(ptc.getTipoConexao()) && PrevencaoEnum.PR.equals(ptc.getPrevencao())) {
				qtd++;
			}
		}

		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();

		sql.append("select pp from PesoPrevencao pp ").append("where pp.ativo = true ")
				.append("and ((:qtd between pp.intervaloInicial and pp.intervaloFinal and pp.tipoIntervalo='E') ")
				.append("or (:qtd <= pp.intervaloFinal and pp.tipoIntervalo='A') ")
				.append("or (:qtd >= pp.intervaloInicial and pp.tipoIntervalo='M')) ");
		Query query = em.createQuery(sql.toString());
		query.setParameter("qtd", qtd);
		if (query.getResultList().size() == 0) {
			throw new Exception(String.format(
					"Não há intervalo de peso de prevenção para a quantidade de preventos do processo originário: %d.",
					qtd));
		} else if (query.getResultList().size() > 1) {
			throw new Exception(
					String.format(
							"Foi encontrado mais de um intervalo de peso de prevenção para a quantidade de preventos do processo originário: %d.",
							qtd));
		}
		return ((PesoPrevencao) query.getResultList().get(0)).getValorPeso();
	}

	private Double calcularPesoDistribuicao(OrgaoJulgadorCargo cargoDestino, Double pesoProcessual) throws Exception {
		if (cargoDestino.getValorPeso() == null
				|| cargoDestino.getValorPeso().doubleValue() == 0.0) {
			throw new Exception(String.format(
					"Não há peso configurado para o cargo judicial: %s do órgão julgador: %s.",
					cargoDestino, cargoDestino.getOrgaoJulgador()));
		}

		Double pesoPrevencao = 1.0;
		if (this.processoTrf.getIsIncidente() && this.processoTrf.getProcessoReferencia() != null) {
			pesoPrevencao = buscarPesoPrevencaoIncidental();
		}

		Double pesoDistribuicao = pesoProcessual * pesoPrevencao * (100 / cargoDestino.getValorPeso());
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
				"Peso de distribuição: %s", pesoDistribuicao));
		
		return pesoDistribuicao;
	}

	private Double calcularPesoProcessual() throws Exception {
		List<ProcessoPesoParte> pesosPartesLst = buscarPesosPartes();
		Double pesoProcessual = processoTrf.calcularPesoProcessual(pesosPartesLst);
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
				"Peso processual: %s", pesoProcessual));
		
		return pesoProcessual;
	}
	
	private List<ProcessoPesoParte> buscarPesosPartes() {
		return ComponentUtil.getComponent(ProcessoPesoParteManager.class).buscarPesosPartes();
	}

	private String listarOrgaos() {
		StringBuilder texto = new StringBuilder();
		for (OrgaoJulgador o : this.orgaoJulgadorList) {
			texto.append("[" + o.getOrgaoJulgador() + "] ");
		}
		return texto.toString();
	}

	private String listarOrgaosColegiados() {
		StringBuilder texto = new StringBuilder();
		for (OrgaoJulgadorColegiado o : this.orgaoJulgadorColegiadoList) {
			texto.append("[" + o.getOrgaoJulgadorColegiado() + "] ");
		}
		return texto.toString();
	}

	private String listarCargos() {
		StringBuilder texto = new StringBuilder();
		for (OrgaoJulgadorCargo o : this.orgaoJulgadorCargoList) {
			texto.append("[" + o.getDescricao() + "-" + o.getOrgaoJulgador().toString() + "] ");
		}
		return texto.toString();
	}

	private boolean isProcessoAssuntoListVazia() {
		this.recuperaAssuntosProcesso();
		return (this.assuntoTrfList == null || this.assuntoTrfList.isEmpty());
	}
	
	private void recuperaAssuntosProcesso() {
		if(this.assuntoTrfList == null || this.assuntoTrfList.isEmpty()) {
			this.assuntoTrfList = ProcessoJudicialManager.instance().recuperaAssuntosNaoComplementares(processoTrf);
		}
	}

	private boolean isOrgaoJulgadorListVazia() {
		return (this.orgaoJulgadorList == null || this.orgaoJulgadorList.size() == 0);
	}

	private boolean isOrgaoJulgadorColegiadoListVazia() {
		return (this.isPrimeiroGrau && this.orgaoJulgadorColegiadoList == null || this.orgaoJulgadorColegiadoList.size() == 0);
	}
	public Competencia getCompetenciaConflito() {
		return competenciaConflito;
	}

	public void setCompetenciaConflito(Competencia competenciaConflito) {
		this.competenciaConflito = competenciaConflito;
	}
	
	/**
	 * Método que popula a lista de órgãos julgadores com OJs que tenham competência ativa em cada órgão julgador colegiado.
	 * @param ojcsOjsList
	 *            Lista de órgãos julgadores e órgãos julgadores colegiados vinculados 
	 */
	private void buscarOrgaosCompetenciaAtiva(List<OrgaoJulgadorColegiadoOrgaoJulgador> ojcsOjsList) throws Exception {
		for (OrgaoJulgadorColegiadoOrgaoJulgador ojcOj : ojcsOjsList) {
			OrgaoJulgador orgaoJulgador = ojcOj.getOrgaoJulgador();
			for (OrgaoJulgadorCompetencia ojComp : this.competenciaConflito.getOrgaoJulgadorCompetenciaAtivoList()) {
				OrgaoJulgador oj = ojComp.getOrgaoJulgador();
				if((oj.equals(orgaoJulgador))
						&& (!this.orgaoJulgadorList.contains(orgaoJulgador))) {
					this.orgaoJulgadorList.add(orgaoJulgador);
				}
			}
		}
	}
	
	/**
	 * Método que popula a lista de órgãos julgadores colegiados com OJCs que tenham competência ativa.
	 * @param ojcsOjsList
	 *            Lista de órgãos julgadores e órgãos julgadores colegiados vinculados 
	 */
	private void buscarOrgaosColegiadosCompetenciaAtiva(List<OrgaoJulgadorColegiadoOrgaoJulgador> ojcsOjsList) {
		buscarOrgaosColegiadosCompetenciaAtiva(ojcsOjsList, this.orgaoJulgadorColegiadoList);
	}
	
	/**
	 * Método que popula a lista de órgãos julgadores colegiados passados no
	 * parâmetro listaOjcsParaPopular com OJCs que tenham competência ativa.
	 * 
	 * @param ojcsOjsList
	 *            	Lista de órgãos julgadores e órgãos julgadores colegiados vinculados
	 * @param listaOjcsParaPopular
	 *				Lista onde serão populados os OJCs ativos na competência.            
	 */
	private void buscarOrgaosColegiadosCompetenciaAtiva(List<OrgaoJulgadorColegiadoOrgaoJulgador> ojcsOjsList,
			List<OrgaoJulgadorColegiado> listaOjcsParaPopular) {	
		for (OrgaoJulgadorColegiadoOrgaoJulgador ojcOj : ojcsOjsList) {
			OrgaoJulgadorColegiado orgaoJulgadorColegiado = ojcOj.getOrgaoJulgadorColegiado();		
			for (OrgaoJulgadorColegiadoCompetencia ojcComp : orgaoJulgadorColegiado.getOrgaoJulgadorColegiadoCompetenciaAtivoList()) {
				OrgaoJulgadorColegiado ojc = ojcComp.getOrgaoJulgadorColegiado();
				if((ojc.equals(orgaoJulgadorColegiado))
						&& (!listaOjcsParaPopular.contains(orgaoJulgadorColegiado))
						&& (this.competenciaConflito.equals(ojcComp.getCompetencia()))) {
					listaOjcsParaPopular.add(orgaoJulgadorColegiado);
				}
			}
		}
	}
	
	/**
	 * Método que remove os órgãos julgadores impedidos de participar do sorteio na distribuição ou redistribuição. 
	 */
	private void removerOrgaosImpedidosSorteio() {
		for (OrgaoJulgador orgaoJulgadorImpedido : this.orgaoJulgadorImpedidoList) {
			this.orgaoJulgadorList.remove(orgaoJulgadorImpedido);
		}
	}
	
	/**
	 * Método que remove o órgão julgador relativo ao gabinete de plantonista do sorteio na redistribuição. 
	 */
	private void removerOjPlantonista() {
		OrgaoJulgador orgaoJulgadorPlantao = ParametroUtil.instance().getOrgaoJulgadorPlantao();
		if(orgaoJulgadorPlantao != null && this.orgaoJulgadorList.contains(orgaoJulgadorPlantao)) {
			this.orgaoJulgadorList.remove(orgaoJulgadorPlantao);
		}
	}

	/**
	 * Referência para processo enquadrado na prevenção do art. 260 do Código
	 * Eleitoral.
	 */
	private ProcessoTrf processoPreventoEleicaoOrigem;
	private VinculacaoDependenciaEleitoral vinculacaoDependenciaEleitoral;

	/**
	 * Verifica o enquadramento do processo na prevenção eleitoral conforme agrupamentos
	 * definidos no parâmetro <code>listaAgrupamentosPrevencao260JE</code>.
	 */
	private boolean verificarEnquadramento(ProcessoTrf processo) throws PJeBusinessException  {
		
		if(!isJEeParametroPrevencaoAtivo()){
			return false;
		}
		
		this.processoTrf = processo;
		String parametroListaAgrupamentos = ParametroUtil.instance().getListaAgrupamentoPrevencaoEleicaoOrigem();
		
		List<RegraAgrupamentoPrevencaoEleicaoOrigem> listaAgrupamentos = RegraAgrupamentoPrevencaoEleicaoOrigem.desmembraListaAgrupamentos(parametroListaAgrupamentos);
		
		AgrupamentoClasseJudicial agrupamento = null;
		boolean retorno = false;
		for (RegraAgrupamentoPrevencaoEleicaoOrigem regra : listaAgrupamentos) {
			agrupamento = AgrupamentoClasseJudicialService.getAgrupamentoClasseJudicial(regra.getCodigoAgrupamentoClasseJudicial());
			if (agrupamento == null) {
				throw new IllegalArgumentException("Código de agrupamento da classe e/ou assunto judicial não foi encontrado: " + regra.getCodigoAgrupamentoClasseJudicial());
			}
			
			if (regra.isObrigatoriaPrevencao260CE()) 
			{
				if (regra.isProdutoCartesianoClasseAssunto()) 
				{
					if (isProcessoEnquadradoClasseEAssunto(agrupamento)) 
					{
						retorno = true;
						continue;
					} 
					else 
					{
						return false;
					}
				} 
				else 
				{
					if (isProcessoEnquadradoClasseOuAssunto(agrupamento)) 
					{
						retorno = true;
						continue;
					} 
					else 
					{
						return false;
					}
				}
			}
			else 
			{
				if (regra.isProdutoCartesianoClasseAssunto()) {
					if (isProcessoEnquadradoClasseEAssunto(agrupamento)) {
						return false;
					} 
					else 
					{
						retorno = true;
						continue; //salta para próxima regra
					}						
				} else {
					if (isProcessoEnquadradoClasseOuAssunto(agrupamento)) 
					{
						return false;
					} 
					else 
					{
						retorno = true;
						continue; 
					}						
				}
			}
		}
		return 	retorno;
	}
	
	private boolean isProcessoEnquadradoClasseOuAssunto(AgrupamentoClasseJudicial agrupamento) {
		ProcessoTrf processo = this.processoTrf;
		
		// Se o agrupamento for nulo ou inativo, o processo não se enquandra
		if (agrupamento == null || Boolean.FALSE.equals(agrupamento.getAtivo())) {
			return false;
		}
		for (ClasseJudicialAgrupamento cja : agrupamento.getClasseJudicialAgrupamentoList()) {
			if (cja.getClasse().equals(processo.getClasseJudicial())) {
				return true;
			}
		}
		List<AssuntoTrf> assuntos = new ArrayList<AssuntoTrf>(processo.getAssuntoTrfList());
		
		if (assuntos == null || assuntos.isEmpty()) {
			List<ProcessoAssunto> processoAssuntoList = processo.getProcessoAssuntoList();
			for (ProcessoAssunto processoAssunto : processoAssuntoList) {
				assuntos.add(processoAssunto.getAssuntoTrf());
			}
		}
		
		if(assuntos.isEmpty()){
			return false;
		}
		
		for (AssuntoAgrupamento aa : agrupamento.getAssuntoAgrupamentoList()) {
			for (AssuntoTrf assunto : assuntos) {
				if (aa.getAssunto().equals(assunto)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isProcessoEnquadradoClasseEAssunto(AgrupamentoClasseJudicial agrupamento) {
		ProcessoTrf processo = this.processoTrf;
		
		// Se o agrupamento for nulo ou inativo, o processo não se enquandra
		if (agrupamento == null || Boolean.FALSE.equals(agrupamento.getAtivo())) {
			return false;
		}
		
		List<ClasseJudicialAgrupamento> classesJudiciais = agrupamento.getClasseJudicialAgrupamentoList();
		List<AssuntoAgrupamento> assuntosAgrupamento = agrupamento.getAssuntoAgrupamentoList();
		List<AssuntoTrf> assuntosProcesso = new ArrayList<AssuntoTrf>(processo.getAssuntoTrfList());
		
		if (assuntosProcesso == null || assuntosProcesso.isEmpty()) {
			List<ProcessoAssunto> processoAssuntoList = processo.getProcessoAssuntoList();
			for (ProcessoAssunto processoAssunto : processoAssuntoList) {
				assuntosProcesso.add(processoAssunto.getAssuntoTrf());
			}
		}
		
		if (classesJudiciais == null || assuntosAgrupamento == null || (classesJudiciais.isEmpty() && assuntosAgrupamento.isEmpty())) {
			return false;
		}
		
		if (assuntosProcesso == null || assuntosProcesso.isEmpty()) {
			return false;
		}
		
		for (ClasseJudicialAgrupamento classeJudicialAgrupamento : classesJudiciais) {
			for (AssuntoAgrupamento assuntoAgrupamento : assuntosAgrupamento) {
				for (AssuntoTrf assuntoProcesso : assuntosProcesso) {
					if (classeJudicialAgrupamento.getClasse().equals(processo.getClasseJudicial()) && assuntoAgrupamento.getAssunto().equals(assuntoProcesso)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Verifica o enquadramento do processo na prevenção prevista no art. 260 do
	 * Código Eleitoral conforme classes e assuntos no agrupamento do parâmetro.
	 * <p>
	 * Este método público será utilizado na validação de processos quando na
	 * Justiça Eleitoral, pois será obrigatório a definição do ano de eleição
	 * quando o processo se enquadrar na prevenção eleitoral.
	 * 
	 */
	public boolean verificarEnquadramentoPEO(ProcessoTrf processo)  throws PJeBusinessException {
		if (ParametroJtUtil.instance().justicaEleitoral()) {
 			return verificarEnquadramento(processo);
		}
		return false;
	}
	
	/**
	 * Se não existir vinculo, não existe cadeia formada pela regra da distribuição do art 260
	 * @param processo
	 * @param agrupamento
	 * @return
	 * @throws PJeBusinessException
	 */
	public ProcessoTrf buscarProcessoPrevencaoEleicaoOrigemProcesso(ProcessoTrf processo) throws PJeBusinessException
 	{
		this.vinculacaoDependenciaEleitoral = ComponentUtil.getComponent(VinculacaoDependenciaEleitoralManager.class).recuperaVinculacaoDependencia(processo);
		if(this.vinculacaoDependenciaEleitoral == null)
		{
			return null;
		}
		return buscarProcessoPrevencaoEleicaoOrigem(processo);
	}
	
	/**
	 * Se não existir vinculo, não existe cadeia formada pela regra da distribuição do art 260
	 * @param processo
	 * @param agrupamento
	 * @return
	 * @throws PJeBusinessException
	 */
	public ProcessoTrf buscarProcessoPrevencaoEleicaoOrigemComplemento(ProcessoTrf processo) throws PJeBusinessException {

		this.vinculacaoDependenciaEleitoral = ComponentUtil.getComponent(VinculacaoDependenciaEleitoralManager.class).recuperaVinculacaoDependencia(processo.getComplementoJE());
		if (this.vinculacaoDependenciaEleitoral == null) {
			return null;
		}
		return buscarProcessoPrevencaoEleicaoOrigem(processo);
	}
	
	/**
	 * Este método <strong>não pode ser utilizado diretamente</strong>. Em vez
	 * deste utilize os seguintes métodos públicos:
	 * <ul>
	 * <li>{@link #buscarProcessoPrevencaoEleicaoOrigemComplemento(ProcessoTrf)}</li>
	 * <li>{@link #buscarProcessoPrevencaoEleicaoOrigemProcesso(ProcessoTrf)}</li>
	 * </ul>
	 */
	private ProcessoTrf buscarProcessoPrevencaoEleicaoOrigem(ProcessoTrf processo) throws PJeBusinessException {

		Eleicao eleicao = extrairEleicao(processo);

		/*
		 * Se a eleição não estiver adequadamente definida, esta consulta e a
		 * prevenção devem ser ignoradas.
		 */
		if (eleicao == null) {
			return null;
		}

		Estado estado = null;
		Municipio municipio = null;

		/*
		 * Se o tipo da eleição não for GERAL ou MUNICIPAL, esta consulta e a
		 * prevenção devem ser ignoradas.
		 */
		if (eleicao.isGeral()) {
			estado = processo.getComplementoJE().getEstadoEleicao();
		} else if (eleicao.isMunicipal()) {
			municipio = processo.getComplementoJE().getMunicipioEleicao();
		} else {
			return null;
		}

		
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder jpql = new StringBuilder();

		/*
		 * Realiza uma consulta por processos já distribuídos - exceto aqueles
		 * por prevenção (PP) - e vinculados à mesma eleição e origem do processo
		 * em distribuição e que possua vínculo de dependência eleitoral.
		 */
		jpql.append("select p from ProcessoTrfLogDistribuicao l ")
			.append("inner join l.processoTrf p ")
			.append("inner join p.processoAssuntoList pa ")
			.append("inner join p.complementoJE comp ")
			.append("where comp.eleicao = :eleicao ")
			.append("and comp.vinculacaoDependenciaEleitoral = :vinculo ")
			.append("and p.numeroSequencia != null ")
			.append("and p.processoStatus = :processoStatus ");

		if (eleicao.isGeral()) {
			jpql.append("and comp.estadoEleicao = :origem ");
		} else if (eleicao.isMunicipal()) {
			jpql.append("and comp.municipioEleicao = :origem ");
		}
		jpql.append("order by comp.dtAtualizacao asc ");

		Query query = em.createQuery(jpql.toString());
		query.setParameter("eleicao", eleicao);
		query.setParameter("processoStatus", ProcessoStatusEnum.D);
		query.setParameter("vinculo", this.vinculacaoDependenciaEleitoral);

		if (eleicao.isGeral()) {
			query.setParameter("origem", estado);
		} else if (eleicao.isMunicipal()) {
			query.setParameter("origem", municipio);
		}

		query.setMaxResults(1);

		return (ProcessoTrf) EntityUtil.getSingleResult(query);
	}

	private Eleicao extrairEleicao(ProcessoTrf processo) {
		Eleicao eleicao = null;
		if (processo.getComplementoJE() != null) {
			eleicao = processo.getComplementoJE().getEleicao();
		}
		return eleicao;
	}

	private void distribuirPorEleicaoOrigem() throws Exception {

		logDist.setProcessoTrf(this.processoTrf);

		if(this.processoPreventoEleicaoOrigem == null){
			if(this.processoTrfRedistribuicao != null){
				this.processoPreventoEleicaoOrigem = buscarProcessoPrevencaoEleicaoOrigemComplemento(this.processoTrfRedistribuicao.getProcessoTrf());
			}
			if(this.processoTrf != null){
				this.processoPreventoEleicaoOrigem = buscarProcessoPrevencaoEleicaoOrigemComplemento(this.processoTrf);
			}
		}
		
		ProcessoTrf processoPrevento = this.processoPreventoEleicaoOrigem;
		
		Eleicao eleicaoPrevento = processoTrf.getComplementoJE().getEleicao();
	 	if(eleicaoPrevento.isGeral()){
	 		tipoDistribuicao260 = TipoDistribuicao260.PREVENCAO_ESTADUAL;
	 	}else{
	 		tipoDistribuicao260 = TipoDistribuicao260.PREVENCAO_MUNICIPAL;
	 	}
	 	
	 	this.orgaoJulgadorCargoAnterior = this.processoTrf.getOrgaoJulgadorCargo();
	 	this.orgaoJulgadorColegiadoAnterior = this.processoTrf.getOrgaoJulgadorColegiado();
	 	
		//Define o vinculo para o processo em distribuicao
		definirVinculoDependenciaEleitoralProcessoEmDistribuicao(this.vinculacaoDependenciaEleitoral,this.processoTrf);
		
		//Pegar o cargo do vinculo, pois o processo prevento pode estar com a presidencia no momento da redistribuição
		this.orgaoJulgadorCargoSorteado = this.vinculacaoDependenciaEleitoral.getCargoJudicial();

		// Define dependência para registro de complemento como prevento
		this.processoTrf.setProcessoDependencia(processoPrevento);

		this.processoTrf.setOrgaoJulgador(this.orgaoJulgadorCargoSorteado.getOrgaoJulgador());
		this.processoTrf.setOrgaoJulgadorCargo(this.orgaoJulgadorCargoSorteado);
		this.processoTrf.setCargo(this.orgaoJulgadorCargoSorteado.getCargo());
		this.processoTrf.setOrgaoJulgadorColegiado(processoPrevento.getOrgaoJulgadorColegiado());

		this.logDist.setInTipoDistribuicao(TipoDistribuicaoEnum.PP);
		
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
			"Distribuição por prevenção conforme Art. 260 do Código Eleitoral. Processo paradigma: ", processoPrevento.getNumeroProcesso()));
		
		adicionarLog(this.logDist, CriticidadeEnum.I, criarDescricaoEleicaoOrigem(processoPrevento));
		
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
			"Órgão julgador prevento: ", this.orgaoJulgadorCargoSorteado.getOrgaoJulgador()));
		
		adicionarLog(this.logDist, CriticidadeEnum.I, String.format(
			"Cargo prevento: ", this.orgaoJulgadorCargoSorteado.getDescricao()));

		this.logDist.setOrgaoJulgador(this.orgaoJulgadorCargoSorteado.getOrgaoJulgador());
		this.logDist.setOrgaoJulgadorCargo(this.orgaoJulgadorCargoSorteado);

		EntityUtil.getEntityManager().persist(logDist);

		this.processoTrf.setProcessoStatus(ProcessoStatusEnum.D);
		this.processoTrf.setDataDistribuicao(new Date());
		
		atualizarAcumuladorCargoJudicial(this.orgaoJulgadorCargoSorteado, this.orgaoJulgadorCargoAnterior);
		
		verificarClasseSigilosa();

	}
	
	private void adicionarLog(ProcessoTrfLogDistribuicao logDist, CriticidadeEnum criticidade, String mensagem) {
		ItemsLog item = new ItemsLog();
		item.setProcessoTrfLog(logDist);
		item.setInCriticidade(criticidade);
		item.setItem(mensagem);
		
		logDist.getItemsLogList().add(item);
	}
	
	public enum TipoDistribuicao260 {
 		SORTEIO_MUNICIPAL, SORTEIO_ESTADUAL, PREVENCAO_MUNICIPAL, PREVENCAO_ESTADUAL;
 	}
 		
 	private TipoDistribuicao260 tipoDistribuicao260;
 	
 	public TipoDistribuicao260 getTipoDistribuicao260() {
 		return tipoDistribuicao260;
 	}

	/**
	 * 
	 * Define o vinculo de dependencia eleitoral para o controle de cadeia de processos no complemento do processo em distribuicao
	 * 
	 * @param vinculacaoDependenciaEleitoral
	 * @author eduardo.pereira
	 */
	private void definirVinculoDependenciaEleitoralProcessoEmDistribuicao(VinculacaoDependenciaEleitoral vinculacaoDependenciaEleitoral,ProcessoTrf processo) {
		boolean paradigma = ComponentUtil.getComponent(ComplementoProcessoJEManager.class).isParadigmaExistente(vinculacaoDependenciaEleitoral);
		
		ComplementoProcessoJE complementoProcessoEmDistribuicao = processo.getComplementoJE();
		complementoProcessoEmDistribuicao.setVinculacaoDependenciaEleitoral(vinculacaoDependenciaEleitoral);
		complementoProcessoEmDistribuicao.setDtAtualizacao(new Date());
		complementoProcessoEmDistribuicao.setParadigma(!paradigma);
		
		EntityUtil.getEntityManager().persist(complementoProcessoEmDistribuicao);
	}

	/**
	 * Cria a vinculação de dependencia eleitoral (caso não exista) que identifica o paradigma necessario para identificação de uma cadeia de processos. 
	 * Atualmente (27/03/2013) este vinculo se faz necessario apenas para contemplar a regra de distribuição/redistribuição por prevenção
	 * do art. 260 do CE, onde se faz necessaria a identificação de uma cadeia de processos para que na redistribuição, toda a cadeia seja transferida
	 * para o Cargo do Orgão Julgador sorteado.  
	 */
	private VinculacaoDependenciaEleitoral criarOuRecuperarVinculoDependenciaEleitoral(ProcessoTrf processo) throws PJeBusinessException {
		Eleicao eleicaoPrevento = processo.getComplementoJE().getEleicao();
		
		if(eleicaoPrevento == null){
			throw new PJeBusinessException("O processo não tem eleição casdastrada! Para se enquadrar no art. 260 é necessário informar a eleição!");
		}
		
		VinculacaoDependenciaEleitoral vinculacaoDependenciaEleitoral = ComponentUtil.getComponent(VinculacaoDependenciaEleitoralManager.class).recuperaVinculacaoDependencia(processo.getComplementoJE());
		if(vinculacaoDependenciaEleitoral == null){
			vinculacaoDependenciaEleitoral = new VinculacaoDependenciaEleitoral();
			vinculacaoDependenciaEleitoral.setCargoJudicial(processo.getOrgaoJulgadorCargo());
			vinculacaoDependenciaEleitoral.setEleicao(eleicaoPrevento);
			if(eleicaoPrevento.isGeral()){
				vinculacaoDependenciaEleitoral.setEstado(processo.getComplementoJE().getEstadoEleicao());
				tipoDistribuicao260 = TipoDistribuicao260.SORTEIO_ESTADUAL;
			}else{
				vinculacaoDependenciaEleitoral.setMunicipio(processo.getComplementoJE().getMunicipioEleicao());
				tipoDistribuicao260 = TipoDistribuicao260.SORTEIO_MUNICIPAL;
			}
			EntityUtil.getEntityManager().persist(vinculacaoDependenciaEleitoral);
		}else{
			if(eleicaoPrevento.isGeral()){
				tipoDistribuicao260 = TipoDistribuicao260.PREVENCAO_ESTADUAL;
			}else{
				tipoDistribuicao260 = TipoDistribuicao260.PREVENCAO_MUNICIPAL;
			}
		}
		
		return vinculacaoDependenciaEleitoral;
	}

	private String criarDescricaoEleicaoOrigem(ProcessoTrf processo) {
		StringBuilder sb = new StringBuilder();
		Eleicao eleicao = extrairEleicao(processo);
		sb.append(processo.getComplementoJE().getEstadoEleicao().toString());
		if (eleicao.isMunicipal()) {
			sb.insert(0, ", ");
			sb.insert(0, processo.getComplementoJE().getMunicipioEleicao().toString());
		}
		sb.insert(0, ", ");
		sb.insert(0, eleicao.getAno());
		sb.insert(0, ' ');
		sb.insert(0, eleicao.getTipoEleicao().toString());
		sb.append('.');
		return sb.toString();
	}

	
	/**
	 * Método criado para tratar a redistribuição por prevenção eleitoral (art.
	 * 260 do Código Eleitoral atual).
	 * 
	 * @param processoJudicial
	 *            o processo paradigma
	 * @param competencia
	 *            a competencia
	 * @param usarOrgaoAtual
	 *            variavel booleana para informar para o método utilizar ou não
	 *            o relator atual do processo na redistribuição, se for
	 *            configurada como false o método considera que o relator
	 *            anterior é o relator da cadeia de processos
	 */
	public long redistribuirPrevencaoRecursalEleitoral(ProcessoTrf processoJudicial, Competencia competencia, boolean usarOrgaoAtual,TipoDistribuicaoEnum tipoDistribuicaoEnum,TipoRedistribuicaoEnum tipoRedistribuicaoEnum) throws PJeBusinessException{
		long qtdProcessos = 0L;
		OrgaoJulgadorCargo cargoOrigem = processoJudicial.getOrgaoJulgadorCargo();
		OrgaoJulgadorColegiado colegiadoOrigem = processoJudicial.getOrgaoJulgadorColegiado();
		OrgaoJulgadorCargo ojcSorteado = null;
		OrgaoJulgador orgaoJulgadorSorteado = null;
		OrgaoJulgadorColegiado colegiadoSorteado = null;
		
		if(!usarOrgaoAtual){
			HistoricoDeslocamentoOrgaoJulgador ultimo = ComponentUtil.getHistoricoDeslocamentoOrgaoJulgadorManager().obterHistoricoSemDataRetorno(processoJudicial);
			cargoOrigem = ultimo.getOrgaoJulgadorCargoOrigem();
			colegiadoOrigem = ultimo.getOrgaoJulgadorColegiadoOrigem();
		}

		VinculacaoDependenciaEleitoral vinculoDependencia = ComponentUtil.getComponent(VinculacaoDependenciaEleitoralManager.class).recuperaVinculacaoDependencia(processoJudicial.getComplementoJE());

		if(processoJudicial.getComplementoJE().getVinculacaoDependenciaEleitoral() != null){
			//O processo já existe em uma cadeia, logo ele e todos os processos de sua cadeia devem ser redistribuidos para um OJC sorteado
			ojcSorteado = sortearOrgaoJulgadorCargo(processoJudicial, competencia, tipoRedistribuicaoEnum,cargoOrigem);
			orgaoJulgadorSorteado = ojcSorteado.getOrgaoJulgador();
			colegiadoSorteado = sortearOrgaoJulgadorColegiado(competencia,orgaoJulgadorSorteado);

			Set<Integer> idsProcessosAfetados = new HashSet<Integer>();
			// Encontrar todos os processos conexos
			for(ComplementoProcessoJE complemento: vinculoDependencia.getComplementosProcessoJE()){
				idsProcessosAfetados.add(complemento.getProcessoTrf().getIdProcessoTrf());
			}

			// iterar nos processos redistribuindo um a um para o OJ sorteado de forma assincrona
			qtdProcessos = idsProcessosAfetados.size();
			if(!idsProcessosAfetados.isEmpty()){
				for(Integer idProcesso: idsProcessosAfetados){
					dispararEventoRedistribuicaoPrevencao260(tipoDistribuicaoEnum,tipoRedistribuicaoEnum, cargoOrigem, colegiadoOrigem,ojcSorteado, colegiadoSorteado,idProcesso,qtdProcessos);
				}
			}
		}else{
			qtdProcessos = 1L;
			if(vinculoDependencia != null){
				/*O processo não esta vinculado em nenhuma cadeia, porem pode ser associado a uma existente, logo o processo é redistribuido para o OJC dono
				da cadeia existente e vinculado a mesma*/
				orgaoJulgadorSorteado = vinculoDependencia.getCargoJudicial().getOrgaoJulgador();
				colegiadoSorteado = sortearOrgaoJulgadorColegiado(competencia, orgaoJulgadorSorteado);

				definirVinculoDependenciaEleitoralProcessoEmDistribuicao(vinculoDependencia,processoJudicial);
				EntityUtil.getEntityManager().flush();

				dispararEventoRedistribuicaoPrevencao260(tipoDistribuicaoEnum,tipoRedistribuicaoEnum, cargoOrigem, colegiadoOrigem,vinculoDependencia.getCargoJudicial(), colegiadoSorteado,processoJudicial.getIdProcessoTrf(),qtdProcessos);
			}else{
				/*O processo não esta vinculado em nenhuma cadeia e não existe uma cadeia que se enquadre nos quesitos, logo deve-se sortear o processo e criar uma nova cadeia*/
				ojcSorteado = sortearOrgaoJulgadorCargo(processoJudicial, competencia, tipoRedistribuicaoEnum,cargoOrigem);
				orgaoJulgadorSorteado = ojcSorteado.getOrgaoJulgador();
				colegiadoSorteado = sortearOrgaoJulgadorColegiado(competencia,orgaoJulgadorSorteado);

				vinculoDependencia = criarOuRecuperarVinculoDependenciaEleitoral(processoJudicial);
				definirVinculoDependenciaEleitoralProcessoEmDistribuicao(vinculoDependencia,processoJudicial);
				EntityUtil.getEntityManager().flush();

				dispararEventoRedistribuicaoPrevencao260(tipoDistribuicaoEnum,tipoRedistribuicaoEnum, cargoOrigem, colegiadoOrigem,ojcSorteado, colegiadoSorteado,processoJudicial.getIdProcessoTrf(),qtdProcessos);
			}
		}

		return qtdProcessos;
	}

	/**
	 * Sorteia o cargo do orgão julgador em caso de distribuição ou redistribuição.
	 * A principio o método foi criado para atender a redistribuição por prevenção do art. 260 do Código Eleitoral, mas atende a todas as regras de sorteio do PJE.
	 * @author eduardo.pereira
	 * @param processoJudicial
	 * @param competencia
	 * @param tipoRedistribuicaoEnum
	 * @param cargoOrigem
	 * @return OrgaoJulgadorCargo sorteado 
	 * @throws PJeBusinessException
	 */
	private OrgaoJulgadorCargo sortearOrgaoJulgadorCargo(
			ProcessoTrf processoJudicial,
			Competencia competencia,
			TipoRedistribuicaoEnum tipoRedistribuicaoEnum,
			OrgaoJulgadorCargo cargoOrigem) throws PJeBusinessException {

		OrgaoJulgadorCargoManager orgaoJulgadorCargoManager = ComponentUtil.getOrgaoJulgadorCargoManager();
		List<OrgaoJulgadorCargo> cargosCompetentes = orgaoJulgadorCargoManager.recuperaCompetentes(processoJudicial.getJurisdicao(), competencia);
		//quando o motivo for Impedimento ou Suspeição, excluir o cargo impedido ou suspeito da redistribuição	
		if (TipoRedistribuicaoEnum.I.equals(tipoRedistribuicaoEnum) || 
				TipoRedistribuicaoEnum.S.equals(tipoRedistribuicaoEnum) ||
				TipoRedistribuicaoEnum.N.equals(tipoRedistribuicaoEnum)) {
			
			cargosCompetentes.remove(cargoOrigem);
		}

		// Limitação de pesos
		// Identificar o menor acumulador entre os cargos
		Double piso = orgaoJulgadorCargoManager.recuperaPisoDistribuicao(cargosCompetentes);

		// Recuperar a Distancia Maxima de Distribuição aplicável 
		DistanciaMaximaDistribuicao dist = ComponentUtil.getComponent(DistanciaMaximaDistribuicaoManager.class).recuperarDistancia(cargosCompetentes.size());
		//criar metodo para extrair os cargos excluidos (exceto o origem)
		List<OrgaoJulgadorCargo> cargosExcluidos =  orgaoJulgadorCargoManager.recuperarExcluidos(cargosCompetentes, (piso + dist.getDistanciaMaxima()));
		cargosCompetentes.removeAll(cargosExcluidos);

		// Repescagem
		// loopar entre os excluídos gerando 25% de chance de eles retornarem
		SecureRandom random = new SecureRandom();
		for(OrgaoJulgadorCargo c: cargosExcluidos){
			if(random.nextInt(100) < 25){
				cargosCompetentes.add(c);
			}
		}
		int cargoEscolhido = random.nextInt(cargosCompetentes.size());
		OrgaoJulgadorCargo ojcSorteado = cargosCompetentes.get(cargoEscolhido);
		return ojcSorteado;
	}

	/**
	 * Sorteia o orgão julgador colegiado competente para a distribuição ou redistribuição com base no orgao julgador sorteado e na competencia.
	 * A principio o método foi criado para atender a redistribuição por prevenção do art. 260 do Código Eleitoral, mas atende a todas as regras de sorteio do PJE.
	 * @author eduardo.pereira
	 * @param competencia
	 * @param vinculoDependencia
	 * @return
	 * @throws PJeBusinessException
	 */
	private OrgaoJulgadorColegiado sortearOrgaoJulgadorColegiado(Competencia competencia,OrgaoJulgador orgaoJulgadorSorteado) throws PJeBusinessException {
		SecureRandom random = new SecureRandom();
		OrgaoJulgadorColegiado colegiadoSorteado = null;
		List<OrgaoJulgadorColegiado> colegiadosCompetentes = ComponentUtil.getOrgaoJulgadorColegiadoManager().getColegiadosCompetentes(competencia, orgaoJulgadorSorteado);
		int size = colegiadosCompetentes.size();
		if(size == 0){
			throw new PJeBusinessException("Não há órgãos colegiados vinculados ao cargo competentes para a redistribuição. Verifique sua configuração.");
		}else if(size == 1){
			colegiadoSorteado = colegiadosCompetentes.get(0);
		}else{
			int orgaoEscolhido = random.nextInt(size);
			colegiadoSorteado = colegiadosCompetentes.get(orgaoEscolhido);
		}
		return colegiadoSorteado;
	}

	/**
	 * Dispara o evento de redistribuicao assincrono utilizado na redistribuição em lote exigida pela prevenção do art. 260 do Código Eleitoral.
	 * @author eduardo.pereira 
	 * @param tipoDistribuicaoEnum
	 * @param tipoRedistribuicaoEnum
	 * @param cargoOrigem
	 * @param colegiadoOrigem
	 * @param ojcSorteado
	 * @param colegiadoSorteado
	 * @param idProcesso
	 */
	private void dispararEventoRedistribuicaoPrevencao260(
			TipoDistribuicaoEnum tipoDistribuicaoEnum,
			TipoRedistribuicaoEnum tipoRedistribuicaoEnum,
			OrgaoJulgadorCargo cargoOrigem,
			OrgaoJulgadorColegiado colegiadoOrigem,
			OrgaoJulgadorCargo ojcSorteado,
			OrgaoJulgadorColegiado colegiadoSorteado,
			Integer idProcesso,
			Long qtdProcessos) {

		Usuario usuario = (Usuario) Contexts.getSessionContext().get("usuarioLogado");

		Contexts.getApplicationContext().set("pje:semaforo:redistribuir:lote", true);
		Contexts.getApplicationContext().set("pje:redistribuicao:lote:numeroProcessos", qtdProcessos);
		Contexts.getApplicationContext().set("pje:redistribuicao:lote:orgaoJulgadorSorteado", ojcSorteado.getOrgaoJulgador().getOrgaoJulgador());

		Events.instance().raiseAsynchronousEvent(
				Eventos.REDISTRIBUIR_PROCESSO,
				idProcesso,
				ojcSorteado.getIdOrgaoJulgadorCargo(),
				colegiadoSorteado != null ? colegiadoSorteado.getIdOrgaoJulgadorColegiado() : null,
				cargoOrigem.getIdOrgaoJulgadorCargo(),
				colegiadoOrigem.getIdOrgaoJulgadorColegiado(),
				tipoDistribuicaoEnum,
				tipoRedistribuicaoEnum,
				usuario.getLogin());

	}

	/**
	 * Método criado para tratar a redistribuição por prevenção eleitoral (art. 260 do Código Eleitoral atual).
	 * 
	 * @param processoJudicial o processo paradigma 
	 */
	public long redistribuirPrevencaoRecursalEleitoral(ProcessoTrf processoJudicial, boolean usarOrgaoAtual,TipoDistribuicaoEnum tipoDistribuicaoEnum,TipoRedistribuicaoEnum tipoRedistribuicaoEnum) throws PJeBusinessException{
		Competencia competencia = recuperarCompetenciaProcesso(processoJudicial);
		return redistribuirPrevencaoRecursalEleitoral(processoJudicial, competencia, usarOrgaoAtual,tipoDistribuicaoEnum,tipoRedistribuicaoEnum);
	}

	/**
	 * Metodo criado para recuperar a competencia de um determinado processo em distribuição ou redistribuição
	 * 
	 * @param processoJudicial (ProcessoTrf) O processo em distribuição ou redistribuição que se queira identificar a competencia.
	 * @return competencia  (Competencia) a competencia
	 * @throws PJeBusinessException
	 */
	private Competencia recuperarCompetenciaProcesso(ProcessoTrf processoJudicial) throws PJeBusinessException {
		Competencia competencia = null;
		if(this.getCompetenciaConflito() != null){
			competencia = this.getCompetenciaConflito();
		}else {
			if(processoJudicial.getCompetencia() != null) {
				competencia = processoJudicial.getCompetencia();
			}else {
				List<Competencia> competencias = ComponentUtil.getComponent(DefinicaoCompetenciaService.class).getCompetencias(processoJudicial);
				if(competencias.size() == 0){
					throw new PJeBusinessException("Não há competência configurada para realizar distribuição ou redistribuição. " +
							"Por favor, fale com o administrador para retificar a configuração de competência.");
				}else if(competencias.size() > 1){
					throw new PJeBusinessException("Há uma ambiguidade de competência para realizar distribuição ou redistribuição e nenhuma competencia foi selecionada. " +
							"Por favor, fale com o administrador para retificar a configuração de competência.");
				}
				competencia = competencias.get(0);				
			}
		}

		return competencia;
	}

	/**
	 * Verifica se houve alteração nos dados de processo para retirar processo
	 * da cadeia. [PJEII-10539]
	 * 
	 * @throws PJeBusinessException
	 */
	public boolean verificarPrevencaoArt260(ProcessoTrf processoTrf) throws PJeBusinessException {
		if (!ParametroJtUtil.instance().justicaEleitoral()) {
			return false;
		}

		try {
			boolean is260 = verificarEnquadramentoPEO(processoTrf);
			boolean isEstaNaCadeia = possuiVinculoDependenciaEleitoral(processoTrf);
			// verifica se o processo ainda possui dados (classe ou assunto)
			// para se manter na cadeia e se
			// o mesmo já pertence a uma cadeia
			if (!is260 && isEstaNaCadeia) {
				return true;
			}
		} catch (PJeBusinessException e) {
			log.warn("Erro ao consulta cadeia de Prevenção 206 CE.", e);
			throw e;
		}

		return false;
	}

	/**
	 * Verifica se houve alteração nos dados eleitorias para retirar processo da
	 * cadeia [PJEII-10539]
	 * 
	 * @throws PJeBusinessException
	 */
	public boolean verificarPrevencaoArt260DadosEleitoriais(ProcessoTrf processoTrf) {
		if (!ParametroJtUtil.instance().justicaEleitoral()) {
			return false;
		}
		try {
			boolean is260 = verificarEnquadramentoPEO(processoTrf);
			boolean isEstaNaCadeia = possuiVinculoDependenciaEleitoral(processoTrf);
			// Verifica se o processo possuida dados (classe x assunto) que o
			// credenciam a uma cadeia
			// e se o mesmo foi modificado com dados eleitorais que o retiram da
			// cadeia em que estava
			if (is260 && !isEstaNaCadeia) {
				return true;
			}
		} catch (PJeBusinessException e) {
			log.warn("Erro ao consulta cadeia de Prevenção 206 CE.", e);
		}
		return false;
	}
		
	private void redistribuirProcessoPorImpedimentoOrgaoJulgadorColegiado() throws Exception {
		distribuir();
	}
	
	public boolean possuiVinculoDependenciaEleitoral(ProcessoTrf processoTrf)throws PJeBusinessException {
		VinculacaoDependenciaEleitoral vinculoDependencia = ComponentUtil.getComponent(VinculacaoDependenciaEleitoralManager.class).recuperaVinculacaoDependencia(processoTrf);
		return vinculoDependencia != null;
	}
	
	/** 
	 * Verifica se o processo esta com o status de distribuido.
	 * 
	 * @return Verdadeiro se o processo está com o status de distribuído. Falso, caso contrário.
	 * @author eduardo.pereira@tse.jus.br 
	 */
	private boolean isProcessoDistribuido(ProcessoTrf processoTrf){
		return ProcessoStatusEnum.D.equals(processoTrf.getProcessoStatus());
	}
	
	/**
	 * Função chamada quando há necessidade de se adequar o peso do processo no OJ atual dele, sem necessidade de haver uma redistribuição do processo
	 * usado por exemplo: quando o processo é retificado
	 * @param processoTrf
	 * @throws Exception 
	 */
	public void atualizarAcumuladoresProcessoRetificado(ProcessoTrf processoTrf) throws Exception{
		limparService();
		this.processoTrf = processoTrf;

		OrgaoJulgadorCargo orgaoJulgadorCargo = processoTrf.getOrgaoJulgadorCargo();
		this.atualizarAcumuladorCargoJudicial(orgaoJulgadorCargo, orgaoJulgadorCargo);
	}
	
	/**
	 * Este método deve ser chamado ao retificar um processo prevento pelo art.
	 * 260 do CE de forma a retirar o processo da cadeia demarcada pela entidade
	 * VinculacaoDependenciaEleitoral.
	 * 
	 * Caso o processo tenha vinculo de dependencia eleitoral, ou seja, esteja
	 * em uma cadeia de processos preventos de acordo com o art.260/CE, e foi
	 * retificado onde teve suas caracteristicas alteradas de forma que o mesmo
	 * não se enquadre mais nas regras de agrupamento de classes e assuntos
	 * definidos no parametro 'listaAgrupamentosPrevencao260JE', o metodo irá
	 * retirar a vinculação deste processo. Se a cadeia não tiver mais processos
	 * vinculados, a cadeia deixará de existir. Como um processo neste tipo de
	 * distribuição não recebe peso, o mesmo terá peso adicionado caso saia da
	 * cadeia de processos. Caso o processo ainda esteja em elaboração, a regra
	 * não é verificada pois ainda não existe vinculo de dependencia eleitoral
	 * criado e associado ao processo.
	 * 
	 * @author eduardo.pereira
	 * @throws PJeBusinessException
	 * @param ProcessoTrf o processo que esta sendo retificado
	 */
	public void retificarVinculacaoDependenciaEleitoral(ProcessoTrf processoTrf) throws Exception{
		if(ParametroJtUtil.instance().justicaEleitoral() && isProcessoDistribuido(processoTrf)) {
			if(!this.verificarEnquadramentoPEO(processoTrf)){
				retirarProcessoPreventoArt260CEDaCadeia(processoTrf);
			}
		}
	}
	
	/**
	 * Retira um processo eleitoral distribuido sob as regras de prevenção
	 * definidas no art.260/CE.
	 * 
	 * Alem da retirada do processo da cadeia, o metodo faz a limpeza de cadeia
	 * de processos vazia e reajusta o peso do processo, pois processos
	 * distribuidos por este tipo de prevenção não recebem peso na
	 * distribuição.
	 * 
	 * @param processoTrf
	 * @throws PJeBusinessException
	 */
	public void retirarProcessoPreventoArt260CEDaCadeia(ProcessoTrf processoTrf) throws Exception {
		VinculacaoDependenciaEleitoralManager vinculacaoDependenciaEleitoralManager = ComponentUtil.getComponent(VinculacaoDependenciaEleitoralManager.class);
		VinculacaoDependenciaEleitoral vinculacaoDependenciaEleitoral = vinculacaoDependenciaEleitoralManager.recuperaVinculacaoDependencia(processoTrf);
		if(vinculacaoDependenciaEleitoral != null){
			List<ProcessoTrf> cadeiaDeProcessosVinculados = vinculacaoDependenciaEleitoralManager.recuperarProcessosAssociadosVinculacaoDependencia(vinculacaoDependenciaEleitoral);
			if(cadeiaDeProcessosVinculados != null && cadeiaDeProcessosVinculados.contains(processoTrf)){
				ComplementoProcessoJE complementoProcessoJE = processoTrf.getComplementoJE();
				complementoProcessoJE.setVinculacaoDependenciaEleitoral(null);
				complementoProcessoJE.setParadigma(Boolean.FALSE);
				EntityUtil.getEntityManager().merge(complementoProcessoJE);
				EntityUtil.getEntityManager().flush();

				cadeiaDeProcessosVinculados.remove(processoTrf);
				if(cadeiaDeProcessosVinculados.isEmpty()){
					EntityUtil.getEntityManager().remove(vinculacaoDependenciaEleitoral);
					EntityUtil.getEntityManager().flush();
				}
				OrgaoJulgadorCargo orgaoJulgadorCargo = processoTrf.getOrgaoJulgadorCargo();
				this.atualizarAcumuladorCargoJudicial(orgaoJulgadorCargo, null);
				atualizarComplementoJENovoProcessoParadigma(vinculacaoDependenciaEleitoral);
				
			}
		}
	}

	/**
	 * Dada uma cadeia coloca o primeiro processo como paradigma.
	 * @param vinculacaoDependenciaEleitoral
	 */
	private void atualizarComplementoJENovoProcessoParadigma(VinculacaoDependenciaEleitoral vinculacaoDependenciaEleitoral) {
		ComplementoProcessoJE novoParadigma = ComponentUtil.getComponent(ComplementoProcessoJEManager.class).recuperarComplementoParadigma(vinculacaoDependenciaEleitoral);
		
		if(novoParadigma != null){
			novoParadigma.setParadigma(Boolean.TRUE);
			EntityUtil.getEntityManager().merge(novoParadigma);
			EntityUtil.getEntityManager().flush();
		}
	}
	
	public Boolean getCompensacaoPesoEleitoralAtivo() {
 		if(compensacaoPesoEleitoralAtivo == null){
 			String parametro = ComponentUtil.getParametroService().valueOf(Parametros.ELEITORAL_ATIVA_COMPENSACAO);
 			compensacaoPesoEleitoralAtivo = Boolean.valueOf(parametro);
 		}
 		return compensacaoPesoEleitoralAtivo;
 	}

	public List<AssuntoTrf> getAssuntoTrfList() {
		return assuntoTrfList;
	}

	public void setAssuntoTrfList(List<AssuntoTrf> assuntoTrfList) {
		this.assuntoTrfList = assuntoTrfList;
	}
	
	public boolean isTaskInstanceAtualAberta() {
		TaskInstance taskInstanceAtual = org.jboss.seam.bpm.TaskInstance.instance();
		if(taskInstanceAtual!=null && taskInstanceAtual.isOpen()) {
			return true;
		} else {
			return false;
		}
	}
}
