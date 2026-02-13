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
@Table(name = EventoAgrupamento.TABLE_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = {
		"id_evento", "id_agrupamento" }) })
@org.hibernate.annotations.GenericGenerator(name = "gen_evento_agrupamento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_evento_agrupamento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class EventoAgrupamento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<EventoAgrupamento,Integer> {

	public static final String TABLE_NAME = "tb_evento_agrupamento";
	private static final long serialVersionUID = 1L;

	private int idEventoAgrupamento;
	private Evento evento;
	private Agrupamento agrupamento;
	private Boolean multiplo;

	@Id
	@GeneratedValue(generator = "gen_evento_agrupamento")
	@Column(name = "id_evento_agrupamento", unique = true, nullable = false)
	public int getIdEventoAgrupamento() {
		return idEventoAgrupamento;
	}

	public void setIdEventoAgrupamento(int idEventoAgrupamento) {
		this.idEventoAgrupamento = idEventoAgrupamento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_evento", nullable = false)
	@NotNull
	public Evento getEvento() {
		return evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
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

	@Override
	public String toString() {
		return evento.toString();
	}

	@Column(name = "in_multiplo", nullable = false)
	@NotNull
	public Boolean getMultiplo() {
		return multiplo;
	}

	public void setMultiplo(Boolean multiplo) {
		this.multiplo = multiplo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof EventoAgrupamento)) {
			return false;
		}
		EventoAgrupamento other = (EventoAgrupamento) obj;
		if (getIdEventoAgrupamento() != other.getIdEventoAgrupamento()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdEventoAgrupamento();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends EventoAgrupamento> getEntityClass() {
		return EventoAgrupamento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdEventoAgrupamento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
