package br.com.infox.pje.manager;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.LockModeType;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.quartz.CronTrigger;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.dao.ProcessoAudienciaDAO;
import br.com.infox.utils.Constantes;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.manager.SalaManager;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.PrazoMinimoMarcacaoAudienciaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.servicos.DateService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService.MovimentoBuilder;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ReservaHorario;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.SalaHorario;
import br.jus.pje.nucleo.entidades.SalaReservaHorario;
import br.jus.pje.nucleo.entidades.TipoAudiencia;
import br.jus.pje.nucleo.enums.EtapaAudienciaEnum;
import br.jus.pje.nucleo.enums.StatusAudienciaEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;


@Name(ProcessoAudienciaManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ProcessoAudienciaManager extends BaseManager<ProcessoAudiencia> {

	public static final String NAME = "processoAudienciaManager";

	private static final int UM_SEGUNDO = 1;

	private final String TIPO_DESIGNACAO_AUDIENCIA_MANUAL = "M";

	@In
	private ProcessoAudienciaDAO processoAudienciaDAO;
		
	private PrazosProcessuaisService prazosProcessuaisService;
	
	@In
	private SalaManager salaManager;
	
	@In
	private ProcessoParteManager processoParteManager;
	
	@In
	private SalaReservaHorarioManager salaReservaHorarioManager;

	@Logger
	private Log log;
	
	@Override
	protected BaseDAO<ProcessoAudiencia> getDAO() {
		return processoAudienciaDAO;
	}

	public long totalAcordosHomologadosJuiz(String dataInicial, String dataFinal, OrgaoJulgador oj, String juiz) {
		return processoAudienciaDAO.totalAcordosHomologadosJuiz(dataInicial, dataFinal, oj, juiz);
	}

	public double valorAcordosHomologadosJuiz(String dataInicial, String dataFinal, OrgaoJulgador oj, String juiz) {
		return processoAudienciaDAO.valorAcordosHomologadosJuiz(dataInicial, dataFinal, oj, juiz);
	}

	public long totalAcordosHomologados(String dataInicial, String dataFinal, OrgaoJulgador oj) {
		return processoAudienciaDAO.totalAcordosHomologados(dataInicial, dataFinal, oj);
	}

	public double valorAcordosHomologados(String dataInicial, String dataFinal, OrgaoJulgador oj) {
		return processoAudienciaDAO.valorAcordosHomologados(dataInicial, dataFinal, oj);
	}
	
	public List<ProcessoAudiencia> getAudienciasMarcadas(Sala sala, Calendar dataInicio) {
		return processoAudienciaDAO.getAudienciasMarcadas(sala, dataInicio.getTime());
	}
	
	public void setProcessoAudienciaDAO(ProcessoAudienciaDAO processoAudienciaDAO) {
		this.processoAudienciaDAO = processoAudienciaDAO;
	}
		
	public ProcessoAudienciaDAO getProcessoAudienciaDAO() {
		return processoAudienciaDAO;
	}
	
	private List<ProcessoAudiencia> getAudienciasDia(List<ProcessoAudiencia> todas, Calendar dia) {
		List<ProcessoAudiencia> audienciasDia = new ArrayList<ProcessoAudiencia>();

		Calendar diaProcurado = Calendar.getInstance();
		diaProcurado.set(dia.get(Calendar.YEAR), dia.get(Calendar.MONTH), dia.get(Calendar.DAY_OF_MONTH));
				
		int audienciasSize = todas.size();
		for (int i=0; i < audienciasSize; i++) {
			ProcessoAudiencia audienciaDia = todas.get(i);
			
			if (StatusAudienciaEnum.M.equals(audienciaDia.getStatusAudiencia())) {

				Calendar audienciaCalendar = (Calendar) diaProcurado.clone();
				Calendar aux = Calendar.getInstance();
				aux.setTime(audienciaDia.getDtInicio());
				audienciaCalendar.set(aux.get(Calendar.YEAR), aux.get(Calendar.MONTH), aux.get(Calendar.DAY_OF_MONTH));

				if (audienciaCalendar.before(diaProcurado)) {
					todas.remove(0);
					i--;
					audienciasSize--;
				} else if (audienciaCalendar.equals(diaProcurado)) {
					audienciasDia.add(audienciaDia);
				} else {
					break;
				}
			}
		}
		
		return audienciasDia;
	}
	
	public Calendar verificaJanelaHorario(SalaHorario periodo, List<ProcessoAudiencia> audiencias, Calendar dtMarcacao, int tempoAudiencia) {
		Calendar retorno = null;
		int intervalo = DateUtil.getIntervaloEntreDatas(periodo.getHoraFinal(), periodo.getHoraInicial());
		
		Calendar horarioInicio = Calendar.getInstance();
		horarioInicio.setTime(periodo.getHoraInicial());
		horarioInicio.set(dtMarcacao.get(Calendar.YEAR), dtMarcacao.get(Calendar.MONTH), dtMarcacao.get(Calendar.DAY_OF_MONTH));
			
		if (horarioInicio.before(dtMarcacao)) {
			horarioInicio.setTime(dtMarcacao.getTime());
		}
		
		Calendar horarioFinalSala = Calendar.getInstance();
		horarioFinalSala.setTime(periodo.getHoraFinal());
		horarioFinalSala.set(dtMarcacao.get(Calendar.YEAR), dtMarcacao.get(Calendar.MONTH), dtMarcacao.get(Calendar.DAY_OF_MONTH));

		Calendar hoje = Calendar.getInstance();
		hoje.setTime(DateUtil.getBeginningOfToday());
		
		if(dtMarcacao.equals(hoje)){
			horarioInicio.setTime(new Date());
		}
		
		Calendar horarioFim = Calendar.getInstance();
		horarioFim.setTime(horarioInicio.getTime());
		horarioFim.add(Calendar.MINUTE, tempoAudiencia);

		if (intervalo >= tempoAudiencia) {
			for(ProcessoAudiencia pa : audiencias){
				if (!(DateUtil.isDataComHoraEntre(pa.getDtInicio(), horarioInicio.getTime(), horarioFim.getTime())
						|| DateUtil.isDataComHoraEntre(pa.getDtFim(), horarioInicio.getTime(), horarioFim.getTime())
						|| DateUtil.isDataComHoraEntre(horarioInicio.getTime(), pa.getDtInicio(), pa.getDtFim())
						|| DateUtil.isDataComHoraEntre(horarioFim.getTime(), pa.getDtInicio(), pa.getDtFim()))) {
					continue;
				}
				if(tempoAudiencia <= Math.abs(DateUtil.getIntervaloEntreDatas(horarioInicio.getTime(), pa.getDtInicio())) && (horarioFim.compareTo(horarioFinalSala) <= 0) &&  pa.getDtInicio().after(horarioInicio.getTime())) {
					retorno = horarioInicio; 
				} else {
					horarioInicio.setTime(pa.getDtFim());
					horarioFim.setTime(horarioInicio.getTime());
					horarioFim.add(Calendar.MINUTE, tempoAudiencia);
				}
			}
			if(retorno == null && tempoAudiencia <= DateUtil.getIntervaloEntreDatas(horarioFinalSala.getTime(), horarioInicio.getTime())) {
				retorno = horarioInicio;
			}
		}
		if(retorno != null && retorno.getTime().after(horarioFinalSala.getTime())) {
			retorno = null;
		}
		return retorno;
	}
	
	public ProcessoAudiencia procurarPrimeiroHorarioDisponivel(Sala sala, Date dataInicioPesquisa, Integer tempoAudiencia){
		return procurarPrimeiroHorarioDisponivel(sala, dataInicioPesquisa, tempoAudiencia, null);
	}

	public ProcessoAudiencia procurarPrimeiroHorarioDisponivel(Sala sala, Date dataInicioPesquisa, int tempoAudiencia, Integer distanciaMaxima) {
		ProcessoAudiencia audiencia = null;
		Date dtMarcacaoFim  = null;
		
		Calendar dtMarcacao = Calendar.getInstance();
		dtMarcacao.setTime(dataInicioPesquisa);
		
		Calendar dataLimite = Calendar.getInstance();
		dataLimite.setTime(dataInicioPesquisa);
		
		if (distanciaMaxima != null){
			dataLimite.add(Calendar.DAY_OF_YEAR, distanciaMaxima);
		} else {
			dataLimite.add(Calendar.YEAR, 2);
		}
		
		boolean existeHorarioCompativel = verificaExistenciaHorarioCompativel(sala.getSalaHorarioList(), tempoAudiencia);
		
		if (existeHorarioCompativel) {
			boolean audMarcada = false;
			while (!audMarcada && DateUtil.getDataSemHora(dtMarcacao.getTime()).before(dataLimite.getTime())) {
				Calendar dataForaDeBloqueio = null;
				Calendar horarioInicio = null;				
				if (sala.getIgnoraFeriado() || ehDiaUtilJudicial(dtMarcacao, sala.getOrgaoJulgador())) {
					List<SalaHorario> horariosDia = sala.getSalaHorarioDiaList(dtMarcacao.get(Calendar.DAY_OF_WEEK));
					dtMarcacaoFim = DateUtil.getEndOfDay(dtMarcacao.getTime());
					List<ProcessoAudiencia> audienciasDia = procurarAudienciasDesignadasPorPeriodo(dtMarcacao.getTime(),dtMarcacaoFim, sala);
					for (SalaHorario salaHorario : horariosDia) {
						if (!salaHorario.getAtivo()) {
							continue;
						}						
						horarioInicio = verificaJanelaHorario(salaHorario, audienciasDia, dtMarcacao, tempoAudiencia);
						dataForaDeBloqueio = salaManager.verificaBloqueio(sala, horarioInicio, tempoAudiencia);
						// Se a data estiver bloqueada, avanço para a data fora de bloqueio.
						if(dataForaDeBloqueio != null && !dataForaDeBloqueio.equals(horarioInicio)){
							break;
						}
						if (horarioInicio != null && !isChoqueHorarioAdvogado(horarioInicio.getTime(), tempoAudiencia)) {
							audiencia = new ProcessoAudiencia();
							audiencia.setSalaAudiencia(sala);
							audiencia.setDtInicio(horarioInicio.getTime());
							horarioInicio.add(Calendar.MINUTE, tempoAudiencia);
							audiencia.setDtFim(horarioInicio.getTime());
							audMarcada = true;
							break;
						}
					}
				}
				// Se a data estiver bloqueada, avanço para a data fora de bloqueio.
				if(dataForaDeBloqueio != null && !DateUtil.isDataIgual(dataForaDeBloqueio.getTime(), horarioInicio.getTime())){
					dtMarcacao = dataForaDeBloqueio;
				}else{
					//Se não, passa para o próximo dia.
					dtMarcacao.setTime(DateUtil.adicionarTempoData(DateUtil.getDataSemHora(dtMarcacao.getTime()), Calendar.DAY_OF_YEAR, 1));
				}				
			}
		}
		return audiencia;
	}

	private boolean isChoqueHorarioAdvogado(Date dtMarcacao, int tempoAudiencia) {
		List<ProcessoParte> advogados = processoParteManager.recuperarAdvogados(ProcessoTrfHome.instance().getInstance());
		for (ProcessoParte advogado : advogados) {
			if (!recuperarAudienciasMarcadas(
					advogado.getPessoa(), DateUtil.adicionarTempoData(dtMarcacao, Calendar.SECOND, UM_SEGUNDO), DateUtil.adicionarTempoData(dtMarcacao, Calendar.MINUTE, tempoAudiencia)).isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	public Map<ProcessoTrf, Pessoa> getAdvogadosChoqueHorario(Date dtMarcacao, int tempoAudiencia) {
		List<ProcessoAudiencia> processosAudiencia = null;
		Map<ProcessoTrf, Pessoa> advogadosChoqueHorario = new HashMap<ProcessoTrf, Pessoa>(0);
		
		List<ProcessoParte> advogados = processoParteManager.recuperarAdvogados(ProcessoTrfHome.instance().getInstance());
		for (ProcessoParte advogado : advogados) {
			processosAudiencia = recuperarAudienciasMarcadas(
					advogado.getPessoa(), DateUtil.adicionarTempoData(dtMarcacao, Calendar.SECOND, UM_SEGUNDO), DateUtil.adicionarTempoData(dtMarcacao, Calendar.MINUTE, tempoAudiencia));
			
			if (!processosAudiencia.isEmpty()) {
				for (ProcessoAudiencia processoAudiencia : processosAudiencia) {
					advogadosChoqueHorario.put(processoAudiencia.getProcessoTrf(), advogado.getPessoa());
				}
			}
		}
		return advogadosChoqueHorario;
	}
	
	/**
	 * Método responsável por recuperar as audiências que foram marcadas para uma pessoa em um determinado período.
	 * 
	 * @param pessoa Pessoa.
	 * @param dataInicio Data de início da marcação
	 * @param dataFim Data fim da marcação
	 * @return Audiências que foram marcadas para uma pessoa em um determinado período.
	 */
	public List<ProcessoAudiencia> recuperarAudienciasMarcadas(Pessoa pessoa, Date dataInicio, Date dataFim) {
		 return processoAudienciaDAO.recuperarAudienciasMarcadas(pessoa, dataInicio, dataFim);
	}
	
	private boolean verificaExistenciaHorarioCompativel(List<SalaHorario> horarioList, int tempoAudiencia) {		
		boolean existeHorarioCompativel = false;
		for (SalaHorario salaHorario : horarioList) {
			int intervalo = DateUtil.getIntervaloEntreDatas(salaHorario.getHoraFinal(), salaHorario.getHoraInicial());
			if (intervalo >= tempoAudiencia) {
				existeHorarioCompativel = true;
				break;
			}
		}
		return existeHorarioCompativel;
	}
	
	/**
	 * Verifica se em um determinado dia é feriado para o órgão julgador do processo atual
	 */
	private boolean ehDiaUtilJudicial(Calendar dia, OrgaoJulgador orgaoJulgador){		
		return prazosProcessuaisService.ehDiaUtilJudicial(dia.getTime(), orgaoJulgador);
	}
	
	public void setPrazosProcessuaisService(PrazosProcessuaisService pps) {
		this.prazosProcessuaisService = pps;
	}
	
	public void setSalaManager(SalaManager salaManager) {
		this.salaManager = salaManager;
	}
	
	
	/**
	 * Procurar todas as Salas do Órgão Julgador com audiência marcada em um intervalo de datas
	 * @param orgaoJulgador
	 * @param dataInicio
	 * @param tipoAudiencia
	 * @return
	 */
	public List<ProcessoAudiencia> procurarSalasComAudienciaMarcadaPorDia(OrgaoJulgador orgaoJulgador, Date dataInicio, Date dataFim, Sala sala) {
		return processoAudienciaDAO.procurarSalasComAudienciaMarcadaPorDia(orgaoJulgador, dataInicio, dataFim, sala);
	}

	/**
	 * Procurar todas as Salas do Órgão Julgador com audiência marcada na data especificada
	 * @param orgaoJulgador
	 * @param dataInicio
	 * @param tipoAudiencia
	 * @return
	 */
	public List<ProcessoAudiencia> procurarSalasComAudienciaMarcadaPorDia(OrgaoJulgador orgaoJulgador, Date dataInicio, TipoAudiencia tipoAudiencia) {
		return processoAudienciaDAO.procurarSalasComAudienciaMarcadaPorDia(orgaoJulgador, dataInicio, tipoAudiencia);
	}
	
	/**
	 * 
	 * Método responsável por recuperar as audiências agendadas para uma sala e data especifica.
	 * @param orgaoJulgador
	 * @param dataInicio Data de início da pesquisa
	 * @param tipoAudiencia 
	 * @param sala Sala onde as audiências foram marcadas
	 * @return Lista de audiências designadas no período
	 */
	public List<ProcessoAudiencia> procurarSalaEspecificaComAudienciaMarcadaPorDia(
			OrgaoJulgador orgaoJulgador, Date dataInicio, TipoAudiencia tipoAudiencia, Sala sala) {
		return processoAudienciaDAO.procurarSalaEspecificaComAudienciaMarcadaPorDia(orgaoJulgador, dataInicio, tipoAudiencia,sala);
	}
	
	/**
	 * PJEII-13547
	 * Método responsável por recuperar as audiências designadas no período do bloqueio de pauta
	 * @param dataInicio Data de início da pesquisa
	 * @param dataFim Data de fim da pesquisa 
	 * @param salaAudiencia Sala onde as audiências foram marcadas
	 * @return Lista de audiências designadas no período
	 */
	public List<ProcessoAudiencia> procurarAudienciasDesignadasPorPeriodo(Date dataInicio, Date dataFim, Sala salaAudiencia) {
		return processoAudienciaDAO.procurarAudienciasDesignadasPorPeriodo(dataInicio, dataFim, salaAudiencia);
	}

	public List<ProcessoAudiencia> obterHorariosLivres(ProcessoTrf processoTrf, TipoAudiencia tipoAudiencia, Sala sala,
			Date dataInicioPesquisa, Boolean buscarHorariosReservados, Integer duracao,
			EtapaAudienciaEnum etapaAudiencia) throws PJeBusinessException {

		List<ProcessoAudiencia> horariosLivres = new ArrayList<ProcessoAudiencia>(0);
		List<Sala> salas = null;
		this.setPrazosProcessuaisService(ComponentUtil.getPrazosProcessuaisService());
		boolean isHorarioEncontrado = true;
		boolean modoMarcacaoSequencial = "sequencial"

				.equals(ComponentUtil.getParametroService().valueOf(Parametros.MODO_MARCACAO_AUTOMATICA));

		Date dataMaximaPesquisa = DateUtil.adicionarTempoData(DateService.instance().getDataHoraAtual(), Calendar.YEAR,
				2);
		if (processoTrf == null || tipoAudiencia == null || (duracao == null || duracao <= 0)) {
			throw new IllegalArgumentException("Argumento(s) inválido(s).");
		}

		Integer prazoMinimoMarcacaoAudiencia = this.getPrazoMinimoMarcacaoAudiencia(processoTrf, tipoAudiencia);

		Date dataAtual = DateUtil.zerarSegundos(DateService.instance().getDataHoraAtual());
		if (dataInicioPesquisa != null && dataInicioPesquisa.before(dataAtual)) {
			dataInicioPesquisa = dataAtual;
		} else if (dataInicioPesquisa == null) {
			dataInicioPesquisa = DateUtil.getBeginningOfDay(
					DateUtil.adicionarTempoData(dataAtual, Calendar.DAY_OF_YEAR, prazoMinimoMarcacaoAudiencia));
		}

		salas = obterSalas(processoTrf, tipoAudiencia, sala);

		while (!this.prazosProcessuaisService.ehDiaUtilJudicial(dataInicioPesquisa, processoTrf.getOrgaoJulgador())) {
			if (DateUtil.isDataMenorIgual(dataInicioPesquisa, dataMaximaPesquisa)) {
				dataInicioPesquisa = DateUtil.dataMaisDias(dataInicioPesquisa, 1);
			} else {

				isHorarioEncontrado = false;
				break;

			}

		}

		if (!isHorarioEncontrado) {
			throw new PJeBusinessException("Sala não cadastrada para este órgao julgador.");
		}
		if (buscarHorariosReservados) {
			encontrarHorariosReservados(sala, salas, dataInicioPesquisa, horariosLivres, duracao);

		} else if (modoMarcacaoSequencial && sala == null) {
			Set<Sala> salasAudienciasMarcadas = new HashSet<Sala>(0);
			List<ProcessoAudiencia> audienciasMarcadas = null;

			while (horariosLivres.isEmpty() && DateUtil.isDataMenorIgual(dataInicioPesquisa, dataMaximaPesquisa)) {
				audienciasMarcadas = this.processoAudienciaDAO.procurarSalasComAudienciaMarcadaPorDia(
						processoTrf.getOrgaoJulgador(), dataInicioPesquisa, tipoAudiencia);

				if (etapaAudiencia == null) {

					if (!audienciasMarcadas.isEmpty()) {
						salasAudienciasMarcadas.addAll(audienciasMarcadas.stream().parallel()
								.map(ProcessoAudiencia::getSalaAudiencia).collect(Collectors.toList()));

						encontrarHorarios(salasAudienciasMarcadas, dataInicioPesquisa, true, horariosLivres, duracao);

					}
				}
				if (horariosLivres.isEmpty()) {
					encontrarHorarios(salas, dataInicioPesquisa, false, horariosLivres, duracao);
				}

				if (horariosLivres.isEmpty()) {
					dataInicioPesquisa = DateUtil.dataMaisDias(dataInicioPesquisa, 1);
				}
			}
			this.ordenarAudienciasPorData(horariosLivres, false);
			if (etapaAudiencia == null) {
				ordenarAudienciasPorSalaComMaisDesignacoes(horariosLivres);
				ordenarAudienciaPorDiaSemHora(horariosLivres);
			}

		} else {
			if (horariosLivres.isEmpty()) {
				encontrarHorarios(salas, dataInicioPesquisa, false, horariosLivres, duracao);
				this.ordenarAudienciasPorData(horariosLivres, false);
			}
		}

		if (horariosLivres.isEmpty()) {
			throw new PJeBusinessException("Não foi encontrado horário disponível.");
		}

		return horariosLivres;
	}

	private void  encontrarHorariosReservados(Sala salaAudienciaTemp, List<Sala> salasDesejadas,Date dataInicioPesquisa ,List<ProcessoAudiencia> horariosEncontrados,Integer tempoAudiencia)
	{
		
		
		List<SalaReservaHorario> salasReservas = salaReservaHorarioManager.findBySala(salaAudienciaTemp, true);
		for (SalaReservaHorario salaReservaHorario : salasReservas) {
			ProcessoAudiencia audienciaDisponivel = getProximaAudienciaPautaEspecifica(null, salaReservaHorario.getReservaHorario(), salasDesejadas, DateUtil.getCalendarFromDate(dataInicioPesquisa),tempoAudiencia);
			if (audienciaDisponivel != null) {						
				audienciaDisponivel.setIdentificadorPautaEspecifica(salaReservaHorario.getIdentificadorReservaHorario());
				horariosEncontrados.add(audienciaDisponivel);
			}
		}
   
		
		
	}
	
	
	private void encontrarHorarios(Collection<Sala> salasAudiencias, Date dataAudienciaDesejada, boolean somenteDataDesejada,List<ProcessoAudiencia> horariosEncontrados,Integer tempoAudiencia) {		
		Calendar dataLimite = DateUtil.getCalendarFromDate(DateService.instance().getDataHoraAtual());
		dataLimite.add(Calendar.YEAR, 2);
		for (Sala sala : salasAudiencias) {
			Calendar dataAudienciaDesejadaAuxiliar = DateUtil.getCalendarFromDate(dataAudienciaDesejada);
			ProcessoAudiencia audienciaDisponivel = null;
			while (audienciaDisponivel == null || audienciaDisponivel.getDtInicio().before(dataLimite.getTime())){
				audienciaDisponivel = procurarPrimeiroHorarioDisponivel(sala, dataAudienciaDesejadaAuxiliar.getTime(), tempoAudiencia);
				if (audienciaDisponivel == null || (somenteDataDesejada && !DateUtil.isDataIgual(audienciaDisponivel.getDtInicio(),  dataAudienciaDesejada))){
					break;
				}
				if (isHorarioReservado(audienciaDisponivel)){
					dataAudienciaDesejadaAuxiliar.setTime(audienciaDisponivel.getDtFim());
				} else {
					horariosEncontrados.add(audienciaDisponivel);
					break;			    		
				}
			}
		}		
	}

	public ProcessoAudiencia procurarPrimeiroHorarioDisponivel(ProcessoTrf processoTrf, Sala sala, Date dataInicioPesquisa, int tempoAudiencia){
		ProcessoAudiencia audiencia = null;
		Calendar dtMarcacao = Calendar.getInstance();
		dtMarcacao.setTime(dataInicioPesquisa);
		
		Calendar dataLimite = Calendar.getInstance();
		dataLimite.setTime(dataInicioPesquisa);
		dataLimite.add(Calendar.YEAR, 2);

		if (verificaExistenciaHorarioCompativel(sala.getSalaHorarioList(), tempoAudiencia)) {
			List<ProcessoAudiencia> audienciasMarcadas = getAudienciasMarcadas(sala, dtMarcacao);
			boolean audMarcada = false;
			while (!audMarcada && dtMarcacao.before(dataLimite)) {
				Calendar dataForaDeBloqueio = null;
				Calendar horarioInicio = null;
				if (sala.getIgnoraFeriado() || ehDiaUtilJudicial(dtMarcacao, sala.getOrgaoJulgador())) {
					dtMarcacao = salaManager.getDataForaDeInatividades(sala, dtMarcacao);
					if(!ehDiaUtilJudicial(dtMarcacao, sala.getOrgaoJulgador())) {
						continue;
					}
					List<SalaHorario> horariosDia = salaManager.getSalaHorarioDiaList(sala, dtMarcacao.get(Calendar.DAY_OF_WEEK));
					List<ProcessoAudiencia> audienciasDia = getAudienciasDia(audienciasMarcadas, dtMarcacao);
					
					for (SalaHorario salaHorario : horariosDia) {
						horarioInicio = verificaJanelaHorario(salaHorario, audienciasDia, dtMarcacao, tempoAudiencia);
						dataForaDeBloqueio = salaManager.verificaBloqueio(sala, horarioInicio, tempoAudiencia);
						if (dataForaDeBloqueio != null && dataForaDeBloqueio.compareTo(horarioInicio) != 0) {
							break;
						}
						if (horarioInicio != null && !isChoqueHorarioAdvogado(processoTrf, horarioInicio.getTime(), tempoAudiencia)) {
							audiencia = new ProcessoAudiencia();
							audiencia.setSalaAudiencia(sala);
							audiencia.setDtInicio(horarioInicio.getTime());
							horarioInicio.add(Calendar.MINUTE, tempoAudiencia);
							audiencia.setDtFim(horarioInicio.getTime());
							audMarcada = true;
							break;
						}
					}
				}
				if (dataForaDeBloqueio != null && dataForaDeBloqueio.compareTo(horarioInicio) != 0) {
					dtMarcacao = dataForaDeBloqueio;
				}else{
					dtMarcacao.setTime(DateUtil.adicionarTempoData(DateUtil.getBeginningOfDay(dtMarcacao.getTime()), Calendar.DAY_OF_YEAR, 1));
				}
			}
		}
		return audiencia;
	}
	
	/**
	 * Método responsável por listar as audiências abertas por processo a
	 * partir de uma determinada data
	 * 
	 * @param processo
	 *            {@link ProcessoTrf} a ser pesquisado.
	 * 
	 * @param aPartirDe
	 *            {@link Date} com a data a partir para pesquisa.
	 * 
	 * @return {@link List} de {@link ProcessoAudiencia} com as audiências
	 *         abertas/agendadas para o processo
	 */
	public List<ProcessoAudiencia> procurarAudienciasAbertasPorProcesso(ProcessoTrf processo, Date aPartirDe) {
		return processoAudienciaDAO.procurarAudienciasAbertasPorProcesso(processo, aPartirDe);
	}
	
	/**
	 * Retorna uma lista de {@link ProcessoAudiencia} com status de {@link StatusAudienciaEnum#M} e que tenham como data de início a data parâmetro.
	 * Se houver horário na data parâmetro, o mesmo será removido na busca.
	 * 
	 * @param dataAudiencia a {@link Date} de início da audiência
	 * @return lista de {@link ProcessoAudiencia} que tenham data de início igual à data parâmetro
	 */
	public List<ProcessoAudiencia> findByDate(Date dataAudiencia){
		return processoAudienciaDAO.findByData(dataAudiencia);				
	}

	public List<Integer> findIdsByDate(Date dataAudiencia) {
		return processoAudienciaDAO.findIdsByData(dataAudiencia);
	}

	public String retornaPadraoTipoMarcacaoAudiencia(String tipoDesignacao) {
		String padrao = "dd/MM/yyyy HH:mm";
		if(!tipoDesignacao.equals(TIPO_DESIGNACAO_AUDIENCIA_MANUAL)){
			padrao = "dd/MM/yyyy";
		}
		return padrao;
	}
	 
	public Boolean isHorarioReservado(ProcessoAudiencia audiencia){
		if (audiencia != null){
			CronTrigger cTrigger = new CronTrigger();
			for (SalaReservaHorario salaReservaHorario : salaReservaHorarioManager.findBySala(audiencia.getSalaAudiencia(), true)) {
				try {
					cTrigger.setCronExpression(salaReservaHorario.getReservaHorario().getDsExpressaoCronInicio());
					Date dataInicioExpressao = cTrigger.getFireTimeAfter(DateUtil.getBeginningOfDay(audiencia.getDtInicio()));
					cTrigger.setCronExpression(salaReservaHorario.getReservaHorario().getDsExpressaoCronTermino());
					Date dataTerminoExpressao = cTrigger.getFireTimeAfter(dataInicioExpressao);					
					if (dataInicioExpressao != null && dataTerminoExpressao != null && DateUtil.isIntersectWithHour(dataInicioExpressao, dataTerminoExpressao, audiencia.getDtInicio(), audiencia.getDtFim())) {
						return true;
					}
				} catch (ParseException e) {
					// swallow it
					// O action de cadastro da express?o cron j? faz a valida??o.
					// Deixei o try/catch para evitar fazer throws no m?todo  
				}				
			}
		}
		return false;
	}
	
	private List<Sala> obterSalas(ProcessoTrf processoTrf, TipoAudiencia tipoAudiencia, Sala sala)
			throws PJeBusinessException {
		List<Sala> salas;
		if (sala == null) {
			salas = this.salaManager.recuperar(processoTrf.getOrgaoJulgador(), tipoAudiencia);
			if (salas.isEmpty()) {
				throw new PJeBusinessException("Não há salas cadastradas.");
			}
		} else {
			salas = this.salaManager.verificarSalaCadastrada(sala, processoTrf.getOrgaoJulgador()) ? Arrays.asList(sala)
					: Collections.emptyList();
			if (salas.isEmpty()) {
				throw new PJeBusinessException("Sala não cadastrada para este órgao julgador.");
			}
		}
		return salas;
	}	
	
	public void ordenarAudienciasPorData(List<ProcessoAudiencia> audiencias, final boolean desconsideraHoras) {
		Collections.sort(audiencias, new Comparator<ProcessoAudiencia>() {
            @Override
			public int compare(ProcessoAudiencia a, ProcessoAudiencia b) {
                return (desconsideraHoras) ? 
                	DateUtil.getDataSemHora(a.getDtInicio()).compareTo(DateUtil.getDataSemHora(b.getDtInicio())) : 
                		a.getDtInicio().compareTo(b.getDtInicio());
              }			
		});
	}

	private boolean isChoqueHorarioAdvogado(ProcessoTrf processoTrf, Date dtMarcacao, int tempoAudiencia) {
		List<ProcessoParte> advogados = processoParteManager.recuperarAdvogados(processoTrf);
		for (ProcessoParte advogado : advogados) {
			if (!this.recuperarAudienciasMarcadas(advogado.getPessoa(), dtMarcacao, 
					DateUtil.adicionarTempoData(dtMarcacao, Calendar.MINUTE, tempoAudiencia)).isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Método responsável por retornar os advogados, associados ao processo, 
	 * que possuem incompatibilidade de horários na designição de audiência.
	 * 
	 * @param dtMarcacao Data de marcação da audiência.
	 * @param tempoAudiencia Tempo da audiência.
	 * @return Advogados, associados ao processo, que possuem 
	 * incompatibilidade de horârios na designição de audiência.
	 */
	public Map<ProcessoTrf, Pessoa> getAdvogadosChoqueHorario(ProcessoTrf processoTrf, Date dtMarcacao, int tempoAudiencia) {
		List<ProcessoAudiencia> processosAudiencia = null;
		Map<ProcessoTrf, Pessoa> advogadosChoqueHorario = new HashMap<ProcessoTrf, Pessoa>(0);
		
		List<ProcessoParte> advogados = processoParteManager.recuperarAdvogados(processoTrf);
		for (ProcessoParte advogado : advogados) {
			processosAudiencia = this.recuperarAudienciasMarcadas(advogado.getPessoa(), dtMarcacao, 
					DateUtil.adicionarTempoData(dtMarcacao, Calendar.MINUTE, tempoAudiencia));
			
			if (!processosAudiencia.isEmpty()) {
				for (ProcessoAudiencia processoAudiencia : processosAudiencia) {
					advogadosChoqueHorario.put(processoAudiencia.getProcessoTrf(), advogado.getPessoa());
				}
			}
		}
		return advogadosChoqueHorario;
	}
	public Integer getPrazoMinimoMarcacaoAudiencia(ProcessoTrf processoTrf, TipoAudiencia tipoAudiencia) {
		Integer prazoMinimoMarcacaoAudiencia = ComponentUtil.getComponent(PrazoMinimoMarcacaoAudienciaManager.class)
			.getPrazoMinimoMarcacaoAudienciaPorTipo(processoTrf.getOrgaoJulgador(), tipoAudiencia);
		
		if (prazoMinimoMarcacaoAudiencia == null) {
			prazoMinimoMarcacaoAudiencia = ParametroUtil.getTempoMinimoAudiencia();
		}
		
		if (prazoMinimoMarcacaoAudiencia == null) {
			prazoMinimoMarcacaoAudiencia = Constantes.DISTANCIA_MINIMA_PESQUISA_AUDIENCIA;
		}
		
		return prazoMinimoMarcacaoAudiencia;
	}
	
	public void lancarMovimentoAudiencia(TipoAudiencia tipoAudiencia, String dtInicioFormatada, String local,
			Processo processo, ProcessoDocumento processoDocumento, StatusAudienciaEnum situacao) {

		// Descri??o = Audi?ncia #{tipo_de_audiencia} #{situacao_da_audiencia} para #{data_hora} #{local}.
		MovimentoBuilder movimentoBuilder = MovimentoAutomaticoService.preencherMovimento()
				.deCodigo(CodigoMovimentoNacional.COD_MOVIMENTO_AUDIENCIA).associarAoProcesso(processo);
		
		if (processoDocumento != null && processoDocumento.getIdProcessoDocumento() != 0) {
			movimentoBuilder.associarAoDocumentoDeId(processoDocumento.getIdProcessoDocumento());
		}
		  						  
		movimentoBuilder.comProximoComplementoVazio()
			.preencherComCodigo(Integer.toString(tipoAudiencia.getIdTipoAudiencia()))
			.preencherComTexto(tipoAudiencia.getTipoAudiencia())
		  	.comProximoComplementoVazio()
		  	.preencherComCodigo(situacao.getCodigo())
		  	.preencherComTexto(situacao.getLabel())
		  	.comProximoComplementoVazio()
		  	.preencherComTexto(dtInicioFormatada)
		  	.comProximoComplementoVazio()
		  	.preencherComTexto(local)
		  	.lancarMovimento();
	}
 	
	public ProcessoAudiencia getProximaAudienciaPautaEspecifica(String identificador, ReservaHorario reservaHorario,
			final List<Sala> salas, Calendar dataInicio,Integer tempoAudiencia) {
		List<ProcessoAudiencia> listaAudiencias = new ArrayList<ProcessoAudiencia>();
		Calendar dataInicioAux = (Calendar) dataInicio.clone();
		dataInicioAux.add(Calendar.SECOND, -1);
		
		if (StringUtil.isSet(identificador)) {
			
			List<SalaReservaHorario> salaReservaHorarioList = salaReservaHorarioManager.find(StringUtil.trimBorders(identificador),
					salas.get(0).getOrgaoJulgador(), true);
			for (SalaReservaHorario salaReservaHorario : salaReservaHorarioList) {
				ProcessoAudiencia proximaAudienciaDaExpressaoQuartz = getProximaAudienciaDaExpressaoQuartz(
						salaReservaHorario.getSala(), salaReservaHorario.getReservaHorario(), dataInicioAux,tempoAudiencia);
				if (proximaAudienciaDaExpressaoQuartz != null) {
					listaAudiencias.add(proximaAudienciaDaExpressaoQuartz);
				}
			}
		} else if (ProjetoUtil.isNotVazio(salas)) {
			for (Sala sala : salas) {
				ProcessoAudiencia proximaAudienciaDaExpressaoQuartz = getProximaAudienciaDaExpressaoQuartz(sala,
						reservaHorario, dataInicioAux,tempoAudiencia);
				if (proximaAudienciaDaExpressaoQuartz != null) {
					listaAudiencias.add(proximaAudienciaDaExpressaoQuartz);
				}
			}
		}

		if (ProjetoUtil.isNotVazio(listaAudiencias)) {
			ordenarAudienciasPorData(listaAudiencias);
			String modoMarcacaoAudiencias = ComponentUtil.getComponent(ParametroService.class)
					.valueOf("pje:modoMarcacaoAutomatica");
			Boolean modoMarcacaoSequencial = "sequencial".equals(modoMarcacaoAudiencias);
			if (modoMarcacaoSequencial) {
				ordenarAudienciasPorSalaComMaisDesignacoes(listaAudiencias);
				ordenarAudienciaPorDiaSemHora(listaAudiencias);
			}
			return listaAudiencias.get(0);
		}
		return null;
	}
	
   

	
	
	private void ordenarAudienciasPorData(List<ProcessoAudiencia> audiencias) {
		if (ProjetoUtil.isNotVazio(audiencias) && audiencias.size() > 1) {
	    	CollectionUtilsPje.sortCollection(audiencias, true, "dtInicio", "salaAudiencia.sala");
	    }
	}
	
	
	public Integer recuperarPrazoMinimoMarcacaoAudiencia(
			OrgaoJulgador orgaoJulgador, TipoAudiencia tipoAudiencia) {
		PrazoMinimoMarcacaoAudienciaManager prazoManager = ComponentUtil.getComponent(PrazoMinimoMarcacaoAudienciaManager.class);
        return prazoManager.getPrazoMinimoMarcacaoAudienciaPorTipo(orgaoJulgador, tipoAudiencia);
	}
	
	
	private void ordenarAudienciasPorSalaComMaisDesignacoes(List<ProcessoAudiencia> listaAudiencias) {
		if (ProjetoUtil.isNotVazio(listaAudiencias) && listaAudiencias.size() > 1){
			Collections.sort(listaAudiencias,
					new Comparator<ProcessoAudiencia>() {
				@Override
				public int compare(ProcessoAudiencia pa1, ProcessoAudiencia pa2) {
					Integer qtde1 = procurarSalasComAudienciaMarcadaPorDia(pa1.getProcessoTrf().getOrgaoJulgador(), pa1.getDtInicio(), pa1.getDtInicio(), pa1.getSalaAudiencia()).size();
					Integer qtde2 = procurarSalasComAudienciaMarcadaPorDia(pa2.getProcessoTrf().getOrgaoJulgador(), pa2.getDtInicio(), pa2.getDtInicio(), pa2.getSalaAudiencia()).size();
					return qtde2.compareTo(qtde1);
				}
			});
		}
	}

	
	private void ordenarAudienciaPorDiaSemHora(List<ProcessoAudiencia> listaAudiencias) {
		if (ProjetoUtil.isNotVazio(listaAudiencias) && listaAudiencias.size() > 1){
			Collections.sort(listaAudiencias,
					new Comparator<ProcessoAudiencia>() {
				@Override
				public int compare(ProcessoAudiencia pa1, ProcessoAudiencia pa2) {
					Date data1 = DateUtil.getDataSemHora(pa1.getDtInicio());
					Date data2 = DateUtil.getDataSemHora(pa2.getDtInicio());
					return data1.compareTo(data2);
				}
			});
		}
	}
	

	
 public ProcessoAudiencia getProximaAudienciaDaExpressaoQuartz(Sala sala, ReservaHorario reservaHorario, Calendar dataInicio,Integer tempoAudiencia) {
		CronTrigger cTriggerInicio = new CronTrigger();
		CronTrigger cTriggerTermino = new CronTrigger();
		Date dataInicialAudienciaExpressao = null;
		Date dataTerminoAudienciaExpressao = null;
		try {
			cTriggerInicio.setCronExpression(reservaHorario.getDsExpressaoCronInicio());
			dataInicialAudienciaExpressao = cTriggerInicio.getFireTimeAfter(dataInicio.getTime());
			cTriggerTermino.setCronExpression(reservaHorario.getDsExpressaoCronTermino());
			dataTerminoAudienciaExpressao = cTriggerTermino.getFireTimeAfter(dataInicialAudienciaExpressao);
		} catch (ParseException e) {
			// swallow it
			// O action de cadastro da expresso cron j faz a validao.
			// Deixei o try/catch para evitar fazer throws no mtodo		
		}
        Calendar dataLimite = (Calendar) dataInicio.clone();
        dataLimite.add(Calendar.YEAR, 2);
		ProcessoAudiencia audiencia = null;
		while (dataInicialAudienciaExpressao != null && dataTerminoAudienciaExpressao != null && DateUtil.isDataMenor(dataInicialAudienciaExpressao, dataLimite.getTime())){
			audiencia = procurarPrimeiroHorarioDisponivel(sala, dataInicialAudienciaExpressao, tempoAudiencia, Variaveis.DISTANCIA_MAXIMA_MARCACAO_PAUTA_ESPECIFICA);
			if (audiencia != null && DateUtil.isIntersectWithHour(dataInicialAudienciaExpressao, dataTerminoAudienciaExpressao, audiencia.getDtInicio(), audiencia.getDtFim())){
				break;
			} else {
				//procurar um prximo horário a partir da prxima data especificada na expresso, j que a atual no serve.
				audiencia = null;
				dataInicialAudienciaExpressao = cTriggerInicio.getFireTimeAfter(dataTerminoAudienciaExpressao);				
				dataTerminoAudienciaExpressao = cTriggerTermino.getFireTimeAfter(dataInicialAudienciaExpressao);
			}
		}
		return audiencia;
	}

	public ProcessoAudiencia designarAudiencia(ProcessoTrf processoTrf, ProcessoAudiencia audienciaRemarcada,  
 			TipoAudiencia tipoAudiencia, Sala sala, Date dataInicioPesquisa, Integer duracao, EtapaAudienciaEnum etapaAudiencia) throws PJeBusinessException {
 		
 		EntityUtil.getEntityManager().createQuery("FROM Lock WHERE codigo = :codigo")
 			.setParameter("codigo", "designar_audiencia_lote")
 			.setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
 		
 		StatusAudienciaEnum situacao = StatusAudienciaEnum.M;
 		ProcessoAudiencia audiencia = obterHorariosLivres(processoTrf, tipoAudiencia, sala, dataInicioPesquisa,false, duracao,etapaAudiencia).get(0);
 		audiencia.setProcessoTrf(processoTrf);
 		audiencia.setTipoAudiencia(tipoAudiencia);
 		audiencia.setInAtivo(Boolean.TRUE);
 		audiencia.setStatusAudiencia(situacao);
 		audiencia.setPessoaMarcador(Authenticator.getPessoaLogada());
 		audiencia.setDtMarcacao(new Date());
 		audiencia.setTipoDesignacao(ProcessoAudiencia.TIPO_DESIGNACAO_SUGERIDA);
 		
 		processoTrf.getProcessoAudienciaList().add(audiencia);
 		
 		if (audienciaRemarcada != null) {
 			audienciaRemarcada.setStatusAudiencia(StatusAudienciaEnum.R);
 			audienciaRemarcada.setDsMotivo(String.format("Audiência redesignada em lote por %s", Authenticator.getUsuarioLogado()));
 			situacao = StatusAudienciaEnum.R;
 		}
 		
 		
 		this.lancarMovimentoAudiencia(tipoAudiencia, audiencia.getDtInicioFormatada(), 
 				String.format("%s %s ", audiencia.getSalaAudiencia(), audiencia.getSalaAudiencia().getOrgaoJulgador()),
 				processoTrf.getProcesso(), null, situacao);
 		
 		this.persistAndFlush(audiencia);
 		
 		return audiencia;
	}
}
