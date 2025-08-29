/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoHistoricoClasse;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * @author cristof
 * 
 */
@Name("processoHistoricoClasseDAO")
public class ProcessoHistoricoClasseDAO extends BaseDAO<ProcessoHistoricoClasse>{

	@Override
	public Object getId(ProcessoHistoricoClasse e) {
		return null;
	}

	public Date verificaDataInicio(ProcessoTrf processo) {
		ProcessoHistoricoClasse prc = null;
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select o from ProcessoHistoricoClasse o ").append("where o.processoTrf = :processoTrf ")
				.append("order by o.dataFim desc");

		Query query = em.createQuery(sql.toString());
		query.setParameter("processoTrf", processo);
		query.setMaxResults(1);
		try {
			prc = (ProcessoHistoricoClasse) query.getSingleResult();
		} catch (NoResultException e) {
			return processo.getDataDistribuicao();
		}
		return prc.getDataFim();
	}
	

}
