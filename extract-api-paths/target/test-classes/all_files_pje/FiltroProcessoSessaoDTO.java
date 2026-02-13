package br.jus.pje.nucleo.dto;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.TipoVoto;

public class FiltroProcessoSessaoDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Sessao sessao;
	private String nomeParte;
	private Integer numeroSequencia;
	private Integer digitoVerificador;
	private Integer ano;
	private String ramoJustica;
	private String respectivoTribunal;
	private Integer numeroOrigem;
	private String assunto;
	private String classeJudicial;
	private OrgaoJulgador orgaoJulgador;
	private TipoVoto tipoVoto;
	private String relator;
	
	public FiltroProcessoSessaoDTO(Sessao sessao, String nomeParte, Integer numeroSequencia, Integer digitoVerificador, Integer ano, String ramoJustica, String respectivoTribunal, Integer numeroOrigem, String assunto, String classeJudicial, OrgaoJulgador orgaoJulgador, TipoVoto tipoVoto, String relator) {
		this.setSessao(sessao);
		this.setNomeParte(nomeParte);
		this.setNumeroSequencia(numeroSequencia);
		this.setDigitoVerificador(digitoVerificador);
		this.setAno(ano);
		this.setRamoJustica(ramoJustica);
		this.setRespectivoTribunal(respectivoTribunal);
		this.setNumeroOrigem(numeroOrigem);
		this.setAssunto(assunto);
		this.setClasseJudicial(classeJudicial);
		this.setOrgaoJulgador(orgaoJulgador);
		this.setTipoVoto(tipoVoto);
		this.setRelator(relator);
	}


	public Sessao getSessao() {
		return sessao;
	}


	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}


	public String getNomeParte() {
		return nomeParte;
	}


	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}


	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}


	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}


	public Integer getDigitoVerificador() {
		return digitoVerificador;
	}


	public void setDigitoVerificador(Integer digitoVerificador) {
		this.digitoVerificador = digitoVerificador;
	}


	public Integer getAno() {
		return ano;
	}


	public void setAno(Integer ano) {
		this.ano = ano;
	}


	public String getRamoJustica() {
		return ramoJustica;
	}


	public void setRamoJustica(String ramoJustica) {
		this.ramoJustica = ramoJustica;
	}


	public String getRespectivoTribunal() {
		return respectivoTribunal;
	}


	public void setRespectivoTribunal(String respectivoTribunal) {
		this.respectivoTribunal = respectivoTribunal;
	}


	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}


	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}


	public String getAssunto() {
		return assunto;
	}


	public void setAssunto(String assunto) {
		this.assunto = assunto;
	}


	public String getClasseJudicial() {
		return classeJudicial;
	}


	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}


	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}


	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}


	public TipoVoto getTipoVoto() {
		return tipoVoto;
	}


	public void setTipoVoto(TipoVoto tipoVoto) {
		this.tipoVoto = tipoVoto;
	}


	public String getRelator() {
		return relator;
	}


	public void setRelator(String relator) {
		this.relator = relator;
	}

	
}