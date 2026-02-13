/**
 * pje-web
 * Copyright (C) 2009-2015 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.SessaoProcessoMultDocsVoto;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Classe responsável por acessar os dados da entidade SessaoProcessoMultDocsVoto.
 * 
 * @author carlos
 */
@Name("sessaoProcessoMultDocsVotoDAO")
public class SessaoProcessoMultDocsVotoDAO extends BaseDAO<SessaoProcessoMultDocsVoto> {

	@Override
	public Object getId(SessaoProcessoMultDocsVoto e) {
		return e.getId();
	}
	
	/**
	 * Recupera o objeto {@link SessaoProcessoMultDocsVoto} de acordo com o argumento informado.
	 * 
	 * @param procDoc {@link ProcessoDocumento}
	 * @return O objeto {@link SessaoProcessoMultDocsVoto} de acordo com o argumento informado.
	 */
	public SessaoProcessoMultDocsVoto recuperarSessaoProcessoDoc(ProcessoDocumento procDoc) {
		List<SessaoProcessoMultDocsVoto> resultado = null;
		Search search = new Search(SessaoProcessoMultDocsVoto.class);
		try {
			search.addCriteria(Criteria.equals("processoDocumento", procDoc));
			resultado = list(search);
		} catch (NoSuchFieldException e) {
			throw PJeDAOExceptionFactory.getDaoException(e);
		}
		return !resultado.isEmpty() ? resultado.get(0) : null;
	}

	/**
	 * Recupera o último número da ordem.
	 * 
	 * @param voto {@link SessaoProcessoDocumentoVoto}.
	 * @return Último número da ordem.
	 */
	public Integer recuperarUltimoNumeroOrdemDoc(SessaoProcessoDocumentoVoto voto) {		
		Query query = getEntityManager().createQuery(
			"select max(a.ordemDocumento) from SessaoProcessoMultDocsVoto a inner join a.sessaoProcessoDocumentoVoto b inner join b.processoTrf c " +
			"where c.idProcessoTrf = :idProcessoTrf and b.sessao " + (voto.getSessao() == null ? "is null" : "= :sessao"));
		
		if (voto.getSessao() != null) {
			query.setParameter("sessao", voto.getSessao());
		}
		
		query.setParameter("idProcessoTrf", voto.getProcessoDocumento().getProcessoTrf().getIdProcessoTrf());

		Object obj = query.getSingleResult();
		return obj == null ? 0 : (Integer)obj;
	}

	/**
	 * Recupera o último documento do voto.
	 * 
	 * @param sessaoProcessoDocumentoVoto {@link SessaoProcessoDocumentoVoto}
	 * @return O último documento do voto.
	 */
	public SessaoProcessoMultDocsVoto recuperarUltimoDoc(SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto) {
		String hql = "select o from SessaoProcessoMultDocsVoto o "
				+ "where o.sessaoProcessoDocumentoVoto =:sessaoProcessoDocumentoVoto "
				+ "order by o.processoDocumento.dataInclusao desc";
		
		Query qry = getEntityManager().createQuery(hql).setParameter("sessaoProcessoDocumentoVoto", sessaoProcessoDocumentoVoto).setMaxResults(1);
		
		@SuppressWarnings("unchecked")
		List<SessaoProcessoMultDocsVoto> resultList = qry.getResultList();
		return !resultList.isEmpty() ? resultList.get(0): null;
	}
	
	/**
	 * Recupera todos os documentos do voto.
	 * 
	 * @param sessaoProcessoDocumentoVoto {@link SessaoProcessoDocumentoVoto}
	 * @return Todos os documentos do voto.
	 */
	public List<SessaoProcessoMultDocsVoto> recuperarDocsVoto(SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto) {
		List<SessaoProcessoMultDocsVoto> resultado = null;
		Search search = new Search(SessaoProcessoMultDocsVoto.class);
		try {
			search.addCriteria(Criteria.equals("sessaoProcessoDocumentoVoto", sessaoProcessoDocumentoVoto));
			search.addOrder("processoDocumento.dataInclusao", Order.DESC);
			resultado = list(search);
		} catch (NoSuchFieldException e) {
			throw PJeDAOExceptionFactory.getDaoException(e);
		}
		return resultado;
	}

	/**
	 * Ao passar um id de um ProcessoDocumento ira apagar todos os SessaoProcessoMultDocsVoto que tenham este
	 * ProcessoDocumento vinculado.
	 * 
	 * @param idProcessoDocumento id do ProcessoDocumento vinculado.
	 */
	public void remover(Integer idProcessoDocumento) {
		String query = "DELETE FROM SessaoProcessoMultDocsVoto s  WHERE s.processoDocumento.idProcessoDocumento = :id";
		getEntityManager().createQuery(query)
			.setParameter("id", idProcessoDocumento)
			.executeUpdate();
	}
}
