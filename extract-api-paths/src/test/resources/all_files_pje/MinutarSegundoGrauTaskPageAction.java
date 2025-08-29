package br.com.infox.bpm.taskPage.FGPJE;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name(value = MinutarSegundoGrauTaskPageAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class MinutarSegundoGrauTaskPageAction extends MinutarTaskPageAction {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "minutarSegundoGrauTaskPageAction";

	private static final String NOME_FLUXO_PROCESSANTE = "Processante";

	@Override
	protected String getNomeTarefaDestinoAposAssinatura() {
		return getNomeTarefaSecretaria();
	}

	public String getNomeTarefaSecretaria() {
		if (NOME_FLUXO_PROCESSANTE.equalsIgnoreCase(getNomeFluxoAtual())) {
			return TaskNamesSegundoGrau.SECRETARIA_PROCESSANTE;
		} else {
			return TaskNamesSegundoGrau.SECRETARIA_SREEO;
		}
	}

	@Override
	public boolean isTarefaConhecimentoSecretaria(String transitionName) {
		return TaskNamesSegundoGrau.SECRETARIA_PROCESSANTE.equals(transitionName)
				|| TaskNamesSegundoGrau.SECRETARIA_SREEO.equals(transitionName);
	}

	public static MinutarSegundoGrauTaskPageAction instance() {
		return (MinutarSegundoGrauTaskPageAction) Component.getInstance(NAME);
	}

}
