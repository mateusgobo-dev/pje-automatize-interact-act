package br.com.infox.cliente.entity.search;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.AplicacaoClasse;
import br.jus.pje.nucleo.entidades.ClasseJudicial;

@Name(ClasseJudicialSearch.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ClasseJudicialSearch extends ClasseJudicial {

	public static final String NAME = "classeJudicialSearch";
	private static final long serialVersionUID = 1L;
	private AplicacaoClasse aplicacaoClasse;

	public void setAplicacaoClasse(AplicacaoClasse aplicacaoClasse) {
		this.aplicacaoClasse = aplicacaoClasse;
	}

	public AplicacaoClasse getAplicacaoClasse() {
		return aplicacaoClasse;
	}

}