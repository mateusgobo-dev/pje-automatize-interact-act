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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.jt.entidades.PeriodoDeInatividadeDaSala;
import br.jus.pje.jt.util.JTDateUtil;
import br.jus.pje.nucleo.enums.SalaEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Entity
@javax.persistence.Cacheable(true)
@Cache(region = "FechamentoPautaSessaoJulgamento", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = Sala.TABLE_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = { "id_orgao_julgador_colegiado", "ds_sala" }) })
@org.hibernate.annotations.GenericGenerator(name = "gen_sala", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_sala"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Sala implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Sala,Integer> {

	public static final String TABLE_NAME = "tb_sala";
	private static final long serialVersionUID = 1L;
	private int idSala;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private OrgaoJulgador orgaoJulgador;
	private String sala;
	private Boolean ignoraFeriado = false;
	private Boolean ativo;
	private SalaEnum tipoSala = SalaEnum.A;
	private List<SalaHorario> salaHorarioList = new ArrayList<SalaHorario>(0);
	private List<TipoAudiencia> tipoAudienciaList = new ArrayList<TipoAudiencia>();
	private List<PeriodoDeInatividadeDaSala> periodoDeInatividadeList;
	private List<BloqueioPauta> bloqueioPautaList;
	private List<ReservaHorario> reservaHorarioList;
	private List<Competencia> competenciaList = new ArrayList<Competencia>();
	
	public Sala() {
		periodoDeInatividadeList = new ArrayList<PeriodoDeInatividadeDaSala>();
		bloqueioPautaList = new ArrayList<BloqueioPauta>();
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@Id
	@GeneratedValue(generator = "gen_sala")
	@Column(name = "id_sala", unique = true, nullable = false)
	public int getIdSala() {
		return this.idSala;
	}

	public void setIdSala(int idSala) {
		this.idSala = idSala;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_orgao_julgador_colegiado")
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return this.orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@Column(name = "ds_sala", nullable = false, length = 50)
	@NotNull
	@Length(max = 50)
	public String getSala() {
		return this.sala;
	}

	public void setSala(String sala) {
		this.sala = sala;
	}

	@Column(name = "in_ignora_feriado", nullable = false)
	@NotNull
	public Boolean getIgnoraFeriado() {
		return this.ignoraFeriado;
	}

	public void setIgnoraFeriado(Boolean ignoraFeriado) {
		this.ignoraFeriado = ignoraFeriado;
	}

	@Override
	public String toString() {
		return sala;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	@OneToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, mappedBy = "sala")
	public List<PeriodoDeInatividadeDaSala> getPeriodoDeInatividadeList() {
		return periodoDeInatividadeList;
	}

	public void setPeriodoDeInatividadeList(List<PeriodoDeInatividadeDaSala> periodoDeInatividadeList) {
		this.periodoDeInatividadeList = periodoDeInatividadeList;
	}
	 
	public void setBloqueioPautaList(List<BloqueioPauta> bloqueioPautaList) {
		this.bloqueioPautaList = bloqueioPautaList;
	}
	
	@OneToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, mappedBy = "salaAudiencia")
	@OrderBy("dtInicial")
	public List<BloqueioPauta> getBloqueioPautaList() {
		return bloqueioPautaList;
	}
	
	public List<BloqueioPauta> getBloqueioPautaNoIntervaloList(Calendar dataInicial, Calendar dataFinal) {
		List<BloqueioPauta> bloqueioPautaNoIntervaloList = new ArrayList<BloqueioPauta>();
		for (BloqueioPauta bloqueioPauta : getBloqueioPautaList()) {
			// Data inicial ou data final do bloqueio está dentro do período desejado.
			if (DateUtil.isDataEntre(bloqueioPauta.getDtInicial(), dataInicial.getTime(), dataFinal.getTime()) ||
				DateUtil.isDataEntre(bloqueioPauta.getDtFinal(), dataInicial.getTime(), dataFinal.getTime())) {
				bloqueioPautaNoIntervaloList.add(bloqueioPauta);
			}
		}
		return bloqueioPautaNoIntervaloList;
	}
 
	/**
	 * Verifica se a sala esta desabilitada para uma data qualquer
	 * 
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @param date
	 * @param incluirDataInicial
	 * @param incluirDataFinal
	 * @return true Se a data estiver em um periodo de desabilitacao.
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 05/08/2011
	 */
	public boolean verificaSeASalaEstaInativa(Date date, boolean incluirDataInicial, boolean incluirDataFinal) {
		for (PeriodoDeInatividadeDaSala periodo : periodoDeInatividadeList) {
			boolean ativo = periodo.getAtivo();
			if (!ativo) {
				continue;
			}

			Date inicio = periodo.getInicio();
			Date termino = periodo.getTermino();

			boolean dataDentroPeriodo = dataDentroPeriodo(date, inicio, termino, incluirDataInicial, incluirDataFinal);
			if (dataDentroPeriodo) {
				return true;
			}

		}
		return false;
	}

	private boolean dataDentroPeriodo(Date data, Date inicio, Date termino, boolean incluirDataInicial,
			boolean incluirDataFinal) {
		if (incluirDataInicial && incluirDataFinal) {
			if (termino == null) {
				return JTDateUtil.afterOrEquals(data, inicio);
			} else {
				return JTDateUtil.between(data, inicio, termino, false);
			}
		} else if (incluirDataInicial && !incluirDataFinal) {
			if (termino == null) {
				return JTDateUtil.afterOrEquals(data, inicio);
			} else {
				return (JTDateUtil.afterOrEquals(data, inicio) && JTDateUtil.before(data, termino));
			}
		} else if (!incluirDataInicial && incluirDataFinal) {
			if (termino == null) {
				return JTDateUtil.after(data, inicio);
			} else {
				return (JTDateUtil.after(data, inicio) && JTDateUtil.beforeOrEquals(data, termino));
			}
		} else { // Exclui data inicial e final
			if (termino == null) {
				return JTDateUtil.after(data, inicio);
			} else {
				return JTDateUtil.between(data, inicio, termino, true);
			}
		}

	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "tp_sala", length = 1)
	@Enumerated(EnumType.STRING)
	public SalaEnum getTipoSala() {
		return tipoSala;
	}

	public void setTipoSala(SalaEnum tipoSala) {
		this.tipoSala = tipoSala;
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "sala")
	@OrderBy("diaSemana, horaInicial")
	public List<SalaHorario> getSalaHorarioList() {
		return salaHorarioList;
	}

	public void setSalaHorarioList(List<SalaHorario> salaHorarioList) {
		this.salaHorarioList = salaHorarioList;
	}

	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "tb_sala_tipo_audiencia", joinColumns = @JoinColumn(name = "id_sala"), inverseJoinColumns = @JoinColumn(name = "id_tipo_audiencia"))
	public List<TipoAudiencia> getTipoAudienciaList() {
		return tipoAudienciaList;
	}

	public void setTipoAudienciaList(List<TipoAudiencia> tipoAudienciaList) {
		this.tipoAudienciaList = tipoAudienciaList;
	}

	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "tb_sala_competencia", joinColumns = @JoinColumn(name = "id_sala"), inverseJoinColumns = @JoinColumn(name = "id_competencia"))
	public List<Competencia> getCompetenciaList() {
		return competenciaList;
	}

	public void setCompetenciaList(List<Competencia> competenciaList) {
		this.competenciaList = competenciaList;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Sala)) {
			return false;
		}
		Sala other = (Sala) obj;
		if (getIdSala() != other.getIdSala()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdSala();
		return result;
	}
    
	@Override
	@javax.persistence.Transient
	public Class<? extends Sala> getEntityClass() {
		return Sala.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdSala());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

	@ManyToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_sala_reserva_horario", 
		joinColumns = @JoinColumn(name = "id_sala", nullable = false, updatable = false), 
		inverseJoinColumns = @JoinColumn(name = "id_reserva_horario", nullable = false, updatable = false)
	)
    public List<ReservaHorario> getReservaHorarioList() {
        return reservaHorarioList;
    }

    public void setReservaHorarioList(List<ReservaHorario> reservaHorarioList) {
        this.reservaHorarioList = reservaHorarioList;
    }
	
	public List<SalaHorario> getSalaHorarioDiaList(int idDiaSemana) {
		List<SalaHorario> horariosDoDia = new ArrayList<SalaHorario>();
		for (SalaHorario salaHorario : getSalaHorarioList()) {
			if (salaHorario.getDiaSemana().getIdDiaSemana() == idDiaSemana) {
				horariosDoDia.add(salaHorario);				
			}
		}		
		return horariosDoDia;
	}
}
