package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.query.TipoProcessoDocumentoQuery;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TipoProcessoDocumentoDTO;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.TipoDocumentoEnum;

/**
 * @author cristof
 *
 */
@Name("tipoProcessoDocumentoDAO")
public class TipoProcessoDocumentoDAO extends BaseDAO<TipoProcessoDocumento> implements TipoProcessoDocumentoQuery{

	public List<TipoProcessoDocumento> findAvailable(Papel papel){
		return this.filtraTipoProcessoDocumento(papel, new Integer[0]);
	}

	public List<TipoProcessoDocumento> findExternallyAvailable(Papel papel){
		return this.filtraTipoProcessoDocumento(papel, new Integer[0]);
	}

	public List<TipoProcessoDocumento> findByIds(Integer... ids){
		return this.filtraTipoProcessoDocumento(null, ids);
	}
	
	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumento> filtraTipoProcessoDocumento(Papel papel, Integer... ids){
		StringBuilder sql = new StringBuilder();

		sql.append(" SELECT DISTINCT tpd FROM TipoProcessoDocumento AS tpd ");
		if(papel != null) {
			sql.append(" INNER JOIN tpd.papeis AS tpdp ");
		}
		sql.append(" WHERE tpd.ativo = true ");
		if (papel != null) {
			if(Authenticator.isUsuarioExterno(papel)) {
				sql.append(" AND (tpd.visibilidade = 'E' OR tpd.visibilidade = 'A') ");
			}else {
				sql.append(" AND (tpd.visibilidade = 'I' OR tpd.visibilidade = 'A') ");
			}
			sql.append(" AND tpdp.papel = :papel ");
		}
		if (ids != null && ids.length > 0) {
			sql.append(" AND tpd.idTipoProcessoDocumento IN (:idList) ");
		}

		sql.append(" ORDER BY tpd.tipoProcessoDocumento ");

		Query q = this.entityManager.createQuery(sql.toString());

		if (papel != null) {
			q.setParameter("papel", papel);
		}
		
		if (ids != null && ids.length > 0) {
			q.setParameter("idList", Arrays.asList(ids));
		}

		return q.getResultList();
	}

	@Override
	public Integer getId(TipoProcessoDocumento e){
		return e.getIdTipoProcessoDocumento();
	}

	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumento> findTiposDocumentosAntigos(boolean inicial, boolean modelo){
		StringBuilder sb = new StringBuilder(
				"SELECT t FROM TipoProcessoDocumento t WHERE t.ativo = true and (t.visibilidade = 'I' OR t.visibilidade = 'A') ");
		//FIXME Não utilizar o 'Petição Inicial' como String 'mágica'
		if (inicial){
			sb.append("AND t.tipoProcessoDocumento = 'Petição Inicial' ");
		}
		if (modelo){
			sb.append("AND (t.inTipoDocumento = 'P' OR t.inTipoDocumento = 'T')");
		}
		else{
			sb.append("AND (t.inTipoDocumento = 'D' OR t.inTipoDocumento = 'T')");
		}
		Query q = entityManager.createQuery(sb.toString());
		List<TipoProcessoDocumento> list = q.getResultList();
		return list;
	}

	/**
	 * Obtém uma lista de tipos de documentos de expediente.
	 *
	 * @return Lista de Tipos de Documento
	 */
	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumento> getTipoProcessoDocumentoExpedienteList(){
		Query q = getEntityManager().createQuery(TIPO_PROCESSO_DOCUMENTO_EXPEDIENTE_LIST_QUERY);
		q.setParameter(QUERY_PARAM_TIPO_PROCESSO_DOCUMENTO_INTIMACAO_PAUTA, ParametroUtil.instance()
				.getTipoProcessoDocumentoIntimacaoPauta());

		List<TipoProcessoDocumento> list = q.getResultList();
		return list;
	}

	@SuppressWarnings("unchecked")
	public TipoProcessoDocumento findByCodigoDocumento(String codigoDocumento, Boolean status){
		EntityManager entityManager = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select o from TipoProcessoDocumento o ");
		sql.append("where ");
		sql.append("o.codigoDocumento = :codigoDocumento ");
		if (status != null) {
			sql.append("and o.ativo = ").append(status.toString());
		}
		Query query = entityManager.createQuery(sql.toString());
		query.setParameter("codigoDocumento", codigoDocumento);
		List<TipoProcessoDocumento> list = query.getResultList();

		if (list.size() > 0) {
			return list.get(0);
		}
		else{
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumento> findAllAtoProferido(){
		EntityManager entityManager = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select tpd from TipoProcessoDocumento tpd ");
		sql.append("where ");
		sql.append(" tpd.ativo = true ");
		sql.append(" AND tpd.documentoAtoProferido = true ");

		Query query = entityManager.createQuery(sql.toString());
		return query.getResultList();
	}


	@SuppressWarnings("unchecked")
	public TipoProcessoDocumento findByCodigoTipoProcessoDocumento(String idTipoProcessoDocumento){
		EntityManager entityManager = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select o from TipoProcessoDocumento o ").append("where o.idTipoProcessoDocumento = :idTipoProcessoDocumento");
		Query query = entityManager.createQuery(sql.toString());
		query.setParameter("idTipoProcessoDocumento", Integer.parseInt(idTipoProcessoDocumento));
		List<TipoProcessoDocumento> list = query.getResultList();

		if (list.size() > 0) {
			return list.get(0);
		}
		else{
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumento> findByAplicacaoClasse(Integer idAplicacaoClasse, String tipoProcessoDocumento){
		EntityManager entityManager = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select o.tipoProcessoDocumento from AplicacaoClasseTipoProcessoDocumento o ").
			append("where o.aplicacaoClasse.idAplicacaoClasse = :idAplicacaoClasse ").
			append("and lower(to_ascii(o.tipoProcessoDocumento.tipoProcessoDocumento)) like lower(to_ascii(:tipoProcessoDocumento))").
			append(" order by o.tipoProcessoDocumento.tipoProcessoDocumento");
		Query query = entityManager.createQuery(sql.toString());
		query.setParameter("idAplicacaoClasse", idAplicacaoClasse);
		query.setParameter("tipoProcessoDocumento", tipoProcessoDocumento + "%");
		return query.getResultList();
	}

    /**
     *  Lista todos documentos relacionados a um processo, exceto para determinado tipo que estejam ativos.
     */
	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumento> findTiposDocumentos(
			Integer idTipoDocumentoExceto) {

		EntityManager entityManager = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append(TipoProcessoDocumentoQuery.TIPO_PROCESSO_DOCUMENTOS);
		Query query = entityManager.createQuery(sql.toString());
		query.setParameter("idTipoProcessoDocumento",idTipoDocumentoExceto);
		query.setParameter("ativo",true);
		
		return query.getResultList();
	}

	/** Método que retorna a lista de tipos de documentos ativos que estejam dentro da lista informada
	 * @param idsTipoDocumento - a lista de ids dos tipos de documento que deverão ser retornados caso estejam ativos
	 * @return a lista de tipos de documentos ativos dentre os informados no parâmetro
	 */
	public List<TipoProcessoDocumento> findTiposDocumentosIn(List<Integer> idsTipoDocumento) throws PJeBusinessException{
		return findTiposDocumentosIn(TipoProcessoDocumentoQuery.TIPO_PROCESSO_DOCUMENTOS_IN, idsTipoDocumento);	}

	/** Método que retorna a lista de tipos de documentos ativos que não estejam dentro da lista informada
	 * @param idsTipoDocumento - a lista de ids dos tipos de documento que não deverão ser retornados.
	 * @return a lista de tipos de documentos ativos exceto os informados no parâmetro
	 */
	public List<TipoProcessoDocumento> findTiposDocumentosNotIn(List<Integer> idsTipoDocumento) throws PJeBusinessException{
		return findTiposDocumentosIn(TipoProcessoDocumentoQuery.TIPO_PROCESSO_DOCUMENTOS_NOT_IN, idsTipoDocumento);
	}

	/** Método que retorna a lista de tipos de documentos ativos, usando com o restrição a lista informada no parâmetro, o sql diferencia entre IN ou NOTIN
	 * @param sqlPadrao - o Sql que definirá se será utilizado In ou Not In
	 * @param idsTipoDocumento - a lista de ids dos tipos de documento considerados na query.
	 * @return a lista de tipos de documentos
	 */
	@SuppressWarnings("unchecked")
	private List<TipoProcessoDocumento> findTiposDocumentosIn(String sqlPadrao, List<Integer> idsTipoDocumento) throws PJeBusinessException{
		EntityManager entityManager = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append(sqlPadrao);
		Query query = entityManager.createQuery(sql.toString());
		query.setParameter("idTipoProcessoDocumento",idsTipoDocumento);
		query.setParameter("ativo",true);
		List<TipoProcessoDocumento> lista = query.getResultList();
		return lista;
	}

	/**
	 * Consulta os tipos de documento disponíveis.
	 *
	 * @return Lista de TipoProcessoDocumento.
	 */
	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumento> consultarTodosDisponiveis(){
		StringBuilder hql = new StringBuilder();
		hql.append("FROM TipoProcessoDocumento AS tpd ");
		hql.append("WHERE tpd.ativo = true ");
		hql.append("ORDER BY tpd.tipoProcessoDocumento");

		Query query = getEntityManager().createQuery(hql.toString());
		return query.getResultList();
	}

	/**
	 * Consulta o tipo de documento por sua descrição
	 * @param descricaoDocumento
	 * @return TipoProcessoDocumento correspondente
	 */
	public TipoProcessoDocumento findByDescricaoDocumento(String descricaoDocumento){
		String matchEspacosAEsquerda = "^\\s+";
		EntityManager entityManager = EntityUtil.getEntityManager();

		StringBuilder sql = new StringBuilder();
		sql.append("select o from TipoProcessoDocumento o ");
		sql.append("where o.tipoProcessoDocumento = :tipoProcessoDocumento and o.ativo = true ");

		Query query = entityManager.createQuery(sql.toString());
		query.setParameter("tipoProcessoDocumento", descricaoDocumento.replaceAll(matchEspacosAEsquerda, ""));
		return (TipoProcessoDocumento) EntityUtil.getSingleResult(query);
	}
	
	/**
	 * Metodo responsavel por recuperar os tipos de processo documento de acordo
	 * com a aplicação classe
	 * 
	 * @return List<TipoProcessoDocumento>
	 */
	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumento> obterTipoProcessoDocumentoPorAplicacaoClasseAtual() {
		int idAplicacaoClasse = ParametroUtil.instance().getAplicacaoSistema().getIdAplicacaoClasse();
		
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT DISTINCT tpd FROM TipoProcessoDocumento AS tpd ");
		sb.append(" WHERE tpd IN ( ");
		sb.append("   SELECT actpd.tipoProcessoDocumento FROM AplicacaoClasseTipoProcessoDocumento AS actpd ");
		sb.append("   WHERE actpd.aplicacaoClasse.idAplicacaoClasse = ").append(idAplicacaoClasse).append(") ");
		sb.append(" AND tpd.ativo IS TRUE ");
		sb.append(" ORDER BY tpd.tipoProcessoDocumento ");
		
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		List<TipoProcessoDocumento> resultList = q.getResultList();
		
		return CollectionUtilsPje.isNotEmpty(resultList) ? resultList : new ArrayList<TipoProcessoDocumento>();
		
	}
	
	/**
	 * Metodo responsavel por recuperar os tipos de processo documento de acordo
	 * com o tipo de documento informado
	 * 
	 * @param tipoDocumento
	 * @return List<TipoProcessoDocumento>
	 */
	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumento> findByTipoDocumento(TipoDocumentoEnum tipoDocumento){
		StringBuilder sb = new StringBuilder("SELECT t FROM TipoProcessoDocumento t ");
		sb.append("WHERE t.ativo = true AND t.visibilidade = 'A'");
		sb.append("AND t.inTipoDocumento = :tipoDocumento");
		Query q = entityManager.createQuery(sb.toString());
		q.setParameter("tipoDocumento", tipoDocumento);
		return q.getResultList();
	}

	
	/**
	 * Consulta os tipos de documento disponíveis.
	 *
	 * @return Lista de TipoProcessoDocumentoDTO.
	 */
	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumentoDTO> consultarTodosDisponiveisDTO() {
		StringBuilder hql = new StringBuilder("SELECT DISTINCT new br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TipoProcessoDocumentoDTO(tpd.idTipoProcessoDocumento, tpd.tipoProcessoDocumento)  FROM TipoProcessoDocumento tpd ");
		hql.append("WHERE tpd.ativo = true ");
		hql.append("ORDER BY tpd.tipoProcessoDocumento");

		Query query = getEntityManager().createQuery(hql.toString());
		return query.getResultList();
	}
}
