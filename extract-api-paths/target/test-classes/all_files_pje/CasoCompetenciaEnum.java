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



/**
 * Enum responsável por identificar os casos possíveis para o conflito de competência.
 * 
 * <ul>
 * 	<li>
 * 		Caso 1: Quando as competências possíveis são diferentes da que está no processo.
 *	</li>
 * 	<li>
 * 		Caso 2: Quando dentro das competências possíveis está a competência que está no processo.
 * 	</li>
 * 	<li>
 * 		Caso 3: Quando existe apenas uma competência possível e é diferente da que está no processo. 
 *	</li>
 * 	<li>
 * 		Caso 4: Quando a competência possível e a que está no processo. 
 *	</li>
 *	<li>
 *		Caso 5: Quando não há competência possível e o processo tem competência.
 * </li>
 *	<li>
 *		Caso 6: Quando não há competência possível e o processo não tem competência.
 * </li>
 * </ul>
 */
public enum CasoCompetenciaEnum implements PJeEnum {

	/**
	 * Quando as competências possíveis são diferentes da que está no processo
	 */
	CASO1("Caso 1"),
	/**
	 * Quando dentro das competências possíveis está a competência que está no processo
	 */
	CASO2("Caso 2"),
	/**
	 * Quando existe apenas uma competência possível e é diferente da que está no processo
	 */
	CASO3("Caso 3"),
	/**
	 * Quando a competência possível é a que está no processo
	 */
	CASO4("Caso 4"),
	/**
	 * Quando não há competência possível e o processo tem competência
	 */
	CASO5("Caso 5"),
	/**
	 * Quando não há competência possível e o processo não tem competência
	 */
	CASO6("Caso 6");

	private String label;
	
	CasoCompetenciaEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}
	
}
