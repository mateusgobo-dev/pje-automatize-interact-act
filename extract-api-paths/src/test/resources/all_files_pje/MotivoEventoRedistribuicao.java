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
@Table(name = MotivoEventoRedistribuicao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_mot_ev_redistribuicao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_mot_ev_redistribuicao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class MotivoEventoRedistribuicao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<MotivoEventoRedistribuicao,Integer> {

	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "tb_motiv_ev_redistribuicao";

	private int idMotivoEventoRedistribuicao;
	private Evento eventoRedistribuicao;
	private MotivoRedistribuicao motivoRedistribuicao;

	public MotivoEventoRedistribuicao() {
	}

	@Id
	@GeneratedValue(generator = "gen_mot_ev_redistribuicao")
	@Column(name = "id_motivo_evnto_redistribuicao", unique = true, nullable = false)
	public int getIdMotivoEventoRedistribuicao() {
		return this.idMotivoEventoRedistribuicao;
	}

	public void setIdMotivoEventoRedistribuicao(int idMotivoEventoRedistribuicao) {
		this.idMotivoEventoRedistribuicao = idMotivoEventoRedistribuicao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_evento_redistribuicao")
	public Evento getEventoRedistribuicao() {
		return this.eventoRedistribuicao;
	}

	public void setEventoRedistribuicao(Evento eventoRedistribuicao) {
		this.eventoRedistribuicao = eventoRedistribuicao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_motivo_redistribuicao")
	public MotivoRedistribuicao getMotivoRedistribuicao() {
		return this.motivoRedistribuicao;
	}

	public void setMotivoRedistribuicao(MotivoRedistribuicao motivoRedistribuicao) {
		this.motivoRedistribuicao = motivoRedistribuicao;
	}

	@Override
	public String toString() {
		return eventoRedistribuicao.getEvento();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MotivoEventoRedistribuicao)) {
			return false;
		}
		MotivoEventoRedistribuicao other = (MotivoEventoRedistribuicao) obj;
		if (getIdMotivoEventoRedistribuicao() != other.getIdMotivoEventoRedistribuicao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdMotivoEventoRedistribuicao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends MotivoEventoRedistribuicao> getEntityClass() {
		return MotivoEventoRedistribuicao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdMotivoEventoRedistribuicao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
