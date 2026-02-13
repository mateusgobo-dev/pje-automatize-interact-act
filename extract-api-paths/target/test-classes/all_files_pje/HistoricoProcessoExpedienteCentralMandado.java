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

@Entity
@Table(name = "tb_hist_proc_exped_cntral_mnddo")
@org.hibernate.annotations.GenericGenerator(name = "gen_hist_proc_exped_cntrl_mandado", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_hist_proc_exped_cntrl_mandado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class HistoricoProcessoExpedienteCentralMandado implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<HistoricoProcessoExpedienteCentralMandado,Integer> {

	private static final long serialVersionUID = 1L;

	private int idHistoricoProcessoExpedienteCentralMandado;
	private ProcessoExpedienteCentralMandado processoExpedienteCentralMandado;
	private CentralMandado centralMandadoAnterior;
	private CentralMandado centralMandadoNova;
	
	public HistoricoProcessoExpedienteCentralMandado() {
	}

	@Id
	@GeneratedValue(generator = "gen_hist_proc_exped_cntrl_mandado")
	@Column(name = "id_hist_proc_expedi_central_mandado", unique = true, nullable = false)
	public int getIdHistoricoProcessoExpedienteCentralMandado() {
		return this.idHistoricoProcessoExpedienteCentralMandado;
	}

	public void setIdHistoricoProcessoExpedienteCentralMandado(int idHistoricoProcessoExpedienteCentralMandado) {
		this.idHistoricoProcessoExpedienteCentralMandado = idHistoricoProcessoExpedienteCentralMandado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_proc_expedi_central_mandado")
	public ProcessoExpedienteCentralMandado getProcessoExpedienteCentralMandado() {
		return this.processoExpedienteCentralMandado;
	}

	public void setProcessoExpedienteCentralMandado(ProcessoExpedienteCentralMandado processoExpedienteCentralMandado) {
		this.processoExpedienteCentralMandado = processoExpedienteCentralMandado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_central_mandado_anterior")
	public CentralMandado getCentralMandadoAnterior() {
		return this.centralMandadoAnterior;
	}

	public void setCentralMandadoAnterior(CentralMandado centralMandadoAnterior) {
		this.centralMandadoAnterior = centralMandadoAnterior;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_central_mandado_nova")
	public CentralMandado getCentralMandadoNova() {
		return this.centralMandadoNova;
	}

	public void setCentralMandadoNova(CentralMandado centralMandadoNova) {
		this.centralMandadoNova = centralMandadoNova;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof HistoricoProcessoExpedienteCentralMandado)) {
			return false;
		}
		HistoricoProcessoExpedienteCentralMandado other = (HistoricoProcessoExpedienteCentralMandado) obj;
		if (getIdHistoricoProcessoExpedienteCentralMandado() != other.getIdHistoricoProcessoExpedienteCentralMandado()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdHistoricoProcessoExpedienteCentralMandado();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends HistoricoProcessoExpedienteCentralMandado> getEntityClass() {
		return HistoricoProcessoExpedienteCentralMandado.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdHistoricoProcessoExpedienteCentralMandado());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
