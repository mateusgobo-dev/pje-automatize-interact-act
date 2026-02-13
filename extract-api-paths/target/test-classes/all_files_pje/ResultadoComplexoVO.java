package br.jus.cnj.pje.entidades.vo;

public class ResultadoComplexoVO {
	private boolean resultado;
	private String mensagem = "";
		
	public ResultadoComplexoVO(boolean resultado, String mensagem) {
		this.resultado = resultado;
		this.mensagem = mensagem;
	}
	
	public boolean getResultado() {
		return resultado;
	}
	public void setResultado(boolean resultado) {
		this.resultado = resultado;
	}
	public String getMensagem() {
		return mensagem;
	}
	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}	
}


