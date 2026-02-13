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

import java.sql.Time;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.SemanaEnum;

@Entity
@Table(name = "tb_pess_perito_disponibili")
@org.hibernate.annotations.GenericGenerator(name = "gen_pess_prto_dspnbilidade", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pess_prto_dspnbilidade"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PessoaPeritoDisponibilidade implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaPeritoDisponibilidade,Integer> {

	private static final long serialVersionUID = 3006434648236911722L;
	private int idPessoaPeritoDisponibilidade;
	private PessoaPeritoEspecialidade pessoaPeritoEspecialidade;
	private SemanaEnum diaSemana;
	private Date horaInicio;
	private Date horaFim;
	private Boolean ignoraFeriado;
	private Date intervalo;
	private Integer qntAtendimento;
	private Boolean ativo = true;
	private List<String> diaSemanaList = new ArrayList<String>(0);

	public PessoaPeritoDisponibilidade() {
	}

	@Id
	@GeneratedValue(generator = "gen_pess_prto_dspnbilidade")
	@Column(name = "id_pess_perito_disponibilidade", unique = true, nullable = false)
	public int getIdPessoaPeritoDisponibilidade() {
		return idPessoaPeritoDisponibilidade;
	}

	public void setIdPessoaPeritoDisponibilidade(int idPessoaPeritoDisponibilidade) {
		this.idPessoaPeritoDisponibilidade = idPessoaPeritoDisponibilidade;
	}

	@Column(name = "ds_dia_semana")
	@Enumerated(EnumType.STRING)
	public SemanaEnum getDiaSemana() {
		return diaSemana;
	}

	public void setDiaSemana(SemanaEnum diaSemana) {
		this.diaSemana = diaSemana;
	}

	@Column(name = "dt_hora_inicio")
	@Temporal(TemporalType.TIME)
	public Date getHoraInicio() {
		return horaInicio;
	}

	public void setHoraInicio(Date horaInicio) {
		this.horaInicio = horaInicio;
	}

	@Column(name = "dt_hora_fim")
	@Temporal(TemporalType.TIME)
	public Date getHoraFim() {
		return horaFim;
	}

	public void setHoraFim(Date horaFim) {
		this.horaFim = horaFim;
	}

	@Column(name = "in_ignora_feriado")
	public Boolean getIgnoraFeriado() {
		return ignoraFeriado;
	}

	public void setIgnoraFeriado(Boolean ignoraFeriado) {
		this.ignoraFeriado = ignoraFeriado;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pessoa_perito_especialidade")
	public PessoaPeritoEspecialidade getPessoaPeritoEspecialidade() {
		return pessoaPeritoEspecialidade;
	}

	public void setPessoaPeritoEspecialidade(PessoaPeritoEspecialidade pessoaPeritoEspecialidade) {
		this.pessoaPeritoEspecialidade = pessoaPeritoEspecialidade;
	}

	@Column(name = "nr_tempo_intervalo")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getIntervalo() {
		return intervalo;
	}

	public void setIntervalo(Date intervalo) {
		this.intervalo = intervalo;
	}

	@Column(name = "nr_atendimento")
	@Min(value = 1)
	public Integer getQntAtendimento() {
		return qntAtendimento;
	}

	public void setQntAtendimento(Integer qntAtendimento) {
		this.qntAtendimento = qntAtendimento;
	}

	@Column(name = "in_ativo")
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Transient
	public List<String> getDiaSemanaList() {
		if (diaSemanaList == null || diaSemanaList.isEmpty()) {
			diaSemanaList = new ArrayList<String>(0);
		}
		if (diaSemana != null) {
			diaSemanaList = new ArrayList<String>(0);
			diaSemanaList.add(diaSemana.toString());
		}
		return diaSemanaList;
	}

	public void setDiaSemanaList(List<String> diaSemanaList) {
		this.diaSemanaList = diaSemanaList;
	}

	@Transient
	public int getMinutos() {
		if (intervalo != null) {
			return (int) (intervalo.getTime() - Time.valueOf("00:00:00").getTime()) / 60 / 1000;
		}
		return 0;
	}

	public void setMinutos(int minutos) {
		intervalo = Time.valueOf(MessageFormat.format("00:{0,number,00}:00", minutos));
	}

	@Override
	public String toString() {
		return MessageFormat.format("{0}-{1} das {2,time,short} as {3,time,short}", pessoaPeritoEspecialidade,
				getDiaSemana(), horaInicio, horaFim);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PessoaPeritoDisponibilidade)) {
			return false;
		}
		PessoaPeritoDisponibilidade other = (PessoaPeritoDisponibilidade) obj;
		if (getIdPessoaPeritoDisponibilidade() != other.getIdPessoaPeritoDisponibilidade()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPessoaPeritoDisponibilidade();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaPeritoDisponibilidade> getEntityClass() {
		return PessoaPeritoDisponibilidade.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPessoaPeritoDisponibilidade());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
