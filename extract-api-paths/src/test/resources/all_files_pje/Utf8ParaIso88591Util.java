/**
 * Utf8ParaIso88591Util.java.
 *
 * Data de criação: 18/08/2014
 */
package br.jus.pje.nucleo.util;

import java.text.Normalizer;

/**
 * Classe responsável pela conversão de string no formato UTF8 para ISO-8859-1.
 * 
 * @author Adriano Pamplona
 */
public final class Utf8ParaIso88591Util {

	/**
	 * Converte uma string de UTF-8 para ISO-8859-1.
	 * Fonte: http://numberformat.wordpress.com/2013/02/09/convert-utf-8-unicode-to-ascii-latin-1/
	 * @param stringOriginal String que será convertida.
	 * @return String no formato ISO-8859-1.
	 */
	public static String converter(String stringOriginal) {
        String normalized_string;
        normalized_string = Normalizer.normalize(stringOriginal,
        		Normalizer.Form.NFC);

        normalized_string = normalized_string.replaceAll(
                "\\p{InCombiningDiacriticalMarks}+", "");

        String str = normalized_string;
        str = str
                .replaceAll(
                        "[\u00AB\u2034\u2037\u00BB\u02BA\u030B\u030E\u201C\u201D\u201E\u201F\u2033\u2036\u3003\u301D\u301E]",
                        "\"");
        str = str.replaceAll("[\u02CB\u0300\u2035]", "`");
        str = str.replaceAll("[\u02C4\u02C6\u0302\u2038\u2303]", "^");
        str = str.replaceAll("[\u02CD\u0331\u0332\u2017]", "_");
        str = str.replaceAll(
                "[\u00AD\u2010\u2011\u2012\u2013\u2014\u2212\u2015]",
                "-");
        str = str.replaceAll("[\u201A]", ",");
        str = str.replaceAll("[\u0589\u05C3\u2236]", ":");
        str = str.replaceAll("[\u01C3\u2762]", "!");
        str = str.replaceAll("[\u203D]", "?");
        str = str
                .replaceAll(
                        "[\u00B4\u02B9\u02BC\u02C8\u0301\u200B\u2018\u2019\u201B\u2032]",
                        "'");
        str = str.replaceAll("[\u27E6]", "[");
        str = str.replaceAll("[\u301B]", "]");
        str = str.replaceAll("[\u2983]", "{");
        str = str.replaceAll("[\u2984]", "}");
        str = str.replaceAll("[\u066D\u204E\u2217\u2731]", "*");
        str = str.replaceAll("[\u00F7\u0338\u2044\u2060\u2215]", "/");
        str = str.replaceAll("[\u20E5\u2216]", "\\");
        str = str.replaceAll("[\u266F]", "#");
        str = str.replaceAll("[\u066A\u2052]", "%");
        str = str.replaceAll("[\u2039\u2329\u27E8\u3008]", "<");
        str = str.replaceAll("[\u203A\u232A\u27E9\u3009]", ">");
        str = str.replaceAll("[\u01C0\u05C0\u2223\u2758]", "|");
        str = str.replaceAll("[\u02DC\u0303\u2053\u223C\u301C]", "~");
        normalized_string = str;
        return normalized_string;
    }
	
	
}
