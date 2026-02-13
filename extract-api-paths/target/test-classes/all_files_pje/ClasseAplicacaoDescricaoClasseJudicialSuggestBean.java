package br.com.infox.cliente.component.suggest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.CompetenciaClasseAssuntoHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.ClasseJudicial;

@Name("classeAplicacaoDescricaoClasseJudicialSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ClasseAplicacaoDescricaoClasseJudicialSuggestBean extends AbstractSuggestBean<ClasseJudicial> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		getHome().limparACSuggest();
		return "select distinct ( o.classeJudicial ) as o from ClasseAplicacao o  "
				+ "where lower(TO_ASCII(o.classeJudicial.classeJudicial)) like " + "lower(concat('%', TO_ASCII(:"
				+ INPUT_PARAMETER + "), '%')) " + "order by o.classeJudicial.classeJudicial";
	}

	protected CompetenciaClasseAssuntoHome getHome() {
		return (CompetenciaClasseAssuntoHome) Component.getInstance("competenciaClasseAssuntoHome");
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

}
