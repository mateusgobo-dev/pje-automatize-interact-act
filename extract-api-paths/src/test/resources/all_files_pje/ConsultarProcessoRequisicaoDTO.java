/*
 * ConsultarProcessoRequisicaoTO.java
 *
 * Data: 29/07/2020
 */
package br.jus.cnj.pje.intercomunicacao.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Adriano Pamplona
 */
public class ConsultarProcessoRequisicaoDTO implements Serializable {
	private String login;
	private String senha;
	private String numeroProcesso;
	private Date dataReferencia;
	private Boolean movimentos;
	private Boolean incluirCabecalho;
	private Boolean incluirDocumentos;
	private List<String> documento = new ArrayList<>();

	/**
	 * @return login.
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * @param login Atribui login.
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * @return senha.
	 */
	public String getSenha() {
		return senha;
	}

	/**
	 * @param senha Atribui senha.
	 */
	public void setSenha(String senha) {
		this.senha = senha;
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
	 * @return dataReferencia.
	 */
	public Date getDataReferencia() {
		return dataReferencia;
	}

	/**
	 * @param dataReferencia Atribui dataReferencia.
	 */
	public void setDataReferencia(Date dataReferencia) {
		this.dataReferencia = dataReferencia;
	}

	/**
	 * @return movimentos.
	 */
	public Boolean getMovimentos() {
		return movimentos;
	}

	/**
	 * @param movimentos Atribui movimentos.
	 */
	public void setMovimentos(Boolean movimentos) {
		this.movimentos = movimentos;
	}

	/**
	 * @return incluirCabecalho.
	 */
	public Boolean getIncluirCabecalho() {
		return incluirCabecalho;
	}

	/**
	 * @param incluirCabecalho Atribui incluirCabecalho.
	 */
	public void setIncluirCabecalho(Boolean incluirCabecalho) {
		this.incluirCabecalho = incluirCabecalho;
	}

	/**
	 * @return incluirDocumentos.
	 */
	public Boolean getIncluirDocumentos() {
		return incluirDocumentos;
	}

	/**
	 * @param incluirDocumentos Atribui incluirDocumentos.
	 */
	public void setIncluirDocumentos(Boolean incluirDocumentos) {
		this.incluirDocumentos = incluirDocumentos;
	}

	/**
	 * @return documento.
	 */
	public List<String> getDocumento() {
		return documento;
	}

	/**
	 * @param documento Atribui documento.
	 */
	public void setDocumento(List<String> documento) {
		this.documento = documento;
	}
}
