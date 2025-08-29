package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Documento;

@Name(DocumentoDAO.NAME)
public class DocumentoDAO extends BaseDAO<Documento>{
	
	public static final String NAME = "documentoDAO";

	@Override
	public Object getId(Documento e) {
		return e.getIdDocumento();
	}
}
