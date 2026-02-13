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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;

/**
 * @author Sérgio Pacheco / Sérgio Simoes
 * @since 1.4.3
 * @see ItemElementar
 * @category PJE-JT
 * @class ItemQuadro
 * @description Classe que representa a definicao de um item de quadro de boletim
 *              de orgao julgador. Define um estrutura em árvore.
 *              As folhas são ItemElementar, que possuem valor. 
 */
@Entity
@Table(name = ItemQuadro.TABLE_NAME)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "in_tipo_item", discriminatorType = DiscriminatorType.STRING, length = 1)
@DiscriminatorValue(value = "H")
@org.hibernate.annotations.GenericGenerator(name = "gen_item_quadro", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_item_quadro"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ItemQuadro implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ItemQuadro,Integer> {

	public static final String TABLE_NAME = "tb_item_quadro";
	private static final long serialVersionUID = 1L;
	
	private Integer idItemQuadro;
	
	private Integer ordem;
	 
	private String nome;
	 
	private String descricao;
	 
	private String definicao;
	 
	private RegiaoQuadro regiaoQuadro;
	 
	private ItemQuadro pai;
	 
	private List<ItemQuadro> filhos = new ArrayList<ItemQuadro>(0);
	 
	private FormatoItem formatoItem;
	
	@Id
	@GeneratedValue(generator = "gen_item_quadro")
	@Column(name = "id_item_quadro", unique = true, nullable = false)
	public Integer getIdItemQuadro() {
		return this.idItemQuadro;
	}

	public void setIdItemQuadro(Integer idItemQuadro) {
		this.idItemQuadro = idItemQuadro;
	}
	
	public ItemQuadro() {
		super();
	}

 	public ItemQuadro(RegiaoQuadro regiaoQuadro) {
		super();
		this.regiaoQuadro = regiaoQuadro;
	}

	@Column(name = "nr_ordem", nullable = false)
	public Integer getOrdem() {
		return ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	@Column(name = "ds_nome", nullable = false)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "ds_descricao", nullable = false)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name = "ds_definicao", nullable = false)
	public String getDefinicao() {
		return definicao;
	}

	public void setDefinicao(String definicao) {
		this.definicao = definicao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_regiao_quadro", nullable = false)
	@ForeignKey(name = "fk_tb_item_quadro_tb_regiao_quadro")
	@NotNull
	public RegiaoQuadro getRegiaoQuadro() {
		return regiaoQuadro;
	}

	public void setRegiaoQuadro(RegiaoQuadro regiaoQuadro) {
		this.regiaoQuadro = regiaoQuadro;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_item_pai")
	@ForeignKey(name = "fk_tb_item_quadro_tb_item_quadro")
	public ItemQuadro getPai() {
		return pai;
	}

	public void setPai(ItemQuadro pai) {
		this.pai = pai;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "pai")
	public List<ItemQuadro> getFilhos() {
		return filhos;
	}

	public void setFilhos(List<ItemQuadro> filhos) {
		this.filhos = filhos;
	}

	public ItemQuadro addFilho() {
		ItemQuadro itemQuadro = new ItemQuadro(null);
		itemQuadro.setPai(this);
		this.filhos.add(itemQuadro);
		return itemQuadro;
	}	
	
	public ItemElementar addFilhoElementar() {
		ItemElementar itemElementar = new ItemElementar(null);
		itemElementar.setPai(this);
		this.filhos.add(itemElementar);
		return itemElementar;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_formato_item")
	@ForeignKey(name = "fk_tb_item_quadro_tb_formato_item")
	public FormatoItem getFormatoItem() {
		return formatoItem;
	}

	public void setFormatoItem(FormatoItem formatoItem) {
		this.formatoItem = formatoItem;
	}

	@Transient
	public Boolean isItemElementar() {
		return this instanceof ItemElementar;
	}

	public Integer nivel() {  
		Integer maxNivel = 0;
		if ( this.isItemElementar() ) { // Cláusula base
			return 1;
		} else { 						// Cláusula de Indução
			for (ItemQuadro itemQuadro : this.getFilhos()) {
				Integer meuNivel = itemQuadro.nivel() + 1;
				maxNivel = meuNivel > maxNivel ? meuNivel : maxNivel;  
			}
		}
		return maxNivel;
	}
	
	public Integer geracao() {
		if ( this.getPai()==null) { // Cláusula Base
			return 1;				
		} else {					// Cláusula de Indução
			return this.getPai().geracao() + 1;
		}
	}

	public Integer qtdElementares() {
		Integer qtd = 0;
		if ( this.isItemElementar() ) { // Cláusula base
			return 1;
		} else { 						// Cláusula de Indução
			for (ItemQuadro itemQuadro : this.getFilhos()) {
				Integer qtdElementaresFilho = itemQuadro.qtdElementares();
				qtd += qtdElementaresFilho;  
			}
		}
		return qtd;
	}
	
	@Transient
	public List<ItemQuadro> getFilhosProximaGeracao() {
		List<ItemQuadro> listaProxGeracao = new ArrayList<ItemQuadro>(0);
		for ( ItemQuadro itemQuadro : this.getFilhos() ) {
			if ( itemQuadro.geracao() == this.geracao()+1 ) {
				listaProxGeracao.add(itemQuadro);
			}
		}
		return listaProxGeracao;
	}	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ItemQuadro)) {
			return false;
		}
		ItemQuadro outroItem = (ItemQuadro) obj;
		return this.getIdItemQuadro().equals(outroItem.getIdItemQuadro());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getIdItemQuadro() == null) ? 0 : getIdItemQuadro().hashCode());
		result = prime * result + ((getOrdem() == null) ? 0 : getOrdem().hashCode());
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ItemQuadro> getEntityClass() {
		return ItemQuadro.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdItemQuadro();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
