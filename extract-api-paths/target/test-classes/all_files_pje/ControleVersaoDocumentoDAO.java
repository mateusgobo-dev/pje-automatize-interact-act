package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.jt.entidades.ControleVersaoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;


@Name(ControleVersaoDocumentoDAO.NAME)
public class ControleVersaoDocumentoDAO extends BaseDAO<ControleVersaoDocumento> {
	public final static String NAME = "controleVersaoDocumentoDAO";

	@Override
	public Integer getId(ControleVersaoDocumento controleVersaoDocumento) {
		return controleVersaoDocumento.getIdControleVersaoDocumento();
	}
	
	/**
	 * Obtém todas as versões de um determinado documento
	 * @param idDocumento
	 * @return
	 */
	public List<ControleVersaoDocumento> obterTodasVersoesPorIdDocumento(Integer idDocumento) {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT controleVersaoDocumento FROM ControleVersaoDocumento controleVersaoDocumento ");
		query.append(" WHERE controleVersaoDocumento.processoDocumentoBin.idProcessoDocumentoBin = :idDocumento");
		query.append(" ORDER BY controleVersaoDocumento.dataModificacao DESC");

		Query querie = getEntityManager().createQuery(query.toString());
		querie.setParameter("idDocumento", idDocumento);

		return querie.getResultList();
	}

	/**
	 * Deleta todas as versões de um determinado documento
	 * @param idDocumento
	 */
	public void deletarTodasVersoesIdDocumento(Integer idProcessoDocumentoBin) {
		StringBuilder query = new StringBuilder();
		query.append(" DELETE FROM ControleVersaoDocumento controleVersaoDocumento ");
		query.append(" WHERE controleVersaoDocumento.processoDocumentoBin.idProcessoDocumentoBin = :idProcessoDocumentoBin");

		Query querie = getEntityManager().createQuery(query.toString());
		querie.setParameter("idProcessoDocumentoBin", idProcessoDocumentoBin);
		querie.executeUpdate();
	}

	/**
	 * Deleta determinada versão
	 * @param ControleVersaoDocumento
	 */

	public void deletarControleVersaoDocumento(
			ControleVersaoDocumento controleVersaoDocumento) {
		StringBuilder query = new StringBuilder();
		query.append(" DELETE FROM ControleVersaoDocumento controleVersaoDocumento ");
		query.append(" WHERE controleVersaoDocumento.idControleVersaoDocumento = :idControleVersaoDocumento");

		Query querie = getEntityManager().createQuery(query.toString());
		querie.setParameter("controleVersaoDocumento.idControleVersaoDocumento", controleVersaoDocumento.getIdControleVersaoDocumento());
		querie.executeUpdate();
	}

	/**
	 * Obtém último documento versionado de determinado processo documento
	 * 
	 * @param ControleVersaoDocumento
	 * @return ControleVersaoDocumento
	 */
	public ControleVersaoDocumento obtemUltimoDocumentoVersionado(
			ControleVersaoDocumento controleVersaoDocumento) {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT controleVersaoDocumento FROM ControleVersaoDocumento controleVersaoDocumento ");
		query.append(" WHERE controleVersaoDocumento.processoDocumentoBin.idProcessoDocumentoBin = :idProcessoDocumentoBin");
		query.append(" ORDER BY controleVersaoDocumento.idControleVersaoDocumento DESC ");

		Query querie = getEntityManager().createQuery(query.toString());
		querie.setParameter("idProcessoDocumentoBin", controleVersaoDocumento.getProcessoDocumentoBin().getIdProcessoDocumentoBin());
		querie.setMaxResults(1);
		List<ControleVersaoDocumento> list =  querie.getResultList();
		
		if(list.size()==0) {
			return null;
		}

		return list.get(0);
	}
	
	/**
	 * Obtém todas as versões de um determinado documento de versão inicial a versão final
	 * @param idDocumento
	 * @return
	 */
	public List<ControleVersaoDocumento> obterVersoesPorIdDocumentoPaginada(Integer idDocumento, int limit, int offset) {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT controleVersaoDocumento FROM ControleVersaoDocumento controleVersaoDocumento ");
		query.append(" WHERE controleVersaoDocumento.processoDocumentoBin.idProcessoDocumentoBin = :idDocumento");
		query.append(" ORDER BY controleVersaoDocumento.versao DESC");

		Query querie = getEntityManager().createQuery(query.toString());
		querie.setParameter("idDocumento", idDocumento);

		return querie.setFirstResult(offset).setMaxResults(limit).getResultList();
	}

	public ControleVersaoDocumento obterVersaoDocumento(int versaoDocumento, ProcessoDocumentoBin processoDocumentoBin) {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT controleVersaoDocumento FROM ControleVersaoDocumento controleVersaoDocumento ");
		query.append(" WHERE controleVersaoDocumento.processoDocumentoBin.idProcessoDocumentoBin = :idProcessoDocumentoBin ");
		query.append(" AND controleVersaoDocumento.versao = :versao");
		query.append(" ORDER BY controleVersaoDocumento.dataModificacao DESC");

		Query querie = getEntityManager().createQuery(query.toString());
		querie.setParameter("idProcessoDocumentoBin", processoDocumentoBin.getIdProcessoDocumentoBin());
		querie.setParameter("versao", versaoDocumento);
		List<ControleVersaoDocumento> list =  querie.getResultList();

		return list.get(0);
	}

	/**
	 * Remove o primeiro documento versionado
	 * @param idDocumento
	 * @return
	 */
	public void removePrimeiroDocumentoVersionado(int idProcessoDocumentoBin) {
		StringBuilder query = new StringBuilder();
		query.append(" DELETE FROM ControleVersaoDocumento controleVersaoDocumento ");
		query.append(" WHERE controleVersaoDocumento.processoDocumentoBin.idProcessoDocumentoBin = :idProcessoDocumentoBin");
		query.append(" AND controleVersaoDocumento.versao = 1");
		Query querie = getEntityManager().createQuery(query.toString());
		querie.setParameter("idProcessoDocumentoBin", idProcessoDocumentoBin);
		querie.executeUpdate();
	}
	
	/**
	 * Obtém todas as versões de um determinado documento ordenado em ordem crescente pela data de modificação
	 * @param idDocumento
	 * @return
	 */
	public List<ControleVersaoDocumento> obterTodasVersoesPorIdDocumentoOrdemCrescenteDataModificao(Integer idDocumento) {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT controleVersaoDocumento FROM ControleVersaoDocumento controleVersaoDocumento ");
		query.append(" WHERE controleVersaoDocumento.processoDocumentoBin.idProcessoDocumentoBin = :idDocumento");
		query.append(" ORDER BY controleVersaoDocumento.dataModificacao ASC");

		Query querie = getEntityManager().createQuery(query.toString());
		querie.setParameter("idDocumento", idDocumento);

		return querie.getResultList();
	}

}
