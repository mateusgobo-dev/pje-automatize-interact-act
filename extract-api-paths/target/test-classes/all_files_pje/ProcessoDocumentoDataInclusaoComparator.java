package br.com.infox.ibpm.home;

import java.util.Comparator;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

/**
 * Classe responsável por comparar a data de inclusão do documento do processo.
 *
 */
public class ProcessoDocumentoDataInclusaoComparator implements Comparator<ProcessoDocumento> {

	@Override
	public int compare(ProcessoDocumento o1, ProcessoDocumento o2) {
		return o2.getDataInclusao().compareTo(o1.getDataInclusao());
	}

}
