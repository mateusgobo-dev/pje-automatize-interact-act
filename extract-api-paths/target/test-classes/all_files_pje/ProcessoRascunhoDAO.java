package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.ProcessoRascunho;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("processoRascunhoDAO")
public class ProcessoRascunhoDAO extends BaseDAO<ProcessoRascunho> {


	@Override
	public Integer getId(ProcessoRascunho pr) {
		return pr.getIdProcessoRascunho();
	}

	public ProcessoRascunho findByProcessoTrf(ProcessoTrf processoTrf) {
		return this.findByProcessoTrf(processoTrf.getIdProcessoTrf());		
	}
	
	@SuppressWarnings("unchecked")
	public ProcessoRascunho findByProcessoTrf(Integer idProcessoJudicial) {
		String query = "SELECT pr FROM ProcessoRascunho pr " +
				"	WHERE pr.processo.idProcessoTrf = :idProcessoJudicial";
		Query q = entityManager.createQuery(query);
		q.setParameter("idProcessoJudicial", idProcessoJudicial);
		
		List<ProcessoRascunho> list = q.getResultList();
		
		return list.isEmpty() ? null : list.get(0) ;		
	}
	
}
