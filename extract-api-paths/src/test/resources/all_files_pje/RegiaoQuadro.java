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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.OrderBy;

/**
 * @author Sérgio Pacheco / Sérgio Simoes
 * @since 1.4.3
 * @category PJE-JT
 * @class RegiaoQuadro
 * @description Classe que representa a definicao de uma regiao dentro de um 
 * 				quadro de boletim de orgao julgador. 
 */
@Entity
@Table(name = RegiaoQuadro.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_regiao_quadro", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_regiao_quadro"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RegiaoQuadro  implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RegiaoQuadro,Integer> {
 
	public static final String TABLE_NAME = "tb_regiao_quadro";
	private static final long serialVersionUID = 1L;

	private Integer idRegiaoQuadro;
	
	private Integer ordem;
	 
	private String nome;
	 
	private String descricao;
	 
	private Quadro quadro;
	 
	private List<ItemQuadro> itensQuadro = new ArrayList<ItemQuadro>(0);
	
	private Boolean dinamica;
	
	@Id
	@GeneratedValue(generator = "gen_regiao_quadro")
	@Column(name = "id_regiao_quadro", unique = true, nullable = false)
	public Integer getIdRegiaoQuadro() {
		return this.idRegiaoQuadro;
	}

	public void setIdRegiaoQuadro(Integer idRegiaoQuadro) {
		this.idRegiaoQuadro = idRegiaoQuadro;
	}
	
	public RegiaoQuadro() {
		super();
	}

	public RegiaoQuadro(Quadro quadro) {
		super();
		this.quadro = quadro;
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

	@Column(name = "ds_descricao")
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_quadro", nullable = false)
	@ForeignKey(name = "fk_tb_regiao_quadro_tb_quadro_boletim")
	@NotNull
	public Quadro getQuadro() {
		return quadro;
	}

	public void setQuadro(Quadro quadro) {
		this.quadro = quadro;
	}

	@Transient
	public List<ItemQuadro> getItensQuadroNGeracao( Integer n ) {
		List<ItemQuadro> lista1Geracao = new ArrayList<ItemQuadro>(0);
		for (ItemQuadro itemQuadro : getItensQuadro()) {
			if (itemQuadro.geracao().equals(n)) {
				lista1Geracao.add(itemQuadro);
			}
		}
		return lista1Geracao;
	}	

	public Integer maxGeracoes() {
		int max = 0;
		for (ItemQuadro itemQuadro : getItensQuadro()) {
			int geracao = itemQuadro.geracao();
			max = geracao > max ? geracao : max;
		}
		return max;
	}	
	
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "regiaoQuadro")
	@OrderBy(clause = "nr_ordem" )
	public List<ItemQuadro> getItensQuadro() {
		return itensQuadro;
	}

	public void setItensQuadro(List<ItemQuadro> itensQuadro) {
		this.itensQuadro = itensQuadro;
	}

	public ItemElementar addItemElementar() {
		ItemElementar itemElementar = new ItemElementar(this);
		itensQuadro.add(itemElementar);
		return itemElementar;
	}	
	 
	public ItemQuadro addItemQuadro() {
		ItemQuadro itemQuadro = new ItemQuadro(this);
		itensQuadro.add(itemQuadro);
		return itemQuadro;
	}
	
	@Column(name = "in_dinamica", nullable = false)
	public Boolean getDinamica() {
		return dinamica;
	}

	public void setDinamica(Boolean dinamica) {
		this.dinamica = dinamica;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RegiaoQuadro)) {
			return false;
		}
		RegiaoQuadro outraRegiaoQuadro = (RegiaoQuadro) obj;
		if (getIdRegiaoQuadro() == 0 && getQuadro() != null) {
			if( getQuadro().equals(outraRegiaoQuadro.getQuadro()) &&
				getOrdem().equals(outraRegiaoQuadro.getOrdem())	)
			return true;
		}
		return getIdRegiaoQuadro().equals(outraRegiaoQuadro.getIdRegiaoQuadro());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getIdRegiaoQuadro() == null) ? 0 : getIdRegiaoQuadro().hashCode());
		result = prime * result + ((getQuadro() == null) ? 0 : getQuadro().hashCode());
		result = prime * result + ((getOrdem() == null) ? 0 : getOrdem().hashCode());
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RegiaoQuadro> getEntityClass() {
		return RegiaoQuadro.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdRegiaoQuadro();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
