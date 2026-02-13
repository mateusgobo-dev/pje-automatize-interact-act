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
package br.jus.pje.jt.enums;

public enum ClassificacaoTipoSituacaoPautaEnum {

	A("Em andamento)"), D("Deliberado em sessão"), J("Julgado"), R("Retirado de pauta");
		
	private String label;
	
	ClassificacaoTipoSituacaoPautaEnum(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return this.label;
	}

}