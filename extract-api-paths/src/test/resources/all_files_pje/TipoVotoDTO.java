package br.jus.je.pje.business.dto;

public class TipoVotoDTO {
	private int idTipoVoto;
	private String tipoVoto;
	private String textoCertidao;
	private Boolean relator;
	private String contexto;
	private Boolean ativo;
	private String cor;
	
	public int getIdTipoVoto() {
		return idTipoVoto;
	}
	public void setIdTipoVoto(int idTipoVoto) {
		this.idTipoVoto = idTipoVoto;
	}
	public String getTipoVoto() {
		return tipoVoto;
	}
	public void setTipoVoto(String tipoVoto) {
		this.tipoVoto = tipoVoto;
	}
	public String getTextoCertidao() {
		return textoCertidao;
	}
	public void setTextoCertidao(String textoCertidao) {
		this.textoCertidao = textoCertidao;
	}
	public Boolean getRelator() {
		return relator;
	}
	public void setRelator(Boolean relator) {
		this.relator = relator;
	}
	public String getContexto() {
		return contexto;
	}
	public void setContexto(String contexto) {
		this.contexto = contexto;
	}
	public Boolean getAtivo() {
		return ativo;
	}
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	public String getCor() {
		return cor;
	}
	public void setCor(String cor) {
		this.cor = cor;
	}
}
