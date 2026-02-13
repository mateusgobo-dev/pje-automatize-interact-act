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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_faixa_cep")
@org.hibernate.annotations.GenericGenerator(name = "gen_faixa_cep", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_faixa_cep"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class FaixaCep implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<FaixaCep,Integer> {

	private static final long serialVersionUID = 4799679130550717675L;
	private Integer idFaixaCep;
	private Long cepInicial;
	private Long cepFinal;
	private Bairro bairro;

	@Id
	@GeneratedValue(generator = "gen_faixa_cep")
	@Column(name = "id_faixa_cep")
	public Integer getIdFaixaCep() {
		return idFaixaCep;
	}

	public void setIdFaixaCep(Integer idFaixaCep) {
		this.idFaixaCep = idFaixaCep;
	}

	@Column(name = "nr_cep_inicial")
	@NotNull
	public Long getCepInicial() {
		return cepInicial;
	}

	public void setCepInicial(Long cepInicial) {
		this.cepInicial = cepInicial;
	}

	@Column(name = "nr_cep_final")
	@NotNull
	public Long getCepFinal() {
		return cepFinal;
	}

	public void setCepFinal(Long cepFinal) {
		this.cepFinal = cepFinal;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_bairro")
	public Bairro getBairro() {
		return bairro;
	}

	public void setBairro(Bairro bairro) {
		this.bairro = bairro;
	}

	@Override
	public String toString() {
		return this.cepInicial + " -> " + this.cepFinal;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends FaixaCep> getEntityClass() {
		return FaixaCep.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdFaixaCep();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
