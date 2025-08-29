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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tb_medcaut_dvrsapess_afast")
@org.hibernate.annotations.GenericGenerator(name = "gen_med_cat_dvrsa_pess_afast", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_med_cat_dvrsa_pess_afast"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class MedidaCautelarPessoaAfastamento implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<MedidaCautelarPessoaAfastamento,Integer>{

	private static final long serialVersionUID = 8860755962803385665L;
	private Integer id;
	private MedidaCautelarDiversa medidaCautelarDiversa;
	private Pessoa pessoaAfastemento;
	private Integer distanciaAfastamento;
	private Boolean pessoaCadastrada = true;
	private Boolean ativo = true;
	private String nomePessoaNaoCadastrada;

	public MedidaCautelarPessoaAfastamento(){

	}

	public MedidaCautelarPessoaAfastamento(MedidaCautelarDiversa medidaCautelarDiversa, Pessoa pessoaAfastemento){
		this.medidaCautelarDiversa = medidaCautelarDiversa;
		this.pessoaAfastemento = pessoaAfastemento;
	}

	public MedidaCautelarPessoaAfastamento(MedidaCautelarDiversa medidaCautelarDiversa, String nomePessoaNaoCadastrada){
		this.medidaCautelarDiversa = medidaCautelarDiversa;
		this.nomePessoaNaoCadastrada = nomePessoaNaoCadastrada;
		this.pessoaCadastrada = false;
	}

	@Id
	@GeneratedValue(generator = "gen_med_cat_dvrsa_pess_afast")
	@Column(name = "id_med_caut_dvrsa_pess_aftmnto", unique = true, nullable = false)
	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_medida_cautelar_diversa", nullable = false)
	public MedidaCautelarDiversa getMedidaCautelarDiversa(){
		return medidaCautelarDiversa;
	}

	public void setMedidaCautelarDiversa(MedidaCautelarDiversa medidaCautelarDiversa){
		this.medidaCautelarDiversa = medidaCautelarDiversa;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "id_pessoa_afastamento", nullable = false)
	public Pessoa getPessoaAfastemento(){
		return pessoaAfastemento;
	}

	public void setPessoaAfastemento(Pessoa pessoaAfastemento){
		this.pessoaAfastemento = pessoaAfastemento;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaAfastemento(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoaAfastemento(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaAfastemento(pessoa.getPessoa());
		} else {
			setPessoaAfastemento((Pessoa)null);
		}
	}

	@Column(name = "in_pessoa_cadastrada", nullable = false)
	public Boolean getPessoaCadastrada(){
		return pessoaCadastrada;
	}

	public void setPessoaCadastrada(Boolean pessoaCadastrada){
		this.pessoaCadastrada = pessoaCadastrada;
	}

	@Column(name = "nome_pessoa_nao_cadastrada")
	public String getNomePessoaNaoCadastrada(){
		return nomePessoaNaoCadastrada;
	}

	public void setNomePessoaNaoCadastrada(String nomePessoaNaoCadastrada){
		this.nomePessoaNaoCadastrada = nomePessoaNaoCadastrada;
	}

	@Column(name = "in_ativo", nullable = false)
	public Boolean getAtivo(){
		return ativo;
	}

	public void setAtivo(Boolean ativo){
		this.ativo = ativo;
	}

	@Column(name = "distancia_afastamento", nullable = false)
	public Integer getDistanciaAfastamento(){
		return distanciaAfastamento;
	}

	public void setDistanciaAfastamento(Integer distanciaAfastamento){
		this.distanciaAfastamento = distanciaAfastamento;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ativo == null) ? 0 : ativo.hashCode());
		result = prime
				* result
				+ ((getMedidaCautelarDiversa() == null) ? 0 : medidaCautelarDiversa
						.hashCode());
		result = prime
				* result
				+ ((getPessoaAfastemento() == null) ? 0 : pessoaAfastemento
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MedidaCautelarPessoaAfastamento other = (MedidaCautelarPessoaAfastamento) obj;
		if (ativo == null) {
			if (other.ativo != null)
				return false;
		} else if (!ativo.equals(other.ativo))
			return false;
		if (getMedidaCautelarDiversa() == null) {
			if (other.getMedidaCautelarDiversa() != null)
				return false;
		} else if (!medidaCautelarDiversa.equals(other.medidaCautelarDiversa))
			return false;
		if (getPessoaAfastemento() == null) {
			if (other.getPessoaAfastemento() != null)
				return false;
		} else if (!pessoaAfastemento.equals(other.pessoaAfastemento))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends MedidaCautelarPessoaAfastamento> getEntityClass() {
		return MedidaCautelarPessoaAfastamento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
