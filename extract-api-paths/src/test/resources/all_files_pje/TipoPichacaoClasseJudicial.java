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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.APTEnum;

@Entity
@Table(name = "tb_tp_pichacao_cl_judicial")
@org.hibernate.annotations.GenericGenerator(name = "gen_tp_pichacao_cl_judicial", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tp_pichacao_cl_judicial"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoPichacaoClasseJudicial implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoPichacaoClasseJudicial,Integer> {

	private static final long serialVersionUID = 1L;

	private int idTipoPichacaoClasseJudicial;
	private TipoPichacao tipoPichacao;
	private ClasseJudicial classeJudicial;

	private Date dataAtualizacao;
	private APTEnum poloPichacao;

	public TipoPichacaoClasseJudicial() {
	}

	@Id
	@GeneratedValue(generator = "gen_tp_pichacao_cl_judicial")
	@Column(name = "id_tp_pichacao_classe_judicial", unique = true, nullable = false)
	public int getIdTipoPichacaoClasseJudicial() {
		return this.idTipoPichacaoClasseJudicial;
	}

	public void setIdTipoPichacaoClasseJudicial(int idTipoPichacaoClasseJudicial) {
		this.idTipoPichacaoClasseJudicial = idTipoPichacaoClasseJudicial;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_tipo_pichacao", nullable = false)
	@NotNull
	public TipoPichacao getTipoPichacao() {
		return this.tipoPichacao;
	}

	public void setTipoPichacao(TipoPichacao tipoPichacao) {
		this.tipoPichacao = tipoPichacao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_classe_judicial", nullable = false)
	@NotNull
	public ClasseJudicial getClasseJudicial() {
		return this.classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_atualizacao")
	public Date getDataAtualizacao() {
		return this.dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	@Column(name = "in_polo_pichacao", length = 1)
	@Enumerated(EnumType.STRING)
	public APTEnum getPoloPichacao() {
		return this.poloPichacao;
	}

	public void setPoloPichacao(APTEnum poloPichacao) {
		this.poloPichacao = poloPichacao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoPichacaoClasseJudicial)) {
			return false;
		}
		TipoPichacaoClasseJudicial other = (TipoPichacaoClasseJudicial) obj;
		if (getIdTipoPichacaoClasseJudicial() != other.getIdTipoPichacaoClasseJudicial()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoPichacaoClasseJudicial();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoPichacaoClasseJudicial> getEntityClass() {
		return TipoPichacaoClasseJudicial.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoPichacaoClasseJudicial());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
