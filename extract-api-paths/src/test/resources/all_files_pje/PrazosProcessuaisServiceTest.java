package br.jus.cnj.pje.servicos;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.pje.nucleo.entidades.CalendarioEvento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.enums.CategoriaPrazoEnum;
import br.jus.pje.nucleo.enums.ContagemPrazoEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;

public class PrazosProcessuaisServiceTest {
	
	private PrazosProcessuaisService service;
	
	private List<CalendarioEvento> eventos;
	
	private Calendar disponibilizacao;
	
	private Calendar intimacao;
	
	private Calendar dataEsperada;
	
	private Calendario calendario;
	
	@Before
	public void before(){
		service = new PrazosProcessuaisServiceImpl();
		eventos = new ArrayList<CalendarioEvento>();
		calendario = new Calendario(new OrgaoJulgador(), eventos);
	}
	
	@Test
	public void testeCienciaFictaSimples() {
		disponibilizacao = new GregorianCalendar(2013, 3, 1, 14, 15, 23);
		dataEsperada = new GregorianCalendar(2013, 3, 11, 23, 59, 59);
		assertThat(service.obtemDataIntimacaoComunicacaoEletronica(disponibilizacao.getTime(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testeCienciaFictaCalendarioEventoInicial(){
		eventos.add(new CalendarioEvento(1, 4, 2013, 1, 4, 2013, false, true, false, false));
		disponibilizacao = new GregorianCalendar(2013, 3, 1, 14, 15, 23);
		dataEsperada = new GregorianCalendar(2013, 3, 12, 23, 59, 59);
		assertThat(service.obtemDataIntimacaoComunicacaoEletronica(disponibilizacao.getTime(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeCienciaFictaCalendarioEventoFinal(){
		eventos.add(new CalendarioEvento(11, 4, 2013, 11, 4, 2013, false, true, false, false));
		disponibilizacao = new GregorianCalendar(2013, 3, 1, 14, 15, 23);
		dataEsperada = new GregorianCalendar(2013, 3, 12, 23, 59, 59);
		assertThat(service.obtemDataIntimacaoComunicacaoEletronica(disponibilizacao.getTime(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testeCienciaFictaCalendarioEventoFinalIntervalo(){
		eventos.add(new CalendarioEvento(11, 4, 2013, 15, 4, 2013, false, true, false, false));
		disponibilizacao = new GregorianCalendar(2013, 3, 1, 14, 15, 23);
		dataEsperada = new GregorianCalendar(2013, 3, 16, 23, 59, 59);
		assertThat(service.obtemDataIntimacaoComunicacaoEletronica(disponibilizacao.getTime(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testeCienciaFictaCalendarioEventosSimplesGeminados(){
		eventos.add(new CalendarioEvento(11, 4, 2013, 11, 4, 2013, false, true, false, false));
		eventos.add(new CalendarioEvento(12, 4, 2013, 12, 4, 2013, false, true, false, false));
		disponibilizacao = new GregorianCalendar(2013, 3, 1, 14, 15, 23);
		dataEsperada = new GregorianCalendar(2013, 3, 15, 23, 59, 59); // 13 e 14 são sábado e domingo
		assertThat(service.obtemDataIntimacaoComunicacaoEletronica(disponibilizacao.getTime(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeCienciaFictaCalendarioEventosDiversosGeminados(){
		eventos.add(new CalendarioEvento(11, 4, 2013, 15, 4, 2013, false, true, false, false));
		eventos.add(new CalendarioEvento(16, 4, 2013, 16, 4, 2013, false, true, false, false));
		disponibilizacao = new GregorianCalendar(2013, 3, 1, 14, 15, 23);
		dataEsperada = new GregorianCalendar(2013, 3, 17, 23, 59, 59); 
		assertThat(service.obtemDataIntimacaoComunicacaoEletronica(disponibilizacao.getTime(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeCienciaFictaCalendarioEventoContido(){
		eventos.add(new CalendarioEvento(11, 4, 2013, 15, 4, 2013, false, true, false, false));
		eventos.add(new CalendarioEvento(12, 4, 2013, 12, 4, 2013, false, true, false, false));
		disponibilizacao = new GregorianCalendar(2013, 3, 1, 14, 15, 23);
		dataEsperada = new GregorianCalendar(2013, 3, 16, 23, 59, 59); 
		assertThat(service.obtemDataIntimacaoComunicacaoEletronica(disponibilizacao.getTime(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testeCienciaFictaCalendarioEventosIntervalosGeminados(){
		eventos.add(new CalendarioEvento(11, 4, 2013, 15, 4, 2013, false, true, false, false));
		eventos.add(new CalendarioEvento(16, 4, 2013, 17, 4, 2013, false, true, false, false));
		disponibilizacao = new GregorianCalendar(2013, 3, 1, 14, 15, 23);
		dataEsperada = new GregorianCalendar(2013, 3, 18, 23, 59, 59); 
		assertThat(service.obtemDataIntimacaoComunicacaoEletronica(disponibilizacao.getTime(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeCienciaFictaInterseccaoCalendarioEventosIntervalos(){
		eventos.add(new CalendarioEvento(11, 4, 2013, 15, 4, 2013, false, true, false, false));
		eventos.add(new CalendarioEvento(13, 4, 2013, 17, 4, 2013, false, true, false, false));
		disponibilizacao = new GregorianCalendar(2013, 3, 1, 14, 15, 23);
		dataEsperada = new GregorianCalendar(2013, 3, 18, 23, 59, 59); 
		assertThat(service.obtemDataIntimacaoComunicacaoEletronica(disponibilizacao.getTime(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testeCienciaFictaFinalSexta(){
		disponibilizacao = new GregorianCalendar(2013, 3, 2, 14, 15, 23);
		dataEsperada = new GregorianCalendar(2013, 3, 12, 23, 59, 59); 
		assertThat(service.obtemDataIntimacaoComunicacaoEletronica(disponibilizacao.getTime(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeCienciaFictaFinalSabado(){
		disponibilizacao = new GregorianCalendar(2013, 3, 3, 14, 15, 23);
		dataEsperada = new GregorianCalendar(2013, 3, 15, 23, 59, 59); 
		assertThat(service.obtemDataIntimacaoComunicacaoEletronica(disponibilizacao.getTime(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeCienciaFictaFinalDomingo(){
		disponibilizacao = new GregorianCalendar(2013, 3, 3, 14, 15, 23);
		dataEsperada = new GregorianCalendar(2013, 3, 15, 23, 59, 59); 
		assertThat(service.obtemDataIntimacaoComunicacaoEletronica(disponibilizacao.getTime(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testeCienciaFictaFinalDomingoCalendarioEventoSegunda(){
		eventos.add(new CalendarioEvento(15, 4, 2013, null, null, null, false, true, false, false));
		disponibilizacao = new GregorianCalendar(2013, 3, 3, 14, 15, 23);
		dataEsperada = new GregorianCalendar(2013, 3, 16, 23, 59, 59); 
		assertThat(service.obtemDataIntimacaoComunicacaoEletronica(disponibilizacao.getTime(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testeCienciaFictaSuspensaoPrazoInicial(){
		eventos.add(new CalendarioEvento(1, 4, 2015, 5, 4, 2015, false, false, true, false));
		disponibilizacao = new GregorianCalendar(2015, 3, 1, 14, 15, 23);
		dataEsperada = new GregorianCalendar(2015, 3, 16, 23, 59, 59); 
		assertThat(service.obtemDataIntimacaoComunicacaoEletronica(disponibilizacao.getTime(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeCienciaFictaSuspensaoPrazoInterna(){
		eventos.add(new CalendarioEvento(3, 4, 2015, 5, 4, 2015, false, false, true, false));
		disponibilizacao = new GregorianCalendar(2015, 3, 1, 14, 15, 23);
		dataEsperada = new GregorianCalendar(2015, 3, 14, 23, 59, 59); 
		assertThat(service.obtemDataIntimacaoComunicacaoEletronica(disponibilizacao.getTime(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testeCienciaFictaSuspensaoPrazoFinal(){
		eventos.add(new CalendarioEvento(3, 4, 2015, 11, 4, 2015, false, false, true, false));
		disponibilizacao = new GregorianCalendar(2015, 3, 1, 14, 15, 23);
		dataEsperada = new GregorianCalendar(2015, 3, 20, 23, 59, 59); 
		assertThat(service.obtemDataIntimacaoComunicacaoEletronica(disponibilizacao.getTime(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeIntimacaoSimples() {
		intimacao = new GregorianCalendar(2013, 3, 3, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 3, 8, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 5, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testeIntimacaoCalendarioEventoInicial(){
		eventos.add(new CalendarioEvento(3, 4, 2013, null, null, null, false, true, false, false));	// 03/04/2013
		intimacao = new GregorianCalendar(2013, 3, 3, 23, 59, 59);		// 03/04/2013
		dataEsperada = new GregorianCalendar(2013, 3, 9, 23, 59, 59);	// 09/04/2013 23h59m59s
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 5, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeIntimacaoCalendarioEventoFinal(){
		eventos.add(new CalendarioEvento(8, 4, 2013, null, null, null, false, true, false, false));
		intimacao = new GregorianCalendar(2013, 3, 3, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 3, 9, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 5, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testeIntimacaoCalendarioEventoInterno(){
		eventos.add(new CalendarioEvento(5, 4, 2013, null, null, null, false, true, false, false));
		intimacao = new GregorianCalendar(2013, 3, 3, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 3, 8, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 5, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testeIntimacaoCalendarioEventosGeminadosFinais(){
		eventos.add(new CalendarioEvento(8, 4, 2013, null, null, null, false, true, false, false));
		eventos.add(new CalendarioEvento(9, 4, 2013, null, null, null, false, true, false, false));
		intimacao = new GregorianCalendar(2013, 3, 3, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 3, 10, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 5, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeIntimacaoCalendarioEventoSuspensaoGeminados(){
		eventos.add(new CalendarioEvento(8, 4, 2013, null, null, null, false, true, false, false));
		eventos.add(new CalendarioEvento(9, 4, 2013, 11, 4, 2013, false, false, true, false));
		intimacao = new GregorianCalendar(2013, 3, 3, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 3, 12, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 5, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeIntimacaoSuspensaoNoCurso(){
		eventos.add(new CalendarioEvento(5, 4, 2013, 8, 4, 2013, false, false, true, false));
		intimacao = new GregorianCalendar(2013, 3, 3, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 3, 12, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 5, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeIntimacaoSuspensaoNoCursoComSabado(){
		eventos.add(new CalendarioEvento(5, 4, 2013, 6, 4, 2013, false, false, true, false));
		intimacao = new GregorianCalendar(2013, 3, 3, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 3, 10, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 5, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeIntimacaoSuspensaoNoCursoSabDom(){
		eventos.add(new CalendarioEvento(5, 4, 2013, 7, 4, 2013, false, false, true, false));
		intimacao = new GregorianCalendar(2013, 3, 3, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 3, 11, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 5, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeIntimacaoSuspensoesGeminadas(){
		eventos.add(new CalendarioEvento(5, 4, 2013, 6, 4, 2013, false, false, true, false));
		eventos.add(new CalendarioEvento(7, 4, 2013, 8, 4, 2013, false, false, true, false));
		intimacao = new GregorianCalendar(2013, 3, 3, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 3, 12, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 5, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testeIntimacaoSuspensoesIntersecao(){
		/* Issue 19166 - Teste quando a contagem de prazo se dá em um período onde há vários eventos de calendário cadastrados com períodos
		 * sobrepostos entre si (com intersecção entre os eventos de calendário) e o período de suspensão é grande suficiente 
		 * para fazer o sistema iterar a rotina de cálculo várias vezes. 
		 * Quando se tem que iterar a rotina várias vezes, um mesmo período é contado mais de uma vez. 
		 */
		eventos.add(new CalendarioEvento(25, 12, null, null, null, null, false, false, true, false));
		eventos.add(new CalendarioEvento(26, 12, null, null, null, null, false, false, true, false));
		eventos.add(new CalendarioEvento(22, 12, null, null, null, null, false, false, true, false));
		eventos.add(new CalendarioEvento(20, 12, null, 06, 01, null, false, false, true, false));
		eventos.add(new CalendarioEvento(07, 01, null, 19, 01, null, false, false, true, false));
		intimacao = new GregorianCalendar(2014, java.util.Calendar.DECEMBER, 17, 14, 10, 00);
		dataEsperada = new GregorianCalendar(2015, java.util.Calendar.JANUARY, 26, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 9, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	
	/**
	 * Teste de contagem de prazo quando o prazo é iniciado em um ano, termina em outro e há registro de CalendarioEvento forense anual no meio.
	 */
	@Test
	public void testeIntimacaoSuspensoesIntersecaoCalendarioEventoAnual(){
		//Issue: 19165
		eventos.add(new CalendarioEvento(25, 12, null, null, null, null, false, false, true, false));
		eventos.add(new CalendarioEvento(20, 12, null, 06, 01, null, false, false, true, false));
		intimacao = new GregorianCalendar(2014, java.util.Calendar.DECEMBER, 16, 14, 10, 00);
		dataEsperada = new GregorianCalendar(2015, java.util.Calendar.JANUARY, 26, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 23, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testeIntimacaoSuspensaoLimitrofe(){
		eventos.add(new CalendarioEvento(20, 12, 2012, 6, 1, 2013, false, false, true, false));
		eventos.add(new CalendarioEvento(25, 12, 2012, null, null, null, false, true, false, false));
		eventos.add(new CalendarioEvento(1, 1, 2013, null, null, null, false, true, false, false));
		intimacao = new GregorianCalendar(2012, 11, 18, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 0, 10, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 5, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeIntimacaoSuspensaoImediata(){
		eventos.add(new CalendarioEvento(20, 12, 2012, 6, 1, 2013, false, false, true, false));
		eventos.add(new CalendarioEvento(25, 12, 2012, null, null, null, false, true, false, false));
		eventos.add(new CalendarioEvento(1, 1, 2013, null, null, null, false, true, false, false));
		intimacao = new GregorianCalendar(2012, 11, 19, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 0, 11, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 5, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeIntimacaoCalendarioEventoSuspensaoImediata(){
		eventos.add(new CalendarioEvento(19, 12, 2012, null, null, null, false, false, true, false));
		eventos.add(new CalendarioEvento(20, 12, 2012, 6, 1, 2013, false, false, true, false));
		eventos.add(new CalendarioEvento(25, 12, 2012, null, null, null, false, true, false, false));
		eventos.add(new CalendarioEvento(1, 1, 2013, null, null, null, false, true, false, false));
		intimacao = new GregorianCalendar(2012, 11, 19, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 0, 14, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 5, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeIntimacaoFinalSexta(){
		intimacao = new GregorianCalendar(2013, 3, 1, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 3, 5, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 4, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeIntimacaoFinalSabado(){
		intimacao = new GregorianCalendar(2013, 3, 1, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 3, 8, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 5, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeIntimacaoFinalDomingo(){
		intimacao = new GregorianCalendar(2013, 3, 1, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 3, 8, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 6, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeIntimacaoFinalDomingoCalendarioEventoSegunda(){
		eventos.add(new CalendarioEvento(8, 4, 2013, null, null, null, false, true, false, false));
		intimacao = new GregorianCalendar(2013, 3, 1, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 3, 9, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 6, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testeIntimacaoFinalDomingoSuspensaoSemana(){
		eventos.add(new CalendarioEvento(8, 4, 2013, 12, 4, 2013, false, false, true, false));
		intimacao = new GregorianCalendar(2013, 3, 1, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 3, 15, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 6, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testePrazoAnoSimples(){
		intimacao = new GregorianCalendar(2013, 3, 1, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2014, 3, 1, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 1, TipoPrazoEnum.A, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testePrazoAnoDataInexistente(){
		intimacao = new GregorianCalendar(2012, 1, 29, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 2, 1, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 1, TipoPrazoEnum.A, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testePrazoMesFevereiroBissexto(){
		intimacao = new GregorianCalendar(2012, 0, 29, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2012, 2, 1, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 1, TipoPrazoEnum.M, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}

	@Test
	public void testePrazoMesFevereiroComum(){
		intimacao = new GregorianCalendar(2013, 0, 28, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 1, 28, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 1, TipoPrazoEnum.M, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testePrazoMesDataInexistente(){
		intimacao = new GregorianCalendar(2013, 0, 31, 23, 59, 59);
		dataEsperada = new GregorianCalendar(2013, 4, 1, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 3, TipoPrazoEnum.M, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
	
	@Test
	public void testeDiasPerdidosGregorio(){
		intimacao = new GregorianCalendar(1582, 9, 4, 23, 59, 59);
		dataEsperada = new GregorianCalendar(1582, 9, 18, 23, 59, 59);
		assertThat(service.calculaPrazoProcessual(intimacao.getTime(), 2, TipoPrazoEnum.D, calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.M), is(dataEsperada.getTime()));
	}
}

