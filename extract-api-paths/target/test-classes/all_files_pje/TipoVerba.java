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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tb_tipo_verba")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_verba", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_verba"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoVerba implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoVerba,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idTipoVerba;
	private String nomeVerba;

	@Id
	@GeneratedValue(generator = "gen_tipo_verba")
	@Column(name = "id_tipo_verba", unique = true, nullable = false)
	public Integer getIdTipoVerba() {
		return idTipoVerba;
	}

	public void setIdTipoVerba(Integer idAcordoVerba) {
		this.idTipoVerba = idAcordoVerba;
	}

	@Column(name = "nm_verba")
	public String getNomeVerba() {
		return nomeVerba;
	}

	public void setNomeVerba(String nomeVerba) {
		this.nomeVerba = nomeVerba;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoVerba> getEntityClass() {
		return TipoVerba.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdTipoVerba();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
