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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = PessoaOficialJustica.TABLE_NAME)
@SecondaryTables({ @SecondaryTable(name = "tb_usuario_login", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_usuario", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_pessoa", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_pessoa_fisica", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa_fisica", referencedColumnName = "id") }) })
public class PessoaOficialJustica extends PessoaFisicaEspecializada {

	public static final String TABLE_NAME = "tb_pessoa_oficial_justica";
	private static final long serialVersionUID = 1L;

	private String numeroMatricula;
	private Date dataPosse;
	private Boolean oficialJusticaAtivo;

	public PessoaOficialJustica() {
	}

	@Column(name = "nr_matricula", length = 15)
	@Length(max = 15)
	public String getNumeroMatricula() {
		return numeroMatricula;
	}

	public void setNumeroMatricula(String numeroMatricula) {
		this.numeroMatricula = numeroMatricula;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_posse")
	public Date getDataPosse() {
		return this.dataPosse;
	}

	public void setDataPosse(Date dataPosse) {
		this.dataPosse = dataPosse;
	}
	
	@Transient
	public Boolean getOficialJusticaAtivo() {
		if(this.oficialJusticaAtivo == null){
			this.oficialJusticaAtivo = (this.getPessoa().getEspecializacoes() & PessoaFisica.OFJ) == PessoaFisica.OFJ;
		}else{
			return this.oficialJusticaAtivo;
		}
		return oficialJusticaAtivo;
	}
	
	public void setOficialJusticaAtivo(Boolean oficialJusticaAtivo) {
		this.oficialJusticaAtivo = oficialJusticaAtivo;
	}

	@Transient
	@Override
	public Class<? extends PessoaFisicaEspecializada> getEntityClass() {
		return PessoaOficialJustica.class;
	}
}