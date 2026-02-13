package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.query.SessaoComposicaoOrdemQuery;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;

@Name(SessaoComposicaoOrdemDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class SessaoComposicaoOrdemDAO extends BaseDAO<SessaoComposicaoOrdem> implements Serializable, SessaoComposicaoOrdemQuery {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "sessaoComposicaoOrdemDAO";

	/**
	 * Lista os Orgãos Julgadores na composição da <code>sessao</code>
	 * informada.
	 * 
	 * @param sessao
	 *            que se deseja obter os Orgaos Julgadores
	 * @return lista de Orgãos Julgadores da sessao informada.
	 */
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> listOrgaoJulgadorComposicaoSessao(Sessao sessao) {
		Query q = getEntityManager().createQuery(LIST_ORGAO_JULGADOR_COMPOSICAO_SESSAO_QUERY);
		q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

		return q.getResultList();
	}
	
	/**
	 * Metodo que retorna a composicao da sessao exceto a composicao passada por parametro.
	 * 
	 * @param idSessao
	 * @param idSessaoComposicaoExcecao
	 * @return Lista de composição.
	 */
	@SuppressWarnings("unchecked")
	public List<SessaoComposicaoOrdem> obterComposicaoSessao(Integer idSessao, Integer idSessaoComposicaoExcecao){
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT sessaoComposicao FROM SessaoComposicaoOrdem sessaoComposicao");
		sql.append(" WHERE sessaoComposicao.sessao.idSessao = :idSessao");
		sql.append(" AND sessaoComposicao.idSessaoComposicaoOrdem != :idSco");
		
		Query query = getEntityManager().createQuery(sql.toString());
		query.setParameter("idSessao", idSessao);
		query.setParameter("idSco", idSessaoComposicaoExcecao);
		
		return query.getResultList();
	}

	@Override
	public Integer getId(SessaoComposicaoOrdem e) {
		return e.getIdSessaoComposicaoOrdem();
	}

	/**
	 * Obtem a composicao do Orgao Julgador Presidente da sessao
	 * 
	 * @param sessao
	 * @return SCO do presidente.
	 */
	public SessaoComposicaoOrdem obterOrgaoJulgadorPresidente(Sessao sessao) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(SessaoComposicaoOrdem.class);
		criteria.add(Restrictions.eq("sessao", sessao));
		criteria.add(Restrictions.eq("presidente", true));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		return (SessaoComposicaoOrdem) criteria.uniqueResult(); 
	}
	
	public void removerComposicao(Sessao sessao) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("DELETE FROM SessaoComposicaoOrdem ");
		sb.append("WHERE sessao = :sessao ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sessao", sessao);
	    q.executeUpdate();
	}

}