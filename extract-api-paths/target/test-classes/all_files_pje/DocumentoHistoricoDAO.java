package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.DocumentoHistorico;

@Name(DocumentoHistoricoDAO.NAME)
public class DocumentoHistoricoDAO extends BaseDAO<DocumentoHistorico>{
	
	public static final String NAME = "documentoHistoricoDAO";

	@Override
	public Object getId(DocumentoHistorico e) {
		return e.getIdDocumentoHistorico();
	} 


}
