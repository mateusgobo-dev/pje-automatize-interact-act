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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = ProcessoCaixaAdvogadoProcurador.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_caixa_adv_proc", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_caixa_adv_proc"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoCaixaAdvogadoProcurador implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoCaixaAdvogadoProcurador,Integer> {

	public static final String TABLE_NAME = "tb_processo_caixa_adv_proc";
	private static final long serialVersionUID = 1L;

	private Integer idProcessoCaixaAdvogadoProcurador;
	private ProcessoTrf processoTrf;
	private CaixaAdvogadoProcurador caixaAdvogadoProcurador;

	@Id
	@GeneratedValue(generator = "gen_processo_caixa_adv_proc")
	@Column(name = "id_processo_caixa_adv_proc", unique = true, nullable = false)
	public Integer getIdProcessoCaixaAdvogadoProcurador() {
		return idProcessoCaixaAdvogadoProcurador;
	}

	public void setIdProcessoCaixaAdvogadoProcurador(Integer idProcessoCaixaAdvogadoProcurador) {
		this.idProcessoCaixaAdvogadoProcurador = idProcessoCaixaAdvogadoProcurador;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setCaixaAdvogadoProcurador(CaixaAdvogadoProcurador caixaAdvogadoProcurador) {
		this.caixaAdvogadoProcurador = caixaAdvogadoProcurador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_caixa_adv_proc")
	public CaixaAdvogadoProcurador getCaixaAdvogadoProcurador() {
		return caixaAdvogadoProcurador;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getCaixaAdvogadoProcurador() == null) ? 0 : caixaAdvogadoProcurador.hashCode());
		result = prime * result
				+ ((getIdProcessoCaixaAdvogadoProcurador() == null) ? 0 : idProcessoCaixaAdvogadoProcurador.hashCode());
		result = prime * result + ((getProcessoTrf() == null) ? 0 : processoTrf.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ProcessoCaixaAdvogadoProcurador))
			return false;
		ProcessoCaixaAdvogadoProcurador other = (ProcessoCaixaAdvogadoProcurador) obj;
		if (getCaixaAdvogadoProcurador() == null) {
			if (other.getCaixaAdvogadoProcurador() != null)
				return false;
		} else if (!caixaAdvogadoProcurador.equals(other.getCaixaAdvogadoProcurador()))
			return false;
		if (getIdProcessoCaixaAdvogadoProcurador() == null) {
			if (other.getIdProcessoCaixaAdvogadoProcurador() != null)
				return false;
		} else if (!idProcessoCaixaAdvogadoProcurador.equals(other.getIdProcessoCaixaAdvogadoProcurador()))
			return false;
		if (getProcessoTrf() == null) {
			if (other.getProcessoTrf() != null)
				return false;
		} else if (!processoTrf.equals(other.getProcessoTrf()))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoCaixaAdvogadoProcurador> getEntityClass() {
		return ProcessoCaixaAdvogadoProcurador.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdProcessoCaixaAdvogadoProcurador();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
