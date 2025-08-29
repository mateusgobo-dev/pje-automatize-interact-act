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
package br.jus.pje.nucleo.entidades.ws.consulta;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "vs_processo_evento_webservice")
public class ConsultaProcessoEventoWebservice implements Serializable {

	private static final long serialVersionUID = 1L;

	private int idProcessoEvento;
	private int idProcessoTrf;
	private String nomeUsuario;
	private String numeroCpfUsuario;
	private String numeroCNPJUsuario;
	private String codigoEvento;
	private Date dataAtualizacao;

	@Id
	@Column(name = "id_processo_evento", insertable = false, updatable = false)
	public int getIdProcessoEvento() {
		return idProcessoEvento;
	}

	public void setIdProcessoEvento(int idProcessoEvento) {
		this.idProcessoEvento = idProcessoEvento;
	}

	@Column(name = "ds_nome", insertable = false, updatable = false)
	public String getNomeUsuario() {
		return nomeUsuario;
	}

	public void setNomeUsuario(String nomeUsuario) {
		this.nomeUsuario = nomeUsuario;
	}

	@Column(name = "nr_cpf", insertable = false, updatable = false)
	public String getNumeroCpfUsuario() {
		return numeroCpfUsuario;
	}

	public void setNumeroCpfUsuario(String numeroCpfUsuario) {
		this.numeroCpfUsuario = numeroCpfUsuario;
	}

	@Column(name = "nr_cnpj", insertable = false, updatable = false)
	public String getNumeroCNPJUsuario() {
		return numeroCNPJUsuario;
	}

	public void setNumeroCNPJUsuario(String numeroCNPJUsuario) {
		this.numeroCNPJUsuario = numeroCNPJUsuario;
	}

	@Column(name = "id_processo_trf", insertable = false, updatable = false)
	public int getIdProcessoTrf() {
		return idProcessoTrf;
	}

	public void setIdProcessoTrf(int idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}

	@Column(name = "cd_evento", insertable = false, updatable = false)
	public String getCodigoEvento() {
		return codigoEvento;
	}

	public void setCodigoEvento(String codigoEvento) {
		this.codigoEvento = codigoEvento;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_atualizacao", insertable = false, updatable = false)
	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

}
