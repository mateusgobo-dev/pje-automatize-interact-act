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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;
import br.jus.pje.nucleo.enums.EfeitoSobreSentencaAnteriorEnum;
import br.jus.pje.nucleo.enums.TipoMedidaSegurancaEnum;

@Entity
@Table(name = "tb_icr_dis_abs_impropria")
@PrimaryKeyJoinColumn(name = "id_icr_dis_abs_impropria")
public class IcrDecisaoSuperiorAbsolvicaoImpropria extends InformacaoCriminalRelevante implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	private PessoaMagistrado relator;
	private TipoMedidaSegurancaEnum inTipoMedidaSeguranca;
	private Date dtPublicacao;
	private Integer nrAnoPrazo;
	private Integer nrMesPrazo;
	private EfeitoSobreSentencaAnteriorEnum inEfeitoSobreSentencaAnterior;
	private String numeroProcessoInstanciaSuperior;
	private InformacaoCriminalRelevante icrAfetada;
	// ver RN90
	private static TipoIcrEnum[] tiposDeIcrAceitos = {TipoIcrEnum.SAP, TipoIcrEnum.SAI, TipoIcrEnum.SEP,
			TipoIcrEnum.SCO, TipoIcrEnum.SAS, TipoIcrEnum.SPR, TipoIcrEnum.SEI};

	public IcrDecisaoSuperiorAbsolvicaoImpropria(){
		//
	}

	public IcrDecisaoSuperiorAbsolvicaoImpropria(InformacaoCriminalRelevante icr){
		super(icr);
	}

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "id_relator", nullable = false)
	public PessoaMagistrado getRelator(){
		return relator;
	}

	public void setRelator(PessoaMagistrado relator){
		this.relator = relator;
	}

	@Column(name = "in_tipo_medida_seguranca", nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.TipoMedidaSegurancaType")
	public TipoMedidaSegurancaEnum getInTipoMedidaSeguranca(){
		return inTipoMedidaSeguranca;
	}

	public void setInTipoMedidaSeguranca(TipoMedidaSegurancaEnum inTipoMedidaSeguranca){
		this.inTipoMedidaSeguranca = inTipoMedidaSeguranca;
	}

	@Column(name = "in_efeito_sob_sentenca1grau", nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.EfeitoSobreSentencaAnteriorType")
	public EfeitoSobreSentencaAnteriorEnum getInEfeitoSobreSentencaAnterior(){
		return inEfeitoSobreSentencaAnterior;
	}

	public void setInEfeitoSobreSentencaAnterior(EfeitoSobreSentencaAnteriorEnum inEfeito){
		this.inEfeitoSobreSentencaAnterior = inEfeito;
	}

	public void setIcrAfetada(InformacaoCriminalRelevante icrAfetada){
		this.icrAfetada = icrAfetada;
	}

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "id_icr_vinculada", nullable = false)
	public InformacaoCriminalRelevante getIcrAfetada(){
		return icrAfetada;
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

	@Column(name = "nr_ano_prazo_minimo")
	@NotNull
	public Integer getNrAnoPrazo(){
		return nrAnoPrazo;
	}

	public void setNrAnoPrazo(Integer nrAnoPrazo){
		this.nrAnoPrazo = nrAnoPrazo;
	}

	@Column(name = "nr_mes_prazo_minimo")
	@NotNull
	public Integer getNrMesPrazo(){
		return nrMesPrazo;
	}

	public void setNrMesPrazo(Integer nrMesPrazo){
		this.nrMesPrazo = nrMesPrazo;
	}

	@Column(name = "num_processo_inst_sup")
	@NotNull
	public String getNumeroProcessoInstanciaSuperior(){
		return numeroProcessoInstanciaSuperior;
	}

	public void setNumeroProcessoInstanciaSuperior(String numeroProcessoInstanciaSuperior){
		this.numeroProcessoInstanciaSuperior = numeroProcessoInstanciaSuperior;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getDtPublicacao() == null) ? 0 : getDtPublicacao().hashCode());
		result = prime * result + ((getInTipoMedidaSeguranca() == null) ? 0 : getInTipoMedidaSeguranca().hashCode());
		result = prime * result + ((getNrAnoPrazo() == null) ? 0 : getNrAnoPrazo().hashCode());
		result = prime * result + ((getNrMesPrazo() == null) ? 0 : getNrMesPrazo().hashCode());
		result = prime * result + ((getRelator() == null) ? 0 : getRelator().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof IcrDecisaoSuperiorAbsolvicaoImpropria))
			return false;
		IcrDecisaoSuperiorAbsolvicaoImpropria other = (IcrDecisaoSuperiorAbsolvicaoImpropria) obj;
		if (getDtPublicacao() == null) {
			if (other.getDtPublicacao() != null)
				return false;
		} else if (!getDtPublicacao().equals(other.getDtPublicacao()))
			return false;
		if (getInTipoMedidaSeguranca() != other.getInTipoMedidaSeguranca())
			return false;
		if (getNrAnoPrazo() == null) {
			if (other.getNrAnoPrazo() != null)
				return false;
		} else if (!getNrAnoPrazo().equals(other.getNrAnoPrazo()))
			return false;
		if (getNrMesPrazo() == null) {
			if (other.getNrMesPrazo() != null)
				return false;
		} else if (!getNrMesPrazo().equals(other.getNrMesPrazo()))
			return false;
		if (getRelator() == null) {
			if (other.getRelator() != null)
				return false;
		} else if (!getRelator().equals(other.getRelator()))
			return false;
		return true;
	}
	
	@Transient
	public static TipoIcrEnum[] getTiposDeIcrAceitos(){
		return tiposDeIcrAceitos;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrDecisaoSuperiorAbsolvicaoImpropria.class;
	}
}
