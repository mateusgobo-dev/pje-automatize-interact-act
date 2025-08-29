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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_proc_prioridde_processo")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_prioridade_processo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_prioridade_processo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoPrioridadeProcesso implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoPrioridadeProcesso,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoPrioridadeProcesso;
	private ProcessoTrf processoTrf;
	private PrioridadeProcesso prioridadeProcesso;

	@Transient
	public Boolean getAtivo() {
		return prioridadeProcesso != null && prioridadeProcesso.getAtivo(); 
	}	

	public ProcessoPrioridadeProcesso() {
	}

	@Id
	@GeneratedValue(generator = "gen_proc_prioridade_processo")
	@Column(name = "id_proc_prioridade_processo", unique = true, nullable = false)
	public int getIdProcessoPrioridadeProcesso() {
		return this.idProcessoPrioridadeProcesso;
	}

	public void setIdProcessoPrioridadeProcesso(int idProcessoPrioridadeProcesso) {
		this.idProcessoPrioridadeProcesso = idProcessoPrioridadeProcesso;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return this.processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_prioridade_processo", nullable = false)
	@NotNull
	public PrioridadeProcesso getPrioridadeProcesso() {
		return this.prioridadeProcesso;
	}

	public void setPrioridadeProcesso(PrioridadeProcesso prioridadeProcesso) {
		this.prioridadeProcesso = prioridadeProcesso;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoPrioridadeProcesso)) {
			return false;
		}
		ProcessoPrioridadeProcesso other = (ProcessoPrioridadeProcesso) obj;
		if (getIdProcessoPrioridadeProcesso() != other.getIdProcessoPrioridadeProcesso()) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return prioridadeProcesso.getPrioridade();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoPrioridadeProcesso();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoPrioridadeProcesso> getEntityClass() {
		return ProcessoPrioridadeProcesso.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoPrioridadeProcesso());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
