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
@Table(name = "tb_proc_exped_diligencia")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_expdente_diligencia", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_expdente_diligencia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoExpedienteDiligencia implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoExpedienteDiligencia,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoExpedienteDiligencia;
	private ProcessoExpediente processoExpediente;
	private TipoDiligencia tipoDiligencia;

	public ProcessoExpedienteDiligencia() {
	}

	@Id
	@GeneratedValue(generator = "gen_proc_expdente_diligencia")
	@Column(name = "id_proc_expediente_diligencia", unique = true, nullable = false)
	public int getIdProcessoExpedienteDiligencia() {
		return this.idProcessoExpedienteDiligencia;
	}

	public void setIdProcessoExpedienteDiligencia(int idProcessoExpedienteDiligencia) {
		this.idProcessoExpedienteDiligencia = idProcessoExpedienteDiligencia;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_expediente", nullable = false)
	@NotNull
	public ProcessoExpediente getProcessoExpediente() {
		return this.processoExpediente;
	}

	public void setProcessoExpediente(ProcessoExpediente processoExpediente) {
		this.processoExpediente = processoExpediente;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_diligencia", nullable = false)
	@NotNull
	public TipoDiligencia getTipoDiligencia() {
		return this.tipoDiligencia;
	}

	public void setTipoDiligencia(TipoDiligencia tipoDiligencia) {
		this.tipoDiligencia = tipoDiligencia;
	}

	@Override
	public String toString() {
		return tipoDiligencia.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoExpedienteDiligencia)) {
			return false;
		}
		ProcessoExpedienteDiligencia other = (ProcessoExpedienteDiligencia) obj;
		if (getIdProcessoExpedienteDiligencia() != other.getIdProcessoExpedienteDiligencia()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoExpedienteDiligencia();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoExpedienteDiligencia> getEntityClass() {
		return ProcessoExpedienteDiligencia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoExpedienteDiligencia());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
