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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.TipoCausaAbsolvicaoSumariaEnum;
import br.jus.pje.nucleo.enums.TipoExtincaoPunibilidadeEnum;

@Entity
@Table(name = "tb_icr_sent_absol_sumaria")
@PrimaryKeyJoinColumn(name = "id_icr_sntnca_abslvcao_sumaria")
public class IcrSentencaAbsSumaria extends InformacaoCriminalRelevante implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	private PessoaMagistrado orgao;
	private Date dtPublicacao;
	private TipoCausaAbsolvicaoSumariaEnum inTipoCausaAbsolvicaoSumaria;
	private TipoExtincaoPunibilidadeEnum tipoExtincao;

	public IcrSentencaAbsSumaria(){
		//
	}

	public IcrSentencaAbsSumaria(InformacaoCriminalRelevante icr){
		super(icr);
	}

	@Column(name = "in_tipo_causa_absolvicao", nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.TipoCausaAbsolvicaoSumariaType")
	public TipoCausaAbsolvicaoSumariaEnum getInTipoCausaAbsolvicaoSumaria(){
		return inTipoCausaAbsolvicaoSumaria;
	}

	public void setInTipoCausaAbsolvicaoSumaria(TipoCausaAbsolvicaoSumariaEnum inTipoCausaAbsolvicaoSumaria){
		this.inTipoCausaAbsolvicaoSumaria = inTipoCausaAbsolvicaoSumaria;
	}

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "id_orgao", nullable = false)
	public PessoaMagistrado getOrgao(){
		return orgao;
	}

	public void setOrgao(PessoaMagistrado orgao){
		this.orgao = orgao;
	}

	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "dt_publicacao", nullable = false)
	public Date getDtPublicacao(){
		return dtPublicacao;
	}

	public void setDtPublicacao(Date dtPublicacao){
		this.dtPublicacao = dtPublicacao;
	}

	public void setTipoExtincao(TipoExtincaoPunibilidadeEnum tipoExtincao){
		this.tipoExtincao = tipoExtincao;
	}

	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.TipoExtincaoPunibilidadeType")
	@Column(name = "in_tipo_extincao")
	public TipoExtincaoPunibilidadeEnum getTipoExtincao(){
		return tipoExtincao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getDtPublicacao() == null) ? 0 : dtPublicacao.hashCode());
		result = prime * result
				+ ((getInTipoCausaAbsolvicaoSumaria() == null) ? 0 : inTipoCausaAbsolvicaoSumaria.hashCode());
		result = prime * result + ((getOrgao() == null) ? 0 : orgao.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (obj instanceof IcrSentencaAbsSumaria)
			return false;
		IcrSentencaAbsSumaria other = (IcrSentencaAbsSumaria) obj;
		if (getDtPublicacao() == null) {
			if (other.getDtPublicacao() != null)
				return false;
		} else if (!dtPublicacao.equals(other.getDtPublicacao()))
			return false;
		if (getInTipoCausaAbsolvicaoSumaria() == null) {
			if (other.getInTipoCausaAbsolvicaoSumaria() != null)
				return false;
		} else if (!inTipoCausaAbsolvicaoSumaria.equals(other.getInTipoCausaAbsolvicaoSumaria()))
			return false;
		if (getOrgao() == null) {
			if (other.getOrgao() != null)
				return false;
		} else if (!orgao.equals(other.getOrgao()))
			return false;
		return true;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrSentencaAbsSumaria.class;
	}
}
