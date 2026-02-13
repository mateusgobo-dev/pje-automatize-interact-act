package br.jus.cnj.pje.servicos.prazos;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.jus.pje.nucleo.entidades.CalendarioEvento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

public class CalendarioTest {

	private Calendario calendario;
	private List<CalendarioEvento> eventos = new ArrayList<CalendarioEvento>();
	
	@Before
	public void preparaAmbiente() {
																			// inJudiciario, inFeriado, inSuspendePrazo, indisponibilidadeSistema
		this.eventos.add(new CalendarioEvento(21, 4, null, null, null, null, false, true, false, false)); // Feriado Nacional e Anual de Tiradentes
		this.eventos.add(new CalendarioEvento(20, 12, null, 6, 1, null, false, false, true, false)); // Recesso forense Anual

		this.calendario = new Calendario(new OrgaoJulgador(), eventos);
	}
		
	@Test
	public void testaIsFeriado() {
		
		Calendar data = novaData(21, 4, 2016);

		assertTrue(this.calendario.isFeriado(data));
	}
	
	@Test
	public void testaIsFeriadoComDataNaoFeriado() {
		
		Calendar data = novaData(4, 4, 2016);
		
		assertFalse(this.calendario.isFeriado(data));
	}
	
	@Test
	public void testaIsFeriadoComDataSuspensaoPrazo() {
		
		Calendar data = novaData(20, 12, 2016);

		assertFalse(this.calendario.isFeriado(data));
	}
	
	@Test
	public void testaIsSuspensaoPrazo() {
		
		Calendar data = novaData(31, 12, 2016);
		
		assertTrue(this.calendario.isSuspensaoPrazo(data));
	}
	
	@Test
	public void testaIsSuspensaoPrazoComDataNormal() {
		
		Calendar data = novaData(19, 12, 2016);
		
		assertFalse(this.calendario.isSuspensaoPrazo(data));
	}
		
	@Test
	public void testaIsSuspensaoPrazoComDataFeriado() {
		
		Calendar data = novaData(21, 4, 2016);
		
		assertFalse(this.calendario.isSuspensaoPrazo(data));
	}
	
	@Test
	public void testaIsFinalSemanaComSabado() {
		
		Calendar data = novaData(9, 4, 2016);
		
		assertTrue(this.calendario.isFinalSemana(data));
	}

	@Test
	public void testaIsFinalSemanaComDomingo() {
		
		Calendar data = novaData(10, 4, 2016);
		
		assertTrue(this.calendario.isFinalSemana(data));
	}
	
	@Test
	public void testaIsFinalSemanaComDataDiaSemana() {
		
		Calendar data = novaData(11, 4, 2016);
		
		assertFalse(this.calendario.isFinalSemana(data));
	}
	
	@Test
	public void testaIsDiaNaoUtilOuHouveSuspensaoPrazoComDataEmDiaUtil() {

		Calendar data = novaData(20, 4, 2016);

		assertFalse(this.calendario.isDiaNaoUtilOuHouveSuspensaoPrazo(data));
	}
	
	@Test
	public void testaIsDiaNaoUtilOuHouveSuspensaoPrazoComDataEmPeriodoDeSuspensaoPrazo() {
		
		Calendar data = novaData(22, 12, 2016);
		
		assertTrue(this.calendario.isDiaNaoUtilOuHouveSuspensaoPrazo(data));
	}
	
	@Test
	public void testaIsDiaNaoUtilOuHouveSuspensaoPrazoComDataEmFeriado() {

		Calendar data = novaData(21, 4, 2016);

		assertTrue(this.calendario.isDiaNaoUtilOuHouveSuspensaoPrazo(data));
	}
	
	@Test
	public void testaIsDiaNaoUtilOuHouveSuspensaoPrazoComDataEmFinalSemana() {
		
		Calendar data = novaData(9, 4, 2016);
		
		assertTrue(this.calendario.isDiaNaoUtilOuHouveSuspensaoPrazo(data));
	}
	
	@Test
	public void testaIsDiaNaoUtilOuHouveSuspensaoPrazoComDataEmDiaQueHouveIndisponibilidadeSistemaEhSuspensaoPrazo() {
		
		this.eventos.add(new CalendarioEvento(20, 4, 2016, null, null, null, false, false, false, true));
		this.eventos.add(new CalendarioEvento(20, 4, 2016, null, null, null, false, false, true, false));
		
		Calendar data = novaData(20, 4, 2016);
		
		assertTrue(this.calendario.isDiaNaoUtilOuHouveSuspensaoPrazo(data));
	}
	
	@Test
	public void testaIsDiaNaoUtilOuHouveSuspensaoPrazoOuIndisponibilidadeSistema() {
		
		this.eventos.add(new CalendarioEvento(20, 4, 2016, null, null, null, false, false, false, true));
		
		Calendar data = novaData(20, 4, 2016);
		
		assertTrue(this.calendario.isDiaNaoUtilOuHouveSuspensaoPrazoOuIndisponibilidadeSistema(data));
	}
	
	@Test
	public void testaIsDiaNaoUtilOuHouveSuspensaoPrazoOuIndisponibilidadeSistemaComDataEmDiaUtil() {
				
		Calendar data = novaData(22, 4, 2016);
		
		assertFalse(this.calendario.isDiaNaoUtilOuHouveSuspensaoPrazoOuIndisponibilidadeSistema(data));
	}
		
	private Calendar novaData(int dia, int mes, int ano) {
		return new GregorianCalendar(ano, mes - 1, dia, 0, 0, 0);
	}
}
