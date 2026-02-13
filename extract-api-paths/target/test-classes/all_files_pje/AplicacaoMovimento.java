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
package br.jus.pje.nucleo.entidades.lancadormovimento;

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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.AplicabilidadeView;
import br.jus.pje.nucleo.entidades.Evento;

/**
 * Classe que representa a aplicacao dos complementos e movimentos em ums
 * determinada aplicabilidade de orgão da justiça, classe e sujeito ativo.
 */
@Entity
@Table(name = AplicacaoMovimento.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_aplic_movimento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_aplicacao_movimento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AplicacaoMovimento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<AplicacaoMovimento,Integer> {

	public static final String TABLE_NAME = "tb_aplicacao_movimento";
	private static final long serialVersionUID = 1L;

	private int idAplicacaoMovimento;
	private List<AplicacaoComplemento> aplicacaoComplementoList = new ArrayList<AplicacaoComplemento>();
	private Evento eventoProcessual;
	private String textoParametrizado;
	private AplicabilidadeView aplicabilidade;
	// Se permite alteração desses tipos de movimento na movimentação processual
	private Boolean permiteAlteracao;
	// Se permite exclusão desses tipos de movimento na movimentação processual
	private Boolean permiteExclusao;
	private Boolean ativo = Boolean.TRUE;

	public AplicacaoMovimento() {
		aplicacaoComplementoList = new ArrayList<AplicacaoComplemento>();
	}

	@Id
	@GeneratedValue(generator = "gen_aplic_movimento")
	@Column(name = "id_aplicacao_movimento", unique = true, nullable = false)
	public int getIdAplicacaoMovimento() {
		return this.idAplicacaoMovimento;
	}

	public void setIdAplicacaoMovimento(int idAplicacaoMovimento) {
		this.idAplicacaoMovimento = idAplicacaoMovimento;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "aplicacaoMovimento")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public List<AplicacaoComplemento> getAplicacaoComplementoList() {
		return aplicacaoComplementoList;
	}

	public void setAplicacaoComplementoList(List<AplicacaoComplemento> aplicacaoComplementoList) {
		this.aplicacaoComplementoList = aplicacaoComplementoList;
	}

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_evento", nullable = false)
	@ForeignKey(name = "id_evento_fkey")
	public Evento getEventoProcessual() {
		return eventoProcessual;
	}

	public void setEventoProcessual(Evento eventoProcessual) {
		this.eventoProcessual = eventoProcessual;
	}

	@Column(name = "ds_texto_parametrizado", length = 2000)
	@Length(max = 2000)
	public String getTextoParametrizado() {
		return textoParametrizado;
	}

	public void setTextoParametrizado(String textoParametrizado) {
		this.textoParametrizado = textoParametrizado;
	}

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_aplicabilidade", nullable = false)
	@ForeignKey(name = "id_aplicabilidade_fkey")
	public AplicabilidadeView getAplicabilidade() {
		return aplicabilidade;
	}

	public void setAplicabilidade(AplicabilidadeView aplicabilidade) {
		this.aplicabilidade = aplicabilidade;
	}

	@Column(name = "in_permite_alteracao")
	public Boolean getPermiteAlteracao() {
		return permiteAlteracao;
	}

	public void setPermiteAlteracao(Boolean permiteAlteracao) {
		this.permiteAlteracao = permiteAlteracao;
	}

	@Column(name = "in_permite_exclusao")
	public Boolean getPermiteExclusao() {
		return permiteExclusao;
	}

	public void setPermiteExclusao(Boolean permiteExclusao) {
		this.permiteExclusao = permiteExclusao;
	}
	
	@Override
	public String toString(){
		return aplicabilidade.toString();
	}
	
	@Column(name = "in_ativo", nullable = false)
  	@NotNull
  	public Boolean getAtivo(){
		return this.ativo;
  	}
  	
  	public void setAtivo(Boolean ativo){
  		this.ativo = ativo;
  	}
  	
	@Override
	@javax.persistence.Transient
	public Class<? extends AplicacaoMovimento> getEntityClass() {
		return AplicacaoMovimento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAplicacaoMovimento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
