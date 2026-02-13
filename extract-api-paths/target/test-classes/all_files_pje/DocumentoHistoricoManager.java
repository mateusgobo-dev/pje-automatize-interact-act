package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.DocumentoHistoricoDAO;
import br.jus.pje.nucleo.entidades.DocumentoHistorico;

@Name(DocumentoHistoricoManager.NAME)
public class DocumentoHistoricoManager extends BaseManager<DocumentoHistorico>{

	public static final String NAME = "documentoHistoricoManager";
	
	@In
	private DocumentoHistoricoDAO documentoHistoricoDAO;

	@Override
	protected BaseDAO<DocumentoHistorico> getDAO() {
		return this.documentoHistoricoDAO;
	}
	
}
