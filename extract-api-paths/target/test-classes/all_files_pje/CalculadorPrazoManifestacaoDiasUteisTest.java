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

public class CalculadorPrazoManifestacaoDiasUteisTest {

	private CalculadorPrazoDiasUteis calculadorPrazos;
	
	private Calendario calendario;
	private List<CalendarioEvento> feriados = new ArrayList<CalendarioEvento>();
	
	@Before
	public void preparaAmbiente() {
		
		this.feriados.add(new CalendarioEvento(21, 4, null, null, null, null, false, true, false, false)); // Feriado Nacional e Anual de Tiradentes 
		
		this.calendario = new Calendario(new OrgaoJulgador(), feriados);
		this.calculadorPrazos = new CalculadorPrazoDiasUteis(calendario);
	}
	
	@Test
	public void testaCalcularEmDiasSemFinalSemanaOuFeriadoOuSuspensao() {
		
		Calendar dtCiencia = novaData(4, 4, 2016),
				dtEsperada = novaData(8, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 4, ContagemPrazoEnum.M), is(dtEsperada));
	}
	
	@Test
	public void testaCalcularEmDiasComFinalSemanaSemFeriadoOuSuspensao() {
		
		Calendar dtCiencia = novaData(4, 4, 2016),
				dtEsperada = novaData(18, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 10, ContagemPrazoEnum.M), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasComFinalSemanaEFeriadoSemSuspensao() {
		
		Calendar dtCiencia = novaData(11, 4, 2016),
				dtEsperada = novaData(26, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 10, ContagemPrazoEnum.M), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasComPrazoIniciandoNaSextaSemFeriadoOuSuspensao() {

		Calendar dtCiencia = novaData(1, 4, 2016),
				dtEsperada = novaData(15, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 10, ContagemPrazoEnum.M), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasComPrazoIniciandoNaSextaComFeriadoSemSuspensao() {

		Calendar dtCiencia = novaData(15, 4, 2016),
				dtEsperada = novaData(2, 5, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 10, ContagemPrazoEnum.M), is(dtEsperada));
	}

	@Test
	public void testaCalcularEmDiasComPrazoTerminandoEmFeriadoSemFeriadoOuSuspensaoNoCurso() {
		
		Calendar dtCiencia = novaData(7, 4, 2016),
				dtEsperada = novaData(22, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 10, ContagemPrazoEnum.M), is(dtEsperada));		
	}

	@Test
	public void testaCalcularEmDiasComPrazoTerminandoEmFeriadoGerminadoSemSuspensaoNoCurso() {
		
		feriados.add(new CalendarioEvento(20, 4, 2016, null, null, null, false, true, false, false)); // Feriado inventado para testar
		
		Calendar dtCiencia = novaData(6, 4, 2016),
				dtEsperada = novaData(22, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 10, ContagemPrazoEnum.M), is(dtEsperada));		
	}

	@Test
	public void testaCalcularEmDiasComPrazoTerminandoEmFeriadoGerminadoComSuspensaoNoCurso() {
		
		feriados.add(new CalendarioEvento(20, 4, 2016, null, null, null, false, true, false, false)); // Feriado inventado para testar
		feriados.add(new CalendarioEvento(12, 4, 2016, null, null, null, false, false, true, false)); // Suspensao de prazo
		
		Calendar dtCiencia = novaData(6, 4, 2016),
				dtEsperada = novaData(25, 4, 2016, 23, 59, 59);
		
		assertThat(this.calculadorPrazos.calcularEmDias(dtCiencia, 10, ContagemPrazoEnum.C), is(dtEsperada));		
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