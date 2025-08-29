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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_acompanh_cond_suspensao")
@org.hibernate.annotations.GenericGenerator(name = "gen_acomp_cond_susp", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_acompanh_cndco_suspensao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AcompanhamentoCondicaoSuspensao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<AcompanhamentoCondicaoSuspensao
,Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -650788792273707394L;
	private Integer id;
	private Integer numeroTarefa;
	private CondicaoSuspensaoAssociada condicaoSuspensaoAssociada;
	private Date dataPrevistaCumprimento;
	private Date dataCumprimento;
	private String observacao;
	private Boolean ativo = true;

	@Id
	@GeneratedValue(generator = "gen_acomp_cond_susp", strategy = GenerationType.AUTO)
	@Column(name = "id_acompanh_condicao_suspensao", unique = true, nullable = false)
	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id = id;
	}

	@Column(name = "nr_sequencia", nullable = false)
	@NotNull
	public Integer getNumeroTarefa(){
		return numeroTarefa;
	}

	public void setNumeroTarefa(Integer numeroTarefa){
		this.numeroTarefa = numeroTarefa;
	}

	public void setCondicaoSuspensaoAssociada(CondicaoSuspensaoAssociada condicaoSuspensaoAssociada){
		this.condicaoSuspensaoAssociada = condicaoSuspensaoAssociada;
	}

	@ManyToOne
	@JoinColumn(name = "id_condcao_suspensao_associada", nullable = false)
	public CondicaoSuspensaoAssociada getCondicaoSuspensaoAssociada(){
		return condicaoSuspensaoAssociada;
	}

	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "dt_prevista", nullable = false)
	public Date getDataPrevistaCumprimento(){
		return dataPrevistaCumprimento;
	}

	public void setDataPrevistaCumprimento(Date dataPrevistaCumprimento){
		this.dataPrevistaCumprimento = dataPrevistaCumprimento;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "dt_cumprimento")
	public Date getDataCumprimento(){
		return dataCumprimento;
	}

	public void setDataCumprimento(Date dataCumprimento){
		this.dataCumprimento = dataCumprimento;
	}

	@Column(name = "ds_obs", nullable = true)
	public String getObservacao(){
		return observacao;
	}

	public void setObservacao(String observacao){
		this.observacao = observacao;
	}

	@Column(name = "in_ativo", nullable = false)
	public Boolean getAtivo(){
		return ativo;
	}

	public void setAtivo(Boolean ativo){
		this.ativo = ativo;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime
			* result
			+ ((getCondicaoSuspensaoAssociada() == null) ? 0 : getCondicaoSuspensaoAssociada().hashCode());
		result = prime * result
			+ ((getDataCumprimento() == null) ? 0 : getDataCumprimento().hashCode());
		result = prime
			* result
			+ ((getDataPrevistaCumprimento() == null) ? 0
					: getDataPrevistaCumprimento().hashCode());
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result
			+ ((getNumeroTarefa() == null) ? 0 : getNumeroTarefa().hashCode());
		result = prime * result
			+ ((getObservacao() == null) ? 0 : getObservacao().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AcompanhamentoCondicaoSuspensao))
			return false;
		AcompanhamentoCondicaoSuspensao other = (AcompanhamentoCondicaoSuspensao) obj;
		if (getCondicaoSuspensaoAssociada() == null){
			if (other.getCondicaoSuspensaoAssociada() != null)
				return false;
		}
		else{
			if (!getCondicaoSuspensaoAssociada().equals(other.getCondicaoSuspensaoAssociada()))
				return false;
		}
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AcompanhamentoCondicaoSuspensao> getEntityClass() {
		return AcompanhamentoCondicaoSuspensao.class;
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
