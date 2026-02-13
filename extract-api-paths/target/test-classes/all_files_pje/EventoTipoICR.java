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
import javax.validation.constraints.NotNull;


@Entity
@Table(name = "tb_tipo_icr_evento")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_evento_icr", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_evento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class EventoTipoICR implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<EventoTipoICR,Integer> {

	private static final long serialVersionUID = 1L;

	private int idEventoTipoICR;
	private TipoInformacaoCriminalRelevante tipoInformacaoCriminalRelevante;
	private Evento evento;

	public EventoTipoICR() {
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_evento_icr")
	@Column(name = "id_tipo_icr_evento", unique = true, nullable = false)
	public int getIdEventoTipoICR() {
		return this.idEventoTipoICR;
	}

	public void setIdEventoTipoICR(int idEventoTipoICR) {
		this.idEventoTipoICR = idEventoTipoICR;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_icr", nullable = false)
	@NotNull
	public TipoInformacaoCriminalRelevante getTipoInformacaoCriminalRelevante() {
		return this.tipoInformacaoCriminalRelevante;
	}

	public void setTipoInformacaoCriminalRelevante(TipoInformacaoCriminalRelevante tipoInformacaoCriminalRelevante) {
		this.tipoInformacaoCriminalRelevante = tipoInformacaoCriminalRelevante;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_evento", nullable = false)
	@NotNull
	public Evento getEvento() {
		return this.evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoEvento)) {
			return false;
		}
		TipoEvento other = (TipoEvento) obj;
		if (getIdEventoTipoICR() != other.getIdTipoEvento()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdEventoTipoICR();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends EventoTipoICR> getEntityClass() {
		return EventoTipoICR.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdEventoTipoICR());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
