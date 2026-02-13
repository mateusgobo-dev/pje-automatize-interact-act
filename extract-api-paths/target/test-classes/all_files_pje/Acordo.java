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
package br.jus.pje.jt.entidades;

import java.io.Serializable;
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

import br.jus.pje.nucleo.entidades.ProcessoAudiencia;

@Entity
@Table(name = "tb_acordo")
@org.hibernate.annotations.GenericGenerator(name = "gen_acordo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_acordo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Acordo implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<Acordo,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idAcordo;
	private ProcessoAudiencia processoAudiencia;
	private List<AcordoVerba> acordoVerbas;
	private List<AcordoParcela> acordoParcelas;

	// private List<AudParteImportacao> audParteImportacao;

	private Double valorAcordo;
	private Integer numParcelas;

	@Id
	@Column(name = "id_acordo", unique = true, nullable = false)
	@GeneratedValue(generator = "gen_acordo")
	public Integer getIdAcordo() {
		return idAcordo;
	}

	public void setIdAcordo(Integer idAcordo) {
		this.idAcordo = idAcordo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_audiencia", nullable = false)
	public ProcessoAudiencia getProcessoAudiencia() {
		return processoAudiencia;
	}

	public void setProcessoAudiencia(ProcessoAudiencia processoAudiencia) {
		this.processoAudiencia = processoAudiencia;
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "acordo")
	public List<AcordoVerba> getAcordoVerbas() {
		return acordoVerbas;
	}

	public void setAcordoVerbas(List<AcordoVerba> acordoVerbas) {
		this.acordoVerbas = acordoVerbas;
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "acordo")
	public List<AcordoParcela> getAcordoParcelas() {
		return acordoParcelas;
	}

	public void setAcordoParcelas(List<AcordoParcela> acordoParcelas) {
		this.acordoParcelas = acordoParcelas;
	}

	@Column(name = "vl_acordo")
	public Double getValorAcordo() {
		return valorAcordo;
	}

	public void setValorAcordo(Double valorAcordo) {
		this.valorAcordo = valorAcordo;
	}

	@Column(name = "nr_parcelas")
	public Integer getNumParcelas() {
		return numParcelas;
	}

	public void setNumParcelas(Integer numParcelas) {
		this.numParcelas = numParcelas;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends Acordo> getEntityClass() {
		return Acordo.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdAcordo();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
