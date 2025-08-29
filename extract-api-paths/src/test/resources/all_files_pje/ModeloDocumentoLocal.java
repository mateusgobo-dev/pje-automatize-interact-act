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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.TipoEditorEnum;


@Entity
@Table(name = "tb_modelo_doc_proc_local")
@PrimaryKeyJoinColumn(name = "id_modelo_doc_proc_local")
public class ModeloDocumentoLocal extends ModeloDocumento implements Serializable {

	private static final long serialVersionUID = 1L;

	private TipoProcessoDocumento tipoProcessoDocumento;
	private Localizacao localizacao;
	private TipoEditorEnum tipoEditor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_processo_documento", nullable = false)
	@NotNull
	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_localizacao", nullable = false)
	@NotNull
	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="tp_editor")	
	public TipoEditorEnum getTipoEditor() {
		return tipoEditor;
	}
	
	public void setTipoEditor(TipoEditorEnum tipoEditor) {
		this.tipoEditor = tipoEditor;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if(!ModeloDocumento.class.isAssignableFrom(obj.getClass())){
			return false;
		}
		ModeloDocumento md = (ModeloDocumento) obj;
		Integer id = this.getIdModeloDocumento();
		Integer idObj = (Integer) md.getIdModeloDocumento();
		return id.equals(idObj);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdModeloDocumento();
		return result;
	}

	@Override
	@Transient
	public Class<? extends ModeloDocumento> getEntityClass() {
		return ModeloDocumentoLocal.class;
	}
}
