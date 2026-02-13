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
@Table(name = "tb_med_protetva_pess_afast")
@org.hibernate.annotations.GenericGenerator(name = "gen_med_prot_pess_afastament", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_med_prot_pess_afastament"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class MedidaProtetivaPessoasAfastamento implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<MedidaProtetivaPessoasAfastamento,Integer> {

	private static final long serialVersionUID = 6895376482407237241L;
	private Integer id;
	private Integer distancia;
	private Pessoa pessoa;
	private MedidaProtetivaUrgencia medidaProtetivaUrgencia;
	private Boolean ativo = true;

	public MedidaProtetivaPessoasAfastamento(){
	}

	public MedidaProtetivaPessoasAfastamento(
			MedidaProtetivaUrgencia medidaProtetiva, Pessoa pessoa){
		this.setMedidaProtetivaUrgencia(medidaProtetiva);
		this.setPessoa(pessoa);
	}

	@Id
	@GeneratedValue(generator = "gen_med_prot_pess_afastament")
	@Column(name = "id_med_prot_pessoa_afastamento", unique = true, nullable = false)
	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id = id;
	}

	@Column(name = "distancia_afastamento")
	public Integer getDistancia(){
		return distancia;
	}

	public void setDistancia(Integer distancia){
		this.distancia = distancia;
	}

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_afastamento", nullable = false)
	public Pessoa getPessoa(){
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa){
		this.pessoa = pessoa;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_medida_protetiva_urgencia", nullable = false)
	public MedidaProtetivaUrgencia getMedidaProtetivaUrgencia(){
		return medidaProtetivaUrgencia;
	}

	public void setMedidaProtetivaUrgencia(
			MedidaProtetivaUrgencia medidaProtetivaUrgencia){
		this.medidaProtetivaUrgencia = medidaProtetivaUrgencia;
	}

	@Column(name = "in_ativo", nullable = false)
	public Boolean getAtivo(){
		return ativo;
	}

	public void setAtivo(Boolean inAtivo){
		this.ativo = inAtivo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ativo == null) ? 0 : ativo.hashCode());
		result = prime
				* result
				+ ((getMedidaProtetivaUrgencia() == null) ? 0
						: medidaProtetivaUrgencia.hashCode());
		result = prime * result + ((getPessoa() == null) ? 0 : pessoa.hashCode());
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
		MedidaProtetivaPessoasAfastamento other = (MedidaProtetivaPessoasAfastamento) obj;
		if (ativo == null) {
			if (other.ativo != null)
				return false;
		} else if (!ativo.equals(other.ativo))
			return false;
		if (getMedidaProtetivaUrgencia() == null) {
			if (other.getMedidaProtetivaUrgencia() != null)
				return false;
		} else if (!medidaProtetivaUrgencia
				.equals(other.getMedidaProtetivaUrgencia()))
			return false;
		if (getPessoa() == null) {
			if (other.getPessoa() != null)
				return false;
		} else if (!pessoa.equals(other.getPessoa()))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends MedidaProtetivaPessoasAfastamento> getEntityClass() {
		return MedidaProtetivaPessoasAfastamento.class;
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
