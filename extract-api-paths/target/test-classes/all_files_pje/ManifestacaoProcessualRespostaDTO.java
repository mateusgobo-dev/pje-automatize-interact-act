/*
 * ConsultarProcessoRequisicaoTO.java
 *
 * Data: 29/07/2020
 */
package br.jus.cnj.pje.intercomunicacao.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author Adriano Pamplona
 */
public class ManifestacaoProcessualRespostaDTO implements Serializable {
	private Boolean sucesso;
	private String mensagem;
	private String numeroProcesso;
	private Date dataOperacao;
	private byte[] recibo;
	private List<Properties> parametro;

	/**
	 * @return sucesso.
	 */
	public Boolean getSucesso() {
		return sucesso;
	}

	/**
	 * @param sucesso Atribui sucesso.
	 */
	public void setSucesso(Boolean sucesso) {
		this.sucesso = sucesso;
	}

	/**
	 * @return mensagem.
	 */
	public String getMensagem() {
		return mensagem;
	}

	/**
	 * @param mensagem Atribui mensagem.
	 */
	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	/**
	 * @return numeroProcesso.
	 */
	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	/**
	 * @param numeroProcesso Atribui numeroProcesso.
	 */
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	/**
	 * @return dataOperacao.
	 */
	public Date getDataOperacao() {
		return dataOperacao;
	}

	/**
	 * @param dataOperacao Atribui dataOperacao.
	 */
	public void setDataOperacao(Date dataOperacao) {
		this.dataOperacao = dataOperacao;
	}

	/**
	 * @return recibo.
	 */
	public byte[] getRecibo() {
		return recibo;
	}

	/**
	 * @param recibo Atribui recibo.
	 */
	public void setRecibo(byte[] recibo) {
		this.recibo = recibo;
	}

	/**
	 * @return parametro.
	 */
	public List<Properties> getParametro() {
		return parametro;
	}

	/**
	 * @param parametro Atribui parametro.
	 */
	public void setParametro(List<Properties> parametro) {
		this.parametro = parametro;
	}
}
