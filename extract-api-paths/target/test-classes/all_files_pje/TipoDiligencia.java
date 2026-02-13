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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_tipo_diligencia")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_diligencia", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_diligencia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoDiligencia implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoDiligencia,Integer> {

	private static final long serialVersionUID = 1L;

	private int idTipoDiligencia;
	private String tipoDiligencia;
	private Boolean ativo;

	private List<ProcessoExpedienteDiligencia> processoExpedienteDiligenciaList = new ArrayList<ProcessoExpedienteDiligencia>(
			0);

	public TipoDiligencia() {
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_diligencia")
	@Column(name = "id_tipo_diligencia", unique = true, nullable = false)
	public int getIdTipoDiligencia() {
		return this.idTipoDiligencia;
	}

	public void setIdTipoDiligencia(int idTipoDiligencia) {
		this.idTipoDiligencia = idTipoDiligencia;
	}

	@Column(name = "ds_tipo_diligencia", unique = true, nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getTipoDiligencia() {
		return this.tipoDiligencia;
	}

	public void setTipoDiligencia(String tipoDiligencia) {
		this.tipoDiligencia = tipoDiligencia;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "tipoDiligencia")
	public List<ProcessoExpedienteDiligencia> getProcessoExpedienteDiligenciaList() {
		return this.processoExpedienteDiligenciaList;
	}

	public void setProcessoExpedienteDiligenciaList(List<ProcessoExpedienteDiligencia> processoExpedienteDiligenciaList) {
		this.processoExpedienteDiligenciaList = processoExpedienteDiligenciaList;
	}

	@Override
	public String toString() {
		return tipoDiligencia;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoDiligencia)) {
			return false;
		}
		TipoDiligencia other = (TipoDiligencia) obj;
		if (getIdTipoDiligencia() != other.getIdTipoDiligencia()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoDiligencia();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoDiligencia> getEntityClass() {
		return TipoDiligencia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoDiligencia());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
