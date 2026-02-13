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
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_processo_clet")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_clet", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_clet"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoClet implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoClet,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idProcessoClet;
	private ProcessoTrf processoTrf;
	private Date dataAjuizamento;
	private Date dataUltimoBndt;
	private Boolean outraJustica = Boolean.FALSE;
	private String numeroProcessoOrigemOutraJustica;
	private Boolean desarquivado = Boolean.FALSE;
	private NaturezaClet naturezaClet;
	private Date dataInicioClet;

	@Id
	@Column(name = "id_processo_clet")
	@NotNull
	@GeneratedValue(generator = "gen_processo_clet")
	public Integer getIdProcessoClet() {
		return idProcessoClet;
	}

	public void setIdProcessoClet(Integer idProcessoClet) {
		this.idProcessoClet = idProcessoClet;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@Column(name = "dt_ajuizamento")
	@NotNull
	public Date getDataAjuizamento() {
		return dataAjuizamento;
	}

	public void setDataAjuizamento(Date dataAjuizamento) {
		this.dataAjuizamento = dataAjuizamento;
	}

	@Column(name = "dt_ultimo_bndt")
	public Date getDataUltimoBndt() {
		return dataUltimoBndt;
	}

	public void setDataUltimoBndt(Date dataUltimoBndt) {
		this.dataUltimoBndt = dataUltimoBndt;
	}

	@Column(name = "in_outra_justica")
	@NotNull
	public Boolean getOutraJustica() {
		return outraJustica;
	}

	public void setOutraJustica(Boolean outraJustica) {
		this.outraJustica = outraJustica;
	}

	@Column(name = "nr_proc_origem_outra_justica")
	public String getNumeroProcessoOrigemOutraJustica() {
		return numeroProcessoOrigemOutraJustica;
	}

	public void setNumeroProcessoOrigemOutraJustica(String numeroProcessoOrigemOutraJustica) {
		this.numeroProcessoOrigemOutraJustica = numeroProcessoOrigemOutraJustica;
	}

	@Column(name = "in_desarquivado")
	@NotNull
	public Boolean getDesarquivado() {
		return desarquivado;
	}

	public void setDesarquivado(Boolean desarquivado) {
		this.desarquivado = desarquivado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_natureza_clet")
	@NotNull
	public NaturezaClet getNaturezaClet() {
		return naturezaClet;
	}

	public void setNaturezaClet(NaturezaClet naturezaClet) {
		this.naturezaClet = naturezaClet;
	}

	@Column(name = "dt_inicio_clet")
	@NotNull
	public Date getDataInicioClet() {
		return dataInicioClet;
	}

	public void setDataInicioClet(Date dataInicioClet) {
		this.dataInicioClet = dataInicioClet;
	}

	@Override
	public String toString() {
		return processoTrf.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idProcessoClet == null) ? 0 : idProcessoClet.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoClet)) {
			return false;
		}
		ProcessoClet other = (ProcessoClet) obj;
		if (idProcessoClet == null) {
			if (other.idProcessoClet != null) {
				return false;
			}
		} else if (!idProcessoClet.equals(other.idProcessoClet)) {
			return false;
		}
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoClet> getEntityClass() {
		return ProcessoClet.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdProcessoClet();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
