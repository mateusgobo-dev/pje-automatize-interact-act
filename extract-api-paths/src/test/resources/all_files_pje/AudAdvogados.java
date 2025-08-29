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
package br.jus.pje.jt.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "vs_aud_advogados")
public class AudAdvogados implements Serializable {

	private static final long serialVersionUID = 1L;

	private AudAdvogadoPK pk;
	private String nome;
	private String uf;
	private String cnpj;
	private String rg;
	private String titulo_eleitor;
	private String oab;
	private String cpf;
	
	@EmbeddedId
	@XmlTransient
	public AudAdvogadoPK getPk() {
		return pk;
	}

	public void setPk(AudAdvogadoPK pk) {
		this.pk = pk;
	}
	
	@Column(name = "nr_oab", insertable = false, updatable = false)
	public String getOab() {
		return oab;
	}

	public void setOab(String oab) {
		this.oab = oab;
	}
	
	@Column(name = "cpf", insertable = false, updatable = false)
	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}
	
	@Column(name = "ds_nome", insertable = false, updatable = false)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "cd_estado", insertable = false, updatable = false)
	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

	@Column(name = "cnpj", insertable = false, updatable = false)
	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	@Column(name = "rg", insertable = false, updatable = false)
	public String getRg() {
		return rg;
	}

	public void setRg(String rg) {
		this.rg = rg;
	}

	@Column(name = "titulo_eleitor", insertable = false, updatable = false)
	public String getTitulo_eleitor() {
		return titulo_eleitor;
	}

	public void setTitulo_eleitor(String titulo_eleitor) {
		this.titulo_eleitor = titulo_eleitor;
	}

}
