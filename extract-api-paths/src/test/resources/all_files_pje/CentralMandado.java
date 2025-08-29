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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;


@Entity
@javax.persistence.Cacheable(true)
@Table(name = CentralMandado.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_central_mandado", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_central_mandado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class CentralMandado implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<CentralMandado,Integer> {

	public static final String TABLE_NAME = "tb_central_mandado";
	private static final long serialVersionUID = 1L;

	private int idCentralMandado;
	private String centralMandado;
	private Boolean ativo = Boolean.TRUE;

	private Localizacao localizacao;

	private List<CentralMandadoLocalizacao> centralMandadoLocalizacaoList = new ArrayList<>(0);
	private List<OficialJusticaCentralMandado> oficialJusticaCentralMandadoList = new ArrayList<>(0);
	private List<GrupoOficialJustica> grupoOficialJusticaList = new ArrayList<>(0);
	private List<ProcessoExpedienteCentralMandado> processoExpedienteCentralMandadoList = new ArrayList<>(0);

	public CentralMandado() {
	}

	@Id
	@GeneratedValue(generator = "gen_central_mandado")
	@Column(name = "id_central_mandado", unique = true, nullable = false)
	public int getIdCentralMandado() {
		return this.idCentralMandado;
	}

	public void setIdCentralMandado(int idCentralMandado) {
		this.idCentralMandado = idCentralMandado;
	}

	@Column(name = "ds_central_mandado", nullable = false, length = 150)
	@NotNull
	@Length(max = 150)
	public String getCentralMandado() {
		return this.centralMandado;
	}

	public void setCentralMandado(String centralMandado) {
		this.centralMandado = centralMandado;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "centralMandado")
	public List<OficialJusticaCentralMandado> getOficialJusticaCentralMandadoList() {
		return this.oficialJusticaCentralMandadoList;
	}

	public void setOficialJusticaCentralMandadoList(List<OficialJusticaCentralMandado> oficialJusticaCentralMandadoList) {
		this.oficialJusticaCentralMandadoList = oficialJusticaCentralMandadoList;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "centralMandado")
	public List<CentralMandadoLocalizacao> getCentralMandadoLocalizacaoList() {
		return this.centralMandadoLocalizacaoList;
	}

	public void setCentralMandadoLocalizacaoList(List<CentralMandadoLocalizacao> centralMandadoLocalizacaoList) {
		this.centralMandadoLocalizacaoList = centralMandadoLocalizacaoList;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "centralMandado")
	public List<GrupoOficialJustica> getGrupoOficialJusticaList() {
		return this.grupoOficialJusticaList;
	}

	public void setGrupoOficialJusticaList(List<GrupoOficialJustica> grupoOficialJusticaList) {
		this.grupoOficialJusticaList = grupoOficialJusticaList;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "centralMandado")
	public List<ProcessoExpedienteCentralMandado> getProcessoExpedienteCentralMandadoList() {
		return this.processoExpedienteCentralMandadoList;
	}

	public void setProcessoExpedienteCentralMandadoList(
			List<ProcessoExpedienteCentralMandado> processoExpedienteCentralMandadoList) {
		this.processoExpedienteCentralMandadoList = processoExpedienteCentralMandadoList;
	}

	@Transient
	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	@Override
	public String toString() {
		return centralMandado;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CentralMandado)) {
			return false;
		}
		CentralMandado other = (CentralMandado) obj;
		if (getIdCentralMandado() != other.getIdCentralMandado()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdCentralMandado();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends CentralMandado> getEntityClass() {
		return CentralMandado.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdCentralMandado());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
