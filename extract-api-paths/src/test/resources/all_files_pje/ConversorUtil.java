/**
 * UtilConversor.java
 * 
 * Data de criação: 20/09/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import br.com.infox.cliente.util.ProjetoUtil;
import br.jus.cnj.intercomunicacao.v222.beans.Data;
import br.jus.cnj.intercomunicacao.v222.beans.DataHora;
import br.jus.cnj.intercomunicacao.v222.beans.NumeroUnico;
import br.jus.cnj.intercomunicacao.v222.beans.Parametro;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.pje.nucleo.util.DateUtil;

/**
 * Conversores gerais usados na intercomunicações.
 * 
 * @author Adriano Pamplona
 */
public final class ConversorUtil {

	/**
	 * Converte um objeto do tipo DataHora para Date.
	 * 
	 * @param dataHora DataHora
	 * @return objeto do tipo Date.
	 */
	public static Date converterParaDate(DataHora dataHora) {
		Date resultado = null;
		
		if (dataHora != null && StringUtils.isNotBlank(dataHora.getValue())) {
			String pattern = MNIParametro.PARAM_FORMATO_DATA_HORA;
			resultado = DateUtil.stringToDate(dataHora.getValue(), pattern);
		}
		return resultado;
	}
	
	/**
	 * Converte um objeto do tipo DataHora para Date.
	 * 
	 * @param dataHora DataHora
	 * @param seNuloDataAtual boleano que indica se dataHora for nula então será 
	 * 			retornado a data atual.
	 * @return objeto do tipo Date.
	 */
	public static Date converterParaDate(DataHora dataHora, boolean seNuloDataAtual) {
		Date resultado = null;
		
		if (dataHora != null && StringUtils.isNotBlank(dataHora.getValue())) {
			resultado = converterParaDate(dataHora);
		} else if (seNuloDataAtual) {
			resultado = new Date();
		}
		return resultado;
	}
	
	/**
	 * Converte um objeto do tipo Date para Data.
	 * 
	 * @param date Date
	 * @return objeto do tipo Data.
	 */
	public static Data converterParaData(Date date) {
		Data resultado = null;
		
		if (date != null) {
			String pattern = MNIParametro.PARAM_FORMATO_DATA;
			resultado = new Data();
			resultado.setValue(DateUtil.dateToString(date, pattern));
		}
		return resultado;
	}
	
	/**
	 * Converte um objeto do tipo Date para DataHora.
	 * 
	 * @param date Date
	 * @return objeto do tipo DataHora.
	 */
	public static DataHora converterParaDataHora(Date date) {
		DataHora resultado = null;
		
		if (date != null) {
			String pattern = MNIParametro.PARAM_FORMATO_DATA_HORA;
			resultado = new DataHora();
			resultado.setValue(DateUtil.dateToString(date, pattern));
		}
		return resultado;
	}
	
	/**
	 * Converter Lista de Parâmetro para Lista de Properties.
	 * 
	 * @param parametros List<Parametro> 
	 * @return List<Properties>
	 */
	public static List<Properties> converterParaProperties(List<Parametro> parametros) {
		List<Properties> resultado = new ArrayList<>();
		
		if (ProjetoUtil.isNotVazio(parametros)) {
			for (Parametro parametro : parametros) {
				Properties properties = new Properties();
				properties.setProperty(parametro.getNome(), parametro.getValor());
				
				resultado.add(properties);
			}
		}
		return resultado;
	}

	/**
	 * Converte a string para NumeroUnico.
	 * 
	 * @param numeroProcesso
	 * @return NumeroUnico com número do processo.
	 */
	public static NumeroUnico converterParaNumeroUnico(String numeroProcesso) {
		NumeroUnico resultado = null;
		
		if (StringUtils.isNotBlank(numeroProcesso)) {
			resultado = new NumeroUnico();
			resultado.setValue(numeroProcesso);
		}
		return resultado;
	}
}
