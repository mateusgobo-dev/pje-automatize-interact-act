package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import br.jus.pje.nucleo.enums.TipoPessoaEnum;

public class ProcessoParteDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private Integer idProcessoParte;
	private String nomeParte;
	private String TipoParte;
	private String documentoIdentificatorio;
	private List<String> filiacoes;
	private Date dataNascimento;
	private TipoPessoaEnum tipoPessoa;
	
	public ProcessoParteDTO() {
		super();
	}

	public ProcessoParteDTO(Integer idProcessoParte, String nomeParte, String tipoParte,
			String documentoIdentificatorio, List<String> filiacoes, Date dataNascimento, TipoPessoaEnum tipoPessoa) {
		super();
		this.idProcessoParte = idProcessoParte;
		this.nomeParte = nomeParte;
		TipoParte = tipoParte;
		this.documentoIdentificatorio = documentoIdentificatorio;
		this.filiacoes = filiacoes;
		this.dataNascimento = dataNascimento;
		this.tipoPessoa = tipoPessoa;
	}

	public Integer getIdProcessoParte() {
		return idProcessoParte;
	}
	
	public void setIdProcessoParte(Integer idProcessoParte) {
		this.idProcessoParte = idProcessoParte;
	}
	
	public String getNomeParte() {
		return nomeParte;
	}
	
	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}
	
	public String getTipoParte() {
		return TipoParte;
	}
	
	public void setTipoParte(String tipoParte) {
		TipoParte = tipoParte;
	}
	
	public String getDocumentoIdentificatorio() {
		return documentoIdentificatorio;
	}
	
	public void setDocumentoIdentificatorio(String documentoIdentificatorio) {
		this.documentoIdentificatorio = documentoIdentificatorio;
	}

	public List<String> getFiliacoes() {
		return filiacoes;
	}

	public void setFiliacoes(List<String> filiacoes) {
		this.filiacoes = filiacoes;
	}

	public Date getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}
	
	public TipoPessoaEnum getTipoPessoa() {
		return tipoPessoa;
	}
	
	public void setTipoPessoa(TipoPessoaEnum tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}
	
}
