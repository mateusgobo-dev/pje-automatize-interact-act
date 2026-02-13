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
package br.jus.pje.jt.entidades;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.nucleo.entidades.Sala;

/**
 * @author Rafael Carvalho
 * @since 1.2.0
 * @category PJE_JT
 */
@Entity
@Table(name = PeriodoDeInatividadeDaSala.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_periodo_inativo_sala", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_periodo_inativo_sala"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PeriodoDeInatividadeDaSala implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<PeriodoDeInatividadeDaSala
,Integer> {

	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "tb_periodo_inativo_sala";

	private Integer id;
	private Sala sala;
	private Date inicio;
	private Date termino;
	private Boolean ativo;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_periodo_inativo_sala")
	@Column(name = "id_periodo_inativo_sala", nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sala", nullable = false)
	@ForeignKey(name = "tb_periodo_inativo_sala_fkey")
	@NotNull
	public Sala getSala() {
		return sala;
	}

	public void setSala(Sala sala) {
		this.sala = sala;
	}

	@Column(name = "dt_inicio", nullable = false)
	@Temporal(TemporalType.DATE)
	@NotNull
	public Date getInicio() {
		return inicio;
	}

	public void setInicio(Date inicio) {
		this.inicio = inicio;
	}

	@Column(name = "dt_termino", nullable = true)
	@Temporal(TemporalType.DATE)
	public Date getTermino() {
		return termino;
	}

	public void setTermino(Date termino) {
		this.termino = termino;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Transient
	public Boolean getCanEditInicio() {
		if (id == null)
			return true;
		return new Date().before(inicio);
	}

	@Transient
	public Boolean getCanEditTermino() {
		if (termino == null)
			return true;
		return new Date().before(termino);
	}

	@Transient
	public Boolean getAbilitado() {
		return getCanEditInicio() || getCanEditTermino();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PeriodoDeInatividadeDaSala))
			return false;
		PeriodoDeInatividadeDaSala other = (PeriodoDeInatividadeDaSala) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PeriodoDeInatividadeDaSala> getEntityClass() {
		return PeriodoDeInatividadeDaSala.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
