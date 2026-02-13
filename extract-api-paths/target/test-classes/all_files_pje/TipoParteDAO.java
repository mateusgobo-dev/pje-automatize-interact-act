package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.TipoParte;

@Name(TipoParteDAO.NAME)
public class TipoParteDAO extends BaseDAO<TipoParte>{
	public static final String NAME = "tipoParteDAO";
	
	@Override
	public Object getId(TipoParte e) {
		return e.getIdTipoParte();
	}
	
	@SuppressWarnings("unchecked")
	public List<TipoParte> findByNomeParticipacao(String nomeParticipacao){
		EntityManager entityManager = getEntityManager();
		StringBuilder sql = new StringBuilder();
		
		
		sql.append("select o from TipoParte o ").append("where tipoParte = :nomeParticipacao ")
				.append("and ativo = true ");
		Query query = entityManager.createQuery(sql.toString());
		query.setParameter("nomeParticipacao", nomeParticipacao);
		
		return query.getResultList();
	}
	
 	@SuppressWarnings("unchecked")
	public List<TipoParte> recuperarPorTipoPrincipal(boolean tipoPrincipal, boolean ativo) {
		StringBuilder sb = new StringBuilder("SELECT o FROM TipoParte AS o ");
		sb.append(" WHERE o.tipoPrincipal = :tipoPrincipal ");
		sb.append(" AND o.ativo = :ativo ");
		sb.append(" ORDER BY o.tipoParte ASC ");
		
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("tipoPrincipal", tipoPrincipal);
		query.setParameter("ativo", ativo);
		
		List<TipoParte> resultList = query.getResultList();
		return resultList.isEmpty() ? new ArrayList<TipoParte>(0) : resultList;
	}
}
