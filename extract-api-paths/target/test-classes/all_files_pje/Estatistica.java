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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_estatistica")
@org.hibernate.annotations.GenericGenerator(name = "gen_estatistica", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_estatistica"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Estatistica implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Estatistica,Integer> {

	private static final long serialVersionUID = 1L;

	private int idEstatistica;
	private Processo processo;
	private String taskName;
	private String nodeName;
	private String nomeFluxo;
	private Fluxo fluxo;
	private Localizacao localizacao;
	private Date dataInicio;
	private Date dataFim;
	private Long duracao;

	public Estatistica() {
	}

	@Id
	@GeneratedValue(generator = "gen_estatistica")
	@Column(name = "id_estatistica", unique = true, nullable = false)
	public int getIdEstatistica() {
		return idEstatistica;
	}

	public void setIdEstatistica(int idEstatistica) {
		this.idEstatistica = idEstatistica;
	}

	@Column(name = "nm_task", nullable = false, length = 150)
	@NotNull
	@Length(max = 150)
	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	@Column(name = "nm_node", length = 150)
	@Length(max = 150)
	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	@Column(name = "ds_fluxo", length = 150)
	@Length(max = 150)
	public String getNomeFluxo() {
		return nomeFluxo;
	}

	public void setNomeFluxo(String fluxo) {
		this.nomeFluxo = fluxo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_localizacao")
	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_fluxo")
	public Fluxo getFluxo() {
		return fluxo;
	}

	public void setFluxo(Fluxo fluxo) {
		this.fluxo = fluxo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo", nullable = false)
	@NotNull
	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicio", nullable = false)
	@NotNull
	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fim")
	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		if (dataFim != null && dataInicio != null) {
			setDuracao(dataFim.getTime() - dataInicio.getTime());
		}
		this.dataFim = dataFim;
	}

	@Column(name = "nr_duracao")
	public Long getDuracao() {
		return duracao;
	}

	public void setDuracao(Long duracao) {
		this.duracao = duracao;
	}

	@Override
	public String toString() {
		return nomeFluxo + " / " + taskName;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Estatistica)) {
			return false;
		}
		Estatistica other = (Estatistica) obj;
		if (getIdEstatistica() != other.getIdEstatistica()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdEstatistica();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Estatistica> getEntityClass() {
		return Estatistica.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdEstatistica());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
