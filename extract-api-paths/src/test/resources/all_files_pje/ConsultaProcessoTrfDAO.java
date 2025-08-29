/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import javax.persistence.Query;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrf;


@Name("consultaProcessoTrfDAO")
public class ConsultaProcessoTrfDAO extends BaseDAO<ConsultaProcessoTrf>{

	public static final String ID_PROCESSO = "idProcesso";
	
	@Override
	public Integer getId(ConsultaProcessoTrf e){
		return e.getIdProcessoTrf();
	}
	
	
	public ConsultaProcessoVO consultaProcessoVO(ProcessoTrf processoJudicial, String nomeTarefa){
		String query = 
				" select new br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO(o.processoTrf,s.idTarefa,s.dataChegadaTarefa,s.idTaskInstance,s.actorId) from ConsultaProcessoTrf o, SituacaoProcesso s where o.processoTrf.idProcessoTrf = s.idProcesso and s.nomeTarefa = :nomeTarefa and o.processoTrf.idProcessoTrf = :idProcesso "+
				" order by o.prioridade DESC, s.dataChegadaTarefa ";
				
		Query q = entityManager.createQuery(query);
		q.setParameter(ID_PROCESSO, processoJudicial.getIdProcessoTrf());
		q.setParameter("nomeTarefa", nomeTarefa);
		return (ConsultaProcessoVO) EntityUtil.getSingleResult(q);
	}
	
	public ConsultaProcessoVO consultaProcessoVO(ProcessoTrf processoJudicial, Integer idTarefa){
		String query = 
				" select new br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO(o.processoTrf,s.idTarefa,s.dataChegadaTarefa,s.idTaskInstance,s.actorId) from ConsultaProcessoTrf o, SituacaoProcesso s where o.processoTrf.idProcessoTrf = s.idProcesso and s.idTarefa = :idTarefa and o.processoTrf.idProcessoTrf = :idProcesso "+
				" order by o.prioridade DESC, s.dataChegadaTarefa ";
				
		Query q = entityManager.createQuery(query);
		q.setParameter(ID_PROCESSO, processoJudicial.getIdProcessoTrf());
		q.setParameter("idTarefa", idTarefa);
		return (ConsultaProcessoVO) EntityUtil.getSingleResult(q);
	}

	
	public Long countConsultaProcessoSituacao(ProcessoTrf processoJudicial, Integer idTarefa){
		String query = " SELECT COUNT(*) FROM tb_processo_tarefa WHERE id_tarefa = :idTarefa AND id_processo_trf = :idProcesso ";
		Query q = entityManager.createNativeQuery(query);
		q.setParameter(ID_PROCESSO, processoJudicial.getIdProcessoTrf());
		q.setParameter("idTarefa", idTarefa);
		return Long.valueOf(((Number) q.getSingleResult()).longValue());		
	}
	
	public Long countConsultaProcessoSituacao(ProcessoTrf processoJudicial, String nmTarefa){
		String query = " SELECT COUNT(*) FROM tb_processo_tarefa WHERE nm_tarefa = :nmTarefa AND id_processo_trf = :idProcesso ";
		Query q = EntityUtil.createNativeQuery(entityManager, query, "tb_processo");
		q.setParameter(ID_PROCESSO, processoJudicial.getIdProcessoTrf());
		q.setParameter("nmTarefa", nmTarefa);
		return Long.valueOf(((Number) q.getSingleResult()).longValue());	
	}

}
