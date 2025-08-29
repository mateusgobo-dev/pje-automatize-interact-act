package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

public class ClasseJudicialDTO {
	
	private int idClasseJudicial;
	private String classeJudicial;
	private String classeJudicialSigla;
	
	public ClasseJudicialDTO() {
		super();
	}
	
	public ClasseJudicialDTO(int idClasseJudicial, String classeJudicial, String classeJudicialSigla) {
		super();
		this.idClasseJudicial = idClasseJudicial;
		this.classeJudicial = classeJudicial;
		this.classeJudicialSigla = classeJudicialSigla;
	}
	
	public int getIdClasseJudicial() {
		return idClasseJudicial;
	}
	
	public void setIdClasseJudicial(int idClasseJudicial) {
		this.idClasseJudicial = idClasseJudicial;
	}
	
	public String getClasseJudicial() {
		return classeJudicial;
	}
	
	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}
	
	public String getClasseJudicialSigla() {
		return classeJudicialSigla;
	}
	
	public void setClasseJudicialSigla(String classeJudicialSigla) {
		this.classeJudicialSigla = classeJudicialSigla;
	}
}
