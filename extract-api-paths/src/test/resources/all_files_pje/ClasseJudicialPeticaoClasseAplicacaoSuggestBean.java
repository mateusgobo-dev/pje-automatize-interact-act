package br.com.infox.cliente.component.suggest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.PeticaoClasseAplicacaoHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.ClasseJudicial;

@Name("classeJudicialPeticaoClasseAplicacaoSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ClasseJudicialPeticaoClasseAplicacaoSuggestBean extends AbstractSuggestBean<ClasseJudicial> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		getHome().limparACSuggest();
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct (o.classeJudicial) from ClasseAplicacao o ");
		sb.append("where lower(TO_ASCII(o.classeJudicial.classeJudicial)) like ");
		sb.append("lower(concat('%', TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) ");
		sb.append("order by o.classeJudicial.classeJudicial");
		return sb.toString();
	}

	@Override
	public String getDefaultValue() {
		String classeJudicial = "";
		if (getInstance() != null) {
			if (getInstance().getClasseJudicial() != null) {
				classeJudicial = getInstance().getClasseJudicial();
			}
		}
		return classeJudicial;
	}

	protected PeticaoClasseAplicacaoHome getHome() {
		return (PeticaoClasseAplicacaoHome) Component.getInstance("peticaoClasseAplicacaoHome");
	}
}
