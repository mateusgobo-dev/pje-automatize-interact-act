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
package br.jus.pje.nucleo.enums.editor;

public enum TipoTopicoEnum {

	/*
	 * [PJEII-5151] PJE-JT: Sérgio Ricardo : PJE-1.4.7 
	 * Adequação de nomes de tópicos para facilitar a leitura pelo usuário. 
	 */		
	
	TEXTO("Texto"), 
	JULGAMENTO_MERITO("Julgamento de Mérito"), 
	ITEM_MERITO("Item de Mérito"), 
	PRELIMINARES("Preliminares"), 
	ITEM_PRELIMINARES("Item de Preliminares"), 
	DISPOSITIVO("Dispositivo"),
	PEDIDOS("Pedidos"), 
	PEDIDOS_PETICAO("Pedidos da petição"), 
	PREJUDICIAIS("Prejudiciais"),
	ITEM_PREJUDICIAIS("Item de Prejudiciais"),
	ADMISSIBILIDADE("Admissibilidade"), 
	ITEM_PRELIMINAR_ADM("Preliminar de admissibilidade"),
	CONCLUSAO_ADM("Conclusão da admissibilidade"),
	PREL_MERITO_REC("Preliminares Mérito Recurso"),
	IT_PREL_MERITO_REC("Item de preliminar"), 
	CON_PREL_MERITO_REC("Conclusão das preliminares"), 
	PREJ_MERITO_REC("Prejudiciais Mérito Recurso"), 
	IT_PREJ_MERITO_REC("Item de prejudicial"), 
	CON_PREJ_MERITO_REC("Conclusão das prejudiciais"), 
	MERITO_GRUPO_REC("Mérito Grupo Recurso"), 
	MERITO_RECURSO("Recurso da parte"), 
	IT_MERITO_RECURSO("Item de recurso"), 
	CON_MERITO_REC("Conclusão do recurso"), 
	DISPOSITIVO_ACORDAO("Dispositivo Acórdão"), 
	IT_DISP_SESSAO("Cabeçalho do acórdão"), 
	IT_DISP_VOTO("Acórdão"), 
	CONSIDERACOES("Votos de Revisores"), 
	ITEM_CONSIDERACOES("Item de Votos de Revisores");
	
	private String label;
	
	private TipoTopicoEnum(String label) {
		this.setLabel(label);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
