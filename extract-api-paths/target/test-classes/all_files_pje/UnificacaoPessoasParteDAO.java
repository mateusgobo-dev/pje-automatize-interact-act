package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.UnificacaoPessoasParte;

@Name(UnificacaoPessoasParteDAO.NAME)
public class UnificacaoPessoasParteDAO extends BaseDAO<UnificacaoPessoasParte>{

	public static final String NAME = "unificacaoPessoasParteDAO";

	@Override
	public Object getId(UnificacaoPessoasParte e) {
		return e.getIdUnificacaoPessoasParte();
	}
	
	/**
	 * Recupera lista de partes alteradas em uma unificação.
	 * 
	 * @param processoparte {@link ProcessoParte}.
	 * @return Lista de partes no processo alterado.
	 */	
	@SuppressWarnings("unchecked")
	public List<UnificacaoPessoasParte> recuperarUnificacaoPessoasParte(ProcessoParte pp) {
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder("select u from UnificacaoPessoasParte u ");
		sql.append("where u.parte = :parte ")
			.append("order by u.ativo, u.unificacao.dataUnificacao");
		Query query = em.createQuery(sql.toString());
		query.setParameter("parte", pp);

		return (List<UnificacaoPessoasParte>) query.getResultList();
	}
}
