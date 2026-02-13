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
package br.jus.pje.nucleo.entidades.editor;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.editor.PreferenciaEditorEnum;

@Entity
@Table(name = "tb_preferencia")
@org.hibernate.annotations.GenericGenerator(name = "gen_preferencia", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_preferencia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Preferencia implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<Preferencia,Integer> {

	private static final long serialVersionUID = 1L;
	
	private int idPreferencia;
	private PreferenciaEditorEnum preferenciaEditor;
	private String valor;
	private Usuario usuario;
	
	@Id
	@GeneratedValue(generator = "gen_preferencia")
	@Column(name = "id_preferencia")
	public int getIdPreferencia() {
		return idPreferencia;
	}
	
	public void setIdPreferencia(int idPreferencia) {
		this.idPreferencia = idPreferencia;
	}
	
	@Enumerated(EnumType.STRING)
	@Column(name = "ds_preferencia", nullable = false, length = 2)
	public PreferenciaEditorEnum getPreferenciaEditor() {
		return preferenciaEditor;
	}
	
	public void setPreferenciaEditor(PreferenciaEditorEnum preferencia) {
		this.preferenciaEditor = preferencia;
	}

	@Column(name = "vl_preferencia", nullable = false)
	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_usuario", nullable = false)
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Override
	public String toString() {
		return preferenciaEditor.getLabel();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((preferenciaEditor == null) ? 0 : preferenciaEditor.hashCode());
		result = prime * result + ((usuario == null) ? 0 : usuario.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Preferencia))
			return false;
		Preferencia other = (Preferencia) obj;
		if (preferenciaEditor == null) {
			if (other.preferenciaEditor != null)
				return false;
		} else if (!preferenciaEditor.equals(other.preferenciaEditor))
			return false;
		if (usuario == null) {
			if (other.usuario != null)
				return false;
		} else if (!usuario.equals(other.usuario))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Preferencia> getEntityClass() {
		return Preferencia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPreferencia());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
