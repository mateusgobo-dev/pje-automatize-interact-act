package br.com.infox.bpm.taskPage.FGPJE;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.bpm.action.TaskAction;
import br.com.infox.ibpm.component.tree.AutomaticEventsTreeHandler;
import br.com.infox.ibpm.component.tree.EventosTreeHandler;
import br.com.itx.exception.AplicationException;

@Name(ConclusaoTaskPageAction.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class ConclusaoTaskPageAction extends TaskAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "conclusaoTaskPageAction";

	private String[] transicoesDestinoPrimarioConclusao = { TaskNamesPrimeiroGrau.MINUTAR,
			TaskNamesSegundoGrau.ANALISE_DO_GABINETE, TaskNamesSegundoGrau.ANALISE_DO_GABINETE_SREEO };

	private String[] transicoesSecretaria = { TaskNamesPrimeiroGrau.CONHECIMENTO_SECRETARIA,
			TaskNamesSegundoGrau.SECRETARIA_PROCESSANTE, TaskNamesSegundoGrau.SECRETARIA_SREEO };

	public void gravar() {
		String analiseDoGabinete = getTransitionNameDestinoPrimarioConclusao();
		if (analiseDoGabinete != null) {
			EventosTreeHandler eventsTreeHandler = (EventosTreeHandler) AutomaticEventsTreeHandler.instance();
			eventsTreeHandler.registraEventos();
			end(analiseDoGabinete);
		} else {
			throw new AplicationException("Configuração errada das transições: " + getTransitions());
		}
	}

	public String getTransitionNameDestinoPrimarioConclusao() {
		return getTransicaoDisponivel(transicoesDestinoPrimarioConclusao);
	}

	public String getTransitionNameSecretaria() {
		String transictionSecret = getTransicaoDisponivel(transicoesSecretaria); 
		return transictionSecret;
	}

	private String getTransicaoDisponivel(String[] transicoesPossiveis) {
		for (String transitionName : transicoesPossiveis) {
			if (canTransit(transitionName)) {
				return transitionName;
			}
		}
		return null;
	}

	public void clearEventAndEndTask(String transitionName) {
		AutomaticEventsTreeHandler.instance().clearList();
		end(transitionName);
	}

	public void clearEventsTree() {
		AutomaticEventsTreeHandler.instance().clearTree();
		AutomaticEventsTreeHandler.instance().clearList();
	}	
	
}
