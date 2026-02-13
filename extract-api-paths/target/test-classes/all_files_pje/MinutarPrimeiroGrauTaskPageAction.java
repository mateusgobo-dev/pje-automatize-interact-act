package br.com.infox.bpm.taskPage.FGPJE;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name(value = MinutarPrimeiroGrauTaskPageAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class MinutarPrimeiroGrauTaskPageAction extends MinutarTaskPageAction {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "minutarPrimeiroGrauTaskPageAction";

	@Override
	protected String getNomeTarefaDestinoAposAssinatura() {
		return TaskNamesPrimeiroGrau.CONHECIMENTO_SECRETARIA;
	}

	@Override
	public boolean isTarefaConhecimentoSecretaria(String transitionName) {
		return getNomeTarefaDestinoAposAssinatura().equals(transitionName);
	}

	public static MinutarPrimeiroGrauTaskPageAction instance() {
		return (MinutarPrimeiroGrauTaskPageAction) Component.getInstance(NAME);
	}

}
