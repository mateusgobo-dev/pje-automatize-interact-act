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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
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
@Table(name = SituacaoDebitoTrabalhista.TABLE_NAME)
public class SituacaoDebitoTrabalhista implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5085182447289048674L;

	public static final String TABLE_NAME = "tb_sit_debito_trabalhista";

	private int idSituacaoDebitoTrabalhista;
	private String descricao;
	private TipoOperacaoEnum operacao;
	private String codComplemento;
	private String codXML;

	@Id
	@Column(name = "id_situacao_debito_trabalhista", unique = true, nullable = false)
	@NotNull
	public int getIdSituacaoDebitoTrabalhista() {
		return idSituacaoDebitoTrabalhista;
	}

	public void setIdSituacaoDebitoTrabalhista(int idSituacaoDebitoTrabalhista) {
		this.idSituacaoDebitoTrabalhista = idSituacaoDebitoTrabalhista;
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

	@Column(name = "in_tipo_operacao")
	@Enumerated(EnumType.STRING)
	public TipoOperacaoEnum getOperacao() {
		return operacao;
	}

	public void setOperacao(TipoOperacaoEnum operacao) {
		this.operacao = operacao;
	}

	@Column(name = "cd_xml", length = 2, nullable = false)
	@Length(max = 2)
	@NotNull
	public String getCodXML() {
		return codXML;
	}

	public void setCodXML(String codXML) {
		this.codXML = codXML;
	}

}