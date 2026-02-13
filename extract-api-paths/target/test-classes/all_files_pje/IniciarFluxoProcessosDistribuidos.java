package br.com.infox.cliente.jbpm;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.AssertionFailure;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ProcessInstance;
import org.jbpm.taskmgmt.exe.SwimlaneInstance;

import br.com.infox.ibpm.jbpm.assignment.LocalizacaoAssignment;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("iniciarFluxoProcessosDistribuidos")
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class IniciarFluxoProcessosDistribuidos implements Serializable {

	private static final long serialVersionUID = 1L;

	public void iniciarProcessoJbpm(Processo processo, Fluxo fluxo, Map<String, Object> variaveis) {
		BusinessProcess.instance().createProcess(fluxo.getFluxo().trim());
		processo.setFluxo(fluxo);
		processo.setIdJbpm(BusinessProcess.instance().getProcessId());
		EntityUtil.getEntityManager().merge(processo);

		// grava a variavel processo no jbpm com o numero do processo e-pa
		org.jbpm.graph.exe.ProcessInstance processInstance = ProcessInstance.instance();
		processInstance.getContextInstance().setVariable(Variaveis.VARIAVEL_PROCESSO, processo.getIdProcesso());
		if (variaveis != null) {
			for (Entry<String, Object> entry : variaveis.entrySet()) {
				processInstance.getContextInstance().setVariable(entry.getKey(), entry.getValue());
			}
		}

		// inicia a primeira tarefa do processo
		Collection<org.jbpm.taskmgmt.exe.TaskInstance> taskInstances = processInstance.getTaskMgmtInstance()
				.getTaskInstances();
		if (taskInstances != null && !taskInstances.isEmpty()) {
			BusinessProcess.instance().setTaskId(taskInstances.iterator().next().getId());
			BusinessProcess.instance().startTask();
		}
		SwimlaneInstance swimlaneInstance = org.jboss.seam.bpm.TaskInstance.instance().getSwimlaneInstance();
		String actorsExpression = swimlaneInstance.getSwimlane().getPooledActorsExpression();
		LocalizacaoAssignment.instance().setPooledActors(actorsExpression);
	}

	@SuppressWarnings("unchecked")
	public void execute() {
		List<ProcessoTrf> listProcessos = EntityUtil
				.getEntityManager()
				.createQuery(
						"select o from ProcessoTrf o " + "where o.processoStatus = 'D' and o not in (select l.processo"
								+ " from ProcessoLocalizacaoIbpm l)").getResultList();
		for (ProcessoTrf processoTrf : listProcessos) {
			Map<String, Object> variaveis = new HashMap<String, Object>();
			variaveis.put("orgaoJulgador", processoTrf.getOrgaoJulgador().getIdOrgaoJulgador());
			variaveis.put("titularidade", processoTrf.getOrgaoJulgadorCargo().getCargo());
			try {
				iniciarProcessoJbpm(processoTrf.getProcesso(), processoTrf.getClasseJudicial().getFluxo(), variaveis);
			} catch (AssertionFailure e) {
			}
			EntityUtil.flush();
		}
	}

}