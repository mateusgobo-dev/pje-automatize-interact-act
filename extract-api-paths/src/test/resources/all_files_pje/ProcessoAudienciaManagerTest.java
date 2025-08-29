package br.com.infox.test.cliente.manager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.infox.pje.dao.ProcessoAudienciaDAO;
import br.com.infox.pje.manager.ProcessoAudienciaManager;
import br.com.jt.pje.manager.SalaManager;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.pje.nucleo.entidades.DiaSemana;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.SalaHorario;
import br.jus.pje.nucleo.enums.StatusAudienciaEnum;

@RunWith(MockitoJUnitRunner.class)
public class ProcessoAudienciaManagerTest {
	
	private ProcessoAudienciaManager processoAudienciaManager; 		
	
	private List<SalaHorario> horarioList;
	
	private ProcessoTrf processoTrf;
	
	private Sala sala;
	
	private SalaManager salaManager;
	
	@Mock
	private PrazosProcessuaisService prazosProcessuaisService;
	
	@Mock
	private ProcessoAudienciaDAO processoAudienciaDAO;
		
	@Before
	public void setup() {
		processoAudienciaManager = new ProcessoAudienciaManager();
		salaManager = new SalaManager();
		processoAudienciaManager.setSalaManager(salaManager);
		
		horarioList = new ArrayList<SalaHorario>();
		
		processoTrf = new ProcessoTrf();
		processoTrf.setIdProcessoTrf(1256);
		
		sala = new Sala();
		sala.setAtivo(true);
		sala.setIgnoraFeriado(false);
		
		DiaSemana ds = new DiaSemana();
		ds.setIdDiaSemana(2);
		
		SalaHorario sh1 = new SalaHorario();
		Calendar horaInicial1 = Calendar.getInstance();
		Calendar horaFinal1 = Calendar.getInstance();		
		
		// Configura período 08:00 as 10:00
		horaInicial1.set(Calendar.HOUR_OF_DAY, 8);
		horaInicial1.set(Calendar.MINUTE, 0);
		horaFinal1.set(Calendar.HOUR_OF_DAY, 10);
		horaFinal1.set(Calendar.MINUTE, 0);
		
		sh1.setAtivo(true);
		sh1.setDiaSemana(ds);
		sh1.setHoraInicial(horaInicial1.getTime());
		sh1.setHoraFinal(horaFinal1.getTime());
		horarioList.add(sh1);
		
		SalaHorario sh2 = new SalaHorario();
		Calendar horaInicial2 = Calendar.getInstance();
		Calendar horaFinal2 = Calendar.getInstance();
		
		// Configura período 14:00 as 16:00
		horaInicial2.set(Calendar.HOUR_OF_DAY, 14);
		horaInicial2.set(Calendar.MINUTE, 0);
		horaFinal2.set(Calendar.HOUR_OF_DAY, 16);
		horaFinal2.set(Calendar.MINUTE, 0);				
		
		sh2.setAtivo(true);
		sh2.setDiaSemana(ds);
		sh2.setHoraInicial(horaInicial2.getTime());
		sh2.setHoraFinal(horaFinal2.getTime());
		horarioList.add(sh2);
				
		sala.setSalaHorarioList(horarioList);
	}

	
	
	/*
	 * Data inicial de pesquisa é 04/06/2013.
	 * Sala não possui inatividades ou bloqueio
	 * Sala não possui outras audiências marcadas.
	 * Audiência deve ser marcada para o dia 10/06/2013, às 08:00 	
	 */
	@Test
	public void nenhumaAudienciaMarcadaDeveRetornarPrimeiroHorarioDoPrimeiroDiaDisponivel() {
		
		Calendar data = Calendar.getInstance();
		data.set(2013, 5, 04, 0, 0);
		Calendar data2 = Calendar.getInstance();
		data2.set(2013, 5, 10, 0, 0);
		
		when(prazosProcessuaisService.ehDiaUtilJudicial(data.getTime(), sala.getOrgaoJulgador())).thenReturn(true);
		when(prazosProcessuaisService.ehDiaUtilJudicial(data2.getTime(), sala.getOrgaoJulgador())).thenReturn(true);
		when(processoAudienciaDAO.getAudienciasMarcadas(sala, data.getTime())).thenReturn(new ArrayList<ProcessoAudiencia>());
		
		processoAudienciaManager.setPrazosProcessuaisService(prazosProcessuaisService);
		processoAudienciaManager.setProcessoAudienciaDAO(processoAudienciaDAO);
		
		ProcessoAudiencia audMarcada = processoAudienciaManager.procurarPrimeiroHorarioDisponivel(processoTrf, sala, data.getTime(), 60);
		
		Calendar dtInicioEsperada = Calendar.getInstance();
		dtInicioEsperada.set(2013, 05, 10, 8, 0);
		
		Calendar dtFimEsperada = Calendar.getInstance();
		dtFimEsperada.set(2013, 05, 10, 9, 0);
		
		Calendar audMarcadaDtInicio = Calendar.getInstance();
		audMarcadaDtInicio.setTime(audMarcada.getDtInicio());
		
		Calendar audMarcadaDtFim= Calendar.getInstance();
		audMarcadaDtFim.setTime(audMarcada.getDtFim());
		
		
		//Valida data de início
		assertEquals(dtInicioEsperada.get(Calendar.YEAR), audMarcadaDtInicio.get(Calendar.YEAR));
		assertEquals(dtInicioEsperada.get(Calendar.MONTH), audMarcadaDtInicio.get(Calendar.MONTH));
		assertEquals(dtInicioEsperada.get(Calendar.DAY_OF_MONTH), audMarcadaDtInicio.get(Calendar.DAY_OF_MONTH));
		assertEquals(dtInicioEsperada.get(Calendar.HOUR_OF_DAY), audMarcadaDtInicio.get(Calendar.HOUR_OF_DAY));
		assertEquals(dtInicioEsperada.get(Calendar.MINUTE), audMarcadaDtInicio.get(Calendar.MINUTE));
		
		//Valida data de término
		assertEquals(dtFimEsperada.get(Calendar.YEAR), audMarcadaDtFim.get(Calendar.YEAR));
		assertEquals(dtFimEsperada.get(Calendar.MONTH), audMarcadaDtFim.get(Calendar.MONTH));
		assertEquals(dtFimEsperada.get(Calendar.DAY_OF_MONTH), audMarcadaDtFim.get(Calendar.DAY_OF_MONTH));
		assertEquals(dtFimEsperada.get(Calendar.HOUR_OF_DAY), audMarcadaDtFim.get(Calendar.HOUR_OF_DAY));
		assertEquals(dtFimEsperada.get(Calendar.MINUTE), audMarcadaDtFim.get(Calendar.MINUTE));
				
		assertEquals(sala, audMarcada.getSalaAudiencia());
	}
	
	
	/*
	 * Data inicial de pesquisa é 04/06/2013 às 9h.
	 * Sala não possui inatividades ou bloqueio
	 * Sala não possui outras audiências marcadas.
	 * Audiência deve ser marcada para o dia 10/06/2013, às 9:00 	
	 */
	@Test
	public void nenhumaAudienciaMarcadaDeveRetornarPrimeiroHorarioDoPrimeiroDiaDisponivelAPartirDaDataEHorarioInformado() {
		
		Calendar data = Calendar.getInstance();
		data.set(2013, 5, 10, 15, 0);
		
		when(prazosProcessuaisService.ehDiaUtilJudicial(data.getTime(), sala.getOrgaoJulgador())).thenReturn(true);
		when(processoAudienciaDAO.getAudienciasMarcadas(sala, data.getTime())).thenReturn(new ArrayList<ProcessoAudiencia>());
		
		processoAudienciaManager.setPrazosProcessuaisService(prazosProcessuaisService);
		processoAudienciaManager.setProcessoAudienciaDAO(processoAudienciaDAO);
		
		ProcessoAudiencia audMarcada = processoAudienciaManager.procurarPrimeiroHorarioDisponivel(processoTrf, sala, data.getTime(), 60);
		
		Calendar dtInicioEsperada = Calendar.getInstance();
		dtInicioEsperada.set(2013, 5, 10, 15, 0);
		
		Calendar dtFimEsperada = Calendar.getInstance();
		dtFimEsperada.set(2013, 5, 10, 16, 0);
		
		Calendar audMarcadaDtInicio = Calendar.getInstance();
		audMarcadaDtInicio.setTime(audMarcada.getDtInicio());
		
		Calendar audMarcadaDtFim= Calendar.getInstance();
		audMarcadaDtFim.setTime(audMarcada.getDtFim());
		
		
		//Valida data de início
		assertEquals(dtInicioEsperada.get(Calendar.YEAR), audMarcadaDtInicio.get(Calendar.YEAR));
		assertEquals(dtInicioEsperada.get(Calendar.MONTH), audMarcadaDtInicio.get(Calendar.MONTH));
		assertEquals(dtInicioEsperada.get(Calendar.DAY_OF_MONTH), audMarcadaDtInicio.get(Calendar.DAY_OF_MONTH));
		assertEquals(dtInicioEsperada.get(Calendar.HOUR_OF_DAY), audMarcadaDtInicio.get(Calendar.HOUR_OF_DAY));
		assertEquals(dtInicioEsperada.get(Calendar.MINUTE), audMarcadaDtInicio.get(Calendar.MINUTE));
		
		//Valida data de término
		assertEquals(dtFimEsperada.get(Calendar.YEAR), audMarcadaDtFim.get(Calendar.YEAR));
		assertEquals(dtFimEsperada.get(Calendar.MONTH), audMarcadaDtFim.get(Calendar.MONTH));
		assertEquals(dtFimEsperada.get(Calendar.DAY_OF_MONTH), audMarcadaDtFim.get(Calendar.DAY_OF_MONTH));
		assertEquals(dtFimEsperada.get(Calendar.HOUR_OF_DAY), audMarcadaDtFim.get(Calendar.HOUR_OF_DAY));
		assertEquals(dtFimEsperada.get(Calendar.MINUTE), audMarcadaDtFim.get(Calendar.MINUTE));
				
		assertEquals(sala, audMarcada.getSalaAudiencia());
	}
	
	/*
	 * Data inicial de pesquisa é 04/06/2013.
	 * Sala não possui inatividades ou bloqueio
	 * Sala possui uma audiencia marcada no dia 10/06/2013, das 08:00 às 09:00.
	 * Audiência deve ser marcada para o dia 10/06/2013, às 09:00 	
	 */
	@Test
	public void audienciaMarcadaMasHorarioAindaPossuiTempoSuficienteDeveMarcarAudienciaNoTempoRestanteDoHorario() {
		
		Calendar data = Calendar.getInstance();
		data.set(2013, 5, 04, 0, 0);

		Calendar data2 = Calendar.getInstance();
		data2.set(2013, 5, 10, 0, 0);
						
		when(prazosProcessuaisService.ehDiaUtilJudicial(data.getTime(), sala.getOrgaoJulgador())).thenReturn(true);
		when(prazosProcessuaisService.ehDiaUtilJudicial(data2.getTime(), sala.getOrgaoJulgador())).thenReturn(true);
		
		List<ProcessoAudiencia> audiencias = new ArrayList<ProcessoAudiencia>();
	
		//Configura audiencia para 10/05/2013 08:00 (60 minutos)
		ProcessoAudiencia audiencia1 = configuraAudiencia(10, 05, 2013, 8, 0, 60);
		audiencia1.setStatusAudiencia(StatusAudienciaEnum.M);
		audiencias.add(audiencia1);
		
		when(processoAudienciaDAO.getAudienciasMarcadas(sala, data.getTime())).thenReturn(audiencias);
		
		processoAudienciaManager.setPrazosProcessuaisService(prazosProcessuaisService);
		processoAudienciaManager.setProcessoAudienciaDAO(processoAudienciaDAO);
		
		ProcessoAudiencia audMarcada = processoAudienciaManager.procurarPrimeiroHorarioDisponivel(processoTrf, sala, data.getTime(), 60);
		
		Calendar dtInicioEsperada = Calendar.getInstance();
		dtInicioEsperada.set(2013, 05, 10, 9, 0);
		
		Calendar dtFimEsperada = Calendar.getInstance();
		dtFimEsperada.set(2013, 05, 10, 10, 0);
		
		Calendar audMarcadaDtInicio = Calendar.getInstance();
		audMarcadaDtInicio.setTime(audMarcada.getDtInicio());
		
		Calendar audMarcadaDtFim= Calendar.getInstance();
		audMarcadaDtFim.setTime(audMarcada.getDtFim());
		
		//Valida data de início
		assertEquals(dtInicioEsperada.get(Calendar.YEAR), audMarcadaDtInicio.get(Calendar.YEAR));
		assertEquals(dtInicioEsperada.get(Calendar.MONTH), audMarcadaDtInicio.get(Calendar.MONTH));
		assertEquals(dtInicioEsperada.get(Calendar.DAY_OF_MONTH), audMarcadaDtInicio.get(Calendar.DAY_OF_MONTH));
		assertEquals(dtInicioEsperada.get(Calendar.HOUR_OF_DAY), audMarcadaDtInicio.get(Calendar.HOUR_OF_DAY));
		assertEquals(dtInicioEsperada.get(Calendar.MINUTE), audMarcadaDtInicio.get(Calendar.MINUTE));
		
		//Valida data de término
		assertEquals(dtFimEsperada.get(Calendar.YEAR), audMarcadaDtFim.get(Calendar.YEAR));
		assertEquals(dtFimEsperada.get(Calendar.MONTH), audMarcadaDtFim.get(Calendar.MONTH));
		assertEquals(dtFimEsperada.get(Calendar.DAY_OF_MONTH), audMarcadaDtFim.get(Calendar.DAY_OF_MONTH));
		assertEquals(dtFimEsperada.get(Calendar.HOUR_OF_DAY), audMarcadaDtFim.get(Calendar.HOUR_OF_DAY));
		assertEquals(dtFimEsperada.get(Calendar.MINUTE), audMarcadaDtFim.get(Calendar.MINUTE));
				
		assertEquals(sala, audMarcada.getSalaAudiencia());
	}		
	
	/*
	 * Data inicial de pesquisa é 04/06/2013.
	 * Sala não possui inatividades ou bloqueio
	 * Sala possui uma audiencia marcada no dia 10/06/2013, das 08:00 às 09:30.
	 * Audiência deve ser marcada para o dia 10/06/2013, às 16:00 	
	 */
	@Test
	public void haAudienciaMarcadaEHorarioNaoPossuiTempoSuficienteDeveMarcarAudienciaNoProximoHorario() {
		
		Calendar data = Calendar.getInstance();
		data.set(2013, 5, 04, 0, 0);
		Calendar data2 = Calendar.getInstance();
		data2.set(2013, 5, 10, 0, 0);
		
		when(prazosProcessuaisService.ehDiaUtilJudicial(data.getTime(), sala.getOrgaoJulgador())).thenReturn(true);
		when(prazosProcessuaisService.ehDiaUtilJudicial(data2.getTime(), sala.getOrgaoJulgador())).thenReturn(true);

		List<ProcessoAudiencia> audiencias = new ArrayList<ProcessoAudiencia>();
		
		//Configura audiencia para 10/05/2013 08:00 (90 minutos)
		ProcessoAudiencia audiencia1 = configuraAudiencia(10, 05, 2013, 8, 0, 90);
		audiencia1.setStatusAudiencia(StatusAudienciaEnum.M);
		audiencias.add(audiencia1);
		
		when(processoAudienciaDAO.getAudienciasMarcadas(sala, data.getTime())).thenReturn(audiencias);
		
		processoAudienciaManager.setPrazosProcessuaisService(prazosProcessuaisService);
		processoAudienciaManager.setProcessoAudienciaDAO(processoAudienciaDAO);
		
		ProcessoAudiencia audMarcada = processoAudienciaManager.procurarPrimeiroHorarioDisponivel(processoTrf, sala, data.getTime(), 60);
		
		Calendar dtInicioEsperada = Calendar.getInstance();
		dtInicioEsperada.set(2013, 05, 10, 14, 0);
		
		Calendar dtFimEsperada = Calendar.getInstance();
		dtFimEsperada.set(2013, 05, 10, 15, 0);
		
		Calendar audMarcadaDtInicio = Calendar.getInstance();
		audMarcadaDtInicio.setTime(audMarcada.getDtInicio());
		
		Calendar audMarcadaDtFim= Calendar.getInstance();
		audMarcadaDtFim.setTime(audMarcada.getDtFim());
		
		//Valida data de início
		assertEquals(dtInicioEsperada.get(Calendar.YEAR), audMarcadaDtInicio.get(Calendar.YEAR));
		assertEquals(dtInicioEsperada.get(Calendar.MONTH), audMarcadaDtInicio.get(Calendar.MONTH));
		assertEquals(dtInicioEsperada.get(Calendar.DAY_OF_MONTH), audMarcadaDtInicio.get(Calendar.DAY_OF_MONTH));
		assertEquals(dtInicioEsperada.get(Calendar.HOUR_OF_DAY), audMarcadaDtInicio.get(Calendar.HOUR_OF_DAY));
		assertEquals(dtInicioEsperada.get(Calendar.MINUTE), audMarcadaDtInicio.get(Calendar.MINUTE));
		
		//Valida data de término
		assertEquals(dtFimEsperada.get(Calendar.YEAR), audMarcadaDtFim.get(Calendar.YEAR));
		assertEquals(dtFimEsperada.get(Calendar.MONTH), audMarcadaDtFim.get(Calendar.MONTH));
		assertEquals(dtFimEsperada.get(Calendar.DAY_OF_MONTH), audMarcadaDtFim.get(Calendar.DAY_OF_MONTH));
		assertEquals(dtFimEsperada.get(Calendar.HOUR_OF_DAY), audMarcadaDtFim.get(Calendar.HOUR_OF_DAY));
		assertEquals(dtFimEsperada.get(Calendar.MINUTE), audMarcadaDtFim.get(Calendar.MINUTE));
				
		assertEquals(sala, audMarcada.getSalaAudiencia());
	}
	
	
	/*
	 * Data inicial de pesquisa é 04/06/2013.
	 * Sala não possui inatividades ou bloqueio.
	 * Sala possui não possui audiencias marcadas.
	 * O primeiro horário da sala está inativo.
	 * Audiência deve ser marcada no segundo horário, dia 10/06/2013, às 14:00 	
	 */
	@Test
	public void salaComHorarioInativoNaoDeveMarcarAudienciaNesteHorario() {
		List<SalaHorario> sl = salaManager.getSalaHorarioDiaList(sala, 2);
		sl.get(0).setAtivo(false);
		
		Calendar data = Calendar.getInstance();
		data.set(2013, 5, 04, 0, 0);
		Calendar data2 = Calendar.getInstance();
		data2.set(2013, 5, 10, 0, 0);
		
		when(prazosProcessuaisService.ehDiaUtilJudicial(data2.getTime(), sala.getOrgaoJulgador())).thenReturn(true);
		when(prazosProcessuaisService.ehDiaUtilJudicial(data.getTime(), sala.getOrgaoJulgador())).thenReturn(true);
		when(processoAudienciaDAO.getAudienciasMarcadas(sala, data.getTime())).thenReturn(new ArrayList<ProcessoAudiencia>());
		
		processoAudienciaManager.setPrazosProcessuaisService(prazosProcessuaisService);
		processoAudienciaManager.setProcessoAudienciaDAO(processoAudienciaDAO);
		
		ProcessoAudiencia audMarcada = processoAudienciaManager.procurarPrimeiroHorarioDisponivel(processoTrf, sala, data.getTime(), 60);
		
		Calendar dtInicioEsperada = Calendar.getInstance();
		dtInicioEsperada.set(2013, 05, 10, 14, 0);
		
		Calendar dtFimEsperada = Calendar.getInstance();
		dtFimEsperada.set(2013, 05, 10, 15, 0);
		
		Calendar audMarcadaDtInicio = Calendar.getInstance();
		audMarcadaDtInicio.setTime(audMarcada.getDtInicio());
		
		Calendar audMarcadaDtFim= Calendar.getInstance();
		audMarcadaDtFim.setTime(audMarcada.getDtFim());
		
		//Valida data de início
		assertEquals(dtInicioEsperada.get(Calendar.YEAR), audMarcadaDtInicio.get(Calendar.YEAR));
		assertEquals(dtInicioEsperada.get(Calendar.MONTH), audMarcadaDtInicio.get(Calendar.MONTH));
		assertEquals(dtInicioEsperada.get(Calendar.DAY_OF_MONTH), audMarcadaDtInicio.get(Calendar.DAY_OF_MONTH));
		assertEquals(dtInicioEsperada.get(Calendar.HOUR_OF_DAY), audMarcadaDtInicio.get(Calendar.HOUR_OF_DAY));
		assertEquals(dtInicioEsperada.get(Calendar.MINUTE), audMarcadaDtInicio.get(Calendar.MINUTE));
		
		//Valida data de término
		assertEquals(dtFimEsperada.get(Calendar.YEAR), audMarcadaDtFim.get(Calendar.YEAR));
		assertEquals(dtFimEsperada.get(Calendar.MONTH), audMarcadaDtFim.get(Calendar.MONTH));
		assertEquals(dtFimEsperada.get(Calendar.DAY_OF_MONTH), audMarcadaDtFim.get(Calendar.DAY_OF_MONTH));
		assertEquals(dtFimEsperada.get(Calendar.HOUR_OF_DAY), audMarcadaDtFim.get(Calendar.HOUR_OF_DAY));
		assertEquals(dtFimEsperada.get(Calendar.MINUTE), audMarcadaDtFim.get(Calendar.MINUTE));
		
	}
	
	
	/*
	 * Data inicial de pesquisa é 04/06/2013.
	 * Sala não possui inatividades ou bloqueio
	 * método ehDiaUtilJudicial retorna sempre null. Testa se o sistema entra em loop por não achar dia para marcar a audiência.
	 * Audiência não deve ser marcada (método deve retornar null).
	 */
	@Test
	public void seNaoExisteDiaUtilJudicialNaoDeveSerMarcadaAudiencia() {
		Calendar data = Calendar.getInstance();
		data.set(2013, 5, 04);
		
		when(prazosProcessuaisService.ehDiaUtilJudicial(data.getTime(), sala.getOrgaoJulgador())).thenReturn(false);
				
		when(processoAudienciaDAO.getAudienciasMarcadas(sala, data.getTime())).thenReturn(new ArrayList<ProcessoAudiencia>());
		
		processoAudienciaManager.setPrazosProcessuaisService(prazosProcessuaisService);
		processoAudienciaManager.setProcessoAudienciaDAO(processoAudienciaDAO);
		
		ProcessoAudiencia audMarcada = processoAudienciaManager.procurarPrimeiroHorarioDisponivel(processoTrf, sala, data.getTime(), 60);
		
		assertEquals(null, audMarcada);
	}
	
	
	/*
	 * Data inicial de pesquisa é 04/06/2013.
	 * Sala não possui inatividades ou bloqueio
	 * Sala possui audiências marcadas nos dois horários do dia 05/06/2013, impedindo que uma nova audiência seja marcada nesse dia.
	 * Audiência deve ser marcada para o dia 17/06/2013, às 08:00 	
	 */
	//TODO Passar a configuração do cenário para um método separado.
	@Test
	public void sePrimeiroDiaDisponivelNaoPossuiJanelaDeHorarioParaMarcarAudienciaDeveMarcarNoProximoDiaDisponivel() {
		Calendar data = Calendar.getInstance();
		data.set(2013, 5, 04, 0, 0);
						
		when(prazosProcessuaisService.ehDiaUtilJudicial(Matchers.<Date>any(), Matchers.<OrgaoJulgador>any())).thenReturn(true);
		
		List<ProcessoAudiencia> audiencias = new ArrayList<ProcessoAudiencia>();
		
		//Configura audiencia para 10/05/2013 08:00 (90 minutos)
		ProcessoAudiencia audiencia1 = configuraAudiencia(10, 05, 2013, 8, 0, 90);
		audiencia1.setStatusAudiencia(StatusAudienciaEnum.M);
		audiencias.add(audiencia1);
		
		//Configura audiencia para 10/05/2013 14:00 (90 minutos)
		ProcessoAudiencia audiencia2 = configuraAudiencia(10, 05, 2013, 14, 0, 90);
		audiencia2.setStatusAudiencia(StatusAudienciaEnum.M);
		audiencias.add(audiencia2);
		
		when(processoAudienciaDAO.getAudienciasMarcadas(Matchers.<Sala>any(), Matchers.<Date>any())).thenReturn(audiencias);		
		
		processoAudienciaManager.setPrazosProcessuaisService(prazosProcessuaisService);
		processoAudienciaManager.setProcessoAudienciaDAO(processoAudienciaDAO);
		
		ProcessoAudiencia audMarcada = processoAudienciaManager.procurarPrimeiroHorarioDisponivel(processoTrf, sala, data.getTime(), 60);
		
		Calendar dtInicioEsperada = Calendar.getInstance();
		dtInicioEsperada.set(2013, 05, 17, 8, 0);
		
		Calendar dtFimEsperada = Calendar.getInstance();
		dtFimEsperada.set(2013, 05, 17, 9, 0);
		
		Calendar audMarcadaDtInicio = Calendar.getInstance();
		audMarcadaDtInicio.setTime(audMarcada.getDtInicio());
		
		Calendar audMarcadaDtFim= Calendar.getInstance();
		audMarcadaDtFim.setTime(audMarcada.getDtFim());
		
		
		//Valida data de início
		assertEquals(dtInicioEsperada.get(Calendar.YEAR), audMarcadaDtInicio.get(Calendar.YEAR));
		assertEquals(dtInicioEsperada.get(Calendar.MONTH), audMarcadaDtInicio.get(Calendar.MONTH));
		assertEquals(dtInicioEsperada.get(Calendar.DAY_OF_MONTH), audMarcadaDtInicio.get(Calendar.DAY_OF_MONTH));
		assertEquals(dtInicioEsperada.get(Calendar.HOUR_OF_DAY), audMarcadaDtInicio.get(Calendar.HOUR_OF_DAY));
		assertEquals(dtInicioEsperada.get(Calendar.MINUTE), audMarcadaDtInicio.get(Calendar.MINUTE));
		
		//Valida data de término
		assertEquals(dtFimEsperada.get(Calendar.YEAR), audMarcadaDtFim.get(Calendar.YEAR));
		assertEquals(dtFimEsperada.get(Calendar.MONTH), audMarcadaDtFim.get(Calendar.MONTH));
		assertEquals(dtFimEsperada.get(Calendar.DAY_OF_MONTH), audMarcadaDtFim.get(Calendar.DAY_OF_MONTH));
		assertEquals(dtFimEsperada.get(Calendar.HOUR_OF_DAY), audMarcadaDtFim.get(Calendar.HOUR_OF_DAY));
		assertEquals(dtFimEsperada.get(Calendar.MINUTE), audMarcadaDtFim.get(Calendar.MINUTE));
				
		assertEquals(sala, audMarcada.getSalaAudiencia());
	}
	
	@Test
	public void seAudienciaFoiRedesignadaPodeMarcarNovaAudienciaNoHorario() {
		Calendar data = Calendar.getInstance();
		data.set(2013, 5, 04, 0, 0);
		
		when(prazosProcessuaisService.ehDiaUtilJudicial(Matchers.<Date>any(), Matchers.<OrgaoJulgador>any())).thenReturn(true);
		
		List<ProcessoAudiencia> audiencias = new ArrayList<ProcessoAudiencia>();
		br.jus.pje.nucleo.entidades.
		//Configura audiencia para 10/05/2013 08:00 (90 minutos)
		ProcessoAudiencia audiencia1 = configuraAudiencia(10, 05, 2013, 8, 0, 90);
		audiencia1.setStatusAudiencia(StatusAudienciaEnum.M);
		audiencias.add(audiencia1);
		
		//Configura audiencia para 10/05/2013 14:00 (90 minutos) REDESIGNADA
		ProcessoAudiencia audiencia2 = configuraAudiencia(10, 05, 2013, 14, 0, 90);
		audiencia2.setStatusAudiencia(StatusAudienciaEnum.R);
		audiencias.add(audiencia2);
		
		when(processoAudienciaDAO.getAudienciasMarcadas(Matchers.<Sala>any(), Matchers.<Date>any())).thenReturn(audiencias);		
		
		processoAudienciaManager.setPrazosProcessuaisService(prazosProcessuaisService);
		processoAudienciaManager.setProcessoAudienciaDAO(processoAudienciaDAO);
		
		ProcessoAudiencia audMarcada = processoAudienciaManager.procurarPrimeiroHorarioDisponivel(processoTrf, sala, data.getTime(), 60);
		
		Calendar dtInicioEsperada = Calendar.getInstance();
		dtInicioEsperada.set(2013, 05, 10, 14, 0);
		
		Calendar dtFimEsperada = Calendar.getInstance();
		dtFimEsperada.set(2013, 05, 10, 15, 0);
		
		Calendar audMarcadaDtInicio = Calendar.getInstance();
		audMarcadaDtInicio.setTime(audMarcada.getDtInicio());
		
		Calendar audMarcadaDtFim= Calendar.getInstance();
		audMarcadaDtFim.setTime(audMarcada.getDtFim());
		
		//Valida data de início
		assertEquals(dtInicioEsperada.get(Calendar.YEAR), audMarcadaDtInicio.get(Calendar.YEAR));
		assertEquals(dtInicioEsperada.get(Calendar.MONTH), audMarcadaDtInicio.get(Calendar.MONTH));
		assertEquals(dtInicioEsperada.get(Calendar.DAY_OF_MONTH), audMarcadaDtInicio.get(Calendar.DAY_OF_MONTH));
		assertEquals(dtInicioEsperada.get(Calendar.HOUR_OF_DAY), audMarcadaDtInicio.get(Calendar.HOUR_OF_DAY));
		assertEquals(dtInicioEsperada.get(Calendar.MINUTE), audMarcadaDtInicio.get(Calendar.MINUTE));
		
		//Valida data de término
		assertEquals(dtFimEsperada.get(Calendar.YEAR), audMarcadaDtFim.get(Calendar.YEAR));
		assertEquals(dtFimEsperada.get(Calendar.MONTH), audMarcadaDtFim.get(Calendar.MONTH));
		assertEquals(dtFimEsperada.get(Calendar.DAY_OF_MONTH), audMarcadaDtFim.get(Calendar.DAY_OF_MONTH));
		assertEquals(dtFimEsperada.get(Calendar.HOUR_OF_DAY), audMarcadaDtFim.get(Calendar.HOUR_OF_DAY));
		assertEquals(dtFimEsperada.get(Calendar.MINUTE), audMarcadaDtFim.get(Calendar.MINUTE));
		
		
	}

	@Test
	public void audienciaMarcadaComInicioAntesDoPeriodoMasHorarioAindaPossuiTempoSuficienteDeveMarcarAudienciaNoTempoRestanteDoHorario() {
		Calendar data = Calendar.getInstance();
		data.set(2013, Calendar.JUNE, 4, 0, 0);
					
		when(prazosProcessuaisService.ehDiaUtilJudicial(Matchers.<Date>any(), Matchers.<OrgaoJulgador>any())).thenReturn(true);
		
		List<ProcessoAudiencia> audiencias = new ArrayList<ProcessoAudiencia>();
		
		//Configura audiencia para 10/06/2013 de 07:00 às 09:00 (120 minutos)
		ProcessoAudiencia audiencia1 = configuraAudiencia(10, Calendar.JUNE, 2013, 7, 0, 120);
		audiencia1.setStatusAudiencia(StatusAudienciaEnum.M);
		audiencias.add(audiencia1);
		
		when(processoAudienciaDAO.getAudienciasMarcadas(sala, data.getTime())).thenReturn(audiencias);
		
		processoAudienciaManager.setPrazosProcessuaisService(prazosProcessuaisService);
		processoAudienciaManager.setProcessoAudienciaDAO(processoAudienciaDAO);
		
		ProcessoAudiencia audMarcada = processoAudienciaManager.procurarPrimeiroHorarioDisponivel(processoTrf, sala, data.getTime(), 60);
		
		Calendar dtInicioEsperada = Calendar.getInstance();
		dtInicioEsperada.set(2013, Calendar.JUNE, 10, 9, 0);
		
		Calendar dtFimEsperada = Calendar.getInstance();
		dtFimEsperada.set(2013, Calendar.JUNE, 10, 10, 0);
		
		Calendar audMarcadaDtInicio = Calendar.getInstance();
		audMarcadaDtInicio.setTime(audMarcada.getDtInicio());
		
		Calendar audMarcadaDtFim= Calendar.getInstance();
		audMarcadaDtFim.setTime(audMarcada.getDtFim());
		
		//Valida data de início
		assertEquals(dtInicioEsperada.get(Calendar.YEAR), audMarcadaDtInicio.get(Calendar.YEAR));
		assertEquals(dtInicioEsperada.get(Calendar.MONTH), audMarcadaDtInicio.get(Calendar.MONTH));
		assertEquals(dtInicioEsperada.get(Calendar.DAY_OF_MONTH), audMarcadaDtInicio.get(Calendar.DAY_OF_MONTH));
		assertEquals(dtInicioEsperada.get(Calendar.HOUR_OF_DAY), audMarcadaDtInicio.get(Calendar.HOUR_OF_DAY));
		assertEquals(dtInicioEsperada.get(Calendar.MINUTE), audMarcadaDtInicio.get(Calendar.MINUTE));
		
		//Valida data de término
		assertEquals(dtFimEsperada.get(Calendar.YEAR), audMarcadaDtFim.get(Calendar.YEAR));
		assertEquals(dtFimEsperada.get(Calendar.MONTH), audMarcadaDtFim.get(Calendar.MONTH));
		assertEquals(dtFimEsperada.get(Calendar.DAY_OF_MONTH), audMarcadaDtFim.get(Calendar.DAY_OF_MONTH));
		assertEquals(dtFimEsperada.get(Calendar.HOUR_OF_DAY), audMarcadaDtFim.get(Calendar.HOUR_OF_DAY));
		assertEquals(dtFimEsperada.get(Calendar.MINUTE), audMarcadaDtFim.get(Calendar.MINUTE));
			
		assertEquals(sala, audMarcada.getSalaAudiencia());
	}	

	private ProcessoAudiencia configuraAudiencia(int dia, int mes, int ano, int hora, int minuto, int tempoAudiencia) {
		ProcessoAudiencia audiencia = new ProcessoAudiencia();		
		audiencia.setSalaAudiencia(sala);
		Calendar horarioInicio = Calendar.getInstance();
		horarioInicio.set(ano, mes, dia, hora, minuto);
		audiencia.setDtInicio(horarioInicio.getTime());
		horarioInicio.add(Calendar.MINUTE, tempoAudiencia);
		audiencia.setDtFim(horarioInicio.getTime());
		
		return audiencia;
	}
}
