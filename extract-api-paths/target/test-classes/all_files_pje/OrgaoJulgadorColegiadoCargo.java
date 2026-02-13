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
import javax.validation.constraints.NotNull;

@Entity
@Table(name = OrgaoJulgadorColegiadoCargo.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_org_julg_clgado_cargo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_org_julg_clgado_cargo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class OrgaoJulgadorColegiadoCargo implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<OrgaoJulgadorColegiadoCargo,Integer> {

	public static final String TABLE_NAME = "tb_org_julg_colgiado_cargo";
	private static final long serialVersionUID = 1L;

	private int idOrgaoJulgadorColegiadoCargo;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private Cargo cargo;

	public OrgaoJulgadorColegiadoCargo() {
	}

	@Id
	@GeneratedValue(generator = "gen_org_julg_clgado_cargo")
	@Column(name = "id_org_julgdor_colegiado_cargo", unique = true, nullable = false)
	public int getIdOrgaoJulgadorColegiadoCargo() {
		return this.idOrgaoJulgadorColegiadoCargo;
	}

	public void setIdOrgaoJulgadorColegiadoCargo(int idOrgaoJulgadorColegiadoCargo) {
		this.idOrgaoJulgadorColegiadoCargo = idOrgaoJulgadorColegiadoCargo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_colegiado", nullable = false)
	@NotNull
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return this.orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cargo")
	@NotNull
	public Cargo getCargo() {
		return this.cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	@Override
	public String toString() {
		return cargo.getCargo();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof OrgaoJulgadorColegiadoCargo)) {
			return false;
		}
		OrgaoJulgadorColegiadoCargo other = (OrgaoJulgadorColegiadoCargo) obj;
		if (getIdOrgaoJulgadorColegiadoCargo() != other.getIdOrgaoJulgadorColegiadoCargo()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdOrgaoJulgadorColegiadoCargo();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends OrgaoJulgadorColegiadoCargo> getEntityClass() {
		return OrgaoJulgadorColegiadoCargo.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdOrgaoJulgadorColegiadoCargo());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
