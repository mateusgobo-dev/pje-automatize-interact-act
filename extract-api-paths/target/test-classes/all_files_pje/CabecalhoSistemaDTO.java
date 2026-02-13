package br.jus.pje.nucleo.dto;

import java.io.Serializable;

public class CabecalhoSistemaDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nomeSistema;
	private String nomeSecaoJudiciaria;
	private Object logoTribunal;
	private String subNomeSistema;
	private String numeroRegional;

	public CabecalhoSistemaDTO(String nomeSistema, String nomeSecaoJudiciaria, Object logoTribunal,
			String subNomeSistema, String numeroRegional) {
		super();
		this.nomeSistema = nomeSistema;
		this.nomeSecaoJudiciaria = nomeSecaoJudiciaria;
		this.logoTribunal = logoTribunal;
		this.subNomeSistema = subNomeSistema;
		this.numeroRegional = numeroRegional;
	}

	public String getNomeSistema() {
		return nomeSistema;
	}

	public void setNomeSistema(String nomeSistema) {
		this.nomeSistema = nomeSistema;
	}

	public String getNomeSecaoJudiciaria() {
		return nomeSecaoJudiciaria;
	}

	public void setNomeSecaoJudiciaria(String nomeSecaoJudiciaria) {
		this.nomeSecaoJudiciaria = nomeSecaoJudiciaria;
	}

	public Object getLogoTribunal() {
		return logoTribunal;
	}

	public void setLogoTribunal(Object logoTribunal) {
		this.logoTribunal = logoTribunal;
	}

	public String getSubNomeSistema() {
		return subNomeSistema;
	}

	public void setSubNomeSistema(String subNomeSistema) {
		this.subNomeSistema = subNomeSistema;
	}

	public String getNumeroRegional() {
		return numeroRegional;
	}

	public void setNumeroRegional(String numeroRegional) {
		this.numeroRegional = numeroRegional;
	}

}
