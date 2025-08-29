package br.com.infox.bpm.taskPage.FGPJE;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.bpm.action.TaskAction;
import br.com.infox.cliente.home.ProcessoExpedienteHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(value = DarCienciaPartesTaskPageAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class DarCienciaPartesTaskPageAction extends TaskAction implements Serializable {

	public static final String NOME_AGRUPAMENTO_EXPEDICAO_DE_DOCUMENTOS = "Expedição de Documentos";

	private static final long serialVersionUID = 1L;

	public static final String NAME = "darCienciaPartesTaskPageAction";

	@In
	private transient ProcessoExpedienteManager processoExpedienteManager;

	private List<ProcessoExpediente> processoExpedienteList;
	private String taskToTransit;
	private boolean showFunctionEndTask = false;

	public void inserirAtualizarDoc() throws Exception {
		if (getTransitions().size() == 0) {
			FacesMessages.instance().add(Severity.ERROR, "Está tafera está configurada sem transições disponíveis");
			return;
		}
		ProcessoExpedienteHome.instance().inserirAtualizarDoc();
	}

	public void verificarExpedientes(String taskName) {
		taskToTransit = taskName;
		showFunctionEndTask = true;
	}

	public void end() {
		end(taskToTransit);
	}

	public void setShowFunctionEndTask(boolean showFunctionEndTask) {
		this.showFunctionEndTask = showFunctionEndTask;
	}

	public boolean getShowFunctionEndTask() {
		return showFunctionEndTask;
	}

	/**
	 * Atualiza todos os expedientes não enviados para o processo em execução no
	 * fluxo, definindo a data de exclusão, o que indica uma removação lógica.
	 */
	public void excluirExpedientesNaoEnviados() {
		if (processoExpedienteList != null && processoExpedienteList.size() > 0) {
			Date now = new Date();
			EntityManager em = getEntityManager();
			for (ProcessoExpediente pe : processoExpedienteList) {
				pe.setDtExclusao(now);
				em.merge(pe);
			}
			em.flush();
			end();
		}
	}

	/**
	 * Obtem os expedientes não enviados.
	 * 
	 * @return true se houver expedientes não enviados.
	 */
	public boolean existeExpedienteNaoEnviados() {
		Processo processo = JbpmUtil.getProcesso();
		ProcessoTrf processoTrf = getEntityManager().find(ProcessoTrf.class, processo.getIdProcesso());
		processoExpedienteList = processoExpedienteManager.listNaoEnviados(processoTrf);
		if (processoExpedienteList != null && processoExpedienteList.size() > 0) {
			return true;
		}
		end();
		return false;
	}

}
