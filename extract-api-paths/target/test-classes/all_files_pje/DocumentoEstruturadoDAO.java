package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.DocumentoEstruturado;

@Name("documentoEstruturadoDAO")
public class DocumentoEstruturadoDAO extends BaseDAO<DocumentoEstruturado> {

	@Override
	public Object getId(DocumentoEstruturado e) {
		return e.getIdDocumentoEstruturado();
	}
	
	

}
