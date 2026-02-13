package br.com.infox.cliente.bean;

import java.io.Serializable;
import java.util.Date;

import br.jus.pje.nucleo.entidades.ClasseJudicial;

public class ConsultaProcessoTrfDocumentoImpresso implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 19979921223377321L;
	private String numeroProcesso;
	private Date dataInicio;
	private ClasseJudicial classeJudicial;
	private Boolean inPesquisa = false;
	private String nomeParte;
	private String cpfCnpj;
	private String nomePessoaImpressao;
	private Date dataInicioImpressao;
	private Date dataFimImpressao;
	private Date dataInicioAtuacao;
	private Date dataFimAtuacao;
	private String tipoDocumento;

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public Boolean getInPesquisa() {
		return inPesquisa;
	}

	public void setInPesquisa(Boolean inPesquisa) {
		this.inPesquisa = inPesquisa;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getCpfCnpj() {
		return cpfCnpj;
	}

	public void setCpfCnpj(String cpfCnpj) {
		this.cpfCnpj = cpfCnpj;
	}

	public String getNomePessoaImpressao() {
		return nomePessoaImpressao;
	}

	public void setNomePessoaImpressao(String nomePessoaImpressao) {
		this.nomePessoaImpressao = nomePessoaImpressao;
	}

	public Date getDataInicioImpressao() {
		return dataInicioImpressao;
	}

	public void setDataInicioImpressao(Date dataInicioImpressao) {
		this.dataInicioImpressao = dataInicioImpressao;
	}

	public Date getDataFimImpressao() {
		return dataFimImpressao;
	}

	public void setDataFimImpressao(Date dataFimImpressao) {
		this.dataFimImpressao = dataFimImpressao;
	}

	public Date getDataInicioAtuacao() {
		return dataInicioAtuacao;
	}

	public void setDataInicioAtuacao(Date dataInicioAtuacao) {
		this.dataInicioAtuacao = dataInicioAtuacao;
	}

	public Date getDataFimAtuacao() {
		return dataFimAtuacao;
	}

	public void setDataFimAtuacao(Date dataFimAtuacao) {
		this.dataFimAtuacao = dataFimAtuacao;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

}
