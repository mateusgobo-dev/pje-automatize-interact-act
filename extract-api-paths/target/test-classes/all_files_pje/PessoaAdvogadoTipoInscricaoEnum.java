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
package br.jus.pje.nucleo.enums;

import org.apache.commons.lang.StringUtils;


public enum PessoaAdvogadoTipoInscricaoEnum implements PJeEnum {

	A("Advogado"), E("Estagiario"), S("Suplementar");

	private String label;

	PessoaAdvogadoTipoInscricaoEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}
	
	/*
	 * [PJEII-3295] PJE-JT: Sérgio Ricardo : PJE-1.4.5 
	 * Método para pesquisa de um dado Tipo de inscrição 
	 */			
	public static boolean contains(String s) {
		for(PessoaAdvogadoTipoInscricaoEnum pati:values())
			if (pati.name().equals(s)) 
				return true;
		return false;
	}	

	/**
	 * Retorna o PessoaAdvogadoTipoInscricaoEnum pelo valor passado por parâmetro.
	 * 
	 * @param string Valor do enum ou label.
	 * @return PessoaAdvogadoTipoInscricaoEnum
	 */
	public static PessoaAdvogadoTipoInscricaoEnum obter(String string) {
		PessoaAdvogadoTipoInscricaoEnum resultado = null;
		PessoaAdvogadoTipoInscricaoEnum[] values = values();
		string = StringUtils.trim(string);
		
		for (int indice = 0; indice < values.length && resultado == null; indice++) {
			PessoaAdvogadoTipoInscricaoEnum valor = values[indice];
			
			if (valor.name().equalsIgnoreCase(string) || 
				valor.getLabel().equalsIgnoreCase(string)) {
				resultado = values[indice];
			}
		}
		
		return resultado;
	}	

	/**
	 * Retorna o PessoaAdvogadoTipoInscricaoEnum pelo valor passado por parâmetro, caso o retorno seja
	 * nulo será retornado o padrão informado.
	 * 
	 * @param string Valor do enum ou label.
	 * @param padrao PessoaAdvogadoTipoInscricaoEnum.
	 * @return PessoaAdvogadoTipoInscricaoEnum
	 */
	public static PessoaAdvogadoTipoInscricaoEnum obter(String string, PessoaAdvogadoTipoInscricaoEnum padrao) {
		PessoaAdvogadoTipoInscricaoEnum resultado = obter(string);
		
		if (resultado == null) {
			resultado = padrao;
		}
		return resultado;
	}	

	/**
	 * Retorna true se o tipo for Estagiário.
	 * 
	 * @param tipo
	 * @return Booleano.
	 */
	public static Boolean isEstagiario(String tipo) {
		return PessoaAdvogadoTipoInscricaoEnum.E == obter(tipo);
	}
	
	/**
	 * Retorna true se o tipo for Advogado.
	 * 
	 * @param tipo
	 * @return Booleano.
	 */
	public static Boolean isAdvogado(String tipo) {
		return PessoaAdvogadoTipoInscricaoEnum.A == obter(tipo);
	}
	
	/**
	 * Retorna true se o tipo for Suplementar.
	 * 
	 * @param tipo
	 * @return Booleano.
	 */
	public static Boolean isSuplementar(String tipo) {
		return PessoaAdvogadoTipoInscricaoEnum.S == obter(tipo);
	}
}
