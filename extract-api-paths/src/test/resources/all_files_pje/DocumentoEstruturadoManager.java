package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.DocumentoEstruturadoDAO;
import br.jus.pje.nucleo.entidades.DocumentoEstruturado;

@Name("documentoEstruturadoManager")
public class DocumentoEstruturadoManager extends BaseManager<DocumentoEstruturado>{
	
	@In
	private DocumentoEstruturadoDAO documentoEstruturadoDAO;

	@Override
	protected BaseDAO<DocumentoEstruturado> getDAO() {
		return documentoEstruturadoDAO;
	}
	
	
}
