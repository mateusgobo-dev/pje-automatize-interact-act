package br.jus.pje.nucleo.enums;

import java.util.Comparator;

import br.jus.pje.nucleo.entidades.LiberacaoPublicacaoDecisao;

public enum LiberacaoSessaoJulgamentoOrderEnum implements Comparator<LiberacaoPublicacaoDecisao>{

	POR_PROCESSO {
		public int compare(LiberacaoPublicacaoDecisao o1, LiberacaoPublicacaoDecisao o2)
	    {
	    	return o1.getNumeroProcesso().compareTo(o2.getNumeroProcesso());
	    }
	};
}