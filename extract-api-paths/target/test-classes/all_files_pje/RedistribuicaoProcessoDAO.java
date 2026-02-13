/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * @author Everton nogueira pereira
 *
 */
@Name(RedistribuicaoProcessoDAO.NAME)
public class RedistribuicaoProcessoDAO extends BaseDAO<ProcessoTrf>{
	public static final String NAME = "redistribuicaoProcessoDAO";

	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> obtemListaComRestricoesBasicas() {
		StringBuilder sb = new StringBuilder();
		sb.append(" select p from ProcessoTrf p ");
		sb.append(" where p.ano = 2016 ");
		Query q = getEntityManager().createQuery(sb.toString());
		return q.getResultList();
	}

	@Override
	public Object getId(ProcessoTrf e) {
		return e.getIdProcessoTrf();
	}

}
