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
package br.jus.pje.jt.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe responsavel para manipulacao de datas.
 * 
 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
 * 
 * @category PJE-JT
 * @since 1.2.0
 * @created 05/08/2011
 */
public class JTDateUtil implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Recupera uma Instancia da classe Date com a Data Atual sem Horas, Minutos
	 * e Segundos.
	 * 
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @return Date
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 05/08/2011
	 */
	public static Date getDate() {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		return now.getTime();
	}

	/**
	 * Retorna true se a data for igual a data de comparacao. Desconsidera:
	 * Horas, minutos e segundos.
	 * 
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @param date
	 * @param comparable
	 * @return true se for anterior.
	 * @throws NullPointerException
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 05/08/2011
	 */
	public static boolean equals(Date date, Date comparable) throws NullPointerException {

		checkNullPointerException(date, comparable);

		return beforeOrAfter(date, comparable) == 0;
	}

	/**
	 * Retorna true se a data for anterior a comparacao. Desconsidera: Horas,
	 * minutos e segundos.
	 * 
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @param date
	 * @param comparable
	 * @return true se for anterior.
	 * @throws NullPointerException
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 05/08/2011
	 */
	public static boolean before(Date date, Date comparable) throws NullPointerException {

		checkNullPointerException(date, comparable);

		return beforeOrAfter(date, comparable) < 0;
	}

	/**
	 * Retorna true se a data for anterior ou igual a comparacao. Desconsidera:
	 * Horas, minutos e segundos.
	 * 
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @param date
	 * @param comparable
	 * @return true se for anterior ou igual.
	 * @throws NullPointerException
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 05/08/2011
	 */
	public static boolean beforeOrEquals(Date date, Date comparable) throws NullPointerException {

		checkNullPointerException(date, comparable);

		return equals(date, comparable) || before(date, comparable);
	}

	/**
	 * Retorna true se a data for posterior a data comparacao. Desconsidera:
	 * Horas, minutos e segundos.
	 * 
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @param date
	 * @param comparable
	 * @return true se for posterior.
	 * @throws NullPointerException
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 05/08/2011
	 */
	public static boolean after(Date date, Date comparable) throws NullPointerException {

		checkNullPointerException(date, comparable);

		return beforeOrAfter(date, comparable) > 0;
	}

	/**
	 * Retorna true se a data for posterior ou igual a comparacao. Desconsidera:
	 * Horas, minutos e segundos.
	 * 
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @param date
	 * @param comparable
	 * @return true se for posterior ou igual.
	 * @throws NullPointerException
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 05/08/2011
	 */
	public static boolean afterOrEquals(Date date, Date comparable) throws NullPointerException {

		checkNullPointerException(date, comparable);

		return equals(date, comparable) || after(date, comparable);
	}

	/**
	 * Retorna true se a data estiver dentro de um intervalo. Desconsidera:
	 * Horas, Minutos e Segundos.
	 * 
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @param date
	 * @param beginDate
	 * @param endDate
	 * @param excludeLimits
	 * @return true se a data estiver entre os limites.
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 19/08/2011
	 */
	public static boolean between(Date date, Date beginDate, Date endDate, boolean excludeLimits) {

		checkNullPointerException(date, beginDate);
		checkNullPointerException(date, endDate);

		if (excludeLimits) {
			return after(date, beginDate) && before(date, endDate);
		} else {
			return afterOrEquals(date, beginDate) && beforeOrEquals(date, endDate);
		}
	}

	/**
	 * Rretorna -1 para anterior, 0 para igual e 1 para posterior.
	 * Desconsiderando: Horas, minutos e segundos.
	 * 
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @param date
	 * @param comparable
	 * @return -1 anterior, 0 igual, 1 posterior.
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 05/08/2011
	 */
	private static int beforeOrAfter(Date date, Date comparable) {

		String dateString = new SimpleDateFormat("yyyyMMdd").format(date);
		String comparableString = new SimpleDateFormat("yyyyMMdd").format(comparable);

		Integer dateInteger = new Integer(dateString);
		Integer comparableInteger = new Integer(comparableString);

		return dateInteger.compareTo(comparableInteger);
	}

	/**
	 * Verifica se as datas estao nulas e lanca a exception
	 * NullPointerException.
	 * 
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @param date
	 * @param comparable
	 * @throws NullPointerException
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 05/08/2011
	 */
	private static void checkNullPointerException(Date date, Date comparable) throws NullPointerException {
		if (comparable == null) {
			throw new NullPointerException("Date comparable is null");
		}

		if (date == null) {
			throw new NullPointerException("Date date is null");
		}

	}

	/**
	 * Método que valida se um horário em string está no formato HH:MM Retorna
	 * true se o string está no formato
	 * 
	 * @author José Borges [jose.borges@tst.jus.br] / Kelly
	 * @category PJE-JT
	 */
	public static boolean horarioValidoHHMM(String hhmm) {
		Pattern pattern;
		Matcher matcher;

		String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
		pattern = Pattern.compile(TIME24HOURS_PATTERN);
		matcher = pattern.matcher(hhmm);

		return matcher.matches();
	}

}
