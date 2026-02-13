package br.jus.pje.nucleo.dto.sinapses;

import java.io.Serializable;

public class Mensagem implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private MovimentacaoSugeridaRequest mensagem;
	private Integer quantidadeClasses;

	public Mensagem(MovimentacaoSugeridaRequest mensagem, Integer quantidadeClasses) {
		super();
		this.mensagem = mensagem;
		this.quantidadeClasses = quantidadeClasses;
	}

	public Mensagem() {
		super();
	}

	public MovimentacaoSugeridaRequest getMensagem() {
		return mensagem;
	}

	public void setMensagem(MovimentacaoSugeridaRequest mensagem) {
		this.mensagem = mensagem;
	}
	
	public Integer getQuantidadeClasses() {
		return quantidadeClasses;
	}
	
	public void setQuantidadeClasses(Integer quantidadeClasses) {
		this.quantidadeClasses = quantidadeClasses;
	}
	
}
