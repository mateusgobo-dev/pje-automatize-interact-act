package br.jus.cnj.pje.webservice.controller.competencia.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.jus.pje.nucleo.entidades.CompetenciaClasseAssunto;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompetenciaClasseAssuntoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nomeCompetencia;
    private int idCompetenciaClasseAssunto;
    private String nomeAssunto;
    private String nomeClasse;
    private String nomeAplicacaoClasse;
    private boolean sigiloSegredo;
    private int nivelAcesso;

    public CompetenciaClasseAssuntoDTO(CompetenciaClasseAssunto competencia){
		this.setIdCompetenciaClasseAssunto(competencia.getIdCompClassAssu());
		this.setNomeCompetencia(competencia.getCompetencia().getCompetencia());
		this.setNivelAcesso(competencia.getNivelAcesso());
		this.setSigiloSegredo(competencia.isSegredoSigilo());
		this.setNomeAplicacaoClasse(competencia.getClasseAplicacao().getAplicacaoClasse().getAplicacaoClasse());
		this.setNomeAssunto(competencia.getAssuntoTrf().getAssuntoCompletoFormatado());
		this.setNomeClasse(competencia.getClasseAplicacao().getClasseJudicial().getClasseJudicial());
	}
    
    public CompetenciaClasseAssuntoDTO() {
		
	}

	public String getNomeCompetencia() {
        return nomeCompetencia;
    }

    public void setNomeCompetencia(String nomeCompetencia) {
        this.nomeCompetencia = nomeCompetencia;
    }

	public String getNomeAssunto() {
		return nomeAssunto;
	}

	public void setNomeAssunto(String nomeAssunto) {
		this.nomeAssunto = nomeAssunto;
	}

	public String getNomeClasse() {
		return nomeClasse;
	}

	public void setNomeClasse(String nomeClasse) {
		this.nomeClasse = nomeClasse;
	}

	public String getNomeAplicacaoClasse() {
		return nomeAplicacaoClasse;
	}

	public void setNomeAplicacaoClasse(String nomeAplicacaoClasse) {
		this.nomeAplicacaoClasse = nomeAplicacaoClasse;
	}

	public boolean isSigiloSegredo() {
		return sigiloSegredo;
	}

	public void setSigiloSegredo(boolean sigiloSegredo) {
		this.sigiloSegredo = sigiloSegredo;
	}

	public int getNivelAcesso() {
		return nivelAcesso;
	}

	public void setNivelAcesso(int nivelAcesso) {
		this.nivelAcesso = nivelAcesso;
	}

	public int getIdCompetenciaClasseAssunto() {
		return idCompetenciaClasseAssunto;
	}

	public void setIdCompetenciaClasseAssunto(int idCompetenciaClasseAssunto) {
		this.idCompetenciaClasseAssunto = idCompetenciaClasseAssunto;
	}
}