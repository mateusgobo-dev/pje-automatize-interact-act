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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import br.jus.pje.nucleo.enums.TarefaEventoEnum;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = TarefaEvento.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_tarefa_evento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tarefa_evento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TarefaEvento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TarefaEvento,Integer> {

	public static final String TABLE_NAME = "tb_tarefa_evento";
	private static final long serialVersionUID = 1L;

	private int idTarefaEvento;
	private Tarefa tarefa;
	private Tarefa tarefaOrigem;
	private TarefaEventoEnum evento;
	private List<TarefaEventoAgrupamento> tarefaEventoAgrupamentoList = new ArrayList<TarefaEventoAgrupamento>(0);
	private List<ProcessoTarefaEvento> processoTarefaEventoList = new ArrayList<ProcessoTarefaEvento>(0);

	@Id
	@GeneratedValue(generator = "gen_tarefa_evento")
	@Column(name = "id_tarefa_evento", unique = true, nullable = false)
	public int getIdTarefaEvento() {
		return idTarefaEvento;
	}

	public void setIdTarefaEvento(int idTarefaEvento) {
		this.idTarefaEvento = idTarefaEvento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tarefa", nullable = false)
	public Tarefa getTarefa() {
		return tarefa;
	}

	public void setTarefa(Tarefa tarefa) {
		this.tarefa = tarefa;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tarefa_origem")
	public Tarefa getTarefaOrigem() {
		return tarefaOrigem;
	}

	public void setTarefaOrigem(Tarefa tarefaOrigem) {
		this.tarefaOrigem = tarefaOrigem;
	}

	@Column(name = "in_evento", length = 2)
	@Enumerated(EnumType.STRING)
	public TarefaEventoEnum getEvento() {
		return evento;
	}

	public void setEvento(TarefaEventoEnum evento) {
		this.evento = evento;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tarefaEvento", cascade = { CascadeType.REMOVE, CascadeType.PERSIST,
			CascadeType.MERGE })
	public List<TarefaEventoAgrupamento> getTarefaEventoAgrupamentoList() {
		return tarefaEventoAgrupamentoList;
	}

	public void setTarefaEventoAgrupamentoList(List<TarefaEventoAgrupamento> tarefaEventoAgrupamentoList) {
		this.tarefaEventoAgrupamentoList = tarefaEventoAgrupamentoList;
	}

	@Override
	public String toString() {
		return evento.getLabel();
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tarefaEvento", cascade = { CascadeType.PERSIST, CascadeType.MERGE,
			CascadeType.REMOVE })
	public List<ProcessoTarefaEvento> getProcessoTarefaEventoList() {
		return processoTarefaEventoList;
	}

	public void setProcessoTarefaEventoList(List<ProcessoTarefaEvento> processoTarefaEventoList) {
		this.processoTarefaEventoList = processoTarefaEventoList;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TarefaEvento)) {
			return false;
		}
		TarefaEvento other = (TarefaEvento) obj;
		if (getIdTarefaEvento() != other.getIdTarefaEvento()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTarefaEvento();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TarefaEvento> getEntityClass() {
		return TarefaEvento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTarefaEvento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
