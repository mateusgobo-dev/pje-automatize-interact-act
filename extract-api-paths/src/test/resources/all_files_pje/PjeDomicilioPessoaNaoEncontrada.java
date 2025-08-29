package br.jus.cnj.pje.nucleo;

import br.jus.cnj.pje.criminal.error.PjeErrorDetail;

public class PjeDomicilioPessoaNaoEncontrada extends PjeRestClientException {
	private static final long serialVersionUID = -8949074607951323890L;

	public PjeDomicilioPessoaNaoEncontrada(String mensagem) {
		super(mensagem);
	}

	public PjeDomicilioPessoaNaoEncontrada(PjeErrorDetail pjeErrorDetail) {
		super(pjeErrorDetail);
	}
}
