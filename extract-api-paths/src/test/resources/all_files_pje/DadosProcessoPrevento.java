package br.com.infox.pje.webservices;

public class DadosProcessoPrevento {

	private int id;
	private String orgaoJulgador;
	private String numeroProcesso;
	private String sessaoJudiciaria;
	private String classeJudicial;
	private String link;
	private String hash;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(String orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getSessaoJudiciaria() {
		return sessaoJudiciaria;
	}

	public void setSessaoJudiciaria(String sessaoJudiciaria) {
		this.sessaoJudiciaria = sessaoJudiciaria;
	}

	public String getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

}
