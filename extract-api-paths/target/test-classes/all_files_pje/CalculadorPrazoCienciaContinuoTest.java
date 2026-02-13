package br.jus.cnj.pje.servicos.prazos;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.jus.pje.nucleo.entidades.CalendarioEvento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.enums.ContagemPrazoEnum;

public class CalculadorPrazoCienciaContinuoTest {

	private CalculadorPrazoContinuo calculadorPrazos;
	
	private Calendario calendario;
	private List<CalendarioEvento> eventos = new ArrayList<CalendarioEvento>();
	
	@Before
	public void preparaAmbiente() {
		
		this.eventos.add(new CalendarioEvento(21, 4, null, null, null, null, false, true, false, false)); // Feriado Nacional e Anual de Tiradentes 
		this.calendario = new Calendario(new OrgaoJulgador(), eventos);
		this.calculadorPrazos = new CalculadorPrazoContinuo(calendario);
	}
	
	@Test
	public void testaCalcularEmDiasSemFinalSemanaOuFeriadoOuSuspensao() {
		
		Calendar dtIntimacao = novaData(4, 4, 2016),
				dtEsperada = novaData(8, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtIntimacao, 4, ContagemPrazoEnum.C), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasComFinalSemanaSemFeriadoOuSuspensao() {
		
		Calendar dtIntimacao = novaData(4, 4, 2016),
				dtEsperada = novaData(14, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtIntimacao, 10, ContagemPrazoEnum.C), is(dtEsperada));
	}
	
	@Test
	public void testaCalcularEmDiasComFinalSemanaSemSuspensaoNoCursoTerminandoEmFeriado() {
		
		Calendar dtIntimacao = novaData(11, 4, 2016),
				dtEsperada = novaData(22, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtIntimacao, 10, ContagemPrazoEnum.C), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasComPrazoIniciandoEmFinalSemanaSemFeriadoOuSuspensao() {

		Calendar dtIntimacao = novaData(1, 4, 2016),
				dtEsperada = novaData(11, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtIntimacao, 10, ContagemPrazoEnum.C), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasComPrazoIniciandoEmFinalSemanaComFeriadoNoCursoSemSuspensao() {

		Calendar dtIntimacao = novaData(15, 4, 2016),
				dtEsperada = novaData(25, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtIntimacao, 10, ContagemPrazoEnum.C), is(dtEsperada));
	}
	
	@Test
	public void testaCalcularEmDiasComPrazoTerminandoEmFeriadoGerminadoSemSuspensaoNoCurso() {
		
		eventos.add(new CalendarioEvento(20, 4, 2016, null, null, null, false, true, false, false)); // Feriado inventado para testar
		
		Calendar dtIntimacao = novaData(8, 4, 2016),
				dtEsperada = novaData(18, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtIntimacao, 10, ContagemPrazoEnum.C), is(dtEsperada));		
	}
	
	@Test
	public void testaCalcularEmDiasComPrazoIniciandoEmFinalSemanaTerminandoEmFeriadoComSuspensaoNoCurso() {
		
		eventos.add(new CalendarioEvento(13, 4, 2016, null, null, null, false, false, true, false)); // Suspensao inventado para testar
		
		Calendar dtIntimacao = novaData(8, 4, 2016),
				dtEsperada = novaData(18, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtIntimacao, 10, ContagemPrazoEnum.C), is(dtEsperada));		
	}


	@Test
	public void testaCalcularEmDiasParaUmDiaPrazoIniciandoEmFeriado() {
					
		Calendar dtIntimacao = novaData(20, 4, 2016),
				dtEsperada = novaData(22, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtIntimacao, 1, ContagemPrazoEnum.C), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasParaUmDiaPrazoIniciandoEmIndisponibilidadeSistema() {

		eventos.add(new CalendarioEvento(13, 4, 2016, null, null, null, false, false, false, true)); // Indisponibilidade
		
		Calendar dtIntimacao = novaData(12, 4, 2016),
				dtEsperada = novaData(14, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtIntimacao, 1, ContagemPrazoEnum.C), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasParaDoisDiaPrazoIniciandoEmIndisponibilidadeSistemaTerminandoEmSuspensaoPrazo() {

		eventos.add(new CalendarioEvento(13, 4, 2016, null, null, null, false, false, false, true)); // Indisponibilidade
		eventos.add(new CalendarioEvento(14, 4, 2016, null, null, null, false, false, true, false)); // Suspensao de Prazo
		
		Calendar dtIntimacao = novaData(12, 4, 2016),
				dtEsperada = novaData(15, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtIntimacao, 2, ContagemPrazoEnum.C), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasUtilizandoCache() {
		
		Calendar dataFinal = this.calculadorPrazos.calcularEmDias(novaData(4, 4, 2016), 10, ContagemPrazoEnum.C);

		// Calcula qualquer coisa
		this.calculadorPrazos.calcularEmDias(novaData(5, 4, 2016), 10, ContagemPrazoEnum.C);
		this.calculadorPrazos.calcularEmDias(novaData(6, 4, 2016), 10, ContagemPrazoEnum.C);
		this.calculadorPrazos.calcularEmDias(novaData(7, 4, 2016), 10, ContagemPrazoEnum.C);

		// Calcula na mesma data
		Calendar dataFinalCache = this.calculadorPrazos.calcularEmDias(novaData(4, 4, 2016), 10, ContagemPrazoEnum.C);
		
		// Verifica se a mesma referencia do objeto retornado no inicio
		assertTrue(dataFinalCache == dataFinal);		
	}
	
	private Calendar novaData(int dia, int mes, int ano) {
		return new GregorianCalendar(ano, mes - 1, dia, 0, 0, 0);
	}
	
	private Calendar novaData(int dia, int mes, int ano, int hora, int minuto, int segundo) {
		return new GregorianCalendar(ano, mes - 1, dia, hora, minuto, segundo);
	}
}