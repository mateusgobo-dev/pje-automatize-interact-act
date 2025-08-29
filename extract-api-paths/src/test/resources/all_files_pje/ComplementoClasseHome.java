package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.ComplementoClasse;

@Name("complementoClasseHome")
@BypassInterceptors
public class ComplementoClasseHome extends AbstractComplementoClasseHome<ComplementoClasse> {

	private static final long serialVersionUID = 1L;

	@Override
	public String persist() {
		refreshGrid("complementoClasseGrid");
		return super.persist();
	}

	public void set(ComplementoClasse complementoClasse) {
		instance = complementoClasse;
	}

}