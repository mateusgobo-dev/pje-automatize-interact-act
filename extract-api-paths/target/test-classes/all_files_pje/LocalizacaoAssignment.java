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
package br.com.infox.ibpm.jbpm.assignment;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.hibernate.type.IntegerType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.exe.ExecutionContext;

import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.component.Util;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Processo;

@Name(LocalizacaoAssignment.NAME)
@BypassInterceptors
@Install(precedence = Install.FRAMEWORK)
public class LocalizacaoAssignment implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(LocalizacaoAssignment.class);
	private static final String IBPM_QUERY_INSERT = "insert into tb_proc_localizacao_ibpm "
			+ "(id_task_jbpm, id_processinstance_jbpm, id_processo, "
			+ "id_localizacao, id_papel) values (:idTaskJbpm, "
			+ ":idProcessInstance, :idProcesso, :idLocalizacao, :idPapel)";
	public static final String NAME = "localizacaoAssignment";
	private org.jbpm.taskmgmt.exe.TaskInstance currentTaskInstance;

	@SuppressWarnings("unchecked")
	public Set<String> getPooledActors(String... localPapel) {
		boolean opened = Util.beginTransaction();
		addLocalizacaoPapel(localPapel);
		if (opened) {
			Util.commitTransction();
		}
		return Collections.EMPTY_SET;
	}

	protected boolean addLocalizacaoPapel(String... localPapel) {
		Processo processo = JbpmUtil.getProcesso();
		JbpmUtil.getJbpmSession().flush();
		if (currentTaskInstance == null) {
			currentTaskInstance = TaskInstance.instance();
		}
		if (localPapel == null || Util.isEmpty(localPapel)) {
			return false;
		}
		if (currentTaskInstance == null || processo == null) {
			return false;
		}
		boolean inserted = false;
		for (String s : localPapel) {
			insertProcessoLocalizacaoIbpm(s, processo);
			inserted = true;
		}
		return inserted;
	}
	
	private void inserirInformacoesTarefa(org.jbpm.taskmgmt.exe.TaskInstance ti, String s, Integer idProcesso){
		Long taskId = ti.getTask().getId();
		Integer[] atores = splitLocalizacaoPapel(s);
		if(atores[0] == null || atores[0].toString().isEmpty()){
			String msg = String.format("Erro ao tentar inserir responsáveis pela tarefa %s: localização vazia a ser atribuída", ti.getName());
			log.error(msg);
			throw new AplicationException(AplicationException.createMessage(msg, "getPooledActors()", "LocalizacaoAssignment", "BPM"));
		}
		org.hibernate.Query query = JbpmUtil.getJbpmSession().createSQLQuery(IBPM_QUERY_INSERT).addSynchronizedQuerySpace("tb_proc_localizacao_ibpm");
		query.setParameter("idTaskJbpm", taskId);
		query.setParameter("idProcessInstance", ti.getProcessInstance().getId());
		query.setParameter("idProcesso", idProcesso);
		query.setParameter("idLocalizacao", atores[0]);
		if (atores[1] == null || atores[1].toString().isEmpty()) {
			query.setParameter("idPapel", null, new IntegerType());
		} else {
			query.setParameter("idPapel", atores[1]);
		}
		query.executeUpdate();
	}

	protected void insertProcessoLocalizacaoIbpm(String s, Processo processo) {
		inserirInformacoesTarefa(currentTaskInstance, s, processo.getIdProcesso());
	}

	private static Integer[] splitLocalizacaoPapel(String localPapel) {
		Integer[] ret = new Integer[2];
		String local = localPapel;
		String papel = null;
		if (localPapel.contains(":")) {
			String[] split = localPapel.split(":");
			local = split[0];
			if (split.length == 2) {
				papel = split[1];
			}
		}
		ret[0] = Integer.parseInt(local);
		if (papel != null) {
			ret[1] = Integer.parseInt(papel);
		}
		return ret;
	}

	public Set<String> getPooledActors(String expression) {
		return getPooledActors(parse(expression));
	}

	public static String[] parse(String expression) {
		if (expression == null) {
			return null;
		}
		expression = expression.substring(expression.indexOf("(") + 1);
		expression = expression.replaceAll("'", "");
		expression = expression.replace(")", "");
		expression = expression.replace("}", "");
		String[] localPapel = expression.split(",");
		return localPapel;
	}

	public void setPooledActors(String expression) {
		getPooledActors(expression);
	}

	@Observer(Event.EVENTTYPE_TASK_CREATE)
	public void onTaskCreate(ExecutionContext context) {
		try {
			String expression = context.getTask().getSwimlane().getPooledActorsExpression();
			this.currentTaskInstance = context.getTaskInstance();
			getPooledActors(expression);
		} catch (Exception ex) {
			ex.printStackTrace();
			String action = "inserir processo localização: ";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.createMessage(action + ex.getLocalizedMessage(),
					"onTaskCreate()", "LocalizacaoAssignment", "BPM"));
		}
	}

	public static LocalizacaoAssignment instance() {
		return ComponentUtil.getComponent(NAME);
	}

}
