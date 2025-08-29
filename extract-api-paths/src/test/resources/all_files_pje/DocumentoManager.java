package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.DocumentoDAO;
import br.jus.pje.nucleo.entidades.Documento;

@Name(DocumentoManager.NAME)
public class DocumentoManager extends BaseManager<Documento>{
	
	public static final String NAME = "documentoManager";
	
	@In
	private DocumentoDAO documentoDAO;

	@Override
	protected BaseDAO<Documento> getDAO() {
		return documentoDAO;
	}

}
