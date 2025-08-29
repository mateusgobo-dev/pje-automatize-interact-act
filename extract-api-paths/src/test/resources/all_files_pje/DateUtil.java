/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


public class DateUtil{

	public static final int QUANTIDADE_DIAS_SEMANA = 7;
	public static final int QUANTIDADE_MESES_ANO = 12;
	public static final String FORMATO_DATA_HORA_MINUTO_SEGUNDO_MILI = "dd/MM/yyyy_HHmmssSSS";
	public static final String FORMATO_DATA_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static final String FORMATO_DATA = "dd/MM/yyyy";
	private static final int HORAEMMINUTOS = 60;

	/**
	 * Retorna a diferencia em dias entre a data inicial e final informadas.
	 * 
	 * @param dataFim - Data final
	 * @param dataIni - Data Inicial
	 * @return A diferencas em dias das datas informadas.
	 */
	public static long diferencaDias(final Date dataFim, final Date dataIni){
		return (dataFim.getTime() - dataIni.getTime()) / (1000 * 60 * 60 * 24);
	}

	
	/**
	 * Retorna a diferencia em dias entre a data inicial e final informadas.
	 * A diferença para o método "diferencaDias" é que esse seta a hora inicial para 
	 * a primeira do dia e a final para a  última do dia para atender a issue [PJEII-650]. 
	 * 
	 * @param dataFim - Data final
	 * @param dataIni - Data Inicial
	 * @return A diferencas em dias das datas informadas.
	 */
	public static long diferencaEntreDias(final Date dataFim, final Date dataIni){
		return (getEndOfDay(dataFim).getTime() - getBeginningOfDay(dataIni).getTime()) / (1000 * 60 * 60 * 24);
	}
	
	public static String diferencaTempo(Date dataFim, Date dataInicio){
		return diferencaTempo(dataFim, dataInicio, false);
	}
	
	public static String diferencaTempo(Date dataFim, Date dataInicio, boolean incluirMilissegundos){
		if (dataFim == null){
			dataFim = new Date();
		}

		if (dataInicio == null){
			return "Tempo indeterminado";
		}
		long diferencaDias = (dataFim.getTime() - dataInicio.getTime());

		int ms = (int) (diferencaDias % 1000);
		diferencaDias /= 1000;
		int ss = (int) (diferencaDias % 60);
		diferencaDias /= 60;
		int mins = (int) (diferencaDias % 60);
		diferencaDias /= 60;
		int horas = (int) (diferencaDias % 24);
		diferencaDias /= 24;
		int dias = (int) diferencaDias;
		StringBuilder ret = new StringBuilder();
		ret.append(StringUtil.completaZeros(String.valueOf(dias), 4)).append("d ");
		ret.append(StringUtil.completaZeros(String.valueOf(horas), 2)).append("h ");
		ret.append(StringUtil.completaZeros(String.valueOf(mins), 2)).append("m ");
		ret.append(StringUtil.completaZeros(String.valueOf(ss), 2)).append("s ");
		if(incluirMilissegundos){
			ret.append(StringUtil.completaZeros(String.valueOf(ms), 3)).append("ms ");
		}
		return ret.toString();
	}

	public static Date dataMenosDias(Date data, int nroDias){
		if (data == null){
			data = new Date();
		}
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(data);
		gc.add(Calendar.DAY_OF_YEAR, -nroDias);
		return gc.getTime();
	}

    /**
     * Foi necessario na issue [PJEII-4329]
     * @param data
     * @param nroDias
     * @return 
     */
	public static Date dataMaisDias(Date data, int nroDias){
		if (data == null){
			data = new Date();
		}
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(data);
		gc.add(Calendar.DAY_OF_YEAR, nroDias);
		return gc.getTime();
	}

	/**
	 * Metodo que recebe uma data e retorna essa data com as horas modificadas para '23:59:59'
	 * 
	 * @param date
	 * @return
	 */
	public static Date getEndOfDay(Date date){
		if (date == null){
			return null;
		}
		Calendar dt = new GregorianCalendar();
		dt.setTime(date);
		dt.set(Calendar.HOUR_OF_DAY, 23);
		dt.set(Calendar.MINUTE, 59);
		dt.set(Calendar.SECOND, 59);
		dt.set(Calendar.MILLISECOND, 999);
		return dt.getTime();
	}

	/**
	 * Metodo que recebe uma data e retorna essa data com as horas modificadas para '00:00:00'
	 * 
	 * @param date
	 * @return
	 */
	public static Date getBeginningOfDay(Date date){
		if (date == null){
			return null;
		}
		return getBeginningOfDayCalendar(date).getTime();
	}

	public static Calendar getBeginningOfDayCalendar(Date date){
		if (date == null){
			return null;
		}
		Calendar dt = new GregorianCalendar();
		dt.setTime(date);
		dt.set(Calendar.HOUR_OF_DAY, 0);
		dt.set(Calendar.MINUTE, 0);
		dt.set(Calendar.SECOND, 0);
		dt.set(Calendar.MILLISECOND, 0);
		return dt;
	}

	/**
	 * @return A data atual com as horas modificadas para '00:00:00'
	 */
	public static Date getBeginningOfToday(){
		return getBeginningOfDay(new Date());
	}
	
	public static Date getEndOfToday() {
		return getEndOfDay(new Date());
	}

	public static Calendar getBeginningOfTodayCalendar(){
		return getBeginningOfDayCalendar(new Date());
	}

	/**
	 * Retorna a data atual no formato informado.
	 * 
	 * @param formato - Formato que deseja receber a data.
	 * @return Data atual.
	 */
	public String getDataAtual(String formato){
		SimpleDateFormat fm = new SimpleDateFormat(formato);
		String data = null;
		try{
			data = fm.format(new Date());
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
		return data;
	}

	public static String dateToString(Date date, String format){
		if (date == null || format == null || format.trim().length() == 0){
			return null;
		}
		SimpleDateFormat fm = new SimpleDateFormat(format);
		try{
			return fm.format(date);
		} catch (Exception e){
			return null;
		}
	}

	public static String dateToString(Date date){
		return dateToString(date, "dd/MM/yyyy");
	}
	
	/**
	 * @param date
	 * @return Data no formato yyyy-MM-dd
	 */
	public static String dateToStringUSA(Date date){
		return dateToString(date, "yyyy-MM-dd");
	}
	
	public static String dateHourToString(Date date){
		return dateToString(date, "dd/MM/yyyy HH:mm");
	}
	
	public static String dateToHour(Date date){
		return dateToString(date, "HH:mm");
	}

	public static String dateToHourISO8601(Date date){
		return dateToString(date, "HH:mm'-'03:00");
	}

	public static Date stringToDate(String dateStr, String format){
		if (dateStr == null || dateStr.trim().length() == 0 || format == null
			|| format.trim().length() == 0){
			return null;
		}
		SimpleDateFormat fm = new SimpleDateFormat(format);
		try{
			return fm.parse(dateStr);
		} catch (Exception e){
			return null;
		}
	}

	/**
	 * Testa se a data informada está entre a data inicio e a data fim
	 * 
	 * @param data - Data que deseja testar se está no intervalo
	 * @param dataInicio - Data inicio do intervalo
	 * @param dataFim - Data fim do intervalo
	 * @return Verdadeiro se a data estiver no intervalo / Falso se a data não estiver no intervalo
	 */
	public static Boolean isBetweenDates(Date data, Date dataInicio,
			Date dataFim){
		return (data.equals(dataInicio)
			|| (data.after(dataInicio) && data.before(dataFim)) || data
				.equals(dataFim));
	}

	/**
	 * Testa se a hora informada está entre a hora inicio e a hora fim
	 * 
	 * @param hora - Hora que deseja testar se está no intervalo
	 * @param horaInicio - Hora inicio do intervalo
	 * @param horaFim - Hora fim do intervalo
	 * @return Verdadeiro se a hora estiver no intervalo / Falso se a hora não estiver no intervalo
	 */
	public static Boolean isBetweenHours(Date hora, Date horaInicio, Date horaFim){
		return (hora.equals(horaInicio)	|| (hora.after(horaInicio) && hora.before(horaFim)) || hora.equals(horaFim));
	}

	/**
	 * Valida se a hora final está depois da hora inicial
	 * 
	 * @param horaInicio
	 * @param horaFim
	 * @return
	 */
	public static boolean validateHour(Date horaInicio, Date horaFim){
		return horaInicio != null && horaFim != null
			&& horaFim.after(horaInicio);
	}

	/**
	 * Metodo onde retorna a data atual no formato informado
	 * 
	 * @param formato
	 * @return String da data.
	 */
	public static String getDataAtualFormatada(String formato){
		return getDataFormatada(new Date(), formato);
	}
	
	/**
	 * Metodo onde retorna a data no formato informado
	 * 
	 * @param data
	 * @param formato
	 * @return
	 */
	public static String getDataFormatada(Date data, String formato){
		if (data == null || formato.isEmpty()){
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(formato);
		return sdf.format(data);
	}

	public static Calendar dataProximoDiaSemana(Calendar data, int diaSemana){
		while (data.get(Calendar.DAY_OF_WEEK) != diaSemana){
			data.add(Calendar.DAY_OF_MONTH, 1);
		}
		return data;
	}

	public static Calendar dataProximoMes(Calendar data, int mes){
		int diferenca = (mes - data.get(Calendar.MONTH));
		if (diferenca < 0){
			diferenca += 12;
		}
		data.add(Calendar.MONTH, diferenca);
		return data;
	}
	
	public static boolean isFimDeSemana(Date data) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(data);
		return isFimDeSemana(calendar);
	}

	public static boolean isFimDeSemana(Calendar calendar){
		return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
			|| calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
	}

	public static String getMesExtenso(Integer mes){
		DateFormat dfmt = new SimpleDateFormat("MMM");
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, mes - 1);

		return dfmt.format(calendar.getTime());
	}

	public static XMLGregorianCalendar getXMLGregorianCalendarFromDate(Date date){
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(date);
		try{
			XMLGregorianCalendar xmlGrogerianCalendar = DatatypeFactory
					.newInstance().newXMLGregorianCalendar(gregorianCalendar);
			return xmlGrogerianCalendar;
		} catch (DatatypeConfigurationException e){
			throw new RuntimeException(e);
		}
	}

	public static Date getDateFromXMLGregorianCalendar(
			XMLGregorianCalendar xmlGregorianCalendar){
		return xmlGregorianCalendar.toGregorianCalendar().getTime();
	}

	public static boolean isDataMenor(Date data, Date when){
		return getDataSemHora(data).before(getDataSemHora(when));
	}

	public static boolean isDataMaior(Date data, Date when){
		return getDataSemHora(data).after(getDataSemHora(when));
	}

	public static boolean isDataMaiorIgual(Date data, Date when){
		return getDataSemHora(data).compareTo(getDataSemHora(when)) >= 0;
	}

	public static boolean isDataMenorIgual(Date data, Date when){
		return getDataSemHora(data).compareTo(getDataSemHora(when)) <= 0;
	}

	public static boolean isDataIgual(Date data, Date when){
		return getDataSemHora(data).compareTo(getDataSemHora(when)) == 0;
	}

	public static boolean isDataEntre(Date data, Date inferior, Date superior){
		return DateUtil.isDataMenorIgual(inferior, data)
			&& DateUtil.isDataMaiorIgual(superior, data);
	}

	public static boolean isIntersect(Date dataInicioA, Date dataFimA,
			Date dataInicioB, Date dataFimB){
		if (DateUtil.isDataEntre(dataInicioA, dataInicioB, dataFimB)
			|| DateUtil.isDataEntre(dataFimA, dataInicioB, dataFimB)
			|| DateUtil.isDataEntre(dataInicioB, dataInicioA, dataFimA)
			|| DateUtil.isDataEntre(dataFimB, dataInicioA, dataFimA)){
			return true;
		}
		return false;
	}

	public static Date getDataAtual(){
		return getDataSemHora(new Date());
	}

	public static int getAnoAtual(){
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		return cal.get(Calendar.YEAR);
	}

	public static Date getDataSemHora(Date data1){
		Calendar auxDt = Calendar.getInstance();
		auxDt.setTime(data1);
		auxDt.set(Calendar.HOUR_OF_DAY, 0);
		auxDt.set(Calendar.MINUTE, 0);
		auxDt.set(Calendar.SECOND, 0);
		auxDt.set(Calendar.MILLISECOND, 0);
		return auxDt.getTime();
	}
	
	public static Date getDataPassandoHoraMinuto(Date data, int horas, int minutos) {
		Calendar novaData = Calendar.getInstance();
		novaData.setTime(data);
		novaData.set(Calendar.HOUR_OF_DAY, horas);
		novaData.set(Calendar.MINUTE, minutos);
		novaData.set(Calendar.SECOND, 0);
		novaData.set(Calendar.MILLISECOND, 0);
		return novaData.getTime();
	}
	
	public static Date zerarSegundos(Date data) {
		if (data == null){
			return null;
		}
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(data);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	public static int obterHora(Date data) {
		Calendar auxDt = Calendar.getInstance();
		auxDt.setTime(data);
		
		return auxDt.get(Calendar.HOUR_OF_DAY);
	}

	public static int obterMinuto(Date data) {
		Calendar auxDt = Calendar.getInstance();
		auxDt.setTime(data);
		
		return auxDt.get(Calendar.MINUTE);
	}
	
	/**
	 * Método que retorna a idade através de uma data
	 * 
	 * @param data
	 * @return idade
	 */
	public static int getIdade(Date data){
		Calendar cData = Calendar.getInstance();
		Calendar cHoje = Calendar.getInstance();
		cData.setTime(data);
		cData.set(Calendar.YEAR, cHoje.get(Calendar.YEAR));
		int idade = cData.after(cHoje) ? -1 : 0;
		cData.setTime(data);
		idade += cHoje.get(Calendar.YEAR) - cData.get(Calendar.YEAR);
		return idade;
	}
	/**
	 * Método que retorna a o último de dia de um mês
	 * 
	 * @param int mes Intervalo 1-12
	 * @param int ano acima de 1800
	 * @return Date
	 */
	public static Date getUltimoDiaDeMesAno(int mes, int ano){
		if (mes<1 || mes>12) return null;
		if (ano<1800 ) return  null;
		Calendar gc = new GregorianCalendar();

		gc.set(Calendar.MONTH, mes-1);
		gc.set(Calendar.YEAR, ano);
		gc.set(Calendar.DAY_OF_MONTH,gc.getActualMaximum(Calendar.DAY_OF_MONTH));
		gc.set(Calendar.HOUR_OF_DAY,gc.getMaximum(Calendar.HOUR_OF_DAY));
		gc.set(Calendar.MINUTE,gc.getMaximum(Calendar.MINUTE));
		gc.set(Calendar.SECOND,gc.getMaximum(Calendar.SECOND));
		gc.set(Calendar.MILLISECOND,gc.getMaximum(Calendar.MILLISECOND));
		return gc.getTime();
	}
	/**
	 * Método que retorna a o último de dia de um mês
	 * 
	 * @param int mes Intervalo 1-12
	 * @param int ano acima de 1800
	 * @return Date
	 */
	public static Date getPrimeiroDiaDeMesAno(int mes, int ano){
		if (mes<1 || mes>12) return null;
		if (ano<1800 ) return  null;
		Calendar gc = new GregorianCalendar();

		gc.set(Calendar.MONTH, mes-1);
		gc.set(Calendar.YEAR, ano);
		gc.set(Calendar.DAY_OF_MONTH,gc.getActualMinimum(Calendar.DAY_OF_MONTH));
		gc.set(Calendar.HOUR_OF_DAY,gc.getMinimum(Calendar.HOUR_OF_DAY));
		gc.set(Calendar.MINUTE,gc.getMinimum(Calendar.MINUTE));
		gc.set(Calendar.SECOND,gc.getMinimum(Calendar.SECOND));
		gc.set(Calendar.MILLISECOND,gc.getMinimum(Calendar.MILLISECOND));

		return gc.getTime();
	}
	
	/**
	 * Retorna o intervalo, em minutos, entre duas datas
	 *  
	 * @param dataMaior
	 * @param dataMenor
	 * @return int Intervalo em minutos
	 */
	public static int getIntervaloEntreDatas(Date dataMaior, Date dataMenor){
		return (int) (Math.ceil(((double) dataMaior.getTime() - (double) dataMenor.getTime()) / 60000.0));
	}

	public static boolean isDataComHoraEntre(Date data, Date inferior,
			Date superior) {
		return DateUtil.isDataComHoraMenorIgual(inferior, data)
				&& DateUtil.isDataComHoraMaiorIgual(superior, data);

	}

	public static boolean isDataComHoraMenorIgual(Date data, Date when) {
		return data.compareTo(when) <= 0;
	}

	public static boolean isDataComHoraMaiorIgual(Date data, Date when) {
		return data.compareTo(when) >= 0;
	}

	public static boolean isDataComHoraMenor(Date data, Date when) {
		return data.before(when);
	}
	
	/**
	 * Verifica se a data1 e posterior a data2
	 * @param data1
	 * @param data2
	 * @return verdadeiro se data1 for posterior a data2.
	 */
	public static boolean isDataPosterior(Date data1, Date data2) {
		return data1.after(data2);
	}

	public static boolean isDataComHoraEntre(Calendar data, Calendar inferior, 	Calendar superior) {
		return DateUtil.isDataComHoraMenorIgual(inferior.getTime(), data.getTime()) && 
				DateUtil.isDataComHoraMaiorIgual(superior.getTime(), data.getTime());
	}

	public static Date adicionarTempoData(Date data, int atributo, int tempo){
		Calendar c = Calendar.getInstance();
		c.setTime(data);
		c.add(atributo, tempo);
		return c.getTime();
	}
	
	/**
	 * Metodo para calcular datas a partir da data atual, somando ou subtraindo os dias, meses ou anos passados nos parametros, e o horario pretendido
	 * Para subtrair os dias, meses ou anos basta somente passar o inteiro negativo como parametro 
	 * @param daysToAdd - null para nao adicionar nenhum dia, (X) para adicionar, (-X) para subtrair
	 * @param monthsToAdd - null para nao adicionar nenhum mes, (X) para adicionar, (-X) para subtrair
	 * @param yearsToAdd - null para nao adicionar nenhum ano, (X) para adicionar, (-X) para subtrair
	 * @param hora - 0 a 23
	 * @param minuto - 0 a 59
	 * @return data calculada a partir da data atual com os parametros passados
	 */
	public static Date defineData(Integer daysToAdd, Integer monthsToAdd, Integer yearsToAdd, int hora, int minuto) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		if(daysToAdd != null) {
			c.add(Calendar.DATE, daysToAdd);
		}
		if(monthsToAdd != null) {
			c.add(Calendar.MONTH, monthsToAdd);
		}
		if(yearsToAdd != null) {
			c.add(Calendar.YEAR, yearsToAdd);
		}
		if(hora >= 0 && hora <= 23) {
			c.add(Calendar.HOUR_OF_DAY, hora);
		}
		if(minuto >= 0 && minuto <= 59) {
			c.add(Calendar.MINUTE, minuto);
		}
		return c.getTime();
	}

	/**
	 * Metodo para obter a data com formato dd/MM/yyyy_HHmmssSSS. Esse formato indicara nao somente o dia/mes/ano, mas
	 * tambem contara com um underline "_" entre a data e a hora, que contera horaMinutoSegundoMilissegundo.
	 * 
	 * @return	String com os dados atuais da data/hora no padrao dd/MM/yyyy_HHmmssSSS.
	 */
	public String obterDataHoraComMilissegundo(){	
		return getDataAtual(FORMATO_DATA_HORA_MINUTO_SEGUNDO_MILI);
	}
	
	/**
	 * Metodo para verificar se a data esta vigente com relacao a data atual.
	 * @param data	eh a data a ser comparada com a data atual
	 * @param dataAtual	eh a data atual
	 * @return	verdadeiro se a data for nula ou for posterior a data atual.
	 */
	public static boolean isDataVigente (Date data, Date dataAtual){
		return data == null || DateUtil.isDataPosterior(data, dataAtual);
	}

	public static Date addMilisegundos(Date data, int milisegundos) {

		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(data);
		calendar.add(Calendar.MILLISECOND, milisegundos);
		
		return calendar.getTime();
	}
	
	/**
	 * Retorna novo Calendar do Date passado por parâmetro.
	 * 
	 * @param data
	 * @return Calendar.
	 */
	public static Calendar novoCalendar(Date data) {
		Calendar calendar = null;
		if (data != null) {
			calendar = Calendar.getInstance();
			calendar.setTime(data);
		}
		
		return calendar;
	}
	
	/**
	 * Método responsável por validar se o tempo informado está no formato HH:mm (00:00 até 23:59)
	 * 
	 * @param tempo Tempo representado no formato String.
	 * @return Verdadeiro caso o tempo informado está no formato HH:mm (00:00 até 23:59). Falso, caso contrário.
	 */
	public static boolean validarRepresentacaoTempo(String tempo) {
		Pattern formatoHora = Pattern.compile("\\d\\d:\\d\\d");
		Matcher matcher = formatoHora.matcher(tempo);
		if (!matcher.matches()) {
			return false;
		}
		
		int hora = Integer.parseInt(tempo.substring(0, 2));
		int minuto = Integer.parseInt(tempo.substring(3, 5));
		return (hora >= 0 && hora <= 23) && (minuto >= 0 && minuto <= 59);
	}
	
	public static int convertToMinutes(Date horaInicial, Date horaFinal) {
		long diferenca = horaFinal.getTime() - horaInicial.getTime();
		return (int) ((diferenca / 1000) / HORAEMMINUTOS);
	}
	
	public static int convertToMinutes(Date tempo) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(tempo);
		return ((cal.get(Calendar.HOUR) * HORAEMMINUTOS) + cal.get(Calendar.MINUTE));
	}
	
	/**
	 * Recebe uma data com zona em string e transforma em uma string no padrão brasileiro
	 *  
	 * @param data
	 * @return string
	 */
	public static String stringZoneToDateString(Date data) {
		SimpleDateFormat f = new SimpleDateFormat(FORMATO_DATA);
		return f.format(data);
	}
	
	public static int converteDataToYYYYMMDD(Calendar calendar) {
		// Internamente o SimpleDateFormat faz algo parecido com esse método,
		// porem faz a traducao dos campos de yyyy para ano, por exemplo. 
		if(calendar != null) {
			int i = calendar.get(Calendar.YEAR) * 10000;
			i += (calendar.get(Calendar.MONTH) + 1) * 100;
			i += calendar.get(Calendar.DAY_OF_MONTH);
			return i;
		}
		return 0;
	}


	public static boolean isIntersectWithHour(Date dataInicioA, Date dataFimA, Date dataInicioB, Date dataFimB){
		if (DateUtil.isDataComHoraEntre(dataInicioA, dataInicioB, dataFimB)
			|| DateUtil.isDataComHoraEntre(dataFimA, dataInicioB, dataFimB)
			|| DateUtil.isDataComHoraEntre(dataInicioB, dataInicioA, dataFimA)
			|| DateUtil.isDataComHoraEntre(dataFimB, dataInicioA, dataFimA)){
			return true;
		}
		return false;
	}

	public static boolean isDataComHoraMaior(Date data, Date when) {
	    return data.compareTo(when) > 0;
	}

	public static Calendar getCalendarFromDate(Date data){
		Calendar c = Calendar.getInstance();
		c.setTime(data);
		return c;
	}


	
	public static String horaPorExtenso(Date hora, boolean comSegundos) {
		String retorno = "";
		if(hora!= null) {
			Calendar horaCal = novoCalendar(hora);
			@SuppressWarnings("static-access")
			int h = horaCal.get(Calendar.HOUR) + ((horaCal.PM != 1) ? 0 : 12);
			int m = horaCal.get(Calendar.MINUTE);
			int s = horaCal.get(Calendar.SECOND);

			retorno = tempoPorExtenso(h, Calendar.HOUR);
			if ((m != 0) && (s != 0)) {
				// tem minutos e segundos
				if(!comSegundos) {
					retorno = retorno + " e ";
				} else {
					retorno = retorno + ", "; 
				}
				retorno = retorno + tempoPorExtenso(m, Calendar.MINUTE);
				if(comSegundos) {
					retorno = retorno + " e " + tempoPorExtenso(s, Calendar.SECOND);
				}
			} else {
				if (m != 0) {
					// so tem minutos (segundos = zero)
					retorno = retorno + " e " + tempoPorExtenso(m, Calendar.MINUTE);
				} else {
					if (comSegundos && s != 0) {
						// so tem segundos (minutos = zero)
						retorno = retorno + " e " + tempoPorExtenso(s, Calendar.SECOND);
					}
				}
			}
		}
		return retorno;
	}


	public static String tempoPorExtenso(int tempo, int tipo) {
		String parte[] = {"zero", "um", "dois", "três",
				"quatro", "cinco", "seis", "sete", "oito", "nove",
				"dez", "onze", "doze", "treze", "quatorze", "quinze",
				"dezesseis", "dezessete", "dezoito", "dezenove"};

		String dezena[] = {"", "", "vinte", "trinta", "quarenta", "cinquenta"};
		String s;
		if (tempo <= 19) {
			if ((tipo == Calendar.HOUR) && (tempo == 1)) {
				// uma hora da madrugada
				s = "uma";
			} else {
				if ((tipo == Calendar.HOUR) && (tempo == 2)) { 
					// duas horas da madrugada
					s = "duas";
				} else {
					s = parte[tempo];
				}
			}
		} else {
			int dez = tempo / 10;
			int unid = tempo % 10;
			s = dezena[dez];
			if (unid != 0) {
				if ((tipo == Calendar.HOUR) && (unid == 1)) {
					// vinte e uma horas
					s = s + " e uma";
				} else {
					if ((tipo == Calendar.HOUR) && (unid == 2)) { 
						// vinte e duas horas
						s = s + " e duas";
					} else {
						s = s + " e " + parte[unid];
					}
				}
			}
		}
		if (tipo == Calendar.HOUR) {
			s = s + " hora";
		} else {
			if (tipo == Calendar.MINUTE) {
				s = s + " minuto";
			} else {
				s = s + " segundo";
			}
		}
		if (tempo > 1) { 
			// plural
			s = s + "s";
		}
		return(s); 
	}
	
	
	/**
	 * Verifica apenas o horário entre duas datas, igualando dia, mês, ano e milisegundos. 
	 */
	public static boolean isMesmoHorario(final Date data1, final Date data2) {
		Calendar cloneData1 = Calendar.getInstance();
		cloneData1.setTime(data1);
		
		Calendar cloneData2 = Calendar.getInstance();
		cloneData2.setTime(data2);
		
		//Iguala o dia, mês e ano.
		cloneData1.set(Calendar.DATE, cloneData2.get(Calendar.DATE));
		cloneData1.set(Calendar.MONTH, cloneData2.get(Calendar.MONTH));
		cloneData1.set(Calendar.YEAR, cloneData2.get(Calendar.YEAR));
		cloneData1.set(Calendar.MILLISECOND, 0);
		cloneData2.set(Calendar.MILLISECOND, 0);
		
		//Compara o horário
		return cloneData1.getTime().compareTo(cloneData2.getTime()) == 0;
	}
	
	/**
	 * Metodo onde retorna a data no formato ISO 8601 (Ex: 01/01/2023T15:30:00Z).
	 * 
	 * @param data
	 * @return Data formatada.
	 */
	public static String formatarDataParaISO8601(Date data){
		return getDataFormatada(data, FORMATO_DATA_ISO8601);
	}
	
}
