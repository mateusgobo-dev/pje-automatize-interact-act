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

import java.util.ArrayList;
import java.util.List;

public enum SituacaoProcessoSessaoEnum {

	AN("Anotação"), AJ("Aguardando julgamento"), EJ("Em julgamento"), JG("Julgado"), PR("Preferência"), PV(
			"Pedido de vista"), SO("Pedido de sustentação oral"), AD("Adiado para próxima sessão"), RJ(
			"Retirado de julgamento"), DD("Destacado para discussão"),JC("Marcado para julgamento célere") ;

	private String label;

	SituacaoProcessoSessaoEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}
	
	public static List<SituacaoProcessoSessaoEnum> getListaSituacaoPermitidaParaCerticao(String listaSituacaoJulgamento) {
		List<SituacaoProcessoSessaoEnum> listaPermitidos = new ArrayList<SituacaoProcessoSessaoEnum>();
		listaPermitidos.add(SituacaoProcessoSessaoEnum.JG);
		if (listaSituacaoJulgamento != null && !listaSituacaoJulgamento.isEmpty()) {
			for (SituacaoProcessoSessaoEnum situacaoProcessoSessaoEnum : SituacaoProcessoSessaoEnum.values()) {
				if (listaSituacaoJulgamento.toUpperCase().contains(situacaoProcessoSessaoEnum.name())) {
					listaPermitidos.add(situacaoProcessoSessaoEnum);
				}
			}
		}
		return listaPermitidos;
	}

}
