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
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vs_aud_tipo_verba")
public class AudTipoVerba implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private String tipoVerba;

	@Id
	@Column(name = "id_tipo_verba", unique = true, insertable = false, updatable = false)
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "nm_verba", nullable = false, insertable = false, updatable = false)
	public String getTipoVerba() {
		return tipoVerba;
	}

	public void setTipoVerba(String tipoVerba) {
		this.tipoVerba = tipoVerba;
	}
}