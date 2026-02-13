/**
 * pje-web
 * Copyright (C) 2014 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.TipoSituacaoProcessual;

/**
 * Componente de acesso a dados da entidade {@link TipoSituacaoProcessual}.
 * 
 * @author cristof
 *
 */
@Name("tipoSituacaoProcessualDAO")
public class TipoSituacaoProcessualDAO extends BaseDAO<TipoSituacaoProcessual> {

	@Override
	public Long getId(TipoSituacaoProcessual e) {
		return e.getId();
	}

	@SuppressWarnings("unchecked")
	public List<TipoSituacaoProcessual> pesquisaSituacaoProcessual(TipoSituacaoProcessual tipoSituacaoProcessual) {

		EntityManager entityManager = getEntityManager();
		StringBuilder query = new StringBuilder("SELECT tsp FROM TipoSituacaoProcessual AS tsp WHERE 1 = 1");
		if (tipoSituacaoProcessual.getCodigo().length() > 1) {
			query.append(" AND LOWER(tsp.codigo) like '%" + tipoSituacaoProcessual.getCodigo().toLowerCase() + "%'");

		}
		if (tipoSituacaoProcessual.getNome().length() > 1) {
			query.append(" AND LOWER(tsp.nome) like '%" + tipoSituacaoProcessual.getNome().toLowerCase() + "%'");

		}
		if (tipoSituacaoProcessual.getDescricao().length() > 1) {
			query.append(
					" AND LOWER(tsp.descricao) like '%" + tipoSituacaoProcessual.getDescricao().toLowerCase() + "%'");

		}

		Query q = entityManager.createQuery(query.toString());
		List<TipoSituacaoProcessual> result = q.getResultList();

		return result;

	}
}
