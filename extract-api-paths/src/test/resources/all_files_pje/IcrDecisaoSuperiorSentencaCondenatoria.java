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

import org.apache.commons.beanutils.BeanUtils;

import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;
import br.jus.pje.nucleo.enums.EfeitoSobreSentencaAnteriorEnum;

@Entity
@Table(name = "tb_icr_dis_sent_condntoria")
@PrimaryKeyJoinColumn(name = "id_icr_dis_sntnca_condenatoria")
public class IcrDecisaoSuperiorSentencaCondenatoria extends InformacaoCriminalRelevante{

	private static final long serialVersionUID = -474650557185454685L;
	private PessoaMagistrado relator;
	private Date dataPublicacao;
	private String numeroProcessoInstanciaSuperior;
	private InformacaoCriminalRelevante icrAfetada;
	private EfeitoSobreSentencaAnteriorEnum inEfeitoSobreSentencaAnterior;
	private static TipoIcrEnum[] tiposDeIcrAceitos = {TipoIcrEnum.SCO};

	@Transient
	public static TipoIcrEnum[] getTiposDeIcrAceitos(){
		return tiposDeIcrAceitos;
	}

	public IcrDecisaoSuperiorSentencaCondenatoria(){
	}

	public IcrDecisaoSuperiorSentencaCondenatoria(InformacaoCriminalRelevante icr){
		copiarPropriedadesIcr(icr);
	}

	public void setIcr(InformacaoCriminalRelevante icr){
		copiarPropriedadesIcr(icr);
	}

	private void copiarPropriedadesIcr(InformacaoCriminalRelevante icr){
		try{
			BeanUtils.copyProperties(this, icr);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "id_relator", nullable = false)
	public PessoaMagistrado getRelator(){
		return relator;
	}

	public void setRelator(PessoaMagistrado relator){
		this.relator = relator;
	}

	public void setIcrAfetada(InformacaoCriminalRelevante icrAfetada){
		this.icrAfetada = icrAfetada;
	}

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "id_icr_afetada", nullable = false)
	public InformacaoCriminalRelevante getIcrAfetada(){
		return icrAfetada;
	}

	public void setDataPublicacao(Date dataDecisao){
		this.dataPublicacao = dataDecisao;
	}

	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "dt_publicacao", nullable = false)
	public Date getDataPublicacao(){
		return dataPublicacao;
	}

	public void setNumeroProcessoInstanciaSuperior(String numeroProcessoInstanciaSuperior){
		this.numeroProcessoInstanciaSuperior = numeroProcessoInstanciaSuperior;
	}

	@NotNull
	@Column(name = "num_processo_inst_sup", nullable = false)
	public String getNumeroProcessoInstanciaSuperior(){
		return numeroProcessoInstanciaSuperior;
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

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrDecisaoSuperiorSentencaCondenatoria.class;
	}
}
