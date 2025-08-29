package br.jus.cnj.pje.nucleo;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

import org.junit.Test;

import br.jus.pje.nucleo.util.DateUtil;

public class DateUtilTest {

	@Test
	public void diferencaDiasTest() throws ParseException {
		final String dtInicio = "10/05/2011";
		final String dtFim = "20/05/2011";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		long diferenca = DateUtil.diferencaDias(sdf.parse(dtFim), sdf.parse(dtInicio));
		assertEquals(10, diferenca);
	}

	@Test
	public void isBetweenDatesTest() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		final Date data = sdf.parse("01/02/2011");
		final Date dataInicio = sdf.parse("01/01/2011");
		final Date dataFim = sdf.parse("02/02/2011");

		boolean isBetweenDate = DateUtil.isBetweenDates(data, dataInicio, dataFim);
		assertEquals(true, isBetweenDate);

		final Date dataForaIntervalo = sdf.parse("01/03/2011");
		isBetweenDate = DateUtil.isBetweenDates(dataForaIntervalo, dataInicio, dataFim);
		assertEquals(false, isBetweenDate);
	}

	@Test
	public void isBetweenHoursTest() throws ParseException {
		Calendar calendar = Calendar.getInstance();

		// Retornará um resultado verdadeiro
		// calendar.set(year, month, date, hourOfDay, minute, second)
		calendar.set(2011, 1, 1, 1, 1, 1);
		Time hora = new Time(calendar.getTimeInMillis());

		calendar.set(2011, 1, 1, 1, 0, 1);
		final Time horaInicio = new Time(calendar.getTimeInMillis());

		calendar.set(2011, 1, 1, 1, 2, 1);
		final Time horaFim = new Time(calendar.getTimeInMillis());

		boolean isBetweenHour = DateUtil.isBetweenHours(hora, horaInicio, horaFim);
		assertEquals(true, isBetweenHour);

		// Retornará um resultado Falso
		// calendar.set(year, month, date, hourOfDay, minute, second)
		calendar.set(2011, 1, 1, 0, 1, 1);
		final Time horaForaIntervalo = new Time(calendar.getTimeInMillis());

		isBetweenHour = DateUtil.isBetweenHours(horaForaIntervalo, horaInicio, horaFim);
		assertEquals(false, isBetweenHour);
	}

	@Test
	public void validateHourTest() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		Date data = sdf.parse("08:00");
		Time horaInicio = new Time(data.getTime());
		data = sdf.parse("08:01");
		Time horaFim = new Time(data.getTime());
		assertEquals(true, DateUtil.validateHour(horaInicio, horaFim));
		assertEquals(false, DateUtil.validateHour(horaFim, horaInicio));

	}

	@Test
	public void getDataAtualTest() throws ParseException {
		DateUtil dateUtil = new DateUtil();

		Calendar calendar = Calendar.getInstance();
		String dataAtual = "";
		String diaFormatado = "";
		String mesFormatado = "";
		String anoFormatado = "";

		int dia = calendar.get(Calendar.DAY_OF_MONTH);
		int mes = calendar.get(Calendar.MONTH) + 1;
		int ano = calendar.get(Calendar.YEAR);

		anoFormatado = Integer.toString(ano);
		String anoConvert = Integer.toString(ano);

		// variável que recebe os dois últimos digitos do ano
		String anoMascara = anoConvert.substring(2, 4);

		if (dia < 10) {
			diaFormatado = "0" + Integer.toString(dia);
		} else {
			diaFormatado = Integer.toString(dia);
		}

		if (mes < 10) {
			mesFormatado = "0" + Integer.toString(mes);
		} else {
			mesFormatado = Integer.toString(mes);
		}

		String formato = "dd/MM/yyyy";
		dataAtual = diaFormatado + "/" + mesFormatado + "/" + anoFormatado;

		String formato2 = "dd/MM/yy";
		String dataAtual2 = diaFormatado + "/" + mesFormatado + "/" + anoMascara;

		String formato3 = "yyyy/MM/dd";
		String dataAtual3 = anoFormatado + "/" + mesFormatado + "/" + diaFormatado;

		String formato4 = "yy/MM/dd";
		String dataAtual4 = anoMascara + "/" + mesFormatado + "/" + diaFormatado;

		assertEquals(dataAtual, dateUtil.getDataAtual(formato));
		assertEquals(dataAtual2, dateUtil.getDataAtual(formato2));
		assertEquals(dataAtual3, dateUtil.getDataAtual(formato3));
		assertEquals(dataAtual4, dateUtil.getDataAtual(formato4));

	}

	@Test
	public void getDataFormatadaTest() throws ParseException {
		Calendar data = Calendar.getInstance();
		data.set(2011, 05, 29);
		Calendar date = Calendar.getInstance();
		date.set(2011, 05, 30);

		assertEquals("29/06/2011", DateUtil.getDataFormatada(data.getTime(), "dd/MM/yyyy"));
		assertEquals("2011-06-30", DateUtil.getDataFormatada(date.getTime(), "yyyy-MM-dd"));
	}

	@Test
	public void convertToMinutesTest() {
		Calendar data = Calendar.getInstance();
		
		data.set(2011, 05, 29, 0, 0, 0);
		assertEquals(0, DateUtil.convertToMinutes(data.getTime()));
		
		data.set(2011, 05, 29, 0, 1, 0);
		assertEquals(1, DateUtil.convertToMinutes(data.getTime()));
		
		data.set(2011, 05, 29, 0, 5, 0);
		assertEquals(5, DateUtil.convertToMinutes(data.getTime()));
		
		data.set(2011, 05, 29, 0, 29, 0);
		assertEquals(29, DateUtil.convertToMinutes(data.getTime()));
		
		data.set(2011, 05, 29, 0, 59, 0);
		assertEquals(59, DateUtil.convertToMinutes(data.getTime()));
		
		data.set(2011, 05, 29, 1, 0, 0);
		assertEquals(60, DateUtil.convertToMinutes(data.getTime()));
		
		data.set(2011, 05, 29, 1, 30, 0);
		assertEquals(90, DateUtil.convertToMinutes(data.getTime()));
		
		data.set(2011, 05, 29, 2, 0, 0);
		assertEquals(120, DateUtil.convertToMinutes(data.getTime()));
		
		data.set(2011, 05, 29, 10, 0, 0);
		assertEquals(600, DateUtil.convertToMinutes(data.getTime()));
		
		data.set(2011, 05, 29, 0, 0, 1);
		assertEquals(0, DateUtil.convertToMinutes(data.getTime()));
		
		data.set(2011, 05, 29, 0, 0, 15);
		assertEquals(0, DateUtil.convertToMinutes(data.getTime()));
	}

	
	@Test
	public void isMesmoHorarioTest() {
		Calendar data1 = Calendar.getInstance();
		Calendar data2 = Calendar.getInstance();
		
		data1.set(2011, 05, 29, 11, 0, 0);
		data1.set(Calendar.MILLISECOND, 1);
		
		data2.set(2011, 05, 29, 11, 0, 0);
		data2.set(Calendar.MILLISECOND, 2);
		assertTrue(DateUtil.isMesmoHorario(data1.getTime(), data2.getTime()));
		
		data2.set(2012, 05, 29, 11, 0, 0);		
		assertTrue(DateUtil.isMesmoHorario(data1.getTime(), data2.getTime()));
		//Garantir que no mudou o ano original do parmetro enviado
		assertEquals(2011, data1.get(Calendar.YEAR)); 
		
		data2.set(2011, 06, 29, 11, 0, 0);		
		assertTrue(DateUtil.isMesmoHorario(data1.getTime(), data2.getTime()));
		//Garantir que no mudou o ms original do parmetro enviado
		assertEquals(5, data1.get(Calendar.MONTH));
		
		data2.set(2011, 05, 28, 11, 0, 0);		
		assertTrue(DateUtil.isMesmoHorario(data1.getTime(), data2.getTime()));
		//Garantir que no mudou o dia original do parmetro enviado
		assertEquals(29, data1.get(Calendar.DATE));
		
		data2.set(2011, 05, 29, 14, 0, 0);
		assertFalse(DateUtil.isMesmoHorario(data1.getTime(), data2.getTime()));
		
		data2.set(2011, 05, 29, 11, 8, 0);
		assertFalse(DateUtil.isMesmoHorario(data1.getTime(), data2.getTime()));
		
		data2.set(2011, 05, 29, 11, 0, 59);
		assertFalse(DateUtil.isMesmoHorario(data1.getTime(), data2.getTime()));
	}
	
}

