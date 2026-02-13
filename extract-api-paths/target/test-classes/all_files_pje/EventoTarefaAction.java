package br.com.infox.bpm.action;

import java.io.Serializable;
import java.text.MessageFormat;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.actions.RegistraEventoAction;
import br.com.itx.exception.AplicationException;

@Scope(ScopeType.EVENT)
@BypassInterceptors
@Name("eventoTarefaAction")
public class EventoTarefaAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public void registraEventoNaEntradaSecretariaProcessante() {
		String nomeTarefaAnterior = JbpmUtil.instance().getNomeTarefaAnteriorFromCurrentExecutionContext();
		if (nomeTarefaAnterior == null) {
			return;
		}
		if (nomeTarefaAnterior.startsWith("Arquivamento Definitivo")) {
			registrarEventoPorNome("Desarquivamento");
		} else if (nomeTarefaAnterior.startsWith("Baixa Definitiva")
				|| nomeTarefaAnterior.startsWith("Gestão Documental")) {
			registrarEventoPorNome("Reativação");
		}
	}

	public void registraEventoNaEntradaConhecimentoSecretaria() {
		String nomeTarefaAnterior = JbpmUtil.instance().getNomeTarefaAnteriorFromCurrentExecutionContext();
		if (nomeTarefaAnterior == null) {
			return;
		}
		if (nomeTarefaAnterior.equals("Baixa") || nomeTarefaAnterior.equals("Gestão Documental")) {
			registrarEventoPorNome("Reativação");
		}
	}

	private void registrarEventoPorNome(String nomeAgrupamento) {
		try {
			RegistraEventoAction.instance().registraPorNome(nomeAgrupamento);
		} catch (Exception e) {
			throw new AplicationException(MessageFormat.format("Erro ao lançar o evento {0}: {1}", nomeAgrupamento,
					e.getMessage()), e);
		}
	}

}
