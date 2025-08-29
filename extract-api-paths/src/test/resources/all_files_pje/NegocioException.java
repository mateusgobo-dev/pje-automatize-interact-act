package br.com.infox.exceptions;

public class NegocioException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4577622500694199251L;

	private String mensagem;
	
	public NegocioException(String mensagem) {
		super(mensagem);
		this.mensagem = mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public String getMensagem() {
		return mensagem;
	}
	
}