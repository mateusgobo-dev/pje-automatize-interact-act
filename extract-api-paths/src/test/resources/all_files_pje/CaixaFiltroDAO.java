package br.com.infox.pje.dao;

import java.io.Serializable;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.CaixaFiltroQuery;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.CaixaFiltro;
import br.jus.pje.nucleo.entidades.Tarefa;

/**
 * Classe que realiza as consultas das NamedQueries e quaisquer outras consultas
 * da entidade de CaixaFiltro.
 * 
 * @author Infox
 * 
 */
@Name(CaixaFiltroDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class CaixaFiltroDAO extends GenericDAO implements Serializable, CaixaFiltroQuery {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "caixaFiltroDAO";

	/**
	 * Retorna a quantidade de caixas contando por nome e tarefa.
	 * 
	 * @param nomeCaixa
	 *            nome da caixa
	 * @param t
	 *            Tarefa
	 * @return quantidade de caixas encontradas
	 */
	public CaixaFiltro countCaixaByNomeAndTarefa(String nomeCaixa, Tarefa t) {
		Query q = getEntityManager().createQuery(COUNT_CAIXA_BY_NOME_AND_TAREFA_QUERY);
		q.setParameter(QUERY_PARAM_NOME_CAIXA, nomeCaixa);
		q.setParameter(QUERY_PARAM_TAREFA, t);
		CaixaFiltro result = EntityUtil.getSingleResult(q);
		return result;
	}

}