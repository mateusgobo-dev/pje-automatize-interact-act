package br.jus.cnj.pje.nucleo;

import br.jus.cnj.pje.criminal.error.PjeErrorDetail;

public class PjeRestClientException extends PJeException {
	private static final long serialVersionUID = -2746042963962537588L;
	private PjeErrorDetail pjeErrorDetail;

	public PjeRestClientException(String mensagem) {
		super(mensagem);
	}

	public PjeRestClientException(PjeErrorDetail pjeErrorDetail) {
		super(pjeErrorDetail.toString());
		this.pjeErrorDetail = pjeErrorDetail;
	}

	public PjeErrorDetail getPjeErrorDetail() {
		return pjeErrorDetail;
	}

	public void setPjeErrorDetail(PjeErrorDetail pjeErrorDetail) {
		this.pjeErrorDetail = pjeErrorDetail;
	}

	public String obterMensagemErroDetail() {
		String mensagem = "Erro na comunicação com serviços do Pje.";
		if (this.pjeErrorDetail != null) {
			mensagem = this.pjeErrorDetail.getMessage();
		}
		if (getMessage() != null) {
			mensagem += getMessage();
		}
		return mensagem;
	}

}
