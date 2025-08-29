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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.ProcessoDocumento;


@Entity
@Table(name = DocumentoVoto.TABLE_NAME)
@PrimaryKeyJoinColumn(name="id_documento_voto")
public class DocumentoVoto extends ProcessoDocumento implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_documento_voto";
	private static final long serialVersionUID = 1L;

	private Voto voto;
	
	public DocumentoVoto() {
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_voto", nullable = false)
	@NotNull
	public Voto getVoto() {
		return voto;
	}

	public void setVoto(Voto voto) {
		this.voto = voto;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoDocumento> getEntityClass() {
		return DocumentoVoto.class;
	}
}