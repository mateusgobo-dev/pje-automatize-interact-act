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
import br.jus.pje.nucleo.enums.TipoExtincaoPunibilidadeEnum;

@Entity
@Table(name = "tb_icr_dis_ext_puniblidade")
@PrimaryKeyJoinColumn(name = "id_icr_dis_extnco_punibilidade")
public class IcrDecisaoSuperiorExtincaoDaPunibilidade extends InformacaoCriminalRelevante implements Serializable{

	private static final long serialVersionUID = -5031716863047329276L;
	private PessoaMagistrado relator;
	private Date dataPublicacao;
	private String numeroProcessoInstanciaSuperior;
	private InformacaoCriminalRelevante icrAfetada;
	private EfeitoSobreSentencaAnteriorEnum efeito;
	private TipoExtincaoPunibilidadeEnum tipoExtincao;
	// ver RN90
	private static TipoIcrEnum[] tiposDeIcrAceitos = {TipoIcrEnum.SAP, TipoIcrEnum.SAI, TipoIcrEnum.SEP,
			TipoIcrEnum.SCO, TipoIcrEnum.SAS, TipoIcrEnum.SPR, TipoIcrEnum.SEI};

	public IcrDecisaoSuperiorExtincaoDaPunibilidade(){
	}

	public IcrDecisaoSuperiorExtincaoDaPunibilidade(InformacaoCriminalRelevante icr){
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
	@JoinColumn(name = "id_icr_vinculada", nullable = false)
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getDataPublicacao() == null) ? 0 : dataPublicacao.hashCode());
		result = prime * result + ((getIcrAfetada() == null) ? 0 : icrAfetada.hashCode());
		result = prime * result
				+ ((getNumeroProcessoInstanciaSuperior() == null) ? 0 : numeroProcessoInstanciaSuperior.hashCode());
		result = prime * result + ((getRelator() == null) ? 0 : relator.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof IcrDecisaoSuperiorExtincaoDaPunibilidade))
			return false;
		IcrDecisaoSuperiorExtincaoDaPunibilidade other = (IcrDecisaoSuperiorExtincaoDaPunibilidade) obj;
		if (getDataPublicacao() == null) {
			if (other.getDataPublicacao() != null)
				return false;
		} else if (!dataPublicacao.equals(other.getDataPublicacao()))
			return false;
		if (getRelator() == null) {
			if (other.getRelator() != null)
				return false;
		} else if (!relator.equals(other.getRelator()))
			return false;
		if (getNumeroProcessoInstanciaSuperior() == null) {
			if (other.getNumeroProcessoInstanciaSuperior() != null)
				return false;
		} else if (!numeroProcessoInstanciaSuperior.equals(other.getNumeroProcessoInstanciaSuperior()))
			return false;
		if (getIcrAfetada() == null) {
			if (other.getIcrAfetada() != null)
				return false;
		} else if (!icrAfetada.equals(other.getIcrAfetada()))
			return false;
		return true;
	}

	@Transient
	public static TipoIcrEnum[] getTiposDeIcrAceitos(){
		return tiposDeIcrAceitos;
	}

	public void setEfeito(EfeitoSobreSentencaAnteriorEnum efeito){
		this.efeito = efeito;
	}

	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.EfeitoSobreSentencaAnteriorType")
	@Column(name = "in_efeito", nullable = false)
	public EfeitoSobreSentencaAnteriorEnum getEfeito(){
		return efeito;
	}

	public void setTipoExtincao(TipoExtincaoPunibilidadeEnum tipoExtincao){
		this.tipoExtincao = tipoExtincao;
	}

	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.TipoExtincaoPunibilidadeType")
	@Column(name = "in_tipo", nullable = false)
	public TipoExtincaoPunibilidadeEnum getTipoExtincao(){
		return tipoExtincao;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrDecisaoSuperiorExtincaoDaPunibilidade.class;
	}
}
