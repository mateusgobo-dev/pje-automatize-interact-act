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
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_item_tipo_documento")
@org.hibernate.annotations.GenericGenerator(name = "gen_item_tipo_documento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_item_tipo_documento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ItemTipoDocumento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ItemTipoDocumento,Integer> {

	private static final long serialVersionUID = 1L;

	private int idItemTipoDocumento;
	private Localizacao localizacao;
	private GrupoModeloDocumento grupoModeloDocumento;

	public ItemTipoDocumento() {
	}

	@Id
	@GeneratedValue(generator = "gen_item_tipo_documento")
	@Column(name = "id_item_tipo_documento", unique = true, nullable = false)
	public int getIdItemTipoDocumento() {
		return this.idItemTipoDocumento;
	}

	public void setIdItemTipoDocumento(int idItemTipoDocumento) {
		this.idItemTipoDocumento = idItemTipoDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_localizacao")
	public Localizacao getLocalizacao() {
		return this.localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_grupo_modelo_documento", nullable = false)
	@NotNull
	public GrupoModeloDocumento getGrupoModeloDocumento() {
		return this.grupoModeloDocumento;
	}

	public void setGrupoModeloDocumento(GrupoModeloDocumento grupoModeloDocumento) {
		this.grupoModeloDocumento = grupoModeloDocumento;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ItemTipoDocumento) {
			ItemTipoDocumento itemTipoDocumento = (ItemTipoDocumento) obj;

			if (this.getIdItemTipoDocumento() != 0 && itemTipoDocumento.getIdItemTipoDocumento() != 0
					&& this.getIdItemTipoDocumento() == itemTipoDocumento.getIdItemTipoDocumento()) {
				return true;
			} else {
				return (this.getLocalizacao() != null && itemTipoDocumento.getLocalizacao() != null)
						&& this.getLocalizacao().getIdLocalizacao() == itemTipoDocumento.getLocalizacao()
								.getIdLocalizacao()
						&& (this.getGrupoModeloDocumento() != null && itemTipoDocumento.getGrupoModeloDocumento() != null)
						&& this.getGrupoModeloDocumento().getIdGrupoModeloDocumento() == itemTipoDocumento
								.getGrupoModeloDocumento().getIdGrupoModeloDocumento();
			}
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdItemTipoDocumento();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ItemTipoDocumento> getEntityClass() {
		return ItemTipoDocumento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdItemTipoDocumento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
