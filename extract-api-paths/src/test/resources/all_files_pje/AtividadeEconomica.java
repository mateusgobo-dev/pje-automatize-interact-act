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
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.anotacoes.Parent;

@Entity
@Table(name = AtividadeEconomica.TABLE_NAME)
public class AtividadeEconomica implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_atividade_economica";

	private int idAtividadeEconomica;
	private String nomeAtividadeEconomica;
	private Date dataAtualizacao;
	private Boolean ativo;
	private AtividadeEconomica atividadeEconomicaPai;
	private List<AtividadeEconomica> atividadeEconomicaList = new ArrayList<AtividadeEconomica>();

	@Id
	@Column(name = "id_atividade_economica", unique = true, nullable = false)
	@NotNull
	public int getIdAtividadeEconomica() {
		return idAtividadeEconomica;
	}

	public void setIdAtividadeEconomica(int idAtividadeEconomica) {
		this.idAtividadeEconomica = idAtividadeEconomica;
	}

	@Column(name = "nome_atividade_economica", length = 100, nullable = false)
	@Length(max = 100)
	@NotNull
	public String getNomeAtividadeEconomica() {
		return nomeAtividadeEconomica;
	}

	public void setNomeAtividadeEconomica(String nomeAtividadeEconomica) {
		this.nomeAtividadeEconomica = nomeAtividadeEconomica;
	}

	@Column(name = "dt_atualizacao", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_atividade_economica_pai")
	@Parent
	public AtividadeEconomica getAtividadeEconomicaPai() {
		return atividadeEconomicaPai;
	}

	public void setAtividadeEconomicaPai(AtividadeEconomica atividadeEconomicaPai) {
		this.atividadeEconomicaPai = atividadeEconomicaPai;
	}

	@Override
	public String toString() {
		return nomeAtividadeEconomica;
	}

	@OneToMany(cascade = { CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "atividadeEconomicaPai")
	public List<AtividadeEconomica> getAtividadeEconomicaList(){
		return atividadeEconomicaList;
	}

	public void setAtividadeEconomicaList(List<AtividadeEconomica> atividadeEconomicaList){
		this.atividadeEconomicaList = atividadeEconomicaList;
	}

}
