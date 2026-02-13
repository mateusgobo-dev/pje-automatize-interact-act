package br.jus.cnj.pje.webservice.controller.cadastropartes.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;

public class EnderecoDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long idEndereco;
	private Cep cep;
	private Long idPessoa;
	private Long idCadastrador;
	private String nomeLogradouro;
	private String nomeBairro;
	private String nomeCidade;
	private String nomeEstado;
	private String numeroEndereco;
	private String complemento;
	private Boolean correspondencia;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dataAlteracao;
	
	public EnderecoDTO() {
		super();
	}
	
	public EnderecoDTO(Endereco endereco) {
		this.idEndereco = Long.valueOf(endereco.getIdEndereco());
		this.cep = endereco.getCep();
		if(endereco.getUsuario() != null && endereco.getUsuario().getIdUsuario() != null){
			this.idPessoa =  Long.valueOf(endereco.getUsuario().getIdUsuario());			
		}
		if(endereco.getUsuarioCadastrador() != null && endereco.getUsuarioCadastrador().getIdUsuario() != null){
			this.idCadastrador = Long.valueOf(endereco.getUsuarioCadastrador().getIdUsuario());
		}
		this.nomeLogradouro = endereco.getNomeLogradouro();
		this.nomeBairro = endereco.getNomeBairro();
		this.nomeCidade = endereco.getNomeCidade();
		this.nomeEstado = endereco.getNomeEstado();
		this.numeroEndereco = endereco.getNumeroEndereco();
		this.complemento = endereco.getComplemento();
		this.correspondencia = endereco.getCorrespondencia();
		this.dataAlteracao = endereco.getDataAlteracao();
	}

	public Long getIdEndereco() {
		return idEndereco;
	}

	public void setIdEndereco(Long idEndereco) {
		this.idEndereco = idEndereco;
	}

	public Cep getCep() {
		return cep;
	}

	public void setCep(Cep cep) {
		this.cep = cep;
	}

	public Long getIdPessoa() {
		return idPessoa;
	}

	public void setIdPessoa(Long idPessoa) {
		this.idPessoa = idPessoa;
	}

	public Long getIdCadastrador() {
		return idCadastrador;
	}

	public void setIdCadastrador(Long idCadastrador) {
		this.idCadastrador = idCadastrador;
	}

	public String getNomeLogradouro() {
		return nomeLogradouro;
	}

	public void setNomeLogradouro(String nomeLogradouro) {
		this.nomeLogradouro = nomeLogradouro;
	}

	public String getNomeBairro() {
		return nomeBairro;
	}

	public void setNomeBairro(String nomeBairro) {
		this.nomeBairro = nomeBairro;
	}

	public String getNomeCidade() {
		return nomeCidade;
	}

	public void setNomeCidade(String nomeCidade) {
		this.nomeCidade = nomeCidade;
	}

	public String getNomeEstado() {
		return nomeEstado;
	}

	public void setNomeEstado(String nomeEstado) {
		this.nomeEstado = nomeEstado;
	}

	public String getNumeroEndereco() {
		return numeroEndereco;
	}

	public void setNumeroEndereco(String numeroEndereco) {
		this.numeroEndereco = numeroEndereco;
	}

	public String getComplemento() {
		return complemento;
	}

	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

	public Boolean getCorrespondencia() {
		return correspondencia;
	}

	public void setCorrespondencia(Boolean correspondencia) {
		this.correspondencia = correspondencia;
	}

	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}
}
