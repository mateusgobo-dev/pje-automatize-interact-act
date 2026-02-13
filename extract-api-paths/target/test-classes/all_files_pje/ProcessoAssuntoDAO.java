package br.jus.cnj.pje.business.dao;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ProcessoAssuntoDAO.NAME)
public class ProcessoAssuntoDAO extends BaseDAO<ProcessoAssunto>{
	public static final String NAME = "processoAssuntoDAO";

	@Override
	public Object getId(ProcessoAssunto e) {
		return e.getIdProcessoAssunto();
	}

	public boolean temAssunto(ProcessoTrf processo, AssuntoTrf assunto) {
		String query = "SELECT 1 FROM ProcessoAssunto AS pa " +
				"	WHERE pa.processoTrf = :processo " +
				"	AND pa.assuntoTrf = :assunto";
		Query q = entityManager.createQuery(query);
		q.setParameter("processo", processo);
		q.setParameter("assunto", assunto);
		q.setMaxResults(1);
		try {
			q.getSingleResult();
			return true;
		} catch (NoResultException e) {
			return false;
		}
	}

}
