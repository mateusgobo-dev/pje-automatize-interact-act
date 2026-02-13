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
package br.jus.pje.jt.entidades.estatistica;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Sérgio Pacheco / Sérgio Simoes
 * @since 1.4.3
 * @category PJE-JT
 * @class FormatoItem
 * @description Classe que representa o formato de apresentação de um ItemQuadro. 
 */
@Entity
@Table(name = ItemQuadro.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_formato_item", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_formato_item"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class FormatoItem implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<FormatoItem,Integer> {

	public static final String TABLE_NAME = "tb_formato_item";
	private static final long serialVersionUID = 1L;

	private Integer idFormatoItem;

	private String nome;
	 
	private String formato;

	@Id
	@GeneratedValue(generator = "gen_formato_item")
	@Column(name = "id_formato_item", unique = true, nullable = false)
	public Integer getIdFormatoItem() {
		return this.idFormatoItem;
	}

	public void setIdFormatoItem(Integer idFormatoItem) {
		this.idFormatoItem = idFormatoItem;
	}

	@Column(name="ds_nome", nullable=false)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name="ds_formato", nullable=false)
	public String getFormato() {
		return formato;
	}

	public void setFormato(String formato) {
		this.formato = formato;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends FormatoItem> getEntityClass() {
		return FormatoItem.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdFormatoItem();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
