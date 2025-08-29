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

import org.apache.commons.lang.StringUtils;

/**
 * Classe utilitária para tratar o número da OAB.
 * O número possui o formato AA000000.
 * AA = Unidade Federativa 
 * 000000 = Número
 * 
 * @author adriano.pamplona
 */
public final class OABUtil {
	
	/**
	 * Construtor.
	 */
	private OABUtil() {
		//Construtor.
	}
	
	/**
	 * @param oab
	 * @return UF da OAB.
	 */
	public static String extrairUF(String oab) {
		String resultado = null;
		
		if (StringUtils.length(oab) > 2) {
			String uf = StringUtils.left(oab, 2);
			if (StringUtils.isAlpha(uf)) { 
				resultado = uf;
			}
		}
		return resultado;
	}
	
	/**
	 * @param oab
	 * @return Inscrição da OAB.
	 */
	public static String extrairInscricao(String oab) {
		return StringUtil.removeNaoNumericos(oab);
	}
}
