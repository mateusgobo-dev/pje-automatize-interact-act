/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.SexoEnum;

@Entity
@Table(name = PessoaPush.TABLE_NAME)
public class PessoaPush implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_pessoa_push";
	private static final long serialVersionUID = 1L;

	private Integer idPessoaPush;
	private String nome;
	private String nrDocumento;
	private TipoDocumentoIdentificacao tipoDocumentoIdentificacao;
	private String email;
	private String cep;
	private String endereco;
	private String numeroEndereco;
	private String complemento;
	private String bairro;
	private Municipio municipio;
	private SexoEnum sexo;
	private String dddTelefone;
	private String telefone;
	private String senha;
	private CadastroTempPush cadastroTempPush;

	public PessoaPush() {
	}
	
	public PessoaPush(String nrDocumento, TipoDocumentoIdentificacao tipoDocumentoIdentificacao, String email) {
		this.nrDocumento = nrDocumento;
		this.tipoDocumentoIdentificacao = tipoDocumentoIdentificacao;
		this.email = email;
	}
	
	@Id
	@Column(name="id_pessoa_push")
	@GeneratedValue
	public Integer getIdPessoaPush() {
		return idPessoaPush;
	}

	public void setIdPessoaPush(Integer idPessoaPush) {
		this.idPessoaPush = idPessoaPush;
	}

	@Column(name = "ds_nome")
	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	@Column(name = "nr_documento")
	public String getNrDocumento() {
		return nrDocumento;
	}

	public void setNrDocumento(String nrDocumento) {
		this.nrDocumento = nrDocumento;
	}

	@ManyToOne
	@JoinColumn(name = "cd_tipo_doc_identificacao")
	public TipoDocumentoIdentificacao getTipoDocumentoIdentificacao() {
		return tipoDocumentoIdentificacao;
	}

	public void setTipoDocumentoIdentificacao(TipoDocumentoIdentificacao tipoDocumentoIdentificacao) {
		this.tipoDocumentoIdentificacao = tipoDocumentoIdentificacao;
	}

	@Column(name = "ds_email", nullable=false)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "nr_cep", nullable=false)
	public String getCep() {
		return cep;
	}

	public void setCep(String cep) {
		this.cep = cep;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_endereco", nullable=false)
	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	@Column(name = "ds_bairro", nullable=false)
	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	@ManyToOne
	@JoinColumn(name = "id_municipio", nullable=false)
	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}

	@Column(name = "in_sexo")
	@Enumerated(EnumType.STRING)
	public SexoEnum getSexo() {
		return sexo;
	}

	public void setSexo(SexoEnum sexo) {
		this.sexo = sexo;
	}

	@Column(name = "nr_ddd_telefone", nullable=false)
	public String getDddTelefone() {
		return dddTelefone;
	}

	public void setDddTelefone(String dddTelefone) {
		this.dddTelefone = dddTelefone;
	}

	@Column(name = "nr_telefone", nullable=false)
	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	@Column(name = "ds_senha", nullable=false)
	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}
	
	@Column(name = "nr_endereco", length = 15)
	@Length(max = 15)
	public String getNumeroEndereco() {
		return numeroEndereco;
	}

	public void setNumeroEndereco(String numeroEndereco) {
		this.numeroEndereco = numeroEndereco;
	}

	@Column(name = "ds_complemento", length = 100)
	@Length(max = 100)
	public String getComplemento() {
		return complemento;
	}

	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime + ((idPessoaPush == null) ? 0 : idPessoaPush.hashCode());
	}

 	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
 	@JoinColumn(name = "id_cadastro_temp_push", nullable = false)
	public CadastroTempPush getCadastroTempPush() {
		return cadastroTempPush;
	}

	public void setCadastroTempPush(CadastroTempPush cadastroTempPush) {
		this.cadastroTempPush = cadastroTempPush;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PessoaPush other = (PessoaPush) obj;
		if (idPessoaPush == null) {
			if (other.idPessoaPush != null)
				return false;
		} else if (!idPessoaPush.equals(other.idPessoaPush))
			return false;
		return true;
	}

}
