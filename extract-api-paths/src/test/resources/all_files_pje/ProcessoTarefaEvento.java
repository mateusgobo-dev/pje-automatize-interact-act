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

@Entity
@Table(name = ProcessoTarefaEvento.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_tarefa_evento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_tarefa_evento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoTarefaEvento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoTarefaEvento,Integer> {

	public static final String TABLE_NAME = "tb_processo_tarefa_evento";
	private static final long serialVersionUID = 1L;

	private int idProcessoTarefaEvento;
	private TarefaEvento tarefaEvento;
	private Processo processo;
	private Boolean registrado;

	@Id
	@GeneratedValue(generator = "gen_processo_tarefa_evento")
	@Column(name = "id_processo_tarefa_evento", unique = true, nullable = false)
	public int getIdProcessoTarefaEvento() {
		return idProcessoTarefaEvento;
	}

	public void setIdProcessoTarefaEvento(int idProcessoTarefaEvento) {
		this.idProcessoTarefaEvento = idProcessoTarefaEvento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tarefa_evento", nullable = false)
	public TarefaEvento getTarefaEvento() {
		return tarefaEvento;
	}

	public void setTarefaEvento(TarefaEvento tarefaEvento) {
		this.tarefaEvento = tarefaEvento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo", nullable = false)
	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	@Column(name = "in_registrado")
	public Boolean getRegistrado() {
		return registrado;
	}

	public void setRegistrado(Boolean registrado) {
		this.registrado = registrado;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoTarefaEvento)) {
			return false;
		}
		ProcessoTarefaEvento other = (ProcessoTarefaEvento) obj;
		if (getIdProcessoTarefaEvento() != other.getIdProcessoTarefaEvento()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoTarefaEvento();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoTarefaEvento> getEntityClass() {
		return ProcessoTarefaEvento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoTarefaEvento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
