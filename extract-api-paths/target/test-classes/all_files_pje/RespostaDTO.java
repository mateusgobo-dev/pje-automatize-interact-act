package br.jus.je.pje.business.dto;

public class RespostaDTO {
	private Boolean sucesso;
	private String mensagem;
	private Object resposta;
	
	public Boolean getSucesso() {
		return sucesso;
	}
	public void setSucesso(Boolean sucesso) {
		this.sucesso = sucesso;
	}
	public String getMensagem() {
		return mensagem;
	}
	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}
	public Object getResposta() {
		return resposta;
	}
	public void setResposta(Object resposta) {
		this.resposta = resposta;
	}
}
