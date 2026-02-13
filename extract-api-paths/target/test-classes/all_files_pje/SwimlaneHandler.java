/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.ibpm.jbpm.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;

import org.hibernate.Query;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.taskmgmt.def.Swimlane;
import org.jbpm.taskmgmt.def.Task;

import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.ReflectionsUtil;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;

public class SwimlaneHandler {

	private Swimlane swimlane;
	private Localizacao localizacaoModelo;
	private Papel papel;
	private List<UsuarioLocalizacao> localPapelList = new ArrayList<UsuarioLocalizacao>();
	private boolean dirty;
	private List<Papel> papelList;
	private static final String IBPM_QUERY_DELETE ="delete from tb_proc_localizacao_ibpm " +
	 "where id_localizacao = :idLocalizacaoModelo and id_papel = :idPapel and id_task_jbpm = "+
	 "(select max(id_) from jbpm_task where name_ = :swimlane)";
	private static final String IBPM_QUERY_INSERT = "insert into tb_proc_localizacao_ibpm " +
	 "(id_task_jbpm, id_processinstance_jbpm, id_processo, " +
	 "id_localizacao, id_papel) values (:idTaskJbpm, " +
	 ":idProcessInstance, :idProcesso, :idLocalizacaoModelo, :idPapel)";
	
	
	public SwimlaneHandler(Swimlane swimlane) {
		this.swimlane = swimlane;
	}

	public Swimlane getSwimlane() {
		return swimlane;
	}

	public void setSwimlane(Swimlane swimlane) {
		this.swimlane = swimlane;
	}

	public String getName() {
		return swimlane.getName();
	}

	public void setName(String name) {
		Map<String, Swimlane> swimlanes = swimlane.getTaskMgmtDefinition().getSwimlanes();
		swimlanes.remove(swimlane.getName());
		ReflectionsUtil.setValue(swimlane, "name", name);
		swimlane.getTaskMgmtDefinition().addSwimlane(swimlane);
	}

	public void setLocalizacaoModelo(Localizacao localizacaoModelo) {
		this.localizacaoModelo = localizacaoModelo;
		this.papel = null;
		this.papelList = null;
	}

	public Localizacao getLocalizacaoModelo() {
		return localizacaoModelo;
	}
	
	public Localizacao getLocalizacao() {
		return localizacaoModelo;
	}

	public void addLocalPapel() {
		if (localizacaoModelo == null || papel == null) {
			FacesMessages.instance().add(Severity.ERROR, "Os campos Localização Modelo e Papel são obrigatórios.");
			return;
		}

		UsuarioLocalizacao u = new UsuarioLocalizacao();
		u.setLocalizacaoModelo(localizacaoModelo);
		u.setPapel(papel);
		getLocalPapelList().add(u);
		updateFluxo();
		buildExpression();

		setPapel(null);
	}

	@SuppressWarnings("unchecked")
	public List<Papel> getPapelList() {
		if (papelList == null) {
			papelList = new ArrayList<Papel>();
			if (localizacaoModelo != null) {
				EntityManager em = EntityUtil.getEntityManager();
				papelList = em
						.createQuery(
								"select distinct l.papel " + "from UsuarioLocalizacao l "
										+ "where l.localizacaoModelo = :loc ").setParameter("loc", localizacaoModelo)
						.getResultList();
			}
		}
		return papelList;
	}

	public void removeLocalPapel(UsuarioLocalizacao u) {
		for (Iterator<UsuarioLocalizacao> i = localPapelList.iterator(); i.hasNext();) {
			UsuarioLocalizacao uloc = i.next();
			Localizacao l = uloc.getLocalizacaoModelo();
			Papel p = uloc.getPapel();
			boolean mesmoPapel = false;
			if (p == null) {
				mesmoPapel = u.getPapel() == null;
			} else {
				mesmoPapel = p.equals(u.getPapel());
			}
			if (l.equals(u.getLocalizacaoModelo()) && mesmoPapel) {
				deleteProcessoLocalizacaoIbpm(u);
				i.remove();
			}
		}
		buildExpression();
	}

	public void deleteProcessoLocalizacaoIbpm(UsuarioLocalizacao ul){
		Task t = null;
		Map<String, Task> map = swimlane.getTaskMgmtDefinition().getTasks();
		  for (Map.Entry<String, Task> m: map.entrySet()) {
		     t = m.getValue();
		     if (t != null && t.getSwimlane() != null && t.getSwimlane().getName() != null && t.getSwimlane().getName().equals(swimlane.getName())) {
			    	 Query query = JbpmUtil.getJbpmSession().createSQLQuery(IBPM_QUERY_DELETE).addSynchronizedQuerySpace("tb_proc_localizacao_ibpm");
			    	 query.setParameter("idLocalizacaoModelo", ul.getLocalizacaoModelo().getIdLocalizacao());
			    	 query.setParameter("idPapel", ul.getPapel().getIdPapel());
			    	 query.setParameter("swimlane", t.getName());
			    	 query.executeUpdate();
		  }
	   }	     
	}	
	
	private void buildExpression() {
		if (getLocalPapelList().isEmpty()) {
			swimlane.setPooledActorsExpression(null);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("#{localizacaoAssignment.getPooledActors('");
			boolean first = true;
			for (UsuarioLocalizacao u : getLocalPapelList()) {
				if (!first) {
					sb.append(",");
				}
				sb.append(u.getLocalizacaoModelo().getIdLocalizacao());
				if (u.getPapel() != null) {
					sb.append(":").append(u.getPapel().getIdPapel());
				}
				first = false;
			}
			sb.append("')}");
			String expression = sb.toString();
			dirty = !expression.equals(swimlane.getPooledActorsExpression());
			swimlane.setPooledActorsExpression(expression);
		}
	}

	public static List<SwimlaneHandler> createList(ProcessDefinition instance) {
		List<SwimlaneHandler> ret = new ArrayList<SwimlaneHandler>();
		Map<String, Swimlane> swimlanes = instance.getTaskMgmtDefinition().getSwimlanes();
		if (swimlanes == null) {
			return ret;
		}
		Collection<Swimlane> values = swimlanes.values();
		for (Swimlane swimlane : values) {
			SwimlaneHandler sh = new SwimlaneHandler(swimlane);
			String exp = swimlane.getPooledActorsExpression();
			if (exp != null) {
				StringTokenizer st = new StringTokenizer(swimlane.getPooledActorsExpression(), "(,)}");
				// pula o inicio
				st.nextToken();
				while (st.hasMoreTokens()) {
					String s = st.nextToken().trim();
					s = s.replaceAll("'", "");
					String local = s;
					Papel papel = null;
					if (s.contains(":")) {
						local = s.split(":")[0];
						String idPapel = s.split(":")[1];
						papel = EntityUtil.getEntityManager().find(Papel.class, Integer.parseInt(idPapel));
					}
					Localizacao loc = EntityUtil.getEntityManager().find(Localizacao.class, Integer.parseInt(local));
					UsuarioLocalizacao u = new UsuarioLocalizacao();
					u.setLocalizacaoModelo(loc);
					u.setPapel(papel);
					sh.getLocalPapelList().add(u);
				}
			}
			ret.add(sh);
		}
		return ret;
	}

	public boolean isDirty() {
		return dirty;
	}

	@SuppressWarnings("static-access")
	private void updateFluxo(){
	  Long taskId = null;
	  Task t = null;
	  Tarefa tarefa = null;
	  Map<String, Task> map = swimlane.getTaskMgmtDefinition().getTasks();
	  for (Map.Entry<String, Task> m: map.entrySet()) {
	     t = m.getValue();
	     	//se for nó de início, não precisa tratar
	     	if(t.getStartState() != null){
	     		continue;
	     	}
			if (t.getSwimlane() != null && t.getSwimlane().getName().equals(swimlane.getName())) {
				tarefa = JbpmUtil.instance().getTarefa(
						t.getName(),
						swimlane.getTaskMgmtDefinition().getProcessDefinition()
								.getName());
				if (tarefa != null) {
					taskId = tarefa.getLastIdJbpmTask();
					String sql=   "insert into tb_proc_localizacao_ibpm "
								+ "(id_task_jbpm, id_processinstance_jbpm, id_processo, "
								+ "id_localizacao, id_papel) "
								+ "(select id_task_jbpm, id_processinstance_jbpm, id_processo, "
								+ ":idLocalizacaoModelo, :idPapel "
								+ "from tb_proc_localizacao_ibpm where id_task_jbpm = :taskId)";
					org.hibernate.Query q = JbpmUtil.getJbpmSession()
								.createSQLQuery(sql).addSynchronizedQuerySpace("tb_proc_localizacao_ibpm");
					q.setParameter("taskId", taskId);
					q.setParameter("idLocalizacaoModelo", localizacaoModelo
							.getIdLocalizacao());
					q.setParameter("idPapel", papel.getIdPapel());
					q.executeUpdate();
				}
			}
	     }
	 }
		 
	 @SuppressWarnings("unchecked")
	 private List<Map<String, Object>> getProcessByTask(Long idTask){
	  String s = "select distinct new Map(tl.processo.idProcesso as idProcesso,tl.idProcessInstanceJbpm as idJbpm) from ProcessoLocalizacaoIbpm tl " +
	  "where tl.idTaskJbpm = :idTask ";
	  EntityManager em = EntityUtil.getEntityManager();
	  List<Map<String,Object>> processList= em.createQuery(s).setParameter("idTask",idTask).getResultList();
	  return processList;
	 }	
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof SwimlaneHandler) {
			if(getName() == null || getName().isEmpty()){
				return false;
			}
			SwimlaneHandler sh = (SwimlaneHandler) obj;
			if(sh.getName() == null || sh.getName().isEmpty()){
				return false;
			}
			return this.getSwimlane().getName().equals(sh.getSwimlane().getName());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		if(getName() == null || getName().isEmpty()){
			return -1;
		}
		return getName().hashCode();
	}

	@Override
	public String toString() {
		return swimlane.getName();
	}

	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	public Papel getPapel() {
		return papel;
	}

	public void setLocalPapelList(List<UsuarioLocalizacao> localPapelList) {
		this.localPapelList = localPapelList;
	}

	public List<UsuarioLocalizacao> getLocalPapelList() {
		return localPapelList;
	}

}