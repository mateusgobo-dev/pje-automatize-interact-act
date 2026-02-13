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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "in_participacao", discriminatorType = DiscriminatorType.STRING)
@Table(name = "vs_aud_parte")
public class AudParte implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private AudPartePK pk;
	private int id;
	private int idProcessoParte;
	private int idProcesso;
	private Date dataAudiencia;
	private String nome;
	private Date dataNascimento;
	private String nomeGenitor;
	private String nomeGenitora;
	private String cpf;
	private String cnpj;
	private String rg;
	private String titulo_eleitor;
	private String nomeAdvogado;
	private String oab;
	private String uf;
	private String advCpf;

	public AudParte() {
		super();
	}

	@Column(name = "id_processo_audiencia", nullable = false, insertable = false, updatable = false)
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "id_processo_parte", nullable = false, insertable = false, updatable = false)
	public int getIdProcessoParte() {
		return idProcessoParte;
	}

	public void setIdProcessoParte(int idProcessoParte) {
		this.idProcessoParte = idProcessoParte;
	}

	@Column(name = "id_processo_trf", nullable = false, insertable = false, updatable = false)
	public int getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(int idProcesso) {
		this.idProcesso = idProcesso;
	}

	@Column(name = "ds_autor", insertable = false, updatable = false)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "dt_inicio", insertable = false, updatable = false)
	public Date getDataAudiencia() {
		return dataAudiencia;
	}

	public void setDataAudiencia(Date dataAudiencia) {
		this.dataAudiencia = dataAudiencia;
	}

	@Column(name = "dt_nascimento", insertable = false, updatable = false)
	public Date getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	@Column(name = "nm_genitor", insertable = false, updatable = false)
	public String getNomeGenitor() {
		return nomeGenitor;
	}

	public void setNomeGenitor(String nomeGenitor) {
		this.nomeGenitor = nomeGenitor;
	}

	@Column(name = "nm_genitora", insertable = false, updatable = false)
	public String getNomeGenitora() {
		return nomeGenitora;
	}

	public void setNomeGenitora(String nomeGenitora) {
		this.nomeGenitora = nomeGenitora;
	}

	@Column(name = "cpf", insertable = false, updatable = false)
	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
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

	@Column(name = "ds_advogado", insertable = false, updatable = false)
	public String getNomeAdvogado() {
		return nomeAdvogado;
	}

	public void setNomeAdvogado(String nomeAdvogado) {
		this.nomeAdvogado = nomeAdvogado;
	}

	@Column(name = "nr_oab", insertable = false, updatable = false)
	public String getOab() {
		return oab;
	}

	public void setOab(String oab) {
		this.oab = oab;
	}

	@Column(name = "cd_estado", insertable = false, updatable = false)
	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

	@Column(name = "adv_cpf", insertable = false, updatable = false)
	public String getAdvCpf() {
		return advCpf;
	}

	public void setAdvCpf(String advCpf) {
		this.advCpf = advCpf;
	}

	@EmbeddedId
	@XmlTransient
	public AudPartePK getPk() {
		return pk;
	}

	public void setPk(AudPartePK pk) {
		this.pk = pk;
	}

	
}