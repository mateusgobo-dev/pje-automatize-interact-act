package br.jus.pdpj.notificacao.service;

public class ComunicacaoAberta {
	private Long numeroComunicacao;
	private boolean foiTribunal;
	private boolean foiCienciaAutomatica;

	public Long getNumeroComunicacao() {
		return numeroComunicacao;
	}

	public void setNumeroComunicacao(Long numeroComunicacao) {
		this.numeroComunicacao = numeroComunicacao;
	}

	public boolean isFoiTribunal() {
		return foiTribunal;
	}

	public void setFoiTribunal(boolean foiTribunal) {
		this.foiTribunal = foiTribunal;
	}

	public boolean isFoiCienciaAutomatica() {
		return foiCienciaAutomatica;
	}

	public void setFoiCienciaAutomatica(boolean foiCienciaAutomatica) {
		this.foiCienciaAutomatica = foiCienciaAutomatica;
	}

}
