package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.ModeloDocumentoLocalQuery;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

@Name("modeloDocumentoLocalDAO")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ModeloDocumentoLocalDAO extends GenericDAO implements Serializable, ModeloDocumentoLocalQuery {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public List<ModeloDocumento> getModeloDocumentoPorTipo(TipoProcessoDocumento tipoProcessoDocumento) {
		Query q = getEntityManager().createQuery(SELECT_MODELO_POR_TIPO_QUERY);
		q.setParameter(QUERY_PARAMETER_TIPO_PROCESSO_DOCUMENTO, tipoProcessoDocumento);
		List<ModeloDocumento> namedResultList = null;
		namedResultList = q.getResultList();
		return namedResultList;
	}
	
	@SuppressWarnings("unchecked")
	public List<ModeloDocumento> findByIds(Integer... ids){
		String queryStr = "SELECT m FROM ModeloDocumento AS m WHERE m.idModeloDocumento IN (?1) ORDER BY m.tituloModeloDocumento ASC";
		Query q = this.entityManager.createQuery(queryStr);
		q.setParameter(1, Arrays.asList(ids));
		List<ModeloDocumento> lista = new ArrayList<ModeloDocumento>();
		lista = q.getResultList();
		return lista;
	}
	
	@SuppressWarnings("unchecked")
	public List<ModeloDocumento> findByTipoDocumentoIdsModeloDocumento(TipoProcessoDocumento tipoProcessoDocumento, Integer... ids){
		StringBuilder queryStr = new StringBuilder();
		
		queryStr.append("SELECT mdl from ModeloDocumentoLocal mdl ");
		queryStr.append("WHERE ");
		queryStr.append("mdl.idModeloDocumento IN (:idsModeloDocumento) ");
		queryStr.append("AND mdl.tipoProcessoDocumento = :tipoProcessoDocumento ");
		queryStr.append("AND mdl.ativo = true ");
		queryStr.append("ORDER BY mdl.tituloModeloDocumento ASC ");
		
		Query q = this.entityManager.createQuery(queryStr.toString());
		q.setParameter("idsModeloDocumento", Arrays.asList(ids));
		q.setParameter("tipoProcessoDocumento", tipoProcessoDocumento);
		
		List<ModeloDocumento> lista = new ArrayList<ModeloDocumento>();
		lista = q.getResultList();
		
		return lista;
	}
	
	@SuppressWarnings("unchecked")
	public List<ModeloDocumento> findAll(){
		String queryStr = "SELECT m FROM ModeloDocumento AS m WHERE m.ativo = true ";
		Query q = EntityUtil.getEntityManager().createQuery(queryStr);
		List<ModeloDocumento> list = new ArrayList<ModeloDocumento>();
		list = q.getResultList();
		return list;
	}
	
	/**
	 * Método que busca a localização de um dado modelo.
	 * @author Ronny Paterson
	 * @since 1.4.7.2
	 * @param md Modelo cuja localização é desejadas
	 * @return Localizacao a localização do modelo informado
	 */
	public Localizacao getLocalizacaoModelo(ModeloDocumento md) {
		StringBuilder queryStr = new StringBuilder();
		
		queryStr.append("SELECT mdl.localizacao FROM ModeloDocumentoLocal mdl ");
		queryStr.append("WHERE ");
		queryStr.append("mdl.idModeloDocumento = :idModeloDocumento ");
		queryStr.append("AND mdl.ativo = true ");
		
		Query query = this.entityManager.createQuery(queryStr.toString());
		query.setParameter("idModeloDocumento", md.getIdModeloDocumento());
		
		Localizacao localizacaoModelo = null;
		
		try {
			localizacaoModelo = (Localizacao) query.getSingleResult();			
		} catch(NoResultException nre) {
			return null;
		} catch(NonUniqueResultException nre) {
			return null;
		}
		return localizacaoModelo;
	}

	/**
	 * [PJEII-5382] Busca lista de modelos de um Tipo de Documento que pertençam
	 * a uma das localizações da lista.
	 * @param tipoDocumento
	 * @param listaLocalizacoes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ModeloDocumento> getModeloDocumentoPorTipoDocumentoPorLocalizacoes(
			TipoProcessoDocumento tipoDocumento,
			List<Localizacao> listaLocalizacoes) {
		
		StringBuilder query = new StringBuilder(SELECT_MODELO_POR_TIPO_QUERY);
		query.append(" and mdl.localizacao.idLocalizacao in (");
		
		for (int i = 0; i < listaLocalizacoes.size() - 1; i++) {
			query.append(listaLocalizacoes.get(i).getIdLocalizacao());
			query.append(", ");
		}
		query.append(listaLocalizacoes.get(listaLocalizacoes.size() - 1).getIdLocalizacao());
		query.append(")");
			
		Query q = getEntityManager().createQuery(query.toString());
		q.setParameter(QUERY_PARAMETER_TIPO_PROCESSO_DOCUMENTO, tipoDocumento);
		
		List<ModeloDocumento> namedResultList = null;
		namedResultList = q.getResultList();
		return namedResultList;
	}

	/**
	 * [PJEII-5382] Busca lista de modelos que pertençam
	 * a uma das localizações da lista.
	 * @param tipoDocumento
	 * @param listaLocalizacoes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ModeloDocumento> getModeloDocumentoPorListaLocalizacao(
			List<Localizacao> listaLocalizacoes) {
		
		StringBuilder query = new StringBuilder(SELECT_MODELO_QUERY);
		query.append(" and mdl.localizacao.idLocalizacao in (");
		
		for (int i = 0; i < listaLocalizacoes.size() - 1; i++) {
			query.append(listaLocalizacoes.get(i).getIdLocalizacao());
			query.append(", ");
		}
		query.append(listaLocalizacoes.get(listaLocalizacoes.size() - 1).getIdLocalizacao());
		query.append(")");
			
		Query q = getEntityManager().createQuery(query.toString());
		
		List<ModeloDocumento> namedResultList = null;
		namedResultList = q.getResultList();
		return namedResultList;
	}
	
	/**
	 * Metodo que retorna o ModeloDocumentoLocal pelo idModeloDocumento 
	 * @param idModeloDocumento
	 * @return ModeloDocumentoLocal
	 */
	public ModeloDocumentoLocal findById(int idModeloDocumento) {
		return getEntityManager().find(ModeloDocumentoLocal.class, idModeloDocumento);
	}
}
