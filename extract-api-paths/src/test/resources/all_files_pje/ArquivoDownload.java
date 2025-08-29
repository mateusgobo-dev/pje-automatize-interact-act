package br.jus.cnj.pje.vo;

public class ArquivoDownload {
	
	private String nome;
	private String contentType;
	private byte[] conteudo;
	
	public ArquivoDownload(String nome, byte[] conteudo) {
		super();
		this.nome = nome;
		this.conteudo = conteudo;
	}
	
	public ArquivoDownload() {
	}
	
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public byte[] getConteudo() {
		return conteudo;
	}
	public void setConteudo(byte[] conteudo) {
		this.conteudo = conteudo;
	}
}
