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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = OrgaoJulgadorColegiadoOrgaoJulgadorLog.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_orgjlg_clgdo_orgjlg_log", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_orgjlg_clgdo_orgjlg_log"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class OrgaoJulgadorColegiadoOrgaoJulgadorLog implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<OrgaoJulgadorColegiadoOrgaoJulgadorLog,Integer> {

	public static final String TABLE_NAME = "tb_orgjlg_clgdo_orgjlg_log";
	private static final long serialVersionUID = 1L;

	private int idOrgaoJulgadorColegiadoOrgaoJulgadorLog;
	private String orgaoJulgadorColegiado;
	private String orgaoJulgador;
	private String cargo;
	private Date dataInicial;
	private Date dataFinal;

	public OrgaoJulgadorColegiadoOrgaoJulgadorLog() {

	}

	@Id
	@GeneratedValue(generator = "gen_orgjlg_clgdo_orgjlg_log")
	@Column(name = "id_org_julg_clgdo_org_julg_log", unique = true, nullable = false)
	public int getIdOrgaoJulgadorColegiadoOrgaoJulgadorLog() {
		return idOrgaoJulgadorColegiadoOrgaoJulgadorLog;
	}

	public void setIdOrgaoJulgadorColegiadoOrgaoJulgadorLog(int idOrgaoJulgadorColegiadoOrgaoJulgadorLog) {
		this.idOrgaoJulgadorColegiadoOrgaoJulgadorLog = idOrgaoJulgadorColegiadoOrgaoJulgadorLog;
	}

	@Column(name = "ds_oj_colegiado")
	@NotNull
	@Length(max = 200)
	public String getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(String orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@Column(name = "ds_orgao_julgador", nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(String orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@Column(name = "ds_cargo", length = 100)
	@Length(max = 100)
	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof OrgaoJulgadorColegiadoOrgaoJulgadorLog)) {
			return false;
		}
		OrgaoJulgadorColegiadoOrgaoJulgadorLog other = (OrgaoJulgadorColegiadoOrgaoJulgadorLog) obj;
		if (getIdOrgaoJulgadorColegiadoOrgaoJulgadorLog() != other.getIdOrgaoJulgadorColegiadoOrgaoJulgadorLog()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdOrgaoJulgadorColegiadoOrgaoJulgadorLog();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends OrgaoJulgadorColegiadoOrgaoJulgadorLog> getEntityClass() {
		return OrgaoJulgadorColegiadoOrgaoJulgadorLog.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdOrgaoJulgadorColegiadoOrgaoJulgadorLog());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
