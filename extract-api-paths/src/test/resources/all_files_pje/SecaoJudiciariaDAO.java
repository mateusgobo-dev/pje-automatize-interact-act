package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.SecaoJudiciariaQuery;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;

/**
 * Classe com as consultas a entidade de Competencia.
 * 
 * @author Daniel
 * 
 */
@Name(SecaoJudiciariaDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class SecaoJudiciariaDAO extends GenericDAO implements Serializable, SecaoJudiciariaQuery {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "secaoJudiciariaDAO";

	@SuppressWarnings("unchecked")
	public List<SecaoJudiciaria> secaoJudiciariaItems() {
		Query q = getEntityManager().createQuery(SECAO_JUDICIARIA_QUERY);
		return q.getResultList();
	}

	public SecaoJudiciaria secaoJudiciaria1Grau() {
		Query q = getEntityManager().createQuery(SECAO_JUDICIARIA_1_GRAU_QUERY);
		q.setParameter(QUERY_PARAMETER_SECAO, ParametroUtil.instance().getSecao());

		SecaoJudiciaria secaoJudiciaria = EntityUtil.getSingleResult(q);
		return secaoJudiciaria;
	}

	@SuppressWarnings("unchecked")
	public List<SecaoJudiciaria> listSecaoJudiciaria1Grau() {
		List<SecaoJudiciaria> resultList = null;
		Query q = getEntityManager().createQuery(LIST_SECAO_JUDICIARIA_1_GRAU_QUERY);
		q.setParameter(QUERY_PARAMETER_SECAO, ParametroUtil.instance().getSecao());

		resultList = q.getResultList();
		return resultList;
	}
}