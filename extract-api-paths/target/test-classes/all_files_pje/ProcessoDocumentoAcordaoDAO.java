package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.ProcessoDocumentoAcordao;

/**
 * Classe que contempla as operações de banco para a entidade ProcessoDocumentoAcordao
 * [PJEII-21041]
 * @author Thiago Nascimento Figueiredo
 *
 */
@Name(ProcessoDocumentoAcordaoDAO.NAME)
public class ProcessoDocumentoAcordaoDAO extends BaseDAO<ProcessoDocumentoAcordao> {

	public final static String NAME = "processoDocumentoAcordaoDAO";
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@SuppressWarnings("boxing")
	@Override
	public Integer getId(ProcessoDocumentoAcordao e) {
		return e.getIdProcessoDocumentosAcordao();
	}

	/**
	 * Operação que recupera os documentos vinculados a um processo e que vão participar da elaboração de
	 * um acórdão.
	 *  
	 * @param idProcesso
	 * @return lista de documentos
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumentoAcordao> recuperarDocumentosParaAcordaoEmAberto(Integer idProcesso){
		
		StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT pda FROM ProcessoDocumentoAcordao AS pda ");
        queryBuilder.append(" WHERE pda.processoDocumento.processo.idProcesso = :id AND pda.processoDocumentoAcordao IS NULL ");
        queryBuilder.append(" ORDER BY pda.ordemDocumento ");
        
        Query q = getEntityManager().createQuery(queryBuilder.toString());
        q.setParameter("id", idProcesso);

        List<ProcessoDocumentoAcordao> list = q.getResultList();

        return list;
		
	}
	
	/**
	 * Ao passar um id de um ProcessoDocumento ira apagar todos os processos de elaboracao de acordao que tenham este
	 * ProcessoDocumento vinculado.
	 * 
	 * @param idProcessoDocumento id do ProcessoDocumento vinculado.
	 */
	public void remover(Integer idProcessoDocumento) {
		String query = "DELETE FROM ProcessoDocumentoAcordao a WHERE a.processoDocumento.idProcessoDocumento = :id";
		getEntityManager().createQuery(query)
			.setParameter("id", idProcessoDocumento)
			.executeUpdate();
	}
}
