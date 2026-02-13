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

import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * @author cristof
 *
 */
public class NumeracaoUnicaUtil {
	/**
	 * Formata o numero do processo para exibição
	 * 
	 * @param numeroSequencia = número seqüencial do processo no ano (NNNNNNN)
	 * @param numeroDigitoVerificador = Número identificador (DD)
	 * @param ano = ano (AAAA)
	 * @param numeroOrgaoJustica = identificação do órgão da justiça (JTR)
	 * @param numeroOrigem = origem do processo (OOOO)
	 * @return Numero do processo formatado (NNNNNNNDD.AAAA.J.TR.OOOO)
	 */
	public static String formatNumeroProcesso(Integer numeroSequencia, Integer numeroDigitoVerificador, Integer ano,
			Long numeroOrgaoJustica, Integer numeroOrigem){
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.completaZeros(numeroSequencia, 7));
		sb.append('-').append(StringUtil.completaZeros(numeroDigitoVerificador, 2));
		sb.append('.').append(StringUtil.completaZeros(ano, 4));

		String nOrgJus = StringUtil.completaZeros(numeroOrgaoJustica.intValue(), 3);
		sb.append('.').append(nOrgJus.substring(0, 1));
		sb.append('.').append(nOrgJus.substring(1));

		sb.append('.').append(StringUtil.completaZeros(numeroOrigem, 4));
		return sb.toString();
	}

	public static String formatNumeroProcesso(Integer numeroSequencia, Integer numeroDigitoVerificador, Integer ano,
			Integer numeroOrgaoJustica, Integer numeroOrigem){
		return formatNumeroProcesso(numeroSequencia, numeroDigitoVerificador, ano, numeroOrgaoJustica.longValue(),
				numeroOrigem);
	}
	
	public static String formatNumeroProcesso(ProcessoTrf processo){
		return formatNumeroProcesso(processo.getNumeroSequencia(), processo.getNumeroDigitoVerificador(), processo.getAno(), processo.getNumeroOrgaoJustica(), processo.getNumeroOrigem());
	}
	
	/**
	 * Formata string de 20 caracteres com a máscara NNNNNNN-DD.AAAA.J.TR.OOOO
	 * 
	 * @param numeroProcesso = número do processo sem formatação
	 * @return Numero do processo formatado (NNNNNNN-DD.AAAA.J.TR.OOOO)
	 */
	public static String formatNumeroProcesso(String numeroProcesso){
	   String numeroFormatado = "";
	   
	   if(numeroProcesso != null && numeroProcesso.length() == 20){
		   String numeroSequencia = numeroProcesso.substring(0,7);
		   String numeroDigitoVerificador = numeroProcesso.substring(7, 9);
		   String ano = numeroProcesso.substring(9, 13);
		   String numeroOrgaoJustica = numeroProcesso.substring(13, 14)+"."+numeroProcesso.substring(14, 16);
		   String numeroOrigem = numeroProcesso.substring(16, 20);
		   
		   numeroFormatado = numeroSequencia+"-"+numeroDigitoVerificador+"."+ano+"."+numeroOrgaoJustica+"."+numeroOrigem;   
	   }
	   return numeroFormatado;
	}
	
}
