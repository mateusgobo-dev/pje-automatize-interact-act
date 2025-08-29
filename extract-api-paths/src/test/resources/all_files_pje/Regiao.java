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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.OrderBy;

/**
 * @author Sérgio Pacheco / Sérgio Simoes
 * @since 1.4.4
 * @category PJE-JT
 * @class Regiao
 * @description Classe que representa uma regiao dinâmica de um relatório 
 *              (região definida para uma instância de relatório).
 */
@Entity
@Table(name = Regiao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_regiao_relatorio", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_regiao_relatorio"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Regiao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Regiao,Integer> {
	
	public static final String TABLE_NAME = "tb_regiao_relatorio";
	private static final long serialVersionUID = 1L;
	
	private Integer idRegiao;
	
	private Integer ordem ;
	
	private String nome ;
	
	private String descricao ;
	
	private Relatorio relatorio;
	
	private RegiaoQuadro regiaoQuadro ;
	
	private List<ValorItem> valoresItem;
	
	@Id
	@GeneratedValue(generator = "gen_regiao_relatorio")
	@Column(name = "id_regiao", unique = true, nullable = false)
	public Integer getIdRegiao() {
		return idRegiao;
	}
	public void setIdRegiao(Integer idRegiao) {
		this.idRegiao = idRegiao;
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
	@JoinColumn(name = "id_relatorio", nullable = true)
	@ForeignKey(name = "fk_tb_regiao_relatorio_tb_relatorio_boletim")
	@NotNull
	public Relatorio getRelatorio() {
		return relatorio;
	}
	
	public void setRelatorio(Relatorio relatorio) {
		this.relatorio = relatorio;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_regiao_quadro", nullable = false)
	@ForeignKey(name = "fk_tb_regiao_relatorio_tb_regiao_quadro")
	@NotNull	
	public RegiaoQuadro getRegiaoQuadro() {
		return regiaoQuadro;
	}
	public void setRegiaoQuadro(RegiaoQuadro regiaoQuadro) {
		this.regiaoQuadro = regiaoQuadro;
	}
	
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "regiao")
	@OrderBy(clause="id_valor_item")
	public List<ValorItem> getValoresItem() {
		return valoresItem;
	}

	public void setValoresItem(List<ValorItem> valoresItem) {
		this.valoresItem = valoresItem;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Regiao> getEntityClass() {
		return Regiao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdRegiao();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
