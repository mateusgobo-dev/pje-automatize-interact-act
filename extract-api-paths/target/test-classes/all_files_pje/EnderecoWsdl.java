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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = EnderecoWsdl.TABLE_NAME)
public class EnderecoWsdl implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<EnderecoWsdl,Integer> {

	public static final String TABLE_NAME = "tb_endereco_wsdl";
	private static final long serialVersionUID = 1L;

	private int idEnderecoWsdl;
	private String wsdlIntercomunicacao;
	private String wsdlConsulta;
	private String descricao;
	private String instancia;
	private Boolean ativo = Boolean.TRUE;
	private String login;
	private String senha;
	private String dsServiceName;
	
	public EnderecoWsdl() {
	}

	@org.hibernate.annotations.GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_endereco_wsdl"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@Id
	@GeneratedValue(generator = "generator")
	@Column(name = "id_endereco_wsdl", unique = true, nullable = false)
	public int getIdEnderecoWsdl() {
		return idEnderecoWsdl;
	}

	public void setIdEnderecoWsdl(int idEnderecoWsdl) {
		this.idEnderecoWsdl = idEnderecoWsdl;
	}

	@Column(name = "ds_wsdl_intercomunicacao", length = 100)
	@Length(max = 100)
	public String getWsdlIntercomunicacao() {
		return wsdlIntercomunicacao;
	}

	public void setWsdlIntercomunicacao(String wsdlIntercomunicacao) {
		this.wsdlIntercomunicacao = wsdlIntercomunicacao;
	}

	@Column(name = "ds_wsdl_consulta", length = 100)
	@Length(max = 100)
	public String getWsdlConsulta() {
		return wsdlConsulta;
	}

	public void setWsdlConsulta(String wsdlConsulta) {
		this.wsdlConsulta = wsdlConsulta;
	}

	@Column(name = "ds_descricao", length = 100)
	@Length(max = 100)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name = "in_instancia")
	public String getInstancia() {
		return instancia;
	}

	public void setInstancia(String instancia) {
		this.instancia = instancia;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo(){
		return this.ativo;
	}

	public void setAtivo(Boolean ativo){
		this.ativo = ativo;
	}
	
	@Column(name = "ds_login", length = 100)
	@Length(max = 100)
	@NotNull
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	@Column(name = "ds_senha", length = 100)
	@Length(max = 100)
	@NotNull
	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}
	
	@Override
	public String toString() {
		return this.descricao;
	}

	@Column(name = "ds_service_name", length = 100)
	@Length(max = 100)
	public String getDsServiceName() {
		return dsServiceName;
	}

	public void setDsServiceName(String dsServiceName) {
		this.dsServiceName = dsServiceName;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof EnderecoWsdl)) {
			return false;
		}
		EnderecoWsdl other = (EnderecoWsdl) obj;
		if (getIdEnderecoWsdl() != other.getIdEnderecoWsdl()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdEnderecoWsdl();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends EnderecoWsdl> getEntityClass() {
		return EnderecoWsdl.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdEnderecoWsdl());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
