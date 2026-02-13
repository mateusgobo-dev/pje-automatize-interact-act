package br.com.infox.cliente.component.suggest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.PeticaoClasseAplicacaoHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;

@Name("aplicacaoClassePeticaoClasseAplicacaoSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class AplicacaoClassePeticaoClasseAplicacaoSuggestBean extends AbstractSuggestBean<AplicacaoClasse> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		String cj = getHome().getCJSuggest();
		return "select distinct (o.aplicacaoClasse) from ClasseAplicacao o "
				+ "where lower(TO_ASCII(o.aplicacaoClasse.aplicacaoClasse)) like " + "lower(concat('%', TO_ASCII(:"
				+ INPUT_PARAMETER + "), '%')) and " + "o.classeJudicial.classeJudicial = '" + cj + "' "
				+ "order by o.aplicacaoClasse.aplicacaoClasse";
	}

	protected PeticaoClasseAplicacaoHome getHome() {
		return (PeticaoClasseAplicacaoHome) Component.getInstance("peticaoClasseAplicacaoHome");
	}

	@Override
	public String getDefaultValue() {
		String aplicacaoClasse = "";
		if (getInstance() != null) {
			if (getInstance().getAplicacaoClasse() != null) {
				aplicacaoClasse = getInstance().getAplicacaoClasse();
			}
		}
		return aplicacaoClasse;
	}
}
