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
@Table(name = TarefaTransicaoEventoAgrupamento.TABLE_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = {
		"id_agrupamento", "id_tarefa_transicao_evento" }) })
@org.hibernate.annotations.GenericGenerator(name = "gen_trfa_trnsco_ev_agrpmento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_trfa_trnsco_ev_agrpmento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TarefaTransicaoEventoAgrupamento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TarefaTransicaoEventoAgrupamento,Integer> {

	public static final String TABLE_NAME = "tb_tarefa_trans_even_agrup";
	private static final long serialVersionUID = 1L;

	private int idTarefaTransicaoEventoAgrupamento;
	private Agrupamento agrupamento;
	private TarefaTransicaoEvento tarefaTransicaoEvento;

	@Id
	@GeneratedValue(generator = "gen_trfa_trnsco_ev_agrpmento")
	@Column(name = "id_tarfa_trnsco_ev_agrupamento", unique = true, nullable = false)
	public int getIdTarefaTransicaoEventoAgrupamento() {
		return idTarefaTransicaoEventoAgrupamento;
	}

	public void setIdTarefaTransicaoEventoAgrupamento(int idTarefaTransicaoEventoAgrupamento) {
		this.idTarefaTransicaoEventoAgrupamento = idTarefaTransicaoEventoAgrupamento;
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
	@JoinColumn(name = "id_tarefa_transicao_evento", nullable = false)
	@NotNull
	public TarefaTransicaoEvento getTarefaTransicaoEvento() {
		return tarefaTransicaoEvento;
	}

	public void setTarefaTransicaoEvento(TarefaTransicaoEvento tarefaTransicaoEvento) {
		this.tarefaTransicaoEvento = tarefaTransicaoEvento;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TarefaTransicaoEventoAgrupamento)) {
			return false;
		}
		TarefaTransicaoEventoAgrupamento other = (TarefaTransicaoEventoAgrupamento) obj;
		if (getIdTarefaTransicaoEventoAgrupamento() != other.getIdTarefaTransicaoEventoAgrupamento()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTarefaTransicaoEventoAgrupamento();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TarefaTransicaoEventoAgrupamento> getEntityClass() {
		return TarefaTransicaoEventoAgrupamento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTarefaTransicaoEventoAgrupamento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
