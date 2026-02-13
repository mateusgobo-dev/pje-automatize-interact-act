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
@Table(name = TarefaJbpm.TABLE_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = {
		"id_tarefa", "id_jbpm_task" }) })
@org.hibernate.annotations.GenericGenerator(name = "gen_tarefa_jbpm", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tarefa_jbpm"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TarefaJbpm implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TarefaJbpm,Integer> {

	public static final String TABLE_NAME = "tb_tarefa_jbpm";

	private static final long serialVersionUID = 1L;

	private int idTarefaJbpm;
	private Tarefa tarefa;
	private Long idJbpmTask;

	@Id
	@GeneratedValue(generator = "gen_tarefa_jbpm")
	@Column(name = "id_tarefa_jbpm", unique = true, nullable = false)
	public int getIdTarefaJbpm() {
		return idTarefaJbpm;
	}

	public void setIdTarefaJbpm(int idTarefaJbpm) {
		this.idTarefaJbpm = idTarefaJbpm;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tarefa", nullable = false)
	@NotNull
	public Tarefa getTarefa() {
		return tarefa;
	}

	public void setTarefa(Tarefa tarefa) {
		this.tarefa = tarefa;
	}

	@Column(name = "id_jbpm_task", nullable = false)
	@NotNull
	public Long getIdJbpmTask() {
		return idJbpmTask;
	}

	public void setIdJbpmTask(Long idJbpmTask) {
		this.idJbpmTask = idJbpmTask;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TarefaJbpm)) {
			return false;
		}
		TarefaJbpm other = (TarefaJbpm) obj;
		if (getIdTarefaJbpm() != other.getIdTarefaJbpm()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTarefaJbpm();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TarefaJbpm> getEntityClass() {
		return TarefaJbpm.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTarefaJbpm());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
