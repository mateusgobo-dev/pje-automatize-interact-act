package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.TipoPessoaQualificacao;
import br.jus.pje.nucleo.entidades.TipoPessoaQualificacaoId;

@Name("tipoPessoaQualificacaoHome")
@BypassInterceptors
public class TipoPessoaQualificacaoHome extends AbstractTipoPessoaQualificacaoHome<TipoPessoaQualificacao> {

	private static final long serialVersionUID = 1L;

	public TipoPessoaQualificacaoHome() {
		setTipoPessoaQualificacaoId(new TipoPessoaQualificacaoId());
	}

	public void submete() {

	}
}
