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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;


@Entity
@javax.persistence.Cacheable(true)
@Table(name = CentralMandadoLocalizacao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_cntral_mnddo_localizacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_cntral_mnddo_localizacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class CentralMandadoLocalizacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<CentralMandadoLocalizacao,Integer> {

	public static final String TABLE_NAME = "tb_central_mandado_localiz";
	private static final long serialVersionUID = 1L;

	private int idCentralMandadoLocalizacao;
	private Localizacao localizacao;
	private CentralMandado centralMandado;

	public CentralMandadoLocalizacao() {
	}

	@Id
	@GeneratedValue(generator = "gen_cntral_mnddo_localizacao")
	@Column(name = "id_central_mandado_localizacao", unique = true, nullable = false)
	public int getIdCentralMandadoLocalizacao() {
		return this.idCentralMandadoLocalizacao;
	}

	public void setIdCentralMandadoLocalizacao(int idCentralMandadoLocalizacao) {
		this.idCentralMandadoLocalizacao = idCentralMandadoLocalizacao;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_localizacao", nullable = false)
	@NotNull
	public Localizacao getLocalizacao() {
		return this.localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_central_mandado", nullable = false)
	@NotNull
	public CentralMandado getCentralMandado() {
		return this.centralMandado;
	}

	public void setCentralMandado(CentralMandado centralMandado) {
		this.centralMandado = centralMandado;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CentralMandadoLocalizacao)) {
			return false;
		}
		CentralMandadoLocalizacao other = (CentralMandadoLocalizacao) obj;
		if (getIdCentralMandadoLocalizacao() != other.getIdCentralMandadoLocalizacao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdCentralMandadoLocalizacao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends CentralMandadoLocalizacao> getEntityClass() {
		return CentralMandadoLocalizacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdCentralMandadoLocalizacao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
