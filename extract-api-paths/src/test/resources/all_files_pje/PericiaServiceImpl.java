package br.jus.cnj.pje.webservice;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.bpm.BusinessProcess;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.dao.ProcessoPericiaDAO;
import br.com.infox.pje.manager.PessoaPeritoDisponibilidadeManager;
import br.com.infox.pje.manager.PessoaPeritoIndisponibilidadeManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.EspecialidadeManager;
import br.jus.cnj.pje.nucleo.manager.PessoaPeritoManager;
import br.jus.cnj.pje.nucleo.service.FluxoService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.servicos.DateService;
import br.jus.cnj.pje.servicos.PessoaPeritoEspecialidadeService;
import br.jus.cnj.pje.vo.DesignarPericia;
import br.jus.cnj.pje.webservice.controller.modalPericiaLote.dto.EspecilidadeDTO;
import br.jus.cnj.pje.webservice.controller.modalPericiaLote.dto.PericiaDTO;
import br.jus.cnj.pje.webservice.controller.modalPericiaLote.dto.PeritoDTO;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaPeritoDisponibilidade;
import br.jus.pje.nucleo.entidades.PessoaPeritoIndisponibilidade;
import br.jus.pje.nucleo.entidades.ProcessoPericia;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.PericiaStatusEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name("periciaService")
public class PericiaServiceImpl implements IPericiaService {
	
	private static final long serialVersionUID = 1L;
	
	@In
	private EspecialidadeManager especialidadeManager;
	
	@In
	private PessoaPeritoManager pessoaPeritoManager;
	
	@In
	private PessoaPeritoDisponibilidadeManager pessoaPeritoDisponibilidadeManager;
	
	@In
	private PessoaPeritoIndisponibilidadeManager pessoaPeritoIndisponibilidadeManager;
	
	@In
	private FluxoService fluxoService;
	
	@In
	private ProcessoPericiaDAO processoPericiaDAO;
	
	@In(create = true)
	private PessoaPeritoEspecialidadeService pessoaPeritoEspecialidadeService; 
	
	@Override
	public List<EspecilidadeDTO> obterEspecialidadesAtiva() {
		List<EspecilidadeDTO> result = new ArrayList<EspecilidadeDTO>(0);
		
		for (Especialidade especialidade : especialidadeManager.recuperarAtivas()) {
			result.add(new EspecilidadeDTO(especialidade.getIdEspecialidade(), especialidade.getEspecialidade()));
		}
		return result;
	}

	@Override
	public List<PeritoDTO> obterPeritosAtivo(Integer idEspecialidade, List<Integer> idsOrgaoJulgador) {
		List<PeritoDTO> result = new ArrayList<PeritoDTO>(0);
		
		for (PessoaPerito pessoaPerito : pessoaPeritoManager.recuperarAtivos(idEspecialidade, new HashSet<Integer>(idsOrgaoJulgador))) {
			result.add(new PeritoDTO(pessoaPerito.getIdUsuario(), pessoaPerito.getNome()));
		}
		return result;
	}

	@Override
	public Date obterDataMarcacaoDisponivel(Especialidade especialidade, PessoaPerito perito, Date data) throws PJeBusinessException {
		if (especialidade == null || perito == null) {
			throw new IllegalArgumentException("Argumentos inválidos.");
		}
		
		if (this.pessoaPeritoDisponibilidadeManager.recuperarAtivos(especialidade, perito).isEmpty()) {
			throw new PJeBusinessException(String.format("Não há disponibilidade cadastrada para o perito %s na especialidade %s.", perito, especialidade));
		}
		Date dataAtual = DateService.instance().getDataHoraAtual();
		Calendar dataPesquisa = Calendar.getInstance();
		
		if (data == null || data.before(dataAtual)) {
			dataPesquisa.setTime(dataAtual);
		} else {
			dataPesquisa.setTime(data);
		}
		
		Date dataMaximoPesquisa = DateUtil.adicionarTempoData(dataPesquisa.getTime(), Calendar.YEAR, 2);
		DesignarPericia designarPericia = null;
		boolean isSomenteHorarioFuturo = true;
		
		while (dataPesquisa.getTime().before(dataMaximoPesquisa)) {
			designarPericia = obtemProximaPericiaDisponivel(especialidade, perito, dataPesquisa, isSomenteHorarioFuturo);
			
			if (designarPericia != null) {
				break;
			}
			dataPesquisa.add(Calendar.DATE, 1);
		}
		
		if (designarPericia == null) {
			throw new PJeBusinessException(String.format("No h disponibilidade cadastrada para o perito %s na especialidade %s.", perito, especialidade));
		}
		
		dataPesquisa.set(Calendar.HOUR_OF_DAY, designarPericia.getDataHora().get(Calendar.HOUR_OF_DAY));
		dataPesquisa.set(Calendar.MINUTE, designarPericia.getDataHora().get(Calendar.MINUTE));
		dataPesquisa.set(Calendar.SECOND, designarPericia.getDataHora().get(Calendar.SECOND));
		dataPesquisa.set(Calendar.MILLISECOND, 0);
		
		return designarPericia.getDataHora().getTime();
	}
	
	private boolean verificarDisponibilidadeMarcacao(Especialidade especialidade, PessoaPerito perito, Date dataHoraAgora, Calendar dataHoraPesquisa, boolean isSomenteHorarioFuturo) {
		if (isSomenteHorarioFuturo && dataHoraPesquisa.getTime().before(dataHoraAgora)) {
			return false;
		}
		Date dataPesquisa = dataHoraPesquisa.getTime();
		List<PericiaStatusEnum> status = Arrays.asList(PericiaStatusEnum.M, PericiaStatusEnum.F, PericiaStatusEnum.P, PericiaStatusEnum.N);
		List<ProcessoPericia> periciasMarcadas = recuperarPericias(perito, status, dataPesquisa);
		List<PessoaPeritoIndisponibilidade>indisponibilidades = this.pessoaPeritoIndisponibilidadeManager.recuperarAtivos(especialidade, perito, dataPesquisa);
		return verificarPericiaMarcada(periciasMarcadas, dataHoraPesquisa) && verificarIndisponibilidadePerito(indisponibilidades, dataHoraPesquisa);
	}
	
	private boolean verificarPericiaMarcada(List<ProcessoPericia> periciasMarcadas, Calendar dataHoraPesquisa) {
		for (ProcessoPericia pericia : periciasMarcadas) {
			if (DateUtil.isMesmoHorario(pericia.getHoraMarcada(), dataHoraPesquisa.getTime())) {
				return false;
			}
		}
		return true;
	}
	
	private boolean verificarIndisponibilidadePerito(List<PessoaPeritoIndisponibilidade> indisponibilidades, Calendar dataHoraPesquisa) {
		Time horarioPesquisa = new Time(dataHoraPesquisa.getTime().getTime());
		
		for(PessoaPeritoIndisponibilidade indisponibilidade : indisponibilidades) {
			Boolean isHorarioPesquisaEntreHoraInicioEHoraFim = DateUtil.isBetweenHours(horarioPesquisa, indisponibilidade.getHoraInicio(), indisponibilidade.getHoraFim());
			if (Boolean.TRUE.equals(isHorarioPesquisaEntreHoraInicioEHoraFim)) {
				return false;
			}
		}
		return true;
	}

	@Override
	@Transactional
	public PericiaDTO designarPericia(Long idTarefa, Long idProcesso, Integer idEspecialidade, Integer idPerito, Double valor, Long dataInicio) throws PJeBusinessException {
		if (idProcesso == null || idEspecialidade == null || idPerito == null) {
			throw new IllegalArgumentException("Argumento(s) inválido(s).");
		}

		EntityManager entityManager = EntityUtil.getEntityManager();
		ProcessoTrf processoTrf = entityManager.find(ProcessoTrf.class, Long.valueOf(idProcesso).intValue());
		Especialidade especialidade = entityManager.find(Especialidade.class, idEspecialidade);
		PessoaPerito perito = entityManager.find(PessoaPerito.class, idPerito);

		if (!this.pessoaPeritoManager.recuperar(especialidade, processoTrf.getOrgaoJulgador()).contains(perito)) {
			throw new PJeBusinessException(String.format("O perito %s não está cadastrado para o órgão julgador %s", perito, processoTrf.getOrgaoJulgador()));
		}
    	
    	entityManager.createQuery("FROM Lock WHERE codigo = :codigo")
			.setParameter("codigo", "designar_pericia_lote")
			.setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
    	
    	Date dataMarcacao = this.obterDataMarcacaoDisponivel(especialidade, perito, dataInicio != null ? new Date(dataInicio) : null);
       	
		if (dataMarcacao == null) {
			throw new PJeBusinessException("Não há horário disponível.");
		}
    	
		DesignarPericia designarPericia = getNovaPericia(processoTrf, especialidade, perito, valor, dataMarcacao);
		designarPericia(designarPericia);
		ProcessoPericia pericia = designarPericia.getProcessoPericia();

		if (idTarefa != null) {
			ProcessInstance pi = null;
    		BusinessProcess.instance().resumeTask(idTarefa);
    		TaskInstance ti = org.jboss.seam.bpm.TaskInstance.instance();
    		pi = ti.getProcessInstance();
			pi.getContextInstance().setVariable(Eventos.EVENTO_SINALIZACAO, Variaveis.PJE_FLUXO_PERICIA_AGUARDA_DESIGNACAO);
			pi.getContextInstance().setVariable(Variaveis.PJE_FLUXO_PERICIA, pericia);
    		
			this.fluxoService.finalizarTarefa(idTarefa, Boolean.FALSE, null);
			
			ProcessoJudicialService pjs = ComponentUtil.getComponent(ProcessoJudicialService.class);
        	Map<String, Object> novasVariaveis = new HashMap<String, Object>();
        	novasVariaveis.put(Variaveis.PJE_FLUXO_PERICIA, pericia);
        	pjs.sinalizarFluxo(processoTrf, Variaveis.PJE_FLUXO_PERICIA_AGUARDA_DESIGNACAO, true, true, true, novasVariaveis);
		}

		return new PericiaDTO(pericia.getPessoaPerito().toString(),	new SimpleDateFormat("dd/MM/yyyy").format(pericia.getDataMarcacao()), 
				new SimpleDateFormat("HH:mm").format(pericia.getHoraMarcada()));
	}
	
	private DesignarPericia obtemProximaPericiaDisponivel(Especialidade especialidade, PessoaPerito perito, Calendar dataHoraPesquisa, boolean isSomenteHorarioFuturo) throws PJeBusinessException {
		List<DesignarPericia> horariosDisponiveis = obterHorariosPerito(especialidade, perito, dataHoraPesquisa, isSomenteHorarioFuturo);
		for (DesignarPericia horarioDisponivel: horariosDisponiveis) {
			if (horarioDisponivel.isAtivo()) {
				return horarioDisponivel;
			}
		}
		return null;
	}
	
	public List<DesignarPericia> obterHorariosPerito(Especialidade especialidade, PessoaPerito perito, Calendar dataPesquisa, boolean isSomenteHorarioFuturo) throws PJeBusinessException {
		List<DesignarPericia> horariosDisponiveis = new ArrayList<>();
		List<PessoaPeritoDisponibilidade> disponibilidades = this.pessoaPeritoDisponibilidadeManager.recuperarAtivos(especialidade, perito, dataPesquisa.getTime());
		
		if(disponibilidades == null || disponibilidades.isEmpty()) {
			return horariosDisponiveis;
		} else if (disponibilidades.size() > 1) {
			String msgErro = "O perito %s na especialidade %s tem mais de uma disponibilidade para o mesmo dia da semana. Favor manter apenas uma disponibilidade.";
			throw new PJeBusinessException(String.format(msgErro, perito, especialidade));
		}
		
		PessoaPeritoDisponibilidade pessoaPeritoDisponibilidade = disponibilidades.get(0);
		
		Calendar horaInicioDaDataPesquisa = Calendar.getInstance();
		horaInicioDaDataPesquisa.setTime(pessoaPeritoDisponibilidade.getHoraInicio());
		
		Calendar horaFimDaDataPesquisa = Calendar.getInstance();
		horaFimDaDataPesquisa.setTime(pessoaPeritoDisponibilidade.getHoraFim());
		
		Calendar inicio = getNovaDataHora(dataPesquisa, horaInicioDaDataPesquisa);
		Calendar fim = getNovaDataHora(dataPesquisa, horaFimDaDataPesquisa);
		
		if (!inicio.before(fim)) {
			return horariosDisponiveis;
		}
		
		Date dataAtual = DateService.instance().getDataHoraAtual();
		int intervaloEmMinutos = getIntervaloEmMinutos(pessoaPeritoDisponibilidade, inicio, fim);				
		
		while (inicio.before(fim)) {
			DesignarPericia disponibilidade = new DesignarPericia();
			disponibilidade.setDataHora((Calendar)inicio.clone());
			boolean isDisponivel = verificarDisponibilidadeMarcacao(especialidade, perito, dataAtual, inicio, isSomenteHorarioFuturo);
			disponibilidade.setAtivo(isDisponivel);
			horariosDisponiveis.add(disponibilidade);
			inicio.add(Calendar.MINUTE, intervaloEmMinutos);
		}
			
		return horariosDisponiveis;
	}

	private int getIntervaloEmMinutos(PessoaPeritoDisponibilidade pessoaPeritoDisponibilidade, Calendar inicio,
			Calendar fim) {
		int intervaloEmMinutos = 0;
		
		//Clculo dos horrios por intervalo
		if (pessoaPeritoDisponibilidade.getIntervalo() != null) {
			intervaloEmMinutos = DateUtil.convertToMinutes(pessoaPeritoDisponibilidade.getIntervalo());
		} else if (pessoaPeritoDisponibilidade.getQntAtendimento() != null && pessoaPeritoDisponibilidade.getQntAtendimento().intValue() > 0) {
			//Clculo dos horrios por quantidade de atendimentos
				int periodoDisponivel = DateUtil.convertToMinutes(inicio.getTime(), fim.getTime());
				Integer quantidadePericias = pessoaPeritoDisponibilidade.getQntAtendimento();
				intervaloEmMinutos = periodoDisponivel / quantidadePericias;
		}

		if (intervaloEmMinutos < 1) {
			//O intervalo mnimo, entre as percias,  de 1 minuto.
			intervaloEmMinutos = 1;
		}
		return intervaloEmMinutos;
	}

	private Calendar getNovaDataHora(Calendar dataPesquisa, Calendar horaPesquisa) {
		Calendar dataHora = Calendar.getInstance();
		dataHora.set(Calendar.DATE, dataPesquisa.get(Calendar.DATE));
		dataHora.set(Calendar.MONTH, dataPesquisa.get(Calendar.MONTH));
		dataHora.set(Calendar.YEAR, dataPesquisa.get(Calendar.YEAR));
		dataHora.set(Calendar.HOUR_OF_DAY, horaPesquisa.get(Calendar.HOUR_OF_DAY));
		dataHora.set(Calendar.MINUTE, horaPesquisa.get(Calendar.MINUTE));
		dataHora.set(Calendar.SECOND, horaPesquisa.get(Calendar.SECOND));
		dataHora.set(Calendar.MILLISECOND, 0);
		return dataHora;
	}
	
	/**
	 * Grava uma nova percia.
	 */
	@Override
	public DesignarPericia designarPericia(DesignarPericia designarPericia) {
		ProcessoPericia processoPericia = designarPericia.getProcessoPericia(); 
				
		if (designarPericia.isRedesignarPericia()) {
			ProcessoPericia processoPericiaAntigo = designarPericia.getProcessoPericiaAntigo();
			processoPericiaAntigo.setStatus(PericiaStatusEnum.R);
			processoPericia.setPericiaAnterior(processoPericiaAntigo);
			processoPericiaDAO.persist(processoPericiaAntigo);
			processoPericiaDAO.flush();
		}
		processoPericiaDAO.persist(processoPericia);
		processoPericiaDAO.flush();
		return designarPericia;
	}
	
	/**
	 * Obtém uma nova perícia para ser persistida em banco de dados. 
	 */
	private DesignarPericia getNovaPericia(ProcessoTrf processoTrf, Especialidade especialidade, PessoaPerito perito, Double valor, Date dataMarcacao) {
		if (processoTrf == null || especialidade == null || perito == null || dataMarcacao == null) {
			throw new IllegalArgumentException("Argumentos da nova perícia inválidos.");
		}
		
		DesignarPericia designarPericia = new DesignarPericia();
 		ProcessoPericia processoPericia = new ProcessoPericia();
 		processoPericia.setProcessoTrf(processoTrf);
 		processoPericia.setEspecialidade(especialidade);
 		processoPericia.setPessoaPerito(perito);
 		processoPericia.setPessoaMarcador(Authenticator.getPessoaLogada());
 		processoPericia.setStatus(PericiaStatusEnum.M);
 		processoPericia.setHoraMarcada(new Time(dataMarcacao.getTime()));
 		processoPericia.setDataMarcacao(DateUtil.getDataSemHora(dataMarcacao));
 		processoPericia.setValorPericia(valor);
 		processoTrf.getProcessoPericiaList().add(processoPericia);
 		designarPericia.setProcessoPericia(processoPericia);
 		return designarPericia;
	}	
	
	public List<ProcessoPericia> recuperarPericias(PessoaPerito perito, List<PericiaStatusEnum> status, Date data) {
		return processoPericiaDAO.recuperarPericias(perito, status, data);
	}
}
