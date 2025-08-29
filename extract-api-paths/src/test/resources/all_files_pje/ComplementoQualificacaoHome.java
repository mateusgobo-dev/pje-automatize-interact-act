package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.ComplementoQualificacao;

@Name("complementoQualificacaoHome")
@BypassInterceptors
public class ComplementoQualificacaoHome extends AbstractComplementoQualificacaoHome<ComplementoQualificacao> {

	private static final long serialVersionUID = 1L;

	@Override
	public String persist() {
		refreshGrid("complementoQualificacaoGrid");
		return super.persist();
	}

	public void set(ComplementoQualificacao complementoQualificacao) {
		instance = complementoQualificacao;
	}

}