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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = Agrupamento.TABLE_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = { "ds_agrupamento" }) })
@org.hibernate.annotations.GenericGenerator(name = "gen_agrup", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_agrupamento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Agrupamento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Agrupamento,Integer> {

	public static final String TABLE_NAME = "tb_agrupamento";
	private static final long serialVersionUID = 1L;

	private int idAgrupamento;
	private String agrupamento;
	private Boolean ativo = Boolean.TRUE;
	private List<EventoAgrupamento> eventoAgrupamentoList = new ArrayList<EventoAgrupamento>(0);
	private List<TarefaEventoAgrupamento> agrupamentoTarefaList = new ArrayList<TarefaEventoAgrupamento>(0);

	@Id
	@GeneratedValue(generator = "gen_agrup")
	@Column(name = "id_agrupamento", unique = true, nullable = false)
	public int getIdAgrupamento() {
		return idAgrupamento;
	}

	public void setIdAgrupamento(int idAgrupamento) {
		this.idAgrupamento = idAgrupamento;
	}

	@Column(name = "ds_agrupamento", nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getAgrupamento() {
		return agrupamento;
	}

	public void setAgrupamento(String agrupamento) {
		this.agrupamento = agrupamento;
	}

	@Transient
	public String getAgrupamentoComId(){
		return getAgrupamento()+" ("+getIdAgrupamento()+")";
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "agrupamento")
	public List<EventoAgrupamento> getEventoAgrupamentoList() {
		return eventoAgrupamentoList;
	}

	public void setEventoAgrupamentoList(List<EventoAgrupamento> eventoAgrupamentoList) {
		this.eventoAgrupamentoList = eventoAgrupamentoList;
	}

	@Override
	public String toString() {
		return agrupamento;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "agrupamento")
	public List<TarefaEventoAgrupamento> getAgrupamentoTarefaList() {
		return agrupamentoTarefaList;
	}

	public void setAgrupamentoTarefaList(List<TarefaEventoAgrupamento> agrupamentoTarefaList) {
		this.agrupamentoTarefaList = agrupamentoTarefaList;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Agrupamento)) {
			return false;
		}
		Agrupamento other = (Agrupamento) obj;
		if (getIdAgrupamento() != other.getIdAgrupamento()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdAgrupamento();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Agrupamento> getEntityClass() {
		return Agrupamento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAgrupamento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
