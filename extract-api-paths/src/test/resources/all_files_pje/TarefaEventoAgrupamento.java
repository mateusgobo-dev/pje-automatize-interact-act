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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = TarefaEventoAgrupamento.TABLE_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = {
		"id_agrupamento", "id_tarefa_evento" }) })
@org.hibernate.annotations.GenericGenerator(name = "gen_tarefa_evnto_agrupamento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tarefa_evnto_agrupamento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TarefaEventoAgrupamento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TarefaEventoAgrupamento,Integer> {

	public static final String TABLE_NAME = "tb_tarefa_even_agrupamento";
	private static final long serialVersionUID = 1L;

	private int idTarefaEventoAgrupamento;
	private Agrupamento agrupamento;
	private TarefaEvento tarefaEvento;

	@Id
	@GeneratedValue(generator = "gen_tarefa_evnto_agrupamento")
	@Column(name = "id_tarefa_evento_agrupamento", unique = true, nullable = false)
	public int getIdTarefaEventoAgrupamento() {
		return idTarefaEventoAgrupamento;
	}

	public void setIdTarefaEventoAgrupamento(int idTarefaEventoAgrupamento) {
		this.idTarefaEventoAgrupamento = idTarefaEventoAgrupamento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_agrupamento", nullable = false)
	@NotNull
	public Agrupamento getAgrupamento() {
		return agrupamento;
	}

	public void setAgrupamento(Agrupamento agrupamento) {
		this.agrupamento = agrupamento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tarefa_evento", nullable = false)
	@NotNull
	public TarefaEvento getTarefaEvento() {
		return tarefaEvento;
	}

	public void setTarefaEvento(TarefaEvento tarefaEvento) {
		this.tarefaEvento = tarefaEvento;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TarefaEventoAgrupamento)) {
			return false;
		}
		TarefaEventoAgrupamento other = (TarefaEventoAgrupamento) obj;
		if (getIdTarefaEventoAgrupamento() != other.getIdTarefaEventoAgrupamento()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTarefaEventoAgrupamento();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TarefaEventoAgrupamento> getEntityClass() {
		return TarefaEventoAgrupamento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTarefaEventoAgrupamento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
