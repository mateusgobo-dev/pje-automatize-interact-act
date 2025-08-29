/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.DocumentoBinManager;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;

/**
 * @author cristof
 * 
 */
@Name("processoDocumentoBinDAO")
public class ProcessoDocumentoBinDAO extends BaseDAO<ProcessoDocumentoBin>{

	private static final String INSERT_STMT = "INSERT INTO tb_processo_documento_bin (id_processo_documento_bin, ob_processo_documento) VALUES (?, ?)";

	private static final String SELECT_STMT = "SELECT ob_processo_documento FROM tb_processo_documento_bin WHERE id_processo_documento_bin = ?";
	
	@In
	private DocumentoBinManager documentoBinManager;

	public void persist(ProcessoDocumentoBin pdb, byte[] data) throws Exception{
		if (data != null && data.length > 0){
			pdb.setNumeroDocumentoStorage(documentoBinManager.persist(data, "application/octet-stream"));
		}
		this.persist(pdb);
	}

	public byte[] getData(ProcessoDocumentoBin pdb){
		try{
			return documentoBinManager.getData(pdb.getNumeroDocumentoStorage());
		} catch (Exception e){
			throw new PJeDAOException(e);
		}
	}

	@Override
	public Integer getId(ProcessoDocumentoBin e){
		return e.getIdProcessoDocumentoBin();
	}
	
	@SuppressWarnings("unchecked")
	public ProcessoDocumentoBin findByIdentificadorUnico(String uid){
		String queryString = "select o from ProcessoDocumentoBin o where " +
				"o.md5Documento = :uid";
		Query query = getEntityManager().createQuery(queryString);
		query.setParameter("uid", uid);
		
		List<ProcessoDocumentoBin> documentos = query.getResultList();
		
		if(documentos != null && !documentos.isEmpty()){
			return documentos.get(0);
		}
			
		return null;
	}
	
	/**
	 * Obter por id Processo Documento
	 * @param idProcDoc
	 * @return ProcessoDocumentoBin
	 */
	public ProcessoDocumentoBin recuperar(Integer idProcDoc){
		StringBuilder jpql = new StringBuilder("")
				.append(" SELECT bin FROM ProcessoDocumento processoDocumento " )
				.append(" INNER JOIN processoDocumento.processoDocumentoBin bin ")
				.append(" WHERE processoDocumento.idProcessoDocumento = :idProcDoc");
		
		Query query = entityManager.createQuery(jpql.toString());
		query.setParameter("idProcDoc", idProcDoc);
		return (ProcessoDocumentoBin) query.getSingleResult();
	}
	
	public boolean existsByNumeroDocumentoStorage(String numeroDocumentoStorage, Integer idProcessoDocumentoBin){
		String queryString = "select count(*) from ProcessoDocumentoBin o where " +
				"o.numeroDocumentoStorage = :numeroDocumentoStorage and o.idProcessoDocumentoBin != :idProcessoDocumentoBin";
		Query query = getEntityManager().createQuery(queryString);
		query.setParameter("numeroDocumentoStorage", numeroDocumentoStorage);
		query.setParameter("idProcessoDocumentoBin", idProcessoDocumentoBin);
		
		Long qtd = (Long) query.getSingleResult();
		
		return qtd>0;
	}

}
