/**
 * pje-comum
 * Copyright (C) 2014 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

/**
 * Entidade destinada a representar os tipos possíveis de situação que um processo judicial 
 * pode apresentar.
 * 
 * código identificador em formato de texto sem espaços, 
 * nome, 
 * descrição curta, 
 * lista de instâncias em que pode ser aplicada, lista de tipos de situação conflitantes, marca indicativa da possibilidade de sua utilização em novos registros de situação processual;
 *
 */
@Entity
@Table(name="tb_tipo_sit_processual")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_sit_processual", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_sit_processual"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoSituacaoProcessual implements Serializable {

	private static final long serialVersionUID = -1536649352410846024L;

	@Id
	@GeneratedValue(generator = "gen_tipo_sit_processual", strategy = GenerationType.AUTO)
	@Column(name = "id", unique = true, nullable = false)
    private Long id;
    
	@Basic(optional=false)
	@NotNull
	@Length(min=3, max=32)
	@Column(name="ds_codigo", nullable=false, length=32)
    private String codigo;
    
	@Basic(optional=false)
	@NotNull
	@Length(min=3, max=128)
	@Column(name="ds_nome", nullable=false, length=128)
    private String nome;
    
	@Basic(optional=true)
	@Length(min=3, max=256)
	@Column(name="ds_descricao", length=256)
    private String descricao;
    
	@Basic(optional=true)
	@Column(name = "in_ativo", nullable = false)
    private Boolean ativo = Boolean.TRUE;
    
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "tb_tp_sit_incompat", 
			joinColumns = {@JoinColumn(name = "id_tipo", nullable = false, updatable = false)}, 
			inverseJoinColumns = {@JoinColumn(name = "id_tipo_incompativel", nullable = false, updatable = false) })
    private Set<TipoSituacaoProcessual> tiposSituacoesIncompatives = new HashSet<TipoSituacaoProcessual>(0);
    
    public TipoSituacaoProcessual() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public Set<TipoSituacaoProcessual> getTiposSituacoesIncompatives() {
		return tiposSituacoesIncompatives;
	}

	public void setTiposSituacoesIncompatives(Set<TipoSituacaoProcessual> tiposSituacoesIncompatives) {
		this.tiposSituacoesIncompatives = tiposSituacoesIncompatives;
	}

}
