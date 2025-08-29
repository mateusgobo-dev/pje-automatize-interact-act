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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import br.jus.pje.nucleo.entidades.log.Ignore;

@Entity
@Ignore
@Table(name = "tb_processo_push_fila")
@SequenceGenerator(allocationSize = 1, name = "gen_processo_push_fila", sequenceName = "sq_tb_processo_push_fila")
public class ProcessoPushFila implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer idProcessoPushFila;
	private Integer idProcesso;
	private String listaEmails;
	private String listaMovimentacoes;
	private Boolean emProcessamento = Boolean.FALSE;
	private Date dtAtualizacao;

	public ProcessoPushFila() {
	}

	public ProcessoPushFila(Integer idProcesso, String emails, String movimentacoes) {
		this.idProcesso = idProcesso;
		this.listaEmails = emails;
		this.listaMovimentacoes = movimentacoes;
	}

	@Id
	@GeneratedValue(generator = "gen_processo_push_fila")
	@Column(name = "id_processo_push_fila")
	public Integer getIdProcessoPushFila() {
		return idProcessoPushFila;
	}

	public void setIdProcessoPushFila(Integer idProcessoPushFila) {
		this.idProcessoPushFila = idProcessoPushFila;
	}

	@Column(name = "id_processo")
	public Integer getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(Integer idProcessoTrf) {
		this.idProcesso = idProcessoTrf;
	}

	@Column(name = "ds_lista_emails")
	public String getListaEmail() {
		return listaEmails;
	}

	public void setListaEmail(String emails) {
		this.listaEmails = emails;
	}

	@Column(name = "ds_movimentacao")
	public String getListaMovimentacao() {
		return listaMovimentacoes;
	}

	public void setListaMovimentacao(String movimentos) {
		this.listaMovimentacoes = movimentos;
	}

	@Column(name = "in_em_processamento")
	public Boolean getEmProcessamento() {
		return emProcessamento;
	}

	public void setEmProcessamento(Boolean emProcessamento) {
		this.emProcessamento = emProcessamento;
	}

	@Column(name = "dt_atualizacao")
	public Date getDtAtualizacao() {
		return dtAtualizacao;
	}

	public void setDtAtualizacao(Date dtAtualizacao) {
		this.dtAtualizacao = dtAtualizacao;
	}
}
