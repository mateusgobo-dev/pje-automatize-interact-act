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
 * Enum para Tipo de Extinção de punibilidade a ser usados como referência para
 * cadastro de de Informação Criminal Relevante chamado Sentença de Extinção de
 * Pubilidade
 * 
 * Caso de uso PJE_UC024 Regras: RD017
 * 
 * @author lucas.souza
 * 
 */
public enum TipoExtincaoPunibilidadeEnum {
	/*
	 * O sistema deverá exibir a Aba de Tipificação de delito caso o usuário
	 * selecione os seguintes atributos do campo Tipo de Extinção de
	 * Punibilidade. São eles: Anistia, Graça ou Indulto; Perdão judicial;
	 * Prescrição, Decadência ou Perempção; Renúncia do queixoso ou Perdão
	 * aceito; Retratação do agente; Retroatividade de Lei; Pagamento integral
	 * do débito.
	 */
	ANI("Anistia, Graça ou Indulto", true), CDP("Cumprimento da pena", false), CSC(
			"Cumprimento da suspensão condicional do processo", false), MOR("Morte do agente", false), PAG(
			"Pagamento integral do débito", true), PER("Perdão judicial", true), PRE("Prescrição", true), DEC(
			"Decadência", true), PRP("Perempção", true), REN("Renúncia do queixoso ou Perdão aceito", true), REA(
			"Retratação do agente", true), REL("Retroatividade de Lei", true);

	private String label;
	private boolean exigeTipificacaoDelito;

	/**
	 * Atributo referente a Descrição do Tipo de Extinção de Punibilidade
	 * 
	 * @param label
	 */
	TipoExtincaoPunibilidadeEnum(String label, boolean exigeTipificacaoDelito) {
		this.label = label;
		this.exigeTipificacaoDelito = exigeTipificacaoDelito;
	}

	public String getLabel() {
		return this.label;
	}

	public boolean exigeTipificacaoDelito() {
		return this.exigeTipificacaoDelito;
	}
}
