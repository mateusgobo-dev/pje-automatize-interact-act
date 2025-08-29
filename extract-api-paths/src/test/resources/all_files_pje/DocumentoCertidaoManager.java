package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import br.jus.cnj.pje.business.dao.DocumentoCertidaoDAO;
import br.jus.pje.nucleo.entidades.DocumentoCertidao;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

@Name(DocumentoCertidaoManager.NAME)
public class DocumentoCertidaoManager extends BaseManager<DocumentoCertidao> {

	public static final String NAME = "documentoCertidaoManager"; 
	
	@In
	DocumentoCertidaoDAO documentoCertidaoDAO;
	
	@Override
	protected DocumentoCertidaoDAO getDAO() {
		return this.documentoCertidaoDAO;
	}
	
	/**
	 * Recupera o objeto {@link DocumentoCertidao} associado ao objeto {@link ProcessoDocumento}.
	 * 
	 * @param processoDocumento {@link ProcessoDocumento}.
	 * @return O objeto {@link DocumentoCertidao}
	 */
	public DocumentoCertidao recuperarDocumentoCertidao(ProcessoDocumento processoDocumento){
		return this.documentoCertidaoDAO.recuperarDocumentoCertidao(processoDocumento);
	}
}