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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = RelatorioLog.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_relatorio_log", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_relatorio_log"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RelatorioLog implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RelatorioLog,Integer> {

	public static final String TABLE_NAME = "tb_relatorio_log";
	private static final long serialVersionUID = 1L;

	private int idRelatorioLog;
	private Usuario idUsuarioSolicitacao;
	private Date dataSolicitacao;
	private String descricao;

	public RelatorioLog() {
	}

	@Id
	@GeneratedValue(generator = "gen_relatorio_log")
	@Column(name = "id_relatorio_log", unique = true, nullable = false)
	public int getIdRelatorioLog() {
		return this.idRelatorioLog;
	}

	public void setIdRelatorioLog(int idRelatorioLog) {
		this.idRelatorioLog = idRelatorioLog;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_solicitacao", nullable = false)
	@NotNull
	public Usuario getIdUsuarioSolicitacao() {
		return idUsuarioSolicitacao;
	}

	public void setIdUsuarioSolicitacao(Usuario idUsuarioSolicitacao) {
		this.idUsuarioSolicitacao = idUsuarioSolicitacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_solicitacao", nullable = false)
	@NotNull
	public Date getDataSolicitacao() {
		return dataSolicitacao;
	}

	public void setDataSolicitacao(Date dataSolicitacao) {
		this.dataSolicitacao = dataSolicitacao;
	}

	@Column(name = "ds_relatorio", length = 100, nullable = false)
	@Length(max = 100)
	@NotNull
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RelatorioLog)) {
			return false;
		}
		RelatorioLog other = (RelatorioLog) obj;
		if (getIdRelatorioLog() != other.getIdRelatorioLog()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdRelatorioLog();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RelatorioLog> getEntityClass() {
		return RelatorioLog.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdRelatorioLog());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
