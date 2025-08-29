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

@Entity
@Table(name = OrgaoJulgadorColegiadoCompetencia.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_org_julg_clgado_cmptncia", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_org_julg_clgado_cmptncia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class OrgaoJulgadorColegiadoCompetencia implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<OrgaoJulgadorColegiadoCompetencia,Integer>{

	public static final String TABLE_NAME = "tb_org_julg_clgiado_compet";
	private static final long serialVersionUID = 1L;

	private int idOrgaoJulgadorColegiadoCompetencia;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private Competencia competencia;
	private Date dataInicio;
	private Date dataFim;

	public OrgaoJulgadorColegiadoCompetencia() {
	}

	@Id
	@GeneratedValue(generator = "gen_org_julg_clgado_cmptncia")
	@Column(name = "id_org_julg_clgado_competencia", unique = true, nullable = false)
	public int getIdOrgaoJulgadorColegiadoCompetencia() {
		return this.idOrgaoJulgadorColegiadoCompetencia;
	}

	public void setIdOrgaoJulgadorColegiadoCompetencia(int idOrgaoJulgadorColegiadoCompetencia) {
		this.idOrgaoJulgadorColegiadoCompetencia = idOrgaoJulgadorColegiadoCompetencia;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_colegiado")
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return this.orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_competencia")
	public Competencia getCompetencia() {
		return this.competencia;
	}

	public void setCompetencia(Competencia competencia) {
		this.competencia = competencia;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicio", nullable = false)
	@NotNull
	public Date getDataInicio() {
		return this.dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fim")
	public Date getDataFim() {
		return this.dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof OrgaoJulgadorColegiadoCompetencia)) {
			return false;
		}
		OrgaoJulgadorColegiadoCompetencia other = (OrgaoJulgadorColegiadoCompetencia) obj;
		if (getIdOrgaoJulgadorColegiadoCompetencia() != other.getIdOrgaoJulgadorColegiadoCompetencia()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdOrgaoJulgadorColegiadoCompetencia();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends OrgaoJulgadorColegiadoCompetencia> getEntityClass() {
		return OrgaoJulgadorColegiadoCompetencia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdOrgaoJulgadorColegiadoCompetencia());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
