package br.jus.cnj.pje.webservice.controller.cadastropartes.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.jus.pje.nucleo.entidades.Escolaridade;
import br.jus.pje.nucleo.entidades.EstadoCivil;
import br.jus.pje.nucleo.entidades.Etnia;
import br.jus.pje.nucleo.entidades.Pais;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.Profissao;
import br.jus.pje.nucleo.enums.SexoEnum;

public class PessoaFisicaDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long id;
	private String documentoPrincipal;
	private String nome;
	private String nomeMae;
	private String nomePai;
	private SexoEnum sexo;
	private String naturalidade;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dataNascimento;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dataObito;
	private Pais pais;
	private Etnia etnia;
	private EstadoCivil estadoCivil;
	private Escolaridade escolaridade;
	private Profissao profissao;
	private PessoaDocumentoIdentificacaoDTO documentoIdentificacao;
	private List<String> alcunhas = new ArrayList<>(0);
	private List<String> outrasFiliacoes = new ArrayList<>(0);
	private List<String> caracteristicasFisicas = new ArrayList<>(0);
	private String estado;
	private String codEstado;
	private List<String> outrosNomes = new ArrayList<>(0);
	private boolean incapaz = false;
	
	public PessoaFisicaDTO() {
		super();
	}

	public PessoaFisicaDTO(PessoaFisica pessoaFisica){
		super();
		
		if(pessoaFisica.getIdPessoa() != null){
			this.id = pessoaFisica.getIdPessoa().longValue();
		}
		
		this.documentoPrincipal = pessoaFisica.getDocumentoCpfCnpj();
		this.nome = pessoaFisica.getNome();
		this.nomeMae = pessoaFisica.getNomeGenitora();
		this.nomePai = pessoaFisica.getNomeGenitor();
		this.sexo = pessoaFisica.getSexo();
		this.dataNascimento = pessoaFisica.getDataNascimento();
		this.pais = pessoaFisica.getPaisNascimento();
		this.etnia = pessoaFisica.getEtnia();
		this.estadoCivil = pessoaFisica.getEstadoCivil();
		this.escolaridade = pessoaFisica.getEscolaridade();
		this.profissao = pessoaFisica.getProfissao();
		this.dataObito = pessoaFisica.getDataObito();
	}
	
	public PessoaFisicaDTO(Pessoa pessoa) {
		if(pessoa.getIdPessoa() != null){
			this.id = pessoa.getIdPessoa().longValue();
		}
		this.nome = pessoa.getNome();
		this.documentoPrincipal = pessoa.getDocumentoCpfCnpj();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDocumentoPrincipal() {
		return documentoPrincipal;
	}

	public void setDocumentoPrincipal(String documentoPrincipal) {
		this.documentoPrincipal = documentoPrincipal;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getNomeMae() {
		return nomeMae;
	}

	public void setNomeMae(String nomeMae) {
		this.nomeMae = nomeMae;
	}

	public String getNomePai() {
		return nomePai;
	}

	public void setNomePai(String nomePai) {
		this.nomePai = nomePai;
	}

	public SexoEnum getSexo() {
		return sexo;
	}

	public void setSexo(SexoEnum sexo) {
		this.sexo = sexo;
	}

	public Date getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public Pais getPais() {
		return pais;
	}

	public void setPais(Pais pais) {
		this.pais = pais;
	}

	public Etnia getEtnia() {
		return etnia;
	}

	public void setEtnia(Etnia etnia) {
		this.etnia = etnia;
	}

	public EstadoCivil getEstadoCivil() {
		return estadoCivil;
	}

	public void setEstadoCivil(EstadoCivil estadoCivil) {
		this.estadoCivil = estadoCivil;
	}

	public Escolaridade getEscolaridade() {
		return escolaridade;
	}

	public void setEscolaridade(Escolaridade escolaridade) {
		this.escolaridade = escolaridade;
	}

	public Profissao getProfissao() {
		return profissao;
	}

	public void setProfissao(Profissao profissao) {
		this.profissao = profissao;
	}
	
	public PessoaDocumentoIdentificacaoDTO getDocumentoIdentificacao() {
		return documentoIdentificacao;
	}
	
	public void setDocumentoIdentificacao(PessoaDocumentoIdentificacaoDTO documentoIdentificacao) {
		this.documentoIdentificacao = documentoIdentificacao;
	}

	public List<String> getAlcunhas() {
		return alcunhas;
	}

	public void setAlcunhas(List<String> alcunhas) {
		this.alcunhas = alcunhas;
	}

	public List<String> getOutrasFiliacoes() {
		return outrasFiliacoes;
	}

	public void setOutrasFiliacoes(List<String> outrasFiliacoes) {
		this.outrasFiliacoes = outrasFiliacoes;
	}

	public List<String> getCaracteristicasFisicas() {
		return caracteristicasFisicas;
	}

	public void setCaracteristicasFisicas(List<String> caracteristicasFisicas) {
		this.caracteristicasFisicas = caracteristicasFisicas;
	}

	public String getNaturalidade() {
		return naturalidade;
	}

	public void setNaturalidade(String naturalidade) {
		this.naturalidade = naturalidade;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public List<String> getOutrosNomes() {
		return outrosNomes;
	}

	public void setOutrosNomes(List<String> outrosNomes) {
		this.outrosNomes = outrosNomes;
	}
	
	public boolean isIncapaz() {
		return incapaz;
	}

	public void setIncapaz(boolean incapaz) {
		this.incapaz = incapaz;
	}

	public Date getDataObito() {
		return dataObito;
	}

	public void setDataObito(Date dataObito) {
		this.dataObito = dataObito;
	}

}
