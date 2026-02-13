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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;

/**
 * @author Sérgio Pacheco / Sérgio Simoes
 * @since 1.4.3
 * @category PJE-JT
 * @class ValorItem
 * @description Classe que representa o valor de item de um Quadro do boletim. 
 */
@Entity
@Table(name = ValorItem.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_valor_item", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_valor_item"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ValorItem implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ValorItem,Integer> {
 
	public static final String TABLE_NAME = "tb_valor_item";
	private static final long serialVersionUID = 1L;
	
	private Integer idValorItem;
 
	private String valor;
	 
	private ItemElementar itemElementar;
	
	private Relatorio relatorio;
	
	private Regiao regiao ;

	@Id
	@GeneratedValue(generator = "gen_valor_item")
	@Column(name = "id_valor_item", unique = true, nullable = false)
	public Integer getIdValorItem() {
		return idValorItem;
	}

	public void setIdValorItem(Integer idValorItem) {
		this.idValorItem = idValorItem;
	}

	public ValorItem() {
		super();
	}
	
	public ValorItem(ItemElementar itemElementar) {
		super();
		this.itemElementar = itemElementar;
	}

	@Column(name = "ds_valor")
	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_item_elementar", nullable = false)
	@ForeignKey(name = "fk_tb_valor_item_tb_item_elementar")
	@NotNull
	public ItemElementar getItemElementar() {
		return itemElementar;
	}

	public void setItemElementar(ItemElementar itemElementar) {
		this.itemElementar = itemElementar;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_relatorio", nullable = false)
	@ForeignKey(name = "fk_tb_valor_item_tb_relatorio_boletim")
	@NotNull
	public Relatorio getRelatorio() {
		return relatorio;
	}

	public void setRelatorio(Relatorio relatorio) {
		this.relatorio = relatorio;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_regiao", nullable = false)
	@ForeignKey(name = "fk_tb_valor_item_tb_regiao_relatorio")
	public Regiao getRegiao() {
		return regiao;
	}

	public void setRegiao(Regiao regiao) {
		this.regiao = regiao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ValorItem> getEntityClass() {
		return ValorItem.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdValorItem();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
