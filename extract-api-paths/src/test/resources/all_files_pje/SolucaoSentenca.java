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
package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.jt.enums.SolucaoSentencaAudEnum;
import br.jus.pje.nucleo.enums.TipoSolucaoEnum;

/**
 * Classe de domínio que possui as possíveis soluções de sentença
 * 
 * @author kelly leal, rafael barros
 * @since versão 1.2.0
 * @category PJE-JT
 */
@Entity
@Table(name = SolucaoSentenca.TABLE_NAME)
public class SolucaoSentenca implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_solucao_sentenca";
	private static final long serialVersionUID = 1L;

	private int idSolucaoSentenca;
	private String descricao;
	private String codEvento;
	private TipoSolucaoEnum tipoSolucao;
	private SolucaoSentencaAudEnum solucaoSentencaAud;
	private int ordenacao;
	private Boolean ativo;

	@Id
	@Column(name = "id_solucao_sentenca", unique = true, nullable = false)
	public int getIdSolucaoSentenca() {
		return idSolucaoSentenca;
	}

	public void setIdSolucaoSentenca(int idSolucaoSentenca) {
		this.idSolucaoSentenca = idSolucaoSentenca;
	}

	@Column(name = "in_tipo_solucao")
	@Enumerated(EnumType.STRING)
	public TipoSolucaoEnum getTipoSolucao() {
		return tipoSolucao;
	}

	public void setTipoSolucao(TipoSolucaoEnum tipoSolucao) {
		this.tipoSolucao = tipoSolucao;
	}

	@Column(name = "ds_descricao", length = 200)
	@Length(max = 200)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name = "cd_evento", length = 30)
	@Length(max = 30)
	public String getCodEvento() {
		return codEvento;
	}

	public void setCodEvento(String codEvento) {
		this.codEvento = codEvento;
	}

	@Column(name = "in_solucao_sentenca_aud", length = 3, unique = true)
	@Enumerated(EnumType.STRING)
	public SolucaoSentencaAudEnum getSolucaoSentencaAud() {
		return solucaoSentencaAud;
	}

	public void setSolucaoSentencaAud(SolucaoSentencaAudEnum solucaoSentencaAud) {
		this.solucaoSentencaAud = solucaoSentencaAud;
	}

	@Column(name = "ordenacao")
	public int getOrdenacao() {
		return ordenacao;
	}

	public void setOrdenacao(int ordenacao) {
		this.ordenacao = ordenacao;
	}

	@Column(name = "in_ativo")
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
}
