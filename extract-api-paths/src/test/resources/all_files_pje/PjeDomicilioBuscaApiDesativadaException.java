package br.jus.cnj.pje.nucleo;

import br.jus.cnj.pje.criminal.error.PjeErrorDetail;

public class PjeDomicilioBuscaApiDesativadaException extends PjeRestClientException {
	private static final long serialVersionUID = -7447025809004252949L;

	public PjeDomicilioBuscaApiDesativadaException(String mensagem) {
		super(mensagem);
	}

	public PjeDomicilioBuscaApiDesativadaException(PjeErrorDetail pjeErrorDetail) {
		super(pjeErrorDetail);
	}
}
