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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @author Rodrigo Cartaxo / Sérgio Pacheco
 * @since 1.2.0
 * @see
 * @category PJE-JT
 * @class CategoriaRubrica
 * @description Classe que representa uma categoria de tipos de rubrica. Ex:
 *              Principal, Honorario, Multa, ...
 */

@Entity
@Table(name = "tb_categoria_rubrica")
@org.hibernate.annotations.GenericGenerator(name = "gen_categoria_rubrica", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_categoria_rubrica"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class CategoriaRubrica implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<CategoriaRubrica,Long> {

	private static final long serialVersionUID = 1L;

	private Long id;
	private Boolean atualizacaoColetiva;
	private String descricao;
	private List<TipoRubrica> tipoRubricaList = new ArrayList<TipoRubrica>();

	@Id
	@GeneratedValue(generator = "gen_categoria_rubrica")
	@Column(name = "id_categoria_rubrica", unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "in_atualizacao_coletiva", nullable = false)
	public Boolean getAtualizacaoColetiva() {
		return atualizacaoColetiva;
	}

	public void setAtualizacaoColetiva(Boolean atualizacaoColetiva) {
		this.atualizacaoColetiva = atualizacaoColetiva;
	}

	@Column(name = "ds_descricao")
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "categoriaRubrica")
	public List<TipoRubrica> getTipoRubricaList() {
		return tipoRubricaList;
	}

	public void setTipoRubricaList(List<TipoRubrica> tipoRubricaList) {
		this.tipoRubricaList = tipoRubricaList;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends CategoriaRubrica> getEntityClass() {
		return CategoriaRubrica.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
