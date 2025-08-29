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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = OrgaoJulgadorCompetencia.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_org_julg_competencia", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_org_julg_competencia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class OrgaoJulgadorCompetencia implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<OrgaoJulgadorCompetencia,Integer>{

	public static final String TABLE_NAME = "tb_org_julg_competencia";
	private static final long serialVersionUID = 1L;

	private int idOrgaoJulgadorCompetencia;
	private OrgaoJulgador orgaoJulgador;
	private Competencia competencia;
	private String orgaoJulgadorSearch;
	private String competenciaSearch;
	private Date dataInicio;
	private Date dataFim;

	public OrgaoJulgadorCompetencia() {
	}

	@Id
	@GeneratedValue(generator = "gen_org_julg_competencia")
	@Column(name = "id_orgao_julgador_competencia", unique = true, nullable = false)
	public int getIdOrgaoJulgadorCompetencia() {
		return this.idOrgaoJulgadorCompetencia;
	}

	public void setIdOrgaoJulgadorCompetencia(int idOrgaoJulgadorCompetencia) {
		this.idOrgaoJulgadorCompetencia = idOrgaoJulgadorCompetencia;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return this.orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {

		this.orgaoJulgador = orgaoJulgador;
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

	@Transient
	public String getOrgaoJulgadorSearch() {
		return orgaoJulgadorSearch;
	}

	public void setOrgaoJulgadorSearch(String orgaoJulgadorSearch) {
		this.orgaoJulgadorSearch = orgaoJulgadorSearch;
	}

	@Transient
	public String getCompetenciaSearch() {
		return competenciaSearch;
	}

	public void setCompetenciaSearch(String competenciaSearch) {
		this.competenciaSearch = competenciaSearch;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof OrgaoJulgadorCompetencia)) {
			return false;
		}
		OrgaoJulgadorCompetencia other = (OrgaoJulgadorCompetencia) obj;
		if (getIdOrgaoJulgadorCompetencia() != other.getIdOrgaoJulgadorCompetencia()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdOrgaoJulgadorCompetencia();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends OrgaoJulgadorCompetencia> getEntityClass() {
		return OrgaoJulgadorCompetencia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdOrgaoJulgadorCompetencia());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
