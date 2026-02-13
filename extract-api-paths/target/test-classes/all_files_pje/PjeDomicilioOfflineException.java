package br.jus.cnj.pje.nucleo;

import br.jus.cnj.pje.criminal.error.PjeErrorDetail;

public class PjeDomicilioOfflineException extends PjeRestClientException {
	private static final long serialVersionUID = -8949074607951323890L;

	public PjeDomicilioOfflineException(String mensagem) {
		super(mensagem);
	}

	public PjeDomicilioOfflineException(PjeErrorDetail pjeErrorDetail) {
		super(pjeErrorDetail);
	}
}
