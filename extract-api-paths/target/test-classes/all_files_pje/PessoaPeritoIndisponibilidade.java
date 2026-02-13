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

import java.text.MessageFormat;
import java.util.Date;

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
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_pess_perito_indisponibi")
@org.hibernate.annotations.GenericGenerator(name = "gen_pess_prto_indspnbilidade", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pess_prto_indspnbilidade"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PessoaPeritoIndisponibilidade implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaPeritoIndisponibilidade,Integer> {

	private static final long serialVersionUID = 1L;

	private int idPeritoIndisponibilidade;
	private PessoaPeritoEspecialidade pessoaPeritoEspecialidade;
	private String indisponibilidade;
	private Date dtInicio;
	private Date dtFim;
	private Date dtCadastro;
	private Boolean ativo = true;
	private Date horaInicio;
	private Date horaFim;

	public PessoaPeritoIndisponibilidade() {
	}

	@Id
	@GeneratedValue(generator = "gen_pess_prto_indspnbilidade")
	@Column(name = "id_perito_indisponibilidade", unique = true, nullable = false)
	public int getIdPeritoIndisponibilidade() {
		return idPeritoIndisponibilidade;
	}

	public void setIdPeritoIndisponibilidade(int idPeritoIndisponibilidade) {
		this.idPeritoIndisponibilidade = idPeritoIndisponibilidade;
	}

	@Column(name = "ds_indisponibilidade", length = 100)
	@Length(max = 100)
	public String getIndisponibilidade() {
		return indisponibilidade;
	}

	public void setIndisponibilidade(String indisponibilidade) {
		this.indisponibilidade = indisponibilidade;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicio")
	public Date getDtInicio() {
		return dtInicio;
	}

	public void setDtInicio(Date dtInicio) {
		this.dtInicio = dtInicio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fim")
	public Date getDtFim() {
		return dtFim;
	}

	public void setDtFim(Date dtFim) {
		this.dtFim = dtFim;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_cadastro")
	public Date getDtCadastro() {
		return dtCadastro;
	}

	public void setDtCadastro(Date dtCadastro) {
		this.dtCadastro = dtCadastro;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pessoa_perito_especialidade")
	public PessoaPeritoEspecialidade getPessoaPeritoEspecialidade() {
		return pessoaPeritoEspecialidade;
	}

	public void setPessoaPeritoEspecialidade(PessoaPeritoEspecialidade pessoaPeritoEspecialidade) {
		this.pessoaPeritoEspecialidade = pessoaPeritoEspecialidade;
	}

	@Column(name = "hr_inicio")
	@Temporal(TemporalType.TIME)
	public Date getHoraInicio() {
		return horaInicio;
	}

	public void setHoraInicio(Date horaInicio) {
		this.horaInicio = horaInicio;
	}

	@Column(name = "hr_fim")
	@Temporal(TemporalType.TIME)
	public Date getHoraFim() {
		return horaFim;
	}

	public void setHoraFim(Date horaFim) {
		this.horaFim = horaFim;
	}

	@Override
	public String toString() {
		return MessageFormat.format("{0} de {1,date,short} {2,time,short} a {3,date,short} {4,time,short}: {5}",
				pessoaPeritoEspecialidade, dtInicio, horaInicio, dtFim, horaFim, getIndisponibilidade());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PessoaPeritoIndisponibilidade)) {
			return false;
		}
		PessoaPeritoIndisponibilidade other = (PessoaPeritoIndisponibilidade) obj;
		if (getIdPeritoIndisponibilidade() != other.getIdPeritoIndisponibilidade()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPeritoIndisponibilidade();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaPeritoIndisponibilidade> getEntityClass() {
		return PessoaPeritoIndisponibilidade.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPeritoIndisponibilidade());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
