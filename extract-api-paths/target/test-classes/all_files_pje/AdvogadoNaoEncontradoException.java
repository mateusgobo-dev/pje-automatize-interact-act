package br.com.infox.cliente.exception;

public class AdvogadoNaoEncontradoException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String errorMsg;

	/**
	 * Retorna o valor da propriedade errorMsg: Mensagem explicativa do erro gerado.
	 * 
	 * @return errorMsg
	 * 
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	

}
