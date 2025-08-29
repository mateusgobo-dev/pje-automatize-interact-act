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
@Table(name = OrgaoJulgadorColegiadoOrgaoJulgador.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_org_julg_clgado_org_julg", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_org_julg_clgado_org_julg"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class OrgaoJulgadorColegiadoOrgaoJulgador implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<OrgaoJulgadorColegiadoOrgaoJulgador,Integer>{

	private static final long serialVersionUID = 2428569756667627075L;

	public static final String TABLE_NAME = "tb_org_julg_clgdo_org_julg";

	private int idOrgaoJulgadorColegiadoOrgaoJulgador;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgadorColegiadoCargo orgaoJulgadorColegiadoCargo;
	
	private Date dataInicial;
	private Date dataFinal;
	private Boolean selecionado;
	private Boolean presidenteSessao;
	private Integer ordem;
	private OrgaoJulgadorColegiadoOrgaoJulgador revisorTemporario;	
	private OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorRevisor;

	public OrgaoJulgadorColegiadoOrgaoJulgador() {

	}

	@Id
	@GeneratedValue(generator = "gen_org_julg_clgado_org_julg")
	@Column(name = "id_org_julg_clgado_org_julgdor", unique = true, nullable = false)
	public int getIdOrgaoJulgadorColegiadoOrgaoJulgador() {
		return idOrgaoJulgadorColegiadoOrgaoJulgador;
	}

	public void setIdOrgaoJulgadorColegiadoOrgaoJulgador(int idOrgaoJulgadorColegiadoOrgaoJulgador) {
		this.idOrgaoJulgadorColegiadoOrgaoJulgador = idOrgaoJulgadorColegiadoOrgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_colegiado", nullable = false)
	@NotNull
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador", nullable = false)
	@NotNull
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_org_jlgdor_colegiado_cargo")
	public OrgaoJulgadorColegiadoCargo getOrgaoJulgadorColegiadoCargo() {
		return orgaoJulgadorColegiadoCargo;
	}

	public void setOrgaoJulgadorColegiadoCargo(OrgaoJulgadorColegiadoCargo orgaoJulgadorColegiadoCargo) {
		this.orgaoJulgadorColegiadoCargo = orgaoJulgadorColegiadoCargo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicial", nullable = false)
	@NotNull
	public Date getDataInicial() {
		return dataInicial;
	}

	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_final")
	public Date getDataFinal() {
		return dataFinal;
	}

	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}
	
	@Column(name = "nr_ordem")
	public Integer getOrdem() {
		return ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	@Transient
	public Boolean getSelecionado() {
		return selecionado;
	}

	public void setSelecionado(Boolean selecionado) {
		this.selecionado = selecionado;
	}

	@Override
	public String toString() {
		return orgaoJulgador.getOrgaoJulgador();
	}

	@Transient
	public Boolean getPresidenteSessao() {
		return presidenteSessao;
	}

	public void setPresidenteSessao(Boolean presidenteSessao) {
		this.presidenteSessao = presidenteSessao;
	}

	@Transient
	public OrgaoJulgadorColegiadoOrgaoJulgador getRevisorTemporario() {
		return revisorTemporario;
	}

	public void setRevisorTemporario(OrgaoJulgadorColegiadoOrgaoJulgador revisorTemporario) {
		this.revisorTemporario = revisorTemporario;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_revisor", nullable=true)
	public OrgaoJulgadorColegiadoOrgaoJulgador getOrgaoJulgadorRevisor() {
		return orgaoJulgadorRevisor;
	}

	public void setOrgaoJulgadorRevisor(OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorRevisor) {
		this.orgaoJulgadorRevisor = orgaoJulgadorRevisor;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof OrgaoJulgadorColegiadoOrgaoJulgador)) {
			return false;
		}
		OrgaoJulgadorColegiadoOrgaoJulgador other = (OrgaoJulgadorColegiadoOrgaoJulgador) obj;
		if (getIdOrgaoJulgadorColegiadoOrgaoJulgador() != other.getIdOrgaoJulgadorColegiadoOrgaoJulgador()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdOrgaoJulgadorColegiadoOrgaoJulgador();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends OrgaoJulgadorColegiadoOrgaoJulgador> getEntityClass() {
		return OrgaoJulgadorColegiadoOrgaoJulgador.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdOrgaoJulgadorColegiadoOrgaoJulgador());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
