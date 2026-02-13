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
package br.jus.pje.nucleo.search.bridge;

import java.util.Arrays;
import java.util.List;

import org.hibernate.search.bridge.StringBridge;

/**
 * Remove caracteres especiais de números de documentos
 * 
 * @author thiago.vieira
 * 
 */
public class NumeroDocumentoBridge implements StringBridge {

	public final static String[] DOCUMENTO_STOP_WORDS = { "/", ".", "-" };

	@Override
	public String objectToString(Object object) {
		List<String> lista = Arrays.asList(DOCUMENTO_STOP_WORDS);
		String texto = (object).toString();
		for (String t : lista) {
			texto = texto.replace(t, "");
		}
		return texto;
	}
}