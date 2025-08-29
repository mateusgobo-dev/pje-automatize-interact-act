package br.com.itx.component.grid;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.contexts.Contexts;

import br.jus.pje.nucleo.entidades.ProcessoDocumento;

public class ProcessoDocumentoGridQuery extends GridQuery {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3122304356270584564L;
	
	public static final String MAPA = "mapaDocPendentes";
	public static final String MAPA_PDB = "mapaDocPendentesPDB";

	@Override
	public List getResultList() {
		List<ProcessoDocumento> lista = super.getResultList();
		List<Integer> listaInterna = new ArrayList<Integer>();
		List<Integer> listaPDB = new ArrayList<Integer>();
		
		for(ProcessoDocumento pd : lista){
			listaInterna.add(pd.getIdProcessoDocumento());
			listaPDB.add(pd.getProcessoDocumentoBin().getIdProcessoDocumentoBin());
		}
		Contexts.getConversationContext().set("mapaDocPendentes", listaInterna);
		Contexts.getConversationContext().set("mapaDocPendentesPDB", listaPDB);
		return lista;
	}	
	
}
