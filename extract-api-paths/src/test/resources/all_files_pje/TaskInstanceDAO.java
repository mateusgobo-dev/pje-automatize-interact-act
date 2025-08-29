/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.hibernate.Session;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.Fluxo;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.Tarefa;
import br.jus.pje.nucleo.util.DateUtil;

/**
 * @author cristof
 * 
 */
@Name("taskInstanceDAO")
public class TaskInstanceDAO extends BaseDAO<TaskInstance>{

	@Override
	public Long getId(TaskInstance e){
		return e.getId();
	}

	/**
	 * Metodo que atualiza o actorId de uma taskInstance para null
	 * 
	 * @param taskInstance
	 */
	public void limparActorId(TaskInstance taskInstance){
		String sql = "update jbpm_taskinstance set actorid_ = null where id_ = :ti";
		HibernateUtil.getSession().createSQLQuery(sql).setParameter("ti", taskInstance.getId());
	}
	
    @SuppressWarnings("unchecked")
	public List<String> obterNomeNosTarefas(Long idTaskInstance) {
        StringBuilder sql = new StringBuilder();

        sql.append(" with recursive task_breadcrumb(nivel_, name_, parent_) as ( ");
        sql.append("  select :limitToPrevent as nivel_, ti.name_, pi.superprocesstoken_ as parent_ ");
        sql.append("  from jbpm_taskinstance ti ");
        sql.append("  inner join jbpm_processinstance pi on ti.procinst_ = pi.id_ ");
        sql.append("  where ti.id_ = :idTaskInstance ");
        sql.append(" union ");
        sql.append("  select tb_.nivel_ - 1, no_.name_, instpai.superprocesstoken_ as parent_  ");
        sql.append("  from task_breadcrumb tb_, jbpm_token to_ ");
        sql.append("  inner join jbpm_node no_ on no_.id_ = to_.node_ ");
        sql.append("  inner join jbpm_processinstance instpai on instpai.id_ = to_.processinstance_ ");
        sql.append("  where to_.id_ = tb_.parent_ ) ");
        sql.append(" select name_ from task_breadcrumb order by nivel_ limit :limitToPrevent ");

        Query query = entityManager.createNativeQuery(sql.toString());

        query.setParameter("idTaskInstance", idTaskInstance);
        query.setParameter("limitToPrevent", 10);

        return query.getResultList();
    }
    
    @SuppressWarnings("unchecked")
	public Fluxo getHistoricoTarefas(Integer idProcesso) {
        Fluxo fluxo = new Fluxo();
        List<Tarefa> res = new ArrayList<Tarefa>();
        String sql =
                " SELECT ti.id_, ti.name_, ti.create_, ti.end_, usu.ds_nome, ti.isopen_, pd.name_ as name_flx_ FROM jbpm_taskinstance ti " +
                        " INNER JOIN core.tb_processo_instance tpi ON tpi.id_proc_inst = ti.procinst_ " +
                		" INNER JOIN jbpm_processinstance pi ON ti.procinst_ = pi.id_ " + 
                        " INNER JOIN jbpm_processdefinition pd on pi.processdefinition_ = pd.id_" +
                        " LEFT JOIN acl.tb_usuario_login usu ON usu.ds_login = ti.actorid_ " +
                        " WHERE tpi.id_processo = :idProcesso " +
                        " ORDER BY create_";
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("idProcesso",idProcesso);
        List<Object[]> resultList = q.getResultList();
        for(Object[] obj : resultList){
            Tarefa t = new Tarefa();
            t.setId(((BigInteger) obj[0]).longValue());
            t.setNome((String) obj[1]);
            t.setInicio((Date) obj[2]);
            t.setFim((Date) obj[3]);
            if(t.getFim() != null){
                t.setDuracao(DateUtil.diferencaTempo(t.getFim(),t.getInicio()));
            }
            t.setResponsavel((String) obj[4]);
            t.setAberta((Boolean) obj[5]);
            t.setNomeFluxo((String) obj[6]);
            res.add(t);
        }
        fluxo.setInicio((Date) resultList.get(0)[2]);
        fluxo.setDuracao(DateUtil.diferencaTempo(null,fluxo.getInicio()));
        fluxo.setAberta(true);
        fluxo.setTarefas(res);
        return fluxo;
    }
    
	@SuppressWarnings("unchecked")
	public org.jbpm.taskmgmt.exe.TaskInstance getUltimaTarefa(Integer idProcesso) {
		org.jbpm.taskmgmt.exe.TaskInstance tarefa = null;

        String sql =
                " SELECT ti.id_, ti.procinst_ FROM jbpm_taskinstance ti " +
                        " INNER JOIN core.tb_processo_instance tpi ON tpi.id_proc_inst = ti.procinst_ " +
                        " WHERE tpi.id_processo = :idProcesso " +
                        " ORDER BY ti.create_ DESC" +
                        " LIMIT 1";
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("idProcesso",idProcesso);
        List<Object[]> resultList = q.getResultList();
        
        if(CollectionUtilsPje.isNotEmpty(resultList) && resultList.size() == 1) {
        	Object[] borderTypes = resultList.get(0);
        	Long idProcessInstance = ((BigInteger) borderTypes[1]).longValue();
        	Long idTaskInstance = ((BigInteger) borderTypes[0]).longValue();
        	
        	if(idProcessInstance != null && idTaskInstance != null) {
				org.jbpm.graph.exe.ProcessInstance pi = ManagedJbpmContext.instance().getProcessInstance(idProcessInstance);
				if (pi != null) {
					ExecutionContext ec = new ExecutionContext(pi.getRootToken());
					JbpmContext context = ec.getJbpmContext();
					Session session = context.getSession();

					StringBuilder sql2 = new StringBuilder("SELECT ti FROM org.jbpm.taskmgmt.exe.TaskInstance ti ")
							.append(" JOIN ti.processInstance pi where pi.id  = :procInstance and ti.id = :taskInstance  ");
					
					org.hibernate.Query q2 = session.createQuery(sql2.toString());
					q2.setParameter("procInstance", idProcessInstance);
					q2.setParameter("taskInstance", idTaskInstance);
					
					
					List<org.jbpm.taskmgmt.exe.TaskInstance> lista = q2.list();
					if(CollectionUtilsPje.isNotEmpty(lista)) {
						tarefa = lista.get(0);
					}
				}
        	}
        }
        
        return tarefa;
	}
    
    @SuppressWarnings("unchecked")
	public List<org.jbpm.taskmgmt.exe.TaskInstance> getTarefasProcesso(Integer idProcesso, String ordem){
    	if(ordem == null) {
    		ordem = "ASC";
    	}
		List<Long> res = new ArrayList<Long>();
		String query = "select distinct o.idProcessoInstance from ProcessoInstance o where o.idProcesso = :idProcesso";

		Query q1 = entityManager.createQuery(query);
		q1.setParameter("idProcesso", idProcesso);
		res = (List<Long>) q1.getResultList();
		List<org.jbpm.taskmgmt.exe.TaskInstance> lista = null;
		
		if(res.size() > 0) {
			lista = new ArrayList<org.jbpm.taskmgmt.exe.TaskInstance>();
			
			for(Long l : res){
				lista.addAll(getTaskInstanceByIdProcessInstance(l, ordem));
			}
			Comparator<org.jbpm.taskmgmt.exe.TaskInstance> comparator = new TaskInstanceComparator();
			if("ASC".equals(ordem)){
				Collections.sort(lista,	comparator);	
			}else{
				Collections.sort(lista,	 Collections.reverseOrder(comparator));
			}

		}

		return lista;
    }

	@SuppressWarnings("unchecked")
	public List<org.jbpm.taskmgmt.exe.TaskInstance> getTaskInstanceByIdProcessInstance(Long idProcessInstance, String ordem) {
		org.jbpm.graph.exe.ProcessInstance pi = ManagedJbpmContext.instance().getProcessInstance(idProcessInstance);
		List<org.jbpm.taskmgmt.exe.TaskInstance> lista = new ArrayList<org.jbpm.taskmgmt.exe.TaskInstance>();
		if (pi != null) {
			ExecutionContext ec = new ExecutionContext(pi.getRootToken());
			JbpmContext context = ec.getJbpmContext();
			Session session = context.getSession();

			StringBuilder sql = new StringBuilder("SELECT ti FROM org.jbpm.taskmgmt.exe.TaskInstance ti ")
					.append(" JOIN ti.processInstance pi where pi.id  = :procInstance ")
					.append(" ORDER BY ti.create "+ordem);

			org.hibernate.Query q = session.createQuery(sql.toString());
			q.setParameter("procInstance", idProcessInstance);
			lista.addAll(q.list());
		}
		return lista;
	}
    
    /**
     * Ordena a lista de taskInstances por ordem crescente de data de criacao
     *
     */
    public class TaskInstanceComparator implements Comparator<org.jbpm.taskmgmt.exe.TaskInstance>{
    	@Override
    	public int compare(org.jbpm.taskmgmt.exe.TaskInstance o1, org.jbpm.taskmgmt.exe.TaskInstance o2) {
    		return o1.getCreate().compareTo(o2.getCreate());
    	}
    }
}
