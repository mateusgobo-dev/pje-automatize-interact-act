package br.jus.cnj.pje.util;

import org.apache.commons.lang.StringEscapeUtils;

public class QueryUtils {
	
	private QueryUtils() {
	}
	
	public static String toParameterOfNativeQuery(Object objeto) {
		String retorno = "";
		if(objeto == null) {
			retorno = "null";
		} else if(objeto instanceof String) {
			retorno = "'"+StringEscapeUtils.escapeSql((String) objeto)+"'";
		} else {
			retorno = StringEscapeUtils.escapeSql(objeto.toString());
		}
		return retorno;
	}
}
