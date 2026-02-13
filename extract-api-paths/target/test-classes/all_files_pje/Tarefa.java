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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = Tarefa.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_tarefa", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tarefa"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Tarefa implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Tarefa,Integer> {

	public static final String TABLE_NAME = "tb_tarefa";

	private static final long serialVersionUID = 1L;

	private int idTarefa;
	private String tarefa;
	private Fluxo fluxo;
	private List<TarefaJbpm> tarefaJbpmList = new ArrayList<TarefaJbpm>(0);
	private List<Caixa> caixaList = new ArrayList<Caixa>(0);
	private List<TarefaEvento> tarefaEventoList = new ArrayList<TarefaEvento>(0);

	@Id
	@GeneratedValue(generator = "gen_tarefa")
	@Column(name = "id_tarefa", unique = true, nullable = false)
	public int getIdTarefa() {
		return idTarefa;
	}

	public void setIdTarefa(int idTarefa) {
		this.idTarefa = idTarefa;
	}

	@Column(name = "ds_tarefa", nullable = false, length = 150, unique = true)
	@NotNull
	@Length(max = 150)
	public String getTarefa() {
		return tarefa;
	}

	public void setTarefa(String tarefa) {
		this.tarefa = tarefa;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_fluxo", nullable = false)
	public Fluxo getFluxo() {
		return fluxo;
	}

	public void setFluxo(Fluxo fluxo) {
		this.fluxo = fluxo;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tarefa")
	@OrderBy("idJbpmTask DESC")
	public List<TarefaJbpm> getTarefaJbpmList() {
		return tarefaJbpmList;
	}

	public void setTarefaJbpmList(List<TarefaJbpm> tarefaJbpmList) {
		this.tarefaJbpmList = tarefaJbpmList;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tarefa")
	public List<Caixa> getCaixaList() {
		return caixaList;
	}

	public void setCaixaList(List<Caixa> caixaList) {
		this.caixaList = caixaList;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tarefa")
	public List<TarefaEvento> getTarefaEventoList() {
		return tarefaEventoList;
	}

	public void setTarefaEventoList(List<TarefaEvento> tarefaEventoList) {
		this.tarefaEventoList = tarefaEventoList;
	}

	@Transient
	public Long getLastIdJbpmTask() {
		if ((tarefaJbpmList == null) || (tarefaJbpmList.size() == 0)) {
			return null;
		}
		return tarefaJbpmList.get(0).getIdJbpmTask();
	}

	@Override
	public String toString() {
		return tarefa;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTarefa();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Tarefa))
			return false;
		Tarefa other = (Tarefa) obj;
		if (getIdTarefa() != other.getIdTarefa())
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Tarefa> getEntityClass() {
		return Tarefa.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTarefa());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
