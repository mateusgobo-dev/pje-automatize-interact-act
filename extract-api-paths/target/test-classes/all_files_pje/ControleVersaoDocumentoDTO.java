package br.jus.je.pje.business.dto;

import java.util.Date;

public class ControleVersaoDocumentoDTO {
	private Integer idControleVersaoDocumento;
	private Date dataModificacao;
	private String conteudo;
	private String sha1Conteudo;
	private String nomeUsuario;
	private String localizacaoUsuario;
	private boolean ativo;
	private Integer versao;
	private String observacao;

	public Integer getIdControleVersaoDocumento() {
		return idControleVersaoDocumento;
	}
	public void setIdControleVersaoDocumento(Integer idControleVersaoDocumento) {
		this.idControleVersaoDocumento = idControleVersaoDocumento;
	}
	public Date getDataModificacao() {
		return dataModificacao;
	}
	public void setDataModificacao(Date dataModificacao) {
		this.dataModificacao = dataModificacao;
	}
	public String getConteudo() {
		return conteudo;
	}
	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}
	public String getSha1Conteudo() {
		return sha1Conteudo;
	}
	public void setSha1Conteudo(String sha1Conteudo) {
		this.sha1Conteudo = sha1Conteudo;
	}
	public String getNomeUsuario() {
		return nomeUsuario;
	}
	public void setNomeUsuario(String nomeUsuario) {
		this.nomeUsuario = nomeUsuario;
	}
	public boolean isAtivo() {
		return ativo;
	}
	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
	public Integer getVersao() {
		return versao;
	}
	public void setVersao(Integer versao) {
		this.versao = versao;
	}
	public String getObservacao() {
		return observacao;
	}
	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}
	public String getLocalizacaoUsuario() {
		return localizacaoUsuario;
	}
	public void setLocalizacaoUsuario(String localizacaoUsuario) {
		this.localizacaoUsuario = localizacaoUsuario;
	}
}
