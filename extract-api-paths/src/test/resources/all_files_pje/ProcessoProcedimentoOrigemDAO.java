package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.ProcessoProcedimentoOrigem;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

@Name(ProcessoProcedimentoOrigemDAO.NAME)
public class ProcessoProcedimentoOrigemDAO extends BaseDAO<ProcessoProcedimentoOrigem>{

	public static final String NAME = "processoProcedimentoOrigemDAO";

	@Override
	public Object getId(ProcessoProcedimentoOrigem e) {
		return e.getId();
	}

	public Integer countProcessosCriminaisLegadosPendentes(){
		StringBuilder sb = new StringBuilder("SELECT COUNT(DISTINCT o.processoTrf.idProcessoTrf) FROM ProcessoProcedimentoOrigem o ");
		sb.append(" WHERE o.codigoNacional IS NULL AND o.processoTrf.processoStatus = :processoStatus");
		
		Query q = this.getEntityManager().createQuery(sb.toString());
		q.setParameter("processoStatus", ProcessoStatusEnum.D);
		
		Long quantidade = (Long) q.getSingleResult();
		return quantidade.intValue(); 
	}

	public Integer countProcedimentosOrigemLegadosPendentes(){
		StringBuilder sb = new StringBuilder("SELECT COUNT(DISTINCT o.id) FROM ProcessoProcedimentoOrigem o ");
		sb.append(" WHERE o.codigoNacional IS NULL AND o.processoTrf.processoStatus = :processoStatus");
		
		Query q = this.getEntityManager().createQuery(sb.toString());
		q.setParameter("processoStatus", ProcessoStatusEnum.D);

		Long quantidade = (Long) q.getSingleResult();
		return quantidade.intValue(); 
	}

	@SuppressWarnings("unchecked")
	public List<Integer> recuperarIdsProcessosCriminaisLegadosPendentes(){
		
		StringBuilder sb = new StringBuilder("SELECT DISTINCT o.processoTrf.idProcessoTrf FROM ProcessoProcedimentoOrigem o ");
		sb.append(" WHERE o.codigoNacional IS NULL AND o.processoTrf.processoStatus = :processoStatus ");
		
		Query q = this.getEntityManager().createQuery(sb.toString());
		q.setParameter("processoStatus", ProcessoStatusEnum.D);
		List<Integer> ret = q.getResultList();
		
		return CollectionUtilsPje.isEmpty(ret) ? new ArrayList<Integer>(0) : ret;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoProcedimentoOrigem> recuperarPorIdProcessTrf(Integer idProcessoTrf){
		StringBuilder sb = new StringBuilder("SELECT o FROM ProcessoProcedimentoOrigem o WHERE o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		
		Query q = this.getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcessoTrf", idProcessoTrf);
		
		List<ProcessoProcedimentoOrigem> ret = q.getResultList();
		
		return CollectionUtilsPje.isEmpty(ret) ? new ArrayList<ProcessoProcedimentoOrigem>(0) : ret;		
	}
	
}
