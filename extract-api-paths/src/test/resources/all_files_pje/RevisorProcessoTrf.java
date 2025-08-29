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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_revisor_processo")
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.GenericGenerator(name = "gen_revisor_processo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_revisor_processo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RevisorProcessoTrf implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RevisorProcessoTrf,Integer> {

	private static final long serialVersionUID = 1L;

	private int idRevisorProcessoTrf;
	private ProcessoTrf processoTrf;
	private OrgaoJulgador orgaoJulgadorRevisor;
	private Date dataInicio;
	private Date dataFinal;
	private Sessao sessaoSugerida;

	public RevisorProcessoTrf() {
	}

	@Id
	@GeneratedValue(generator = "gen_revisor_processo")
	@Column(name = "id_revisor_processo", nullable = false)
	public int getIdRevisorProcessoTrf() {
		return idRevisorProcessoTrf;
	}

	public void setIdRevisorProcessoTrf(int idRevisorProcessoTrf) {
		this.idRevisorProcessoTrf = idRevisorProcessoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@NotNull
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@NotNull
	@JoinColumn(name = "id_orgao_julgador_revisor")
	public OrgaoJulgador getOrgaoJulgadorRevisor() {
		return orgaoJulgadorRevisor;
	}

	public void setOrgaoJulgadorRevisor(OrgaoJulgador orgaoJulgadorRevisor) {
		this.orgaoJulgadorRevisor = orgaoJulgadorRevisor;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	@Column(name = "dt_inicio")
	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
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
		if (!(obj instanceof RevisorProcessoTrf)) {
			return false;
		}
		RevisorProcessoTrf other = (RevisorProcessoTrf) obj;
		if (getIdRevisorProcessoTrf() != other.getIdRevisorProcessoTrf()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdRevisorProcessoTrf();
		return result;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sessao_sugerida")
	public Sessao getSessaoSugerida() {
		return sessaoSugerida;
	}

	public void setSessaoSugerida(Sessao sessaoSugerida) {
		this.sessaoSugerida = sessaoSugerida;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RevisorProcessoTrf> getEntityClass() {
		return RevisorProcessoTrf.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdRevisorProcessoTrf());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
