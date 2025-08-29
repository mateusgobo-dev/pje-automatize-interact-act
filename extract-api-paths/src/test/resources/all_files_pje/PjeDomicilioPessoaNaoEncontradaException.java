package br.jus.cnj.pje.nucleo;

import br.jus.cnj.pje.criminal.error.PjeErrorDetail;

public class PjeDomicilioPessoaNaoEncontradaException extends PjeRestClientException {
	private static final long serialVersionUID = -7131062614921427603L;

	public PjeDomicilioPessoaNaoEncontradaException(String mensagem) {
		super(mensagem);
	}

	public PjeDomicilioPessoaNaoEncontradaException(PjeErrorDetail pjeErrorDetail) {
		super(pjeErrorDetail);
	}
}
