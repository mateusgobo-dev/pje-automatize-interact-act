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
package br.jus.pje.mni.enums;


/**
 * Enumeração das situações possíveis para uma audiência quando da transferência de informação 
 * dentro do modelo nacional de interoperabilidade (MNI).
 * 
 * @author CNJ
 *
 */
public enum StatusAudienciaEnum {
	/**
	 * Indica que a audiência foi designada para uma determinada data.
	 */
	DESIGNADA,
	
	/**
	 * Indica que a audiência foi designada para uma determinada data, mas
	 * posteriormente cancelada. 
	 */
	CANCELADA, 
	
	/**
	 * Indica que a audiência foi designada para uma determinada data, mas
	 * posteriormente redesignada para data posterior à data original. 
	 */
	REDESIGNADA, 
	
	/**
	 * Indica que a audiência foi realizada na data previamente agendada.
	 */
	REALIZADA,
	
	/**
	 * Indica que a audiência não foi realizada na data previamente agendada.
	 */
	NAO_REALIZADA, 
	
	/**
	 * Indica que a audiência, depois de iniciada, foi suspensa para concretização
	 * de atividade não passível de realização na audiência.
	 */
	CONVERTIDA_EM_DILIGENCIA;
	
}
