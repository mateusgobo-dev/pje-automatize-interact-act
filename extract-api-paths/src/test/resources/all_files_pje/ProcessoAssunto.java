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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tb_processo_assunto")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_assunto", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_assunto"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoAssunto implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoAssunto,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoAssunto;
	private ProcessoTrf processoTrf;
	private AssuntoTrf assuntoTrf;
	private Boolean assuntoPrincipal;
	private List<ProcessoAssuntoAntecedente> processoAssuntoAntecedenteList = new ArrayList<ProcessoAssuntoAntecedente>(
			0);

	public ProcessoAssunto() {
	}

	@Id
	@GeneratedValue(generator = "gen_processo_assunto")
	@Column(name = "id_processo_assunto", unique = true, nullable = false)
	public int getIdProcessoAssunto() {
		return this.idProcessoAssunto;
	}

	public void setIdProcessoAssunto(int idProcessoAssunto) {
		this.idProcessoAssunto = idProcessoAssunto;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return this.processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_assunto_trf")
	public AssuntoTrf getAssuntoTrf() {
		return this.assuntoTrf;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	@Override
	public String toString() {
		if (getAssuntoTrf() != null) {
			return getAssuntoTrf().getAssuntoTrf();
		} else {
			return "";
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoAssunto)) {
			return false;
		}
		ProcessoAssunto other = (ProcessoAssunto) obj;
		if (getIdProcessoAssunto() != other.getIdProcessoAssunto()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoAssunto();
		return result;
	}

	@Column(name = "in_assunto_principal")
	public Boolean getAssuntoPrincipal() {
		return assuntoPrincipal;
	}

	public void setAssuntoPrincipal(Boolean assuntoPrincipal) {
		this.assuntoPrincipal = assuntoPrincipal;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "processoAssunto")
	public List<ProcessoAssuntoAntecedente> getProcessoAssuntoAntecedenteList() {
		return processoAssuntoAntecedenteList;
	}

	public void setProcessoAssuntoAntecedenteList(List<ProcessoAssuntoAntecedente> processoAssuntoAntecedenteList) {
		this.processoAssuntoAntecedenteList = processoAssuntoAntecedenteList;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoAssunto> getEntityClass() {
		return ProcessoAssunto.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoAssunto());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
