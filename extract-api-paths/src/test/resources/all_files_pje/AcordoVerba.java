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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tb_acordo_verba")
@org.hibernate.annotations.GenericGenerator(name = "gen_acordo_verba", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_acordo_verba"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AcordoVerba implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<AcordoVerba,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idAcordoVerba;
	private Integer idAcordo;
	private Integer idTipoVerba;
	private Acordo acordo;
	private TipoVerba tipoVerba;
	private Double valorVerba;

	@Id
	@Column(name = "id_acordo_verba", unique = true, nullable = false)
	@GeneratedValue(generator = "gen_acordo_verba")
	public Integer getIdAcordoVerba() {
		return idAcordoVerba;
	}

	public void setIdAcordoVerba(Integer idAcordoVerba) {
		this.idAcordoVerba = idAcordoVerba;
	}

	@Column(name = "id_acordo", updatable = false, insertable = false)
	public Integer getIdAcordo() {
		return idAcordo;
	}

	public void setIdAcordo(Integer idAcordo) {
		this.idAcordo = idAcordo;
	}

	@Column(name = "id_tipo_verba", updatable = false, insertable = false)
	public Integer getIdTipoVerba() {
		return idTipoVerba;
	}

	public void setIdTipoVerba(Integer idTipoVerba) {
		this.idTipoVerba = idTipoVerba;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_acordo", nullable = false)
	public Acordo getAcordo() {
		return acordo;
	}

	public void setAcordo(Acordo acordo) {
		this.acordo = acordo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_verba", nullable = false)
	public TipoVerba getTipoVerba() {
		return tipoVerba;
	}

	public void setTipoVerba(TipoVerba tv) {
		this.tipoVerba = tv;
	}

	@Column(name = "vl_verba")
	public Double getValorVerba() {
		return valorVerba;
	}

	public void setValorVerba(Double valorVerba) {
		this.valorVerba = valorVerba;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AcordoVerba> getEntityClass() {
		return AcordoVerba.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdAcordoVerba();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
