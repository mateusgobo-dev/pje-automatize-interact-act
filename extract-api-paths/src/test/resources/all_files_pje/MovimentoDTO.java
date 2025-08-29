package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

public class MovimentoDTO {

	private int idEvento;
	private String movimento;
	
	public MovimentoDTO() {
		super();
	}
	
	public MovimentoDTO(int idEvento, String movimento) {
		super();
		this.idEvento = idEvento;
		this.movimento = movimento;
	}

	public int getIdEvento() {
		return idEvento;
	}

	public void setIdEvento(int idEvento) {
		this.idEvento = idEvento;
	}

	public String getMovimento() {
		return movimento;
	}

	public void setMovimento(String evento) {
		this.movimento = evento;
	}
	
	
}
