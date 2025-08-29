package br.com.infox.cliente.comparator;

import java.io.Serializable;
import java.util.Comparator;

import br.jus.pje.nucleo.entidades.ProcessoParte;

public class ProcessoParteComparator implements Comparator<ProcessoParte>, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(ProcessoParte p1, ProcessoParte p2) {
		if (p1.getInParticipacao().equals(p2.getInParticipacao())) {
			return p1.getPessoa().getNome().compareToIgnoreCase(p2.getPessoa().getNome());
		} else {
			return p1.getPolo().compareTo(p2.getPolo());
		}
	}

}