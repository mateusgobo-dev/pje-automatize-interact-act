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

import javax.persistence.CascadeType;
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

import br.jus.pje.nucleo.enums.SituacaoPublicacaoDiarioEnum;
import br.jus.pje.nucleo.enums.TipoPesquisaDJEEnum;


@Entity
@Table(name = PublicacaoDiarioEletronico.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_publicacao_diario", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_publicacao_diario"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PublicacaoDiarioEletronico implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PublicacaoDiarioEletronico,Integer> {

	public static final String TABLE_NAME = "tb_publicacao_diario";
	private static final long serialVersionUID = 1L;

	private int idPublicacaoDiario;
	private ProcessoParteExpediente processoParteExpediente;
	private SituacaoPublicacaoDiarioEnum situacao;
	private String reciboPublicacaoDiarioEletronico;
	private Date dtExpectativaPublicacao;
	private Date dtPublicacao;
	private Date dtUltimaVerificacao;
	private Integer qtdVerificacoes;
	private TipoPesquisaDJEEnum tipoUltimaPesquisa;

	public PublicacaoDiarioEletronico() {
	}

	@Id
	@GeneratedValue(generator = "gen_publicacao_diario")
	@Column(name = "id_publicacao_diario", unique = true, nullable = false)
	public int getIdPublicacaoDiario() {
		return idPublicacaoDiario;
	}

	public void setIdPublicacaoDiario(int idPublicacaoDiario) {
		this.idPublicacaoDiario = idPublicacaoDiario;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.REMOVE)
	@JoinColumn(name = "id_processo_parte_expediente", nullable = false)
	@NotNull
	public ProcessoParteExpediente getProcessoParteExpediente() {
		return processoParteExpediente;
	}

	public void setProcessoParteExpediente(ProcessoParteExpediente processoParteExpediente) {
		this.processoParteExpediente = processoParteExpediente;
	}

	@Column(name = "in_situacao", length = 1)
	@Enumerated(EnumType.STRING)
	@NotNull
	public SituacaoPublicacaoDiarioEnum getSituacao() {
		return situacao;
	}

	public void setSituacao(SituacaoPublicacaoDiarioEnum situacao) {
		this.situacao = situacao;
	}
	
	
	@Column(name = "tp_ultima_pesquisa")
	@Enumerated(EnumType.STRING)
	public TipoPesquisaDJEEnum getTipoUltimaPesquisa() {
		return tipoUltimaPesquisa;
	}

	public void setTipoUltimaPesquisa(TipoPesquisaDJEEnum tipoUltimaPesquisa) {
		this.tipoUltimaPesquisa = tipoUltimaPesquisa;
	}

	@Column(name = "ds_recibo_dje")
	public String getReciboPublicacaoDiarioEletronico() {
		return reciboPublicacaoDiarioEletronico;
	}

	public void setReciboPublicacaoDiarioEletronico(String reciboPublicacaoDiarioEletronico) {
		this.reciboPublicacaoDiarioEletronico = reciboPublicacaoDiarioEletronico;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_expectativa_publicacao")
	public Date getDtExpectativaPublicacao() {
		return dtExpectativaPublicacao;
	}

	public void setDtExpectativaPublicacao(Date dtExpectativaPublicacao) {
		this.dtExpectativaPublicacao = dtExpectativaPublicacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_publicacao")
	public Date getDtPublicacao() {
		return dtPublicacao;
	}

	public void setDtPublicacao(Date dtPublicacao) {
		this.dtPublicacao = dtPublicacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ultima_verificacao")
	public Date getDtUltimaVerificacao() {
		return dtUltimaVerificacao;
	}

	public void setDtUltimaVerificacao(Date dtUltimaVerificacao) {
		this.dtUltimaVerificacao = dtUltimaVerificacao;
	}

	@Column(name = "qt_verificacoes")
	@NotNull
	public Integer getQtdVerificacoes() {
		return qtdVerificacoes;
	}

	public void setQtdVerificacoes(Integer qtdVerificacoes) {
		this.qtdVerificacoes = qtdVerificacoes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idPublicacaoDiario;
		result = prime * result + ((processoParteExpediente == null) ? 0 : processoParteExpediente.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PublicacaoDiarioEletronico other = (PublicacaoDiarioEletronico) obj;
		if (idPublicacaoDiario != other.idPublicacaoDiario)
			return false;
		if (processoParteExpediente == null) {
			if (other.processoParteExpediente != null)
				return false;
		} else if (!processoParteExpediente.equals(other.processoParteExpediente))
			return false;
		return true;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends PublicacaoDiarioEletronico> getEntityClass() {
		return PublicacaoDiarioEletronico.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPublicacaoDiario());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
}
