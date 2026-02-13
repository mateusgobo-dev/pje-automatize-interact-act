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

import java.io.Serializable;
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


@Entity
@Table(name = "tb_historico_distribuicao")
@org.hibernate.annotations.GenericGenerator(name = "gen_historico_distribuicao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_historico_distribuicao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class HistoricoDistribuicao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<HistoricoDistribuicao,Integer> {

	private static final long serialVersionUID = 1L;

	private int idHistoricoDistribuicao;
	private ProcessoTrf processoTrf;
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private Cargo cargo;
	private Date dataDistribuicao;
	private Fluxo fluxo;

	@Id
	@GeneratedValue(generator = "gen_historico_distribuicao")
	@Column(name = "id_historico_distribuicao", unique = true, nullable = false)
	public int getIdHistoricoDistribuicao() {
		return idHistoricoDistribuicao;
	}

	public void setIdHistoricoDistribuicao(int idHistoricoDistribuicao) {
		this.idHistoricoDistribuicao = idHistoricoDistribuicao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_oj")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_ojc")
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cargo")
	public Cargo getCargo() {
		return cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_distribuicao", nullable = false)
	public Date getDataDistribuicao() {
		return dataDistribuicao;
	}

	public void setDataDistribuicao(Date dataDistribuicao) {
		this.dataDistribuicao = dataDistribuicao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_fluxo")
	public Fluxo getFluxo() {
		return fluxo;
	}

	public void setFluxo(Fluxo fluxo) {
		this.fluxo = fluxo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof HistoricoDistribuicao)) {
			return false;
		}
		HistoricoDistribuicao other = (HistoricoDistribuicao) obj;
		if (getIdHistoricoDistribuicao() != other.getIdHistoricoDistribuicao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdHistoricoDistribuicao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends HistoricoDistribuicao> getEntityClass() {
		return HistoricoDistribuicao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdHistoricoDistribuicao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
