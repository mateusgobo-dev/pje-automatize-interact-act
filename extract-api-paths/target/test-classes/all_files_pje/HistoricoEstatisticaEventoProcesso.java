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

@Entity
@Table(name = HistoricoEstatisticaEventoProcesso.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_hist_est_evento_processo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_hist_est_evento_processo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class HistoricoEstatisticaEventoProcesso implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<HistoricoEstatisticaEventoProcesso,Integer> {

	public static final String TABLE_NAME = "tb_hist_est_evento_proc";
	private static final long serialVersionUID = 1L;

	private int idHistoricoEstatisticaEventoProcesso;
	private SecaoJudiciaria secaoJudiciaria;
	private Date dtUltimaAtualizacao;

	public HistoricoEstatisticaEventoProcesso() {
	}

	@Id
	@GeneratedValue(generator = "gen_hist_est_evento_processo")
	@Column(name = "id_hist_est_evento_processo", unique = true, nullable = false)
	public int getIdHistoricoEstatisticaEventoProcesso() {
		return this.idHistoricoEstatisticaEventoProcesso;
	}

	public void setIdHistoricoEstatisticaEventoProcesso(int idHistoricoEstatisticaEventoProcesso) {
		this.idHistoricoEstatisticaEventoProcesso = idHistoricoEstatisticaEventoProcesso;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cd_estado", nullable = false)
	@NotNull
	public SecaoJudiciaria getSecaoJudiciaria() {
		return secaoJudiciaria;
	}

	public void setSecaoJudiciaria(SecaoJudiciaria secaoJudiciaria) {
		this.secaoJudiciaria = secaoJudiciaria;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ultima_atualizacao", nullable = false)
	public Date getDtUltimaAtualizacao() {
		return this.dtUltimaAtualizacao;
	}

	public void setDtUltimaAtualizacao(Date dtUltimaAtualizacao) {
		this.dtUltimaAtualizacao = dtUltimaAtualizacao;
	}

	@Override
	public String toString() {
		return secaoJudiciaria.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof HistoricoEstatisticaEventoProcesso)) {
			return false;
		}
		HistoricoEstatisticaEventoProcesso other = (HistoricoEstatisticaEventoProcesso) obj;
		if (getIdHistoricoEstatisticaEventoProcesso() != other.getIdHistoricoEstatisticaEventoProcesso()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdHistoricoEstatisticaEventoProcesso();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends HistoricoEstatisticaEventoProcesso> getEntityClass() {
		return HistoricoEstatisticaEventoProcesso.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdHistoricoEstatisticaEventoProcesso());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
