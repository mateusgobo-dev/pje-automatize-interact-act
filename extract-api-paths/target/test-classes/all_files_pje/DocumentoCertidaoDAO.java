package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.DocumentoCertidao;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

@Name(DocumentoCertidaoDAO.NAME)
public class DocumentoCertidaoDAO extends BaseDAO<DocumentoCertidao> {

	public static final String NAME = "documentoCertidaoDAO"; 
	
	@Override
    public Integer getId(DocumentoCertidao e) {
        return e.getIdDocumentoCertidao().intValue();
    }
	
	/**
	 * Recupera o objeto {@link DocumentoCertidao} associado ao objeto {@link ProcessoDocumento}.
	 * 
	 * @param processoDocumento {@link ProcessoDocumento}.
	 * @return O objeto {@link DocumentoCertidao}
	 */
	@SuppressWarnings("unchecked")
	public DocumentoCertidao recuperarDocumentoCertidao(ProcessoDocumento processoDocumento){
		DocumentoCertidao certidao = null;
		String query = "FROM DocumentoCertidao o WHERE o.processoDocumento = :processoDocumento";
		try{
			List<DocumentoCertidao> certidoes = getEntityManager().createQuery(query).setParameter("processoDocumento", processoDocumento).getResultList();
			if(certidoes != null && certidoes.size() > 0) {
				certidao = certidoes.get(0);
			}
		}
		catch(NoResultException e){
			return null;
		}
		return certidao;
	}
}
