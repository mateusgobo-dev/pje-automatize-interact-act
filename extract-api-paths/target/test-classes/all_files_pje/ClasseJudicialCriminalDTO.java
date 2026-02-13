package br.jus.pje.nucleo.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ClasseJudicialCriminalDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String codClasseJudicial;
	private String classeJudicial;
	
	public ClasseJudicialCriminalDTO() {
		super();
	}

	public ClasseJudicialCriminalDTO(String codClasseJudicial, String classeJudicial) {
		super();
		this.codClasseJudicial = codClasseJudicial;
		this.classeJudicial = classeJudicial;
	}

	public String getCodClasseJudicial() {
		return codClasseJudicial;
	}
	
	public void setCodClasseJudicial(String codClasseJudicial) {
		this.codClasseJudicial = codClasseJudicial;
	}
	
	public String getClasseJudicial() {
		return classeJudicial;
	}
	
	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}
		
}

