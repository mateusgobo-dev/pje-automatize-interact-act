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
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@javax.persistence.Cacheable(true)
@Cache(region = "FechamentoPautaSessaoJulgamento", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = SalaHorario.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_sala_horario", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_sala_horario"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class SalaHorario implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<SalaHorario,Integer> {

	private static final long serialVersionUID = -4264969707812005119L;

	public static final String TABLE_NAME = "tb_sala_horario";

	private int idSalaHorario;
	private Sala sala;
	private DiaSemana diaSemana;
	private Date horaInicial;
	private Date horaFinal;
	private Boolean ativo;

	private List<String> diaSemanaList = new ArrayList<String>(0);
	private Boolean selecionado;

	public SalaHorario() {
	}

	@Id
	@GeneratedValue(generator = "gen_sala_horario")
	@Column(name = "id_sala_horario", unique = true, nullable = false)
	public int getIdSalaHorario() {
		return this.idSalaHorario;
	}

	public void setIdSalaHorario(int idSalaHorario) {
		this.idSalaHorario = idSalaHorario;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_sala")
	public Sala getSala() {
		return this.sala;
	}

	public void setSala(Sala sala) {
		this.sala = sala;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_dia_semana", nullable = false)
	@NotNull
	public DiaSemana getDiaSemana() {
		return diaSemana;
	}

	public void setDiaSemana(DiaSemana diaSemana) {
		this.diaSemana = diaSemana;
	}

	@Column(name = "nr_hora_inicial", nullable = false)
	@Temporal(TemporalType.TIME)
	@NotNull
	public Date getHoraInicial() {
		return this.horaInicial;
	}

	public void setHoraInicial(Date horaInicial) {
		this.horaInicial = horaInicial;
	}

	@Column(name = "nr_hora_final", nullable = false)
	@Temporal(TemporalType.TIME)
	@NotNull
	public Date getHoraFinal() {
		return this.horaFinal;
	}

	public void setHoraFinal(Date horaFinal) {
		this.horaFinal = horaFinal;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Transient
	public List<String> getDiaSemanaList() {
		if (diaSemanaList == null) {
			diaSemanaList = new ArrayList<String>(0);
		}
		if (diaSemana != null) {
			if (diaSemana.getDiaSemana().equals("Domingo")) {
				diaSemanaList.add("[" + diaSemana.getDiaSemana());
			} else {
				if (diaSemana.getDiaSemana().equals("Sábado")) {
					diaSemanaList.add(" " + diaSemana.getDiaSemana() + "]");
				} else {
					diaSemanaList.add(" " + diaSemana.getDiaSemana());
				}
			}
		}
		return diaSemanaList;
	}

	public void setDiaSemanaList(List<String> diaSemanaList) {
		this.diaSemanaList = diaSemanaList;
	}

	@Transient
	public Boolean getSelecionado() {
		return selecionado;
	}

	public void setSelecionado(Boolean selecionado) {
		this.selecionado = selecionado;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SalaHorario)) {
			return false;
		}
		SalaHorario other = (SalaHorario) obj;
		if (getIdSalaHorario() != other.getIdSalaHorario()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdSalaHorario();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends SalaHorario> getEntityClass() {
		return SalaHorario.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdSalaHorario());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
