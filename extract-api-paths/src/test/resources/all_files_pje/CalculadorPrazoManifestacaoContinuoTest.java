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

public class CalculadorPrazoManifestacaoContinuoTest {

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
		
		this.eventos.add(new CalendarioEvento(20, 12, 2016, 31, 01, 2017, false, true, true, false)); // Feriado Nacional e Anual de Tiradentes 
		
		Calendar dtCiencia = novaData(03, 02, 2017),
				dtEsperada = novaData(7, 3, 2017, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 30, ContagemPrazoEnum.M), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmHorasSemFinalSemanaOuFeriadoOuSuspensao() {
		
		Calendar dtCiencia = novaData(7, 2, 2017, 15, 16, 56),
				dtEsperada = novaData(9, 2, 2017, 15, 16, 56); // 24 horas
		
		assertThat(this.calculadorPrazos.calcularEmHoras(dtCiencia, 48), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasComFinalSemanaSemFeriadoOuSuspensao() {
		Calendar dtCiencia = novaData(4, 4, 2016),
				dtEsperada = novaData(14, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 10, ContagemPrazoEnum.M), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmHorasComFinalSemanaSemFeriadoOuSuspensao() {
		Calendar dtCiencia = novaData(7, 4, 2016, 23, 59, 59),
				dtEsperada = novaData(9, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmHoras(dtCiencia, 48), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasComFinalSemanaSemSuspensaoNoCursoTerminandoEmFeriado() {
		
		Calendar dtCiencia = novaData(11, 4, 2016),
				dtEsperada = novaData(22, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 10, ContagemPrazoEnum.M), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasComPrazoIniciandoEmFinalSemanaSemFeriadoOuSuspensao() {

		Calendar dtCiencia = novaData(1, 4, 2016),
				dtEsperada = novaData(13, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 10, ContagemPrazoEnum.M), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasComPrazoIniciandoEmFinalSemanaComFeriadoNoCursoSemSuspensao() {

		Calendar dtCiencia = novaData(15, 4, 2016),
				dtEsperada = novaData(27, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 10, ContagemPrazoEnum.M), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasComPrazoTerminandoEmFeriadoSemSuspensaoNoCurso() {
		
		eventos.add(new CalendarioEvento(20, 4, 2016, null, null, null, false, true, false, false)); // Feriado inventado para testar
		
		Calendar dtCiencia = novaData(8, 4, 2016),
				dtEsperada = novaData(22, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 10, ContagemPrazoEnum.M), is(dtEsperada));		
	}

	@Test
	public void testaCalcularEmDiasComPrazoIniciandoEmFinalSemanaTerminandoEmFeriadoComSuspensaoNoCurso() {
		
		eventos.add(new CalendarioEvento(13, 4, 2016, null, null, null, false, false, true, false)); // Suspensao inventado para testar
		
		Calendar dtCiencia = novaData(8, 4, 2016),
				dtEsperada = novaData(22, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 10, ContagemPrazoEnum.M), is(dtEsperada));		
	}

	@Test
	public void testaCalcularEmDiasParaUmDiaPrazoIniciandoEmFeriado() {
					
		Calendar dtCiencia = novaData(20, 4, 2016),
				dtEsperada = novaData(22, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 1, ContagemPrazoEnum.M), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasParaUmDiaPrazoIniciandoEmIndisponibilidadeSistema() {

		eventos.add(new CalendarioEvento(13, 4, 2016, null, null, null, false, false, false, true)); // Indisponibilidade
		
		Calendar dtCiencia = novaData(12, 4, 2016),
				dtEsperada = novaData(14, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 1, ContagemPrazoEnum.M), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasParaDoisDiasPrazoIniciandoEmIndisponibilidadeSistemaTerminandoEmSuspensaoPrazo() {

		eventos.add(new CalendarioEvento(13, 4, 2016, null, null, null, false, false, false, true)); // Indisponibilidade
		eventos.add(new CalendarioEvento(14, 4, 2016, null, null, null, false, false, true, false)); // Suspensao de Prazo
		
		Calendar dtCiencia = novaData(12, 4, 2016),
				dtEsperada = novaData(18, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 2, ContagemPrazoEnum.M), is(dtEsperada));
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