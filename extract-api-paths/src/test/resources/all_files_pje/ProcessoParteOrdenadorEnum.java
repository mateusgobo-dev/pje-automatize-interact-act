package br.jus.pje.nucleo.enums;

import java.util.Comparator;

import br.jus.pje.nucleo.entidades.ProcessoParte;

public enum ProcessoParteOrdenadorEnum implements Comparator<ProcessoParte> {
	ORDERNAR_POR_ORDEM_POLO {
		@Override
        public int compare(ProcessoParte a, ProcessoParte b) {
			if (a.getOrdem() == null && b.getOrdem() != null) return 1;
    		if (b.getOrdem() == null && a.getOrdem() != null) return -1;
    		if (a.getOrdem() == null && b.getOrdem() == null) return 0;
    		return Integer.valueOf(a.getOrdem()).compareTo(Integer.valueOf(b.getOrdem()));
        }
    }
}