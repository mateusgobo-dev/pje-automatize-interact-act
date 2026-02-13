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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_norma_penal")
@org.hibernate.annotations.GenericGenerator(name = "gen_norma_penal", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_norma_penal"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class NormaPenal implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<NormaPenal,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idNormaPenal;
	private Integer nrNorma;
	private String normaPenal;
	private String dsSigla;
	private Date dataInicioVigencia;
	private Date dataFimVigencia;
	private TipoNormaPenal tipoNormaPenal;
	private Boolean ativo = true;

	public NormaPenal() {
	}

	public NormaPenal(Integer id) {
		this.idNormaPenal = id;
	}

	public void resetar() {
		this.nrNorma = null;
		this.normaPenal = null;
		this.dsSigla = null;
		this.dataInicioVigencia = null;
		this.dataFimVigencia = null;
		this.ativo = null;
	}

	@Id
	@GeneratedValue(generator = "gen_norma_penal")
	@Column(name = "id_norma_penal", unique = true, nullable = false)
	public Integer getIdNormaPenal() {
		return idNormaPenal;
	}

	public void setIdNormaPenal(Integer idNormaPenal) {
		this.idNormaPenal = idNormaPenal;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_tipo_norma_penal", nullable = true)
	public TipoNormaPenal getTipoNormaPenal() {
		return tipoNormaPenal;
	}

	public void setTipoNormaPenal(TipoNormaPenal tipoNormaPenal) {
		this.tipoNormaPenal = tipoNormaPenal;
	}

	@Column(name = "nr_norma", nullable = false)
	@NotNull
	public Integer getNrNorma() {
		return nrNorma;
	}

	public void setNrNorma(Integer nrNorma) {
		this.nrNorma = nrNorma;
	}

	@Transient
	public String getNrNormaString() {
		return nrNorma != null ? String.valueOf(nrNorma) : "";
	}

	public void setNrNormaString(String nrNormaString) {
		this.nrNorma = nrNormaString != null && !nrNormaString.isEmpty() ? Integer.valueOf(nrNormaString)
				: this.nrNorma;
	}

	@Column(name = "ds_norma", length = 100)
	@Length(max = 100)
	public String getNormaPenal() {
		return normaPenal;
	}

	public void setNormaPenal(String normaPenal) {
		this.normaPenal = normaPenal;
	}

	@Column(name = "ds_sigla", length = 15)
	@Length(max = 15)
	public String getDsSigla() {
		return dsSigla;
	}

	public void setDsSigla(String dsSigla) {
		this.dsSigla = dsSigla;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicio_vigencia", nullable = false)
	public Date getDataInicioVigencia() {
		return dataInicioVigencia;
	}

	public void setDataInicioVigencia(Date dataInicioVigencia) {
		this.dataInicioVigencia = dataInicioVigencia;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fim_vigencia")
	public Date getDataFimVigencia() {
		return dataFimVigencia;
	}

	public void setDataFimVigencia(Date dataFimVigencia) {
		this.dataFimVigencia = dataFimVigencia;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends NormaPenal> getEntityClass() {
		return NormaPenal.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdNormaPenal();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
