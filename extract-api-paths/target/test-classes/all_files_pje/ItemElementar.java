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
package br.jus.pje.jt.entidades.estatistica;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

/**
 * @author Sérgio Pacheco / Sérgio Simoes
 * @since 1.4.3
 * @see ItemQuadro
 * @category PJE-JT
 * @class ItemElementar
 * @description Classe que representa a definicao de um item elementar 
 * 				(folha na estrutura em árvore de ItemQuadro). 
 */
@Entity
@DiscriminatorValue(value = "E")
public class ItemElementar extends ItemQuadro {
 
	private static final long serialVersionUID = 1L;
	
	private String statment;

	public ItemElementar() {
		super();
	}

	public ItemElementar(RegiaoQuadro regiaoQuadro) {
		super(regiaoQuadro);
	}

	public String getStatment() {
		return statment;
	}

	public void setStatment(String statment) {
		this.statment = statment;
	}
	
	@Transient
	public String getValor() {
		// TODO: Usar ValorQuadro.
		return null; 
	} 
	
	
	 
} 
 
