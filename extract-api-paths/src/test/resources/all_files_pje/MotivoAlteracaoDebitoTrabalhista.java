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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

/**
 * Classe de representa a situação do débito trabalhista, que é associada ao
 * movimento que será lançado
 * 
 * @author kelly leal
 * @since versão 1.2.3
 * @category PJE-JT
 */
@Entity
@Table(name = MotivoAlteracaoDebitoTrabalhista.TABLE_NAME)
public class MotivoAlteracaoDebitoTrabalhista implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_mot_alt_dbto_trabalhsta";

	private int idMotivo;
	private String descricao;
	private String codComplemento;
	private SituacaoDebitoTrabalhista situacao;

	@Id
	@Column(name = "id_motivo", unique = true, nullable = false)
	@NotNull
	public int getIdMotivo() {
		return idMotivo;
	}

	public void setIdMotivo(int idMotivo) {
		this.idMotivo = idMotivo;
	}

	@Column(name = "ds_descricao", length = 200, nullable = false)
	@Length(max = 200)
	@NotNull
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name = "cd_complemento", length = 10, nullable = false)
	@Length(max = 10)
	@NotNull
	public String getCodComplemento() {
		return codComplemento;
	}

	public void setCodComplemento(String codComplemento) {
		this.codComplemento = codComplemento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_situacao_debito_trabalhista", nullable = false)
	@NotNull
	public SituacaoDebitoTrabalhista getSituacao() {
		return situacao;
	}

	public void setSituacao(SituacaoDebitoTrabalhista situacao) {
		this.situacao = situacao;
	}

}