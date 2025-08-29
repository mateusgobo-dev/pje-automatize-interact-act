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
package br.jus.pje.je.enums;

import br.jus.pje.nucleo.enums.PJeEnum;

/**
 * Enumeração dos tipos de documentos ordinariamente utilizados em instâncias
 * colegiadas da Justiça Eleitoral.
 * 
 * @author TSE
 *
 */
public enum TipoDocumentoColegiadoEnum implements PJeEnum {

	/**
	 * Tipo de documento relatório.
	 */
	REL("Relatório"), 
	
	/**
	 * Tipo de documento voto 
	 */
	VOT("Voto"), 
	
	/**
	 * Tipo de documento voto vencedor, a ser utilizado apenas
	 * quando o voto do relator não é o condutor do acórdão. 
	 */
	VOT_VENC("Voto vencedor"), 
	
	/**
	 * Tipo de documento ementa. 
	 */
	EME("Ementa"), 
	
	/**
	 * Tipo de documento indicativo de notas oralmente proferidas
	 * pelos magistrados durante o julgamento.
	 */
	NOT_ORA("Notas orais"),
	
	/**
	 * Tipo de documento acórdão, que é um resumo da conclusão a que
	 * se chegou no julgamento. 
	 */
	ACO("Acórdão");

	private String label;

	private TipoDocumentoColegiadoEnum(String label) {
		this.label = label;
	}

	/* (non-Javadoc)
	 * @see br.jus.pje.nucleo.enums.PJeEnum#getLabel()
	 */
	public String getLabel() {
		return this.label;
	}

}
